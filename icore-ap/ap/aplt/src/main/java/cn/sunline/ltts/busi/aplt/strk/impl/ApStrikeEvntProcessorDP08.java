package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApDpOpcuIn;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorDP08 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DPOPCU) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DPOPCU + "]，不能冲正！");

		// DP08 客户账号开立 DP
		IoApDpOpcuIn cplIn = SysUtil.getInstance(IoApDpOpcuIn.class);

		cplIn.setOtrasq(evnt.getTrandt());// 流水
		cplIn.setOtradt(evnt.getTrandt());// 日期
		cplIn.setStacps(stacps);// 冲正冲账分类
		cplIn.setTranac(evnt.getCustac()); // 客户账号

		// SysUtil.getInstance(IoDpSrvStrike.class).prcOpcuStrike(cplIn);
	}

}
