package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApCaUpacstIn;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorCA01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCA01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("电子账户状态冲正处理开始=====================");

		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_UPACST) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_UPACST + "]，不能冲正！");

		// CA01 电子账户状态冲正

		IoApCaUpacstIn entity = CommTools.getInstance(IoApCaUpacstIn.class);

		entity.setCustac(evnt.getCustac());
		entity.setDime01(evnt.getEvent1());
		entity.setDime02(evnt.getEvent2());
		entity.setDime03(evnt.getEvent3());
		entity.setDime04(evnt.getEvent4());
		entity.setDime05(evnt.getEvent5());
		entity.setDime06(evnt.getEvent6());
		entity.setPrcscd(evnt.getEvent7());
		entity.setFacesg(CommUtil.toEnum(E_YES___.class, evnt.getEvent8()));

		// 调用电子账户状态更新服务
		SysUtil.getInstance(IoCaSrvGenEAccountInfo.class).procUpAcctst(entity);

		if (bizlog.isDebugEnabled())
			bizlog.debug("电子账户状态冲正处理结束=====================");
	}

}
