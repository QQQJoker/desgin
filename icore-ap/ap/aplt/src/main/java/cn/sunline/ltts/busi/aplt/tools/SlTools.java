package cn.sunline.ltts.busi.aplt.tools;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import org.junit.Test;

//import com.greenpineyu.fel.FelEngine;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.tables.KSysCommFieldTable;
import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_JILUZTAI;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.busi.sdk.component.BaseComp.FileTransfer;
import cn.sunline.adp.cedar.busi.sdk.type.CompTypes.FileTransferResult;
import cn.sunline.adp.cedar.service.remote.exception.RemoteTimeoutException;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.vine.base.exception.BusinessException;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.amsg.util.SMSUtil;
import cn.sunline.ltts.busi.aplt.component.AbstractComponent;
import cn.sunline.ltts.busi.aplt.namedsql.ApSysBatchDao;
import cn.sunline.ltts.busi.aplt.servicetype.ApFileTask;
import cn.sunline.ltts.busi.aplt.servicetype.ApFileTask.FileInteractiveReqByServer;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpGlbl;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpGlblDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydtDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Pss_file_rgstDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Pss_warn_infoDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.pss_file_rgst;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.pss_warn_info;
import cn.sunline.ltts.busi.aplt.type.ApDefineType;
import cn.sunline.ltts.busi.aplt.type.ApDefineType.PssFilePlat;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;
import cn.sunline.ltts.busi.sys.dict.BaseDict.Comm;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;
import cn.sunline.ltts.busi.sys.errors.OdError;
import cn.sunline.ltts.busi.sys.errors.SlError;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.Pss_mobile_recdDao;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.pss_mobile_recd;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SERVTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.BaseEnumType2.E_FAILTO;
import cn.sunline.ltts.busi.sys.type.BaseEnumType2.E_FILETP;
import cn.sunline.ltts.busi.sys.type.CnBaseEnumType.E_FAILTYPE;
import cn.sunline.ltts.busi.sys.type.OdEnumType.E_FAILTY;
import cn.sunline.ltts.busi.sys.type.PbEnumType2.E_FILEST;
import cn.sunline.ltts.busi.sys.type.PbEnumType2.E_FLJHST;
import cn.sunline.ltts.busi.sys.type.PbEnumType2.E_SYSNAME;
import cn.sunline.ltts.busi.sys.type.PbEnumType2.E_SYSTEMTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType2.E_WARNTY;
import cn.sunline.ltts.busi.sys.type.SlEnumType.E_PROPOP;
import cn.sunline.ltts.busi.sys.type.SlEnumType.E_PRVLTP;
import cn.sunline.ltts.busi.sys.type.SlEnumType.E_RULEPP;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

//import cn.sunline.ltts.busi.sys.type.RpEnumType.E_SENDOB;

/**
 * 
 * <p>
 * Title: SlTools.java
 * </p>
 * 
 * <p>
 * Description: 销售工厂系统工具类
 * </p>
 *  * <p>
 * Create Time : 2016年6月20日 下午4:39:18
 * </p>
 * 
 * @author : wl </br>
 *         <p>
 *         Copyright: Copyright (c) 2012
 *         </p>
 *         <p>
 *         Company: com.sunline
 *         </p>
 * @version 1.0 </br>
 *          <p>
 *          ------------------------------
 *          </p>
 *          <p>
 *          Modify Time :
 *          </p>
 *          <p>
 *          Mender :
 *          </p>
 *          <p>
 *          Reason ：
 *          </p>
 *          <p>
 *          ------------------------------
 *          </p>
 */
public class SlTools {
	
	private static final BizLog bizLog = BizLogUtil.getBizLog(SlTools.class);
	
	//外系统发起产品变更、额度更新时引入虚拟柜员，销售工厂在修改产品时对此柜员开通特殊权限
	public static final String SYS_UPDTUS = "999S206";
	public static final String BANK_CODE = "999";
	//理财撤单标志"CP-251-021"为否，不予撤销新加校验
	public static final String PDU_PROPID = "CP-251-021";
	//金额格式化
	public static final NumberFormat BANKFMT = new DecimalFormat("###,###,###,##0.00");
	
	/**
	 * 获取sql类型的当期日期
	 * 
	 * @return
	 */
	public static java.sql.Date getSqlDate() {
		return new java.sql.Date(new java.util.Date().getTime());
	}

	/**
	 * 获取sql类型的当期日期和时间
	 * 
	 * @return
	 */
	public static java.sql.Timestamp getSqlTimestamp() {
		return new java.sql.Timestamp(new java.util.Date().getTime());
	}

	/**
	 * 
	 * <p>
	 * Description: 获取sql类型的当期时间
	 * </p>
	 * 
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午2:38:15
	 *         </p>
	 */
	public static java.sql.Time getSqlTime() {
		return new java.sql.Time(new java.util.Date().getTime());
	}
	

	/**
	 * 
	 * <p>
	 * Description: sql类型的Time转换为String(HH:mm)
	 * </p>
	 * 
	 * @param time
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午2:44:01
	 *         </p>
	 */
	public static String SqlTimeToString(java.sql.Time time) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.format(time);
	}

	/**
	 * 
	 * <p>
	 * Description: sql类型的Time转换为String(HH:mm:ss)
	 * </p>
	 * 
	 * @param time
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午2:44:01
	 *         </p>
	 */
	public static String SqlTimeToString2(java.sql.Time time) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(time);
	}
	/**
	 * 
	 * <p>
	 * Description: sql类型的Time转换为String(HHmmss)
	 * </p>
	 * 
	 * @param time
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午2:44:01
	 *         </p>
	 */
	public static String SqlTimeToString3(java.sql.Time time) {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
		return sdf.format(time);
	}

	/**
	 * 
	 * <p>
	 * Description: sql类型的Date转换为String(yyyyMMdd)
	 * </p>
	 * 
	 * @param date
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午2:50:29
	 *         </p>
	 */
	public static String SqlDateToString1(java.sql.Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(date);
	}
	
	/**
	 * 
	 * @Title: SimpleDateToSting 
	 * @Description: String经过date由 yyyy-MM-dd转yyyyMMdd
	 * @param str
	 * @return
	 * @author baifp
	 * @date 2017年8月10日 下午5:03:20 
	 * @version V2.3.0
	 */
	public static String SimpleDateToSting(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			java.util.Date date = sdf.parse(str);
			
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
			
			return sdf1.format(date);
		} catch (ParseException e) {
			throw SlError.SlComm.E0001("数据格式转换异常！");
		}
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
	 * 时间比较
	 * @param t1
	 * @param t2
	 * @throws ParseException 
	 */
	public static int compareTime(String t1,String t2){
		Date date1 ,date2;
		DateFormat formart = new SimpleDateFormat("HH:mm:ss");
		int a = 0;
		try {
			date1 = formart.parse(t1);
			date2 = formart.parse(t2);
			if (date1.compareTo(date2) < 0) {
				return a=1;
			} else {
				return a=0;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return a;
		
		
	}
	/**
	 * 时间戳比较
	 * @param t1
	 * @param t2
	 * @throws ParseException 
	 */
	public static int compareTimeDatm(String t1,String t2){
		Date date1 ,date2;
		DateFormat formart = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int a = 0;
		try {
			date1 = formart.parse(t1);
			date2 = formart.parse(t2);
			return date1.compareTo(date2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -2;
		
		
	}
	/**
	 * 时间比较
	 * @param t1
	 * @param t2
	 * @throws ParseException 
	 * @return t1 < t2 返回-1 t1 > t2 返回1 t1 = t2 返回0 ,
	 */
	public static int compareTimeN(String t1,String t2){
		Date date1 ,date2;
		DateFormat formart = new SimpleDateFormat("HH:mm:ss");
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		
		try {
			date1 = formart.parse(t1);
			date2 = formart.parse(t2);
			cal1.setTime(date1);
			cal2.setTime(date2);
			return cal1.compareTo(cal2);
		} catch (ParseException e) {
			throw SlError.SlComm.E0001("数据格式转换异常！");
		}
		
	}
	/**
	 * 
	 * <p>
	 * Description: sql类型的Timestamp转换为String(yyyy-MM-dd HH:mm:ss)
	 * </p>
	 * 
	 * @param timestamp
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午2:50:34
	 *         </p>
	 */
	public static String SqlTimestampToString(java.sql.Timestamp timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(timestamp);
	}
	
	
	
	public static String SqlTimestampToString2(java.sql.Timestamp timestamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(timestamp);
	}
	
	/**
	 * 
	 * <p>
	 * Description: sql类型的Date转换为String(yyyy-MM-dd)
	 * </p>
	 * 
	 * @param Date
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午2:50:34
	 *         </p>
	 */
	public static String SqlDateToString(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}

	/**
	 * 
	 * <p>
	 * Description: String(yyyyMMdd)转换为sql类型的Date:yyyy-MM-dd
	 * </p>
	 * 
	 * @param str
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午3:07:43
	 *         </p>
	 */
	public static java.sql.Date SqlStringToDate(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		try {
			java.util.Date date = sdf.parse(str);
			return new java.sql.Date(date.getTime());
		} catch (ParseException e) {
			throw SlError.SlComm.E0001("数据格式转换异常！");
		}

	}
	/**
	 * 
	 * @Title: SqlStringToDate2 
	 * @Description: String(yyyy-MM-dd)转换为sql类型的Date:yyyy-MM-dd 
	 * @param str
	 * @return
	 * @author baifangping
	 * @date 2016年9月14日 上午10:20:55 
	 * @version V2.3.0
	 */
	public static java.sql.Date SqlStringToDate2(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			java.util.Date date = sdf.parse(str);
			return new java.sql.Date(date.getTime());
		} catch (ParseException e) {
			throw SlError.SlComm.E0001("数据格式转换异常！");
		}

	}
	/**
	 * 
	 * <p>
	 * Description: String(HH:mm:ss)转换为sql类型的Time : HH:mm:ss
	 * </p>
	 * 
	 * @param str
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午3:08:45
	 *         </p>
	 */
	public static java.sql.Time SqlStringToTime(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		try {
			java.util.Date date = sdf.parse(str);
			return new java.sql.Time(date.getTime());
		} catch (ParseException e) {
			throw SlError.SlComm.E0001("数据格式转换异常！");
		}

	}

	/**
	 * 
	 * <p>
	 * Description: String(HHmmss)转换为sql类型的Time : HH:mm:ss
	 * </p>
	 * 
	 * @param str
	 * @return
	 * 
	 *         <p>
	 *         create by AT TIME 2016年7月4日 下午3:08:45
	 *         </p>
	 */
	public static java.sql.Time SqlStringToTime2(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
		try {
			java.util.Date date = sdf.parse(str);
			return new java.sql.Time(date.getTime());
		} catch (ParseException e) {
			throw SlError.SlComm.E0001("数据格式转换异常！");
		}

	}
	
	/**
	 * 
	 * <p>
	 * Description: String(HH:mm)转换为sql类型的Time : HH:mm
	 * </p>
	 * @param str
	 * @return
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午9:51:50
	 *         </p>
	 */
	public static java.sql.Time SqlStringToTime1(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		try {
			java.util.Date date = sdf.parse(str);
			return new java.sql.Time(date.getTime());
		} catch (ParseException e) {
			throw SlError.SlComm.E0001("数据格式转换异常！");
		}

	}

	/**
	 * 
	 * <p>
	 * Description: String(yyyy-MM-dd HH:mm:ss)转换为sql类型的Timestamp : yyyy-MM-dd HH:mm:ss
	 * </p>
	 * 
	 * @param str
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年7月4日 下午3:09:27
	 *         </p>
	 */
	public static java.sql.Timestamp SqlStringToTimestamp(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			java.util.Date date = sdf.parse(str);
			return new java.sql.Timestamp(date.getTime());
		} catch (ParseException e) {
			throw SlError.SlComm.E0001("数据格式转换异常！");
		}

	}
	
	/**
	 * 
	 * <p>
	 * Description: String(yyyyMMddHHmmss)转换为sql类型的Timestamp : yyyy-MM-dd HH:mm:ss
	 * </p>
	 * 
	 * @param str
	 * @return
	 * 
	 *         <p>
	 *         create by AT TIME 2016年7月4日 下午3:09:27
	 *         </p>
	 */
	public static java.sql.Timestamp SqlStringToTimestamp2(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			java.util.Date date = sdf.parse(str);
			return new java.sql.Timestamp(date.getTime());
		} catch (ParseException e) {
			throw SlError.SlComm.E0001("数据格式转换异常！");
		}

	}

	/**
	 * 
	 * <p>
	 * Description:生成红包编号：RP+8位日期+10位序列号
	 * </p>
	 * 
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年6月20日 下午4:41:36
	 *         </p>
	 */
	public static String getRpcode() {
		StringBuffer rpcode = new StringBuffer(20);
		rpcode.append("RP").append(DateTools.getSystemDate())
				.append(CommTools.getSequence("rps_rpcode", 10));
		return rpcode.toString();
	}
	
	/**
	 * 
	 * @Title: getRptype 
	 * @Description: 生成红包类型8位：RPLX1-个人；RPLX2-商户；RPLX3-行社
	 * @return
	 * @author wanglei
	 * @date 2016年7月8日 下午2:57:19 
	 * @version V2.3.0
	 */
	/*public static String getRptype(E_SENDOB sendob){
		StringBuffer rpcode = new StringBuffer(8);
		String temp = "";
		if(sendob == E_SENDOB.GR)
			temp="RPLX1";
		else if(sendob == E_SENDOB.SH)
			temp="RPLX2";
		else
			temp="RPLX3";
		
		rpcode.append(temp).append(CommTools.getSequence("rps_rptype", 3));
		return rpcode.toString();
	}*/
	/**
	 * 
	 * <p>
	 * Description: 生成系统流水号：PSS+8位日期+10位序列号
	 * </p>
	 * 
	 * @return
	 * 
	 *         <p>
	 *         create by wl AT TIME 2016年6月20日 下午4:43:32
	 *         </p>
	 */
	public static String getTranno() {
		StringBuffer tranno = new StringBuffer(21);
		tranno.append("PSS").append(DateTools.getSystemDate())
				.append(CommTools.getSequence("pss_tranno", 10));
		return tranno.toString();
	}
	/**
	 * 
	 * @Title: getSerialNo 
	 * @Description: 获取消息流水号 
	 * @return
	 * @author wanglei
	 * @date 2016年7月25日 下午6:56:34 
	 * @version V2.3.0
	 */
	public static String getSerialNo() {
		StringBuffer tranno = new StringBuffer(24);
		tranno.append("RPSMSG").append(DateTools.getSystemDate())
				.append(CommTools.getSequence("Rps_Msg", 10));
		return tranno.toString();
	}
	
	/**
	 * @Author T
	 *         <p>
	 *         <li>2016年7月30日-下午3:17:42</li>
	 *         <li>功能说明：获取8位主机日期 返回Date类型</li>
	 *         </p>
	 * @return 主机时间
	 */
	public static java.sql.Date getSystemSqlDate(){
		java.sql.Date date = new java.sql.Date(new Date().getTime());
		return date;
	}
	
	/**
	 * 
	 * @Title: ScripExecute 
	 * @Description: 执行表达式返回ture false
	 * @return
	 * @author zhouping1
	 * @date 2016年8月04日 上午9:08:34 
	 * @version V2.3.0
	 * @throws ScriptException 
	 */
	
	
	public static String ScripExecute(String exeScript)   {
		Object result = "fasle";
		try {
			/*FelEngine felEng = FelEngine.instance;
			result = felEng.eval(exeScript);*/
		} catch (Exception e) {
			
			throw SlError.SlComm.E0001("执行属性间关系表达式异常,表达式内容["+exeScript+"]请核查表达式是否正确",e);
		}
		if(CommUtil.equals(String.valueOf(result), "0")){
			result="true";
		}else if(CommUtil.equals(String.valueOf(result), "1")){
			result="false";
		}
		return String.valueOf(result);
	}
	
	@Test
	public void test(){
		
		System.out.println(ScripExecute(" 10!=10.00 "));
	//	System.out.println(JSONArray.fromObject("[{\"dttpid\":\"DMB-000001\",\"dttpna\":\"\",\"dictid\":\"221\",\"dictna\":\"\"}]"));
		 //System.out.println(pattern.matcher("2016-05-31 12:22:31").matches());*/
		 //if(!pattern.matcher(input.getMprpvl()).matches()){
		//input.getMprpvl().matches()
	/*	List<String> list = new ArrayList<>();
		list.add("11");
		list.add("2");
		list.add("11");
		System.out.println(list);
		 HashSet hashSet = new HashSet(list);
		 list.clear();
    	 list.addAll(hashSet);
    		System.out.println(list);*/
	//System.out.println(ScripExecute(" 10=='1' "));t703653
		/* Map<String,List<String>> slp_prop_ruleToMap = new Hashtable<String,List<String>>();
		 List<String> list = new ArrayList<>();
		    list.add("11");
			list.add("2");
			list.add("11");
		slp_prop_ruleToMap.put("123", list);
			 System.out.println(slp_prop_ruleToMap);	
			List<String> list1 = new ArrayList<>();
		    list1.add("11");
			list1.add("3");
			list1.add("11");
			slp_prop_ruleToMap.put("1234", list1);
			
			System.out.println(slp_prop_ruleToMap);	
			
		HashSet<String> hashSet = new HashSet();
		hashSet.addAll(slp_prop_ruleToMap.get("123"));
		hashSet.addAll(slp_prop_ruleToMap.get("1234"));
		
	       System.out.println(hashSet);	*/
			
			
		
	}
	 
	 /**
	 * 
	 * @Title: isDateTime 
	 * @Description: 匹配日期加时间
	 * @param listL
	 * @return
	 * @author zhouping
	 * @date 2016年8月15日 上午11:31:07 
	 * @version V2.3.0
	 */
	 public static boolean isDateTime(String datetime){  
	        Pattern p = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1][0-9])|([2][0-4]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");  
	        return p.matcher(datetime).matches();  
	    }  
		
	 /**
		 * 
		 * @Title: isTime 
		 * @Description: 匹配时间
		 * @param listL
		 * @return
		 * @author zhouping
		 * @date 2016年8月15日 上午11:31:07 
		 * @version V2.3.0
		 */
		public static boolean isTime(String time){  
	        Pattern p = Pattern.compile("((((0?[0-9])|([1][0-9])|([2][0-4]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");  
	        return p.matcher(time).matches();  
	    } 
	
	/**
	 * 
	 * @Title: ListInListInter 
	 * @Description: 多个list求交集 
	 * @param listL
	 * @return
	 * @author baifangping
	 * @date 2016年8月15日 上午11:31:07 
	 * @version V2.3.0
	 */
	public static List<String> ListInListInter(List<List<String>> listL) {
		List<String> listOne = new ArrayList<String>();
		listOne = listL.get(0);
		for (List<String> list : listL) {
			listOne.retainAll(list);
		}
		return listOne;
	}
	
	/**
	 * 获取当前时间
	 * @author chengyinghao
	 * @return
	 */
	public static String getCurrentTime() {
		Date date = new Date();
		Format f = new SimpleDateFormat("HH:mm:ss");

		return f.format(date);
	}
	/**
	 * 
	 * @Title: getCurrentDate 
	 * @D escription: 获取当前日期（yyyyMMdd）
	 * @return
	 * @author yuanxiaojie
	 * @date 2016年10月19日 下午7:36:21 
	 * @version V2.3.0
	 */
	public static String getCurrentDate() {
		Date date = new Date();
		Format f = new SimpleDateFormat("yyyyMMdd");
		return f.format(date);
	}
	
	/**
	 * @author chengyinghao
	 * @param date
	 * @param time
	 * @return
	 * 判断生效/失效
	 * @throws ParseException 
	 */
	public static Boolean getTimeCation(Date date,Time time){
		Boolean b = false;
		//获取当前时间
		String currentTime = getCurrentTime();
		//获取当前日期
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String sdate = sdf.format(new Date());
		try{
			Date date1 = sdf.parse(sdate);
			//生效/失效时间
			String efcTime = SlTools.SqlTimeToString2(time);
			//当前日期大于生效/失效日期返回true
			if(date1.after(date)){
				b = true;
				//当前日期等于生效/失效日期且生效时间/失效时间大于等于当前时间返回true
			}else if(date1.equals(date)&&compareTime(currentTime,efcTime)!=1){
				b = true;
			}
		}catch(ParseException e){
			e.printStackTrace();
		}
		return b;
	}
	/**
	 * 
	 * @Title: getSlfPropvr 
	 * @Description: 版本递增方法 
	 * @param bprdvr
	 * @return
	 * @author baifangping
	 * @date 2016年8月23日 下午5:28:37 
	 * @version V2.3.0
	 */
	public static Long getSlfPropvr(Long bprdvr) {
		if (bprdvr == 0) {
			return 1L;
		}
		Long prodvr = bprdvr +1;
		return prodvr;
	}
	
	
	/**
	 * 将字符串转换成HashMap结构
	 * @param str 分割的字符窜
	 * @param bigDelim
	 * @param smallDelim
	 * @return
	 */
	public  static    HashMap<String,Object>    String2Map(String  str,String bigDelim,String  smallDelim){
		
		HashMap<String,Object>  hashMap=new HashMap<String,Object>();
		StringTokenizer   items;
		for(StringTokenizer   entrys=new StringTokenizer(str,bigDelim);entrys.hasMoreTokens();
				hashMap.put(entrys.nextToken(), entrys.hasMoreTokens()?((Object)(entrys.nextToken())):null)){
							items=new StringTokenizer(str,smallDelim);   
		}
		return hashMap;
	}
	
	/**
	 * 属性间直接约束操作符转换
	 * @param str 数字
	 * @author zhouping
	 * @return
	 */
	public static Map<String,String> strToOperator(Map<String,String> number){
		
		Map<String,String> map = new HashMap<String,String>();
		if(CommUtil.equals(E_RULEPP.EQU.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
			if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){	
			map.put("arrayMprpid","'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId", "'"+number.get("arrayAprpId")+"'");
			map.put("operator", "==");
			}else{
				map.put("arrayMprpid",number.get("arrayMprpid"));
				map.put("arrayAprpId", number.get("arrayAprpId"));
				map.put("operator", "==");
			}
		}else if(CommUtil.equals(E_RULEPP.NEQ.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
			if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){	
			map.put("arrayMprpid","'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId", "'"+number.get("arrayAprpId")+"'");
			map.put("operator", "!=");
			}else{
				map.put("arrayMprpid",number.get("arrayMprpid"));
				map.put("arrayAprpId", number.get("arrayAprpId"));
				map.put("operator", "!=");
			}
		}else if(CommUtil.equals(E_RULEPP.LES.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
			map.put("arrayMprpid","'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId","'"+number.get("arrayAprpId")+"'");
			map.put("operator", "<");
		    }else{
		    map.put("arrayMprpid",number.get("arrayMprpid"));
			map.put("arrayAprpId",number.get("arrayAprpId"));
			map.put("operator", "<");
		    }
			
		}else if(CommUtil.equals(E_RULEPP.LEE.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
			map.put("arrayMprpid","'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId","'"+number.get("arrayAprpId")+"'");
			map.put("operator", "<=");
		    }else{
		    map.put("arrayMprpid",number.get("arrayMprpid"));
			map.put("arrayAprpId",number.get("arrayAprpId"));
			map.put("operator", "<=");
		    }
			
		}else if(CommUtil.equals(E_RULEPP.GRA.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
			map.put("arrayMprpid","'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId","'"+number.get("arrayAprpId")+"'");
			map.put("operator", ">");
		    }else{
		    map.put("arrayMprpid",number.get("arrayMprpid"));
			map.put("arrayAprpId",number.get("arrayAprpId"));
			map.put("operator", ">");
		    }
		}else if(CommUtil.equals(E_RULEPP.GRE.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
			map.put("arrayMprpid","'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId","'"+number.get("arrayAprpId")+"'");
			map.put("operator", ">=");
		    }else{
		    map.put("arrayMprpid",number.get("arrayMprpid"));
			map.put("arrayAprpId",number.get("arrayAprpId"));
			map.put("operator", ">=");	
		    }
		}else if(CommUtil.equals(E_RULEPP.MOD.getValue(), number.get("operator"))){
			
			map.put("arrayMprpid",number.get("arrayMprpid"));
			map.put("arrayAprpId",number.get("arrayAprpId"));
			map.put("operator", "%"); 
		}		
		return map;
	}
	
	/**
	 * 属性间直接约束操作符转换中文
	 * @param str 数字
	 * @author zhouping
	 * @return
	 */
	public static String strToChinese(String number){
		
		String operator =  null;
		if(CommUtil.equals(E_RULEPP.EQU.getValue(), number)){
			operator= "等于";
		}else if(CommUtil.equals(E_RULEPP.NEQ.getValue(), number)){
			operator= "不等于";
		}else if(CommUtil.equals(E_RULEPP.LES.getValue(), number)){
			operator= "小于";
		}else if(CommUtil.equals(E_RULEPP.LEE.getValue(), number)){
			operator= "小于等于";
		}else if(CommUtil.equals(E_RULEPP.GRA.getValue(), number)){
			operator= "大于";
		}else if(CommUtil.equals(E_RULEPP.GRE.getValue(), number)){
			operator= "大于等于";
		}else if(CommUtil.equals(E_RULEPP.MOD.getValue(), number)){
			operator= "不能整除";
		}else if (CommUtil.equals(E_RULEPP.CON.getValue(), number)) {
			operator="包含";
		}else if (CommUtil.equals(E_RULEPP.EXC.getValue(), number)) {
			operator="排斥";
		}		
		return operator;
	}
	
	
	/**
	 * 属性间关系条件约束操作符转换中文
	 * @param str 数字
	 * @author zhouping
	 * @return
	 */
     public static String strToChinese1(String number){
		
		String operator =  null;
		if(CommUtil.equals(E_PROPOP.NNU.getValue(), number)){
			operator= "非空";
		}else if(CommUtil.equals(E_PROPOP.NUL.getValue(), number)){
			operator= "空";
		}else if(CommUtil.equals(E_PROPOP.EQU.getValue(), number)){
			operator= "等于";
		}else if(CommUtil.equals(E_PROPOP.NEQ.getValue(), number)){
			operator= "不等于";
		}else if(CommUtil.equals(E_PROPOP.LES.getValue(), number)){
			operator= "小于";
		}else if(CommUtil.equals(E_PROPOP.LEE.getValue(), number)){
			operator= "小于等于";
		}else if(CommUtil.equals(E_PROPOP.GRA.getValue(), number)){
			operator= "大于";
		}else if(CommUtil.equals(E_PROPOP.GRE.getValue(), number)){
			operator= "大于等于";
		}else if(CommUtil.equals(E_PROPOP.MOD.getValue(), number)){
			operator= "整除";
		}else if(CommUtil.equals(E_PROPOP.COM.getValue(), number)){
			operator= "包含";
		}else if(CommUtil.equals(E_PROPOP.EXC.getValue(), number)){
			operator= "排斥";
		}
			
		return operator;
	}
	
	/**
	 * 属性间关系条件约束操作符转换
	 * @param str 数字
	 * @author zhouping
	 * @return
	 */
     public static Map<String,String> strToOperatorCon(Map<String,String> number){
		Map<String,String> map = new HashMap<String,String>();
		
		if(CommUtil.equals(E_PROPOP.NNU.getValue(), number.get("operator"))){
			map.put("mprpvl", "'"+number.get("mprpvl")+"'");
			map.put("arrayMprpid","'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId","'"+number.get("arrayAprpId")+"'");
			map.put("aprpvl", "'"+number.get("aprpvl")+"'");
			map.put("operator", "!=");
		}else if(CommUtil.equals(E_PROPOP.NUL.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
			map.put("mprpvl", "'"+number.get("mprpvl")+"'");
			map.put("arrayMprpid","'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId","'"+number.get("arrayAprpId")+"'");
			map.put("aprpvl", "'"+number.get("aprpvl")+"'");
			map.put("operator", "==");
		    }else{
		    map.put("mprpvl", number.get("mprpvl"));
			map.put("arrayMprpid",number.get("arrayMprpid"));
			map.put("arrayAprpId",number.get("arrayAprpId"));
			map.put("aprpvl", number.get("aprpvl"));
			map.put("operator", "==");	
		    }
		}else if(CommUtil.equals(E_PROPOP.NEQ.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
			map.put("mprpvl", "'"+number.get("mprpvl")+"'");
			map.put("arrayMprpid", "'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId", "'"+number.get("arrayAprpId")+"'");
			map.put("aprpvl", "'"+number.get("aprpvl")+"'");
			map.put("operator", "!=");
		    }else{
		    map.put("mprpvl", number.get("mprpvl"));
			map.put("arrayMprpid", number.get("arrayMprpid"));
			map.put("arrayAprpId", number.get("arrayAprpId"));
			map.put("aprpvl", number.get("aprpvl"));
			map.put("operator", "!=");
			}
		}else if(CommUtil.equals(E_PROPOP.EQU.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
			map.put("mprpvl", "'"+number.get("mprpvl")+"'");
			map.put("arrayMprpid", "'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId", "'"+number.get("arrayAprpId")+"'");
			map.put("aprpvl", "'"+number.get("aprpvl")+"'");
			map.put("operator", "==");
		    }else{
		    map.put("mprpvl", number.get("mprpvl"));
			map.put("arrayMprpid", number.get("arrayMprpid"));
			map.put("arrayAprpId", number.get("arrayAprpId"));
			map.put("aprpvl", number.get("aprpvl"));
			map.put("operator", "==");
		    }
		}else if(CommUtil.equals(E_PROPOP.LES.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
		    map.put("mprpvl", "'"+number.get("mprpvl")+"'");
			map.put("arrayMprpid", "'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId", "'"+number.get("arrayAprpId")+"'");
			map.put("aprpvl", "'"+number.get("aprpvl")+"'");
			map.put("operator", "<");
		    }else{
		    map.put("mprpvl", number.get("mprpvl"));
			map.put("arrayMprpid", number.get("arrayMprpid"));
			map.put("arrayAprpId", number.get("arrayAprpId"));
			map.put("aprpvl", number.get("aprpvl"));
			map.put("operator", "<");	
		    }
		}else if(CommUtil.equals(E_PROPOP.LEE.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
		    map.put("mprpvl", "'"+number.get("mprpvl")+"'");
			map.put("arrayMprpid", "'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId", "'"+number.get("arrayAprpId")+"'");
			map.put("aprpvl", "'"+number.get("aprpvl")+"'");
			map.put("operator", "<=");
		    }else{
		    map.put("mprpvl", number.get("mprpvl"));
			map.put("arrayMprpid", number.get("arrayMprpid"));
			map.put("arrayAprpId", number.get("arrayAprpId"));
			map.put("aprpvl", number.get("aprpvl"));
			map.put("operator", "<=");
		    }
		}else if(CommUtil.equals(E_PROPOP.GRA.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
		    map.put("mprpvl", "'"+number.get("mprpvl")+"'");
			map.put("arrayMprpid", "'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId", "'"+number.get("arrayAprpId")+"'");
			map.put("aprpvl", "'"+number.get("aprpvl")+"'");
			map.put("operator", ">");
		    }else{
		    map.put("mprpvl", number.get("mprpvl"));
			map.put("arrayMprpid", number.get("arrayMprpid"));
			map.put("arrayAprpId", number.get("arrayAprpId"));
			map.put("aprpvl",number.get("aprpvl"));
			map.put("operator", ">");
		    }
		}else if(CommUtil.equals(E_PROPOP.GRE.getValue(), number.get("operator"))){
			String arrayMprpid = number.get("arrayMprpid");
			String arrayAprpId = number.get("arrayAprpId");
			String	aprptp = number.get("aprptp");
			String mprptp = number.get("mprptp");
		    if(isTime(arrayMprpid) || isDateTime(arrayAprpId) || CommUtil.equals(aprptp, E_PRVLTP.STR.getValue()) || CommUtil.equals(mprptp, E_PRVLTP.STR.getValue())){
		    map.put("mprpvl", "'"+number.get("mprpvl")+"'");
			map.put("arrayMprpid", "'"+number.get("arrayMprpid")+"'");
			map.put("arrayAprpId", "'"+number.get("arrayAprpId")+"'");
			map.put("aprpvl", "'"+number.get("aprpvl")+"'");
			map.put("operator", ">=");
		    }else{
		    map.put("mprpvl",number.get("mprpvl"));
			map.put("arrayMprpid", number.get("arrayMprpid"));
			map.put("arrayAprpId", number.get("arrayAprpId"));
			map.put("aprpvl",number.get("aprpvl"));
			map.put("operator", ">=");
		    }
		}else if(CommUtil.equals(E_PROPOP.MOD.getValue(), number.get("operator"))){
			map.put("mprpvl", number.get("mprpvl"));
			map.put("arrayMprpid", number.get("arrayMprpid"));
			map.put("arrayAprpId", number.get("arrayAprpId"));
			map.put("aprpvl", number.get("aprpvl"));
			map.put("operator", "%");
		}
		return map;
	}
     
     
     
     /**
 	 * 包含前者包含后者
 	 * @param src 设置的列表值，src1 需要检查的列表值
 	 * @author zhouping1
 	 * @return true 为包含 false 不包含
 	 */
     
     public static boolean ContainToexclude(String src,String src1){
    	 
    	 Map<String,String> map = new HashMap<String,String>();
    	 JSONObject	 jsonObjSplit = new JSONObject().fromObject(src);
    	 JSONArray	 aprpvlAttay =  null;
    	 JSONArray   mprpvlAttay = null;
    	 String a = JsonUtil.format(jsonObjSplit);
    	 Map<String,Object> oo = JsonUtil.parse(a);
    	 mprpvlAttay =  JSONArray.fromObject(src1);
    	 
    	 if(CommUtil.isNull(oo.get("aprpvl"))){
    		 aprpvlAttay = jsonObjSplit.getJSONArray("mprpvl");
    	 }else{
    		 aprpvlAttay = jsonObjSplit.getJSONArray("aprpvl");
    	 }
    	 
    	 if (CommUtil.isNull(aprpvlAttay)){
    		 throw SlError.SlComm.E0001("主属性值或副属性值为空或不是json数据格式,包含,排斥检查失败!");
    	 }
    	 
    	 for (Object object : aprpvlAttay) {
    		 Map<String, Object>   map1 = 	JsonUtil.parse(object.toString());
    		 map.put(map1.get("dictid").toString(), map1.get("dictid").toString());
		 }
    	 
    	 //循环检查列表值是否在设置的范围之内
    	 for (Object object : mprpvlAttay) {
    		 Map<String, Object>   map2 = 	JsonUtil.parse(object.toString());
    		 boolean isBoolean = map.containsKey(map2.get("dictid"));
    		 if(isBoolean==false){
    			 return false; 
    		 }
		}
    	 return true;
     }
     
     
     /**
  	 * 包含检查  ：后者包含前者
  	 * @param src 需要检查的列表值，src1 设置的列表值
  	 * @author zhouping1
  	 * @return true 为包含 false 不包含
  	 */
      
      public static boolean propRuleCheckToCom(String src,String src1){
     	 
     	 Map<String,String> map = new HashMap<String,String>();
     	 JSONObject	 jsonObjSplit = new JSONObject().fromObject(src);
     	 JSONArray	 aprpvlAttay =  null;
     	 JSONArray   mprpvlAttay = null;
     	 String a = JsonUtil.format(jsonObjSplit);
     	 Map<String,Object> oo = JsonUtil.parse(a);
     	 mprpvlAttay =  JSONArray.fromObject(src1);
     	 
     	 if(CommUtil.isNull(oo.get("aprpvl"))){
     		 aprpvlAttay = jsonObjSplit.getJSONArray("mprpvl");
     	 }else{
     		 aprpvlAttay = jsonObjSplit.getJSONArray("aprpvl");
     	 }
     	 
     	 if (CommUtil.isNull(aprpvlAttay)){
     		 throw SlError.SlComm.E0001("主属性值或副属性值为空或不是json数据格式,包含,排斥检查失败!");
     	 }
     	 
     	 for (Object object : mprpvlAttay) {
     		 Map<String, Object>   map1 = 	JsonUtil.parse(object.toString());
     		 map.put(map1.get("dictid").toString(), map1.get("dictid").toString());
 		 }
     	 
     	 
	     for (Object object : aprpvlAttay) {
	    	 
	    	 Map<String, Object>   map2 = 	JsonUtil.parse(object.toString());
     		 boolean isBoolean = map.containsKey(map2.get("dictid"));
     		 if(isBoolean==false){
     			 return false; 
     		 }	
		 }
     	 return true;
      }
      
      /**
    	 * 排斥	检查
    	 * @param src 需要检查的列表值，src1 设置的列表值
    	 * @author zhouping1
    	 * @return true 为排斥 false 不排斥
    	 */
        
        public static boolean propRuleCheckToExc(String src,String src1){
       	 
       	 Map<String,String> map = new HashMap<String,String>();
       	 JSONObject	 jsonObjSplit = new JSONObject().fromObject(src);
       	 JSONArray	 aprpvlAttay =  null;
       	 JSONArray   mprpvlAttay = null;
       	 String a = JsonUtil.format(jsonObjSplit);
       	 Map<String,Object> oo = JsonUtil.parse(a);
       	 mprpvlAttay =  JSONArray.fromObject(src1);
       	 
       	 if(CommUtil.isNull(oo.get("aprpvl"))){
       		 aprpvlAttay = jsonObjSplit.getJSONArray("mprpvl");
       	 }else{
       		 aprpvlAttay = jsonObjSplit.getJSONArray("aprpvl");
       	 }
       	 
       	 if (CommUtil.isNull(aprpvlAttay)){
       		 throw SlError.SlComm.E0001("主属性值或副属性值为空或不是json数据格式,包含,排斥检查失败!");
       	 }
       	 
       	 for (Object object : mprpvlAttay) {
       		 Map<String, Object>   map1 = 	JsonUtil.parse(object.toString());
       		 map.put(map1.get("dictid").toString(), map1.get("dictid").toString());
   		 }
       	 
       	 
  	     for (Object object : aprpvlAttay) {
  	    	 
  	    	 Map<String, Object>   map2 = 	JsonUtil.parse(object.toString());
       		 boolean isBoolean = map.containsKey(map2.get("dictid"));
       		 if(isBoolean==true){
       			 return false; 
       		 }	
  		 }
       	 return true;
        }
      
     /**
   	 *  
   	 * @param src 设置的列表值，src1 需要检查的列表值  数据格式为map key value
   	 * @author zhouping1
   	 * @return true 为包含 false 不包含
   	 */ 
     
     public static boolean ContainToexclude2(String src,String src1){ 
    	 
    	 Map<String,String> map = new HashMap<String,String>();
    	 JSONObject	 jsonObjSplit = new JSONObject().fromObject(src);
    	 JSONArray	 aprpvlAttay =  null;
    	 String a = JsonUtil.format(jsonObjSplit);
    	 Map<String,Object> oo = JsonUtil.parse(a);
    	 Map<String,Object>  srcMap= JsonUtil.parse(src1);
    	 if(CommUtil.isNull(oo.get("aprpvl"))){
    		 aprpvlAttay = jsonObjSplit.getJSONArray("mprpvl");
    	 }else{
    		 aprpvlAttay = jsonObjSplit.getJSONArray("aprpvl");
    	 }
    	 
    	 if (CommUtil.isNull(aprpvlAttay)){
    		 throw SlError.SlComm.E0001("主属性值或副属性值为空或不是json数据格式,包含,排斥检查失败!");
    	 }
    	 
    	 for (Object object : aprpvlAttay) {
    		 Map<String, Object>   map1 = 	JsonUtil.parse(object.toString());
    		 map.put(map1.get("dictid").toString(), map1.get("dictid").toString());
		 }
    	 boolean i =   map.containsKey(srcMap.get("dictid"));
 		if(i==false){
 			return false;	
 		}
    	 return true;
     }
     
     /**
  	 *  
  	 * @param src 设置的列表值，src1 需要检查的列表值
  	 * @author zhouping1
  	 * @return true 为包含 false 不包含
  	 */
     
     public static boolean ContainToexclude1(String src,String src1){
     	
    	 JSONObject	 jsonObjSplitsrc = new JSONObject().fromObject(src1);
    	 Map<String,String> map = new HashMap<String,String>();
    	 JSONArray	 aprpvlAttay =  JSONArray.fromObject(src);
    	 
    	 for (Object object : aprpvlAttay) {
    		 Map<String, Object>   map1 = 	JsonUtil.parse(object.toString());
    		 map.put(map1.get("dictid").toString(), map1.get("dictid").toString());
		}
    	 Map<String,String> map1 = jsonObjSplitsrc;
    	 return CommUtil.equals(map.get(map1.get("dictid")), map1.get("dictid"));
     }
     
     /**
  	 *  
  	 * @param list转json数组
  	 * @author zhouping1
  	 * @return true 为包含 false 不包含
  	 */
     
     public static List<String> JsonToList(String arrayJson){
    	 JSONArray	 aprpvlAttay =  JSONArray.fromObject(arrayJson);
    	 List<String> list = new ArrayList<>();
    	 for (Object object : aprpvlAttay) {
    		 Map<String, Object> map = JsonUtil.parse(object.toString());
    		 list.add(map.get("dictid").toString());
		}
    	 return list;
     }
     
 	/**
 	 * 
 	 * @Title: rangeInspect 
 	 * @Description: 属性范围检查 
 	 * @param inInitialVa 需要检查起始值  inEndVa 需要检查终止值  InitialVa 源起始值  a 原起始值 1 包含 2不包含  EndVa 原终止值  b 原终止值 1 包含 2不包含  c 1整形 2 浮点 3 时间 4 日期
 	 * @return
 	 * @author zhouping1
 	 * @date 2016年11月14日 下午2:33:40 
 	 * @version V0.0.1
 	 */
      
     public static boolean rangeInspect(String inInitialVa,String inEndVa,String InitialVa,int a,String  EndVa,int b,int c){
    	
    	 //检查int类型值范围校验
    	 if(c==1){
    		 //检查的值
    		 int instartInt = Integer.parseInt(inInitialVa);
    		 int inendInt = Integer.parseInt(inEndVa);
    		 //原始值
    		 int startInt = Integer.parseInt(InitialVa);
    		 int endInt = Integer.parseInt(EndVa);
    		 
    		 //起始值表达式
    		 String staOperator = null;
    		 String endOperator = null;
    		 if(a==1){
    			// staOperator= strToOperatorCon("8"); 
    		 }else{
    			// staOperator= strToOperatorCon("7"); 
    		 }
    		 if(b==1){
    			 //endOperator= strToOperatorCon("6"); 
    		 }else{
    			 //endOperator= strToOperatorCon("5"); 
    		 }
    		 
    		int Intotal=  instartInt + inendInt;
    		
    		int total =  startInt + endInt; 
    		
    		String ExpressionReturn1 = null;
            //检查二个值是否在原始值的累加范围之内
    		if(a==2 || b ==2){
    			
    			// ExpressionReturn1 = 	ScripExecute(Intotal+""+strToOperatorCon("5")+""+total);
    			
    		}else{
    			// ExpressionReturn1 = 	ScripExecute(Intotal+""+strToOperatorCon("6")+""+total);
    		
    		}
    		 //表达式执行不通过返回false
   		   if(CommUtil.equals(ExpressionReturn1,"false")){
   			 return false;
   		    }
   		   
    		 String staExpression1 = instartInt + staOperator + startInt;
    		 
    		 String staExpression2 = instartInt + endOperator + endInt;
    		 
    		 String endExpression1 = inendInt + staOperator + startInt;
    		 
    		 String endExpression2 = inendInt + endOperator + endInt;
    		 
    		 String ExpressionReturn =  ScripExecute(staExpression1 + "&&" + staExpression2 +"&&"+ endExpression1 + "&&" + endExpression2);
    		 //表达式执行不通过返回false
    		 if(CommUtil.equals(ExpressionReturn,"false")){
    			 return false;
    		 }
    	 }
    	
    	 return true;
     }
     
     
   /*  public static void main(String[] args) {
    	
    	 Map<Integer,Integer> y = new HashMap();
    	 for (int i = 0; i <= 10000; i++) {
			y.put(i, i+100);
		}
    	 System.out.println(y);
    	 long startTime =  System.currentTimeMillis();
    	 for (Entry<Integer, Integer> entry : y.entrySet()) {
    		 boolean a =	 rangeInspect("1","90",entry.getKey().toString(),2,entry.getValue().toString(),1,1);
    		 System.out.println(a);
		}
    	 
    	    long endTime =  System.currentTimeMillis();
    		System.out.println("代码执行时间:"+(endTime - startTime)+"ms");
    
	}*/
 	/**
 	 * 
 	 * @Title: stringToF 
 	 * @Description: ID编号带|符号 获取后面字符串
 	 * @param str
 	 * @return
 	 * @author zhouping
 	 * @date 2016年9月9日 下午2:33:40 
 	 * @version V2.3.0
 	 */
    public static String splitsrc(String args ){
    	 String prodid = "";
    	 String[] string =  args.split("|");
    	 for (int i = 0; i < string.length; i++) {
			String string2 = string[i];
			if(CommUtil.equals(string2, "|")){
				int srgLength = string.length;
				prodid=args.substring(i, srgLength-1);
			} else if (CommUtil.isNull(prodid)) {
				prodid=args;
			}
		}
      return prodid;
    }
     
    
    /**
 	 * 
 	 * @Title: stringToF 
 	 * @Description: ID编号带_符号 获取后面字符串
 	 * @param str
 	 * @return
 	 * @author zhouping
 	 * @date 2016年9月9日 下午2:33:40 
 	 * @version V2.3.0
 	 */
    public static String splitsrc1(String args ){
    	 String prodid = "";
    	 String[] string =  args.split("_");
    	 for (int i = 0; i < string.length; i++) {
			String string2 = string[i];
			if(CommUtil.equals(string2, "_")){
				int srgLength = string.length;
				prodid=args.substring(i, srgLength-1);
			} else if (CommUtil.isNull(prodid)) {
				prodid=args;
			}
		}
      return prodid;
    }
    
    
    
	/**
	 * 
	 * @Title: stringToF 
	 * @Description: 去除字符串中":" 
	 * @param str
	 * @return
	 * @author baifangping
	 * @date 2016年9月9日 下午2:33:40 
	 * @version V2.3.0
	 */
	public static String stringToF(String str) {
		String s = str.replaceAll(":", "");
		return s;
	}
	
	/**
	 * 
	 * @Title: getTyinno 
	 * @Description: 获取录入编号
	 * @return
	 * @author jiwenbo
	 * @date 2016年9月13日 下午3:47:16 
	 * @version V2.3.0
	 */
	public static String getTyinno(){
		String tyinno = "";// 15位录入编号
		String corpno = CommTools.prcRunEnvs().getCorpno();// 机构号
		String trandt = CommTools.prcRunEnvs().getTrandt();//当前日期
		String liusno = "tyinno_seq" + trandt;
		String sequence = "";// 顺序号
		sequence = CommTools.getSequence(liusno, 4);
		tyinno = new StringBuilder().append(corpno).append(trandt)
				.append(sequence).toString();
		
		return tyinno;
	}
	/**
	 * 
	 * @Title: getParams
	 * @Description: string解析为map
	 * @param param
	 * @param relationship
	 * @param seperator
	 * @return
	 * @author baojianfeng
	 * @date 2016年8月27日 下午2:14:31
	 * @version V2.3.0
	 */
	public static Map<String, Object> getParams(String param,
			String relationship, String seperator) {
		// 创建空的实例供数据存入
		Map<String, Object> map = new HashMap<String, Object>();
		// 若为空则直接返回
		if ("".equals(param) || param == null) {
			return map;
		}
		// seperator分隔符对str进行拆分
		String[] params = param.split(seperator);
		// 循环将分割出来的多组数据插入
		for (int i = 0; i < params.length; i++) {
			// 每组数据之间用relationship进行拆分
			String[] p = params[i].split(relationship);
			// 插入
			if (p.length == 2) {
				map.put(p[0], p[1]);
			}
		}
		// 返回map
		return map;
	}
	/**
	 * 
	 * @Title: ToCompareTwoList 
	 * @Description: 比较两个list列表，相等返回1，不相等0 
	 * @param value
	 * @param valList
	 * @return
	 * @author baifangping
	 * @date 2016年10月18日 上午8:58:14 
	 * @version V2.3.0
	 */
	public static int ToCompareTwoList(List<String> value, List<String> valList) {
		if(CommUtil.isNull(value) ||CommUtil.isNull(valList)){
			return 0;
		}
		if(value.size() != valList.size()){
			return 0;
		}
		int i = value.size();
		try {
			value.retainAll(valList);
			if (i ==value.size()) {
				return 1;
			}
		} catch (Exception e) {
			throw SlError.SlComm.E0001("两list求交集，出现异常！");
		}
		return 0;
	}
	/**
	 * 
	 * @Title: getNextDate 
	 * @Description: 获取到自定义的日期，date表示当前时间，days表示与date相隔的天数,days为负数表示减，days为正数表示加
	 * @param date
	 * @param days
	 * @return
	 * @author yuanxiaojie
	 * @date 2016年10月19日 下午7:27:38 
	 * @version V2.3.0
	 */
	public static String getDefineDate(String date , int days){
		chkIsDate(date);
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Date dd;
		try {
			dd = df.parse(date);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dd);
			calendar.add(Calendar.DAY_OF_MONTH, days);//days为负数表示减，days为正数表示加
			date=df.format(calendar.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
		
	}
	
	/**
	 * 
	 * @Title: chkIsDate 
	 * @Description: 检查当前字符变量是否日期
	 * @param sDate
	 * @return
	 * @author yuanxiaojie
	 * @date 2016年10月19日 下午7:30:52 
	 * @version V2.3.0
	 */
	public static boolean chkIsDate(String sDate) {
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
	 * 
	 * @Title: covStringToDate 
	 * @Description: 将字符串日期转为日期类型
	 * @param sDate
	 * @return
	 * @author yuanxiaojie
	 * @date 2016年10月19日 下午7:30:32 
	 * @version V2.3.0
	 */
	public static Date covStringToDate(String sDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date d = new Date();
		try {
			d = dateFormat.parse(sDate);
		}
		catch (ParseException e) {
			throw Sys.E0003(e);
		}
		return d;
	}
	
	/**
     * 
     * @Title: getLiusNo 
     * @Description: 生成9位交易流水， 流水号规则为W（1位）+柜员号（7位）+序号（4位）
     * @param crdate
     * @param tranus
     * @return
     * @author yuanxiaojie
     * @date 2016年10月18日 下午3:15:45 
     * @version V2.3.0
     */
    public static String getLiusNo(Date crdate , String tranus){
    	String liuscd = "";
    	liuscd +=  "W";
    	liuscd += tranus;
    	String liusno ="sls_gyls_seq_" + DateTools.covDateToString(crdate);
    	String sequenceNo = CommTools.getSequence(liusno, 4);
    	liuscd += sequenceNo;
    	return liuscd;
    }
    /**
	 * 获取制定格式的当前时间
	 * @author wenggl
	 * @return
	 */
	public static String getCurrentTimeWithFormat(String format) {
		Date date = new Date();
		Format f = new SimpleDateFormat(format);
		return f.format(date);
	}
	/**
	 * 
	 * @Title: judgeListWheOrNotSameValue 
	 * @Description:判断list中是否有相同的值 
	 * @param list
	 * @return
	 * @author baifangping
	 * @date 2016年11月7日 上午10:47:32 
	 * @version V2.3.0
	 */
	public static int judgeListWheOrNotSameValue(List<String> list) {
		Map<String, String> map = new HashMap<String , String >();
		for (int i = 0; i < list.size() ; i++) {
			String key = list.get(i);
			String old = map.get(key);
			if (old!=null) {
				map.put(key, old+","+(i+1));
			}else {
				map.put(key, ""+(i+1));
			}
		}
		Iterator<String> it  = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = map.get(key);
			if (value.indexOf(",")!= -1) {
				System.out.print(key+"重复行："+value);
				return 0;
			}
		}
 		return  1;
	}
	
	/**
	 * 
	 * @Title: getCurrentDateOfAny 
	 * @Description:获取某8位日期的当前或之后日期（i可为负数）
	 * @param date ，i
	 * @return String
	 * @author baojianfeng
	 * @date 2016年11月8日 上午10:01:32 
	 * @version V2.3.0
	 */
	public static String getCurrentDateOfAny(String date,int i){
		
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Date dd = null;
		try {
			dd = df.parse(date);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(dd);
			calendar.add(Calendar.DAY_OF_MONTH, i);
			date=df.format(calendar.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
		
	}
	
	/**
	 * 
	 * @Title: getLastDate
	 * @Description:获取8位系统日期的前一天日期
	 * @param 
	 * @return String
	 * @author baojianfeng
	 * @date 2016年11月8日 上午10:01:32 
	 * @version V2.3.0
	 */
	public static String getLastDate(){
		
		DateFormat df = new SimpleDateFormat("yyyyMMdd");

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		
		String date=df.format(calendar.getTime());
		return date;
		
	}
	
	/**
	 * 
	 * @Title: getConDateTime
	 * @Description:组装6位时间和9位日期到23位sql时间
	 * @param String
	 * @return timestamp
	 * @author baojianfeng
	 * @date 2016年11月8日 上午10:01:32 
	 * @version V2.3.0
	 */
	public static Timestamp getConDateTime(String time , String date){
		Timestamp dateTime = null;
		try {
			//组装shtmpl日期时间字段
			String newdt = date.substring(0,4)+"-"+date.substring(4,6)+"-"+date.substring(6,date.length());
			String newtm = time.substring(0,2)+":"+time.substring(2,4)+":"+time.substring(4,time.length());
			String datetm = newdt + " " + newtm;
			dateTime = SlTools.SqlStringToTimestamp(datetm);
		} catch (Exception e) {
			throw OdError.Order.E9999("组装dateTime日期时间字段失败");
		}
		return dateTime;
	}
	
	/**
	 * 
	 * @Title: getNull2String 
	 * @Description: 空值转换成空字符串的方法
	 * @param obj
	 * @return
	 * @author yuanxiaojie
	 * @date 2016年12月3日 下午9:48:32 
	 * @version V2.3.0
	 */
	public static String parseNull2Str(Object obj){
		if(null==obj){
			return "";
		}
		String newString = String.valueOf(obj);
		return newString;
	}
	
	/**
	 * @Title: parsStr2Long 
	 * @Description: 将String对象转换成Long对象
	 * @param obj
	 * @return
	 * @author yuanxiaojie
	 * @date 2016年12月3日 下午10:22:28 
	 * @version V2.3.0
	 */
	public static long parsStr2Long(Object obj){
		if(null==obj){
			return 0;
		}
		long newString = Long.parseLong(obj.toString());
		return newString;
	}
	
	/**
	 * @Title: exceptionSubstring 
	 * @Description: 截取Exception两个中括号内的内容
	 * @param String
	 * @return
	 * @author baojianfeng
	 * @date 2016年12月3日 下午10:22:28 
	 * @version V2.3.0
	 */
	public  static String exceptionSubstring(String eString){
		//截取时从“：”出现位置开始截取，如果不存在“：”则从“-1”开始截取
		String str = eString.substring(eString.indexOf(":")+1,eString.length());
		return str;
	}
	
	/**
	 * @Title: exceptionSub 
	 * @Description: 截取Exception两个中括号内的内容
	 * @param String
	 * @return
	 * @author baojianfeng
	 * @date 2016年12月3日 下午10:22:28 
	 * @version V2.3.0
	 */
	public  static String exceptionSub(String eString){
		//截取时从“：”出现位置开始截取，如果不存在“：”则从“-1”开始截取
		String str = eString.substring(eString.lastIndexOf("["),eString.length());
		return str;
	}
	
	/**
	 * @Title: subTrantmTotime 
	 * @Description: 截取日期时间类型(1997-12-12 13:05:01.0)字段的6位时间返回
	 * @param String
	 * @return
	 * @author baojianfeng
	 * @date 2016年12月28日 下午10:22:28 
	 * @version V1.1.0
	 */
	public  static String subTrantmTotime(String eString){
		String str;
		try {
			str = eString.substring(eString.length()-10,eString.length()-2);
			str = str.replace(":", "");
		} catch (Exception e) {
			throw OdError.Order.E9999("截取日期时间类型字段出错");
		}
		return str;
	}
	/**
	 * @Title: subTrantmTotime 
	 * @Description: 截取日期时间类型(1997-12-12 13:05:01.0)字段的6位时间返回
	 * @param String
	 * @return
	 * @author baojianfeng
	 * @date 2016年12月28日 下午10:22:28 
	 * @version V1.1.0
	 */
	public  static String subTrantmToDate(String eString){
		String str;
		try {
			str = eString.substring(0,10);
			str = str.replace("-", "");
		} catch (Exception e) {
			throw OdError.Order.E9999("截取日期时间类型字段出错");
		}
		return str;
	}
	/**
	 * 
	 * @Title: getBrchJiBie 
	 * @Description: 截取机构号前三位获取机构级别 
	 * @param tranbr
	 * @return 1表示省联社，0表示其它
	 * @author baifangping
	 * @date 2016年12月14日 下午8:35:58 
	 * @version V2.3.0
	 */
	public static String getBrchJiBie(String tranbr) {
		try {
			if (CommUtil.equals(tranbr.substring(0, 3), "999")) {
				return "1";
			}
		} catch (Exception e) {
			throw SlError.SlComm.E0001("获取机构级别失败！");
		}
		return "0";
	}
	/**
	 * 
	 * @Title: arrayToMap 
	 * @Description:  将数组拼接为map
	 * @param str
	 * @param strA
	 * @return
	 * @date 2017年1月5日 下午6:15:29 
	 * @version V2.3.0
	 */
	public static String arrayToMap(String str, String strA) {
		return  "{" + str+":" + strA + "}";
	}
	/**
	 * 
	 * @Title: listToMap 
	 * @Description: 将list转化为Map 
	 * @param str1
	 * @param str2
	 * @param strL
	 * @return
	 * @date 2017年1月5日 下午6:19:18 
	 * @version V2.3.0
	 */
	public static String listToMap(String str1, String str2, String strL) {
		String mapToString  = "";
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(str1, strL);
		
		mapToString = "{" +str2+":["
				+ JsonUtil.format(map) + "]}";
		return mapToString;
	}
	/**
	 * 
	 * @Title: fieldToArray 
	 * @Description: 将{key:value} 转变成 [{key:value}]
	 * @param str
	 * @param strF
	 * @return
	 * @author baifangping
	 * @date 2017年1月8日 下午2:10:08 
	 * @version V2.3.0
	 */
	public static String fieldToArray(String str, String strF) {
		String arrayToString = "";
		Map<String, String> map = new HashMap<String, String>();
		map.put(str, strF);
		arrayToString = "[" + JsonUtil.format(map) + "]";
		return arrayToString;
	}
	/**
	 * 
	 * @Title: prvltpvlToMap 
	 * @Description: 值类型键与值类型名称映射
	 * @return
	 * @author baifangping
	 * @date 2017年1月12日 上午11:01:15 
	 * @version V2.3.0
	 */
	public static Map<String, String> prvltpvlToMap() {
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("1", "字符串");
		map.put("2", "整数");
		map.put("3", "金额");
		map.put("4", "小数");
		map.put("5", "百分比");
		map.put("6", "日期");
		map.put("7", "时间");
		map.put("8", "日期时间");
		map.put("9", "列表");
		map.put("10", "按钮");
		return map;
	}
	/**
	 * 
	 * @Title: formatStrgToDecl 
	 * @Description: 浮点型属性值格式化
	 * @param string
	 * @param string2
	 * @return
	 * @author baifangping
	 * @date 2017年1月13日 上午10:13:56 
	 * @version V2.3.0
	 */
	public static String formatStrgToDecl(String str, String str1 ) {
		
		String s = "";
		
		if (CommUtil.equals(str, E_PRVLTP.DE2.getValue())
				|| CommUtil.equals(str, E_PRVLTP.PER.getValue())
				|| CommUtil.equals(str, E_PRVLTP.DE5.getValue())) {
			BigDecimal str2 = new BigDecimal(0.0);
			
			try {
				if (CommUtil.isNotNull(str1)) {
					str2 = new BigDecimal(str1);
				}
			} catch (Exception e) {
				throw SlError.SlComm.E0001("字符转BigDecimal类型异常，请确认主属性值类型是否正确！");
			}
			
			if (CommUtil.equals(str, E_PRVLTP.DE2.getValue())||CommUtil.equals(str, E_PRVLTP.PER.getValue())) {
				s = String.format("%.2f",str2);
			}else {
				s = String.format("%.6f",str2);
			}
		}else {
			s = str1 ;
		}
		
		return s;
	}
	/**
	 * 
	 * @Title: strgExcCheck 
	 * @Description: 字符串排斥校验 
	 * @param string
	 * @param string2
	 * @author baifangping
	 * @date 2017年1月16日 上午9:27:43 
	 * @version V2.3.0
	 * @return 
	 */
	public static boolean strgExcCheck(String str, String str2) {
		boolean a  = true ; 
		boolean b = true ;
		
		try {
			a = str.contains(str2);
			b = str2.contains(str);
		} catch (Exception e) {
			throw SlError.SlComm.E0001("字符串排斥校验异常！");
		}
		
		if (!a && !b) {
			return true;
		}else {
			return false;
		}
	}
	/**
	 * 
	 * @Title: strgConCheck 
	 * @Description: 字符串包含校验 
	 * @param string
	 * @param string2
	 * @author baifangping
	 * @date 2017年1月16日 上午9:27:43 
	 * @version V2.3.0
	 * @return 
	 */
	public static boolean strgConCheck(String str, String str2) {
		
		boolean b = true ;
		try {
			b = str2.contains(str);
		} catch (Exception e) {
			throw SlError.SlComm.E0001("字符串包含校验异常！");
		}
		return b;
	}
	/*
	 * 外调异常结果处理
	 * list[0],返回1，失败，返回2，超时。
	 */
	public static List<String> diversionAbnormalResult(Throwable e) {
		
		List<String> list_str = new ArrayList<String>();
		//异常码
		String errorCode = E_FAILTO.FA.getValue();
		//异常信息
		String errorInfo = "";
		
		if (CommUtil.isNotNull(e)) {
			
			try {
				//重新抛出错误
				throw e;
			} catch(BusinessException busEx){
				
				//业务
				errorCode = E_FAILTO.FA.getValue();
				errorInfo = busEx.getMessage();
			}
			catch (RemoteTimeoutException reTimEx) {
				
				//响应超时
				errorCode = E_FAILTO.TO.getValue();
				if (!CommUtil.equals(reTimEx.getCode(), "SP_RP.E022")) {
					//原则不执行下行语句，若执行则是代码漏洞
					throw OdError.Order.E9027("外调异常(代码漏洞，需及时处理)");
				}
				errorInfo = reTimEx.getMessage();
			} catch (RuntimeException runtiEx){
				
				/*
				 * 1、连接错误 2、服务配置错误 3、外调没有提供者
				 * 以上三种错误均包含
				 */
				errorCode = E_FAILTO.FA.getValue();
				errorInfo = runtiEx.getMessage();
			} catch (Throwable e1) {
				
				//其它异常情况
				errorCode = E_FAILTO.FA.getValue();
				errorInfo = "外调异常（系统内部错误）！";
			}
			
		}else {
			//原则不执行下行语句，若执行则是代码漏洞
			throw OdError.Order.E9027("外调异常catch捕获信息e为空！");
		}
		
		//返回列表赋值
		list_str.add(errorCode);
		list_str.add(errorInfo);
		
		return list_str;
	}
	
	/**
	 * 外调异常时分情况进行cry catch
	 */
	public static void tryException(Throwable e) {

		String ercode = E_FAILTYPE.otherException.getValue();
		String errorMsg = "外调异常：未知错误" + e.getMessage();

		if (CommUtil.isNotNull(e)) {

			try {
				// 重新抛出错误
				throw e;
			} catch (BusinessException busEx) {// 外调系统处理抛出异常
				try {
					busEx = (BusinessException) e;
					errorMsg = busEx.getMessage();
					ercode = busEx.getErrId();
				} catch (Exception e2) {
					bizLog.debug(" 外调失败 e = %s", e);
				}
				int index1 = errorMsg.indexOf("]");
				if (index1 >= 0) {
					errorMsg = errorMsg.substring(index1 + 1);
				}

			} catch (RemoteTimeoutException reTimEx) {// 外调系统处理时间过长或处理时间超过本系统设置超时时间
				// 响应超时
				ercode = E_FAILTYPE.responseTimeOut.getValue();
				errorMsg = reTimEx.getCause().getMessage();
			} catch (RuntimeException runtiEx) {// 请求方请求异常 1、连接错误 2、服务配置错误
												// 3、外调没有提供者 等

				/*
				 * 1、连接错误 2、服务配置错误 3、外调没有提供者 以上三种错误均包含
				 */
				ercode = E_FAILTYPE.requestException.getValue();
				errorMsg = runtiEx.getCause().getMessage();
			} catch (Throwable e1) {

				// 其它异常情况
				ercode = E_FAILTYPE.otherException.getValue();
				errorMsg = "外调异常：未知错误" + e1.getMessage();
			}

		}
		// 将错误码和错误信息放在公共运行环境变量中
		CommTools.prcRunEnvs().setErorcd(ercode);
		CommTools.prcRunEnvs().setErortx(errorMsg);

	}
	
	/**
	 * 外调异常时分情况进行cry catch
	 * @return 
	 */
	public static List<String> tryCatchException(Throwable e) {
		List<String> errorList = new ArrayList<String>();
		
		String ercode = E_FAILTY.otherException.getValue();
		String errorMsg = "外调异常：未知错误" + e.getMessage();

		if (CommUtil.isNotNull(e)) {

			try {
				// 重新抛出错误
				throw e;
			} catch (BusinessException busEx) {// 外调系统处理抛出异常
				try {
					busEx = (BusinessException) e;
					errorMsg = "外调返回错误码："+busEx.getErrId()+"错误信息："+busEx.getMessage();
					ercode = E_FAILTY.LttsBusExce.getValue();
				} catch (Exception e2) {
					bizLog.debug(" 外调失败 e = %s", e);
				}

			} catch (RemoteTimeoutException reTimEx) {// 外调系统处理时间过长或处理时间超过本系统设置超时时间
				// 响应超时
				ercode = E_FAILTY.responseTimeOut.getValue();
				errorMsg = reTimEx.toString();
			} catch (RuntimeException runtiEx) {// 请求方请求异常 1、连接错误 2、服务配置错误
												// 3、外调没有提供者 等

				/*
				 * 1、连接错误 2、服务配置错误 3、外调没有提供者 以上三种错误均包含
				 */
				ercode = E_FAILTY.requestException.getValue();
				errorMsg = runtiEx.toString();
			} catch (Throwable e1) {

				// 其它异常情况
				ercode = E_FAILTY.otherException.getValue();
				errorMsg = "外调异常：未知错误" + e1.getMessage();
			}

		}
		// 将错误码和错误信息放在接受变量list中
		errorList.add(ercode);
		errorList.add(errorMsg);
		
		return errorList;
	}

	public static int getPacketID(java.lang.String cordno) {
		int gropid = cordno.hashCode();
		int h = gropid;
		h ^= (h >>> 20) ^ (h >>> 12);
		gropid = h ^ (h >>> 7) ^ (h >>> 4);
		gropid = gropid % 1000;
		if (gropid < 0) {
			gropid = gropid * (-1);
		}
		return gropid;
	}
    /**
     * 
     * <p>Title:getTargetStr 产生一个包含随机数的自定义长度的字符串</p>
     * <p>Description:	</p>
     * @author 董志宇
     * @date   2017年8月24日 
     * @param len 需要产生的字符串的长度
     * @param front 开始的字符串
     * @param seqName 序列名
     * @param complement 结束的字符串
     * @return
     */
    public static String getTargetStr(int len,String front,String seqName,String complement) {
        
        //序列名称为空则直接返回null
        if(CommUtil.isNull(seqName)){
            return null;
        }
        
        StringBuffer tranno = null;
        //如果len未传或者len小于1，则自动走默认拼接方式
        if(CommUtil.isNull(len) || len<1){
            tranno = new StringBuffer();
            
            tranno.append(CommUtil.isNotNull(front)?front:"");
            tranno.append(DateTools.getSystemDate()).append(CommTools.getSequence(seqName,10));
            tranno.append(CommUtil.isNotNull(complement)?complement:"");
            return tranno.toString();
        }else{
            tranno = new StringBuffer(len);
        }
        
        //判断开始字符串是否为空，如果为空则赋值为""
        front = CommUtil.isNull(front)?"":front;
        //判断结尾字符串是否为空，如果为空则赋值为""
        complement = CommUtil.isNull(complement)?"":complement;
        
        //获取开始字符串的长度
        int frontLen = CommUtil.isNotNull(front)?front.length():0;
        //获取结尾字符串的长度
        int complementLen = CommUtil.isNotNull(complement)?complement.length():0;
        
        if(len<(frontLen+complementLen)){//目的字符串总长度小于开始字符串与结尾字符串的长度，则直接拼接开始字符串和结束字符串，然后根据传入的长度进行截位
            tranno.append(CommUtil.isNotNull(front)?front:"").append(CommUtil.isNotNull(complement)?complement:"").substring(0, len);
            return  tranno.toString();
        }if(len==(frontLen+complementLen)){//目的字符串总长度小于开始字符串与结尾字符串的长度，则直接拼接开始字符串和结束字符串
            tranno.append(CommUtil.isNotNull(front)?front:"").append(CommUtil.isNotNull(complement)?complement:"");
            return tranno.toString();
        }else{
            //拼接开始字符串
            tranno.append(CommUtil.isNotNull(front)?front:"");
            
            //判断是否拼接日期
            if((len-frontLen-complementLen)<DateTools.getSystemDate().length()){
                tranno.append(CommTools.getSequence(seqName, len-frontLen-complementLen));
            }else{
                tranno.append(DateTools.getSystemDate()).append(CommTools.getSequence(seqName, len-frontLen-complementLen-DateTools.getSystemDate().length()));
            }
            
            //拼接结尾字符串
            tranno.append(CommUtil.isNotNull(complement)?complement:"");
            
            return tranno.toString();
        }
    }
    
    /*
	 * 生成文件流水号
	 */
	public static String getfilesq() {
		StringBuffer tempbtchno = new StringBuffer(21);
		tempbtchno.append("PSS");
		tempbtchno.append(DateTools.getSystemDate())
				.append(CommTools.getSequence("cmp_filesq", 9));
		String tranno = tempbtchno.toString();

		bizLog.debug("  ######文件交互流水号 = %s", tranno);
		return tranno;

	}
    
	/**
	 * 文件交互获取文件根目录,查询参数表
	 * 
	 */
	public static String getParam(String paramkey){
		KnpGlbl kp = KnpGlblDao.selectOne_odb1(paramkey, "%", "%", "%", false);
		if(CommUtil.isNull(kp)){
			throw SlError.SlComm.E0001("根目录参数未配置");
		}
		return File.separatorChar+kp.getPmval1();
	}
    
	/**
	 * 文件交互获取文件名
	 */
	public static String getFileName(String dataid,String trandt){
		StringBuffer filename = new StringBuffer(100);
		filename.append("PSS_").append(dataid+"_").append(trandt+"_")
				.append(CommTools.getSequence("file_"+dataid, 4));
		return filename.toString();
	}
	/**
	 * 文件上传
	 * 
	 * @param localFileName
	 *            ,本地应用服务器文件名
	 * @param properties
	 *            文件下载需要的属性内容
	 * @return 文件上传返回的信息 注：均为相对路径,可使用带目录文件名.
	 */
	public static FileTransferResult upload(String localFileName, Map<String, Object> properties) {
		if (properties == null)
			properties = new HashMap<String, Object>();
		return getFileTransfer().upload(localFileName, properties);
	}
	
	private static FileTransfer getFileTransfer() {
		return SysUtil.getInstance(FileTransfer.class, AbstractComponent.FileTransfer);
	}
	/**
	 * (非 Javadoc) 
	 * <p>Title: fileInteractionRequest</p> 
	 * <p>Description: 文件交互通知请求(联机使用)</p> 
	 * @param source 源系统
	 * @param target 目标系统
	 * @param dataid 数据类型
	 * @param busseq 业务流水
	 * @param filenm 文件名
	 * @param flpath 文件路径
	 * @param params 业务参数域（json）
	 * @see cn.sunline.ltts.busi.iobus.servicetype.pr.IoCommInfoSvcType#fileInteractionRequest(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 **/
	public static void FileInteractionRequestBatch(String source,  String target,  String dataid,  String busseq,  E_FLJHST status,  String descri,  String filenm,  String flpath,  String params,String acctdt){
		if (CommUtil.isNull(source)) {
			throw SlError.SlComm.E0001("参数source不能为空");
		}
		if (CommUtil.isNull(target)) {
			throw SlError.SlComm.E0001("参数target不能为空");
		}
		if (CommUtil.isNull(dataid)) {
			throw SlError.SlComm.E0001("参数dataid不能为空");
		}
		if (CommUtil.isNull(filenm)) {
			throw SlError.SlComm.E0001("参数filenm不能为空");
		}
		if (CommUtil.isNull(flpath)) {
			throw SlError.SlComm.E0001("参数flpath不能为空");
		}
		if (CommUtil.isNull(acctdt)) {
			throw SlError.SlComm.E0001("参数acctdt不能为空");
		}
		
		//设置虚拟机构和柜员
		SlTools.setRunEnvs(E_SERVTP.NP,  E_SYSTEMTP.PSS);
		//主机地址
		String hostName = SysUtil.getIp();
		CommTools.prcRunEnvs().setHostip(hostName);
		
		FileInteractionRequest(source, target, dataid, busseq, status, descri, filenm, flpath, params, acctdt, "share_file_path");
	}
	
	public static void FileInteractionRequest(String source,  String target,  String dataid,  String busseq,  E_FLJHST status,  String descri,  String filenm,  String flpath,  String params, String acctdt, String pathky){
		//外调对应系统文件交互分装服务
		ApFileTask apFileTask = SysUtil.getInstance(ApFileTask.class);
		ApFileTask.FileInteractiveReqByServer.InputSetter fileInput = SysUtil.getInstance(ApFileTask.FileInteractiveReqByServer.InputSetter.class);
		ApFileTask.FileInteractiveReqByServer.Output fileOutput = SysUtil.getInstance(ApFileTask.FileInteractiveReqByServer.Output.class);
		
		PssFilePlat filePlat = SysUtil.getInstance(PssFilePlat.class);   //文件集合符合类型
		List<PssFilePlat> fileList = new ArrayList<PssFilePlat>();  //文件list
		Options<ApDefineType.PssFilePlat> op = SysUtil.getInstance(Options.class);  //option对象


		String pathpr = SlTools.getParam("share_file_path");//交互根路径
		bizLog.debug(" 文件交互公共方法，交互根路径 = %s  ", pathpr);
		
		//文件MD5加密处理
		File newfile = new File(pathpr+flpath+filenm);
				
		bizLog.debug("===========newfile: " + newfile + "=============");
		
		String filemd = null;
		try{
			filemd = MD5EncryptUtil.getFileMD5String(newfile);
			bizLog.debug("===========filemd: " + filemd + "=============");
		}catch(Exception e){
			throw SlError.SlComm.E0001("文件MD5校验异常");
		}
		filePlat.setFilemd(filemd);
		filePlat.setFilenm(filenm);
		filePlat.setFlpath(flpath);
		filePlat.setParams(params);
		
		fileList.add(filePlat);
		op.addAll(fileList);
		
		fileInput.setAcctdt(acctdt);//会计日期
		fileInput.setBusseq(busseq);//业务流水
		fileInput.setDataid(dataid);//数据类型
		fileInput.setDescri(descri);//结果描述
		fileInput.setFileList(op);//文件集合
		fileInput.setSource(E_SYSNAME.get(source));//源系统
		fileInput.setStatus(status);//交互状态
		fileInput.setTarget(E_SYSNAME.get(target));//目标系统
		
		//登记文件登记簿
		pss_file_rgst pss_file_rgst =SysUtil.getInstance(pss_file_rgst.class);
		String tranno = SlTools.getfilesq();//文件批次号
		pss_file_rgst.setTranno(tranno);

		pss_file_rgst.setAcctdt(acctdt);
		
		bizLog.debug("======trandt.toString():  "+acctdt +"=====批量消息通知公共外调服务============="); 
		pss_file_rgst.setDataid(dataid);
		pss_file_rgst.setFilemd(filemd);
		pss_file_rgst.setFilenm(filenm);
		pss_file_rgst.setFilest(E_FILEST.DJ);//00 - 已登记（请求文件初始状态）
		pss_file_rgst.setFlpath(flpath);
		pss_file_rgst.setSource(E_SYSNAME.get(source));
		pss_file_rgst.setTarget(E_SYSNAME.get(target));
		pss_file_rgst.setParams(params);
		pss_file_rgst.setTrandt(DateUtil.getCurTime().toString());
		pss_file_rgst.setBusseq("init");
		pss_file_rgst.setTrantm(DateUtil.getCurTime().toString());
		Pss_file_rgstDao.insert(pss_file_rgst);
		//调用文件交互通知外调分装服务
		apFileTask.FileInteractiveReqByServer(fileInput, fileOutput);
		
		//根据外调结果进行相应业务处理
		resultDeal(busseq,tranno,fileInput,fileOutput);
		
	}
	
	/*
	 * 外调对应文件系统结果处理
	 */
	public static void resultDeal (String busseq,String tranno,FileInteractiveReqByServer.InputSetter input,FileInteractiveReqByServer.Output out){
		String rebusseq = busseq;
		String dataid = input.getDataid();
		E_SYSNAME target = input.getTarget();
		E_SYSNAME source = input.getSource();
		
		if (CommUtil.isNull(busseq)) {
			rebusseq = out.getBusseq();
		}
		
		bizLog.debug("rebusseq："+rebusseq);
		String dlcode = CommTools.prcRunEnvs().getErorcd();
		String dldesc = CommTools.prcRunEnvs().getErortx();
		bizLog.debug(" 文件交互公共方法，交互错误码 = %s  ", dlcode);
		bizLog.debug(" 文件交互公共方法，交互结果 = %s  ", dldesc);
		if (CommUtil.equals(dlcode, E_FAILTYPE.success.getValue())) {
			ApSysBatchDao.updSendFileRgst(tranno,E_FILEST.FS.getValue(),rebusseq,"文件交互发送成功");
			
			// 根据文件类型查询对应批量交易信息
			//KnpPara kp = KnpParaDao.selectOne_odb1("file_interactive", dataid,target.getValue(),source.getValue(), false);
			
			//将原来收到的请求改为已回执
			/*if (CommUtil.isNotNull(kp)){
				String prcscd = String.valueOf(kp.getPmkey1());  //批量交易码
				String groupId = String.valueOf(kp.getPmkey2()); //批量交易组
				String rqflag = String.valueOf(kp.getPmkey3());  //发送标识  1-是  0-否
				
				if (CommUtil.equals(rqflag, "0")  ) {
		        	int reqnum = ApSysBatchDao.sel_filergst_by_busseq(E_FILEST.SL.toString(), busseq, target.getValue(), source.getValue(), false);
		        	if (reqnum > 0) {
		        		ApSysBatchDao.upd_filergst_by_busseq(E_FILEST.HZ.getValue(), E_FILEST.SL.getValue(), busseq, target.getValue(), source.getValue());
		    		}
				}
			}*/
			
			int reqnum = ApSysBatchDao.sel_filergst_by_busseq(E_FILEST.SL.toString(), busseq, target.getValue(), source.getValue(), false);
        	if (reqnum > 0) {
        		ApSysBatchDao.upd_filergst_by_busseq(E_FILEST.HZ.getValue(), E_FILEST.SL.getValue(), busseq, target.getValue(), source.getValue());
    		}
			
		//响应方超时
		}else if(CommUtil.equals(dlcode, E_FAILTYPE.responseTimeOut.getValue())){
			ApSysBatchDao.updSendFileRgst(tranno,E_FILEST.FSCS.getValue(),rebusseq,dldesc);
			pss_warn_info tblrps_warn_info = SysUtil.getInstance(pss_warn_info.class);
			tblrps_warn_info.setTranno(tranno);
			tblrps_warn_info.setTrancd(dataid);
			tblrps_warn_info.setTrannm(0+"");//交易笔数
			tblrps_warn_info.setWarnty(E_WARNTY.FSCS);
//			tblrps_warn_info.setErinfo("文件请求通知重发超限");
			tblrps_warn_info.setErinfo(E_WARNTY.FSCS.getLongName());
			Pss_warn_infoDao.insert(tblrps_warn_info);
			
			//大额存单预约购买生成文件，通知核心失败后，短信提示相关人员及时处理
			if(CommUtil.equals(dataid, E_FILETP.FN050880.getValue())){
				Map<String,Object> map = new HashMap<String,Object>();
				Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String errMsg = f.format(new java.util.Date())+"大额存单预约购买生成文件成功，通知核心失败";
				map.put("descrb", errMsg);
				//发送短信提醒
				BatchSendSms("1","COMM_FILE_ACTIVE",dataid,map);
			}
		}
		//异常或错误
		else {
			//大额存单预约购买生成文件，通知核心失败后，短信提示相关人员及时处理
			if(CommUtil.equals(dataid, E_FILETP.FN050880.getValue())){
				Map<String,Object> map = new HashMap<String,Object>();
				Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String errMsg = f.format(new java.util.Date())+"大额存单预约购买生成文件成功，通知核心失败";
				map.put("descrb", errMsg);
				//发送短信提醒
				BatchSendSms("1","COMM_FILE_ACTIVE",dataid,map);
			}
			
			ApSysBatchDao.updSendFileRgst(tranno,E_FILEST.SB.getValue(),rebusseq,dldesc);
		}
	}
	/**
	 * 从公共参数表中获取虚拟柜员号和虚拟机构号
	 * @return
	 */
	public static KnpGlbl getTeeler(){
		KnpGlbl kp = KnpGlblDao.selectOne_odb1("pss", "xuniguiyuan", "%", "%", false);
		return kp;
	}
	/**
	 * 批量外调时，作为交易发起方，为公共变量中一些必输参数进行赋值
	 * E_SERVTP 渠道标识
	 */
	public static void setRunEnvs(E_SERVTP servtp,E_SYSTEMTP systid){
		KnpGlbl knpPara = SlTools.getTeeler();  //虚拟机构和虚拟柜员
		if(CommUtil.isNull(knpPara)){
			throw SlError.SlComm.E0001("虚拟柜员和虚拟机构不存在");
		}
		//虚拟柜员
		String teller = knpPara.getPmval1();
		String branch= knpPara.getPmval2();
		
		bizLog.debug("***虚拟柜员***"+teller+"****虚拟机构***"+branch);
		CommToolsAplt.prcRunEnvs().setTranus(teller);//交易柜员
		CommToolsAplt.prcRunEnvs().setTranbr(branch);//交易机构
		CommToolsAplt.prcRunEnvs().setBusisq(SeqUtil.getTransqFromEnvs());//业务流水号
		CommToolsAplt.prcRunEnvs().setServtp(servtp);//交易渠道
		//CommToolsAplt.prcRunEnvs().setCallsq(SeqUtil.getNextCallTransqFromEnvs());//当前调用流水
		//CommToolsAplt.prcRunEnvs().setTransq(SeqUtil.getTransqFromEnvs());//交易流水
		CommToolsAplt.prcRunEnvs().setInpudt(DateTools.getSystemDate());//上送系统日期
		CommToolsAplt.prcRunEnvs().setInpusq(SeqUtil.getTransqFromEnvs());//上送系统流水号
		CommToolsAplt.prcRunEnvs().setCorpno(CommTools.getCenterCorpno());//交易法人代码
		switch (systid.getValue()){
		case "PSS":
			CommToolsAplt.prcRunEnvs().setInpucd("PSS");//上送系统编号
			break;
			default :
				throw SlError.SlComm.E0001("遇到未知返回类型");	
		}
		
		//CommToolsAplt.prcRunEnvs().setPckgsq(SeqUtil.getPckgsq(CmpTools.SqlDateToString(CmpTools.getNowSqlDate())));//报文流水
	}
	
	
	/** 
	 * @Title: getSysDate 
	 * @Description: 获取当前日切日期系统yyyyMMdd
	 * @author Administrator
	 * @date 2017年9月26日 下午8:02:10 
	 * @version V2.3.0 
	 */
	public static String getSysCurrDate(){
		AppSydt tblAppSydt = AppSydtDao.selectOne_odb1(CommTools.getTranCorpno(), false); //modified by xieqq 20170809
		if(CommUtil.isNull(tblAppSydt)){
			throw OdError.Order.E9999("法人代码【"+CommToolsAplt.prcRunEnvs().getCorpno()+"】未在【交易系统日期参数表】中配置参数");
		}
		return tblAppSydt.getSystdt();
	}
	/** 
	 * @Title: getSysLastDate 
	 * @Description: 获取上一天日切日期系统yyyyMMdd
	 * @return
	 * @author Administrator
	 * @date 2017年9月26日 下午8:04:33 
	 * @version V2.3.0 
	 */
	public static String getSysLastDate(){
		AppSydt tblAppSydt = AppSydtDao.selectOne_odb1(CommTools.getTranCorpno(), false); //modified by xieqq 20170809
		if(CommUtil.isNull(tblAppSydt)){
			throw OdError.Order.E9999("法人代码【"+CommToolsAplt.prcRunEnvs().getCorpno()+"】未在【交易系统日期参数表】中配置参数");
		}
		return tblAppSydt.getLastdt();
	}
	/** 
	 * @Title: BatchSendSms 
	 * @Description: 批量任务失败后发送短信提醒相关人员及时处理
	 * @return
	 * @author 董志宇
	 * @date 2017年11月17日 下午16:32:33 
	 * @version V2.3.0 
	 */
	public static void BatchSendSms(Object ...args){
		if(CommUtil.isNull(args))return;
		String trantp = (String)args[0];
		if(CommUtil.equals("1", trantp)){
			
			String tranid = (String)args[1];
			String dataid = (String)args[2];
			Map<String,Object> map = (Map<String,Object>)args[3];
			
			//从knp_para表获取模版ID
			KnpPara knpParaDO = ApKnpPara.getKnpPara("MESGSD", tranid, "%");
			//短息参数配置，同时生效字段为Y，生效
			if(CommUtil.isNotNull(knpParaDO) && CommUtil.equals(knpParaDO.getPmval1(), "Y")){
				String Meteid = knpParaDO.getPmval2(); //短息模版ID
				if(CommUtil.isNotNull(Meteid)){
					
					List<pss_mobile_recd> list = Pss_mobile_recdDao.selectAll_mobile_recd_odb2(trantp, tranid, false);
					if(list!=null && list.size()>0){
						List<ApMessageComplexType.SMSCType> listSMS = new ArrayList<ApMessageComplexType.SMSCType>();
						ApMessageComplexType.SMSCType smscTypeDO = null;
						for(pss_mobile_recd recdInfo : list){
							smscTypeDO = SysUtil.getInstance(ApMessageComplexType.SMSCType.class);
							
							if(CommUtil.isNull(map))return;
							
							smscTypeDO.setMsgparm(map);//模版中的变量值,以map形式传进去
							smscTypeDO.setMeteid(Meteid); //短息模版ID,此处如何获取模版ID可参考联机模式从knp_para中获取,也可直接根据不同场景写死模版ID
							smscTypeDO.setMobile(recdInfo.getTpcode());//手机号码	
							smscTypeDO.setMessty (E_YES___.YES);//是否实时发送 ,非实时发送平台会配置定时任务拉起发送
							smscTypeDO.setMsgopt(E_MSGOPT.SUCESS);//默认值,无需修改
							smscTypeDO.setNacode("86");//默认值
							listSMS.add(smscTypeDO);
						}
						if(listSMS!=null && listSMS.size()>0){
							SMSUtil.sendSMSMessage(listSMS);  // 描述：增加待通知的短信消息,待批量交易整个成功完成,平台自动处理发送
						}
					}
				}
			}
		}else if(CommUtil.equals("2", trantp)){
			String groupid = (String)args[1];
			String stepnum = (String)args[2];
			Map<String,Object> map = (Map<String,Object>)args[3];
			
			//从knp_para表获取模版ID
			KnpPara knpParaDO = ApKnpPara.getKnpPara("MESGSD", groupid, stepnum);
			//短息参数配置，同时生效字段为Y，生效
			if(CommUtil.isNotNull(knpParaDO) && CommUtil.equals(knpParaDO.getPmval1(), "Y")){
				String Meteid = knpParaDO.getPmval2(); //短息模版ID
				if(CommUtil.isNotNull(Meteid)){
					
					List<pss_mobile_recd> list = Pss_mobile_recdDao.selectAll_mobile_recd_odb2(trantp, groupid, false);
					if(list!=null && list.size()>0){
						List<ApMessageComplexType.SMSCType> listSMS = new ArrayList<ApMessageComplexType.SMSCType>();
						ApMessageComplexType.SMSCType smscTypeDO = null;
						for(pss_mobile_recd recdInfo : list){
							smscTypeDO = SysUtil.getInstance(ApMessageComplexType.SMSCType.class);
							
							smscTypeDO.setMsgparm(map);//模版中的变量值,以map形式传进去
							smscTypeDO.setMeteid(Meteid); //短息模版ID,此处如何获取模版ID可参考联机模式从knp_para中获取,也可直接根据不同场景写死模版ID
							smscTypeDO.setMobile(recdInfo.getTpcode());//手机号码	
							smscTypeDO.setMessty (E_YES___.YES);//是否实时发送 ,非实时发送平台会配置定时任务拉起发送
							smscTypeDO.setMsgopt(E_MSGOPT.SUCESS);//默认值,无需修改
							smscTypeDO.setNacode("86");//默认值
							listSMS.add(smscTypeDO);
						}
						if(listSMS!=null && listSMS.size()>0){
							SMSUtil.sendSMSMessage(listSMS);  // 描述：增加待通知的短信消息,待批量交易整个成功完成,平台自动处理发送
						}
					}
				}
			}
		}
	}
	
	/**
	 * @Title：getSignificantDigits
	 * @Description：获取数值型参数的有效数字
	 * @param：@param propvl
	 * @param：@return
	 * @return：String
	 * @author：dongzhiyu
	 * @date：2019年10月28日
	 * @throws
	 */
	public static String getSignificantDigits(String propvl) {
		if (CommUtil.isNull(propvl)) return null;
		
		BigDecimal big = null;
		try {
			big = new BigDecimal(propvl);
		} catch (Exception e) {
			return null;
		}
		
		String[] strs = propvl.split("\\.");
		String value=strs[0];
		if (strs.length > 1) {
			String param = "";
			for (int i = strs[1].length()-1; i >= 0; i--) {
				String str = strs[1].substring(i, i+1);
				if (!CommUtil.equals("0", str)) {
					param = strs[1].substring(0, i+1);
					break;
				}
			}
			
			if (CommUtil.isNotNull(param)) {
				value = strs[0].concat(".").concat(param);
			}
		}
		
		if (CommUtil.compare(big, new BigDecimal(value)) != 0) return null;
		
		return value;
	}
	
	/*
	 * 获取自身法人代码，不同法人对应不同的法人代码
	 */
	public static String getFrdm(Class<?> intfClass) {
		RunEnvsComm trxRun = CommTools.prcRunEnvs();
		String corpno = BANK_CODE;
		// ZJRC 法人编号获取规则：机构号前三位 jiwb 20161109
		if (CommUtil.isNotNull(trxRun.getTranbr())) {
			corpno = trxRun.getTranbr().substring(0, 3);
		}
		return corpno;

	}
	
	/**
	 * @Author T
	 *         <p>
	 *         <li>2014年3月19日-上午10:18:18</li>
	 *         <li>功能说明：设置表公共字段</li>
	 *         </p>
	 * @param objCommFld
	 *            表公共字段对象
	 */
	public static void setCommField(Class<?> intfClass, Object objCommFld) {

		if (intfClass != null && KSysCommFieldTable.tsp_comm_filed.class.isAssignableFrom(intfClass)) {
			return;
		}

		Map<String, Object> mapCommFld = CommUtil.toMap(objCommFld);

		Object objFarendma = getFrdm(intfClass);
		bizLog.debug("法人代码为[%s]-[%s]", objFarendma, intfClass);

		mapCommFld.put("farendma", objFarendma);
		mapCommFld.put(Comm.corpno.getId(), objFarendma);

		// 记录状态设置 jiluztai

		if (CommUtil.isNull(mapCommFld.get("jiluztai"))) {
			mapCommFld.put("jiluztai", E_JILUZTAI.Normal);
		}

		RunEnvsComm trxRun = CommTools.prcRunEnvs();

		if (trxRun == null)
			return;
		// lipf 如果运行变量不为空，法人代码取公共运行变量中的法人代码
		mapCommFld.put("corpno", trxRun.getCorpno());
		// 维护柜员设置 weihguiy
		// mapCommFld.put("weihguiy", trxRun.getUserid());

		// 维护机构设置 weihjigo
		// mapCommFld.put("weihjigo", trxRun.getTranbr());

		// 维护日期设置 weihriqi
		// mapCommFld.put("weihriqi", trxRun.getTrandt());
		// mapCommFld.put("datetm", trxRun.getTrandt());
		// mapCommFld.put("timetm", DateTools.getTimeStamp());
		// 非批量交易设置公共字段
		/*if (SysUtil.getCurrentSystemType() != SystemType.batch) {*/
		// 创建时间
		if (CommUtil.isNull(mapCommFld.get("crtime"))) {
			mapCommFld.put("crtime", SlTools.SqlTimestampToString(SlTools.getSqlTimestamp()));
		}

		// 创建机构
		if (CommUtil.isNull(mapCommFld.get("tranbr"))) {
			mapCommFld.put("tranbr", trxRun.getTranbr());
		}

		// 创建柜员
		if (CommUtil.isNull(mapCommFld.get("tranus"))) {
			mapCommFld.put("tranus", trxRun.getTranus());
		}

		// getIscofi()为1，表示需要重新赋值，为0表示不需要重新赋值
		/*if (CommUtil.isNull(trxRun.getIscofi())
				|| CommUtil.equals(trxRun.getIscofi().getValue(),
						E_ISYES_.YES.getValue())) {
			mapCommFld.put("updtus", trxRun.getTranus());
			mapCommFld.put("updtbr", trxRun.getTranbr());
			mapCommFld.put("uptime", SlTools.getSqlTimestamp());
		}*/
		// 修改柜员
		if (CommUtil.isNull(mapCommFld.get("updtus"))) {
			mapCommFld.put("updtus", trxRun.getTranus());
		}

		// 修改机构
		if (CommUtil.isNull(mapCommFld.get("updtbr"))) {
			mapCommFld.put("updtbr", trxRun.getTranbr());
		}

		// 修改时间
		if (CommUtil.isNull(mapCommFld.get("uptime"))) {
			mapCommFld.put("uptime", SlTools.SqlTimestampToString(SlTools.getSqlTimestamp()));
		}
		
		//创建时间
		if(CommUtil.isNull(mapCommFld.get("gmt_create"))){
			mapCommFld.put("gmt_create",SlTools.getSqlTimestamp());
		}
		
		//修改时间
		if(CommUtil.isNull(mapCommFld.get("gmt_modified"))){
			mapCommFld.put("gmt_modified",SlTools.getSqlTimestamp());
		}
	}
	
	/*
	 * 拼接获取redis中slf_sprd_bprd_rela key   （为list）
	 */
	public static String getSlfSprdBprdRela(String sprdid,String sprdvr){
		String key = sprdid+"_"+sprdvr+"_bprdrela";
		return key;
	}
	/*
	 * 拼接获取redis中基础产品版本key
	 */
	public static String getSlfBprdVerf(String bprdid, String bprdvr){
		String key = bprdid+"_"+bprdvr;
		return key;
	}
	/*
	 * 拼接获取redis中可售产品版本key
	 */
	public static String getSlfSprdVerf(String sprdid,String sprdvr){
		String key = sprdid+"_"+sprdvr;
		return key;
	}
	/*
	 * 拼接获取redis中产品属性key
	 */
	public static String getProdPropInfo(String propid,String sprdid,String sprdvr,String bprdid,String bprdvr){
		String key = propid+"_"+sprdid+"_"+sprdvr+"_"+bprdid+"_"+bprdvr;
		return key;
	}
	/*
	 * 拼接获取redis中销售条件属性key
	 */
	public static String getSalePropInfo(String propid,String sprdid,String sprdvr){
		String key = propid+"_"+sprdid+"_"+sprdvr+"_0_0";
		return key;
	}
	
}
