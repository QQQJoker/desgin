package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.ce.IoDcmtSvc;
import cn.sunline.ltts.busi.iobus.type.ce.IoDcmtComplexType.useDcmtStrikeIuput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorDC01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorDC01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("凭证使用冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DC_USE) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DC_USE + "]，不能冲正！");

		//throw ApError.Aplt.E0000("暂不支持[凭证使用冲正]冲正！");

		useDcmtStrikeIuput input = SysUtil.getInstance(useDcmtStrikeIuput.class);
		input.setOdtrdt(evnt.getEvent8());
		input.setOdtrsq(evnt.getEvent9());
		input.setDcmttp(evnt.getEvent3());
		SysUtil.getInstance(IoDcmtSvc.class).useDcmtStrike(input);

		if (bizlog.isDebugEnabled())
			bizlog.debug("凭证使用冲正处理结束=====================");
	}

}
