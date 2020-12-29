package cn.sunline.ltts.busi.aplt.tools;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.service.remote.exception.RemoteTimeoutException;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpGlbl;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpGlblDao;
import cn.sunline.ltts.busi.icmp.aplt.constant.ApltContants;
import cn.sunline.ltts.busi.sys.errors.UsError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SERVTP;
import cn.sunline.ltts.busi.sys.type.CfEnumType.E_FAILTO;
import cn.sunline.ltts.busi.sys.type.CfEnumType.E_FAILTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.CfEnumType.E_SYSTEMTP;

public class CifTools {
    private static final BizLog bizlog = BizLogUtil.getBizLog(CifTools.class);
    public static final String pattern8 = "yyyyMMdd";
    public static final String pattern10 = "yyyy-MM-dd";

    /**
     * @Title: getIdentityNonce
     * @Description: 获取身份核查随机数号
     * @return
     * @author Administrator
     * @date 2017年12月05日 下午3:19:49
     * @version V1.0.0
     */
    public static String getIdentityNonce() {
        StringBuffer idcknc = new StringBuffer(14);
        idcknc.append(CommTools.getSequence("idcknc", 14));
        return idcknc.toString();
    }

    /**
     * @Title: getIdentifyChekSeq
     * @Description: 获取身份核查流水号
     * @return
     * @author Administrator
     * @date 2017年12月05日 下午3:19:49
     * @version V1.0.0
     */
    public static String getIdentifyChekSeq() {
        StringBuffer idckid = new StringBuffer(22);

        idckid.append("IDCK").append(DateTools.getSystemDate()).append(CommTools.getSequence("checkid", 10));

        return idckid.toString();
    }
    /**
     * 
     * @Title: getLoginSeq
     * @Description: 获取登录流水号
     * @return
     * @author: 王磊
     * @date: 2020年9月27日 下午2:17:26
     * @version v1.0.0
     */
    public static String getLoginSeq() {
        StringBuffer lgnseq = new StringBuffer(20);

        lgnseq.append("lg").append(DateTools.getSystemDate()).append(CommTools.getSequence("loginSeq", 10));

        return lgnseq.toString();
    }
    /**
     * 
     * @Title: getImgeId
     * @Description: 获取影像流程流水号
     * @return
     * @author: liuting
     * @date: 2020年9月27日 下午2:17:26
     * @version v1.0.0
     */
    public static String getImgeId() {
    	StringBuffer imgeid = new StringBuffer(20);

    	imgeid.append("IMGEID").append(CommTools.getSequence("imgeid", 14));

        return imgeid.toString();
    }

    /**
     * @Title: getOcrPid
     * @Description: 获取OCR主键
     * @return
     * @author Administrator
     * @date 2017年12月07日 下午3:19:49
     * @version V1.0.0
     */
    public static String getOcrPid() {
        StringBuffer ocrpid = new StringBuffer(14);

        ocrpid.append("OCRP").append(CommTools.getSequence("ocrpid", 12));

        return ocrpid.toString();
    }

    /**
     * @Title: getUseridSeqNo
     * @Description: 获取唯一userid用户号
     * @return
     * @author Administrator
     * @date 2017年5月19日 下午3:19:49
     * @version V2.3.0
     */
    public static String getUseridSeqNo() {
        StringBuffer list = new StringBuffer(12);

        list.append("001").append(CommTools.getSequence("userid", 9));

        return list.toString();
    }

    public static String getListidSeqNo() {
        StringBuffer userid = new StringBuffer(12);

        //		userid.append("US").append(CommTools.getSequence("userid", 12));
        userid.append("LIST").append(CommTools.getSequence("list", 8));

        return userid.toString();
    }

    /**
     * 生成营销信息编号
     * 编号规则：8位日期+3位顺序号
     */
    public static String getInfonoSeqNo() {
        StringBuffer sendid = new StringBuffer(11);

        sendid.append(DateTools.getSystemDate()).append(CommTools.getSequence("mkin", 3));

        return sendid.toString();
    }

    /**
     * 生成消息信息编号
     * 编号规则：No+8位日期+3位顺序号
     */
    public static String getMessNotiSeqNo() {
        StringBuffer sendid = new StringBuffer(13);

        sendid.append("NO").append(DateTools.getSystemDate()).append(CommTools.getSequence("Noti", 3));

        return sendid.toString();
    }

    /**
     * 生成产品分享信息编号
     * 编号规则：SH+8位日期+3位顺序号
     */
    public static String getProdSharSeqNo() {
        StringBuffer sendid = new StringBuffer(13);

        sendid.append("SH").append(DateTools.getSystemDate()).append(CommTools.getSequence("Shar", 3));

        return sendid.toString();
    }

    /**
     * 生成绑定设备信息编号
     * 编号规则：No+8位日期+3位顺序号
     */
    public static String getDeceSeqNo() {
        StringBuffer deceid = new StringBuffer(18);

        deceid.append("DE").append(DateTools.getSystemDate()).append(CommTools.getSequence("Dece", 8));

        return deceid.toString();
    }

    /**
     * 生成机构限额编号
     * 编号规则：No+8位日期+3位顺序号
     */
    public static String getBrchLimtSeqNo() {
        StringBuffer limtid = new StringBuffer(18);

        limtid.append("BL").append(DateTools.getSystemDate()).append(CommTools.getSequence("BrchLimt", 8));

        return limtid.toString();
    }

    /**
     * 生成机构限额编号
     * 编号规则：No+8位日期+3位顺序号
     */
    public static String getLimtDetlSeqNo() {
        StringBuffer tranno = new StringBuffer(18);

        tranno.append("LD").append(DateTools.getSystemDate()).append(CommTools.getSequence("LimtDetl", 8));

        return tranno.toString();
    }

    public static String getXedSeqNo() {
        StringBuffer tranno = new StringBuffer(18);

        tranno.append("XED").append(DateTools.getSystemDate()).append(CommTools.getSequence("xed", 8));

        return tranno.toString();
    }

    public static String getSendidSeqNo() {
        StringBuffer sendid = new StringBuffer(20);

        sendid.append(DateTools.getSystemDate()).append(CommTools.getSequence("send", 12));

        return sendid.toString();
    }

    /**
     * @Title: getUseridSeqNo
     * @Description: 获取唯一客户名称
     * @return
     * @author Administrator
     * @date 2017年9月25 21：23
     * @version V2.3.0
     */
    public static String getUserName() {
        StringBuffer userid = new StringBuffer(14);

        userid.append("USNA").append(CommTools.getSequence("userna", 12));

        return userid.toString();
    }

    /**
     * @Title: getUseridSeqNo
     * @Description: 获取唯一custid客户号
     * @return
     * @author Administrator
     * @date 2017年5月19日 下午3:19:49
     * @version V2.3.0
     */
    public static String getCustidSeqNo() {
        StringBuffer custid = new StringBuffer(12);
        custid.append("CU").append(CommTools.getSequence("custid", 10));
        return custid.toString();
    }
    
    /**
     * @Title: getUseridSeqNo
     * @Description: 获取用户操作记录流水
     * @return
     * @author Administrator
     * @date 2017年5月19日 下午3:19:49
     * @version V2.3.0
     */
    public static String getOperSeq() {
        StringBuffer operSeq = new StringBuffer(20);
        operSeq.append("OPER").append(CommTools.getSequence("operseq", 16));
        return operSeq.toString();
    }

    /**
     * @Title: getUseridSeqNo
     * @Description: 获取唯一chkseq实名认证核查流水
     * @return
     * @author Administrator
     * @date 2017年5月19日 下午3:19:49
     * @version V2.3.0
     */
    public static String getChkSeqNo() {
        StringBuffer chkseq = new StringBuffer(40);

        chkseq.append("CHK").append(DateTools.getSystemDate())
                .append(CommTools.getSequence("chkseq", 20));
        return chkseq.toString();
    }

    /**
     * @Title: getUseridSeqNo
     * @Description: 获取唯一biztra签约流水
     * @return
     * @author Administrator
     * @date 2017年5月19日 下午3:19:49
     * @version V2.3.0
     */
    public static String getBiztraSeqNo() {
        StringBuffer biztra = new StringBuffer(22);

        biztra.append("BI").append(DateTools.getSystemDate())
                .append(CommTools.getSequence("biztra", 12));
        return biztra.toString();
    }

    /**
     * @Title: getClerkSeqNo
     * @Description: 获取唯一clerk柜员流水
     * @return
     * @author Administrator
     * @date 2017年6月5日 下午3:04:29
     * @version V2.3.0
     */
    public static String getClerkSeqNo() {
        StringBuffer userid = new StringBuffer(14);

        userid.append("CL").append(CommTools.getSequence("clerk", 12));
        return userid.toString();
    }

    /**
     * 
     * @Title: getAddrid
     * @Description: 获取用户地址id
     * @return
     * @author: liuting
     * @date: 2020年10月13日 下午8:53:47
     * @version v1.0.0
     */
    public static String getAddrId() {
        StringBuffer addrid = new StringBuffer(14);
        addrid.append("AD").append(CommTools.getSequence("address", 12));
        return addrid.toString();
    }

    /**
     * @Title: getRsklevSeqNo
     * @Description: 获取风险评级信息流水
     * @return
     * @author Administrator
     * @date 2017年8月23日 上午11:04:29
     * @version V2.3.0
     */
    public static String getRsklevSeqNo() {
        StringBuffer rsklev = new StringBuffer(14);

        rsklev.append("RSK").append(CommTools.getSequence("rsklev", 12));
        return rsklev.toString();
    }

    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数 此方法中前三位格式有： 13+任意数 15+除4的任意数 18+除1和4的任意数
     * 17+除9的任意数 147,198,199,166
     */
    private static boolean isChinaPhoneLegal(String phonno)
            throws PatternSyntaxException {
        KnpGlbl bapPara = KnpGlblDao.selectOne_odb1("PHONE_REG", "%", "%", "%", false);
        String regExp = bapPara.getPmval1();
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(phonno);
        return m.matches();
    }

    /**
     * 香港手机号码8位数，5|6|8|9开头+7位任意数
     */
    private static boolean isHKPhoneLegal(String phonno)
            throws PatternSyntaxException {
        String regExp = "^(5|6|8|9)\\d{7}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(phonno);
        return m.matches();
    }

    /**
     * @Title: isPhoneLegal
     * @Description: 手机号码检验
     * @param phonno
     * @return
     * @throws PatternSyntaxException
     * @author Administrator
     * @date 2017年5月25日 上午9:24:08
     * @version V2.3.0
     */
    public static boolean isPhoneLegal(String phonno)
            throws PatternSyntaxException {
        return isChinaPhoneLegal(phonno);
    }

    /**
     * 电子邮箱校验
     * 
     * @param userem
     * @return
     * @throws PatternSyntaxException
     */
    public static boolean isEmailLegal(String userem)
            throws PatternSyntaxException {
        String validateStr = "^[A-Za-z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        boolean rs = false;
        rs = matcher(validateStr, userem);
        return rs;
    }

    /**
     * 安全问题答案规则校验
     * 
     */
    public static boolean isAnswerLegal(String answer)
            throws PatternSyntaxException {
        String validateStr = "^[0-9\u4e00-\u9fa5a-zA-Z]+$";
        boolean rs = false;
        rs = matcher(validateStr, answer);
        if (rs) {
            int strLenth = answer.length();
            if (strLenth > 20 || strLenth < 2) {
                rs = false;
            }
        }
        return rs;
    }

    /**
     * 用户昵称校验
     * 
     * @param userna
     * @return
     * @throws PatternSyntaxException
     */
    public static boolean isUserNameLegal(String userna)
            throws PatternSyntaxException {
        String validateStr = "^[\\w\\-－＿[０-９]\u4e00-\u9fa5\uFF21-\uFF3A\uFF41-\uFF5A]+$";
        boolean rs = false;
        rs = matcher(validateStr, userna);
        if (rs) {
            int strLenth = userna.length();
            if (strLenth > 20 || strLenth < 2) {
                rs = false;
            }
        }
        return rs;
    }

    /**
     * @Title: isLoginNameLegal
     * @Description: 校验用户名
     * @param logina
     * @return
     * @throws PatternSyntaxException
     * @author Administrator
     * @date 2017年5月25日 上午9:34:24
     * @version V2.3.0
     */
    public static boolean isLoginNameLegal(String logina)
            throws PatternSyntaxException {
        //String validateStr = "^[\\w\\-－＿[０-９]\u4e00-\u9fa5\uFF21-\uFF3A\uFF41-\uFF5A]+$";
        String validateStr = "^[A-Za-z0-9]+$";
        boolean rs = false;
        rs = matcher(validateStr, logina);
        if (rs) {
            int strLenth = logina.length();
            if (strLenth > 20 || strLenth < 6) {
                rs = false;
            }
        }
        return rs;
    }

    private static int getStrLength(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        for (int i = 0; i < value.length(); i++) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(chinese)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }

    private static boolean matcher(String reg, String string) {
        boolean tem = false;
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(string);
        tem = matcher.matches();
        return tem;
    }
    /**
     * 
     * @Title: getBirthdayByIdno
     * @Description: 通过证件号获取出生日期
     * @param idno
     * @return 出生日期
     * @author: liuting
     * @date: 2020年9月24日 下午7:04:52
     * @version v1.0.0
     */
    public static String getBirthdayByIdno(String idtfno) {
    	String birth;
    	if (idtfno.length() == 18) {
    		birth = idtfno.substring(6, 14);
        } else {
        	// 15位身份证号
            String lbirtdt = idtfno.substring(6, 12);
            birth = "19" + lbirtdt;
        }
    	return birth;
    }

    /**
     * 
     * @param idtftp
     *        证件类型
     * @param idtfno
     *        证件号码
     */
    public static boolean chkCertnoInfo(E_IDTFTP idtftp, String idtfno) {
        // 如果校验标志为"0-不校验"，则返回true
        /*KnpGlbl tblKnpGlbl = KnpGlblDao.selectOne_odb1("idtfno_ischeck", "%",
        		"%", "%", false);
        if (CommUtil.isNotNull(tblKnpGlbl)) {
        	if (CommUtil.equals(tblKnpGlbl.getPmval1(), "N")) {
        		return;
        	}
        }*/
        // 白名单不需校验
        /*List<KnpGlbl> tblKnpGlbls = KnpGlblDao.selectAll_odb2("idtfno_nocheck",
        		false);
        for (KnpGlbl tblKnp : tblKnpGlbls) {
        	if (CommUtil.equals(tblKnp.getPmval1(), idtfno)
        			&& CommUtil.equals(tblKnp.getPmval2(), "N")) {
        		return;
        	}
        }*/
        // 对于身份证、临时身份证、户口本（身份证号）、户口本（户口本号）、港澳居民来往内地通行证、台湾居民来往大陆通行证这几种证类型，证件号码的录入规则必须为半角
        if (E_IDTFTP.SFZ == idtftp) {
            // 校验证件号码和出生日期
            String lbirtdt = "";
            if (!CommUtil.in(idtfno.length(), 15, 18)) {
                return false;
            }
            if (CommTools.rpxMatch("^([0-9]{15})$", idtfno) == 1
                    && idtfno.length() == 15) {
                return false;
            }
            if (CommTools.rpxMatch("^([0-9]{17}([X]|[0-9]{1}))$", idtfno) == 1
                    && idtfno.length() == 18) {
                return false;
            }

            // 校验出生日期和检查证件合法性
            if (idtfno.length() == 18) {
                if (!checkIDParityBit(idtfno)) {
                    return false;
                }
                lbirtdt = idtfno.substring(6, 14);
            } else if (idtfno.length() == 15) {
                lbirtdt = idtfno.substring(6, 12);
                lbirtdt = "19" + lbirtdt;
            }
            // --生日检查
            //					checkBorndt(lbirtdt);

        }
        //				else if (E_IDTFTP.HKB == idtftp) {
        //					// 证件号码必须全部为半角输入
        //					if (!(CommUtil.equals("", idtfno))
        //							&& idtfno.length() != idtfno.getBytes().length) {
        //						return false;
        //					}
        //					if (!letterIsUpperCase(idtfno)) {
        //						return false;
        //					}
        //				} 

        return true;

    }

    /**
     * 检查校验位1
     * 
     * @param certiCode
     * @return
     */
    private static boolean checkIDParityBit(String certiCode) {
        boolean flag = false;
        if (certiCode == null || "".equals(certiCode))
            return false;
        int[] ai = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1 };
        if (certiCode.length() == 18) {
            int i = 0;
            for (int k = 0; k < 18; k++) {
                char c = certiCode.charAt(k);
                int j;
                if (c == 'X')
                    j = 10;
                else if (c <= '9' || c >= '0')
                    j = c - 48;
                else
                    return flag;
                i += j * ai[k];
            }
            if (i % 11 == 1)
                flag = true;
        }
        return flag;
    }

    /**
     * 校验生日是否合法
     * 
     * @param borndt
     *        出生日期
     */
    public static void checkBorndt(String borndt) {
        // 获取交易日期
        String trandt = CommToolsAplt.prcRunEnvs().getTrandt();

        String lyear = borndt.substring(0, 4);
        String lomnth = borndt.substring(4, 6);
        String lday = borndt.substring(6, 8);

        // 1900<年份<系统日期年份
        if (!CommUtil.Between(lyear, "1901", trandt.substring(0, 4)))
            //throw Aplt.E0000("生日信息年份[" + lyear + "]应处于1900和["
            //	+ trandt.substring(0, 4) + "]之间");

            if (!CommUtil.Between(lomnth, "01", "12"))
                //throw Aplt.E0000("生日信息月份[" + lomnth + "]非法");

                if (!CommUtil.Between(lday, "01", "31"))
                    //throw Aplt.E0000("生日信息日期[" + lday + "]非法");

                    if (!CommUtil.in(lomnth, "01", "03", "05", "07", "08", "10", "12")
                            && Integer.parseInt(lday) >= 31)
                        //throw Aplt.E0000("生日信息日期[" + borndt + "]非法");

                        if (CommUtil.in(lomnth, "02") && !(DateTools.chkIsLeepYear(borndt))
                                && Integer.parseInt(lday) > 28)
                            throw ExceptionUtil.wrapThrow("TODO ");
        //throw Aplt.E0000("生日信息日期[" + borndt + "]非法");

        //		if (CommUtil.in(lomnth, "02") && DateTools.chkIsLeepYear(borndt)
        //				&& Integer.parseInt(lday) > 29)
        //throw Aplt.E0000("生日信息日期[" + borndt + "]非法");
    }

    /**
     * 检查字符串是否存在小写字母
     * 
     * @param sNumber
     *        待检查字符串
     * @return true-不存在 false-存在
     */
    public static boolean letterIsUpperCase(String sNumber) {
        for (int i = 0; i < sNumber.length(); i++) {
            char c = sNumber.charAt(i);
            if (Character.isLetter(c)) {
                if (!(Character.isUpperCase(c))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @Title: isIdNum
     * @Description: 身份证号码校验
     * @param idNum
     * @return
     * @author Administrator
     * @date 2017年6月1日 下午12:05:15
     * @version V2.3.0
     */
    public static boolean isIdNum(String idNum) {

        // 验证身份证是否合法
        return chkCertnoInfo(E_IDTFTP.SFZ, idNum);
    }

    /**
     * 
     * @Title: chkIsDate
     * @Description: 转换日期类型为yyyy-MM-dd字符串
     * @param stdate
     * @param eddate
     * @author wanglei
     * @date 2016年8月22日 上午11:06:35
     * @version V2.3.0
     */
    public static String formatDate(Date trdate, String format) {
        Format sdf = new SimpleDateFormat(format);
        try {
            String datestr = sdf.format(trdate);
            return datestr;
        } catch (Exception e) {
            throw UsError.ToolsErr.E0001();
        }

    }

    /**
     * @Title: getRandom
     * @Description: 获取6位验证码
     * @param str
     * @return
     * @author Administrator
     * @date 2017年7月11日 上午10:14:53
     * @version V2.3.0
     */
    public static String getIdentifyCode() {
        return String.format("%06d", (int) (Math.random() * 1000000)); // 长度6位;

    }

    /**
     * @Title: randomCommon
     * @Description: 随机指定范围内N个不重复的数
     * @param min
     *        指定范围最小值
     * @param max
     *        指定范围最大值
     * @param n
     *        随机数个数
     * @return
     * @author Administrator
     * @date 2017年7月11日 下午8:20:18
     * @version V2.3.0
     */
    public static int[] randomCommon(int min, int max, int n) {
        if (n > (max - min + 1) || max < min) {
            return null;
        }
        int[] result = new int[n];
        int count = 0;
        while (count < n) {
            int num = (int) (Math.random() * (max - min)) + min;
            boolean flag = true;
            for (int j = 0; j < n; j++) {
                if (num == result[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                result[count] = num;
                count++;
            }
        }
        return result;
    }

    /*
     * 外调异常结果处理 list[0],返回1，失败，返回2，超时。
     */
    public static List<String> diversionAbnormalResult(Throwable e) {

        List<String> list_str = new ArrayList<String>();
        // 异常码
        String errorCode = E_FAILTO.FA.getValue();
        // 异常信息
        String errorInfo = "";

        if (CommUtil.isNotNull(e)) {

            try {
                // 重新抛出错误
                throw e;
            } catch (AdpBusinessException busEx) {

                // 业务
                errorCode = E_FAILTO.FA.getValue();
                errorInfo = busEx.getMessage();
            } catch (RemoteTimeoutException reTimEx) {

                // 响应超时
                errorCode = E_FAILTO.TO.getValue();
                if (!CommUtil.equals(reTimEx.getCode(), "SP_RP.E022")) {
                    // 原则不执行下行语句，若执行则是代码漏洞
                    throw UsError.ToolsErr.E9999("外调异常(代码漏洞，需及时处理)");
                }
                errorInfo = reTimEx.getMessage();
            } catch (RuntimeException runtiEx) {

                /*
                 * 1、连接错误 2、服务配置错误 3、外调没有提供者 以上三种错误均包含
                 */
                errorCode = E_FAILTO.FA.getValue();
                errorInfo = runtiEx.getMessage();
            } catch (Throwable e1) {

                // 其它异常情况
                errorCode = E_FAILTO.FA.getValue();
                errorInfo = "外调异常（系统内部错误）！";
            }

        } else {
            // 原则不执行下行语句，若执行则是代码漏洞
            throw UsError.ToolsErr.E9999("外调异常catch捕获信息e为空！");
        }

        // 返回列表赋值
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
            } catch (AdpBusinessException busEx) {// 外调系统处理抛出异常
                try {
                    busEx = (AdpBusinessException) e;
                    errorMsg = busEx.getMessage();
                    ercode = busEx.getCode();
                } catch (Exception e2) {
                    bizlog.debug(" 外调失败 e = %s", e);
                }
                int index1 = errorMsg.indexOf("]");
                if (index1 >= 0) {
                    errorMsg = errorMsg.substring(index1 + 1);
                }

            } catch (RemoteTimeoutException reTimEx) {// 外调系统处理时间过长或处理时间超过本系统设置超时时间
                // 响应超时
                ercode = E_FAILTYPE.responseTimeOut.getValue();
                errorMsg = reTimEx.getMessage();
            } catch (RuntimeException runtiEx) {// 请求方请求异常 1、连接错误 2、服务配置错误
                                                // 3、外调没有提供者 等

                /*
                 * 1、连接错误 2、服务配置错误 3、外调没有提供者 以上三种错误均包含
                 */
                ercode = E_FAILTYPE.requestException.getValue();
                errorMsg = runtiEx.getMessage();
            } catch (Throwable e1) {

                // 其它异常情况
                ercode = E_FAILTYPE.otherException.getValue();
                errorMsg = "外调异常：未知错误" + e1.getMessage();
            }

        }
        // 将错误码和错误信息放在公共运行环境变量中
        CommTools.prcRunEnvs().setErorcd(ercode);
        CommTools.prcRunEnvs().setErortx(errorMsg);
        CommTools.prcRunEnvs().setErorcd(ercode);
        CommTools.prcRunEnvs().setErortx(errorMsg);

    }

    /**
     * @Title: compareTo
     * @Description: s1<s2返回true s1>=s2返回false
     * @param s1
     *        小
     * @param s2
     *        大
     * @return
     * @author Administrator
     * @date 2017年7月18日 下午5:23:25
     * @version V2.3.0
     */
    public static boolean compareTo(String small, String big) {

        int compareTo = small.compareTo(big);
        if (compareTo < 0) {
            return true;
        }
        return false;

    }

    /**
     * @Title: testPlate
     * @Description: 挡板测试外调异常
     * @author Administrator
     * @date 2017年7月19日 上午10:34:03
     * @version V2.3.0
     */
    public static void testPlate() {
        String prcscd = CommToolsAplt.prcRunEnvs().getPrcscd();
        // 挡板测试异常
        KnpGlbl KnpGlbl = KnpGlblDao.selectOne_odb1("cif.config",
                SysUtil.getSubSystemId(), prcscd, "%", false);
        if (CommUtil.isNotNull(KnpGlbl)) {
            String pmval1 = KnpGlbl.getPmval1();

            String pmval5 = KnpGlbl.getPmval5();
            if (CommUtil.equals("0", pmval1)) {
                throw UsError.ToolsErr.E9999("模拟发起外调超时" + pmval5 + prcscd);
            }
            if (CommUtil.equals("1", pmval1)) {
                throw UsError.ToolsErr.E9999("模拟发起外调失败" + pmval5 + prcscd);
            }

        }

    }

    /**
     * @Title: getGropid
     * @Description: 通过传入的字段进行数据分组，用于后续进行批量分发进行数据拆分
     * @param gropidStr
     * @return
     * @author Administrator
     * @date 2017年7月31日 下午4:46:51
     * @version V2.3.0
     */
    public static int getGropid(String gropidStr) {
        int gropid = gropidStr.hashCode();
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
     * 文件交互获取文件名
     */
    public static String getFileName(String dataid, String trandt) {
        StringBuffer filename = new StringBuffer(100);
        filename.append("CIF_").append(trandt + "_")
                .append(CommTools.getSequence("file_" + dataid, 10));
        return filename.toString();
    }

    /**
     * 文件交互获取文件根目录,查询参数表
     * 
     */
    public static String getParam(String paramkey) {
        KnpGlbl kp = KnpGlblDao.selectOne_odb1(paramkey, "%", "%", "%", false);
        if (CommUtil.isNull(kp)) {
            throw UsError.UsComm.E0006("根目录参数未配置");
        }
        return kp.getPmval1();
    }

    /**
     * 从公共参数表中获取虚拟柜员号和虚拟机构号
     * 
     * @return
     */
    public static KnpGlbl getTeeler() {
        KnpGlbl kp = KnpGlblDao.selectOne_odb1("cif", "xuniguiyuan", "%", "%",
                false);
        return kp;
    }

    /**
     * 批量外调时，作为交易发起方，为公共变量中一些必输参数进行赋值 E_SERVTP 渠道标识
     */
    public static void setRunEnvs(E_SERVTP servtp, E_SYSTEMTP systid) {

        // CommToolsAplt.prcRunEnvs().setPckgsq(SeqUtil.getPckgsq(CmpTools.SqlDateToString(CmpTools.getNowSqlDate())));//报文流水
        /*KnpGlbl KnpGlbl = CmpTools.getTeeler();  //虚拟机构和虚拟柜员
        if(CommUtil.isNull(KnpGlbl)){
        	throw Cmp.E0132();
        }
        //虚拟柜员
         
        String teller = KnpGlbl.getPmval1();
        String branch= KnpGlbl.getPmval2();
        
        CommToolsAplt.prcRunEnvs().setTranus(teller);//交易柜员
        CommToolsAplt.prcRunEnvs().setTranbr(branch);//交易机构
        
        
        CommToolsAplt.prcRunEnvs().setBusisq(mainSeq);//业务流水号
        CommToolsAplt.prcRunEnvs().setServtp(servtp.toString());//交易渠道		
        //CommToolsAplt.prcRunEnvs().setCallsq(SeqUtil.getNextCallTransqFromEnvs());//当前调用流水
        //		CommToolsAplt.prcRunEnvs().setTransq(SeqUtil.getTransqFromEnvs());//交易流水
        CommToolsAplt.prcRunEnvs().setMntrsq(mainSeq);
        CommToolsAplt.prcRunEnvs().setPckgdt(DateTools.getSystemDate());//报日期
        
        CommToolsAplt.prcRunEnvs().setPckgsq(SeqUtil.genPkgSeq());;//.getPckgsq(DateTools.getSystemDate()));//包流水
        CommToolsAplt.prcRunEnvs().setInpudt(CmpTools.getSysDate());//上送系统日期
        
        
        CommToolsAplt.prcRunEnvs().setInpusq(mainSeq);//上送系统流水号
        CommToolsAplt.prcRunEnvs().setCorpno(getCenterCorpno());//交易法人代码
        switch (systid.getValue()){
        case "CMP":
        	CommToolsAplt.prcRunEnvs().setInpucd("CMP");//上送系统编号
        	break;
        case "RPS":
        	CommToolsAplt.prcRunEnvs().setInpucd("RPS");
        	break;
        case "CCS":
        	CommToolsAplt.prcRunEnvs().setInpucd("CCS");
        	break;
        	 
        }*/

        String mainSeq = SeqUtil.genMainTranSeq();

        CommToolsAplt.prcRunEnvs().setPckgsq(SeqUtil.genPkgSeq());//报文流水

        CommToolsAplt.prcRunEnvs().setInpusq(mainSeq);//上送系统流水号

        CommToolsAplt.prcRunEnvs().setServtp(E_SERVTP.CIF);//交易渠道        

        CommToolsAplt.prcRunEnvs().setBusisq(mainSeq);//业务流水

    }

    /*
     * 生成客户信息文件流水号
     */
    public static String getfilesq() {
        StringBuffer tempbtchno = new StringBuffer(21);
        tempbtchno.append("CIF");
        tempbtchno.append(DateTools.getSystemDate()).append(
                CommTools.getSequence("cmp_filesq", 9));
        String tranno = tempbtchno.toString();

        bizlog.debug("  ######文件交互流水号 = %s", tranno);
        return tranno;

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
    public static boolean chkIsDateString(String sDate, String format) {
        bizlog.debug("输入日期为[%s]", sDate);
        boolean bValid = false;
        if (CommUtil.isNull(sDate)) {
            //throw Sys.E0002(sDate);
        }
        Format f = new SimpleDateFormat(format);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        java.util.Date date = new java.util.Date();
        try {
            date = dateFormat.parse(sDate);
        } catch (ParseException e) {
            throw UsError.ToolsErr.E0001();
        }
        String tmp = f.format(date);
        bValid = CommUtil.equals(tmp, sDate);

        return bValid;
    }

    /**
     * 
     * @Title: getBrchJiBie
     * @Description: 截取机构号前六位获取机构级别
     * @param tranbr
     * @return
     */
    public static String getBrchJiBie(String tranbr) {

        KnpGlbl tbl_KnpGlbl = KnpGlblDao.selectOne_odb1("ProvincialBranch", "%", "provbr", "%", true);
        String provbr = tbl_KnpGlbl.getPmval1();

        if (CommUtil.isNotNull(tranbr) && tranbr.startsWith(provbr)) {
            tranbr = null;
        }

        return tranbr;

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
    public static boolean chkIsDate(Date Date, String format) {
        bizlog.debug("输入日期为[%s]", Date);
        boolean bValid = false;
        if (CommUtil.isNull(Date)) {
            //throw Sys.E0002(Date.toString());
        }

        try {
            String formatDate = DateUtil.formatDate(Date, format);

            if (CommUtil.isNull(formatDate)) {
                //throw Sys.E0002("--------------------Date:"+Date+"日期转换异常-------------------format:"+format);
            }
        } catch (Exception e) {
            //throw Sys.E0002("--------------------Date:"+Date+"日期转换异常-------------------format:"+format);
        }
        bValid = true;
        return bValid;
    }

    /**
     * 
     * <p>
     * Description: String(yyyy-MM-dd HH:mm:ss)转换为sql类型的Timestamp : yyyy-MM-dd
     * HH:mm:ss
     * </p>
     * 
     * @param str
     * @return
     * 
     *         <p>
     *         create by wl AT TIME 2016年7月4日 下午3:09:27
     *         </p>
     */
    public static Date SqlStringToTimestamp(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            java.util.Date date = sdf.parse(str);
            return date;
        } catch (ParseException e) {
            throw UsError.ToolsErr.E0001();
        }

    }

    /**
     * 获取两个时间之间的小时差 (向下取整)
     * 
     * @param startDt
     * @param endDt
     * @return
     */
    public static double calDiffHours(String startDt, String endDt) {
        Date stDate = SqlStringToTimestamp(startDt);
        Date endDate = SqlStringToTimestamp(endDt);
        Calendar cal = Calendar.getInstance();
        cal.setTime(stDate);
        long time1 = stDate.getTime();
        cal.setTime(endDate);
        long time2 = endDate.getTime();

        long time = time2 - time1;
        DecimalFormat df = new DecimalFormat("0.00");
        String huor = df.format((float) time / (1000 * 3600));
        return Double.parseDouble(huor);
    }
    
    /**
     * 
     * @Title: calDiffTimes
     * @Description: 时间差返回格式为 HH:mm:ss
     * @param startDt
     * @param endDt
     * @return
     * @author: ltg
     * @date: 2020年10月27日 下午3:24:23
     * @version v1.0.0
     */
    
    public static String calDiffTimes(String startDt, String endDt) {
        Date stDate = SqlStringToTimestamp(startDt);
        Date endDate = SqlStringToTimestamp(endDt);
        Calendar cal = Calendar.getInstance();
        cal.setTime(stDate);
        long time1 = stDate.getTime();
        cal.setTime(endDate);
        long time2 = endDate.getTime();
        long l = time1 - time2;
        long day = l/(24*60*60*1000);
        long hour = (l/(60*60*1000)-day*24);
        long min = ((l/(60*1000))-day*24-hour*60);
        long s = (l/1000-day*24*60*60-hour*60*60-min*60);
        String betwTime = hour+":"+min+":"+s+":";
        return betwTime;
    }
    
    /**
     * 
     * @Title: millisecondTurnTime
     * @Description: 将时间转换为毫秒进行计算然后再转换为时间
     * @param oretDt
     * @param addDt
     * @return
     * @author: ltg
     * @date: 2020年10月26日 下午3:05:37
     * @version v1.0.0
     */
	public static String millisecondTurnTime(String oretDt, String addDt) {
		Date stDate = SqlStringToTimestamp(oretDt);
		Calendar cal = Calendar.getInstance();
		cal.setTime(stDate);
		long time1 = stDate.getTime();
		long endTime = time1 + Integer.parseInt(addDt) * 60 * 60*1000;
		cal.setTimeInMillis(endTime);
		Date formTime = cal.getTime();
		Format t = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return t.format(formTime);
	}

    /**
     * 
     * <p>Description:根据String类型参数对日期作比较 </p>
     * 
     * @author 39renmingming
     * @date 2018年1月17日
     * @param d1
     * @param d2
     * @return
     */
    public static int compareDate(String d1, String d2) {
        return DateTools.compareDate(DateTools.covStringToDate(d1), DateTools.covStringToDate(d2));
    }

    /**
     * 
     * <p>Description: 密码校验</p>
     * 
     * @author 39renmingming
     * @date 2018年1月22日
     * @param pwd1
     * @param pwd2
     * @return
     */
    public static boolean validatePwd(String pwd1, String pwd2) {
        return CommUtil.equals(pwd1, pwd2);
    }

    /**
     * 
     * <p>Description: 设置加密密码</p>
     * 
     * @author 39renmingming
     * @date 2018年3月7日
     * @param passwd
     * @return
     */
    public static String dealPasswd(String passwd) {
        //		return CryptFactory.getCrypt().encryptionKeys(passwd);
        return passwd;
    }

    /**
     * @Title: getSqlDate
     * @Description: 获取当前sql日期类型
     * @return
     * @author Administrator
     * @date 2017年5月25日 上午9:21:53
     * @version V2.3.0
     */
    public static java.sql.Date getSqlDate() {
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        return date;
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

    /**
     * 
     * <p>
     * Description: (yyyy-MM-dd HH:mm:ss)String转date
     * </p>
     * 
     * @param timestamp
     * @return
     * 
     *         <p>
     *         create by wl AT TIME 2018年3月10日 下午11:50:34
     *         </p>
     */
    public static Date timestampStrToDate(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(timestamp);
        } catch (ParseException e) {
            throw UsError.ToolsErr.E0001();
        }
        return date;
    }

    public static Date covStringToDate(String sDate, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date d = new Date();
        try {
            d = dateFormat.parse(sDate);
        } catch (ParseException e) {
            throw UsError.ToolsErr.E0001(e);
        }
        return d;
    }

    public static String covDateToString(Date dtDate, String pattern) {
        Format f = new SimpleDateFormat(pattern);
        return f.format(dtDate);
    }

    /**
     * 
     * <p>Description: yyyy-mm-dd--->>yyyymmdd</p>
     * 
     * @author 39renmingming
     * @date 2018年3月14日
     * @param strDate
     * @return
     */
    public static String tun10to8(String strDate) {
        return covDateToString(covStringToDate(strDate, pattern10), pattern8);
    }

    /**
     * 
     * <p>Description: yyyymmdd--->>yyyy-mm-dd</p>
     * 
     * @author 39renmingming
     * @date 2018年3月14日
     * @param strDate
     * @return
     */
    public static String tun8to10(String strDate) {
        return covDateToString(covStringToDate(strDate, pattern8), pattern10);
    }

    /**
     * 
     * <p>Description:从ODS读取文件 </p>
     * 
     * @author 39renmingming
     * @date 2018年4月7日
     */
    public static boolean readFileFromODS(String ip, String username, String passwd, String sharePath, String odsPath, List<String> fileList) {

        bizlog.debug("**********连接ODS开始****############");
        Connection con = new Connection(ip, 22);
        boolean flag = true;

        try {
            bizlog.debug("**********ODS连接开始****############");
            con.connect();

            bizlog.debug("**********ODS验证开始****############");
            boolean isAuthed = con.authenticateWithPassword(username, passwd);

            if (isAuthed) {

                bizlog.debug("**********开始取ok文件****############");
                SCPClient scpClient = con.createSCPClient();
                String newPath = sharePath + DateTools.getSystemDate();

                File filePath = new File(newPath);

                if (!filePath.isDirectory()) {
                    filePath.mkdirs();
                }
                bizlog.debug("**********ok文件数量****############" + fileList.size());
                for (String filename : fileList) {
                    String okFile = filename.substring(0,
                            filename.lastIndexOf("."))
                            + "_" + getDay(DateTools.getSystemDate()) + ".ok";
                    scpClient.get(odsPath + okFile, newPath);

                }

                bizlog.debug("**********取ok文件结束****############");

            }

        } catch (Exception e) {
            flag = false;
            bizlog.debug("**********取ok文件失败****############");
        } finally {
            con.close();
        }

        if (flag) {//先验证.ok文件是否生成，如果未生成，不再继续取数据文件并抛错

            try {
                bizlog.debug("**********ODS连接开始****############");
                con.connect();

                bizlog.debug("**********ODS连接验证开始****############");
                boolean isAuthed = con.authenticateWithPassword(username, passwd);

                if (isAuthed) {

                    bizlog.debug("**********开始取数据文件****############");
                    SCPClient scpClient = con.createSCPClient();
                    String newPath = sharePath + DateTools.getSystemDate();

                    File filePath = new File(newPath);

                    if (!filePath.isDirectory()) {
                        filePath.mkdirs();
                    }

                    for (String filename : fileList) {

                        scpClient.get(odsPath + filename, newPath);

                    }

                    bizlog.debug("**********取数据文件结束****############");

                }

            } catch (Exception e) {
                bizlog.debug("**********取数据文件失败****############" + e);
            } finally {
                con.close();
            }
        }

        bizlog.debug("**********连接ODS结束****############");

        return flag;

    }

    /* public static void main(String[] args) throws IOException {
    	 String lastDay = getDay(DateTools.getSystemDate());
    	 System.out.println(lastDay.substring(0, 4));
    	 System.out.println( DateTools.dateAdd(1, "20191011"));
    }*/

    /**
     * 
     * <p>Description: 短信MQ全局流水号</p>
     * 
     * @author 39wanglei
     * @date 2018年3月22日
     * @return
     */
    public static String getMQGlblsq() {
        StringBuffer tempbtchno = new StringBuffer(32);
        tempbtchno.append("CIF");
        tempbtchno.append(DateTools.getSystemDate()).append("01")
                .append(CommTools.getSequence("MQ_glblsq", 10)).append("001000000");
        String tranno = tempbtchno.toString();

        return tranno;

    }

    /**
     * 
     * <p>Description: 短信MQ全局流水号</p>
     * 
     * @author 39wanglei
     * @date 2018年3月22日
     * @return
     */
    public static String getMQMsgid() {
        StringBuffer tempbtchno = new StringBuffer(36);
        tempbtchno.append("CIF");
        tempbtchno.append(DateTools.getSystemDate())
                .append(CommTools.getSequence("MQ_msgid", 19));
        String tranno = tempbtchno.toString();

        return tranno;

    }

    /**
     * 
     * <p>Description: 获取10位服务器日期 yyyy-MM-dd</p>
     * 
     * @author 39wanglei
     * @date 2018年3月22日
     * @param date
     * @return
     */
    public static String getSysDate10() {
        SimpleDateFormat sdf = new SimpleDateFormat(BusinessConstants.CIF_DATEFORMAT10);
        return sdf.format(new Date());
    }

    /**
     * 
     * <p>Description: 获取12位服务器时间 HH:mm:ss:SSS</p>
     * 
     * @author 39wanglei
     * @date 2018年3月22日
     * @param date
     * @return
     */
    public static String getSysTime12() {
        SimpleDateFormat sdf = new SimpleDateFormat(BusinessConstants.CIF_TIMEFORMAT12);
        return sdf.format(new Date());
    }

    /**
     * 
     * <p>Description: 外调异常时分情况进行cry catch</p>
     * 保证外调时不抛出异常->DB commit
     * 
     * @author 39wanglei
     * @date 2018年2月1日
     * @param e
     */
    public static void tryRemoteException(Throwable e) {
        String ercode = E_FAILTYPE.otherException.getValue();
        String errorMsg = BusinessConstants.CIF_ERRORMSG + e.getMessage();
        //TODO 架构将extend改为String类型 
        //	        CommTools.prcRunEnvs().getExtend().put(CmpConstants.REMOTEEXCEPTIONKEY, e);

        if (CommUtil.isNotNull(e)) {

            try {
                if (bizlog.isDebugEnabled()) {
                    bizlog.error(ApltContants.aplt.L_RpsServiceImpl_0003(), e);
                }
                //重新抛出错误
                throw e;
            } catch (AdpBusinessException busEx) {//外调系统处理抛出异常
                try {
                    busEx = (AdpBusinessException) e;
                    errorMsg = busEx.getMessage();
                    ercode = busEx.getCode();
                } catch (Exception e2) {
                    if (bizlog.isDebugEnabled()) {
                        bizlog.debug(ApltContants.aplt.L_RpsServiceImpl_0001(), e);
                    }
                }
                int index1 = errorMsg.indexOf("]");
                if (index1 >= 0) {
                    errorMsg = errorMsg.substring(index1 + 1);
                }

            } catch (RemoteTimeoutException reTimEx) {//外调系统处理时间过长或处理时间超过本系统设置超时时间
                //响应超时
                ercode = reTimEx.getCode();
                errorMsg = reTimEx.getMessage();
            } catch (RuntimeException runtiEx) {// 请求方请求异常 1、连接错误 2、服务配置错误 3、外调没有提供者 等

                /*
                 * 1、连接错误 2、服务配置错误 3、外调没有提供者
                 * 以上三种错误均包含
                 */
                ercode = E_FAILTYPE.requestException.getValue();
                errorMsg = BusinessConstants.CIF_ERRORMSG + (CommUtil.isNull(runtiEx.getMessage()) ? "" : runtiEx.getMessage());

            } catch (Throwable e1) {

                //其它异常情况
                ercode = E_FAILTYPE.otherException.getValue();
                errorMsg = BusinessConstants.CIF_ERRORMSG + (CommUtil.isNull(e1.getMessage()) ? "" : e1.getMessage());
            }

        }
        //将错误码和错误信息放在公共运行环境变量中
        CommTools.prcRunEnvs().setErorcd(ercode);
        CommTools.prcRunEnvs().setErortx(errorMsg);
        if (bizlog.isDebugEnabled()) {
            bizlog.debug(ApltContants.aplt.L_RpsServiceImpl_0001(), ercode, errorMsg);
        }
    }

    /**
     * 获取当前日期前一日
     * 
     * @param source
     * @return
     */
    public static String getDay(String source) {
        Calendar c = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyyMMdd").parse(source);
        } catch (Exception e) {
            e.printStackTrace();
        }

        c.setTime(date);
        int day1 = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day1 - 1);

        String lastDay = new SimpleDateFormat("yyyyMMdd").format(c.getTime());
        return lastDay;

    }

    /**
     * 时间戳比较
     * 
     * @param t1
     * @param t2
     * @throws ParseException
     */
    public static int compareTimeDatm(String t1, String t2) {
        Date date1, date2;
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
     * 获取系统日期
     * 格式 ：yyyy-MM-dd
     * 
     * @throws ParseException
     */
    public static String getSystemDate() {
        Format t = new SimpleDateFormat("yyyy-MM-dd");

        return t.format(new Date());
    }

    /**
     * 获取系统时间
     * 格式 ：yyyyMMddHHmmss
     * 
     * @throws ParseException
     */
    public static String getSystemDateTime() {
        Format t = new SimpleDateFormat("yyyyMMddHHmmss");

        return t.format(new Date());
    }

    /**
     * 获取N天之后的日期
     * 格式 ：yyyy-MM-dd
     * 
     * @throws ParseException
     */
    public static String getDateAddDay(String sttime, int num) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(pattern10);
        calendar.setTime(CifTools.covStringToDate(sttime, "yyyy-MM-dd"));
        calendar.add(Calendar.DATE, num);

        return sdf.format(calendar.getTime());
    }

    /**
     * 
     * <p>Title:calDiffMonths </p>
     * <p>Description: 两个时间相差月份</p>
     * 
     * @author wl
     * @date 2020年8月20日
     * @param startDt
     * @param endDt
     * @return
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

        if (i > 0) {
            months--;
        }
        return years * 12 + months;
    }

    /**
     * 
     * <p>Title:calDiffYears </p>
     * <p>Description: 两个时间年份之差</p>
     * 
     * @author wl
     * @date 2020年8月20日
     * @param startDt
     * @param endDt
     * @return
     */
    public static int calDiffYears(Date startDt, Date endDt) {
        int years = 0;
        int year1 = getYear(startDt);
        int year2 = getYear(endDt);

        years = year2 - year1;

        int i = compareDate(addYears(startDt, years), endDt);

        if (i > 0) {
            years--;
        }
        return years;
    }

    /**
     * 
     * <p>Title:getYear </p>
     * <p>Description: 获取年份</p>
     * 
     * @author wl
     * @date 2020年8月20日
     * @param dt
     * @return
     */
    public static int getYear(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(1);
    }

    /**
     * 
     * <p>Title:getMonth </p>
     * <p>Description: 获取月份</p>
     * 
     * @author wl
     * @date 2020年8月20日
     * @param dt
     * @return
     */
    public static int getMonth(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(2);
    }

    /**
     * 
     * <p>Title:getDay </p>
     * <p>Description: 获取天数</p>
     * 
     * @author wl
     * @date 2020年8月20日
     * @param dt
     * @return
     */
    public static int getDay(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        return cal.get(5);
    }

    /**
     * 
     * <p>Title:addYears </p>
     * <p>Description: 加年份</p>
     * 
     * @author wl
     * @date 2020年8月20日
     * @param dt
     * @param years
     * @return
     */
    public static Date addYears(Date dt, int years) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(1, years);

        return cal.getTime();
    }

    /**
     * 
     * <p>Title:compareDate </p>
     * <p>Description: 两个日期比较</p>
     * 
     * @author wl
     * @date 2020年8月20日
     * @param d1
     * @param d2
     * @return
     */
    public static int compareDate(Date d1, Date d2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);

        return cal1.compareTo(cal2);
    }

    /**
     * 
     * <p>Title:addMonths </p>
     * <p>Description: 加月份</p>
     * 
     * @author wl
     * @date 2020年8月20日
     * @param dt
     * @param months
     * @return
     */
    public static Date addMonths(Date dt, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.add(2, months);

        return cal.getTime();
    }
}
