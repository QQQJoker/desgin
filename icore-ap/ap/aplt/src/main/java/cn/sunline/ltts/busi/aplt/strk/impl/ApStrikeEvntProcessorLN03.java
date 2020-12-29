package cn.sunline.ltts.busi.aplt.strk.impl;

import java.math.BigDecimal;

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
import cn.sunline.ltts.busi.iobus.type.ln.IoLnStrikeType.ProcLnLendStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.LnError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorLN03 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorLN03.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("贷款放款冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_LNLEND) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_LNLEND + "]，不能冲正！");

		//add by wuwei 20180512 增加热点机制	
		// 贷款不允许隔日冲账
		if (CommUtil.compare(CommTools.prcRunEnvs().getTrandt(), evnt.getTrandt()) > 0) {
			throw LnError.detail.E0467(evnt.getTransq());
		}
		Output out=SysUtil.getInstance(IoLnAcctInfoSvtp.LnAcctInfo.Output.class);
		SysUtil.getInstance(IoLnAcctInfoSvtp.class).prcLnAcctInfo(evnt.getTranac(), out);
		if(SysUtil.getInstance(IoHotCtrlSvcType.class).selHcpDefn(out.getAcctInfo().getProdcd()) == E_YES___.YES){
			if (bizlog.isDebugEnabled())
				bizlog.debug("热点账户冲正处理开始=====================");				
			IoHotCtrlStrikeIn ioHotCtrlStrikeIn = SysUtil.getInstance(IoHotCtrlStrikeIn.class);
			//ioHotCtrlStrikeIn.setAmntcd(evnt.getAmntcd());
			ioHotCtrlStrikeIn.setAmntcd(E_AMNTCD.DR);
			ioHotCtrlStrikeIn.setHcacct(out.getAcctInfo().getProdcd());
			ioHotCtrlStrikeIn.setTrandt(evnt.getTrandt());
			ioHotCtrlStrikeIn.setTransq(evnt.getTransq());	
			ioHotCtrlStrikeIn.setTranam(evnt.getTranam());
			SysUtil.getInstance(IoHotCtrlStrikeSvcType.class).hotCtrlGeneralStrike(ioHotCtrlStrikeIn);			
		}else{
			ProcLnLendStrikeInput cplLnLendStrikeIn = SysUtil.getInstance(ProcLnLendStrikeInput.class);

			cplLnLendStrikeIn.setCustac(evnt.getCustac()); // 电子账号
			cplLnLendStrikeIn.setAcctno(evnt.getTranac()); // 贷款账号
			cplLnLendStrikeIn.setCrcycd(evnt.getCrcycd()); // 币种
			cplLnLendStrikeIn.setStacps(stacps); // 冲正冲账类型
			cplLnLendStrikeIn.setLnnpbl(evnt.getTranam()); // 正常本金
			
			if (bizlog.isDebugEnabled())
				bizlog.debug("冲正登记簿的交易金额》》》》》》》》》》》》》》" + evnt.getTranam());
			cplLnLendStrikeIn.setLnopbl(new BigDecimal(evnt.getEvent3())); // 逾期本金
			cplLnLendStrikeIn.setLndpbl(new BigDecimal(evnt.getEvent4())); // 呆滞本金
			cplLnLendStrikeIn.setLnbpbl(new BigDecimal(evnt.getEvent5())); // 呆账本金
			cplLnLendStrikeIn.setTrandt(evnt.getEvent1()); // 交易日期
			cplLnLendStrikeIn.setTransq(evnt.getEvent2()); // 交易流水
			
			if (bizlog.isDebugEnabled())
				bizlog.debug("贷款冲正服务输入正常本金》》》》》》》》》》》》》》" + evnt.getTranam());
			SysUtil.getInstance(IoLnStrikeSvcType.class).prcLnLendStrike(cplLnLendStrikeIn);
		}
		
		if (bizlog.isDebugEnabled())
			bizlog.debug("贷款放款冲正处理结束=====================");
	}

}
