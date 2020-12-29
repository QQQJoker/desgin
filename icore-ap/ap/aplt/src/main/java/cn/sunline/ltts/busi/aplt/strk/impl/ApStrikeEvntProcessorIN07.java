package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.ac.IoAcCheckBalanceServ;
import cn.sunline.ltts.busi.iobus.type.IoInWriteOffComplex.IoCheckBlanaceStrike;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorIN07 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorIN07.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("平衡检查中的清算补账冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CHCKBL) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CHCKBL + "]，不能冲正！");

		// IN07平衡检查中的清算补账冲正
		IoCheckBlanaceStrike checkstrike = CommTools.getInstance(IoCheckBlanaceStrike.class);
		checkstrike.setTrandt(evnt.getTrandt());
		checkstrike.setTransq(evnt.getMntrsq());
		SysUtil.getInstance(IoAcCheckBalanceServ.class).strikeCheckBalanceClear(checkstrike);

		if (bizlog.isDebugEnabled())
			bizlog.debug("平衡检查中的清算补账冲正处理结束=====================");
	}

}
