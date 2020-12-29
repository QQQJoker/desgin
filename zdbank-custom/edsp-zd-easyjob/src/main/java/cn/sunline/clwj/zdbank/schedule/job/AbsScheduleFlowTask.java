package cn.sunline.clwj.zdbank.schedule.job;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.wangyin.schedule.client.job.ScheduleContext;
import com.wangyin.schedule.client.job.ScheduleFlowTask;
import com.wangyin.schedule.client.job.TaskResult;
import com.wangyin.schedule.client.job.TaskResult.Builder;
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
import cn.sunline.edsp.base.util.lang.StringUtil;


public abstract class AbsScheduleFlowTask implements ScheduleFlowTask {

	private static final SysLog logger = SysLogUtil.getSysLog(AbsScheduleFlowTask.class);

	/**
	 * 给我交易用的dataArea、input里需要组ID:：groupId、交易ID：tranId;comm_req里需要 交易日期：jiaoyirq
	 * 
	 * @return
	 */
	public DataArea createDataArea(ScheduleContext context) {
		// 流程-任务节点的参数信息
		Map<String, String> flowParameterMap = context.getFlowParameterMap();
		logger.info("the flow parameter info is [%s]", flowParameterMap);
		
		// TODO:啥参数？
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
	public TaskResult doTask(ScheduleContext context) throws Exception {
		EdspCoreBeanUtil.getEngineResourceManager().clearThreadCache(true);
		Builder resultBuilder = TaskResult.newBuilder();
		String taskId = null;
		try {
			EdspCoreBeanUtil.getDBConnectionManager().checkAndReconnect();
			EdspCoreBeanUtil.getDBConnectionManager().beginTransation();
			
			DataArea dataArea = createDataArea(context);
			
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
			resultBuilder.withFailed().withCompletedMessage(t.getMessage());
			return resultBuilder.build();
		} finally {
			try {
				EdspCoreBeanUtil.getDBConnectionManager().close();
			} catch (Exception e) {
				logger.error("release pool conn is failed!", e);
			}
		}

		return waitTaskResult(taskId);
	}

	private TaskResult waitTaskResult(String taskId) {
		Builder resultBuilder = TaskResult.newBuilder().withFailed();
		boolean finish = false;
		BatchTaskInfoManager batchTaskInfoManager = BatchControllerFactory.getBatchTaskInfoManager();
		while (!finish) {
			BatchTaskInfo batchTaskInfo = batchTaskInfoManager.getBatchTaskInfo(taskId);
			if (batchTaskInfo == null) {
				return resultBuilder.withFailed().withCompletedMessage("找不到任务批次号[" + taskId + "]的任务.").build();
			}
			logger.info("批量任务信息===========" + batchTaskInfo.getStatus());

			String errorStack = batchTaskInfo.getErrorStack();
			switch (batchTaskInfo.getStatus()) {
			case interrupted:
				finish = true;
				return resultBuilder.withFailed().withCompletedMessage(String.format(ERROR_MSG_TPL, taskId, "interrupted", errorStack)).build();
			case success:
				finish = true;
				return resultBuilder.withSuccess().withCompletedMessage(String.format(ERROR_MSG_TPL, taskId, "success", "")).build();
			case failure:
				finish = true;
				logger.error("批量任务执行失败信息===========" + batchTaskInfo.getErrorStack());
				return resultBuilder.withFailed().withCompletedMessage(String.format(ERROR_MSG_TPL, taskId, "failure", errorStack)).build();
			default:
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					resultBuilder.withFailed().withCompletedMessage("休眠中断异常");
				}
			}
		}
		return resultBuilder.build();
	}
	
	private static final String ERROR_MSG_TPL = "任务批次号[%s]的任务执行状态:[%s],错误堆栈:[%s]";
}
