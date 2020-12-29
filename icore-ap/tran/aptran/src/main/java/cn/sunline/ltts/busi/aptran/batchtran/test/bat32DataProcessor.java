package cn.sunline.ltts.busi.aptran.batchtran.test;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.adp.cedar.base.logging.BizLog;

	 /**
	  * 核心换日测试批量交易
	  *
	  */

public class bat32DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Bat32.Input, cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Bat32.Property> {
	public static final BizLog bizlog = LogManager.getBizLog(bat32DataProcessor.class);
	
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Bat32.Input input, cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Bat32.Property property) {
		 System.out.println("=================================核心换日测试批量交易[bat32]==================================");
		 bizlog.info("=================================核心换日测试批量交易[bat32]==================================");
	}

}


