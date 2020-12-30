
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
	  * 试算后处理
	  *
	  *	1.试算完成后，更新年结状态为00-正常
	  */


public class gl29DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl29.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl29.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl29DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl29.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl29.Property property) {
		bizlog.method("gl29 process Begin>>>>>>>>>>>>");
			
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getTrandt();
		if (DateTools2.isLastDay("Y", trxnDate)) {
			// 初始化年结状态为10-暂停
			FaTools.updateYearendStatus(FaConst.YEAREND_STATUS_NORMAL);
		}
					
		bizlog.method("gl29 process end>>>>>>>>>>>>");
	}

}


