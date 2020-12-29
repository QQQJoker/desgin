package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorCUPS implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCUPS.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("银联来账登记簿冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CUPSTR) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CUPSTR + "]，不能冲正！");

		// 银联来账登记簿冲正
		String mntrsq = evnt.getEvent1();
		String trandt = evnt.getEvent2();
		E_YES___ istrcf = CommUtil.toEnum(BaseEnumType.E_YES___.class, evnt.getEvent3());
		CommTools.getInstance(IoDpStrikeSvcType.class).procKnlIoblCupsStrike(mntrsq, trandt, istrcf);

		if (bizlog.isDebugEnabled())
			bizlog.debug("银联来账登记簿冲正处理结束=====================");
	}

}
