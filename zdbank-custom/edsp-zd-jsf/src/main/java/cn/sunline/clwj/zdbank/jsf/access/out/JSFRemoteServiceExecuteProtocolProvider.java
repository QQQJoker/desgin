package cn.sunline.clwj.zdbank.jsf.access.out;

import com.jd.jsf.gd.registry.RegistryFactory;

import cn.sunline.adp.cedar.base.engine.service.ServiceRequest;
import cn.sunline.adp.cedar.service.remote.config.RemoteServiceConfig;
import cn.sunline.adp.cedar.service.remote.controller.RemoteServiceController;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceExecuteProtocolHandler;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceExecuteProtocolProvider;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceInfo;
import cn.sunline.adp.cedar.service.router.ServiceRouteResult;

public class JSFRemoteServiceExecuteProtocolProvider implements RemoteServiceExecuteProtocolProvider {

	private RemoteServiceConfig config;

	@Override
	public void init(RemoteServiceConfig config) {
		this.config = config;
	}

	@Override
	public RemoteServiceExecuteProtocolHandler getRemoteServiceExecuteProtocolHandler() {
		return new JSFRemoteServiceExecuteProtocolHandler(config);
	}

	@Override
	public RemoteServiceInfo getRemoteServiceInfo(ServiceRequest request, ServiceRouteResult routeResult,
			RemoteServiceController remoteServiceController) {
		RemoteServiceInfo ret = new RemoteServiceInfo();
		ret.setDcnCode(routeResult.getTargetDCN()); // 设置DCN编号
		ret.setTenantId(routeResult.getTargetCorpno());
		return ret;
	}

	@Override
	public void destroy() {
		RegistryFactory.destroyAll();

	}

}
