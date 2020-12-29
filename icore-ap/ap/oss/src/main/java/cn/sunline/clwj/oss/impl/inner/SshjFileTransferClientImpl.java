package cn.sunline.clwj.oss.impl.inner;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.ChannelSftp.LsEntry;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.clwj.oss.config.SftpConfig;
import cn.sunline.clwj.oss.config.SftpConfigLoader;
import cn.sunline.clwj.oss.config.SftpConnectionConfig;
import cn.sunline.clwj.oss.util.FileConstant;
import cn.sunline.clwj.oss.util.MsMD5Util;
import cn.sunline.clwj.oss.util.MsStringUtil;
import cn.sunline.clwj.oss.util.SftpFileUtil;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

/**
 * 通过sshj开发包实现sftp加载私钥传输文件
 * @author os_cl_zhangan
 *
 */
public class SshjFileTransferClientImpl {

	private static final Logger bizlog = LogManager.getLogger(SshjFileTransferClientImpl.class);
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
	
	public void upload(String localFilename, String remoteFilename) {
		uploadLocalFile(localFilename, remoteFilename, true, false);
	}
	
	public void upload(String localFilename, String remoteFilename, boolean okfile) {
		uploadLocalFile(localFilename, remoteFilename, okfile, false);
	}
	
	public void upload(String localFilename, String remoteFilename, boolean okfile, boolean isMD5) {
		uploadLocalFile(localFilename, remoteFilename, okfile, isMD5);
	}

	/**
	 * 
	 * @param localFilename 本地文件
	 * @param remoteFilename 远程文件
	 * @param okfile 是否同步生成远端OK文件
	 */
	private void uploadLocalFile(String localFilename, String remoteFilename, boolean okfile, boolean isMD5) {
		localFilename = SftpFileUtil.getFullPathName(connectionConfig.getLocalHome(), localFilename);
		remoteFilename = SftpFileUtil.getFullPathName(connectionConfig.getRemoteHome(), remoteFilename);
		
		SFTPClient sftp = getSftpSession(connectionConfig);
		
		makeRemoteFilePath(sftp, remoteFilename);
		try {
			sftp.put(localFilename, remoteFilename);
			if(okfile) makeAndUploadOKFile(sftp, remoteFilename, isMD5);
		} catch (IOException e) {
			bizlog.error(String.format("SFTP文件上传失败:[%s]", remoteFilename));
			bizlog.error("", e.getMessage());
			throw new RuntimeException(e);
		} finally {
			closSftpSession(sftp);
		}
	}
	public void download(String localFilename, String remoteFilename) {
		downloadRemoteFile(localFilename, remoteFilename,false, false);
	}
	public void download(String localFilename, String remoteFilename, boolean okfile) {
		downloadRemoteFile(localFilename, remoteFilename,okfile, false);
	}
	public void download(String localFilename, String remoteFilename, boolean okfile, boolean isMD5) {
		if (isMD5 && !okfile) {
			bizlog.error("SFTP need okfile to check MD5");
			throw new RuntimeException("SFTP need okfile to check MD5");
		}
		downloadRemoteFile(localFilename, remoteFilename,okfile, false);
	}
	/**
	 * 
	 * @param localFilename 本地文件
	 * @param remoteFilename 远程文件
	 * @param okfile 是否同步下载OK文件
	 * @param isMD5 是否校验MD5
	 */
	private void downloadRemoteFile(String localFilename, String remoteFilename, boolean okfile, boolean isMD5) {
		SFTPClient sftp = downloadRemoteFile(localFilename, remoteFilename);
		if (okfile) {
			String remoteOKFilename = remoteFilename + FileConstant.FILE_OK;
			String localOKFilename = localFilename + FileConstant.FILE_OK;
			downloadRemoteFile(localOKFilename, remoteOKFilename);
			if (isMD5) {
				checkFileMD5(localOKFilename, localFilename);
			}
		}
		
		closSftpSession(sftp);
	}
	
	/**
	 * 下载远程文件，返回SFTP链接
	 * @param localFilename
	 * @param remoteFilename
	 * @return
	 */
	private SFTPClient downloadRemoteFile(String localFilename, String remoteFilename) {
		localFilename = SftpFileUtil.getFullPathName(connectionConfig.getLocalHome(), localFilename);
		remoteFilename = SftpFileUtil.getFullPathName(connectionConfig.getRemoteHome(), remoteFilename);
		makeLocalFilePath(localFilename);
		SFTPClient sftp = getSftpSession(connectionConfig);
		try {
			sftp.get(remoteFilename, localFilename);
		} catch (IOException e) {
			bizlog.error(String.format("SFTP download file failure,localfile:[%s],remotefile[%s]", localFilename, remoteFilename));
			throw new RuntimeException(e);
		}
		
		return sftp;
	}
	
	/**
	 * 删除远端文件
	 * @param remoteFilename
	 */
	public void delete(String remoteFilename) {
		remoteFilename = SftpFileUtil.getFullPathName(connectionConfig.getRemoteHome(), remoteFilename);
		SFTPClient sftp = getSftpSession(connectionConfig);
		try {
			sftp.rm(remoteFilename);
		} catch (IOException e) {
			bizlog.error(String.format("SFTP delete remote file failure:[%s]", remoteFilename));
			throw new RuntimeException(e);
		} finally {
			closSftpSession(sftp);
		}
	}
	/**
	 * 删除本地文件
	 * @param localFilename
	 */
	public void deleteLoal(String localFilename) {
		localFilename = SftpFileUtil.getFullPathName(connectionConfig.getLocalHome(), localFilename);
		try {
			FileUtils.forceDelete(new File(localFilename));
		} catch (IOException e) {
			throw new RuntimeException("SshjFileTransferClientImpl delete file failed!", e);
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
		List<String> fileList = null;
		SFTPClient sftp = getSftpSession(connectionConfig);
		List<RemoteResourceInfo> lstRemoteFile;
		try {
			fileList = new ArrayList<String>();
			lstRemoteFile = sftp.ls(remoteDir);
			for (Object file : lstRemoteFile) {

				if (!MsStringUtil.isEmpty(file) && !".".equals(((RemoteResourceInfo) file).getName())
						&& !"..".equals(((RemoteResourceInfo) file).getName()) && !((RemoteResourceInfo) file).isDirectory()) {
					fileList.add(((RemoteResourceInfo) file).getName());
				}
			}
		} catch (IOException e) {
			bizlog.error("SFTP get remotefile list failure");
			throw new RuntimeException(e);
		}
		
		return fileList;
	}
	
	/**
	 * 获取SFTP连接信息
	 * @param info
	 * @return 
	 */
	private SFTPClient getSftpSession(SftpConnectionConfig info) {
		SSHClient ssh = null;
		SFTPClient sftp = null;
		try {
			ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			ssh.connect(InetAddress.getByName(info.getServerIp()), info.getServerPort());
			if (info.getKeyPath() != null && info.getKeyPath() != "") {
				KeyProvider pro = ssh.loadKeys(info.getKeyPath(), info.getPassword().toCharArray());
				ssh.authPublickey(info.getUserName(), pro);
			} else {
				ssh.authPassword(info.getUserName(), info.getPassword());
			}
			sftp = ssh.newSFTPClient();
		} catch (UnknownHostException e) {
			bizlog.error(String.format("SFTP unknow host IP:[%s]", info.toString()));
			throw new RuntimeException(e);
		} catch (UserAuthException e) {
			bizlog.error(String.format("SFTP user certification failure:[%s]", info.toString()));
			throw new RuntimeException(e);
		} catch (IOException e) {
			bizlog.error(String.format("SFTP session open failure:[%s]", info.toString()));
			throw new RuntimeException(e);
		}
		
		return sftp;
	}
	/**
	 * 关闭SFTP通道
	 * @param sftp
	 */
	private void closSftpSession(SFTPClient sftp) {
		
		try {
			sftp.close();
		} catch (IOException e) {
			bizlog.error("SFTP close sftp session failure");
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * 打开一个远端目录，不存在则新建
	 * @param sftp
	 * @param path
	 */
	private void makeRemoteFilePath(SFTPClient sftp, String remoteFilename) {
		String filepath = SftpFileUtil.getFilePath(remoteFilename);
		FileAttributes attr = null;
		try {
			attr = sftp.statExistence(filepath);
			if (CommUtil.isNull(attr)) {
				sftp.mkdirs(filepath);
			}
		} catch (IOException e) {
			bizlog.error(String.format("SFTP make remotedir failure:[%s]", filepath));
			throw new RuntimeException(e);
		}
	}
	/**
	 * 打开远程OK文件，并写入MD5值
	 * @param sftp
	 * @param remoteFilename
	 * @param isMD5
	 */
	private void makeAndUploadOKFile(SFTPClient sftp, String remoteFilename, boolean isMD5) {
		String MD5 = "";
		if (isMD5) {
			File file = new File(remoteFilename);
			try {
				MD5 = MsMD5Util.getFileMD5String(file);
			} catch (IOException e) {
				bizlog.error(String.format("SFTP generate MD5 failure:[%s]", remoteFilename));
				throw new RuntimeException(e);
			}
		}
		String okFilename = remoteFilename + FileConstant.FILE_OK;
		RemoteFile okFile = null;
		RemoteFile.RemoteFileOutputStream rmo = null;
		try {
			okFile = sftp.open(okFilename, EnumSet.of(OpenMode.WRITE,OpenMode.CREAT));
			rmo = okFile.new RemoteFileOutputStream();
			rmo.write(MD5.getBytes());
		} catch (IOException e) {
			bizlog.error(String.format("SFTP generate okfile failure:[%s]", okFilename));
			throw new RuntimeException(e);
		} finally {
			if (rmo != null) {
				try {
					rmo.close();
				} catch (IOException e) {
					bizlog.error(String.format("SFTP open okfile failure:[%s]", okFilename));
					throw new RuntimeException(e);
				}
			}
			if (okFile != null) {
				try {
					okFile.close();
				} catch (IOException e) {
					bizlog.error(String.format("SFTP open okfile failure:[%s]", okFilename));
					throw new RuntimeException(e);
				}
			}
		}
	}
	/**
	 * 创建本地文件路径
	 * @param localFilename
	 */
	private void makeLocalFilePath(String localFilename) {
		String filepath = SftpFileUtil.getFilePath(localFilename);
		File f = new File(filepath);
		if (!f.exists()) {
			f.mkdirs();
		}
	}
	/**
	 * 校验文件MD5
	 * @param lcoalOKFilename
	 * @param localFilename
	 */
	private void checkFileMD5(String lcoalOKFilename, String localFilename) {
		try {
			String okMd5 = FileUtils.readFileToString(new File(lcoalOKFilename));
			String fileMd5 = MsMD5Util.getFileMD5String(new File(localFilename));
			if (!okMd5.equals(fileMd5)) {
				throw new RuntimeException("The file MD5 mismatch, the downloaded file is incomplete.");
			}
		} catch (IOException e) {
			throw new RuntimeException("SftpFileTransferClientImpl.download01,read MD5 is fail!", e);
		}
	}
	
	public String getLocalHome() {
		return SftpFileUtil.getHomePath(connectionConfig.getLocalHome());
	}
	
	public String getRemoteHome() {
		return SftpFileUtil.getHomePath(connectionConfig.getRemoteHome());
	}
}
