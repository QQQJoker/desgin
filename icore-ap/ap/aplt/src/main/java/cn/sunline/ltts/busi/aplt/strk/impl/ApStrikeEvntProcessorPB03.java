package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.cg.IoCgStrikeSvcType;
import cn.sunline.ltts.busi.iobus.type.pb.IoPbTypeStrikeInfo.ProcPbChargStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorPB03 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorPB03.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("收费调整冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CHRGJT) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CHRGJT + "]，不能冲正！");

		ProcPbChargStrikeInput cplIn = CommTools.getInstance(ProcPbChargStrikeInput.class);

		cplIn.setOrtrdt(evnt.getEvent1()); // 原交易日期
		cplIn.setOrtrsq(evnt.getEvent2()); // 原主交易流水
		cplIn.setTranam(evnt.getTranam()); // 交易金额
		cplIn.setCrcycd(evnt.getCrcycd()); // 货币代号
		SysUtil.getInstance(IoCgStrikeSvcType.class).procPbChargAdjtStrike(cplIn);

		if (bizlog.isDebugEnabled())
			bizlog.debug("收费调整冲正处理结束=====================");
	}

}
