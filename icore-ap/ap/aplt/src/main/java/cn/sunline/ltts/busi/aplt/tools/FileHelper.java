package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.adp.core.expression.ExpressionEvaluator;
import cn.sunline.adp.core.expression.ExpressionEvaluatorFactory;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_file_info;



public class FileHelper {

	
	private static final String FILE_TRANDT = "trandt";
	
	
	/**
	* @Title: genFileInfo  
	* @Description: 使用ognl表达式组装文件名及文件路径 
	* @param @param file_info    设定文件  
	* @auther zhangan
	* @return void    返回类型  
	* @throws
	 */
	public static void genFileInfo(knp_file_info file_info){
		
		String trandt = CommTools.prcRunEnvs().getTrandt();
		ExpressionEvaluator ee = ExpressionEvaluatorFactory.getInstance();
		Params param = new Params();
		
		param.put(FILE_TRANDT, trandt);
		
		
		String rtname = ee.evalText(file_info.getRtname(), param, param);
		String lcname = ee.evalText(file_info.getLcname(), param, param);
		String rtpath = ee.evalText(file_info.getRtpath(), param, param);
		String lcpath = ee.evalText(file_info.getLcpath(), param, param);
		
		
		file_info.setRtname(rtname);
		file_info.setLcname(lcname);
		file_info.setRtpath(rtpath);
		file_info.setLcpath(lcpath);
		
	}
	
}

