package cn.sunline.clwj.zdbank.schedule.job;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.wangyin.schedule.client.job.AsyncSchedulerJob;
import com.wangyin.schedule.client.job.ScheduleContext;
import com.wangyin.schedule.client.jobstatus.JobFinalStatus;
import com.wangyin.schedule.client.report.ReportCompleted;
import com.wangyin.schedule.json.JSONMap;
import com.wangyin.schedule.sdk.response.TaskGetResponse;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.server.batch.domain.BatchTaskInfo;
import cn.sunline.adp.cedar.server.batch.domain.BatchTaskInfoManager;
import cn.sunline.adp.cedar.server.batch.engine.controller.BatchControllerFactory;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.clwj.zdbank.schedule.threadpool.AsyncJobProcessorManager;
import cn.sunline.edsp.base.util.lang.StringUtil;


public abstract class AbsAsyncSchedulerJob implements AsyncSchedulerJob {

	private static final SysLog logger = SysLogUtil.getSysLog(AbsAsyncSchedulerJob.class);

	/**
	 * 交易用的dataArea、input里需要组ID:：groupId、交易ID：tranId;comm_req里需要 交易日期：jiaoyirq
	 * 
	 * @return
	 */
	private DataArea createDataArea(ScheduleContext context) {
		// 流程触发器-开始节点-流程参数信息
		Map<String, String> flowParameterMap = context.getFlowParameterMap();
		logger.info("the flow parameter info is [%s]", flowParameterMap);
		
		// 普通触发器-任务参数 / 流程触发器-任务节点-任务参数
		Map<String, String> parameters = context.getParameters();
		logger.info("the parameter info is [%s]", parameters);
		
		DataArea dataArea = DataArea.buildWithEmpty();
		
		String flowId = parameters.get(BatchConfigConstant.BATCH_TRAN_FLOW);
		if(StringUtil.isNotEmpty(flowId)) {
			// flowId不为空则执行flow
			dataArea.getInput().put(BatchConfigConstant.BATCH_TRAN_FLOW, flowId);
		}
		
		String groupId = parameters.get(BatchConfigConstant.BATCH_TRAN_GROUP);
		if(StringUtil.isNotEmpty(groupId)) {
			// flowId为空，groupId不为空，tranId为空则执行group
			dataArea.getInput().put(BatchConfigConstant.BATCH_TRAN_GROUP, groupId);
		}
		
		String tranId = parameters.get(BatchConfigConstant.BATCH_TRAN_ID);
		if(StringUtil.isNotEmpty(tranId)) {
			// flowId为空，groupId和tranId不为空则执行tran
			dataArea.getInput().put(BatchConfigConstant.BATCH_TRAN_ID, tranId);
		}
		
		String tranDate = parameters.get(BatchConfigConstant.BATCH_TRAN_DATE);
		if(StringUtil.isNotEmpty(tranDate)) {
			// 批量交易日期
			dataArea.getCommReq().put(BatchConfigConstant.BATCH_TRAN_DATE, tranDate);
		}
		
		dataArea.getCommReq().put(BatchConfigConstant.BATCH_TENANT_ID, CoreUtil.getDefaultTenantId());
		String tenantId = parameters.get(BatchConfigConstant.BATCH_TENANT_ID);
		if(StringUtil.isNotEmpty(tenantId)) {
			// 法人信息
			dataArea.getCommReq().put(BatchConfigConstant.BATCH_TENANT_ID, tenantId);
		}
		return dataArea;
	}
	
	/**
	 * @param taskId
	 * 		  批量任务批次号
	 * @param dataArea
	 *        可以添加需要传递给批量任务的数据,(默认只包含comm_req区交易日期信息)
	 */
	protected abstract void beforeRunBatch(String taskId, final DataArea dataArea);

	@Override
	public Future<ReportCompleted> doJob(final ScheduleContext context) throws Exception {
		final DataArea dataArea = createDataArea(context);

		return AsyncJobProcessorManager.submit(new Callable<ReportCompleted>() {

			@Override
			public ReportCompleted call() throws Exception {
				EdspCoreBeanUtil.getEngineResourceManager().clearThreadCache(true);
				String taskId = null;
				try {
					EdspCoreBeanUtil.getDBConnectionManager().checkAndReconnect();
					EdspCoreBeanUtil.getDBConnectionManager().beginTransation();

					logger.debug("异步任务：解析原始任务ID");
					TaskGetResponse taskGetResponse = context.getTaskGetResponse();
					JSONMap properties = taskGetResponse.getProperties();
					String originalId = properties.getString("originalId");
					logger.debug("原始执行任务ID : [%s]", originalId);
					boolean isNewJob = StringUtil.isBlank(originalId);
					logger.debug("此任务是否新执行任务？%s", isNewJob);
					
					taskId = String.valueOf(context.getTaskId());
					// 重新拉起，批次号为原始的任务ID
					if(!isNewJob) {
						taskId = originalId;
					}
					
					beforeRunBatch(taskId, dataArea);
					
					logger.info("批量任务[{}],运行数据区为[{}]", taskId, dataArea);
					
					final String flowId = dataArea.getInput().getString(BatchConfigConstant.BATCH_TRAN_FLOW);
					final String groupId = dataArea.getInput().getString(BatchConfigConstant.BATCH_TRAN_GROUP);
					final String tranId = dataArea.getInput().getString(BatchConfigConstant.BATCH_TRAN_ID);
					
					if (StringUtil.isEmpty(flowId) && StringUtil.isEmpty(tranId) && StringUtil.isEmpty(groupId)) {
						throw new RuntimeException("input参数[flowId],[groupId],[tranId]不能同时为空！");
					}

					// 按流程执行
					if(StringUtil.isNotEmpty(flowId)) {
						BatchUtil.submitAndRunBatchTranFlow(taskId, flowId, dataArea);
					} else {
						if(StringUtil.isEmpty(groupId)) {
							throw new RuntimeException("按组运行批量,input参数[groupId]不能为空！");
						}
						
						if(StringUtil.isEmpty(tranId)) {
							// 按组执行
							BatchUtil.submitAndRunBatchTranGroup(taskId, groupId, dataArea);
						} else {
							// 按交易运行
							BatchUtil.submitAndRunBatchTran(taskId, groupId, tranId, dataArea);
						}
					}
					EdspCoreBeanUtil.getDBConnectionManager().commit();
				} catch (Throwable t) {
					logger.error("commit batch task faied !", t);
					try {
						EdspCoreBeanUtil.getDBConnectionManager().rollback();
					} catch (Exception e) {
						logger.error("rollback is failed!", e);
					}
					
					return new QuickFailureReportCompleted(taskId,t.getMessage());
				} finally {
					try {
						EdspCoreBeanUtil.getDBConnectionManager().close();
					} catch (Exception e) {
						logger.error("release pool conn is failed!", e);
					}
				}
				return new DefaultReportCompleted(taskId);
			}
		});
	}
	
	@SuppressWarnings("serial")
	class QuickFailureReportCompleted implements ReportCompleted {
		String taskId;
		String errorMessage;
		
		public QuickFailureReportCompleted(String taskId, String errorMessage) {
			super();
			this.taskId = taskId;
			this.errorMessage = errorMessage;
		}

		@Override
		public String getCompletedMessage() {
			return String.format(ERROR_MSG_TPL, this.taskId,"failure",errorMessage);
		}

		@Override
		public JobFinalStatus getJobFinalStatus() {
			return JobFinalStatus.FAILED;
		}
	}

	@SuppressWarnings("serial")
	class DefaultReportCompleted implements ReportCompleted {

		private String pljypich;
		private JobFinalStatus retStatus;
		private String completedMessage;

		public DefaultReportCompleted(String pljypich) {
			super();
			this.pljypich = pljypich;
		}

		@Override
		public String getCompletedMessage() {
			return completedMessage;
		}

		@Override
		public JobFinalStatus getJobFinalStatus() {
			// TODO query by taskId return job status
			EdspCoreBeanUtil.getEngineResourceManager().clearThreadCache(true);
			try {

				EdspCoreBeanUtil.getDBConnectionManager().checkAndReconnect();
				EdspCoreBeanUtil.getDBConnectionManager().beginTransation();

				BatchTaskInfoManager batchTaskInfoManager = BatchControllerFactory.getBatchTaskInfoManager();
				while (retStatus == null) {
					BatchTaskInfo batchTaskInfo = batchTaskInfoManager.getBatchTaskInfo(pljypich);
					if (batchTaskInfo == null) {
						return JobFinalStatus.FAILED;
					}
					logger.info("批量任务信息===========" + batchTaskInfo.getStatus());
					completedMessage = batchTaskInfo.getErrorStack();

					switch (batchTaskInfo.getStatus()) {
					case interrupted:
						retStatus = JobFinalStatus.ABORT;
						break;
					case success:
						retStatus = JobFinalStatus.SUCCESS;
						break;
					case failure:
						logger.error("批量任务执行失败信息===========" + batchTaskInfo.getErrorStack());
						retStatus = JobFinalStatus.FAILED;
						break;
					default:
						try {
							TimeUnit.SECONDS.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						break;
					}
				}
				EdspCoreBeanUtil.getDBConnectionManager().commit();
			}finally {
				try {
					EdspCoreBeanUtil.getDBConnectionManager().close();
				}catch(Exception e) {
					logger.error("release pool conn is failed ! ",e);
				}
			}
			return retStatus;
		}
	}
	
	private static final String ERROR_MSG_TPL = "任务批次号[%s]的任务执行状态:[%s],错误堆栈:[%s]";
}
