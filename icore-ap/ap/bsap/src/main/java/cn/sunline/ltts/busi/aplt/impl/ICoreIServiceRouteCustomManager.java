package cn.sunline.ltts.busi.aplt.impl;

import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.cedar.service.router.drs.spi.IServiceRouteCustomManager;
import cn.sunline.ltts.busi.aplt.tools.CommTools;

//public class ICoreIServiceRouteCustomManager implements IServiceRouteCustomManager {
//
//	@Override
//	public String getCurrentCorpno() {
//		return CommTools.prcRunEnvs().getCorpno();
//	}
//
//	@Override
//	public boolean isOutCallOtherCorpno(String currentCorpno, String targetCorpno, boolean isMutilCorpnoMode) {
//		if (!isMutilCorpnoMode)
//			return false;
//		if (StringUtil.isBlank(currentCorpno) || StringUtil.isBlank(targetCorpno))
//			return false;
//		if (!currentCorpno.equals(targetCorpno))
//			return true;
//		return false;
//	}
//
//}
