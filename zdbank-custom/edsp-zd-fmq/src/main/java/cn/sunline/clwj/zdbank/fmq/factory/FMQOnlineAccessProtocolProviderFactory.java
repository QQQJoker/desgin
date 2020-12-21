package cn.sunline.clwj.zdbank.fmq.factory;

import cn.sunline.adp.cedar.server.online.factory.OnlineAccessProtocolProviderFactory;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessProtocolProvider;
import cn.sunline.edsp.base.factories.SPIMeta;

@SPIMeta(id = "fmq")
public class FMQOnlineAccessProtocolProviderFactory implements OnlineAccessProtocolProviderFactory{

	@Override
	public OnlineAccessProtocolProvider create() {
		return new FMQOnlineAccessProtocolProvider();
	}

}
