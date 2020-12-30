package cn.sunline.ltts.busi.gl.item;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.core.exception.AdpDaoNoDataFoundException;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.gl.namedsql.GlGeneralLedgerDao;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlItemGL;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlSubjectBal;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlTranData;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoCompPbBranch.PbBranchUpLow;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_GLDATEINTERVAL;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.busi.sys.type.PbEnumType;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.Glb_gl_reportDao;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glb_gl_report;

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
public class GlGeneralLedger {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlGeneralLedger.class);

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月7日-下午4:36:31</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param reportType
	 */
	public static void prcBusiGlBefore(String orgId, String trxnDate, E_REPORTTYPE reportType) {

		BIZLOG.method("prcBefore begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId=[%s] trxnDate=[%s] reportType=[%s]", orgId, trxnDate, reportType);

		// 先删除（支持重复处理）
		GlGeneralLedgerDao.delReportGl(orgId, reportType, trxnDate);

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
	 * @param reportType
	 * @param branchId
	 */
	public static void genBusiGL(String orgId, E_REPORTTYPE reportType, String trxnDate, String branchId) {
		BIZLOG.method("genGL >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]trxnDate[%s] reportType[%s] branchId[%s] ", orgId, trxnDate, reportType, branchId);

		// 生成总账日报数据
		genReport(orgId, trxnDate, branchId, E_GLDATEINTERVAL.DAILY, reportType);

		// 生成特殊日期总账
		genInterval(orgId, trxnDate, branchId, reportType);

		BIZLOG.method("genGL <<<<<<<<<<<<End<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月11日-上午10:22:46</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param dateInterval
	 * @param reportType
	 */
	public static void genReport(String orgId, String trxnDate, String branchId, E_GLDATEINTERVAL dateInterval, E_REPORTTYPE reportType) {

		BIZLOG.method("genReport begin >>>>>>>>>>>>>>>>>>>>");

		BIZLOG.parm("orgId [%s],trxnDate [%s],branchId [%s],dateInterval [%s],reportType [%s]", orgId, trxnDate, branchId, dateInterval, reportType);

		// 将上日总账初始化到总账表
		loadBuisGlInit(orgId, trxnDate, branchId, dateInterval, reportType);

		// 发生数据
		List<GlTranData> lstTD = getTranData(orgId, trxnDate, branchId, dateInterval, reportType);

		BIZLOG.debug("lstTD----record count------->[%s] ", lstTD.size());
		if (CommUtil.isNotNull(lstTD) && lstTD.size() > 0) {

			int insCount = 0;
			int updCount = 0;

			// 将发生数据更新到总账
			for (GlTranData cplTran : lstTD) {

				boolean bIns = false; // 插入标记
				fap_accounting_subject tabSubjectInfo = Fap_accounting_subjectDao.selectOne_odb1(cplTran.getGl_code(), true);
				glb_gl_report tabGL = SysUtil.getInstance(glb_gl_report.class);

				// 取总账记录,取到做更新，未取到做插入
				try {
					bIns = false;
					tabGL = Glb_gl_reportDao.selectOne_odb1(reportType, trxnDate, branchId, dateInterval, cplTran.getCcy_code(), cplTran.getGl_code(), true);
				} catch (AdpDaoNoDataFoundException e) {

					tabGL.setCorpno(orgId);
					tabGL.setTrxn_date(trxnDate);
					tabGL.setBranch_id(branchId);
					tabGL.setReport_type(reportType);
					tabGL.setCcy_code(cplTran.getCcy_code());
					tabGL.setGl_code(cplTran.getGl_code());
					tabGL.setOn_bal_sheet_ind(tabSubjectInfo.getOn_bal_sheet_ind());
					tabGL.setGl_code_level(tabSubjectInfo.getGl_code_level());
					tabGL.setGl_code_desc(tabSubjectInfo.getGl_code_desc());
					tabGL.setEnd_gl_code_ind(tabSubjectInfo.getEnd_gl_code_ind());
					tabGL.setSimple_list_display_ind(tabSubjectInfo.getSimple_list_display_ind());
					tabGL.setGl_date_interval(dateInterval);
					tabGL.setPrev_debit_bal(BigDecimal.ZERO);
					tabGL.setPrev_credit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_debit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_credit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_credit_bal(BigDecimal.ZERO);

					bIns = true;
				} catch (Exception e) {
					// 读取xxx失败
					throw GlError.GL.E0082(OdbFactory.getTable(glb_gl_report.class).getLongname());
				}

				// 发生额处理
				tabGL.setCurrent_debit_amt(tabGL.getCurrent_debit_amt().add(cplTran.getDebit_amt()));
				tabGL.setCurrent_credit_amt(tabGL.getCurrent_credit_amt().add(cplTran.getCredit_amt()));

				// 余额计算
				GlSubjectBal cplBal = GlBranch.calSubjectBal(tabSubjectInfo.getBal_prop(), tabGL.getPrev_debit_bal(), tabGL.getPrev_credit_bal(), tabGL.getCurrent_debit_amt(),
						tabGL.getCurrent_credit_amt());

				// 贷方余额处理
				tabGL.setCurrent_debit_bal(cplBal.getCurrent_debit_bal());
				tabGL.setCurrent_credit_bal(cplBal.getCurrent_credit_bal());
				
				tabGL.setRecdver(1L);  // 版本号

				// 最终数据
				BIZLOG.debug("tabGL=====>[%s]", tabGL);

				/* 更新数据库 */
				if (bIns) {
					Glb_gl_reportDao.insert(tabGL);
					insCount++;
				} else {
					Glb_gl_reportDao.updateOne_odb1(tabGL);
					updCount++;
				}
			}

			BIZLOG.debug("generate report GL: date%s branch%s Occur count%s add count%s, update count%s条", trxnDate, branchId, lstTD.size(), insCount, updCount);
		}

		chkBusiGlBal(orgId, trxnDate, branchId, dateInterval, reportType);

		// 生成汇总数据
		prcSummary(orgId, trxnDate, branchId, reportType, dateInterval);

		BIZLOG.method("genReport end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月11日-上午10:23:04</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param dateInterval
	 * @param reportType
	 */
	private static void loadBuisGlInit(String orgId, String trxnDate, String branchId, E_GLDATEINTERVAL dateInterval, E_REPORTTYPE reportType) {

		BIZLOG.method("lodInit begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s],trxnDate [%s],branchId [%s],dateInterval [%s],reportType [%s]", orgId, trxnDate, branchId, dateInterval, reportType);

		String begindate = "";
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		switch (dateInterval) {
		case DAILY:
			begindate = runEnvs.getLstrdt();
			break;
		case MONTHLY:
			begindate = DateTools2.firstDay("M", trxnDate);
			begindate = DateTools2.dateAdd("D", begindate, -1);// 向前退一天
																// 取上一期最后一天的数据
			break;
		case SEASONLY:
			begindate = DateTools2.firstDay("Q", trxnDate);
			begindate = DateTools2.dateAdd("D", begindate, -1);// 向前退一天
																// 取上一期最后一天的数据
			break;
		case HALF_YEARLY:
			begindate = DateTools2.firstDay("H", trxnDate);
			begindate = DateTools2.dateAdd("D", begindate, -1);// 向前退一天
																// 取上一期最后一天的数据
			break;
		case YEARLY:
			begindate = DateTools2.firstDay("Y", trxnDate);
			begindate = DateTools2.dateAdd("D", begindate, -1);// 向前退一天
																// 取上一期最后一天的数据
			break;
		// case NIJRI:
		// sQiShiRQi = DateTimeUtil.firstDay(sJiaoyirq, "Y");
		// sQiShiRQi = DateTools.calPrevDate(sQiShiRQi, 1);//向前退一天 取上一期最后一天的数据
		// break;

		default:
			// 不支持的报表总账区间
			throw GlError.GL.E0095();
		}

		BIZLOG.parm("begindate [%s]", begindate);
		GlGeneralLedgerDao.insInitReportData(orgId, reportType, trxnDate, begindate, dateInterval, branchId);

		BIZLOG.method("lodInit end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月11日-上午10:23:12</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param dateInterval
	 * @param reportType
	 * @return
	 */
	private static List<GlTranData> getTranData(String orgId, String trxnDate, String branchId, E_GLDATEINTERVAL dateInterval, E_REPORTTYPE reportType) {

		BIZLOG.method("getTranData begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s],trxnDate [%s],branchId [%s],dateInterval [%s],reportType [%s]", orgId, trxnDate, branchId, dateInterval, reportType);

		List<GlTranData> lstTD;

		// 装载发生额
		if (dateInterval == E_GLDATEINTERVAL.DAILY) {

			lstTD = GlGeneralLedgerDao.lstTranData(orgId, trxnDate, branchId, reportType, false);
		} else {
			String beginDate = "";
			E_GLDATEINTERVAL dateIntervalTemp = E_GLDATEINTERVAL.MONTHLY;
			switch (dateInterval) {
			case MONTHLY:
				dateIntervalTemp = E_GLDATEINTERVAL.DAILY;
				beginDate = DateTimeUtil.firstDay(trxnDate, "M");
				break;
			case SEASONLY:
				beginDate = DateTimeUtil.firstDay(trxnDate, "Q");
				break;
			case HALF_YEARLY:
				beginDate = DateTimeUtil.firstDay(trxnDate, "H");
				break;
			case YEARLY:
				beginDate = DateTimeUtil.firstDay(trxnDate, "Y");
				break;
			default:
				// 不支持的报表总账区间
				throw GlError.GL.E0095();
			}
			// 试算报表不存在月区间总账数据
			E_REPORTTYPE reportTypeTemp = reportType;
			if (E_REPORTTYPE.TRIAL_TYPE == reportType) {
				reportTypeTemp = E_REPORTTYPE.BUSINETT_TYPE;
				dateIntervalTemp = E_GLDATEINTERVAL.DAILY;
				RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
				String lastDate = runEnvs.getLstrdt();
				lstTD = GlGeneralLedgerDao.lstTranDataBySelf(orgId, beginDate, lastDate, branchId, reportTypeTemp, dateIntervalTemp, false);
				List<GlTranData> lstTdtmp = GlGeneralLedgerDao.lstTranDataBySelf(orgId, trxnDate, trxnDate, branchId, reportType, dateIntervalTemp, false);
				lstTD.addAll(lstTdtmp);
			} else {
				lstTD = GlGeneralLedgerDao.lstTranDataBySelf(orgId, beginDate, trxnDate, branchId, reportTypeTemp, dateIntervalTemp, false);
			}
		}
		BIZLOG.parm("lstTD [%s]", lstTD);
		BIZLOG.method("getTranData end <<<<<<<<<<<<<<<<<<<<");

		return lstTD;
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月11日-上午10:23:19</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param reportType
	 * @param dateInterval
	 */
	public static void prcSummary(String orgId, String trxnDate, String branchId, E_REPORTTYPE reportType, E_GLDATEINTERVAL dateInterval) {

		BIZLOG.method("prcSummary begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s],trxnDate [%s],branchId [%s],reportType [%s],dateInterval [%s]", orgId, trxnDate, branchId, reportType, dateInterval);

		int subjectLevel = GlGeneralLedgerDao.selItemMaxLevel(orgId, trxnDate, branchId, reportType, dateInterval, true);

		BIZLOG.debug("subjectLevel-=====>[%s] ", subjectLevel);

		String keyValue = FaTools.getFirstLevelSubjectLength();
		if (keyValue == null) {
			throw GlError.GL.E0096(); // //科目长度参数未设置
		}
		BIZLOG.parm("keyValue [%s] ", keyValue);
		int firstLength = Integer.valueOf(keyValue);

		keyValue = FaTools.getIncreaseSubjectLength();
		if (keyValue == null) {
			throw GlError.GL.E0096(); // //科目长度参数未设置
		}
		BIZLOG.parm("keyValue [%s] ", keyValue);
		int levelLength = Integer.valueOf(keyValue);

		// 从最末层往上逐层汇总
		for (long i = subjectLevel; i > 1; i--) {

			BIZLOG.debug("i-=====>[%s]", i);

			// 上级科目长度 = 首层科目长度 + （ 上级科目层级 - 1 ） * 递增科目长度2
			long seniorLevel = i - 1;
			long seniorLength = firstLength + (seniorLevel - 1) * levelLength;

			List<GlItemGL> lstUp = GlGeneralLedgerDao.lstUpBalance(orgId, trxnDate, branchId, i, seniorLength, reportType, dateInterval, false);

			if (CommUtil.isNotNull(lstUp) && lstUp.size() > 0) {
				insGLByItemSummary(orgId, reportType, branchId, trxnDate, dateInterval, lstUp);
			}
		}

		BIZLOG.method("prcSummary end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月11日-上午10:23:23</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param reportType
	 * @param branchId
	 * @param trxnDate
	 * @param dateInterval
	 * @param lstAmount
	 */
	public static void insGLByItemSummary(String orgId, E_REPORTTYPE reportType, String branchId, String trxnDate, E_GLDATEINTERVAL dateInterval, List<GlItemGL> lstAmount) {

		BIZLOG.method("insGLByItemSummary begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s],trxnDate [%s],branchId [%s],dateInterval [%s],reportType [%s]", orgId, trxnDate, branchId, dateInterval, reportType);
		BIZLOG.parm("lstAmount [%s]", lstAmount);

		for (GlItemGL cplAmount : lstAmount) {

			fap_accounting_subject tabSubjectInfo = Fap_accounting_subjectDao.selectOne_odb1(cplAmount.getGl_code(), true);
			glb_gl_report tabGL = SysUtil.getInstance(glb_gl_report.class);

			tabGL.setCorpno(orgId);
			tabGL.setReport_type(reportType);
			tabGL.setTrxn_date(trxnDate);
			tabGL.setBranch_id(branchId);
			tabGL.setGl_date_interval(dateInterval);
			tabGL.setCcy_code(cplAmount.getCcy_code());
			tabGL.setGl_code(cplAmount.getGl_code());
			tabGL.setOn_bal_sheet_ind(tabSubjectInfo.getOn_bal_sheet_ind());
			tabGL.setGl_code_level(tabSubjectInfo.getGl_code_level());
			tabGL.setGl_code_desc(tabSubjectInfo.getGl_code_desc());
			tabGL.setEnd_gl_code_ind(E_YESORNO.NO);
			tabGL.setSimple_list_display_ind(tabSubjectInfo.getSimple_list_display_ind());

			// tabGL.setPrev_debit_bal(cplAmount.getPrev_debit_bal);
			// tabGL.setPrev_credit_bal(cplAmount.getPrev_credit_bal());
			tabGL.setCurrent_debit_amt(cplAmount.getCurrent_debit_amt());
			tabGL.setCurrent_credit_amt(cplAmount.getCurrent_credit_amt());
			// tabGL.setCurrent_debit_bal(cplAmount.getCurrent_debit_bal());
			// tabGL.setCurrent_credit_bal(cplAmount.getCurrent_credit_bal());

			switch (tabSubjectInfo.getBal_prop()) {
			case DEBIT:
				tabGL.setPrev_debit_bal(cplAmount.getPrev_debit_bal().subtract(cplAmount.getPrev_credit_bal()));
				tabGL.setPrev_credit_bal(BigDecimal.ZERO);
				tabGL.setCurrent_debit_bal(cplAmount.getCurrent_debit_bal().subtract(cplAmount.getCurrent_credit_bal()));
				tabGL.setCurrent_credit_bal(BigDecimal.ZERO);
				break;
			case CREDIT:
				tabGL.setPrev_debit_bal(BigDecimal.ZERO);
				tabGL.setPrev_credit_bal(cplAmount.getPrev_credit_bal().subtract(cplAmount.getPrev_debit_bal()));
				tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
				tabGL.setCurrent_credit_bal(cplAmount.getCurrent_credit_bal().subtract(cplAmount.getCurrent_debit_bal()));
				break;
			case BOTH_SIDES:
				tabGL.setPrev_debit_bal(cplAmount.getPrev_debit_bal());
				tabGL.setPrev_credit_bal(cplAmount.getPrev_credit_bal());
				tabGL.setCurrent_debit_bal(cplAmount.getCurrent_debit_bal());
				tabGL.setCurrent_credit_bal(cplAmount.getCurrent_credit_bal());
				break;
			case NETTING:
				if (CommUtil.compare(cplAmount.getPrev_debit_bal(), cplAmount.getPrev_credit_bal()) >= 0) {
					tabGL.setPrev_debit_bal(cplAmount.getPrev_debit_bal().subtract(cplAmount.getPrev_credit_bal()));
					tabGL.setPrev_credit_bal(BigDecimal.ZERO);
				} else {
					tabGL.setPrev_debit_bal(BigDecimal.ZERO);
					tabGL.setPrev_credit_bal(cplAmount.getPrev_credit_bal().subtract(cplAmount.getPrev_debit_bal()));
				}

				if (CommUtil.compare(cplAmount.getCurrent_debit_bal(), cplAmount.getCurrent_credit_bal()) >= 0) {
					tabGL.setCurrent_debit_bal(cplAmount.getCurrent_debit_bal().subtract(cplAmount.getCurrent_credit_bal()));
					tabGL.setCurrent_credit_bal(BigDecimal.ZERO);
				} else {
					tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_credit_bal(cplAmount.getCurrent_credit_bal().subtract(cplAmount.getCurrent_debit_bal()));
				}

				break;
			default:
				break;
			}

			tabGL.setRecdver(1L);	// 版本号
			Glb_gl_reportDao.insert(tabGL);
		}
		BIZLOG.method("insGLByItemSummary end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月11日-上午10:23:30</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param reportType
	 */
	public static void genInterval(String orgId, String trxnDate, String branchId, E_REPORTTYPE reportType) {

		BIZLOG.method("genInterval begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s],trxnDate [%s],branchId [%s],reportType [%s]", orgId, trxnDate, branchId, reportType);
		// 月末
		String endDate = DateTimeUtil.lastDay(trxnDate, "M");
		if (trxnDate.equals(endDate)) {
			BIZLOG.method("genReport MONTHLY begin >>>>>>>>>>>>>>>>>>>>");
			genReport(orgId, trxnDate, branchId, E_GLDATEINTERVAL.MONTHLY, reportType);
			BIZLOG.method("genReport MONTHLY end >>>>>>>>>>>>>>>>>>>>");
		}
		// 为季末
		endDate = DateTimeUtil.lastDay(trxnDate, "Q");
		if (trxnDate.equals(endDate)) {
			BIZLOG.method("genReport SEASONLY begin >>>>>>>>>>>>>>>>>>>>");
			genReport(orgId, trxnDate, branchId, E_GLDATEINTERVAL.SEASONLY, reportType);
			BIZLOG.method("genReport SEASONLY end >>>>>>>>>>>>>>>>>>>>");
		}
		// 为半年
		endDate = DateTimeUtil.lastDay(trxnDate, "H");
		if (trxnDate.equals(endDate)) {
			BIZLOG.method("genReport HALF_YEARLY begin >>>>>>>>>>>>>>>>>>>>");
			genReport(orgId, trxnDate, branchId, E_GLDATEINTERVAL.HALF_YEARLY, reportType);
			BIZLOG.method("genReport HALF_YEARLY end >>>>>>>>>>>>>>>>>>>>");
		}
		// 年
		endDate = DateTimeUtil.lastDay(trxnDate, "Y");
		if (trxnDate.equals(endDate)) {
			BIZLOG.method("genReport YEARLY begin >>>>>>>>>>>>>>>>>>>>");
			genReport(orgId, trxnDate, branchId, E_GLDATEINTERVAL.YEARLY, reportType);
			BIZLOG.method("genReport YEARLY end >>>>>>>>>>>>>>>>>>>>");
		}

		BIZLOG.method("genInterval end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月11日-上午10:23:40</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param dateInterval
	 * @param reportType
	 */
	private static void chkBusiGlBal(String orgId, String trxnDate, String branchId, E_GLDATEINTERVAL dateInterval, E_REPORTTYPE reportType) {

		BIZLOG.method("chkExchangeGlBal begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s],trxnDate [%s],branchId [%s],reportType [%s] dateInterval [%s]", orgId, trxnDate, branchId, reportType, dateInterval);

		// 横向平衡检查，返回不平衡的总账信息 ,前三 条
		List<GlItemGL> lstGlBalData = GlGeneralLedgerDao.lstErrorBalData(orgId, reportType, trxnDate, branchId, dateInterval, 1, 3, false);

		long count = lstGlBalData.size();
		if (count > 0) {
			for (GlItemGL cplInfo : lstGlBalData) {
				BIZLOG.error("...branch[%s]ccy_code[%s]subject_no[%s]:[%][%][%s][%s][%][%s][%s]", cplInfo.getBranch_id(), cplInfo.getCcy_code(), cplInfo.getGl_code(), cplInfo.getPrev_debit_bal(),
						cplInfo.getPrev_credit_bal(), cplInfo.getCurrent_debit_amt(), cplInfo.getCurrent_credit_amt(), cplInfo.getCurrent_debit_bal(), cplInfo.getCurrent_credit_bal());
			}
			// 日期[%s]机构[%s]机构日总账不平衡
			throw GlError.GL.E0081(count, trxnDate, lstGlBalData.get(0).getCcy_code(), lstGlBalData.get(0).getBranch_id(), lstGlBalData.get(0).getGl_code()); //
		}

		BIZLOG.method("chkExchangeGlBal end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月11日-上午10:24:26</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param reportType
	 * @param trxnDate
	 */
	public static void genReportGL(String orgId, E_REPORTTYPE reportType, String trxnDate) {
		BIZLOG.method("genReportGL >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("orgId=[%s] trxnDate=[%s] reportType=[%s]", orgId, trxnDate, reportType);

		//当前法人
		String corpno = CommToolsAplt.prcRunEnvs().getCorpno();
		//机构对照关系
		PbEnumType.E_BRMPTP brmptp = PbEnumType.E_BRMPTP.C;
		
		//从最低层逐级向上,

		//报表关系代码参数
		String ccyFlag = ApConstants.WILDCARD;
		if (CommUtil.equals(FaTools.getDiffCcyInd(), E_YESORNO.YES.getValue())) {
			// 区分币种时,取本币
			ccyFlag = CommTools.getDefineCurrency();
		}

		/*String topBranchId = SysUtil.getInstance(IoSrvPbBranch.class).getRootBranch(ccyFlag, PbEnumType.E_BRMPTP.C);
        if (topBranchId==null )
            throw GlError.GL.E0098();  // //获取报表关系根机构失败
        long brchMaxLevel = SysUtil.getInstance(IoSrvPbBranch.class).getBranchMaxLevel(ccyFlag, PbEnumType.E_BRMPTP.C, topBranchId);
        if (CommUtil.isNull(brchMaxLevel) || brchMaxLevel <= 0 )
            throw GlError.GL.E0099();  // //账务机构关系代码级数读取出错
        
        
        for (long i = brchMaxLevel; i > 0; i--) {
            bizlog.parm("Level[%s]", i);
            List<PbBranchUpLow> brchList = SysUtil.getInstance(IoSrvPbBranch.class).getBranchListByLevel(ccyFlag, PbEnumType.E_BRMPTP.C, topBranchId, i);
            if (brchList.size() <= 0) {
                throw GlError.GL.E0058(i);  // //读取[%s]级机构失败
            }
            for (PbBranchUpLow cplInfo : brchList) {
                bizlog.parm("senior branch[%s]junior branch[%s]", cplInfo.getUppebr(), cplInfo.getLowebr());
                if (cplInfo.getUppebr().equals(cplInfo.getLowebr())) {
                    //上下级机构不能相同
                    throw GlError.GL.E0059(cplInfo.getUppebr());  // 
                }
                GlGeneralLedgerDao.insReportGlData(orgId, trxnDate, report_type, cplInfo.getUppebr(), PbEnumType.E_BRMPTP.C.toString(), ccyFlag);
                break; //cplInfo父节点值一样,只需要跑一次
            }
        }*/
		
		
		/**
		//mdy by zhanga 使用下面的逻辑进行替代
		//获取当前法人下最大机构层级数 (注：当前法人下无层级关系的不作处理 ，中心法人下县级法人数据由中心法人批量汇总)
		long brchMaxLevel = SysUtil.getInstance(IoSrvPbBranch.class).getBranchMaxLevelByCorpno(ccyFlag, brmptp, corpno);
		for (long i = brchMaxLevel; i > 0; i--) {
		    BIZLOG.parm("Level[%s]", i);
			//获取当前法人下当前层级的上级机构列表，没有则不作处理
			List<PbBranchUpLow> brchList = SysUtil.getInstance(IoSrvPbBranch.class).getDistinctBranchListByLevelAndCorpno(
			        ccyFlag, brmptp, corpno, i);
            for (PbBranchUpLow cplInfo : brchList) {
                BIZLOG.parm("cplInfo[%s]", cplInfo);
				GlGeneralLedgerDao.insReportGlData(corpno, trxnDate, reportType, cplInfo.getUppebr(), 
				        brmptp.toString(), ccyFlag);
			}
		}
		**/
		
		/*mdy by zhanga --按机构层级关系逐级向上汇总
		 * */
		
		//获取当前法人下最大机构层级数 (注：当前法人下无层级关系的不作处理 ，中心法人下县级法人数据由中心法人批量汇总)
		long brchMaxLevel = SysUtil.getInstance(IoSrvPbBranch.class).getBranchMaxLevelByCorpno(ccyFlag, brmptp, corpno);
		for (long i = brchMaxLevel; i > 0; i--) {
		    BIZLOG.parm("Level[%s]", i);
			//获取当前法人下当前层级的上级机构列表，没有则不作处理
			List<PbBranchUpLow> brchList = SysUtil.getInstance(IoSrvPbBranch.class).getDistinctBranchListByLevelAndCorpno(ccyFlag, brmptp, corpno, i);
			for (PbBranchUpLow cplInfo : brchList) {
                BIZLOG.parm("cplInfo[%s]", cplInfo);
                
                List<glb_gl_report> lstGlbGlReport = GlGeneralLedgerDao.lstReportGlData(corpno, trxnDate, reportType, cplInfo.getUppebr(), 
				        brmptp.toString(), ccyFlag, false);
				
                for (glb_gl_report report : lstGlbGlReport) {

    				boolean bIns = false; // 插入标记
    				fap_accounting_subject tabSubjectInfo = Fap_accounting_subjectDao.selectOne_odb1(report.getGl_code(), true);
    				glb_gl_report tabGL = SysUtil.getInstance(glb_gl_report.class);

    				// 取总账记录,取到做更新，未取到做插入
    				try {
    					bIns = false;
    					tabGL = Glb_gl_reportDao.selectOne_odb1(reportType, trxnDate, report.getBranch_id(), report.getGl_date_interval(), report.getCcy_code(), report.getGl_code(), true);
    				} catch (AdpDaoNoDataFoundException e) {

    					tabGL.setCorpno(orgId);
    					tabGL.setTrxn_date(trxnDate);
    					tabGL.setBranch_id(report.getBranch_id());
    					tabGL.setReport_type(reportType);
    					tabGL.setCcy_code(report.getCcy_code());
    					tabGL.setGl_code(report.getGl_code());
    					tabGL.setOn_bal_sheet_ind(tabSubjectInfo.getOn_bal_sheet_ind());
    					tabGL.setGl_code_level(tabSubjectInfo.getGl_code_level());
    					tabGL.setGl_code_desc(tabSubjectInfo.getGl_code_desc());
    					tabGL.setEnd_gl_code_ind(tabSubjectInfo.getEnd_gl_code_ind());
    					tabGL.setSimple_list_display_ind(tabSubjectInfo.getSimple_list_display_ind());
    					tabGL.setGl_date_interval(report.getGl_date_interval());
    					tabGL.setPrev_debit_bal(BigDecimal.ZERO);
    					tabGL.setPrev_credit_bal(BigDecimal.ZERO);
    					tabGL.setCurrent_debit_amt(BigDecimal.ZERO);
    					tabGL.setCurrent_credit_amt(BigDecimal.ZERO);
    					tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
    					tabGL.setCurrent_credit_bal(BigDecimal.ZERO);

    					bIns = true;
    				} catch (Exception e) {
    					// 读取xxx失败
    					throw GlError.GL.E0082(OdbFactory.getTable(glb_gl_report.class).getLongname());
    				}

    				// 发生额处理
    				tabGL.setCurrent_debit_amt(tabGL.getCurrent_debit_amt().add(report.getCurrent_debit_amt()));
    				tabGL.setCurrent_credit_amt(tabGL.getCurrent_credit_amt().add(report.getCurrent_credit_amt()));

    				// 余额计算
    				GlSubjectBal cplBal = GlBranch.calSubjectBal(tabSubjectInfo.getBal_prop(), tabGL.getPrev_debit_bal(), tabGL.getPrev_credit_bal(), tabGL.getCurrent_debit_amt(),
    						tabGL.getCurrent_credit_amt());

    				// 贷方余额处理
    				tabGL.setCurrent_debit_bal(cplBal.getCurrent_debit_bal());
    				tabGL.setCurrent_credit_bal(cplBal.getCurrent_credit_bal());

    				// 最终数据
    				BIZLOG.debug("tabGL=====>[%s]", tabGL);

    				/* 更新数据库 */
    				if (bIns) {
    					Glb_gl_reportDao.insert(tabGL);
    				} else {
    					Glb_gl_reportDao.updateOne_odb1(tabGL);
    				}
                }
			}
		}
	}
	
}
