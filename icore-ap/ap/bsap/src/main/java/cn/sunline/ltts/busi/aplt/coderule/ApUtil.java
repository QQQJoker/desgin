package cn.sunline.ltts.busi.aplt.coderule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRMPTP;
/**
 * 
 * <p>
 * 文件功能说明：
 * 冲账、冲正相关业务逻辑
 * </p>
 * 
 * @Author Administrator
 *         <p>
 *         <li>2014年12月30日-上午9:03:42</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>冲账处理</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class ApUtil {
	//默认参数
	public static final String CURRENCY = "CNY";

    // 交易事件(放在一起统一管理)
	public static final String TRANS_EVENT_CROSSSVC = "CSVC";
    public static final String TRANS_EVENT_OUTSVC = "OSVC";//外调服务
    public static final String TRANS_EVENT_OUTTXN = "OTXN";//外调交易
	public static final String TRANS_EVENT_DPOPEN = "DP01";//开户
    public static final String TRANS_EVENT_DPSAVE = "DP03";//存
    public static final String TRANS_EVENT_DPDRAW = "DP04";//取
    public static final String TRANS_EVENT_DPCLOS = "DP02";//销户
    public static final String TRANS_EVENT_WASAVE = "WA01";//亲情钱包存
    public static final String TRANS_EVENT_WADRAW = "WA02";//亲情钱包取

    public static final String TRANS_EVENT_CD_POST = "CD03"; // 存入
    public static final String TRANS_EVENT_CD_DRAW = "CD04"; // 支取
    
    public static final String TRANS_EVENT_DPFROZ = "DP05";//冻结
    public static final String TRANS_EVENT_DPTHAW = "DP06";//解冻

    public static final String TRANS_EVENT_LIMIT = "DP07";//限额

    public static final String TRANS_EVENT_DPOPCU = "DP08";//开客户账号
    public static final String TRANS_EVENT_DPCLCU = "DP09";//销客户账号

    public static final String TRANS_EVENT_FALLINST = "DP10";//倒起息(倒起息也是一种计息)
    public static final String TRANS_EVENT_CALCINST = "DP11";//日间定期计息事件(针对定期支取计息的)
    public static final String TRANS_EVENT_PAYINST = "DP12";//日间付息事件
    public static final String TRANS_EVENT_PAYINST_TALLY = "DP14";//账户付息记账事件
    public static final String TRANS_EVENT_TAX_TALLY = "DP15";//账户代扣利息税记账事件
    public static final String TRANS_EVENT_IOBILL = "DP16"; //出入金登记簿冲正	
    public static final String TRANS_EVENT_XM_IOBILL = "DP17"; //出入金登记簿冲正
    public static final String TRANS_EVENT_ACTOAC = "DP18"; //电子账户转电子账户登记簿冲正	
    public static final String TRANS_EVENT_IRADJT = "DP19"; //利息调整冲正
    public static final String TRANS_EVENT_DEDUCT = "DP20"; //扣划冲正
    public static final String TRANS_EVENT_ACTING = "DP21"; //登记会计流水冲正
    
    public static final String TRANS_EVENT_UPACST = "CA01"; //电子账户状态冲正
    public static final String TRANS_EVENT_CREPBL = "CA02"; //卡贷通额度扣减冲正
    
    
    
    public static final String TRANS_EVENT_OPENSUB = "OP01"; //负债子账号开户冲正登记
    public static final String TRANS_EVENT_OPCUSTAC = "OP02";//电子账户开户冲正登记
    
    
    public static final String TRANS_EVENT_SNINST = "SN01";//插入外围登记簿冲正登记
    public static final String TRANS_EVENT_SNUPDT = "SN02";//更新外围登记簿冲正登记
    //基金事件
    public static final String TRANS_EVENT_FDBYAP = "FD01";//基金申购申请
    public static final String TRANS_EVENT_FDBYPR = "FD02";//借据申购处理
    public static final String TRANS_EVENT_FDBKAP = "FD03";//基金赎回申请
    public static final String TRANS_EVENT_FDBKPR = "FD04";//基金赎回处理
    public static final String TRANS_EVENT_FDLIMI = "FD05";//基金限额处理
    public static final String TRANS_EVENT_FDLISU = "FD06";//基金分红处理
    
    public static final String TRANS_EVENT_ACPOST = "AC01"; //红包存入
    public static final String TRANS_EVENT_ACDRAW = "AC02"; //红包支取
    
    public static final String TRANS_EVENT_CLSACT = "CL01"; //结算账户销户
    public static final String TRANS_EVENT_CLACST = "CLST01"; //销户登记簿
    public static final String TRANS_EVENT_CUPSTR = "CUPS01"; //银联来账登记簿
    
    public static final String TRANS_EVENT_SENDRP = "RP01"; //发红包
    public static final String TRANS_EVENT_RECVRP = "RP02"; //领红包
    public static final String TRANS_EVENT_SPEND = "SP01"; //电子账户消费
    public static final String TRANS_EVENT_REFUND = "SP02"; //电子账户消费退货
    public static final String TRANS_EVENT_PAY = "SP03"; //电子账户缴费
    public static final String TRANS_EVENT_SLEP = "SL01"; //休眠登记簿冲正
    
    public static final String TRANS_EVENT_BYNOTE = "NT01"; //票据购买
		
    public static final String TRANS_EVENT_INACCR = "IN01"; //内部户贷方交易冲正
    public static final String TRANS_EVENT_INACDR = "IN02"; //内部户借方交易冲正
    public static final String TRANS_EVENT_INACPV = "IN03"; //内部户付方交易冲正
    public static final String TRANS_EVENT_INACRV = "IN04"; //内部户收方交易冲正
    public static final String TRANS_EVENT_INACOT = "IN05"; //内部户转出
    public static final String TRANS_EVENT_NESTBK = "IN06"; //内部户收方交易冲正
    public static final String TRANS_EVENT_CHCKBL = "IN07"; //平衡检查中的清算补账冲正
    public static final String TRANS_EVENT_INCLER = "IN08";//统一支付清算登记簿冲正
    public static final String TRANS_EVENT_BALNCE = "IN09"; //补记流水的冲正
    public static final String TRANS_EVENT_VIRTBL = "IN10"; //内部户虚拟子户记账的冲正
    
    public static final String TRANS_EVENT_LNPRPY = "LN01"; //贷款归还事件
    public static final String TRANS_EVENT_LNLEND = "LN03"; //贷款放款
    //public static final String TRANS_EVENT_LNREFD = "LN04"; //贷款退款
    public static final String TRANS_EVENT_LNBZEX = "LN05"; //委托贷款豁免
    public static final String TRANS_EVENT_LNINEX = "LN06"; //贷款利息免除
    //public static final String TRANS_EVENT_LNREFD = "LN04"; //贷款退款
    public static final String TRANS_EVENT_UPDLNI = "LN11"; //贷款放款更新出账信息
    public static final String TRANS_EVENT_REGTAX = "LN12"; //计收印花税登记
    
    public static final String TRANS_EVENT_CLSTRK = "CL03"; //额度冲正事件
    
    public static final String TRANS_EVENT_LGCLOS = "LG01"; //保函赔付注销
    public static final String TRANS_EVENT_LGBHKL = "LG02"; //保函开立
    
    public static final String TRANS_EVENT_LDCHAG = "LD01"; //贴现记账

    public static final String TRANS_EVENT_LCREGI = "LC01"; //卡贷通垫款登记
    public static final String TRANS_EVENT_LCREOT = "LT01"; //卡贷通垫款登记冲正

    public static final String TRANS_EVENT_INREPY = "TA01";//暂收暂付
    public static final String TRANS_EVENT_INHANG = "TA02";//挂账
    public static final String TRANS_EVENT_INCNCL = "TA03";//销账

    public static final String TRANS_EVENT_DPLOSS = "CM01";//挂失
    public static final String TRANS_EVENT_DPRELS = "CM02";//解挂
    public static final String TRANS_EVENT_DPCHVO = "CM03";//更换
    public static final String TRANS_EVENT_CM_IOBLL = "CM04";//通用记账登记簿

    
    public static final String TRANS_EVENT_CC_RETAIL = "CC01";//类信用卡消费
    public static final String TRANS_EVENT_CC_REPAY = "CC02";//类信用卡还款
    public static final String TRANS_EVENT_CC_FREE = "CC03";//随借随还
    
    public static final String TRANS_EVENT_DC_USE = "DC01";//凭证使用
    public static final String TRANS_EVENT_DC_OUT = "DC02";//凭证调出
    public static final String TRANS_EVENT_DC_REG = "DC03";//凭证对照登记
    public static final String TRANS_EVENT_DC_EXT = "DC04";//存折扩展信息登记
    public static final String TRANS_EVENT_DC_CKZ = "DC05";//存款证明冲正
    public static final String TRANS_EVENT_DC_CKB = "DC06";//存款证明冲正

    public static final String TRANS_EVENT_PBRGST = "PB01"; //收费登记
    public static final String TRANS_EVENT_CHARGE = "PB02"; //销记收费登记簿登记
    public static final String TRANS_EVENT_CHRGJT = "PB03"; //收费调整冲正

    public static final String TRANS_EVENT_FCOPEN = "FC01";//融资账户开户
    public static final String TRANS_EVENT_FCCLOS = "FC02";//融资账户销户
    public static final String TRANS_EVENT_FCSAVE = "FC03";//融资账户转入
    public static final String TRANS_EVENT_FCDRAW = "FC04";//融资账户转出

    public static final String TRANS_EVENT_CDIOBI = "CD01"; //对公电子账户账单表冲正
    public static final String TRANS_EVENT_CDSAVE = "CD05"; //单位账户存
    public static final String TRANS_EVENT_CDDRAW = "CD06"; //单位账户取
    public static final String TRANS_EVET_SD_MJBY = "SD01"; //单位账户取
    /*
     * 公共模块涉及参数
     */
    public static final E_BRMPTP BRCH_BRMPTP = E_BRMPTP.B; //公共模块机构关系默认值
    public static final E_BRMPTP CASH_BRCH_BRMPTP = E_BRMPTP.D; //机构现金凭证管理关系    

    /*
     * 现金冲正服务事件定义
     * Added by LWG at 2015/8/31
     */
    public static final String TRANS_EVENT_CS_SAVE = "CS01";//现金收入
    public static final String TRANS_EVENT_CS_DRAW = "CS02";//现金支出
    public static final String TRANS_EVENT_CS_MVIN = "CS03";//现金入库
    public static final String TRANS_EVENT_CS_MVOT = "CS04";//现金出库
    public static final String TRANS_EVENT_CS_MVRG = "CS05";//现金调拨登记    
    public static final String TRANS_EVENT_CS_OVPR = "CS06";//现金长短款销账处理      
    public static final String TRANS_EVENT_CS_OVGE = "CS07";//现金长短款挂账处理       

   // public static final String TRANS_EVENT_HC_TRAN = "HC01";//热点控制交易
    /**
     * 增值税冲正服务事件定义
     * add by luoxiaoyu at 2017/10/13
     */
    public static final String TRANS_EVENT_AC_TAX = "TX01";//现金长短款销账处理       
    
    // 冲正返回码
    public static final String Strike_backcode_SUCCESS = "00";//完成冲账
    public static final String Strike_backcode_NOTEXIST = "10";//该交易不存在
    public static final String Strike_backcode_COMPLETED = "20";//该交易已冲账
    
    //冲账处理码
    public static final String STRIKE_TXN_CODE_STR = "AS_NAS_AP_API100,AS_NAS_AP_API111,AS_NAS_BAS_ONSTRK";//内部交易码

    // 摘要码
    /*
    
    public static final String SMRY_CODE_TAX = "000214"; // 扣税
    public static final String SMRY_CODE_INST = "000212"; // 入息
    public static final String SMRY_CODE_AJUST = "000230"; // 调账
    public static final String SMRY_CODE_REPAIR = "000230"; // 串户冲正
    public static final String SMRY_CODE_TR = "000228"; // 转账
    public static final String SMRY_CODE_CHRG = "MB0002"; // 手续费
    */

    // 保全冻结类型
    public static final String FROZ_CODE_SAVE = "28";// 保全冻结类型代码

    // 跨DCN记账时的补平科目（即跨DCN往来）
    public static final String DCN_ITEM_CODE = "30209902"; //存款系统内部往来款项

    // 差错补平借方科目
    public static final String FILL_DR = "10440503";

    // 差错补平贷方科目
    public static final String FILL_CR = "20610403";

    // 散列值分布上限
    public static final int PART_NUM = 200;

    // 缺省产品号（大总账用）
    public static final String DEFAULT_PROD_CODE = "0";

    // 负债账户属性代码值
    public static final long SubAcct_Shuxdaim_1 = 1;//简易电子账户(弱电子户标志)
    public static final long SubAcct_Shuxdaim_2 = 2;//简易电子账户-待激活(待激活标志)
    public static final long SubAcct_Shuxdaim_3 = 3;//强电子户
    
    //渠道常量
    public static final String DP_DAYEND_CHANNEL = "002"; //日终批量渠道类型
    public static final String COUNTER_CHANNEL = "EB"; //统一后管
    
    public static final String TRANS_EVENT_DFPYIU = "DF01";//保险申购处理
    public static final String TRANS_EVENT_DFBKIU = "DF02";//保险退保处理
    
    public static final String DEPT_PROD_368 = "010010003";//定期368产品号
    public static final String DEPT_PROD_788 = "010010004";//定期701产品号
    
    public static final String LN_PROD_DM = "020010001";//大麦产品号
    public static final String LN_PROD_SMY = "020010002";//萨摩耶产品号
    public static final String LN_PROD_YD = "YDJF";//雨点金服产品组
    public static final String LN_PROD_XM_XF = "020010008";//小马助贷消费贷产品号
    public static final String LN_PROD_XM_BW = "020010009";//小马助贷非涉农产品号
    public static final String LN_PROD_XM_SN = "020010010";//小马助贷涉农产品号
    public static final String LN_PROD_YDJM = "020010011";//雨点加盟贷
    public static final String LN_PROD_YDYF = "020010012";//雨点运费贷
    public static final String LN_PROD_YXCD = "YXCD";//易鑫车贷产品组
    public static final String LN_PROD_YX_XC = "020010015"; //易鑫车贷新车产品号
    public static final String LN_PROD_YX_OC = "020010016"; //易鑫车贷二手车产品号
    public static final String LN_PROD_BY = "020010017"; //包银消费产品号
    public static final String LN_PROD_YYBX = "020010018";//现金贷
    public static final String LN_PROD_YYYG = "020010019";//有氧包享贷
    public static final String LN_PROD_YYXJ = "020010020";//现金贷
    public static final String LN_RPD ="020010021";//51人品贷产品代码
    public static final String LN_SY ="020010022";//闪银租房贷产品代码
    public static final String LN_PROD_ANTS ="020010023";//蚂蚁借呗产品代码

    public static final String LN_XM_REPAY = "2413300199011"; //小马代偿准备金
    public static final String LN_XM_HKQS = "2413300199012"; //小马还款待清算户
    public static final String LN_XM_RISKRT = "2413300199013"; //小马风险评估金
    public static final String LN_XM_FINANR= "2413300199014";   //小马融资顾问费
    public static final String LN_XM_ZHQS = "2413300199015"; //小马还款待清算户
    public static final String LN_XM_SNDQS = "2413300199016";//小马助贷涉农贷款待清算
    public static final String LN_XM_TQHKQS = "2413300199017";//小马助贷提前还款代扣代清算
    
    public static final String LN_SMY_CLEAR_YF = "191326115301";//萨摩耶待清算应付款项
    public static final String LN_SMY_CLEAR_ZS = "19133001991";//萨摩耶待暂收款项
    public static final String LN_SMY_CLOSE = "19133001992";//萨摩耶结算户

    public static final String GL_PROF_BNLR = "09014201990100";//本年利润
    public static final String GL_PROF_WFP = "09014301050100";//未分配利润
    public static final String GL_PROF_BFJ = "023002";//总行备付金
    public static final String GL_PROF_UN = "21143001";//银联全渠道清算资金
    public static final String GL_PROF_YS = "25193001";//银盛渠道清算资金  900013
    public static final String GL_VAT_XX = "090127113706";//销项税业务编码   

    //额度相关
    public static final String CA_EDUK_QT = "QT01";//额度扣减
    public static final String LN_PROD_BYYJ = "071602090128"; //包银消费逾期手续费
	public static final String TRANS_EVENT_SMY_IOBILL = "DP018"; //萨摩耶出入金登记簿冲正
	public static final String LN_SMY_DCZJ = "1913300199011"; //萨摩耶贷款代偿资金
    public static final String LN_SMY_LENDING = "1913300199012"; //萨摩耶贷款放款待清算   
    public static final String LN_SMY_COST = "1913300199014"; //萨摩耶贷款服务费
    public static final String LN_SMY_ZHQS = "N985201700000015"; //萨摩耶贷款代偿追回待清算
    //public static final String LN_SMY_DKQS = "1913300199016"; //萨摩耶贷款还款代扣待清算
    public static final String LN_SMY_DKQS = "N985201700000014"; //萨摩耶贷款还款代扣待清算
    public static final String LN_SMY_DSZJ = "1913300199017"; //萨摩耶贷款代收资金
    

	public static final String RISKNOTE= "RISK" ; //预警短信通知
	public static final String TRANS_EVENT_YD_KEEP = "DP019"; //雨点放款清算登记簿冲正
    public static final String LN_YX_TQHKQS = "2524300199012"; // 易鑫车贷提前还款代扣待清算
    public static final String LN_YX_DKDQS = "2524300199013"; // 易鑫车贷正常还款代扣待清算
    public static final String LN_YX_YQDQS = "2524300199014" ; //易鑫车贷逾期还款代扣待清算
    public static final String LN_YX_DCZJ = "2524300199015" ; //易鑫车贷代偿资金
    public static final String LN_YX_ZHQS = "2524300199016" ; //易鑫车贷代偿追回待清算
    public static final String LN_YX_YJLX = "252450013101001" ; //易鑫个人贷款利息收入


    public static final String LN_RPD__HKQS ="1816300199011";//51人品贷还款代扣待清算

    public static final String LN_SY_DKQS = "1925300199012"; //闪银租房贷还款代扣待清算
    public static final String LN_SY_DCQS = "1925300199013"; //闪银租房贷代偿及回购资金账户
    public static final String LN_SY_CSQS = "1925300199014"; //闪银租房贷催收代扣款项待清算
    public static final String GL_PROF_CP = "CP3001";//ChinaPay渠道清算资金
    public static final String LN_YD_DCZJ = "2504300199022";//雨点代偿资金
    public static final String LN_YD_DCZJ_JM = "25043001990260";//雨点加盟贷代偿资金
    public static final String LN_YD_FKQS_JM = "25043001990261";//雨点加盟贷放款待清算资金

    public static final String MESS_GROUPCD = "GROUP_MSG";

    public static final int DEFAULT_DivScale = 30; /* 除不尽的时候默认截取位数 */
	
	public static final String DEFAULT_CACTTP = "1"; /* 电子账户（knp_acct_type.cacttp）默认类型 */
    //insert by shangdw 20181031 规范客户账户类型代码
    public static final String CACTTP_YKTFAULT = "738";//一卡通
    public static final String CACTTP_CDHFAULT = "737";//存单
    public static final String CACTTP_CZHFAULT = "731";//存折
    //735 : 一本通
    public static final String CACTTP_YBTFAULT = "735";// 一本通
    
    //支票凭证类账户类型代码
    public static final String DCMTTP_XJZPAULT = "001";// 现金支票
    public static final String DCMTTP_ZZZPAULT = "732";// 转账支票
    public static final String DCMTTP_SCHPAULT = "746";// 商业承兑汇票
    
	//费种代码
    public static final String EXPENSES_POUNDAGE = "10005001";//手续费
    public static final String EXPENSES_REMITTANCE = "10005001";//汇划费
    
    //存放是否需要进行二阶段提交的事件
    private static final Map<String,String> n2cMap = new ConcurrentHashMap<>();
    static {
    	n2cMap.put(TRANS_EVENT_DPSAVE,TRANS_EVENT_DPSAVE);//存
    	n2cMap.put(TRANS_EVENT_WASAVE,TRANS_EVENT_WASAVE);//亲情钱包存
    	n2cMap.put(TRANS_EVENT_ACPOST,TRANS_EVENT_ACPOST);//红包存入
    	n2cMap.put(TRANS_EVENT_DEDUCT,TRANS_EVENT_DEDUCT);//扣划冲正 ？
    	n2cMap.put(TRANS_EVENT_CS_SAVE,TRANS_EVENT_CS_SAVE);//现金收入 ？
    	n2cMap.put(TRANS_EVENT_DPDRAW,TRANS_EVENT_DPDRAW);//取
    	n2cMap.put(TRANS_EVENT_CLSACT,TRANS_EVENT_CLSACT);//结算户销户
    	n2cMap.put(TRANS_EVENT_INACCR,TRANS_EVENT_INACCR);//内部户贷方交易
    	n2cMap.put(TRANS_EVENT_INACDR,TRANS_EVENT_INACDR);//内部户借方交易
    	
    	n2cMap.put(TRANS_EVET_SD_MJBY,TRANS_EVET_SD_MJBY);//结构性存款，管理节点扣减份额冲正事件
    	n2cMap.put(TRANS_EVENT_DC_USE,TRANS_EVENT_DC_USE);//凭证使用
    	
    	//TRANS_EVET_SD_MJBY
    }
    
    /**
     * 判断事件是否属于二阶段提交
     * @param tranev 事件
     * @return
     */
    public static boolean needDo2Commit(String tranev) {
	    return n2cMap.containsKey(tranev);
    }
}
