package cn.sunline.clwj.zdbank.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import com.wangyin.schedule.client.ScheduleClient;
import com.wangyin.schedule.client.service.JobService;

//@SpringBootApplication(exclude= {FreeMarkerAutoConfiguration.class})
public class TestApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "test");
		ConfigurableApplicationContext applicationContext = SpringApplication.run(TestApplication.class);
		ScheduleClient bean = (ScheduleClient) applicationContext.getBean(JobService.class);
		System.out.println(bean.toString() + ",间隔:" + bean.getIntervalMill());
	}
}
