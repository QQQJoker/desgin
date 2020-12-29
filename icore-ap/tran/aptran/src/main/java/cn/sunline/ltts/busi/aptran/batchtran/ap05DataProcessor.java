package cn.sunline.ltts.busi.aptran.batchtran;

import java.util.concurrent.TimeUnit;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.BatchTools;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.adp.cedar.base.logging.BizLog;

	 /**
	  * 等待所有正在处理的批量任务结束
	  *
	  */

public class ap05DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Ap05.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap05.Property> {
  
	private static final BizLog bizlog = LogManager.getBizLog(ap05DataProcessor.class);
	
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Ap05.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap05.Property property) {
		 while (true) {
			 //TODO:需要考虑定时任务
			 //TODO:需要支持中途停止功能
			 if (BatchTools.hasOtherTaskProcessing(BatchTools.getCurrentTaskId()))
				 return;
			 
			 int minutes = 5;
			 
			 bizlog.info("等待正在运行的联机批量任务，[%d]分钟后继续检查...", minutes);
			
			 //等待
			 try {
				TimeUnit.MINUTES.sleep(minutes);
			} catch (InterruptedException e) {
			}
		 }
	}

}


