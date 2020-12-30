
package cn.sunline.ltts.busi.gl.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
	 /**
	  * 结转后处理
	  *
	  *	1.初始化年结状态为10-暂停（下一年年结使用）
	  */


public class gl28DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl28.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl28.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl28DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl28.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl28.Property property) {
		bizlog.method("gl28 process Begin>>>>>>>>>>>>");
			
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getTrandt();
		
		if (DateTools2.isLastDay("Y", trxnDate)) {
			// 初始化年结状态为10-暂停
			FaTools.updateYearendStatus(FaConst.YEAREND_STATUS_STOP);
		}
					
		bizlog.method("gl28 process end>>>>>>>>>>>>");
	}

}


