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
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_GLDATEINTERVAL;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ITEMDATATYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTNAME;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.Glb_profit_sheetDao;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glb_profit_sheet;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glp_report_define;

public class GlProftLoss {

    /**
     * @Author Administrator
     *         <p>
     *         <li>2017年3月17日-下午2:55:03</li>
     *         <li>功能说明：利润表</li>
     *         </p>
     */
    public static void genPriftMain(String orgId, String trxnDate, E_REPORTTYPE reportType, String branchId) {
        // 删除利润表
        GlReportDao.delProfit(orgId, reportType, trxnDate, branchId);
        // 生成日报
        genReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.DAILY);
        // 月报
        if (DateTools2.isLastDay("M", trxnDate)) {
            genReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.MONTHLY);
        }
        // 季报
        if (DateTools2.isLastDay("Q", trxnDate)) {
            genReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.SEASONLY);
        }
        // 半年报
        if (DateTools2.isLastDay("H", trxnDate)) {
            genReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.HALF_YEARLY);
        }
        // 年报
        if (DateTools2.isLastDay("Y", trxnDate)) {
            genReport(reportType, trxnDate, branchId, orgId, E_GLDATEINTERVAL.YEARLY);
        }
    }

    public static void genReport(E_REPORTTYPE reportType, String trxnDate, String branchId, String orgId, E_GLDATEINTERVAL dataInterval) {

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
        List<KnpPara> exCcyList = ApKnpPara.listKnpPara(FaConst.EX_CCY_CODE,false);
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
                List<glp_report_define> reportDefines = GlReportDao.lstReprotElement(orgId, E_REPORTNAME.PROFIT_SHEET, false);

                for (glp_report_define reportDefine : reportDefines) {

                    // 打印显示号不为0

                    genReport(reportDefine, dataInterval, trxnDate, reportType, orgId, ccyCode, branchId);

                }
            }

        }

    }

    public static void genReport(glp_report_define reportDefine, E_GLDATEINTERVAL dataInterval, String trxnDate, E_REPORTTYPE reportType, String orgId, String ccyCode,
            String branchId) {

        // 根据总账区间获取起始日期
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

            reportData = GlReportDao.lstReportData(reportType, E_REPORTNAME.PROFIT_SHEET, branchId, ccyCode, baseDataInterval, reportDefine.getReport_item_no(), startDate,
                    trxnDate, orgId, false);
            // 取到空值，赋值0
            if (CommUtil.isNull(reportData)) {
                reportData = SysUtil.getInstance(GlReportData.class);
                reportData.setAdd_up_amt(BigDecimal.ZERO);
                reportData.setCur_term_amt(BigDecimal.ZERO);
            }
        }

        glb_profit_sheet profitSheet = SysUtil.getInstance(glb_profit_sheet.class);

        // 第一列数据
        if (reportDefine.getReport_item_column() == 1) {
            //bizlog.debug("reportType[%s]", reportType);
            profitSheet.setReport_type(reportType); // 报表类型
            profitSheet.setTrxn_date(trxnDate); // 交易日期
            profitSheet.setBranch_id(branchId); // 机构号
            profitSheet.setGl_date_interval(dataInterval); // 总账区间
            profitSheet.setCcy_code(ccyCode); // 货币代码
            profitSheet.setReport_item_line(reportDefine.getReport_item_line()); // 报表项次归属行
            profitSheet.setReport_item_no(reportDefine.getReport_item_no()); // 报表项次编号
            profitSheet.setItem_data_name(GlReports.prcFillCHSpace(reportDefine.getItem_data_name(), reportDefine.getFront_fill_space_count())); // 报表项次名称
            profitSheet.setPrint_display_no(reportDefine.getPrint_display_no()); // 打印显示号
            profitSheet.setCur_term_amt(reportData.getCur_term_amt()); // 本期金额
            profitSheet.setAdd_up_amt(reportData.getAdd_up_amt()); // 累计金额
            //if (CommUtil.isNotNull(reportData)) {
            //	profitSheet.setCur_term_amt(reportData.getAdd_up_amt().subtract(reportData.getCur_term_amt())); // 本期金额
            //	profitSheet.setAdd_up_amt(reportData.getAdd_up_amt()); // 累计金额
            //}

            Glb_profit_sheetDao.insert(profitSheet);

        } else {
            throw GlError.GL.E0061();
        }

    }
}
