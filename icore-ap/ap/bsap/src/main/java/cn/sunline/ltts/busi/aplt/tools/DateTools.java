package cn.sunline.ltts.busi.aplt.tools;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppHold;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppHoldDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydtDao;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.ApError.Extn;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;
import cn.sunline.ltts.busi.sys.errors.LnError;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_JIARCLFS;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_QIXIANDW;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TERMCD;
import cn.sunline.ltts.gns.AcdtImpl;
import cn.sunline.ltts.gns.api.AcdtApi;
import cn.sunline.ltts.gns.api.AcdtInf;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author T
 *         <p>
 *         <li>2014年3月11日-下午4:07:51</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228Name：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class DateTools {
    public static final String TIMESTAMP21 = "yyyyMMddHHmmss.SSS000";
    private static final BizLog bizlog = BizLogUtil.getBizLog(DateTools.class);
    public static Date addDays(Date dt, int days)
    /*      */   {
    /* 1185 */     Calendar cal = Calendar.getInstance();
    /* 1186 */     cal.setTime(dt);
    /* 1187 */     cal.add(5, days);
    /*      */     
    /* 1189 */     return cal.getTime();
    /*      */   }

    public static int compareDate(Date d1, Date d2)
    /*      */   {
    /* 1110 */     Calendar cal1 = Calendar.getInstance();
    /* 1111 */     cal1.setTime(d1);
    /*      */     
    /* 1113 */     Calendar cal2 = Calendar.getInstance();
    /* 1114 */     cal2.setTime(d2);
    /*      */     
    /* 1116 */     return cal1.compareTo(cal2);
    /*      */   }

     



    
    public static String getTransTimestamp() {
        RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
        if (CommUtil.isNotNull(runEnvs.getTmstmp()))
            return runEnvs.getTmstmp();
        
        String ret= new SimpleDateFormat(TIMESTAMP21).format(new Date(System.currentTimeMillis()));
        runEnvs.setTmstmp(ret);
        return ret;
    }
    
    /**
     * 获取当前时间戳，格式：yyyyMMddHHmmss.SSSSSS
     * 
     * @return String
     */
    public static String getCurrentTimestamp21() {
        return getTransTimestamp();
    }

    /**
     * 获取当前时间戳，格式：pattern
     * 
     * @return String
     */
    public static String getCurrentTimestamp(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date(System.currentTimeMillis()));
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月5日-下午3:17:14</li>
     *         <li>功能说明：获取8位主机日期</li>
     *         </p>
     * @return 主机日期
     */
    public static String getSystemDate() {
        Format t = new SimpleDateFormat("yyyyMMdd");

        return t.format(new Date());
    }

    public static String getCurrentDateTime() {
        Format t = new SimpleDateFormat("yyyyMMddHHmmss");

        return t.format(new Date());
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月5日-下午3:17:42</li>
     *         <li>功能说明：获取9位字符串的主机时间</li>
     *         </p>
     * @return 主机时间
     */
    public static String getCurrentTime() {
        Format f = new SimpleDateFormat("HHmmssSSS");

        return f.format(new Date());
    }

    /**
     * @Author T
     *         <p>
     *         <li>2016年3月18日-下午3:17:42</li>
     *         <li>功能说明：获取6位字符串的主机时间</li>
     *         </p>
     * @return 主机时间
     */
    public static String getCurrentLocalTime() {
        Format f = new SimpleDateFormat("HHmmss");

        return f.format(new Date());
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月5日-下午3:17:42</li>
     *         <li>功能说明：获取最大9位长整的主机时间</li>
     *         </p>
     * @return 主机时间
     */
    public static long getCurrentTimeL() {
        Format f = new SimpleDateFormat("HHmmssSSS");

        return Long.parseLong(f.format(new Date()));
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月5日-下午3:18:07</li>
     *         <li>功能说明：获取交易系统日期</li>
     *         </p>
     * @return 日期信息的复合类型
     */
    public static ApSysDateStru getDateInfo() {

    	AcdtApi acdtApi = new AcdtImpl();
    	AcdtInf res;
    	
    	//通过交易级缓存获取或初始化会计日期对象
    	// 如果是测试环境，则直接从表里取
    	KnpPara knpPara = KnpParaDao.selectOne_odb1("Day.Parms", "incgdt", "environment", "isTest", CommTools.prcRunEnvs().getCorpno(), false);
		if(CommUtil.isNotNull(knpPara) && CommUtil.equals("1", knpPara.getPmval1())) {
			res = acdtApi.getNewAcdt();
		}else {
			if(CommUtil.isNull(EngineContext.getTxnTempObj("acdtInfo"))) {
	    		res = acdtApi.getAcdt();
	    		EngineContext.setTxnTempObj("acdtInfo", res);
	    	}else {
	    		res =EngineContext.getTxnTempObj("acdtInfo");
	    	}
		}
		
    	
        ApSysDateStru cplSysDateInfo = SysUtil.getInstance(ApSysDateStru.class);

        cplSysDateInfo.setHostdt(getSystemDate());
        cplSysDateInfo.setTrantm(getTransTimestamp());

        cplSysDateInfo.setSystdt(res.getSystdt());
        cplSysDateInfo.setLastdt(res.getLastdt());
        cplSysDateInfo.setBflsdt(res.getBflsdt());
        cplSysDateInfo.setNextdt(res.getNextdt());
        cplSysDateInfo.setAfnxdt(res.getAfnxdt());
        cplSysDateInfo.setYreddt(res.getYreddt());

        return cplSysDateInfo;
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月4日-下午3:03:54</li>
     *         <li>功能说明：检查当前字符变量是否日期</li>
     *         </p>
     * @param sDate
     * @return true 合法 false 非法
     */
    public static boolean chkIsDate(String sDate) {
        bizlog.debug("输入日期为[%s]", sDate);
        boolean bValid = false;
        if (CommUtil.isNull(sDate)) {
            throw Sys.E0002(sDate);
        }
        Format f = new SimpleDateFormat("yyyyMMdd");
        String tmp = f.format(covStringToDate(sDate));
        bValid = CommUtil.equals(tmp, sDate);

        return bValid;
    }

    /**
     * @Author Luxy
     *         <p>
     *         <li>2014年3月4日-下午3:03:54</li>
     *         <li>功能说明：检查当前字符变量是否合法时间</li>
     *         </p>
     * @param sTime
     * @return true 合法 false 非法
     */
    public static boolean chkIsTime(String sTime) {
        bizlog.debug("输入时间为[%s]", sTime);
        boolean bValid = false;

        if (CommUtil.isNull(sTime)) {
            throw Sys.E0002(sTime);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
        Date d = new Date();
        try {
            d = dateFormat.parse(sTime);
        } catch (ParseException e) {
            throw Sys.E0002(sTime);
        }

        Format f = new SimpleDateFormat("HHmmss");
        String tmp = f.format(d);
        bValid = sTime.equals(tmp);
        return bValid;
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年4月9日-上午10:57:02</li>
     *         <li>功能说明：通过起止日期和期限类型，计算间隔的期限</li>
     *         </p>
     * @param sStartDate
     * @param sEndDate
     * @param sTerm
     *            eg:1M,2M,1D,10D...
     * @return 期数
     */
    public static int calTermByDate(String sStartDate, String sEndDate, String sTerm) {
        if (!DateTools.chkIsDate(sStartDate)) {
            bizlog.debug("起始输入日期格式不正确[%s]", sStartDate);
            throw Sys.E0001("起始输入日期格式不正确");
        }
        if (!DateTools.chkIsDate(sEndDate)) {
            bizlog.debug("终止输入日期格式不正确[%s]", sEndDate);
            throw Sys.E0001("终止输入日期格式不正确");
        }
        bizlog.debug("sStartDate [%s],  sEndDate [%s], eQixiandw [%s]", sStartDate, sEndDate, sTerm);
        String sStartDate_tmp = (Integer.parseInt(sStartDate) < Integer.parseInt(sEndDate)) ? sStartDate : sEndDate;
        String sEndDate_tmp = (Integer.parseInt(sStartDate) > Integer.parseInt(sEndDate)) ? sStartDate : sEndDate;

        Calendar caStart = Calendar.getInstance();
        caStart.setTime(DateTools.covStringToDate(sStartDate_tmp));
        Calendar caEnd = Calendar.getInstance();
        caEnd.setTime(DateTools.covStringToDate(sEndDate_tmp));
        int iTerm = 0;
        if (E_QIXIANDW.Day.getValue().equals(sTerm.substring(sTerm.length() - 1))) {
            iTerm = ((int) (caEnd.getTime().getTime() / 1000) - (int) (caStart.getTime().getTime() / 1000)) / 3600 / 24;
            bizlog.debug("iTerm %d", iTerm);
        }
        else {
            while (true) {
                sStartDate_tmp = DateTools.calDateByTerm(sStartDate_tmp, sTerm);
                if (Integer.parseInt(sStartDate_tmp) > Integer.parseInt(sEndDate_tmp)) {
                    break;
                }
                else {
                    iTerm += 1;
                }
                bizlog.debug("sStartDate_tmp [%s] iTerm [%d]", sStartDate_tmp, iTerm);
            }

        }
        return iTerm;
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月4日-下午2:56:57</li>
     *         <li>功能说明：通过频率计算下一日期，支持向后及向前算</li>
     *         <li>
     *         格式说明：期限值（数字）+期限单位（DWSMQY）+节假日处理标志（ANP）+具体日（F/E/天数/D）+季月份指定（FME）
     *         <li>具体日（F/E/天数/D） 说明：D对日 F初 E末 天数指定固定日期</li>
     *         <li>示例：</li>
     *         <li>日（D）：1DA（无具体日指定）</li>
     *         <li>周（W）：1WAF/1WA[2-6]/1WAE</li>
     *         <li>旬（S）：1SAF/1SA[2-9]/1SAE</li>
     *         <li>月（M）：1MAF/1MA[2-28/29/30]/1MAE</li>
     *         <li>季（Q）：1QAF[FME]/1QA20[FME]/1QAE[FME]</li>
     *         <li>年（Y）：1YAF/1YA[2-364/365]/1YAE</li>
     *         <li>年（Y）：1YA0501（表示每年的5月1日）</li>
     *         </p>
     * @param sCurDate
     *            当前日期
     * @param sFrequence
     *            周期/频率
     * @param holdcd
     *            节假日代码           
     * @param sGuobiedm
     *            国别代码 可不传
     * @param sJiaoyjg
     *            机构号 用于节假检查
     * @param iFlag
     *            节假日标记，1 - 对公假日 2 - 对私假日 3 - 不分对公对私
     * @param iType
     *            是否包含本期， 0，否，1，是，2，上期
     * @return 下一日期
     */
    public static String calDateByFreq(String sCurDate, String sFrequence, String holdcd,  int iType) {

        int iFlag =1;
        if (CommUtil.isNull(sFrequence)) {
            throw Sys.E0001("存期/期限不能为空");
        }
        if (CommUtil.isNull(sCurDate)) {
            throw Sys.E0005();
        }
        if (!DateTools.chkIsDate(sCurDate)) {
            bizlog.debug("输入日期格式不正确[%s]", sCurDate);
            throw Sys.E0001("输入日期格式不正确");
        }

        String[] sFreq_tmp = DateTools.prcFrequence(sFrequence).split("-");

        String sType = sFreq_tmp[2];
        if (1 != iFlag && 2 != iFlag && 3 != iFlag) {
            throw Sys.E0006();
        }
        if (0 != iType && 1 != iType && 2 != iType) {
            throw Sys.E0007();
        }
        
        String sCurDate_tmp =calDatethisFreq(sCurDate,sFrequence);
        
        bizlog.debug("起始日期为[%s]", sCurDate_tmp);
        String sTerm = null;
        int iTerm = Integer.parseInt(sFreq_tmp[0]);
        
        if (0 == iType) {
            sTerm = iTerm + sFreq_tmp[1];
        }
        else if (2 == iType) {
            sTerm = -1 + sFreq_tmp[1];
        }
        else{
            if (Integer.parseInt(sCurDate_tmp) < Integer.parseInt(sCurDate)) {
                sTerm = iTerm + sFreq_tmp[1];
            }
            else {
            	if (sFreq_tmp.length > 3 && CommUtil.compare(sFreq_tmp[3], "D") != 0 ){
            		sTerm = iTerm - 1 + sFreq_tmp[1];
            	}else{
            		sTerm = iTerm + sFreq_tmp[1];
            	}
            }
        }
        
        bizlog.debug("期限为[%s] 频率为[%s]", sTerm, sFrequence);
        String sTarDate = DateTools.calDateByTerm(sCurDate_tmp, sTerm, holdcd, sType);
        bizlog.debug("目标日期[%s]", sTarDate);
        
        sTarDate =calDatethisFreq(sTarDate,sFrequence);
        
        if (Integer.parseInt(sTarDate) <= Integer.parseInt(sCurDate) && iType!=2) {
            sTerm = 1 + sFreq_tmp[1];
            sTarDate = DateTools.calDateByTerm(sTarDate, sTerm, holdcd, sType);
        }
        
        return sTarDate;
    }
    


    /**
     * @Author Administrator
     *         <p>
     *         <li>2014年12月10日-下午7:54:37</li>
     *         <li>功能说明：通过频率计算下一日期</li>
     *         </p>
     * @param sCurDate
     *            当前日期
     * @param sFrequence
     *            周期/频率
     * @return
     */
    public static String calDateByFreq(String sCurDate, String sFrequence) {

        return DateTools.calDateByFreq(sCurDate, sFrequence, "", 1);
    }
    /**
     * @Author Administrator
     *         <p>
     *         <li>2014年12月10日-下午7:54:37</li>
     *         <li>功能说明：通过频率（并从指定日期的下个期间）的计算下一日期</li>
     *         </p>
     * @param sCurDate
     *            当前日期
     * @param sFrequence
     *            周期/频率
     * @return
     */
    public static String calDateByNextFreq(String sCurDate, String sFrequence) {

        return DateTools.calDateByFreq(sCurDate, sFrequence, "", 0);
    }
    public static String calDateByTerm(String sCurDate, String sTerm, String holidayCode, String sType) {

        String sMaturityDate = null;

        if (CommUtil.isNull(sTerm)) {
            throw Sys.E0001("存期/期限不能为空");
        }

        // 负数判断标志
        boolean bMin = false;
        if (CommUtil.equals(sTerm.substring(0, 1), "-")) {
            bMin = true;
            sTerm = sTerm.substring(1);
        }

        // 存期拆分为列表
        List<String> lstTerm = new ArrayList<String>();
        int iTmp = 0;
        for (int i = 0; i < sTerm.length(); i++) {
            if (!Character.isDigit(sTerm.charAt(i))) {
                lstTerm.add(sTerm.substring(iTmp, i + 1));
                iTmp = i + 1;
                continue;
            }

        }

        if (CommUtil.compare(iTmp, sTerm.length()) != 0) {
            throw Sys.E0001("输入期限不正确");
        }

        if (!DateTools.chkIsDate(sCurDate)) {
            bizlog.debug("输入日期格式不正确[%s]", sCurDate);
            throw Sys.E0001("输入日期格式不正确");
        }

        Calendar ca = Calendar.getInstance();

        ca.setTime(DateTools.covStringToDate(sCurDate));

        // 循环计算存期
        for (int i = 0; i < lstTerm.size(); i++) {
            String sTermTmp = lstTerm.get(i);
            if (sTermTmp.length() == 1) {
                throw Sys.E0001("输入期限格式不正确");
            }

            int iTerm = Integer.parseInt(sTermTmp.substring(0, sTermTmp.length() - 1));

            if (bMin) {
                iTerm = -1 * iTerm;
            }

            char cTerm = sTermTmp.charAt(sTermTmp.length() - 1);
            bizlog.debug("当期计算数据：[%s]", i + "次/" + iTerm + "个/" + cTerm + "期限");
            ca = calDateByTermSubMethod(ca, iTerm, cTerm);
        }

        sMaturityDate = DateTools.covDateToString(ca.getTime());
        bizlog.debug("到期日期为[%s]", sMaturityDate);


            return sMaturityDate;

    }
    /**
     * @Author T
     *         <p>
     *         <li>2014年3月4日-下午2:57:21</li>
     *         <li>功能说明：通过存期计算下一日期，存期支持多个存期列表，如：3M，3M2M</li>
     *         </p>
     * @param sCurDate
     *            当前日期
     * @param sTerm
     *            存期
     * @param sGuobiedm
     *            国别代码 可不传
     * @param sJiaoyjg
     *            机构号 用于节假检查
     * @param iFlag
     *            节假日标记，1 - 对公假日 2 - 对私假日 3 - 不分对公对私
     * @param sType
     *            顺延方式， A - 不顺延 N - 向后顺延 P - 向前顺延
     * @return 下一日期
     */
    public static String calDateByTerm(String sCurDate, String sTerm, String sGuobiedm, String sJiaoyjg, int iFlag, String sType) {

        String sMaturityDate = null;

        if (CommUtil.isNull(sTerm)) {
            throw Sys.E0001("存期/期限不能为空");
        }
        
        //负数判断标志
        boolean bMin = false;
        if(CommUtil.equals(sTerm.substring(0, 1), "-")){
            bMin = true;
            sTerm = sTerm.substring(1);
        }
        
        //存期拆分为列表
        List<String> lstTerm = new ArrayList<String>();
        int iTmp = 0;
        for(int i = 0; i < sTerm.length(); i++){
            if(!Character.isDigit(sTerm.charAt(i))){
                lstTerm.add(sTerm.substring(iTmp, i + 1));
                iTmp = i + 1;
                continue;
            }
                
        }
        
        if(CommUtil.compare(iTmp, sTerm.length()) != 0){
            throw  Sys.E0001("输入日期格式不正确");
        }

        if (!DateTools.chkIsDate(sCurDate)) {
            bizlog.debug("输入日期格式不正确[%s]", sCurDate);
            throw Sys.E0001("输入日期格式不正确");
        }

        Calendar ca = Calendar.getInstance();

        ca.setTime(DateTools.covStringToDate(sCurDate));
        
        //循环计算存期
        for(int i =  0; i < lstTerm.size(); i++){
            String sTermTmp = lstTerm.get(i);
            if(sTermTmp.length() == 1){
                throw Sys.E0001("存期不正确");
            }
            
            int iTerm = Integer.parseInt(sTermTmp.substring(0, sTermTmp.length() - 1));
            
            if(bMin){
                iTerm = -1 * iTerm;
            }
            
            char cTerm = sTermTmp.charAt(sTermTmp.length() - 1);
            bizlog.debug("当期计算数据：[%s]", i + "次/" + iTerm + "个/" + cTerm + "期限");
            ca = calDateByTermSubMethod(ca, iTerm, cTerm);
        }

        sMaturityDate = DateTools.covDateToString(ca.getTime());
        bizlog.debug("到期日期为[%s]", sMaturityDate);

        if (E_JIARCLFS.Actual.getValue().equals(sType)) {
            return sMaturityDate;
        }
        else {
            while (DateTools.isHoliday(sMaturityDate, sGuobiedm, ApBaseEnumType.E_HOLIDAYTYPE.LEGAL))  {
                if (E_JIARCLFS.Previous.equals(sType)) {
                    ca.add(Calendar.DAY_OF_MONTH, -1);
                }
                else {
                    ca.add(Calendar.DAY_OF_MONTH, 1);
                }
                sMaturityDate = DateTools.covDateToString(ca.getTime());
            }
            bizlog.debug("遇节假日，到期日期为[%s]", sMaturityDate);
            return sMaturityDate;
        }
    }
        
    /**
     * @Author Administrator
     *         <p>
     *         <li>2014年12月10日-下午7:54:37</li>
     *         <li>功能说明：计算当前周期内，频率对应日期</li>
     *         </p>
     * @param sCurDate
     *            当前日期
     * @param sFrequence
     *            周期/频率
     * @return
     */
    private static String calDatethisFreq(String sCurDate, String sFrequence) {

        if (!DateTools.chkIsDate(sCurDate)) {
            bizlog.debug("输入日期格式不正确[%s]", sCurDate);
            throw  Sys.E0001("输入日期格式不正确");
        }
        
        String[] sFreq_tmp = DateTools.prcFrequence(sFrequence).split("-");
        Calendar ca = Calendar.getInstance();
        ca.setTime(DateTools.covStringToDate(sCurDate));
        
        if (sFreq_tmp.length >= 4) {
            if (E_QIXIANDW.Week.getValue().equals(sFreq_tmp[1])) {
                if ("F".equals(sFreq_tmp[3])) {
                    ca.set(Calendar.DAY_OF_WEEK, ca.getActualMinimum(Calendar.DAY_OF_WEEK));
                }
                else if ("E".equals(sFreq_tmp[3])) {
                    ca.set(Calendar.DAY_OF_WEEK, ca.getMaximum(Calendar.DAY_OF_WEEK));
                }else if("D".equals(sFreq_tmp[3])){
                    throw Sys.E0001("频率为周输入日期格式不正确");
                }
                else {
                    ca.set(Calendar.DAY_OF_WEEK, ca.getActualMinimum(Calendar.DAY_OF_WEEK));
                    ca.add(Calendar.DATE, Integer.parseInt(sFreq_tmp[3]) - 1);
                }
            }
            else if (E_QIXIANDW.Xun.getValue().equals(sFreq_tmp[1])) {
                int iOldDay = ca.get(Calendar.DAY_OF_MONTH);
                if ("F".equals(sFreq_tmp[3])) {
                    if (iOldDay >= 21) {
                        iOldDay = 21;
                    }
                    else if (iOldDay >= 11) {
                        iOldDay = 11;
                    }
                    else {
                        iOldDay = 1;
                    }
                    ca.set(Calendar.DAY_OF_MONTH, iOldDay);
                }
                else if ("E".equals(sFreq_tmp[3])) {
                    if (iOldDay >= 21) {
                        iOldDay = ca.getActualMaximum(Calendar.DAY_OF_MONTH);
                    }
                    else if (iOldDay >= 11) {
                        iOldDay = 20;
                    }
                    else {
                        iOldDay = 10;
                    }
                    ca.set(Calendar.DAY_OF_MONTH, iOldDay);
                }else if("D".equals(sFreq_tmp[3])){
                    throw Sys.E0001("频率为旬输入日期格式不正确");
                }
                else {
                    ca.set(Calendar.DAY_OF_MONTH, (ca.get(Calendar.DAY_OF_MONTH) / 10) * 10);
                    ca.add(Calendar.DATE, Integer.valueOf(sFreq_tmp[3]));
                }
                
                Calendar caTmpXun = Calendar.getInstance();
                caTmpXun.setTime(DateTools.covStringToDate(sCurDate));
                if(ca.get(Calendar.MONTH) > caTmpXun.get(Calendar.MONTH)){
                    ca.add(Calendar.MONTH, -1);
                    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
                } else if(ca.get(Calendar.MONTH) < caTmpXun.get(Calendar.MONTH)){
                    ca.add(Calendar.MONTH, 1);
                    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
                }
                
                
            }
            else if (E_QIXIANDW.Month.getValue().equals(sFreq_tmp[1])) {
                if ("F".equals(sFreq_tmp[3])) {
                    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
                }
                else if ("E".equals(sFreq_tmp[3])) {
                    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
                }else if("D".equals(sFreq_tmp[3])){
                    ca.set(Calendar.DAY_OF_MONTH, Integer.parseInt(sCurDate.substring(6,8)));
                }
                else {
                    ca.set(Calendar.DAY_OF_MONTH, Integer.parseInt(sFreq_tmp[3]));
                    Calendar catmpMonth =Calendar.getInstance();
                    catmpMonth.setTime(DateTools.covStringToDate(sCurDate));
                    if(ca.get(Calendar.MONTH)> catmpMonth.get(Calendar.MONTH)) {
                        ca.add(Calendar.MONTH,-1);
                        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
                    } else if(ca.get(Calendar.MONTH)< catmpMonth.get(Calendar.MONTH)){
                        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
                    }
                }

            }
            else if (E_QIXIANDW.Quart.getValue().equals(sFreq_tmp[1])) {
                
                if ("F".equals(sFreq_tmp[3])) {
                    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
                }
                else if ("E".equals(sFreq_tmp[3])) {
                    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
                }else if("D".equals(sFreq_tmp[3])){
                    ca.set(Calendar.DAY_OF_MONTH, Integer.parseInt(sCurDate.substring(6,8)));
                }
                else {
                    ca.set(Calendar.DAY_OF_MONTH, Integer.parseInt(sFreq_tmp[3]));
                    Calendar caTmpQuart = Calendar.getInstance();
                    caTmpQuart.setTime(DateTools.covStringToDate(sCurDate));
                    if (ca.get(Calendar.MONTH) > caTmpQuart.get(Calendar.MONTH)){
                        ca.add(Calendar.MONTH, -1);
                        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
                    } else if(ca.get(Calendar.MONTH) < caTmpQuart.get(Calendar.MONTH)){
                        ca.add(Calendar.MONTH, 1);
                        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
                    }
                }

                // 月计算
                int iQuart = (ca.get(Calendar.MONTH) / 3) * 3;
                ca.add(Calendar.MONTH, iQuart - ca.get(Calendar.MONTH));
                
                if ("F".equals(sFreq_tmp[4])) {
                }
                else if ("M".equals(sFreq_tmp[4])) {
                    ca.add(Calendar.MONTH, 1);
                }
                else {
                    ca.add(Calendar.MONTH, 2);
                }
                
                if ("E".equals(sFreq_tmp[3]))
                    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
                
            }
            else if (E_QIXIANDW.Year.getValue().equals(sFreq_tmp[1])) {
                if ("F".equals(sFreq_tmp[3])) {
                    ca.set(Calendar.MONTH, ca.getActualMinimum(Calendar.MONTH));
                    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
                }
                else if ("E".equals(sFreq_tmp[3])) {
                    ca.set(Calendar.MONTH, ca.getActualMaximum(Calendar.MONTH));
                    ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
                }else if("D".equals(sFreq_tmp[3])){
                    ca.set(Calendar.DAY_OF_MONTH, Integer.parseInt(sCurDate.substring(6,8)));
                }
                else {
                    if (sFreq_tmp[3].length() <= 3) {
                        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
                        ca.set(Calendar.MONTH, ca.getActualMinimum(Calendar.MONTH));
                        ca.add(Calendar.DATE, Integer.parseInt(sFreq_tmp[3]) - 1);
                           
                    }
                    else {
                        ca.setTime(DateTools.covStringToDate(sCurDate.substring(0, 4) + sFreq_tmp[3]));
                    }
                }

            }
            else {
            }
        }
        String sCurDate_tmp = DateTools.covDateToString(ca.getTime());
        
        return sCurDate_tmp;
        
        
    }

    /**
     * @Author T
     *         <p>
     *         <li>2016年11月23日-下午2:57:21</li>
     *         <li>功能说明：通过拆分存期(存期数及存期单位)计算下一日期</li>
     *         </p>
     * @param ca
     *            当前日期
     * @param iTerm
     *            存期数
     * @param cTerm
     *            存期单位
     * @return 下一日期
     */
    private static Calendar calDateByTermSubMethod(Calendar ca, int iTerm, char cTerm) {

        if (!CommUtil.isInEnum(E_QIXIANDW.class, String.valueOf(cTerm))) {
            bizlog.debug("存期/期限类型不正确[%s]", String.valueOf(cTerm));
            throw Sys.E0001("存期/期限类型不正确");
        }

        switch (cTerm) {
        case 'D':
            ca.add(Calendar.DATE, iTerm);
            break;
        case 'W':
            ca.add(Calendar.WEEK_OF_YEAR, iTerm);
            break;
        case 'S':
            ca.setTime(DateTools.calXun(DateTools.covDateToString(ca.getTime()), iTerm));
            break;
        case 'M':
            ca.add(Calendar.MONTH, iTerm);
            break;
        case 'Q':
            ca.add(Calendar.MONTH, iTerm * 3);
            break;
        case 'Y':
            ca.add(Calendar.YEAR, iTerm);
            break;
        case 'H':
            ca.add(Calendar.MONTH, iTerm * 6);
            break;
        default:
            bizlog.debug("无对应的存期或期限类型，返回原日期");
            break;
        }

        return ca;
    }

    /**
     * @Author Administrator
     *         <p>
     *         <li>2014年12月10日-下午7:31:07</li>
     *         <li>功能说明：通过存期计算下一日期（不考虑节假日）</li>
     *         </p>
     * @param sCurDate
     *            当前日期
     * @param sTerm
     *            存期
     * @return
     */
    public static String calDateByTerm(String sCurDate, String sTerm) {

        return calDateByTerm(sCurDate, sTerm, null,null, 3, "A");
    }

    /**
     * 按照存款存期计算到期日
     * 
     * @param sCurDate
     *            当前日期
     * @param eTermcd
     *            存储存期
     * @return 到期日
     */
    public static String calDateByTerm(String sCurDate, E_TERMCD eTermcd) {
        String sTerm = "";
        if (E_TERMCD.T000 == eTermcd) {
            return "";
        } else if (E_TERMCD.T101 == eTermcd) {
            sTerm = "1D";
        } else if (E_TERMCD.T103 == eTermcd) {
            sTerm = "3D";
        } else if (E_TERMCD.T107 == eTermcd) {
            sTerm = "7D";
        } else if (E_TERMCD.T114 == eTermcd) {
            sTerm = "14D";
        } else if (E_TERMCD.T201 == eTermcd) {
            sTerm = "1M";
        } else if (E_TERMCD.T202 == eTermcd) {
            sTerm = "2M";
        } else if (E_TERMCD.T203 == eTermcd) {
            sTerm = "3M";
        } else if (E_TERMCD.T206 == eTermcd) {
            sTerm = "6M";
        } else if (E_TERMCD.T209 == eTermcd) {
            sTerm = "9M";
        } else if (E_TERMCD.T218 == eTermcd) {
            sTerm = "18M";
        } else if (E_TERMCD.T301 == eTermcd) {
            sTerm = "1Y";
        } else if (E_TERMCD.T302 == eTermcd) {
            sTerm = "2Y";
        } else if (E_TERMCD.T303 == eTermcd) {
            sTerm = "3Y";
        } else if (E_TERMCD.T304 == eTermcd) {
            sTerm = "4Y";
        } else if (E_TERMCD.T305 == eTermcd) {
            sTerm = "5Y";
        } else if (E_TERMCD.T306 == eTermcd) {
            sTerm = "6Y";
        } else if (E_TERMCD.T308 == eTermcd) {
            sTerm = "8Y";
        } else if (E_TERMCD.T330 == eTermcd) {
            sTerm = "30Y";
        } else {
            throw Sys.E0008(eTermcd);
        }
        return calDateByTerm(sCurDate, sTerm, null, null, 3, "A");

    }

    private static Date calXun(String sCurDate, int iTerm) {
        Calendar cal = Calendar.getInstance();

        cal.setTime(DateTools.covStringToDate(sCurDate));

        int iOldDay = cal.get(Calendar.DAY_OF_MONTH);

        int iLastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        boolean bLastFlag = false;
        boolean bFirstFlag = false;

        if (iOldDay == iLastDay || 20 == iOldDay || 10 == iOldDay) {
        	if(!(iOldDay == iLastDay && cal.get(Calendar.MONTH) == 1)) { //jym 特殊判断处理2月下旬天数不够
        		bLastFlag = true;
        	}
        } else if (1 == iOldDay || 11 == iOldDay || 21 == iOldDay) {
            bFirstFlag = true;
        }
        int iDay = 0;
        if (iOldDay >= 21) {
            iDay = 21;
        } else if (iOldDay >= 11) {
            iDay = 11;
        } else {
            iDay = 1;
        }
        int iOffset = iOldDay - iDay;
        int monOffset = 0;
        if (iTerm > 0) {
            iDay = iDay + 10 * iTerm;
            monOffset = iDay / 30;
            iDay = iDay % 30;
        } else {
            monOffset = iTerm / 3;
            int iDayOffset = 10 * iTerm % 30;
            if ((iDay + iDayOffset) > 0) {
                iDay = iDay + iDayOffset;
            } else {
                monOffset = monOffset - 1;
                iDay = 30 + iDayOffset + iDay;
                bizlog.debug("iDay is [%d]", iDay);
            }
        }
        cal.add(Calendar.MONTH, monOffset);
//        if (bLastFlag || (iOldDay > cal.getActualMaximum(Calendar.DAY_OF_MONTH))) {
        if (bLastFlag){
                    
            if (iDay >= 21) {
                iOldDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            else if (iDay >= 11) {
                iOldDay = 20;
            }
            else {
                iOldDay = 10;
            }
        }
        else if (bFirstFlag) {
            iOldDay = iDay;
        }
        else {
            iOldDay = iDay + iOffset;
        }
        cal.set(Calendar.DAY_OF_MONTH, iOldDay);
        bizlog.debug("旬计算日期为[%s]", DateTools.covDateToString(cal.getTime()));
        return cal.getTime();
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月4日-下午3:00:28</li>
     *         <li>功能说明：格式化周期/频率是否合法</li>
     *         </p>
     * @param sFrequence
     * @return 拆分后的频率值
     */
    public static String prcFrequence(String sFrequence) {

        if (sFrequence.length() < 3) {
            throw Sys.E0004(sFrequence);
        }
        int iFlag = 0;
        for (int i = 0; i < sFrequence.length(); i++) {

            if (!Character.isDigit(sFrequence.charAt(i))) {
                iFlag = i;
                break;
            }
        }

        String sQiXian = sFrequence.substring(0, iFlag);
        String sQiXianDW = sFrequence.substring(iFlag, iFlag + 1);
        String sRiQiFS = sFrequence.substring(iFlag + 1, iFlag + 2);
        String sDay;
        String sMonth;
        if (E_QIXIANDW.Quart.getValue().equals(sQiXianDW)) {
            sDay = sFrequence.substring(iFlag + 2, sFrequence.length() - 1);
            sMonth = sFrequence.substring(sFrequence.length() - 1);

            if (!"F".equals(sMonth) && !"M".equals(sMonth) && !"E".equals(sMonth) && !"D".equals(sMonth)) {
                throw Sys.E0001("季频率格式错误");
            }
        } else {
            sDay = sFrequence.substring(iFlag + 2, sFrequence.length());
            sMonth = "";
        }

        if (!CommUtil.isInEnum(E_QIXIANDW.class, sQiXianDW)) {
            throw Sys.E0004(sFrequence);
        }
        if (E_QIXIANDW.Day.getValue().equals(sQiXianDW) && CommUtil.isNotNull(sDay)) {
            throw Sys.E0004(sFrequence);
        }
        if (!CommUtil.isInEnum(E_JIARCLFS.class, sRiQiFS)) {
            throw Sys.E0004(sFrequence);
        }
        if (sDay.isEmpty()) {
            sFrequence = sQiXian + "-" + sQiXianDW + "-" + sRiQiFS;
        } else if (sMonth.isEmpty()) {
            sFrequence = sQiXian + "-" + sQiXianDW + "-" + sRiQiFS + "-" + sDay;
        } else {
            sFrequence = sQiXian + "-" + sQiXianDW + "-" + sRiQiFS + "-" + sDay + "-" + sMonth;
        }
        bizlog.debug("sFrequence is [%s]", sFrequence);
        return sFrequence;
    }

    public static boolean chkFrequence(String sFrequence) {

        try {
            prcFrequence(sFrequence);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
    /**
     * 
     * <p>Title:chkLonaTerm </p>
     * <p>贷款期限格式检查，数字+M/Q/Y</p>
     * @author wenbo@sunline.cn
     * @date   2017年10月2日 
     * @param period
     * @return
     */
    public static boolean chkLonaTerm(String period){
        try {
            String termfm = period;
            String termut ="";//期限单位
            if(CommUtil.compare(termfm.length(), 1)<0){
                throw LnError.detail.E0464();
            }
            termut = String.valueOf(termfm.charAt(termfm.length()-1));
            if(!E_QIXIANDW.Year.getValue().equals(termut) && !E_QIXIANDW.Month.getValue().equals(termut) && !E_QIXIANDW.Quart.getValue().equals(termut)){
                throw LnError.detail.E0465();
            }
        } catch (Exception e) {
            return false;
        }
        return true;       
    }
    /**
     * @Author T
     *         <p>
     *         <li>2014年3月4日-下午3:07:49</li>
     *         <li>功能说明：计算两日期间的天数</li>
     *         </p>
     * @param sStartDate
     *            起始日期
     * @param sEndDate
     *            终止日期
     * @param iType
     *            天数类型 0，实际天数，1，储蓄天数
     * @param iFlag
     *            计算方式 0，算头不算尾，1，起止都算
     * @return
     */
    public static int calDays(String sStartDate, String sEndDate, int iType, int iFlag) {
        int days = 0;

        if (!chkIsDate(sStartDate) || !chkIsDate(sEndDate)) {
            throw Sys.E0003();
        }

        bizlog.debug("sStartDate is [%s],  sEndDate is [%s]", sStartDate, sEndDate);

        if (0 == iType) {
            days = (int) ((covStringToDate(sEndDate).getTime() - covStringToDate(sStartDate).getTime()) / (24 * 60 * 60 * 1000));
            if (1 == iFlag) {
                days += 1;
            }
        } else {
            days = DateTools2.calDepositDays(covStringToDate(sStartDate), covStringToDate(sEndDate));

            if (1 == iFlag) {
                days += 1;
            }
        }

        return days;
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月7日-下午2:33:20</li>
     *         <li>功能说明：获取日历相关日期</li>
     *         </p>
     * @param sDate
     *            指定日期，传空取当前交易日期
     * @param sType
     *            01 - 月初 02 - 下月初 03 - 月末 04 - 下月末 05 - 季末 06 - 年末 07 - 下季初 08
     *            - 下年初 09 - 上一日 10 - 旬初 11 - 旬末 12 -年初 13 - 季初
     * @return
     */
    public static String getCalendarDate(String sDate, String sType) {
        String CalendarDate = null;
        if (CommUtil.isNull(sDate)) {
            sDate = CommTools.prcRunEnvs().getTrandt();
        }

        bizlog.debug("sDate is [%s]", sDate);

        Calendar ca = Calendar.getInstance();
        ca.setTime(DateTools.covStringToDate(sDate));
        if ("01".equals(sType)) {
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
        } else if ("02".equals(sType)) {
            ca.set(Calendar.MONTH, ca.get(Calendar.MONTH) + 1);
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
        } else if ("03".equals(sType)) {
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else if ("04".equals(sType)) {
            ca.set(Calendar.MONTH, ca.get(Calendar.MONTH) + 1);
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else if ("05".equals(sType)) {
            ca.set(Calendar.MONTH, (ca.get(Calendar.MONTH) / 3) * 3 + 2);
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else if ("06".equals(sType)) {
            ca.set(Calendar.MONTH, ca.getActualMaximum(Calendar.MONTH));
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else if ("07".equals(sType)) {
            ca.set(Calendar.MONTH, (ca.get(Calendar.MONTH) / 3) * 3 + 3);
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
        } else if ("08".equals(sType)) {
            ca.add(Calendar.YEAR, 1);
            ca.set(Calendar.MONTH, ca.getActualMinimum(Calendar.MONTH));
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
        } else if ("09".equals(sType)) {
            ca.add(Calendar.DAY_OF_MONTH, -1);
        } else if ("10".equals(sType)) {
            if (10 >= ca.get(Calendar.DAY_OF_MONTH)) {
                ca.set(Calendar.DAY_OF_MONTH, 1);
            } else if (20 >= ca.get(Calendar.DAY_OF_MONTH)) {
                ca.set(Calendar.DAY_OF_MONTH, 11);
            } else {
                ca.set(Calendar.DAY_OF_MONTH, 21);
            }
        } else if ("11".equals(sType)) {
            if (10 >= ca.get(Calendar.DAY_OF_MONTH)) {
                ca.set(Calendar.DAY_OF_MONTH, 10);
            } else if (20 >= ca.get(Calendar.DAY_OF_MONTH)) {
                ca.set(Calendar.DAY_OF_MONTH, 20);
            } else {
                ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
        } else if ("12".equals(sType)) {
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
        } else if ("13".equals(sType)) {
            ca.set(Calendar.MONTH, (ca.get(Calendar.MONTH) / 3) * 3);
            ca.set(Calendar.DAY_OF_MONTH, ca.getActualMinimum(Calendar.DAY_OF_MONTH));
        }
        CalendarDate = DateTools.covDateToString(ca.getTime());
        return CalendarDate;
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月11日-下午4:53:47</li>
     *         <li>功能说明：计算当前日期是星期几</li>
     *         </p>
     * @param sDate
     *            字符串日期
     * @return 整型
     */
    public static int calWeekDay(String sDate) {

        chkIsDate(sDate);
        Calendar ca = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        ca.setTime(covStringToDate(sDate));
        int iWeekDay = ca.get(Calendar.DAY_OF_WEEK);

        if (1 != iWeekDay) {
            return iWeekDay - 1;
        } else {
            return iWeekDay + 6;
        }

    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月11日-下午5:49:03</li>
     *         <li>功能说明：检查当前日期是否节假日</li>
     *         </p>
     * @param sJigouhao
     *            机构号
     * @param sDate
     *            字符串日期
     * @param sGuobiedm
     *            国别代码
     * @param iFlag
     *            节假日标记，1 - 对公假日 2 - 对私假日 3 - 不分对公对私
     * @return boolean true 节假日 false 非节假日
     */
    public static boolean isHoliday(String holiday, String holidayCode, ApBaseEnumType.E_HOLIDAYTYPE holidayType) {
        if (CommUtil.isNull(holidayCode))
            throw ApError.Aplt.E0049();
        String sCurrentDate = CommTools.prcRunEnvs().getTrandt();
        boolean isHoliday = false;
        AppHold appHoliday = AppHoldDao.selectOne_odb2(holidayCode, holidayType, holiday, false);
        if (CommUtil.isNotNull(appHoliday)) {
            if (CommUtil.compare(appHoliday.getEfctdt(), sCurrentDate) <= 0) {
                isHoliday = true;
            } else {
                isHoliday = false;
            }
        } else {
            isHoliday = false;
        }

        return isHoliday;
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月11日-下午5:32:02</li>
     *         <li>功能说明：检查当前日期所年是否闰年</li>
     *         </p>
     * @param sDate
     *            字符串日期
     * @return true - 是闰年 false - 不是闰年
     */
    public static boolean chkIsLeepYear(String sDate) {

        chkIsDate(sDate);
        int iYear = Integer.valueOf(sDate.substring(0, 4));

        if ((iYear % 4 == 0 && iYear % 100 != 0) || (iYear % 400 == 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月11日-下午5:22:03</li>
     *         <li>功能说明：获取系统时间戳</li>
     *         </p>
     * @return 至毫秒的长整型数值
     */
    public static long getTimeStamp() {

        return System.currentTimeMillis();
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月11日-下午5:10:31</li>
     *         <li>功能说明：将字符串日期转为日期类型</li>
     *         </p>
     * @param sDate
     *            字符串日期
     * @return Date 日期类型
     */
    public static Date covStringToDate(String sDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date d = new Date();
        try {
            d = dateFormat.parse(sDate);
        } catch (ParseException e) {
            throw Sys.E0003(e);
        }
        return d;
    }

    /**
     * @Author T
     *         <p>
     *         <li>2014年3月11日-下午5:20:25</li>
     *         <li>功能说明：将日期类型转为字符串日期</li>
     *         </p>
     * @param dtDate
     *            日期类型
     * @return 字符串日期
     */
    public static String covDateToString(Date dtDate) {
        Format f = new SimpleDateFormat("yyyyMMdd");

        return f.format(dtDate);
    }

    public static AppSydt chgSystemDate() {
        final BizLog bizlog = BizLogUtil.getBizLog(DateTools.class);
//      AppSydt tblAppSydt = AppSydtDao.selectOneWithLock_odb1(CommTools.getCenterCorpno(), true);
        bizlog.method("============="+CommTools.getTranCorpno()+"===============");
        AppSydt tblAppSydt = AppSydtDao.selectOneWithLock_odb1(CommTools.getTranCorpno(), true); //modified by xieqq 20170809
        tblAppSydt.setBflsdt(tblAppSydt.getLastdt());
        tblAppSydt.setLastdt(tblAppSydt.getSystdt());

        if ("".equals(tblAppSydt.getNextdt())) {
            throw Sys.E0003();
        } else {
            tblAppSydt.setSystdt(tblAppSydt.getNextdt());
        }

        tblAppSydt.setNextdt(tblAppSydt.getAfnxdt());
        tblAppSydt.setAfnxdt(DateTools.calDateByTerm(tblAppSydt.getAfnxdt(), "1D"));
        tblAppSydt.setYreddt(DateTools.calDateByFreq(tblAppSydt.getSystdt(), "1YAE"));

        AppSydtDao.updateOne_odb1(tblAppSydt);

        return tblAppSydt;
    }

    public static int chkStartStopDate(String staDate, String endDate) { // 需要结构体吗？
        int hasValue;
        if (CommUtil.isNull(staDate)) {
            if (CommUtil.isNotNull(endDate)) {
                throw Extn.E0002(staDate, endDate); // 开始日期必输
            }
            hasValue = 0;
        } else {
            if (CommUtil.isNull(endDate)) {
                throw Extn.E0002(staDate, endDate); // 结束日期必输
            }
            if (CommUtil.compare(staDate, endDate) > 0) {
                throw Extn.E0002(staDate, endDate); // 开始结束日期输入错误
            }
            hasValue = 1;
        }
        return hasValue;
    }

    public static String dateAdd(int amount, String date) {
        return DateUtil.dateAdd(Calendar.DAY_OF_MONTH, amount, date);

    }
    public static String dateAdd(String precision , String date,int amount) {
        return DateUtil.dateAdd(Integer.parseInt(precision), amount, date);

    }

    /**
     * 
     * <p>
     * <li>功能说明：按照存期转化为存期枚举类型</li>
     * <li>例如1M转化为E_TERMCD.T201，1Y转化为E_TERMCD.T301</li>
     * </p>
     * 
     * @param sTerm
     *            存期,取值如：1M,3M,6M,9M,1Y
     * @return 存期枚举类型
     */
    public static E_TERMCD calETermCdByTerm(String sTerm) {

        if (CommUtil.isNull(sTerm)) {
            throw Sys.E0001("存期/期限不能为空");
        }

        if (!CommUtil.isInEnum(E_QIXIANDW.class, sTerm.substring(sTerm.length() - 1))) {
            bizlog.debug("存期/期限类型不正确[%s]", sTerm.substring(sTerm.length() - 1));
            throw Sys.E0001("存期/期限类型不正确");
        }

        E_TERMCD termcd = null;
        int iTerm = 0;

        if (sTerm.substring(0, 1).equals("-")) {
            iTerm = -1 * Integer.parseInt(sTerm.substring(1, sTerm.length() - 1));
        } else {
            iTerm = Integer.parseInt(sTerm.substring(0, sTerm.length() - 1));
        }

        if (iTerm == 0) {
            throw Sys.E0001("无对应的存期或期限类型");
        }

        switch (sTerm.charAt(sTerm.length() - 1)) {
        case 'M':
            if(iTerm <= 6){
                termcd = E_TERMCD.T206;
            }else if(iTerm <= 12){
                termcd = E_TERMCD.T301;
            } else if (iTerm <= 36) {
                termcd = E_TERMCD.T303;
            } else if (iTerm <= 60) {
                termcd = E_TERMCD.T305;
            } else if (iTerm > 60) {
                termcd = E_TERMCD.T306;
            }
            break;
        case 'Y':
            if (iTerm == 1) {
                termcd = E_TERMCD.T301;
            } else if (iTerm <= 3) {
                termcd = E_TERMCD.T303;
            } else if (iTerm <= 5) {
                termcd = E_TERMCD.T305;
            } else if (iTerm > 5) {
                termcd = E_TERMCD.T306;
            }
            break;
        default:
            bizlog.debug("无对应的存期或期限类型");
            throw Sys.E0001("无对应的存期或期限类型");
        }

        return termcd;
    }
    /**
     *       
     * <p>
     *     <li>功能说明：按照存期转化为存期枚举类型</li>
     *     <li>例如1M转化为E_TERMCD.T201，1Y转化为E_TERMCD.T301</li>
     * </p>
     * @param sTerm 存期,取值如：1M,3M,6M,9M,1Y
     * @return 存期枚举类型
     */
    public static E_TERMCD calTermCdByTerm(String sTerm){
        
        if (CommUtil.isNull(sTerm)) {
            throw Sys.E0001("存期/期限不能为空");
        }

        if (!CommUtil.isInEnum(E_QIXIANDW.class, sTerm.substring(sTerm.length() - 1))) {
            bizlog.debug("存期/期限类型不正确[%s]", sTerm.substring(sTerm.length() - 1));
            throw Sys.E0001("存期/期限类型不正确");
        }

        E_TERMCD termcd = null;
        int iTerm = 0;

        if (sTerm.substring(0, 1).equals("-")) {
            iTerm = -1 * Integer.parseInt(sTerm.substring(1, sTerm.length() - 1));
        }
        else {
            iTerm = Integer.parseInt(sTerm.substring(0, sTerm.length() - 1));
        }
        
        if(iTerm == 0){
            throw Sys.E0001("无对应的存期或期限类型");
        }

        switch (sTerm.charAt(sTerm.length() - 1)) {
        case 'M':
            if(iTerm == 1){
                termcd = E_TERMCD.T201;
            }else if(iTerm == 2){
                termcd = E_TERMCD.T202;
            }else if(iTerm < 6){
                termcd = E_TERMCD.T203;
            }else if(iTerm < 9){
                termcd = E_TERMCD.T206;
            }else if(iTerm < 12){
                termcd = E_TERMCD.T209;
            }
            break;
        case 'Y':
            if(iTerm == 1){
                termcd = E_TERMCD.T301;
            }else if(iTerm == 2){
                termcd = E_TERMCD.T302;
            }else if(iTerm == 3){
                termcd = E_TERMCD.T303;
            }else if(iTerm == 4){
                termcd = E_TERMCD.T304;
            }else if(iTerm == 5){
                termcd = E_TERMCD.T305;
            }else if(iTerm == 6){
                termcd = E_TERMCD.T306;
            }else if(iTerm > 6 && iTerm<= 8){
                termcd = E_TERMCD.T308;
            }else if(iTerm > 8 && iTerm <= 30){
                termcd = E_TERMCD.T330;
            }
            break;
        default:
            bizlog.debug("无对应的存期或期限类型");
            throw Sys.E0001("无对应的存期或期限类型");
        }
        
        return termcd;
        
    }
    
    /**
     * @Author Administrator
     *         <p>
     *         <li>2014年12月10日-下午7:54:37</li>
     *         <li>功能说明：通过频率计算下一日期</li>
     *         </p>
     * @param sCurDate
     *            当前日期
     * @param sFrequence
     *            周期/频率
     * @return
     */
    public static String calDateByCycle(String sCurDate, String sCycleType) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String outDate = "";
        if (!DateTools.chkIsDate(sCurDate)) {
            bizlog.debug("输入日期格式不正确[%s]", sCurDate);
            throw Sys.E0001("输入日期格式不正确");
        }

        if (CommUtil.equals(sCycleType, "01")) {
            // 月末
            Date maxDate = getLastDayOfMonth(DateTools.covStringToDate(sCurDate));

            outDate = format.format(maxDate);
        } else if (CommUtil.equals(sCycleType, "02")) {
            // 季末

            Date maxDate = getLastDayOfQuarter(DateTools.covStringToDate(sCurDate));

            outDate = format.format(maxDate);
        } else if (CommUtil.equals(sCycleType, "03")) {
            // 年末
            Calendar currCal = Calendar.getInstance();
            int currentYear = currCal.get(Calendar.YEAR);
            Date maxDate = getYearLast(currentYear);
            outDate = format.format(maxDate);
        } else {
            throw Sys.E0001("周期类型不正确，【" + sCycleType + "】未定义!");
        }

        return outDate;
    }

    /**
     * 返回指定日期的季的最后一天
     * 
     * @param year
     * @param quarter
     * @return
     */
    public static Date getLastDayOfQuarter(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return getLastDayOfQuarter(calendar.get(Calendar.YEAR),
         getQuarterOfYear(date));
    }

    /**
     * 返回指定日期的季度
     * 
     * @param date
     * @return
     */
    public static int getQuarterOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) / 3 + 1;
    }

    /**
     * 返回指定年季的季的最后一天
     * 
     * @param year
     * @param quarter
     * @return
     */
    public static Date getLastDayOfQuarter(Integer year, Integer quarter) {
        Calendar calendar = Calendar.getInstance();
        Integer month = new Integer(0);
        if (quarter == 1) {
            month = 3 - 1;
        } else if (quarter == 2) {
            month = 6 - 1;
        } else if (quarter == 3) {
            month = 9 - 1;
        } else if (quarter == 4) {
            month = 12 - 1;
        } else {
            month = calendar.get(Calendar.MONTH);
        }
        return getLastDayOfMonth(year, month);
    }

    /**
     * 返回指定年月的月的最后一天
     * 
     * @param year
     * @param month
     * @return
     */
    public static Date getLastDayOfMonth(Integer year, Integer month) {
        Calendar calendar = Calendar.getInstance();
        if (year == null) {
            year = calendar.get(Calendar.YEAR);
        }
        if (month == null) {
            month = calendar.get(Calendar.MONTH);
        }
        calendar.set(year, month, 1);
        calendar.roll(Calendar.DATE, -1);
        return calendar.getTime();
    }

    /**
     * 返回指定年的月的最后一天
     * 
     * @param year
     * @param month
     * @return
     */
    public static Date getYearLast(Integer year) {
        Calendar calendar = Calendar.getInstance();
        if (year == null) {
            year = calendar.get(Calendar.YEAR);
        }
        calendar.clear();
        calendar.set(Calendar.YEAR, year);

        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }

    /**
     * 返回指定日期的月的最后一天
     * 
     * @param year
     * @param month
     * @return
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR),
         calendar.get(Calendar.MONTH), 1);
        calendar.roll(Calendar.DATE, -1);
        return calendar.getTime();
    }
    
    public static String getStartDateStr(String stardt)
       {
       SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        
        Date date = new Date();
         try {
           date = sdf.parse(stardt);
        } catch (ParseException e) {
          throw ExceptionUtil.wrapThrow("日期[%s]格式转换失败", e, new String[] { stardt });
        }
         
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
         
         return sdf1.format(date) + " 00:00:00";
       }
      
    
    public static String getEndDateStr(String enddat)
       {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
         Date date = new Date();
         try {
          date = sdf.parse(enddat);
        } catch (ParseException e) {
           throw ExceptionUtil.wrapThrow("日期[%s]格式转换失败", e, new String[] { enddat });
         }
         sdf = new SimpleDateFormat("yyyy-MM-dd");
       return sdf.format(date) + " 23:59:59";
       }
   
    /**
	 * @Author 
	 *         <p>
	 *         <li>2016年12月22日-下午12:40:57</li>
	 *         <li>功能说明：检查日期是否在指定范围</li>
	 *         </p>
	 * @param date
	 *            需要检查的日期
	 * @param effectDate
	 *            生效日期
	 * @param includeEffectDate
	 *            是否包含生效日期
	 * @param expiryDate
	 *            失效日期
	 * @param includeExpiryDate
	 *            是否包含失效日期
	 * @return 日期是否在指定范围
	 */
	public static boolean dateBetween(String date, String effectDate, boolean includeEffectDate, String expiryDate, boolean includeExpiryDate) {
		int day1 = calDays(date, expiryDate,0,0); // 检查日期 - 生效日期
		int day2 = calDays(effectDate, date,0,1); // 失效日期 - 检查日期

		if ((day1 > 0 || (includeExpiryDate && day1 == 0)) && (day2 > 0 || (includeEffectDate && day2 == 0))) {
			return true;
		}

		return false;
	}
    
	/**
	 * @Author jollyja
	 *         <p>
	 *         <li>2017年1月11-上午9:29:30</li>
	 *         <li>功能说明: 判断日期是对应期间的最后一个日期</li>
	 *         </p>
	 * @param type
	 *            M-月初 Q-季初 Y-年初 H-半年初 T-旬初 D-基准日期 W-本周开始的周日
	 * @param date
	 *            字符串日期yyyyMMdd
	 * @return
	 */
	public static boolean isLastDay(String type, String date) {
		if (CommUtil.isNull(date))
			return false;
		else
			return date.equals(lastDay(type, date));
	}

	/**
	 * @Author jollyja
	 *         <p>
	 *         <li>2016年12月9日-上午9:27:09</li>
	 *         <li>功能说明：根据基准日期、类型计算最近日期</li>
	 *         </p>
	 * @param type
	 *            M-月末 Q-季末 Y-年末 H-半年末 T-旬末 D-返回基准日期 W-返回本周的周六日期
	 * @param date
	 *            基准日期 (字符串类型yyyyMMdd)
	 * @return
	 */
	public static String lastDay(String type, String date) {
		return DateUtil.lastDay(date, type.toUpperCase());
	}
	
	/**
	 * @Author jollyja
	 *         <p>
	 *         <li>2017年1月11-上午9:29:30</li>
	 *         <li>功能说明: 判断日期是对应期间的第一个日期</li>
	 *         </p>
	 * @param type
	 *            M-月初 Q-季初 Y-年初 H-半年初 T-旬初 D-基准日期 W-本周开始的周日
	 * @param date
	 *            字符串日期yyyyMMdd
	 * @return
	 */
	public static boolean isFirstDay(String type, String date) {
		if (CommUtil.isNull(date))
			return false;
		else
			return date.equals(firstDay(type, date));
	}
	
	/**
	 * @Author jollyja
	 *         <p>
	 *         <li>2016年12月9日-上午9:29:30</li>
	 *         <li>功能说明：根据基准日期、类型计算最近的额前一个日期</li>
	 *         </p>
	 * @param type
	 *            M-月初 Q-季初 Y-年初 H-半年初 T-旬初 D-基准日期 W-本周开始的周日
	 * @param date
	 *            字符串日期yyyyMMdd
	 * @return
	 */
	public static String firstDay(String type, String date) {
		return DateUtil.firstDay(date, type.toUpperCase());
	}
	/**
	 * 获取当前机器时间，字符串格式（yyyy-MM-dd HH:mm:ss.SSS）
	 */
	public static String getComputerDateTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return df.format(new Date());
	}
	
	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月12日-下午5:56:20</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @return
	 */
	public static ApSysDateStru getGlDateInfo() {

		String sFrdm = CommTools.getTranCorpno();
		AppSydt tblKapp_kjidat = AppSydtDao.selectOne_odb1(sFrdm, true);

		if (tblKapp_kjidat == null)
			throw Sys.E0001("读取会计系统日期失败，无对应记录[" + sFrdm + "]");

		ApSysDateStru cplSysDateInfo = SysUtil.getInstance(ApSysDateStru.class);

		cplSysDateInfo.setHostdt(DateTools.getSystemDate());
		cplSysDateInfo.setTrantm(DateTools.getTransTimestamp());

		cplSysDateInfo.setSystdt(tblKapp_kjidat.getSystdt());
		cplSysDateInfo.setLastdt(tblKapp_kjidat.getLastdt());
		cplSysDateInfo.setBflsdt(tblKapp_kjidat.getBflsdt());
		cplSysDateInfo.setNextdt(tblKapp_kjidat.getNextdt());
		cplSysDateInfo.setAfnxdt(tblKapp_kjidat.getAfnxdt());
		cplSysDateInfo.setYreddt(tblKapp_kjidat.getYreddt());

		return cplSysDateInfo;
	}
	
}
