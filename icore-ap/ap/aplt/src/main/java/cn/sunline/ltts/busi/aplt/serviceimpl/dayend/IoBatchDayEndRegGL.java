package cn.sunline.ltts.busi.aplt.serviceimpl.dayend;

import java.util.Map;

import cn.sunline.adp.cedar.base.dict.KsDict;
import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_PILJYZHT;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.custom.comm.servicetype.DayEndBatch;
import cn.sunline.adp.cedar.custom.comm.type.DayEndBatchType.TaskProcessIn;
import cn.sunline.adp.cedar.server.batch.namedsql.SyDistributedBatchDao;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.Tsp_taskDao;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;
 /**
  * 通知小总账日终开始执行
  *
  */
@cn.sunline.adp.core.annotation.Generated
@Deprecated
public class IoBatchDayEndRegGL implements cn.sunline.ltts.busi.iobus.servicetype.ap.IoApEvent{
 /**
  * 通用的事件处理服务
  *
  */
	public void onEvent(String shijneir){
		if(StringUtil.isEmpty(shijneir))
			throw ApError.Aplt.E0000("[通知小总账日终开始执行]事件处理服务输入参数不能为空！");
		TaskProcessIn dotaskin = SysUtil.getInstance(TaskProcessIn.class);
		dotaskin = SysUtil.deserialize(shijneir, TaskProcessIn.class);
		DayEndBatch dayendbatch = SysUtil.getInstance(DayEndBatch.class);
		Map<String, Object> glInfo = SyDistributedBatchDao.queryGLDate(dotaskin.getCorporate_code(), false);
		
		String jiaoyirq = (String)glInfo.get(KsDict.BtDict.tran_date.getId());
		String scjioyrq = (String)glInfo.get(KsDict.BtDict.last_date.getId());
		//获取小总账系统当前日终日期，与传过来的核心日终日期做比较
		String batchId = dotaskin.getTran_flow_id() + "_" + jiaoyirq;
		KSysBatchTable.tsp_task currRunInfo = Tsp_taskDao.selectOne_odb_1(batchId, false);
		String preBatchId = dotaskin.getTran_flow_id() + "_" + scjioyrq;
		KSysBatchTable.tsp_task preRunInfo = Tsp_taskDao.selectOne_odb_1(preBatchId, false);
		
		if(currRunInfo == null && preRunInfo != null && preRunInfo.getTran_state() != E_PILJYZHT.success) 
			jiaoyirq = scjioyrq;
		
		if(CommUtil.compare(jiaoyirq, dotaskin.getQuery_date()) > 0)
			throw ApError.Aplt.E0000("小总账系统日终日期不能大于核心系统日终日期！");
		dotaskin.setQuery_date(jiaoyirq); 
		dayendbatch.doTaskProcess(dotaskin);
	}
}

