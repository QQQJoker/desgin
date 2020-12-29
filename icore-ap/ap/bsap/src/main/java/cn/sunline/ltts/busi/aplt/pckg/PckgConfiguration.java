package cn.sunline.ltts.busi.aplt.pckg;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@ConditionalOnProperty(prefix="adp.pckg", name="enabled", matchIfMissing=false, havingValue="true")
@Order(20)
public class PckgConfiguration {

	@Bean
	@ConfigurationProperties(prefix="adp.pckg.service", ignoreUnknownFields = true)
	public PckgConfig getPckgConfig() {
		return new PckgConfig();
		
	}
}
