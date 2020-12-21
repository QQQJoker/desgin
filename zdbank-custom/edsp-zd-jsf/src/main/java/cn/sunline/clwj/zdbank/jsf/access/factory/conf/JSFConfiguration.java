package cn.sunline.clwj.zdbank.jsf.access.factory.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JSFConfiguration {
	@Bean
	@ConfigurationProperties(prefix = "adp.jsf", ignoreUnknownFields = true)
	public JSFConfig jsfConfig() {
		return new JSFConfig();
	}
}
