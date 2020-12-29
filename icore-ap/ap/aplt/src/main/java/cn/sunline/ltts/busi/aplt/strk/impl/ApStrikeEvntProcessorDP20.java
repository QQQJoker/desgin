package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorDP20 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DEDUCT) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DEDUCT + "]，不能冲正！");

		// 扣划登记簿冲正
		String trandt = evnt.getEvent1();
		String frozno = evnt.getFrozno();
		String mntrsq = evnt.getEvent2();

		CommTools.getInstance(IoDpFrozSvcType.class).deduStrike(mntrsq, trandt, frozno);

	}

}
