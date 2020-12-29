package cn.sunline.ltts.busi.aplt.spi;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.engine.service.ServiceRequestHeader;
import cn.sunline.adp.cedar.base.engine.transaction.ServiceTransactionMode;
import cn.sunline.adp.cedar.base.engine.transaction.distributed.DistributedOnlineTransaction;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.service.engine.handler.ServiceEngineHandler;
import cn.sunline.adp.cedar.service.engine.handler.ServiceEngineHandler.ServiceEngineHandlerType;
import cn.sunline.adp.cedar.service.executor.ServiceExecutorContext;
import cn.sunline.adp.core.util.SpringUtils;
import cn.sunline.edsp.base.annotation.Groups;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.gns.api.AcdtApi;

@SPIMeta(id=InitTranDateServiceEngineHandler.SPI_ID)
@Order(15)
@Groups({ServiceEngineHandlerType.BEFORE})
public class InitTranDateServiceEngineHandler implements ServiceEngineHandler {
	private static final SysLog log = SysLogUtil.getSysLog(InitTranDateServiceEngineHandler.class);

	public static final String SPI_ID = "init_trandate";
	
	@Override
	public void handle(ServiceExecutorContext context) {
		
		if (context.isLocal()) {
			// 不需要做事务控制的，则不需要初始化“服务调用流水”
			if (ServiceTransactionMode.NotSupported == context.getServiceController().getServiceTransactionMode()) {
				return;
			} else {
				// 如果事务对象为空或者事务对象不是分布式事务，则不需要初始化“服务调用流水”
				if (EngineContext.getOnlineTransactionManager() == null || !(EngineContext.getOnlineTransactionManager() instanceof DistributedOnlineTransaction)) {
					return;
				}
			}
		}
		
		ServiceRequestHeader header = context.getServiceRequest().getRequestHeader();
//		RequestHeaderData headerContext = EngineContext.getRequestData().getRequestHeader();
		if(SysUtil.getCurrentSystemType() == SystemType.batch) {
			header.setBusiSeqNo(header.getCallSeqNo());
			header.getMap().put("servno", "999");
		}
		AcdtApi api = SpringUtils.getBean(AcdtApi.class);
		header.setTranTimestamp(api.getCurrentDate()); // 设置交易开始日期
		log.info("-------------->>>> api.getCurrentDate()" + api.getCurrentDate());
		
	}

}
