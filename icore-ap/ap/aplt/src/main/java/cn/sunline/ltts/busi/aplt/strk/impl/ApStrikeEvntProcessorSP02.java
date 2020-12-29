package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlSvcType;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoHotCtrlStrikeIn;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorSP02 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorSP02.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("电子账户消费退货冲正处理开始=====================");
		
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_REFUND) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_REFUND + "]，不能冲正！");
		//add by wuwei 20180512 增加热点机制
		if(SysUtil.getInstance(IoHotCtrlSvcType.class).selHcpDefn(evnt.getTranac()) == E_YES___.YES){
			if (bizlog.isDebugEnabled())
				bizlog.debug("热点账户冲正处理开始=====================");
			IoHotCtrlStrikeIn ioHotCtrlStrikeIn = SysUtil.getInstance(IoHotCtrlStrikeIn.class);
			ioHotCtrlStrikeIn.setAmntcd(evnt.getAmntcd());
			ioHotCtrlStrikeIn.setHcacct(evnt.getTranac());
			ioHotCtrlStrikeIn.setTrandt(evnt.getTrandt());
			ioHotCtrlStrikeIn.setTransq(evnt.getTransq());	
			ioHotCtrlStrikeIn.setTranam(evnt.getTranam());
			SysUtil.getInstance(IoHotCtrlStrikeSvcType.class).hotCtrlGeneralStrike(ioHotCtrlStrikeIn);			
		}else{
			String fronsq = evnt.getEvent1();
			String frondt = evnt.getEvent2();
			E_TRANST status = E_TRANST.STRIKED;
			SysUtil.getInstance(IoDpStrikeSvcType.class).procAccConStrike(fronsq, frondt, status);
		}
		if (bizlog.isDebugEnabled())
			bizlog.debug("电子账户消费退货冲正处理结束=====================");
	}

}
