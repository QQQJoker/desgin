package cn.sunline.ltts.busi.aplt.serviceimpl.dayend;

import java.util.List;

import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_PILJYZHT;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.custom.comm.biz.util.DcnDataUtil;
import cn.sunline.adp.cedar.custom.comm.type.DayEndBatchType.TaskProcessIn;
import cn.sunline.adp.cedar.server.batch.namedsql.SyDistributedBatchDao;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;

/**
 * 通知ADMBatch该DCN执行日终批量成功
 * 
 */
@cn.sunline.adp.core.annotation.Generated
public class IoBatchDayEndRegADM implements
		cn.sunline.ltts.busi.iobus.servicetype.ap.IoApEvent {
	/**
	 * 通用的事件处理服务
	 * 
	 * @throws
	 * 
	 */
	public void onEvent(String shijneir) {
		TaskProcessIn dotaskin = SysUtil.getInstance(TaskProcessIn.class);
		dotaskin = SysUtil.deserialize(shijneir, TaskProcessIn.class);
		String dqrizriq = dotaskin.getQuery_date();
		String farendma = dotaskin.getCorporate_code();
		String pljylcbs = dotaskin.getTran_flow_id();
		String xitongbs = dotaskin.getDcn_num();

		// IMS指标字段定义
		String flowId = pljylcbs;
		String returnMessage = null;

		KSysBatchTable.tsp_dayend_exe_management dsrzzx = KSysBatchTable.Tsp_dayend_exe_managementDao
				.selectOne_odb_1(pljylcbs, dqrizriq, false);
		if (dsrzzx == null) {
			// 当根据pljylcbs, dqrizriq, farendma参数查询不到记录时，初始化定时日终执行表
			int totalDcn = 0;
			try {
				List<String> dcnNos = DcnUtil.findAllDcnNosWithAdmin();
				totalDcn = dcnNos.size();
			} catch (Throwable e) {
				throw ApError.Aplt.E0000("通过GNSUtil获取所有DCN编号异常", e);
			}
			if (totalDcn == 0)
				throw ApError.Aplt.E0000("没有可用DCN");
			KSysBatchTable.tsp_dayend_exe_management initdsrzzx = SysUtil
					.getInstance(KSysBatchTable.tsp_dayend_exe_management.class);
			initdsrzzx.setCurrent_dayend_date(dqrizriq);
			initdsrzzx.setCorporate_code(farendma);
			initdsrzzx.setSuccess_dcn_num(1);
			initdsrzzx.setSystem_code(xitongbs);
			initdsrzzx.setTotal_dcn_num(totalDcn);
			initdsrzzx.setTran_flow_id(pljylcbs);
			KSysBatchTable.Tsp_dayend_exe_managementDao.insert(initdsrzzx);
		} else {
			if (dsrzzx.getSuccess_dcn_num()>= dsrzzx.getTotal_dcn_num())
				throw ApError.Aplt.E0000("目前所有DCN已经成功跑完！不允许重复执行");
			SyDistributedBatchDao.updateKsysDsrzzxSusCount(dqrizriq, pljylcbs);
		}

		dsrzzx = KSysBatchTable.Tsp_dayend_exe_managementDao.selectOne_odb_1(pljylcbs,
				dqrizriq, true);

		// 当前执行成功的日终批量是最后一个DCN则通知小总账系统开始执行日终
		if (dsrzzx.getSuccess_dcn_num().equals(dsrzzx.getTotal_dcn_num())
				&&(ApConstants.hx_dayend.equals(dsrzzx.getTran_flow_id())
				  ||ApConstants.hx_swday.equals(dsrzzx.getTran_flow_id()))
		   ) {
			// TODO:更新核心换日后日期信息,独立事务
			KSysBatchTable.tsp_date_management plrzrqgl = KSysBatchTable.Tsp_date_managementDao
					.selectOne_odb_1(pljylcbs, farendma, false);
			if (plrzrqgl == null)
				DcnDataUtil.registerPlrzrqgl(pljylcbs, dqrizriq, farendma,
						E_PILJYZHT.success.getValue());
			else
				DcnDataUtil.updatePlrzrqgl(pljylcbs, dqrizriq, farendma);

			// //等待5分钟后才通知小总账开始执行
			// try {
			// TimeUnit.MINUTES.sleep(5);
			// } catch (InterruptedException e) {
			// throw ApError.Aplt.E0000("sleep失败", e);
			// }
			// 20150116 执行小总账批量日终流程

			// 所有DCN的日终批量流程 换日及换日后全部执行完，上报，将执行总账的日终批量
//			returnMessage = "所有DCN在日终日期[" + dqrizriq
//					+ "]的核心日终批量流程[换日及换日后]执行成功,将执行总账日终批量!";
//			cn.sunline.ltts.plugin.monitor.util.RealReportUtil.alertIMSMsg(
//					"flow", flowId, "success", returnMessage);

		} else if (ApConstants.gl_dayend.equals(dsrzzx.getTran_flow_id())) {
			returnMessage = "日终日期[" + dqrizriq + "]的总账日终执行成功!";
//			RealReportUtil
//					.alertIMSMsg("flow", flowId, "success", returnMessage);
			// TODO:更新核心换日后日期信息,独立事务
			KSysBatchTable.tsp_date_management plrzrqgl = KSysBatchTable.Tsp_date_managementDao
					.selectOne_odb_1(pljylcbs, farendma, false);
			if (plrzrqgl == null)
				DcnDataUtil.registerPlrzrqgl(pljylcbs, dqrizriq, farendma,
						E_PILJYZHT.success.getValue());
			else
				DcnDataUtil.updatePlrzrqgl(pljylcbs, dqrizriq, farendma);

		} else if (dsrzzx.getSuccess_dcn_num().equals(dsrzzx.getTotal_dcn_num())
				&& ApConstants.hx_before.equals(dsrzzx.getTran_flow_id())) {
//			returnMessage = "所有DCN在日终日期[" + dqrizriq + "]的日终批量流程[换日前]执行成功！";
//			RealReportUtil
//					.alertIMSMsg("flow", flowId, "success", returnMessage);

			// TODO:更新核心换日后日期信息,独立事务
			KSysBatchTable.tsp_date_management plrzrqgl = KSysBatchTable.Tsp_date_managementDao
					.selectOne_odb_1(pljylcbs, farendma, false);
			if (plrzrqgl == null)
				DcnDataUtil.registerPlrzrqgl(pljylcbs, dqrizriq, farendma,
						E_PILJYZHT.success.getValue());
			else
				DcnDataUtil.updatePlrzrqgl(pljylcbs, dqrizriq, farendma);

			// add 20150425 baoshang
			// before执行后开始执行hx_dayend
			// dotaskin.setPljylcbs("hx_dayend");
			// shijneir = SysUtil.serialize(dotaskin);
			// BatchTools.fireService("HxBatchBegin", shijneir);
		}
	}
}
