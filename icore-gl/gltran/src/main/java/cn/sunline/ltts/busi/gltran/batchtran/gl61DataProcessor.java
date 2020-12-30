package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
	 /**
	  * 日切后检查
	  *
	  */

public class gl61DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.gl.gltran.batchtran.intf.Gl61.Input, cn.sunline.ltts.gl.gltran.batchtran.intf.Gl61.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl61DataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.gl.gltran.batchtran.intf.Gl61.Input input, cn.sunline.ltts.gl.gltran.batchtran.intf.Gl61.Property property) {
		bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("input [%s],property [%s]", input,property); 

		//ApDateInfo dateInfo = ApDate.getInfo(ApOrg.getReferenceOrgId(app_date.class));
		ApSysDateStru dateInfo = DateTools.getDateInfo();
		String sysDate = dateInfo.getSystdt();  // 20201013
		String trxnDate = CommToolsAplt.prcRunEnvs().getTrandt();

		if (!CommUtil.equals(trxnDate, sysDate))
			throw GlError.GL.E0016(trxnDate, dateInfo.getSystdt());
	}

}


