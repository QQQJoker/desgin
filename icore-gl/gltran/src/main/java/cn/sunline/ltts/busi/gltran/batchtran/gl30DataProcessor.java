package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.gl.settlement.GlSettle;
	 /**
	  * 统一清算
	  *
	  */

public class gl30DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl30.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl30.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl30.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl30.Property property) {
		//TODO:
		 GlSettle.prcSettlement();
	}

}


