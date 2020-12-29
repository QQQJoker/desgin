package cn.sunline.ltts.busi.bsap;

import cn.sunline.adp.cedar.base.boot.plugin.IReplaceExtension;
import cn.sunline.adp.cedar.base.engine.data.DataArea;

public interface BsapProcessManager extends IReplaceExtension{
	
	public static String POINT = "bsap.process";
	/**
	 * 获得法人代码
	 * <li>只有集中式系统且存在DRS和配置为“是否支持多法人路由模式”为“否”
	 * @param dataArea
	 * @return
	 */
	public String getCorpno(DataArea dataArea);
}
