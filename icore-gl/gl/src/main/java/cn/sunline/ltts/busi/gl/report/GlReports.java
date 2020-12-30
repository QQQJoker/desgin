package cn.sunline.ltts.busi.gl.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApCurrency;
import cn.sunline.ltts.busi.gl.namedsql.GlReportDao;
import cn.sunline.ltts.busi.gl.type.GlReport.GlFactorCal;
import cn.sunline.ltts.busi.gl.type.GlReport.GlReportDetailCalIn;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_GLDATEINTERVAL;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ITEMDATAPROP;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ITEMDATATYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_NETTINGWAY;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTNAME;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.Glb_report_calc_resultDao;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glb_report_calc_result;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glp_report_define;

public class GlReports {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlReports.class);

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月14日-下午4:09:46</li>
	 *         <li>功能说明：生成三大报表，资产负债表，损益明细表，利润表</li>
	 *         </p>
	 * @param orgId
	 * @param reportType
	 */
	public static void genThreeReportsMain(String orgId, E_REPORTTYPE reportType, E_REPORTNAME[] reportNames, String trxnDate, String branchId) {
		BIZLOG.method("genThreeReports >>>>>>>>>>begin>>>>>>>>>");

		// 计算把各个报表数据，将计算结果插入 报表计算结果 表中
		for (E_REPORTNAME reportName : reportNames) {
			genReport(reportName, orgId, reportType, trxnDate, branchId);
		}

		// 资产负债表
		GlBalanceSheet.genBalanceSheet(orgId, reportType, branchId, trxnDate);
		
		// 损益表
		GlIncome.genIncomeMain(orgId, trxnDate, reportType, branchId);

		// 利润表
		GlProftLoss.genPriftMain(orgId, trxnDate, reportType, branchId);

		BIZLOG.method("genThreeReports >>>>>>>>>>end>>>>>>>>>>>");

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月17日-上午10:23:35</li>
	 *         <li>功能说明：计算结果插入 报表计算结果数据</li>
	 *         </p>
	 * @param reportName
	 * @param orgId
	 * @param reportType
	 * @param trxnDate
	 * @param branchId
	 */
	public static void genReport(E_REPORTNAME reportName, String orgId, E_REPORTTYPE reportType, String trxnDate, String branchId) {

		// 删除T日数据
		GlReportDao.delReportCalResult(orgId, reportName, reportType, trxnDate, branchId);

		// 取所有的货币：交易货币+折本+折美 
		List<String> ccyCodeList = new ArrayList<String>();
		List<ApCurrency> ccyList = CommTools.listApCurrency();
		// List<ApDropListInfo> ccyList = ApDropList.getItems(FaConst.CCY_CODE);
		List<KnpPara> exCcyList = ApKnpPara.listKnpPara(FaConst.EX_CCY_CODE,false);
		//
		for (ApCurrency ccyInfo : ccyList) {

			ccyCodeList.add(ccyInfo.getCrcycd());
		}

		for (KnpPara exCcyInfo : exCcyList) {
			ccyCodeList.add(exCcyInfo.getPmval1());
		}

		boolean yearEndInd = false;

		if (DateTools2.isLastDay("Y", trxnDate)) {
			yearEndInd = true;
		}
		GlReportDetailCalIn reportDetailCalIn = SysUtil.getInstance(GlReportDetailCalIn.class);

		reportDetailCalIn.setTrxn_date(trxnDate); // 交易日期
		reportDetailCalIn.setBranch_id(branchId); // 机构号

		reportDetailCalIn.setReport_type(reportType); // 报表类型
		reportDetailCalIn.setOrg_id(orgId); // 法人代码

		for (String ccyCode : ccyCodeList) {

			reportDetailCalIn.setCcy_code(ccyCode); // 货币代码
			// 查询是否有相关总账数据
			int i = GlReportDao.selCountsFromReport(reportType, trxnDate, branchId, ccyCode, orgId, false);

			if (i > 0) {
				// 报表数据定义
				List<glp_report_define> reportDefines = GlReportDao.lstReprotDefine(orgId, reportName, false);

				for (glp_report_define reportDefine : reportDefines) {

					genReportDetail(reportDefine, yearEndInd, reportDetailCalIn);
				}
			}

		}
		// 月末
		if (DateTools2.isLastDay("M", trxnDate)) {
			GlReportDao.insMonthReportResults(orgId, reportType, reportName, branchId, DateTools2.firstDay("M", trxnDate), trxnDate, trxnDate);
		}
	}

	public static void genReportDetail(glp_report_define reportDefine, boolean yearEndInd, GlReportDetailCalIn reportDetailCalIn) {
		BIZLOG.method("genReportDetail Begin>>>>>>>>>>>>");

		GlFactorCal addItemCalcFactor = SysUtil.getInstance(GlFactorCal.class); // 增项因子计算结果
		GlFactorCal reduceItemCalcFactor = SysUtil.getInstance(GlFactorCal.class); // 减项因子计算结果
		GlFactorCal nettingItemCalcFactor = SysUtil.getInstance(GlFactorCal.class); // 轧差因子计算结果

		addItemCalcFactor.setBal_margin(BigDecimal.ZERO);
		addItemCalcFactor.setOccurrence_margin(BigDecimal.ZERO);	
		reduceItemCalcFactor.setBal_margin(BigDecimal.ZERO);
		reduceItemCalcFactor.setOccurrence_margin(BigDecimal.ZERO);	
		nettingItemCalcFactor.setBal_margin(BigDecimal.ZERO);
		nettingItemCalcFactor.setOccurrence_margin(BigDecimal.ZERO);	
		
		// 报表结算结果表
		glb_report_calc_result reportCalcResult = SysUtil.getInstance(glb_report_calc_result.class);

		// 如果是数据项，则从总账表里取数计算
		if (reportDefine.getItem_data_type() == E_ITEMDATATYPE.DATA_ELEMENT) {

			E_GLDATEINTERVAL glDateInterval = E_GLDATEINTERVAL.DAILY;

			// 损益表 和 利润表 来自 年总账
			if (yearEndInd) {

				if (reportDefine.getReport_name() == E_REPORTNAME.INCOME_STATEMENT || reportDefine.getReport_name() == E_REPORTNAME.PROFIT_SHEET) {
					glDateInterval = E_GLDATEINTERVAL.YEARLY;
				}

			}
			if (CommUtil.isNull(reportDefine.getItem_data_prop())) {
				throw GlError.GL.E0060(reportDefine.getReport_name().getLongName(), reportDefine.getReport_item_no());
			}

			// 轧差方式是汇总，先汇总再取值
			if (reportDefine.getNetting_way() == E_NETTINGWAY.BY_COLLECTION) {
				// 资产的轧差汇总
				if (reportDefine.getItem_data_prop() == E_ITEMDATAPROP.ASSETS) {
					nettingItemCalcFactor = GlReportDao.selBalanceByBusiGlByCollectDebit(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(),
							reportDetailCalIn.getCcy_code(), reportDetailCalIn.getReport_type(), glDateInterval, reportDetailCalIn.getOrg_id(),
							',' + reportDefine.getNetting_item() + ',', false);

					// 负债的轧差汇总
				} else if (reportDefine.getItem_data_prop() == E_ITEMDATAPROP.LABILITIES_AND_CAPITAL) {

					nettingItemCalcFactor = GlReportDao.selBalanceByBusiGlByCollectCrebit(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(),
							reportDetailCalIn.getCcy_code(), reportDetailCalIn.getReport_type(), glDateInterval, reportDetailCalIn.getOrg_id(),
							',' + reportDefine.getNetting_item() + ',', false);

				}

				// 如果存在增项计算因子
				if (CommUtil.isNotNull(reportDefine.getAdd_item_calc_factor())) {

					addItemCalcFactor = GlReportDao.selBalanceByBusiGl(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(), reportDetailCalIn.getCcy_code(),
							reportDetailCalIn.getReport_type(), glDateInterval, reportDetailCalIn.getOrg_id(), ',' + reportDefine.getAdd_item_calc_factor() + ',', false);

				} // 以科目方式轧差汇总
			} else if (reportDefine.getNetting_way() == E_NETTINGWAY.BY_SUBJECT) {
				if (reportDefine.getItem_data_prop() == E_ITEMDATAPROP.ASSETS) {

					nettingItemCalcFactor = GlReportDao.selBalanceByBusiGlBySubjectDebit(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(),
							reportDetailCalIn.getCcy_code(), reportDetailCalIn.getReport_type(), glDateInterval, reportDetailCalIn.getOrg_id(),
							',' + reportDefine.getNetting_item() + ',', false);
					// 负债的轧差汇总
				}
				else if (reportDefine.getItem_data_prop() == E_ITEMDATAPROP.LABILITIES_AND_CAPITAL) {
					nettingItemCalcFactor = GlReportDao.selBalanceByBusiGlBySubjectCrebit(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(),
							reportDetailCalIn.getCcy_code(), reportDetailCalIn.getReport_type(), glDateInterval, reportDetailCalIn.getOrg_id(),
							',' + reportDefine.getNetting_item() + ',', false);
				}

				// 如果存在增项计算因子
				if (CommUtil.isNotNull(reportDefine.getAdd_item_calc_factor())) {

					addItemCalcFactor = GlReportDao.selBalanceByBusiGl(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(), reportDetailCalIn.getCcy_code(),
							reportDetailCalIn.getReport_type(), glDateInterval, reportDetailCalIn.getOrg_id(), ',' + reportDefine.getAdd_item_calc_factor() + ',', false);
				}
			} else {
				// 按增项处理
				if (CommUtil.isNotNull(reportDefine.getAdd_item_calc_factor())) {

					addItemCalcFactor = GlReportDao.selBalanceByBusiGl(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(), reportDetailCalIn.getCcy_code(),
							reportDetailCalIn.getReport_type(), glDateInterval, reportDetailCalIn.getOrg_id(), ',' + reportDefine.getAdd_item_calc_factor() + ',', false);
				}
			}

			// 减项因子处理
			if (CommUtil.isNotNull(reportDefine.getReduce_item_calc_factor())) {
				reduceItemCalcFactor = GlReportDao.selBalanceByBusiGl(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(), reportDetailCalIn.getCcy_code(),
						reportDetailCalIn.getReport_type(), glDateInterval, reportDetailCalIn.getOrg_id(), ',' + reportDefine.getReduce_item_calc_factor() + ',', false);
			}
			// 根据数据性质调整
			switch (reportDefine.getItem_data_prop()) {

			case ASSETS:

				//  前面的计算结果：借方-贷方；而资产的支出的减项计算因子应该是贷方-借方，故转换方向
			case PAYMENT:
				reduceItemCalcFactor.setOccurrence_margin(BigDecimal.ZERO.subtract(reduceItemCalcFactor.getOccurrence_margin()));
				reduceItemCalcFactor.setBal_margin(BigDecimal.ZERO.subtract(reduceItemCalcFactor.getBal_margin()));
				break;

			default: // 负债/收入的增加应该是：贷方-借方；故转换方向
				addItemCalcFactor.setOccurrence_margin(BigDecimal.ZERO.subtract(addItemCalcFactor.getOccurrence_margin()));
				addItemCalcFactor.setBal_margin(BigDecimal.ZERO.subtract(addItemCalcFactor.getBal_margin()));
				break;
			}
		} else if (reportDefine.getItem_data_type() == E_ITEMDATATYPE.CACULATE) {
			// 增项处理
			if (CommUtil.isNotNull(reportDefine.getAdd_item_calc_factor())) {
				addItemCalcFactor = GlReportDao.selBalanceFromReportResult(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(), reportDetailCalIn.getReport_type(),
						reportDefine.getReport_name(), reportDetailCalIn.getCcy_code(), reportDetailCalIn.getOrg_id(), ',' + reportDefine.getAdd_item_calc_factor() + ',', false);
			}
			// 减项处理
			if (CommUtil.isNotNull(reportDefine.getReduce_item_calc_factor())) {
				reduceItemCalcFactor = GlReportDao.selBalanceFromReportResult(reportDetailCalIn.getTrxn_date(), reportDetailCalIn.getBranch_id(), reportDetailCalIn.getReport_type(), 
						reportDefine.getReport_name(), reportDetailCalIn.getCcy_code(), reportDetailCalIn.getOrg_id(), ',' + reportDefine.getReduce_item_calc_factor() + ',', false);
			}
		}

		// 处理轧差
		addItemCalcFactor.setOccurrence_margin(nettingItemCalcFactor.getOccurrence_margin().add(addItemCalcFactor.getOccurrence_margin()));
		addItemCalcFactor.setBal_margin(nettingItemCalcFactor.getBal_margin().add(addItemCalcFactor.getBal_margin()));

		// 轧差方式为3-按比例计算时，特殊处理
		if (reportDefine.getNetting_way() == E_NETTINGWAY.BY_RATE) {
			int scale = Integer.valueOf(CommUtil.nvl(reportDefine.getNetting_item(), "2"));//保留小数点位数
			
			if( CommUtil.compare(reduceItemCalcFactor.getOccurrence_margin(), BigDecimal.ZERO) == 0){
				// 本期发生比例
				reportCalcResult.setCur_term_amt( BigDecimal.ZERO );
			}else{
				// 本期发生比例
				reportCalcResult.setCur_term_amt(addItemCalcFactor.getOccurrence_margin().divide(reduceItemCalcFactor.getOccurrence_margin(), scale,BigDecimal.ROUND_HALF_UP ).multiply(BigDecimal.valueOf(100)));
			}
			if( CommUtil.compare(reduceItemCalcFactor.getBal_margin(), BigDecimal.ZERO) == 0){
				// 累计余额比例
				reportCalcResult.setAdd_up_amt( BigDecimal.ZERO );
			}else{
				// 累计余额比例
				reportCalcResult.setAdd_up_amt(addItemCalcFactor.getBal_margin().divide(reduceItemCalcFactor.getBal_margin(), scale,BigDecimal.ROUND_HALF_UP ).multiply(BigDecimal.valueOf(100)));
			}
		}else{
			// 本期金额= 增项-减项
			reportCalcResult.setCur_term_amt(addItemCalcFactor.getOccurrence_margin().subtract(reduceItemCalcFactor.getOccurrence_margin()));
			// 累计金额=增项-减项
			reportCalcResult.setAdd_up_amt(addItemCalcFactor.getBal_margin().subtract(reduceItemCalcFactor.getBal_margin()));

			// 结果都为零，则不登记
			if (CommUtil.compare(reportCalcResult.getCur_term_amt(), new BigDecimal(0)) == 0 && CommUtil.compare(reportCalcResult.getAdd_up_amt(), new BigDecimal(0)) == 0) {
				BIZLOG.debug("The results calculated are zero, so there is no need to record them.");
				return;
			}
		}
		
		
		reportCalcResult.setReport_name(reportDefine.getReport_name()); // 报表名称
		reportCalcResult.setReport_type(reportDetailCalIn.getReport_type()); // 报表类型
		reportCalcResult.setTrxn_date(reportDetailCalIn.getTrxn_date()); // 交易日期
		reportCalcResult.setBranch_id(reportDetailCalIn.getBranch_id()); // 机构号
		reportCalcResult.setGl_date_interval(E_GLDATEINTERVAL.DAILY); // 总账区间
		reportCalcResult.setCcy_code(reportDetailCalIn.getCcy_code()); // 货币代码
		reportCalcResult.setReport_item_no(reportDefine.getReport_item_no()); // 报表项次编号

		Glb_report_calc_resultDao.insert(reportCalcResult);

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2014年8月25日-上午10:35:46</li>
	 *         <li>在字段前填充中文全角空格</li>
	 *         </p>
	 * @param sFieldVal
	 * @param lNum
	 * @return
	 */
	public static String prcFillCHSpace(String sFieldVal, Long lNum) {

		BIZLOG.method("prcFillCHSpace>>>>>>>>>>>>Begin>>>>>>>>>>>>");

		BIZLOG.parm("sFieldVal [%s],lNum [%s]", sFieldVal, String.valueOf(lNum));

		if (lNum == null || lNum <= 0) {
			BIZLOG.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
			return sFieldVal;
		} else {
			// 中文全角空格
			String sChineseSpace = "　";
			String sFillVal = "";

			for (int i = 0; i < lNum; i++) {
				sFillVal = sFillVal + sChineseSpace;
			}
			BIZLOG.method("<<<<<<<<<<<<End<<<<<<<<<<<<");

			return sFillVal + sFieldVal;
		}
	}

}
