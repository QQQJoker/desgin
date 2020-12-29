package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApDpFallInst;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorDP10 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_FALLINST) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_FALLINST + "]，不能冲正！");

		// DP10 倒起息
		IoApDpFallInst cplIn = SysUtil.getInstance(IoApDpFallInst.class);

		cplIn.setOtrasq(evnt.getTransq()); // 原流水
		cplIn.setOtradt(evnt.getTrandt()); // 原日期
		// cplIn.setCzjdaibz(tblSj.getJiedaibz());
		cplIn.setEracam(evnt.getTranam());
		cplIn.setTrandt(CommToolsAplt.prcRunEnvs().getTrandt());
		cplIn.setTransq(CommToolsAplt.prcRunEnvs().getTransq());
		cplIn.setBgindt(evnt.getBgindt()); // 计息起始日期
		cplIn.setAcctno(evnt.getTranac()); // 负债账号

		// SysUtil.getInstance(IoDpSrvStrike.class).prcDpFallInstStrike(cplIn);

	}

}
