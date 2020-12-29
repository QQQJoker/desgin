package cn.sunline.ltts.busi.aptran.batchtran.dayend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.custom.comm.biz.util.DcnDataUtil;
import cn.sunline.adp.cedar.custom.comm.servicetype.DayEndBatch;
import cn.sunline.adp.cedar.custom.comm.type.DayEndBatchType.TaskProcessIn;
import cn.sunline.adp.cedar.server.batch.collector.DataCollector;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.adp.metadata.base.util.CommUtil_;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.sys.errors.ApError;

	 /**
	  * 定时执行日终换日前流程
	  *
	  */

public class ap35DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aplt.batchtran.dayend.intf.Ap35.Input, cn.sunline.ltts.busi.aplt.batchtran.dayend.intf.Ap35.Property> {
	public static final BizLog bizlog = LogManager.getBizLog(ap35DataProcessor.class);
	private final String alertType = "check";
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aplt.batchtran.dayend.intf.Ap35.Input input, cn.sunline.ltts.busi.aplt.batchtran.dayend.intf.Ap35.Property property) {
		String mySysId = CommTools.getMySysId();
	    if (!mySysId.equals("3050")) {
	        bizlog.info("只需在DepositAdmBatch(3050)子系统上执行，当前子系统[%s]无需执行，跳过！", mySysId);
	        return;
	    }
	    
		 TaskProcessIn dotaskin = SysUtil.getInstance(TaskProcessIn.class);
		 String flowType = input.getTran_flow_id();
		 String farendma = input.getCorpno();
		 String chaxriqi = DcnDataUtil.queryAllDcnDayEnd(farendma, flowType);
		 String last_chaxriqi = DateTimeUtil.mountDate(chaxriqi, 1);
		 
		 if(!"hx_before".equals(flowType))
			 throw ApError.Aplt.E0000("输入的批量流程类型有误，期待：[hx_before]，实际：[" + flowType + "]！");
		 
		 KSysBatchTable.tsp_dayend_exe_management curDsrzzx = KSysBatchTable.Tsp_dayend_exe_managementDao.selectOne_odb_1(flowType, chaxriqi, false);
//		 KSysBatchTable.tsp_dayend_exe_management curDsrzzx = KSysBatchTable.Tsp_dayend_exe_managementDao.selectOne_odb_1(flowType, chaxriqi, farendma, false);
//		 if (curDsrzzx != null) {
//			String returnMessage = "因为日终日期：[" + chaxriqi + "] 的核心日终正在执行，所以不再发送[" + chaxriqi + "] 的核心换日前流程执行请求！";
//			RealReportUtil.alertIMSMsg(alertType, flowType, "error", returnMessage);
//			return;
//		 }
		 
		 KSysBatchTable.tsp_dayend_exe_management preDsrzzx = KSysBatchTable.Tsp_dayend_exe_managementDao.selectOne_odb_1("hx_dayend", last_chaxriqi, false);
//		 KSysBatchTable.tsp_dayend_exe_management preDsrzzx = KSysBatchTable.Tsp_dayend_exe_managementDao.selectOne_odb_1("hx_dayend", last_chaxriqi, farendma, false);
//		 if (preDsrzzx != null && !preDsrzzx.getZddcnges().equals(preDsrzzx.getCgdcnges())) {
//			String returnMessage = "因为上日日终日期：[" + last_chaxriqi + "] 的核心换日及换日后批量流程在各DCN未全部执行成功，所以不发送[" + chaxriqi + "] 的核心换日前流程执行请求！";
//			RealReportUtil.alertIMSMsg(alertType, flowType, "error", returnMessage);
//			return;
//		 }
		
		 dotaskin.setQuery_date(chaxriqi);;
//		 dotaskin.setdcn_num(DMBUtil.getIntanse().getConfiguredDcnNo());
		 dotaskin.setDcn_num(CommTools.getMySysId());
		 dotaskin.setCorporate_code(farendma);
		 dotaskin.setTran_flow_id(flowType);
		 
		// 初始化输入参数对象
		List<String> dcnNos;
//		try {
//			dcnNos = DMBUtil.getIntanse().findAllDcnNosWithAdmin();
			dcnNos = DcnUtil.findAllDcnNosWithAdmin();
//		} catch (GNSAccessException e1) {
//			throw ApError.Aplt.E0000("通过GNSUtil获取所有DCN编号异常", e1);
//		}
		
		Map<String, Runnable> dcnMap = new HashMap<>();
		for (String dcnNo : dcnNos) {
			dotaskin.setDcn_num(dcnNo);
			TaskProcessIn newDotaskin = SysUtil.getInstance(TaskProcessIn.class);
			CommUtil_.copyProperties(newDotaskin, dotaskin);
			dcnMap.put(dcnNo, new MyRunnable(newDotaskin));
		}
		DataCollector.collect(dcnMap);
	}
	 
	 class MyRunnable implements Runnable {
		private TaskProcessIn dotaskin;
		private String dcnNo;
		public MyRunnable(TaskProcessIn dotaskin) {
			this.dotaskin = dotaskin;
			this.dcnNo = dotaskin.getDcn_num();
		}
		@Override
		public void run() {
			try {
				dotaskin.setDcn_num(dcnNo);
				DayEndBatch dayendbatch;
//				if (DMBUtil.getIntanse().isAdminDcn(dcnNo)) {
				if (DcnUtil.isAdminDcn(dcnNo)) {
					dayendbatch = SysUtil.getInstance(DayEndBatch.class);
				} else {
					dayendbatch = CommTools.getRemoteInstance(DayEndBatch.class);
				}
				dayendbatch.doTaskProcess(dotaskin);
			} catch (Exception e) {
				// TODO: 失败时需要通过IMS告警
				bizlog.info("DCN：" + dcnNo + ", 日终日期:" + dotaskin.getQuery_date() + " 发送执行核心换日前批量请求失败！", e);
				throw ApError.Aplt.E0000("DCN：" + dcnNo + ", 日终日期:" + dotaskin.getQuery_date() + " 发送执行核心换日前批量请求失败！", e);
			}
		}
	}

}


