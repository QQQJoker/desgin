package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
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
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 跨节点补记流水 冲正
 * @author jiangyaming
 * 2018年6月21日 下午1:38:44
 */
public class ApStrikeEvntProcessorIN09 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorIN09.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled()) {
			bizlog.debug("处理开始=====================");
		}
		
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_BALNCE) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_BALNCE + "]，不能冲正！");

//        if(CommUtil.compare(evnt.getTrandt(),CommTools.prcRunEnvs().getTrandt()) != 0) {//不冲隔日的账
//        	return;
//        }
        
        IoAccounttingIntf cplIoAccounttingIntf = SysUtil.getInstance(IoAccounttingIntf.class);
        cplIoAccounttingIntf.setCorpno(evnt.getCorpno());
        cplIoAccounttingIntf.setAcctno(evnt.getTranac());
        cplIoAccounttingIntf.setProdcd(ApUtil.DEFAULT_PROD_CODE);
        cplIoAccounttingIntf.setDtitcd(evnt.getEvent3());
        cplIoAccounttingIntf.setCrcycd(evnt.getCrcycd());
        cplIoAccounttingIntf.setTranam(evnt.getTranam());
        if(StringUtil.equals(evnt.getEvent4(),"true")) {//是 补跨日跨dcn的
        	cplIoAccounttingIntf.setAcctdt(evnt.getInpudt());
            cplIoAccounttingIntf.setTrandt(evnt.getInpudt());
        } else {
        	cplIoAccounttingIntf.setAcctdt(CommToolsAplt.prcRunEnvs().getTrandt());// 应入账日期
            cplIoAccounttingIntf.setTrandt(CommToolsAplt.prcRunEnvs().getTrandt());
        }
        cplIoAccounttingIntf.setMntrsq(CommToolsAplt.prcRunEnvs().getMntrsq());
        cplIoAccounttingIntf.setAcctbr(CommToolsAplt.prcRunEnvs().getTranbr());//账务机构
        cplIoAccounttingIntf.setServtp(CommToolsAplt.prcRunEnvs().getServtp());//渠道      
        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.IN);
        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
        
		if(CommUtil.compare(evnt.getAmntcd(),E_AMNTCD.DR) == 0) {//冲借方
	        cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
		} else if(CommUtil.compare(evnt.getAmntcd(),E_AMNTCD.CR) == 0) {//冲贷方
			cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR);
		}
		bizlog.debug("登记会计流水[%s]",cplIoAccounttingIntf);
		//登记会计流水
        SysUtil.getInstance(IoAcAccountServ.class).ioAccountting(cplIoAccounttingIntf);

		if (bizlog.isDebugEnabled()) {
			bizlog.debug("跨节点补记流水冲正结束=====================");
		}
	}

}
