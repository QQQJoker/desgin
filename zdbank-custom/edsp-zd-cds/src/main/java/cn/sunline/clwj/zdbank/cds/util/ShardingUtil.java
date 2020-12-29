package cn.sunline.clwj.zdbank.cds.util;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jdjr.cds.driver.jdbc.CdsHelper;
import com.jdjr.cds.driver.rulebase.SplitTable;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.core.util.SpringUtils;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.adp.metadata.base.util.PropertyUtil;
import cn.sunline.clwj.zdbank.cds.config.RouteConfig;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.gns.api.GnsApi;
import cn.sunline.ltts.gns.api.GnsKey;
import cn.sunline.ltts.gns.api.GnsRes;

public class ShardingUtil {

	private static final SysLog logger = SysLogUtil.getSysLog(ShardingUtil.class);
	/**
	 * 根据路由key获取路由映射信息
	 * <p>key的格式：映射标识@映射类型@映射字段 </p>
	 * <p>案例：P@AC@acctno  </p>
	 * <p>注释：个人客户@账号@接口字段acctno  </p>
	 * @param routeKeys
	 * @param bizServiceId
	 * @param data
	 * @return
	 */
	public static String findShardingIdByRouteKeys(String routeKeys, String bizServiceId, DataArea data) {
		if(CDSConstants.ROUTE_TYPE.equals(routeKeys)) { // 操作孤立库 需要单独的路由值，用于判断是否跨库，控制分布式事务
			return CDSConstants.DEFAULT_SHARDING_ID;
		}
		String[] keys = routeKeys.split(CDSConstants.MULT_ROUTE_KEY_SPILT);;  // 多路由，只要有一个路由到了就返回。没有路由到就继续
		String value = null;
		for(String rout:keys) {
			 value = findShardingIdByroutekey(rout.trim(),bizServiceId,data);
			if(value != null){
				break;
			}
		}
		return value;
	}
	
	private static String findShardingIdByroutekey(String rout, String bizServiceId, DataArea data) {
		String[] route = rout.split(CDSConstants.ROUTE_KEY_SPILT);
		if (route.length != 3) {
			throw new IllegalArgumentException("路由映射key格式配置错误["+rout+"],格式：[type@key@filed],案例：[P@AC@acctno]");
		}
		
		Object cancel = PropertyUtil.createAccessor(data.getInput()).getNestedProperty(CDSConstants.INPUT_BODY);
		
		Object value = PropertyUtil.createAccessor(data.getInput()).getNestedProperty(route[2]);
		
		if(cancel != null) { // 如果input={input_body={}}有值说明是cancel的服务 
			value = PropertyUtil.createAccessor(JsonUtil.parse(cancel.toString())).getNestedProperty(route[2]);
		}
		
		logger.debug("路由映射健:[%s],值：[%s]",route[2],value !=null ? value : CoreUtil.getMainShardingId());
			
		if(CDSConstants.SHARDING_TYPE.equals(route[1])) { //首开户交易，服务,或者路由类型是客户号的就直接获取值返回
			return String.valueOf(value) != null ? String.valueOf(value) : CoreUtil.getMainShardingId();
		}
		
		Object channel = EngineContext.getRequestData().getCommReq().get(CDSConstants.CHANNEL_NO);
		
		GnsApi api = SpringUtils.getBean(GnsApi.class);
		GnsKey gnsKey = new GnsKey();
		gnsKey.setGnskey(String.valueOf(value)); // 路由映射要素
		gnsKey.setGnsopt(route[0].trim()); // 路由映射识别标志 个人用户(U)，对公用户(V)，个人客户(P)  对公客户(C)
		gnsKey.setGnstyp(route[1].trim()); // 路由映射类型 账号 卡号 等
		gnsKey.setGnschn(String.valueOf(channel)); // 路由映射渠道标识
		GnsRes res = api.query(gnsKey);
		logger.debug("路由结果值：[%s]",res.getGnsval());
		return res.getGnsval();
	}

	
	/**
	 * 判断是否开启了路由模式
	 * <p>adp.route.enabled=true</p>
	 * @return
	 */
	public static boolean isRouteMode() {
		RouteConfig config = getRouteConfig();
		if (StringUtil.isEmpty(config)) {
			return false;
		}		
		return true;
		
	}
	
	/**
	 * 获取路由配置，如果没配置抛出异常
	 * <p>adp.route.enabled=true</p>
	 * @return
	 */
	public static RouteConfig getRouteConfig(boolean exception) {
		RouteConfig config = getRouteConfig();
		if(config == null && exception) {
			throw new RuntimeException("路由定位配置未开启，请配置：adp.route.enabled=true！！！");
		}
		return config;
		
	}
	/**
	 * 获取路由配置，如果没有开启返回null
	 * <p>adp.route.enabled=true</p>
	 * @return
	 */
	public static RouteConfig getRouteConfig() {
		RouteConfig config = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager().getConfig(RouteConfig.class);
		return config;
		
	}
	
	/**
	 * 从交易上下文中获取带注册的路由映射信息进行注册登记（交易后处理）
	 * @author Joker
	 * @return int 
	 */
	public static int registGnsInfo() {
		
		if(GnsEngineContext.getTxnTempObjMap().isEmpty()) {
			return 0;
		}
		List<GnsKey> gnsKeys =  GnsEngineContext.getTxnTempObjMap();		
		GnsApi api = SpringUtils.getBean(GnsApi.class);		
		api.add(gnsKeys);
		
		return gnsKeys.size();
	}
	
	/**
	 * 路由映射信息添加到交易上下文，交易后处理统一进行落库处理
	 * @author Joker
	 * 2020-10-10
	 */
	public static void addGnsInfoToContext(GnsKey gsnKey) {
		GnsEngineContext.addTxnTempObj(gsnKey);
	}
	
	/**
	 * 查询集群下面所有的切分健以及对应的表
	 * @return
	 */
	public static Map<String, SplitTable> getSplitTable() {
		Map<String, SplitTable> sky = new HashMap<>();
		try {
			sky = CdsHelper.getSplitTable(getRouteConfig().getClusterId());
		} catch (SQLException e) {
			ExceptionUtil.wrapThrow("Get split table info error",e);
		}
		return sky;
	}

}
