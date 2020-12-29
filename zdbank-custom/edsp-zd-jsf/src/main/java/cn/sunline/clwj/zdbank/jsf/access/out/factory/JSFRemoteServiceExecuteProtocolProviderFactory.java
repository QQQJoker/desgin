package cn.sunline.clwj.zdbank.jsf.access.out.factory;

import cn.sunline.adp.cedar.service.remote.factory.RemoteServiceExecuteProtocolProviderFactory;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceExecuteProtocolProvider;
import cn.sunline.clwj.zdbank.jsf.access.out.JSFRemoteServiceExecuteProtocolProvider;
import cn.sunline.edsp.base.factories.SPIMeta;

@SPIMeta(id = JSFRemoteServiceExecuteProtocolProviderFactory.SPI_ID)
public class JSFRemoteServiceExecuteProtocolProviderFactory implements RemoteServiceExecuteProtocolProviderFactory {

	public static final String SPI_ID = "jsf";

	@Override
	public RemoteServiceExecuteProtocolProvider create() {
		return new JSFRemoteServiceExecuteProtocolProvider();
	}

}
