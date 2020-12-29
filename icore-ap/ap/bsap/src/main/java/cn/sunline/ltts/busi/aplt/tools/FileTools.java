package cn.sunline.ltts.busi.aplt.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.oss.api.OssFactory;
import cn.sunline.clwj.oss.model.MsTransferFileInfo;
import cn.sunline.clwj.oss.spi.MsTransfer;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.file.FileProcessor;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.file.FileUtil;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author sunliang
 *         <p>
 *         <li>2014年6月20日-下午5:37:22</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228T：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */

public class FileTools {


	public static final String FILE_ID = "fileId";

	public static final String MD5 = "md5";

	/**
	 * 读文件
	 * 
	 * @param fileName
	 *            文件名称（带路径）
	 * @param executor
	 *            单行记录处理器
	 * @return 文件行数 注：本方法是一个基于文件遍历器的读文件api
	 * 
	 *     
	 *
	 */
	public static int readFile(String fileName, FileDataExecutor executor) {
		return FileUtil.readFile(fileName, executor);
	}

	
	/**
	 * 带数据库事务的读文件
	 * @param fileName
	 * @param executor
	 * @return
	 */
	public static <T> int readFileWithTrans(String fileName, FileDataExecutorWithTrans<T> executor) {
			FileProcessor processor = new FileProcessor(null, fileName);
			processor.open(false);
			int index = 1;
			try {
				while(true) {
					String line = processor.read();
					if (line == null)
						break;
					executor.process(index, line);
					index ++;
					
				}
				
				executor.after();
			} catch (Exception e) {
				DaoUtil.rollbackTransaction();
				DaoUtil.beginTransaction();
				throw ExceptionUtil.wrapThrow(e);
			} finally {
				processor.close();
			}
			
			return index;
	}
	

	/**
	 * 文件下载
	 * 
	 */
	public static String download(MsTransferFileInfo downFile) {
		MsTransfer transfer = getFileTransfer();
		transfer.download(downFile);
		return downFile.getLocalFile().getFileFullName();
	}

	/**
	 * 文件上传
	 * 
	 */
	public static String upload(MsTransferFileInfo uploadFile) {
		getFileTransfer().upload(uploadFile);
		return uploadFile.getRemoteFile().getFileFullName();
	}

	private static MsTransfer getFileTransfer() {
		return OssFactory.get().create();
	}

	/**
	 * 获取文件全路径
	 * 
	 * @param rootDir
	 *            根目录
	 * @param fileName
	 *            可带相对路径的文件名
	 * @return
	 */
	public static String getFileFullPath(String rootDir, String fileName) {
		return FileUtil.getFullPath(rootDir, fileName);
	}

	/**
	 * 获取文件名称
	 * 
	 * @param fullPath
	 *            带文件名的文件全路径
	 * @return
	 */
	public static String getFileName(String fullPath) {
		return FileUtil.getFileName(fullPath);
	}

	/**
	 * 获取文件目录
	 * 
	 * @param fullPath
	 *            带文件名的文件全路径
	 * @return
	 */
	public static String getFileDir(String fullPath) {
		return FileUtil.getFileDir(fullPath);
	}

	/**
	 * 创建文件目录
	 * 
	 * @param dir
	 *            文件目录
	 * @return
	 * @throws IOException 
	 */
	public static void createDir(String dir) throws IOException {
		FileUtil.createDir(dir);
	}

	/**
	 * @Author yuhch
	 *         <p>
	 *         <li>2014年12月12日-下午5:29:27</li>
	 *         <li>功能说明：文件操作本地根目录获取</li>
	 *         </p>
	 * @return
	 */
	public static String getFileHome() {
		return getFileTransfer().getLocalkPath();
	}

	/**
	 * 文件处理器，可用于文件的写操作
	 * 
	 * @author caiqq
	 * 
	 *         <pre>
	 * // 文件处理创建
	 * LttsFileWriter file = new LttsFileWriter(&quot;/home/ltts/apps/&quot;, &quot;cm/dfdk.txt&quot;);
	 * // 打开文件
	 * file.open();
	 * try {
	 * 	// 写文件
	 * 	file.write(&quot;abcdeftgggg&quot;);
	 * }
	 * finally {
	 * 	// 关闭文件
	 * 	file.close();
	 * }
	 * </pre>
	 */
	public static class LttsFileWriter {
		private final FileProcessor fileProcess;

		public LttsFileWriter(String path, String fileName) {
			fileProcess = new FileProcessor(path, fileName);
		}

		public LttsFileWriter(String path, String fileName, String encoding) {
			fileProcess = new FileProcessor(path, fileName, encoding);
		}

		public LttsFileWriter(String path, String fileName, int flushSize) {
			fileProcess = new FileProcessor(path, fileName, flushSize);
		}

		public LttsFileWriter(String path, String fileName, String encoding, int flushSize) {
			fileProcess = new FileProcessor(path, fileName, encoding, flushSize);
		}

		public void write(String content) {
			fileProcess.write(content);
		}
		
		public void writeLastLine(String content) {
			fileProcess.writeLastLine(content);
		}

		/**
		 * 打开文件
		 */
		public void open() {
			fileProcess.open(true);
		}

		/**
		 * 关闭文件
		 */
		public void close() {
			fileProcess.close();
		}

		/**
		 * 删除文件
		 */
		public void remove() {
			fileProcess.remove();
		}

		/**
		 * 获取文件名
		 */
		public String getDataFileName() {
			return fileProcess.getDataFileName();
		}

		/**
		 * 获取路径
		 */
		public String getDataFilePath() {
			return fileProcess.getDataFilePath();
		}

		/**
		 * 获取已写的文件长度
		 */
		public int getSize() {
			return fileProcess.getSize();
		}

	}
	
	/**
	 * 支持cache多行进行处理（可用于GNS批量查询）和多笔一次数据库提交的功能。
	 *
	 * @param <T> 数据项
	 */
	public static abstract class FileDataExecutorWithTrans<T> implements FileDataExecutor {
	    private final List<T> lineCache = new ArrayList<T>(); // 记录临时缓存
	    private final int cacheSize; // 缓存大小
	    private final int commitInterval; // 事务提交间隔
	    private int start = 0;
	    
		public FileDataExecutorWithTrans() {
		    this(100, 10);
		}
		
		public FileDataExecutorWithTrans(int commitInterval) {
		    this(100, commitInterval);
		}
		
		/**
		 * 
		 * @param cacheSize  缓存大小
		 * @param commitInterval 事务提交间隔
		 */
		public FileDataExecutorWithTrans(int cacheSize, int commitInterval) {
		    this.cacheSize = (cacheSize == 0 ? 1 : cacheSize);
	        this.commitInterval = commitInterval;
		}
		

	    
	    final public void before() {
	        if (hasTransaction()) 
	            DaoUtil.beginTransaction(); //开始事务
	    }
	    
	    @Override
	    final public void process(int index, String line) {
	        T t = parseLine(index, line);
	        if (t == null)
	        	return;
	        
	        lineCache.add(t);
	        
	        if (lineCache.size() >= cacheSize) {
	            _process();
	        }
	    }
	    
	    final public void after() {
	        if (lineCache.size() != 0) {
	            _process();
	        }
	        if (hasTransaction()) {
	            // 2014.12.25 commit与begin要成对出现
	            DaoUtil.commitTransaction();
	            DaoUtil.beginTransaction();
	        }
	    }
	    
	    private void _process() {
	         List<T> items = buildLines(lineCache);
	         
        	 for (T item : items) {
        		 processLine(item);
        		 start ++ ;
        		 if (hasTransaction() && start % commitInterval == 0) {
        			 // 2014.12.25 commit与begin要成对出现
        			 DaoUtil.commitTransaction();
        			 DaoUtil.beginTransaction();
        		 }
        		 
        	 }
	        
	         lineCache.clear();
	    }
	    
	    private boolean hasTransaction() {
	        if (commitInterval > 0) 
	            return true;
	        return false;
	    }
	    
		/**
		 * 文件行解析，返回数据项对象。
		 * 
		 * @param line 行内容
		 * @return 数据项对象
		 */
		public abstract T parseLine(int index, String line);
		
		/**
		 * 批量处理行记录，通常用于批量查询GNS获取DCN编号并填充到数据项的DCN编号字段。
		 * 
		 * @param start 起始行号
		 * @param lines 缓存的数据项列表
		 * @return 新的数据项列表
		 */
		public abstract List<T> buildLines(List<T> lines);
		
		/**
		 * 每一行数据的处理，
		 * 
		 * @param index
		 * @param t
		 */
		public abstract void processLine(T t);
	}
	
}
