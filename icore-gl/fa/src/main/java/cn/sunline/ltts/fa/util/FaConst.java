package cn.sunline.ltts.fa.util;

/**
 * 
 * <p>
 * 文件功能说明：
 * 
 * </p>
 * 
 * @Author Administrator
 *         <p>
 *         <li>2017年2月27日-下午3:58:50</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年2月27日-Administrator：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaConst {

    // 总账系统编号
    public static final String GL_SYSTEM = "019";
    // 核心系统编号
    public static final String CORE_SYSTEM = "010";
    
    // 网贷系统编号
    public static final String LOAN_SYSTEM = "020";
    // 网贷文件交易机构
    public static final String LOAN_ACCT_BRANCH = "999000";
    // 网贷文件交易币种
    public static final String LOAN_TRXN_CCY = "CNY";
    // 网贷系统文件分隔符
    public static final String LOAN_SEPARATION_CHARACTER = "\\|@\\|";
    // 系统文件分隔符,
    public static final String SEPARATION_CHARACTER = ",";
    // 网贷解析报错标志
    public static final String GL_CODE_LN_ANALYSIS_ERROR_KEY = "GL_CODE_LN_ANALYSIS_ERROR";
    public static final String GL_CODE_LN_ANALYSIS_ERROR = "1";
    // 网贷汇总编号
    public static final String LN_TOTAL_BATCH_NO = "LN_TOTAL_BATCH_NO";

    // 批量系统编号
    public static final String BAT_SYSTEM = "01902";
    public static final String BAT_ACCOUNTING_TRXN_CODE = "fat2";

    // 下拉字典中系统编号的类型
    public static final String SYS_NO = "SYS_NO";

    //下拉字典中会计主体的类型
    public static final String ACCOUNTING_SUBJECT = "ACCOUNTING_SUBJECT";

    //下拉字典中余额属性的类型
    public static final String BAL_PROPERTY = "BAL_PROPERTY";

    //货币代码
    //public static final String CCY_CODE = "CCY_CODE";
    public static final String EX_CCY_CODE = "EXCHANGE_CCY_CODE";

    //  日终批量
    public static final String DAY_END = "999";

    //清算方式参数键名称 清算方式值1单点清算 2多级清算
    public static final String KEY_SETT_TYPE = "SETT_TYPE";
    public static final String SETT_SINGLE_POINT = "1";
    public static final String SETT_MULTI_LEVEL = "2";

    //清算方式补平标志参数键名称 Y补平N不补平
    public static final String KEY_SETT_FLAT_FLAG = "SETT_FLAT_FLAG";
    //清算方式补平参数键名称 0实时逐笔补平1批量逐笔补平2批量扎差补平
    public static final String KEY_SETT_FLAT_TYPE = "SETT_FLAT_TYPE";
    public static final String SETT_FLAT_REAL = "0";
    public static final String SETT_FLAT_SINGLE = "1";
    public static final String SETT_FLAT_NETTING = "2";

    //单点清算补平科目参数键值
    public static final String KEY_SETT_SINGLE_SUBJECT = "SETT_SINGLE_SUBJECT";

    //折算补平科目参数键值
    public static final String KEY_EXCHANGE_FLAT_SUBJECT = "EXCHANGE_FLAT_SUBJECT";

    //清算补平科目参数键值
    public static final String KEY_SETTLE_FLAT_SUBJECT = "SETTLE_FLAT_SUBJECT";

    //折算差额科目
    public static final String KEY_EXCHANGE_ERROR_FLAT_SUBJECT = "EXCHANGE_ERROR_FLAT_SUBJECT";

    //本年利润科目
    public static final String KEY_PROFIT_SUBJECT = "PROFIT_SUBJECT";
    
    //与总行往来科目
    public static final String KEY_HEAD_SUBJECT = "HEAD_SUBJECT";

    //  账务机构关系代码参数
    //public static final String KEY_BRCH_RELATION_CODE_ACCOUNT = "BRCH_RELATION_CODE_ACCOUNT";

    //科目长度相关
    public static final String KEY_FIRST_LEVEL_SUBJECT_LENGTH = "FIRST_LEVEL_SUBJECT_LENGTH";
    public static final String KEY_INCREASE_SUBJECT_LENGTH = "INCREASE_SUBJECT_LENGTH";

    //报表关系、账务机构代码
    public static final String KEY_REPORT_RELATION = "REPORT_RELATION";
    public static final String KEY_DIFF_CCY_IND = "DIFF_CCY_IND";

    // 表内外营业报表文件名称
    public static final String BALANCE_FILE_NAME = "OPERATIONS_STATEMENT";
    public static final String OFF_BALANCEFILE_NAME = "BUSINESS_CONDITION_OFF_BALANCE_SHEET";
    public static final String BUSINESS_CONDITION_ON = "BUSINESS_CONDITION_ON_BALANCE_SHEET";
    // 系统等待时间
    public static final String WAIT_TIME_MAIN = "EOD_SLEEP_TIME";
    public static final String WAIT_TIME_SUB_FILE = "WAIT_FILE_DOWN";

    // 批量插入数据库笔数
    public static final String BATCH_SUBMIT_COUNT = "BATCH_SUBMIT_COUNT";
    // 批量处理数据笔数
    public static final String BATCH_DO_COUNT = "BATCH_DO_COUNT";

    // 总分核对失败是否报错 1报错(缺省)0不报错
    public static final String KEY_LEDGERC_CHECK_ERROR = "LEDGERC_CHECK_ERROR";
    public static final String LEDGERC_CHECK_ERROR = "1";
    // 折算汇率来源方式 1手功维护2核心系统
    public static final String KEY_EXCHANGE_RATE_MODE = "EXCHANGE_RATE_MODE";
    public static final String EXCHANGE_RATE_MODE_MANUAL = "1";
    public static final String EXCHANGE_RATE_MODE_CORE = "2";

    public static final String GL_CODE_ANALYSIS_ERROR_KEY = "GL_CODE_ANALYSIS_ERROR";
    public static final String GL_CODE_ANALYSIS_ERROR = "1";
    
    public static final String TOTAL_BATCH_NO = "TOTAL_BATCH_NO";	// 汇总批次号

    // 折币种
    //public static final String EX_CCY_CODE_SCY = "SCY";
    //public static final String EX_CCY_CODE_SUS = "SUS";

    public static final String REMOTE_DIR = "FTP_OPEN_SETT";
    
    //年结状态 00-正常 10-暂停
    public static final String KEY_YEAREND_STATUS = "YEAREND_STATUS";
    public static final String YEAREND_STATUS_NORMAL = "00";
    public static final String YEAREND_STATUS_STOP = "10";
    
    //分户账余额初始化标示 0-未完成 1-完成
    public static final String KEY_INIT_COMP_IND = "INIT_COMP_IND";
    public static final String INIT_COMP_IND_YES = "1";
    public static final String INIT_COMP_INDNO = "0";
    public static final String INIT_FILE_IND = "INIT01.file";
    

}
