package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.gl.file.GlFile;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 计提数据文件导入
 */

public class gl02DataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl02.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl02.Property> {

	private static final BizLog bizlog = BizLogUtil.getBizLog(gl02DataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl02.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl02.Property property) {

		bizlog.method("gl04DataProcessor  >>>begin>>>>>>>>>");
		
		GlFile.doAccureFile();
		
		bizlog.method("gl04DataProcessor  >>>end>>>>>>>>>");

	}

}
