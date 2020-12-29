package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpGlbl;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpGlblDao;
import cn.sunline.ltts.busi.sys.errors.ApError;

/**
 * 全局参数表工具类
 * 
 * @author cuijia
 *
 */
public class ApKnpGlbl {
	
	/**
	 * 获取参数参数值
	 * 
	 * @param parmcd 
	 * @param pmkey1
	 * @param pmkey2
	 * @param pmkey3
	 * @return
	 */
	public static KnpGlbl getKnpGlbl(String parmcd,String pmkey1,String pmkey2,String pmkey3){
		KnpGlbl knpGlbl = KnpGlblDao.selectOne_odb1(parmcd, pmkey1, pmkey2, pmkey3, false);
		if(CommUtil.isNull(knpGlbl)){
			throw ApError.Aplt.E0062(parmcd, pmkey1, pmkey2, pmkey3);
		}
		return knpGlbl;
	}
	
	/**
	 * 获取参数参数值
	 * 
	 * @param parmcd
	 * @param pmkey1
	 * @param pmkey2
	 * @return
	 */
	public static KnpGlbl getKnpGlbl(String parmcd,String pmkey1,String pmkey2){
		return getKnpGlbl(parmcd, pmkey1, pmkey2, ApConstants.WILDCARD);
	}
	
	/**
	 * 获取参数参数值
	 * 
	 * @param parmcd
	 * @param pmkey1
	 * @return
	 */
	public static KnpGlbl getKnpGlbl(String parmcd,String pmkey1){
		return getKnpGlbl(parmcd, pmkey1, ApConstants.WILDCARD, ApConstants.WILDCARD);
	}

}
