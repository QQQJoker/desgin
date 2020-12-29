package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.IoLnStrikeSvcType;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.LnError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorLN05 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorLN05.class);
	
	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {

		if (bizlog.isDebugEnabled())
			bizlog.debug("委托贷款豁免冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_LNBZEX) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_LNBZEX + "]，不能冲正！");
		// 贷款不允许隔日冲账
		if (CommUtil.compare(CommTools.prcRunEnvs().getTrandt(), evnt.getTrandt()) > 0) {
			throw LnError.geno.E0001("此交易[流水：" + evnt.getTransq() + "]不允许隔日冲正！");
		}	
		
		// 调用冲正服务
		SysUtil.getInstance(IoLnStrikeSvcType.class).IoLnbzexStkSvc(evnt.getEvent1(), evnt.getEvent2(), evnt.getEvent3());
		
		if (bizlog.isDebugEnabled())
			bizlog.debug("委托贷款豁免冲正处理结束=====================");
	
		
	}

}
