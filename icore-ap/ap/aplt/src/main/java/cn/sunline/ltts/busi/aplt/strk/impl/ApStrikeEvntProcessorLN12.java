package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.ln.servicetype.IoLnIntfSvcType;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.LnEnumType.E_DCTXST;

public class ApStrikeEvntProcessorLN12 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorLN12.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("贷款印花税处理冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_REGTAX) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_REGTAX + "]，不能冲正！");

		SysUtil.getInstance(IoLnIntfSvcType.class).preLnTaxRevs(evnt.getTranac(), CommUtil.toEnum(E_DCTXST.class, evnt.getEvent1()), evnt.getTrandt(), evnt.getTransq());
		
		if (bizlog.isDebugEnabled())
			bizlog.debug("贷款印花税处理冲正处理结束=====================");
	}

}
