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
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FCFLAG;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorOP01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorOP01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("负债子账号开户冲正处理开始=====================");
		
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_OPENSUB) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_OPENSUB + "]，不能冲正！");

		// OP01 负债子账号开户冲正
		String custac = evnt.getCustac();
		String acctno = evnt.getTranac();
		String retrdt = evnt.getEvent1();
		String retrsq = evnt.getEvent2();
		// E_FCFLAG fcflag = E_FCFLAG.valueOf(evnt.getEvent3());
		E_FCFLAG fcflag = CommUtil.toEnum(E_FCFLAG.class, evnt.getEvent3());

		CommTools.getInstance(IoDpStrikeSvcType.class).procOpenSubAcctStrike(custac, acctno, retrdt, retrsq, fcflag);

		if (bizlog.isDebugEnabled())
			bizlog.debug("负债子账号开户冲正处理结束=====================");
	}

}
