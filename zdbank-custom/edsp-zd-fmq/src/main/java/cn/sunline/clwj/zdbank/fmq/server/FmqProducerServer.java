package cn.sunline.clwj.zdbank.fmq.server;

import java.util.List;

import com.jdjr.fmq.client.connection.ClusterTransportManager;
import com.jdjr.fmq.client.connection.TransportConfig;
import com.jdjr.fmq.client.connection.TransportManager;
import com.jdjr.fmq.client.producer.MessageProducer;
import com.jdjr.fmq.common.exception.JMQException;
import com.jdjr.fmq.common.message.Message;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.clwj.zdbank.busi.fmq.errors.FmqError;
import cn.sunline.clwj.zdbank.fmq.config.FmqConfig;

public class FmqProducerServer {
	private static final SysLog log = SysLogUtil.getSysLog(FmqProducerServer.class);
	
	private TransportManager manager;

	private MessageProducer producer;

	public FmqProducerServer(FmqConfig fmqConfig) {
		TransportConfig config = new TransportConfig();
		// 设置应用代码
		config.setApp(fmqConfig.getProducerApp());
		// 设置broker地址
		config.setAddress(fmqConfig.getProducerAddress());
		// 设置用户名
		config.setUser(fmqConfig.getProducerUser());
		// 设置密码
		config.setPassword(fmqConfig.getProducerPasswd());
		// 设置发送超时
		config.setSendTimeout(fmqConfig.getSendTimeout());
		// 设置连接超时时间
		config.setConnectionTimeout(fmqConfig.getConnectionTimeout());
		// 设置是否使用epoll模式，windows环境下设置为false，linux环境下设置为true
		config.setEpoll(fmqConfig.isEopll());
		// 创建集群连接管理器
		manager = new ClusterTransportManager(config);
		// 创建发送者
		producer = new MessageProducer(manager);
	}


	public void start() {
		try {
			manager.start();
			log.info("producer start sucess！！！！！");
		} catch (Exception e) {
//			throw new RuntimeException("producer start error",e);
			throw FmqError.fmqConn.F0005(e);
		}

		try {
			producer.start();
			log.info("sender start sucess！！！！！");
		} catch (Exception e) {
//			throw LangUtil.wrapThrow("sender start error",e);
			throw FmqError.fmqConn.F0006(e);
		}

	}

	public void shutdown() {

		if (producer != null) {
			producer.stop();
		}
		if (manager != null) {
			manager.stop();
		}
	}

	/**
	 * 非事务的单条发送
	 */
	public void send(Message message) throws JMQException {
		producer.send(message);
	}

	/**
	 * 非事务的单条发送
	 */
	public void send(Message message, int timeout) throws JMQException {
		producer.send(message, timeout);
	}

	/**
	 * 批量发送，不支持顺序消息，不支持事务
	 */
	public void batchSend(List<Message> messages) throws JMQException {
		producer.send(messages);
	}

	/**
	 * 批量发送，不支持顺序消息，不支持事务
	 */
	public void batchSend(List<Message> messages, int timeout) throws JMQException {
		producer.send(messages, timeout);
	}


}
