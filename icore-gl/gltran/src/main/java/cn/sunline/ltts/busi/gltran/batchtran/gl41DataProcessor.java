package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.gl.item.GlBranch;
import cn.sunline.ltts.busi.gl.item.GlExchange;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaTools;
	 /**
	  * 折币四舍五入误差补平
	  *
	  */

public class gl41DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl41.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl41.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(GlBranch.class);  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl41.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl41.Property property) {
		bizlog.method("gl41 process [%s] Begin>>>>>>>>>>>>");
		 
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		String trxnDate = runEnvs.getTrandt();
		E_REPORTTYPE reportType = E_REPORTTYPE.BUSINETT_TYPE;
		if(DateTools2.isLastDay("Y", trxnDate) && !FaTools.getYearendStatus()){
			reportType = E_REPORTTYPE.TRIAL_TYPE;
		}
		GlExchange.prcFillBal(orgId, trxnDate, reportType);
		bizlog.method("gl41 process [%s] end>>>>>>>>>>>>");
	}

}


