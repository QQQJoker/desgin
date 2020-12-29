package cn.sunline.ltts.busi.aplt.serviceimpl.fbat;

import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.adp.cedar.base.type.KBaseEnumType;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_PLKZHIBZ;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
 /**
  * 文件批量日终期间不可运行服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class ApFileDayendBatchDisableImpl implements cn.sunline.adp.cedar.server.batch.servicetype.BatchCondition{
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApFileDayendBatchDisableImpl.class);
	
	 /**
	  * 判断批量任务是否可以运行
	  *
	  */
	public Boolean canRun(String batchId, String parms,java.util.Map dataArea){
		String plkzhibz = E_PLKZHIBZ.WJ.getValue();
		KSysBatchTable.tsp_batch_execution_control ksys_plzxkz = KSysBatchTable.Tsp_batch_execution_controlDao.selectOne_odb_1(plkzhibz, false);
		
		if (ksys_plzxkz == null 
				|| KBaseEnumType.E_YESORNO.YES.getValue().equals(ksys_plzxkz.getControl_value()))
			return true;
			
		
		bizlog.info("当前正在进行日终批处理，文件批量不允许允许");
		return false;
	}
	
}

