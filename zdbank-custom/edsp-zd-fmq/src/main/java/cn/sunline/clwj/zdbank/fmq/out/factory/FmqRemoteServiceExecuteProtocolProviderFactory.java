package cn.sunline.clwj.zdbank.fmq.out.factory;

import cn.sunline.adp.cedar.service.remote.factory.RemoteServiceExecuteProtocolProviderFactory;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceExecuteProtocolProvider;
import cn.sunline.clwj.zdbank.fmq.out.FmqRemoteServiceExecuteProtocolProvider;
import cn.sunline.edsp.base.factories.SPIMeta;

@SPIMeta(id = FmqRemoteServiceExecuteProtocolProviderFactory.SPI_ID)
public class FmqRemoteServiceExecuteProtocolProviderFactory implements RemoteServiceExecuteProtocolProviderFactory {

	public static final String SPI_ID = "fmq";

	@Override
	public RemoteServiceExecuteProtocolProvider create() {
		return new FmqRemoteServiceExecuteProtocolProvider();
	}

}
