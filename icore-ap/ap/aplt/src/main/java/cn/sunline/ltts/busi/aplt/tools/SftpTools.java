/**   
 * @Title: SftpTools.java 
 * @Package cn.sunline.ltts.busi.aplt.tools 
 * @Description: 
 * @author zhanga
 * @date 2017年9月9日 下午3:14:14 
 * @version V2.3.0   
 */ 
 
package cn.sunline.ltts.busi.aplt.tools;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.security.EncryptUtils;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch.SftpInfo;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/** 
 * @ClassName: SftpTools 
 * @Description: 
 * @author zhanga
 * @date 2017年9月9日 下午3:14:14 
 *  
 */
public class SftpTools {
	
	private static BizLog log = LogManager.getBizLog(SftpTools.class);

	static Session session = null;
	
	/**
	 * 获取Sftp连接，设置超时时间，等待自动释放连接
	 * @param sftpInfo
	 * @return
	 */
	private static Session getSession(SftpInfo sftpInfo){
		
		if(session.isConnected()){
			return session;
		}
		
		String host = sftpInfo.getRmaddr();
		String username = sftpInfo.getRmname();
		String password = getPass(sftpInfo.getRmpass());
		
		int port = sftpInfo.getRmport();
		int timeout = 5000;
		if(CommUtil.isNotNull(sftpInfo.getTimeot())){
			timeout = sftpInfo.getTimeot();
		}
		
		log.info("远程服务器地址：[%s]", host);
		log.info("远程服务器用户：[%s]", username);
		JSch jsch = new JSch();
		try{
			if (sftpInfo.getUsersa() == E_YES___.YES) {
				jsch.addIdentity("$HOME/.ssh/id_rsa"); //设置私钥路径
			}
			session = jsch.getSession(username, host, port);
			
			Properties prop = new Properties();
			prop.put("StrictHostKeyChecking", "no");
			prop.put("PreferredAuthentications","password");
			
			session.setPassword(password);
			session.setConfig(prop);
			session.setTimeout(timeout);
			session.connect();
			log.info("==========Sftp 会话建立成功,IP地址[%s],端口：[%s],用户名：[%s]",host, port, username);
		}catch (JSchException e){
			e.printStackTrace();
			log.error("==========Sftp 会话建立失败,IP地址[%s],端口：[%s],用户名：[%s]",host, port, username);
			throw ApError.Aplt.E0419();
		}finally{
			closChan(session, null, null);
		}
		
		return session;
	}
	
	
	
	/**
	 * @Title: upload  
	 * @Description: sftp上传文件
	 * @param sftpInfo IP地址，端口，用户名，密码，本地及远程路径，文件名
	 * @author zhanga
	 * @date 2017年9月9日 下午4:06:31 
	 * @version V2.3.0
	 */
	public static void upload(SftpInfo sftpInfo){
		
		Session session = getSession(sftpInfo);
		
		String l_path = getPath(sftpInfo.getLcpath());
		String r_path = getPath(sftpInfo.getRmpath());
		
		String l_file = sftpInfo.getLcfile();
		String r_file = sftpInfo.getRmfile();
		
		
		
		String lpname = l_path + l_file;
		String rpname = r_path + r_file;
		

		ChannelSftp sftp = null;
		Channel chan = null;

		try{
			
			chan = session.openChannel("sftp");
			chan.connect();
			log.info("==========Sftp 通道建立成功");
			
			sftp = (ChannelSftp) chan;
			if(openAndMkDir(sftp, r_path)){
				sftp.put(lpname, rpname);
			}
			log.info("==========Sftp 文件上传成功,本地文件：[%s],远程文件:[%s]",lpname, rpname);
		}catch (JSchException e){
			e.printStackTrace();
			throw ApError.Aplt.E0419();
		}catch (SftpException e){
			e.printStackTrace();
			log.info("==========Sftp 文件上传失败,本地文件：[%s],远程文件:[%s]",lpname, rpname);
			throw ApError.Aplt.E0415();
		}finally{
			closChan(null, chan, sftp);
		}
	}
	/**
	 * @Title: download 
	 * @Description: sftp下载文件到本地 
	 * @param sftpInfo
	 * @author zhanga
	 * @date 2017年9月9日 下午4:12:09 
	 * @version V2.3.0
	 */
	public static void download(SftpInfo sftpInfo){
		
		
		Session session = getSession(sftpInfo);
		
		String l_file = sftpInfo.getLcfile();
		String r_file = sftpInfo.getRmfile();
		
		String l_path = getPath(sftpInfo.getLcpath());
		String r_path = getPath(sftpInfo.getRmpath());
		
		String rpname = r_path + r_file; //目标服务器上的文件路径名
		String lcname = l_path + l_file;
		
		ChannelSftp sftp = null;
		Channel chan = null;
		
		File file = new File(l_path);
		if(!file.exists()){
			file.mkdirs();
		}
		
		try{
			
			chan = session.openChannel("sftp");
			chan.connect();
			log.info("==========Sftp 通道建立成功");
			
			sftp = (ChannelSftp) chan;
			
			sftp.get(rpname, lcname);

			log.info("==========Sftp 文件下载成功,本地文件：[%s],远程文件:[%s]",lcname, rpname);
		}catch (JSchException e){
			e.printStackTrace();
			throw ApError.Aplt.E0419();
		}catch (SftpException e){
			e.printStackTrace();
			log.info("==========Sftp 文件下载失败,本地文件：[%s],远程文件:[%s]",lcname, rpname);
			throw ApError.Aplt.E0416();
		}finally{
			closChan(null, chan, sftp);
		}
		
	}
	/**
	 * @Title: isRemoteExist 
	 * @Description: 判断远程文件是否存在 
	 * @param sftpInfo
	 * @return
	 * @author zhanga
	 * @date 2017年9月20日 上午10:02:37 
	 * @version V2.3.0
	 */
	public static boolean isRemoteExist(SftpInfo sftpInfo){
		
		Session session = getSession(sftpInfo);
		
		String r_file = sftpInfo.getRmfile();
		String r_path = getPath(sftpInfo.getRmpath());
		
		
		
		ChannelSftp sftp = null;
		Channel chan = null;
		
		try{
			
			chan = session.openChannel("sftp");
			chan.connect();
			log.info("==========Sftp 通道建立成功");
			log.info("文件路径：[%s]", r_path);
			log.info("ok文件名：[%s]", r_file);
			sftp = (ChannelSftp) chan;
			List<ChannelSftp.LsEntry> files = sftp.ls(r_path);
			for(ChannelSftp.LsEntry entry : files){
				if(!entry.getAttrs().isDir() && entry.getFilename().equals(r_file)){
					return true;
				}
			}
			
		}catch (Exception e){
			log.info("==========Sftp 文件不存在,文件路径：[%s],远程文件:[%s]",r_path, r_file);
			return false;
		}finally{
			closChan(null, chan, sftp);
		}
		return false;
	}
	/**
	 * @Title: makeOK 
	 * @Description: 生成OK文件 
	 * @param sftpInfo
	 * @param in
	 * @author zhanga
	 * @date 2017年9月19日 下午4:08:44 
	 * @version V2.3.0
	 */
	public static void makeOK(SftpInfo sftpInfo, InputStream in){
		
		Session session = getSession(sftpInfo);
		
		String r_file = sftpInfo.getRmfile();
		String r_path = getPath(sftpInfo.getRmpath());
		
		String rpname = r_path + r_file;
		
		
		ChannelSftp sftp = null;
		Channel chan = null;
		
		try{
			
			chan = session.openChannel("sftp");
			chan.connect();
			log.info("==========Sftp 通道建立成功");
			
			sftp = (ChannelSftp) chan;
			if(!openAndMkDir(sftp, r_path)){
				log.info("创建目录失败");
			}else{
				sftp.put(in, rpname);
			}
			log.info("==========Sftp 文件上传成功,OK文件：[%s],远程路径:[%s]", r_file, r_path);
		}catch (JSchException e){
			e.printStackTrace();
			throw ApError.Aplt.E0419();
		}catch (SftpException e){
			e.printStackTrace();
			log.info("==========Sftp 文件上传失败,OK文件：[%s],远程路径:[%s]", r_file, r_path);
			throw ApError.Aplt.E0415();
		}finally{
			closChan(null, chan, sftp);
			try {
				if (CommUtil.isNotNull(in)) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @Title: closChan 
	 * @Description: 关闭sftp通道 
	 * @author zhanga
	 * @date 2017年9月9日 下午4:15:15 
	 * @version V2.3.0
	 */
	private static void closChan(Session session, Channel chan, ChannelSftp sftp){
		if(sftp != null){
			sftp.disconnect();
		}
		if(chan != null){
			chan.disconnect();
		}
		if(session != null){
			session.disconnect();
		}
	}
	/**
	 * @Title: openAndMkDir 
	 * @Description: 创建远程目录 
	 * @param path
	 * @return
	 * @author zhanga
	 * @date 2017年9月20日 上午10:02:55 
	 * @version V2.3.0
	 * @throws SftpException 
	 */
	private static boolean openAndMkDir(ChannelSftp sftp, String path) throws SftpException{
		
		try{
			
			String now = sftp.pwd();
			if(now.equals(path)){
				return true;
			}else{
				try{
					sftp.cd(path);
					return true;
				}catch(SftpException e){
					String root = "";
					String[] lsDir = null;
					if(path.startsWith(now)){
						path = path.replaceFirst(now, "");
						root = now;
						lsDir = path.split("/");
					}else{
						lsDir = path.split("/");
						root = lsDir[0] + "/";
					}
					
					sftp.cd(root);
					lsDir = (String[])ArrayUtils.removeElement(lsDir, "");
					for (String dir : lsDir){
						try {
							sftp.cd(dir);
							log.info("dir:[%s],pwd:[%s]", dir, sftp.pwd());
						} catch (SftpException e1) {
							sftp.mkdir(dir);
							sftp.cd(dir);
						}
					}
					
					return true;
				}
			}
		}catch(SftpException e){
			e.printStackTrace();
			log.info("创建目录失败:[%s]", path);
			throw ApError.Aplt.E0000("创建目录失败:["+path+"]");
		}
		
		
	}
	/**
	 * @Title: getPath 
	 * @Description: 处理路径中的文件分隔符 
	 * @param str
	 * @return
	 * @author zhanga
	 * @date 2017年9月20日 上午10:03:13 
	 * @version V2.3.0
	 */
	public static String getPath(String str){
		
		String path = "";
		if(CommUtil.isNotNull(str)){
			path = str.replace(File.separator, "/");
			if(!path.endsWith("/")){
				path = path + "/";
			}
		}
		return path;
	}
	
	/**
	 * @Title: main 
	 * @Description:密码解密 
	 * @param args
	 * @author zhanga
	 * @date 2017年11月14日 上午10:06:43 
	 * @version V2.3.0
	 */
	public static String getPass(String passwd){
		String pwd = "";
		try{
			pwd = EncryptUtils.decrypt(passwd);
		} catch (Exception e){
			e.printStackTrace();
			log.info("sftp密码解密失败");
			throw ApError.Aplt.E0000("sftp密码解密失败");
		}
		
		return pwd;
	}
	
}
