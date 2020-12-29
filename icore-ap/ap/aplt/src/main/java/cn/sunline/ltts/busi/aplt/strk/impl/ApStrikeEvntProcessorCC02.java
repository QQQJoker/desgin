package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorCC02 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCC02.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("类信用卡还款冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CC_REPAY) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CC_REPAY + "]，不能冲正！");

		throw ApError.Aplt.E0000("暂不支持[类信用卡还款冲正]冲正！");

//		IoCcStrikeSvcType.CcStrikeInput input = SysUtil.getInstance(IoCcStrikeSvcType.CcStrikeInput.class);
//
//		input.setElacct(evnt.getKehuzhao());// 电子账户
//		input.setAcctno(evnt.getJiaoyizh()); // 类信用卡账号
//		input.setAccttp(CommUtil.toEnum(E_ACCTTP.class, evnt.getSjgjzhi1()));// 账户类型
//		input.setTxndtx(evnt.getTran_date());// 原交易日期
//		input.setTxnamt(evnt.getJiaoyije());// 交易金额
//		input.setTxnseq(evnt.getJiaoyixh() + "");// 交易流水
//		input.setDbcrfg(evnt.getJiedaibz());// 借贷标志
//		input.setProdcd(evnt.getSjgjzhi2());// 产品代码
//		input.setOltime(evnt.getSjgjzhi3());// 联机处理时间
//		input.setCurrcd(evnt.getHuobdaih());// 币种
//		input.setManual(E_MANUAL.NOTMANUAL);// 人工标识
//		SysUtil.getInstance(IoCcStrikeSvc.class).repayStrike(input);
//
//		if (bizlog.isDebugEnabled())
//			bizlog.debug("类信用卡还款冲正处理结束=====================");
	}

}
