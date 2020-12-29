package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.IoInWriteOff;
import cn.sunline.ltts.busi.iobus.type.IoInWriteOffComplex.NestcmRbInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorIN06 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorIN06.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("隔日错账冲正交易冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_NESTBK) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_NESTBK + "]，不能冲正！");

			// IN06 隔日错账冲正交易冲正
			NestcmRbInput nestcmRbInput = CommTools.getInstance(NestcmRbInput.class);
			nestcmRbInput.setTrandt(evnt.getTrandt());
			nestcmRbInput.setTransq(evnt.getMntrsq());
			SysUtil.getInstance(IoInWriteOff.class).nestcmRb(nestcmRbInput);

		
		if (bizlog.isDebugEnabled())
			bizlog.debug("隔日错账冲正交易冲正处理结束=====================");
	}

}
