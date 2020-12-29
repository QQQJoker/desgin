package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.BusinessConstants;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApUnHangIn;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

public class ApStrikeEvntProcessorTA03 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_INCNCL) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_INCNCL + "]，不能冲正！");

		// TA03 销账 TA
		IoApUnHangIn cplIn = SysUtil.getInstance(IoApUnHangIn.class);

		cplIn.setOtrdsq(evnt.getTransq());// 流水
		cplIn.setOtradt(evnt.getTrandt());// 日期
		cplIn.setStacps(stacps);// 冲正冲账分类
		cplIn.setStayno(evnt.getStayno()); // 账号
		cplIn.setTranno(evnt.getTranno());// 序号
		cplIn.setTranam(evnt.getTranam());// 交易金额
		// cplIn.setJiedaibz(evnt.getJiedaibz());// 借贷标志
		cplIn.setZhaiyodm(BusinessConstants.SUMMARY_CZZ);// 摘要代码
		cplIn.setColour(colour);// 红蓝字记账标识
		// cplIn.setHuobdaih(evnt.getHuobdaih());// 货币
		cplIn.setOmtrsq(evnt.getMntrsq());

		// SysUtil.getInstance(IoTaSrvStrike.class).prcUnHangUp(cplIn);
	}

}
