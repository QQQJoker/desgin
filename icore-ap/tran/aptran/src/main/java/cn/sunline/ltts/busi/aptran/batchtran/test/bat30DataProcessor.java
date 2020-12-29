package cn.sunline.ltts.busi.aptran.batchtran.test;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.adp.cedar.base.logging.BizLog;

	 /**
	  * 核心批前检查测试批量交易
	  *
	  */

public class bat30DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Bat30.Input, cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Bat30.Property> {
	public static final BizLog bizlog = LogManager.getBizLog(bat30DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Bat30.Input input, cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Bat30.Property property) {
		System.out.println("=================================核心批前检查测试批量交易[bat30]==================================");
		bizlog.info("=================================核心批前检查测试批量交易[bat30]==================================");
	}

}


