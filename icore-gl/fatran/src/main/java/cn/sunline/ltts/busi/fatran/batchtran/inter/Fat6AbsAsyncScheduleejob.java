package cn.sunline.ltts.busi.fatran.batchtran.inter;

import org.springframework.stereotype.Component;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.clwj.zdbank.schedule.job.AbsScheduleFlowTask;

@Component
public class Fat6AbsAsyncScheduleejob extends AbsScheduleFlowTask{
	@Override
	protected void beforeRunBatch(String taskId, DataArea dataArea) {
		
	}
}