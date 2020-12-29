//package cn.sunline.ltts.busi.aplt.impl;
//
//import cn.sunline.ltts.busi.aplt.tools.CommTools;
//import cn.sunline.adp.cedar.base.engine.data.DataArea;
//import cn.sunline.ltts.report.engine.ReportContext;
//import cn.sunline.ltts.report.impl.DefaultReportTransactionProcessCallBack;
///**
// * 报表交易回调点
// * @author huangzl
// *
// */
//public class ReportTransactionProcessCallBackImpl extends DefaultReportTransactionProcessCallBack {
//	
//	@Override
//	public void beforeBizEnv(DataArea dataArea) {
//		super.beforeBizEnv(dataArea);
//		CommTools.setRunEnvs(dataArea);
//		ReportContext.setTrxRunEnvs(CommTools.prcRunEnvs());
//	}
//
//
//	@Override
//	public void afterBizTran() {
//		// TODO 临时让业务代码构建成功处理
//		
//	}
//
//}
