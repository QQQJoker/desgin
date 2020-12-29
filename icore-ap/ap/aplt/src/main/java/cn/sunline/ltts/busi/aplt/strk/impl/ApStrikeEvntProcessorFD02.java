package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorFD02 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		
		throw ApError.Aplt.E0000("暂不支持[借据申购处理]冲正！");
		
//		if (bizlog.isDebugEnabled())
//			bizlog.debug("借据申购处理处理开始=====================");
//		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_FDBYPR) != 0)
//			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_FDBYPR + "]，不能冲正！");
//
//		if (bizlog.isDebugEnabled())
//			bizlog.debug("借据申购处理处理结束=====================");
	}

}
