package cn.sunline.ltts.busi.fa.accounting;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApBuffer;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.ApSmryTools;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.account.FaOpenAccount;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountingDao;
import cn.sunline.ltts.busi.fa.parm.FaAccountingSubject;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.Faa_accountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_sys_defineDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_sys_define;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_accounting_vochDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_teller_seqDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_voch;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_teller_seq;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTSTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_BALPROP;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_EXCHANGEMODE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SYSTEMSERVICESTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaCalcuBalanceIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaDebitAndCrebitBalance;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaOffBanlceAccountingIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegAccountingVoch;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingDoIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaUpdateBalanceIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaaOutAccountTable;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaAccounting {
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(FaSetAccountApply.class);

	/**
	 * 登记柜员流水
	 * 
	 * @param regTellerSeqIn
	 */
	public static void regTellerSeq(FaRegTellerSeq regTellerSeqIn) {

		// 检查非空
		CommTools.fieldNotNull(regTellerSeqIn.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
		CommTools.fieldNotNull(regTellerSeqIn.getTrxn_seq_type(), GlDict.A.trxn_seq_type.getId(), GlDict.A.trxn_seq_type.getLongName());
		CommTools.fieldNotNull(regTellerSeqIn.getSett_status(), GlDict.A.sett_status.getId(), GlDict.A.sett_status.getLongName());
		CommTools.fieldNotNull(regTellerSeqIn.getReversal_status(), GlDict.A.reversal_status.getId(), GlDict.A.reversal_status.getLongName());
		CommTools.fieldNotNull(regTellerSeqIn.getTrxn_seq(), GlDict.A.trxn_seq.getId(), GlDict.A.trxn_seq.getLongName());

		if (regTellerSeqIn.getReversal_status() == E_REVERSALSTATE.STRIKE_BALANCE) {
			CommTools.fieldNotNull(regTellerSeqIn.getOriginal_trxn_date(), GlDict.A.original_trxn_date.getId(), GlDict.A.original_trxn_date.getLongName());
			CommTools.fieldNotNull(regTellerSeqIn.getOriginal_trxn_seq(), GlDict.A.original_trxn_seq.getId(), GlDict.A.original_trxn_seq.getLongName());

		}
		fab_teller_seq tellerSeq = SysUtil.getInstance(fab_teller_seq.class);

		// 获取公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		tellerSeq.setTrxn_date(runEnvs.getTrandt()); // 交易日期
		tellerSeq.setTrxn_seq(regTellerSeqIn.getTrxn_seq()); // 交易流水
		tellerSeq.setSys_no(regTellerSeqIn.getSys_no()); // 系统编号
		tellerSeq.setTrxn_code(runEnvs.getPrcscd()); // 交易码
		tellerSeq.setTrxn_name(runEnvs.getPrcsna()); // 交易名称
		tellerSeq.setTrxn_seq_type(regTellerSeqIn.getTrxn_seq_type()); // 交易流水类型
		tellerSeq.setTrxn_subject(regTellerSeqIn.getTrxn_subject()); // 交易主体
		tellerSeq.setBusi_ref_no(regTellerSeqIn.getBusi_ref_no()); // 业务参考号
		tellerSeq.setRemark(regTellerSeqIn.getRemark()); // 备注
		tellerSeq.setSett_status(regTellerSeqIn.getSett_status()); // 清算
		tellerSeq.setSett_batch_no(regTellerSeqIn.getSett_batch_no()); // 清算批次号
		tellerSeq.setReversal_status(regTellerSeqIn.getReversal_status()); // 冲账状态
		tellerSeq.setOriginal_trxn_date(regTellerSeqIn.getOriginal_trxn_date()); // 原日期
		tellerSeq.setOriginal_trxn_seq(regTellerSeqIn.getOriginal_trxn_seq()); // 原交易流水
		tellerSeq.setHost_date(DateUtil.getNow("yyyyMMdd")); // 主机日期
		tellerSeq.setTrxn_time(runEnvs.getTrantm()); // 交易时间
		tellerSeq.setTrxn_branch(runEnvs.getTranbr()); // 交易机构
		tellerSeq.setTrxn_teller(runEnvs.getTranus()); // 交易柜员
		tellerSeq.setReview_teller(regTellerSeqIn.getReview_teller()); // 复核柜员
		tellerSeq.setRecdver(1l);

		Fab_teller_seqDao.insert(tellerSeq);

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月6日-上午10:20:24</li>
	 *         <li>功能说明：单笔记账</li>
	 *         </p>
	 * @param accountingIn
	 * @return
	 */
	public static void singleAccounting(FaSingleAccountingCheckIn accountingIn, String tellerSeq, Long toalCount) {

		BIZLOG.method("FaAccounting.singleAccounting begin>>>>>>>>>> ");
		BIZLOG.debug("accountingIn[%s]", accountingIn);

		// faa_account acctTable = SysUtil.getInstance(faa_account.class);

		// 单笔记账check
		String acctNo = singleAccountingCheck(accountingIn);

		FaSingleAccountingDoIn accountingDoIn = SysUtil.getInstance(FaSingleAccountingDoIn.class);

		accountingDoIn.setAcct_no(acctNo); // 账号
		accountingDoIn.setAccounting_amt(accountingIn.getAccounting_amt()); // 记账金额
		accountingDoIn.setDebit_credit(accountingIn.getDebit_credit()); // 记账方向
		accountingDoIn.setSummary_code(accountingIn.getSummary_code()); // 摘要代码
		accountingDoIn.setRemark(accountingIn.getRemark());

		// 单笔记账do
		singleAccountingDo(accountingDoIn, tellerSeq, toalCount);

		BIZLOG.method("FaAccounting.singleAccounting end>>>>>>>>>>>>>>> ");

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月11日-下午1:53:39</li>
	 *         <li>功能说明：单笔记账及登记总账凭证</li>
	 *         </p>
	 * @param accountingDoIn
	 */
	private static void singleAccountingDo(FaSingleAccountingDoIn accountingDoIn, String tellerSeq, long totalCount) {

		// 外面没有登记柜员流水，则登记柜员流水
		if (CommUtil.isNull(tellerSeq)) {

			// 重新生成柜员流水
			tellerSeq = getTellerSeq();

			// 登记柜员流水
			FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
			regTellerSeq.setSys_no(accountingDoIn.getSys_no()); // 系统编号
			regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
			// regTellerSeq.setTrxn_subject(); //交易主体
			regTellerSeq.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
			regTellerSeq.setRemark(accountingDoIn.getRemark()); // 备注
			regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
			// regTellerSeq.setSett_batch_no(); //清算批次号
			regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
			// regTellerSeq.setOriginal_trxn_date(); //原日期
			// regTellerSeq.setOriginal_trxn_seq(); //原交易流水
			// regTellerSeq.setCheck_teller(); //复核柜员
			regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水

			FaAccounting.regTellerSeq(regTellerSeq);

		}
		// 更新账户余额
		FaUpdateBalanceIn updateBalanceIn = SysUtil.getInstance(FaUpdateBalanceIn.class);
		updateBalanceIn.setAccounting_amt(accountingDoIn.getAccounting_amt());
		updateBalanceIn.setAcct_no(accountingDoIn.getAcct_no());
		updateBalanceIn.setDebit_credit(accountingDoIn.getDebit_credit());

		faa_account updateAcctTable = updateBalance(updateBalanceIn);

		// 登记凭证
		FaRegAccountingVoch accountingVoch = SysUtil.getInstance(FaRegAccountingVoch.class);

		accountingVoch.setSys_no(updateAcctTable.getSys_no()); // 系统编号
		accountingVoch.setAcct_branch(updateAcctTable.getAcct_branch()); // 账务机构
		accountingVoch.setTrxn_ccy(updateAcctTable.getCcy_code()); // 交易币种
		accountingVoch.setGl_code(updateAcctTable.getGl_code()); // 科目号
		accountingVoch.setAcct_seq(updateAcctTable.getAcct_seq()); // 账户序号
		accountingVoch.setAcct_no(updateAcctTable.getAcct_no()); // 账号
		accountingVoch.setDebit_credit(accountingDoIn.getDebit_credit()); // 记账方向
		accountingVoch.setAccounting_amt(accountingDoIn.getAccounting_amt()); // 记账金额

		accountingVoch.setOn_bal_sheet_ind(updateAcctTable.getOn_bal_sheet_ind()); // 表内标志
		accountingVoch.setBal_direction(updateAcctTable.getBal_direction()); // 余额方向
		accountingVoch.setAcct_bal(updateAcctTable.getAcct_bal()); // 账户余额
		accountingVoch.setSummary_code(accountingDoIn.getSummary_code()); // 摘要代码
		accountingVoch.setSummary_desc(accountingDoIn.getRemark());
		
		accountingVoch.setTotal_count(totalCount); // 总记录数
		accountingVoch.setTrxn_seq(tellerSeq);

		regAccountingVoch(accountingVoch);
	}

	public static faa_account updateBalance(FaUpdateBalanceIn updateBalanceIn) {

		// 带锁取出账户信息
		faa_account updateAcctTable = Faa_accountDao.selectOneWithLock_odb1(updateBalanceIn.getAcct_no(), false);

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 计算余额
		FaCalcuBalanceIn calBalanceIn = SysUtil.getInstance(FaCalcuBalanceIn.class);
		calBalanceIn.setBal_direction(updateAcctTable.getBal_direction()); // 余额方向
		calBalanceIn.setAcct_bal(updateAcctTable.getAcct_bal()); // 账户余额
		calBalanceIn.setDebit_credit(updateBalanceIn.getDebit_credit()); // 记账方向
		calBalanceIn.setAccounting_amt(updateBalanceIn.getAccounting_amt()); // 记账金额
		calBalanceIn.setBal_prop(updateAcctTable.getBal_prop()); // 余额性质

		// 计算余额
		calcuBalance(calBalanceIn);

		// 判断是否透支,如果余额小于最小值或者大于最大值，报错
		if (updateAcctTable.getBal_check_ind() == E_YESORNO.YES) {
			if (CommUtil.compare(runEnvs.getSystcd(), FaConst.BAT_SYSTEM) != 0 && CommUtil.compare(runEnvs.getServno(), FaConst.BAT_SYSTEM) != 0
					&& CommUtil.compare(runEnvs.getPrcscd(), FaConst.BAT_ACCOUNTING_TRXN_CODE) != 0) {

				if (CommUtil.compare(calBalanceIn.getAcct_bal(), BigDecimal.ZERO) < 0) {
					throw GlError.GL.E0044(calBalanceIn.getAcct_bal().toString());
				}
			}
		}

		// 更新余额
		if (CommUtil.isNotNull(updateAcctTable.getBal_update_date()) && CommUtil.compare(runEnvs.getTrandt(), updateAcctTable.getBal_update_date()) > 0) {
			// 已经换日
			updateAcctTable.setPrevious_acct_bal(updateAcctTable.getAcct_bal());
			updateAcctTable.setPrevious_bal_direction(updateAcctTable.getBal_direction());
			updateAcctTable.setBal_update_date(runEnvs.getTrandt());
		}

		// 更新余额
		updateAcctTable.setAcct_bal(calBalanceIn.getAcct_bal());
		updateAcctTable.setBal_direction(calBalanceIn.getBal_direction());

		Faa_accountDao.updateOne_odb1(updateAcctTable);
		return updateAcctTable;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月9日-下午8:15:38</li>
	 *         <li>功能说明：单笔记账检查</li>
	 *         </p>
	 * @param accountingIn
	 * @param acctTable
	 */
	public static String singleAccountingCheck(FaSingleAccountingCheckIn accountingIn) {

		String acctNo;
		// 检查总账的账务服务状态，关账时只许日终记账
		checkSystemStatus();

		// 检查非空
		checkAccountingInNull(accountingIn);

		faa_account acctTable = SysUtil.getInstance(faa_account.class);
		// 优先账号记账
		if (CommUtil.isNotNull(accountingIn.getAcct_no())) {

			acctTable = Faa_accountDao.selectOne_odb1(accountingIn.getAcct_no(), false);
			if (acctTable == null) {
				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(faa_account.class).getLongname(), GlDict.A.acct_no.getLongName(), accountingIn.getAcct_no());
			}

		} else { // 根据科目号取账号

			FaAcctInfo openIn = SysUtil.getInstance(FaAcctInfo.class);
			FaAcctInfo openOut = SysUtil.getInstance(FaAcctInfo.class);
			openIn.setSys_no(accountingIn.getSys_no()); // 系统编号
			openIn.setCcy_code(accountingIn.getCcy_code()); // 货币代码
			openIn.setAcct_seq(accountingIn.getAcct_seq()); // 账户序号
			openIn.setAcct_type(accountingIn.getAcct_type()); // 账户分类
			openIn.setAcct_branch(accountingIn.getAcct_branch()); // 财务机构
			openIn.setGl_code(accountingIn.getGl_code()); // 科目号
			openIn.setOn_bal_sheet_ind(E_YESORNO.YES);
			openIn.setOpen_acct_ind(E_YESORNO.YES);

			// 开户
			if (accountingIn.getAcct_type() == E_ACCTTYPE.BASE_ACCOUNT) {
				openOut = FaOpenAccount.openBaseAccount(openIn);
			} else {
				openOut = FaOpenAccount.openSpecialAccount(openIn);
			}

			// 复合类型转换为表类型
			getAcctTable(acctTable, openOut);
		}
		acctNo = acctTable.getAcct_no();
		BIZLOG.debug("acctTable[%s]", acctTable);
		// 检查科目记账权限
		FaAccountingSubject.checkSubjectAccounting(acctTable.getGl_code(), acctTable.getSys_no());

		// 检查账户状态
		if (acctTable.getAcct_status() == E_ACCTSTATUS.CLOSE) {
			throw GlError.GL.E0034(acctTable.getAcct_no());
		}

		// 记账金额为0，跳过
		if (CommUtil.compare(accountingIn.getAccounting_amt(), BigDecimal.ZERO) == 0) {

			return acctNo;
		}

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		if (CommUtil.compare(runEnvs.getSystcd(), FaConst.BAT_SYSTEM) != 0) {
			// 判断借贷方记账许可
			if (acctTable.getDebit_manual_allow() == E_YESORNO.NO && accountingIn.getDebit_credit() == E_DEBITCREDIT.DEBIT) {
				throw GlError.GL.E0045(acctTable.getAcct_no(), accountingIn.getDebit_credit().getLongName());
			} else if (acctTable.getCredit_manual_allow() == E_YESORNO.NO && accountingIn.getDebit_credit() == E_DEBITCREDIT.CREDIT) {
				throw GlError.GL.E0045(acctTable.getAcct_no(), accountingIn.getDebit_credit().getLongName());

			}
		}

		CommTools.validAmount(acctTable.getCcy_code(), accountingIn.getAccounting_amt());
		return acctNo;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月6日-下午1:50:41</li>
	 *         <li>功能说明：计算账户余额</li>
	 *         </p>
	 * @param calBalanceIn
	 */
	public static void calcuBalance(FaCalcuBalanceIn calBalanceIn) {

		BIZLOG.method("FaAccounting.calcuBalance>>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.debug("calBalanceIn[%s]", calBalanceIn);

		// 账户余额方向
		E_DEBITCREDIT balDeriction = calBalanceIn.getBal_direction();
		// 账户余额
		BigDecimal acctBalance = BigDecimal.ZERO;

		// 如果记账金额为负数，则转换记账方向
		if (CommUtil.compare(calBalanceIn.getAccounting_amt(), BigDecimal.ZERO) < 0) {

			if (calBalanceIn.getDebit_credit() == E_DEBITCREDIT.CREDIT) {
				calBalanceIn.setDebit_credit(E_DEBITCREDIT.DEBIT);
			} else {
				calBalanceIn.setDebit_credit(E_DEBITCREDIT.CREDIT);
			}

			// 交易金额转换
			calBalanceIn.setAccounting_amt(calBalanceIn.getAccounting_amt().negate());
		}

		if (balDeriction == E_DEBITCREDIT.DEBIT) { // 账户余额在借方

			if (calBalanceIn.getDebit_credit() == E_DEBITCREDIT.DEBIT) { // 记账方向在借方
				acctBalance = calBalanceIn.getAcct_bal().add(calBalanceIn.getAccounting_amt());
			} else {
				acctBalance = calBalanceIn.getAcct_bal().subtract(calBalanceIn.getAccounting_amt());
			}

		} else { // 账户余额原来在贷方

			if (calBalanceIn.getDebit_credit() == E_DEBITCREDIT.CREDIT) { // 记账方向在贷方
				acctBalance = calBalanceIn.getAcct_bal().add(calBalanceIn.getAccounting_amt());
			} else {
				acctBalance = calBalanceIn.getAcct_bal().subtract(calBalanceIn.getAccounting_amt());
			}

		}

		// 如果账户余额小于0，轧差性质的账户余额方向要转变方向
		if (CommUtil.compare(acctBalance, BigDecimal.ZERO) < 0) {
			if (calBalanceIn.getBal_prop() == E_BALPROP.NETTING) {
				if (balDeriction == E_DEBITCREDIT.CREDIT) {
					balDeriction = E_DEBITCREDIT.DEBIT;
				} else {
					balDeriction = E_DEBITCREDIT.CREDIT;
				}
				acctBalance = acctBalance.negate();
			}
		}

		calBalanceIn.setAcct_bal(acctBalance);
		calBalanceIn.setBal_direction(balDeriction);

		BIZLOG.debug("After calculating >>>>>>calBalanceIn[%s]", calBalanceIn);
		BIZLOG.method("FaAccounting.calcuBalance>>>>>>>>>>>>End>>>>>>>>>>>>");

	}

	public static void getAcctTable(faa_account acctTable, FaAcctInfo openOut) {
		acctTable.setSys_no(openOut.getSys_no()); // 系统编号
		acctTable.setAcct_no(openOut.getAcct_no()); // 账号
		acctTable.setAcct_name(openOut.getAcct_name()); // 账户名称
		acctTable.setAcct_branch(openOut.getAcct_branch()); // 账务机构
		acctTable.setGl_code(openOut.getGl_code()); // 科目号
		acctTable.setCcy_code(openOut.getCcy_code()); // 货币代码
		acctTable.setAcct_seq(openOut.getAcct_seq()); // 账户序号
		acctTable.setAcct_type(openOut.getAcct_type()); // 账户分类
		acctTable.setBal_direction(openOut.getBal_direction()); // 余额方向
		acctTable.setBal_prop(openOut.getBal_prop()); // 余额性质
		acctTable.setAcct_bal(openOut.getAcct_bal()); // 账户余额
		acctTable.setPrevious_bal_direction(openOut.getPrevious_bal_direction()); // 上期余额方向
		acctTable.setPrevious_acct_bal(openOut.getPrevious_acct_bal()); // 上期账户余额
		acctTable.setBal_update_date(openOut.getBal_update_date()); // 余额更新日期
		acctTable.setBal_check_ind(openOut.getBal_check_ind()); // 余额检查标志
		acctTable.setOn_bal_sheet_ind(openOut.getOn_bal_sheet_ind()); // 表内标志
		acctTable.setDebit_manual_allow(openOut.getDebit_manual_allow()); // 借方手工记账许可
		acctTable.setCredit_manual_allow(openOut.getCredit_manual_allow()); // 贷方手工记账许可
		acctTable.setAcct_status(openOut.getAcct_status()); // 账户状态
		acctTable.setRemark(openOut.getRemark()); // 备注
		acctTable.setOpen_acct_brch(openOut.getOpen_acct_brch()); // 开户机构
		acctTable.setOpen_acct_user(openOut.getOpen_acct_user()); // 开户柜员
		acctTable.setOpen_acct_date(openOut.getOpen_acct_date()); // 开户日期
		acctTable.setOpen_acct_seq(openOut.getOpen_acct_seq()); // 开户流水
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月6日-上午10:39:26</li>
	 *         <li>功能说明：检查账系统服务</li>
	 *         </p>
	 */
	public static void checkSystemStatus() {

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		fap_sys_define sysDefine = Fap_sys_defineDao.selectOne_odb1(FaConst.GL_SYSTEM, true);
		// 当总账关账时，只允许会计批量记账
		if (sysDefine.getSystem_service_status() == E_SYSTEMSERVICESTATUS.OFF) {
			if (CommUtil.compare(runEnvs.getServno(), FaConst.DAY_END) != 0) {
				BIZLOG.debug("checkSystemStatus servno[%s] ", runEnvs.getServno());
				throw GlError.GL.E0042();
			}
		}
	}


	private static void checkAccountingInNull(FaSingleAccountingCheckIn accountingIn) {
		BIZLOG.debug("checkAccountingInNull accountingIn[%s]", accountingIn);
		// 记账方向不能为空
		CommTools.fieldNotNull(accountingIn.getDebit_credit(), GlDict.A.debit_credit.getId(), GlDict.A.debit_credit.getLongName());

		// 记账金额不能为空
		CommTools.fieldNotNull(accountingIn.getAccounting_amt(), GlDict.A.accounting_amt.getId(), GlDict.A.accounting_amt.getLongName());

		// 表内记账标志不许为空
		// CommTools.fieldNotNull(accountingIn.getOn_bal_sheet_ind(),
		// GlDict.A.table_in_ind.getId(), GlDict.A.table_in_ind.getLongName());

		// 如果账号为空，则科目、系统、序号和账户分类不允许为空
		if (CommUtil.isNull(accountingIn.getAcct_no())) {

			CommTools.fieldNotNull(accountingIn.getAcct_branch(), GlDict.A.acct_branch.getId(), GlDict.A.acct_branch.getLongName());
			CommTools.fieldNotNull(accountingIn.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());

			CommTools.fieldNotNull(accountingIn.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());

			CommTools.fieldNotNull(accountingIn.getAcct_type(), GlDict.A.acct_type.getId(), GlDict.A.acct_type.getLongName());

			CommTools.fieldNotNull(accountingIn.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
			// 基准账户
			if (accountingIn.getAcct_type() == E_ACCTTYPE.BASE_ACCOUNT) {
				// 如果输入了账户序号，判断；否则赋值,赋值不能等于默认的基础账号子序号
				if (CommUtil.isNotNull(accountingIn.getAcct_seq())) {
					String acctSeq = ApKnpPara.getKnpPara("ACCT_SEQ", "BASE_ACCOUNT").getPmval1();
					if (accountingIn.getAcct_seq().equals(acctSeq) 
							&& !CommUtil.equals(FaConst.LOAN_SYSTEM, accountingIn.getSys_no())) {
						throw GlError.GL.E0002(acctSeq);
					}
				} else {
					accountingIn.setAcct_seq(ApKnpPara.getKnpPara("ACCT_SEQ", "BASE_ACCOUNT").getPmval1());
				}
			} else if (accountingIn.getAcct_type() == E_ACCTTYPE.SPECIAL_ACCOUNT) {

				CommTools.fieldNotNull(accountingIn.getAcct_seq(), GlDict.A.acct_seq.getId(), GlDict.A.acct_seq.getLongName());

			} else {
				throw GlError.GL.E0043();
			}

		}

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月6日-下午3:01:43</li>
	 *         <li>功能说明：登记总账凭证</li>
	 *         </p>
	 * @param regAccountigVochIn
	 */
	public static void regAccountingVoch(FaRegAccountingVoch regAccountigVochIn) {

		// 系统标志号不能为空
		CommTools.fieldNotNull(regAccountigVochIn.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getAcct_branch(), GlDict.A.acct_branch.getId(), GlDict.A.acct_branch.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getTrxn_ccy(), GlDict.A.trxn_ccy.getId(), GlDict.A.trxn_ccy.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getAcct_seq(), GlDict.A.acct_seq.getId(), GlDict.A.acct_seq.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getAcct_no(), GlDict.A.acct_no.getId(), GlDict.A.acct_no.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getDebit_credit(), GlDict.A.debit_credit.getId(), GlDict.A.debit_credit.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getAccounting_amt(), GlDict.A.accounting_amt.getId(), GlDict.A.accounting_amt.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getOn_bal_sheet_ind(), GlDict.A.on_bal_sheet_ind.getId(), GlDict.A.on_bal_sheet_ind.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getBal_direction(), GlDict.A.bal_direction.getId(), GlDict.A.bal_direction.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getAcct_bal(), GlDict.A.acct_bal.getId(), GlDict.A.acct_bal.getLongName());
		CommTools.fieldNotNull(regAccountigVochIn.getTrxn_seq(), GlDict.A.trxn_seq.getId(), GlDict.A.trxn_seq.getLongName());

		fab_accounting_voch accountingVochTable = SysUtil.getInstance(fab_accounting_voch.class);

		// 公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		accountingVochTable.setTrxn_date(runEnvs.getTrandt()); // 交易日期
		accountingVochTable.setTrxn_seq(regAccountigVochIn.getTrxn_seq()); // 交易流水

		accountingVochTable.setData_sort(CommTools.getCurrentThreadSeq(regAccountigVochIn.getTrxn_seq())); // 数据序号
		accountingVochTable.setSys_no(regAccountigVochIn.getSys_no()); // 系统编号
		accountingVochTable.setAcct_branch(regAccountigVochIn.getAcct_branch()); // 账务机构
		accountingVochTable.setTrxn_ccy(regAccountigVochIn.getTrxn_ccy()); // 交易币种
		accountingVochTable.setGl_code(regAccountigVochIn.getGl_code()); // 科目号
		accountingVochTable.setAcct_seq(regAccountigVochIn.getAcct_seq()); // 账户序号
		accountingVochTable.setAcct_no(regAccountigVochIn.getAcct_no()); // 账号
		accountingVochTable.setDebit_credit(regAccountigVochIn.getDebit_credit()); // 记账方向
		accountingVochTable.setAccounting_amt(regAccountigVochIn.getAccounting_amt()); // 记账金额
		accountingVochTable.setTotal_count(regAccountigVochIn.getTotal_count()); // 总记录数

		accountingVochTable.setOn_bal_sheet_ind(regAccountigVochIn.getOn_bal_sheet_ind()); // 表内标志
		accountingVochTable.setBal_direction(regAccountigVochIn.getBal_direction()); // 余额方向
		accountingVochTable.setAcct_bal(regAccountigVochIn.getAcct_bal()); // 账户余额
		accountingVochTable.setSummary_code(regAccountigVochIn.getSummary_code()); // 摘要代码
		
		if (CommUtil.isNotNull(regAccountigVochIn.getSummary_code())) {
			accountingVochTable.setSummary_desc(ApSmryTools.getText(regAccountigVochIn.getSummary_code())); // 摘要描述
		} else {
			accountingVochTable.setSummary_desc(regAccountigVochIn.getSummary_desc());
		}
		accountingVochTable.setHost_date(DateUtil.getNow("yyyyMMdd")); // 主机日期
		accountingVochTable.setTrxn_time(runEnvs.getTrantm()); // 交易时间
		
		accountingVochTable.setRecdver(1L);  // 默认数据版本号

		Fab_accounting_vochDao.insert(accountingVochTable);
	}

	// 表外记账
	@SuppressWarnings("null")
    public static FaaOutAccountTable bookOffBalanceAccounting(FaOffBanlceAccountingIn offBalanceIn) {
		BIZLOG.method("FaAccounting.bookOffBalanceAccounting>>>>begin>>>>");
		BIZLOG.debug("offBalanceIn[%s]", offBalanceIn);

		// 记账方向不能为空
		CommTools.fieldNotNull(offBalanceIn.getDebit_credit(), GlDict.A.debit_credit.getId(), GlDict.A.debit_credit.getLongName());

		// 记账金额不能为空
		CommTools.fieldNotNull(offBalanceIn.getAccounting_amt(), GlDict.A.accounting_amt.getId(), GlDict.A.accounting_amt.getLongName());

		// 摘要代码
		CommTools.fieldNotNull(offBalanceIn.getSummary_code(), GlDict.A.summary_code.getId(), GlDict.A.summary_code.getLongName());

		faa_account acctTable = Faa_accountDao.selectOne_odb1(offBalanceIn.getAcct_no(), false);
		if (acctTable == null) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(faa_account.class).getLongname(), GlDict.A.acct_no.getId(), offBalanceIn.getAcct_no());
		}
		// 必须为表外账户
		if (acctTable.getOn_bal_sheet_ind() != E_YESORNO.NO) {
			throw GlError.GL.E0050(offBalanceIn.getAcct_no());
		}
		// 已关闭账户不许记账
		if (acctTable.getAcct_status() == E_ACCTSTATUS.CLOSE) {
			throw GlError.GL.E0034(acctTable.getAcct_no());
		}

		// 记账金额为0，跳过
		if (CommUtil.compare(offBalanceIn.getAccounting_amt(), new BigDecimal(0)) == 0) {

			throw GlError.GL.E0031();
		}

		// 只有总账账户允许记账
		if (CommUtil.compare(acctTable.getSys_no(), FaConst.GL_SYSTEM) != 0) {
			throw GlError.GL.E0051(offBalanceIn.getAcct_no());
		}
		//货币币种不符合不允许记账
		if(CommUtil.compare(acctTable.getCcy_code(), offBalanceIn.getCcy_code()) != 0){
	          throw GlError.GL.E0049(offBalanceIn.getCcy_code(), offBalanceIn.getAcct_no(), acctTable.getCcy_code());
		}
		//将收付方向转换为借贷方向
		switch (offBalanceIn.getDebit_credit()) {
        case RV:
            offBalanceIn.setDebit_credit(E_DEBITCREDIT.DEBIT);
            break;
        case PY:
            offBalanceIn.setDebit_credit(E_DEBITCREDIT.CREDIT);
            break;
        default:
            throw GlError.GL.E0203(offBalanceIn.getDebit_credit());            
        }
		FaSingleAccountingDoIn accountingDoIn = SysUtil.getInstance(FaSingleAccountingDoIn.class);

		accountingDoIn.setAcct_no(offBalanceIn.getAcct_no()); // 账号
		accountingDoIn.setAccounting_amt(offBalanceIn.getAccounting_amt()); // 记账金额
		accountingDoIn.setDebit_credit(offBalanceIn.getDebit_credit()); // 记账方向
		accountingDoIn.setSummary_code(offBalanceIn.getSummary_code()); // 摘要代码
		accountingDoIn.setSys_no(acctTable.getSys_no());
		// 记账
		singleAccountingDo(accountingDoIn, "", 1l);

		// 登记柜员流水
		FaRegTellerSeq regTellerSeqIn = SysUtil.getInstance(FaRegTellerSeq.class);

		regTellerSeqIn.setSys_no(acctTable.getSys_no()); // 系统编号
		regTellerSeqIn.setTrxn_seq_type(E_TRXNSEQTYPE.MANUAL_ACCOUNTING); // 交易流水类型
		regTellerSeqIn.setTrxn_subject(acctTable.getAcct_no()); // 交易主体
		regTellerSeqIn.setRemark(offBalanceIn.getRemark()); // 备注
		regTellerSeqIn.setSett_status(E_SETTSTATE.NONE); // 清算
		regTellerSeqIn.setReversal_status(E_REVERSALSTATE.NONE);// 冲账状态
		regTellerSeqIn.setTrxn_seq(getTellerSeq());
		regTellerSeq(regTellerSeqIn);
		//设置表外记账返回值
		FaaOutAccountTable faaoutaccounttable=SysUtil.getInstance(FaaOutAccountTable.class);
		faaoutaccounttable.setAcct_no(acctTable.getAcct_no());//账号
		faaoutaccounttable.setAcct_name(acctTable.getAcct_name());
		faaoutaccounttable.setGl_code(acctTable.getGl_code());//科目号
		faaoutaccounttable.setDebit_credit(offBalanceIn.getDebit_credit());//记账方向
		faaoutaccounttable.setAccounting_amt(offBalanceIn.getAccounting_amt());//记账金额
		faaoutaccounttable.setCcy_code(offBalanceIn.getCcy_code());//币种
		faaoutaccounttable.setSummary_code(offBalanceIn.getSummary_code());//摘要代码
		faaoutaccounttable.setRemark(offBalanceIn.getRemark());//备注
		BIZLOG.method("FaAccounting.bookOffBalanceAccounting>>>>end>>>>");
		
		return faaoutaccounttable;
	}

	/**
	 * 生成柜员流水
	 * 
	 * @return
	 */
	public static String getTellerSeq() {

		ApBuffer.clear();
		String tellerSeq = ApSeq.genSeq("TELLER_SEQ"); // 生成内部账号
		BIZLOG.debug("TELLER_SEQ[%s]", tellerSeq);

		return tellerSeq;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月11日-下午1:22:32</li>
	 *         <li>功能说明：套账记账（含科目）</li>
	 *         </p>
	 */
	public static void bookMultiAccounting(List<FaSingleAccountingCheckIn> accountingList, String tellerSeq, Long totalCount, String sysNo, String remark, boolean isCheck) {

		BIZLOG.method("bookMultiAccounting begin>>>>>");

		if (CommUtil.isNull(accountingList) || accountingList.size() == 0) {
			return;
		}
		BIZLOG.method("accountingList.size()=[%s]", accountingList.size());

		CommTools.fieldNotNull(sysNo, GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());

		// 传入柜员流水为空，则重新生成交易流水
		if (CommUtil.isNull(tellerSeq)) {
			// 重新生成柜员流水
			tellerSeq = getTellerSeq();

			// 登记柜员流水
			FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
			regTellerSeq.setSys_no(sysNo); // 系统编号
			regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
			// regTellerSeq.setTrxn_subject(); //交易主体
			regTellerSeq.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
			regTellerSeq.setRemark(remark); // 备注
			regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
			// regTellerSeq.setSett_batch_no(); //清算批次号
			regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
			// regTellerSeq.setOriginal_trxn_date(); //原日期
			// regTellerSeq.setOriginal_trxn_seq(); //原交易流水
			// regTellerSeq.setCheck_teller(); //复核柜员
			regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水

			FaAccounting.regTellerSeq(regTellerSeq);

		}

		// 单笔记账
		for (FaSingleAccountingCheckIn accountingInfo : accountingList) {
			singleAccounting(accountingInfo, tellerSeq, totalCount);
		}

		// 检查平衡
		if (isCheck) {
			checkBalance(tellerSeq);
		}

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月9日-下午5:30:30</li>
	 *         <li>功能说明：检查借贷平衡</li>
	 *         </p>
	 */
	public static void checkBalance(String trxn_seq) {

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		List<FaDebitAndCrebitBalance> noBalanceCount = FaAccountingDao.lstNoBalanceCount(runEnvs.getCorpno(), trxn_seq, runEnvs.getTrandt(), false);

		if (noBalanceCount.size() > 0) {

			BIZLOG.debug("trxn_seq[%s],it faild to pass the balance-check", trxn_seq);

			for (FaDebitAndCrebitBalance noBalance : noBalanceCount) {

				BIZLOG.debug("ccyCode[%s], margin is [%s]", noBalance.getTrxn_ccy(), noBalance.getDebit_creit_margin());

			}

			throw GlError.GL.E0052();
		}
	}

	/**
	 * 记账复合类型赋值（输入账号）
	 */
	public static void getAccountingCheckByAcct(List<FaSingleAccountingCheckIn> singAccountCheckInList, String acctNo, E_DEBITCREDIT accountingDirect, BigDecimal accountingAmt, String summaryCode,
			String remark) {

		FaSingleAccountingCheckIn singleAccountingCheckIn = SysUtil.getInstance(FaSingleAccountingCheckIn.class);
		singleAccountingCheckIn.setAcct_no(acctNo); // 账号
		singleAccountingCheckIn.setDebit_credit(accountingDirect); // 记账方向
		singleAccountingCheckIn.setAccounting_amt(accountingAmt); // 记账金额
		singleAccountingCheckIn.setSummary_code(summaryCode); // 摘要代码
		singleAccountingCheckIn.setRemark(remark); // 备注

		singAccountCheckInList.add(singleAccountingCheckIn);

	}
    
}