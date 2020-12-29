package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.tx.TxSvc;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorTX01 implements ApStrikeEvntProcessor {

	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorIN01.class);
	
	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		// TODO Auto-generated method stub

		if (bizlog.isDebugEnabled())
			bizlog.debug("增值税处理开始=====================");
		IoApRegBook strike = CommTools.getInstance(IoApRegBook.class);
		strike.setEvent1(evnt.getEvent1());
		strike.setEvent2(evnt.getEvent2());
		strike.setEvent3(evnt.getEvent3());
		strike.setEvent4(evnt.getEvent4());
		strike.setEvent5(evnt.getEvent5());
		strike.setEvent6(evnt.getEvent6());
		strike.setTranac(evnt.getTranac());
		strike.setTranam(evnt.getTranam());
		strike.setCustac(evnt.getCustac());
		strike.setTranno(evnt.getTranno());
		strike.setAmntcd(evnt.getAmntcd());
		strike.setCrcycd(evnt.getCrcycd());
		strike.setEvent7(evnt.getTransq());
		strike.setEvent8(evnt.getTrandt());
		
		// 增值税冲正
		SysUtil.getInstance(TxSvc.class).taxStrike(strike, colour);
	}

}
