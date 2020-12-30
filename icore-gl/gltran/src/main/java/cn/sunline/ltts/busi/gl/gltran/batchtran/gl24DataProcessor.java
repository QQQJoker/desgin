
package cn.sunline.ltts.busi.gl.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.gl.item.GlBranch;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaTools;
	 /**
	  * 结转前处理
	  *
	  *	1.判断年结状态为空或10-暂停时，批量流程暂停进入试算流程
	  *
	  */


public class gl24DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl24.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl24.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(GlBranch.class);
	
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl24.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl24.Property property) {
		bizlog.method("gl24 process Begin>>>>>>>>>>>>");
			
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getTrandt();
		if (DateTools2.isLastDay("Y", trxnDate)) {
			// 年结状态为false时，年结暂停进入试算流程
			if(!FaTools.getYearendStatus()){
				bizlog.method("年结暂停，请进入年结试算流程！试算确认完成后，继续总账系统日终流程");
				throw GlError.GL.E0199();
			}
		}
					
		bizlog.method("gl24 process end>>>>>>>>>>>>");
	}

}


