package cn.sunline.clwj.zdbank.zd.r2m.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wangyin.r2m.client.jedis.JedisPoolConfig;
import com.wangyin.rediscluster.client.R2mClusterClient;
import com.wangyin.rediscluster.provider.ZkProvider;

import cn.sunline.clwj.zdbank.zd.r2m.model.R2MCacheClient;
import cn.sunline.clwj.zdbank.zd.r2m.model.R2MZkProvider;


@Configuration
@ConditionalOnProperty(prefix="adp.r2m", name="enabled", matchIfMissing=false, havingValue="true")
public class R2MCacheAutoConfiguration {

	@Bean
	@ConfigurationProperties(prefix="adp.r2m.zk-provider", ignoreUnknownFields = true)
	public R2MZkProvider r2mZkProvider() {
		return new R2MZkProvider();
	}
	
	@Bean
	@ConfigurationProperties(prefix="adp.r2m.cache.client", ignoreUnknownFields = true)
	public R2MCacheClient r2mCacheClient() {
		return new R2MCacheClient();
	}
	
	@Bean
	@ConditionalOnBean(R2MZkProvider.class)
	public ZkProvider zkProvider(R2MZkProvider r2mZkProvider) {
		return new ZkProvider(r2mZkProvider.getAppName(), r2mZkProvider.getZkConnectionStr(), 
				r2mZkProvider.getZkTimeout(), r2mZkProvider.getZkSessionTimeout(), r2mZkProvider.getPassword());
	}
	
	@Bean
	public JedisPoolConfig jedisPoolConfig() {
		return new JedisPoolConfig();
	}
	
	@Bean
	@ConditionalOnBean({ZkProvider.class, JedisPoolConfig.class})
	public R2mClusterClient cacheClusterClient(ZkProvider zkProvider, JedisPoolConfig jedisPoolConfig) {
		R2mClusterClient r2mClusterClient = new R2mClusterClient();
		r2mClusterClient.setProvider(zkProvider);
		r2mClusterClient.setRedisPoolConfig(jedisPoolConfig);
		r2mClusterClient.afterPropertiesSet();
		return r2mClusterClient;
	}
	
}
