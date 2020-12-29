package cn.sunline.ltts.busi.aplt.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.file.FileProcessor;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BizParmInfo;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.FileInfo;
import cn.sunline.ltts.busi.aplt.coderule.ApDCN;
import cn.sunline.ltts.busi.aplt.servicetype.ApFileTask;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.sys.errors.ApError;

/**
 * 文件批量相关工具类
 * @author Administrator
 *
 */
public class FileBatchTools {
	
	public static final String FILE_ID = "fileId";
	public static final String MD5 = "md5";
	/**
	 * 获取文件名称
	 * @param plwenjlx
	 * @param wenjxxlb
	 * @return
	 * 文件名命名规则： 文件ID_渠道_文件类型_时间戳
	 */
	public static String getFileName(String plwenjlx, Map<String, Object> wenjxxlb) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(CommToolsAplt.prcRunEnvs().getTrandt()).append(File.separator);
		sb.append(ApDCN.getMyDcnNo()).append(File.separator);
		
		if (wenjxxlb != null)
			sb.append((String) wenjxxlb.get(FileUtil.FILE_ID)).append("_"); //文件id
		sb.append(CommToolsAplt.prcRunEnvs().getServtp()).append("_"); //渠道
		sb.append(plwenjlx).append("_"); //文件类型
		sb.append(DateTools.getTimeStamp()); //时间戳
		
		return sb.toString(); 
	}
	
	public static Map<String, Object> getBizParamInfo (Options<BizParmInfo> canshulb) {
		Map<String, Object> ret = new HashMap<String, Object>();
		if (canshulb != null) {
			for (BizParmInfo canshuxx : canshulb.getValues()) {
				ret.put(canshuxx.getParana(), canshuxx.getParavl());
			}
		}
		
		return ret;
	}
	
	public static Map<String, Object> getBizParamInfo(Options<BizParmInfo> canshulb,Map<String,Object> chgMap) {
		Map<String, Object> ret = new HashMap<String, Object>();
		String zhwenzhi;
		if (canshulb != null) {
			for (BizParmInfo canshuxx : canshulb.getValues()) {
				zhwenzhi=(String)(chgMap.get(canshuxx.getParana()));
				if (CommUtil.isNull(zhwenzhi))
					zhwenzhi=canshuxx.getParana();
				ret.put(zhwenzhi, canshuxx.getParavl());
			}
		}
		
		return ret;
	}
	
	public static Options<FileInfo> getFileInfo(String fileStr) {
		return getFileInfo(fileToMap(fileStr));
	}
	
	public static Options<FileInfo> getFileInfo(Map<String, Object> fileInfos) {
		Options<FileInfo> ret = new DefaultOptions<FileInfo>();
		if (fileInfos != null) {
			for (String key : fileInfos.keySet()) {
				FileInfo fileInfo = SysUtil.getInstance(FileInfo.class);
				fileInfo.setParavl(key);
				fileInfo.setParana(String.valueOf(fileInfos.get(key)));
				ret.add(fileInfo);
			}
		}
		return ret;
	}
	
	public static String getFileInfoStr(Map<String, Object> fileInfos) {
		 return SysUtil.serialize(fileInfos);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> fileToMap (String sFileInfo) {
		 return SysUtil.deserialize(sFileInfo, Map.class);
	}
	
	public static Map<String, Object> getFileInfo (String fileId, String md5) {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put(FileTools.FILE_ID, fileId);
		ret.put(FileTools.MD5, md5);
		
		return ret;
	}
	
	/*public static Map<String, Object> fileToMap (Options<FileInfo> fileInfos) {
		Map<String, Object> ret = new HashMap<String, Object>();
		if (fileInfos != null) {
			for (FileInfo fileInfo : fileInfos.getValues()) {
				ret.put(fileInfo.getCanshumc(), fileInfo.getCanshuzh());
			}
		}
		
		return ret;
	}*/
	/**
	 * @param path 原路径
	 * @param substr 需要从原路径去掉的路径串
	 * */
	public static String subPathByString(String substr,String path){
          if(path.contains(substr)){
        	  int i = substr.length();
  			  path = path.substring(i-1);
          }
          return path;		
	}
	
	/**
	 * 拷贝文件，生成最终文件（文件头+文件体）
	 * @param content 目标文件的头内容
	 * @param targetFile 目标文件，最终生成的文件
	 * @param sourceFile 来源文件，临时文件
	 */
	public static void generateFile(String content,String targetFile,String sourceFile) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel fci = null;
		FileChannel fco = null;
		File sfile = null;
        try {
            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(targetFile);
            fos.write(content.getBytes(FileProcessor.DEFAULT_ENCODING));
            fos.write(System.getProperty("line.separator").getBytes());
            fos.flush();
            fci =  fis.getChannel();
            fco = fos.getChannel();
            fci.transferTo(0,fci.size(),fco);
            fci.close();
            fco.close();
            fis.close();
            fos.close();
            sfile = new File(sourceFile);
            if(sfile.exists())
            	sfile.delete();
        } catch (FileNotFoundException e) {
            throw ApError.Aplt.E0419(e);
        } catch (IOException e) {
        	throw ApError.Aplt.E0419(e);
        } finally {
        	try {
	        	if(fci != null){
					fci.close();
	        	}
	        	if(fco != null){
					fco.close();   		
				}
				if(fis != null){
					fis.close();
				}
				if(fos != null){
					fos.close();
				}
				if(sfile.exists())
					sfile.delete();
			} catch (IOException e) {
				throw ApError.Aplt.E0419(e);
			}
        }
		/*
		File tfile = new File(targetFile);
		File sfile = new File(sourceFile);
		PrintWriter pw = null;
		Scanner scanner = null;
		InputStream input = null;
		try {
			//输出到最终文件
			pw = new PrintWriter(tfile);
			pw.println(content);
			//读取临时文件
			input = new FileInputStream(sfile);
			scanner = new Scanner(input);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				pw.println(line);
			}
			pw.flush();
			
		} catch (IOException e) {
			//TODO 
		} finally{
			if (scanner != null) {
				scanner.close();
			}
			if (pw != null) {
				pw.close();
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			sfile.delete();//删除临时文件
		}
	*/
    }
	
	/**
	 * 交易失败后，独立事务修改文件批量信息表状态
	 */
	public static void updateWjplxxbToFail(final kapb_wjplxxb wjplxxb,final Throwable t){
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>(){
			@Override
			public Void execute() {
				wjplxxb.setErrotx(wjplxxb.getBtfest().getLongName()+":"+t.getMessage());
				Kapb_wjplxxbDao.updateOne_odb1(wjplxxb);
				return null;
			}
		});
		if(!DcnUtil.isAdminDcn(DcnUtil.getCurrDCN())){//当前节点不是主节点，外调主节点变更文件批量信息表状态
			ApFileTask apFileTask = SysUtil.getRemoteInstance(ApFileTask.class);//变更主节点文件批量信息表状态
			apFileTask.updateKapbPlwjxxb(wjplxxb);
		}
	}
	
	public static void updateWjplxxbToSuccess(final kapb_wjplxxb wjplxxb){
		wjplxxb.setErrotx(wjplxxb.getBtfest().getLongName());
		Kapb_wjplxxbDao.updateOne_odb1(wjplxxb);
	}
	
	/**
	 * 按节点拆分，初始化待拆分的文件体流
	 * @param wjplxxb 文件批量信息表
	 * @return DCN---写文件体流
	 */
	public static ConcurrentMap<String,FileProcessor> getTempFileProcessors(kapb_wjplxxb wjplxxb){
		List<String> dcnnos = DcnUtil.findAllRDcnNos();
		
		if(CommUtil.isNull(dcnnos)) {
			ApError.Aplt.E0416();
		}
		ConcurrentMap<String,FileProcessor> writeTempFileMap = new ConcurrentHashMap<>();
		for (String dcnno : dcnnos) {
			//临时文件写入流
			FileProcessor file = new FileProcessor(wjplxxb.getDownph()+"split"+File.separator,dcnno+"_temp_"+wjplxxb.getDownna());
			file.open(true);
			writeTempFileMap.put(dcnno, file);
		}
		return writeTempFileMap;
	}
	
}
