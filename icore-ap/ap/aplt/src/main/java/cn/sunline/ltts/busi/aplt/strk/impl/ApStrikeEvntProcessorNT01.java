package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.serv.IoStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcApplNoteBuyStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorNT01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorNT01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("票据购买冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_BYNOTE) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_BYNOTE + "]，不能冲正！");

		ProcApplNoteBuyStrikeInput cplNoteBuyStrikeInput = SysUtil.getInstance(ProcApplNoteBuyStrikeInput.class);

		cplNoteBuyStrikeInput.setCustac(evnt.getCustac()); // 电子账号
		cplNoteBuyStrikeInput.setAcctno(evnt.getTranac()); // 票据账号
		cplNoteBuyStrikeInput.setCrcycd(evnt.getCrcycd()); // 货币代号
		cplNoteBuyStrikeInput.setStacps(stacps); // 冲正冲账类型
		cplNoteBuyStrikeInput.setTranam(evnt.getTranam()); // 交易金额
		cplNoteBuyStrikeInput.setTransq(evnt.getEvent1()); // 交易流水
		cplNoteBuyStrikeInput.setOrtrdt(evnt.getTrandt()); // 原交易日期

		SysUtil.getInstance(IoStrikeSvcType.class).prcNoteBuyStrike(cplNoteBuyStrikeInput);

		if (bizlog.isDebugEnabled())
			bizlog.debug("票据购买冲正处理结束=====================");
	}

}
