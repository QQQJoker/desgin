package cn.sunline.ltts.fa.util;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpGlbl;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.ApKnpGlbl;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 
 * <p>Title:FaTools</p>
 * <p>Description: 总账工具类型 </p>
 * 
 * @author cuijia
 * @date 2017年6月19日
 */
public class FaTools {
	private static final BizLog bizlog = BizLogUtil.getBizLog(FaTools.class);

    /**
     * 
     * <p>Title:getRemoteDir </p>
     * <p>Description:	FTP远程获取文件开关设置</p>
     * @author cuijia
     * @date   2017年7月11日 
     * @return
     */
    public static String getRemoteDir(){
        KnpGlbl knpGlblDO = ApKnpGlbl.getKnpGlbl(FaConst.REMOTE_DIR, ApConstants.WILDCARD);
        return knpGlblDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getBatchSubmitCount </p>
     * <p>Description: 获取批量提交记录条数</p>
     * 
     * @author cuijia
     * @date 2017年6月19日
     * @return
     */
    public static int getBatchSubmitCount() {
        KnpGlbl knpGlblDO = ApKnpGlbl.getKnpGlbl(FaConst.BATCH_SUBMIT_COUNT, ApConstants.WILDCARD);
        return Integer.valueOf(knpGlblDO.getPmval1());
    }

    /**
     * 
     * <p>Title:getWaitTime </p>
     * <p>Description: 获取等待时间</p>
     * 
     * @author cuijia
     * @date 2017年6月20日
     * @return
     */
    public static int getWaitTime() {
        KnpGlbl knpGlblDO = ApKnpGlbl.getKnpGlbl(FaConst.WAIT_TIME_MAIN, FaConst.WAIT_TIME_SUB_FILE);
        return Integer.valueOf(knpGlblDO.getPmval1());
    }

    /**
     * 
     * <p>Title:getAnalysisError </p>
     * <p>Description: 获取错误解析错误</p>
     * 
     * @author cuijia
     * @date 2017年6月20日
     * @return
     */
    public static String getAnalysisError() {
        KnpGlbl knpGlblDO = ApKnpGlbl.getKnpGlbl(FaConst.GL_CODE_ANALYSIS_ERROR_KEY, ApConstants.WILDCARD);
        return knpGlblDO.getPmval1();
    }

    /**
     * 
     * <p>Title:getSettFlatFlag </p>
     * <p>Description: 系统清算补平标志</p>
     * 
     * @author cuijia
     * @date 2017年6月20日
     * @return
     */
    public static String getSettFlatFlag() {
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_SETT_FLAT_FLAG, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getSettFlatType </p>
     * <p>Description:	获取清算补平类型</p>
     * @author songhao
     * @date   2017年9月6日 
     * @return
     */
    public static String getSettFlatType() {
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_SETT_FLAT_TYPE, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }

    /**
     * 
     * <p>Title:getSettType </p>
     * <p>Description: 获取清算类型</p>
     * 
     * @author cuijia
     * @date 2017年6月20日
     * @return
     */
    public static String getSettType() {
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_SETT_TYPE, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }

    /**
     * 
     * <p>Title:getSettSubjectNoD </p>
     * <p>Description: 获取借方清算科目号</p>
     * 
     * @author cuijia
     * @date 2017年6月20日
     * @return
     */
    public static String getSettSubjectNoD() {
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_SETT_SINGLE_SUBJECT, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getSettSubjectNoC </p>
     * <p>Description: 获取贷方清算科目号</p>
     * 
     * @author cuijia
     * @date 2017年6月20日
     * @return
     */
    public static String getSettSubjectNoC() {
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_SETT_SINGLE_SUBJECT, ApConstants.WILDCARD);
        return knpParaDO.getPmval2();
    }
    
    /**
     * 
     * <p>Title:getSettSubject </p>
     * <p>Description:	补平科目</p>
     * @author cuijia
     * @date   2017年6月20日 
     * @return
     */
    public static String getSettSubject(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_SETTLE_FLAT_SUBJECT, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getLedgerCheckError </p>
     * <p>Description:	总账检查错误是否报错</p>
     * @author cuijia
     * @date   2017年6月20日 
     * @return
     */
    public static String getLedgerCheckError(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_LEDGERC_CHECK_ERROR, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getExchangeRateMode </p>
     * <p>Description:	外汇利率模式</p>
     * @author cuijia
     * @date   2017年6月20日 
     * @return
     */
    public static String getExchangeRateMode(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_EXCHANGE_RATE_MODE, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getAccountBranchRelation </p>
     * <p>Description:	账务机构关系</p>
     * @author cuijia
     * @date   2017年6月22日 
     * @return
     */
    public static String getDiffCcyInd(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_DIFF_CCY_IND, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getExchangeFlatSubject </p>
     * <p>Description:	外汇平账科目</p>
     * @author cuijia
     * @date   2017年6月22日 
     * @return
     */
    public static String getExchangeFlatSubject(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_EXCHANGE_FLAT_SUBJECT, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getSettSingleSubject </p>
     * <p>Description:	单点清算科目</p>
     * @author cuijia
     * @date   2017年6月22日 
     * @return
     */
    public static String getSettSingleSubject(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_SETT_SINGLE_SUBJECT, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getProfitSubject </p>
     * <p>Description:	利润科目</p>
     * @author cuijia
     * @date   2017年6月22日 
     * @return
     */
    public static String getProfitSubject(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_PROFIT_SUBJECT, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getHeadSubject </p>
     * <p>Description: 与总行往来科目</p>
     * @author songhao
     * @date   2017年9月21日 
     * @return
     */
    public static String getHeadSubject(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_HEAD_SUBJECT, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getExchangeCurrency </p>
     * <p>Description:	外汇折算币种</p>
     * @author cuijia
     * @date   2017年6月22日 
     * @return
     */
    public static List<KnpPara> listExchangeCurrency(){
        return ApKnpPara.listKnpPara(FaConst.EX_CCY_CODE, false);
    }
    
    /**
     * 
     * <p>Title:getFirstLevelSubjectLength </p>
     * <p>Description: 一级科目长度</p>
     * @author cuijia
     * @date   2017年6月22日 
     * @return
     */
    public static String getFirstLevelSubjectLength(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_FIRST_LEVEL_SUBJECT_LENGTH, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getIncreaseSubjectLength </p>
     * <p>Description:	增长科目长度</p>
     * @author cuijia
     * @date   2017年6月22日 
     * @return
     */
    public static String getIncreaseSubjectLength(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_INCREASE_SUBJECT_LENGTH, ApConstants.WILDCARD);
        return knpParaDO.getPmval1();
    }
    
    /**
     * 
     * <p>Title:getYearendStatus </p>
     * <p>Description:获取年结状态</p>
     * @author songhao
     * @date   2017年10月22日 
     * @return true-正常状态 false-年结状态，需要暂停进入年终试算
     */
    public static boolean getYearendStatus(){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_YEAREND_STATUS, CommToolsAplt.prcRunEnvs().getCorpno());
        bizlog.method("knpParaDO [%s] ", knpParaDO);
        if(CommUtil.isNull(knpParaDO.getPmval1()) || 
				CommUtil.compare(FaConst.YEAREND_STATUS_STOP, knpParaDO.getPmval1()) == 0){
			return false;
		}
        return true;
    }
    
    /**
     * 
     * <p>Title:updateYearendStatus </p>
     * <p>Description:修改年结状态</p>
     * @author songhao
     * @date   2017年10月22日 
     * @return 
     */
    public static void updateYearendStatus(String yearend_status){
        KnpPara knpParaDO = ApKnpPara.getKnpPara(FaConst.KEY_YEAREND_STATUS, CommToolsAplt.prcRunEnvs().getCorpno());
        knpParaDO.setPmval1(yearend_status);
        KnpParaDao.updateOne_odb1(knpParaDO);
    }

    /**
     * 
     * @Author 
     *         <p>
     *         <li>2020年11月22日-下午3:21:02</li>
     *         <li>功能说明：贷款解析错误后继续处理标志</li>
     *         </p>
     * @return
     */
	public static String getLnAnalysisError() {
		
		bizlog.method(" FaTools.getLnAnalysisError begin >>>>>>>>>>>>>>>>");
		
		KnpGlbl knpGlblDO = ApKnpGlbl.getKnpGlbl(FaConst.GL_CODE_LN_ANALYSIS_ERROR_KEY, ApConstants.WILDCARD);
	        
	    bizlog.method(" FaTools.getLnAnalysisError end <<<<<<<<<<<<<<<<");
	    return knpGlblDO.getPmval1();		
	}
}
