package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInVirtualAccount;
import cn.sunline.ltts.busi.iobus.type.in.IoInVirtualType.IoInVirtualAccountStrkIN;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STRKTP;

/**
 * 跨节点补记流水 冲正
 * @author jiangyaming
 * 2018年6月21日 下午1:38:44
 */
public class ApStrikeEvntProcessorIN10 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorIN10.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled()) {
			bizlog.debug("处理开始=====================");
		}
		
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_VIRTBL) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_VIRTBL + "]，不能冲正！");
		
		//登记冲正
		IoInVirtualAccountStrkIN virtualAccountStrkIN = CommTools.getInstance(IoInVirtualAccountStrkIN.class);
		virtualAccountStrkIN.setAcctno(evnt.getTranac()); // 内部户账号
		virtualAccountStrkIN.setAmntcd(evnt.getAmntcd()); // 借贷标志
		virtualAccountStrkIN.setDetlsq(evnt.getTranno()); // 交易序号
		virtualAccountStrkIN.setMsacdt(evnt.getEvent1()); //日期
		virtualAccountStrkIN.setOrigtq(evnt.getEvent2()); //流水
		virtualAccountStrkIN.setTranam(evnt.getTranam()); // 交易金额
		virtualAccountStrkIN.setVirtno(evnt.getCustac()); //虚拟子户号
		virtualAccountStrkIN.setSmrycd("CZ");
		virtualAccountStrkIN.setColrfg(E_COLOUR.RED);
		virtualAccountStrkIN.setRemark("内部户虚户记账冲正");
		//virtualAccountStrkIN.setStrktp(E_STRKTP.TODAY);
		
		CommTools.getInstance(IoInVirtualAccount.class).strkBookVirtualAccount(virtualAccountStrkIN);
	}

}
