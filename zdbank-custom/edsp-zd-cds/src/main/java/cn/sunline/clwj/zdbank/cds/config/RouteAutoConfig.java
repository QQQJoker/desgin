package cn.sunline.clwj.zdbank.cds.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteAutoConfig {

	@Bean
	@ConfigurationProperties(prefix="adp.route", ignoreUnknownFields = true)
	@ConditionalOnProperty(prefix="adp.route", name="enabled", matchIfMissing=false, havingValue="true")
	public RouteConfig initRouteDBClientConfig() {
		return new RouteConfig();
	}
	
}
