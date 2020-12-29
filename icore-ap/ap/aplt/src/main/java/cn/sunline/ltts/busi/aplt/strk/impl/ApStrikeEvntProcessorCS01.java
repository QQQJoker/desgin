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

public class ApStrikeEvntProcessorCS01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCS01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("现金收入冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CS_SAVE) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CS_SAVE + "]，不能冲正！");

		throw ApError.Aplt.E0000("暂不支持[现金收入冲正]冲正！");

//		ProcCsProcStrikeInput input = SysUtil.getInstance(ProcCsProcStrikeInput.class);
//		input.setStacps(stacps); // 冲正冲账类型
//		input.setTranam(evnt.getJiaoyije()); // 冲正金额
//		input.setTransq(evnt.getSjgjzhi2()); // 交易流水
//		input.setOrtrdt(evnt.getSjgjzhi1()); // 原交易日期
//		input.setAmntcd(evnt.getJiedaibz()); // 借贷标志
//		input.setBillsq(evnt.getSjgjzhi3()); // 原账单流水
//		input.setColrfg(CommUtil.toEnum(E_COLOUR.class, eHolzjzbz)); // 红蓝字记账标识
//
//		SysUtil.getInstance(IoStrikeSvcType.class).procCsSaveStrike(input);
//		if (bizlog.isDebugEnabled())
//			bizlog.debug("现金收入冲正处理结束=====================");
	}
}
