package cn.sunline.ltts.busi.aplt.tools;


/**
 * 
 * 业务类常量定义类
 * 
 * @author cuijia
 * 
 */
public class BusinessConstants {
	
	/*
	 * 摘要码预设常量
	 */
	public final static String SUMMARY_QT = "QT"; //其他
	public final static String SUMMARY_ZR = "ZR"; //转入
	public final static String SUMMARY_ZC = "ZC"; //转出
	public final static String SUMMARY_TZ = "TZ"; //投资
	public final static String SUMMARY_JS = "JS"; //交税
	public final static String SUMMARY_XH = "XH"; //销户
	public final static String SUMMARY_SX = "SX"; //收息
	public final static String SUMMARY_CZ = "CZ"; //冲正
	public final static String SUMMARY_AJ = "AJ"; //调整
	public final static String SUMMARY_SJ = "SJ"; //升级
	public final static String SUMMARY_SF = "SF"; //收费
	public final static String SUMMARY_FF = "FF"; //付费
	public final static String SUMMARY_JX = "JX"; //贷款计息
	public final static String SUMMARY_FK = "FK"; //放款
	public final static String SUMMARY_HK = "HK"; //还款
	public final static String SUMMARY_HX = "HX"; //还息
	public final static String SUMMARY_FX = "FX"; //付息
	public final static String SUMMARY_TK = "TK"; //退款
	public static final String SUMMARY_CZZ = "CZZ";// 冲账
	public static final String SUMMARY_IQ = "IQ";// 贷款结息
	public static final String SUMMARY_KH = "KH";// 扣划
	public static final String SUMMARY_LCTZ = "LT";// 理财投资
	public static final String SUMMARY_LCDF = "LD";// 理财兑付
	public final static String SUMMARY_DZ = "DZ"; //定期转存
	public final static String SUMMARY_TX = "TX"; //利息摊销
	//联合贷款
	public final static String SUMMARY_FKQS = "FKQS"; //联合贷款放款清算
	public final static String SUMMARY_HKQS = "HKQS"; //联合贷款还款清算
	public final static String SUMMARY_FEQS = "FEQS"; //联合贷款费用清算
	//息费减免
	public final static String SUMMARY_XFJM = "XFJM"; //贷款息费减免
	//产品编号
	public static final String DEPT_PROD_MSJJ = "040010001";//民生基金产品号
	public static final String DEPT_PROD_JJG = "040010002";//阶阶高产品号
    public static final String DEPT_PROD_YYY = "040010003";//月月盈产品号
    public static final String DEPT_PROD_DOY = "040010004";//双月盈产品号
    public static final String DEPT_PROD_DOY1 = "00001";//双月盈产品号
    //核算代码
    public static final String FD_SYY = "05043001";//基金清算
    public static final String FD_SYY_SG = "050430010201001";//双月盈申购待清算
    public static final String FD_SYY_SH = "050430010201002";//双月盈赎回待清算
    public static final String FD_SYY_FH = "050430010201003" ;//双月盈分红待清算    
    
    public static final String CallMQMess= "MESS_SEND"; //互联网核心短信MQ
    public static final String CIF_DATEFORMAT10 = "yyyy-MM-dd";//yyyy-MM-dd
    public static final String CIF_DATEFORMAT2 = "yyyy-MM-dd HH:mm:ss";//yyyy-MM-dd HH:mm:ss
    public static final String CIF_TIMEFORMAT12 = "HH:mm:ss:SSS";//HH:mm:ss:SSS
    
    public static final String CIF_ERRORMSG = "外调异常：未知错误";
    //客户锁定
    public static final String PROD_ON_MFS_CODE = "M004";
    
	public static final String SUMMARY_KYBI = "KI";// 快鱼宝存入
	public static final String SUMMARY_KYBO = "KO";// 快鱼宝转出
	
	public static final String SUMMARY_DQZC = "DQ"; //到期转存
	
	public static final String SUMMARY_SD = "SD" ; // 结构性存款
	
	public static final String SUMMARY_SUPP = "SUPP" ; // 保函保证金补充
	public static final String SUMMARY_CLOS = "CLOS" ; // 保函赔付注销
	public static final String SUMMARY_CLSK = "CLSK" ; // 保函赔付注销冲正
	public static final String SUMMARY_BHKL = "BHKL" ; // 保函开立
	
	public static final String SUMMARY_LCRP = "LCRP" ; // 卡贷通还款
	public static final String SUMMARY_REVQ = "REVQ" ; // 卡贷通额度恢复
	public static final String SUMMARY_ONPY = "ONPY" ; // 卡贷通随还
	public static final String SUMMARY_REGI = "REGI" ; // 卡贷通垫登记
	public static final String SUMMARY_LCSK = "LCSK" ; // 卡贷通垫登记
	
	public static final String SUMMARY_LDCG = "LDCG" ; // 贴现记账
	
	public static final String SUMMARY_LACL = "LACL" ; // 银承汇票注销
	public static final String SUMMARY_AC = "AC" ; // 银票承兑

    //现金模块摘要码设置
    public static final String CS_CASHIN = ""; //现金调入
    
    //外调客户信息接口
    public static final String BP1007 = "BP1007";
}
