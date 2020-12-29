package cn.sunline.clwj.oss.impl.inner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.sunline.clwj.oss.config.NfsConfig;
import cn.sunline.clwj.oss.config.NfsConfigLoader;
import cn.sunline.clwj.oss.config.NfsConnectionConfig;
import cn.sunline.clwj.oss.util.FileConstant;
import cn.sunline.clwj.oss.util.MsMD5Util;
import cn.sunline.clwj.oss.util.MsStringUtil;

public class NfsFileTransferClientImpl {

	private static final Logger bizlog = LogManager.getLogger(NfsFileTransferClientImpl.class);

	private NfsConfig config;

	private NfsConnectionConfig connectionConfig = null;

	private static Map<String, NfsConnectionConfig> configMap = new ConcurrentHashMap<String, NfsConnectionConfig>();

	public void init(String configId) {

		if (config == null) {
			synchronized (configMap) {
				if (config == null) {
					config = NfsConfigLoader.getConfig();
					for (NfsConnectionConfig conn : config.getNfsConnectionConfigs()) {
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
		bizlog.debug("NfsFileTransferClientImpl.upload begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}, remoteFileName {}, uploadOk {}", localFileName, remoteFileName, uploadOk);

		if(!remoteFileName.startsWith("/") && !remoteFileName.startsWith("\\")) {
			remoteFileName = getFullPathName(getRemoteHome(), remoteFileName);
		}
		
		if(!localFileName.startsWith("/") && !localFileName.startsWith("\\")) {
			localFileName = getFullPathName(getLocalHome(), localFileName);
		}

		uploadFileToRemote(localFileName, remoteFileName);
		if (uploadOk) {
			File file = new File(localFileName);
			String MD5 = "";
			try {
				MD5 = MsMD5Util.getFileMD5String(file);
			} catch (IOException e) {
				throw new RuntimeException(
						"NfsFileTransferClientImpl.upload01,while upload, failed to Generate MD5 by file ["
								+ file.getAbsolutePath() + "],error message [" + e.getMessage() + "]");
			}
			File okFile = new File(localFileName + FileConstant.FILE_OK);

			if (!okFile.exists()) {
				try {
					okFile.createNewFile();
					FileUtils.writeStringToFile(okFile, MD5);
				} catch (IOException e) {
					throw new RuntimeException(
							"NfsFileTransferClientImpl.upload02,while upload, failed to create OK file ["
									+ okFile.getAbsolutePath() + "],error message [" + e.getMessage() + "]");
				}
			}
			uploadFileToRemote(localFileName + FileConstant.FILE_OK, remoteFileName + FileConstant.FILE_OK);
		}
		bizlog.debug("NfsFileTransferClientImpl.upload end >>>>>>>>>>>>>>>>>>>>");

	}

	public String download(String localFileName, String remoteFileName) {
		return download(localFileName, remoteFileName, true);
	}

	public String download(String localFileName, String remoteFileName, boolean downloadOk) {
		bizlog.debug("download begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {},remoteFileName {}", localFileName, remoteFileName);

		if(!remoteFileName.startsWith("/") && !remoteFileName.startsWith("\\")) {
			remoteFileName = getFullPathName(getRemoteHome(), remoteFileName);
		}
		
		if(!localFileName.startsWith("/") && !localFileName.startsWith("\\")) {
			localFileName = getFullPathName(getLocalHome(), localFileName);
		}

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
				throw new RuntimeException("NfsFileTransferClientImpl.download01,read MD5 is fail!", e);
			}
		}
		bizlog.debug("download end >>>>>>>>>>>>>>>>>>>>");
		return localFileName;
	}

	public void delete(String remoteFileName) {
		bizlog.debug("delete begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("remoteFileName {}", remoteFileName);
		//BUGFIX,20190710  传进来的已经是全路径了不需要再次拼接
		//remoteFileName = getFullPathName(getRemoteHome(), remoteFileName);
		if(!remoteFileName.startsWith("/") && !remoteFileName.startsWith("\\")) {
			remoteFileName = getFullPathName(getRemoteHome(), remoteFileName);
		}

		try {
			FileUtils.forceDelete(new File(remoteFileName));
		} catch (IOException e) {
			throw new RuntimeException("NfsFileTransferClientImpl.delete02,nfs delete file failed!", e);
		}

		bizlog.debug("delete end >>>>>>>>>>>>>>>>>>>>");

	}

	public void deleteLoal(String localFileName) {
		bizlog.debug("delete begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}", localFileName);
		//BUGFIX,20190710  传进来的已经是全路径了不需要再次拼接
		//localFileName = getFullPathName(getLocalHome(), localFileName);
		
		if(!localFileName.startsWith("/") && !localFileName.startsWith("\\")) {
			localFileName = getFullPathName(getLocalHome(), localFileName);
		}
		
		try {
			FileUtils.forceDelete(new File(localFileName));
		} catch (IOException e) {
			throw new RuntimeException("NfsFileTransferClientImpl.delete02,nfs delete file failed!", e);
		}

		bizlog.debug("delete end >>>>>>>>>>>>>>>>>>>>");
	}

	public List<String> getRemoteFileList(String remoteDir) {
		bizlog.debug("getRemoteFileList begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("remoteFileName {}", remoteDir);
		remoteDir = getFullPathName(getRemoteHome(), remoteDir);
		Collection<File> files = FileUtils.listFiles(new File(remoteDir), null, false);
		if (files != null) {
			List<String> fileList = new ArrayList<String>();
			for (File file : files) {
				if (!".".equals(file.getName()) && !"..".equals(file.getName())) {
					fileList.add(file.getName());
				}
			}
			bizlog.debug("getRemoteFileList begin >>>>>>>>>>>>>>>>>>>>");
			return fileList;
		}
		return null;
	}

	public List<String> getLocalFileList(String localDir) {
		bizlog.debug("getLocalFileList begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localDir {}", localDir);
		localDir = getFullPathName(getLocalHome(), localDir);
		Collection<File> files = FileUtils.listFiles(new File(localDir), null, false);
		if (files != null) {
			List<String> fileList = new ArrayList<String>();
			for (File file : files) {
				if (!".".equals(file.getName()) && !"..".equals(file.getName())) {
					fileList.add(file.getName());
				}
			}
			bizlog.debug("getLocalFileList begin >>>>>>>>>>>>>>>>>>>>");
			return fileList;
		}
		return null;
	}

	public boolean toReachable() {
		// 对于NFS来说没有连接的说法
		return true;
	}

	public String getLocalHome() {
		return this.connectionConfig.getLocalHome();
	}

	public String getRemoteHome() {
		return this.connectionConfig.getRemoteHome();
	}

	private void uploadFileToRemote(String localFileName, String remoteFileName) {
		bizlog.debug("NfsFileTransferClientImpl.uploadFileToRemote end >>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}, remoteFileName {}", localFileName, remoteFileName);
		long start = System.currentTimeMillis();
		new File(remoteFileName).getParentFile().mkdirs();
		try {
			FileUtils.copyFile(new File(localFileName), new File(remoteFileName));
			//cn.sunline.ltts.base.util.FileLock lock=new cn.sunline.ltts.base.util.FileLock(new File(remoteFileName));
			
			new File(remoteFileName);
			bizlog.info("upload file from {} to {} is sucess, using time {} ms",
					new Object[] { remoteFileName, localFileName, Long.valueOf(System.currentTimeMillis() - start) });
		} catch (IOException e) {
			throw new RuntimeException(
					"NfsFileTransferClientImpl.uploadFileToRemote01,UPloading files to nfs server failed,"
							+ e.getMessage(),
					e);
		}
		bizlog.debug(" NfsFileTransferClientImpl.uploadFileToRemote end >>>>>>>>>>>>>>>>");
	}

	private void downloadFileToLocal(String localFileName, String remoteFileName) {
		bizlog.debug("NfsFileTransferClientImpl.downloadFileToLocal end >>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}, remoteFileName {}", localFileName, remoteFileName);
		long start = System.currentTimeMillis();
		localPrepare(localFileName);
		try {
			FileUtils.copyFile(new File(remoteFileName), new File(localFileName));
			bizlog.info("download file from {} to {} is sucess, using time {} ms",
					new Object[] { remoteFileName, localFileName, Long.valueOf(System.currentTimeMillis() - start) });
		} catch (IOException e) {
			throw new RuntimeException(
					"NfsFileTransferClientImpl.downloadFileToLocal01,UPloading files to nfs server failed,"
							+ e.getMessage(),
					e);
		}
		bizlog.debug(" NfsFileTransferClientImpl.uploadFileToRemote end >>>>>>>>>>>>>>>>");
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

	private boolean isFileSeparator(char cha) {
		if (cha == '/' || cha == '\\')
			return true;
		return false;
	}
}
