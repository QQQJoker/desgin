package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApDpFrozIn;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorDP05 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DPFROZ) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DPFROZ + "]，不能冲正！");

		// DP05 冻结 DP
		IoApDpFrozIn cplIn = SysUtil.getInstance(IoApDpFrozIn.class);

		cplIn.setOtrasq(evnt.getMntrsq());// 主交易流水
		cplIn.setOtradt(evnt.getTrandt());// 日期
		cplIn.setStacps(stacps);// 冲正冲账分类
		cplIn.setOfrono(evnt.getFrozno()); // 冻结编号
		cplIn.setOfrozo(evnt.getTranno());// 原冻结序号
		cplIn.setTranam(evnt.getTranam());// 交易金额
		cplIn.setOfrndt(evnt.getEvent1());// 原冻结终止日期
		cplIn.setFreddt(evnt.getEvent2());// 新冻结终止日期
		cplIn.setFrozcd(evnt.getEvent3());// 原冻结分类码
		// SysUtil.getInstance(IoDpSrvStrike.class).prcFrozStrike(cplIn);
	}

}
