package cn.sunline.clwj.zdbank.schedule.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wangyin.schedule.spring.SchedulerFactoryBean;

import cn.sunline.clwj.zdbank.schedule.model.ScheduleClientConfig;

@Configuration
@ConditionalOnProperty(prefix="adp.schedule.client", name="enabled", matchIfMissing=false, havingValue="true")
public class ScheduleClientAutoConfiguration{
	

	@Bean
	@ConfigurationProperties(prefix="adp.schedule.client", ignoreUnknownFields = true)
	public ScheduleClientConfig scheduleClientConfig() {
		return new ScheduleClientConfig();
	}
	
	@Bean
	@ConditionalOnBean(ScheduleClientConfig.class)
	public SchedulerFactoryBean schedulerFactoryBean(ScheduleClientConfig scheduleClientConfig) {
		
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		schedulerFactoryBean.setHost(scheduleClientConfig.getHost());
		schedulerFactoryBean.setTenantId(scheduleClientConfig.getTenantId());
		schedulerFactoryBean.setAppId(scheduleClientConfig.getAppId());
		schedulerFactoryBean.setGroup(scheduleClientConfig.getGroup());
		schedulerFactoryBean.setSecret(scheduleClientConfig.getSecret());
		schedulerFactoryBean.setAutoStart(scheduleClientConfig.isAutoStart());
		
		if (scheduleClientConfig.getMaxConcurrentNo() != null && scheduleClientConfig.getMaxConcurrentNo() > 0) {
			schedulerFactoryBean.setMaxConcurrentNo(scheduleClientConfig.getMaxConcurrentNo());
		}
		if (scheduleClientConfig.getIntervalMill() != null && scheduleClientConfig.getIntervalMill() > 0) {
			schedulerFactoryBean.setIntervalMill(scheduleClientConfig.getIntervalMill().intValue());
		}
		
		return schedulerFactoryBean;
	}
	
}
