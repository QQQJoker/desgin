package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.in.InSaveClerTransBill;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;

public class ApStrikeEvntProcessorIN08 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorIN08.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_INCLER) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_INCLER + "]，不能冲正！");
		
			// DP016 出入金登记簿登记
			String transq = evnt.getEvent1();
			String trandt = evnt.getEvent2();
			E_TRANST status = E_TRANST.STRIKED;
//			SysUtil.getInstance(IoDpStrikeSvcType.class).procSaveIoBillStrike(fronsq, frondt, status);
			SysUtil.getInstance(InSaveClerTransBill.class).procSaveClerBillStrike(transq, trandt, status);
			bizlog.debug("清算登记簿登记冲正处理结束=====================");
	}

}
