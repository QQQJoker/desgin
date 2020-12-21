package cn.sunline.clwj.zdbank.fmq.registry;

import java.util.List;

import com.jdjr.fmq.client.consumer.MessageListener;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.server.online.config.OnlineServerConfig;
import cn.sunline.adp.cedar.server.online.protocol.OnlineAccessServiceRegister;
import cn.sunline.adp.cedar.server.online.registry.InServiceRegistry;
import cn.sunline.adp.cedar.server.online.server.OnlineAccessServiceFacade;
import cn.sunline.clwj.zdbank.fmq.server.DefaultMessageListenerHandler;
import cn.sunline.clwj.zdbank.fmq.server.FmqConsumerServer;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;

public class FMQServiceRegistry implements OnlineAccessServiceRegister {
	private static final SysLog log = SysLogUtil.getSysLog(FMQServiceRegistry.class);
	private final OnlineAccessServiceFacade serviceExecutor;
	private final OnlineServerConfig onlineServerConfig;
	private FmqConsumerServer consumerServer;

	public FMQServiceRegistry(OnlineServerConfig onlineServerConfig, OnlineAccessServiceFacade serviceExecutor) {
		this.serviceExecutor = serviceExecutor;
		this.onlineServerConfig = onlineServerConfig;
	}

	@Override
	public void register(List<InServiceRegistry> serviceRegistries) {
		try {
			consumerServer = new FmqConsumerServer(onlineServerConfig);
			consumerServer.start();
			int sum = 0;
			for (InServiceRegistry bean : serviceRegistries) {
				if (!bean.isEnable()) {
					continue;
				}
				String topicId = bean.getOutServiceCode();
				MessageListener handler = new DefaultMessageListenerHandler(serviceExecutor);
				consumerServer.subscribe(topicId, handler);
				sum++;
			}
			log.info("FMQ服务注册启动成功，当前子系统:[%s],注册的FMQ服务总数为:[%s],数据库表中tsp_service_in中记录数:[%s]", CoreUtil.getSubSystemId(),
					sum, serviceRegistries.size());
		} catch (Exception e) {
			throw ExceptionUtil.wrapThrow(e);
		}

	}

	public FmqConsumerServer getConsumerServer() {
		return consumerServer;
	}

	public void setConsumerServer(FmqConsumerServer consumerServer) {
		this.consumerServer = consumerServer;
	}
	
	

}
