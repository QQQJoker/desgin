package cn.sunline.ltts.busi.hc.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.ApltEngineContext;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefnDao;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpSydt;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpSydtDao;
import cn.sunline.ltts.busi.sys.errors.HcError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLNCDR;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HCSTAS;

/**
 * <p>Title:HotCtrlUtil</p>
 * <p>Description: 热点机制工具类</p>
 * 
 * @author yanggx
 * @date 2018年4月27日
 */
public class HotCtrlUtil {

    // 热点控制KEY
    private final static String HOT_CTRL_KEY = "HOT_CTRL_KEY";
    // 热点控制日期KEY
    public final static String HOT_CTRL_DATE_KEY = "HOT_CTRL_DATE_KEY";

    /**
     * 检查给定的账号或产品号等是否属于热点控制定义
     * 
     * @param hcmain 热点主体（账号或产品号等）
     * @return null-表示非热点，非空-表示为热点
     */
    public static HcpDefn checkHotMainAndReturn(String hcmain) {
        if (hcmain == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, HcpDefn> hotMap = (Map<String, HcpDefn>) ApltEngineContext.getTxnTempObjMap().get(HOT_CTRL_KEY);

        if (hotMap == null) {
            ApltEngineContext.getTxnTempObjMap().put(HOT_CTRL_KEY, hotMap = new ConcurrentHashMap<String, HcpDefn>());
        }

        if (hotMap.containsKey(hcmain)) {
            return hotMap.get(hcmain);
        } else {
            HcpDefn defn = HcpDefnDao.selectOne_odb1(hcmain, false);
            if (checkHotMain(defn)) {
                hotMap.put(hcmain, defn);
                return defn;
            } else {
                return null;
            }
        }
    }

    /**
     * 检查给定的账号或产品号等是否属于热点控制定义
     * 
     * @param hcmain
     * @return boolean 是否热点控制
     */
    public static boolean checkHotMain(String hcmain) {
        return checkHotMainAndReturn(hcmain) != null;
    }

    /**
     * 获取热点主体中余额方向
     * 
     * @param hcmain 热点主体
     * @return E_BLNCDR 余额方向
     */
    public static E_BLNCDR getHotCtrlBlncdr(String hcmain) {
        if (checkHotMainAndReturn(hcmain) != null) {
            return checkHotMainAndReturn(hcmain).getBlncdr();
        }

        return null;
    }

    /**
     * 判断热点状态及记录是否生效
     * 
     * @param defn
     * @return boolean 是否生效
     */
    private static boolean checkHotMain(HcpDefn defn) {
        if (defn == null) {
            return false;
        }

        String trandt = CommTools.prcRunEnvs().getTrandt();
        String effcdt = defn.getEffcdt();
        String expidt = defn.getExpidt();

        if (E_HCSTAS.ZC == defn.getHcstas() && CommUtil.compare(trandt, effcdt) >= 0 && CommUtil.compare(trandt, expidt) < 0) {
            return true;
        }

        return false;
    }

    /**
     * 获取热点控制日期
     * 
     * @return String-热点日期
     */
    public static String getHotCtrlDate() {
        Object hotCtrlDate = ApltEngineContext.getTxnTempObjMap().get(HOT_CTRL_DATE_KEY);

        if (hotCtrlDate == null) {
            // 查询热点控制日期
            HcpSydt tblHcpSydt = HcpSydtDao.selectOne_odb1(CommToolsAplt.prcRunEnvs().getCorpno(), false);

            String hcsydt = tblHcpSydt.getHcsydt();

            ApltEngineContext.getTxnTempObjMap().put(HOT_CTRL_DATE_KEY, hcsydt);

            return hcsydt;
        } else {
            return hotCtrlDate.toString();
        }
    }

    /**
     * 获取分表号
     */
    public static String getTabnum(DataArea paramData){
    	String tabnum="";//默认第一张表
		if(CommUtil.isNotNull(paramData.getInput()) && CommUtil.isNotNull(paramData.getInput().get("tabnum"))){
			tabnum = paramData.getInput().get("tabnum").toString();//分表号
			if(!CommTools.isNum(tabnum)){
				HcError.HcGen.E0000("输入参数分表号不正确！");
			}
		}else{
			HcError.HcGen.E0000("输入参数分表号不存在！");
		}
		if(CommUtil.equals(tabnum, "0")){
			tabnum="";
		}
		return tabnum;	
    }
    
    /**
     * 设置热点控制日期（热点日切时调用）
     */
    public static void setHotCtrlDate(String hcsydt) {
        ApltEngineContext.getTxnTempObjMap().put(HOT_CTRL_DATE_KEY, hcsydt);
    }


}
