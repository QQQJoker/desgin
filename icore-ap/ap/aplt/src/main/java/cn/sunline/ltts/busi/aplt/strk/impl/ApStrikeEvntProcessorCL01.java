package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorCL01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCL01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("结算户销户冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CLSACT) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CLSACT + "]，不能冲正！");

		// 结算户销户冲正

		IoApRegBook strike = CommTools.getInstance(IoApRegBook.class);
		strike.setEvent1(evnt.getEvent1());
		strike.setEvent2(evnt.getEvent2());
		strike.setEvent3(evnt.getEvent3());
		strike.setEvent4(evnt.getEvent4());
		strike.setEvent5(evnt.getEvent5());
		strike.setEvent6(evnt.getEvent6());
		strike.setTranac(evnt.getTranac());
		strike.setTranam(evnt.getTranam());
		strike.setCustac(evnt.getCustac());
		strike.setTranno(evnt.getTranno());
		strike.setAmntcd(evnt.getAmntcd());
		strike.setCrcycd(evnt.getCrcycd());
		CommTools.getInstance(IoDpStrikeSvcType.class).procCloseAcctStrike(strike, colour);

		if (bizlog.isDebugEnabled())
			bizlog.debug("结算户销户冲正处理结束=====================");
	}

}
