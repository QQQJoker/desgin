package cn.sunline.ltts.busi.fa.accounting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.ApBuffer;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.fa.account.FaOpenAccount;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountingDao;
import cn.sunline.ltts.busi.fa.parm.FaAccountingSubject;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.Faa_accountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_set_accounts_detailDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_set_accounts_mainDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_set_accounts_detail;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_set_accounts_main;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTSTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_CASHTRXN;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_EXCHANGEMODE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETACCOUNTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETACCOUNTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SUBJECTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_REBUWA;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaQuerySetAccountsOut;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsDatailDelIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsDetail;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSetAccountsMain;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSummarySetAccountDetail;
import cn.sunline.ltts.sys.dict.GlDict;

/**
 * <p>
 * 文件功能说明：套账申请和录入
 * </p>
 * 
 * @Author Administrator
 *         <p>
 *         <li>2017年3月2日-下午5:13:19</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年3月2日-Administrator：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaSetAccountApply {
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(FaSetAccountApply.class);

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月6日-下午7:27:23</li>
	 *         <li>功能说明：申请套号</li>
	 *         </p>
	 * @param remark
	 * @return
	 */
	public static FaSetAccountsMain applySetAccountsNo(String remark, E_YESORNO subjectAccountingInd) {

		BIZLOG.method("FaSetAccountApply.applySetAccountsNo begin>>>>>>>>>");
		BIZLOG.debug("subjectAccountingInd[%s]", subjectAccountingInd);

		// 科目记账标志不允许为空
		CommTools.fieldNotNull(subjectAccountingInd, GlDict.A.subject_accounting_ind.getId(), GlDict.A.subject_accounting_ind.getLongName());

		// 获取公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		FaSetAccountsMain applyOut = SysUtil.getInstance(FaSetAccountsMain.class);

		fab_set_accounts_main mainTable = SysUtil.getInstance(fab_set_accounts_main.class);

		mainTable.setSys_no(FaConst.GL_SYSTEM); // 系统编号为总账系统编号

		String setAccountNo = geneSetAccountsNo(); // 生成套号
		BIZLOG.debug("setAccountNo[%s]", setAccountNo);

		mainTable.setSet_account_no(setAccountNo); // 套号

		if (runEnvs.getServtp() == BaseEnumType.E_SERVTP.EB) { // 柜面渠道则是手工套账

			mainTable.setSet_account_type(E_SETACCOUNTTYPE.MANUAL_SET_ACCOUNT); // 套帐类别
		} else {

			mainTable.setSet_account_type(E_SETACCOUNTTYPE.SYSTEM_SET_ACCOUNT); // 否则是系统套账
		}
		mainTable.setTrxn_date(runEnvs.getTrandt()); // 交易日期
		mainTable.setTrxn_seq(runEnvs.getTransq()); // 交易流水
		mainTable.setTrxn_branch(runEnvs.getTranbr()); // 交易机构
		mainTable.setTrxn_teller(runEnvs.getTranus()); // 交易柜员
		mainTable.setSet_account_status(E_SETACCOUNTSTATE.RECORDING); // 套账状态
		mainTable.setRemark(remark); // 备注
		mainTable.setSubject_accounting_ind(subjectAccountingInd);

		Fab_set_accounts_mainDao.insert(mainTable);

		applyOut.setSys_no(mainTable.getSys_no()); // 系统编号
		applyOut.setSet_account_no(mainTable.getSet_account_no()); // 套号
		applyOut.setSet_account_type(mainTable.getSet_account_type()); // 套帐类别
		applyOut.setTrxn_date(mainTable.getTrxn_date()); // 交易日期
		applyOut.setTrxn_seq(mainTable.getTrxn_seq()); // 交易流水
		applyOut.setTrxn_branch(mainTable.getTrxn_branch()); // 交易机构
		applyOut.setTrxn_teller(mainTable.getTrxn_teller()); // 交易柜员
		applyOut.setSet_account_status(mainTable.getSet_account_status()); // 套账状态
		applyOut.setPosting_date(mainTable.getPosting_date()); // 入账日期
		applyOut.setPosting_seq(mainTable.getPosting_seq()); // 入账流水
		applyOut.setPosting_branch(mainTable.getPosting_branch()); // 入账机构
		applyOut.setPosting_teller(mainTable.getPosting_teller()); // 入账柜员
		applyOut.setRemark(mainTable.getRemark()); // 备注
		applyOut.setRecdver(mainTable.getRecdver()); // 数据版本号
		applyOut.setSubject_accounting_ind(mainTable.getSubject_accounting_ind());

		BIZLOG.method("FaSetAccountApply.applySetAccountsNo end>>>>>>>>>");
		return applyOut;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月3日-上午9:51:23</li>
	 *         <li>功能说明：生成套号</li>
	 *         </p>
	 * @return
	 */
	private static String geneSetAccountsNo() {
		// ApSeq.clearBuffer(); // 清空缓存区

		ApBuffer.clear();
		String setAccountNo = ApSeq.genSeq("SET_ACCOUNTS_NO"); // 生成内部账号

		BIZLOG.debug("genAccountNo=[%s]", setAccountNo);

		return setAccountNo;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月6日-下午5:30:11</li>
	 *         <li>功能说明：增加明细入账</li>
	 *         </p>
	 * @param setAccountDeatilIn
	 * @param setAccountNo
	 * @return
	 */
	public static void addSetAccountsDetails(FaSetAccountsDetail setAccountDeatilIn, String setAccountNo) {

		BIZLOG.method("FaSetAccountApply.addSetAccountsDetails begin>>>>>>>>>");
		BIZLOG.debug("setAccountDeatilIn[%s]", setAccountDeatilIn);
		
		BIZLOG.debug("********************[%s]",setAccountNo);
		

		// FaSetAccountsDetail setAccountDeatilOut =
		// SysUtil.getInstance(FaSetAccountsDetail.class);

		// 记账方向不能为空
		CommTools.fieldNotNull(setAccountDeatilIn.getDebit_credit(), GlDict.A.debit_credit.getId(), GlDict.A.debit_credit.getLongName());

		// 记账金额不能为空
		CommTools.fieldNotNull(setAccountDeatilIn.getAccounting_amt(), GlDict.A.accounting_amt.getId(), GlDict.A.accounting_amt.getLongName());

		// 账务机构不能为空
		CommTools.fieldNotNull(setAccountDeatilIn.getAcct_branch(), GlDict.A.acct_branch.getId(), GlDict.A.acct_branch.getLongName());

		// 摘要代码不能为空
		CommTools.fieldNotNull(setAccountDeatilIn.getSummary_code(), GlDict.A.summary_code.getId(), GlDict.A.summary_code.getLongName());

		// 货币代码
		CommTools.fieldNotNull(setAccountDeatilIn.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		
		// 红蓝字
		CommTools.fieldNotNull(setAccountDeatilIn.getRebuwa(), GlDict.A.rebuwa.getId(), GlDict.A.rebuwa.getLongName());

		// 获取公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 如果套号为空，则报错
		if (CommUtil.isNull(setAccountNo)) {
			// throw GlError.GL.E0026(GlDict.A.set_account_no.getLongName());
			throw ApPubErr.APPUB.E0001(GlDict.A.set_account_no.getId(), GlDict.A.set_account_no.getLongName());
		}

		// 检查套号是否已存在
		fab_set_accounts_main acctMainTable = Fab_set_accounts_mainDao.selectOne_odb1(setAccountNo, false);
		
		
		if (acctMainTable == null) {
			throw GlError.GL.E0027(setAccountNo);
		}

		// 检查套号状态
		if (acctMainTable.getSet_account_status() != E_SETACCOUNTSTATE.RECORDING) {
			throw GlError.GL.E0028(setAccountNo, acctMainTable.getSet_account_status().getLongName());
		}

		// 判断是否是记账机构
		if (CommUtil.compare(acctMainTable.getTrxn_branch(), runEnvs.getTranbr()) != 0) {
			throw GlError.GL.E0029(acctMainTable.getTrxn_branch());
		}

		// 套号申请与明细录入必须是同一个柜员
		if (CommUtil.compare(acctMainTable.getTrxn_teller(), runEnvs.getTranus()) != 0) {
			throw GlError.GL.E0030(acctMainTable.getTrxn_teller());
		}
		
		// 科目记账，请输入科目
		if (acctMainTable.getSubject_accounting_ind() == E_YESORNO.YES) {
			CommTools.fieldNotNull(setAccountDeatilIn.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
			
		} else {
			CommTools.fieldNotNull(setAccountDeatilIn.getAcct_no(), GlDict.A.acct_no.getId(), GlDict.A.acct_no.getLongName());

		}
		// 交易金额大于0
		if (CommUtil.compare(setAccountDeatilIn.getAccounting_amt(), new BigDecimal(0)) <= 0) {
			throw GlError.GL.E0041();
		}

		faa_account acctTable = SysUtil.getInstance(faa_account.class);

		// 账号记账
		if (acctMainTable.getSubject_accounting_ind() == E_YESORNO.NO) {

			acctTable = Faa_accountDao.selectOne_odb1(setAccountDeatilIn.getAcct_no(), false);
			if (acctTable == null) {
				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(faa_account.class).getLongname(), GlDict.A.acct_no.getLongName(), setAccountDeatilIn.getAcct_no());
			}
			//获取科目信息
			fap_accounting_subject acctSubject = Fap_accounting_subjectDao.selectOne_odb1(acctTable.getGl_code(), false);
			if(acctSubject == null){
			    throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), acctTable.getGl_code());
			}
			setAccountDeatilIn.setGl_code_desc(acctSubject.getGl_code_desc());// 设置科目名称
			
			if (CommUtil.compare(acctTable.getCcy_code(), setAccountDeatilIn.getCcy_code()) != 0) {
				throw GlError.GL.E0049(setAccountDeatilIn.getCcy_code(), acctTable.getAcct_no(), acctTable.getCcy_code());
			}
		} else { // 根据科目号找基准账户

			FaAcctInfo openIn = SysUtil.getInstance(FaAcctInfo.class);
			FaAcctInfo openOut = SysUtil.getInstance(FaAcctInfo.class);
			openIn.setSys_no(FaConst.GL_SYSTEM); // 系统编号
			openIn.setCcy_code(setAccountDeatilIn.getCcy_code()); // 货币代码
			openIn.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT); // 账户分类
			openIn.setAcct_branch(setAccountDeatilIn.getAcct_branch()); // 财务机构
			openIn.setGl_code(setAccountDeatilIn.getGl_code()); // 科目号
			openIn.setOn_bal_sheet_ind(E_YESORNO.YES); // 表内标志
			openIn.setOpen_acct_ind(E_YESORNO.YES);

			openOut = FaOpenAccount.openBaseAccount(openIn);

			FaAccounting.getAcctTable(acctTable, openOut);
		}

		BIZLOG.debug("acctTable[%s]", acctTable);

		// 账号必须是表内记账
		if (acctTable.getOn_bal_sheet_ind() == E_YESORNO.NO) {
			throw GlError.GL.E0032(setAccountDeatilIn.getAcct_no());
		}

		// 账户货币代码和录入货币代码必须一致
		if (CommUtil.compare(acctTable.getCcy_code(), setAccountDeatilIn.getCcy_code()) != 0) {
			throw GlError.GL.E0049(setAccountDeatilIn.getCcy_code(), setAccountDeatilIn.getAcct_no(), acctTable.getCcy_code());
		}
		
		
		
		// 检查账户状态
		if (acctTable.getAcct_status() == E_ACCTSTATUS.CLOSE) {
			throw GlError.GL.E0034(setAccountDeatilIn.getAcct_no());
		}

		if (CommUtil.compare(FaConst.GL_SYSTEM, acctTable.getSys_no()) != 0) {
			throw GlError.GL.E0036(acctTable.getSys_no(), acctTable.getAcct_no());
		}

		// 判断科目是否可以记账
		FaAccountingSubject.checkSubjectAccounting(acctTable.getGl_code(), acctTable.getSys_no());

		// 取最大的序号
		Long dataSort = FaAccountingDao.selMaxSortBySetNo(CommToolsAplt.prcRunEnvs().getCorpno(), setAccountNo, false);
		dataSort++;
		fab_set_accounts_detail detailTable = SysUtil.getInstance(fab_set_accounts_detail.class);

		detailTable.setSet_account_no(setAccountNo); // 套号
		detailTable.setData_sort(dataSort); // 数据序号
		detailTable.setGl_code(acctTable.getGl_code()); // 科目
		detailTable.setGl_code_desc(setAccountDeatilIn.getGl_code_desc());//科目名称
		detailTable.setAcct_no(acctTable.getAcct_no()); // 账号
		detailTable.setAcct_name(acctTable.getAcct_name());//账户名称
		detailTable.setAcct_branch(acctTable.getAcct_branch());// 机构
		detailTable.setCcy_code(acctTable.getCcy_code()); // 货币代码
		detailTable.setDebit_credit(setAccountDeatilIn.getDebit_credit()); // 记账方向
		detailTable.setAccounting_amt(setAccountDeatilIn.getAccounting_amt()); // 记账金额

		detailTable.setRebuwa(setAccountDeatilIn.getRebuwa());//设置红蓝字标志
		
		detailTable.setExchg_method(E_EXCHANGEMODE.SPOT_EXCHANGE_RATE); // 拆算方式
																		// TODO
																		// 待定
		detailTable.setExchg_org_ccy_amt(setAccountDeatilIn.getExchg_org_ccy_amt()); // 折本金额
		detailTable.setExchg_dol_ccy_amt(setAccountDeatilIn.getExchg_dol_ccy_amt()); // 折美金额
		detailTable.setTrxn_offset_acct_no(setAccountDeatilIn.getTrxn_offset_acct_no()); // 交易对手账号
		detailTable.setTrxn_offset_acct_name(setAccountDeatilIn.getTrxn_offset_acct_name()); // 交易对手户名
		detailTable.setCash_trxn_ind(E_CASHTRXN.TRXN); // 现转标志
		detailTable.setSummary_code(setAccountDeatilIn.getSummary_code()); // 摘要代码
		// ApSmryTools.exists(setAccountDeatilIn.getSummary_code(), true);
		detailTable.setSummary_desc(ApSmryTools.getText(setAccountDeatilIn.getSummary_code())); // 摘要描述
		detailTable.setRemark(setAccountDeatilIn.getRemark()); // 备注

		// 校验金额小数位
		CommTools.validAmount(detailTable.getCcy_code(), setAccountDeatilIn.getAccounting_amt());

		// 登记套账明细簿
		Fab_set_accounts_detailDao.insert(detailTable);
		BIZLOG.debug("detailTable[%s]", detailTable);
		// 登记柜员流水
		FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);

		regTellerSeq.setSys_no(acctTable.getSys_no()); // 系统编号
		regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.NON_ACCOUNTING); // 交易流水类型
		regTellerSeq.setTrxn_subject(detailTable.getSet_account_no()); // 交易主体
		// regTellerSeq.setBusi_ref_no(); //业务参考号
		regTellerSeq.setRemark(detailTable.getRemark()); // 备注
		regTellerSeq.setSett_status(E_SETTSTATE.NONE); // 清算
		// regTellerSeq.setSett_batch_no(); //清算批次号
		regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
		// regTellerSeq.setOriginal_trxn_date(); //原日期
		// regTellerSeq.setOriginal_trxn_seq(); //原交易流水
		// regTellerSeq.setCheck_teller(); //复核柜员
		regTellerSeq.setTrxn_seq(FaAccounting.getTellerSeq());
		FaAccounting.regTellerSeq(regTellerSeq);

		// setAccountDeatilOut.setSet_account_no(detailTable.getSet_account_no());
		// // 套号
		// setAccountDeatilOut.setData_sort(detailTable.getData_sort()); // 数据序号
		// setAccountDeatilOut.setAcct_no(detailTable.getAcct_no()); // 账号
		// setAccountDeatilOut.setCcy_code(detailTable.getCcy_code()); // 货币代码
		// setAccountDeatilOut.setDebit_credit(detailTable.getDebit_credit());
		// // 记账方向
		// setAccountDeatilOut.setAccounting_amt(detailTable.getAccounting_amt());
		// // 记账金额
		// setAccountDeatilOut.setGl_code(acctTable.getGl_code()); // 科目号
		// setAccountDeatilOut.setAcct_branch(acctTable.getAcct_branch()); //
		// 账务机构
		// setAccountDeatilOut.setExchg_mode(detailTable.getExchg_mode()); //
		// 拆算方式
		// setAccountDeatilOut.setExchg_org_ccy_amt(detailTable.getExchg_org_ccy_amt());
		// // 折本金额
		// setAccountDeatilOut.setExchg_dol_ccy_amt(detailTable.getExchg_dol_ccy_amt());
		// // 折美金额
		// setAccountDeatilOut.setTrxn_opp_acct_no(detailTable.getTrxn_opp_acct_no());
		// // 交易对手账号
		// setAccountDeatilOut.setTrxn_opp_acct_name(detailTable.getTrxn_opp_acct_name());
		// // 交易对手户名
		// setAccountDeatilOut.setCash_trxn_ind(detailTable.getCash_trxn_ind());
		// // 现转标志
		// setAccountDeatilOut.setSummary_code(detailTable.getSummary_code());
		// // 摘要代码
		// setAccountDeatilOut.setSummary_name(detailTable.getSummary_name());
		// // 摘要描述
		// setAccountDeatilOut.setRemark(detailTable.getRemark()); // 备注

		// BIZLOG.debug("setAccountDeatilOut[%s]", setAccountDeatilOut);
		BIZLOG.method("FaSetAccountApply.addSetAccountsDetails  end >>>>>>>>");
		// return setAccountDeatilOut;

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月9日-下午5:34:07</li>
	 *         <li>功能说明：套平入账</li>
	 *         </p>
	 * @param setAccountNo
	 * @return
	 */
	public static FaSetAccountsMain bookSetAccount(String setAccountNo) {

		BIZLOG.method("FaSetAccountApply.addSetAccountsDetails  begin >>>>>>>>");

		// 公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		List<fab_set_accounts_detail> setAccountsDetail = FaAccountingDao.lstSetDetailByNo(CommToolsAplt.prcRunEnvs().getCorpno(), setAccountNo, false);

		// 明细为空报错
		if (setAccountsDetail.isEmpty()) {
			throw GlError.GL.E0046(setAccountNo);
		}

		long totalCount = setAccountsDetail.size();

		BIZLOG.debug("setAccountsDetail[%s],totalCount[%s]", setAccountsDetail, totalCount);

		// 取出主套号信息,为空报错
		fab_set_accounts_main mainTable = Fab_set_accounts_mainDao.selectOne_odb1(setAccountNo, false);
		if (CommUtil.isNull(mainTable)) {
			throw GlError.GL.E0027(setAccountNo);
		}

		// 复核柜员和录入柜员是同一个，报错
		if (CommUtil.compare(runEnvs.getTranus(), mainTable.getTrxn_teller()) == 0) {
			throw GlError.GL.E0047(runEnvs.getTranus(), mainTable.getTrxn_teller());
		}

		// 套号状态不是录入，报错
		if (mainTable.getSet_account_status() != E_SETACCOUNTSTATE.RECORDING) {
			throw GlError.GL.E0028(setAccountNo, mainTable.getSet_account_status().getLongName());
		}

		// 交易机构不是同一个，报错
		if (CommUtil.compare(mainTable.getTrxn_branch(), runEnvs.getTranbr()) != 0) {
			throw GlError.GL.E0029(mainTable.getTrxn_branch());
		}

		// 取出账套汇总信息，判断是否借贷平衡 
		List<FaSummarySetAccountDetail> summarySetDetailList = FaAccountingDao.lstSummaySetAccountDetail(CommToolsAplt.prcRunEnvs().getCorpno(), setAccountNo, false);
		for (FaSummarySetAccountDetail summarySetDetail : summarySetDetailList) {
			if (CommUtil.compare(summarySetDetail.getDebit_creit_margin(), BigDecimal.ZERO) != 0) {
				throw GlError.GL.E0048(setAccountNo, summarySetDetail.getCcy_code(), summarySetDetail.getDebit_amt(), summarySetDetail.getCredit_amt());
			}
		}

		// 登记柜员流水
		String tellerSeq = FaAccounting.getTellerSeq();

		FaRegTellerSeq regTellerSeqIn = SysUtil.getInstance(FaRegTellerSeq.class);
		regTellerSeqIn.setSys_no(FaConst.GL_SYSTEM); // 系统编号
		regTellerSeqIn.setTrxn_seq_type(E_TRXNSEQTYPE.MANUAL_ACCOUNTING); // 交易流水类型
		regTellerSeqIn.setTrxn_subject(setAccountNo); // 交易主体
		regTellerSeqIn.setRemark(mainTable.getRemark()); // 备注
		regTellerSeqIn.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
		regTellerSeqIn.setReversal_status(E_REVERSALSTATE.NOMARL); // 冲账状态
		regTellerSeqIn.setReview_teller(runEnvs.getTranus());
		regTellerSeqIn.setTrxn_seq(tellerSeq);

		FaAccounting.regTellerSeq(regTellerSeqIn);

		List<FaSingleAccountingCheckIn> accountingDoInList = new ArrayList<FaSingleAccountingCheckIn>();

		for (fab_set_accounts_detail setAccounts : setAccountsDetail) {

			FaSingleAccountingCheckIn accountingDoIn = SysUtil.getInstance(FaSingleAccountingCheckIn.class);

			//损益类科目非年结当天不允许手工记账
			checkProfitIsYearEnd(setAccounts);
			
			accountingDoIn.setAcct_no(setAccounts.getAcct_no()); // 账号
			//如果为红字记账则取负数
			if(setAccounts.getRebuwa()==E_REBUWA.R){
			    setAccounts.setAccounting_amt(setAccounts.getAccounting_amt().negate());
			}
			accountingDoIn.setAccounting_amt(setAccounts.getAccounting_amt()); // 记账金额
			accountingDoIn.setDebit_credit(setAccounts.getDebit_credit()); // 记账方向
			accountingDoIn.setSys_no(mainTable.getSys_no()); // 系统编号
			accountingDoIn.setRemark(setAccounts.getRemark()); // 备注
			accountingDoIn.setSummary_code(setAccounts.getSummary_code());

			accountingDoInList.add(accountingDoIn);

		}

		// 多笔记账
		FaAccounting.bookMultiAccounting(accountingDoInList, tellerSeq, totalCount, mainTable.getSys_no(), mainTable.getRemark(), true);

		// 更新套账主簿
		FaSetAccountsMain setAccountOut = SysUtil.getInstance(FaSetAccountsMain.class);

		mainTable.setSet_account_status(E_SETACCOUNTSTATE.RECORDING_AND_CHECKING); // 套账状态
		mainTable.setPosting_date(runEnvs.getTrandt()); // 入账日期
		mainTable.setPosting_seq(runEnvs.getTransq()); // 入账流水
		mainTable.setPosting_branch(runEnvs.getTranbr()); // 入账机构
		mainTable.setPosting_teller(runEnvs.getTranus()); // 入账柜员

		Fab_set_accounts_mainDao.updateOne_odb1(mainTable);

		setAccountOut.setSys_no(mainTable.getSys_no()); // 系统编号
		setAccountOut.setSet_account_no(mainTable.getSet_account_no()); // 套号
		setAccountOut.setSet_account_type(mainTable.getSet_account_type()); // 套帐类别
		setAccountOut.setTrxn_date(mainTable.getTrxn_date()); // 交易日期
		setAccountOut.setTrxn_seq(mainTable.getTrxn_seq()); // 交易流水
		setAccountOut.setTrxn_branch(mainTable.getTrxn_branch()); // 交易机构
		setAccountOut.setTrxn_teller(mainTable.getTrxn_teller()); // 交易柜员
		setAccountOut.setSet_account_status(mainTable.getSet_account_status()); // 套账状态
		setAccountOut.setPosting_date(mainTable.getPosting_date()); // 入账日期
		setAccountOut.setPosting_seq(mainTable.getPosting_seq()); // 入账流水
		setAccountOut.setPosting_branch(mainTable.getPosting_branch()); // 入账机构
		setAccountOut.setPosting_teller(mainTable.getPosting_teller()); // 入账柜员
		setAccountOut.setRemark(mainTable.getRemark()); // 备注
		setAccountOut.setSubject_accounting_ind(mainTable.getSubject_accounting_ind()); // 科目记账标志

		return setAccountOut;
	}

	/**  
    * @Title: checkProfitIsYearEnd  
    * @Description: 损益类科目非年结当天不允许手工记账  
    * @Author xionglz
    * @param @param setAccounts     
    * @return void    返回类型  
    * @throws  
    */  
    private static void checkProfitIsYearEnd(fab_set_accounts_detail setAccounts) {
        //根据科目号查询科目信息表
        fap_accounting_subject tblfapaccountsubject = Fap_accounting_subjectDao.selectOne_odb1
                                                                        (setAccounts.getGl_code(), false);
        //如果科目类型为损益类且日期不为年结
        if(tblfapaccountsubject.getGl_code_type()==E_SUBJECTTYPE.PEOFIT_AND_LOSS
                   &&!DateTools2.isLastDay("Y", CommToolsAplt.prcRunEnvs().getTrandt())){
            throw GlError.GL.E0209(setAccounts.getGl_code());
        }
        
    }

    /**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月8日-上午11:22:47</li>
	 *         <li>功能说明：套账明细删除</li>
	 *         </p>
	 * @param detailDelIn
	 */
	public static void delSetAccountDetail(FaSetAccountsDatailDelIn detailDelIn) {

		BIZLOG.method("FaSetAccountApply.delSetAccountDetail>>>>>>begin");
		BIZLOG.debug("detailDelIn[%s]", detailDelIn);

		// 公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 如果套号为空，则报错
		if (CommUtil.isNull(detailDelIn.getSet_account_no())) {
			// throw GlError.GL.E0026(GlDict.A.set_account_no.getLongName());
			throw ApPubErr.APPUB.E0001(GlDict.A.set_account_no.getId(), GlDict.A.set_account_no.getLongName());
		}

		// 检查套号是否已存在
		fab_set_accounts_main acctMainTable = Fab_set_accounts_mainDao.selectOne_odb1(detailDelIn.getSet_account_no(), false);
		if (acctMainTable == null) {
			throw GlError.GL.E0027(detailDelIn.getSet_account_no());
		}

		// 检查套号状态
		if (acctMainTable.getSet_account_status() != E_SETACCOUNTSTATE.RECORDING) {
			throw GlError.GL.E0028(detailDelIn.getSet_account_no(), acctMainTable.getSet_account_status().getLongName());
		}

		// 判断是否是记账机构
		if (CommUtil.compare(acctMainTable.getTrxn_branch(), runEnvs.getTranbr()) != 0) {
			throw GlError.GL.E0029(acctMainTable.getTrxn_branch());
		}

		// 套号申请与明删除必须是同一个柜员
		if (CommUtil.compare(acctMainTable.getTrxn_teller(), runEnvs.getTranus()) != 0) {
			throw GlError.GL.E0030(acctMainTable.getTrxn_teller());
		}

		fab_set_accounts_detail detailTable = Fab_set_accounts_detailDao.selectOne_odb1(detailDelIn.getSet_account_no(), detailDelIn.getData_sort(), false);
		if (detailTable == null) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fab_set_accounts_detail.class).getLongname(), GlDict.A.set_account_no.getLongName() + "-" + GlDict.A.data_sort.getLongName(),
					detailDelIn.getSet_account_no() + "-" + detailDelIn.getData_sort());

		}

		BIZLOG.debug("detailTable[%s]", detailTable);
		// 对比版本号
		if (CommUtil.compare(detailDelIn.getRecdver(), detailTable.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fab_set_accounts_detail.class).getName());
		}

		Fab_set_accounts_detailDao.deleteOne_odb1(detailDelIn.getSet_account_no(), detailDelIn.getData_sort());

		// 登记柜员流水
		FaRegTellerSeq regTellerSeqIn = SysUtil.getInstance(FaRegTellerSeq.class);

		regTellerSeqIn.setSys_no(FaConst.GL_SYSTEM); // 系统编号
		regTellerSeqIn.setTrxn_seq_type(E_TRXNSEQTYPE.NON_ACCOUNTING); // 交易流水类型
		regTellerSeqIn.setTrxn_subject(detailDelIn.getSet_account_no()); // 交易主体
		regTellerSeqIn.setSett_status(E_SETTSTATE.NONE); // 清算
		regTellerSeqIn.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
		regTellerSeqIn.setTrxn_seq(CommTools.prcRunEnvs().getTransq());
		FaAccounting.regTellerSeq(regTellerSeqIn);

		BIZLOG.method("FaSetAccountApply.delSetAccountDetail>>>>>end>>>>>");

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月8日-上午11:51:15</li>
	 *         <li>功能说明：套账作废</li>
	 *         </p>
	 * @param setAccountNo
	 * @param dataSort
	 */
	public static void invalidSetAcccounts(String setAccountNo, Long versionData) {

		BIZLOG.method("FaSetAccountApply.invalidSetAcccounts>>>>>>begin");
		BIZLOG.debug("setAccountNo[%s]", setAccountNo);

		// 公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		// 如果套号为空，则报错
		if (CommUtil.isNull(setAccountNo)) {
			// throw GlError.GL.E0026(GlDict.A.set_account_no.getLongName());
			throw ApPubErr.APPUB.E0001(GlDict.A.set_account_no.getId(), GlDict.A.set_account_no.getLongName());
		}

		// 检查套号是否已存在
		fab_set_accounts_main acctMainTable = Fab_set_accounts_mainDao.selectOne_odb1(setAccountNo, false);

		if (acctMainTable == null) {
			throw GlError.GL.E0027(setAccountNo);
		}

		// 检查版本号
		if (CommUtil.compare(versionData, acctMainTable.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fab_set_accounts_main.class).getName());
		}
		// 检查套号状态
		if (acctMainTable.getSet_account_status() != E_SETACCOUNTSTATE.RECORDING) {
			throw GlError.GL.E0028(setAccountNo, acctMainTable.getSet_account_status().getLongName());
		}

		// 判断是否是记账机构
		if (CommUtil.compare(acctMainTable.getTrxn_branch(), runEnvs.getTranbr()) != 0) {
			throw GlError.GL.E0029(acctMainTable.getTrxn_branch());
		}

		// 套号申请与明删除必须是同一个柜员
		if (CommUtil.compare(acctMainTable.getTrxn_teller(), runEnvs.getTranus()) != 0) {
			throw GlError.GL.E0030(acctMainTable.getTrxn_teller());
		}

		// 账套状态设为无效
		acctMainTable.setSet_account_status(E_SETACCOUNTSTATE.INVALID);

		Fab_set_accounts_mainDao.updateOne_odb1(acctMainTable);

		// 登记柜员流水
		FaRegTellerSeq regTellerSeqIn = SysUtil.getInstance(FaRegTellerSeq.class);
		regTellerSeqIn.setSys_no(FaConst.GL_SYSTEM); // 系统编号
		regTellerSeqIn.setTrxn_seq_type(E_TRXNSEQTYPE.NON_ACCOUNTING); // 交易流水类型
		regTellerSeqIn.setTrxn_subject(acctMainTable.getSet_account_no()); // 交易主体
		regTellerSeqIn.setSett_status(E_SETTSTATE.NONE); // 清算
		regTellerSeqIn.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
		regTellerSeqIn.setTrxn_seq(CommTools.prcRunEnvs().getTransq());
		FaAccounting.regTellerSeq(regTellerSeqIn);

		BIZLOG.method("FaSetAccountApply.invalidSetAcccounts>>>>>>end");

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月9日-上午10:58:08</li>
	 *         <li>功能说明：查询套账明细</li>
	 *         </p>
	 * @param set_account_no
	 * @return
	 */
	public static FaQuerySetAccountsOut querySetAccountsDeitail(String setAccountNo) {

		FaQuerySetAccountsOut setAccountsQueryOut = SysUtil.getInstance(FaQuerySetAccountsOut.class);

		// 检查套号是否已存在
		fab_set_accounts_main acctMainTable = Fab_set_accounts_mainDao.selectOne_odb1(setAccountNo, false);

		if (acctMainTable == null) {
			throw GlError.GL.E0027(setAccountNo);
		}

		setAccountsQueryOut.setSys_no(acctMainTable.getSys_no()); // 系统编号
		setAccountsQueryOut.setSet_account_no(setAccountNo); // 套号
		setAccountsQueryOut.setSet_account_type(acctMainTable.getSet_account_type()); // 套帐类别
		setAccountsQueryOut.setSubject_accounting_ind(acctMainTable.getSubject_accounting_ind()); // 科目记账标志
		setAccountsQueryOut.setTrxn_date(acctMainTable.getTrxn_date()); // 交易日期
		setAccountsQueryOut.setTrxn_seq(acctMainTable.getTrxn_seq()); // 交易流水
		setAccountsQueryOut.setTrxn_branch(acctMainTable.getTrxn_branch()); // 交易机构
		setAccountsQueryOut.setTrxn_teller(acctMainTable.getTrxn_teller()); // 交易柜员
		setAccountsQueryOut.setSet_account_status(acctMainTable.getSet_account_status()); // 套账状态
		setAccountsQueryOut.setPosting_date(acctMainTable.getPosting_date()); // 入账日期
		setAccountsQueryOut.setPosting_seq(acctMainTable.getPosting_seq()); // 入账流水
		setAccountsQueryOut.setPosting_branch(acctMainTable.getPosting_branch()); // 入账机构
		setAccountsQueryOut.setPosting_teller(acctMainTable.getPosting_teller()); // 入账柜员
		setAccountsQueryOut.setRemark(acctMainTable.getRemark()); // 备注
		setAccountsQueryOut.setRecdver(acctMainTable.getRecdver()); // 数据版本号

		List<fab_set_accounts_detail> detailTableList = FaAccountingDao.lstSetDetailByNo(CommToolsAplt.prcRunEnvs().getCorpno(), setAccountNo, false);
		if (detailTableList.isEmpty()) {
			throw GlError.GL.E0046(setAccountNo);
		}
		List<FaSetAccountsDetail> setAccountsDetailListOut = new ArrayList<FaSetAccountsDetail>();

		for (fab_set_accounts_detail detailTable : detailTableList) {
		    
		    //查询总账分户账表信息
//		    faa_account tblFaa_account = Faa_accountDao.selectOne_odb1(detailTable.getAcct_no(), false);
//		    
//		    if(tblFaa_account == null){
//		        throw ApPubErr.APPUB.E0005(OdbFactory.getTable(faa_account.class).getLongname(), GlDict.A.acct_no.getLongName(), detailTable.getAcct_no());
//		    }
		    
			FaSetAccountsDetail setAccountsDetailOut = SysUtil.getInstance(FaSetAccountsDetail.class);
			
			setAccountsDetailOut.setSet_account_no(detailTable.getSet_account_no());
			setAccountsDetailOut.setData_sort(detailTable.getData_sort()); // 数据序号
			setAccountsDetailOut.setAcct_no(detailTable.getAcct_no()); // 账号
			setAccountsDetailOut.setCcy_code(detailTable.getCcy_code()); // 货币代码
			setAccountsDetailOut.setDebit_credit(detailTable.getDebit_credit()); // 记账方向
			setAccountsDetailOut.setAccounting_amt(detailTable.getAccounting_amt()); // 记账金额
			setAccountsDetailOut.setGl_code(detailTable.getGl_code()); // 科目号
			setAccountsDetailOut.setAcct_branch(detailTable.getAcct_branch()); // 账务机构
			setAccountsDetailOut.setExchg_method(detailTable.getExchg_method()); // 拆算方式
			setAccountsDetailOut.setExchg_org_ccy_amt(detailTable.getExchg_org_ccy_amt()); // 折本金额
			setAccountsDetailOut.setExchg_dol_ccy_amt(detailTable.getExchg_dol_ccy_amt()); // 折美金额
			setAccountsDetailOut.setTrxn_offset_acct_no(detailTable.getTrxn_offset_acct_no()); // 交易对手账号
			setAccountsDetailOut.setTrxn_offset_acct_name(detailTable.getTrxn_offset_acct_name()); // 交易对手户名
			setAccountsDetailOut.setCash_trxn_ind(detailTable.getCash_trxn_ind()); // 现转标志
			setAccountsDetailOut.setSummary_code(detailTable.getSummary_code()); // 摘要代码
			setAccountsDetailOut.setSummary_desc(detailTable.getSummary_desc()); // 摘要描述
			setAccountsDetailOut.setRemark(detailTable.getRemark()); // 备注
			setAccountsDetailOut.setRecdver(detailTable.getRecdver()); // 数据版本号
			setAccountsDetailOut.setRebuwa(detailTable.getRebuwa()); // 红蓝字
			setAccountsDetailOut.setAcct_name(detailTable.getAcct_name());//账户名称
			setAccountsDetailOut.setGl_code_desc(detailTable.getGl_code_desc());//科目名称
			
			setAccountsDetailListOut.add(setAccountsDetailOut);
			
		}

		Options<FaSetAccountsDetail> setAccountsDetailOptionsOut = new DefaultOptions<FaSetAccountsDetail>();
		setAccountsDetailOptionsOut.setValues(setAccountsDetailListOut);
		if (CommUtil.isNotNull(setAccountsDetailListOut)) {
			CommToolsAplt.prcRunEnvs().setCounts(Long.valueOf(setAccountsDetailListOut.size()));
		}
		setAccountsQueryOut.setSetAccountsDetail(setAccountsDetailOptionsOut); // 套账明细

		BIZLOG.debug("*********查询结果*********[%s]", setAccountsQueryOut);
		
		return setAccountsQueryOut;
	}
	
	
	/**
	 * <p>	
	 * <li>根据开始时间、截止时间、交易柜员...查询套账明细</li>
	 * </p>
	 * @author meizhian
	 * @date   2018年1月15日 
	 * @param input
	 * @param output
	 */
	public static void  querySetAccountsDeitails( final cn.sunline.ltts.gl.fa.servicetype.SrvFaAccounting.querySetAccountsDeitails.Input input,  final cn.sunline.ltts.gl.fa.servicetype.SrvFaAccounting.querySetAccountsDeitails.Output output){
	    BIZLOG.method("FaQueryAccount.querySetAccountsDeitails begin>>>>>>>>>>>>>>>>>>");
	    //获取公共运行变量
	    RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
	   
	    //获取符合条件的所有明细
	    long pageno = runEnvs.getPageno();
	    long pgsize = runEnvs.getPgsize();
	    Page<FaQuerySetAccountsOut> detailList = FaAccountingDao.lstSetDetailsByData(
	            input.getStart_date(), input.getEnd_date(), input.getTrxn_teller(), 
	            input.getGl_code(), input.getAcct_no(), input.getSet_account_no(), input.getAccounting_amt(),
	            input.getCcy_code(), runEnvs.getCorpno(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
	    
	    
	    BIZLOG.debug("**********[%s]", detailList);
	    
	    runEnvs.setCounts(detailList.getRecordCount());
	    Options<FaQuerySetAccountsOut> queryOut = new DefaultOptions<FaQuerySetAccountsOut>();
	    queryOut.setValues(detailList.getRecords());
	    //设置输出参数
	    output.setSetDetailOut(queryOut);
	    
	    BIZLOG.debug("****************[%s]", output);
	    BIZLOG.method("FaQueryAccount.querySetAccountsDeitails end>>>>>>>>>>>>>>>>>>");
	    
	}
	
	
	/**
	 * 录入时增加平衡性校验（带红蓝字）
	 * xionglz
	 */
	public static BigDecimal checkBanlance(FaSetAccountsDetail setAccountDeatilIn){
	    BigDecimal total=BigDecimal.ZERO;
	    //D蓝字，C红字取正数;D红字，C蓝字取负数;
	    if(setAccountDeatilIn.getDebit_credit()==E_DEBITCREDIT.DEBIT && setAccountDeatilIn.getRebuwa()==E_REBUWA.B||
	       setAccountDeatilIn.getDebit_credit()==E_DEBITCREDIT.CREDIT && setAccountDeatilIn.getRebuwa()==E_REBUWA.R||
           setAccountDeatilIn.getDebit_credit()==E_DEBITCREDIT.DEBIT && setAccountDeatilIn.getRebuwa()==E_REBUWA.N){
	        total=setAccountDeatilIn.getAccounting_amt();
	    }else{
	        total=setAccountDeatilIn.getAccounting_amt().negate();
	    }
	    return total ;
	}
	
	
	
	public static void modfiySetAccountsDeitail(FaSetAccountsDetail setAccountDetail){
	    
	    BIZLOG.method("FaSetAccountApply.modfiySetAccountsDeitail>>>>>>begin");
	    BIZLOG.debug("******[%s]", setAccountDetail);
	   
	    // 记账方向不能为空
        CommTools.fieldNotNull(setAccountDetail.getDebit_credit(), GlDict.A.debit_credit.getId(), GlDict.A.debit_credit.getLongName());
        // 记账金额不能为空
        CommTools.fieldNotNull(setAccountDetail.getAccounting_amt(), GlDict.A.accounting_amt.getId(), GlDict.A.accounting_amt.getLongName());
        // 账务机构不能为空
        CommTools.fieldNotNull(setAccountDetail.getAcct_branch(), GlDict.A.acct_branch.getId(), GlDict.A.acct_branch.getLongName());
        // 摘要代码不能为空
        CommTools.fieldNotNull(setAccountDetail.getSummary_code(), GlDict.A.summary_code.getId(), GlDict.A.summary_code.getLongName());
        // 货币代码
        CommTools.fieldNotNull(setAccountDetail.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
        //账号不能为空
        CommTools.fieldNotNull(setAccountDetail.getAcct_no(), GlDict.A.acct_no.getId(),GlDict.A.acct_no.getLongName() );
        
        //获取公共变量
        RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
        
        // 如果套号为空，则报错
        if (CommUtil.isNull(setAccountDetail.getSet_account_no())) {
            // throw GlError.GL.E0026(GlDict.A.set_account_no.getLongName());
            throw ApPubErr.APPUB.E0001(GlDict.A.set_account_no.getId(), GlDict.A.set_account_no.getLongName());
        }
        
        //检查套号是否存在
        fab_set_accounts_main mainTable = Fab_set_accounts_mainDao.selectOne_odb1(setAccountDetail.getSet_account_no(), false);
        
        if(mainTable == null){
            throw GlError.GL.E0027(setAccountDetail.getSet_account_no());
        }
        
        //检查套号状态
        if (mainTable.getSet_account_status() != E_SETACCOUNTSTATE.RECORDING) {
            throw GlError.GL.E0028(setAccountDetail.getSet_account_no(), mainTable.getSet_account_status().getLongName());
        }
        //明细修改与套号申请必须是同一个柜员
        if (CommUtil.compare(mainTable.getTrxn_teller(), runEnvs.getTranus()) != 0) {
            throw GlError.GL.E0030(mainTable.getTrxn_teller());
        }
        //交易金额大于0
        if(CommUtil.compare(setAccountDetail.getAccounting_amt(), new BigDecimal(0)) <= 0){
            throw GlError.GL.E0041();
        }
        
        
        //检查账号是否存在
        faa_account accountTable = Faa_accountDao.selectOne_odb1(setAccountDetail.getAcct_no(), false);
        
        if(accountTable == null){
            throw ApPubErr.APPUB.E0005(OdbFactory.getTable(faa_account.class).getLongname(), GlDict.A.acct_no.getLongName(), setAccountDetail.getAcct_no());
        }
        //查询科目信息
        fap_accounting_subject acctSubject = Fap_accounting_subjectDao.selectOne_odb1(accountTable.getGl_code(), false);
        if(acctSubject == null){
            throw ApPubErr.APPUB.E0005(OdbFactory.getTable(faa_account.class).getLongname(), GlDict.A.acct_no.getLongName(), accountTable.getGl_code());
        }
       
        // 账号必须是表内记账
        if (accountTable.getOn_bal_sheet_ind() == E_YESORNO.NO) {
            throw GlError.GL.E0032(setAccountDetail.getAcct_no());
        }
        //检查记账货币与账户货币是否一致
        if (CommUtil.compare(accountTable.getCcy_code(), setAccountDetail.getCcy_code()) != 0) {
            throw GlError.GL.E0049(setAccountDetail.getCcy_code(), accountTable.getAcct_no(), accountTable.getCcy_code());
        }
       
        // 检查账户状态
        if (accountTable.getAcct_status() == E_ACCTSTATUS.CLOSE) {
            throw GlError.GL.E0034(setAccountDetail.getAcct_no());
        }

        if (CommUtil.compare(FaConst.GL_SYSTEM, accountTable.getSys_no()) != 0) {
            throw GlError.GL.E0036(accountTable.getSys_no(), accountTable.getAcct_no());
        }
        // 判断科目是否可以记账
        FaAccountingSubject.checkSubjectAccounting(accountTable.getGl_code(), accountTable.getSys_no());
	
        //查询明细
        fab_set_accounts_detail detailTable = Fab_set_accounts_detailDao.selectOne_odb1(setAccountDetail.getSet_account_no(), setAccountDetail.getData_sort(), false);
        if(detailTable == null){
            throw GlError.GL.E0046(setAccountDetail.getSet_account_no());
        }
        detailTable.setSet_account_no(setAccountDetail.getSet_account_no());    //套号
        detailTable.setAcct_no(setAccountDetail.getAcct_no());      //账号
        detailTable.setData_sort(setAccountDetail.getData_sort());  //数据序号
        detailTable.setCcy_code(setAccountDetail.getCcy_code());    //币种代码
        detailTable.setDebit_credit(setAccountDetail.getDebit_credit());    //记账方向
        detailTable.setAccounting_amt(setAccountDetail.getAccounting_amt());    //记账金额
        detailTable.setAcct_branch(setAccountDetail.getAcct_branch());  //账务机构
        detailTable.setExchg_method(setAccountDetail.getExchg_method());    //折算方式
        detailTable.setExchg_org_ccy_amt(setAccountDetail.getExchg_org_ccy_amt());  //折本金额
        detailTable.setExchg_dol_ccy_amt(setAccountDetail.getExchg_dol_ccy_amt());  //折美金额
        detailTable.setTrxn_offset_acct_no(setAccountDetail.getTrxn_offset_acct_no());  //交易对手账号
        detailTable.setTrxn_offset_acct_name(setAccountDetail.getTrxn_offset_acct_name());  //交易对手户名
        detailTable.setSummary_code(setAccountDetail.getSummary_code());    //摘要代码
        detailTable.setSummary_desc(setAccountDetail.getSummary_desc());    //摘要描述
        detailTable.setRemark(setAccountDetail.getRemark());    //备注
        detailTable.setRebuwa(setAccountDetail.getRebuwa());    //红蓝字
        detailTable.setGl_code(accountTable.getGl_code());      //科目号
        detailTable.setAcct_name(accountTable.getAcct_name());  //账户名称
        detailTable.setGl_code_desc(acctSubject.getGl_code_desc()); //科目名称
        
        // 校验金额小数位
        CommTools.validAmount(detailTable.getCcy_code(), setAccountDetail.getAccounting_amt());
        
        //更新明细
        Fab_set_accounts_detailDao.updateOne_odb1(detailTable);
        BIZLOG.debug("**************[%s]", detailTable);
        
        //登记柜员流水
        FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);

        regTellerSeq.setSys_no(accountTable.getSys_no()); // 系统编号
        regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.NON_ACCOUNTING); // 交易流水类型
        regTellerSeq.setTrxn_subject(detailTable.getSet_account_no()); // 交易主体
        regTellerSeq.setRemark(detailTable.getRemark()); // 备注
        regTellerSeq.setSett_status(E_SETTSTATE.NONE); // 清算
        regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
        regTellerSeq.setTrxn_seq(FaAccounting.getTellerSeq());
        FaAccounting.regTellerSeq(regTellerSeq); 
        
        BIZLOG.method("FaSetAccountApply.modfiySetAccountsDeitail>>>>>>end");
        
	}

}
