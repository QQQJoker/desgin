package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.busi.iobus.servicetype.ue.IoRegisterBookStrike;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;

public class ApStrikeEvntProcessorCM04 implements ApStrikeEvntProcessor {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCM04.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CM_IOBLL) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CM_IOBLL + "]，不能冲正！");
		
		  bizlog.debug("统一记账一借一贷登记簿登记冲正处理结束=====================");
		 //CM04 出入金登记簿登记
		 String transq = evnt.getEvent1();
		 String trandt = evnt.getEvent2();
		 E_TRANST status = E_TRANST.STRIKED;
			
		 SysUtil.getInstance(IoRegisterBookStrike.class).registerAcInOutBookStrike(transq, trandt, status);

		bizlog.debug("统一记账一借一贷登记簿登记冲正处理结束=====================");
	}

}
