package cn.sunline.ltts.busi.bsap.impl;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.engine.online.handler.OETFinallyHandler;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerConstant;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerContext;
import cn.sunline.edsp.base.annotation.Groups;
import cn.sunline.edsp.base.factories.SPIMeta;

@SPIMeta(id = "bsap_online_process_finally")
@Order(900)
@Groups({OETHandlerConstant.FLOW_ENGINE_TYPE})
public class OETOnlineProcessFinallyHandler implements OETFinallyHandler{

	private static final SysLog log = SysLogUtil.getSysLog(OETOnlineProcessFinallyHandler.class);
	
	public static final String SPI_ID = "bsap_online_process_finally";
	
	@Override
	public void handler(OETHandlerContext arg0) {
		
		
	}

}
