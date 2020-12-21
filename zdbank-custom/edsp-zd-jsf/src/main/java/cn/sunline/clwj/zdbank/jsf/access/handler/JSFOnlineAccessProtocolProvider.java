package cn.sunline.clwj.zdbank.jsf.access.handler;

import java.util.Map;

import com.jd.jsf.gd.config.ProviderConfig;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.server.online.config.OnlineServerConfig;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessProtocolHandler;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessProtocolProvider;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessServiceRegister;
import cn.sunline.adp.cedar.server.online.server.OnlineAccessServiceFacade;
import cn.sunline.clwj.zdbank.jsf.access.registry.JSFServiceRegistry;

public class JSFOnlineAccessProtocolProvider implements OnlineAccessProtocolProvider {
	private static final SysLog log = SysLogUtil.getSysLog(JSFOnlineAccessProtocolProvider.class);

	private JSFServiceRegistry jsfRegistry;

	private OnlineServerConfig onlineServerConfig;

	@Override
	public boolean init(OnlineServerConfig onlineServerConfig) {

		this.onlineServerConfig = onlineServerConfig;

		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public OnlineAccessProtocolHandler<Map<String,Object>, Map<String,Object>> getOnlineAccessProtocolHandler(String pkgType,
			String encoding) {
		return new JSFOnlineAccessProtocolHandler(onlineServerConfig, pkgType, encoding);
	}

	@Override
	public OnlineAccessServiceRegister getOnlineAccessServiceRegister(OnlineAccessServiceFacade serviceExecutor) {
		if (jsfRegistry == null) {
			jsfRegistry = new JSFServiceRegistry(onlineServerConfig, serviceExecutor);
		}
		return jsfRegistry;
	}

	@Override
	public void destory() {

		for (ProviderConfig<Object> providerConfig : jsfRegistry.getProviderConfigs()) {
			try {
				providerConfig.unexport();
				log.debug("服务[%s][%s]下线成功", providerConfig.getIfaceId(), providerConfig.getAlias());
			} catch (Exception e) {
				log.error("服务[%s][%s]下线失败", e, providerConfig.getIfaceId(), providerConfig.getAlias());
			}
		}

	}

}
