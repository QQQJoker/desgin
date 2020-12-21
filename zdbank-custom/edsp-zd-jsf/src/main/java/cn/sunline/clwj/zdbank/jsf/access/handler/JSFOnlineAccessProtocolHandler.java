package cn.sunline.clwj.zdbank.jsf.access.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.sunline.adp.cedar.base.constant.ConstantValueManager;
import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.RequestHeaderData;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.protocol.rest.constant.RestConstant;
import cn.sunline.adp.cedar.server.online.config.OnlineServerConfig;
import cn.sunline.adp.cedar.server.online.protocol.AbstractOnlineAccessProtocolHandler;
import cn.sunline.adp.cedar.server.online.registry.InServiceRegistry;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFConstant;
import cn.sunline.clwj.zdbank.jsf.access.util.JSFUtil;
import cn.sunline.clwj.zdbank.jsf.access.util.StringUtils;
import cn.sunline.clwj.zdbank.jsf.constant.JSFPluginConstantDef.SPC_JSF;
import cn.sunline.edsp.base.util.lang.StringUtil;


public class JSFOnlineAccessProtocolHandler extends AbstractOnlineAccessProtocolHandler<Map<String,Object>, Map<String,Object>> {
	private static final SysLog log = SysLogUtil.getSysLog(JSFOnlineAccessProtocolHandler.class);
	public static final String PKG_TYPE = ConstantValueManager.get().getValue("pkgType", "pkgType");

	private OnlineServerConfig onlineServerConfig;

	public JSFOnlineAccessProtocolHandler(OnlineServerConfig onlineServerConfig, String defaultPkgType,
			String encoding) {
		super(onlineServerConfig, defaultPkgType, encoding);
		this.onlineServerConfig = onlineServerConfig;
	}

	@Override
	public String getServiceApplicationId(Map<String,Object> request) {
		return String.valueOf(JSFUtil.getHeader(request).get("application"));
	}

	@Override
	public String getServiceGroupId(Map<String,Object> request) {
		String serviceGroupId = String.valueOf(JSFUtil.getHeader(request).get("group"));
		return StringUtils.isBlank(serviceGroupId) ? onlineServerConfig.getServiceGroup() : serviceGroupId;
	}

	@Override
	public String getServiceVersion(Map<String,Object> request) {
		return onlineServerConfig.getServiceVersion();
	}

	@Override
	public Map<String,Object> processException(RequestData request, ResponseData response, Throwable e) {
		if (log.isInfoEnabled()) {
			// log.info("报文接出Head信息--->[RES][" + JsonUtil.format(response.getHeaderData()) +
			// "]");
			log.info(SPC_JSF.C021(), JsonUtil.format(response.getHeaderData()));
			// log.info("报文接出信息--->[RES][%s]", rpcBodyStr);
			log.info(SPC_JSF.C022(), "为空对象");
		}

		Map<String,Object> jsfResult = new HashMap<String,Object>();
		JSFUtil.setHeader(jsfResult,toMapString(response.getHeaderData()));

		return jsfResult;
	}

	@Override
	public RequestData readBodyMessage(InServiceRegistry inServiceRegistry, RequestHeaderData requestHeader,
			Map<String,Object> request) {
		if(!JSFUtil.getCommData(request).isEmpty()) {
			return readBodyMessageWithCommData(inServiceRegistry, requestHeader, request);
		}else {
			return readBodyMessageWithNoCommData(inServiceRegistry, requestHeader, request);
		}
	}

	@Override
	public RequestHeaderData readHeaderMessage(Map<String,Object> request) {
		if(!JSFUtil.getCommData(request).isEmpty()) {
			return	readHeaderMessageWithCommData(request);
		}else {
			return	readHeaderMessageWithNoCommData(request);
		}
	}

	@Override
	public Map<String,Object> writeMessage(InServiceRegistry inServiceRegistry, RequestData request, ResponseData response) {
		if(request.getRequestHeader().get(JSFConstant.GLB_SEQ_NO) != null) {
			return writeMessageWithNoCommData(inServiceRegistry, request, response);
		}else {
			return writeMessageWithCommData(inServiceRegistry, request, response);
		}
		
	}

	private Map<String, Object> toMapString(Map<String, Object> map) {
		Map<String, Object> retMap = new HashMap<>();
		if(map==null) {
			return retMap;
		}
		map.remove("profile");
		Set<Entry<String, Object>> entrySet = map.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			retMap.put(entry.getKey(), (String) entry.getValue());
		}
		return retMap;
	}
	
	private RequestHeaderData readHeaderMessageWithCommData(Map<String,Object> request) {
		Optional.ofNullable(JSFUtil.getCommData(request).get(RestConstant.BUSI_SEQ_NO)).orElseThrow(()->new RuntimeException("Parameters 'busi_seq' Can't Be Empty"));
		Optional.ofNullable(JSFUtil.getCommData(request).get(RestConstant.CALL_SEQ_NO)).orElseThrow(()->new RuntimeException("Parameters 'call_seq' Can't Be Empty"));
		
		// 联机交易请求系统头
		RequestHeaderData onlineRequestHeader = new RequestHeaderData();
		
		onlineRequestHeader.setBusiSeqNo(String.valueOf(JSFUtil.getCommData(request).get(RestConstant.BUSI_SEQ_NO)));
		onlineRequestHeader.setCallSeqNo(String.valueOf(JSFUtil.getCommData(request).get(RestConstant.CALL_SEQ_NO)));
		
		Object param = JSFUtil.getHeader(request).get("service");
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setServiceCode(String.valueOf(param));
		}
		
		param = JSFUtil.getCommData(request).get(HeaderDataConstants.IN_SERVICE_MARK);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setInServiceMark(String.valueOf(param));
		}
		
		param = JSFUtil.getCommData(request).get(HeaderDataConstants.USER_LANG);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setUserLang(String.valueOf(param));
		}
		
		param = JSFUtil.getCommData(request).get(HeaderDataConstants.IN_SERVICE_MARK);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setInServiceMark(String.valueOf(param));
		}
		
		param = JSFUtil.getCommData(request).get(HeaderDataConstants.CONSUMER_SYS_ID);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setConsumerSysId(String.valueOf(param));
		}
		
		param = JSFUtil.getCommData(request).get(HeaderDataConstants.ORIG_SYS_ID);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setOrigSysId(String.valueOf(param));
		}
		
		param = JSFUtil.getCommData(request).get(HeaderDataConstants.ORIG_SRV_ID);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setOrigSrvId(String.valueOf(param));
		}
		
		param = JSFUtil.getCommData(request).get(HeaderDataConstants.IN_SERVICE_MARK);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setTranTimestamp(String.valueOf(param));
		}
		
		param = JSFUtil.getCommData(request).get(HeaderDataConstants.REVERSAL_SEQ_NO);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setReversalSeqNo(String.valueOf(param));
		}
		
		param = JSFUtil.getCommData(request).get(HeaderDataConstants.GLOBAL_TRAN_SEQ_NO);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setGlobalTranSeqNo(String.valueOf(param));
		}
		
		return onlineRequestHeader;
	}
	
	private RequestHeaderData readHeaderMessageWithNoCommData(Map<String,Object> request) {
		
		Optional.ofNullable(JSFUtil.getHeader(request).get(JSFConstant.GLB_SEQ_NO)).orElseThrow(()->new RuntimeException("Parameters 'glbSeqno' Can't Be Empty"));
		Optional.ofNullable(JSFUtil.getHeader(request).get(JSFConstant.SYS_SEQ_NO)).orElseThrow(()->new RuntimeException("Parameters 'sysSeqno' Can't Be Empty"));
		
		// 联机交易请求系统头
		RequestHeaderData onlineRequestHeader = new RequestHeaderData();
		
		// 全局业务流水
		onlineRequestHeader.setBusiSeqNo(String.valueOf(JSFUtil.getHeader(request).get(JSFConstant.GLB_SEQ_NO)));
		//系统调用流水
		onlineRequestHeader.setCallSeqNo(String.valueOf(JSFUtil.getHeader(request).get(JSFConstant.SYS_SEQ_NO)));
		
		Object param = JSFUtil.getHeader(request).get("service");
		
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setServiceCode(String.valueOf(param));
		}
		
		// 交易码
		param = JSFUtil.getHeader(request).get(JSFConstant.TRAN_CODE);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setServiceCode(String.valueOf(param));
		}
		
		// 判断是内部TSP服务的标识
		param = JSFUtil.getHeader(request).get(HeaderDataConstants.IN_SERVICE_MARK);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setInServiceMark(String.valueOf(param));
		}
		
		//用户语言
		param = JSFUtil.getHeader(request).get(HeaderDataConstants.USER_LANG);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setUserLang(String.valueOf(param));
		}
		
		//消费者系统编号
		param = JSFUtil.getHeader(request).get(HeaderDataConstants.CONSUMER_SYS_ID);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setConsumerSysId(String.valueOf(param));
		}
		//原始发起方系统编号
		param = JSFUtil.getHeader(request).get(JSFConstant.SOURCE_SYSID);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setOrigSysId(String.valueOf(param));
			onlineRequestHeader.getMap().put(JSFConstant.INPUCD, String.valueOf(param));
		}
		
		
		//原始发起方服务器标识（服务器名称或者IP）
		param = JSFUtil.getHeader(request).get(JSFConstant.WSLD);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setOrigSrvId(String.valueOf(param));
		}		
		
		//交易机构
		param = JSFUtil.getHeader(request).get(JSFConstant.BRANCH_ID);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.getMap().put(JSFConstant.TRANBR, String.valueOf(param));
		}
		
		//交易渠道
		param = JSFUtil.getHeader(request).get(JSFConstant.SOURCE_TYPE);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.getMap().put(JSFConstant.SERVNO, String.valueOf(param));
		}		
		
		//交易日期
		param = JSFUtil.getHeader(request).get(JSFConstant.SYS_DATE);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.getMap().put(JSFConstant.INPUDT, String.valueOf(param));
		}
		
		//交易时间
		param = JSFUtil.getHeader(request).get(HeaderDataConstants.TRAN_TIMESTAMP);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setTranTimestamp(String.valueOf(param));
		}
		// 需要冲正的原始流水号
		param = JSFUtil.getHeader(request).get(HeaderDataConstants.REVERSAL_SEQ_NO);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setReversalSeqNo(String.valueOf(param));
		}
		//全局事务流水号 
		param = JSFUtil.getHeader(request).get(HeaderDataConstants.GLOBAL_TRAN_SEQ_NO);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setGlobalTranSeqNo(String.valueOf(param));
		}
		
		onlineRequestHeader.getMap().putAll(JSFUtil.getHeader(request));  
		
		return onlineRequestHeader;
	}
	
	private RequestData readBodyMessageWithCommData(InServiceRegistry inServiceRegistry, RequestHeaderData requestHeader,
			Map<String,Object> request) {
		String rpcBodyStr = "";
		DataArea reqDataArea;
		if (StringUtils.isNotEmpty(JSFUtil.getBody(request))) {
			reqDataArea = DataArea.buildWithInput(JSFUtil.getBody(request));
			reqDataArea.setHeader(JSFUtil.getHeader(request));
			reqDataArea.setCommReq(JSFUtil.getCommData(request));
			log.info("报文接入信息--->[REQ][%s]", reqDataArea.getData());
		} else {
			reqDataArea = DataArea.buildWithInput(new HashMap<String, Object>());
			reqDataArea.setHeader(JSFUtil.getHeader(request));
			reqDataArea.setCommReq(JSFUtil.getCommData(request));
			log.warn(SPC_JSF.C007(), "为空");
		}

		return new RequestData(rpcBodyStr, requestHeader, reqDataArea);
		
	}
	
	private RequestData readBodyMessageWithNoCommData(InServiceRegistry inServiceRegistry, RequestHeaderData requestHeader,
			Map<String,Object> request) {
		String rpcBodyStr = "";
		DataArea reqDataArea;
		if (StringUtils.isNotEmpty(JSFUtil.getBody(request))) {
			reqDataArea = DataArea.buildWithInput(JSFUtil.getBody(request));
			reqDataArea.setHeader(requestHeader);
			reqDataArea.setCommReq(requestHeader);
			log.info("报文接入信息--->[REQ][%s]", reqDataArea.getData());
		} else {
			reqDataArea = DataArea.buildWithInput(new HashMap<String, Object>());
			reqDataArea.setHeader(requestHeader);
			reqDataArea.setCommReq(requestHeader);
			log.warn(SPC_JSF.C007(), "为空");
		}

		return new RequestData(rpcBodyStr, requestHeader, reqDataArea);
		
	}
	
	private Map<String,Object> writeMessageWithCommData(InServiceRegistry inServiceRegistry, RequestData request, ResponseData response) {
        // 返回的对象不能复用传进来的参数
		Map<String,Object> jsfResult = new HashMap<String,Object>();
		response.getBody().getSystem().putAll(response.getHeaderData());
		log.info("报文接出Body信息--->[RES][" + this.pkgDataToString(response.getBody()) + "]");
		String resStr = JsonUtil.format(response.getBody());
		DataArea res = DataArea.buildWithData(JsonUtil.parse(resStr));

		if (log.isInfoEnabled()) {
			// log.info("报文接出Body信息--->[RES][" + this.pkgDataToString(responsePkg) + "]");
			log.info(SPC_JSF.C022(), res);
		}
		
		JSFUtil.setHeader(jsfResult, toMapString((Map)res.getData().get(JSFConstant.SYSTEM)));
		JSFUtil.setCommData(jsfResult,(Map)res.getData().get(JSFConstant.COMMRES));
		JSFUtil.setBody(jsfResult,res.getOutput());
		
		return jsfResult;
	}
	
	private Map<String,Object> writeMessageWithNoCommData(InServiceRegistry inServiceRegistry, RequestData request, ResponseData response) {
		 // 返回的对象不能复用传进来的参数
		Map<String,Object> jsfResult = new HashMap<String,Object>();
		jsfResult.put(JSFConstant.BODY, new HashMap<>());
		jsfResult.put(JSFConstant.HEADER, new HashMap<>());
		
		response.getBody().getSystem().putAll(response.getHeaderData());
		
		JSFUtil.setHeader(jsfResult, response.getBody().getCommRes());
		JSFUtil.setHeader(jsfResult, response.getHeaderData());

		JSFUtil.removeMappingFiled(jsfResult);
		
		Map<String,Object> body = JSFUtil.getBody(jsfResult);
		
		body.put(JSFConstant.RETFLAG, response.getHeaderData().get(JSFConstant.STATUS));
		body.put(JSFConstant.RETCODE, response.getHeaderData().get(JSFConstant.ERORCD));
		body.put(JSFConstant.RETMSG, response.getHeaderData().get(JSFConstant.ERORTX));
		body.put(JSFConstant.RETDATA,response.getBody().getOutput());
		
		JSFUtil.setBody(jsfResult, body);
		log.info(SPC_JSF.C022(), jsfResult);
		
		String result = JsonUtil.format(jsfResult);		
		log.info(SPC_JSF.C022(), result);
		
		jsfResult = JsonUtil.parseEntity(result, Map.class);
		log.info(SPC_JSF.C022(), jsfResult);
				
		return jsfResult;
	}
	
	
}
