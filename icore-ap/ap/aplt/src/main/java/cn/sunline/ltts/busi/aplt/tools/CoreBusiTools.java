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
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
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
 * Description: 核心工具类
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
public class CoreBusiTools {
	
	private static final BizLog bizLog = BizLogUtil.getBizLog(CoreBusiTools.class);
	
	
	 
    /**
     * 
     * @Author os_cl_zhangwenliang
     *         <p>
     *         <li>2020年11月18日-上午10:27:01</li>
     *         <li>根据卡号判断客户类型</li>
     *         </p>
     * @param cordno
     * @return
     */
    public static E_CUSTTP getCusttpByCordno(String cordno) {
    	
    	String x = cordno.substring(0,1);
    	//后期卡bin确定，截取长度可以进行修改
    	if(CommUtil.equals(x, "6")) {
    		return E_CUSTTP.PERSON;
    	}else{
    		return E_CUSTTP.CORP;
    	}
    	
    	
    }
    
    /**
     * 
     * @Author os_cl_zhangwenliang
     *         <p>
     *         <li>2020年11月18日-上午10:27:01</li>
     *         <li>根据卡号判断账户类型</li>
     *         </p>
     * @param cordno
     * @return
     */
    public static E_ACCTROUTTYPE getAcctpByCordno(String cordno) {
    	
    	String x = cordno.substring(0,1);
    	//后期卡bin确定，截取长度可以进行修改
    	if(CommUtil.equals(x, "6")) {
    		return E_ACCTROUTTYPE.CARD;
    	}else if(CommUtil.equals(x, "9")){
    		return E_ACCTROUTTYPE.INSIDE;
    	}else{
    		return E_ACCTROUTTYPE.CORP;
    	}
    	
    	
    }
    
    /**
     * 
     * @Author os_cl_zhangwenliang
     *         <p>
     *         <li>2020年12月5日-上午11:57:38</li>
     *         <li>功能说明：将timestemp转化为8位String类型</li>
     *         </p>
     * @param time
     * @return
     */
    public static String getTimeStampToString(Timestamp time) {
    	
    	String timeS = new SimpleDateFormat("yyyyMMdd").format(time);
    	
    	return timeS; 
    	
    }
    
    
	
	
	
	
	
	
}
