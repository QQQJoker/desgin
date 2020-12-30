package cn.sunline.ltts.busi.fa.account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.ApBuffer;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApCurrency;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.Faa_accountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_sys_defineDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_sys_define;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctDefineInfo;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaSubmitAcct;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTSTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_BALPROP;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_BATCHOPENBRCHSCOPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_MANUALOPENACCTMODE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.sys.dict.GlDict;

/**
 * <p>
 * 文件功能说明：总账开户
 * </p>
 * 
 * @Author Administrator
 *         <p>
 *         <li>2017年2月22日-下午12:06:28</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年2月22日-Administrator：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */

public class FaOpenAccount {
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(FaOpenAccount.class);

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年2月25日-下午2:32:57</li>
	 *         <li>功能说明：开户</li>
	 *         </p>
	 * @param openAccount
	 */
	public static FaAcctInfo openAccount(FaAcctInfo acctIn) {

		FaAcctInfo acctOut = SysUtil.getInstance(FaAcctInfo.class);
		// 开户标志为YES
		acctIn.setOpen_acct_ind(E_YESORNO.YES);
		// 账户分类不允许为空
		CommTools.fieldNotNull(acctIn.getAcct_type(), GlDict.A.acct_type.getId(), GlDict.A.acct_type.getLongName());

		if (acctIn.getAcct_type() == E_ACCTTYPE.BASE_ACCOUNT) {
			acctOut = openBaseAccount(acctIn);
		} else if (acctIn.getAcct_type() == E_ACCTTYPE.SPECIAL_ACCOUNT) {
			acctOut = openSpecialAccount(acctIn);
		} else {
			acctOut = openManulAcct(acctIn);
		}

		FaRegTellerSeq regTellerSeqIn = SysUtil.getInstance(FaRegTellerSeq.class);

		regTellerSeqIn.setSys_no(acctOut.getSys_no()); // 系统编号
		regTellerSeqIn.setTrxn_seq_type(E_TRXNSEQTYPE.NON_ACCOUNTING); // 交易流水类型
		regTellerSeqIn.setTrxn_subject(acctOut.getAcct_no()); // 交易主体
		// regTellerSeqIn.setBusi_ref_no(); //业务参考号
		regTellerSeqIn.setRemark(acctOut.getRemark()); // 备注
		regTellerSeqIn.setSett_status(E_SETTSTATE.NONE); // 清算
		// regTellerSeqIn.setSett_batch_no(); //清算批次号
		regTellerSeqIn.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
		
		regTellerSeqIn.setTrxn_seq(CommTools.prcRunEnvs().getTransq());
		
		FaAccounting.regTellerSeq(regTellerSeqIn);
		return acctOut;
	}
	
	public static FaAcctInfo openBaseAccount(FaAcctInfo openBaseacctIn, boolean autoOpen) {
		BIZLOG.method("FaOpenAccount.openBaseAccountBegin>>>>>>>>>>>>");
		BIZLOG.debug("openBaseacctIn[%s] autoOpen=[%s]", openBaseacctIn, autoOpen);

		// 检查非空
		checkNull(openBaseacctIn);

		String acctSeq = ApKnpPara.getKnpPara("ACCT_SEQ", "BASE_ACCOUNT").getPmval1();

		// 如果输入了账户类型，判断是否正确，否则赋值
		if (CommUtil.isNotNull(openBaseacctIn.getAcct_type())) {

			if (openBaseacctIn.getAcct_type() != E_ACCTTYPE.BASE_ACCOUNT) {
				throw GlError.GL.E0003(E_ACCTTYPE.BASE_ACCOUNT.getLongName());
			}
		} else {
			openBaseacctIn.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT);
		}

		if(CommUtil.isNull(openBaseacctIn.getAcct_seq())){
		    openBaseacctIn.setAcct_seq(acctSeq);
		}

		FaAcctInfo openAcctOut = SysUtil.getInstance(FaAcctInfo.class);
		// 判断是否已开户
		faa_account accountTable = Faa_accountDao.selectOne_odb3(openBaseacctIn.getSys_no(), openBaseacctIn.getAcct_branch(), openBaseacctIn.getGl_code(),
				openBaseacctIn.getCcy_code(), openBaseacctIn.getAcct_seq(), false);

		if (autoOpen) {
			// 如果该类账户已经销户且允许开户，则重新启用
			if (CommUtil.isNotNull(accountTable) && accountTable.getAcct_status() == E_ACCTSTATUS.CLOSE && openBaseacctIn.getOpen_acct_ind() == E_YESORNO.YES) {
				restartAcct(accountTable);
			} else if (CommUtil.isNull(accountTable) && openBaseacctIn.getOpen_acct_ind() == E_YESORNO.YES) {
	
				FaSubmitAcct submitAcct = setSumitActt(openBaseacctIn);
	
				accountTable = submitAcct(submitAcct);
	
			}
		}
		if (accountTable != null) {
			getAcctCom(openAcctOut, accountTable);
		}

		BIZLOG.debug("openAcctOut[%s]", openAcctOut);

		return openAcctOut;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年2月23日-上午11:01:27</li>
	 *         <li>功能说明：开基本户</li>
	 *         </p>
	 * @param openBaseacctIn
	 * @return
	 */
	public static FaAcctInfo openBaseAccount(FaAcctInfo openBaseacctIn) {
		return openBaseAccount(openBaseacctIn, true);
	}

	private static void checkNull(FaAcctInfo openBaseacctIn) {
		// 系统编号不许为空
		CommTools.fieldNotNull(openBaseacctIn.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
		fap_sys_define  tbfapSysDefine = Fap_sys_defineDao.selectOne_odb1(openBaseacctIn.getSys_no(), false);
		if(CommUtil.isNull(tbfapSysDefine)){
			throw GlError.GL.E0200(openBaseacctIn.getSys_no());
		}
		
		//select * from fap_sys_define a
		// 判断系统是否存在
		//ApDropList.exists(FaConst.SYS_NO, openBaseacctIn.getSys_no());
		
		// 财务机构不允许为空
		CommTools.fieldNotNull(openBaseacctIn.getAcct_branch(), GlDict.A.acct_branch.getId(), GlDict.A.acct_branch.getLongName());
		// 科目号不允许为空
		CommTools.fieldNotNull(openBaseacctIn.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		// 开户标志不许为空
		CommTools.fieldNotNull(openBaseacctIn.getOpen_acct_ind(), GlDict.A.open_acct_ind.getId(), GlDict.A.open_acct_ind.getLongName());
		// 货币代码不允许为空
		CommTools.fieldNotNull(openBaseacctIn.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		// 检查财务机构是否合法
		//if (!ApBranch.exists(openBaseacctIn.getAcct_branch())) {
		//	throw ApPubErr.APPUB.E0005(OdbFactory.getTable(KubBrch.class).getLongname(), GlDict.A.acct_branch.getLongName(), openBaseacctIn.getAcct_branch());
		//}
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年2月24日-上午11:17:33</li>
	 *         <li>功能说明：总账账户开专户</li>
	 *         </p>
	 * @param openSpecialAcctIn
	 * @return
	 */
	public static FaAcctInfo openSpecialAccount(FaAcctInfo openSpecialAcctIn) {

		BIZLOG.method("FaOpenAccount.openSpecialAccount begin >>>>>>>>>>>>");
		BIZLOG.debug("openBaseacctIn[%s]", openSpecialAcctIn);

		FaAcctInfo openSpecialAcctOut = SysUtil.getInstance(FaAcctInfo.class);

		checkNull(openSpecialAcctIn);

		// 如果输入了账户类型，判断是否正确，否则赋值
		if (CommUtil.isNotNull(openSpecialAcctIn.getAcct_type())) {

			if (openSpecialAcctIn.getAcct_type() != E_ACCTTYPE.SPECIAL_ACCOUNT) {
				throw GlError.GL.E0003(E_ACCTTYPE.SPECIAL_ACCOUNT.getLongName());
			}
		} else {
			openSpecialAcctIn.setAcct_type(E_ACCTTYPE.SPECIAL_ACCOUNT);
		}

		// 账户序号不允许为空
		CommTools.fieldNotNull(openSpecialAcctIn.getAcct_seq(), GlDict.A.acct_seq.getId(), GlDict.A.acct_seq.getLongName());

		String minSeq = ApKnpPara.getKnpPara("ACCT_SEQ", "SPECILA_ACCOUNT_MIN_SEQ").getPmval1();
		String maxSeq = ApKnpPara.getKnpPara("ACCT_SEQ", "SPECILA_ACCOUNT_MAX_SEQ").getPmval1();
		// 判断账户序号是否合法
		checkAcctSeqScale(openSpecialAcctIn, minSeq, maxSeq);

		// 判断是否已开户
		faa_account accountTable = Faa_accountDao.selectOne_odb3(openSpecialAcctIn.getSys_no(), openSpecialAcctIn.getAcct_branch(), openSpecialAcctIn.getGl_code(),
				openSpecialAcctIn.getCcy_code(), openSpecialAcctIn.getAcct_seq(), false);

		// 如果该类账户已经销户且允许开户，则重新启用
		if (CommUtil.isNotNull(accountTable)) {
			throw GlError.GL.E0005(openSpecialAcctIn.getAcct_seq());
		}
		
		// 没开户则自动开户， 且开户标志为yes，则自动开户
		if (accountTable == null && openSpecialAcctIn.getOpen_acct_ind() == E_YESORNO.YES) {

			// 赋值
			FaSubmitAcct submitAcct = setSumitActt(openSpecialAcctIn);
			accountTable = submitAcct(submitAcct);
		}

		getAcctCom(openSpecialAcctOut, accountTable);
		BIZLOG.debug("openSpecialAcctOut[%s]", openSpecialAcctOut);
		BIZLOG.method("FaOpenAccount.openSpecialAccount end >>>>>>>>>>>>");

		return openSpecialAcctOut;

	}

	private static void restartAcct(faa_account accountTable) {
		accountTable.setAcct_status(E_ACCTSTATUS.NORMAL);
		// 销户信息质空
		accountTable.setClose_acct_brch("");
		accountTable.setClose_acct_date("");
		accountTable.setClose_acct_seq("");
		accountTable.setClose_acct_user("");
	}

	private static FaSubmitAcct setSumitActt(FaAcctInfo openSpecialAcctIn) {

		FaSubmitAcct submitAcct = SysUtil.getInstance(FaSubmitAcct.class);

		submitAcct.setSys_no(openSpecialAcctIn.getSys_no()); // 系统编号
		submitAcct.setAcct_branch(openSpecialAcctIn.getAcct_branch()); // 账务机构
		submitAcct.setGl_code(openSpecialAcctIn.getGl_code()); // 科目号
		submitAcct.setCcy_code(openSpecialAcctIn.getCcy_code()); // 货币代码
		submitAcct.setAcct_type(openSpecialAcctIn.getAcct_type()); // 账户分类
		//submitAcct.setOn_bal_sheet_ind(openSpecialAcctIn.getOn_bal_sheet_ind()); // 表内标志
		submitAcct.setBal_check_ind(openSpecialAcctIn.getBal_check_ind()); // 余额检查标志
		submitAcct.setDebit_manual_allow(openSpecialAcctIn.getDebit_manual_allow()); // 借方手工记账许可
		submitAcct.setCredit_manual_allow(openSpecialAcctIn.getCredit_manual_allow()); // 贷方手工记账许可
		submitAcct.setAcct_seq(openSpecialAcctIn.getAcct_seq()); // 账户序号
		submitAcct.setRemark(openSpecialAcctIn.getRemark()); // 备注
		return submitAcct;
	}

	public static FaAcctInfo openManulAcct(FaAcctInfo openManulAcctIn) {

		BIZLOG.method("FaAcctInfo.openManulAcct begin >>>>>>>>>");
		BIZLOG.debug("manulAcctIn[%s]", openManulAcctIn);

		FaAcctInfo openManulAcctOut = SysUtil.getInstance(FaAcctInfo.class);
		// 检查是否为空
		checkNull(openManulAcctIn);
		
		// 如果输入了账户类型，判断是否正确，否则赋值
		        
		if (CommUtil.isNotNull(openManulAcctIn.getAcct_type())) {

			if (openManulAcctIn.getAcct_type() != E_ACCTTYPE.MUNUAL_ACCOUNR) {
				throw GlError.GL.E0003(E_ACCTTYPE.MUNUAL_ACCOUNR.getLongName());
			}
		} else {
			openManulAcctIn.setAcct_type(E_ACCTTYPE.MUNUAL_ACCOUNR);
		}
		// 账户序号不允许为空
		//CommTools.fieldNotNull(openManulAcctIn.getAcct_seq(), GlDict.A.acct_seq.getId(), GlDict.A.acct_seq.getLongName());
		//账户序号改为按科目号与系统编号（420）查询出最大序号+1
		openManulAcctIn.setAcct_seq(getMaxSeqAddOne(openManulAcctIn));
		//获取配置好的最序号区间
		String minSeq = ApKnpPara.getKnpPara("ACCT_SEQ", "MANUL_ACCOUNT_MIN_SEQ").getPmval1();
		String maxSeq = ApKnpPara.getKnpPara("ACCT_SEQ", "MANUL_ACCOUNT_MAX_SEQ").getPmval1();

		// 判断账户序号是否合法
		checkAcctSeqScale(openManulAcctIn, minSeq, maxSeq);
		// 判断是否已开户
		faa_account accountTable = Faa_accountDao.selectOne_odb3(openManulAcctIn.getSys_no(), openManulAcctIn.getAcct_branch(), openManulAcctIn.getGl_code(),
				openManulAcctIn.getCcy_code(), openManulAcctIn.getAcct_seq(), false);
		//判断是否允许手工开账户
		fap_accounting_subject subjectInfo = Fap_accounting_subjectDao.selectOne_odb1(openManulAcctIn.getGl_code(), false);
        if(subjectInfo.getManual_open_acct_mode()== E_MANUALOPENACCTMODE.MANUAL_ACCOUNT_IS_NOT_ALLOWED){
            throw GlError.GL.E0207(subjectInfo.getGl_code());
        }
        //是否末级科目校验
        if(subjectInfo.getEnd_gl_code_ind() == E_YESORNO.NO){
            throw GlError.GL.E0204(subjectInfo.getGl_code());
        }
		// 账户存在，重新输入账户序号
		if (accountTable != null) {
			// 该序号[${acctSqe}]已经手工开户
			throw GlError.GL.E0005(openManulAcctIn.getAcct_seq());
		} else {
			// 赋值
			FaSubmitAcct submitAcct = setSumitActt(openManulAcctIn);

			accountTable = submitAcct(submitAcct);

		}
		
		openManulAcctOut.setSys_no(accountTable.getSys_no());
		openManulAcctOut.setAcct_no(accountTable.getAcct_no());
		openManulAcctOut.setRemark(accountTable.getRemark());

		BIZLOG.method("FaAcctInfo.openManulAcct end >>>>>>>>>");
		BIZLOG.debug("openManulAcctOut[%s]", openManulAcctOut);
		return openManulAcctOut;

	}

	private static void checkAcctSeqScale(FaAcctInfo openManulAcctIn, String minSeq, String maxSeq) {
		// 判断账户序号是否在范围内
		if (CommUtil.compare(openManulAcctIn.getAcct_seq(), minSeq) < 0 || CommUtil.compare(openManulAcctIn.getAcct_seq(), maxSeq) > 0) {
			// 账户序号[${acctSeq}]不在范围[${minSeq}]-[${maxSeq}]内。
			throw GlError.GL.E0004(minSeq, maxSeq);
		}
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年2月23日-上午11:01:48</li>
	 *         <li>功能说明：表类型到复合类型的转换</li>
	 *         </p>
	 * @param openAcctOut
	 * @param accountTable
	 */
	public static void getAcctCom(FaAcctInfo openAcctOut, faa_account accountTable) {
		CommUtil.copyProperties(openAcctOut, accountTable);
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年2月23日-上午11:02:22</li>
	 *         <li>功能说明：内部户开户提交</li>
	 *         </p>
	 * @param submitAcct
	 * @return
	 */
	public static faa_account submitAcct(FaSubmitAcct submitAcct) {

		BIZLOG.method("FaOpenAccount.submitAcct begin >>>>>>>>>>>>");
		BIZLOG.debug("submitAcct[%s]", submitAcct);

		// 获取公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 系统编号不许为空
		CommTools.fieldNotNull(submitAcct.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
		// 财务机构不允许为空
		CommTools.fieldNotNull(submitAcct.getAcct_branch(), GlDict.A.acct_branch.getId(), GlDict.A.acct_branch.getLongName());
		// 科目号不允许为空
		CommTools.fieldNotNull(submitAcct.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		// 货币代码不允许为空
		CommTools.fieldNotNull(submitAcct.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		// 账户序号不允许为空
		CommTools.fieldNotNull(submitAcct.getAcct_seq(), GlDict.A.acct_seq.getId(), GlDict.A.acct_seq.getLongName());
		// 账户分类不允许为空
		CommTools.fieldNotNull(submitAcct.getAcct_type(), GlDict.A.acct_type.getId(), GlDict.A.acct_type.getLongName());
		// 表内外标志
		//CommTools.fieldNotNull(submitAcct.getOn_bal_sheet_ind(), GlDict.A.on_bal_sheet_ind.getId(), GlDict.A.on_bal_sheet_ind.getLongName());

		faa_account acctTable = SysUtil.getInstance(faa_account.class);

		fap_accounting_subject subjectInfo = Fap_accounting_subjectDao.selectOne_odb1(submitAcct.getGl_code(), false);
		
		//科目号不能为空且必须是末级科目
		if (subjectInfo == null) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), submitAcct.getGl_code());
		}
		
		
		submitAcct.setOn_bal_sheet_ind(subjectInfo.getOn_bal_sheet_ind());
		// 判断货币代码是否存在
//		if (!ApCurrency.exists(submitAcct.getCcy_code())) {
//			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppCrcy.class).getLongname(), GlDict.A.ccy_code.getId(), submitAcct.getCcy_code());
//		}

		// 判断系统编号是否存在；
//		if (!ApDropList.exists("SYS_NO", submitAcct.getSys_no())) {
//			throw ApPubErr.APPUB.E0004(submitAcct.getSys_no(), "SYS_NO", GlDict.A.sys_no.getLongName());
//		}

		acctTable.setSys_no(submitAcct.getSys_no()); // 系统编号
		acctTable.setAcct_name(subjectInfo.getGl_code_desc()); // 账户名称
		acctTable.setAcct_branch(submitAcct.getAcct_branch()); // 账务机构
		acctTable.setGl_code(submitAcct.getGl_code()); // 科目号
		acctTable.setCcy_code(submitAcct.getCcy_code()); // 货币代码
		acctTable.setAcct_seq(submitAcct.getAcct_seq()); // 账户序号
		acctTable.setAcct_type(submitAcct.getAcct_type()); // 账户分类

		// 表外的余额方向为借方；表内的借方、轧差、双方都在借方，其余为贷方。
		E_DEBITCREDIT balDirection;
		if (submitAcct.getOn_bal_sheet_ind() == E_YESORNO.NO || subjectInfo.getBal_prop() == E_BALPROP.DEBIT || subjectInfo.getBal_prop() == E_BALPROP.BOTH_SIDES
				|| subjectInfo.getBal_prop() == E_BALPROP.NETTING) {
			balDirection = E_DEBITCREDIT.DEBIT; // 余额方向
		} else {
			balDirection = E_DEBITCREDIT.CREDIT;
		}
		acctTable.setBal_direction(balDirection);
		acctTable.setBal_prop(subjectInfo.getBal_prop()); // 余额性质
		acctTable.setAcct_bal(BigDecimal.ZERO); // 账户余额
		acctTable.setPrevious_bal_direction(balDirection); // 上期余额方向
		acctTable.setPrevious_acct_bal(BigDecimal.ZERO); // 上期账户余额
		acctTable.setBal_update_date(runEnvs.getTrandt()); // 余额更新日期

		// 如果无输入则按科目信息
		if (CommUtil.isNotNull(submitAcct.getBal_check_ind())) {

			acctTable.setBal_check_ind(submitAcct.getBal_check_ind()); // 余额检查标志

		} else {
			acctTable.setBal_check_ind(subjectInfo.getBal_check_ind());
		}

		acctTable.setOn_bal_sheet_ind(submitAcct.getOn_bal_sheet_ind()); // 表内标志

		if (CommUtil.isNotNull(submitAcct.getDebit_manual_allow())) {

			acctTable.setDebit_manual_allow(submitAcct.getDebit_manual_allow()); // 借方手工记账许可
		} else {
			acctTable.setDebit_manual_allow(subjectInfo.getDebit_manual_allow());
		}

		if (CommUtil.isNotNull(submitAcct.getCredit_manual_allow())) {
			acctTable.setCredit_manual_allow(submitAcct.getCredit_manual_allow()); // 贷方手工记账许可
		} else {
			acctTable.setCredit_manual_allow(subjectInfo.getCredit_manual_allow());
		}

		acctTable.setAcct_status(E_ACCTSTATUS.NORMAL); // 账户状态
		acctTable.setRemark(submitAcct.getRemark()); // 备注
		acctTable.setOpen_acct_brch(runEnvs.getTranbr()); // 开户机构
		acctTable.setOpen_acct_user(runEnvs.getTranus()); // 开户柜员
		acctTable.setOpen_acct_date(runEnvs.getTrandt()); // 开户日期
		acctTable.setOpen_acct_seq(runEnvs.getTransq()); // 开户流水

		// 取货币代码对应的货币数字代码
		ApCurrency ccyInfo = CommTools.getApCurrency(submitAcct.getCcy_code());
		String acctNo = geneAcctNo(ccyInfo, subjectInfo, submitAcct);
		BIZLOG.debug("acctNo[%s]", acctNo);
		acctTable.setAcct_no(acctNo);
		
		// 增加默认版本号 1
		acctTable.setRecdver(1L);

		Faa_accountDao.insert(acctTable); // 插入数据库

		// 登记审计
		ApDataAudit.regLogOnInsertParameter(acctTable);

		BIZLOG.debug("acctTable[%s]", acctTable);

		BIZLOG.method("FaOpenAccount.submitAcct end >>>>>>>>>>>>");

		return acctTable;

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月2日-下午6:31:54</li>
	 *         <li>功能说明：生成总账账号</li>
	 *         </p>
	 * @param ccyInfo
	 * @param subjectInfo
	 * @param submitAcct
	 * @return
	 */
	private static String geneAcctNo(ApCurrency ccyInfo, fap_accounting_subject subjectInfo, FaSubmitAcct submitAcct) {

		// 清空缓存区
		ApBuffer.clear();

		ApBuffer.addData(ApConstants.CURRENCY_DATA_MART, CommUtil.toMap(ccyInfo)); // 币种信息
		// ApSeq.addDataToBuffer(ApConst.INPUT_DATA_MART,
		// CommUtil.toMap(openAccountIn)); // 输入参数
		ApBuffer.addData(ApConstants.PARM_DATA_MART, CommUtil.toMap(subjectInfo));// 添加参数到缓存区
		ApBuffer.appendData(ApConstants.PARM_DATA_MART, CommUtil.toMap(submitAcct));

		String accountNo = ApSeq.genSeq("GL_ACCOUNT_NO"); // 生成内部账号

		BIZLOG.debug("genAccountNo=[%s]", accountNo);

		return accountNo;

	}

	/**
	 * @Author dengyu
	 *         <p>
	 *         <li>2017年3月10日-上午10:23:56</li>
	 *         <li>功能说明：批量开户</li>
	 *         </p>
	 * @param orgId
	 *            法人代码
	 * @param acctNo
	 *            账号
	 */
	public static void prcbatchOpen(String orgId, FaAcctDefineInfo acctDefineInfo) {

		fap_accounting_subject subject = Fap_accounting_subjectDao.selectOne_odb1(acctDefineInfo.getGl_code(), false);
		// 非末级科目跳过
		if (subject.getEnd_gl_code_ind() == E_YESORNO.NO) {
			BIZLOG.debug("-----传入为末级科目跳过-----");
			return;
		}
		// 停用的科目跳过
		if (subject.getValid_ind() == E_YESORNO.NO) {
			BIZLOG.debug("-----传入为停用科目跳过-----");
			return;
		}
		// 范围为所有实体账务机构
		if (acctDefineInfo.getBatch_open_brch_scope() == E_BATCHOPENBRCHSCOPE.ALL) {
			// 获取所有实体机构
		    List<IoBrComplexType.IoBrchInfo> branch = SysUtil.getInstance(IoSrvPbBranch.class).getRealBranchList();

			for (IoBrchInfo info : branch) {
				// 获取币种
				List<String> ccyCodeList = getCcycode(acctDefineInfo);

				// 开户
				for (String ccy_code : ccyCodeList) {

					FaAcctInfo acctInfo = SysUtil.getInstance(FaAcctInfo.class);

					acctInfo.setSys_no(FaConst.GL_SYSTEM);// 系统编号
					acctInfo.setCcy_code(ccy_code);// 货币代码
					acctInfo.setAcct_branch(info.getBrchno());// 批开机构
					acctInfo.setAcct_type(E_ACCTTYPE.SPECIAL_ACCOUNT);// 账户类别
					acctInfo.setAcct_seq(acctDefineInfo.getAcct_seq());// 账户序号
					acctInfo.setGl_code(acctDefineInfo.getGl_code());// 科目号
					acctInfo.setOpen_acct_ind(E_YESORNO.YES);// 开户标志

					openSpecialAccount(acctInfo);
				}
			}
		} else {// 指定机构
			// 获取币种
			List<String> ccyCodeList = getCcycode(acctDefineInfo);

			// 开户
			for (String ccyCode : ccyCodeList) {

				FaAcctInfo acctInfo = SysUtil.getInstance(FaAcctInfo.class);

				acctInfo.setSys_no(FaConst.GL_SYSTEM);// 系统编号
				acctInfo.setCcy_code(ccyCode);// 货币代码
				acctInfo.setAcct_branch(acctDefineInfo.getMulti_draw_ind());// 批开机构
				acctInfo.setAcct_type(E_ACCTTYPE.SPECIAL_ACCOUNT);// 账户类别
				acctInfo.setAcct_seq(acctDefineInfo.getAcct_seq());// 账户序号
				acctInfo.setGl_code(acctDefineInfo.getGl_code());// 科目号
				acctInfo.setOpen_acct_ind(E_YESORNO.YES);// 开户标志

				openSpecialAccount(acctInfo);

			}

		}

	}

	/**
	 * @Author dengyu
	 *         <p>
	 *         <li>2017年3月10日-上午11:23:29</li>
	 *         <li>功能说明：字符串解析</li>
	 *         </p>
	 * @param stringlist
	 * @return
	 */
	public static List<String> resolveString(String stringlist) {

		List<String> list = new ArrayList<String>();

		// 传入字符串为空 直接返回空对象
		if (CommUtil.isNull(stringlist))
			return list;

		// 传入不为空
		stringlist = stringlist.replace("，", ",").replace(" ", "");// 全角转化为半角,去空格
		String[] stringlist_tep = stringlist.split(",");// 将字符串拆成字符串数组
		for (String info : stringlist_tep) {
			if (CommUtil.isNotNull(info))
				list.add(info);
		}
		return list;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月13日-下午3:20:20</li>
	 *         <li>功能说明：获取批开账户的币种</li>
	 *         </p>
	 * @param acctDefineInfo
	 */
	private static List<String> getCcycode(FaAcctDefineInfo acctDefineInfo) {

		// 获取币种
		List<String> ccyCodeList = new ArrayList<String>();
		// 如果为空则为全币种
		if (CommUtil.isNull(acctDefineInfo.getAvailable_ccy_list())) {
			// 获取币种的下拉列表值
			List<ApCurrency> ccyList = CommTools.listApCurrency();

			// 将币种加入 ccyCodeList
			for (ApCurrency listInfo : ccyList)
				ccyCodeList.add(listInfo.getCrcycd());
		} else {// 指定币种
			ccyCodeList = resolveString(acctDefineInfo.getAvailable_ccy_list());
		}
		return ccyCodeList;

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月10日-上午9:33:00</li>
	 *         <li>功能说明：根据科目号取基本账户信息</li>
	 *         </p>
	 * @param accrueBook
	 * @param subjectNo
	 * @return
	 */

	public static FaAcctInfo getAcctBySubject(String sysNo, String ccyCode, String acctBranch, String subjectNo) {

		FaAcctInfo openIn = SysUtil.getInstance(FaAcctInfo.class);

		// 账户序号
		String acctSeq = ApKnpPara.getKnpPara("ACCT_SEQ", "BASE_ACCOUNT").getPmval1();

		openIn.setSys_no(sysNo); // 系统编号
		openIn.setCcy_code(ccyCode); // 货币代码
		openIn.setAcct_seq(acctSeq); // 账户序号
		openIn.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT); // 账户分类
		openIn.setAcct_branch(acctBranch); // 财务机构
		openIn.setGl_code(subjectNo); // 科目号
		openIn.setOn_bal_sheet_ind(E_YESORNO.YES);
		openIn.setOpen_acct_ind(E_YESORNO.YES);

		FaAcctInfo acctTable = FaOpenAccount.openBaseAccount(openIn);

		return acctTable;
	}
	
	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月10日-上午9:33:00</li>
	 *         <li>功能说明：根据科目号、账户序号取基本账户信息</li>
	 *         </p>
	 * @param accrueBook
	 * @param subjectNo
	 * @return
	 */

	public static FaAcctInfo getAcctBySubjectAndAcctseq(String sysNo, String ccyCode, String acctBranch, String subjectNo, String acctSeq) {

		FaAcctInfo openIn = SysUtil.getInstance(FaAcctInfo.class);

		openIn.setSys_no(sysNo); // 系统编号
		openIn.setCcy_code(ccyCode); // 货币代码
		openIn.setAcct_seq(acctSeq); // 账户序号
		openIn.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT); // 账户分类
		openIn.setAcct_branch(acctBranch); // 财务机构
		openIn.setGl_code(subjectNo); // 科目号
		openIn.setOn_bal_sheet_ind(E_YESORNO.YES);
		openIn.setOpen_acct_ind(E_YESORNO.YES);

		FaAcctInfo acctTable = FaOpenAccount.openBaseAccount(openIn);

		return acctTable;
	}

	/**  
	* @Title: getMaxSeqAddOne  
	* @Description: 按科目号与系统编号查询已开出的最大序号  
	* @Author xionglz
	* @param @param openManulAcctIn
	* @param @return     
	* @return String    返回类型  
	* @throws  
	*/  
	public static String getMaxSeqAddOne(FaAcctInfo openManulAcctIn){
	    //按科目号与系统编号查询已开出的最大序号
	    String acctseq= FaAccountDao.selMaxSeq(		openManulAcctIn.getCorpno(),
	    											openManulAcctIn.getGl_code(),
	                                                openManulAcctIn.getSys_no(),
	                                                E_ACCTTYPE.MUNUAL_ACCOUNR,false);
	    
	    BIZLOG.debug("acctseq=[%s]", acctseq);
	    //如果最大序号为空，则给默认最小值
	    if(CommUtil.isNull(acctseq)){
	        acctseq = ApKnpPara.getKnpPara("ACCT_SEQ", "MANUL_ACCOUNT_MIN_SEQ").getPmval1();
	    } else{
	        //将字符串类型转化为整型并且+1
	        int iacctseq=Integer.parseInt(acctseq)+1;
	        //将整型转换为字符串类型
	        acctseq=Integer.toString(iacctseq);
	    }	    
	    return acctseq;
	}
}
