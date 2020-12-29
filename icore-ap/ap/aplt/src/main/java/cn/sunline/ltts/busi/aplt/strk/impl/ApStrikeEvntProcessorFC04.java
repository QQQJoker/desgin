package cn.sunline.ltts.busi.aplt.strk.impl;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.serv.IoStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.fc.IoFcAcctComplexType.strikeFinacAcctIn;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.FcEnumType.E_SUBJST;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorFC04 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorFC04.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("融资账户转出冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_FCDRAW) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_FCDRAW + "]，不能冲正！");

		strikeFinacAcctIn input = SysUtil.getInstance(strikeFinacAcctIn.class);
		input.setCustac(evnt.getCustac());// 电子账号
		input.setSubjcd(evnt.getEvent2());// 标的编号
		input.setAcctno(evnt.getTranac());// 投资账号
		input.setNdtrin(BigDecimal.valueOf(Double.parseDouble(evnt.getEvent1())));// 利息
		input.setNdtrpr(evnt.getTranam());// 本金
		input.setTransq(evnt.getEvent4());// 交易流水
		input.setSubjst(E_SUBJST.valueOf(evnt.getEvent3()));// 标的状态

		SysUtil.getInstance(IoStrikeSvcType.class).strikePrcFcAcctOnlnblOut(input);

		if (bizlog.isDebugEnabled())
			bizlog.debug("融资账户转出冲正处理结束=====================");
	}

}
