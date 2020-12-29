package cn.sunline.ltts.busi.aplt.tools;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SEXTYP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.DpEnumType;

public class BusiTools {

    /**
     * 根据身份证获取生日日期
     * 
     * @param sid
     * @return
     */
    public static String getBirthBySID(String sid) {
        if (sid.length() == 18) {
            return sid.substring(6, 14); // 18位身份证
        } else if (sid.length() == 15) {
            return "19" + sid.substring(6, 12); // 15位身份证
        } else {
            throw Aplt.E0000("证件号码[" + sid + "]长度非法");
        }
    }

    /**
     * 根据身份证获取性别
     * 
     * @param sid
     * @return
     */
    public static E_SEXTYP getSexBySID(String sid) {
        String a = "";
        if (sid.length() == 18) {
            // 18位身份证
            a = sid.substring(16, 17);
        } else if (sid.length() == 15) {
            // 15位身份证
            a = sid.substring(14, 15);
        } else {
            throw Aplt.E0000("证件号码[" + sid + "]长度非法");
        }
        int b = Integer.parseInt(a);
        if (b % 2 == 1) {
            return E_SEXTYP.MALE;
        } else {
            return E_SEXTYP.FEMALE;
        }
    }

    /**
     * 根据身份证获取区域号
     * 
     * @param sid
     * @return
     */
    public static String getAreaBySID(String sid) {
        return sid.substring(0, 6);
    }

    /**
     * 
     * @param idtftp
     *        证件类型
     * @param idtfno
     *        证件号码
     */
    public static void chkCertnoInfo(E_IDTFTP idtftp, String idtfno) {
        // 如果校验标志为"0-不校验"，则返回true
        KnpPara tblKnpPara = CommTools.KnpParaQryByCorpno("idtfno_ischeck", "%",
                "%", "%", false);
        if (CommUtil.isNotNull(tblKnpPara)) {
            if (CommUtil.equals(tblKnpPara.getPmval1(), "N")) {
                return;
            }
        }
        // 白名单不需校验
        List<KnpPara> tblKnpParas = KnpParaDao.selectAll_odb2("idtfno_nocheck",
                false);
        for (KnpPara tblKnp : tblKnpParas) {
            if (CommUtil.equals(tblKnp.getPmval1(), idtfno)
                    && CommUtil.equals(tblKnp.getPmval2(), "N")) {
                return;
            }
        }
        // 对于身份证、临时身份证、户口本（身份证号）、户口本（户口本号）、港澳居民来往内地通行证、台湾居民来往大陆通行证这几种证类型，证件号码的录入规则必须为半角
        if (E_IDTFTP.SFZ == idtftp || E_IDTFTP.LS == idtftp) {
            // 校验证件号码和出生日期
            String lbirtdt = "";

            if (!CommUtil.in(idtfno.length(), 15, 18)) {
                throw Aplt.E0000("证件号码[" + idtfno + "]长度非法");
            }
            if (CommTools.rpxMatch("^([0-9]{15})$", idtfno) == 1
                    && idtfno.length() == 15) {
                throw Aplt.E0000("身份证号码[" + idtfno + "]格式非法");
            }
            if (CommTools.rpxMatch("^([0-9]{17}([X]|[0-9]{1}))$", idtfno) == 1
                    && idtfno.length() == 18) {
                throw Aplt.E0000("身份证号码[" + idtfno + "]格式非法");
            }

            // 校验出生日期和检查证件合法性
            if (idtfno.length() == 18) {
                if (!checkIDParityBit(idtfno)) {
                    throw Aplt.E0000("身份证号码[" + idtfno + "]校验不正确");
                }
                lbirtdt = idtfno.substring(6, 14);
            } else if (idtfno.length() == 15) {
                lbirtdt = idtfno.substring(6, 12);
                lbirtdt = "19" + lbirtdt;
            }
            // --生日检查
            checkBorndt(lbirtdt);

        } else if (E_IDTFTP.HKB == idtftp) {
            // 证件号码必须全部为半角输入
            if (!(CommUtil.equals("", idtfno))
                    && idtfno.length() != idtfno.getBytes().length) {
                throw Aplt.E0000("证件号码请采用半角输入");
            }
            if (!letterIsUpperCase(idtfno)) {
                throw Aplt.E0000("证件号码中不能存在小写字母");
            }
        } else {
            // 其他证件不检查
            return;
        }

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
	 * 计算卡号最后一位校验数
	 */
	public static String countParityBit(StringBuffer cardno) {
		/*
		 * Luhn 计算模 10“隔位 2 倍加”校验数的公式 计算步骤如下： 
		 * 步骤1：从右边第1个数字（低序）开始每隔一位乘以2。
		 * 步骤2：把在步骤1中获得的乘积的各位数字与原号码中未乘2的各位数字相加。
		 * 步骤3：从邻近的较高的一个以0结尾的数中减去步骤2中所得到的总和[这相当于求这个总和的低
		 * 位数字（个位数）的“10的补数”]。如果在步骤2得到的总和是以零结尾的数（如30、40等等），则 校验数字就是零
		 */
		int a = 0;
		String sCardno = cardno.toString().substring(5);
		int ch[] = new int[sCardno.length()];
		for (int i = 0; i < sCardno.length(); i++) {
			ch[i] = Integer.parseInt(sCardno.substring(sCardno.length() - i - 1, sCardno.length() - i));
			if (i % 2 == 0) {
				a = a + ch[i] * 2;
			} else {
				a = a + ch[i];
			}
		}
		int b = 10 - a % 10;
		if (b == 10) {
			b = 0;
		}
		String checkNo = Integer.toString(b);
		return checkNo;
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
            throw Aplt.E0000("生日信息年份[" + lyear + "]应处于1900和["
                    + trandt.substring(0, 4) + "]之间");

        if (!CommUtil.Between(lomnth, "01", "12"))
            throw Aplt.E0000("生日信息月份[" + lomnth + "]非法");

        if (!CommUtil.Between(lday, "01", "31"))
            throw Aplt.E0000("生日信息日期[" + lday + "]非法");

        if (!CommUtil.in(lomnth, "01", "03", "05", "07", "08", "10", "12")
                && Integer.parseInt(lday) >= 31)
            throw Aplt.E0000("生日信息日期[" + borndt + "]非法");

        if (CommUtil.in(lomnth, "02") && !(DateTools.chkIsLeepYear(borndt))
                && Integer.parseInt(lday) > 28)
            throw Aplt.E0000("生日信息日期[" + borndt + "]非法");

        if (CommUtil.in(lomnth, "02") && DateTools.chkIsLeepYear(borndt)
                && Integer.parseInt(lday) > 29)
            throw Aplt.E0000("生日信息日期[" + borndt + "]非法");
    }

    /**
     * 校验有效日期
     * 
     * @param certdt
     *        校验日期
     */
    public static void checkEffectDate(String certdt) {
        DateTools.chkIsDate(certdt);

        String lyear = certdt.substring(0, 4);
        String lomnth = certdt.substring(4, 6);
        String lday = certdt.substring(6, 8);

        // 1900<年份<系统日期年份
        if (Integer.parseInt(lyear) < Integer.parseInt("1900"))
            throw Aplt.E0000("有效日期年份[" + lyear + "]应大于1900年");

        if (!CommUtil.Between(lomnth, "01", "12"))
            throw Aplt.E0000("有效日期月份[" + lomnth + "]非法");

        if (!CommUtil.Between(lday, "01", "31"))
            throw Aplt.E0000("有效日期日期[" + lday + "]非法");

        if (!CommUtil.in(lomnth, "01", "03", "05", "07", "08", "10", "12")
                && Integer.parseInt(lday) >= 31)
            throw Aplt.E0000("有效日期日期[" + certdt + "]非法");

        if (CommUtil.in(lomnth, "02") && !(DateTools.chkIsLeepYear(certdt))
                && Integer.parseInt(lday) > 28)
            throw Aplt.E0000("有效日期日期[" + certdt + "]非法");

        if (CommUtil.in(lomnth, "02") && DateTools.chkIsLeepYear(certdt)
                && Integer.parseInt(lday) > 29)
            throw Aplt.E0000("有效日期日期[" + certdt + "]非法");
    }

    /**
     * 生成客户号
     * 
     * @return 客户号
     * 
     */
    public static String genCustno() {
        return ApSeq.genSeq("CUSTNO");
    }
    
    /**
     * 生成客户号
     * 
     * @return 客户号
     * 
     */
    public static String genCustno(E_CUSTTP custtp) {
    	if (custtp == E_CUSTTP.PERSON){
    		return ApSeq.genSeq("CUSTNO");
    	} else if(custtp == E_CUSTTP.CORP){
    		return ApSeq.genSeq("CUSTNO_CORP");
    	} else if(custtp == E_CUSTTP.BANK){
    		return ApSeq.genSeq("CUSTNO_BANK");
    	} else{
    		return ApSeq.genSeq("CUSTNO");
    	}
    }

    /**
     * <p>
     * 生成产生电子账号
     * </p>
     * 
     * @return 电子账号
     */
    public static String genCustac() {
        return ApSeq.genSeq("CUSTAC");
    }

    /**
     * <p>
     * 生成产生电子账号
     * </p>
     * 
     * @return 电子账号
     */
    public static String genCustac(E_CUSTTP custtp) {
        
        if (custtp == E_CUSTTP.PERSON){
        	return ApSeq.genSeq("CUSTAC");
    	} else if(custtp == E_CUSTTP.CORP){
    		return ApSeq.genSeq("CUSTAC_CORP");
    	} else {
    		return ApSeq.genSeq("CUSTAC");
    	}
    }
    
    /**
     * <p>
     * 生成电子账户子户号
     * </p>
     * 
     * @return
     */
    public static String genSubEAccountno() {
        int len = 15;
        StringBuffer custno = new StringBuffer(len);
        custno.append(CommTools.getSequence("subsac_seq", len));
        return custno.toString();
    }

    /**
     * 
     * @Title: getAcctno
     * @Description: 生成负债子账号这里用一句话描述这个方法的作用)
     * @param busiType
     * @param custtp
     * @return
     * @author xiongzhao
     * @date 2016年9月9日 下午8:05:26
     * @version V2.3.0
     */
    public static String getAcctno(E_CUSTTP custtp,DpEnumType.E_PRODTP prodtp) {
        // 总位数23位，首位为6，剩余的22位全省大排序
        StringBuffer bufAcctno = null;
        int maxLen = 23;
        int seqLen = 0;
        String frstnm = "";
        
        if (prodtp == DpEnumType.E_PRODTP.FINA){
        	frstnm = "40000";
        } else {
        	if(custtp == E_CUSTTP.PERSON){
            	frstnm = "10000";
            }else if(custtp == E_CUSTTP.CORP){
            	frstnm = "20000";
            }else if(custtp == E_CUSTTP.BANK){
            	frstnm = "90000";
            }else{
            	frstnm = "10000";
            }
        }
        
        String dcnrecdno = DcnUtil.getCurrDCN();
		seqLen = maxLen - frstnm.length() - dcnrecdno.length();
        bufAcctno = new StringBuffer(maxLen);
        bufAcctno.append(frstnm);
        bufAcctno.append(dcnrecdno);
        bufAcctno.append(CommTools.getSequence("acctno_rank", seqLen));
        
        return bufAcctno.toString();

    }
    /**
     * 生成负债子账号 负债子账号共15位 第1位为数字银行标志，固定为8 第2-3位为产品大类 01-存款 02-贷款 03-信用卡 04-理财产品
     * 05-保险产品 06-票据产品 07-基金产品 08-红包产品 09-融资产品 第4位为个人/对公标志 0-7表示个人 8-9表示对公
     * 第5-14位为顺序号 第10位为校验位
     * 
     * @param busiType
     *        产品种类 0-存款 1-基金 2-贷款 3-信用卡 4-理财 5-保险 6-票据 7-红包 9-融资
     * @param custtp
     *        客户类型
     * @return 负债子账号
     */
    public static String getAcctNo(String busiType, String custtp) {
        int maxLen = 19;
        int seqLen = 11;
        String bankfg = "8"; // 数字银行标志
        String prodtp = null; // 01-存款 02-贷款 03-信用卡 04-理财产品（基金）
        StringBuffer bufAcctno = null;
        String sAcctno = null; // 负债账号

        int j = 0;
        boolean sucFlag = true;
        char[] acctnoArr = null;
        int valino = 0;// 校验位

        while (sucFlag) {
            bufAcctno = new StringBuffer(maxLen);
            j++;
            if (j > 10) {
                throw Aplt.E0000("生成账号出现异常");
            }
            switch (busiType) {
            case "0":
                // 存款
                prodtp = "01";
                break;
            case "1":
                // 基金
                prodtp = "07";
                break;
            case "2":
                // 贷款
                prodtp = "02";
                break;
            case "3":
                // 信用卡
                prodtp = "03";
                break;
            case "4":
                // 理财
                throw Aplt.E0000("暂未实现");
            case "5":
                // 保险
                prodtp = "05";
                break;
            case "6":
                // 票据
                prodtp = "06";
                break;
            case "7":
                // 红包产品
                prodtp = "08";
                break;
            // wuxq 2015/09/14 add 增加‘9-融资’类型账号生成处理
            case "9":
                // 融资产品
                prodtp = "09";
                break;
            default:
                throw Aplt.E0000("遇到未知业务类型");
            }

            // 个人/对公标志和顺序位
            bufAcctno.append(bankfg).append(prodtp);
            if ("11".equals(custtp)) {
                // 对私
                bufAcctno.append(ApSeq.genSeq("acctno_indv"));
            } else if ("12".equals(custtp) || "13".equals(custtp)) {
                // 对公
                bufAcctno.append(ApSeq.genSeq("acctno_corp"));
            } else {
                throw Aplt.E0000("遇到未知客户类型");
            }

            // 获取校验位，校验位为10的不取
            acctnoArr = bufAcctno.toString().toCharArray();
            int sum = 0;
            char curr;

            for (int i = 0; i < acctnoArr.length; i++) {
                curr = acctnoArr[i];
                if (curr > '9' || curr < '0') {
                    sucFlag = true;
                    continue;
                }
                sum = sum + (i + 1) * Integer.parseInt(acctnoArr[i] + "");
            }
            valino = sum % 11;
            if (valino == 10) {
                sucFlag = true;
                continue;
            }

            sucFlag = false;

        }
        sAcctno = bufAcctno.toString() + valino;
        return sAcctno;
    }

    /**
     * 
     * @Title: getAcctno
     * @Description: 生成负债子账号这里用一句话描述这个方法的作用)
     * @param busiType
     * @param custtp
     * @return
     * @author xiongzhao
     * @date 2016年9月9日 下午8:05:26
     * @version V2.3.0
     */
    public static String getAcctno() {
        // 总位数23位，首位为6，剩余的22位全省大排序
        StringBuffer bufAcctno = null;
        int maxLen = 23;
   //     int seqLen = 18;
   //     String frstnm = "60000";
        bufAcctno = new StringBuffer(maxLen);
   //     bufAcctno.append(frstnm);
        bufAcctno.append(ApSeq.genSeq("acctno_rank"));

        return bufAcctno.toString();

    }

    /**
     * 客户信息多法人管理模式
     * 
     * @return
     */
    public static String getMultCorpCIFModule() {
        return ApKnpGlbl.getKnpGlbl("system.module", SysUtil.getSubSystemId(),
                "cif").getPmval1();
    }

    /**
     * 客户额度多法人管理模式
     * 
     * @return
     */
    public static String getMultCorpCIFLimitModule() {
        return ApKnpGlbl.getKnpGlbl("system.module", SysUtil.getSubSystemId(),
                "limit").getPmval1();
    }

    /**
     * 密码加密模式
     * 
     * @return
     */
    public static String getPasswdModule() {
        return ApKnpGlbl.getKnpGlbl("system.password", "crypto").getPmval1();
    }

    /**
     * 密码最大错误次数
     * 
     * @return
     */
    public static String getPasswdMaxErrCnt() {
        return ApKnpGlbl.getKnpGlbl("system.password", "crypto").getPmval2();
    }

    /**
     * 存款入账日期判断模式
     * 
     * @return
     */
    public static boolean getDeptAcctdt() {
        // 当前为联机交易，核心系统内部外调，分布式模式并且跨DCN节点不是跨法人, 跨法人的情况例外
        if (getDistributedDeal() && CommToolsAplt.prcRunEnvs().getXcopfg() == E_YES___.NO) {
            return true;
        }
        return false;
    }

    /**
     * 判断分布式下是否跨节点处理
     * 
     * @return
     */
    public static boolean getDistributedDeal() {
        if (CommTools.isDistributedSystem()
                && SysUtil.getCurrentSystemType() == SystemType.onl
                && CommToolsAplt.prcRunEnvs().getXdcnfg() == E_YES___.YES && CommUtil.equals(CommToolsAplt.prcRunEnvs().getInpucd(), SysUtil.getSystemId())) {
            return true;
        }
        return false;
    }
    
 	/**
      * 随机10字符串
      */
     public static String getRandomID(){
     	return UUID.randomUUID().toString().replace("-", "").substring(0,10);
     }
     
     
	 /**
	  * 产生写磁控制码
	  * @return
	  * 线下业务存折类凭证的磁条码生成方法
	  */
     public static String genMgntcd() {
 		String mgntcd = new BigDecimal((Math.random() * Math.pow(10, 9)))
 				.toString();
 		// 截成6位数
 		mgntcd = mgntcd.substring(0, 6);

 		if (mgntcd.indexOf(".") != -1) {
 			mgntcd = mgntcd.replace(".", "9");
 		}
 		if (mgntcd.length() != 6) {
 			mgntcd = "517398";
 		}
 		return mgntcd;
 	}
    
     /**
      * 长度23位，81 + 7位业务细类 + 2位节点号 + 10位顺序 + 2位校验
      * @param debttp 产品细类值
      * @return
      */
     public static String genUnitAcctno(String debttp){
     	
     	int maxLen = 23;
     	StringBuffer sb = new StringBuffer();
     	//String dcnrecdno = DcnUtil.getDCNRecdno();
     	String dcnrecdno ="000";
     	sb.append("81");
     	sb.append(debttp);
     	sb.append(dcnrecdno);
     	
     	int seqLen = maxLen - sb.length() - 2;
     	
     	sb.append(CommTools.getSequence("acctno_unit", seqLen));
     	
     	String chkno = BusiTools.countParityBit(sb);
     	
     	sb.append(CommUtil.lpad(chkno, 2, "0"));
     	
     	return sb.toString();
     }

}
