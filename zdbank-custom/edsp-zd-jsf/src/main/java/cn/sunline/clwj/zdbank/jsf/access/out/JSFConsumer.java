package cn.sunline.clwj.zdbank.jsf.access.out;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.registry.RegistryFactory;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.MethodUtil;
import cn.sunline.adp.cedar.service.remote.controller.RemoteServiceController;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.clwj.zdbank.jsf.access.factory.conf.JSFConfig;
import cn.sunline.clwj.zdbank.jsf.access.factory.conf.JSFRegistryConfig;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFUtil;
import cn.sunline.clwj.zdbank.jsf.access.util.StringUtils;
import cn.sunline.clwj.zdbank.jsf.access.util.URL;


public class JSFConsumer {
	private static final SysLog syslog = SysLogUtil.getSysLog(JSFConsumer.class);

	private static final Map<String, ConsumerConfig<Object>> consumerRefs = new ConcurrentHashMap<>();

	private JSFConsumer() {
		init();
	}

	private static final JSFConsumer instance = new JSFConsumer();

	private JSFConfig config;

	private boolean isInit = false;

	public static JSFConsumer get() {
		return instance;
	}

	public void init() {
		if (isInit)
			return;
		config = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager().getConfig(JSFConfig.class);
		creatRegistryConfig(URL.valueOf(config.getRegistryURL()));

		syslog.info("get jsfConfig is " + config);
		isInit = true;

	}

	private RegistryConfig creatRegistryConfig(URL registryUrl) {
		RegistryConfig jsfRegistry = JSFRegistryConfig.getInstance().getJsfRegistry();
		if (jsfRegistry == null) {
			// 构造JSF注册中心对象
			jsfRegistry = new RegistryConfig();
			String index = registryUrl.getParameter("index");
			if (StringUtils.isNotEmpty(index)) {
				syslog.info("regist by index " + index);
				jsfRegistry.setIndex(index);
			} else {
				syslog.info("regist by address " + registryUrl.getAddress());
				jsfRegistry.setAddress(registryUrl.getAddress());
			}
			JSFRegistryConfig.getInstance().setJsfRegistry(jsfRegistry);

		}
		return jsfRegistry;
	}

	private RegistryConfig getRegistryConfig(String registryId) {
		RegistryConfig ret = JSFRegistryConfig.getInstance().getJsfRegistry();
		if (ret == null)
			throw new RuntimeException("服务消费对象构造失败：获取注册中心信息失败!");
		// }

		return ret;
	}

	/**
	 * 建议启动的时候进行初始化
	 */
	public synchronized Object getConsumerRef(String interfaceId, String alias, String registryId,RemoteServiceController remoteServiceController) {
		if (!isInit)
			throw new RuntimeException("JSF消费者尚未初始化!");

		String key = interfaceId + ":" + alias;

		ConsumerConfig<Object> ret = consumerRefs.get(key);
		if (ret == null) {
			ret = new ConsumerConfig<>();
			ret.setInterfaceId(interfaceId);
			ret.setAlias(alias);
			ret.setProtocol("jsf");
			ret.setTimeout(remoteServiceController.getTimeout());
			List<RegistryConfig> registryConfigs = RegistryFactory.getRegistryConfigs();
			// 根据配置的多个注册中心
			if (registryConfigs != null && registryConfigs.size() > 0) {
				ret.setRegistry(registryConfigs);
			} else {
				// 根据index获取注册中心
				ret.setRegistry(getRegistryConfig(registryId));
			}
			syslog.info("add consumerRef key [%s],ref [%s]",key,ret);
			consumerRefs.put(key, ret);
		}
		return ret.refer();
	}
	
	public synchronized <T> T getConsumerRef(String interfaceId, String alias, int timeout) {
	
		if (!isInit)
			throw new RuntimeException("JSF消费者尚未初始化!");

		String key = interfaceId + ":" + alias;

		ConsumerConfig<Object> ret = consumerRefs.get(key);
		if (ret == null) {
			ret = new ConsumerConfig<>();
			ret.setInterfaceId(interfaceId);
			ret.setAlias(alias);
			ret.setProtocol("jsf");
			ret.setTimeout(timeout);
			List<RegistryConfig> registryConfigs = RegistryFactory.getRegistryConfigs();
			// 根据配置的多个注册中心
			if (registryConfigs != null && registryConfigs.size() > 0) {
				ret.setRegistry(registryConfigs);
			} 
			syslog.info("add consumerRef key [%s],ref [%s]",key,ret);
			consumerRefs.put(key, ret);
		}
		return (T) ret.refer();
	}
	
	public synchronized <T> T getConsumerRef(String interfaceId, String alias) {
		
		return getConsumerRef(interfaceId,alias,10000);
	}
	
	

	public Map<String,Object> invoke(RemoteServiceController remoteServiceController, String routeGroup,
			DataArea requestData) {

		String outServiceCode = remoteServiceController.getOutServiceVisitIdentity().getServiceCode();

		String interfaceId = JSFUtil.getInterfaceId(config.getApiPackage(), outServiceCode);
		String aliasName = remoteServiceController.getOutServiceVisitIdentity().getServiceGroup();

		Object ref = getConsumerRef(interfaceId, aliasName, config.getConsumerAddress(),remoteServiceController);

		String methodName = JSFUtil.getMethodName(outServiceCode);

		Method method = MethodUtil.getMethod(JSFUtil.classForName(interfaceId), methodName, false, Map.class,
				Map.class);

		Map<String,Object> request = new HashMap<>();

		// 清理内部类型
		String reqStr = JsonUtil.format(requestData.getData());
		DataArea req = DataArea.buildWithData(JsonUtil.parse(reqStr));

		JSFUtil.setHeader(request, req.getSystem());
		JSFUtil.setHeader(request, req.getCommReq());
		JSFUtil.setBody(request, req.getInput());

		String fullService = interfaceId + ":" + aliasName;
		try {
			syslog.info("开始执行服务[%s]外调,methodName is[%s]", fullService,method.getName());
			Map<String,Object> response = (Map<String,Object>) method.invoke(ref, request);
			syslog.info("服务[%s]外调执行成功", fullService);
			return response;
		} catch (Exception e) {
			syslog.error("服务[%s]外调执行失败", e, fullService);
			throw new RuntimeException(
					"服务[" + remoteServiceController.getOutServiceVisitIdentity().getVisitIdentity() + "]外调失败", e);
		}
	}

	public void destroy() {
		RegistryFactory.destroyAll();
	}
}
