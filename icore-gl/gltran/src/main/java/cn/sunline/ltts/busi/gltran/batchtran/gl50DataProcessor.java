package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.gl.item.GlBranch;
import cn.sunline.ltts.busi.gl.item.GlGeneralLedger;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaTools;
	 /**
	  * 业务总账生成前处理
	  *
	  */

public class gl50DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl50.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl50.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(GlBranch.class); 
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl50.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl50.Property property) {
		bizlog.method("gl50 process [%s] Begin>>>>>>>>>>>>");
	 
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		String trxnDate = runEnvs.getTrandt();
		E_REPORTTYPE report_type = E_REPORTTYPE.BUSINETT_TYPE;
		if(DateTools2.isLastDay("Y", trxnDate) && !FaTools.getYearendStatus()){
			report_type = E_REPORTTYPE.TRIAL_TYPE;
		}
		
		GlGeneralLedger.prcBusiGlBefore(orgId, trxnDate, report_type);;
		
		
		bizlog.method("gl50 process [%s] end>>>>>>>>>>>>");
	}

}


