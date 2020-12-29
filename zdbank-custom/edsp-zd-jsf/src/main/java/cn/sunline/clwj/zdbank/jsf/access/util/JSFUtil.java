package cn.sunline.clwj.zdbank.jsf.access.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;

import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.core.util.JsonUtil;

public class JSFUtil {

	private static final String INTERFACE_PREFIX = "S";
	private static final String METHOD_PREFIX = "m";

	public static String getInterfaceId(String apiPackage, String serviceCode) {
		if (serviceCode.contains(".")) {
			return apiPackage + "." + serviceCode.split("\\.")[0];
		}
		return apiPackage + "." + INTERFACE_PREFIX + serviceCode;
	}

	public static String getMethodName(String serviceCode) {
		if (serviceCode.contains(".")) {
			return serviceCode.split("\\.")[1];
		}
		return METHOD_PREFIX + serviceCode;
	}

	public static Class<?> classForName(String className) {
		try {
			return Class.forName(className, true, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException var2) {
			throw new IllegalArgumentException(var2);
		}
	}

	public static Map<String, String> toMapString(Map<String, Object> map) {
		Map<String, String> retMap = new HashMap<>();
		Set<Entry<String, Object>> entrySet = map.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			// 处理RPC的HTTP协议外调时，HEAD中值为空对象时抛出异常的问题
			String value = entry.getValue() == null ? "" : (String) entry.getValue();
			retMap.put(entry.getKey(), value);
		}
		return retMap;
	}
	
	public static Map<String,Object> getHeader(Map<String,Object> map){
		if(map == null) return null;
		Map<String, Object> rest = (Map<String, Object>) map.get(JSFConstant.HEADER);
		return rest != null ? rest : new HashMap<>();
	}
	
	public static void setHeader(Map<String,Object> src,Map<String,Object> dest){
		Map<String,Object> head = getHeader(src);
		head.putAll(dest);
		src.put(JSFConstant.HEADER,head);
	}
	
	public static Map<String,Object> getBody(Map<String,Object> map){
		if(map == null) return null;
		Map<String, Object> rest = (Map<String, Object>) map.get(JSFConstant.BODY);
		return rest != null ? rest : new HashMap<>();
	}
	
	public static void setBody(Map<String,Object> src,Map<String,Object> dest){		
		src.put(JSFConstant.BODY,dest);
	}
	
	public static Map<String,Object> getCommData(Map<String,Object> map){
		if(map == null) return null;
		Map<String, Object> rest = (Map<String, Object>) map.get(JSFConstant.COMMDATA);
		return rest != null ? JsonUtil.toMap(rest) : new HashMap<>();
	}
	
	public static void setCommData(Map<String,Object> src,Map<String,Object> dest){		
		 src.put(JSFConstant.COMMDATA,dest);
	}
	
	public static void removeMappingFiled(Map<String,Object> map) {
		Map<String,Object> header = getHeader(map);
		header.remove(JSFConstant.INPUCD);
		header.remove(JSFConstant.INPUDT);
		header.remove(JSFConstant.TRANBR);
		header.remove(HeaderDataConstants.BUSI_SEQ_NO);
		header.remove(HeaderDataConstants.CALL_SEQ_NO);
		setHeader(map, header);
	}
	
	
}
