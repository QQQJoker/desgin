package cn.ltts.report;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.busi.sdk.report.LttsReportQueryExecuter;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApCurrency;
import cn.sunline.ltts.busi.gl.namedsql.GlReportDao;
import cn.sunline.ltts.busi.gl.type.GlReport.GlBuinessCondition;
import cn.sunline.ltts.busi.gl.type.GlReport.GlTiller;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;

/**
 * business condition based on balance sheet 业务、审计、合并均用此报表展示
 */
public class GlInsideGenlReportProcessor
		implements
		cn.sunline.adp.cedar.busi.sdk.report.LttsReportDataProcessor<cn.sunline.ltts.busi.gl.type.GlReport.GlBuinessCondition, cn.sunline.ltts.busi.gl.report.GlInsideGenl.Input, cn.sunline.ltts.busi.gl.type.GlReport.GlTiller> {

	@Override
	public LttsReportQueryExecuter<cn.sunline.ltts.busi.gl.type.GlReport.GlBuinessCondition> getMainDataQueryExecuter(cn.sunline.ltts.busi.gl.report.GlInsideGenl.Input input) {

		Params para = new Params();
		para.add("org_id", CommToolsAplt.prcRunEnvs().getCorpno());
		para.add("branch_id", input.getBranch_id());
		para.add("ccy_code", input.getCcy_code());
		para.add("trxn_date", input.getTrxn_date());
		para.add("gl_date_interval", input.getGl_date_interval());
		para.add("on_bal_sheet_ind", E_YESORNO.YES);
		
		return new LttsReportQueryExecuter<GlBuinessCondition>(GlReportDao.namedsql_lstInsideGenl, para);
	}

	@Override
	public boolean processSingleData(int index, cn.sunline.ltts.busi.gl.type.GlReport.GlBuinessCondition item) {
		return true;
	}

	@Override
	public cn.sunline.ltts.busi.gl.type.GlReport.GlTiller getAssistentData(cn.sunline.ltts.busi.gl.report.GlInsideGenl.Input input) {
		// 实现辅助数据获取

		GlTiller tittle = SysUtil.getInstance(GlTiller.class);
		// 获取货币名称
		String ccyCode = input.getCcy_code();
		//ApCurrencyInfo ccyInfo = ApCurrency.getItem(ccyCode, true);
		ApCurrency ccyInfo = CommTools.getApCurrency(ccyCode);
		if (ccyInfo != null) {
			tittle.setCcy_name(ccyInfo.getCrcyna()); // currency name
		}
		else {
			tittle.setCcy_name(ccyCode);
		}
		// 获取机构名称
		String branchId = input.getBranch_id();
		//ApBranchInfo branchInf = ApBranch.getItem(branchId);
		tittle.setBranch_name(SysUtil.getInstance(IoSrvPbBranch.class).getBranch(branchId).getBrchna()); // branch name

		tittle.setTrxn_date(input.getTrxn_date());
	
		return tittle;
	}

}
