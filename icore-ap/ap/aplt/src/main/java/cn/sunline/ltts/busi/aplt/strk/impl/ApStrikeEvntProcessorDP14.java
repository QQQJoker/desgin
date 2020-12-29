package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApDpInstTally;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorDP14 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_PAYINST_TALLY) != 0
				&& CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_TAX_TALLY) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_PAYINST_TALLY + "]且不是["
					+ ApUtil.TRANS_EVENT_TAX_TALLY + "]，不能冲正！");

		// 利息和利息税记账事件差不多，故冲账也在一个程序实现
		// DP14 利息支付记账事件 、DP15 代扣利息税记账事件
		IoApDpInstTally cplIn = SysUtil.getInstance(IoApDpInstTally.class);

		cplIn.setOtrasq(evnt.getTransq());// 流水
		cplIn.setOtradt(evnt.getTrandt());// 日期
		cplIn.setTranac(evnt.getTranac());
		cplIn.setTranam(evnt.getTranam());
		// cplIn.setJiedaibz(evnt.getJiedaibz());

		// SysUtil.getInstance(IoDpSrvStrike.class).prcDpInstTallyStrike(cplIn);

	}

}
