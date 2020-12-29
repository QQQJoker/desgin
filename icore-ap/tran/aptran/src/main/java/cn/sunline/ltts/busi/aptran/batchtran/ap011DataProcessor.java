package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.cleardate.ApClearDate;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppCldt;

	 /**
	  * 清算场次控制
	  *
	  */

public class ap011DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Ap011.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap011.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Ap011.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap011.Property property) {
		 
		 AppCldt tblKapp_clrdat = ApClearDate.chgClearDate();
		 
		 
	}

}


