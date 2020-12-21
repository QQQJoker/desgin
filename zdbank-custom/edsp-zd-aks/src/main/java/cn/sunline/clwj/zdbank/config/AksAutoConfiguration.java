package cn.sunline.clwj.zdbank.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix="adp.aks", name="enabled", matchIfMissing=false, havingValue="true")
public class AksAutoConfiguration {

	@Bean
	@ConfigurationProperties(prefix="adp.aks.config", ignoreUnknownFields = true)
	public AksConfig aksConfig() {
		return new AksConfig();
	}
}
