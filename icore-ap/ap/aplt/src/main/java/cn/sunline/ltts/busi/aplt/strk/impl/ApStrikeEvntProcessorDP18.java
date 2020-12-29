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
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorDP18 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorDP18.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {

		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_ACTOAC) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_ACTOAC + "]，不能冲正！");
		
		bizlog.debug("电子账户出入金登记簿冲正开始=====================");
		String fronsq = evnt.getEvent1();
		String frondt = evnt.getEvent2();

		CommTools.getInstance(IoDpStrikeSvcType.class).procKnlCaryStrike(fronsq, frondt, E_TRANST.STRIK);
		bizlog.debug("电子账户出入金登记簿冲正结束=====================");

	}

}
