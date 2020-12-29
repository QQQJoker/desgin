package cn.sunline.clwj.oss.impl.inner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.oss.config.SftpConfig;
import cn.sunline.clwj.oss.config.SftpConfigLoader;
import cn.sunline.clwj.oss.config.SftpConnectionConfig;
import cn.sunline.clwj.oss.util.FileConstant;
import cn.sunline.clwj.oss.util.MsFileUtil;
import cn.sunline.clwj.oss.util.MsMD5Util;
import cn.sunline.clwj.oss.util.MsStringUtil;
import cn.sunline.clwj.oss.util.SftpFileUtil;

public class SftpFileTransferClientImpl {

	private static final Logger bizlog = LogManager.getLogger(SftpFileTransferClientImpl.class);
	private static boolean connReachable = false;

	private SftpConfig config;

	private SftpConnectionConfig connectionConfig;

	private static Map<String, SftpConnectionConfig> configMap = new ConcurrentHashMap<String, SftpConnectionConfig>();

	public void init(String configId) {
		if (config == null) {
			synchronized (configMap) {
				if (config == null) {
					config = SftpConfigLoader.getConfig();
					for (SftpConnectionConfig conn : config.getSftpConnectionConfigs()) {
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
		bizlog.debug("SftpFileTransferClientImpl.upload begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}, remoteFileName {}, uploadOk {}", localFileName, remoteFileName, uploadOk);
		uploadFileToRemote(localFileName, remoteFileName);
		
		if (uploadOk) {
			String localFileFullPath = SftpFileUtil.getFullPathName(connectionConfig.getLocalHome(), localFileName);;
			File file = new File(localFileFullPath);
			String MD5 = "";
			try {
				MD5 = MsMD5Util.getFileMD5String(file);
			} catch (IOException e) {
				throw new RuntimeException(
						"SftpFileTransferClientImpl.upload01,while upload, failed to Generate MD5 by file ["
								+ file.getAbsolutePath() + "],error message [" + e.getMessage() + "]");
			}
			File okFile = new File(localFileFullPath + FileConstant.FILE_OK);

			if (!okFile.exists()) {
				try {
					okFile.createNewFile();
					FileUtils.writeStringToFile(okFile, MD5);
				} catch (IOException e) {
					throw new RuntimeException(
							"SftpFileTransferClientImpl.upload02,while upload, failed to create OK file ["
									+ okFile.getAbsolutePath() + "],error message [" + e.getMessage() + "]");
				}
			}
			uploadFileToRemote(localFileName + FileConstant.FILE_OK, remoteFileName + FileConstant.FILE_OK);
		}

		bizlog.debug("SftpFileTransferClientImpl.upload end >>>>>>>>>>>>>>>>>>>>");
	}

	public String download(String localFileName, String remoteFileName) {
		return download(localFileName, remoteFileName, true);
	}

	public String download(String localFileName, String remoteFileName, boolean downloadOk) {
		bizlog.debug("SftpFileTransferClientImpl.download begin >>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {},remoteFileName {}", localFileName, remoteFileName);
		remoteFileName = SftpFileUtil.getFullPathName(connectionConfig.getRemoteHome(), remoteFileName);
		localFileName =SftpFileUtil.getFullPathName(connectionConfig.getLocalHome(), localFileName);
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
				throw new RuntimeException("SftpFileTransferClientImpl.download01,read MD5 is fail!", e);
			}
		}
		bizlog.debug("SftpFileTransferClientImpl.download end >>>>>>>>>>>>>>>>");
		return localFileName;
	}

	public void delete(String remoteFileName) {
		bizlog.debug(" SftpFileTransferClientImpl.delete begin >>>>>>>>>>>>>>>>");
		bizlog.debug("remoteFileName {}", remoteFileName);
		long start = System.currentTimeMillis();

		remoteFileName = SftpFileUtil.getFullPathName(connectionConfig.getRemoteHome(), remoteFileName);
		ChannelSftp sftp = login();

		// remotePrepare(sftp, remoteFileName);
		remoteFileName = getFileName(remoteFileName);
		try {
			sftp.rm(remoteFileName);
			bizlog.info("Sftp host{},user{}, remoteFile{} delete sucess , took {} ms。",
					new Object[] { connectionConfig.getServerIp(), connectionConfig.getUserName(), remoteFileName,
							Long.valueOf(System.currentTimeMillis() - start) });
		} catch (SftpException e) {
			if (2 == e.id) {
				bizlog.error("File {} is not exist!", e, remoteFileName);
			} else {
				bizlog.error(FileConstant.SimpleFTPClient17, e, e.getMessage());
				throw new RuntimeException("SftpFileClientImpl.delete01,Sftp delete file failed!", e);
			}
		} finally {
			disconnect(sftp);
		}

		bizlog.debug(" SftpFileTransferClientImpl.delete end >>>>>>>>>>>>>>>>");
	}

	public void deleteLoal(String localFileName) {
		bizlog.debug("delete begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}", localFileName);
		localFileName = SftpFileUtil.getFullPathName(connectionConfig.getLocalHome(), localFileName);
		try {
			FileUtils.forceDelete(new File(localFileName));
		} catch (IOException e) {
			throw new RuntimeException("NfsFileTransferClientImpl.delete02,nfs delete file failed!", e);
		}

		bizlog.debug("delete end >>>>>>>>>>>>>>>>>>>>");
	}

	public boolean toReachable() {
		return connReachable;
	}

	private ChannelSftp login() {
		ChannelSftp sftp = null;
		String keypath = connectionConfig.getKeyPath();
		
		
		
		bizlog.info("============sftp connect,path of private key file：{}", connectionConfig.getKeyPath());
		JSch jsch = new JSch();
		try {
			if (CommUtil.isNotNull(connectionConfig.getKeyPath())) {
				//jsch.addIdentity(connectionConfig.getKeyPath()); // 设置私钥
				
				String prk = MsFileUtil.getKey(new FileReader(new File(connectionConfig.getKeyPath())));
				
				jsch.addIdentity(connectionConfig.getUserName(), prk.getBytes(), null, connectionConfig.getPassword().getBytes());
				bizlog.info("-----------sftp connect,path of private key file：{}", connectionConfig.getKeyPath());
			}
			Session sshSession = jsch.getSession(connectionConfig.getUserName(), connectionConfig.getServerIp(),connectionConfig.getServerPort());
			bizlog.info("sftp connect by host:{} username:{}", connectionConfig.getServerIp(),connectionConfig.getUserName());
			if (CommUtil.isNotNull(connectionConfig.getPassword())) {
				sshSession.setPassword(connectionConfig.getPassword());
			}
			sshSession.setTimeout(connectionConfig.getConnTimeoutInMs());
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			sshSession.setConfig(config); // 为Session对象设置properties
			sshSession.connect();
			sftp = (ChannelSftp) sshSession.openChannel("sftp");

			bizlog.info(String.format("sftp server host:{} port:{} is connect successfull",connectionConfig.getServerIp(), connectionConfig.getServerPort()));
			sftp.connect();
			connReachable = true;

			if ((MsStringUtil.isNotEmpty(connectionConfig.getRemoteHome()))) {
				sftp.cd(connectionConfig.getRemoteHome());
			}
			
		} catch (JSchException e) {
			bizlog.error(e);
			throw new RuntimeException(e);
		}catch (SftpException e) {
			bizlog.error(e);
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			bizlog.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			bizlog.error(e);
			throw new RuntimeException(e);
		}
	
		return sftp;
	}


	/**
	 * 关闭SFTP连接
	 * 
	 * @param sftp
	 */
	private void disconnect(ChannelSftp sftp) {
		connReachable = false;
		try {
			if (sftp != null) {
				sftp.disconnect();
				bizlog.debug("channel disconnect ...");
			}
			if (sftp.getSession() != null) {
				sftp.getSession().disconnect();
				bizlog.debug("session disconnect...");
			}
			bizlog.debug("Success to close sftp connect!");
		} catch (Exception e) {
			bizlog.debug("disconnect end >>>>>>>>>>>>>>>>>>>>");
			throw new RuntimeException("SftpFileTransferClientImpl.disconnect01,Sftp disconnect failed:user="
					+ connectionConfig.getUserName() + ",ip=" + connectionConfig.getServerIp() + ",port="
					+ connectionConfig.getServerPort() + " Error Message:" + e.getMessage());
		}
	}

	private void downloadFileToLocal(String localFileName, String remoteFileName) {
		long start = System.currentTimeMillis();
		ChannelSftp sftp = login();
		FileOutputStream fos = null;
		try {
			localPrepare(localFileName);
			remoteFileName = getFileName(remoteFileName);
			fos = new FileOutputStream(localFileName);
			try {
				sftp.get(remoteFileName, fos);
			} catch (SftpException e) {
				throw new RuntimeException(
						"SftpFileTransferClientImpl.downloadFileToLocal01,Sftp download file failed!", e);
			}
			if (fos != null) {
				try {
					fos.close();
					fos.flush();
				} catch (IOException e) {
					// 是否使用FileConstant常量
					bizlog.error(FileConstant.SimpleFTPClient03, e, new Object[0]);
				}
			}
			bizlog.info(FileConstant.SimpleFTPClient02,
					new Object[] { connectionConfig.getServerIp(), connectionConfig.getServerPort(), remoteFileName,
							localFileName, Long.valueOf(System.currentTimeMillis() - start) });
		} catch (FileNotFoundException e1) {
			throw new RuntimeException(
					"SftpFileTransferClientImpl.downloadFileToLocal02,File download failed,the local file["
							+ localFileName + "] does not exist or can not be operated." + e1.getMessage(),
					e1);
		} finally {
			disconnect(sftp);
			if (fos != null) {
				try {
					fos.close();
					fos.flush();
				} catch (IOException e) {
					bizlog.error(FileConstant.SimpleFTPClient03, e, new Object[0]);
				}
			}
		}
	}

	private void uploadFileToRemote(String localFileName, String remoteFileName) {
		bizlog.debug(" SftpFileTransferClientImpl.uploadFileToRemote end >>>>>>>>>>>>>>>>");
		bizlog.debug("localFileName {}, remoteFileName {}", localFileName, remoteFileName);
		long start = System.currentTimeMillis();
		remoteFileName = SftpFileUtil.getFullPathName(connectionConfig.getRemoteHome(), remoteFileName);
		localFileName = SftpFileUtil.getFullPathName(connectionConfig.getLocalHome(), localFileName);

		ChannelSftp sftp = login();
		FileInputStream in = null;
		try {
			remotePrepare(sftp, remoteFileName);
			in = new FileInputStream(new File(localFileName));
			try {
				sftp.cd(connectionConfig.getRemoteHome());
				sftp.put(in, remoteFileName);
			} catch (SftpException e) {
				if (e.getMessage().indexOf("550") != -1) {
					throw new RuntimeException("SftpFileTransferClientImpl.uploadFileToRemote01, file upload failed, [ "
							+ localFileName + " ] not exists");
				} else {
					throw new RuntimeException(
							"SftpFileTransferClientImpl.uploadFileToRemote02,Sftp upload file failed!", e);
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					bizlog.error(FileConstant.SimpleFTPClient05, e);
				}

			}
			bizlog.info(FileConstant.SimpleFTPClient04,
					new Object[] { connectionConfig.getServerIp(), connectionConfig.getUserName(), remoteFileName,
							localFileName, Long.valueOf(System.currentTimeMillis() - start) });
		} catch (FileNotFoundException e) {
			throw new RuntimeException(
					"SftpFileTransferClientImpl.uploadFileToRemote03,File upload failed, the local file["
							+ localFileName + "] does not exist or can not be operated." + e.getMessage(),
					e);
		} finally {
			disconnect(sftp);
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					bizlog.error(FileConstant.SimpleFTPClient03, e, new Object[0]);
				}
			}
		}
		bizlog.debug("SftpFileTransferClientImpl.uploadFileToRemote end >>>>>>>>>>>>>>>>");
	}

	public void remotePrepare(ChannelSftp sftp, String remoteFileName) {
		if (new File(remoteFileName).exists()) {
			try {
				sftp.rmdir(getFileName(remoteFileName));
			} catch (SftpException e) {
				throw new RuntimeException(
						"SftpFileClientImpl.remotePrepare01,Deleting remote file [" + remoteFileName + "] failed", e);
			}
		}
		List<String> dirs = getDirectories(remoteFileName);
		StringBuilder sb;
		if ((dirs != null) && (dirs.size() > 0)) {
			for (String dir : dirs) {
				sb = new StringBuilder();
				dir = dir.replace('\\', '/');
				dir = dir.replace('/', '/');
				sb.append(dir).append("/");
				try {
					sftp.cd(sb.toString());
				} catch (SftpException e) {
					try {
						sftp.mkdir(getFileName(dir));
						sftp.cd(sb.toString());
					} catch (SftpException e1) {
						throw new RuntimeException("SftpFileClientImpl.remotePrepare02,Remote directory ["
								+ sb.toString() + "] creation failed", e1);
					}
				}
			}
		}
	}

	private String getFileName(String name) {
		try {
			return new String(name.getBytes(FileConstant.LOCAL_ENCODING), FileConstant.REMOTE_ENCODING_UTF);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("SftpFileClientImpl.getFileName01,[" + name + "]" + "conversion failed", e);
		}
	}

	public static List<String> getDirectories(String fileName) {
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

	public static void localPrepare(String localFileName) {
		List<String> dirs = getDirectories(localFileName);
		if ((dirs != null) && (dirs.size() > 0)) {
			StringBuilder sb = new StringBuilder();
			for (String dir : dirs) {
				sb.append(dir).append(File.separator);
			}
			new File(sb.toString()).mkdirs();
		}
	}

	@SuppressWarnings("rawtypes")
	public List<String> getRemoList(String pathname) {
		ChannelSftp sftp = login();
		List<String> fileList;
		// Pattern pattern = null;
		Vector vste;
		try {
			fileList = new ArrayList<String>();
			pathname = pathname.replace('\\', FileConstant.COMM_SEPARATOR);
			pathname = pathname.replace('/', FileConstant.COMM_SEPARATOR);
			pathname = SftpFileUtil.getFullPathName(connectionConfig.getRemoteHome(), pathname);
			sftp.cd(pathname);
			vste = sftp.ls(sftp.pwd());
			for (Object file : vste) {
				// Matcher matcher = pattern.matcher(((LsEntry) file).getFilename());
				if (!MsStringUtil.isEmpty(file) && !".".equals(((LsEntry) file).getFilename())
						&& !"..".equals(((LsEntry) file).getFilename()) && !((LsEntry) file).getAttrs().isDir()) {
					fileList.add(((LsEntry) file).getFilename());
				}
			}
			return fileList;
		} catch (SftpException e) {
			throw new RuntimeException("9013, Remote directory[" + pathname + "] traversal failed", e);
		} finally {
			disconnect(sftp);
		}
	}


	public List<String> getLocalFileList(String localDir) {
		bizlog.debug("getLocalFileList begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("localDir {}", localDir);
		localDir = SftpFileUtil.getFullPathName(connectionConfig.getLocalHome(), localDir);
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

	public List<String> getRemoteFileList(String remoteDir) {
		return getRemoList(remoteDir);
		// return getRemoList(remoteDir, FileConstant.FILE_OK);
	}

	public String getLocalHome() {
		return SftpFileUtil.getHomePath(connectionConfig.getLocalHome());
	}
	
	public String getRemoteHome() {
		return SftpFileUtil.getHomePath(connectionConfig.getRemoteHome());
	}
}
