package cn.sunline.ltts.busi.gltran.batchtran;

import cn.ltts.report.GlReport;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.gl.namedsql.GlReportDao;
import cn.sunline.ltts.busi.gl.type.GlReport.GlCcyAndBranchList;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_GLDATEINTERVAL;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTNAME;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_CYCLETYPE;
import cn.sunline.ltts.sys.dict.GlDict;

/**
 * 总账报表生成（资产负债表）
 */

public class gl85DataProcessor
		extends
		AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl85.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl85.Property, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo, cn.sunline.ltts.busi.gl.type.GlReport.GlCcyAndBranchList> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl85DataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(String jobId, int index, cn.sunline.ltts.busi.gl.type.GlReport.GlCcyAndBranchList dataItem, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl85.Input input,
			cn.sunline.ltts.busi.gltran.batchtran.intf.Gl85.Property property) {

		bizlog.method("gl85DataProcessor>>>>begin>>>>>>>>>");

		if (CommUtil.isNull(dataItem)) {
			bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
			return;
		}
		bizlog.debug("dataItem[%s]", dataItem);

		// 公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getLstrdt();

		Params para = new Params();
		para.putAll(CommUtil.toMap(dataItem));

		para.add(GlDict.A.branch_id.getId(), dataItem.getBranch_id());
		para.add(GlDict.A.trxn_date.getId(), trxnDate);
		para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.DAILY);
		para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
		para.add("ccy_code", dataItem.getCcy_code());
		para.add(GlDict.A.file_name.getId(), E_REPORTNAME.THE_BALANCE_SHEET.getLongName() +"_" + dataItem.getCcy_code() +"_" + E_CYCLETYPE.DAY.getLongName());

		bizlog.debug("reportName[%s]", E_REPORTNAME.THE_BALANCE_SHEET.getLongName());

		GlReport.genOneReport("GlAssetLiability", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

		
		// 月
		if (DateTools2.isLastDay("M", trxnDate)) {

			para.add(GlDict.A.branch_id.getId(), dataItem.getBranch_id());
			para.add(GlDict.A.trxn_date.getId(), trxnDate);
			para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.MONTHLY);
			para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
			para.add(GlDict.A.ccy_code.getId(), dataItem.getCcy_code());
			para.add(GlDict.A.file_name.getId(), E_REPORTNAME.THE_BALANCE_SHEET.getLongName() +"_" + dataItem.getCcy_code() +"_" + E_CYCLETYPE.MONTH.getLongName());

			GlReport.genOneReport("GlAssetLiability", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

		}

		// 季节
		if (DateTools2.isLastDay("Q", trxnDate)) {

			para.add(GlDict.A.branch_id.getId(), dataItem.getBranch_id());
			para.add(GlDict.A.trxn_date.getId(), trxnDate);
			para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.SEASONLY);
			para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
			para.add(GlDict.A.ccy_code.getId(), dataItem.getCcy_code());
			para.add(GlDict.A.file_name.getId(), E_REPORTNAME.THE_BALANCE_SHEET.getLongName() +"_" + dataItem.getCcy_code() +"_" + E_CYCLETYPE.QUARTER.getLongName());

			GlReport.genOneReport("GlAssetLiability", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

		}

		// 半年
		if (DateTools2.isLastDay("H", trxnDate)) {

			para.add(GlDict.A.branch_id.getId(), dataItem.getBranch_id());
			para.add(GlDict.A.trxn_date.getId(), trxnDate);
			para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.HALF_YEARLY);
			para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
			para.add(GlDict.A.ccy_code.getId(), dataItem.getCcy_code());
			para.add(GlDict.A.report_name.getId(), E_REPORTNAME.THE_BALANCE_SHEET.getLongName() +"_" + dataItem.getCcy_code() +"_" + E_CYCLETYPE.HALF_YEAR.getLongName());

			GlReport.genOneReport("GlAssetLiability", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

		}

		// 年
		if (DateTools2.isLastDay("Y", trxnDate)) {

			para.add(GlDict.A.branch_id.getId(), dataItem.getBranch_id());
			para.add(GlDict.A.trxn_date.getId(), trxnDate);
			para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.YEARLY);
			para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
			para.add(GlDict.A.ccy_code.getId(), dataItem.getCcy_code());

			para.add(GlDict.A.report_name.getId(), E_REPORTNAME.THE_BALANCE_SHEET.getLongName()+"_" + dataItem.getCcy_code() +"_" + E_CYCLETYPE.YEAR.getLongName());

			GlReport.genOneReport("GlAssetLiability", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

		}

		// 年终试算 :失算日和年结日
		if (runEnvs.getYreddt().equals(trxnDate)) {

			para.add(GlDict.A.branch_id.getId(), dataItem.getBranch_id());
			para.add(GlDict.A.trxn_date.getId(), trxnDate);
			para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.YEARLY);
			para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
			para.add(GlDict.A.ccy_code.getId(), dataItem.getCcy_code());

			para.add(GlDict.A.report_name.getId(), E_REPORTNAME.THE_BALANCE_SHEET.getLongName() +"_trial_" + dataItem.getCcy_code() +"_" + E_CYCLETYPE.DAY.getLongName());

			GlReport.genOneReport("GlAssetLiability", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

		}
		

		bizlog.method("gl85DataProcessor>>>>end>>>>>>>>>");
		//throw GlErr.GL.E0001("123");
	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo> getBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl85.Input input,
			cn.sunline.ltts.busi.gltran.batchtran.intf.Gl85.Property property) {

		 // 调用服务查询所有机构列表
		Options<IoBrchInfo> branchList = SysUtil.getInstance(IoSrvPbBranch.class).getBranchListByBrchtp(null, CommToolsAplt.prcRunEnvs().getCorpno());
		return new ListBatchDataWalker<>(branchList);
	}

	/**
	 * 获取作业数据遍历器
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @param dataItem
	 *            批次数据项
	 * @return
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.gl.type.GlReport.GlCcyAndBranchList> getJobBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl85.Input input,
			cn.sunline.ltts.busi.gltran.batchtran.intf.Gl85.Property property, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo dataItem) {

		Params para = new Params();
		para.add("org_id", CommToolsAplt.prcRunEnvs().getCorpno());
		para.add("report_type", E_REPORTTYPE.BUSINETT_TYPE);
		para.add("branch_id", dataItem.getBrchno());

		return new CursorBatchDataWalker<GlCcyAndBranchList>(GlReportDao.namedsql_lstCcyCodeListByBranchId, para);
	}

}
