package cn.sunline.clwj.zdbank.schedule.autoconfig;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.wangyin.schedule.client.ScheduleClient;
import com.wangyin.schedule.client.dao.PushDataDao;
import com.wangyin.schedule.client.service.JobService;

import cn.sunline.clwj.zdbank.schedule.job.AbsAsyncSchedulerJob;
import cn.sunline.clwj.zdbank.schedule.job.AbsScheduleFlowTask;
import cn.sunline.clwj.zdbank.schedule.threadpool.AsyncJobProcessorManager;

@Component
public class ScheduleClientCommandLineRunner implements CommandLineRunner, Ordered{

	public static final int DEFAULT_ORDER = 100;

	private int order = DEFAULT_ORDER;

	@Autowired(required=false)
	private JobService jobService;

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public void run(String... args) throws Exception {

		if (jobService != null && ((ScheduleClient) jobService).getPushDataDao() != null) {
			PushDataDao pushDataDao  = ((ScheduleClient) jobService).getPushDataDao();
			// ScheduleClient启动才能向调度服务端推送
			AsyncJobProcessorManager.start();
			HashSet<String> scheduleSet = new HashSet<>();
			scheduleSet.add(AbsAsyncSchedulerJob.class.getName());
			scheduleSet.add(AbsScheduleFlowTask.class.getName());
			pushDataDao.pushClass(scheduleSet);
		}


	}
}
