package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoHotCtrlStrikeIn;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 热点账户冲正实现
 * @author jizhirong 
 * 20180420
 *
 */
public class ApStrikeEvntProcessorHC01 implements ApStrikeEvntProcessor {

	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorHC01.class);
	
	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {

		if (bizlog.isDebugEnabled())
			bizlog.debug("热点账户冲正处理开始=====================");
		IoHotCtrlStrikeIn ioHotCtrlStrikeIn = SysUtil.getInstance(IoHotCtrlStrikeIn.class);
		ioHotCtrlStrikeIn.setAmntcd(evnt.getAmntcd());
		ioHotCtrlStrikeIn.setHcacct(evnt.getTranac());
		ioHotCtrlStrikeIn.setTrandt(evnt.getTrandt());
		ioHotCtrlStrikeIn.setTransq(evnt.getTransq());
		ioHotCtrlStrikeIn.setTranam(evnt.getTranam());
		SysUtil.getInstance(IoHotCtrlStrikeSvcType.class).hotCtrlGeneralStrike(ioHotCtrlStrikeIn);
	}

}
