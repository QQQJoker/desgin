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
	  * 生成业务总账-报表机构
	  *
	  */

public class gl52DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl52.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl52.Property> {
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlBranch.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl52.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl52.Property property) {
		BIZLOG.method("gl52 process [%s] Begin>>>>>>>>>>>>");
		 
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getTrandt();
		String orgId = runEnvs.getCorpno();
		
		E_REPORTTYPE reportType = E_REPORTTYPE.BUSINETT_TYPE;
		if(DateTools2.isLastDay("Y", trxnDate) && !FaTools.getYearendStatus()){
			reportType = E_REPORTTYPE.TRIAL_TYPE;
		}
		//逐级向上统计
		GlGeneralLedger.genReportGL(orgId, reportType, trxnDate);
		BIZLOG.method("gl52 process [%s] end>>>>>>>>>>>>");
		}

}


