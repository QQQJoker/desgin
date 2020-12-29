package cn.sunline.clwj.zdbank.cds.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.ltts.gns.api.GnsKey;

/**
 * 功能:路由映射上下文
 * <p>因为原技术平台上下文EngineConetx.java是整个平台的交易上下文，主要技术平台内部使用.
 * <p>考虑到热点账户等功能，给应用平台开放，但发现存在使用不当导致交易运行失败的风险，故增加本类。
 * 
 * <p>防止应用平台使用技术平台级缓存EngineConetxt过程中进行清理等不合理操作
 * <p>统一使用应用平台级交易上下文
 * 
 * 2020-10-10
 *
 */
public class GnsEngineContext {
	private static final String _GNS_TXN_CACHE_KEY = "_GNS_TXN_CACHE_KEY";
	
	@SuppressWarnings("unchecked")
	private static List<GnsKey> getEngineMap() {
		
		Map<String,Object> txnMap = EngineContext.getTxnTempObjMap();
		if( !txnMap.containsKey(_GNS_TXN_CACHE_KEY) )
			txnMap.put(_GNS_TXN_CACHE_KEY, new ArrayList<GnsKey>());
		
		return (List<GnsKey>) txnMap.get(_GNS_TXN_CACHE_KEY);
	}

    public static List<GnsKey> getTxnTempObjMap(){
        return getEngineMap();
    }

    public static  void addTxnTempObj(GnsKey key){
        getEngineMap().add(key);
    }
        
    public static void clear() {
    	getEngineMap().clear();
    }
}
