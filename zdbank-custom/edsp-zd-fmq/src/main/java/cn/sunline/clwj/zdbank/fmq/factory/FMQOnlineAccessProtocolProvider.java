package cn.sunline.clwj.zdbank.fmq.factory;

import java.util.Map;

import cn.sunline.adp.cedar.server.online.config.OnlineServerConfig;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessProtocolHandler;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessProtocolProvider;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessServiceRegister;
import cn.sunline.adp.cedar.server.online.server.OnlineAccessServiceFacade;
import cn.sunline.clwj.zdbank.fmq.handler.FMQOnlineAccessProtocolHandler;
import cn.sunline.clwj.zdbank.fmq.registry.FMQServiceRegistry;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;

public class FMQOnlineAccessProtocolProvider implements OnlineAccessProtocolProvider {
	private OnlineServerConfig onlineServerConfig;
	private FMQServiceRegistry fmqRegistry;

	@Override
	public boolean init(OnlineServerConfig onlineServerConfig) {
		this.onlineServerConfig = onlineServerConfig;
		return true;
	}

	@Override
	public OnlineAccessProtocolHandler<Map<String,Object>, Map<String,Object>> getOnlineAccessProtocolHandler(String pkgType,
			String encoding) {
		return new FMQOnlineAccessProtocolHandler(onlineServerConfig, pkgType, encoding);

	}

	@Override
	public OnlineAccessServiceRegister getOnlineAccessServiceRegister(OnlineAccessServiceFacade serviceExecutor) {
		if (fmqRegistry == null) {
			fmqRegistry = new FMQServiceRegistry(onlineServerConfig, serviceExecutor);
		}
		return fmqRegistry;

	}

	@Override
	public void destory() {
		try {
			fmqRegistry.getConsumerServer().shutdown();
		}catch(Exception e) {
			ExceptionUtil.wrapThrow(e);
		}
	}

}
