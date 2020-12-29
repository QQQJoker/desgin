package cn.sunline.ltts.busi.aplt.spi;

import java.util.Map;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.engine.online.handler.OETAfterHandler;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerConstant;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerContext;
import cn.sunline.adp.cedar.service.router.drs.util.CustomDRSUtil.TargetInfo;
import cn.sunline.edsp.base.annotation.Groups;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbDrss;
import cn.sunline.ltts.busi.aplt.tools.ApltEngineContext;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.bsap.util.DaoSplitInvokeUtil;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DRSSTATUS;

@SPIMeta(id="regapbdrss")
@Order(1000)
@Groups({OETHandlerConstant.FLOW_ENGINE_TYPE,OETHandlerConstant.SERVICE_ENGINE_TYPE,OETHandlerConstant.BPL_ENGINE_TYPE})
public class OnlineProcessRegApbDrssHandler implements OETAfterHandler {
	
	public static final SysLog log = SysLogUtil.getSysLog(OnlineProcessRegApbDrssHandler.class);

	@Override
	public void handler(OETHandlerContext var1) {
		if (CommTools.isDistributedSystem()) {
			try {
				regApbDrssImmediate();
			} catch (Exception e) {
				log.debug(e.getMessage());
				regApbDrss();
			}
		}
	}
	
	private static void regApbDrssImmediate() {		
		@SuppressWarnings("unchecked")
		Map<String, String> dcn_route_info = (Map<String, String>) ApltEngineContext.getTxnTempObjMap().get("DRS_IDTFNO");
		if (CommUtil.isNotNull(dcn_route_info) && CommUtil.isNotNull(dcn_route_info.get("acctno"))) {
			TargetInfo targetDO = new TargetInfo();
			targetDO.setCorpno(dcn_route_info.get("corpno"));
			targetDO.setDcnNo(dcn_route_info.get("dcnno"));
			
			String idtfno = dcn_route_info.get("acctno");
			DcnUtil.registRouteByIDCard(dcn_route_info.get("acctno"), targetDO);
			
			registOthers("DRS_CARDNO",idtfno);
			registOthers("DRS_IDTFNO",idtfno);
			registOthers("DRS_CUSTAC",idtfno);
			registOthers("DRS_CUSTNO",idtfno);
			registOthers("DRS_CUSTID",idtfno);
			registOthers("DRS_TELENO",idtfno);
			registOthers("DRS_LNCFNO",idtfno);
		}
	}
	private static void registOthers(String drsNo, String primaryRouteKey) {
		@SuppressWarnings("unchecked")
		Map<String, String> dcn_route_info = (Map<String, String>) ApltEngineContext.getTxnTempObjMap().get(drsNo);
		if (CommUtil.isNotNull(dcn_route_info) && CommUtil.isNotNull(dcn_route_info.get("acctno"))) {
			TargetInfo targetDO = new TargetInfo();
			targetDO.setCorpno(dcn_route_info.get("corpno"));
			targetDO.setDcnNo(dcn_route_info.get("dcnno"));
			if (CommUtil.equals(drsNo, "DRS_CARDNO")) {
				DcnUtil.registRouteByCard(dcn_route_info.get("acctno"),primaryRouteKey, targetDO);
			} else if (CommUtil.equals(drsNo, "DRS_CUSTAC")) {
				DcnUtil.registRouteByAccount(dcn_route_info.get("acctno"),primaryRouteKey, targetDO);
			} else if (CommUtil.equals(drsNo, "DRS_CUSTID")) {
				DcnUtil.registRouteByECIF(dcn_route_info.get("acctno"),primaryRouteKey, targetDO);
			} else if (CommUtil.equals(drsNo, "DRS_CUSTNO")) {
				DcnUtil.registRouteByECIF(dcn_route_info.get("acctno"),primaryRouteKey, targetDO);
			} else if (CommUtil.equals(drsNo, "DRS_TELENO")) {
				DcnUtil.registRouteByMobile(dcn_route_info.get("acctno"),primaryRouteKey, targetDO);
			}
		}
	}
	
	private static void regApbDrss() {
		 genApbDrss("DRS_CUSTAC");
		 genApbDrss("DRS_CARDNO");
		 genApbDrss("DRS_CUSTNO");
		 genApbDrss("DRS_CUSTID");
		 genApbDrss("DRS_LNCFNO");
		 genApbDrss("DRS_IDTFNO");
		 genApbDrss("DRS_TELENO");

	}

	private static void genApbDrss(String drsNo) {
		@SuppressWarnings("unchecked")
		Map<String, String> dcn_route_info = (Map<String, String>) ApltEngineContext.getTxnTempObjMap().get(drsNo);

		if (CommUtil.isNotNull(dcn_route_info) && CommUtil.isNotNull(dcn_route_info.get("acctno"))) {
			log.debug("dcn_route_info:" + dcn_route_info);

			ApbDrss tblApbDrss = SysUtil.getInstance(ApbDrss.class);

			tblApbDrss.setAcctno(dcn_route_info.get("acctno"));
			tblApbDrss.setCdcnno(dcn_route_info.get("dcnno"));
			tblApbDrss.setCorpno(dcn_route_info.get("corpno"));
			tblApbDrss.setDrstyp(drsNo);
			tblApbDrss.setStatus(E_DRSSTATUS.FAIL);

			String tabnum = Math.abs(dcn_route_info.get("acctno").hashCode() % 8) + "";
			log.debug("dcn_route_info.get(acctno).hashCode()---->[%s]", tabnum);

			DaoSplitInvokeUtil.insert(tblApbDrss, tabnum);
		}
	}

}
