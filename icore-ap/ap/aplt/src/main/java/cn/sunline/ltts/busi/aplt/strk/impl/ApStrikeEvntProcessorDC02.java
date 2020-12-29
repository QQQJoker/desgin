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

public class ApStrikeEvntProcessorDC02 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorDC02.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("凭证调出冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DC_OUT) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DC_OUT + "]，不能冲正！");

		throw ApError.Aplt.E0000("暂不支持[凭证调出冲正]冲正！");

//		DcmtOutStrikeIuput input = SysUtil.getInstance(DcmtOutStrikeIuput.class);
//		input.setStardt(evnt.getSjgjzhi1());
//		input.setMovesq(evnt.getSjgjzhi2());
//		SysUtil.getInstance(IoDcmtSvc.class).outDcmtStrike(input);
//
//		if (bizlog.isDebugEnabled())
//			bizlog.debug("凭证调出冲正处理结束=====================");
	}

}
