package cn.sunline.clwj.zdbank.zd.r2m.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wangyin.rediscluster.client.R2mClusterClient;
import com.wangyin.rediscluster.provider.CCProvider;

import cn.sunline.clwj.zdbank.zd.r2m.model.R2MCacheClient;


@Configuration
@ConditionalOnProperty(prefix="adp.r2m", name="enabled", matchIfMissing=false, havingValue="true")
public class R2MCacheAutoConfiguration {

	@Bean
	@ConfigurationProperties(prefix="adp.r2m.ccprovider", ignoreUnknownFields = true)
	public CCProvider r2mZkProvider() {
		return new CCProvider();
	}
	
	@Bean
	@ConfigurationProperties(prefix="adp.r2m.cache.client", ignoreUnknownFields = true)
	public R2MCacheClient r2mCacheClient() {
		return new R2MCacheClient();
	}
	
	
	@Bean
	@ConditionalOnBean({CCProvider.class, R2MCacheClient.class})
	public R2mClusterClient cacheClusterClient(CCProvider ccProvider, R2MCacheClient cacheClient) {
		R2mClusterClient r2mClusterClient = new R2mClusterClient();
		r2mClusterClient.setMaxRedirections(cacheClient.getMaxRedirections());
		r2mClusterClient.setRedisTimeOut(cacheClient.getRedisTimeOut());
		r2mClusterClient.setProvider(ccProvider);
		return r2mClusterClient;
	}
	
}
