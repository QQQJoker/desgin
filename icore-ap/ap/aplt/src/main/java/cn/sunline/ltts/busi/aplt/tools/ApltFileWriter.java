package cn.sunline.ltts.busi.aplt.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.sunline.adp.cedar.base.util.UnicodeReader;
import cn.sunline.edsp.base.file.FileLock;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.file.FileUtil;
import cn.sunline.edsp.base.util.lang.StringUtil;

public class ApltFileWriter {

	private static final Logger log = LogManager.getLogger(ApltFileWriter.class);
	
	
	private static final String NOTEXIS = "] doesn't exist!";
	public static final String DEFAULT_ENCODING = "UTF-8";
	private String encoding;
	private String path;
	private String fileName;
	private final int FLUSH_SIZE_IN_WRITE;
	private int size;
	private File file;
	private BufferedWriter writer;
	private BufferedReader reader;
	private boolean deleteWhenExists;
	private boolean lock;
	private FileLock fileLock;

	/**
	 * 默认构造方法
	 * @param path
	 * @param fileName
	 */
	public ApltFileWriter(String path, String fileName) {
		this(path, fileName, DEFAULT_ENCODING, 500);
	}

	public ApltFileWriter(String path, String fileName, String encoding) {
		this(path, fileName, encoding, 500);
	}

	public ApltFileWriter(String path, String fileName, int flushSize) {
		this(path, fileName, DEFAULT_ENCODING, flushSize);
	}

	public ApltFileWriter(String path, String fileName, String encoding, int flushSize) {
		this.deleteWhenExists = true;
		this.fileLock = null;
		if (path != null) {
			File f = new File(path);
			if (!f.exists()) {
				f.mkdirs();
			}
		}

		String fullPath = FileUtil.getFullPath(path, fileName);
		this.path = FileUtil.getFileDir(fullPath);
		this.fileName = FileUtil.getFileName(fullPath);
		this.encoding = encoding;
		this.size = 0;
		this.FLUSH_SIZE_IN_WRITE = flushSize;
	}

	public void write(String content) {
		try {
			this.getWriter().write(content + SystemUtils.LINE_SEPARATOR);
			++this.size;
			if (this.size % this.FLUSH_SIZE_IN_WRITE == 0) {
				this.getWriter().flush();
			}

		} catch (IOException var3) {
			throw new RuntimeException(var3);
		}
	}

	public void writeLastLine(String content) {
		try {
			this.getWriter().write(content);
			++this.size;
			if (this.size % this.FLUSH_SIZE_IN_WRITE == 0) {
				this.getWriter().flush();
			}

		} catch (IOException var3) {
			throw new RuntimeException(var3);
		}
	}

	public String read() {
		try {
			return this.getReader().readLine();
		} catch (IOException var2) {
			throw new RuntimeException(var2);
		}
	}

	
	/**
	 * 打开一个写文件的句柄
	 * @param forWrite
	 * @param append
	 */
	public void openWrite(boolean append) {
		this.open(true,append, false);
	}
	
	/**
	 * 打开一个读文件的句柄
	 * @param forWrite
	 * @param append
	 */
	
	public void openReader() {
		this.open(false, false, false);
	}

	/**
	 * 在第一行插入
	 */
	public void writeFirstLine(String content) {
		this.file = this.fileEmpty();
		RandomAccessFile accessFile = null;
		try {
			accessFile = new RandomAccessFile(this.file, "rw");
			accessFile.seek(0);
			accessFile.writeBytes(content);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File [" + this.file.getAbsolutePath() + "] doesn't exist!");
		} catch (IOException e) {
			throw new RuntimeException("File [" + this.file.getAbsolutePath() + "] doesn't exist!");
		} finally {
			if (accessFile != null) {
				try {
					accessFile.close();
				} catch (IOException e) {
					throw new RuntimeException("File [" + this.file.getAbsolutePath() + "] doesn't exist!");
				}
			}
		}
	}
	
	private void open(boolean forWrite, boolean append, boolean lock) {
		this.lock = lock;
		this.size = 0;
		this.file = this.fileEmpty();
		this.lockFile();
		this.fileWrite(forWrite, append);
		this.fileRead(forWrite);
	}

	private void fileRead(boolean forWrite) {
		if (this.reader == null && !forWrite) {
			try {
				if (!this.file.exists()) {
					throw new FileNotFoundException("File [" + this.file.getAbsolutePath() + "] doesn't exist!");
				}

				this.reader = new BufferedReader(new UnicodeReader(new FileInputStream(this.file), this.encoding));
			} catch (FileNotFoundException var3) {
				throw new RuntimeException("File [" + this.file.getAbsolutePath() + "] doesn't exist!", var3);
			}
		}

	}

	private void fileWrite(boolean forWrite, boolean append) {
		if (this.writer == null && forWrite) {
			if (!this.file.exists()) {
				FileUtil.createFile(this.file.getAbsolutePath(), this.deleteWhenExists);
			}
			try {
				this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file, append), this.encoding), 1024);
			} catch (FileNotFoundException var3) {
				throw new RuntimeException("File [" + this.file.getAbsolutePath() + "] doesn't exist!", var3);
			} catch (UnsupportedEncodingException var4) {
				throw new RuntimeException(var4);
			}
		}

	}

	private File fileEmpty() {
		File file = null;
		if (StringUtil.isEmpty(this.path)) {
			(new File(this.fileName.substring(0, this.fileName.lastIndexOf(File.separatorChar)))).mkdirs();
			file = new File(this.fileName);
		} else {
			file = new File(this.path, this.fileName);
		}

		return file;
	}

	private void lockFile() {
		if (this.lock) {
			this.fileLock = new FileLock(new File(this.file.getAbsolutePath() + ".lock"));
			if (!this.fileLock.tryLock()) {
				throw ExceptionUtil.wrapThrow(
						this.file.getAbsolutePath() + " isn't allowed to read-write concurrently!", new String[0]);
			}
		}
	}

	private void unlockFile() {
		if (this.lock) {
			if (this.fileLock != null) {
				this.fileLock.release();
			}

			(new File(this.file.getAbsolutePath() + ".lock")).delete();
		}
	}

	public void close() {
		this.unlockFile();
		if (this.reader != null) {
			try {
				this.reader.close();
			} catch (IOException var3) {
				log.error(var3.getMessage(), var3);
			}

			this.reader = null;
		}

		if (this.writer != null) {
			try {
				this.writer.close();
			} catch (IOException var2) {
				log.error(var2.getMessage(), var2);
			}

			this.writer = null;
		}

		this.file = null;
	}

	public void remove() {
		if (this.file == null) {
			this.file = new File(this.path, this.fileName);
		}

		this.file.delete();
		this.file = null;
	}

	public String getDataFileName() {
		return this.fileName;
	}

	public String getDataFilePath() {
		return this.file.getAbsolutePath();
	}

	private BufferedWriter getWriter() {
		return this.writer;
	}

	private BufferedReader getReader() {
		return this.reader;
	}

	public int getSize() {
		return this.size;
	}

	public boolean isDeleteWhenExists() {
		return this.deleteWhenExists;
	}

	public void setDeleteWhenExists(boolean deleteWhenExists) {
		this.deleteWhenExists = deleteWhenExists;
	}
}
