package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppSmry;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppSmryDao;
import cn.sunline.ltts.busi.sys.errors.ApError;

/**
 * 
 * <p>Title:ApSmryTools</p>
 * <p>Description: 摘要信息工具类  </p>
 * @author cuijia
 * @date   2017年6月19日
 */
public class ApSmryTools {
	
	/**
	 * 
	 * <p>Title:getText </p>
	 * <p>Description:	根据摘要码获取摘要信息</p>
	 * @author cuijia
	 * @date   2017年6月19日 
	 * @param summaryCode
	 * @return
	 */
	public static String getText(String summaryCode) {
		return getAppSmry(summaryCode).getSmryds();
	}
	
	/**
	 * 
	 * <p>Title:getAppSmry </p>
	 * <p>Description:	根据摘要码获取表数据</p>
	 * @author cuijia
	 * @date   2017年6月19日 
	 * @param summaryCode
	 * @return
	 */
	private static AppSmry getAppSmry(String summaryCode){
	    AppSmry summary = AppSmryDao.selectOne_odb1(summaryCode, false);

        if (CommUtil.isNull(summary)) {
            throw ApError.Aplt.E0058(summaryCode);
        }
        return summary;
	}
}
