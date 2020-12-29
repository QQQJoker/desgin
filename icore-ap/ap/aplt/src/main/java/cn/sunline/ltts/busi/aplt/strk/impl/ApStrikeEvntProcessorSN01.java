package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.busi.iobus.servicetype.sn.IoAcctInfoSvtp;
import cn.sunline.edsp.busi.iobus.type.sn.IoAcctComplexType.updKnlStrInfo;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

//冲正外围登记簿登记处理
public class ApStrikeEvntProcessorSN01 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_SNINST) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_SNINST + "]，不能冲正！");

		
		IoAcctInfoSvtp IoAcctInfoSvtp = CommTools.getInstance(IoAcctInfoSvtp.class);
		
		updKnlStrInfo input = SysUtil.getInstance(updKnlStrInfo.class);
		input.setTransq(evnt.getEvent1());
		input.setTrandt(evnt.getEvent2());
		IoAcctInfoSvtp.strikInsKnlSntr(input);
	}

}