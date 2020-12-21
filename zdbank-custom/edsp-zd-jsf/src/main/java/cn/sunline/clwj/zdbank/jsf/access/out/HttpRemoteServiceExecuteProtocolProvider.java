package cn.sunline.clwj.zdbank.jsf.access.out;

import com.jd.jsf.gd.registry.RegistryFactory;

import cn.sunline.adp.cedar.base.engine.service.ServiceRequest;
import cn.sunline.adp.cedar.service.remote.config.RemoteServiceConfig;
import cn.sunline.adp.cedar.service.remote.controller.RemoteServiceController;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceExecuteProtocolHandler;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceExecuteProtocolProvider;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceInfo;
import cn.sunline.adp.cedar.service.router.ServiceRouteResult;


public class HttpRemoteServiceExecuteProtocolProvider implements RemoteServiceExecuteProtocolProvider{

	private RemoteServiceConfig config;
	
	@Override
	public void destroy() {
		RegistryFactory.destroyAll();
	}

	@Override
	public RemoteServiceExecuteProtocolHandler getRemoteServiceExecuteProtocolHandler() {
		return new HttpRemoteServiceExecuteProtocolHandler(config);
	}

	@Override
	public RemoteServiceInfo getRemoteServiceInfo(ServiceRequest arg0, ServiceRouteResult arg1,
			RemoteServiceController arg2) {
		RemoteServiceInfo ret = new RemoteServiceInfo();
		return ret;
	}

	@Override
	public void init(RemoteServiceConfig arg0) {
		this.config = arg0;
	}

}
