package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_PLKZHIBZ;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SHIFOUBZ;

	 /**
	  * 解除对文件批量的运行禁止
	  *
	  */

public class ap09DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Ap09.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap09.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Ap09.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap09.Property property) {
		 String plkzhibz = E_PLKZHIBZ.WJ.getValue();
		 KSysBatchTable.tsp_batch_execution_control ksys_plzxkz = KSysBatchTable.Tsp_batch_execution_controlDao.selectOneWithLock_odb_1(plkzhibz, true);
		 ksys_plzxkz.setControl_value(E_SHIFOUBZ.YES.getValue());
		 KSysBatchTable.Tsp_batch_execution_controlDao.updateOne_odb_1(ksys_plzxkz);
	}

}


