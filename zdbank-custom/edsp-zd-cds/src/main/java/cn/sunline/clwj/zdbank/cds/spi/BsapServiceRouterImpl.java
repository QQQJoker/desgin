package cn.sunline.clwj.zdbank.cds.spi;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.engine.service.ServiceRequest;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.service.controller.ServiceController;
import cn.sunline.adp.cedar.service.controller.ServiceExecutorType;
import cn.sunline.adp.cedar.service.remote.controller.RemoteServiceController;
import cn.sunline.adp.cedar.service.remote.registry.RemoteServiceRegistryManager;
import cn.sunline.adp.cedar.service.router.ServiceRouteResult;
import cn.sunline.adp.cedar.service.router.ServiceRouter;
import cn.sunline.clwj.zdbank.cds.util.CDSConstants;
import cn.sunline.clwj.zdbank.cds.util.ShardingUtil;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.edsp.base.util.lang.StringUtil;

@SPIMeta(id = BsapServiceRouterImpl.SPI_ID)
@Order(1000)
public class BsapServiceRouterImpl implements ServiceRouter {

	private static final SysLog logger = SysLogUtil.getSysLog(BsapServiceRouterImpl.class);

	public static final String SPI_ID = "bsap_route_impl";

	@Override
	public ServiceRouteResult route(ServiceRequest request, ServiceController serviceControler) {
		
		if (StringUtil.isEmpty(serviceControler.getRouteKeys())) {
			if(ShardingUtil.getRouteConfig() == null) {
				CoreUtil.setCurrentShardingId(CDSConstants.DEFAULT_SHARDING_ID);
			}
			return ServiceRouteResult.NULL_ROUTE_RESULT;
		}
		
		CoreUtil.setCurrentShardingId(null);
		String shardingId = ShardingUtil.findShardingIdByRouteKeys(serviceControler.getRouteKeys(),
				serviceControler.getInnerServiceCode(), request.getRequestBody());

		ServiceRouteResult serviceRouteResult = new ServiceRouteResult();

        ServiceExecutorType serviceExecutorType = serviceControler.getServiceExecutorType();
        
        if(serviceExecutorType == ServiceExecutorType.LOCAL) {
        	serviceRouteResult.setTargetSubsystemId(SysUtil.getSubSystemId());
        }else {
    		RemoteServiceController remoteServiceController = RemoteServiceRegistryManager.get().getRemoteServiceController(
    				serviceControler.getServiceInvokeId(), serviceControler.getInnerServiceCode());
    		if (remoteServiceController != null) {
    			serviceRouteResult.setTargetSubsystemId(remoteServiceController.getOutServiceVisitIdentity().getServiceApp());
    		}
        }
/*		if (StringUtil.isBlank(shardingId))
			throw new RuntimeException("请求接入路由失败，未返回分片信息["+shardingId+"]");*/
		logger.info("服务[%s]前处理路由定位到指定分片[%s]", serviceControler.getInnerServiceCode(), shardingId);
		CoreUtil.setCurrentShardingId(shardingId);
		//add xieqq 20201218  交易层面没有路由，以第一个服务作为路由的分片作为主分片
		if(StringUtil.isEmpty(CoreUtil.getMainShardingId())) {
			CoreUtil.setMainShardingId(shardingId);
		}
		
		return serviceRouteResult;
	}

}
