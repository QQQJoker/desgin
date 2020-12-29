package cn.sunline.clwj.oss.util;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.sunline.clwj.oss.config.SftpConnectionConfig;
import cn.sunline.clwj.oss.impl.inner.SftpFileTransferClientImpl;

public class SftpFileUtil {
	
	private static final Logger bizlog = LogManager.getLogger(SftpFileUtil.class);
	
	
	/**
	 * 替换本地或远程目录中的分隔符
	 * @param path
	 * @return
	 */
	public static String getHomePath(String path) {
		bizlog.debug("getHomePath begin >>>>>>>>>>>>>>>>>>>>");
		if (!MsStringUtil.isEmpty(path)) {
			path = path.replace('/', FileConstant.COMM_SEPARATOR);
			path = path.replace('\\', FileConstant.COMM_SEPARATOR);
			if (!isFileSeparator(path.charAt(path.length() - 1)))
				path = path + FileConstant.COMM_SEPARATOR;
		}
		bizlog.debug("Home Path file {}", path);
		bizlog.debug("getHomePath end >>>>>>>>>>>>>>>>>>>>");
		return path;
	}
	
	/**
	 * 判断是否是文件分割符
	 * 
	 * @param cha
	 *            分隔符
	 * @return
	 */
	private static boolean isFileSeparator(char cha) {
		if (cha == '/' || cha == '\\')
			return true;
		return false;
	}
	
	/**
	 * 获取文件全路径
	 * 
	 * @param workDir
	 *            文件目录
	 * @param fileName
	 *            文件名
	 * @return
	 */
	public static String getFullPathName(String workDir, String fileName) {
		bizlog.debug("getFullPathName begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.debug("workDir {},fileName {}", workDir, fileName);
		
		if (fileName.startsWith("/") || fileName.startsWith("\\")) {
			return fileName;
		}
		
		workDir = getHomePath(workDir);
		
		String file = "";
		if (MsStringUtil.isEmpty(workDir)) {
			file = fileName;
		} else {
			// 2013-11-10 BUG-FIX: 目录未添加文件分隔符，自动添加
			file = workDir;
			if (!isFileSeparator(file.charAt(file.length() - 1))) {
				file = file + FileConstant.COMM_SEPARATOR;
			}
			if (!MsStringUtil.isEmpty(fileName) && isFileSeparator(fileName.charAt(0))) {
				file = file + fileName.substring(1);
			} else {
				file = file + fileName;
			}
		}
		file = file.replace("//", "/").replace("\\", "/");
		bizlog.debug("file {}", file);
		bizlog.debug("getFullPathName end <<<<<<<<<<<<<<<<<<<<");
		return file;
	}
	/**
	 * 根据全路径名获取文件路径
	 * @param filePathName
	 * @return 
	 */
	public static String getFilePath(String filePathName) {
		File f = new File(filePathName);
		return f.getParent() + FileConstant.COMM_SEPARATOR;
	}
}
