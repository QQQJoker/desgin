package cn.sunline.clwj.zdbank.datacenter.util;

import java.util.List;

import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.registry.RegistryFactory;

import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.clwj.zdbank.jsf.access.factory.conf.JSFConfig;
import cn.sunline.clwj.zdbank.jsf.access.factory.conf.JSFRegistryConfig;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFConstant;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.edsp.midware.drs.common.utils.URI;

public class DataCenterUtil {
	
	public static <T> T getService(Class<?> cls,String aliasName) {
		return getService(cls.getName(),aliasName);
	}
	
	public static <T> T getService(String interfaceId,String aliasName) {
		return getService(interfaceId,aliasName,JSFConstant.DEFAULT_PROTOCOL);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getService(String interfaceId,String aliasName,String protocol) {
		
		ConsumerConfig<Object> consumerConfig = new ConsumerConfig<Object>();
		consumerConfig.setInterfaceId(interfaceId);
		consumerConfig.setAlias(aliasName);
		consumerConfig.setProtocol(protocol);
		
		List<RegistryConfig> registryConfigs = RegistryFactory.getRegistryConfigs();
		if (registryConfigs != null && registryConfigs.size() > 0) {
			consumerConfig.setRegistry(RegistryFactory.getRegistryConfigs());
		} else {
			RegistryConfig jsfRegistry = JSFRegistryConfig.getInstance().getJsfRegistry();
			if(jsfRegistry == null) {
				jsfRegistry = new RegistryConfig();
				JSFConfig config = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager().getConfig(JSFConfig.class);
				URI registryUrl = URI.valueOf(config.getRegistryURL());
				String index = registryUrl.getParameter("index");
				if(StringUtil.isNotEmpty(index)) {
					jsfRegistry.setIndex(index);
				} else {
					jsfRegistry.setAddress(registryUrl.getHost());
				}
				JSFRegistryConfig.getInstance().setJsfRegistry(jsfRegistry);
			}
			consumerConfig.setRegistry(jsfRegistry);
		}

		return (T) consumerConfig.refer(); // 获取 JsfDataReadService 代理对象
	}
}
