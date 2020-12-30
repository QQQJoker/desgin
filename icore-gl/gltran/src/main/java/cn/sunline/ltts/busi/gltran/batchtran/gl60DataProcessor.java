package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.ApDate;
	 /**
	  * 总账换日
	  *
	  */

public class gl60DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.gl.gltran.batchtran.intf.Gl60.Input, cn.sunline.ltts.gl.gltran.batchtran.intf.Gl60.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.gl.gltran.batchtran.intf.Gl60.Input input, cn.sunline.ltts.gl.gltran.batchtran.intf.Gl60.Property property) {
//	    if (DcnUtil.isGL()) {
//	        ApDate.swithGlDate();
//	    }else{	
	        ApDate.swithSysDate();
//	    }
	}

}


