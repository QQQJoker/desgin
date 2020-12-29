package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.BusiTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType.ProcSaveStrike;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlSvcType;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoHotCtrlStrikeIn;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcSaveStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorDP03 implements ApStrikeEvntProcessor {

	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorDP03.class);
	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_DPSAVE) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_DPSAVE + "]，不能冲正！");

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
			// DP03 存入 DP
			ProcSaveStrike.Input input = SysUtil.getInstance(ProcSaveStrike.Input.class);
			ProcSaveStrikeInput cplIn = input.getStrikeInput();
			cplIn.setCustac(evnt.getCustac());// 电子账户
			cplIn.setAcctno(evnt.getTranac()); // 负债账号
			cplIn.setStacps(stacps);// 冲正冲账分类
			if(BusiTools.getDistributedDeal()) {
				cplIn.setOrtrdt(evnt.getInpudt());// 原交易日期
			} else {
				cplIn.setOrtrdt(evnt.getTrandt());// 原交易日期
			}
			cplIn.setTranam(evnt.getTranam());// 交易金额
			cplIn.setAmntcd(evnt.getAmntcd());// 借贷标志
			cplIn.setColrfg(colour); // 红蓝字记账标识
			cplIn.setDetlsq(evnt.getTranno());// 原交易序号
			cplIn.setCrcycd(evnt.getCrcycd());
			SysUtil.getInstance(IoDpStrikeSvcType.class).procSaveStrike(input);
		}

	}

}
