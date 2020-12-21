package cn.sunline.clwj.zdbank.jsf.access.out.factory;

import cn.sunline.adp.cedar.service.remote.factory.RemoteServiceExecuteProtocolProviderFactory;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceExecuteProtocolProvider;
import cn.sunline.clwj.zdbank.jsf.access.out.HttpRemoteServiceExecuteProtocolProvider;
import cn.sunline.edsp.base.factories.SPIMeta;

@SPIMeta(id = HttpRemoteServiceExecuteProtocolProviderFactory.SPI_ID)
public class HttpRemoteServiceExecuteProtocolProviderFactory implements RemoteServiceExecuteProtocolProviderFactory{
	public static final String SPI_ID = "http";

	@Override
	public RemoteServiceExecuteProtocolProvider create() {
		return new HttpRemoteServiceExecuteProtocolProvider();
	}
}
