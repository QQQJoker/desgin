package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_PLKZHIBZ;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SHIFOUBZ;

	 /**
	  * 禁用文件批量
	  *
	  */

public class ap04DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Ap04.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap04.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Ap04.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap04.Property property) {
		 String plkzhibz = E_PLKZHIBZ.WJ.getValue();
		 KSysBatchTable.tsp_batch_execution_control ksys_plzxkz = KSysBatchTable.Tsp_batch_execution_controlDao.selectOneWithLock_odb_1(plkzhibz, false);
		 
		 if (ksys_plzxkz == null) {
			 ksys_plzxkz = SysUtil.getInstance(KSysBatchTable.tsp_batch_execution_control.class);
			 ksys_plzxkz.setControl_code(plkzhibz);
			 ksys_plzxkz.setControl_value(E_SHIFOUBZ.NO.getValue());
			 ksys_plzxkz.setDesc_message("控制文件批量是否允许执行");
			 KSysBatchTable.Tsp_batch_execution_controlDao.insert(ksys_plzxkz);
		 } else {
			 ksys_plzxkz.setControl_value(E_SHIFOUBZ.NO.getValue());
			 KSysBatchTable.Tsp_batch_execution_controlDao.updateOne_odb_1(ksys_plzxkz);
		 }
	}

}


