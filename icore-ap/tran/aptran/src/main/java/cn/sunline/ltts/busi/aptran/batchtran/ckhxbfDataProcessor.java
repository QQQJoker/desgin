
package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.adp.cedar.base.logging.BizLog;
	 /**
	  * 核心换日前检查
	  * 
	  * 1.检查日终开始时间是否满足配置
	  *
	  */


public class ckhxbfDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Ckhxbf.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ckhxbf.Property> {
  
	private static BizLog log = LogManager.getBizLog(ckhxbfDataProcessor.class); 
	
	private static String parmcd = "Parmcd.ckhxbf"; //
	
	private static String pmkey1 = "dayendtime"; //
		
	
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Ckhxbf.Input input, 
							cn.sunline.ltts.busi.aptran.batchtran.intf.Ckhxbf.Property property) {
		 log.debug("ckhxbfDataProcessor begin ...");
		 
		 KnpPara tblKnpPara = ApKnpPara.getKnpPara(parmcd, pmkey1);
		 log.debug("tblKnpPara:[%s]", tblKnpPara);
		 //判断日终开始时间控制开关 Y-检查 空或N-不检查
		 if(CommUtil.compare("Y", tblKnpPara.getPmval1()) == 0){
			 
			 String currTime = DateTools.getCurrentDateTime();		//当前机器时间 yyyyMMddHHmmss
			 String trandt = CommTools.prcRunEnvs().getTrandt(); 	//当前系统账务日期yyyyMMdd
			 String dayendtime = tblKnpPara.getPmval2();			//参数配置：日终开始时间 HHmmss
			 
			 log.debug("当前机器系统时间[" + StringUtil.substr(currTime, 0, 8) + " " + StringUtil.substr(currTime, 8, 10) + ":" 
					 + StringUtil.substr(currTime, 10, 12) + ":" + StringUtil.substr(currTime, 12, 14) + "]");
			 log.debug("日终允许开始时间[" + trandt + " " + StringUtil.substr(dayendtime, 0, 2) + ":" 
					 + StringUtil.substr(dayendtime, 2, 4) + ":" + StringUtil.substr(dayendtime, 4, 6) + "]");
			 //判断当前机器时间是否大于日终开始时间配置
			 if( CommUtil.compare(currTime, trandt + dayendtime) < 0 ){
				 throw ApError.Aplt.E0000("当前机器系统时间[" + StringUtil.substr(currTime, 0, 8) + " " + StringUtil.substr(currTime, 8, 10) + ":" 
						 + StringUtil.substr(currTime, 10, 12) + ":" + StringUtil.substr(currTime, 12, 14) + 
						 "]早于日终允许开始时间[" + trandt + " " + StringUtil.substr(dayendtime, 0, 2) + ":" 
						 + StringUtil.substr(dayendtime, 2, 4) + ":" + StringUtil.substr(dayendtime, 4, 6) + "],请等待后重试." );
			 }
			 
		 }else{
			 log.debug("日终开始时间控制参数[%s]未开启,不做控制.", tblKnpPara.getPmval1());
		 }
		 
		 log.debug("ckhxbfDataProcessor end ...");
	}

}


