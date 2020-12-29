//package cn.sunline.ltts.busi.aplt.impl;
//
//import java.util.Map;
//
//import cn.sunline.adp.cedar.service.router.drs.util.CustomDRSUtil.TargetInfo;
//import cn.sunline.adp.cedar.base.engine.RequestData;
//import cn.sunline.adp.cedar.base.engine.ResponseData;
//import cn.sunline.adp.cedar.base.util.CommUtil;
//import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
//import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbDrss;
//import cn.sunline.ltts.busi.aplt.tools.ApltEngineContext;
//import cn.sunline.ltts.busi.aplt.tools.CommTools;
//import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
//import cn.sunline.ltts.busi.bsap.util.DaoSplitInvokeUtil;
//import cn.sunline.ltts.busi.bsap.util.ShardingUtil;
//import cn.sunline.ltts.busi.security.util.MsSecurityUtil;
//import cn.sunline.ltts.busi.sys.errors.ApError;
//import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRSSTATUS;
//import cn.sunline.adp.cedar.base.logging.SysLog;
//import cn.sunline.adp.cedar.base.logging.SysLogUtil;
//import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
//import cn.sunline.adp.cedar.base.engine.data.DataArea;
//import cn.sunline.ltts.plugin.online.api.OnlineProcessPointExtension;
//import cn.sunline.ltts.plugin.online.facade.OnlineFacade.OnlineFacadeHelper;
//
///**
// * 
// * 
// */
//public class OnlineProcessPointExtensionForBsap implements OnlineProcessPointExtension {
//	public static final SysLog log = SysLogUtil.getSysLog(OnlineProcessPointExtensionForBsap.class);
//
//	public void flowProcessBefore() {
//		String trxn_code = EngineContext.peek().getDataHelper().getTxnCd();
//		DataArea dataArea = EngineContext.peek().getReuqestDataArea();
//		MsSecurityUtil.handleInputMessage(trxn_code, dataArea);
//
//
//	}
//
//	public void flowProcessAfter() {
//		String trxn_code = EngineContext.peek().getDataHelper().getTxnCd();
//		DataArea dataArea = EngineContext.peek().getRequestContext().getRunDataArea();
//		MsSecurityUtil.handleOutputMessage(trxn_code, dataArea);
//	}
//
//	public void dbMainTransactionRollbackBefore(ResponseData responseData) {
//		// TODO Auto-generated method stub
//	}
//
//	public void dbMainTransactionRollbackAfter(ResponseData responseData) {
//		// TODO Auto-generated method stub
//	}
//
//	public void dbMainTransactionCommitBefore() {
//		try {
//			// ApJournal.saveKnbEvnts(); //批量保存冲正事件，每次提交前保存
//		} catch (Exception e) {
//			throw ApError.Aplt.E0000("批量保存冲正事件失败!", e);
//		}
//
//	}
//
//	public void dbMainTransactionCommitAfter() {
//		if (CommTools.isDistributedSystem() || ShardingUtil.isSharding()) {
//			try {
//				regApbDrssImmediate("DRS_CARDNO");
//				regApbDrssImmediate("DRS_IDTFNO");
//				regApbDrssImmediate("DRS_CUSTAC");
//				regApbDrssImmediate("DRS_CUSTNO");
//				regApbDrssImmediate("DRS_CUSTID");
//			
//			} catch (Exception e) {
//				log.debug(e.getMessage());
//				regApbDrss();
//			}
//		}
//	}
//
//	public void afterWorking(final ResponseData response) {
//		// TODO Auto-generated method stub
//	}
//
//	public void beforeWorking(OnlineFacadeHelper arg0, RequestData arg1) {
//		// TODO Auto-generated method stub
//
//	}
//
//	public void beforeWorking() {
//		// TODO Auto-generated method stub
//
//	}
//
//	private static void regApbDrss() {
//		 genApbDrss("DRS_CUSTAC");
//		 genApbDrss("DRS_CARDNO");
//		 genApbDrss("DRS_CUSTNO");
//		 genApbDrss("DRS_CUSTID");
//		 genApbDrss("DRS_LNCFNO");
//		 genApbDrss("DRS_IDTFNO");
//		 genApbDrss("DRS_TELENO");
//
//	}
//
//	private static void genApbDrss(String drsNo) {
//		@SuppressWarnings("unchecked")
//		Map<String, String> dcn_route_info = (Map<String, String>) ApltEngineContext.getTxnTempObjMap().get(drsNo);
//
//		if (CommUtil.isNotNull(dcn_route_info) && CommUtil.isNotNull(dcn_route_info.get("acctno"))) {
//			log.debug("dcn_route_info:" + dcn_route_info);
//
//			ApbDrss tblApbDrss = SysUtil.getInstance(ApbDrss.class);
//
//			tblApbDrss.setAcctno(dcn_route_info.get("acctno"));
//			tblApbDrss.setCdcnno(dcn_route_info.get("dcnno"));
//			tblApbDrss.setCorpno(dcn_route_info.get("corpno"));
//			tblApbDrss.setDrstyp(drsNo);
//			tblApbDrss.setStatus(E_DRSSTATUS.FAIL);
//
//			String tabnum = Math.abs(dcn_route_info.get("acctno").hashCode() % 8) + "";
//			log.debug("dcn_route_info.get(acctno).hashCode()---->[%s]", tabnum);
//
//			DaoSplitInvokeUtil.insert(tblApbDrss, tabnum);
//		}
//	}
//
//	private static void regApbDrssImmediate(String drsNo) {
//
//		@SuppressWarnings("unchecked")
//		Map<String, String> dcn_route_info = (Map<String, String>) ApltEngineContext.getTxnTempObjMap().get(drsNo);
//
//		if (CommUtil.isNotNull(dcn_route_info) && CommUtil.isNotNull(dcn_route_info.get("acctno"))) {
//		TargetInfo targetDO = new TargetInfo();
//		targetDO.setCorpno(dcn_route_info.get("corpno"));
//		targetDO.setDcnNo(dcn_route_info.get("dcnno"));
//
//		if (CommUtil.equals(drsNo, "DRS_IDTFNO")) {
//			String acctno[] = dcn_route_info.get("acctno").split("\\|@\\|"); //证件类型和证件号码之间用"|@|"分隔
//			DcnUtil.registRouteByIDCard(acctno[0], acctno[1], targetDO);
//		} else if (CommUtil.equals(drsNo, "DRS_CARDNO")) {
//			DcnUtil.registRouteByCard(dcn_route_info.get("acctno"), targetDO);
//		} else if (CommUtil.equals(drsNo, "DRS_CUSTAC")) {
//			DcnUtil.registRouteByAccount(dcn_route_info.get("acctno"), targetDO);
//		} else if (CommUtil.equals(drsNo, "DRS_CUSTID")) {
//			DcnUtil.registRouteByECIF(dcn_route_info.get("acctno"), targetDO);
//		}else if (CommUtil.equals(drsNo, "DRS_CUSTNO")) {
//			DcnUtil.registRouteByECIF(dcn_route_info.get("acctno"), targetDO);
//		}else if (CommUtil.equals(drsNo, "DRS_TELENO")) {
//			DcnUtil.registRouteByMobile(dcn_route_info.get("acctno"), targetDO);
//		}
//		}
//	}
//
//}
