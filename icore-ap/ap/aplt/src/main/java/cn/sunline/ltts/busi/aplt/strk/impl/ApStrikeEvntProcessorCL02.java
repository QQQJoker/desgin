package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_CLSTAT;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorCL02 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCL02.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("销户登记簿冲正处理开始=====================");

		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CLACST) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CLACST + "]，不能冲正！");

		// 销户登记簿冲正

		String custac = evnt.getCustac();
		String clossq = evnt.getEvent1();
		E_CLSTAT clstat = CommUtil.toEnum(E_CLSTAT.class, evnt.getEvent2());
		CommTools.getInstance(IoDpStrikeSvcType.class).procKnbClacStrike(clossq, custac, clstat);

		if (bizlog.isDebugEnabled())
			bizlog.debug("销户登记簿冲正处理结束=====================");
	}

}
