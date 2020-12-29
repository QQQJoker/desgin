package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApDpLimit;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorDP07 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_LIMIT) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_LIMIT + "]，不能冲正！");
		
		// DP07 限额 DP
		IoApDpLimit cplIn = SysUtil.getInstance(IoApDpLimit.class);

		cplIn.setOtrasq(evnt.getTransq());// 流水
		cplIn.setOtradt(evnt.getTrandt());// 日期
		cplIn.setStacps(stacps);// 冲正冲账分类
		cplIn.setTranam(evnt.getTranam());// 交易金额
		// cplIn.setHuobdaih(evnt.getHuobdaih());// 货币代号
		cplIn.setCustac(evnt.getCustac());// 客户账号
		cplIn.setAcctno(evnt.getTranac());// 交易账号
		cplIn.setChanpdma(evnt.getEvent1());// 事件关键字1=产品代码
		cplIn.setCsactp(evnt.getEvent2());// 事件关键字2=客户账号类型
		cplIn.setChanid(evnt.getEvent3());// 事件关键字3=渠道条件
		cplIn.setEvenid(evnt.getEvent4());// 事件关键字4=事件条件
		cplIn.setScenid(evnt.getEvent5());// 事件关键字5=场景条件

		// SysUtil.getInstance(IoDpSrvStrike.class).prcDpLimitStrike(cplIn);
	}

}
