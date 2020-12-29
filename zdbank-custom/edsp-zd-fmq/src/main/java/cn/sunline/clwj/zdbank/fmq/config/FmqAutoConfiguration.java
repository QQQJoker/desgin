package cn.sunline.clwj.zdbank.fmq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix="adp.fmq", name="enabled", matchIfMissing=false, havingValue="true")
public class FmqAutoConfiguration {

	@Bean
	@ConfigurationProperties(prefix="adp.fmq.config", ignoreUnknownFields = true)
	public FmqConfig fmqConfig() {
		return new FmqConfig();
	}
}
