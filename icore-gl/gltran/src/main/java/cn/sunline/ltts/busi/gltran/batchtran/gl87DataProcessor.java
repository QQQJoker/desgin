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
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_GLDATEINTERVAL;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTNAME;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_CYCLETYPE;
import cn.sunline.ltts.sys.dict.GlDict;
	 /**
	  * 总账报表生成（损益明细表）
	  *
	  */

public class gl87DataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl87.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl87.Property, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo, cn.sunline.ltts.busi.gl.type.GlReport.GlCcyAndBranchList> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl87DataProcessor.class);

	/**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.gl.type.GlReport.GlCcyAndBranchList dataItem, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl87.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl87.Property property) {


			if (CommUtil.isNull(dataItem)) {
				bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
				return;
			}
				bizlog.debug("dataItem>>>>>>>>>>[%s]", dataItem);
				
				Params para = new Params();
				para.putAll(CommUtil.toMap(dataItem));
				
				para.add(GlDict.A.branch_id.getId(), dataItem.getBranch_id());
				para.add(GlDict.A.trxn_date.getId(), CommToolsAplt.prcRunEnvs().getLstrdt());
				//para.add(GlDict.A.branch_id.getId(), "99001");     // TODO
				//BizUtil.getTrxRunEnvs().setLast_date("20170101");  // TODO
				String trxnDate = CommToolsAplt.prcRunEnvs().getLstrdt();
				para.add(GlDict.A.trxn_date.getId(), trxnDate);
				
				String ccyCode = dataItem.getCcy_code();
				bizlog.debug("ccyCode>>>>>>>>>>[%s]", ccyCode);
				para.add(GlDict.A.ccy_code.getId(), ccyCode);
				
				para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.DAILY);
				para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
				para.add(GlDict.A.ccy_code.getId(), ccyCode);
				para.add(GlDict.A.file_name.getId(), E_REPORTNAME.INCOME_STATEMENT.getLongName() +"_" + ccyCode + E_GLDATEINTERVAL.DAILY.getLongName());

				GlReport.genOneReport("GlIncomeStatement", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

				
				if (DateTools2.isLastDay("M", trxnDate)) {

					para.add(GlDict.A.trxn_date.getId(), trxnDate);
					para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.MONTHLY);
					para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
					para.add(GlDict.A.ccy_code.getId(), ccyCode);
					para.add(GlDict.A.file_name.getId(), E_REPORTNAME.INCOME_STATEMENT.getLongName() +"_" + ccyCode + E_GLDATEINTERVAL.MONTHLY.getLongName());
					
					GlReport.genOneReport("GlIncomeStatement", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

				}
				
				if (DateTools2.isLastDay("Q", trxnDate)) {

					para.add(GlDict.A.trxn_date.getId(), trxnDate);
					para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.MONTHLY);
					para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
					para.add(GlDict.A.ccy_code.getId(), ccyCode);
					para.add(GlDict.A.file_name.getId(), E_REPORTNAME.INCOME_STATEMENT.getLongName() +"_" + ccyCode + E_GLDATEINTERVAL.SEASONLY.getLongName());
					
					GlReport.genOneReport("GlIncomeStatement", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

				}

				
				if (DateTools2.isLastDay("H", trxnDate)) {

					para.add(GlDict.A.trxn_date.getId(), trxnDate);
					para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.HALF_YEARLY);
					para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
					para.add(GlDict.A.ccy_code.getId(), ccyCode);
					para.add(GlDict.A.file_name.getId(), E_REPORTNAME.INCOME_STATEMENT.getLongName() +"_" + ccyCode + E_GLDATEINTERVAL.HALF_YEARLY.getLongName());
				
					GlReport.genOneReport("GlIncomeStatement", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);

				}
				if (DateTools2.isLastDay("Y", trxnDate)) {

					para.add(GlDict.A.trxn_date.getId(), trxnDate);
					para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.YEARLY);
					para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
					para.add(GlDict.A.ccy_code.getId(), ccyCode);
					para.add(GlDict.A.file_name.getId(), E_REPORTNAME.INCOME_STATEMENT.getLongName() +"_" + ccyCode + E_GLDATEINTERVAL.YEARLY.getLongName());
				
					GlReport.genOneReport("GlIncomeStatement", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);
				}
				
				if (CommToolsAplt.prcRunEnvs().getYreddt().equals(trxnDate)) {

					para.add(GlDict.A.trxn_date.getId(), trxnDate);
					para.add(GlDict.A.gl_date_interval.getId(), E_GLDATEINTERVAL.YEARLY);
					para.add(GlDict.A.report_type.getId(), E_REPORTTYPE.BUSINETT_TYPE);
					para.add(GlDict.A.ccy_code.getId(), ccyCode);
					para.add(GlDict.A.file_name.getId(),E_REPORTNAME.INCOME_STATEMENT.getLongName() +"_trial_" + ccyCode + E_GLDATEINTERVAL.YEARLY.getLongName());
					
					GlReport.genOneReport("GlIncomeStatement", dataItem.getBranch_id(), E_CYCLETYPE.DAY, para);
				}

		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo> getBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl87.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl87.Property property) {
		
			// 调用服务查询所有机构列表
			Options<IoBrchInfo> branchList = SysUtil.getInstance(IoSrvPbBranch.class).getBranchListByBrchtp(null, CommToolsAplt.prcRunEnvs().getCorpno());
			return new ListBatchDataWalker<>(branchList);
		}
		
		/**
		 * 获取作业数据遍历器
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @param dataItem 批次数据项
		 * @return
		 */
		public BatchDataWalker<cn.sunline.ltts.busi.gl.type.GlReport.GlCcyAndBranchList> getJobBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl87.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl87.Property property, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo dataItem) {
			Params para = new Params();
			para.add("org_id", CommToolsAplt.prcRunEnvs().getCorpno());
			para.add("report_type", E_REPORTTYPE.BUSINETT_TYPE);
			para.add("branch_id", dataItem.getBrchno());

			return new CursorBatchDataWalker<GlCcyAndBranchList>(GlReportDao.namedsql_lstCcyCodeListByBranchId, para);
		}
	  

}


