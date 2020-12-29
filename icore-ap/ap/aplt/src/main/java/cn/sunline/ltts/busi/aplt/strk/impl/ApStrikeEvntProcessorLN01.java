package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.IoLnStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.ln.IoLnAcctInfoSvtp;
import cn.sunline.ltts.busi.iobus.servicetype.ln.IoLnAcctInfoSvtp.LnAcctInfo.Output;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoHotCtrlStrikeIn;
import cn.sunline.ltts.busi.iobus.type.ln.IoLnStrikeType.ProcLnRepayStrikeIn;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.LnError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.LnEnumType.E_EPCHPL;
import cn.sunline.ltts.busi.sys.type.LnEnumType.E_REPYTP;

public class ApStrikeEvntProcessorLN01 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorLN01.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("贷款还款冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_LNPRPY) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_LNPRPY + "]，不能冲正！");
		//add by wuwei 20180512 增加热点机制	
		// 贷款不允许隔日冲账
		if (CommUtil.compare(CommTools.prcRunEnvs().getTrandt(), evnt.getTrandt()) > 0) {
			throw LnError.geno.E0001("此交易[流水：" + evnt.getTransq() + "]不允许隔日冲正！");
		}	
		Output out=SysUtil.getInstance(IoLnAcctInfoSvtp.LnAcctInfo.Output.class);
		SysUtil.getInstance(IoLnAcctInfoSvtp.class).prcLnAcctInfo(evnt.getTranac(), out);
		if(SysUtil.getInstance(IoHotCtrlSvcType.class).selHcpDefn(out.getAcctInfo().getProdcd()) == E_YES___.YES){
			if (bizlog.isDebugEnabled())
				bizlog.debug("热点账户冲正处理开始=====================");		
			IoHotCtrlStrikeIn ioHotCtrlStrikeIn = SysUtil.getInstance(IoHotCtrlStrikeIn.class);
			//ioHotCtrlStrikeIn.setAmntcd(evnt.getAmntcd());
			ioHotCtrlStrikeIn.setAmntcd(E_AMNTCD.CR);
			ioHotCtrlStrikeIn.setHcacct(out.getAcctInfo().getProdcd());
			ioHotCtrlStrikeIn.setTrandt(evnt.getTrandt());
			ioHotCtrlStrikeIn.setTransq(evnt.getTransq());	
			ioHotCtrlStrikeIn.setTranam(evnt.getTranam());
			SysUtil.getInstance(IoHotCtrlStrikeSvcType.class).hotCtrlGeneralStrike(ioHotCtrlStrikeIn);			
		}else{
			// 调用冲正服务
			ProcLnRepayStrikeIn info = SysUtil.getInstance(ProcLnRepayStrikeIn.class);
			info.setTrandt(evnt.getEvent1());
			info.setTransq(evnt.getEvent2());
			info.setRepytp(CommUtil.toEnum(E_REPYTP.class, evnt.getEvent3()));
			info.setLncfno(evnt.getEvent4());
			info.setEpchpl(CommUtil.toEnum(E_EPCHPL.class, evnt.getEvent5()));
			SysUtil.getInstance(IoLnStrikeSvcType.class).procLoanPaymentStrike(info);
		}
			
		if (bizlog.isDebugEnabled())
			bizlog.debug("贷款还款冲正处理结束=====================");
	}

}
