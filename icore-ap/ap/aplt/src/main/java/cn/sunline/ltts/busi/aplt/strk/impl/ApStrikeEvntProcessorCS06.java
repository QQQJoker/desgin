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

public class ApStrikeEvntProcessorCS06 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCS06.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("现金长短款销账冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CS_OVPR) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CS_OVPR + "]，不能冲正！");

		throw ApError.Aplt.E0000("暂不支持[现金长短款销账]冲正！");

//		ProcCsProcStrikeInput input = SysUtil.getInstance(ProcCsProcStrikeInput.class);
//		input.setTranam(evnt.getJiaoyije()); // 冲正金额
//		input.setTransq(evnt.getSjgjzhi2()); // 交易流水
//		input.setOrtrdt(evnt.getSjgjzhi1()); // 原交易日期
//
//		SysUtil.getInstance(IoStrikeSvcType.class).procCsOverShortStrike(input);
//
//		if (bizlog.isDebugEnabled())
//			bizlog.debug("现金长短款销账冲正处理结束=====================");
	}
}
