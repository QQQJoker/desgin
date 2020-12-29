package cn.sunline.clwj.zdbank.fmq.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;

/**
 * 功能:消息上下文
 * <p>因为原技术平台上下文EngineConetx.java是整个平台的交易上下文，主要技术平台内部使用.
 * <p>防止应用平台使用技术平台级缓存EngineConetxt过程中进行清理等不合理操作
 * <p>统一使用应用平台级交易上下文
 * 2020-10-10
 *
 */
public class MessageEngineContext {
	private static final String _MSG_TXN_CACHE_KEY = "_MSG_TXN_CACHE_KEY";
	
	@SuppressWarnings("unchecked")
	private static List<Message> getEngineMap() {
		
		Map<String,Object> txnMap = EngineContext.getTxnTempObjMap();
		if( !txnMap.containsKey(_MSG_TXN_CACHE_KEY) )
			txnMap.put(_MSG_TXN_CACHE_KEY, new ArrayList<Message>());
		
		return (List<Message>) txnMap.get(_MSG_TXN_CACHE_KEY);
	}

    public static List<Message> getTxnTempObjMap(){
        return getEngineMap();
    }

    public static  void addTxnTempObj(Message key){
        getEngineMap().add(key);
    }
        
    public static void clear() {
    	getEngineMap().clear();
    }
}
