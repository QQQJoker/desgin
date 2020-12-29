package cn.sunline.ltts.busi.aplt.strk.impl;
import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType.ProcDrawStrike;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlSvcType;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnbFroz;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoHotCtrlStrikeIn;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcDrawStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.DpError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FRLMTP;
import cn.sunline.ltts.busi.sys.type.DpEnumType.E_FROZST;

public class ApStrikeEvntProcessorDP04 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorDP04.class);
	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DPDRAW) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DPDRAW + "]，不能冲正！");
		
		//add by wuwei 20180512 增加热点机制
		if(SysUtil.getInstance(IoHotCtrlSvcType.class).selHcpDefn(evnt.getTranac()) == E_YES___.YES){
			if (bizlog.isDebugEnabled())
				bizlog.debug("热点账户冲正处理开始=====================");
			IoHotCtrlStrikeIn ioHotCtrlStrikeIn = SysUtil.getInstance(IoHotCtrlStrikeIn.class);
			ioHotCtrlStrikeIn.setAmntcd(evnt.getAmntcd());
			ioHotCtrlStrikeIn.setHcacct(evnt.getTranac());
			ioHotCtrlStrikeIn.setTrandt(evnt.getTrandt());
			ioHotCtrlStrikeIn.setTransq(evnt.getTransq());
			ioHotCtrlStrikeIn.setTranam(evnt.getTranam());
			SysUtil.getInstance(IoHotCtrlStrikeSvcType.class).hotCtrlGeneralStrike(ioHotCtrlStrikeIn);			
		}else{
			// DP04 支取 DP
			IoDpFrozSvcType ioDpFrozSvcType = SysUtil.getInstance(IoDpFrozSvcType.class);
			List<IoDpKnbFroz> listKnbFroz = ioDpFrozSvcType.qryKnbFroz(evnt.getCustac(), E_FROZST.VALID);
			for (IoDpKnbFroz ioDpKnbFroz : listKnbFroz) {
			if (ioDpKnbFroz.getFrlmtp() == E_FRLMTP.IN) {
				throw DpError.DeptComm.BNAS9965();
				}
			}
			ProcDrawStrike.Input input = SysUtil.getInstance(ProcDrawStrike.Input.class);
			ProcDrawStrikeInput cplIn = input.getStrikeInput();

			
			cplIn.setCustac(evnt.getCustac());// 电子账户
			cplIn.setAcctno(evnt.getTranac()); // 负债账号
			cplIn.setStacps(stacps);// 冲正冲账分类
			cplIn.setOrtrdt(evnt.getTrandt());// 原交易日期
			cplIn.setTranam(evnt.getTranam());// 交易金额
			cplIn.setAmntcd(evnt.getAmntcd());// 借贷标志
			cplIn.setColrfg(colour); // 红蓝字记账标识
			cplIn.setDetlsq(evnt.getTranno());// 原交易序号
			cplIn.setCrcycd(evnt.getCrcycd());
			if (CommUtil.isNotNull(evnt.getEvent1())) {
				cplIn.setInstam(new BigDecimal(evnt.getEvent1())); // 利息
			}
			if (CommUtil.isNotNull(evnt.getEvent2())) {
				cplIn.setAcctst(CommUtil.toEnum(E_DPACST.class, evnt.getEvent2())); // 账户状态
			}
			if (CommUtil.isNotNull(evnt.getEvent3())) {
				cplIn.setIntxam(new BigDecimal(evnt.getEvent3())); // 利息税
			}
			if (CommUtil.isNotNull(evnt.getEvent4())) {
	            cplIn.setPyafam(new BigDecimal(evnt.getEvent4())); // 利息税
	            cplIn.setPydlsq(Long.valueOf(evnt.getEvent5())); //原追缴金额流水
	        }
			SysUtil.getInstance(IoDpStrikeSvcType.class).procDrawStrike(input);
		}
		
	}

}
