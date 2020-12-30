package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.item.GlCheck;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
	 /**
	  * 日终前检查
	  *
	  */

public class gl08DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl08.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl08.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl08DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl08.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl08.Property property) {
		//TODO:
		 
		// 检查缓冲区的日期与数据库中的是否一样
		bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("input [%s],property [%s]", input, property);
		
		GlCheck.prcEodBefore(CommToolsAplt.prcRunEnvs().getCorpno(), false);
		

		// TODO
		//等待文件上传
	}

}


