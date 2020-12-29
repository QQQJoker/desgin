package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.IoSdStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.IoSdStrikeSvcType.ProcCollentBuyyStrike;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorSD01 implements ApStrikeEvntProcessor {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorSD01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled()) 
			bizlog.debug(">>> 募集购买冲正开始 =====================");

		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVET_SD_MJBY) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVET_SD_MJBY + "]，不能冲正！");


		ProcCollentBuyyStrike.InputSetter input = SysUtil.getInstance(ProcCollentBuyyStrike.InputSetter.class);
		
		input.setMujiam(evnt.getTranam());
		input.setJjprod(evnt.getEvent1());
		input.setTransq(evnt.getEvent2());
		input.setStrktp(evnt.getEvent3());
		
		SysUtil.getInstance(IoSdStrikeSvcType.class).procCollentBuyyStrike(input, null);
		
		if (bizlog.isDebugEnabled())
			bizlog.debug(">>> 募集购买冲正结束  =====================");
	
	}
}
