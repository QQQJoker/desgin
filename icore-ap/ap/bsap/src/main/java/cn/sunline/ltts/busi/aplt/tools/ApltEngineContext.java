package cn.sunline.ltts.busi.aplt.tools;

import java.util.HashMap;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;

/**
 * 功能:应用平台上下文
 * <p>因为原技术平台上下文EngineConetx.java是整个平台的交易上下文，主要技术平台内部使用.
 * <p>考虑到热点账户等功能，给应用平台开放，但发现存在使用不当导致交易运行失败的风险，故增加本类。
 * 
 * <p>防止应用平台使用技术平台级缓存EngineConetxt过程中进行清理等不合理操作
 * <p>统一使用应用平台级交易上下文
 * 
 * 2016-5-30
 *
 */
public class ApltEngineContext {
	private static final String _APLT_TXN_CACHE_KEY = "_APLT_TXN_CACHE_KEY";
	
	@SuppressWarnings("unchecked")
	private static Map<String,Object> getEngineMap() {
		
		Map<String,Object> txnMap = EngineContext.getTxnTempObjMap();
		if( !txnMap.containsKey(_APLT_TXN_CACHE_KEY) )
			txnMap.put(_APLT_TXN_CACHE_KEY, new HashMap<String,Object>());
		
		return (Map<String,Object>)txnMap.get(_APLT_TXN_CACHE_KEY);
	}

    public static Map<String,Object> getTxnTempObjMap(){
        return getEngineMap();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getTxnTempObj(String key){
        return (T)getEngineMap().get(key);
    }
    
    public static <T> void setTxnTempObj(String key, T value) {
    	getEngineMap().put(key, value);
    }
    
    public static void clear() {
    	getEngineMap().clear();
    }
}
