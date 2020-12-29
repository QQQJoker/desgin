package cn.sunline.ltts.busi.aplt.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;

import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;
import cn.sunline.adp.cedar.base.logging.BizLog;

//import com.dap.exception.FileNotFoundException;

public class CompressFileUtils {
	public static BizLog bizlog = LogManager.getBizLog(CompressFileUtils.class);
	private static final int BUFFER_SIZE = 1024;
	private static String filesparator = System.getProperty("file.separator");
	/**
	 * *.tar.Z => *.tar
	 * @param path
	 */
	public static void zToTar(String path,String descPath){
		ZCompressorInputStream zIn = null;
		FileOutputStream out = null;
		String targetFilePath = null;
		if(CommUtil.isNotNull(descPath)){
			 targetFilePath = descPath+File.separator+getFileNameWithoutExtend(path);
			
		}else{
			
			 targetFilePath = getFilePrefix(path)+File.separator+getFileNameWithoutExtend(path);
		}
		
		try {
			zIn = new ZCompressorInputStream(new BufferedInputStream(new FileInputStream(path)));
			out = new FileOutputStream(targetFilePath);
			int n=0;
			byte[] buffer = new byte[BUFFER_SIZE];
			while(-1!=(n = zIn.read(buffer))){
				out.write(buffer, 0, n);
			}
			bizlog.parm("zToTar执行成功：[%s]", path);
//		} catch (FileNotFoundException e) {
//			bizlog.error(e.getMessage(),e);
//			throw ExceptionUtil.wrapThrow(e);
		} catch (IOException e) {
			bizlog.error(e.getMessage(),e);
			throw ExceptionUtil.wrapThrow(e);
		}finally {
			try {
				out.close();
				zIn.close();
			} catch (IOException e2) {
				bizlog.error(e2.getMessage(),e2);
				throw ExceptionUtil.wrapThrow(e2);
			}
		}
	}
	/**
	 * 归档文件解开到本地
	 * @param path
	 * @throws IOException 
	 * @throws ArchiveException 
	 */
	public void deArchiveLocal(String path) throws ArchiveException, IOException{
		String target = getFilePrefix(path)+getFileNameWithoutExtend(path);
		deArchive(path,target);
	}
	/**
	 * 解开归档文件
	 * @param source
	 * @param target
	 * @throws ArchiveException 
	 * @throws IOException 
	 */
	public static void deArchive(String source,String target){
		File sourceFolder = new File(source);
		TarArchiveInputStream archInputStream = null;
		try{
			archInputStream = new TarArchiveInputStream(new FileInputStream(sourceFolder));
			deArchive(archInputStream,target);
		}catch(Exception e){
			bizlog.error(e.getMessage(),e);
			throw ExceptionUtil.wrapThrow(e);
		}finally {
			try {
				if(archInputStream != null){
					archInputStream.close();
				}
			} catch (Exception e2) {
				bizlog.error(e2.getMessage(),e2);
				throw ExceptionUtil.wrapThrow(e2);
			}
		}
		
	}
	/**
	 * 解开归档文件
	 * @param source
	 * @param target
	 * @throws ArchiveException 
	 * @throws IOException 
	 */
	public static void deArchive(TarArchiveInputStream archInputStream ,String target) throws ArchiveException, IOException{
		TarArchiveEntry entry = null;  
		while((entry = archInputStream.getNextTarEntry())!=null){
			String filePath = target + File.separator + entry.getName();
			File targetFile = new File(filePath);
			
			fileProber(targetFile);
			
			if(entry.isDirectory()){
				targetFile.mkdir();
			}else{
				deArchiveFile(archInputStream,targetFile);
			}
		}
	}
	/**
	 * 递归创建父目录
	 * @param targetFile
	 */
	private static void fileProber(File targetFile) {
		File parentFile = targetFile.getParentFile();
		if(!parentFile.exists()){
			fileProber(parentFile);
			parentFile.mkdir();
		}
	}
	/**
	 * 解归档文件
	 * @param archInputStream
	 * @param targetFile
	 * @throws IOException
	 */
	private static void deArchiveFile(TarArchiveInputStream archInputStream, File targetFile){
		BufferedOutputStream bufout = null;
		try{
			bufout = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] datas = new byte[BUFFER_SIZE];
			while(archInputStream.available() > 0){
				if(archInputStream.available() < BUFFER_SIZE){
					datas = new byte[archInputStream.available()];
					archInputStream.read(datas, 0, archInputStream.available());
				}else{
					archInputStream.read(datas, 0, BUFFER_SIZE);
				}
				
				bufout.write(datas);
			}
		}catch(IOException e){
			bizlog.error(e.getMessage(),e);
			throw ExceptionUtil.wrapThrow(e);
		}finally {
			try {
				if(bufout != null)
					bufout.close();
			} catch (Exception e2) {
				bizlog.error(e2.getMessage(),e2);
				throw ExceptionUtil.wrapThrow(e2);
			}
		}
	}
	
	public static String getFilePrefix(String path){
		return path.substring(0,path.lastIndexOf(File.separator));
	}
	public static String getFileName(String path){
		return path.substring(path.lastIndexOf(File.separator)+1);
	}
	/**
	 * 采用递归方式遍历制定文件夹，扫描txt类型文件
	 * 
	 * @param file
	 *            文件路径
	 * @param arrayLFiles
	 *            文件
	 */
	public static void showList(File file, ArrayList<File> arrayLFiles,
			String flag) {

		File[] fileArr = file.listFiles();

		for (File pFile : fileArr) {
			if (pFile.isDirectory()) {
				showList(pFile, arrayLFiles, flag);
			} else {
				if ((pFile.getName()).endsWith(flag)) {
					arrayLFiles.add(pFile);
				}
			}
		}
	}	
	/**
	 * 例子：CORE_128000_20160426_ADD.tar.Z
	 * @param path
	 * @return CORE_128000_20160426_ADD.tar
	 */
	public static String getFileNameWithoutExtend(String path){
		String fileName = getFileName(path);
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
	/**
	 * 例子：CORE_128000_20160426_ADD.tar.Z
	 * @param path
	 * @return Z
	 */
	public static String getFileExtend(String path){
		return path.substring(path.lastIndexOf(".")+1);
	}
	public static Boolean isTar(String path){
		return "tar".equals(getFileExtend(path))? true : false;
	}
	public static Boolean isGz(String path){
		return "gz".equals(getFileExtend(path))? true : false;
	}
	public static Boolean isZ(String path){
		return "Z".equals(getFileExtend(path))? true : false;
	}
	
	public static void main(String[] args) throws InterruptedException {
//		String path_bak = "D:\\Users\\chenlinkang\\Desktop\\柜面核心提供参数文件\\CORE_128000_20160426_ADD.tar.Z";
//		String path = "D:\\Users\\chenlinkang\\Desktop\\柜面核心提供参数文件\\CORE_128000_20160426_ADD.tar.Z";
//		String target = "D:\\SunLineWorkDir\\tar\\柜面核心提供参数文件\\test";
//		String target1= "D:\\SunLineWorkDir\\tar\\柜面核心提供参数文件\\test1";
//		System.out.println(getFilePrefix(path));
//		System.out.println(getFileName(path));
//		System.out.println(getFileNameWithoutExtend(path));
//		System.out.println(getFileExtend(path));
//		System.out.println("tar:"+isTar(path));
//		System.out.println("gz:"+isGz(path));
//		zToTar(path);
//		TimeUnit.SECONDS.sleep(2);
//		deArchive(getFilePrefix(path)+File.separator+getFileNameWithoutExtend(path), target);
//		deArchive(path, target1);
		
		DateTools2.calDepositDays(covStringToDate("20160820"),covStringToDate( "20180815"));
	}
	/**
	 * @Author T
	 *         <p>
	 *         <li>2014年3月11日-下午5:10:31</li>
	 *         <li>功能说明：将字符串日期转为日期类型</li>
	 *         </p>
	 * @param sDate
	 *            字符串日期
	 * @return Date 日期类型
	 */
	public static Date covStringToDate(String sDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date d = new Date();
		try {
			d = dateFormat.parse(sDate);
		}
		catch (ParseException e) {
			throw Sys.E0003(e);
		}
		return d;
	}	
	/**
	 * 
	 * @Title: deleteFile
	 * @Description: 删除该文件夹以及文件夹下所有文件
	 * @param file
	 * @author zhangjunlei
	 * @date 2016年7月27日 下午4:11:39
	 * @version V2.3.0
	 */
	public static void deleteFile(File file) {

		if (file.exists()) {

			if (file.isFile()) {// 该文件不是文件夹
				file.delete();
			} else {

				String[] childFilePaths = file.list();
				for (String childFilePath : childFilePaths) {

					// 子路径
					File childFile = new File(file.getAbsolutePath()+ filesparator + childFilePath);
					deleteFile(childFile);// 递归删除文件夹下内容
			
	}
				file.delete();// 删除文件夹
			}
		}
	}
	
	
}
