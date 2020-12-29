package cn.sunline.ltts.busi.aplt.tools;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.errors.SlError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.engine.sequence.SequenceManager;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SERVTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType2.E_SYSTEMTP;

public class CommToolsAplt {
	
    private static final BizLog bizlog = BizLogUtil.getBizLog(CommTools.class);
    public static final String PSS_SRCPID = "PSS";
    public static final String BANK_CODE = CommTools.prcRunEnvs().getCtcono();
    public static final String MEG_SEQ = "pss_send_meg";
	
	/**
	 *         <p>
	 *         <li>功能说明：获取公共运行变量</li>
	 *         </p>
	 * @return trxRun 公共运行变量
	 */
	public static RunEnvs prcRunEnvs() {
		RunEnvs trxRun = SysUtil.getTrxRunEnvs();
		return trxRun;
	}
	

	 /**
    * 获取批量错误发送消息id方法
    * 
    * @author yuanxiaojie
    * @return
    */
   public static synchronized String getPssMesSequence(String date) {
       StringBuffer buffer = new StringBuffer();
       buffer.append(PSS_SRCPID).append(date).append(getSequence(MEG_SEQ, 12));
       return buffer.toString();
   }
   
   
   /**
    * 获取序列号，当位数不足时左补齐0
    * 
    * @param key
    *        产生序列号key
    * @param len
    *        产生序列号长度
    * @return 序列号
    */
   public static String getSequence(String key, int len) {
       return getSequence(key, len, "0");
   }
   

   public static Map<String, Object> getCommReq() {
       return SysUtil.getCurrentDataArea().getCommReq();
   }
   
   
   /**
    * 获取序列号，当位数不足时左补齐
    * 
    * @param key
    *        产生序列号key
    * @param len
    *        产生序列号长度
    * @param padStr
    *        补齐字符，默认为0
    * @return 序列号
    */
   public static String getSequence(String key, int len, String padStr) {
       if (CommUtil.isNull(key))
           throw Aplt.E0000("生成序列号key不能为空");
       if (CommUtil.isNull(len)) {
           throw Aplt.E0000("生成序列号长度不能为空");
       }
       if (len == 0) {
           throw Aplt.E0000("生成序列号长度不能为0");
       }
       String keyno = SequenceManager.nextval(null,key).getNextValue();

       return CommUtil.lpad(keyno, len, padStr);

   }
   
   public static <T> List<T> parseListFromJson(String json,Class<T> type){
   	return JSON.parseArray(json, type);
   }
   
   
   public static void setRunEnvs(E_SERVTP servtp, E_SYSTEMTP systid) {
       KnpPara knpPara = getTeeler(); //虚拟机构和虚拟柜员
       if (CommUtil.isNull(knpPara)) {
           throw SlError.SlComm.E0001("虚拟柜员和虚拟机构不存在");
       }
       //虚拟柜员
       String teller = knpPara.getPmval1();
       String branch = knpPara.getPmval2();

       bizlog.debug("***虚拟柜员***" + teller + "****虚拟机构***" + branch);
       CommTools.prcRunEnvs().setTranus(teller);//交易柜员
       CommTools.prcRunEnvs().setTranbr(branch);//交易机构
       CommTools.prcRunEnvs().setBusisq(SeqUtil.getTransqFromEnvs());//业务流水号
       CommTools.prcRunEnvs().setPckgdt(DateTools.getSystemDate());//报日期
       CommTools.prcRunEnvs().setPckgsq(SeqUtil.getPckgsq(DateTools.getSystemDate()));//包流水
       CommTools.prcRunEnvs().setInpudt(DateTools.getSystemDate());//上送系统日期
       CommTools.prcRunEnvs().setInpusq(SeqUtil.getTransqFromEnvs());//上送系统流水号
       CommTools.prcRunEnvs().setCorpno(CommTools.getCenterCorpno());//交易法人代码
       switch (systid.getValue()) {
       case "PSS":
    	   CommTools.prcRunEnvs().setInpucd("PSS");//上送系统编号
           break;
       case "CMP":
    	   CommTools.prcRunEnvs().setInpucd("CMP");
           break;
       case "CIF":
    	   CommTools.prcRunEnvs().setInpucd("CIF");
           break;
       case "NAS":
    	   CommTools.prcRunEnvs().setInpucd("NAS");
           break;
       case "ICMS":
    	   CommTools.prcRunEnvs().setInpucd("ICMS");
           break;
       case "MFS":
    	   CommTools.prcRunEnvs().setInpucd("MFS");
           break;
       default:
           throw SlError.SlComm.E0001("遇到未知返回类型");
       }

       //CommToolsAplt.prcRunEnvs().setPckgsq(SeqUtil.getPckgsq(CmpTools.SqlDateToString(CmpTools.getNowSqlDate())));//报文流水
   }
	
   
   public static KnpPara getTeeler() {
       KnpPara kp = KnpParaDao.selectOne_odb1("pss", "xuniguiyuan", "%", "%","999", false);
       return kp;
   }
	
}
