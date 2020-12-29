package cn.sunline.clwj.zdbank.fmq.server;

import com.jdjr.fmq.client.connection.ClusterTransportManager;
import com.jdjr.fmq.client.connection.TransportConfig;
import com.jdjr.fmq.client.connection.TransportManager;
import com.jdjr.fmq.client.consumer.ConsumerConfig;
import com.jdjr.fmq.client.consumer.MessageConsumer;
import com.jdjr.fmq.client.consumer.MessageListener;

import cn.sunline.adp.cedar.server.online.config.OnlineServerConfig;
import cn.sunline.clwj.zdbank.fmq.plugin.FmqPlugin;

/**
 * 消息消费服务
 * @author Joker
 * 20201020
 */
public class FmqConsumerServer {
	
	private TransportManager manager;
	
	private MessageConsumer messageConsumer;

	public FmqConsumerServer(OnlineServerConfig onlineServerConfig) {
		TransportConfig config = new TransportConfig();
		config.setApp(FmqPlugin.config.getConsumerApp());
		config.setAddress(onlineServerConfig.getOnlineAccessProtocolConfig().getAddress());
		config.setUser(FmqPlugin.config.getConsumerUser());
		config.setPassword(FmqPlugin.config.getConsumerPasswd());
		config.setSendTimeout(20000);
		config.setConnectionTimeout(20000);
		config.setSoTimeout(20000);
		
		ConsumerConfig consumerConfig = new ConsumerConfig();
		consumerConfig.setMaxConcurrent(onlineServerConfig.getOnlineAccessProtocolConfig().getMaxThread());
		consumerConfig.setMinConcurrent(onlineServerConfig.getOnlineAccessProtocolConfig().getMinThread());
		
		manager = new ClusterTransportManager(config);
		messageConsumer = new MessageConsumer(consumerConfig,manager,null);
		
	}

	public void start() throws Exception {
		messageConsumer.start();
	}
	
	public void subscribe(String topic, MessageListener listener) {
		messageConsumer.subscribe(topic, listener);
	}
	
	public void shutdown() throws Exception{
		if(messageConsumer != null) {
			messageConsumer.stop();
		}
		
		if(manager != null) {
			manager.stop();
		}
	}
	
	

}
