package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.serv.IoStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcApplFdbkStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorFD04 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorFD04.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("基金赎回处理处理开始=====================");

		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_FDBKPR) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_FDBKPR + "]，不能冲正！");

		// FD04 基金赎回处理
		ProcApplFdbkStrikeInput cplFdBackStrikeIn = SysUtil.getInstance(ProcApplFdbkStrikeInput.class);

		cplFdBackStrikeIn.setCustac(evnt.getCustac()); // 客户账号
		cplFdBackStrikeIn.setAcctno(evnt.getTranac()); // 基金内部账号
		cplFdBackStrikeIn.setStacps(stacps); // 冲正冲账类型
		cplFdBackStrikeIn.setTrandt(evnt.getEvent1()); // 基金赎回交易日期
		cplFdBackStrikeIn.setTransq(evnt.getEvent2()); // 基金赎回交易流水
		cplFdBackStrikeIn.setBackam(evnt.getTranam()); // 赎回金额
		cplFdBackStrikeIn.setOrtrdt(evnt.getMntrsq()); // 原交易流水

		SysUtil.getInstance(IoStrikeSvcType.class).prcFdBackStrike(cplFdBackStrikeIn);

		if (bizlog.isDebugEnabled())
			bizlog.debug("基金赎回处理处理结束=====================");
	}

}
