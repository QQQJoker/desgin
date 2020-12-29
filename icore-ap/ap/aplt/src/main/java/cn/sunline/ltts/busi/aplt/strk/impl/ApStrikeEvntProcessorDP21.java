package cn.sunline.ltts.busi.aplt.strk.impl;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.ac.IoAcAccountServ;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.type.ac.IoAcServType.IoAccounttingIntf;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;

public class ApStrikeEvntProcessorDP21 implements ApStrikeEvntProcessor {

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_ACTING) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_ACTING + "]，不能冲正！");
		
		String busino = evnt.getTranac();
		BigDecimal tranam = evnt.getTranam();
		E_AMNTCD amntcd = evnt.getAmntcd();
		// 登记会计流水开始
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCuacno(busino); //记账账号-登记核算代码
        cplIoAccounttingIntf.setAcseno(busino); //子账户序号-登记核算代码
        cplIoAccounttingIntf.setAcctno(busino); //负债账号-登记核算代码
        cplIoAccounttingIntf.setProdcd(busino); //产品编号-登记核算代码
        cplIoAccounttingIntf.setDtitcd(busino); //核算口径-登记核算代码
        cplIoAccounttingIntf.setCrcycd(evnt.getCrcycd()); //币种
        cplIoAccounttingIntf.setAcctdt(CommToolsAplt.prcRunEnvs().getTrandt());// 应入账日期
        cplIoAccounttingIntf.setMntrsq(CommToolsAplt.prcRunEnvs().getMntrsq()); //主交易流水
        cplIoAccounttingIntf.setTrandt(CommToolsAplt.prcRunEnvs().getTrandt()); //交易日期 
        cplIoAccounttingIntf.setAcctbr(CommToolsAplt.prcRunEnvs().getTranbr()); //账务机构
        cplIoAccounttingIntf.setCorpno(CommToolsAplt.prcRunEnvs().getCorpno());
        if(colour == E_COLOUR.RED){
        	tranam = tranam.negate();
        }else{
        	if(amntcd == E_AMNTCD.DR){
        		amntcd = E_AMNTCD.CR;
        	}else if(amntcd == E_AMNTCD.CR){
        		amntcd = E_AMNTCD.DR;
        	}else if(amntcd == E_AMNTCD.PY){
        		amntcd = E_AMNTCD.RV;
        	}else if(amntcd == E_AMNTCD.RV){
        		amntcd = E_AMNTCD.PY;
        	}else{
        		throw ApError.Aplt.E0000("不支持的记账方向[" + amntcd + "]！");
        	}
        }
        cplIoAccounttingIntf.setAmntcd(amntcd); //借贷标志
        cplIoAccounttingIntf.setTranam(tranam); //交易金额
        
        cplIoAccounttingIntf.setAtowtp(CommUtil.toEnum(E_ATOWTP.class, evnt.getEvent1())); //会计主体类型-手续费
        cplIoAccounttingIntf.setTrsqtp(CommUtil.toEnum(E_ATSQTP.class, evnt.getEvent2())); //会计流水类型-账务流水
        cplIoAccounttingIntf.setBltype(CommUtil.toEnum(E_BLTYPE.class, evnt.getEvent3())); //余额属性-本金科目
        cplIoAccounttingIntf.setTranms(evnt.getEvent4());								   //交易信息
        cplIoAccounttingIntf.setServtp(CommToolsAplt.prcRunEnvs().getServtp());
        //登记会计流水
        SysUtil.getInstance(IoAcAccountServ.class).ioAccountting(cplIoAccounttingIntf);
        
	}

}
