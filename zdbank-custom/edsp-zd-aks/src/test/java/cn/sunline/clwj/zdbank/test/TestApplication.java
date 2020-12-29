package cn.sunline.clwj.zdbank.test;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import cn.sunline.clwj.zdbank.config.AksConfig;

//@SpringBootApplication
public class TestApplication {
	
	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "test");
		ConfigurableApplicationContext applicationContext = SpringApplication.run(TestApplication.class);
		AksConfig bean = applicationContext.getBean(AksConfig.class);
		System.out.println(bean.toString());
	}
	
}
