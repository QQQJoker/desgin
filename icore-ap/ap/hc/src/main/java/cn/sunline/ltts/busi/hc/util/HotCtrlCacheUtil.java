package cn.sunline.ltts.busi.hc.util;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.cache.BsapRedisUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbBlce;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbBlceDao;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.in.IoInQueryTypes.IoInAcctTmp;
import cn.sunline.ltts.busi.sys.errors.HcError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLNCDR;

/**
 * <p>
 * Title:HotCtrlCacheUtil
 * </p>
 * <p>
 * Description: 热点机制Redis缓存工具类
 * </p>
 * TODO 目前简单用exist(key)，然后setData变更值会存在线程安全问题,但目前平台提供API缺少incrByFloat原子操作，改造逻辑如下：
 * 1.exist判断键是否存在，如果存在，则用原子递增incrByFloat(key,amount)，更新redis余额
 * 2.如果不存在，则数据库查询余额，然后采用原子操作setNx(key,amount)设置值,看是否成功，如果成功说明redis还未初始化该账户余额，
 *   不成功，说明该账户已经被别的线程初始化，则直接走上面1的逻辑
 * @author liuych
 * @date 2018年4月27日
 */
public class HotCtrlCacheUtil {

    // 日志信息
    private static final BizLog bizlog = BizLogUtil.getBizLog(HotCtrlCacheUtil.class);

    // 热点机制
    private final static String HotCtrl = "HotCtrl:";
    // 当前余额
    private final static String CurBal = ":CurBal";
    // 余额明细
    private final static String BalDetl = ":BalDetl:";
    // 分隔符
    private final static String Separator = ":";
    
    
    
    /**
     * <p>Title: getHotCtrlCurBalData</p>
     * <p>Description: 判断缓存中是否存在余额</p>
     * 
     * @author jizhirong
     * @date 2018年5月15日
     * @param hcmain
     *        热点主体号
     * @return BigDecimal 是否存在余额
     * @throws Exception 
     */
    public static boolean isExistBalance(String hcmain) {
    	 BsapRedisUtil lttsCache = BsapRedisUtil.getInstance();
    	 
         StringBuffer key = new StringBuffer();
         key.append(HotCtrl);
         key.append(hcmain);
         key.append(CurBal);
         
        try {
			return lttsCache.exist(key.toString());
		} catch (Exception e) {
			bizlog.error("获取缓存键值发生错误", e);
            throw HcError.HcComm.E0003();
		}
    }

    /**
     * <p>Title: getHotCtrlCurBalData</p>
     * <p>Description: 获取热点主体号余额</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @return BigDecimal 当前余额
     */
    public static BigDecimal getHotCtrlCurBalData(String hcmain) {
        /** 获取热点主体号余额 */
        return getBalanceData(hcmain);
    }

    /**
     * <p>Title: getHotCtrlAccrualData</p>
     * <p>Description: 根据日期获取热点主体号发生额</p>
     * 
     * @author jizhirong
     * @date 2018年5月4日
     * @param hcmain
     *        热点主体号
     * @param lastdt 日期
     * @return BigDecimal 发生额余额
     */
    public static BigDecimal getHotCtrlAccrualData(String hcmain, String lastdt) {
        BigDecimal accrua = BigDecimal.ZERO;

        BsapRedisUtil lttsCache = BsapRedisUtil.getInstance();

        StringBuffer key = new StringBuffer();
        key.append(HotCtrl);
        key.append(hcmain);
        key.append(Separator);
        key.append(lastdt);

        try {
            if (lttsCache.exist(key.toString())) {
                /** 获取热点主体号发生额 */
                accrua = new BigDecimal(lttsCache.selectValueByKey(key.toString()).toString());
            }
        } catch (Exception e) {
            bizlog.error("根据日期获取热点主体号发生额错误：", e);
            throw HcError.HcComm.E0003();
        }

        return accrua;
    }

    /**
     * <p>Title:confirmHotCtrlData</p>
     * <p>Description: 确认占用额度</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param blncdr
     *        余额方向
     * @param amntcd
     *        借贷方向
     * @param value
     *        交易金额
     */
    public static void confirmHotCtrlData(String hcmain, E_BLNCDR blncdr, E_AMNTCD amntcd, BigDecimal value) {
        /** 计算发生额 */
        value = calBalanceData(blncdr, amntcd, value);

        /** 设置热点余额 */
        setBalanceData(hcmain, value);

        /** 设置热点发生额 */
        setAccrualData(hcmain, value);
    }

    /**
     * <p>Title:confirmHotCtrlData</p>
     * <p>Description: 确认占用额度</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param blncdr
     *        余额方向
     * @param amntcd
     *        借贷方向
     * @param value
     *        交易金额
     * @param transq
     *        交易流水号
     */
    public static void confirmHotCtrlData(String hcmain, E_BLNCDR blncdr, E_AMNTCD amntcd, BigDecimal value, String transq) {
        /** 计算发生额 */
        value = calBalanceData(blncdr, amntcd, value);

        /** 设置热点余额 */
        setBalanceData(hcmain, value);

        /** 设置热点发生额 */
        setAccrualData(hcmain, value);

        /** 设置热点占用明细 */
        setDetailData(hcmain, transq, value);
    }

    /**
     * <p>Title:releaseHotCtrlData</p>
     * <p>Description: 释放占用额度</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param blncdr
     *        余额方向
     * @param amntcd
     *        借贷方向
     * @param value
     *        交易金额
     */
    public static void releaseHotCtrlData(String hcmain, E_BLNCDR blncdr, E_AMNTCD amntcd, BigDecimal value) {
        /** 计算发生额 */
        value = calBalanceData(blncdr, amntcd, value);

        // 取负数
        value = BigDecimal.ZERO.subtract(value);

        /** 设置热点余额 */
        setBalanceData(hcmain, value);

        /** 设置热点发生额 */
        setAccrualData(hcmain, value);
    }

    /**
     * <p>Title:releaseHotCtrlData</p>
     * <p>Description: 释放占用额度</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param blncdr
     *        余额方向
     * @param amntcd
     *        借贷方向
     * @param value
     *        交易金额
     * @param transq
     *        交易流水号
     */
    public static void releaseHotCtrlData(String hcmain, E_BLNCDR blncdr, E_AMNTCD amntcd, BigDecimal value, String transq) {
        /** 计算发生额 */
        value = calBalanceData(blncdr, amntcd, value);

        // 取负数
        value = BigDecimal.ZERO.subtract(value);

        /** 设置热点余额 */
        setBalanceData(hcmain, value);

        /** 设置热点发生额 */
        setAccrualData(hcmain, value);

        /** 设置热点占用明细 */
        setDetailData(hcmain, transq, value);
    }

    /**
     * <p>Title:removeHotCtrlData</p>
     * <p>Description: 删除占用明细</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param transq
     *        交易流水号
     */
    public static void removeHotCtrlData(String hcmain) {
        /** 删除热点主体号余额 */
        removeBalanceData(hcmain);
    }

    /**
     * <p>Title:removeHotCtrlData</p>
     * <p>Description: 删除占用明细</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param transq
     *        交易流水号
     */
    public static void removeHotCtrlData(String hcmain, String transq) {
        /** 删除热点主体号余额 */
        removeBalanceData(hcmain);

        /** 删除热点主体号占用明细 */
        removeDetailData(hcmain, transq);
    }

    /**
     * <p>Title:setBalanceData</p>
     * <p>Description: 设置热点余额</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param value
     *        交易金额
     */
    public static void setBalanceData(String hcmain, BigDecimal value) {
        BsapRedisUtil lttsCache = BsapRedisUtil.getInstance();

        StringBuffer key = new StringBuffer();
        key.append(HotCtrl);
        key.append(hcmain);
        key.append(CurBal);

        try {
            // 如果缓存已经存在则加上当前发生额，否则查询账户余额后
        	
            if (lttsCache.exist(key.toString())) {
                BigDecimal bigBalanceData = new BigDecimal(lttsCache.selectValueByKey(key.toString()).toString());

                bigBalanceData = bigBalanceData.add(value);

                lttsCache.setData(key.toString(), bigBalanceData.toString());
            } else {
                /** 初始化缓存金额 */
                BigDecimal bigBalanceData = initBalanceData(hcmain);

                bigBalanceData = bigBalanceData.add(value);

                lttsCache.setData(key.toString(), bigBalanceData.toString());
            }
        } catch (Exception e) {
            bizlog.error("根据日期获取热点主体号发生额错误：", e);
            throw HcError.HcComm.E0003();
        }
    }

    /**
     * <p>Title:setAccrualData</p>
     * <p>Description: 设置热点发生额</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param value
     *        交易金额
     */
    private static void setAccrualData(String hcmain, BigDecimal value) {
        BsapRedisUtil lttsCache = BsapRedisUtil.getInstance();

        StringBuffer key = new StringBuffer();
        key.append(HotCtrl);
        key.append(hcmain);
        key.append(Separator);
        key.append(CommTools.prcRunEnvs().getTrandt());

        try {
            // 如果缓存已经存在则加上当前发生额，否则将此次发生额作为初始化金额
            if (lttsCache.exist(key.toString())) {
                BigDecimal bigAccrualData = new BigDecimal(lttsCache.selectValueByKey(key.toString()).toString());

                bigAccrualData = bigAccrualData.add(value);

                lttsCache.setData(key.toString(), bigAccrualData.toString());
            } else {
                /** 初始化缓存发生额 */
                lttsCache.setData(key.toString(), value.toString());
            }

            lttsCache.setData(key.toString(), value);
        } catch (Exception e) {
            bizlog.error("根据日期获取热点主体号发生额错误：", e);

            throw HcError.HcComm.E0003();
        }
    }

    /**
     * <p>Title:setDetailData</p>
     * <p>Description: 设置热点占用明细</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param transq
     *        交易流水号
     * @param value
     *        交易金额
     */
    private static void setDetailData(String hcmain, String transq, BigDecimal value) {
        BsapRedisUtil lttsCache = BsapRedisUtil.getInstance();

        StringBuffer key = new StringBuffer();
        key.append(HotCtrl);
        key.append(hcmain);
        key.append(BalDetl);
        key.append(transq);

        try {
            lttsCache.setData(key.toString(), value);
        } catch (Exception e) {
            bizlog.error("根据日期获取热点主体号发生额错误：", e);
            throw HcError.HcComm.E0003();
        }
    }

    /**
     * <p>Title:getBalanceData</p>
     * <p>Description: 获取热点主体号余额</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @return BigDecimal 热点主体号余额
     */
    private static BigDecimal getBalanceData(String hcmain) {
        BsapRedisUtil lttsCache = BsapRedisUtil.getInstance();

        StringBuffer key = new StringBuffer();
        key.append(HotCtrl);
        key.append(hcmain);
        key.append(CurBal);

        try {
            if (lttsCache.exist(key.toString())) {
                return new BigDecimal(lttsCache.selectValueByKey(key.toString()).toString());
            } else {
                /** 初始化缓存发生额 */
                return initBalanceData(hcmain);
            }
        } catch (Exception e) {
            bizlog.error("根据日期获取热点主体号发生额错误：", e);
            throw HcError.HcComm.E0003();
        }
    }

    /**
     * <p>Title:removeBalanceData</p>
     * <p>Description: 删除热点主体号余额</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     */
    private static void removeBalanceData(String hcmain) {
        BsapRedisUtil lttsCache = BsapRedisUtil.getInstance();

        StringBuffer key = new StringBuffer();
        key.append(HotCtrl);
        key.append(hcmain);
        key.append(CurBal);

        try {
            lttsCache.delCachedData(key.toString());
        } catch (Exception e) {
            bizlog.error("根据日期获取热点主体号发生额错误：", e);
            throw HcError.HcComm.E0003();
        }
    }

    /**
     * <p>Title:removeDetailData</p>
     * <p>Description: 删除热点主体号占用明细</p>
     * 
     * @author liuych
     * @date 2018年4月27日
     * @param hcmain
     *        热点主体号
     * @param transq
     *        交易流水号
     */
    private static void removeDetailData(String hcmain, String transq) {
        BsapRedisUtil lttsCache = BsapRedisUtil.getInstance();

        StringBuffer key = new StringBuffer();
        key.append(HotCtrl);
        key.append(hcmain);
        key.append(BalDetl);
        key.append(transq);

        try {
            lttsCache.delCachedData(key.toString());
        } catch (Exception e) {
            bizlog.error("根据日期获取热点主体号发生额错误：", e);
            throw HcError.HcComm.E0003();
        }
    }

    /**
     * <p>Title: calBalanceData</p>
     * <p>Description: 根据余额方向及借贷方向</p>
     * 
     * @author liuych
     * @date 2018年5月4日
     * @param blncdr
     *        余额方向
     * @param amntcd
     *        借贷方向
     * @param value
     *        交易金额
     * @return BigDecimal 交易金额
     */
    private static BigDecimal calBalanceData(E_BLNCDR blncdr, E_AMNTCD amntcd, BigDecimal value) {
        if (E_BLNCDR.DR == blncdr && E_AMNTCD.CR == amntcd) {
            // 发生额取负数
            value = BigDecimal.ZERO.subtract(value);
        }

        if (E_BLNCDR.CR == blncdr && E_AMNTCD.DR == amntcd) {
            // 发生额负数
            value = BigDecimal.ZERO.subtract(value);
        }
        
        if(E_BLNCDR.Z == blncdr && E_AMNTCD.CR == amntcd) {
        	// 发生额负数
            value = BigDecimal.ZERO.subtract(value);
        }

        return value;
    }

    /**
     * <p>Title:initBalanceData</p>
     * <p>Description: 初始化缓存余额</p>
     * 
     * @author liuych
     * @date 2018年4月28日
     * @param hcmain
     *        热点主体号
     * @return BigDecimal 余额
     */
    private static BigDecimal initBalanceData(String hcmain) {
        BigDecimal bigBalance = BigDecimal.ZERO;

        HcpDefn defn = HotCtrlUtil.checkHotMainAndReturn(hcmain);

        if (CommUtil.isNotNull(defn)) {
            switch (defn.getHctype()) {
            case DP: {
                // 获取存款账户余额
                IoDpSrvQryTableInfo qryTableInfo = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
                IoDpKnaAcct knaAcct = qryTableInfo.getKnaAcctOdb1(hcmain, true);
                bigBalance = knaAcct.getOnlnbl();
                break;
            }
            case IN: {
                // 获取内部户余额
            	IoInQuery inQuery = SysUtil.getInstance(IoInQuery.class);
            	IoInAcctTmp acctTmp = inQuery.InacBalQuery(hcmain);
            	bigBalance = acctTmp.getOnlnbl();
            	break;
            }
            case CL: {
                HcbBlce hcbBlce = HcbBlceDao.selectOne_odb1(hcmain, false);             
                if (CommUtil.isNotNull(hcbBlce)) {
                    bigBalance = hcbBlce.getBalnce();
                }else{
                	throw HcError.HcGen.E0000("热点账户余额信息不存在！");	
                }
                break;
            }
            default: {
            	throw HcError.HcGen.E0000("不支持的热点类型！");
            }
            }
        } else {
        	throw HcError.HcGen.E0000("热点账户信息获取失败！");

        }
        return bigBalance;
    }

}
