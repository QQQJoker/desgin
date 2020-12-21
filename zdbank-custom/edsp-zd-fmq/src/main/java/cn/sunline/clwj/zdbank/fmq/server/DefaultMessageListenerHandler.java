package cn.sunline.clwj.zdbank.fmq.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jdjr.fmq.client.consumer.MessageListener;
import com.jdjr.fmq.common.message.Message;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.server.online.server.OnlineAccessServiceFacade;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.edsp.base.util.lang.StringUtil;

public class DefaultMessageListenerHandler implements MessageListener{

	private static final SysLog log = SysLogUtil.getSysLog(DefaultMessageListenerHandler.class);
	private OnlineAccessServiceFacade executor;
	
	public DefaultMessageListenerHandler(OnlineAccessServiceFacade executor) {
		this.executor=executor;
	}
	
	@Override
	public void onMessage(List<Message> messages) throws Exception {
		for (Message message : messages) {
			String body = message.getText();
			Map<String,Object> ret = new HashMap<>();
			ret = StringUtil.isBlank(body)?ret:JsonUtil.parse(body);
			log.info("收到FMQ消息：App:[%s],TopicId:[%s],busiId:[%s],message:[%s]",message.getApp(),message.getTopic(),message.getBusinessId(),body);
			executor.execute(ret);
		}
		
	}

}
