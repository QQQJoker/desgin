package cn.sunline.ltts.busi.aplt.serviceimpl.dayend;

import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.adp.cedar.base.type.KBaseEnumType;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_PLKZHIBZ;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
 /**
  * 定时日批处理触发条件实现
  * 支持全局参数控制是否启用日批处理。
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class TaskScheduleConditionControlImpl implements cn.sunline.adp.cedar.server.batch.servicetype.BatchCondition{
	private static final BizLog bizlog = BizLogUtil.getBizLog(TaskScheduleConditionControlImpl.class);
 /**
  * 判断批量任务是否可以运行
  *
  */
	public Boolean canRun(String batchId,  String parms,java.util.Map dataArea){
		String plkzhibz = E_PLKZHIBZ.RZ.getValue();
		KSysBatchTable.tsp_batch_execution_control ksys_plzxkz = KSysBatchTable.Tsp_batch_execution_controlDao.selectOne_odb_1(plkzhibz, false);
		/**
		 * 对应的日终日期没有记录，或者是记录里面的日终  是否执行标识为null,或者标示为YES  则都是要执行日终
		 */
		if (ksys_plzxkz == null 
				|| KBaseEnumType.E_YESORNO.YES.getValue().equals(ksys_plzxkz.getControl_value()))
			return true;
			
		
		bizlog.info("当前正在进行文件批处理，日终批量不允许允许");
		return false;
	}
}

