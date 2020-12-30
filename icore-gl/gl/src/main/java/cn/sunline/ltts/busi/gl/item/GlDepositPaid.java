package cn.sunline.ltts.busi.gl.item;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApCurrency;
import cn.sunline.ltts.busi.fa.account.FaOpenAccount;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_deposit_paidDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_deposit_paid;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.gl.namedsql.GlDepositPaidDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.Gla_branch_glDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.gla_branch_gl;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_appointDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_cycleDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_level_defineDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_percentDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_specialDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_appoint;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_cycle;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_index;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_level_define;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_percent;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_special;
import cn.sunline.ltts.busi.gl.type.GlDepositPaid.GlDepositPaidAmt;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoCompPbBranch.PbBranchUpLow;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_CALCFACTOR;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_DEPOSITPAIDBUSIPROP;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_DEPOSITPAIDCYCLE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_DEPOSITPAIDDEALTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_DEPOSITPAIDSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_GLDATEINTERVAL;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.busi.sys.type.PbEnumType;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;

/**
 * <p>
 * 文件功能说明：
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
public class GlDepositPaid {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlDepositPaid.class);

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
	public static void prcMain(String orgId, String trxnDate) {

		BIZLOG.method("prcMain begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId=[%s]  trxnDate=[%s]", orgId, trxnDate);

		String diffCcyInd = FaTools.getDiffCcyInd();
		String ccyFlag = ApConstants.WILDCARD;
		if (CommUtil.equals(diffCcyInd, E_YESORNO.YES.getValue())) {
			// 区分币种时,取本币
			ccyFlag = CommTools.getDefineCurrency();
		}
		BIZLOG.parm("crcycd=[%s]",ccyFlag);
		// 财务关系代码参数
		// String reportRelation =
		// ApSystemParm.getValue(FaConst.KEY_ACCOUNT_RELATION,
		// ApConst.WILDCARD);
		// if (reportRelation == null)
		// throw GlError.GL.E0055(); // //财务关系代码参数未设置
		// BIZLOG.parm("reportRelation [%s] ", reportRelation);

		String topBranchId = SysUtil.getInstance(IoSrvPbBranch.class).getRootBranch(ccyFlag, PbEnumType.E_BRMPTP.B);
		if (topBranchId == null) {
			throw GlError.GL.E0056(); // //获取财务关系根机构失败
		}

		Long brchMaxLevel = SysUtil.getInstance(IoSrvPbBranch.class).getBranchMaxLevelByCorpno(ccyFlag, PbEnumType.E_BRMPTP.B, orgId);
		if (CommUtil.isNull(brchMaxLevel) || brchMaxLevel <= 0) {
			throw GlError.GL.E0057(); // //账务机构关系代码级数读取出错
		}

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String lastdate = runEnvs.getLstrdt();

		for (long i = brchMaxLevel; i > 0; i--) {
			BIZLOG.parm("Level[%s]", i);
			
			/**
			 * mdy by zhanga
			 * 修改为按法人来获取机构级别，级别从小到大（数字从大到小），原sql执行时，总行清算中心也能获取到，与下方的顶级处理逻辑重复
			 */
			//List<PbBranchUpLow> brchList = SysUtil.getInstance(IoSrvPbBranch.class).getBranchListByLevel(ccyFlag, PbEnumType.E_BRMPTP.B, topBranchId, i);
			List<PbBranchUpLow> brchList = SysUtil.getInstance(IoSrvPbBranch.class).getBranchListByLevelAndCorpno(ccyFlag, PbEnumType.E_BRMPTP.B, orgId, i);
			if (CommUtil.isNotNull(brchList) && brchList.size() <= 0) {
				throw GlError.GL.E0058(i); // 读取[%s]级机构失败
			}
			for (PbBranchUpLow cplInfo : brchList) {
				BIZLOG.parm("senior branch[%s]junior branch[%s]", cplInfo.getUppebr(), cplInfo.getLowebr());
				if (cplInfo.getUppebr().equals(cplInfo.getLowebr())) {
					// 上下级机构不能相同
					throw GlError.GL.E0059(cplInfo.getUppebr()); //
				}
				prcByBranch(orgId, lastdate, ccyFlag, cplInfo.getUppebr(), cplInfo.getLowebr(), i);
			}
		}
		if (brchMaxLevel > 0) {
			// 求顶级
			prcByBranch(orgId, lastdate, ccyFlag, null, topBranchId, 0L);
		}

		BIZLOG.method("prcMain end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午10:26:54</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param lastdate
	 * @param ccyFlag
	 * @param seniorBrchId
	 * @param juniorBrchId
	 * @param brchLevel
	 */
	private static void prcByBranch(String orgId, String lastdate, String ccyFlag, String seniorBrchId, String juniorBrchId, long brchLevel) {
		BIZLOG.method("prcByBranch begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s] trxnDate[%s] ccyFlag[%s] seniorBrchId[%s] juniorBrchId[%s] ", orgId, lastdate, ccyFlag, seniorBrchId, juniorBrchId);

		// 获取所有的币种
		List<ApCurrency> ccyDroplist = CommTools.listApCurrency();

		for (ApCurrency listInfo : ccyDroplist) {
			// 币种和日期满足缴存周期币种定义
			 if (!chkCcyCle(listInfo.getCrcycd(), lastdate)) {//TODO 测试用
				 continue;
			 }

			prcByBranchAndCcyCode(orgId, lastdate, ccyFlag, seniorBrchId, juniorBrchId, brchLevel, listInfo.getCrcycd());
		}
		BIZLOG.method("prcByBranch end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午10:26:59</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param lastdate
	 * @param ccyFlag
	 * @param seniorBrchId
	 * @param juniorBrchId
	 * @param brchLevel
	 * @param ccyCode
	 */
	private static void prcByBranchAndCcyCode(String orgId, String lastdate, String ccyFlag, String seniorBrchId, String juniorBrchId, long brchLevel, String ccyCode) {
		BIZLOG.method("prcByBranchAndCcyCode begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s] lastdate[%s] ccyFlag[%s] seniorBrchId[%s] juniorBrchId[%s] ccyCode[%s]", orgId, lastdate, ccyFlag, seniorBrchId, juniorBrchId, ccyCode);

		E_RESERVEAMTTYPE[] listReserveType = E_RESERVEAMTTYPE.values();
		long brchLevelTemp = brchLevel;

		/* 遍历循环上缴准备金类别枚举 */
		for (int i = 0; i < listReserveType.length; i++) {

			E_DEPOSITPAIDBUSIPROP depositPaidBusiProp = getDepositPaidLevel(juniorBrchId, ccyCode, listReserveType[i], brchLevel);

			if (depositPaidBusiProp == E_DEPOSITPAIDBUSIPROP.BRANCH_DEPOSIT)
				brchLevelTemp = 0L;// 分行对外缴存重置为0
			// 获取缴存层级信息
			glp_reserve_level_define tabLevelDefine = Glp_reserve_level_defineDao.selectOne_odb1(brchLevelTemp, listReserveType[i], depositPaidBusiProp, false);
			/* selectOne_odb1(eJcunchji, eZhbjzlei, false); */

			if (CommUtil.isNull(tabLevelDefine)) {
				BIZLOG.info("deposit paid parm is not set : level[%s]type[%s]prop[%s] NO SET EXIT", brchLevelTemp, listReserveType[i], depositPaidBusiProp);
				// throw GlError.GL.E0089(brchLevelTemp,
				// listReserveType[i].getLongName()); // 缴存层级参数未设置:
				// 缴存层级[%s]准备金种类[%s]
				continue;
			}

			// 不缴存
			if (tabLevelDefine.getDeposit_processing_category() == E_DEPOSITPAIDDEALTYPE.NOEN) {
				BIZLOG.info("not deposit paid : level[%s]type[%s]prop[%s]", brchLevelTemp, listReserveType[i], depositPaidBusiProp);
				continue;
			}

			prcByBranchAndCcyCodeAndReserveType(orgId, lastdate, ccyFlag, tabLevelDefine, seniorBrchId, juniorBrchId, brchLevelTemp, ccyCode, listReserveType[i]);

		}

		BIZLOG.method("prcByBranchAndCcyCode end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午10:27:02</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param lastdate
	 * @param ccyFlag
	 * @param tabLevelDefine
	 * @param seniorBrchId
	 * @param juniorBrchId
	 * @param brchLevel
	 * @param ccyCode
	 * @param reserveType
	 */
	private static void prcByBranchAndCcyCodeAndReserveType(String orgId, String lastdate, String ccyFlag, glp_reserve_level_define tabLevelDefine, String seniorBrchId, String juniorBrchId,
			long brchLevel, String ccyCode, E_RESERVEAMTTYPE reserveType) {
		BIZLOG.method("prcByBranchAndCcyCodeAndReserveType begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s] trxnDate[%s] ccyFlag[%s] juniorBrchId[%s] ccyCode[%s] reserveType[%s]", orgId, lastdate, ccyFlag, juniorBrchId, ccyCode, reserveType);

		BigDecimal paidAmt = BigDecimal.ZERO; // 已缴金额
		BigDecimal currPaidAmt = BigDecimal.ZERO; // 本次缴存金额

		// 计算当前已缴存金额
		gla_branch_gl tblGL = Gla_branch_glDao.selectOne_odb1(E_REPORTTYPE.BUSINETT_TYPE, lastdate, juniorBrchId, ccyCode, tabLevelDefine.getAccounting_gl_code(), false);
		if (CommUtil.isNotNull(tblGL)) {
			paidAmt = tblGL.getCurrent_debit_bal().subtract(tblGL.getCurrent_credit_bal());
		}

		// 计算应缴金额
		GlDepositPaidAmt cplAmt = calDepositPaidAmt(orgId, lastdate, juniorBrchId, reserveType, ccyCode, tabLevelDefine);

		// 计算本次缴存金额
		currPaidAmt = cplAmt.getDeposit_paid_amt_sum().subtract(paidAmt);
		BIZLOG.debug("currPaidAmt[%s]=应缴金额[%s]-机构已缴存金额[%s]", String.valueOf(currPaidAmt), String.valueOf(cplAmt.getDeposit_paid_amt_sum()), String.valueOf(paidAmt));
		// 应记缴存款余额、本次缴存金额都为零，则不做任何处理
		if (CommUtil.equals(cplAmt.getDeposit_payable_bal(), BigDecimal.ZERO) && CommUtil.equals(currPaidAmt, BigDecimal.ZERO)) {
			BIZLOG.method("prcByBranchAndCcyCodeAndReserveType <<<<<<<<<<<<End<<<<<<<<<<<<");
			return;
		}

		fab_deposit_paid tblPaid = SysUtil.getInstance(fab_deposit_paid.class);

		/* 登记登记薄 */
		// tblJckuan.setDengjhao(genDengjhao(sJiaoyirq));
		if (tabLevelDefine.getDeposit_busi_type() == E_DEPOSITPAIDBUSIPROP.LEGAL_DEPOSIT || tabLevelDefine.getDeposit_busi_type() == E_DEPOSITPAIDBUSIPROP.BRANCH_DEPOSIT) {
			if (CommUtil.isNull(seniorBrchId)) {
				seniorBrchId = "#####"; // 对外缴存为空时写死
			}
		}
		tblPaid.setDeposit_status(E_DEPOSITPAIDSTATE.REGISTER);
		tblPaid.setRegister_date(lastdate);
		tblPaid.setDeposit_busi_type(tabLevelDefine.getDeposit_busi_type());
		tblPaid.setDeposit_level(brchLevel);
		tblPaid.setReserve_type(reserveType);
		tblPaid.setCcy_code(ccyCode);
		tblPaid.setDeposit_brch(juniorBrchId);
		tblPaid.setDeposit_parent_brch(seniorBrchId);
		tblPaid.setDeposit_payable_bal(cplAmt.getDeposit_paid_bal_sum());
		tblPaid.setDeposit_percent(cplAmt.getDeposit_percent());
		tblPaid.setDeposit_payable_amt(cplAmt.getDeposit_paid_amt_sum());
		tblPaid.setDeposit_paid_amt(paidAmt);
		tblPaid.setCurrent_deposit_amt(currPaidAmt);

		// 需要自动入账
		if (tabLevelDefine.getDeposit_processing_category() == E_DEPOSITPAIDDEALTYPE.DEAL_WITHOUT_RECORDING && !CommUtil.equals(currPaidAmt, BigDecimal.ZERO)) {
			// 记账
			prcKeep(tblPaid, tabLevelDefine.getAccounting_gl_code(), tabLevelDefine.getParent_accounting_gl_code());
		}
		BIZLOG.debug("tblPaid.........[%s]", tblPaid);

		Fab_deposit_paidDao.insert(tblPaid);

		BIZLOG.method("prcByBranchAndCcyCodeAndReserveType end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午10:27:06</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param ccyCode
	 * @param lastdate
	 * @return
	 */
	
	  private static boolean chkCcyCle(String ccyCode, String lastdate) {
	  
		  BIZLOG.method("chkCcyCle >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		  BIZLOG.parm("ccyCode [%s],trxnDate [%s]", ccyCode, lastdate);
		  
		  String calDate = "";// 计算缴存日期值
		  
		  glp_reserve_cycle tblCycle = Glp_reserve_cycleDao.selectOne_odb1(ccyCode,false);
		  if (CommUtil.isNull(tblCycle)) { // 货币[%s]的准备金缴存周期没有设置
			  BIZLOG.info("ccycode[%s] Paid no set", ccyCode);
			  BIZLOG.method("chkCcyCle <<<<<<<<<<<<End<<<<<<<<<<<<"); 
			  return false;
		  //throw GlError.GL.E0090(ccyCode); 
		  }
		  
		  E_DEPOSITPAIDCYCLE depositPaidCycle = tblCycle.getDeposit_cycle();
		  
		  if (depositPaidCycle == E_DEPOSITPAIDCYCLE.HALFMONTH) {// 询 
			  calDate = DateTimeUtil.lastDay(lastdate, "T"); 
		  } else if (depositPaidCycle == E_DEPOSITPAIDCYCLE.MONTH) {// 月 
			  calDate = DateTimeUtil.lastDay(lastdate,"M"); 
		  } else if (depositPaidCycle == E_DEPOSITPAIDCYCLE.THREE_MONTH) {// 季度 
			  calDate = DateTimeUtil.lastDay(lastdate, "Q"); 
		  } else {
			  return false;
		  }
		  
		  if (CommUtil.compare(lastdate, calDate) == 0) {
			  BIZLOG.info("ccycode[%s]date[%s] Satisfy condition exit true", ccyCode, lastdate); 
			  BIZLOG.method("chkCcyCle <<<<<<<<<<<<End<<<<<<<<<<<<"); 
			  return true; 
		  }
		  
		  // 指定日期缴存 
		  glp_reserve_appoint tabAppoint = Glp_reserve_appointDao.selectOne_odb1(lastdate, ccyCode, false); 
		  if (CommUtil.isNotNull(tabAppoint)) {
			  BIZLOG.info("ccycode[%s]date[%s] Satisfy appoint condition exit true", ccyCode, lastdate);
			  BIZLOG.method("chkCcyCle <<<<<<<<<<<<End<<<<<<<<<<<<"); 
			  return true; 
		  }
		  BIZLOG.info("ccycode[%s]date[%s] not Satisfy appoint condition exit true",ccyCode, lastdate);
		  BIZLOG.method("chkCcyCle <<<<<<<<<<<<End<<<<<<<<<<<<"); 
		  return false; 
	  }
	 
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午10:27:10</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param branchId
	 * @param ccyCode
	 * @param reserveType
	 * @param brchLevel
	 * @return
	 */
	private static E_DEPOSITPAIDBUSIPROP getDepositPaidLevel(String branchId, String ccyCode, E_RESERVEAMTTYPE reserveType, long brchLevel) {
		BIZLOG.method("getDepositPaidLevel >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("branchId [%s],ccyCode [%s],reserveType [%s],brchLevel [%s]", branchId, ccyCode, reserveType, brchLevel);

		E_DEPOSITPAIDBUSIPROP depositPaidBusiProp;

		if (brchLevel == 0) {
			depositPaidBusiProp = E_DEPOSITPAIDBUSIPROP.LEGAL_DEPOSIT;
		} else {
			/* 判断机构是否在特殊分行缴存记录中 */
			glp_reserve_special tabSP = Glp_reserve_specialDao.selectOne_odb1(branchId, ccyCode, reserveType, false);

			/* 设置缴存层级 */
			if (CommUtil.isNotNull(tabSP)) {
				depositPaidBusiProp = E_DEPOSITPAIDBUSIPROP.BRANCH_DEPOSIT; // 分行对外缴存
			} else {
				depositPaidBusiProp = E_DEPOSITPAIDBUSIPROP.INTERNAL_DEPOSIT;
			}
		}

		BIZLOG.parm("depositPaidBusiProp [%s]", depositPaidBusiProp);
		BIZLOG.method("getDepositPaidLevel <<<<<<<<<<<<End<<<<<<<<<<<<");
		return depositPaidBusiProp;
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午10:27:14</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param lastdate
	 * @param branchId
	 * @param reserveType
	 * @param ccyCode
	 * @param tabLevelDefine
	 * @return
	 */
	private static GlDepositPaidAmt calDepositPaidAmt(String orgId, String lastdate, String branchId, E_RESERVEAMTTYPE reserveType, String ccyCode, glp_reserve_level_define tabLevelDefine) {
		BIZLOG.method("calDepositPaidAmt >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s],lastdate [%s],branchId [%s],reserveType [%s],ccyCode [%s]", orgId, lastdate, branchId, reserveType, ccyCode);
		BigDecimal depositPaidAmtSum = BigDecimal.ZERO;
		BigDecimal depositPaidBalSum = BigDecimal.ZERO;
		BigDecimal depositPaidAmtMust = BigDecimal.ZERO;
		BigDecimal reservePercent = BigDecimal.ZERO;
		GlDepositPaidAmt cplAmt = SysUtil.getInstance(GlDepositPaidAmt.class);

		// 统计非配减项科目余额
		BigDecimal depositPaidBal = GlDepositPaidDao.selDepositPaidAmt(orgId, lastdate, reserveType, ccyCode, branchId, true);
		BIZLOG.parm("orgId [%s],lastdate [%s],ccyCode [%s],branchId [%s],branchId [%s] depositPaidBal[%s]", orgId, lastdate, ccyCode, branchId, branchId, depositPaidBal);

		/* 统计配减科目余额 */
		List<glp_reserve_index> lstTab = GlDepositPaidDao.lstReserveIndexDynamic(orgId, reserveType, E_CALCFACTOR.MATCH_SUBTRACT, ccyCode, false);
		if (CommUtil.isNotNull(lstTab) && lstTab.size() > 0) {
			for (glp_reserve_index tabReservIndex : lstTab) {
				if (CommUtil.compare(tabReservIndex.getReserve_percent(), BigDecimal.ZERO) == 0) {// 比率为0不计算
					BIZLOG.parm("ccy_code[%s],subject_no[%s] reserve_type[%s] Reserve_percent[%s] continue", tabReservIndex.getCcy_code(), tabReservIndex.getGl_code(),
							tabReservIndex.getReserve_type(), tabReservIndex.getReserve_percent().toString());
					continue;
				}
				// 获取配减项主科目余额
				BigDecimal tempBal = GlDepositPaidDao.selGlbGlReportDynamic(orgId, lastdate, tabReservIndex.getCcy_code(), E_GLDATEINTERVAL.DAILY, E_REPORTTYPE.BUSINETT_TYPE, branchId,
						tabReservIndex.getGl_code(), false);
				BIZLOG.parm("ccy_code[%s],subject_no[%s] reserve_type[%s] Reserve_percent[%s] main subject bal[%s]", tabReservIndex.getCcy_code(), tabReservIndex.getGl_code(),
						tabReservIndex.getReserve_type(), tabReservIndex.getReserve_percent(), tempBal.toString());
				// 获取配对科目余额
				String[] lstSubjectNo = tabReservIndex.getPaired_gl_code().split(",");
				if (CommUtil.isNotNull(lstSubjectNo)) {
					for (String subjectNo : lstSubjectNo) {
						BigDecimal tempBal2 = GlDepositPaidDao.selGlbGlReportDynamic(orgId, lastdate, tabReservIndex.getCcy_code(), E_GLDATEINTERVAL.DAILY, E_REPORTTYPE.BUSINETT_TYPE, branchId,
								subjectNo, false);
						tempBal = tempBal.subtract(tempBal2);
						BIZLOG.debug("subjectno[%s]bal[%s]", subjectNo, tempBal2.toString());
					}
				}
				BIZLOG.debug("Final result[%s]加配减项前应缴存科目余额:基数[%s]", tempBal.toString(), depositPaidBal.toString());

				if (CommUtil.compare(tempBal, BigDecimal.ZERO) > 0) {// 每个配减项，只有计算结果大于0才统计
					depositPaidBal = depositPaidBal.add(tempBal.multiply(tabReservIndex.getReserve_percent()).divide(BigDecimal.TEN).divide(BigDecimal.TEN));
				}
				BIZLOG.debug("final base bal【%s】", depositPaidBal);
			}
		}

		// 按机构取缴存比例相关
		glp_reserve_percent tblPercent = getRateByBranchAndCcy(branchId, ccyCode, reserveType);
		/* 本次缴存比率 */
		reservePercent = tblPercent.getReserve_percent();

		if (tabLevelDefine.getDeposit_busi_type() == E_DEPOSITPAIDBUSIPROP.INTERNAL_DEPOSIT && CommUtil.isNotNull(tblPercent.getBank_intn_dep_add_percent())) {
			reservePercent = reservePercent.add(tblPercent.getBank_intn_dep_add_percent()); // 行内加上“行内缴存追加比率”
		}
		BIZLOG.debug("reservePercent......[%s]", reservePercent.toString());

		// 比率计算时，还需要除以100
		depositPaidAmtMust = depositPaidBal.multiply(reservePercent).divide(BigDecimal.TEN).divide(BigDecimal.TEN);
		depositPaidAmtMust = CommUtil.round(depositPaidAmtMust, 2);

		depositPaidBalSum = depositPaidBal;
		depositPaidAmtSum = depositPaidAmtMust;

		String diffCcyInd = FaTools.getDiffCcyInd();
		String ccyFlag = ApConstants.WILDCARD;
		if (CommUtil.equals(diffCcyInd, E_YESORNO.YES.getValue())) {
			// 区分币种时,取本币
			ccyFlag = CommTools.getDefineCurrency();
		}

		BIZLOG.parm("depositPaidBal [%s],depositPaidAmtMust [%s],bigYjjckyueSum[%s],bigYjjcjineSum[%s]", depositPaidBal, depositPaidAmtMust, depositPaidBalSum, depositPaidAmtSum);

		cplAmt.setDeposit_payable_bal(depositPaidBal);
		cplAmt.setDeposit_payable_amt(depositPaidAmtMust);
		cplAmt.setDeposit_percent(reservePercent);
		cplAmt.setDeposit_paid_bal_sum(depositPaidBalSum);
		cplAmt.setDeposit_paid_amt_sum(depositPaidAmtSum);
		BIZLOG.debug("cplAmt.........[%s]", cplAmt);
		BIZLOG.method("calDepositPaidAmt <<<<<<<<<<<<End<<<<<<<<<<<<");
		return cplAmt;
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午10:27:21</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param branchId
	 * @param ccyCode
	 * @param reserveType
	 * @return
	 */
	public static glp_reserve_percent getRateByBranchAndCcy(String branchId, String ccyCode, E_RESERVEAMTTYPE reserveType) {
		BIZLOG.method("getRateByBranchAndCcy >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("branchId [%s] ccyCode[%s] reserveType[%s]", branchId, ccyCode, reserveType);
		// 首先拿机构币种取一遍数据，若无数据，则用DEFAULT再查一遍，若无记录则报错
		glp_reserve_percent tblPercent = Glp_reserve_percentDao.selectOne_odb1(branchId, ccyCode, reserveType, false);

		if (CommUtil.isNull(tblPercent)) {
			tblPercent = Glp_reserve_percentDao.selectOne_odb1("*", ccyCode, reserveType, false);

			if (CommUtil.isNull(tblPercent)) {
				// 准备金种类对应参数未配置
				throw GlError.GL.E0091(); //

			} else {
				// 找到默认记录，返回缴存比例
				BIZLOG.method("getRateByBranchAndCcy <<<<<<<<<<<<End<<<<<<<<<<<<");
				return tblPercent;
			}
		} else {
			// 按机构找到记录，返回缴存比例
			BIZLOG.method("getRateByBranchAndCcy <<<<<<<<<<<<End<<<<<<<<<<<<");
			return tblPercent;
		}

	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午10:27:29</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param tblPaid
	 * @param subjectNo
	 * @param seniorSubjectNo
	 */
	private static void prcKeep(fab_deposit_paid tblPaid, String subjectNo, String seniorSubjectNo) {

		BIZLOG.method("prcKeep >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("tblPaid [%s],subjectNo [%s],seniorSubjectNo [%s]", tblPaid, subjectNo, seniorSubjectNo);

		List<FaSingleAccountingCheckIn> accountingDoInList = new ArrayList<FaSingleAccountingCheckIn>();
		FaAcctInfo acctInfo;

		// 取出账号信息
		acctInfo = FaOpenAccount.getAcctBySubject(FaConst.GL_SYSTEM, tblPaid.getCcy_code(), tblPaid.getDeposit_brch(), subjectNo);
		BIZLOG.debug("acctInfo[%s]", acctInfo);
		String debitAcctNo = acctInfo.getAcct_no(); // 借方账号
		String creditAcctNo;
		// 借方账号记账复合类型 对内缴存， 本级
		//FaAccounting.getAccountingCheckByAcct(accountingDoInList, debitAcctNo, E_DEBITCREDIT.DEBIT, tblPaid.getDeposit_paid_amt(), "", "depositPaid"); 
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, debitAcctNo, E_DEBITCREDIT.DEBIT, tblPaid.getCurrent_deposit_amt(), "", "depositPaid"); // TODO
		 																																				// 摘要码没写

		// 准备金科目，目前调减时以红字调整
		if (tblPaid.getDeposit_busi_type() == E_DEPOSITPAIDBUSIPROP.BRANCH_DEPOSIT || tblPaid.getDeposit_busi_type() == E_DEPOSITPAIDBUSIPROP.LEGAL_DEPOSIT) {
			// 对外缴存， 是对人行，只能自己记自己的科目

			// 备付金科目
			acctInfo = FaOpenAccount.getAcctBySubject(FaConst.GL_SYSTEM, tblPaid.getCcy_code(), tblPaid.getDeposit_brch(), seniorSubjectNo);
			BIZLOG.debug("acctInfo[%s]", acctInfo);
			creditAcctNo = acctInfo.getAcct_no(); // 贷方账号
		} else {
			// 取出上级账户信息
			acctInfo = FaOpenAccount.getAcctBySubject(FaConst.GL_SYSTEM, tblPaid.getCcy_code(), tblPaid.getDeposit_parent_brch(), seniorSubjectNo);
			BIZLOG.debug("acctInfo[%s]", acctInfo);
			creditAcctNo = acctInfo.getAcct_no(); // 贷方账号
		}
		// 贷方账户记账复合类型
		//FaAccounting.getAccountingCheckByAcct(accountingDoInList, creditAcctNo, E_DEBITCREDIT.CREDIT, tblPaid.getDeposit_paid_amt(), "", "depositPaid"); // TODO
		FaAccounting.getAccountingCheckByAcct(accountingDoInList, creditAcctNo, E_DEBITCREDIT.CREDIT, tblPaid.getCurrent_deposit_amt(), "", "depositPaid"); // TODO
		 																																				// 摘要码没写

		// 登记柜员流水
		String tellerSeq = FaAccounting.getTellerSeq();
		FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
		regTellerSeq.setSys_no(FaConst.GL_SYSTEM); // 系统编号
		regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
		regTellerSeq.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
		regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
		regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
		regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水

		FaAccounting.regTellerSeq(regTellerSeq);

		FaAccounting.bookMultiAccounting(accountingDoInList, tellerSeq, 2L, FaConst.GL_SYSTEM, "", true);

		tblPaid.setAccounting_date(CommToolsAplt.prcRunEnvs().getTrandt());
		tblPaid.setAccounting_seq(tellerSeq);
		tblPaid.setDeposit_status(E_DEPOSITPAIDSTATE.RECORDING);
		BIZLOG.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
	}

}
