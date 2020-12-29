package cn.sunline.clwj.oss.impl.inner;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.sunline.clwj.oss.config.FtpConfig;
import cn.sunline.clwj.oss.config.FtpConfigLoader;
import cn.sunline.clwj.oss.config.FtpConnectionConfig;
import cn.sunline.clwj.oss.util.FileConstant;
import cn.sunline.clwj.oss.util.MsMD5Util;
import cn.sunline.clwj.oss.util.MsStringUtil;

public class FtpFileTransferClientImpl {

	private static final Logger bizlog = LogManager.getLogger(FtpFileTransferClientImpl.class);
	private boolean connReachable = false;

	private Boolean binaryMode = Boolean.valueOf(true);

	private FtpConfig config = null;

	private FtpConnectionConfig connectionConfig = null;

	private static Map<String, FtpConnectionConfig> configMap = new ConcurrentHashMap<String, FtpConnectionConfig>();

	public List<String> getLocalFileList(String localDir) {
		if( bizlog.isDebugEnabled() ) {
			bizlog.debug("getLocalFileList begin >>>>>>>>>>>>>>>>>>>>");
			bizlog.debug("localDir {}", localDir);
		}
		localDir = getFullPathName(getLocalHome(), localDir);
		Collection<File> files = FileUtils.listFiles(new File(localDir), null, false);
		if (files != null) {
			List<String> fileList = new ArrayList<String>();
			for (File file : files) {
				if (!".".equals(file.getName()) && !"..".equals(file.getName())) {
					fileList.add(file.getName());
				}
			}
			if( bizlog.isDebugEnabled() ) {
				bizlog.debug("getLocalFileList begin >>>>>>>>>>>>>>>>>>>>");
			}
			return fileList;
		}
		return null;
	}

	public void deleteLoal(String localFileName) {
		bizlog.debug("delete begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}", localFileName);
		localFileName = getFullPathName(getLocalHome(), localFileName);
		try {
			FileUtils.forceDelete(new File(localFileName));
		} catch (IOException e) {
			throw new RuntimeException("NfsFileTransferClientImpl.delete02, nfs delete file failed!", e);
		}

		bizlog.debug("delete end >>>>>>>>>>>>>>>>>>>>");
	}

	public void init(String configId) {

		if (config == null) {
			synchronized (configMap) {
				if (config == null) {
					config = FtpConfigLoader.getConfig();
					for (FtpConnectionConfig conn : config.getFtpConnectionConfigs()) {
						configMap.put(conn.getId(), conn);
					}
				}
			}
		}

		if (MsStringUtil.isEmpty(configId)) {
			configId = config.getDefaultConfigId();
		}

		connectionConfig = configMap.get(configId);
		if (MsStringUtil.isEmpty(connectionConfig.getLocalHome())) {
			connectionConfig.setLocalHome(config.getDefaultLocalPath());
		}
	}

	public void upload(String localFileName, String remoteFileName) {
		upload(localFileName, remoteFileName, true);
	}

	public void upload(String localFileName, String remoteFileName, boolean uploadOk) {
		bizlog.debug("FtpFileTransferClientImpl.upload begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}, remoteFileName {}, uploadOk {}", localFileName, remoteFileName, uploadOk);
		remoteFileName = getFullPathName(getRemoteHome(), remoteFileName);
		localFileName = getFullPathName(getLocalHome(), localFileName);
		uploadFileToRemote(localFileName, remoteFileName);
		if (uploadOk) {
			File file = new File(localFileName);
			String MD5 = "";
			try {
				MD5 = MsMD5Util.getFileMD5String(file);
			} catch (IOException e) {
				throw new RuntimeException(
						"FtpFileTransferClientImpl.upload01, while upload, failed to Generate MD5 by file ["
								+ file.getAbsolutePath() + "],error message [" + e.getMessage() + "]",
						e);
			}
			File okFile = new File(localFileName + FileConstant.FILE_OK);

			if (!okFile.exists()) {
				try {
					okFile.createNewFile();
					FileUtils.writeStringToFile(okFile, MD5);
				} catch (IOException e) {
					throw new RuntimeException(
							"FtpFileTransferClientImpl.upload02, while upload, failed to create OK file ["
									+ okFile.getAbsolutePath() + "],error message [" + e.getMessage() + "]",
							e);
				}
			}
			uploadFileToRemote(localFileName + FileConstant.FILE_OK, remoteFileName + FileConstant.FILE_OK);
		}
		bizlog.debug("FtpFileTransferClientImpl.upload end >>>>>>>>>>>>>>>>>>>>");
	}

	public String download(String localFileName, String remoteFileName) {
		return download(localFileName, remoteFileName, true);
	}

	public String download(String localFileName, String remoteFileName, boolean downloadOk) {
		bizlog.debug("download begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {},remoteFileName {}", localFileName, remoteFileName);

		remoteFileName = getFullPathName(getRemoteHome(), remoteFileName);
		localFileName = getFullPathName(getLocalHome(), localFileName);
		downloadFileToLocal(localFileName, remoteFileName);
		if (downloadOk) {
			String lcoalOkFileName = localFileName + FileConstant.FILE_OK;
			String remoteOkFileName = remoteFileName + FileConstant.FILE_OK;
			downloadFileToLocal(lcoalOkFileName, remoteOkFileName);
			try {
				String okMd5 = FileUtils.readFileToString(new File(lcoalOkFileName));
				String fileMd5 = MsMD5Util.getFileMD5String(new File(localFileName));
				if (!okMd5.equals(fileMd5)) {
					throw new RuntimeException("The file MD5 mismatch, the downloaded file is incomplete.");
				}
			} catch (IOException e) {
				throw new RuntimeException("FtpFileTransferClientImpl.download01, read MD5 is fail!", e);
			}
		}
		bizlog.debug("download end >>>>>>>>>>>>>>>>>>>>");
		return localFileName;
	}

	public void delete(String remoteFileName) {
		bizlog.debug(" FtpFileTransferClientImpl.delete begin >>>>>>>>>>>>>>>>");
		bizlog.debug("remoteFileName {}!", remoteFileName);
		long start = System.currentTimeMillis();

		remoteFileName = getFullPathName(getRemoteHome(), remoteFileName);
		FTPClient ftp = login();

		remoteFileName = getFileName(remoteFileName);
		try {
			boolean deleteInd = ftp.deleteFile(remoteFileName);
			if (true == deleteInd) {
				bizlog.info("Ftp host{},user{}, remote file {} delete sucess! using time %s ms。",
						new Object[] { connectionConfig.getServerIp(), connectionConfig.getUserName(), remoteFileName,
								Long.valueOf(System.currentTimeMillis() - start) });
			} else {
				bizlog.info("Ftp host{},user{}, remote file {} delete fail! using time %s ms。",
						new Object[] { connectionConfig.getServerIp(), connectionConfig.getUserName(), remoteFileName,
								Long.valueOf(System.currentTimeMillis() - start) });
			}
		} catch (NoSuchFileException e) {
			throw new RuntimeException("FtpFileTransferClientImpl.delete01, File delete failed! the remote file["
					+ remoteFileName + "] does not exist or can not be operated." + e.getMessage(), e);
		} catch (Exception e) {
			bizlog.error(FileConstant.SimpleFTPClient17, e, e.getMessage());
			throw new RuntimeException("FtpFileTransferClientImpl.delete02, Ftp delete file failed!", e);
		} finally {
			logoff(ftp);
		}
		bizlog.debug(" FtpFileTransferClientImpl.delete end >>>>>>>>>>>>>>>>");
	}

	public String getRemoteHome() {
		bizlog.debug("getRemoteHome begin >>>>>>>>>>>>>>>>>>>>");
		String file = connectionConfig.getRemoteHome();
		if (!MsStringUtil.isEmpty(file)) {
			file = file.replace('/', FileConstant.COMM_SEPARATOR);
			file = file.replace('\\', FileConstant.COMM_SEPARATOR);
			if (!isFileSeparator(file.charAt(file.length() - 1)))
				file = file + FileConstant.COMM_SEPARATOR;
		}
		bizlog.debug("remote file {}", file);
		bizlog.debug("getRemoteHome end >>>>>>>>>>>>>>>>>>>>");
		return file;
	}

	public String getLocalHome() {
		bizlog.debug("getLocalHome begin >>>>>>>>>>>>>>>>>>>>");
		String file = connectionConfig.getLocalHome();
		if (!MsStringUtil.isEmpty(file)) {
			file = file.replace('/', FileConstant.COMM_SEPARATOR);
			file = file.replace('\\', FileConstant.COMM_SEPARATOR);
			if (!isFileSeparator(file.charAt(file.length() - 1)))
				file = file + FileConstant.COMM_SEPARATOR;
		}
		bizlog.debug("Local file {}", file);
		bizlog.debug("getLocalHome end >>>>>>>>>>>>>>>>>>>>");
		return file;
	}

	public List<String> getRemoteFileList(String remoteDir) {
		// return getRemoList(remoteDir, FileConstant.FILE_OK);
		return getRemoList(remoteDir);
	}

	//
	// public List<String> getRemoteFileList(String remoteDir, String fileRegs) {
	// bizlog.debug(" FtpFileTransferClientImpl.getRemoteFileList begin
	// >>>>>>>>>>>>>>>>");
	//
	// List<String> fileList = getRemoList(remoteDir, fileRegs);
	// bizlog.debug("Remote file list{},fileRegs:{}", fileList, fileRegs);
	//
	// bizlog.debug(" FtpFileTransferClientImpl.getRemoteFileList end
	// <<<<<<<<<<<<<<<<");
	// return fileList;
	// }

	public boolean toReachable() {
		return connReachable;
	}

	private FTPClient login() {
		FTPClient ftp = new FTPClient();
		try {
			connect(ftp, connectionConfig.getRetryTime());

			ftp.setSoTimeout(connectionConfig.getDataTimeoutInMs());
			ftp.setControlEncoding(FileConstant.REMOTE_ENCODING_ISO);

			if (!ftp.login(connectionConfig.getUserName(), connectionConfig.getPassword())) {
				throw new RuntimeException("FtpFileTransferClientImpl.download01, Logon FTP server failed：user="
						+ connectionConfig.getUserName() + ",password=" + connectionConfig.getPassword());
			}
			if (MsStringUtil.isNotEmpty(connectionConfig.getRemoteHome())
					&& !ftp.changeWorkingDirectory(connectionConfig.getRemoteHome())) {
				throw new RuntimeException(
						"FtpFileTransferClientImpl.download02, FTP server working directory change failed："
								+ connectionConfig.getRemoteHome());
			}
			if ((binaryMode != null) && (!binaryMode.booleanValue())) {
				if (!ftp.setFileType(0)) {
					throw new RuntimeException(
							"FtpFileTransferClientImpl.download03, FTP file transfer type setting failed：0");
				}
			} else if (!ftp.setFileType(2)) {
				throw new RuntimeException(
						"FtpFileTransferClientImpl.download04, FTP file transfer type setting failed：2");
			}
			connReachable = true;
			ftp.setBufferSize(1048576);
		} catch (SocketException e) {
			connReachable = false;
			throw new RuntimeException("FtpFileTransferClientImpl.download05,FTP connection error", e);
		} catch (IOException e) {
			connReachable = false;
			throw new RuntimeException("FtpFileTransferClientImpl.download06,FTP connection error", e);
		}
		return ftp;
	}

	private void connect(FTPClient ftp, int retryTime) {
		int retryCount = 0;
		try {
			ftp.setDefaultTimeout(connectionConfig.getConnTimeoutInMs());
			ftp.setDataTimeout(connectionConfig.getDataTimeoutInMs());
			ftp.setConnectTimeout(connectionConfig.getConnTimeoutInMs());
			ftp.connect(connectionConfig.getServerIp(), connectionConfig.getServerPort());
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				throw new RuntimeException(
						"FtpFileTransferClientImpl.connect01, Can't Connect to :" + connectionConfig.getServerIp());
			}

			return;
		} catch (Exception e) {
			while (retryCount < retryTime) {
				try {
					Thread.sleep(connectionConfig.getRetryInterval());
				} catch (InterruptedException e1) {
				}
				retryCount++;
			}
			logoff(ftp);
			throw new RuntimeException("FtpFileTransferClientImpl.connect02, errorMessage:" + e.getMessage(), e);
		}
	}

	/**
	 * FTP连接登出
	 * 
	 * @param ftp
	 */
	private void logoff(FTPClient ftp) {
		connReachable = false;
		if (ftp.isConnected()) {
			try {
				ftp.logout();
			} catch (Exception e) {
				bizlog.error("logout fail", e, new Object[0]);
			}
			try {
				ftp.disconnect();
			} catch (Exception e) {
				bizlog.error(FileConstant.SimpleFTPClient01);
			}
		}
	}

	private String getFileName(String name) {
		try {
			return new String(name.getBytes(FileConstant.LOCAL_ENCODING), FileConstant.REMOTE_ENCODING_ISO);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("FtpFileTransferClientImpl.getFileName01[" + name + "]" + "conversion failed",
					e);
		}
	}

	private static void localPrepare(String localFileName) {
		List<String> dirs = getDirectories(localFileName);
		if ((dirs != null) && (dirs.size() > 0)) {
			StringBuilder sb = new StringBuilder();
			for (String dir : dirs) {
				sb.append(dir).append(File.separator);
			}
			new File(sb.toString()).mkdirs();
		}
	}

	private static List<String> getDirectories(String fileName) {
		fileName = fileName.replace('\\', FileConstant.COMM_SEPARATOR);
		fileName = fileName.replace('/', FileConstant.COMM_SEPARATOR);
		List<String> ret = null;

		boolean abs = false;
		int idx = fileName.indexOf(FileConstant.COMM_SEPARATOR);
		if (idx != -1) {
			if (idx == 0) {
				fileName = fileName.substring(idx + 1);
				abs = true;
			}

			idx = fileName.lastIndexOf(FileConstant.COMM_SEPARATOR);
			if (idx != -1) {
				fileName = fileName.substring(0, idx);

				ret = MsStringUtil.split(fileName, FileConstant.COMM_SEPARATOR);
				ret.set(0, abs ? FileConstant.COMM_SEPARATOR + (String) ret.get(0) : (String) ret.get(0));
			}
		}

		return ret;
	}

	private boolean isFileSeparator(char cha) {
		if (cha == '/' || cha == '\\')
			return true;
		return false;
	}

	/**
	 * 根据形参workDir和fileName,获取文件全路径
	 * 
	 * @param workDir
	 * @param fileName
	 * @return
	 */
	private String getFullPathName(String workDir, String fileName) {
		bizlog.debug("getFullPathName begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("workDir {},fileName {}", workDir, fileName);
		if (MsStringUtil.isEmpty(workDir)) {
			return fileName;
		} else {
			// 2013-11-10 BUG-FIX: 目录未添加文件分隔符，自动添加
			String file = workDir;
			if (!isFileSeparator(file.charAt(file.length() - 1)))
				file = file + FileConstant.COMM_SEPARATOR;
			if (!MsStringUtil.isEmpty(fileName) && isFileSeparator(fileName.charAt(0)))
				file = file + fileName.substring(1);
			else
				file = file + fileName;
			bizlog.debug("file {}", file);
			bizlog.debug("getFullPathName end <<<<<<<<<<<<<<<<<<<<");
			return file;
		}
	}

	private void remotePrepare(FTPClient client, String remoteFileName) {
		try {
			client.deleteFile(getFileName(remoteFileName));
		} catch (IOException e) {
			throw new RuntimeException(
					"FtpFileTransferClientImpl.remotePrepare01, Deleting remote file[" + remoteFileName + "] failed",
					e);
		}

		List<String> dirs = getDirectories(remoteFileName);
		StringBuilder sb;
		if ((dirs != null) && (dirs.size() > 0)) {
			sb = new StringBuilder();
			for (String dir : dirs) {
				sb.append(dir).append("/");
				try {
					client.makeDirectory(getFileName(sb.toString()));

				} catch (IOException e) {
					throw new RuntimeException("FtpFileTransferClientImpl.remotePrepare02, Remote directory"
							+ sb.toString() + "creation failed", e);
				}
			}
		}
	}

	/**
	 * @param pathname
	 * @param regs
	 * @return
	 */
	private List<String> getRemoList(String pathname) {
		FTPClient ftpClient = login();
		FTPFile[] ftpFiles = null;
		try {
			pathname = pathname.replace('\\', FileConstant.COMM_SEPARATOR);
			pathname = pathname.replace('/', FileConstant.COMM_SEPARATOR);
			pathname = getFullPathName(getRemoteHome(), pathname);

			if (!ftpClient.changeWorkingDirectory(pathname)) {
				throw new RuntimeException("FtpFileTransferClientImpl.getRemoList01" + pathname);
			}

			ftpFiles = ftpClient.listFiles(pathname, new FTPFileFilter() {

				public boolean accept(FTPFile file) {
					return true;
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(
					"FtpFileTransferClientImpl.getRemoList02,Remote directory[" + pathname + "] traversal failed", e);
		} finally {
			logoff(ftpClient);
		}

		if (ftpFiles != null) {
			List<String> fileList = new ArrayList<String>();
			for (FTPFile ftpFile : ftpFiles) {
				fileList.add(ftpFile.getName());
			}
			return fileList;
		}
		return null;
	}

	/**
	 * 下载文件到本地
	 * 
	 * @param localFileName
	 * @param remoteFileName
	 */
	private void downloadFileToLocal(String localFileName, String remoteFileName) {
		long start = System.currentTimeMillis();
		FTPClient ftp = login();

		localPrepare(localFileName);
		try {
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				fos = new FileOutputStream(localFileName);
				bos = new BufferedOutputStream(fos);
				remoteFileName = getFileName(remoteFileName);
				if (!ftp.retrieveFile(remoteFileName, bos)) {
					throw new RuntimeException("FtpFileTransferClientImpl.download01, File[" + remoteFileName
							+ "] download failed," + ftp.getReplyString());
				}
				bizlog.info(FileConstant.SimpleFTPClient02,
						new Object[] { connectionConfig.getServerIp(), connectionConfig.getUserName(), remoteFileName,
								localFileName, Long.valueOf(System.currentTimeMillis() - start) });

				try {
					if (bos != null) {
						bos.flush();
						bos.close();
					}
					if (fos != null) {
						fos.flush();
						fos.close();
					}
				} catch (Exception e) {
					bizlog.error(FileConstant.SimpleFTPClient03, e, new Object[0]);
					if (e.getMessage().indexOf("550") != -1) {
						throw new RuntimeException("FtpFileTransferClientImpl.download02, file load failed, ["
								+ remoteFileName + "] not exists");
					} else if (e.getCause() != null) {
						throw new RuntimeException("FtpFileTransferClientImpl.download03", e);
					} else {
						throw new RuntimeException("FtpFileTransferClientImpl.download04", e);
					}
				}
			} catch (FileNotFoundException e) {
				throw new RuntimeException("FtpFileTransferClientImpl.download05, File download failed,the local file["
						+ localFileName + "] does not exist or can not be operated." + e.getMessage(), e);
			} catch (Exception e) {
				throw new RuntimeException("FtpFileTransferClientImpl.download06, File download failed", e);
			} finally {
				try {
					if (bos != null) {
						bos.flush();
						bos.close();
					}
					if (fos != null) {
						fos.flush();
						fos.close();
					}
				} catch (Exception e) {
					bizlog.error(FileConstant.SimpleFTPClient03, e, new Object[0]);
				}
			}
		} finally {
			logoff(ftp);
		}
	}

	private void uploadFileToRemote(String localFileName, String remoteFileName) {
		bizlog.debug("FtpFileTransferClientImpl.uploadFileToRemote end >>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}, remoteFileName {}", localFileName, remoteFileName);
		long start = System.currentTimeMillis();

		FTPClient ftp = login();
		remotePrepare(ftp, remoteFileName);
		try {
			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(localFileName));
				if (!ftp.storeFile(getFileName(remoteFileName), in)) {
					throw new RuntimeException(
							"FtpFileTransferClientImpl.upload01, Uploading files to FTP server failed: "
									+ ftp.getReplyString());
				}
				bizlog.info(FileConstant.SimpleFTPClient04,
						new Object[] { connectionConfig.getServerIp(), connectionConfig.getUserName(), remoteFileName,
								localFileName, Long.valueOf(System.currentTimeMillis() - start) });
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						bizlog.error(FileConstant.SimpleFTPClient05);
					}
				}
			} catch (FileNotFoundException e) {
				throw new RuntimeException("FtpFileTransferClientImpl.upload02,File upload failed, the local file["
						+ localFileName + "] does not exist or can not be operated." + e.getMessage(), e);
			} catch (Exception e) {
				if (e.getMessage().indexOf("550") != -1) {
					throw new RuntimeException("FtpFileTransferClientImpl.upload03, file load failed, [ "
							+ localFileName + " ] not exists");
				} else {
					throw new RuntimeException(
							"FtpFileTransferClientImpl.upload04, UPloading files to FTP server failed,"
									+ e.getMessage(),
							e);
				}
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						bizlog.error(FileConstant.SimpleFTPClient05);
					}
				}
			}
		} finally {
			logoff(ftp);
		}
		bizlog.debug(" FtpFileTransferClientImpl.uploadFileToRemote end >>>>>>>>>>>>>>>>");
	}

}
