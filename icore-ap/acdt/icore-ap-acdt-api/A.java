package cn.sunline.ltts.gns.api;

import java.util.Date;

public interface AcdtApi {
	
	/**
	 * 按当前机器时间获取日期
	 * <p>若当前线程存在日期对象，则直接返回；
	 * <p>若当前线程不存在日期对象，通过生效
	 * 时间比较获得日期对象，放入当前线程缓存，并返回
	 * @return
	 */
	public String getCurrentDate();
	/**
	 * 按当前机器时间获取日期对象
	 * @return
	 */
	public AcdtInf getAcdt();
	/**
	 * 清理当前线程的日期(交易后处理必须清理)
	 */
	public void clean();
	// TODO ...
}