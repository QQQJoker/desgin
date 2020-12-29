package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSevAccountLimit.IoAcRevQuota;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SERVTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorQT01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorQT01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("额度扣减冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.CA_EDUK_QT) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.CA_EDUK_QT + "]，不能冲正！");

		// 额度扣减
		IoAcRevQuota.InputSetter input = CommTools.getInstance(IoAcRevQuota.InputSetter.class);
		IoAcRevQuota.Output output = CommTools.getInstance(IoAcRevQuota.Output.class);

		input.setCustac(evnt.getCustac());
		input.setTranam(evnt.getTranam());
		input.setServdt(evnt.getEvent3());
		input.setServsq(evnt.getEvent2());
		input.setServtp(CommUtil.toEnum(E_SERVTP.class, evnt.getEvent1()));
		CommTools.getInstance(IoCaSevAccountLimit.class).RevAcctQuota(input, output);

		if (bizlog.isDebugEnabled())
			bizlog.debug("额度扣减冲正处理结束=====================");
	}

}
