package cn.sunline.ltts.busi.aplt.tools;

/**
 * 应用层所有常量统一管理
 * 
 * @author lizhs
 * @date 2017年8月8日
 */
public class ApConstants {

    public static final String RUN_ENVS = "RUN_ENVS"; // dataMart字段公共运行变量固定写法
    // 数据集在 app_droplist 中的定义
    public static final String DATA_MART = "DATA_MART";
    // 客户信息集
    public static final String CUST_DATA_MART = "CUSTOMER";
    // 输入区数据集
    public static final String INPUT_DATA_MART = "INPUT";
    // 参数数据集
    public static final String PARM_DATA_MART = "PARM";
    // 货币代码数据集
    public static final String CURRENCY_DATA_MART = "CURRENCY";
    // 账户数据集
    public static final String ACCOUNT_DATA_MART = "ACCOUNT";
    // 子账户数据集
    public static final String SUB_ACCOUNT_DATA_MART = "SUBACCT";
    // 借据信息集
    public static final String LOAN_DATA_MART = "LOAN";
    // 参数通配符
    public static final String WILDCARD = "%";
    // 缺省的最大日期
    public static final String DEFAULT_MAX_DATE = "20991231";
    //缺省渠道号
    public static final String DEFAULT_CHANNEL = "000";

    public static final String LANGCD_NAME_KEY = "langcd"; //语言代码
    public static final String PACKSQ_NAME_KEY = "pckgsq"; // 报文流水号定义键值
    public static final String TRANSQ_NAME_KEY = "transq"; // 交易流水号定义键值	
    public static final String TRANDT_NAME_KEY = "trandt"; // 交易日期Key
    public static final String BUSISQ_NAME_KEY = "busisq"; // 业务流水号Key
    public static final String INPUDT_NAME_KEY = "inpudt"; // 上送系统日期Key
    public static final String INPUSQ_NAME_KEY = "inpusq"; // 上送系统流水号Key
    public static final String INPUCD_NAME_KEY = "inpucd"; // 上送系统编号Key
    public static final String MNTRSQ_NAME_KEY = "mntrsq"; // 主交易流水号Key
    public static final String CALLSQ_NAME_KEY = "callsq"; // 当前调用流水号Key
    public static final String ROUTRL_NAME_KEY = "routrl"; // DCN路由关键字名称Key
    public static final String COPRNO_NAME_KEY = "corpno"; // 交易法人名称Key
    public static final String TDCNNO_NAME_KEY = "tdcnno"; // 目标DCNNO名称Key
    public static final String CORPNO_NAME_KEY = "corpno"; // 法人代码名称Key
    public static final String PRCSCD_NAME_KEY = "prcscd"; // 交易码Key
    public static final String SYSTCD_NAME_KEY = "systcd"; // 系统编号Key
    public static final String CRCYCD_NAME_KEY = "crcycd"; // 币种Key
    public static final String TRANAM_NAME_KEY = "tranam"; // 交易金额Key，主要用于登记kns_tran时，从交易input中获得为该字段的值
    public static final String EVNTLV_NAME_KEY = "evntlv"; // 事件级别Key
    //	public static final String MSGCNT_NAME_KEY = "msgcnt"; // 跨节点消息数
    //	public static final String MSGDCN_NAME_KEY = "msgdcn"; // 跨节点消息内容
    public static final String XCOPFG_NAME_KEY = "xcopfg"; // 跨法人标志
    public static final String XDCNFG_NAME_KEY = "xdcnfg"; // 跨DCN标志

    public static final int DEFAULT_PAGE_SIZE = 20; // 默认页记录数
    //	public static final String BATCH_CORPNO = "999"; // 默认批量法人代码
    //	public static final String BATCH_TRANUS = "999S201"; // 日终柜员
//    public static final String BATCH_CORPNO = "985"; // 默认批量法人代码
//    public static final String BATCH_TRANUS = "9854015"; // 日终柜员

    public static final int PCKG_MAX_LEN = 4000; // 最大报文长度
    public static final int MAX_TXN_TRY_TIMES = 5; // 默认最大重试次数

    public static final String GL_DCNNO = "A00"; // 总账DCN号码

    /**
     * drs路由类型
     */
    public static final String DRS_ROUTER_ECIF = "E"; //客户号
    public static final String DRS_ROUTER_ACCOUNT = "A"; //电子账号
    public static final String DRS_ROUTER_CARDNO = "C"; //卡号
    public static final String DRS_ROUTER_IDENTIFICATION = "I"; //证件类型+证件号

    /**
     * 日终流程id
     */

    public static final String hx_before = "hx_before";
    public static final String hx_swday = "hx_swday";
    public static final String hx_dayend = "hx_dayend";
    public static final String gl_dayend = "gl_dayend";

    /**
	 *  批量汇报事件Key
	 */
    public static final String dayendRepADM_event_key = "dayendRepADM";

    public static final String dayendGLRepADM_event_key = "dayendGLRepADM";
    
    // gl 系统编号
    public static final String GL_SYSTEMID = "019";

}
