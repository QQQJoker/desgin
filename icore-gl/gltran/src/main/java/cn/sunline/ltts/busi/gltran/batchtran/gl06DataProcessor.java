package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.gl.file.GlFile;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
	 /**
	  * 对账文件导入及检查
	  *
	  */

public class gl06DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl06.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl06.Property> {
  
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl06DataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl06.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl06.Property property) {
		bizlog.method("gl03DataProcessor>>>>>>begin>>>>");
		
		GlFile.doCheckAccountingFile();
		
		bizlog.method("gl03DataProcessor>>>>>>end>>>>>");
		 
	}

}


