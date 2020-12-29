package cn.sunline.ltts.busi.aplt.spi.impl;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.busi.aplt.spi.KnsTranManager;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTran;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;

@SPIMeta
public class KnsTranManagerImpl implements KnsTranManager {

	@Override
	public KnsTran getKnsTran() {
		RunEnvs runEnv = SysUtil.getTrxRunEnvs();

		KnsTran tran = SysUtil.getInstance(KnsTran.class);
		tran.setAuthus(runEnv.getAuthvo().getAuthus());
		tran.setServtp(runEnv.getServtp());

		return tran;
	}

}
