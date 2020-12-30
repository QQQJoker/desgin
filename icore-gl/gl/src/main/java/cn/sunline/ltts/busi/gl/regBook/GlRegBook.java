package cn.sunline.ltts.busi.gl.regBook;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.account.FaOpenAccount;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountDao;
import cn.sunline.ltts.busi.fa.namedsql.FaRegBookDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_accrue_accountingDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accrue;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accrue_accounting;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_original_voch;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.gl.type.ComFaRegBook.FaAccrueBook;
import cn.sunline.ltts.busi.gl.type.ComFaRegBook.FaAccureInfoms;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCRUETYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;

public class GlRegBook {
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlRegBook.class);

	public static long regOriginalaVochFromAccrue(fab_accrue accureData, long recordNo, long totalCnt) {

		long dataSort = recordNo;
		List<fab_original_voch> vochList = new ArrayList<fab_original_voch>();
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 外系统凭证
		fab_original_voch voch = SysUtil.getInstance(fab_original_voch.class);
		// dataSort++;
		BIZLOG.debug("dataSort[%s]", dataSort);
		voch.setTrxn_date(accureData.getAccrue_date()); // 交易日期
		voch.setSys_no(accureData.getSys_no()); // 系统编号
		voch.setTrxn_seq(runEnvs.getTransq()); // 交易流水
		voch.setData_sort(dataSort); // 数据序号
		voch.setBusi_seq(runEnvs.getBusisq()); // 业务流水
		voch.setAcct_branch(accureData.getAcct_branch()); // 账务机构
		voch.setTrxn_ccy(accureData.getCcy_code()); // 交易币种
		voch.setGl_code(accureData.getAccrue_gl_code()); // 科目号
		if (accureData.getAccrue_type() == E_ACCRUETYPE.DEPOSIT_INTEREST_PAYBLE) {// 借：利息支出
																					// 贷：应付利息
			voch.setDebit_credit(E_DEBITCREDIT.CREDIT); // 记账方向
			
		} else if (accureData.getAccrue_type() == E_ACCRUETYPE.LOAN_INTEREST_RECEIVABLE) {// 借：应计利息
																						// 贷：利息收入
			voch.setDebit_credit(E_DEBITCREDIT.DEBIT); // 记账方向
			
		} else if (accureData.getAccrue_type() == E_ACCRUETYPE.LOAN_LOST_PROVISION) {// 借：减值损失
																					// 贷：减值准备
			voch.setDebit_credit(E_DEBITCREDIT.CREDIT); // 记账方向
			
		} else if (accureData.getAccrue_type() == E_ACCRUETYPE.TAX_SEPARATION) {//营改增价税分离
			
			voch.setDebit_credit(E_DEBITCREDIT.CREDIT); // 记账方向
			
		} else if (accureData.getAccrue_type() == E_ACCRUETYPE.LOAN_INTEREST_O_RECEIVABLE) {//贷款表外计提
		
			voch.setDebit_credit(E_DEBITCREDIT.DEBIT); // 记账方向
			
		}
		voch.setTrxn_amt(accureData.getBudget_inst_amt()); // 交易金额 这里是不是应该是计差额？
		voch.setCorpno(accureData.getCorpno()); // 法人代码
		voch.setRecdver(1L);					// 版本号
		vochList.add(voch);
        
		if(accureData.getAccrue_type() != E_ACCRUETYPE.LOAN_INTEREST_O_RECEIVABLE) {//表外只登记一笔
		    fab_original_voch voch2 = SysUtil.getInstance(fab_original_voch.class);
		    long dataSort2 = dataSort + totalCnt;
		    BIZLOG.debug("dataSort2[%s] totalCnt=[%s]", dataSort2, totalCnt);
		    
		    voch2.setTrxn_date(accureData.getAccrue_date()); // 交易日期
		    voch2.setSys_no(accureData.getSys_no()); // 系统编号
		    voch2.setTrxn_seq(runEnvs.getTransq()); // 交易流水
		    voch2.setData_sort(dataSort2); // 数据序号
		    voch2.setBusi_seq(runEnvs.getBusisq()); // 业务流水
		    voch2.setAcct_branch(accureData.getAcct_branch()); // 账务机构
		    voch2.setTrxn_ccy(accureData.getCcy_code()); // 交易币种
		    voch2.setGl_code(accureData.getOffset_gl_code()); // 对方科目号
		    if (accureData.getAccrue_type() == E_ACCRUETYPE.DEPOSIT_INTEREST_PAYBLE) {
		        voch2.setDebit_credit(E_DEBITCREDIT.DEBIT); // 记账方向
		    } else if (accureData.getAccrue_type() == E_ACCRUETYPE.LOAN_INTEREST_RECEIVABLE) {
		        voch2.setDebit_credit(E_DEBITCREDIT.CREDIT); // 记账方向
		    } else if (accureData.getAccrue_type() == E_ACCRUETYPE.LOAN_LOST_PROVISION) {
		        voch2.setDebit_credit(E_DEBITCREDIT.DEBIT); // 记账方向
		    }else if (accureData.getAccrue_type() == E_ACCRUETYPE.TAX_SEPARATION) {
		        voch2.setDebit_credit(E_DEBITCREDIT.CREDIT); // 记账方向
		    } 
		    voch2.setTrxn_amt(accureData.getBudget_inst_amt()); // 交易金额
		    voch2.setCorpno(accureData.getCorpno()); // 法人代码
		    voch2.setRecdver(1L);  // 版本号
		    vochList.add(voch2);
		}

		DaoUtil.insertBatch(fab_original_voch.class, vochList);

		return dataSort;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月13日-下午6:29:24</li>
	 *         <li>功能说明：存款计提</li>
	 *         </p>
	 * @param dataItem
	 */
	public static void depositAccure(FaAccrueBook dataItem) {

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 计提科目
		String accureSubject = dataItem.getAccrue_gl_code();

		// 取出计提账号信息
		FaAcctInfo accureAcct = FaOpenAccount.getAcctBySubjectAndAcctseq(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), 
				accureSubject, dataItem.getAcct_seq());
		BIZLOG.debug("accureAcct[%s]", accureAcct);

		// 计提账号
		String accureAcctNo = accureAcct.getAcct_no();
		// 已计提金额
		BigDecimal accuredAmt = accureAcct.getAcct_bal();
		BIZLOG.debug("accuredAmt[%s]", accuredAmt);

		// 记账金额
		BigDecimal accountingAmt = dataItem.getBudget_inst_amt().subtract(accuredAmt);
		BIZLOG.debug("accountingAmt[%s]", accountingAmt);

		// 计提方向
		E_DEBITCREDIT accureDirect = E_DEBITCREDIT.CREDIT;

		// 损益科目
		String oppSubject = dataItem.getOffset_gl_code();
		// 取出对方账号信息
		FaAcctInfo oppAcct = FaOpenAccount.getAcctBySubjectAndAcctseq(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), 
				oppSubject, dataItem.getAcct_seq());
		BIZLOG.debug("oppAcct[%s]", oppAcct);

		// 对方账户账号
		String oppAcctNo = oppAcct.getAcct_no();
		// 对方账户记账方向
		E_DEBITCREDIT oppDirect = E_DEBITCREDIT.DEBIT;

		List<FaSingleAccountingCheckIn> accountingDoInList = new ArrayList<FaSingleAccountingCheckIn>();

		// 计提账号记账复合类型
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, accureAcctNo, accureDirect, accountingAmt, "", "depositAccure");

		// 对方账户记账复合类型
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, oppAcctNo, oppDirect, accountingAmt, "", "depositAccure");

		// 登记柜员流水
		String tellerSeq = FaAccounting.getTellerSeq();

		FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
		regTellerSeq.setSys_no(dataItem.getSys_no()); // 系统编号
		regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
		regTellerSeq.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
		regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
		regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
		regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水

		FaAccounting.regTellerSeq(regTellerSeq);

		FaAccounting.bookMultiAccounting(accountingDoInList, tellerSeq, 2L, dataItem.getSys_no(), "", true);

		// 计提种类
		E_ACCRUETYPE accureType = E_ACCRUETYPE.DEPOSIT_INTEREST_PAYBLE;

		// 登记计提记账登记簿
		fab_accrue_accounting accureAccountingTable = SysUtil.getInstance(fab_accrue_accounting.class);

		accureAccountingTable.setSys_no(dataItem.getSys_no()); // 系统编号
		accureAccountingTable.setAccrue_date(dataItem.getAccrue_date()); // 计提日期
		accureAccountingTable.setAccrue_branch(dataItem.getAcct_branch()); // 计提机构
		accureAccountingTable.setCcy_code(dataItem.getCcy_code()); // 货币代码
		accureAccountingTable.setAccrue_type(accureType); // 计提种类
		accureAccountingTable.setAccrue_gl_code(dataItem.getAccrue_gl_code()); // 计提科目
		accureAccountingTable.setAccrue_offset_gl_code(dataItem.getOffset_gl_code()); // 计提对手方科目
		accureAccountingTable.setAccrue_inst_total_amt(dataItem.getBudget_inst_amt()); // 应计提利息总额
		accureAccountingTable.setAccrued_amt(accuredAmt); // 当前已计提金额
		accureAccountingTable.setCurrent_accrue_amt(accountingAmt); // 本次计提金额
		accureAccountingTable.setAccounting_date(runEnvs.getTrandt()); // 记账日期
		accureAccountingTable.setAccounting_seq(tellerSeq); // 记账流水
		accureAccountingTable.setAccrue_acct_no(accureAcctNo); // 计提账号
		accureAccountingTable.setAccrue_opp_acct_no(oppAcctNo); // 对手方账号
		accureAccountingTable.setRecdver(1L);  // 版本号

		Fab_accrue_accountingDao.insert(accureAccountingTable);

		FaRegBookDao.updAccureBook(runEnvs.getCorpno(), dataItem.getSys_no(), dataItem.getAccrue_date(), dataItem.getAccrue_type(), dataItem.getAcct_branch(), dataItem.getAccrue_gl_code(), dataItem.getOffset_gl_code());

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月13日-下午6:29:24</li>
	 *         <li>功能说明：贷款计提</li>
	 *         </p>
	 * @param dataItem
	 */
	public static void loanAccure(FaAccrueBook dataItem) {

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 计提科目
		String accureSubject = dataItem.getAccrue_gl_code();

		// 取出计提账号信息
		FaAcctInfo accureAcct = FaOpenAccount.getAcctBySubjectAndAcctseq(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), 
				accureSubject, dataItem.getAcct_seq());
		BIZLOG.debug("accureAcct[%s]", accureAcct);

		// 计提账号
		String accureAcctNo = accureAcct.getAcct_no();
		// 已计提金额
		BigDecimal accuredAmt = accureAcct.getAcct_bal();
		BIZLOG.debug("accuredAmt[%s]", accuredAmt);

		// 记账金额
		BigDecimal accountingAmt = dataItem.getBudget_inst_amt().subtract(accuredAmt);
		BIZLOG.debug("accountingAmt[%s]", accountingAmt);

		// 计提方向
		E_DEBITCREDIT accureDirect = E_DEBITCREDIT.DEBIT;

		// 对方科目
		String oppSubject = dataItem.getOffset_gl_code();
		// 取出对方账号信息
		FaAcctInfo oppAcct = FaOpenAccount.getAcctBySubjectAndAcctseq(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), 
				oppSubject, dataItem.getAcct_seq());
		BIZLOG.debug("oppAcct[%s]", oppAcct);

		// 损益账户账号
		String oppAcctNo = oppAcct.getAcct_no();
		// 损益账户记账方向
		E_DEBITCREDIT oppDirect = E_DEBITCREDIT.CREDIT;

		// 登记外系统原始凭证，计提科目 在导入时已登记
		// GlRegBook.regOriginalaVoch(dataItem, accountingAmt, accureDirect,
		// oppSubject);
		// 登记外系统原始凭证，损益科目 在导入时已登记
		// GlRegBook.regOriginalaVoch(dataItem, accountingAmt, oppDirect,
		// oppSubject);

		List<FaSingleAccountingCheckIn> accountingDoInList = new ArrayList<FaSingleAccountingCheckIn>();

		// 计提账号记账复合类型
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, accureAcctNo, accureDirect, accountingAmt, "", "loanAccure");

		// 损益账户记账复合类型
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, oppAcctNo, oppDirect, accountingAmt, "", "loanAccure");

		// 登记柜员流水
		String tellerSeq = FaAccounting.getTellerSeq();

		FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
		regTellerSeq.setSys_no(dataItem.getSys_no()); // 系统编号
		regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
		regTellerSeq.setBusi_ref_no(runEnvs.getTransq()); // 业务参考号
		regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
		regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
		regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水

		FaAccounting.regTellerSeq(regTellerSeq);

		FaAccounting.bookMultiAccounting(accountingDoInList, tellerSeq, 2L, dataItem.getSys_no(), "", true);

		// 计提种类
		E_ACCRUETYPE accureType = dataItem.getAccrue_type();

		// 登记计提记账登记簿
		fab_accrue_accounting accureAccountingTable = SysUtil.getInstance(fab_accrue_accounting.class);

		accureAccountingTable.setSys_no(dataItem.getSys_no()); // 系统编号
		accureAccountingTable.setAccrue_date(dataItem.getAccrue_date()); // 计提日期
		accureAccountingTable.setAccrue_branch(dataItem.getAcct_branch()); // 计提机构
		accureAccountingTable.setCcy_code(dataItem.getCcy_code()); // 货币代码
		accureAccountingTable.setAccrue_type(accureType); // 计提种类
		accureAccountingTable.setAccrue_gl_code(dataItem.getAccrue_gl_code()); // 计提科目
		accureAccountingTable.setAccrue_offset_gl_code(dataItem.getOffset_gl_code()); // 计提对手方科目
		accureAccountingTable.setAccrue_inst_total_amt(dataItem.getBudget_inst_amt()); // 应计提利息总额
		accureAccountingTable.setAccrued_amt(accuredAmt); // 当前已计提金额
		accureAccountingTable.setCurrent_accrue_amt(accountingAmt); // 本次计提金额
		accureAccountingTable.setAccounting_date(runEnvs.getTrandt()); // 记账日期
		accureAccountingTable.setAccounting_seq(tellerSeq); // 记账流水
		accureAccountingTable.setAccrue_acct_no(accureAcctNo); // 计提账号
		accureAccountingTable.setAccrue_opp_acct_no(oppAcctNo); // 对手方账号

		Fab_accrue_accountingDao.insert(accureAccountingTable);

		FaRegBookDao.updAccureBook(runEnvs.getCorpno(), dataItem.getSys_no(), dataItem.getAccrue_date(), dataItem.getAccrue_type(), dataItem.getAcct_branch(), dataItem.getAccrue_gl_code(), dataItem.getOffset_gl_code());

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月20日-上午9:47:02</li>
	 *         <li>功能说明：贷款损失准备</li>
	 *         </p>
	 * @param dataItem
	 */
	public static void loanLostProvision(FaAccrueBook dataItem) {

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 计提科目
		String accureSubject = dataItem.getAccrue_gl_code();

		// 取出计提账号信息
		FaAcctInfo accureAcct = FaOpenAccount.getAcctBySubjectAndAcctseq(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), 
				accureSubject, dataItem.getAcct_seq());

		BIZLOG.debug("accureAcct[%s]", accureAcct);

		// 计提账号
		String accureAcctNo = accureAcct.getAcct_no();
		// 已计提金额
		BigDecimal accuredAmt = accureAcct.getAcct_bal();

		BIZLOG.debug("accuredAmt[%s]", accuredAmt);

		// 记账金额
		BigDecimal accountingAmt = dataItem.getBudget_inst_amt().subtract(accuredAmt);
		BIZLOG.debug("accountingAmt[%s]", accountingAmt);

		// 计提方向
		E_DEBITCREDIT accureDirect = E_DEBITCREDIT.CREDIT;

		// 损益科目
		String oppSubject = dataItem.getOffset_gl_code();
		// 取出对方账号信息
		FaAcctInfo oppAcct = FaOpenAccount.getAcctBySubjectAndAcctseq(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), 
				oppSubject,dataItem.getAcct_seq());
		BIZLOG.debug("oppAcct[%s]", oppAcct);

		// 对方账户账号
		String oppAcctNo = oppAcct.getAcct_no();
		// 对方账户记账方向
		E_DEBITCREDIT oppDirect = E_DEBITCREDIT.DEBIT;

		// 登记外系统原始凭证，计提科目 在导入时已登记
		// GlRegBook.regOriginalaVoch(dataItem, accountingAmt, accureDirect,
		// oppSubject);
		// 登记外系统原始凭证，对方科目 在导入时已登记
		// GlRegBook.regOriginalaVoch(dataItem, accountingAmt, oppDirect,
		// oppSubject);

		List<FaSingleAccountingCheckIn> accountingDoInList = new ArrayList<FaSingleAccountingCheckIn>();

		// 计提账号记账复合类型
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, accureAcctNo, accureDirect, accountingAmt, "", "loanAccure");

		// 对方账户记账复合类型
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, oppAcctNo, oppDirect, accountingAmt, "", "loanAccure");

		// 登记柜员流水
		String tellerSeq = FaAccounting.getTellerSeq();

		FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
		regTellerSeq.setSys_no(dataItem.getSys_no()); // 系统编号
		regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
		regTellerSeq.setBusi_ref_no(runEnvs.getTransq()); // 业务参考号
		regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
		regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
		regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水

		FaAccounting.regTellerSeq(regTellerSeq);

		FaAccounting.bookMultiAccounting(accountingDoInList, tellerSeq, 2L, dataItem.getSys_no(), "", true);

		// 计提种类
		E_ACCRUETYPE accureType = dataItem.getAccrue_type();
		;

		// 登记计提记账登记簿
		fab_accrue_accounting accureAccountingTable = SysUtil.getInstance(fab_accrue_accounting.class);

		accureAccountingTable.setSys_no(dataItem.getSys_no()); // 系统编号
		accureAccountingTable.setAccrue_date(dataItem.getAccrue_date()); // 计提日期
		accureAccountingTable.setAccrue_branch(dataItem.getAcct_branch()); // 计提机构
		accureAccountingTable.setCcy_code(dataItem.getCcy_code()); // 货币代码
		accureAccountingTable.setAccrue_type(accureType); // 计提种类
		accureAccountingTable.setAccrue_gl_code(dataItem.getAccrue_gl_code()); // 计提科目
		accureAccountingTable.setAccrue_offset_gl_code(dataItem.getOffset_gl_code()); // 计提对手方科目
		accureAccountingTable.setAccrue_inst_total_amt(dataItem.getBudget_inst_amt()); // 应计提利息总额
		accureAccountingTable.setAccrued_amt(accuredAmt); // 当前已计提金额
		accureAccountingTable.setCurrent_accrue_amt(accountingAmt); // 本次计提金额
		accureAccountingTable.setAccounting_date(runEnvs.getTrandt()); // 记账日期
		accureAccountingTable.setAccounting_seq(tellerSeq); // 记账流水
		accureAccountingTable.setAccrue_acct_no(accureAcctNo); // 计提账号
		accureAccountingTable.setAccrue_opp_acct_no(oppAcctNo); // 对手方账号

		Fab_accrue_accountingDao.insert(accureAccountingTable);

		FaRegBookDao.updAccureBook(runEnvs.getCorpno(), dataItem.getSys_no(), dataItem.getAccrue_date(), dataItem.getAccrue_type(), dataItem.getAcct_branch(), dataItem.getAccrue_gl_code(), dataItem.getOffset_gl_code());

		throw GlError.GL.E0068();

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月20日-上午10:23:32</li>
	 *         <li>功能说明：红字补计提</li>
	 *         </p>
	 * @param branchId
	 */

	public static void redInkAccure(String branchId) {

		// 公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		// 从登记簿中找出计提的科目
		List<FaAccureInfoms> subjectList = FaRegBookDao.lstAccureSubject(runEnvs.getCorpno(), branchId, runEnvs.getTrandt(), false);

		if (subjectList.isEmpty()) {
			BIZLOG.debug("There is no accure provision in the branch[%s] in [%s]", branchId, runEnvs.getTrandt());
			return;
		}

		// 根据科目，开始红字计提
		for (FaAccureInfoms subjectInfo : subjectList) {

			List<faa_account> acctList = FaAccountDao.lstAcctByAccureSubject(runEnvs.getCorpno(), runEnvs.getTrandt(), subjectInfo.getAccrue_gl_code(), false);

			if (acctList.isEmpty() || acctList.size() <= 0) {
				BIZLOG.debug("There is no  account need to be fiexed of subject[%s] in the branch[%s] ", subjectInfo.getAccrue_gl_code(), branchId);
				return;
			}

			// 循环所有的账户
			for (faa_account acctTable : acctList) {

				// 余额方向
				E_DEBITCREDIT balDirect = acctTable.getBal_direction();
				// 计提方向
				E_DEBITCREDIT accountingDirect;
				// 对手方向
				E_DEBITCREDIT oppDirect;

				// 余额方向相反，计提方向相同
				if (balDirect == E_DEBITCREDIT.DEBIT) {
					accountingDirect = E_DEBITCREDIT.CREDIT;
					oppDirect = E_DEBITCREDIT.DEBIT;
				} else {
					accountingDirect = E_DEBITCREDIT.DEBIT;
					oppDirect = E_DEBITCREDIT.CREDIT;
				}

				BigDecimal accountAmt = acctTable.getAcct_bal(); // 账户余额

				// 记账金额，红字计提
				// BigDecimal accountingAmt =
				// BigDecimal.ZERO.subtract(accountAmt);

				// 记账复合类型
				List<FaSingleAccountingCheckIn> accountingDoInList = new ArrayList<FaSingleAccountingCheckIn>();

				FaAccounting.getAccountingCheckByAcct(accountingDoInList, acctTable.getAcct_no(), accountingDirect, accountAmt, null, "redInkAccure");

				// 取对方账户
				FaAcctInfo oppAcctTable = FaOpenAccount.getAcctBySubject(acctTable.getSys_no(), acctTable.getCcy_code(), branchId, subjectInfo.getAccrue_offset_gl_code());

				// 对方账户记账复合类型
				FaAccounting.getAccountingCheckByAcct(accountingDoInList, oppAcctTable.getAcct_no(), oppDirect, accountAmt, null, "redInkAccure");

				// 记账
				FaAccounting.bookMultiAccounting(accountingDoInList, "", 2L, acctTable.getSys_no(), "redInkAccure", true);
			}
		}
	}
	
	/**
	 * @Author songhao
	 *         <p>
	 *         <li>2017年10月22日 05:32:24</li>
	 *         <li>功能说明：营改增入账</li>
	 *         </p>
	 * @param dataItem
	 */
	public static void taxSeparation(FaAccrueBook dataItem) {

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 营改增科目
		String accureSubject = dataItem.getAccrue_gl_code();
		// 取出营改增账号信息
		FaAcctInfo accureAcct = FaOpenAccount.getAcctBySubjectAndAcctseq(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), 
				accureSubject, dataItem.getAcct_seq());
		BIZLOG.debug("accureAcct[%s]", accureAcct);
		// 营改增账号
		String accureAcctNo = accureAcct.getAcct_no();
		// 记账金额
		BigDecimal accountingAmt = dataItem.getBudget_inst_amt().multiply(BigDecimal.valueOf(-1));
		BIZLOG.debug("accountingAmt[%s]", accountingAmt);
		// 计提方向
		E_DEBITCREDIT accureDirect = E_DEBITCREDIT.CREDIT;

		// 对方科目
		String oppSubject = dataItem.getOffset_gl_code();
		// 取出对方账号信息
		FaAcctInfo oppAcct = FaOpenAccount.getAcctBySubjectAndAcctseq(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), 
				oppSubject, dataItem.getAcct_seq());
		BIZLOG.debug("oppAcct[%s]", oppAcct);
		// 对方账号
		String oppAcctNo = oppAcct.getAcct_no();
		// 对方记账金额
		BigDecimal oppAcctAmt = dataItem.getBudget_inst_amt();
		BIZLOG.debug("oppAcctAmt[%s]", oppAcctAmt);
		// 对方记账方向
		E_DEBITCREDIT oppDirect = E_DEBITCREDIT.CREDIT;

		// 登记外系统原始凭证，计提科目 在导入时已登记
		// GlRegBook.regOriginalaVoch(dataItem, accountingAmt, accureDirect,
		// oppSubject);
		// 登记外系统原始凭证，损益科目 在导入时已登记
		// GlRegBook.regOriginalaVoch(dataItem, accountingAmt, oppDirect,
		// oppSubject);

		List<FaSingleAccountingCheckIn> accountingDoInList = new ArrayList<FaSingleAccountingCheckIn>();

		// 营改增账户记账复合类型
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, accureAcctNo, accureDirect, accountingAmt, "", "taxSeparation");

		// 对方账户记账复合类型
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, oppAcctNo, oppDirect, oppAcctAmt, "", "taxSeparation");

		// 登记柜员流水
		String tellerSeq = FaAccounting.getTellerSeq();

		FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
		regTellerSeq.setSys_no(dataItem.getSys_no()); // 系统编号
		regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
		regTellerSeq.setBusi_ref_no(runEnvs.getTransq()); // 业务参考号
		regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
		regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
		regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水

		FaAccounting.regTellerSeq(regTellerSeq);

		FaAccounting.bookMultiAccounting(accountingDoInList, tellerSeq, 2L, dataItem.getSys_no(), "", true);

		// 计提种类
		E_ACCRUETYPE accureType = dataItem.getAccrue_type();

		// 登记计提记账登记簿
		fab_accrue_accounting accureAccountingTable = SysUtil.getInstance(fab_accrue_accounting.class);

		accureAccountingTable.setSys_no(dataItem.getSys_no()); // 系统编号
		accureAccountingTable.setAccrue_date(dataItem.getAccrue_date()); // 计提日期
		accureAccountingTable.setAccrue_branch(dataItem.getAcct_branch()); // 计提机构
		accureAccountingTable.setCcy_code(dataItem.getCcy_code()); // 货币代码
		accureAccountingTable.setAccrue_type(accureType); // 计提种类
		accureAccountingTable.setAccrue_gl_code(dataItem.getAccrue_gl_code()); // 营改增科目
		accureAccountingTable.setAccrue_offset_gl_code(dataItem.getOffset_gl_code()); // 对方科目
		//accureAccountingTable.setAccrue_inst_total_amt(dataItem.getBudget_inst_amt()); 
		//accureAccountingTable.setAccrued_amt(accuredAmt); 
		accureAccountingTable.setCurrent_accrue_amt(accountingAmt); // 本次价税分离金额
		accureAccountingTable.setAccounting_date(runEnvs.getTrandt()); // 记账日期
		accureAccountingTable.setAccounting_seq(tellerSeq); // 记账流水
		accureAccountingTable.setAccrue_acct_no(accureAcctNo); // 营改增账号
		accureAccountingTable.setAccrue_opp_acct_no(oppAcctNo); // 对手方账号

		Fab_accrue_accountingDao.insert(accureAccountingTable);

		FaRegBookDao.updAccureBook(runEnvs.getCorpno(), dataItem.getSys_no(), dataItem.getAccrue_date(), dataItem.getAccrue_type(), dataItem.getAcct_branch(), dataItem.getAccrue_gl_code(), dataItem.getOffset_gl_code());

	}
	/**
	 * 
	 * <p>Title:loanOffBalanceProvision </p>
	 * <p>Description:贷款表外计提 分录：R 表外应收未收利息	</p>
	 * @author wenbo@sunline.cn
	 * @date   2017年10月24日 
	 * @param dataItem
	 */
	public static void loanOffBalanceProvision(FaAccrueBook dataItem){
	    RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

        // 计提科目
        String accureSubject = dataItem.getAccrue_gl_code();

        // 取出计提账号信息
        //FaAcctInfo accureAcct = FaOpenAccount.getAcctBySubject(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), accureSubject);
        FaAcctInfo accureAcct = FaOpenAccount.getAcctBySubjectAndAcctseq(dataItem.getSys_no(), dataItem.getCcy_code(), dataItem.getAcct_branch(), 
                accureSubject, dataItem.getAcct_seq());
        BIZLOG.debug("accureAcct[%s]", accureAcct);

        // 计提账号
        String accureAcctNo = accureAcct.getAcct_no();
        // 已计提金额
        BigDecimal accuredAmt = accureAcct.getAcct_bal();
        BIZLOG.debug("accuredAmt[%s]", accuredAmt);

        // 记账金额
        BigDecimal accountingAmt = dataItem.getBudget_inst_amt().subtract(accuredAmt);
        BIZLOG.debug("accountingAmt[%s]", accountingAmt);

        // 计提方向 收-->借
        E_DEBITCREDIT accureDirect = E_DEBITCREDIT.DEBIT;


        List<FaSingleAccountingCheckIn> accountingDoInList = new ArrayList<FaSingleAccountingCheckIn>();

        // 计提账号记账复合类型
        FaAccounting.getAccountingCheckByAcct(accountingDoInList, accureAcctNo, accureDirect, accountingAmt, "", "loanOffBalanceProvision");

        // 登记柜员流水
        String tellerSeq = FaAccounting.getTellerSeq();

        FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
        regTellerSeq.setSys_no(dataItem.getSys_no()); // 系统编号
        regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
        regTellerSeq.setBusi_ref_no(runEnvs.getTransq()); // 业务参考号
        regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
        regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
        regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水

        FaAccounting.regTellerSeq(regTellerSeq);

        FaAccounting.bookMultiAccounting(accountingDoInList, tellerSeq, 1L, dataItem.getSys_no(), "", true);

        // 计提种类
        E_ACCRUETYPE accureType = dataItem.getAccrue_type();

        // 登记计提记账登记簿
        fab_accrue_accounting accureAccountingTable = SysUtil.getInstance(fab_accrue_accounting.class);

        accureAccountingTable.setSys_no(dataItem.getSys_no()); // 系统编号
        accureAccountingTable.setAccrue_date(dataItem.getAccrue_date()); // 计提日期
        accureAccountingTable.setAccrue_branch(dataItem.getAcct_branch()); // 计提机构
        accureAccountingTable.setCcy_code(dataItem.getCcy_code()); // 货币代码
        accureAccountingTable.setAccrue_type(accureType); // 计提种类
        accureAccountingTable.setAccrue_gl_code(dataItem.getAccrue_gl_code()); // 计提科目
        accureAccountingTable.setAccrue_offset_gl_code(""); // 收付记账
        accureAccountingTable.setAccrue_inst_total_amt(dataItem.getBudget_inst_amt()); // 应计提利息总额
        accureAccountingTable.setAccrued_amt(accuredAmt); // 当前已计提金额
        accureAccountingTable.setCurrent_accrue_amt(accountingAmt); // 本次计提金额
        accureAccountingTable.setAccounting_date(runEnvs.getTrandt()); // 记账日期
        accureAccountingTable.setAccounting_seq(tellerSeq); // 记账流水
        accureAccountingTable.setAccrue_acct_no(accureAcctNo); // 计提账号
        accureAccountingTable.setAccrue_opp_acct_no(""); // 收付记账

        Fab_accrue_accountingDao.insert(accureAccountingTable);

        FaRegBookDao.updAccureBook(runEnvs.getCorpno(), dataItem.getSys_no(), dataItem.getAccrue_date(), dataItem.getAccrue_type(), dataItem.getAcct_branch(), dataItem.getAccrue_gl_code(), dataItem.getOffset_gl_code());
 
	}
}
