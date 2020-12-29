package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.serv.IoStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcApplStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorDF01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorDF01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("保险购买冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DFPYIU) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DFPYIU + "]，不能冲正！");

		ProcApplStrikeInput input = SysUtil.getInstance(ProcApplStrikeInput.class);

		input.setCustac(evnt.getCustac()); // 电子账号
		input.setAcctno(evnt.getTranac()); // 票据账号
		input.setCrcycd(evnt.getCrcycd()); // 货币代号
		input.setStacps(stacps); // 冲正冲账类型
		input.setTranam(evnt.getTranam()); // 交易金额
		input.setTransq(evnt.getEvent1()); // 交易流水
		input.setOrtrdt(evnt.getTrandt()); // 原交易日期
		input.setBakup1(evnt.getEvent3()); // 投保单号

		SysUtil.getInstance(IoStrikeSvcType.class).prcInsuBuyStrike(input);

		if (bizlog.isDebugEnabled())
			bizlog.debug("保险购买冲正处理结束=====================");
	}

}
