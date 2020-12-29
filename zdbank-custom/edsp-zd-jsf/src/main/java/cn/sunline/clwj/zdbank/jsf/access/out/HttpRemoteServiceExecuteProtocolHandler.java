package cn.sunline.clwj.zdbank.jsf.access.out;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.protocol.rest.constant.RestConstant;
import cn.sunline.adp.cedar.service.executor.ServiceExecutorContext;
import cn.sunline.adp.cedar.service.remote.config.RemoteServiceConfig;
import cn.sunline.adp.cedar.service.remote.controller.RemoteServiceController;
import cn.sunline.adp.cedar.service.remote.exception.RemoteTimeoutException;
import cn.sunline.adp.cedar.service.remote.protocol.AbstractRemoteServiceExecuteProtocolHandler;
import cn.sunline.adp.cedar.service.remote.protocol.RemoteServiceExecuteProtocolHandler;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.core.util.SpringUtils;
import cn.sunline.clwj.zdbank.jsf.access.out.conf.HttpConfig;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFConstant;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFUtil;
import cn.sunline.clwj.zdbank.jsf.ccess.spi.HttpRequestHeaderHandler;
import cn.sunline.edsp.base.factories.FactoriesLoader;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.lang.StringUtil;

public class HttpRemoteServiceExecuteProtocolHandler extends AbstractRemoteServiceExecuteProtocolHandler
implements RemoteServiceExecuteProtocolHandler{
	private static final SysLog log = SysLogUtil.getSysLog(HttpRemoteServiceExecuteProtocolHandler.class);

	public HttpRemoteServiceExecuteProtocolHandler(RemoteServiceConfig config) {
		super(config);
	}

	@Override
	public void callRemote(ServiceExecutorContext context, RemoteServiceController controller) throws RemoteTimeoutException {
		
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
				
		context.getServiceRequest().getRequestBody().setCommReq(commReq);
		
		HttpRequestHeaderHandler headerHandler = FactoriesLoader.getNewestFactory(HttpRequestHeaderHandler.class);
		if(headerHandler != null) {
			Map<String,Object> head = headerHandler.processHeader(context);		
			context.getServiceRequest().getRequestBody().getCommReq().putAll(head);
		}

		
		HttpPost request = this.buildRequest(context,controller);
		try {
			HttpResponse  response = this.invoker(request);
			this.handlerResult(response, context);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	
	// 构造请求对象
	private HttpPost buildRequest(ServiceExecutorContext context, RemoteServiceController controller) {
		
		HttpConfig config = SpringUtils.getBean(HttpConfig.class);
		
		String url = config.getUrl()+"/"+controller.getOutServiceVisitIdentity().getServiceGroup()+"/"+controller.getOutServiceVisitIdentity().getServiceApp()+"/"+controller.getOutServiceVisitIdentity().getServiceCode();
		log.info("========外调网关请求url=======[%s]",url);
		HttpPost post = new HttpPost(url);

		Map<String,Object> request = new HashMap<>();

		// 清理内部类型
		String reqStr = JsonUtil.format(context.getServiceRequest().getRequestBody().getData());
		DataArea req = DataArea.buildWithData(JsonUtil.parse(reqStr));

		JSFUtil.setHeader(request, req.getSystem());
		JSFUtil.setHeader(request, req.getCommReq());
		JSFUtil.setBody(request, req.getInput());
		
		String str = JsonUtil.format(request);
		log.info("========外调网关请求报文=======[%s]",str);
		 // 请求业务参数
        StringEntity reqEntity = new  StringEntity(str, "utf-8");          
         
         // http请求控制参数 
        RequestConfig requestConfig = RequestConfig.custom()  
                 .setConnectTimeout(controller.getTimeout() != 0 ? controller.getTimeout() : 5000 )//一、连接超时：connectionTimeout-->指的是连接一个url的连接等待时间  
                 .setSocketTimeout(controller.getTimeout() != 0 ? controller.getTimeout() : 5000 )// 二、读取数据超时：SocketTimeout-->指的是连接上一个url，获取response的返回等待时间  
                 .setConnectionRequestTimeout(controller.getTimeout() != 0 ? controller.getTimeout() : 5000 )  
                 .build();                  
		// 构建
        post.addHeader(JSFConstant.CHARSET, "UTF-8");
        post.addHeader(JSFConstant.CONTENT_TYPE, "application/json");
        post.addHeader(JSFConstant.GW_APP_ID, controller.getOutServiceVisitIdentity().getServiceVersion());
        post.addHeader(JSFConstant.GW_REQUEST_TIME, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
         
        post.setEntity(reqEntity);  
        post.setConfig(requestConfig);
         
		return post; 
	}
	
	private HttpResponse invoker(HttpPost post) throws Exception {
		return HttpClients.createDefault().execute(post);  
	}

	private void handlerResult(HttpResponse response,ServiceExecutorContext context) throws Exception {

		HttpEntity resEntity = response.getEntity(); 
        String message = EntityUtils.toString(resEntity, "utf-8");
        if(response.getStatusLine().getStatusCode() != 200) {
			ExceptionUtil.wrapThrow(message);
		}
       
    	log.info("========网关响应报文=======[%s]",message);
		//格式化响应报文
		Map<String, Object> res =JsonUtil.parse(message);
		
		DataArea retDataArea = DataArea.buildWithEmpty();
		
		Map<String, Object> m = JSFUtil.getBody(res);
		retDataArea.getOutput().putAll((Map<? extends String, ? extends Object>) JSFUtil.getBody(res).get(JSFConstant.RETDATA));
		
		retDataArea.getOutput().put(JSFConstant.RETCODE, m.get(JSFConstant.RETCODE));
		retDataArea.getOutput().put(JSFConstant.RETMSG, m.get(JSFConstant.RETMSG));
		
		retDataArea.getCommRes().putAll(JSFUtil.getHeader(res));
		
		context.getServiceResponse().setRespStr(JsonUtil.format(retDataArea)); // 设置原始响应字符串
		context.getServiceResponse().setResponseData(retDataArea); // 设置原始响应内容
		
		if(StringUtil.isEmpty(JSFUtil.getHeader(res).get(HeaderDataConstants.NAME_ERORCD))
				|| StringUtil.isEmpty(JSFUtil.getHeader(res).get(HeaderDataConstants.NAME_ERORTX))
				|| StringUtil.isEmpty(JSFUtil.getHeader(res).get(HeaderDataConstants.NAME_RET_STATUS))) {
			Map<String,Object> body = JSFUtil.getBody(res);
			
			Map<String,Object> head = JSFUtil.getHeader(res);
			head.put(HeaderDataConstants.NAME_ERORCD, body.get(JSFConstant.RETCODE));
			head.put(HeaderDataConstants.NAME_ERORTX, body.get(JSFConstant.RETMSG));
			head.put(HeaderDataConstants.NAME_RET_STATUS, body.get(JSFConstant.RETFLAG));
			
			JSFUtil.setHeader(res, head);
		}
		
		
		context.getServiceResponse().getResponseHeader().putAll(JSFUtil.getHeader(res));
		log.info("报文接出Body信息-->[RES][%s]",context.getServiceResponse().getResponseData().getData());

	}
	
}
