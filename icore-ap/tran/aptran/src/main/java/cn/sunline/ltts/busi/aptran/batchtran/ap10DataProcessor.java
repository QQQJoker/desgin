package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;

	 /**
	  * 换日后批处理等待...
	  *
	  */

public class ap10DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Ap10.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap10.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Ap10.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap10.Property property) {
			
		// 睡眠的目的是等待, 暂时睡眠1分钟  1000 * 60 * 1
		try{
			Thread.sleep(60000);
		}catch(InterruptedException e){
			return;
		}
	}

}


