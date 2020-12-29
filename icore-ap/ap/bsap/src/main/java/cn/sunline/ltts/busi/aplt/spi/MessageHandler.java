package cn.sunline.ltts.busi.aplt.spi;

import java.util.List;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.engine.online.handler.OETAfterHandler;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerConstant;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerContext;
import cn.sunline.clwj.zdbank.fmq.util.FmqUtil;
import cn.sunline.clwj.zdbank.fmq.util.Message;
import cn.sunline.clwj.zdbank.fmq.util.MessageEngineContext;
import cn.sunline.edsp.base.annotation.Groups;
import cn.sunline.edsp.base.factories.SPIMeta;

@SPIMeta(id=MessageHandler.SPI_ID)
@Order(1001)
@Groups({OETHandlerConstant.FLOW_ENGINE_TYPE})
public class MessageHandler implements OETAfterHandler {
	
	public static final String SPI_ID = "message_handler";
	
	public static final SysLog log = SysLogUtil.getSysLog(MessageHandler.class);

	@Override
	public void handler(OETHandlerContext context) {
		List<Message> gnsKeys =  MessageEngineContext.getTxnTempObjMap();
		
		if(MessageEngineContext.getTxnTempObjMap().isEmpty()) {
			return ;
		}
		
		for (Message message : gnsKeys) {
			FmqUtil.send(message.getTopicId(), message.getMessageBody(), message.getBusiId());
			log.debug("交易后处理成功发送异步信息：[%s]条！！！",gnsKeys.size());
		}
	}

}
