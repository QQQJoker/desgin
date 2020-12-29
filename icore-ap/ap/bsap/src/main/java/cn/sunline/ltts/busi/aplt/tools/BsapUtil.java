package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.adp.cedar.service.router.drs.config.DrsClientConfig;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;

public class BsapUtil {

	/**
	 * 返回是否由DRS支持多法人模式
	 * <li>DRS服务.是否支持多法人路由模式=false</li>
	 * @return
	 */
	public static boolean isMutilCorpnoMode() {
		DrsClientConfig drs = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager()
				.getConfig(DrsClientConfig.class);
		return drs == null ? false : drs.isMutilCorpnoMode();
	}

}
