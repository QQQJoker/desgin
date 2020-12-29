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

public class ApStrikeEvntProcessorCS05 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCS05.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("现金调拨冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CS_MVRG) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CS_MVRG + "]，不能冲正！");

		throw ApError.Aplt.E0000("暂不支持[现金调拨]冲正！");

//		ProcApplStrikeInput input = SysUtil.getInstance(ProcApplStrikeInput.class);
//		input.setStacps(stacps); // 冲正冲账类型
//		input.setBakup1(evnt.getSjgjzhi1()); // 原调拨单日期
//		input.setBakup2(evnt.getSjgjzhi2()); // 原调拨单号
//
//		SysUtil.getInstance(IoStrikeSvcType.class).procCsMoveStrike(input);
//
//		if (bizlog.isDebugEnabled())
//			bizlog.debug("现金调拨冲正处理结束=====================");
	}

}
