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
import cn.sunline.ltts.busi.gl.type.GlReport.GlReportData;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_GLDATEINTERVAL;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ITEMDATATYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTNAME;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.Glb_balance_sheetDao;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glb_balance_sheet;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glp_report_define;

public class GlBalanceSheet {
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlBalanceSheet.class);

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月17日-上午11:23:06</li>
	 *         <li>功能说明：生成资产负债表</li>
	 *         </p>
	 *         s
	 * @param orgId
	 * @param reportType
	 * @param branchId
	 * @param trxnDate
	 */
	public static void genBalanceSheet(String orgId, E_REPORTTYPE reportType, String branchId, String trxnDate) {

		// 删除已有数据
		GlReportDao.delBalanceSheet(orgId, reportType, branchId, trxnDate);

		// 日报
		genBalanceSheetReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.DAILY);

		// 月报
		if (DateTools2.isLastDay("M", trxnDate)) {
			genBalanceSheetReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.MONTHLY);
		}
		
		// 季报
		if (DateTools2.isLastDay("Q", trxnDate)) {
			genBalanceSheetReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.SEASONLY);
		}

		// 半年报
		if (DateTools2.isLastDay("H", trxnDate)) {
			genBalanceSheetReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.HALF_YEARLY);
		}

		// 年结
		if (DateTools2.isLastDay("Y", trxnDate)) {
			genBalanceSheetReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.YEARLY);
		}

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月17日-上午11:15:39</li>
	 *         <li>功能说明：生成资产负债表日报</li>
	 *         </p>
	 */
	public static void genBalanceSheetReport(E_REPORTTYPE reportType, String trxnDate, String branchId, String orgId, E_GLDATEINTERVAL dataInterVal) {

		// 取所有的货币：交易货币+折本+折美
		List<String> ccyCodeList = new ArrayList<String>();

		// 交易币种
		List<ApCurrency> ccyList = CommTools.listApCurrency();
		for (ApCurrency ccyCode : ccyList) {
			ccyCodeList.add(ccyCode.getCrcycd());
		}
		// 折币种
		// ccyCodeList.add(FaConst.EX_CCY_CODE_SCY);
		// ccyCodeList.add(FaConst.EX_CCY_CODE_SUS);
		List<KnpPara> exCcyList = ApKnpPara.listKnpPara(FaConst.EX_CCY_CODE, false);
		if (exCcyList.size() > 0) {
			for (KnpPara cplInfo : exCcyList) {
				ccyCodeList.add(cplInfo.getPmval1());
			}
		}

		for (String ccyCode : ccyCodeList) {

			// 查询是否有相关总账数据
			int i = GlReportDao.selCountsFromReport(reportType, trxnDate, branchId, ccyCode, orgId, false);

			if (i > 0) {
				// 报表数据定义
				List<glp_report_define> reportDefines = GlReportDao.lstReprotElement(orgId, E_REPORTNAME.THE_BALANCE_SHEET, false);

				for (glp_report_define reportDefine : reportDefines) {
					BIZLOG.debug("reportDefine[%s]", reportDefine);

					// 打印显示号不为0
					if (CommUtil.compare(reportDefine.getPrint_display_no(), "0") != 0) {

						genBalanceSheetReportDetail(reportDefine, trxnDate, dataInterVal, reportType, branchId, ccyCode, orgId);
					}

				}
			}

		}

	}

	public static void genBalanceSheetReportDetail(glp_report_define reportDefine, String trxnDate, E_GLDATEINTERVAL dataInterval, E_REPORTTYPE reportType, String branchId, String ccyCode,
			String ordId) {

		BIZLOG.debug("reportDefine[%s]", reportDefine);
		// 根据总账区间获取起始日期
		GlReportData reportData = SysUtil.getInstance(GlReportData.class);

		// 如果是占位项，则无需显示
		if (reportDefine.getItem_data_type() == E_ITEMDATATYPE.PLACEHOLDER) {
			reportData.setAdd_up_amt(null);
			reportData.setCur_term_amt(null);
		} else {
			String startDate = null;

			// 除了 日报以外都以 月 为统计维度
			E_GLDATEINTERVAL baseDataInterval = E_GLDATEINTERVAL.MONTHLY;

			switch (dataInterval) {

			case DAILY:
				startDate = trxnDate;
				baseDataInterval = E_GLDATEINTERVAL.DAILY;

				break;
			case MONTHLY:

				startDate = DateTools2.firstDay("M", trxnDate);
				break;
			case HALF_YEARLY:

				startDate = DateTools2.firstDay("H", trxnDate);
				break;
			case SEASONLY:

				startDate = DateTools2.firstDay("Q", trxnDate);
				break;
			case YEARLY:

				startDate = DateTools2.firstDay("Y", trxnDate);

				break;

			default:
				break;
			}

			reportData = GlReportDao
					.lstReportData(reportType, E_REPORTNAME.THE_BALANCE_SHEET, branchId, ccyCode, baseDataInterval, reportDefine.getReport_item_no(), startDate, trxnDate, ordId, false);
			// 取到空值，赋值0
			if (CommUtil.isNull(reportData)) {
				reportData = SysUtil.getInstance(GlReportData.class);
				reportData.setAdd_up_amt(BigDecimal.ZERO);
				reportData.setCur_term_amt(BigDecimal.ZERO);
			}
		}

		glb_balance_sheet balanceSheet = SysUtil.getInstance(glb_balance_sheet.class);

		if (reportDefine.getReport_item_column() == 1) { // 第一列

			balanceSheet.setReport_type(reportType); // 报表类型
			balanceSheet.setTrxn_date(trxnDate); // 交易日期
			balanceSheet.setBranch_id(branchId); // 机构号
			balanceSheet.setGl_date_interval(dataInterval); // 总账区间
			balanceSheet.setCcy_code(ccyCode); // 货币代码
			balanceSheet.setReport_item_line(reportDefine.getReport_item_line()); // 报表项次归属行
			balanceSheet.setFirst_column_item_no(reportDefine.getReport_item_no()); // 第一列项次编号
			balanceSheet.setFirst_column_item_name(GlReports.prcFillCHSpace(reportDefine.getItem_data_name(), reportDefine.getFront_fill_space_count())); // 第一列项次名称
			balanceSheet.setFirst_column_print_display_no(reportDefine.getPrint_display_no()); // 第一列打印显示号

			// 计算数据存在
			if (CommUtil.isNotNull(reportData.getAdd_up_amt()) && CommUtil.isNotNull(reportData.getCur_term_amt())) {
				balanceSheet.setFirst_column_cur_term_amt(reportData.getAdd_up_amt().subtract(reportData.getCur_term_amt())); // 第一列本期金额
				balanceSheet.setFirst_column_add_up_amt(reportData.getAdd_up_amt()); // 第一列累计金额
			}

			Glb_balance_sheetDao.insert(balanceSheet);

		} else {
			balanceSheet = Glb_balance_sheetDao.selectOne_odb1(reportType, trxnDate, branchId, dataInterval, ccyCode, reportDefine.getReport_item_line(), false);

			if (balanceSheet == null) {
				// BIZLOG.debug("reportType[%s], trxnDate[%s], branchId[%s], dataInterval[%s], ccyCode[%s], reportDefine.getReport_item_line()[%s]",
				// reportType, trxnDate, branchId, dataInterval, ccyCode,
				// reportDefine.getReport_item_line());
				return;
			}

			balanceSheet.setSecond_column_item_no(reportDefine.getReport_item_no()); // 第二列项次编号
			balanceSheet.setSecond_column_item_name(reportDefine.getItem_data_name()); // 第二列项次名称
			balanceSheet.setSecond_column_print_display_no(reportDefine.getPrint_display_no()); // 第二列打印显示号

			if (CommUtil.isNotNull(reportData.getAdd_up_amt()) && CommUtil.isNotNull(reportData.getCur_term_amt())) {
				balanceSheet.setSecond_column_cur_term_amt(reportData.getAdd_up_amt().subtract(reportData.getCur_term_amt())); // 第二列本期金额
				balanceSheet.setSecond_column_add_up_amt(reportData.getAdd_up_amt()); // 第二列累计金额
			}
			Glb_balance_sheetDao.updateOne_odb1(balanceSheet);

		}

	}

}
