package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.IoInWriteOff;
import cn.sunline.ltts.busi.iobus.type.IoInWriteOffComplex.IavccmRbInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApStrikeEvntProcessorIN05 implements ApStrikeEvntProcessor {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrikeEvntProcessorIN05.class);

	@Override
	public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
		if (bizlog.isDebugEnabled())
			bizlog.debug("套平入账交易冲正处理开始=====================");
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_INACOT) != 0)
			throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_INACOT + "]，不能冲正！");

		// IN05 套平入账交易冲正

		IavccmRbInput iavccmRbInput = CommTools.getInstance(IavccmRbInput.class);
		// iavccmRbInput.setTrandt(CommToolsAplt.prcRunEnvs().getTrandt());
		iavccmRbInput.setTrandt(evnt.getBgindt());// 原交易日期
		iavccmRbInput.setTransq(evnt.getMntrsq());// 主交易流水
		SysUtil.getInstance(IoInWriteOff.class).iavccmRb(iavccmRbInput);

		if (bizlog.isDebugEnabled())
			bizlog.debug("套平入账交易冲正处理结束=====================");
	}

}
