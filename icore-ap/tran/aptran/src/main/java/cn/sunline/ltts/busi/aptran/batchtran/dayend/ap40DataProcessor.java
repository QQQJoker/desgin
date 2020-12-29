package cn.sunline.ltts.busi.aptran.batchtran.dayend;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.ConvertUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.custom.comm.biz.util.DcnDataUtil;
import cn.sunline.adp.cedar.custom.comm.servicetype.DayEndBatch;
import cn.sunline.adp.cedar.custom.comm.type.DayEndBatchType.DoChangeDayIn;
import cn.sunline.adp.cedar.server.batch.collector.DataCollector;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.adp.metadata.base.util.CommUtil_;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.busi.aplt.tools.BatchTools;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_PLKZHIBZ;

/**
 * 所有DCN换日就绪检查
 * 
 */

public class ap40DataProcessor
		extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Ap40.Input, cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Ap40.Property> {
	public static final BizLog bizlog = LogManager.getBizLog(ap40DataProcessor.class);
	private final String alertType = "check";

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(
			cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Ap40.Input input,
			cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Ap40.Property property) {
		
		String mySysId = CommTools.getMySysId();
	    if (!mySysId.equals("3050")) {
	        bizlog.info("只需在DepositAdmBatch(3050)子系统上执行，当前子系统[%s]无需执行，跳过！", mySysId);
	        return;
	    }
		
		String trigtime = input.getTrigtime();
		if (!DateTimeUtil.isDate(trigtime, ConvertUtil.DEFAULT_TIME_PATTERN)) {
			String nowTime = DateTimeUtil.getNow("HH:mm:ss");
			// 当前时间与23:40:00比较，小于时把日切定时触发时间设置为00:00:00，否则则延迟10分钟后执行
			if (CommUtil.compare(nowTime, "23:40:00") < 0 && CommUtil.compare(nowTime, "23:00:00") > 0)
				trigtime = "00:00:00";
			else {
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				Calendar now = Calendar.getInstance();
				now.add(Calendar.MINUTE, 10);
				trigtime = sdf.format(now.getTime());
			}

			// throw ApError.Aplt.E0000("定时日切时间点" + trigtime + "与格式hh:mm:ss");
		}

		String flowType = input.getTran_flow_id();
		String farendma = input.getCorpno();
		String chaxriqi = DcnDataUtil.queryAllDcnDayEnd(farendma, flowType);

		
		/***
		 * 定义IMS指标BatchResultMetric的元素
		 */
		 String flowId = "hx_before";                  //批量流程
		 String returnMessage = null;                  //返回消息
		 
		// 一直循环等待所有DCN换日前批处理已经执行成功
		int item = 0;
		while (true) {
			// TODO: 方案一：并行从所有DCN同步查询换日前批处理状态
			// TODO: 方案二：依赖各个DCN换日前批处理汇报的信息来判断是否可以执行换日操作
			if(!BatchTools.canRun(E_PLKZHIBZ.DS.getValue())){
				bizlog.info("停止循环等待所有DCN换日前批处理已经执行成功，已循环次数[" + item + "]！");
				throw ApError.Aplt.E0000(StringUtil.nullable(returnMessage, "停止循环等待所有DCN换日前批处理已经执行成功！"));
			}
			
			if (item != 0) {
				// 每次检查等待5分钟
				try {
					TimeUnit.MINUTES.sleep(5);
				} catch (InterruptedException e) {
					throw ApError.Aplt.E0000("sleep失败", e);
				}
			}
			item++;

			// 获取日终日期
//			KSysBatchTable.tsp_dayend_exe_management dsrzzx = KSysBatchTable.Tsp_dayend_exe_managementDao.selectOne_odb_1(flowType, chaxriqi, farendma, false);
			KSysBatchTable.tsp_dayend_exe_management dsrzzx = KSysBatchTable.Tsp_dayend_exe_managementDao.selectOne_odb_1(flowType, chaxriqi, false);
			if (dsrzzx == null) {
				//所有DCN批前都是没有完成或者是没有开始的,2015,01,16  huangxq
				returnMessage = "日终日期：[" + chaxriqi + "] 核心换日及换日后就绪检查未通过: 核心换日前流程[" + flowId + "]还没有开始执行！";
//				RealReportUtil.alertIMSMsg(alertType, flowId, "error", returnMessage);
				continue;
			}

			if (!dsrzzx.getTotal_dcn_num().equals(dsrzzx.getSuccess_dcn_num())) {
				// 告警信息要包括哪个DCN有问题？不需要：日终批前失败直接通过IMS告警
				returnMessage = "日终日期：[" + chaxriqi + "] 核心换日及换日后就绪检查未通过: 核心换日前流程[" + flowId + "]还没有全部执行成功！总共DCN数：["
						+ dsrzzx.getTotal_dcn_num() + "];成功DCN数：["
						+ dsrzzx.getSuccess_dcn_num() + "]！";
//				RealReportUtil.alertIMSMsg(alertType, flowId, "error", returnMessage);
				continue;
			}

			// 初始化输入参数对象
			List<String> dcnNos;
//			try {
//				dcnNos = DMBUtil.getIntanse().findAllDcnNosWithAdmin();
				dcnNos = DcnUtil.findAllDcnNosWithAdmin();
//			} catch (GNSAccessException e1) {
//				throw ApError.Aplt.E0000("通过GNSUtil获取所有DCN编号异常", e1);
//			}
			
//			RealReportUtil.alertIMSMsg(alertType, flowId, "success", "日终日期：[" + chaxriqi + "] 核心换日及换日后就绪检查通过， 所有DCN的核心换日前流程全部执行成功, 开始发送定时日切请求");
			
			//20150203定时核心日终和手动核心日终执行完成都要通知总账,所以此处不作控制
//			BatchTools.addOrUpdBatchCondition(E_PLKZHIBZ.DZ.getValue(), E_SHIFOUBZ.YES.getValue());
			
			// 公共信息
			final DoChangeDayIn dochgdin = SysUtil.getInstance(DoChangeDayIn.class);
			dochgdin.setQuery_date(chaxriqi);
			dochgdin.setCorporate_code(input.getCorpno());
			dochgdin.setTran_flow_id(input.getTran_flow_id());
			dochgdin.setChange_time(trigtime);

//			// 更新核心日终日期表
//			// 20150302 更新核心换日前流程的日终日期移到成功汇报时更新
//			DcnDataUtil.updatePlrzrqgl(dochgdin.getTran_flow_id(), chaxriqi,dochgdin.getCorporate_code());

			// 修改批量交易流程ID，更改为执行换日及换日后流程
			dochgdin.setTran_flow_id("hx_dayend");

			// TODO: 需要判断是否存在失败的DCN的？
			Map<String, Runnable> dcnMap = new HashMap<>();
			for (String dcnNo : dcnNos) {
				dochgdin.setDcn_num(dcnNo);
				DoChangeDayIn newDochgdin = SysUtil.getInstance(DoChangeDayIn.class);
				CommUtil_.copyProperties(newDochgdin, dochgdin);
				dcnMap.put(dcnNo, new MyRunnable(newDochgdin));
			}
			DataCollector.collect(dcnMap);

			//发送定时日切请求成功要跳出循环
			break;
		}
	}
	
	class MyRunnable implements Runnable {
		private DoChangeDayIn dochgdin;
		private String dcnNo;
		public MyRunnable(DoChangeDayIn dochgdin) {
			this.dochgdin = dochgdin;
			this.dcnNo = dochgdin.getDcn_num();
		}
		@Override
		public void run() {
			try {
				dochgdin.setDcn_num(dcnNo);
				DayEndBatch dayendbatch;
//				if (DMBUtil.getIntanse().isAdminDcn(dcnNo)) {
				if (DcnUtil.isAdminDcn(dcnNo)) {
					dayendbatch = SysUtil.getInstance(DayEndBatch.class);
				} else {
					dayendbatch = CommTools.getRemoteInstance(DayEndBatch.class);
				}
				dayendbatch.doChangeDay(dochgdin);
			} catch (Exception e) {
				// TODO: 失败时需要通过IMS告警
				bizlog.info("DCN：" + dcnNo + ", 日终日期:" + dochgdin.getQuery_date() + " 发送定时日切请求失败！", e);
				throw ApError.Aplt.E0000("DCN：" + dcnNo + ", 日终日期:" + dochgdin.getQuery_date() + " 发送定时日切请求失败！", e);
			}
		}
	}

}
