package cn.sunline.clwj.zdbank.fmq.out;

import java.util.HashMap;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.cedar.base.engine.ResponseHeaderData.RetStatus;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.engine.service.ServiceRequest;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.protocol.rest.constant.RestConstant;
import cn.sunline.adp.cedar.service.executor.ServiceExecutorContext;
import cn.sunline.adp.cedar.service.remote.config.RemoteServiceConfig;
import cn.sunline.adp.cedar.service.remote.controller.RemoteServiceController;
import cn.sunline.adp.cedar.service.remote.exception.RemoteTimeoutException;
import cn.sunline.adp.cedar.service.remote.protocol.AbstractRemoteServiceExecuteProtocolHandler;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceExecuteProtocolHandler;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.clwj.zdbank.fmq.util.FMQConstant;
import cn.sunline.clwj.zdbank.fmq.util.FmqUtil;
import cn.sunline.clwj.zdbank.fmq.util.Message;
import cn.sunline.clwj.zdbank.fmq.util.MessageEngineContext;
import cn.sunline.edsp.base.util.lang.StringUtil;

public class FmqRemoteServiceExecuteProtocolHandler extends AbstractRemoteServiceExecuteProtocolHandler
		implements RemoteServiceExecuteProtocolHandler {

	private static final SysLog log = SysLogUtil.getSysLog(FmqRemoteServiceExecuteProtocolHandler.class);

	public FmqRemoteServiceExecuteProtocolHandler(RemoteServiceConfig config) {
		super(config);
	}

	@Override
	public void callRemote(ServiceExecutorContext context, RemoteServiceController remoteServiceController)
			throws RemoteTimeoutException {
		
		Map<String, Object> commReq = context.getServiceRequest().getRequestBody().getCommReq();
		commReq.put(FMQConstant.GLB_SEQ_NO, context.getServiceRequest().getRequestHeader().getBusiSeqNo());
		commReq.put(FMQConstant.SYS_SEQ_NO, context.getServiceRequest().getRequestHeader().getCallSeqNo());
		commReq.put(RestConstant.GLOBAL_TRAN_SEQ_NO, context.getServiceRequest().getRequestHeader().getGlobalTranSeqNo());
		commReq.put(HeaderDataConstants.IN_SERVICE_MARK, context.getServiceRequest().getRequestHeader().getInServiceMark());
		commReq.put(HeaderDataConstants.CONSUMER_SYS_ID, context.getServiceRequest().getRequestHeader().getConsumerSysId());
		commReq.put(HeaderDataConstants.ORIG_SYS_ID, context.getServiceRequest().getRequestHeader().getOrigSysId());
		commReq.put(HeaderDataConstants.ORIG_SRV_ID, context.getServiceRequest().getRequestHeader().getOrigSrvId());
		commReq.put(HeaderDataConstants.CONSUMER_CALL_SEQ_NO, context.getServiceRequest().getRequestHeader().getConsumerCallSeqNo());
		commReq.put(FMQConstant.SYS_DATE, context.getServiceRequest().getRequestHeader().getTranTimestamp());
		commReq.put(HeaderDataConstants.REVERSAL_SEQ_NO, context.getServiceRequest().getRequestHeader().getReversalSeqNo());

		if(CommUtil.isNull(commReq.get(FMQConstant.SERVNO))) {
			commReq.put(FMQConstant.SERVNO, context.getServiceRequest().getRequestHeader().get(FMQConstant.SERVNO));
		}
		context.getServiceRequest().getRequestBody().setCommReq(commReq);

		String topicId = remoteServiceController.getOutServiceVisitIdentity().getServiceCode();
		String busiId = context.getServiceRequest().getRequestHeader().getBusiSeqNo();
		String isNow = remoteServiceController.getOutServiceVisitIdentity().getServiceVersion();
		
		Map<String,Object> request = getRequestMap(context.getServiceRequest());
		
		String messgae = JsonUtil.format(request);
		
		if(FMQConstant.IS_NOW.equals(isNow) && StringUtil.isNotEmpty(isNow)) {
			FmqUtil.send(topicId, messgae, busiId);
		}else {
			Message msg = new Message(topicId,messgae,busiId);
			MessageEngineContext.addTxnTempObj(msg);
		}
		log.debug("发送fmq消息：主题[%s],消息内容[%s],是否立即发送[%s]",topicId,messgae,isNow);
		DataArea retDataArea = DataArea.buildWithEmpty();
		context.getServiceRequest();
		context.getServiceResponse().setResponseData(retDataArea); // 设置原始响应内容
		context.getServiceResponse().getResponseHeader().setRetStatus(RetStatus.SUCCESS.getValue());
	}
	
	private Map<String,Object> getRequestMap(ServiceRequest map){
		Map<String,Object>  request = new HashMap<>();
		request.put(FMQConstant.HEADER, map.getRequestHeader());
		request.put(FMQConstant.BODY, map.getRequestBody());
		return request;
	}

}
