package cn.sunline.clwj.zdbank.plugin;

import java.util.List;

import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.registry.RegistryFactory;
import com.wangyin.key.server.DeviceCryptoService;
import com.wangyin.key.server.JSFCryptoDistanceService;

import cn.sunline.adp.cedar.base.boot.plugin.PluginSupport;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.clwj.zdbank.config.AksConfig;
import cn.sunline.clwj.zdbank.jsf.access.factory.conf.JSFConfig;
import cn.sunline.clwj.zdbank.jsf.access.factory.conf.JSFRegistryConfig;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.edsp.midware.drs.common.utils.URI;

public class AKSPlugin extends PluginSupport {

	private static final SysLog log = SysLogUtil.getSysLog(AKSPlugin.class);

	public static AksConfig config;
	
	public static DeviceCryptoService deviceCryptoService;
	
	@Override
	public boolean initPlugin() {
		config = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager().getConfig(AksConfig.class);
		if (config != null) {
			deviceCryptoService = new DeviceCryptoService();
			return true;
			
		}
		log.warn("末找到配置，AKS服务不启动");
		return false;
	}

	@Override
	public void shutdownPlugin() {
		deviceCryptoService.logout();
	}

	@Override
	public void startupPlugin() {
		
		ConsumerConfig<JSFCryptoDistanceService> consumerConfig = new ConsumerConfig<JSFCryptoDistanceService>();
		consumerConfig.setInterfaceId(config.getInferface());
		consumerConfig.setAlias(config.getAliasname());
		consumerConfig.setProtocol(config.getProtocol());
		
		List<RegistryConfig> registryConfigs = RegistryFactory.getRegistryConfigs();
		if (registryConfigs != null && registryConfigs.size() > 0) {
			consumerConfig.setRegistry(RegistryFactory.getRegistryConfigs());
		} else {
			RegistryConfig jsfRegistry = JSFRegistryConfig.getInstance().getJsfRegistry();
			if(jsfRegistry == null) {
				//构造JSF注册中心对象
				jsfRegistry = new RegistryConfig();
				//TODO 獲取註冊地址  JSFConfigUtil.getRegistryURL()
				JSFConfig config = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager().getConfig(JSFConfig.class);
				URI registryUrl = URI.valueOf(config.getRegistryURL());
				String index = registryUrl.getParameter("index");
				if(StringUtil.isNotEmpty(index)) {
					log.info("regist by index " + index);
					jsfRegistry.setIndex(index);
				} else {
					log.info("regist by address " + registryUrl.getHost());
					jsfRegistry.setAddress(registryUrl.getHost());
				}
				JSFRegistryConfig.getInstance().setJsfRegistry(jsfRegistry);
			}
			consumerConfig.setRegistry(jsfRegistry);
		}
		

		JSFCryptoDistanceService service = consumerConfig.refer(); // 获取JSFCryptoDistanceService 代理对象

		deviceCryptoService.setCryptoDisService(service);
		deviceCryptoService.setAppId(config.getAppid());
		deviceCryptoService.setAppName(config.getAppname());
		
		deviceCryptoService.init();
		
		log.debug("AKS系统init 完成");
		
	}

}
