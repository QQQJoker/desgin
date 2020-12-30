package cn.sunline.ltts.busi.fa.scene.accounting;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.EnumUtils;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.namedsql.FaLoanAccountingDao;
import cn.sunline.ltts.busi.fa.servicetype.SrvFaLoanAccountingEvent;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_prod_seq;
import cn.sunline.ltts.busi.fa.type.ComFaLoanAccounting.FaLoanAccountingEventInfo;
import cn.sunline.ltts.busi.fa.type.ComFaLoanAccounting.FaLoanAccountingEventResult;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CRCYCD;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQSTATE;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;

/**
 * 
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author
 *         <p>
 *         <li>2020年10月13日-下午4:05:05</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2020年10月13日：产品事件解析</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaSceneBookAccountingSeq {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaSceneBookAccountingSeq.class);

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年10月13日-下午4:06:22</li>
	 *         <li>功能说明：解析单笔流水</li>
	 *         </p>
	 * @param sys_no
	 */
	public static void bookAccountingSeq(String trxn_seq) {
		
		bizlog.method(" FaProdBookAccountingSeq.bookAccountingSeq begin >>>>>>>>>>>>>>>>");
		
		List<fab_accounting_prod_seq> seqList = FaLoanAccountingDao.lstTranSeqsFromProdAccounting(trxn_seq, false);
		
		if(seqList.isEmpty()) {
			return;
		}
		
		// 组装解析入参并解析
		Options<FaLoanAccountingEventInfo> eventInfoList = assembEventInfo(seqList);
		Options<FaLoanAccountingEventResult> eventResultList = 
				SysUtil.getInstance(SrvFaLoanAccountingEvent.class).checkAnalysisLoanEvent(new DefaultOptions<>(eventInfoList));
		
		// 组装入账入参 并入账
		List<FaSingleAccountingCheckIn> accountingList = assembAccountingInfo(eventResultList,seqList.get(0).getSys_no(),seqList.get(0).getAcct_branch());
		FaAccounting.bookMultiAccounting(accountingList, null, 1L, seqList.get(0).getSys_no(), "bookProdAccountingSeq", true);

		// 更改状态
		FaLoanAccountingDao.updStatusOfProdSeq(trxn_seq, E_TRXNSEQSTATE.SUMMARY);
		
		bizlog.method(" FaProdBookAccountingSeq.bookAccountingSeq end <<<<<<<<<<<<<<<<");
		
	}

	/**
	 * 
	 * @Author 组装记账入参集
	 *         <p>
	 *         <li>2020年11月16日-下午2:06:54</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param eventResultList
	 * @param sysNo
	 * @param acctBranch
	 * @return
	 */
	private static List<FaSingleAccountingCheckIn> assembAccountingInfo(Options<FaLoanAccountingEventResult> eventResultList, String sysNo, String acctBranch) {
		
		bizlog.method(" FaProdBookAccountingSeq.assembAccountingInfo begin >>>>>>>>>>>>>>>>");
		
		// 定义数据返回
		List<FaSingleAccountingCheckIn> accountingList = new ArrayList<FaSingleAccountingCheckIn>();
		
		for (FaLoanAccountingEventResult result : eventResultList) {
			
			// TODO 账户序号未设置，目前是记科目账
			FaSingleAccountingCheckIn accountingSingle = SysUtil.getInstance(FaSingleAccountingCheckIn.class);
			accountingSingle.setSys_no(sysNo); // 系统编号
			accountingSingle.setAcct_branch(acctBranch); // 账务机构
			accountingSingle.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT); // 账户分类
			// accountingSingle.setAcct_seq(accountingData.getSub_acct_seq()); //账户序号
			accountingSingle.setCcy_code(result.getTrxn_ccy().toString()); // 货币代码
			accountingSingle.setDebit_credit(result.getDebit_credit()); // 记账方向
			accountingSingle.setAccounting_amt(result.getTran_amount()); // 记账金额
			accountingSingle.setGl_code(result.getGl_code()); // 科目号
			accountingSingle.setRemark("bookProdAccountingSeq"); // 备注
			
			accountingList.add(accountingSingle);
			
		}
		
		bizlog.method(" FaProdBookAccountingSeq.assembAccountingInfo end <<<<<<<<<<<<<<<<");
		
		return accountingList;
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月16日-下午2:07:06</li>
	 *         <li>功能说明：将流水信息转换成解析入参 </li>
	 *         </p>
	 * @param seqList
	 * @return
	 */
	private static Options<FaLoanAccountingEventInfo> assembEventInfo(List<fab_accounting_prod_seq> seqList) {
		
		bizlog.method(" FaProdBookAccountingSeq.assembEventInfo begin >>>>>>>>>>>>>>>>");
		
		ArrayList<FaLoanAccountingEventInfo> eventInfoList = new ArrayList<FaLoanAccountingEventInfo>();
		for (fab_accounting_prod_seq seq : seqList) {
			FaLoanAccountingEventInfo eventInfo = SysUtil.getInstance(FaLoanAccountingEventInfo.class);
			CommUtil.copyProperties(eventInfo, seq);
			eventInfo.setCurrency_code(EnumUtils.toEnum(E_CRCYCD.class, seq.getTrxn_ccy()));
			eventInfo.setTran_amount(seq.getTrxn_amt());
			eventInfoList.add(eventInfo);
		}
		
		bizlog.method(" FaProdBookAccountingSeq.assembEventInfo end <<<<<<<<<<<<<<<<");
		return new DefaultOptions<>(eventInfoList);
	}
	
}
