package cn.sunline.ltts.busi.aplt.tools;

import java.math.BigDecimal;
import java.util.UUID;


/**
 * 
 * @ClassName: Constant 
 * @Description: 常量类 
 * @author baifp
 * @date 2017年7月21日 上午11:03:25 
 *
 */
public class SlConstant {
	
	/**
  	 *现金贷外调交易码
	 */
	public static final String LOAN_CASH_TRAN = "loan_cash_tran";
	/**
  	 *合作贷外调交易码
	 */
	 public static final String LOAN_COOP_TRAN = "loan_coop_tran";
	 /**
	   *存款购买交易码
	 */
	 public static final String DEPO_PURC_CODE = "AS_NAS_DP_SMBUYY";
	 /**
	  	 *存款签约交易码
	 */
	 public static final String DEPO_SIGN_CODE = "AS_NAS_DP_SMSIGN";
	 /**
	  *红包购买交易码
	 */
	 public static final String REDP_PURC_CODE = "AS_RPS_RPM_SNDACT";
	 /**
	  *存款冲正交易码
	 */
	 public static final String DEPO_CORR_CODE = "AS_NAS_BAS_ONSTRK";
	 /**
  	 *贷款产品本地服务标识
	 */
	 public static final String LOAN_LOCAL_SERVICE_CODE = "loan_local_service";
	 /**
  	 *理财产品配置复核完成后给核心发送待办接口
	 */
	 public static final String FINA_WAIT_NAS_CODE = "AS_NAS_FN_SDWAIT";
	 /**
  	 *存款购买金额
	 */
	 public static final String DEPO_PROP_TRANAM = "CP-101-008";
	 /**
  	 *存款存期
	 */
	 public static final String DEPO_PROP_PERIOD = "CP-102-004";
	 /**
  	 *贷款放款查证
	 */
	 public static final String LOAN_TRAN_VERI = "loan_tran_veri";
	 /**
  	 *募集开始日期
	 */
	 public static final String FINA_RAISE_START = "CP-251-005";
	 /**
  	 *募集结束日期
	 */
	 public static final String FINA_RAISE_OVER = "CP-251-006";
	 /**
  	 *理财最小购买单位:步长
	 */
	 public static final String FINA_MIN_BUY = "CP-251-004";
	 /**
  	 *理财最低投资金额
	 */
	 public static final String FINA_MIN_MONEY = "CP-251-003";
	 /**
  	 *预期收益率
	 */
	 public static final String FINA_PROCE_NUMB = "CP-251-001";
	 /**
  	 *收益开始日期
	 */
	 public static final String FINA_GETM_START = "CP-251-007";
	 /**
  	 *收益结束日期
	 */
	 public static final String FINA_GETM_OVER = "CP-251-008";
	 /**
  	 *面值
	 */
	public static final String FINA_FINA_VALUE = "CP-251-013";
	/**
 	 *额度类型
	 */
	public static final String QUOTA_TYPE_CODE = "SW-003-001";
	/**
 	 *总销售额度
	 */
	public static final String QUOTA_TOTAL_SALES = "SW-003-002";
	/**
 	 *已销售额度
	 */
	public static final String QUOTA_LIMIT_SALES = "SW-003-003";
	/**
 	 *理财部件	
	 */
	public static final String FINA_PART_VALUE = "CP-251";
	/**
 	 *理财风险等级
	 */
	public static final String FINA_RIST_VALUE = "CP-251-012";
	/**
 	 *预约日期:销售条件
	 */
	public static final String FINA_PRESE_VALUE = "TJ-000-009";
	/**
 	 *预约日期:预约部件
	 */
	public static final String FINA_PRESE_PART = "SW-004-001";
	/**
 	 *预约部件:总预约额度
	 */
	public static final String FINA_TOTAL_SALES = "SW-004-004";
	/**
 	 *预约部件:已预约额度
	 */
	public static final String FINA_LIMIT_SALES = "SW-004-005";
	/**
 	 *理财购买交易码
	 */
	public static final String FINA_BUY_CODE = "AS_FSM_OD_BUY";
	/**
 	 *理财撤销交易码
	 */
	public static final String FINA_CANEL_CODE = "AS_FSM_OD_CANEL";
	/**
 	 *存款购买和签约查证交易
	 */
	public static final String DEPO_VERI_CODE = "AS_NAS_DP_VERITN";
	/**
 	 *大额存单最小购买单位:步长
	 */
	public static final String BIG_MIN_BUY = "CP-102-011";
	/**
 	 *大额存单最低投资金额
	 */
	public static final String BIG_MIN_MONEY = "CP-102-010";
	/**
 	 *业务细类
	 */
	public static final String BUTYPE_MORE_DETAIL = "CP-101-003";
	/**
 	 *大额存单类型
	 */
	public static final String BIG_DETAIL_MORE = "020508";
	
	public static void main(String[] args) {

        
	}
	/**
 	 *获取总订单号的序列
	 */
	public static final String GET_ODAMAIN_SEQ = "pss_orderno";
	
	/**
 	 *产品上架同步时，APP提供给销售工厂的接口
	 */
	public static final String PROD_ON_MFS_CODE = "APP003";
	/**
 	 *产品下架时，APP提供给销售工厂的接口
	 */
	public static final String PROD_OFF_MFS_CODE = "APP004";
	
	/**
 	 *审批系统文件产品同步通知接口
    */
    public static final String FILE_ON_ICMS_CODE = "AS_ICMS_JK_UPPROD";
    /**
 	 *审批系统文件通用通知接口
    */
    public static final String FILE_CM_ICMS_CODE = "AS_ICMS_JK_FILNTC ";
    /**
 	 *产品下架时，审批系统提供给销售工厂的接口
    */
    public static final String PROD_OFF_ICMS_CODE = "AS_ICMS_JK_XJPROD";
    /**
 	 *营销系统文件通用通知接口
    */
    public static final String FILE_CMP_NOTICE_CODE = "AS_CMP_AC_REFIIN";
    /**
 	 *订单对账电子账户申请发起
    */
    public static final String CHEK_LAUC_NAS_CODE = "AS_NAS_DP_CHKNAS";
    /**
 	 *可售产品模板开头字符串
  	 */
  	public static final String SPRD_TMPL_START_CODE = "KM";
  	/**
 	 *基础产品模板开头字符串
  	 */
  	public static final String BPRD_TMPL_START_CODE = "JM";
  	/**
 	 *贷款业务分类1：自营贷款；2：委托贷款；
  	 */
  	public static final String LOAN_BUSI_IFY_ClASS = "CP-201-007";
  	
  	/**
 	 *可售产品购买次数；
  	 */
  	public static final String SPRD_PSON_BUY_TOMES = "SW-001-006";
  	/**
 	 *产品发行总份额；
  	 */
  	public static final String PROD_TOSH_MAY_ISSUE = "CP-101-009";
  	/**
 	 *产品发行已售份额；
  	 */
  	public static final String PROD_SOLD_MAY_ISSUE = "CP-101-010";
  	/**
 	 *销售日期时间周期
  	 */
  	public static final String SALE_TIME_CYC_START = "TJ-000-003";
  	/**
 	 *销售日期周期
  	 */
  	public static final String SALE_DATE_CYC_START = "TJ-000-004";
  	/**
 	 *销售时间周期
  	 */
  	public static final String SALE_TIME_CYCLE = "TJ-000-005";
  	/**
 	 *客户类型
  	 */
  	public static final String SALE_CTOM_TYPE = "TJ-000-006";
  	/**
 	 *客户性别
  	 */
  	public static final String SALE_CTOM_SEX = "TJ-000-007";
  	/**
 	 *客户年龄
  	 */
  	public static final String SALE_CTOM_AGE = "TJ-000-008";
  	/**
 	 *首次购买
  	 */
  	public static final String FINA_FIRST_BUY = "CP-251-023";
  	/**
 	 *个人累计限额
	 */
	public static final String FINA_TOTAL_LIMIT = "CP-251-024";
	/**
 	 *协议文本部件
  	 */
  	public static final String AGMT_TXT_PART = "SW-002";
  	/**
 	 *全国银行业理财登记编码
 	 */
  	public static final String FINA_REGT_CODE = "CP-251-020";
  	/**
  	 * 贷款最大放款金额
  	 */
  	public static final String FINA_MAXI_CODE = "CP-202-003";
  	/**
  	 * 贷款最小放款金额
  	 */
  	public static final String FINA_MINI_CODE = "CP-202-004";
  	/**
  	 * 理财产品展示名称(客户在APP上看到的名称)
  	 */
  	public static final String FINA_VIEW_NAME = "CP-251-025";
  	/**
  	 * 产品展示渠道[00: 云端金融,01: 微信公众号,02: 银互通]
  	 */
  	public static final String SPRD_SHOW_CHANNEL = "SW-001-011";
  	/**
  	 * 支取计划类型
  	 */
  	public static final String DEPO_DRAW_PLAN_TYPE = "CP-106-009";
	/**
 	 *获取外调的序列
	 */
	public static final String GET_CALLOUT_SEQ = "pss_remcal_seq";
}
