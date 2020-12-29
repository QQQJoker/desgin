package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorFD03 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		throw ApError.Aplt.E0000("暂不支持[基金赎回申请]冲正！");

//		if (bizlog.isDebugEnabled())
//			bizlog.debug("基金赎回申请处理开始=====================");
//		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_FDBKAP) != 0)
//			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_FDBKAP + "]，不能冲正！");
//
//		
//		if (bizlog.isDebugEnabled())
//			bizlog.debug("基金赎回申请处理结束=====================");
	}

}
