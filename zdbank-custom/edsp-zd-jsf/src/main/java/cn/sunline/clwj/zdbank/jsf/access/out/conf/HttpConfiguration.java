package cn.sunline.clwj.zdbank.jsf.access.out.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConfiguration {
	
	@Bean
	@ConfigurationProperties(prefix="adp.http",ignoreUnknownFields=true)
	public HttpConfig getConfig() {
		return new HttpConfig();
	}

}
