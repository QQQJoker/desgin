package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType.ProcCloseStrike;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcCloseStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorDP02 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorDP02.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		bizlog.debug("销户冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DPCLOS) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DPCLOS + "]，不能冲正！");

		// DP02 销户 DP
		ProcCloseStrike.Input input = SysUtil.getInstance(ProcCloseStrike.Input.class);
		ProcCloseStrikeInput cplIn = input.getStrikeInput();

		cplIn.setCustac(evnt.getCustac());// 电子账户
		cplIn.setCardno(evnt.getTranac()); // 卡号
		cplIn.setStacps(stacps);// 冲正冲账分类
		cplIn.setOrtrdt(evnt.getTrandt());// 原交易日期
		cplIn.setAcctst(CommUtil.toEnum(E_ACCTST.class, evnt.getEvent1())); // 原电子账户状态
		cplIn.setClossq(evnt.getEvent2()); // 销户流水
		cplIn.setOrcssq(evnt.getEvent3()); // 原销户流水

		SysUtil.getInstance(IoDpStrikeSvcType.class).procCloseStrike(input);

		bizlog.debug("销户冲正处理结束=====================");

		return;
	}

}
