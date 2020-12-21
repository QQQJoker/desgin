package cn.sunline.clwj.zdbank.zd.r2m;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.wangyin.rediscluster.client.R2mClusterClient;

import cn.sunline.adp.core.util.SpringUtils;

//SpringBootApplication
public class TestApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "test");
		ConfigurableApplicationContext applicationContext = SpringApplication.run(TestApplication.class);
		R2mClusterClient bean = applicationContext.getBean(R2mClusterClient.class);
		System.out.println(bean.toString());
		
		// Use R2M Cache
		R2mClusterClient r2mClient = SpringUtils.getBean(R2mClusterClient.class);
		
		r2mClient.set("r2mkey", "rm2val");
		
		String ret = r2mClient.get("r2mkey");
	}
}
