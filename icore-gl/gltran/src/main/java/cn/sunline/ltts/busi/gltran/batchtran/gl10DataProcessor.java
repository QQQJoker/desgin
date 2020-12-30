package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.fa.parm.FaSysService;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
	 /**
	  * 总账服务关闭
	  *
	  */

public class gl10DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl10.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl10.Property> {
  
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl70DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl10.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl10.Property property) {
		//TODO:
			bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
			bizlog.parm("input [%s],property [%s]", input, property);
			// 将账务服务状态变更为关闭
			FaSysService.setSysClose();
			bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
	}

}


