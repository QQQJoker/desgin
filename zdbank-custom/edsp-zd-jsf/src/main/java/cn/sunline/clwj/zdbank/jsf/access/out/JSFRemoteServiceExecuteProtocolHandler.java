package cn.sunline.clwj.zdbank.jsf.access.out;

import java.util.Map;

import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
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
import cn.sunline.clwj.zdbank.jsf.access.util.JSFConstant;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFUtil;

public class JSFRemoteServiceExecuteProtocolHandler extends AbstractRemoteServiceExecuteProtocolHandler
		implements RemoteServiceExecuteProtocolHandler {

	private static final SysLog log = SysLogUtil.getSysLog(JSFRemoteServiceExecuteProtocolHandler.class);

	public JSFRemoteServiceExecuteProtocolHandler(RemoteServiceConfig config) {
		super(config);
	}

	@Override
	public void callRemote(ServiceExecutorContext context, RemoteServiceController remoteServiceController)
			throws RemoteTimeoutException {
		
		Map<String, Object> commReq = context.getServiceRequest().getRequestBody().getCommReq();
		commReq.put(JSFConstant.GLB_SEQ_NO, context.getServiceRequest().getRequestHeader().getBusiSeqNo());
		commReq.put(JSFConstant.SYS_SEQ_NO, context.getServiceRequest().getRequestHeader().getCallSeqNo());
		commReq.put(RestConstant.GLOBAL_TRAN_SEQ_NO, context.getServiceRequest().getRequestHeader().getGlobalTranSeqNo());
		commReq.put(HeaderDataConstants.IN_SERVICE_MARK, context.getServiceRequest().getRequestHeader().getInServiceMark());
		commReq.put(HeaderDataConstants.CONSUMER_SYS_ID, context.getServiceRequest().getRequestHeader().getConsumerSysId());
		commReq.put(HeaderDataConstants.ORIG_SYS_ID, context.getServiceRequest().getRequestHeader().getOrigSysId());
		commReq.put(HeaderDataConstants.ORIG_SRV_ID, context.getServiceRequest().getRequestHeader().getOrigSrvId());
		commReq.put(HeaderDataConstants.CONSUMER_CALL_SEQ_NO, context.getServiceRequest().getRequestHeader().getConsumerCallSeqNo());
		commReq.put(JSFConstant.SYS_DATE, context.getServiceRequest().getRequestHeader().getTranTimestamp());
		commReq.put(HeaderDataConstants.REVERSAL_SEQ_NO, context.getServiceRequest().getRequestHeader().getReversalSeqNo());
		// update by zhangwh  服务请求头中并没有servno，导致服务接入方无法获取到servno
//		commReq.put(JSFConstant.SERVNO, context.getServiceRequest().getRequestHeader().get(JSFConstant.SERVNO));
		if(CommUtil.isNull(commReq.get(JSFConstant.SERVNO))) {
			commReq.put(JSFConstant.SERVNO, context.getServiceRequest().getRequestHeader().get(JSFConstant.SERVNO));
		}
		context.getServiceRequest().getRequestBody().setCommReq(commReq);
		
		String routeGroup = context.getServiceRouteResult().getTargetDCN(); // 获取路由后的分组

		log.info("报文接出Body信息-->[REQ][%s]",context.getServiceRequest().getRequestBody().getData());
		Map<String,Object> resultRes = JSFConsumer.get().invoke(remoteServiceController, routeGroup, context.getServiceRequest().getRequestBody());
		
		DataArea retDataArea = DataArea.buildWithEmpty();
		retDataArea.getOutput().putAll((Map<? extends String, ? extends Object>) JSFUtil.getBody(resultRes).get(JSFConstant.RETDATA));
		retDataArea.getCommRes().putAll(JSFUtil.getHeader(resultRes));
		log.info("报文接出DataArea信息-->[RES][%s]",retDataArea);		
		context.getServiceResponse().setRespStr(JsonUtil.format(retDataArea)); // 设置原始响应字符串
		context.getServiceResponse().setResponseData(retDataArea); // 设置原始响应内容
		context.getServiceResponse().getResponseHeader().putAll(JSFUtil.getHeader(resultRes));
		log.info("报文接出Body信息-->[RES][%s]",context.getServiceResponse().getResponseData().getData());
	}

}
