
package cn.sunline.clwj.hist.batchtran;
import cn.sunline.edsp.base.lang.*;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
	 /**
	  * 删除表分区
	  * @author 
	  * @Date 
	  */

public class ap6002DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.bsap.batchtran.intf.Ap6002.Input, cn.sunline.edsp.busi.bsap.batchtran.intf.Ap6002.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.edsp.busi.bsap.batchtran.intf.Ap6002.Input input, cn.sunline.edsp.busi.bsap.batchtran.intf.Ap6002.Property property) {
		//TODO:
	}

}


