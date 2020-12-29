package cn.sunline.ltts.busi.bsap.impl;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.engine.online.InServiceController.ServiceCategory;
import cn.sunline.adp.cedar.engine.online.handler.OETAfterHandler;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerConstant;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerContext;
import cn.sunline.edsp.base.annotation.Groups;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.busi.aplt.tools.BaseEnvUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.ParamUtil;

@SPIMeta(id = "bsap_online_process_after")
@Order(900)
@Groups({OETHandlerConstant.FLOW_ENGINE_TYPE})
public class OETOnlineProcessAfterHandler implements OETAfterHandler {

	private static final SysLog log = SysLogUtil.getSysLog(OETOnlineProcessAfterHandler.class);
	
	public static final String SPI_ID = "bsap_online_process_after";
	
	@Override
	public void handler(OETHandlerContext context) {
		
		if(context.getInServiceController().getServiceCategory() == ServiceCategory.T ) {
			String shareEnv = ParamUtil.getPublicParmWithoutCorpno("SHARERUNENV").getPmval1();
			if (shareEnv != null) {
				for (String key : shareEnv.split(",")) {
					context.getResponse().getData().getCommRes().remove(key);
				}
			}
			
		}
		
		
		if(CommTools.isNeedRegistPckg()) {
			if(log.isDebugEnabled()) 
			log.debug("begin regist package sequence");
			
			BaseEnvUtil.registPackageSequence(CommTools.prcRunEnvs().getTransq(), CommTools.prcRunEnvs().getTrandt(),
					context.getRequest(), context.getResponse(), context.getBeginTime(),
					context.getException(), true);
		}
	}

}
