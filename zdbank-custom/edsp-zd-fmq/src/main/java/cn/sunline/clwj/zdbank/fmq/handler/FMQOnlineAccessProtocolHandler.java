package cn.sunline.clwj.zdbank.fmq.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import cn.sunline.adp.cedar.base.constant.ConstantValueManager;
import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.RequestHeaderData;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.server.online.config.OnlineServerConfig;
import cn.sunline.adp.cedar.server.online.protocol.AbstractOnlineAccessProtocolHandler;
import cn.sunline.adp.cedar.server.online.registry.InServiceRegistry;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.clwj.zdbank.fmq.util.FMQConstant;
import cn.sunline.clwj.zdbank.fmq.util.FmqUtil;
import cn.sunline.clwj.zdbank.fmq.util.StringUtils;
import cn.sunline.edsp.base.util.lang.StringUtil;

public class FMQOnlineAccessProtocolHandler extends AbstractOnlineAccessProtocolHandler<Map<String,Object>, Map<String,Object>> {

	private static final SysLog log = SysLogUtil.getSysLog(FMQOnlineAccessProtocolHandler.class);
	public static final String PKG_TYPE = ConstantValueManager.get().getValue("pkgType", "pkgType");

	private OnlineServerConfig onlineServerConfig;

	public FMQOnlineAccessProtocolHandler(OnlineServerConfig onlineServerConfig, String defaultPkgType,
			String encoding) {
		super(onlineServerConfig, defaultPkgType, encoding);
		this.onlineServerConfig = onlineServerConfig;
	}

	@Override
	public String getServiceApplicationId(Map<String,Object> request) {
		return String.valueOf(FmqUtil.getHeader(request).get("application"));
	}

	@Override
	public String getServiceGroupId(Map<String,Object> request) {
		String serviceGroupId = String.valueOf(FmqUtil.getHeader(request).get("group"));
		return StringUtils.isBlank(serviceGroupId) ? onlineServerConfig.getServiceGroup() : serviceGroupId;
	}

	@Override
	public String getServiceVersion(Map<String,Object> request) {
		return onlineServerConfig.getServiceVersion();
	}

	@Override
	public Map<String,Object> processException(RequestData request, ResponseData response, Throwable e) {
		if (log.isInfoEnabled()) {
			log.info("报文接出Head信息--->[RES][" + JsonUtil.format(response.getHeaderData()) +"]");
		}

		Map<String,Object> jsfResult = new HashMap<String,Object>();
		FmqUtil.setHeader(jsfResult,toMapString(response.getHeaderData()));

		return jsfResult;
	}

	@Override
	public RequestData readBodyMessage(InServiceRegistry inServiceRegistry, RequestHeaderData requestHeader,
			Map<String,Object> request) {
		String rpcBodyStr = "";
		DataArea reqDataArea;
		if (StringUtils.isNotEmpty(FmqUtil.getBody(request))) {
			reqDataArea = DataArea.buildWithInput(FmqUtil.getBody(request));
			reqDataArea.setHeader(requestHeader);
			reqDataArea.setCommReq(requestHeader);
			log.info("报文接入信息--->[REQ][%s]", reqDataArea.getData());
		} else {
			reqDataArea = DataArea.buildWithInput(new HashMap<String, Object>());
			reqDataArea.setHeader(requestHeader);
			reqDataArea.setCommReq(requestHeader);
			log.warn("报文接入--->[REQ][%s]", "为空");
		}

		return new RequestData(rpcBodyStr, requestHeader, reqDataArea);
	}

	@Override
	public RequestHeaderData readHeaderMessage(Map<String,Object> request) {
		Optional.ofNullable(FmqUtil.getHeader(request).get(FMQConstant.GLB_SEQ_NO)).orElseThrow(()->new RuntimeException("Parameters 'glbSeqno' Can't Be Empty"));
		Optional.ofNullable(FmqUtil.getHeader(request).get(FMQConstant.SYS_SEQ_NO)).orElseThrow(()->new RuntimeException("Parameters 'sysSeqno' Can't Be Empty"));
		
		// 联机交易请求系统头
		RequestHeaderData onlineRequestHeader = new RequestHeaderData();
		
		// 全局业务流水
		onlineRequestHeader.setBusiSeqNo(String.valueOf(FmqUtil.getHeader(request).get(FMQConstant.GLB_SEQ_NO)));
		//系统调用流水
		onlineRequestHeader.setCallSeqNo(String.valueOf(FmqUtil.getHeader(request).get(FMQConstant.SYS_SEQ_NO)));
		
		Object param = FmqUtil.getHeader(request).get("service");
		
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setServiceCode(String.valueOf(param));
		}
		
		// 交易码
		param = FmqUtil.getHeader(request).get(FMQConstant.TRAN_CODE);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setServiceCode(String.valueOf(param));
		}
		
		// 判断是内部TSP服务的标识
		param = FmqUtil.getHeader(request).get(HeaderDataConstants.IN_SERVICE_MARK);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setInServiceMark(String.valueOf(param));
		}
		
		//用户语言
		param = FmqUtil.getHeader(request).get(HeaderDataConstants.USER_LANG);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setUserLang(String.valueOf(param));
		}
		
		//消费者系统编号
		param = FmqUtil.getHeader(request).get(HeaderDataConstants.CONSUMER_SYS_ID);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setConsumerSysId(String.valueOf(param));
		}
		//原始发起方系统编号
		param = FmqUtil.getHeader(request).get(FMQConstant.SOURCE_SYSID);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setOrigSysId(String.valueOf(param));
			onlineRequestHeader.getMap().put(FMQConstant.INPUCD, String.valueOf(param));
		}
		
		
		//原始发起方服务器标识（服务器名称或者IP）
		param = FmqUtil.getHeader(request).get(FMQConstant.WSLD);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setOrigSrvId(String.valueOf(param));
		}		
		
		//交易机构
		param = FmqUtil.getHeader(request).get(FMQConstant.BRANCH_ID);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.getMap().put(FMQConstant.TRANBR, String.valueOf(param));
		}		
		
		//交易日期
		param = FmqUtil.getHeader(request).get(FMQConstant.SYS_DATE);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.getMap().put(FMQConstant.INPUDT, String.valueOf(param));
		}
		
		//交易时间
		param = FmqUtil.getHeader(request).get(HeaderDataConstants.TRAN_TIMESTAMP);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setTranTimestamp(String.valueOf(param));
		}
		// 需要冲正的原始流水号
		param = FmqUtil.getHeader(request).get(HeaderDataConstants.REVERSAL_SEQ_NO);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setReversalSeqNo(String.valueOf(param));
		}
		//全局事务流水号 
		param = FmqUtil.getHeader(request).get(HeaderDataConstants.GLOBAL_TRAN_SEQ_NO);
		if(StringUtil.isNotEmpty(param)) {
			onlineRequestHeader.setGlobalTranSeqNo(String.valueOf(param));
		}
		
		onlineRequestHeader.getMap().putAll(FmqUtil.getHeader(request));  
		
		return onlineRequestHeader;
	}

	@Override
	public Map<String,Object> writeMessage(InServiceRegistry inServiceRegistry, RequestData request, ResponseData response) {
		 // 返回的对象不能复用传进来的参数
		Map<String,Object> jsfResult = new HashMap<String,Object>();
		jsfResult.put(FMQConstant.BODY, new HashMap<>());
		jsfResult.put(FMQConstant.HEADER, new HashMap<>());
		
		response.getBody().getSystem().putAll(response.getHeaderData());
		
		FmqUtil.setHeader(jsfResult, response.getBody().getCommRes());
		FmqUtil.setHeader(jsfResult, response.getHeaderData());

		FmqUtil.removeMappingFiled(jsfResult);
		
		Map<String,Object> body = FmqUtil.getBody(jsfResult);
		
		body.put(FMQConstant.RETFLAG, response.getHeaderData().get(FMQConstant.STATUS));
		body.put(FMQConstant.RETCODE, response.getHeaderData().get(FMQConstant.ERORCD));
		body.put(FMQConstant.RETMSG, response.getHeaderData().get(FMQConstant.ERORTX));
		body.put(FMQConstant.RETDATA,response.getBody().getOutput());
		
		FmqUtil.setBody(jsfResult, body);
		log.info("报文接出Body信息--->[RES][%s]",jsfResult);
		
		String result = JsonUtil.format(jsfResult);		
		log.info("报文接出Body信息--->[RES][%s]",result);
		
		jsfResult = JsonUtil.parseEntity(result, Map.class);
		log.info("报文接出Body信息--->[RES][%s]",jsfResult);
				
		return jsfResult;
		
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
	
}
