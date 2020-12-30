package cn.ltts.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.ReportUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.BatchTaskContext;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_CYCLETYPE;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.Glp_reportDao;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glp_report;

public class GlReport {
	
	public static List<String> genReport(String reportId, String brchno, Map<String, Object> input) {
		String tranDate = getReportTranDate();
		
		List<glp_report> reports = Glp_reportDao.selectAll_obd2(reportId, false);
		List<String> rpts = new ArrayList<String>();
		for (glp_report report : reports) {
			String rpt = genOneReport(reportId, brchno, report.getGen_frequency(), input,null);
			rpts.add(rpt);
		}
		//年结日特殊处理
		if (tranDate.equals(CommToolsAplt.prcRunEnvs().getYreddt())) {
			String rpt = ReportUtil.getReportById(reportId, input, brchno,  E_CYCLETYPE.DAY.getValue());
			if(StringUtil.isNotEmpty(rpt))
				rpts.add(rpt);
		}
		return rpts;
	}
	
	public static String genOneReport(String reportId, String brchno,  E_CYCLETYPE  interval, Map<String, Object> input ,String chineseName) {
		String tranDate = getReportTranDate();
		glp_report report = Glp_reportDao.selectOne_odb1(reportId, interval, true);
			String tmpDate = DateTimeUtil.lastDay(tranDate, report.getGen_frequency().getValue());
			if(tranDate.equals(tmpDate)) {
				return ReportUtil.getReportById(reportId, input, brchno, report.getGen_frequency().getValue(), chineseName);
			}
		//年结日特殊处理
		if (tranDate.equals(CommToolsAplt.prcRunEnvs().getYreddt())) {
			return ReportUtil.getReportById(reportId, input, brchno,  E_CYCLETYPE.DAY.getValue(),chineseName);
		}
		return null;
	}
	public static String genOneReport(String reportId, String brchno,  E_CYCLETYPE interval, Map<String, Object> input ) {
		String tranDate = getReportTranDate();
		glp_report report = Glp_reportDao.selectOne_odb1(reportId, interval, true);
		String tmpDate = DateTimeUtil.lastDay(tranDate, report.getGen_frequency().getValue());
		if(tranDate.equals(tmpDate)) {
			return ReportUtil.getReportById(reportId, input, brchno, report.getGen_frequency().getValue());
		}
	    //年结日特殊处理
	    if (tranDate.equals(CommToolsAplt.prcRunEnvs().getYreddt())) {
		   return ReportUtil.getReportById(reportId, input, brchno,  E_CYCLETYPE.DAY.getValue());
	    }
		return null;
	}
	
	public static String getReportTranDate() {
		String tranDate = CommToolsAplt.prcRunEnvs().getTrandt();
		//String endDate = CommToolsAplt.prcRunEnvs().getYreddt();
		if(SysUtil.getCurrentSystemType() == SystemType.batch) {

			if(CommUtil.compare("830", BatchTaskContext.get().getCurrentBatchTranGroupId()) == 0
					|| CommUtil.compare("930", BatchTaskContext.get().getCurrentBatchTranGroupId()) == 0){
				tranDate = CommToolsAplt.prcRunEnvs().getLstrdt();
			}
		}
		
		return tranDate;
	}
	
	/**
	 * @Author liuzf@sunline.cn
	 *         <p>
	 *         <li>2016年11月2日-上午9:18:57</li>
	 *         <li>功能说明：生成报表并加密</li>
	 *         </p>
	 * @param reportId
	 * @param brchno
	 * @param input
	 * @param passwd
	 * @return
	 */
	public static List<String> generateReport(String reportId, String brchno, Map<String, Object> input, String passwd) {
		ReportUtil.getContext().put(ReportUtil.USER_CIPHER, passwd);
		return genReport(reportId, brchno, input);
	}
}
