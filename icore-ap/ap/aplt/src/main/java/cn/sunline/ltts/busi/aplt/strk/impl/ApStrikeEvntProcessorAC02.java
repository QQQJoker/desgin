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

public class ApStrikeEvntProcessorAC02 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorAC02.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("红包支取冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_ACDRAW) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_ACDRAW + "]，不能冲正！");

		throw ApError.Aplt.E0000("暂不支持[红包支取]冲正！");
		
//		ProcAcDrawStrike.Input input = SysUtil.getInstance(ProcAcDrawStrike.Input.class);
//		ProcAcDrawStrikeInput cplIn = input.getStrikeInput();
//
//		cplIn.setCustac(evnt.getKehuzhao()); // 电子账户
//		cplIn.setAcctno(evnt.getJiaoyizh()); // 红包账号
//		// cplIn.setStrktp(null); // 冲正冲账标志
//		cplIn.setOrtrdt(evnt.getTran_date()); // 原交易日期
//		cplIn.setOrigtq(cplInput.getYszjylsh()); // 原主交易流水
//		cplIn.setTranam(evnt.getJiaoyije()); // 交易金额
//		cplIn.setAmntcd(evnt.getJiedaibz()); // 借贷标志
//		cplIn.setColrfg(CommUtil.toEnum(E_COLOUR.class, eHolzjzbz)); // 红蓝字记账标识
//		cplIn.setDetlsq(evnt.getJiaoyixh()); // 明细序号
//		cplIn.setCrcycd(evnt.getHuobdaih()); // 货币代号
//		SysUtil.getInstance(IoAcStrikeSvcType.class).procAcDrawStrike(input);

//		if (bizlog.isDebugEnabled())
//			bizlog.debug("红包支取冲正处理结束=====================");
	}
}
