package cn.sunline.ltts.busi.aplt.tools;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;



public class FtpHelper implements Closeable {
	private static BizLog logger = BizLogUtil.getBizLog(FtpHelper.class);
	
	private static FTPClient ftp = null;
	static boolean _isLogin = false;

	public static FtpHelper getInstance() {
		return new FtpHelper();
	}

	/**
	 * 
	 * ftp 匿名登录
	 * 
	 * @param ip
	 *            ftp服务地址
	 * @param port
	 *            端口号
	 * @param uname
	 *            用户名
	 * @param pass
	 *            密码
	 */
	public static boolean login(String ip, int port) {
		// 如果没有设置ftp用户可将username设为anonymous，密码为任意字符串
		return login(ip, port, "", "");
	}

	/**
	 * 
	 * ftp登录
	 * 
	 * @param ip
	 *            ftp服务地址
	 * @param port
	 *            端口号
	 * @param uname
	 *            用户名
	 * @param pass
	 *            密码
	 * @param workingDir
	 *            ftp 根目目录
	 */
	public static boolean login(String ip, int port, String uname, String pass) {
		ftp = new FTPClient();
		try {
			ftp.connect(ip, port);
			_isLogin = ftp.login(uname, pass);
			// 检测连接是否成功
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				logger.error("ftp服务器连接失败,IP:[%s],用户名:[%s],密码:[%s]", ip, uname, pass);
				return false;
			}
			return true;
		} catch (Exception ex) {
			logger.error("ftp服务器连接错误,IP:[%s],用户名:[%s],密码:[%s]", ip, uname, pass);
			return false;
		}
	}

	/**
	 * 
	 * ftp上传文件
	 * 
	 * @param localFileName
	 *            待上传文件
	 * @param ftpDirName
	 *            ftp 目录名
	 * @param ftpFileName
	 *            ftp目标文件
	 * @return true||false
	 */
	public static boolean uploadFile(String localFileName, String ftpDirName,
			String ftpFileName) {
		return uploadFile(localFileName, ftpDirName, ftpFileName, false);
	}

	/**
	 * 
	 * ftp上传文件
	 * 
	 * @param localFileName
	 *            待上传文件
	 * @param ftpDirName
	 *            ftp 目录名
	 * @param ftpFileName
	 *            ftp目标文件
	 * @param deleteLocalFile
	 *            是否删除本地文件
	 * @return true||false
	 */
	public static boolean uploadFile(String localFileName, String ftpDirName,
			String ftpFileName, boolean deleteLocalFile) {
		
//		if (StringUtils.isEmpty(ftpFileName)){
//			throw new GateWayBusinessException("ftperr0001","上传文件必须填写文件名！");
//		}
//
		File srcFile = new File(localFileName);
//		if (!srcFile.exists()){
//			throw new GateWayBusinessException("ftperr0002","文件不存在：" + localFileName);
//		}
		try (FileInputStream fis = new FileInputStream(srcFile)) {// fis自动关闭
			// 上传文件
			boolean flag = uploadFile(fis, ftpDirName, ftpFileName);
			// 是否删除文件
			if (deleteLocalFile) {
				srcFile.delete();
				logger.info("ftp服务器删除文件成功[%s]", srcFile);
			}
			fis.close();
			return flag;
		} catch (Exception e) {
			logger.error("ftp服务器上传文件异常[%s]", e);
			return false;
		}
	}

	/**
	 * 
	 * ftp上传文件 (使用inputstream)
	 * 
	 * @param localFileName
	 *            待上传文件
	 * @param ftpDirName
	 *            ftp 目录名
	 * @param ftpFileName
	 *            ftp目标文件
	 * @return true||false
	 */
	public static boolean uploadFile(FileInputStream uploadInputStream,
			String ftpDirName, String ftpFileName) {
//		if (StringUtils.isEmpty(ftpFileName))
//			throw new GateWayBusinessException("ftperr0003","上传文件必须填写文件名！");

		try {
			// 设置上传目录(没有则创建)
			if (!createDir(ftpDirName)) {
				throw ApError.Aplt.E0420();
			}
			ftp.setBufferSize(1024);
			// 解决上传中文 txt 文件乱码
			ftp.setControlEncoding("utf-8");
			FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
			conf.setServerLanguageCode("zh");
			// 设置文件类型（二进制）
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			// 上传
			if (ftp.storeFile(ftpFileName, uploadInputStream)) {
				uploadInputStream.close();
				logger.info("ftp服务器上传文件成功，文件目录：[%s]，文件名称：[%s]", ftpDirName, ftpFileName);
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("ftp服务器删除文件失败[%s]",e);
			return false;
		} finally {
		}
	}

	/**
	 * 下载文件
	 * 
	 * @param ftpDirName
	 *            ftp目录名
	 * @param ftpFileName
	 *            ftp文件名
	 * @param localFileFullName
	 *            本地文件名
	 * @return
	 * @author xxj
	 */
	public static boolean downloadFile(String ftpDirName, String ftpFileName,
			String localFileFullName) {
		try {
			if ("".equals(ftpDirName))
				ftpDirName = "/";
			if (!ftp.changeWorkingDirectory(ftpDirName)) {
				logger.info("ftp文件不存在[%s]" , ftpDirName);
				return false;
			}
			FTPFile[] fs = ftp.listFiles();
			for (FTPFile ff : fs) {
				if (ff.getName().equals(ftpFileName)) {
					FileOutputStream is = new FileOutputStream(new File(
							localFileFullName));
					ftp.retrieveFile(ff.getName(), is);
					is.close();
					logger.info("ftp服务器下载文件成功[%s]" , localFileFullName);
					return true;
				}
			}
			logger.info("ftp服务器下载文件失败，文件名称：[%s]，文件路径:[%s]", ftpFileName,ftpDirName);
			return false;
		} catch (Exception e) {
			logger.error("ftp服务器下载文件错误[%s]",e);
			return false;
		}
	}

	/**
	 * 
	 * 删除ftp上的文件
	 * 
	 * @param ftpFileName
	 * @return true || false
	 */
	public static boolean removeFile(String ftpFileName) {
		boolean flag = false;
		logger.info("ftp服务器准备移除文件[%s]", ftpFileName);
		try {
			flag = ftp.deleteFile(ftpFileName);
			logger.info("ftp服务器移除文件成功[%s]", ftpFileName);
			return flag;
		} catch (IOException e) {
			logger.error("ftp服务器移除文件错误[%s]",e);
			return false;
		}
	}

	/**
	 * 删除空目录
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean removeDir(String dir) {
		if (!StringUtils.startsWith(dir, "/"))
			dir = "/" + dir;
		try {
			return ftp.removeDirectory(dir);
		} catch (Exception e) {
			logger.error("ftp服务器移除目录错误[%s]",e);
			return false;
		}
	}

	/**
	 * 创建目录(有则切换目录，没有则创建目录)
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean createDir(String dir) {
		if (StringUtils.isEmpty(dir)){
			return false;
		}
		try {
			// 尝试切入目录
			if (ftp.changeWorkingDirectory(dir)){
				return true;
			}
			dir = StringUtils.trim(dir);
			if (StringUtils.startsWith(dir, "/"))
				dir = StringUtils.substring(dir, 1);
			if (StringUtils.endsWith(dir, "/")) {
				dir = StringUtils.substring(dir, 0, dir.length() - 1);
			}
			String[] arr = dir.split("/");
			StringBuffer sbfDir = new StringBuffer();
			// 循环生成子目录
			for (String s : arr) {
				sbfDir.append("/");
				sbfDir.append(s);
				// 尝试切入目录
				if (ftp.changeWorkingDirectory(dir)){
					continue;
				}
				if (!ftp.makeDirectory(dir)) {
					logger.info("ftp服务器创建目录失败[%s]", sbfDir.toString());
					return false;
				}
				logger.info("ftp服务器创建目录成功[%s]", sbfDir.toString());
			}
			// 将目录切换至指定路径
			return ftp.changeWorkingDirectory(dir);
		} catch (Exception e) {
			logger.error("ftp服务器创建目录错误[%s]",e);
			return false;
		}
	}
	
	
	/**
	 * 
	 * ftp上传文件 (使用inputstream)
	 * 
	 * @param localFileName
	 *            待上传文件
	 * @param ftpDirName
	 *            ftp 目录名
	 * @param ftpFileName
	 *            ftp目标文件
	 * @return true||false
	 */
	public static void listFiles(String dir) {
		if (StringUtils.isEmpty(dir))
//			throw new GateWayBusinessException("ftperr0004","文件目录不能为空！");
		try {
			//ftp.changeWorkingDirectory(dir);
			FTPFile[] files = ftp.listFiles();
			for (FTPFile ftpFile : files) {
				System.out.println(ftpFile.getName());
			}
		} catch (Exception e) {
		}
	}

	/**
	 *
	 * 销毁ftp连接
	 *
	 */
	public static void closeFtpConnection() {
		_isLogin = false;
		if (ftp != null) {
			if (ftp.isConnected()) {
				try {
					ftp.logout();
					ftp.disconnect();
					logger.info("ftp服务器关闭连接成功");
				} catch (IOException e) {
					logger.error("ftp服务器关闭连接失败[%s]",e);
				}
			}
		}
	}

	/**
	 *
	 * 销毁ftp连接
	 *
	 */
	@Override
	public  void close() {
		FtpHelper.closeFtpConnection();
	}
	
	
	public static void main(String[] args) {
		FtpHelper.login("10.91.131.242", 21, "zxyh", "zxyh");
		FtpHelper.listFiles("/gl/");
		
		FtpHelper.uploadFile("G:/home/zly.jpg","/gl/", "zly.jpg");
		
		FtpHelper.closeFtpConnection();
	}
}