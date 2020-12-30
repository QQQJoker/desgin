
package cn.sunline.ltts.busi.gl.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaTools;
	 /**
	  * 试算前处理
	  *
	  *	1.判断年结状态为空或10-暂停时，进入试算流程，否则报错
	  */


public class gl27DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl27.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl27.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl29DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl27.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl27.Property property) {
		bizlog.method("gl27 process Begin>>>>>>>>>>>>");
			
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getTrandt();
		if (!DateTools2.isLastDay("Y", trxnDate) || FaTools.getYearendStatus()) {
			bizlog.method("非年结当天或非年结状态[10]，不允许进入年结试算流程！");
			throw GlError.GL.E0198();
		}
					
		bizlog.method("gl27 process end>>>>>>>>>>>>");
	}

}


