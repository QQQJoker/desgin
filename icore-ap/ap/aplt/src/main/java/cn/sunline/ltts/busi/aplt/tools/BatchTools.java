package cn.sunline.ltts.busi.aplt.tools;

import java.util.Map;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
//import cn.sunline.adp.cedar.server.batch.domain.BatchTaskInfo;
//import cn.sunline.adp.cedar.server.batch.domain.BatchTaskInfoManager;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.domain.BatchTaskInfo;
import cn.sunline.adp.cedar.server.batch.domain.BatchTaskInfoManager;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.adp.cedar.server.batch.type.BatchEnum.E_BATCHTASKSTATUS;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.adp.cedar.server.batch.util.TaskSubmitReturn;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpSvcx;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpSvcxDao;
import cn.sunline.ltts.busi.iobus.servicetype.ap.IoApEvent;
import cn.sunline.ltts.busi.iobus.servicetype.ap.IoApService;
import cn.sunline.ltts.busi.iobus.servicetype.ap.IoBatchEvent;
import cn.sunline.ltts.busi.iobus.servicetype.ap.IoGLEvent;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SHIFOUBZ;

/**
 * 批量工具类（包括数据汇报机制、文件抽取机制）
 * 
 * 
 */
public class BatchTools {
	public static BizLog bizlog = LogManager.getBizLog(BatchTools.class);

	/**
	 * 获取批量交易任务编号
	 * 
	 * @param sModules
	 *            模块名
	 * @return taskId
	 */
//	public static String getTaskId(String sModule, String sJiaoyirq) {
//		bizlog.info("begin >>>>>>>>>>");
//		bizlog.info("Argument sModule[" + sModule + "], sJiaoyirq[" + sJiaoyirq
//				+ "]");
//
//		IoApCodeRuleStru cplIoApCodeRule = SysUtil
//				.getInstance(IoApCodeRuleStru.class);
//
//		String orignalCodeRuleID = "PLJYRWBH";
//		String codeRuleID = ApFileUtil.getCodeRuleID(orignalCodeRuleID);
//		bizlog.info("Orignal Code Rule ID[" + orignalCodeRuleID
//				+ "], Code Rule ID[" + codeRuleID + "]");
//
//		cplIoApCodeRule.setZhscdaim(codeRuleID);
//		cplIoApCodeRule.setCanszh01(sModule);
//		if (CommUtil.isNull(sJiaoyirq)) {
//			sJiaoyirq = CommToolsAplt.prcRunEnvs().getTran_date();
//		}
//		cplIoApCodeRule.setCanszh02(sJiaoyirq);
//		cplIoApCodeRule.setCanszh03(CommTools.getSystemId());
//		String result = ApGenParaStru.genParaStru(cplIoApCodeRule);
//		bizlog.info("return taksID[" + result + "]");
//
//		bizlog.info("end <<<<<<<<<<");
//		return result;
//	}
	/**
	 * 修改批量运行状态
	 * @param batchInfoMng
	 * @param batchTaskExcuteInfo
	 * @param inNewTranaction
	 * @return
	 */
	public static int modifyBatchTaskInfo(BatchTaskInfoManager batchInfoMng,BatchTaskInfo batchTaskExcuteInfo,boolean inNewTranaction){
//		return batchInfoMng.modifyTaskInfo(batchTaskExcuteInfo);
		return batchInfoMng.modifyBatchTaskInfo(batchTaskExcuteInfo);
	}

	/**
	 * 根据事件业务服务实现关键字完成异步事件的处理。
	 * 
	 * @param fwsxgjzi
	 *            业务服务实现关键字
	 * @param params
	 *            事件参数
	 */
	public static void fireEvent(String fwsxgjzi, String shijneir) {
	    KnpSvcx fwsxdy = KnpSvcxDao.selectOne_odb1(
				IoApEvent.class.getSimpleName(), fwsxgjzi, true);
		IoApEvent processor = SysUtil.getInstance(IoApEvent.class,
				fwsxdy.getSvimid());
		processor.onEvent(shijneir);
	}

	/**
	 * DepositBatch数据汇报异步事件 (DepositGL子系统的数据汇报调用）
	 * 
	 * @param fwsxgjzi
	 *            业务服务实现关键字
	 * @param params
	 *            事件参数
	 */
	public static void fireBatchEvent(String fwsxgjzi, String shijneir) {
		IoBatchEvent batchEvent = SysUtil.getRemoteInstance(IoBatchEvent.class);
		switch (CommTools.getMySysId()) {
		case "3051":
			// ADM-DCN DepositGL -> DepositBatch
			batchEvent.onBatchEvent(fwsxgjzi, shijneir);
			break;

		default:
			throw ApError.Aplt.E0000("DepositBatch数据汇报机制只允许在DepositGL子系统上使用！");
		}
	}

	/**
	 * 
	 * @Author Administrator
	 *         <p>
	 *         <li>2015年2月28日-上午11:55:54</li>
	 *         <li>功能说明：判断系统是否小总账</li>
	 *         </p>
	 * @return
	 */
	public static boolean isGlSysId() {

		// 当前DCN系统ID
		String sSysId = CommTools.getMySysId();

		if ("3051".equals(sSysId))
			return true;
		else
			return false;
	}

	/**
	 * 
	 * @Author Administrator
	 *         <p>
	 *         <li>2015年1月28日-下午1:00:53</li>
	 *         <li>功能说明：判断是否小总账业务引发的服务</li>
	 *         </p>
	 * @return
	 */
	public static boolean isGLSerives() {

		//TODO

		return false;
	}

	/**
	 * GL数据汇报异步事件
	 * (DepositAdmBatch、DepositBatch两个子系统的数据汇报需要调用不同的业务服务，因为注册的RMB场景不一样）
	 * 
	 * @param fwsxgjzi
	 *            业务服务实现关键字
	 * @param params
	 *            事件参数
	 */
	public static void fireGLEvent(String fwsxgjzi, String shijneir) {
		/**
		 * core("3009"), batch("3048"), admCore("3049"), admBatch("3050"),
		 * GL("3051"), counter("3052"), admWeb("3053");
		 */
		IoGLEvent glEvent = SysUtil.getRemoteInstance(IoGLEvent.class);
		switch (CommTools.getMySysId()) {
		case "3048":
			// R/C-DCN DepositBatch -> DepositGL
			glEvent.onGLEvent(fwsxgjzi, shijneir);
			break;
		case "3050":

			// ADM-DCN DepositAdmBatch -> DepositGL
			glEvent.onGLEventForAdm(fwsxgjzi, shijneir);
			break;
		default:
			throw ApError.Aplt
					.E0000("GL数据汇报机制只允许在DepositBatch和DepositAdmBatch子系统上使用！");
		}
	}

	/**
	 * 批量数据汇报异步事件
	 * 
	 * @param fwsxgjzi
	 *            业务服务实现关键字
	 * @param params
	 *            事件参数
	 */
	public static void fireAdmBatchEvent(String fwsxgjzi, String shijneir) {
		IoBatchEvent batchEvent;
		// TODO 包商银行一期非分布式时，只有一个sysid，直接调用本地方法
		batchEvent = SysUtil.getInstance(IoBatchEvent.class);
		batchEvent.onAdmBatchEvent(fwsxgjzi, shijneir);

		// switch (CommTools.getMySysId()) {
		// case "3048":
		// // R/C-DCN DepositBatch -> DepositAdmBatch
		// batchEvent = SysUtil.getRemoteInstance(IoBatchEvent.class);
		// batchEvent.onAdmBatchEvent(fwsxgjzi, shijneir);
		// break;
		// case "3050":
		// // ADM-DCN 直接调用本地方法
		// batchEvent = SysUtil.getInstance(IoBatchEvent.class);
		// batchEvent.onAdmBatchEvent(fwsxgjzi, shijneir);
		// break;
		// case "3051":
		// // ADM-DCN DepositGL -> DepositAdmBatch
		// batchEvent = SysUtil.getRemoteInstance(IoBatchEvent.class);
		// batchEvent.onAdmBatchEventForGL(fwsxgjzi, shijneir);
		// break;
		// default:
		// throw
		// ApError.Aplt.E0000("DCN数据汇报机制只允许在DepositBatch、DepositGL和DepositAdmBatch子系统上使用！");
		// }
	}

	/**
	 * 根据业务服务实现关键字完成同步服务的处理。
	 * 
	 * @param fwsxgjzi
	 *            业务服务实现关键字
	 * @param params
	 *            事件参数
	 */
    public static String fireService(String fwsxgjzi, String shijneir) {
        KnpSvcx fwsxdy = KnpSvcxDao.selectOne_odb1(IoApService.class.getSimpleName(), fwsxgjzi, true);
		IoApService processor = SysUtil.getInstance(IoApService.class,
				fwsxdy.getSvimid());
		return processor.onService(shijneir);
	}

	/**
	 * DepositBatch数据汇报同步服务 (DepositGL子系统的数据汇报调用）
	 * 
	 * @param fwsxgjzi
	 *            业务服务实现关键字
	 * @param params
	 *            事件参数
	 */
	public static String fireBatchService(String dcn_num, String fwsxgjzi,
			String shijneir) {
		IoBatchEvent batchEvent = SysUtil.getRemoteInstance(IoBatchEvent.class);
		String fanhjieg = null;
		switch (CommTools.getMySysId()) {
		case "3051":
			// ADM-DCN DepositGL -> DepositBatch
			fanhjieg = batchEvent.onBatchService(dcn_num, fwsxgjzi, shijneir);
			break;
		default:
			throw ApError.Aplt.E0000("DepositBatch数据汇报机制只允许在DepositGL子系统上使用！");
		}
		return fanhjieg;
	}

	/**
	 * GL数据汇报同步服务
	 * (DepositAdmBatch、DepositBatch两个子系统的数据汇报需要调用不同的业务服务，因为注册的RMB场景不一样）
	 * 
	 * @param fwsxgjzi
	 *            业务服务实现关键字
	 * @param params
	 *            事件参数
	 * @return 返回结果
	 */
	public static String fireGLService(String fwsxgjzi, String shijneir) {
		IoGLEvent glEvent = SysUtil.getRemoteInstance(IoGLEvent.class);
		String fanhjieg = null;
		switch (CommTools.getMySysId()) {
		case "3048":
			// R/C-DCN DepositBatch -> DepositGL
			fanhjieg = glEvent.onGLService(fwsxgjzi, shijneir);
			break;
		case "3050":

			// ADM-DCN DepositAdmBatch -> DepositGL
			fanhjieg = glEvent.onGLService(fwsxgjzi, shijneir);
			break;
		default:
			throw ApError.Aplt
					.E0000("GL数据汇报同步服务只允许在DepositBatch和DepositAdmBatch子系统上使用！");
		}
		return fanhjieg;
	}

	/**
	 * 批量数据汇报同步服务
	 * 
	 * @param fwsxgjzi
	 *            业务服务实现关键字
	 * @param params
	 *            事件参数
	 * @return 返回结果
	 */
	public static String fireAdmBatchService(String fwsxgjzi, String shijneir) {
		IoBatchEvent batchEvent;
		String fanhjieg = null;
		switch (CommTools.getMySysId()) {
		case "3048":
			// R/C-DCN DepositBatch -> DepositAdmBatch
			batchEvent = SysUtil.getRemoteInstance(IoBatchEvent.class);
			fanhjieg = batchEvent.onAdmBatchService(fwsxgjzi, shijneir);
			break;
		case "3050":
			// ADM-DCN 直接调用本地方法
			batchEvent = SysUtil.getInstance(IoBatchEvent.class);
			fanhjieg = batchEvent.onAdmBatchService(fwsxgjzi, shijneir);
			break;
		case "3051":
			// ADM-DCN DepositGL -> DepositAdmBatch
			batchEvent = SysUtil.getRemoteInstance(IoBatchEvent.class);
			fanhjieg = batchEvent.onAdmBatchService(fwsxgjzi, shijneir);
			break;
		default:
			throw ApError.Aplt
					.E0000("DCN数据汇报机制只允许在DepositBatch、DepositGL和DepositAdmBatch子系统上使用！");
		}
		return fanhjieg;
	}

	/**
	 * 判断是否允许执行定时日切就绪检查
	 * 
	 */
	public static Boolean canRun(String plkzhibz) {
		KSysBatchTable.tsp_batch_execution_control ksys_plzxkz = KSysBatchTable.Tsp_batch_execution_controlDao
				.selectOne_odb_1(plkzhibz, false);
		if (ksys_plzxkz != null
				&& E_SHIFOUBZ.NO.getValue().equals(ksys_plzxkz.getControl_value()))
			return false;

		return true;
	}

	/**
	 * 更新控制表批量交易不需要进行二次处理
	 */
	public static void unNeedTwoProcess(final String plkzhibz) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				KSysBatchTable.tsp_batch_execution_control ksys_plzxkz = KSysBatchTable.Tsp_batch_execution_controlDao
						.selectOneWithLock_odb_1(plkzhibz, false);
				if (ksys_plzxkz == null) {
					ksys_plzxkz = SysUtil
							.getInstance(KSysBatchTable.tsp_batch_execution_control.class);
					ksys_plzxkz.setControl_code(plkzhibz);
					ksys_plzxkz.setControl_value(E_SHIFOUBZ.NO.getValue());
					ksys_plzxkz.setDesc_message("控制批量交易是否需要二次处理");
					KSysBatchTable.Tsp_batch_execution_controlDao.insert(ksys_plzxkz);
				} else {
					ksys_plzxkz.setControl_value(E_SHIFOUBZ.NO.getValue());
					KSysBatchTable.Tsp_batch_execution_controlDao.updateOne_odb_1(ksys_plzxkz);
				}
				return null;
			}
		});
	}

	/**
	 * DCN文件抽取（支持ADM-DCN）
	 * 
	 * <p>
	 * 根据目标DCN与当前DCN判断是远程调用还是直接调用本地方法。
	 * 
	 * @param input
	 *            文件抽取输入对象
	 * @return 文件抽取结果对象
	 */
//	public static IoApFileBatchType.ExtractFileOut extractDcnFile(
//			IoApFileBatchType.ExtractFileIn input) {
//		if (!"3050".equals(CommTools.getMySysId())) {
//			throw ApError.Aplt.E0000("方法仅支持在DepositAdmBatch上使用！");
//		}
//
//		// 参数合法性验证
//		if (StringUtil.isEmpty(input.getCdcnno()))
//			throw ApError.Aplt.E0000("DCN编号不能为空！");
//
//		// 判断是否调用本DCN(ADM-DCN)的文件抽取服务
//		IoApDcnFileExtractor fileExtractor;
//		if (input.getMdcnno().equals(input.getCdcnno())) {
//			// 直接调用本地方法
//			fileExtractor = SysUtil.getInstance(IoApDcnFileExtractor.class);
//		} else {
//			// 远程调用
//			fileExtractor = SysUtil
//					.getRemoteInstance(IoApDcnFileExtractor.class);
//		}
//		return fileExtractor.extractFile(input);
//	}

	/**
	 * 调用流程类交易，仅批量交易中能够调用
	 * 
	 * @param prcscd
	 *            外部交易码
	 * @param input
	 *            输入数据区
	 * @throws Exception
	 *             其他异常
	 * @return 输出数据
	 */
	public static Map<String, Object> callFlowTran(String prcscd,
			Map<String, Object> input) throws AdpBusinessException, Exception {

		return SysUtil.callFlowTran(prcscd, input);
	}

	/**
	 * 提交批量任务。
	 * 
	 * @param taskId
	 *            批量交易任务编号 注：该方法仅在任务失败、中断、二次提交时使用，用于修改批量任务的状态
	 */
	public static void submitAndRunBatchTask(String taskId) {
		BatchUtil.submitAndRunBatchTask(taskId);
	}

	/**
	 * 提交批量任务。
	 * 
	 * @param taskId
	 *            批量交易任务编号
	 * @param groupId
	 *            批量交易组
	 * @param dataArea
	 *            任务数据区
	 */
	public static FileTaskSubmitReturn submitTask(String taskId,
			String groupId, DataArea dataArea) {
		return new FileTaskSubmitReturn(BatchUtil.submitAndRunBatchTranGroup(
				taskId, groupId, dataArea));
	}

	/**
	 * 
	 * @param scheduleName
	 *            调度名称，必须唯一。
	 * @param groupId
	 *            批量交易组id
	 * @param dataArea
	 *            数据区 （可以是Map，也可以是String）
	 * @param startDate
	 *            开始日期
	 * @param endDate
	 *            结束日期
	 * @param hour
	 *            小时
	 * @param minute
	 *            分钟
	 * @param second
	 *            秒
	 * @param autoRemove
	 *            执行结束后是否自动删除。
	 * @param description
	 *            描述信息
	 */
	public static void scheduleTask(String scheduleName, String groupId,
			DataArea dataArea, String startDate, String endDate, String hour,
			String minute, String second, boolean autoRemove, String description) {
		BatchUtil.scheduleTask(scheduleName, groupId, dataArea, startDate,
				endDate, hour, minute, second, autoRemove, description);
	}

	/**
	 * 
	 * @param scheduleName
	 *            调度名称，必须唯一。
	 * @param flow
	 *            批量流程id
	 * @param groupId
	 *            批量交易组id
	 * @param dataArea
	 *            数据区 （可以是Map，也可以是String）
	 * @param startDate
	 *            开始日期
	 * @param endDate
	 *            结束日期
	 * @param hour
	 *            小时
	 * @param minute
	 *            分钟
	 * @param second
	 *            秒
	 * @param autoRemove
	 *            执行结束后是否自动删除。
	 * @param description
	 *            描述信息
	 */
	public static void scheduleTask(String scheduleName, String flowId,
			String groupId, DataArea dataArea, String startDate,
			String endDate, String hour, String minute, String second,
			boolean autoRemove, String description) {
		BatchUtil.scheduleTask(scheduleName, flowId, groupId, dataArea, hour,
				minute, second, autoRemove, description);

	}

	/**
	 * 
	 * @param scheduleName
	 *            调度名称，必须唯一。
	 * @param batchTranGroupId
	 *            批量交易组id
	 * @param weituoho
	 *            委托号(也是批量任务表的批次号)
	 * @param year
	 *            第几年
	 * @param month
	 *            第几月
	 * @param dayOfMonth
	 *            第几天
	 * @param description
	 *            描述信息
	 */
	public static void scheduleTask(String scheduleName,
			String batchTranGroupId, String weituoho, String year,
			String month, String dayOfMonth, String description) {
		DataArea dataArea = DataArea.buildWithEmpty();
		dataArea.getSystem().put("pljypich", weituoho);
		BatchUtil.scheduleTask(scheduleName, batchTranGroupId, dataArea, "",
				"", year, month, dayOfMonth, "12", "00", "00", true,
				description);
	}

	/**
	 * 是否存在除指定批次外的其他任务正在运行
	 * 
	 * @param taskId
	 * @return
	 */
	public static boolean hasOtherTaskProcessing(String taskId) {
		return BatchUtil.hasOtherTaskProcessing();
	}

	/**
	 * 获取当前批量任务的任务id
	 * 
	 * @return
	 */
	public static String getCurrentTaskId() {
		return BatchUtil.getCurrentTaskId();
	}

	/**
	 * 移除批量调度器
	 * 
	 * @param ScheduleTaskInfo
	 *            待修改为的批量调度对象。
	 */
	public static void removeScheduleTask(String name) {
		BatchUtil.removeScheduleTask(name);
	}

	/**
	 * 文件批量任务提交返回结果对象
	 * 
	 * @author caiqq
	 * 
	 */
	public static class FileTaskSubmitReturn {
		private final TaskSubmitReturn delegator;

		public FileTaskSubmitReturn(TaskSubmitReturn delegator) {
			this.delegator = delegator;
		}

		public String getBatchId() {
			return delegator.getBatchId();
		}

		public void waitForFinish() {
			delegator.waitForFinish();
		}

		public E_BATCHTASKSTATUS getStatus() {
			return delegator.getStatus();
		}

	}

	/**
	 * 获取本地主机名+当前线程ID
	 * 
	 * @author cuijia
	 * @return 20150728
	 */
	public static String getHostNameThread() {

			String hostName = SysUtil.getSvcId();
//					InetAddress.getLocalHost().getHostName();
			Long threadId = Thread.currentThread().getId();
			return hostName + threadId;


	}

}
