package cn.sunline.clwj.zdbank.cds.spi;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.engine.online.handler.OETBeforeHandler;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerConstant;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerContext;
import cn.sunline.clwj.zdbank.cds.util.CDSConstants;
import cn.sunline.clwj.zdbank.cds.util.ShardingUtil;
import cn.sunline.edsp.base.annotation.Groups;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.edsp.microcore.spi.SPIMeta;


@SPIMeta(id = OETInitShardingBeforeHandler.SPI_ID)
@Order(1000)
@Groups({ OETHandlerConstant.FLOW_ENGINE_TYPE,OETHandlerConstant.SERVICE_ENGINE_TYPE})
public class OETInitShardingBeforeHandler implements OETBeforeHandler{

	private static final SysLog logger = SysLogUtil.getSysLog(OETInitShardingBeforeHandler.class);
	public static final String SPI_ID = "init_sharding";

	@Override
	public void handler(OETHandlerContext context) {
		String routeKeys = context.getInServiceController().getDbRouteKey();
		String serviceCode = context.getInServiceController().getInnerServiceCode();
		if(StringUtil.isBlank(routeKeys)){ // 开启了分布式路由，没有配置路由key就直接返回
			logger.warn("交易[%s]前处理路由 路由关键字[%s]为空，不做路由！！！", serviceCode, routeKeys);
			if(ShardingUtil.getRouteConfig() == null) {
				CoreUtil.setCurrentShardingId(CDSConstants.DEFAULT_SHARDING_ID);
			}
			return;
		}
		String shardingId = ShardingUtil.findShardingIdByRouteKeys(routeKeys, serviceCode, context.getRequest().getBody());
/*		if (StringUtil.isBlank(shardingId))
			throw new RuntimeException("请求接入路由失败，未返回分片信息["+shardingId+"]");*/
		logger.info("交易[%s]前处理路由定位到指定分片[%s]", serviceCode, shardingId);
		CoreUtil.setMainShardingId(shardingId);
		CoreUtil.setCurrentShardingId(shardingId);
		
	}

}
