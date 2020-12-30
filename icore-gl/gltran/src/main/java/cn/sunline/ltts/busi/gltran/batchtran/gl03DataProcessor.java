package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.gl.file.GlFile;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
	 /**
	  * 核心分户账余额数据文件导入
	  *
	  */

public class gl03DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl03.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl03.Property> {
  
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl03DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl03.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl03.Property property) {
		
		 bizlog.method("gl03DataProcessor >>>>begin>>>>>>>>>>");
		 
		 GlFile.doLedgerBalFile();
		 
		 bizlog.method("gl03DataProcessor >>>>end>>>>>>>>>>");

	}

}


