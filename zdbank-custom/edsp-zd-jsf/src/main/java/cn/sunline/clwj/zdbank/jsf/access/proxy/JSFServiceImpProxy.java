package cn.sunline.clwj.zdbank.jsf.access.proxy;

import java.lang.reflect.Method;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.server.online.registry.InServiceRegistry;
import cn.sunline.adp.cedar.server.online.server.OnlineAccessServiceFacade;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFUtil;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class JSFServiceImpProxy {
	
	private static final SysLog syslog = SysLogUtil.getSysLog(JSFServiceImpProxy.class);

	public static Object creatInstance(String interfaceId, Map<String, InServiceRegistry> methodMapping, OnlineAccessServiceFacade serviceExecutor) {
		Enhancer enhancer = new Enhancer();
		enhancer.setUseCache(true);
		Class<?> interfaceClass;
		try {
			interfaceClass = Class.forName(interfaceId);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
		enhancer.setInterfaces(new Class[] { interfaceClass });
		enhancer.setCallback(new JSFServiceImp(interfaceClass, methodMapping, serviceExecutor));
		Object obj = enhancer.create();
		return obj;
	}

	/**
	 * 服务类型（xx.serviceType.xml）对应的服务类型接口类（xx.java），对应方法的拦截处理，每一个服务类型对象可能对应不同的服务实现<br/>
	 * 
	 * 如果这里将 callIdentity 作为一个变量的话会导致获得代理对象之后就得调用其方法，否则可能 callIdentity 会被改变从而导致调用bug
	 * 
	 */
	public static class JSFServiceImp implements MethodInterceptor {

		// serviceExecutor
		private final OnlineAccessServiceFacade serviceExecutor;
		// methodMapping
		private final Map<String, InServiceRegistry> methodMapping;
		// interface 
		private final Class<?> interfaceClass;

		public JSFServiceImp(Class<?> interfaceClass, Map<String, InServiceRegistry> methodMapping, OnlineAccessServiceFacade serviceExecutor) {
			this.interfaceClass = interfaceClass;
			this.methodMapping = methodMapping;
			this.serviceExecutor = serviceExecutor;
		}

		@Override
		public Object intercept(Object paramObject, Method paramMethod, Object[] paramArrayOfObject,
				MethodProxy paramMethodProxy) throws Throwable {
			if (!(paramArrayOfObject[0] instanceof Map)) {
				throw new RuntimeException("参数类型不正确");
			}
			
			InServiceRegistry serviceRegistry = methodMapping.get(paramMethod.getName());
			if(serviceRegistry == null) {
				syslog.info("接口[" + interfaceClass.getName() +"." + paramMethod.getName() + "]未找到映射信息:" + methodMapping);
				throw new RuntimeException("未找到接口[" + interfaceClass.getName() + "]的方法[" + paramMethod.getName() + "]关联的内部服务码或配置未启用");
			}
			Map<String,Object> jsfRequest = (Map<String,Object>) paramArrayOfObject[0];
			Map<String,Object> header = JSFUtil.getHeader(jsfRequest);
			header.put("service", serviceRegistry.getOutServiceCode());
			JSFUtil.setHeader(jsfRequest, header);
			return serviceExecutor.execute(jsfRequest);
		}
	}
}
