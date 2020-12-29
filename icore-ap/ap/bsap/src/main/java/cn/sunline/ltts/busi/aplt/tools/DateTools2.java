package cn.sunline.ltts.busi.aplt.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.ApPubErr.APPUB;

/**
 * 日期处理工具类
 * 
 * @author cuijia
 * @since 20150507
 */
public class DateTools2 {
    /**
     * 获取日期年份
     * 
     * @param dt
     *        日期
     * @return 年份
     */
    public static int getYear(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.YEAR);
    }

    /**
     * 获取日期月份
     * 
     * @param dt
     *        日期
     * @return 月份
     */
    public static int getMonth(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.MONTH);
    }

    /**
     * 获取日期日
     * 
     * @param dt
     *        日期
     * @return 某日
     */
    public static int getDay(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(Calendar.DATE);
    }

    /**
     * 设置日期日
     * 
     * @param dt
     *        日期
     * @param day
     *        日
     * @return
     */
    public static Date setDay(Date dt, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.set(Calendar.DATE, day);
        return cal.getTime();
    }

    /**
     * 日期比较
     * 
     * @param d1
     * @param d2
     * @return d1 < d2 返回-1 d1 > d2 返回1 d1 = d2 返回0
     */
    public static int compareDate(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);

        return cal1.compareTo(cal2);
    }

    /**
     * 增加年数
     * 
     * @param dt
     *        日期
     * @param years
     *        年数
     * @return 计算后日期
     */
    public static Date addYears(Date dt, int years) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.YEAR, years);

        return cal.getTime();
    }

    /**
     * 增加月数
     * 
     * @param dt
     *        日期
     * @param months
     *        月数
     * @return 计算后日期
     * 
     *         例如: 20150228 + 1M = 20150328 20150331 + 1M = 20150430 20150430 +
     *         1M = 20150530
     */
    public static Date addMonths(Date dt, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.MONTH, months);

        return cal.getTime();
    }

    /**
     * 增加月数(按照实际月底计算)
     * 
     * @param dt
     *        日期
     * @param months
     *        月数
     * @return 计算后日期
     * 
     *         例如: 20150228 + 1M = 20150331 20150331 + 1M = 20150430 20150430 +
     *         1M = 20150531
     */
    public static Date addMonthsReal(Date dt, int months) {
        Date month = addMonths(dt, months);
        if (isMonthEnd(dt))
            return getMonthEnd(month);
        return month;
    }

    /**
     * 增加天数
     * 
     * @param dt
     *        日期
     * @param days
     *        天数
     * @return 计算后日期
     */
    public static Date addDays(Date dt, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(Calendar.DATE, days);

        return cal.getTime();
    }

    /**
     * 判断是否月底
     * 
     * @param dt
     *        日期
     * @return 月底返回true
     * 
     *         日期增加一天，如果为1号就判断为月底
     */
    public static boolean isMonthEnd(Date dt) {
        if (getDay(addDays(dt, 1)) == 1)
            return true;
        return false;
    }

    /**
     * 判读是否月初
     * 
     * @param dt 日期
     * @return 月初返回true
     */
    public static boolean isMonthFirst(Date dt) {
        if (getDay(dt) == 1)
            return true;
        return false;
    }

    /**
     * 判断是否年初
     * 
     * @param dt 日期
     * @return 月初返回true
     */
    public static boolean isYearFirst(Date dt) {
        if (getMonth(dt) == 0 && getDay(dt) == 1)
            return true;
        return false;
    }

    /**
     * 判断是否年底
     * 
     * @param dt 日期
     * @return 年底返回true
     */
    public static boolean isYearEnd(Date dt) {
        if (getMonth(dt) == 11 && getDay(dt) == 31)
            return true;
        return false;
    }

    /**
     * 获取当月月初日期
     * 
     * @param dt
     *        日期
     * @return
     */
    public static Date getMonthFirst(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.set(Calendar.DATE, 1);

        return cal.getTime();
    }

    /**
     * 获取当月月末
     * 
     * @param dt
     *        日期
     * @return
     */
    public static Date getMonthEnd(Date dt) {
        if (isMonthEnd(dt))
            return dt;
        return addDays(getMonthFirst(addMonths(dt, 1)), -1);
    }

    /**
     * 
     * <p>Title:getYearFirt </p>
     * <p>Description: 当前日期年初日期</p>
     * 
     * @author cuijia
     * @date 2017年6月19日
     * @param dt
     * @return
     */
    public static Date getYearFirt(Date dt) {
        if (isYearFirst(dt))
            return dt;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DATE, 1);

        return cal.getTime();
    }

    /**
     * 
     * <p>Title:getYearLast </p>
     * <p>Description: 当前日期年底日期</p>
     * 
     * @author cuijia
     * @date 2017年6月19日
     * @param dt
     * @return
     */
    public static Date getYearEnd(Date dt) {
        if (isYearEnd(dt))
            return dt;
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DATE, 31);

        return cal.getTime();
    }

    /**
     * 计算实际天数差
     * 
     * @param startDt
     *        开始日期
     * @param endDt
     *        结束日期
     * @return 实际天数差
     */
    public static int calDiffDays(Date startDt, Date endDt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDt);
        long time1 = cal.getTimeInMillis();
        cal.setTime(endDt);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }

    /**
     * 计算两个日期年数差
     * 
     * @param startDt
     *        开始日期
     * @param endDt
     *        结束日期
     * @return 年数差
     */
    public static int calDiffYears(Date startDt, Date endDt) {
        int years = 0;
        int year1 = getYear(startDt);
        int year2 = getYear(endDt);

        years = year2 - year1;

        int i = compareDate(addYears(startDt, years), endDt);

        if (i > 0)
            years--;

        return years;
    }

    /**
     * 计算两个日期月数差
     * 
     * @param startDt
     *        开始日期
     * @param endDt
     *        结束日期
     * @return 月数差
     */
    public static int calDiffMonths(Date startDt, Date endDt) {
        int years = calDiffYears(startDt, endDt);
        Date tmp = addYears(startDt, years);

        int month1 = getMonth(tmp);
        int month2 = getMonth(endDt);

        int months = month2 - month1;
        if (months < 0) {
            months = 12 + months;
        } else if (months == 0) {
            int starDay = getDay(tmp);
            int endDay = getDay(endDt);
            if (starDay > endDay) {
                months = 11;
            }
        }
        int i = compareDate(addMonths(tmp, months), endDt);

        if (i > 0)
            months--;

        return years * 12 + months;
    }
    
    
    /**
     * 计算两个日期月数差
     * 
     * @param startDt
     *        开始日期
     * @param endDt
     *        结束日期
     * @param iFlag
     *        是否跳过日期
     *        
     * @return 月数差
     */
    public static int calDiffMonths(Date startDt, Date endDt ,boolean iflag) {
        int years = calDiffYears(startDt, endDt);
        Date tmp = addYears(startDt, years);

        int month1 = getMonth(tmp);
        int month2 = getMonth(endDt);

        int months = month2 - month1;
        if (months < 0) 
            months = 12 + months;
        
        int i = compareDate(addMonths(tmp, months), endDt);

        if (months == 0){
        	
        	int starDay = getDay(tmp);
        	int endDay = getDay(endDt);
        	
        	if(starDay > endDay){
        		months = 12 + months - 1;
        		if(!iflag){
        			months++;
        		}
        	}
        }
   
        if(i > 0 && iflag)
        	months--;

        return years * 12 + months;
    }
    
    /**
     * 计算储蓄天数
     * 
     * @param startDt
     *        开始日期
     * @param endDt
     *        结束日期
     * @return 储蓄天数
     */
    public static int calDepositDays(Date startDt, Date endDt) {//处理calDepositDaysOld二月底到其他月底日期的天数 != (endM - startM)*30 的bug
    	int startM = getMonth(startDt) + 1;
    	int endM = getMonth(endDt) + 1;
    	int depositdays = 0;
    	int b = 30;
    	
    	if(startM == 2 && startM != endM && isMonthEnd(startDt)) {
    		int days = (endM - startM)*30;
    		depositdays = calDepositDaysOld(startDt, endDt);
    		if(depositdays > days) {
    			depositdays = days;
    		}
    	} else {
    		depositdays = calDepositDaysOld(startDt, endDt);
    	}
    	
    	return depositdays;
    }

    /**
     * 计算储蓄天数
     * 
     * @param startDt
     *        开始日期
     * @param endDt
     *        结束日期
     * @return 储蓄天数
     */
    public static int calDepositDaysOld(Date startDt, Date endDt) {

        Date startTmp = startDt;
        Date endTmp = endDt;
        // 先统一将31号处理成30号
        int day = getDay(startDt);
        if (day == 31)
            startTmp = setDay(startDt, 30);
        day = getDay(endDt);
        if (day == 31)
            endTmp = setDay(endDt, 30);

        int years = calDiffYears(startTmp, endTmp);
        Date tmpYear = addYears(startTmp, years);
        int months = calDiffMonths(tmpYear, endTmp);

        Date tmpMonth = addMonths(tmpYear, months);

        int days = 0;
        if (years + months == 0) {
            days = calDiffDays(startDt, endDt);
        } else {
            days = calDiffDays(tmpMonth, endTmp);
        }

        int depositDays= 0;
        
        //按照往后跳的规则再算一遍
        int months_end = calDiffMonths(tmpYear, endTmp ,false);
        
        Date tmpMonth_end = addMonths(tmpYear, months_end);
        
        int days_end = 0;
        if(years + months_end == 0){
        	days_end = calDiffDays(startDt, endDt);
        } else {
        	days_end = calDiffDays(tmpMonth_end, endTmp);
        }
        
        if(CommUtil.compare(years * 360 + months * 30 + days, years * 360 + months_end * 30 + days_end) >0){
        	depositDays = years * 360 + months_end * 30 +days_end;
        } else {
        	depositDays = years * 360 + months * 30 +days;
        }
        
        return depositDays;
    }
    

    /**
     * 将账单年月转为日期类型返回当月1号
     * 
     * @param stmtDate
     * @return
     * @throws Exception
     * @throws ProcessException
     */
    public static Date parseStmtDate(String stmtDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        sdf.setLenient(false);

        if (stmtDate.length() != 6 || !StringUtils.isNumeric(stmtDate)) {
            throw ApError.Aplt.E0000("无效日期格式");
        }
        try {
            return sdf.parse(stmtDate);
        } catch (ParseException e) {
            throw ApError.Aplt.E0000("日期错误");
        }
    }

    /**
     * 转换交易时间为14位字符串
     * 
     * @param time 时间戳
     * @return
     */
    public static String get14DateTime(Long time) {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(time));
    }

    /**
     * @Author jollyja
     *         <p>
     *         <li>2017年1月11-上午9:29:30</li>
     *         <li>功能说明: 判断日期是对应期间的最后一个日期</li>
     *         </p>
     * @param type
     *        M-月初 Q-季初 Y-年初 H-半年初 T-旬初 D-基准日期 W-本周开始的周日
     * @param date
     *        字符串日期yyyyMMdd
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
     *        M-月末 Q-季末 Y-年末 H-半年末 T-旬末 D-返回基准日期 W-返回本周的周六日期
     * @param date
     *        基准日期 (字符串类型yyyyMMdd)
     * @return
     */
    public static String lastDay(String type, String date) {

        return DateTimeUtil.lastDay(date, type.toUpperCase());
    }

    /**
     * @Author jollyja
     *         <p>
     *         <li>2016年12月9日-上午9:29:30</li>
     *         <li>功能说明：根据基准日期、类型计算最近的额前一个日期</li>
     *         </p>
     * @param type
     *        M-月初 Q-季初 Y-年初 H-半年初 T-旬初 D-基准日期 W-本周开始的周日
     * @param date
     *        字符串日期yyyyMMdd
     * @return
     */
    public static String firstDay(String type, String date) {

        return DateTimeUtil.firstDay(date, type.toUpperCase());
    }

    /**
     * @Author jollyja
     *         <p>
     *         <li>2016年12月9日-上午9:20:49</li>
     *         <li>功能说明：根据类型，数量，基准日期计算结果日期</li>
     *         </p>
     * @param type
     *        day\dd\DD\d\D week\ww\WW\w\W month\mm\MM\m\M quarter\qq\QQ\q\Q
     *        year\yy\YY\y\Y
     * @param date
     *        基准日期
     * @param num
     *        日期间隔
     * @return
     */
    public static String dateAdd(String type, String date, int num) {

        // 将D,Y,M 转换为DD, YY, MM
        if (1 == type.length())
            type = type + type;

        return DateTimeUtil.dateAdd(type.toLowerCase(), date, num);
    }

    /**
     * 
     * @Author lid
     *         <p>
     *         <li>2017年3月16日-上午10:11:48</li>
     *         <li>功能说明：检查是否是格式的日期字符串</li>
     *         </p>
     * @param date
     * @param format
     * @return
     */
    public static boolean isDateString(String date, String format) {
        if (CommUtil.isNull(date))
            return false;

        SimpleDateFormat df = new SimpleDateFormat(format);

        try {
            String tmp = df.format(toDate(date, format));
            if (date.equals(tmp)) {
                return true;
            }
        } catch (Throwable e) {
            ;
        }
        return false;
    }

    /**
     * 将日期字符串格式为日期对象
     * 
     * @param date
     * @param format
     * @return
     */
    public static Date toDate(String date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        Date d = new Date();
        try {
            d = df.parse(date);
        } catch (ParseException e) {
            throw APPUB.E0011(date);
        }
        return d;
    }
}
