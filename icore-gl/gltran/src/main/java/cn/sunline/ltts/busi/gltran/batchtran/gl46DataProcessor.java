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
	 /**
	  * 年终决算折算损益科目结转上划
	  *
	  */

public class gl46DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl46.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl46.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(GlBranch.class); 
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl46.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl46.Property property) {
		bizlog.method("gl46 process [%s] Begin>>>>>>>>>>>>");
		 
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getTrandt();
		String orgId = runEnvs.getCorpno();
		E_REPORTTYPE reportType = E_REPORTTYPE.BUSINETT_TYPE;
		
		if (DateTools2.isLastDay("Y", trxnDate)) {
			//损益结转
			GlExchange.prcExchangeIncomeToZero(orgId, reportType, trxnDate);
			//利润上划
			GlExchange.prcExchangeProfitUp(orgId, reportType, trxnDate);
		}
		bizlog.method("gl46 process [%s] end>>>>>>>>>>>>");
}

}


