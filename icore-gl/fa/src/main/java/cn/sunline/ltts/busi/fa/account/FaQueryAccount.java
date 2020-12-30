package cn.sunline.ltts.busi.fa.account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.Faa_accountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctBal;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaCalcuBalanceIn;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaQueryAccount {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(FaQueryAccount.class);

	public static FaAcctInfo queryAccount(String acctNo) {
		FaAcctInfo queryOut = SysUtil.getInstance(FaAcctInfo.class);

		faa_account acctTable = Faa_accountDao.selectOne_odb1(acctNo, false);
		if (acctTable == null) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(faa_account.class).getLongname(), GlDict.A.acct_no.getLongName(), acctNo);
		}
		FaOpenAccount.getAcctCom(queryOut, acctTable);
		return queryOut;
	}

	public static Options<FaAcctInfo> queryAccountList(FaAcctInfo queryIn) {
		BIZLOG.method("FaQueryAccount.queryAccountList begin>>>>>>>>>>>>>>>>>>");
		BIZLOG.debug("queryIn[%s]", queryIn);

		// 公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
	    long pageno = runEnvs.getPageno();
	    long pgsize = runEnvs.getPgsize();
		Page<FaAcctInfo> page = FaAccountDao.lstAcctList(runEnvs.getCorpno(),queryIn.getSys_no(), queryIn.getAcct_no(), 
				queryIn.getAcct_name(), queryIn.getAcct_branch(), queryIn.getGl_code(), queryIn.getCcy_code(),
				queryIn.getAcct_seq(), queryIn.getAcct_type(), queryIn.getBal_direction(), queryIn.getBal_prop(), 
				queryIn.getBal_check_ind(), queryIn.getOn_bal_sheet_ind(),queryIn.getDebit_manual_allow(), 
				queryIn.getCredit_manual_allow(), queryIn.getAcct_status(),queryIn.getOpen_acct_date(), 
				queryIn.getClose_acct_date(), (pageno - 1) * pgsize, pgsize,
				runEnvs.getCounts(), false);
		// runEnvs.setCounts(page.getPageCount());
		runEnvs.setCounts(page.getRecordCount());

		Options<FaAcctInfo> queryOut = new DefaultOptions<FaAcctInfo>();
		queryOut.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

		BIZLOG.debug("queryOut[%s]", queryOut);
		BIZLOG.method("FaQueryAccount.queryAccountList end>>>>>>>>>>>>>>>>>>");

		return queryOut;

	}

	public static List<FaAcctInfo> queryBaseAccount(String corpno, String sysNo, String acctBranch, String glCode, String ccyCode, E_ACCTTYPE acctType) {
		List<FaAcctInfo> faAcctList = new ArrayList<FaAcctInfo>();

		long count = FaAccountDao.cntAccountCount(corpno, sysNo, acctBranch, glCode, ccyCode, acctType, false);

		/*
		 * cuijia 基础类型账号允许开出多个账户
		 */
		if (count > 1L) {
			// 系统号[${sys_no}]机构[${acct_branch}]科目号[${gl_code}]贷币[${ccy_code}]基准账户数[${count}]不等于1
			// throw GlError.GL.E0109(sys_no, acct_branch, gl_code, ccy_code,
			// count);
			List<faa_account> acctTableList = Faa_accountDao.selectAll_odb4(sysNo, acctBranch, glCode, ccyCode, acctType, true);
			for (faa_account acctTable : acctTableList) {
				FaAcctInfo queryOut = SysUtil.getInstance(FaAcctInfo.class);
				FaOpenAccount.getAcctCom(queryOut, acctTable);
				faAcctList.add(queryOut);
			}
		} else if (count == 0L) {
			BIZLOG.method("selAccountCount count=[%s]", count);
			return null;
		} else {
			faa_account acctTable = Faa_accountDao.selectFirst_odb4(sysNo, acctBranch, glCode, ccyCode, acctType, true);
			FaAcctInfo queryOut = SysUtil.getInstance(FaAcctInfo.class);
			FaOpenAccount.getAcctCom(queryOut, acctTable);
			faAcctList.add(queryOut);
		}
		return faAcctList;
	}

	/**
	 * 
	 * <p>
	 * Title:queryBaseAccountBal
	 * </p>
	 * <p>
	 * Description: 根据科目、系统号、机构、币种、账号类型查询总余额
	 * </p>
	 * 
	 * @author cuijia
	 * @date 2017年7月1日
	 * @param corpno
	 * @param sys_no
	 * @param acct_branch
	 * @param gl_code
	 * @param ccy_code
	 * @param acct_type
	 * @return
	 */
	public static FaAcctBal queryBaseAccountBal(String corpno, String sysNo, String acctBranch, String glCode, String ccyCode, E_ACCTTYPE acctType) {

		BIZLOG.debug("queryBaseAccountBal====corpno[%s]  sys_no[%s] acct_branch[%s] gl_code[%s] ccy_code[%s] acct_type[%s]", corpno, sysNo, acctBranch, glCode, ccyCode, acctType);

		List<faa_account> acctTableList = Faa_accountDao.selectAll_odb4(sysNo, acctBranch, glCode, ccyCode, acctType, false);
		if (acctTableList.size() == 0) {
			return null;
		}
		FaAcctBal queryOut = SysUtil.getInstance(FaAcctBal.class);
		queryOut.setAcct_bal(BigDecimal.ZERO);
		queryOut.setAcct_branch(acctBranch);
		queryOut.setAcct_type(acctType);
		queryOut.setBal_direction(acctTableList.get(0).getBal_direction());
		queryOut.setCcy_code(ccyCode);
		queryOut.setGl_code(glCode);
		queryOut.setSys_no(sysNo);

		FaCalcuBalanceIn calBalanceIn = SysUtil.getInstance(FaCalcuBalanceIn.class);

		for (faa_account acctTable : acctTableList) {

			calBalanceIn.setAcct_bal(queryOut.getAcct_bal()); // 账户余额
			calBalanceIn.setBal_direction(queryOut.getBal_direction()); // 余额方向
			calBalanceIn.setDebit_credit(acctTable.getBal_direction()); // 记账方向
			calBalanceIn.setAccounting_amt(acctTable.getAcct_bal()); // 记账金额
			calBalanceIn.setBal_prop(acctTable.getBal_prop()); // 余额性质

			BIZLOG.debug("calBalanceIn  begin====Acct_bal[%s] Bal_direction[%s] Debit_credit[%s] Accounting_amt[%s] ", calBalanceIn.getAcct_bal(), calBalanceIn.getBal_direction(),
					calBalanceIn.getDebit_credit(), calBalanceIn.getAccounting_amt());
			// 计算余额
			FaAccounting.calcuBalance(calBalanceIn);

			BIZLOG.debug("calBalanceIn  after====Acct_bal[%s]  Bal_direction[%s] ", calBalanceIn.getAcct_bal(), calBalanceIn.getBal_direction());

			queryOut.setAcct_bal(calBalanceIn.getAcct_bal());
			queryOut.setBal_direction(calBalanceIn.getBal_direction());
		}

		return queryOut;
	}

}
