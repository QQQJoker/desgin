package cn.sunline.ltts.busi.gl.item;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.core.exception.AdpDaoNoDataFoundException;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.gl.namedsql.GlExchangeDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.Gla_exchange_glDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.Glb_exchange_processDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.Glb_exchange_vochDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.gla_exchange_gl;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.glb_exchange_process;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.glb_exchange_voch;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlItemGL;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlSubjectBal;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlTranData;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlVochBalData;
import cn.sunline.ltts.busi.gl.type.GlExchange.GlExchangeGL;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoCompPbBranch.PbBranchUpLow;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.busi.sys.type.PbEnumType;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;

/**
 * <p>
 * 文件功能说明：
 * 
 * </p>
 * 
 * @Author ThinkPad
 *         <p>
 *         <li>2017年3月6日-下午3:10:17</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年3月6日-ThinkPad：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class GlExchange {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlExchange.class);

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月7日-下午4:36:31</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param report_type
	 */
	public static void prcBefore(String orgId, String trxnDate, E_REPORTTYPE reportType) {

		BIZLOG.method("prcBefore begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId=[%s]  trxnDate=[%s] report_type=[%s]", orgId, trxnDate, reportType);

		// 生成折币汇率在导入文件时生成
		// 先删除（支持重复处理）
		// 删除折算过程
		GlExchangeDao.delExchangeBook(orgId, trxnDate, reportType);
		GlExchangeDao.delConvertVoch(orgId, trxnDate, reportType);

		BIZLOG.method("prcBefore end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月7日-下午4:36:41</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param report_type
	 * @param branchId
	 */
	public static void prcExchange(String orgId, String trxnDate, E_REPORTTYPE reportType, String branchId) {
		BIZLOG.method("prcExchange >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]trxnDate[%s] report_type[%s] branchId[%s] ", orgId, trxnDate, reportType, branchId);

		// genExchangeVoch(orgId, trxnDate, branchId, "*", report_type);

		// 上日日期
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String lastDate = runEnvs.getLstrdt();

		// 从上日折合总账装载期初数据到折算过程表glb_exchange_process
		GlExchangeDao.insInitDataProcess(orgId, trxnDate, branchId, lastDate, reportType);

		// 按项目对折算过程表进行加工处理
		prcExchangeBook(orgId, trxnDate, branchId, reportType);

		// 凭证落地
		GlExchangeDao.insExchangeVoch(orgId, trxnDate, branchId, reportType);

		BIZLOG.method("prcExchange <<<<<<<<<<<<End<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月7日-下午4:36:44</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param report_type
	 */
	private static void prcExchangeBook(String orgId, String trxnDate, String branchId, E_REPORTTYPE reportType) {
		BIZLOG.method("prcExchangeBook >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]trxnDate[%s] branchId[%s] report_type[%s]  ", orgId, trxnDate, branchId, reportType);

		// 准备数据到 gla_exchange_ledger
		// 先删除中间数据gla_exchange_ledger
		GlExchangeDao.delDetailBuffer(orgId, trxnDate, branchId, reportType);

		String exchangeRateMode = FaTools.getExchangeRateMode();
		BIZLOG.parm("exchangeRateMode [%s] ", exchangeRateMode);
		if (exchangeRateMode == null)
			exchangeRateMode = FaConst.EXCHANGE_RATE_MODE_CORE;

		int count = 0;

		// 业务报表的发生和余额，从原币总账中获取
		if (CommUtil.compare(exchangeRateMode, FaConst.EXCHANGE_RATE_MODE_CORE) == 0) {// 2折算汇率自动来自核心系统
			count = GlExchangeDao.insDetailFromGL(orgId, trxnDate, branchId, reportType);
		} else {
			// 1手功维护折算汇率
			count = GlExchangeDao.insDetailFromGL2(orgId, trxnDate, branchId, reportType);
		}
		if (count == 0) {
			BIZLOG.method("record is 0 prcExchangeBook <<<<<<<<<<<<End<<<<<<<<<<<<");
			return;
		}

		// 将gla_exchange_ledger 做汇总，开始折算处理
		List<GlExchangeGL> lstExchangeGL = GlExchangeDao.lstExchangeData(orgId, trxnDate, branchId, reportType, false);

		if (CommUtil.isNotNull(lstExchangeGL) && lstExchangeGL.size() > 0) {

			for (GlExchangeGL cplInfo : lstExchangeGL) {

				/* 更新折合过程登记簿 */
				prcExchangeProfitLoss(orgId, trxnDate, branchId, cplInfo, reportType);
			}
		}

		BIZLOG.method("prcExchangeBook <<<<<<<<<<<<End<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月7日-下午4:36:48</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param cplInfo
	 * @param report_type
	 */
	private static void prcExchangeProfitLoss(String orgId, String trxnDate, String branchId, GlExchangeGL cplInfo, E_REPORTTYPE reportType) {
		BIZLOG.method("prcExchangeProfitLoss >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]trxnDate[%s] branchId[%s] exchangeFlatSubject[%s] report_type[%s] ", orgId, trxnDate, branchId, cplInfo, reportType);

		if (CommUtil.isNull(cplInfo)) {
			BIZLOG.method("cplInfo is null,exit!");
			BIZLOG.method("prcExchangeProfitLoss <<<<<<<<<<<<End<<<<<<<<<<<<");
			return;
		}
		BIZLOG.parm("cplInfo [%s]", String.valueOf(cplInfo));
		boolean insFlag = false;/* 插入标记 */

		// 折算补平科目参数
		String exchangeFlatSubject = FaTools.getExchangeFlatSubject();
		if (exchangeFlatSubject == null) {
			throw GlError.GL.E0092(); // 折算补平科目参数未设置
		}
		BIZLOG.parm("exchangeFlatSubject [%s] ", exchangeFlatSubject);

		glb_exchange_process tabExchangeProcess = SysUtil.getInstance(glb_exchange_process.class);

		/* 取折合过程记录,取到做更新，未取到做插入 */
		try {
			tabExchangeProcess = Glb_exchange_processDao.selectOne_odb1(reportType, trxnDate, branchId, cplInfo.getExchange_ccy_code(), cplInfo.getGl_code(), true);
		} catch (AdpDaoNoDataFoundException e) {

			tabExchangeProcess.setCorpno(orgId);
			tabExchangeProcess.setReport_type(reportType);
			tabExchangeProcess.setTrxn_date(trxnDate);
			tabExchangeProcess.setBranch_id(branchId);
			tabExchangeProcess.setReport_branch_id(branchId);
			tabExchangeProcess.setExchange_ccy_code(cplInfo.getExchange_ccy_code());
			tabExchangeProcess.setGl_code(cplInfo.getGl_code());
			tabExchangeProcess.setOn_bal_sheet_ind(cplInfo.getOn_bal_sheet_ind());// 表内表外标志

			setAmountToZero(tabExchangeProcess);

			insFlag = true;
		} catch (Exception e) {
			// 读取机构总账数据失败
			throw GlError.GL.E0082(OdbFactory.getTable(glb_exchange_process.class).getLongname());
		}

		// 给借、贷方发生额、余额赋值
		tabExchangeProcess.setCurrent_debit_amt(cplInfo.getExchange_debit_amt());/* 即期汇率借方发生 */
		tabExchangeProcess.setCurrent_credit_amt(cplInfo.getExchange_credit_amt());/* 即期汇率贷方发生 */

		tabExchangeProcess.setCurrent_debit_bal(cplInfo.getExchange_debit_bal());/* 本期借方余额 */
		tabExchangeProcess.setCurrent_credit_bal(cplInfo.getExchange_credit_bal());/* 本期贷方余额 */

		// 计算借贷方补平金额
		BigDecimal bigLeft = tabExchangeProcess.getPrev_debit_bal();
		bigLeft = bigLeft.subtract(tabExchangeProcess.getPrev_credit_bal());
		bigLeft = bigLeft.add(tabExchangeProcess.getCurrent_debit_amt());
		bigLeft = bigLeft.subtract(tabExchangeProcess.getCurrent_credit_amt());

		BigDecimal bigRight = tabExchangeProcess.getCurrent_debit_bal();
		bigRight = bigRight.subtract(tabExchangeProcess.getCurrent_credit_bal());

		BigDecimal bigFlatDebitAmt = BigDecimal.ZERO; // 借方补平金额
		BigDecimal bigFlatCreditAmt = BigDecimal.ZERO; // 贷方补平金额 */

		if (CommUtil.compare(bigRight, bigLeft) > 0) {
			bigFlatDebitAmt = bigRight.subtract(bigLeft);// 借方补平金额
		} else if (CommUtil.compare(bigRight, bigLeft) < 0) {
			bigFlatCreditAmt = bigLeft.subtract(bigRight);// 贷方补平金额 */
		}
		tabExchangeProcess.setDebit_adj_amt(bigFlatDebitAmt);
		tabExchangeProcess.setCredit_adj_amt(bigFlatCreditAmt);

		// 更新数据库
		if (insFlag) {
			Glb_exchange_processDao.insert(tabExchangeProcess);
		} else {
			Glb_exchange_processDao.updateOne_odb1(tabExchangeProcess);
		}

		// 补汇兑损益 —— 开始另外一边账务处理
		if (CommUtil.compare(bigFlatDebitAmt, BigDecimal.ZERO) == 0 && CommUtil.compare(bigFlatCreditAmt, BigDecimal.ZERO) == 0) {
			BIZLOG.method("flat amt is 0 ,exit!");
			BIZLOG.method("prcExchangeProfitLoss <<<<<<<<<<<<End<<<<<<<<<<<<");
			return;
		}
		if (cplInfo.getOn_bal_sheet_ind() == E_YESORNO.NO) {
			BIZLOG.method("not table in subject ,exit!");
			BIZLOG.method("prcExchangeProfitLoss <<<<<<<<<<<<End<<<<<<<<<<<<");
			return;
		}

		glb_exchange_process tabExchangeProcessFlat = SysUtil.getInstance(glb_exchange_process.class);

		try {
			insFlag = false;
			tabExchangeProcessFlat = Glb_exchange_processDao.selectOne_odb1(reportType, trxnDate, branchId, cplInfo.getExchange_ccy_code(), exchangeFlatSubject, true);
		} catch (AdpDaoNoDataFoundException e) {

			tabExchangeProcessFlat.setCorpno(orgId);
			tabExchangeProcessFlat.setReport_type(reportType);
			tabExchangeProcessFlat.setTrxn_date(trxnDate);
			tabExchangeProcessFlat.setBranch_id(branchId);
			tabExchangeProcessFlat.setReport_branch_id(branchId);
			tabExchangeProcessFlat.setExchange_ccy_code(cplInfo.getExchange_ccy_code());
			tabExchangeProcessFlat.setGl_code(exchangeFlatSubject);
			tabExchangeProcessFlat.setOn_bal_sheet_ind(cplInfo.getOn_bal_sheet_ind());// 表内表外标志

			setAmountToZero(tabExchangeProcessFlat);

			insFlag = true;
		} catch (Exception e) {
			// 读取折算过程数据失败
			throw GlError.GL.E0082(OdbFactory.getTable(glb_exchange_process.class).getLongname());
		}

		/* 对汇兑损益科目的补平处理，补平金额记在反方向 */
		tabExchangeProcessFlat.setDebit_adj_amt(tabExchangeProcessFlat.getDebit_adj_amt().add(bigFlatCreditAmt));
		tabExchangeProcessFlat.setCredit_adj_amt(tabExchangeProcessFlat.getCredit_adj_amt().add(bigFlatDebitAmt));

		/* 更新数据库 */
		if (insFlag) {
			Glb_exchange_processDao.insert(tabExchangeProcessFlat);
		} else {
			Glb_exchange_processDao.updateOne_odb1(tabExchangeProcessFlat);
		}
		BIZLOG.method("prcExchangeProfitLoss <<<<<<<<<<<<End<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月7日-下午4:36:56</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param tabExchangeProcess
	 */
	private static void setAmountToZero(glb_exchange_process tabExchangeProcess) {

		tabExchangeProcess.setPrev_debit_bal(BigDecimal.ZERO);
		tabExchangeProcess.setPrev_credit_bal(BigDecimal.ZERO);
		tabExchangeProcess.setCurrent_debit_amt(BigDecimal.ZERO);
		tabExchangeProcess.setCurrent_credit_amt(BigDecimal.ZERO);
		tabExchangeProcess.setCurrent_debit_bal(BigDecimal.ZERO);
		tabExchangeProcess.setCurrent_credit_bal(BigDecimal.ZERO);
		tabExchangeProcess.setDebit_adj_amt(BigDecimal.ZERO);
		tabExchangeProcess.setCredit_adj_amt(BigDecimal.ZERO);
		tabExchangeProcess.setDebit_adj_err_amt(BigDecimal.ZERO);
		tabExchangeProcess.setCredit_adj_err_amt(BigDecimal.ZERO);
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月8日-下午3:59:55</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param report_type
	 */
	public static void prcFillBal(String orgId, String trxnDate, E_REPORTTYPE reportType) {
		BIZLOG.method("prcFillBal >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]trxnDate[%s] report_type[%s]  ", orgId, trxnDate, reportType);

		// 获取折算差额科目
		String exchangeFlatErrorSubject = FaTools.getExchangeFlatSubject();
		if (exchangeFlatErrorSubject == null) {
			throw GlError.GL.E0093(); // 折算差额科目参数未设置
		}
		BIZLOG.parm("exchangeFlatErrorSubject [%s] ", exchangeFlatErrorSubject);

		// List<String> lstExchangeCcy = GlExchangeDao.selExchangeCcy(orgId,
		// trxnDate, false);

		// List<ApDropListInfo> exCcyList =
		// ApDropList.getItems(FaConst.EX_CCY_CODE);
		List<KnpPara> exCcyList = FaTools.listExchangeCurrency();

		if (exCcyList.size() > 0) {

			for (KnpPara cplInfo : exCcyList) {

				// 逐层补平
				prcMultiFillBal(orgId, trxnDate, reportType, cplInfo.getPmval1(), exchangeFlatErrorSubject);

				// 将平补数据转化成传票
				GlExchangeDao.insExchangeVochByFill(orgId, trxnDate, reportType, cplInfo.getPmval1());
			}
		}
		// 对折合传票做平衡性检查
		checkVoucher(orgId, trxnDate, reportType);

		BIZLOG.method("prcFillBal <<<<<<<<<<<<End<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月8日-下午3:59:52</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param report_type
	 * @param exchangeCcyCode
	 * @param exchangeFlatErrorSubject
	 */
	public static void prcMultiFillBal(String orgId, String trxnDate, E_REPORTTYPE report_type, String exchangeCcyCode, String exchangeFlatErrorSubject) {
		/*
		 * ApBranchParmCondtion cplInput =
		 * SysUtil.getInstance(ApBranchParmCondtion.class) ;
		 * cplInput.setCorpno(orgId);
		 * cplInput.setReal_branch_ind(E_YESORNO.YES);
		 * 
		 * List<ApBranchInfo> lstBranchInfo =
		 * ApBranchDao.selBranchList(cplInput, false);
		 */
		List<IoBrchInfo> lstBranchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getRealBranchList();

		for (IoBrchInfo cplInfo : lstBranchInfo) {

			prcFillBalBranch(orgId, trxnDate, cplInfo.getBrchno(), report_type, exchangeCcyCode, exchangeFlatErrorSubject);
		}

	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月8日-下午3:59:48</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param report_type
	 * @param exchangeCcyCode
	 * @param exchangeFlatErrorSubject
	 */
	public static void prcFillBalBranch(String orgId, String trxnDate, String branchId, E_REPORTTYPE reportType, String exchangeCcyCode, String exchangeFlatErrorSubject) {
		BIZLOG.method("prcFillBalBranch >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]trxnDate[%s] branchId[%s] report_type[%s] exchangeCcyCode[%s] exchangeFlatErrorSubject[%s]  ", orgId, trxnDate, branchId, reportType, exchangeCcyCode,
				exchangeFlatErrorSubject);

		// 扎差金额（总借减去总贷）
		BigDecimal bigErrorAmt = GlExchangeDao.selFillAmount(orgId, trxnDate, exchangeCcyCode, branchId, reportType, true);

		BIZLOG.debug("branch id %s exchange ccycode %s error amount %s", branchId, exchangeCcyCode, bigErrorAmt);
		if (CommUtil.compare(bigErrorAmt, BigDecimal.ZERO) == 0) {
			BIZLOG.method("error amount is 0");
			BIZLOG.method("prcFillBalBranch <<<<<<<<<<<<End<<<<<<<<<<<<");
			return;
		}

		boolean insFlag = false; /* 插入标记 */
		glb_exchange_process tabExchangeProcess = SysUtil.getInstance(glb_exchange_process.class);

		/* 取折合过程记录,取到做更新，未取到做插入 */
		try {

			tabExchangeProcess = Glb_exchange_processDao.selectOne_odb1(reportType, trxnDate, branchId, exchangeCcyCode, exchangeFlatErrorSubject, true);
		} catch (AdpDaoNoDataFoundException e) {

			tabExchangeProcess.setCorpno(orgId);
			tabExchangeProcess.setReport_type(reportType);
			tabExchangeProcess.setTrxn_date(trxnDate);
			tabExchangeProcess.setBranch_id(branchId);
			tabExchangeProcess.setReport_branch_id(branchId);
			tabExchangeProcess.setExchange_ccy_code(exchangeCcyCode);
			tabExchangeProcess.setGl_code(exchangeFlatErrorSubject);
			tabExchangeProcess.setOn_bal_sheet_ind(E_YESORNO.YES);// 表内表外标志

			setAmountToZero(tabExchangeProcess);

			insFlag = true;
		} catch (Exception e) {
			// 读取机构总账数据失败
			throw GlError.GL.E0082(OdbFactory.getTable(glb_exchange_process.class).getLongname());
		}

		/* 更新借贷方平补金额 */
		if (CommUtil.compare(bigErrorAmt, BigDecimal.ZERO) > 0) {
			// 扎差金额大余零，补在贷方
			tabExchangeProcess.setDebit_adj_err_amt(BigDecimal.ZERO);
			tabExchangeProcess.setCredit_adj_err_amt(bigErrorAmt);
		} else {
			// 扎差金额小余零，补在借方
			tabExchangeProcess.setDebit_adj_err_amt(bigErrorAmt.abs());
			tabExchangeProcess.setCredit_adj_err_amt(BigDecimal.ZERO);
		}

		/* 更新数据库 */
		if (insFlag) {
			Glb_exchange_processDao.insert(tabExchangeProcess);
		} else {
			Glb_exchange_processDao.updateOne_odb1(tabExchangeProcess);
		}

		BIZLOG.method("prcFillBalBranch <<<<<<<<<<<<End<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月8日-下午3:59:44</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param report_type
	 */
	public static void checkVoucher(String orgId, String trxnDate, E_REPORTTYPE reportType) {

		// 借贷方扎差——仅返回不平衡的数据
		List<GlVochBalData> lstVochBalData = GlExchangeDao.lstVoucherNotBalList(orgId, trxnDate, reportType, false);

		if (lstVochBalData.size() > 0) {
			// 币种串
			String ccyCode = "";

			for (GlVochBalData cplInfo : lstVochBalData) {
				BIZLOG.parm("trxnDate[%s] ccyCode=[%s] debit_amt=[%s] credit_amt=[%s]", trxnDate, cplInfo.getCcy_code(), cplInfo.getDebit_amt(), cplInfo.getCredit_amt());
				ccyCode = ccyCode + cplInfo.getCcy_code() + ';';
			}
			// 交易日期[yyyymmdd]货币代码[]不平衡
			throw GlError.GL.E0075(trxnDate, ccyCode); //
		}
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月8日-下午3:59:41</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param report_type
	 */
	public static void prcExchangeGlBefore(String orgId, String trxnDate, E_REPORTTYPE reportType) {
		BIZLOG.method("prcExchangeGlBefore >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]trxnDate[%s] report_type[%s]  ", orgId, trxnDate, reportType);

		// 对折合传票做平衡性检查
		checkVoucher(orgId, trxnDate, reportType);
		// 先删除（支持重复处理）
		GlExchangeDao.delExchangeGL(orgId, trxnDate, reportType);

		BIZLOG.method("prcExchangeGlBefore <<<<<<<<<<<<End<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月8日-下午3:59:36</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param report_type
	 */
	public static void genExchangeGl(String orgId, String trxnDate, String branchId, E_REPORTTYPE reportType) {
		BIZLOG.method("genExchangeGl >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]trxnDate[%s] branchId[%s] report_type[%s]  ", orgId, trxnDate, branchId, reportType);

		// 上日日期
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String lastDate = runEnvs.getLstrdt();

		// 生成初始数据
		GlExchangeDao.insInitDataGl(orgId, trxnDate, branchId, lastDate, reportType);

		// 装载交易数据
		List<GlTranData> lstTranData = GlExchangeDao.lstGlTranData(orgId, trxnDate, branchId, reportType, false);
		int insCnt = 0, updCnt = 0;
		if (CommUtil.isNotNull(lstTranData) && lstTranData.size() > 0) {
			for (GlTranData cplTran : lstTranData) {

				boolean insFlag = false; /* 插入标记 */
				fap_accounting_subject tabSubjectInfo = Fap_accounting_subjectDao.selectOne_odb1(cplTran.getGl_code(), true);
				gla_exchange_gl tabGL = SysUtil.getInstance(gla_exchange_gl.class);

				/* 取总账记录,取到做更新，未取到做插入 */
				try {
					insFlag = false;
					tabGL = Gla_exchange_glDao.selectOne_odb1(reportType, trxnDate, branchId, cplTran.getCcy_code(), cplTran.getGl_code(), true);
				} catch (AdpDaoNoDataFoundException e) {

					tabGL.setCorpno(orgId);
					tabGL.setReport_type(reportType);
					tabGL.setTrxn_date(trxnDate);
					tabGL.setBranch_id(branchId);
					tabGL.setExchange_ccy_code(cplTran.getCcy_code());
					tabGL.setGl_code(cplTran.getGl_code());

					tabGL.setPrev_debit_bal(BigDecimal.ZERO);
					tabGL.setPrev_credit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_debit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_credit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_credit_bal(BigDecimal.ZERO);

					insFlag = true;
				} catch (Exception e) {
					// 读取折币总账数据失败
					throw GlError.GL.E0082(OdbFactory.getTable(gla_exchange_gl.class).getLongname());
				}

				// 发生额处理
				tabGL.setCurrent_debit_amt(cplTran.getDebit_amt());
				tabGL.setCurrent_credit_amt(cplTran.getCredit_amt());
				// 余额计算
				GlSubjectBal cplBal = GlBranch.calSubjectBal(tabSubjectInfo.getBal_prop(), tabGL.getPrev_debit_bal(), tabGL.getPrev_credit_bal(), cplTran.getDebit_amt(), cplTran.getCredit_amt());

				// 贷方余额处理
				tabGL.setCurrent_debit_bal(cplBal.getCurrent_debit_bal());
				tabGL.setCurrent_credit_bal(cplBal.getCurrent_credit_bal());

				/* 更新数据库 */
				if (insFlag) {
					Gla_exchange_glDao.insert(tabGL);
					insCnt = insCnt + 1;
				} else {
					Gla_exchange_glDao.updateOne_odb1(tabGL);
					updCnt = updCnt + 1;
				}
			}
			BIZLOG.debug("gen branch gl process: date%s branch%s occur record%s add count数据%s条, update count%s条", trxnDate, branchId, lstTranData.size(), insCnt, updCnt);
		}

		// 平衡检查
		chkExchangeGlBal(orgId, reportType, trxnDate, branchId);

		BIZLOG.method("genExchangeGl <<<<<<<<<<<<End<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月8日-下午3:59:29</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param reportType
	 * @param trxnDate
	 * @param branchId
	 */
	private static void chkExchangeGlBal(String orgId, E_REPORTTYPE reportType, String trxnDate, String branchId) {

		BIZLOG.method("chkExchangeGlBal begin >>>>>>>>>>>>>>>>>>>>");

		// 横向平衡检查，返回不平衡的总账信息 ,前三 条
		List<GlItemGL> lstGlBalData = GlExchangeDao.lstErrorBalData(orgId, reportType, trxnDate, branchId, 1, 3, false);

		// if (lstGlBalData.size() > 0) {
		long count = lstGlBalData.size();
		if (count > 0) {
			for (GlItemGL cplInfo : lstGlBalData) {
				BIZLOG.error("...branch[%s]ccy_code[%s]subject_no[%s]:[%][%][%s][%s][%][%s][%s]", cplInfo.getBranch_id(), cplInfo.getCcy_code(), cplInfo.getGl_code(), cplInfo.getPrev_debit_bal(),
						cplInfo.getPrev_credit_bal(), cplInfo.getCurrent_debit_amt(), cplInfo.getCurrent_credit_amt(), cplInfo.getCurrent_debit_bal(), cplInfo.getCurrent_credit_bal());
			}
			// 日期[%s]机构[%s]机构日总账不平衡
			throw GlError.GL.E0081(count, trxnDate, lstGlBalData.get(0).getCcy_code(), lstGlBalData.get(0).getBranch_id(), lstGlBalData.get(0).getGl_code());
		}

		BIZLOG.method("chkExchangeGlBal end <<<<<<<<<<<<<<<<<<<<");
	}

	public static void prcExchangeIncomeToZero(String orgId, E_REPORTTYPE reportType, String trxnDate) {
		BIZLOG.method("prcExchangeIncomeToZero begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]reportType[%s] trxnDate[%s] eBaobleix [%s] ", orgId, reportType, trxnDate);

		/**
		 * 查询gla_exchange_gl
		 * 表中余额不为零的损益科目数据，首先结转到本年利润中，直接更新或插入折币总账（有记录直接更新发生）,同时插入折币凭证,本机构操作不需要清算
		 */
		/*
		 * ApBranchParmCondtion cplInput =
		 * SysUtil.getInstance(ApBranchParmCondtion.class) ;
		 * cplInput.setCorpno(orgId);
		 * cplInput.setReal_branch_ind(E_YESORNO.YES);
		 * 
		 * List<ApBranchInfo> lstBranchInfo =
		 * ApBranchDao.selBranchList(cplInput, false);
		 */
		List<IoBrchInfo> lstBranchInfo = SysUtil.getInstance(IoSrvPbBranch.class).getRealBranchList();
		for (IoBrchInfo cplInfo : lstBranchInfo) {

			prcExchangeIncomeToZeroByBranch(orgId, reportType, trxnDate, cplInfo.getBrchno());
		}

		BIZLOG.method("prcExchangeIncomeToZero end <<<<<<<<<<<<<<<<<<<<");
	}

	private static void prcExchangeIncomeToZeroByBranch(String orgId, E_REPORTTYPE reportType, String trxnDate, String branchId) {
		BIZLOG.method("prcExchangeIncomeToZeroByBranch begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]reportType[%s] trxnDate[%s] eBaobleix [%s] branchId[%s]", orgId, reportType, trxnDate, branchId);

		List<GlItemGL> lstIncomeData = GlExchangeDao.lstYearEndIncome(orgId, reportType, trxnDate, branchId, false);

		if (CommUtil.isNotNull(lstIncomeData) && lstIncomeData.size() > 0) {
			// 本年利润科目参数
			String profitSubject = FaTools.getProfitSubject();
			if (profitSubject == null)
				throw GlError.GL.E0053();
			BIZLOG.parm("profitSubject [%s] ", profitSubject);

			for (GlItemGL cplIncomeData : lstIncomeData) {

				boolean insFlag = false;
				E_DEBITCREDIT debitCreditFlag;
				gla_exchange_gl tabGL = SysUtil.getInstance(gla_exchange_gl.class);

				// 根据余额（本期借方余额-本期贷方余额） 进行结转处理
				BigDecimal bigIncomeFlatAmt = cplIncomeData.getCurrent_debit_bal().subtract(cplIncomeData.getCurrent_credit_bal());
				BIZLOG.debug("branch [%s] exchange ccy_code [%s] IncomeAmt [%s]", branchId, cplIncomeData.getCcy_code(), bigIncomeFlatAmt);
				if (CommUtil.compare(bigIncomeFlatAmt, BigDecimal.ZERO) == 0) {
					BIZLOG.method("bigIncomeFlatAmt is 0 continue ");
					continue;
				}

				// 先处理损益科目数据，肯定会有值，直接更新
				tabGL = Gla_exchange_glDao.selectOne_odb1(reportType, trxnDate, branchId, cplIncomeData.getCcy_code(), cplIncomeData.getGl_code(), true);
				// 如果扎差余额在借方，则补贷方发生，清零
				if (CommUtil.compare(bigIncomeFlatAmt, BigDecimal.ZERO) > 0) {
					BIZLOG.debug("Income flat credit occur");
					debitCreditFlag = E_DEBITCREDIT.CREDIT;
					tabGL.setCurrent_credit_amt(cplIncomeData.getCurrent_credit_amt().add(bigIncomeFlatAmt));
				} else {
					BIZLOG.debug("Income flat debit occur");
					debitCreditFlag = E_DEBITCREDIT.DEBIT;
					tabGL.setCurrent_debit_amt(cplIncomeData.getCurrent_debit_amt().add(bigIncomeFlatAmt.abs()));
				}

				tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
				tabGL.setCurrent_credit_bal(BigDecimal.ZERO);

				Gla_exchange_glDao.updateOne_odb1(tabGL);

				// 增加相应分录
				glb_exchange_voch tabVoch = SysUtil.getInstance(glb_exchange_voch.class);
				tabVoch.setReport_type(reportType);
				tabVoch.setTrxn_date(trxnDate);
				tabVoch.setReg_no((long) 19);
				tabVoch.setBranch_id(branchId);
				tabVoch.setExchange_ccy_code(cplIncomeData.getCcy_code());
				tabVoch.setGl_code(cplIncomeData.getGl_code());
				tabVoch.setDebit_credit(debitCreditFlag);
				tabVoch.setAccounting_amt(bigIncomeFlatAmt.abs());
				tabVoch.setOn_bal_sheet_ind(E_YESORNO.YES);
				tabVoch.setRemark("exchange income");
				Glb_exchange_vochDao.insert(tabVoch);

				// 处理本年利润
				// 损益类科目对应本年利润
				fap_accounting_subject tabSubjectInfo = Fap_accounting_subjectDao.selectOne_odb1(profitSubject, true);

				try {
					insFlag = false;
					tabGL = Gla_exchange_glDao.selectOne_odb1(reportType, trxnDate, branchId, cplIncomeData.getCcy_code(), profitSubject, true);
				} catch (AdpDaoNoDataFoundException e) {
					tabGL.setCorpno(orgId);
					tabGL.setReport_type(reportType);
					tabGL.setTrxn_date(trxnDate);
					tabGL.setBranch_id(branchId);
					tabGL.setExchange_ccy_code(cplIncomeData.getCcy_code());
					tabGL.setGl_code(profitSubject);

					tabGL.setPrev_debit_bal(BigDecimal.ZERO);
					tabGL.setPrev_credit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_debit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_credit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_credit_bal(BigDecimal.ZERO);

					insFlag = true;
				} catch (Exception e) {
					// 读取xxx失败
					throw GlError.GL.E0082(OdbFactory.getTable(gla_exchange_gl.class).getLongname());
				}

				// 发生额处理： 如果扎差余额在借方，则发生和余额均补在借方
				if (CommUtil.compare(bigIncomeFlatAmt, BigDecimal.ZERO) > 0) {
					BIZLOG.debug("profit flat debit occur");
					debitCreditFlag = E_DEBITCREDIT.DEBIT;
					tabGL.setCurrent_debit_amt(tabGL.getCurrent_debit_amt().add(bigIncomeFlatAmt));

				} else {
					BIZLOG.debug("profit flat credit occur");
					debitCreditFlag = E_DEBITCREDIT.CREDIT;
					tabGL.setCurrent_credit_amt(tabGL.getCurrent_credit_amt().add(bigIncomeFlatAmt.abs()));
				}

				// 余额计算
				GlSubjectBal cplBal = GlBranch.calSubjectBal(tabSubjectInfo.getBal_prop(), tabGL.getPrev_debit_bal(), tabGL.getPrev_credit_bal(), tabGL.getCurrent_debit_amt(),
						tabGL.getCurrent_credit_amt());

				// 贷方余额处理
				tabGL.setCurrent_debit_bal(cplBal.getCurrent_debit_bal());
				tabGL.setCurrent_credit_bal(cplBal.getCurrent_credit_bal());

				/* 更新数据库 */
				if (insFlag) {
					Gla_exchange_glDao.insert(tabGL);
				} else {
					Gla_exchange_glDao.updateOne_odb1(tabGL);
				}

				// 增加相应分录
				// tabVoch = SysUtil.getInstance(glb_exchange_voch.class);
				tabVoch.setReport_type(reportType);
				tabVoch.setTrxn_date(trxnDate);
				tabVoch.setReg_no((long) 20);
				tabVoch.setBranch_id(branchId);
				tabVoch.setExchange_ccy_code(cplIncomeData.getCcy_code());
				tabVoch.setGl_code(profitSubject);
				tabVoch.setDebit_credit(debitCreditFlag);
				tabVoch.setAccounting_amt(bigIncomeFlatAmt.abs());
				tabVoch.setOn_bal_sheet_ind(E_YESORNO.YES);
				tabVoch.setRemark("exchange income");
				Glb_exchange_vochDao.insert(tabVoch);
			}
		}

		// 平衡检查
		chkExchangeGlBal(orgId, reportType, trxnDate, branchId);

		BIZLOG.method("prcExchangeIncomeToZeroByBranch end <<<<<<<<<<<<<<<<<<<<");
	}

	public static void prcExchangeProfitUp(String orgId, E_REPORTTYPE reportType, String trxnDate) {
		BIZLOG.method("prcExchangeProfitUp begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]reportType[%s] trxnDate[%s] ", orgId, reportType, trxnDate);

		/**
		 * 从最低层逐级向上,查询gla_exchange_gl
		 * 表中余额不为零本年利润，直接更新本机构本年利润为0,上划至其上级机构,采用单点清算,本级机构及上级机构分别补平上划金额
		 */

		// String brchRelationCode =
		// ApSystemParm.getValue(FaConst.KEY_ACCOUNT_RELATION,
		// ApConst.WILDCARD);
		// if (brchRelationCode==null)
		// throw GlError.GL.E0054(); //账务机构关系代码参数未设置
		// BIZLOG.parm("brchRelationCode [%s] ",brchRelationCode);

		String ccyFlag = ApConstants.WILDCARD;
		// app_branch_relation tabRelation =
		// App_branch_relationDao.selectOne_odb1(brchRelationCode, true);
		if (CommUtil.equals(FaTools.getDiffCcyInd(), E_YESORNO.YES.getValue())) {
			// 区分币种时,取本币
			ccyFlag = CommTools.getDefineCurrency();
		}

		String topBranchId = SysUtil.getInstance(IoSrvPbBranch.class).getRootBranch(ccyFlag, PbEnumType.E_BRMPTP.B);
		if (topBranchId == null) {
			throw GlError.GL.E0056(); // 获取财务关系根机构失败
		}
		long brchMaxLevel = SysUtil.getInstance(IoSrvPbBranch.class).getBranchMaxLevel(ccyFlag, PbEnumType.E_BRMPTP.B, topBranchId);
		if (CommUtil.isNull(brchMaxLevel) || brchMaxLevel <= 0) {
			throw GlError.GL.E0057(); // 账务机构关系代码级数读取出错
		}

		for (long i = brchMaxLevel; i > 0; i--) {
			BIZLOG.parm("Level[%s]", i);
			List<PbBranchUpLow> brchList = SysUtil.getInstance(IoSrvPbBranch.class).getBranchListByLevel(ccyFlag, PbEnumType.E_BRMPTP.B, topBranchId, i);
			if (brchList.size() <= 0) {
				throw GlError.GL.E0058(i); // //读取[%s]级机构失败
			}
			for (PbBranchUpLow cplInfo : brchList) {
				BIZLOG.parm("senior branch[%s]junior branch[%s]", cplInfo.getUppebr(), cplInfo.getLowebr());
				if (cplInfo.getUppebr().equals(cplInfo.getLowebr())) {
					// 上下级机构不能相同
					throw GlError.GL.E0059(cplInfo.getUppebr()); //
				}
				prcExchangeProfitUpByBranch(orgId, reportType, trxnDate, ccyFlag, cplInfo.getUppebr(), cplInfo.getLowebr());
			}
		}

		BIZLOG.method("prcExchangeProfitUp end <<<<<<<<<<<<<<<<<<<<");
	}

	private static void prcExchangeProfitUpByBranch(String orgId, E_REPORTTYPE reportType, String trxnDate, String ccyFlag, String seniorBrchId, String juniorBrchId) {
		BIZLOG.method("prcExchangeProfitUpByBranch begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]reportType[%s] trxnDate[%s] ccyFlag[%s] seniorBrchId[%s] juniorBrchId[%s] ", orgId, reportType, trxnDate, ccyFlag, seniorBrchId, juniorBrchId);
		// 本年利润科目参数
		String profitSubject = FaTools.getProfitSubject();
		if (profitSubject == null)
			throw GlError.GL.E0053(); // 本年利润科目参数未设置
		BIZLOG.parm("profitSubject [%s] ", profitSubject);

		List<GlItemGL> lstIncomeData = GlExchangeDao.lstYearEndProfit(orgId, reportType, trxnDate, juniorBrchId, profitSubject, false);

		if (CommUtil.isNotNull(lstIncomeData) && lstIncomeData.size() > 0) {

			for (GlItemGL cplIncomeData : lstIncomeData) {

				boolean insFlag = false;
				E_DEBITCREDIT debitCreditFlag;
				gla_exchange_gl tabGL = SysUtil.getInstance(gla_exchange_gl.class);

				// 根据余额（本期借方余额-本期贷方余额） 进行上划处理
				BigDecimal bigIncomeFlatAmt = cplIncomeData.getCurrent_debit_bal().subtract(cplIncomeData.getCurrent_credit_bal());
				BIZLOG.debug("juniorBrchId [%s] exchange ccy_code [%s] IncomeAmt [%s]", juniorBrchId, cplIncomeData.getCcy_code(), bigIncomeFlatAmt);
				if (CommUtil.compare(bigIncomeFlatAmt, BigDecimal.ZERO) == 0) {
					BIZLOG.method("bigIncomeFlatAmt is 0 continue ");
					continue;
				}

				// 先处理本机构本年利润数据，肯定会有值，直接更新
				tabGL = Gla_exchange_glDao.selectOne_odb1(reportType, trxnDate, juniorBrchId, cplIncomeData.getCcy_code(), cplIncomeData.getGl_code(), true);
				// 如果扎差余额在借方，则补贷方发生，清零
				if (CommUtil.compare(bigIncomeFlatAmt, BigDecimal.ZERO) > 0) {
					BIZLOG.debug("Income flat credit occur");
					debitCreditFlag = E_DEBITCREDIT.CREDIT;
					tabGL.setCurrent_credit_amt(cplIncomeData.getCurrent_credit_amt().add(bigIncomeFlatAmt));
				} else {
					BIZLOG.debug("Income flat debit occur");
					debitCreditFlag = E_DEBITCREDIT.DEBIT;
					tabGL.setCurrent_debit_amt(cplIncomeData.getCurrent_debit_amt().add(bigIncomeFlatAmt.abs()));
				}

				tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
				tabGL.setCurrent_credit_bal(BigDecimal.ZERO);

				Gla_exchange_glDao.updateOne_odb1(tabGL);

				long regNo = GlExchangeDao.selExchangeVochMaxRegNo(orgId, reportType, trxnDate, cplIncomeData.getGl_code(), false);
				// 增加相应分录
				glb_exchange_voch tabVoch = SysUtil.getInstance(glb_exchange_voch.class);
				tabVoch.setReport_type(reportType);
				tabVoch.setTrxn_date(trxnDate);
				tabVoch.setReg_no(regNo);
				tabVoch.setBranch_id(juniorBrchId);
				tabVoch.setExchange_ccy_code(cplIncomeData.getCcy_code());
				tabVoch.setGl_code(cplIncomeData.getGl_code());
				tabVoch.setDebit_credit(debitCreditFlag);
				tabVoch.setAccounting_amt(bigIncomeFlatAmt.abs());
				tabVoch.setOn_bal_sheet_ind(E_YESORNO.YES);
				tabVoch.setRemark("exchange profit up");
				Glb_exchange_vochDao.insert(tabVoch);

				// 处理上级机构本年利润
				fap_accounting_subject tabSubjectInfo = Fap_accounting_subjectDao.selectOne_odb1(profitSubject, true);

				try {
					insFlag = false;
					tabGL = Gla_exchange_glDao.selectOne_odb1(reportType, trxnDate, seniorBrchId, cplIncomeData.getCcy_code(), profitSubject, true);
				} catch (AdpDaoNoDataFoundException e) {
					tabGL.setCorpno(orgId);
					tabGL.setReport_type(reportType);
					tabGL.setTrxn_date(trxnDate);
					tabGL.setBranch_id(seniorBrchId);
					tabGL.setExchange_ccy_code(cplIncomeData.getCcy_code());
					tabGL.setGl_code(profitSubject);

					tabGL.setPrev_debit_bal(BigDecimal.ZERO);
					tabGL.setPrev_credit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_debit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_credit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_credit_bal(BigDecimal.ZERO);

					insFlag = true;
				} catch (Exception e) {
					// 读取xxx失败
					throw GlError.GL.E0082(OdbFactory.getTable(gla_exchange_gl.class).getLongname());
				}

				// 发生额处理： 如果扎差余额在借方，则发生和余额均补在借方
				if (CommUtil.compare(bigIncomeFlatAmt, BigDecimal.ZERO) > 0) {
					BIZLOG.debug("profit up debit occur");
					debitCreditFlag = E_DEBITCREDIT.DEBIT;
					tabGL.setCurrent_debit_amt(tabGL.getCurrent_debit_amt().add(bigIncomeFlatAmt));

				} else {
					BIZLOG.debug("profit up credit occur");
					debitCreditFlag = E_DEBITCREDIT.CREDIT;
					tabGL.setCurrent_credit_amt(tabGL.getCurrent_credit_amt().add(bigIncomeFlatAmt.abs()));
				}

				// 余额计算
				GlSubjectBal cplBal = GlBranch.calSubjectBal(tabSubjectInfo.getBal_prop(), tabGL.getPrev_debit_bal(), tabGL.getPrev_credit_bal(), tabGL.getCurrent_debit_amt(),
						tabGL.getCurrent_credit_amt());

				// 贷方余额处理
				tabGL.setCurrent_debit_bal(cplBal.getCurrent_debit_bal());
				tabGL.setCurrent_credit_bal(cplBal.getCurrent_credit_bal());

				// 更新数据库
				if (insFlag) {
					Gla_exchange_glDao.insert(tabGL);
				} else {
					Gla_exchange_glDao.updateOne_odb1(tabGL);
				}

				// 增加相应分录
				// tabVoch = SysUtil.getInstance(glb_exchange_voch.class);
				regNo++;
				tabVoch.setReport_type(reportType);
				tabVoch.setTrxn_date(trxnDate);
				tabVoch.setReg_no(regNo);
				tabVoch.setBranch_id(seniorBrchId);
				tabVoch.setExchange_ccy_code(cplIncomeData.getCcy_code());
				tabVoch.setGl_code(profitSubject);
				tabVoch.setDebit_credit(debitCreditFlag);
				tabVoch.setAccounting_amt(bigIncomeFlatAmt.abs());
				tabVoch.setOn_bal_sheet_ind(E_YESORNO.YES);
				tabVoch.setRemark("exchange profit up");
				Glb_exchange_vochDao.insert(tabVoch);

				// 单点清算补平
				prcExchangeProfitUpSettle(orgId, reportType, trxnDate, ccyFlag, seniorBrchId, juniorBrchId, debitCreditFlag, cplIncomeData.getCcy_code(), bigIncomeFlatAmt.abs(), regNo);

			}
		}

		// 下级机构平衡检查
		chkExchangeGlBal(orgId, reportType, trxnDate, juniorBrchId);
		// 上级机构平衡检查
		chkExchangeGlBal(orgId, reportType, trxnDate, seniorBrchId);

		BIZLOG.method("prcExchangeProfitUpByBranch end <<<<<<<<<<<<<<<<<<<<");
	}

	private static void prcExchangeProfitUpSettle(String orgId, E_REPORTTYPE reportType, String trxnDate, String ccyFlag, String seniorBrchId, String juniorBrchId, E_DEBITCREDIT debitCreditFlag,
			String ccyCode, BigDecimal settleAmt, long regNo) {
		BIZLOG.method("prcExchangeProfitUpSettle begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]reportType[%s] trxnDate[%s] ccyFlag[%s] seniorBrchId[%s] juniorBrchId[%s] debitCreditFlag[%s] ", orgId, reportType, trxnDate, ccyFlag, seniorBrchId, juniorBrchId,
				debitCreditFlag);

		// 折表利润上划,采用单点清算,补平本机构发生额 传入清算金额必须是正数,传上的记账方向上划记账时上级机构的记账方向
		String settSubjectNo = FaTools.getSettSingleSubject();
		if (settSubjectNo == null) {
			// 单点清算补平科目参数没有设置
			throw GlError.GL.E0112();
		}
		// FaBranchSettlementInfo settlementBranch =
		// FaParmDao.selSettlementByBranch(orgId, ccyFlag, juniorBrchId, false);
		// if (CommUtil.isNull(settlementBranch)) {
		// 获取机构[%s]清算关系失败
		// throw GlError.GL.E0094(juniorBrchId);
		// }

		boolean insFlag = false;

		gla_exchange_gl tabGL = SysUtil.getInstance(gla_exchange_gl.class);

		// // 先清算下机构本年利润数据
		fap_accounting_subject tabSubjectInfo = Fap_accounting_subjectDao.selectOne_odb1(settSubjectNo, true);
		try {
			insFlag = false;
			tabGL = Gla_exchange_glDao.selectOne_odb1(reportType, trxnDate, juniorBrchId, ccyCode, settSubjectNo, true);
		} catch (AdpDaoNoDataFoundException e) {
			tabGL.setCorpno(orgId);
			tabGL.setReport_type(reportType);
			tabGL.setTrxn_date(trxnDate);
			tabGL.setBranch_id(juniorBrchId);
			tabGL.setExchange_ccy_code(ccyCode);
			tabGL.setGl_code(settSubjectNo);

			tabGL.setPrev_debit_bal(BigDecimal.ZERO);
			tabGL.setPrev_credit_bal(BigDecimal.ZERO);
			tabGL.setCurrent_debit_amt(BigDecimal.ZERO);
			tabGL.setCurrent_credit_amt(BigDecimal.ZERO);
			tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
			tabGL.setCurrent_credit_bal(BigDecimal.ZERO);

			insFlag = true;
		} catch (Exception e) {
			// 读取xxx失败
			throw GlError.GL.E0082(OdbFactory.getTable(gla_exchange_gl.class).getLongname());
		}

		// 本级机构清算记账方向与上划记账时上级机构的方向相同
		// 发生额处理： 如果扎差余额在借方，则发生和余额均补在借方
		if (debitCreditFlag == E_DEBITCREDIT.DEBIT) {
			BIZLOG.debug("profit up debit occur");
			tabGL.setCurrent_debit_amt(tabGL.getCurrent_debit_amt().add(settleAmt));

		} else {
			BIZLOG.debug("profit up credit occur");
			tabGL.setCurrent_credit_amt(tabGL.getCurrent_credit_amt().add(settleAmt));
		}

		// 余额计算
		GlSubjectBal cplBal = GlBranch.calSubjectBal(tabSubjectInfo.getBal_prop(), tabGL.getPrev_debit_bal(), tabGL.getPrev_credit_bal(), tabGL.getCurrent_debit_amt(), tabGL.getCurrent_credit_amt());

		// 贷方余额处理
		tabGL.setCurrent_debit_bal(cplBal.getCurrent_debit_bal());
		tabGL.setCurrent_credit_bal(cplBal.getCurrent_credit_bal());

		// 更新数据库
		if (insFlag) {
			Gla_exchange_glDao.insert(tabGL);
		} else {
			Gla_exchange_glDao.updateOne_odb1(tabGL);
		}

		// 增加相应分录
		regNo++;
		glb_exchange_voch tabVoch = SysUtil.getInstance(glb_exchange_voch.class);
		tabVoch.setReport_type(reportType);
		tabVoch.setTrxn_date(trxnDate);
		tabVoch.setReg_no(regNo);
		tabVoch.setBranch_id(juniorBrchId);
		tabVoch.setExchange_ccy_code(ccyCode);
		tabVoch.setGl_code(settSubjectNo);
		tabVoch.setDebit_credit(debitCreditFlag); // 本级机构清算记账方向与上划记账时上级机构的方向相同
		tabVoch.setAccounting_amt(settleAmt);
		tabVoch.setOn_bal_sheet_ind(E_YESORNO.YES);
		tabVoch.setRemark("exchange profit up settle");
		Glb_exchange_vochDao.insert(tabVoch);

		// // 再清算上划至上级机构本年利润数据
		tabSubjectInfo = Fap_accounting_subjectDao.selectOne_odb1(settSubjectNo, true);
		try {
			insFlag = false;
			tabGL = Gla_exchange_glDao.selectOne_odb1(reportType, trxnDate, seniorBrchId, ccyCode, settSubjectNo, true);
		} catch (AdpDaoNoDataFoundException e) {
			tabGL.setCorpno(orgId);
			tabGL.setReport_type(reportType);
			tabGL.setTrxn_date(trxnDate);
			tabGL.setBranch_id(seniorBrchId);
			tabGL.setExchange_ccy_code(ccyCode);
			tabGL.setGl_code(settSubjectNo);

			tabGL.setPrev_debit_bal(BigDecimal.ZERO);
			tabGL.setPrev_credit_bal(BigDecimal.ZERO);
			tabGL.setCurrent_debit_amt(BigDecimal.ZERO);
			tabGL.setCurrent_credit_amt(BigDecimal.ZERO);
			tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
			tabGL.setCurrent_credit_bal(BigDecimal.ZERO);

			insFlag = true;
		} catch (Exception e) {
			// 读取xxx失败
			throw GlError.GL.E0082(OdbFactory.getTable(gla_exchange_gl.class).getLongname());
		}

		// 上级机构清算记账方赂与本级机构清算方向相关
		E_DEBITCREDIT dcFlag = debitCreditFlag;
		// 清算记原分录的反向
		if (debitCreditFlag == E_DEBITCREDIT.DEBIT) {
			dcFlag = E_DEBITCREDIT.CREDIT;
		} else {
			dcFlag = E_DEBITCREDIT.DEBIT;
		}
		// 发生额处理： 如果扎差余额在借方，则发生和余额均补在借方
		if (dcFlag == E_DEBITCREDIT.DEBIT) {
			BIZLOG.debug("profit up debit occur");
			tabGL.setCurrent_debit_amt(tabGL.getCurrent_debit_amt().add(settleAmt));
		} else {
			BIZLOG.debug("profit up credit occur");
			tabGL.setCurrent_credit_amt(tabGL.getCurrent_credit_amt().add(settleAmt));
		}

		// 余额计算
		cplBal = GlBranch.calSubjectBal(tabSubjectInfo.getBal_prop(), tabGL.getPrev_debit_bal(), tabGL.getPrev_credit_bal(), tabGL.getCurrent_debit_amt(), tabGL.getCurrent_credit_amt());

		// 贷方余额处理
		tabGL.setCurrent_debit_bal(cplBal.getCurrent_debit_bal());
		tabGL.setCurrent_credit_bal(cplBal.getCurrent_credit_bal());

		// 更新数据库
		if (insFlag) {
			Gla_exchange_glDao.insert(tabGL);
		} else {
			Gla_exchange_glDao.updateOne_odb1(tabGL);
		}

		// 增加相应分录
		regNo++;
		tabVoch = SysUtil.getInstance(glb_exchange_voch.class);
		tabVoch.setReport_type(reportType);
		tabVoch.setTrxn_date(trxnDate);
		tabVoch.setReg_no(regNo);
		tabVoch.setBranch_id(seniorBrchId);
		tabVoch.setExchange_ccy_code(ccyCode);
		tabVoch.setGl_code(settSubjectNo);
		tabVoch.setDebit_credit(dcFlag);
		tabVoch.setAccounting_amt(settleAmt);
		tabVoch.setOn_bal_sheet_ind(E_YESORNO.YES);
		tabVoch.setRemark("exchange profit up settle");
		Glb_exchange_vochDao.insert(tabVoch);

		BIZLOG.method("prcExchangeProfitUpSettle end <<<<<<<<<<<<<<<<<<<<");
	}
}
