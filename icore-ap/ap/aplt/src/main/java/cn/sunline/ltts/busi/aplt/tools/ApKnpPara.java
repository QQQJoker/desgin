package cn.sunline.ltts.busi.aplt.tools;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;

/**
 * knp_para参数表工具类
 * 
 * @author cuijia
 *
 */
public class ApKnpPara {
	
	/**
	 * 获取参数参数值
	 * 
	 * @param parmcd 
	 * @param pmkey1
	 * @param pmkey2
	 * @param pmkey3
	 * @return
	 */
	public static KnpPara getKnpPara(String parmcd,String pmkey1,String pmkey2,String pmkey3){
		return getKnpPara(parmcd, pmkey1, pmkey2, pmkey3, true);
	}
	
	/**
	 * 
	 * <p>Title:getKnpPara </p>
	 * <p>Description:	获取参数表数据</p>
	 * @author Cuijia
	 * @date   2017年8月31日 
	 * @param parmcd
	 * @param pmkey1
	 * @param pmkey2
	 * @param pmkey3
	 * @param throwabled 是否空值抛异常
	 * @return
	 */
	public static KnpPara getKnpPara(String parmcd,String pmkey1,String pmkey2,String pmkey3,boolean throwabled){
	    KnpPara knpPara = CommTools.KnpParaQryByCorpno(parmcd, pmkey1, pmkey2, pmkey3, false);
        if(CommUtil.isNull(knpPara) && throwabled ){
            throw ApError.Aplt.E0065(parmcd, pmkey1, pmkey2, pmkey3);
        }
        return knpPara;
	}
	
	/**
	 * 获取参数参数值
	 * 
	 * @param parmcd
	 * @param pmkey1
	 * @param pmkey2
	 * @return
	 */
	public static KnpPara getKnpPara(String parmcd,String pmkey1,String pmkey2){
		return getKnpPara(parmcd, pmkey1, pmkey2, ApConstants.WILDCARD);
	}
	
	/**
	 * 获取参数参数值
	 * 
	 * @param parmcd
	 * @param pmkey1
	 * @return
	 */
	public static KnpPara getKnpPara(String parmcd,String pmkey1){
		return getKnpPara(parmcd, pmkey1, ApConstants.WILDCARD, ApConstants.WILDCARD);
	}
	
	/**
	 * 
	 * <p>Title:getKnpPara </p>
	 * <p>Description:	</p>
	 * @author Cuijia
	 * @date   2017年8月31日 
	 * @param parmcd
	 * @param pmkey1
	 * @param throwabled
	 * @return
	 */
	public static KnpPara getKnpPara(String parmcd,String pmkey1,boolean throwabled){
	    return getKnpPara(parmcd, pmkey1, ApConstants.WILDCARD, ApConstants.WILDCARD,throwabled);
	}
	
	/**
	 * 
	 * <p>Title:listKnpPara </p>
	 * <p>Description:	根据parmcd查询多条记录</p>
	 * @author cuijia
	 * @date   2017年6月20日 
	 * @param parmcd
	 * @return
	 */
	public static List<KnpPara> listKnpPara(String parmcd,boolean throwabled){
	    List<KnpPara> knpParaDO = KnpParaDao.selectAll_odb2(parmcd, false);
	    if(CommUtil.isNull(knpParaDO) && throwabled){
            throw ApError.Aplt.E0065(parmcd, null, null, null);
        }
	    return knpParaDO;
	}

}
