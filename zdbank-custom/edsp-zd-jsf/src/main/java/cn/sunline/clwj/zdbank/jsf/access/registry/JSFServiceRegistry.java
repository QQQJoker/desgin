package cn.sunline.clwj.zdbank.jsf.access.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.jd.jsf.gd.config.ProviderConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.config.ServerConfig;
import com.jd.jsf.gd.registry.RegistryFactory;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.protocol.rpc.access.registry.RPCServiceRegistry;
import cn.sunline.adp.cedar.server.online.config.OnlineAccessProtocolConfig;
import cn.sunline.adp.cedar.server.online.config.OnlineServerConfig;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessServiceRegister;
import cn.sunline.adp.cedar.server.online.registry.InServiceRegistry;
import cn.sunline.adp.cedar.server.online.server.OnlineAccessServiceFacade;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.clwj.zdbank.jsf.access.factory.conf.JSFConfig;
import cn.sunline.clwj.zdbank.jsf.access.factory.conf.JSFRegistryConfig;
import cn.sunline.clwj.zdbank.jsf.access.proxy.JSFServiceImpProxy;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFConstant;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFUtil;
import cn.sunline.clwj.zdbank.jsf.access.util.StringUtils;
import cn.sunline.clwj.zdbank.jsf.access.util.URL;
import cn.sunline.clwj.zdbank.jsf.constant.JSFPluginConstantDef.SPC_JSF;
import cn.sunline.clwj.zdbank.jsf.errors.JSFPluginErrorDef.SP_JSF;

public class JSFServiceRegistry implements OnlineAccessServiceRegister {

	private static final SysLog log = SysLogUtil.getSysLog(RPCServiceRegistry.class);
	final private OnlineAccessServiceFacade serviceExecutor;
	final private OnlineServerConfig onlineServerConfig;
	private List<ProviderConfig<Object>> providerConfigs = new ArrayList<>();

	public JSFServiceRegistry(OnlineServerConfig onlineServerConfig, OnlineAccessServiceFacade serviceExecutor) {
		this.serviceExecutor = serviceExecutor;
		this.onlineServerConfig = onlineServerConfig;
	}

	@Override
	public void register(List<InServiceRegistry> serviceRegistries) {
		try {

			JSFConfig config = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager()
					.getConfig(JSFConfig.class);

			URL registryUrl = URL.valueOf(config.getRegistryURL());

			RegistryConfig jsfRegistry = JSFRegistryConfig.getInstance().getJsfRegistry();
			if (jsfRegistry == null) {

				// 构造JSF注册中心对象
				jsfRegistry = new RegistryConfig();
				String index = registryUrl.getParameter("index");
				if(StringUtils.isEmpty(index) && !registryUrl.getHost().matches(JSFConstant.IP_VAILD) ) {
					index = registryUrl.getHost();
				}
				
				if (StringUtils.isNotEmpty(index)) {
					log.info("regist by index " + index);
					jsfRegistry.setIndex(index);
				} else {
					log.info("regist by address " + registryUrl.getAddress());
					jsfRegistry.setAddress(registryUrl.getAddress());
				}
				JSFRegistryConfig.getInstance().setJsfRegistry(jsfRegistry);
			}

			log.info("实例RegistryConfig");

			int countServices = 0;

			Map<String, InServiceRegistry> serviceRegsitryMap = new HashMap<>();
			// key:interfaceId,value:{key:methodName,IServiceRegistry}
			Map<String, Map<String, InServiceRegistry>> serviceMapping = new HashMap<>();

			for (InServiceRegistry bean : serviceRegistries) {
				String interfaceId = JSFUtil.getInterfaceId(config.getApiPackage(), bean.getOutServiceCode());
				String alias = bean.getOutServiceGroup();
				String methodName = JSFUtil.getMethodName(bean.getOutServiceCode());
				String registKey = interfaceId + "@" + alias;

				// 方法名与内部服务码映射关系
				Map<String, InServiceRegistry> methodMapping = serviceMapping.get(interfaceId);
				if (methodMapping == null) {
					methodMapping = Maps.newHashMap();
					serviceMapping.put(interfaceId, methodMapping);
				}

				if (!bean.isEnable()) {
					continue;
				}

				methodMapping.put(methodName, bean);

				// 以接口+别名粒度注册
				if (serviceRegsitryMap.get(registKey) == null) {
					serviceRegsitryMap.put(registKey, bean);
				}
			}

			// 设置server配置
			ServerConfig serverConfig = new ServerConfig();
			OnlineAccessProtocolConfig protocolConfig = onlineServerConfig.getOnlineAccessProtocolConfig();
			serverConfig.setProtocol("jsf");
			serverConfig.setHost("0.0.0.0");
			URL protocolUrl = null;
			if(StringUtils.isNotEmpty(protocolConfig.getAddress())) {
				protocolUrl = URL.valueOf(protocolConfig.getAddress());
				serverConfig.setProtocol(protocolUrl.getProtocol());
				serverConfig.setHost(protocolUrl.getIp());
				serverConfig.setPort(protocolUrl.getPort());
			}
			serverConfig.setQueues(Integer.valueOf(protocolConfig.getQueuesSize()));
			serverConfig.setThreads(Integer.valueOf(protocolConfig.getMaxThread()));

			for (Entry<String, InServiceRegistry> serviceEntry : serviceRegsitryMap.entrySet()) {
				InServiceRegistry bean = serviceEntry.getValue();

				String interfaceId = JSFUtil.getInterfaceId(config.getApiPackage(), bean.getOutServiceCode());

				ProviderConfig<Object> providerConfig = new ProviderConfig<Object>();
				providerConfig.setInterfaceId(interfaceId);
				providerConfig.setRef(JSFServiceImpProxy.creatInstance(interfaceId, serviceMapping.get(interfaceId), serviceExecutor));
				// 别名不同服务需要不同
				providerConfig.setAlias(serviceEntry.getValue().getOutServiceGroup());
				providerConfig.setServer(serverConfig);

				if(protocolUrl != null) {
					for (Map.Entry<String, String> entry : protocolUrl.getParameters().entrySet()) {
						providerConfig.setParameter(entry.getKey(), entry.getValue());
					}
				}

				List<RegistryConfig> registryConfigs = RegistryFactory.getRegistryConfigs();
				if (registryConfigs != null && registryConfigs.size() > 0) {
					providerConfig.setRegistry(registryConfigs);
				} else {
					providerConfig.setRegistry(JSFRegistryConfig.getInstance().getJsfRegistry());
				}

				// syslog.info("接口[" + serviceEntry.getKey() +"]注册的服务信息:" + methodMapping);
				providerConfig.export();
				providerConfigs.add(providerConfig);
				countServices++;
			}

			log.info(SPC_JSF.C002(), CoreUtil.getSubSystemId(), countServices, serviceRegistries.size());
		} catch (Exception e) {
			// syslog.error("RPC服务注册启动失败", e);
			log.error(SPC_JSF.C003(), e);
			// throw LangUtil.wrapThrow("RPC服务注册启动失败", e);
			throw SP_JSF.E037(e);
		}
	}

	public List<ProviderConfig<Object>> getProviderConfigs() {
		return providerConfigs;
	}

	public void setProviderConfigs(List<ProviderConfig<Object>> providerConfigs) {
		this.providerConfigs = providerConfigs;
	}

}
