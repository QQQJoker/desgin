package cn.sunline.clwj.zdbank.fmq.util;

import java.util.HashMap;
import java.util.Map;

import com.jdjr.fmq.common.exception.JMQException;
import com.jdjr.fmq.common.message.Message;

import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.clwj.zdbank.busi.fmq.errors.FmqError;
import cn.sunline.clwj.zdbank.fmq.plugin.FmqPlugin;

public class FmqUtil {
	
	private static final SysLog log = SysLogUtil.getSysLog(FmqUtil.class);

	public static void send(String topicId, String messageContext, String businessId) {
		send(topicId, messageContext, businessId, FmqPlugin.config.getSendTimeout());
	}
	
	public static void send(String topicId, String messageContext, String businessId, int timeout) {
		try {
			Message message = new Message(topicId, messageContext, businessId); //业务ID暂时需要修改
			FmqPlugin.getProducer().send(message, timeout);
			if (log.isDebugEnabled()) {
				log.debug("发送消息[" + messageContext + "]成功 ");
			}
		} catch (JMQException e) {
//			throw LangUtil.wrapThrow("发送消息["+ messageContext +"]失败", e);
			throw FmqError.fmqConn.F0007(messageContext, e);
		}
	}
	
	
	public static Map<String,Object> getHeader(Map<String,Object> map){
		if(map == null) return null;
		Map<String, Object> rest = (Map<String, Object>) map.get(FMQConstant.HEADER);
		return rest != null ? rest : new HashMap<>();
	}
	
	public static void setHeader(Map<String,Object> src,Map<String,Object> dest){
		Map<String,Object> head = getHeader(src);
		head.putAll(dest);
		src.put(FMQConstant.HEADER,head);
	}
	
	public static Map<String,Object> getBody(Map<String,Object> map){
		if(map == null) return null;
		Map<String, Object> rest = (Map<String, Object>) map.get(FMQConstant.BODY);
		return rest != null ? rest : new HashMap<>();
	}
	
	public static void setBody(Map<String,Object> src,Map<String,Object> dest){		
		src.put(FMQConstant.BODY,dest);
	}
	
	public static Map<String,Object> getCommData(Map<String,Object> map){
		if(map == null) return null;
		Map<String, Object> rest = (Map<String, Object>) map.get(FMQConstant.COMMDATA);
		return rest != null ? JsonUtil.toMap(rest) : new HashMap<>();
	}
	
	public static void setCommData(Map<String,Object> src,Map<String,Object> dest){		
		 src.put(FMQConstant.COMMDATA,dest);
	}
	
	public static void removeMappingFiled(Map<String,Object> map) {
		Map<String,Object> header = getHeader(map);
		header.remove(FMQConstant.INPUCD);
		header.remove(FMQConstant.INPUDT);
		header.remove(FMQConstant.TRANBR);
		header.remove(HeaderDataConstants.BUSI_SEQ_NO);
		header.remove(HeaderDataConstants.CALL_SEQ_NO);
		setHeader(map, header);
	}

}
