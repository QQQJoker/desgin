package cn.sunline.ltts.busi.gl.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.Glb_income_statementDao;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glb_income_statement;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glp_report_define;

public class GlIncome {

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月17日-下午3:56:44</li>
	 *         <li>功能说明：损益表</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param reportType
	 * @param branchId
	 */
	public static void genIncomeMain(String orgId, String trxnDate, E_REPORTTYPE reportType, String branchId) {

		// 删除损益表
		GlReportDao.delIncomeTable(orgId, trxnDate, reportType, branchId);

		// 生成日报
		genReport(orgId, reportType, trxnDate, branchId, E_GLDATEINTERVAL.DAILY);

		// 月报
		if (DateTools2.isLastDay("M", trxnDate)) {
			genReport(orgId, reportType, trxnDate, branchId, E_GLDATEINTERVAL.MONTHLY);
		}
		
		// 季报
		if (DateTools2.isLastDay("Q", trxnDate)) {
			genReport(orgId, reportType, trxnDate, branchId, E_GLDATEINTERVAL.SEASONLY);
		}

		// 半年报
		if (DateTools2.isLastDay("H", trxnDate)) {
			genReport(orgId, reportType, trxnDate, branchId, E_GLDATEINTERVAL.HALF_YEARLY);
		}

		// 年报
		if (DateTools2.isLastDay("Y", trxnDate)) {
			genReport(orgId, reportType, trxnDate, branchId, E_GLDATEINTERVAL.YEARLY);

		}

	}

	public static void genReport(String orgId, E_REPORTTYPE reportType, String trxnDate, String branchId, E_GLDATEINTERVAL dataInterval) {

		// 删除记录
		// GlReportDao.delIncome(orgId, reportType, trxnDate, branchId);

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

			// 取报表定义表的数据
			List<glp_report_define> reportDefineList = GlReportDao.lstReprotElement(orgId, E_REPORTNAME.INCOME_STATEMENT, false);

			for (glp_report_define reportDefine : reportDefineList) {

				genReportDetail(reportDefine, trxnDate, dataInterval, reportType, branchId, ccyCode, orgId);

			}
		}
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月17日-下午4:03:32</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 */
	public static void genReportDetail(glp_report_define reportDefine, String trxnDate, E_GLDATEINTERVAL dataInterval, E_REPORTTYPE reportType, String branchId, String ccyCode, String orgId) {

		GlReportData reportData;

		if (reportDefine.getItem_data_type() == E_ITEMDATATYPE.PLACEHOLDER) {
			reportData = SysUtil.getInstance(GlReportData.class);
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

			reportData = GlReportDao.lstReportData(reportType, E_REPORTNAME.INCOME_STATEMENT, branchId, ccyCode, baseDataInterval, reportDefine.getReport_item_no(), startDate, trxnDate, orgId, false);

			if (reportData == null) {

				reportData = SysUtil.getInstance(GlReportData.class);
				reportData.setAdd_up_amt(BigDecimal.ZERO);
				reportData.setCur_term_amt(BigDecimal.ZERO);
			}

		}

		glb_income_statement incodeSate = SysUtil.getInstance(glb_income_statement.class);

		if (reportDefine.getReport_item_column() == 1) { // 第一列

			incodeSate.setReport_type(reportType); // 报表类型
			incodeSate.setTrxn_date(trxnDate); // 交易日期
			incodeSate.setBranch_id(branchId); // 机构号
			incodeSate.setGl_date_interval(dataInterval); // 总账区间
			incodeSate.setCcy_code(ccyCode); // 货币代码
			incodeSate.setReport_item_line(reportDefine.getReport_item_line()); // 报表项次归属行
			incodeSate.setFirst_column_item_no(reportDefine.getReport_item_no()); // 第一列项次编号
			incodeSate.setFirst_column_item_name(GlReports.prcFillCHSpace(reportDefine.getItem_data_name(), reportDefine.getFront_fill_space_count())); // 第一列项次名称
			incodeSate.setFirst_column_print_display_no(reportDefine.getPrint_display_no()); // 第一列打印显示号
			incodeSate.setFirst_column_cur_term_amt(reportData.getCur_term_amt());
			incodeSate.setFirst_column_add_up_amt(reportData.getAdd_up_amt());
			// if (CommUtil.isNotNull(reportData)) {
			// incodeSate.setFirst_column_cur_term_amt(reportData.getAdd_up_amt().subtract(reportData.getCur_term_amt()));
			// //第一列本期金额
			// incodeSate.setFirst_column_add_up_amt(reportData.getAdd_up_amt());
			// //第一列累计金额
			// }

			Glb_income_statementDao.insert(incodeSate);

		} else {

			incodeSate = Glb_income_statementDao.selectOne_odb1(reportType, trxnDate, branchId, dataInterval, ccyCode, reportDefine.getReport_item_line(), false);
			if (incodeSate == null) {
				return;
			}
			incodeSate.setReport_type(reportType); // 报表类型
			incodeSate.setTrxn_date(trxnDate); // 交易日期
			incodeSate.setBranch_id(branchId); // 机构号
			incodeSate.setGl_date_interval(dataInterval); // 总账区间
			incodeSate.setCcy_code(ccyCode); // 货币代码
			incodeSate.setReport_item_line(reportDefine.getReport_item_line()); // 报表项次归属行
			incodeSate.setSecond_column_item_no(reportDefine.getReport_item_no()); // 第二列项次编号
			incodeSate.setSecond_column_item_name(GlReports.prcFillCHSpace(reportDefine.getItem_data_name(), reportDefine.getFront_fill_space_count())); // 第二列项次名称
			incodeSate.setSecond_column_print_display_no(reportDefine.getPrint_display_no()); // 第二列打印显示号
			incodeSate.setSecond_column_cur_term_amt(reportData.getCur_term_amt());
			incodeSate.setSecond_column_add_up_amt(reportData.getAdd_up_amt());

			// if (CommUtil.isNotNull(reportData)) {
			// incodeSate.setSecond_column_cur_term_amt(reportData.getAdd_up_amt().subtract(reportData.getCur_term_amt()));
			// //第一列本期金额
			// incodeSate.setSecond_column_add_up_amt(reportData.getAdd_up_amt());
			// //第一列累计金额
			// }
			Glb_income_statementDao.updateOne_odb1(incodeSate);
		}

	}
}
