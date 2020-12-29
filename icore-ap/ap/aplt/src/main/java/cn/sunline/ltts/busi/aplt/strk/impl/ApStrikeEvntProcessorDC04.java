package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.cd.IoCdAcctDcmtSvcType;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorDC04 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorDC04.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("存折扩展信息登记冲正处理开始（删除）=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DC_EXT) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DC_EXT + "]，不能冲正！");
		
		String dcmttp = evnt.getEvent3();
		String dcmtno = evnt.getEvent4();
		SysUtil.getInstance(IoCdAcctDcmtSvcType.class).deleteKdbVochBook(dcmttp, dcmtno);
		if (bizlog.isDebugEnabled())
			bizlog.debug("存折扩展信息登记冲正处理结束（删除）=====================");
	}

}
