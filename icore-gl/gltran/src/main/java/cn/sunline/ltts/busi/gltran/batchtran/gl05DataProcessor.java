
package cn.sunline.ltts.busi.gltran.batchtran;
import cn.sunline.ltts.busi.gl.file.GlLnFile;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
	 /**
	  * 贷款对账文件导入及检查
	  * @author 
	  * @Date 
	  */

public class gl05DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl05.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl05.Property> {
  
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl06DataProcessor.class);
	
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl05.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl05.Property property) {
		 
		 bizlog.method("gl05DataProcessor>>>>>>begin>>>>");
		 
		 GlLnFile.doCheckLnAccountingFile();
		 
		 bizlog.method("gl05DataProcessor>>>>>>begin>>>>");
	}

}


