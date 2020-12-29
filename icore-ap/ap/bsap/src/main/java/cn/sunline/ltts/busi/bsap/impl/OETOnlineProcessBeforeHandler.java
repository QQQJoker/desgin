package cn.sunline.ltts.busi.bsap.impl;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.engine.online.handler.OETBeforeHandler;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerConstant;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerContext;
import cn.sunline.edsp.base.annotation.Groups;
import cn.sunline.edsp.base.factories.FactoriesLoader;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.busi.aplt.spi.KnsTranManager;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApJournal;

@SPIMeta(id = "bsap_online_process_before")
@Order(900)
@Groups({OETHandlerConstant.FLOW_ENGINE_TYPE})
public class OETOnlineProcessBeforeHandler implements OETBeforeHandler {

	private static final SysLog log = SysLogUtil.getSysLog(OETOnlineProcessBeforeHandler.class);
	
	public static final String SPI_ID = "bsap_online_process_before";
	
	@Override
	public void handler(OETHandlerContext context) {
		
		if(CommTools.isRegistTransactionSeq()) {
			if(log.isDebugEnabled()) 
			log.debug("begin regist transaction sequence");
			KnsTranManager manager = FactoriesLoader.getNewestFactory(KnsTranManager.class);
			ApJournal.saveKnsTran(manager.getKnsTran());
		}
	}

}
