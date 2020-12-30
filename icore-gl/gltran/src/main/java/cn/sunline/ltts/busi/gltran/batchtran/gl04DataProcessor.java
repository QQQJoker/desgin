
package cn.sunline.ltts.busi.gltran.batchtran;
import cn.sunline.ltts.busi.gl.file.GlLnFile;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
	 /**
	  * 网贷分户账余额数据文件导入
	  * @author 
	  * @Date 
	  */

public class gl04DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl04.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl04.Property> {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl04DataProcessor.class);
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl04.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl04.Property property) {

		 bizlog.method("gl04DataProcessor >>>>begin>>>>>>>>>>");
		 
		 GlLnFile.doLnLedgerBalFile();
		 
		 bizlog.method("gl04DataProcessor >>>>end>>>>>>>>>>");
	}

}


