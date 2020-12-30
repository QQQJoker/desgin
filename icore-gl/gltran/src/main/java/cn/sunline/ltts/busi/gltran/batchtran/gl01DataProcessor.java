package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.gl.item.GlFileCheck;
	 /**
	  * 总账日终前文件检查
	  *
	  */

public class gl01DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl01.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl01.Property> {
  
    private static final BizLog BIZLOG = BizLogUtil.getBizLog(gl01DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl01.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl01.Property property) {
	     
	     BIZLOG.method("gl01DataProcessor >>>>begin>>>>>>>>>>");
	     //检查存贷计提文件
	     GlFileCheck.doCheckFiles();
	     
	     //检查分户账余额文件
	     GlFileCheck.doCheckBalFile();
	     
	     //检查对账文件
	     GlFileCheck.doCheckAccountingFile();
	     
	     //会计流水检查
	     GlFileCheck.doCheckEodBefore();
	     
	     BIZLOG.method("gl01DataProcessor >>>>end>>>>>>>>>>");
	     
	}

}


