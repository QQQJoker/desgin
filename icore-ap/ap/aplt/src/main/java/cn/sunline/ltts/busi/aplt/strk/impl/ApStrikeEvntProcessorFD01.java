package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.serv.IoStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcApplFdbyStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorFD01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorFD01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("基金申购申请处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_FDBYAP) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_FDBYAP + "]，不能冲正！");

		// FD01 基金申购申请
		ProcApplFdbyStrikeInput cplFdBuyStrikeIn = SysUtil.getInstance(ProcApplFdbyStrikeInput.class);

		// cplFdBuyStrikeIn.setCustac(evnt.getKehuzhao()); //客户账号
		// cplFdBuyStrikeIn.setAcctno(evnt.getJiaoyizh()); //基金内部账号
		cplFdBuyStrikeIn.setStacps(stacps); // 冲正冲账类型
		cplFdBuyStrikeIn.setTrandt(evnt.getEvent1()); // 基金申购日期
		cplFdBuyStrikeIn.setTransq(evnt.getEvent2()); // 基金申购交易流水
		cplFdBuyStrikeIn.setBuyamt(evnt.getTranam()); // 申购金额
		cplFdBuyStrikeIn.setAcctno(evnt.getTranac());

		SysUtil.getInstance(IoStrikeSvcType.class).prcFdBuyStrike(cplFdBuyStrikeIn);

		if (bizlog.isDebugEnabled())
			bizlog.debug("基金申购申请处理结束=====================");
	}

}
