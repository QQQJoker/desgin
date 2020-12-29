package cn.sunline.ltts.busi.aplt.para;


import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;


/**
 * 
 * 文件路径处理
 *        
 */
public class ApBatchFilePath {
	/**
	 * 转换文件路径为相对路径（电子账户系统生成文件路径）       
	 */
	public static String getApBatchDownFilePath(String fipath){
		KnpPara  tbl_knpPara = CommTools.getInstance(KnpPara.class); 
		tbl_knpPara = CommTools.KnpParaQryByCorpno("DPTRAN", "RPPATH", "%", "%", true);
		String depath ="";//处理后的相对路径
		depath = fipath.substring(tbl_knpPara.getPmval2().length()-1);
		return depath;
		
	}
    
	/**
	 * 转换文件路径为相对路径（电子账户系统获取数据文件系统路径）       
	 */
	public static String getApBatchUploadFilePath(String uppath){
		return uppath;
	}
}
