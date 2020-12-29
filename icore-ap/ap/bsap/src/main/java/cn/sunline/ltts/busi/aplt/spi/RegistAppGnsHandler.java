package cn.sunline.ltts.busi.aplt.spi;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.engine.online.handler.OETAfterHandler;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerConstant;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerContext;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.zdbank.cds.util.ShardingUtil;
import cn.sunline.edsp.base.annotation.Groups;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.edsp.base.lang.RunnableWithReturn;

@SPIMeta(id=RegistAppGnsHandler.SPI_ID)
@Order(1000)
@Groups({OETHandlerConstant.FLOW_ENGINE_TYPE})
public class RegistAppGnsHandler implements OETAfterHandler {
	
	public static final String SPI_ID = "regist_appGns";
	
	public static final SysLog log = SysLogUtil.getSysLog(RegistAppGnsHandler.class);

	@Override
	public void handler(OETHandlerContext var1) {
		if (!ShardingUtil.isRouteMode()) {
			return;
		}
		// 主事务已经提交，
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				int sum = ShardingUtil.registGnsInfo();
				log.debug("交易后处理成功注册路由映射信息：[%s]条！！！",sum);
				return null;
			}
		});
		
	}

	

}
