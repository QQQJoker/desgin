package cn.sunline.clwj.zdbank.schedule.threadpool;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.wangyin.schedule.client.report.ReportCompleted;

public class AsyncJobProcessorManager {

	private static ExecutorService executor;

	public static void start() {
		executor = Executors.newCachedThreadPool();
	}
	
	public static void shutdown() {
		if(executor != null) {
			executor.shutdown();
		}
	}
	
	public static Future<ReportCompleted> submit(Callable<ReportCompleted> callable) {
		if(executor == null || executor.isShutdown()) {
			throw new RuntimeException("异步Job处理线程池未启动或已关闭");
		}
		
		return executor.submit(callable);
	}
}
