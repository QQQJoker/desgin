package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoHotCtrlStrikeIn;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_CORRTG;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_INPTSR;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorIN01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorIN01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled()) {
			bizlog.debug("内部户贷方交易冲正处理开始=====================[%s]",evnt);
		}
		
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_INACCR) != 0) {
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_INACCR + "]，不能冲正！");
		}

		//热点
		if(SysUtil.getInstance(IoHotCtrlSvcType.class).selHcpDefn(evnt.getTranac()) == E_YES___.YES){
			if (bizlog.isDebugEnabled()) {
				bizlog.debug("热点账户冲正处理开始=====================");
			}
			IoHotCtrlStrikeIn ioHotCtrlStrikeIn = SysUtil.getInstance(IoHotCtrlStrikeIn.class);
			ioHotCtrlStrikeIn.setAmntcd(evnt.getAmntcd());
			ioHotCtrlStrikeIn.setHcacct(evnt.getTranac());
			ioHotCtrlStrikeIn.setTrandt(evnt.getTrandt());
			ioHotCtrlStrikeIn.setTransq(evnt.getTransq());
			ioHotCtrlStrikeIn.setTranam(evnt.getTranam());
			SysUtil.getInstance(IoHotCtrlStrikeSvcType.class).hotCtrlGeneralStrike(ioHotCtrlStrikeIn);			
		} else {
			// IN01 内部户贷方交易冲正
			IaAcdrInfo info = SysUtil.getInstance(IaAcdrInfo.class);
			info.setAcctno(evnt.getTranac());
			info.setCorrtg(E_CORRTG._1);
			info.setTranam(evnt.getTranam().negate());
			info.setQuotfs(E_YES___.NO);
			info.setCrcycd(evnt.getCrcycd());
			info.setSttsdt(evnt.getTrandt());
			info.setInptsr(E_INPTSR.GL03);
			info.setAmntcd(E_AMNTCD.CR);
			info.setSttssq(evnt.getTransq());// 交易流水
			info.setDscrtx(CommToolsAplt.prcRunEnvs().getRemark());// 冲账备注登记
	
			// 调用贷方服务
			SysUtil.getInstance(IoInAccount.class).ioInAccr(info);
		}
		if (bizlog.isDebugEnabled()) {
			bizlog.debug("处理结束=====================");
		}
	}

}
