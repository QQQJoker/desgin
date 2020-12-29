package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class LogManager {
	
	public static BizLog getBizLog(String name) {
		
		return BizLogUtil.getBizLog(name);
	}
	
	public static BizLog getBizLog(Class<?> clazz) {
		return BizLogUtil.getBizLog(clazz);
	}
}
