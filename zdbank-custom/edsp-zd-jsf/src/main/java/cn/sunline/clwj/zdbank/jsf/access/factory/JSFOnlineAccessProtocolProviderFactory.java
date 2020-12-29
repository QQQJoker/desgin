package cn.sunline.clwj.zdbank.jsf.access.factory;

import cn.sunline.adp.cedar.server.online.factory.OnlineAccessProtocolProviderFactory;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessProtocolProvider;
import cn.sunline.clwj.zdbank.jsf.access.handler.JSFOnlineAccessProtocolProvider;
import cn.sunline.edsp.base.factories.SPIMeta;

@SPIMeta(id = "jsf")
public class JSFOnlineAccessProtocolProviderFactory implements OnlineAccessProtocolProviderFactory {

	@Override
	public OnlineAccessProtocolProvider create() {
		return new JSFOnlineAccessProtocolProvider();
	}

}
