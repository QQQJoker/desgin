package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.cd.IoCdHouseHoldSvcType;
import cn.sunline.ltts.busi.iobus.type.cd.CdHouseHoldType.HVirtAccountIN;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;

public class ApStrikeEvntProcessorCD04 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCD04.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {

		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CD_DRAW) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CD_DRAW + "]，不能冲正！");
		
			// DP016 出入金登记簿登记

			String cardno = evnt.getCustac();
			String acctno = evnt.getTranac();
			String trandt = evnt.getEvent1();
			Long detlsq   = evnt.getTranno();
			
			
			E_TRANST status = E_TRANST.STRIKED;
			
			HVirtAccountIN cplAccountIN =  SysUtil.getInstance(HVirtAccountIN.class);
			cplAccountIN.setCardno(cardno);
			cplAccountIN.setAcctno(acctno);
			cplAccountIN.setTrandt(trandt);
			cplAccountIN.setDetlsq(detlsq);
			
			SysUtil.getInstance(IoCdHouseHoldSvcType.class).drawVirtAcctStrike(cplAccountIN);

			bizlog.debug("对公账户一户通虚户支取冲正处理结束=====================");
	}

}
