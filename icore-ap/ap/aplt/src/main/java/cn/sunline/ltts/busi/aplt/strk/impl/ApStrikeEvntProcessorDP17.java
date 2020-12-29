package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorDP17 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorDP17.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_XM_IOBILL) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_XM_IOBILL + "]，不能冲正！");

		bizlog.debug("小马出入金明细冲正开始=====================");
		/*IolnXmzdSvc lnxmsvc = SysUtil.getInstance(IolnXmzdSvc.class);
		String termno = CommUtil.equals(evnt.getEvent4(), "") ? "0" : evnt.getEvent4();
		String subtno = CommUtil.equals(evnt.getEvent5(), "") ? "0" : evnt.getEvent5();
		lnxmsvc.ProcXmCpInStrike(evnt.getEvent1(), evnt.getEvent2(), evnt.getEvent3(), Long.parseLong(termno),
				Long.parseLong(subtno));*/

		bizlog.debug("小马出入金明细冲正结束=====================");
	}

}
