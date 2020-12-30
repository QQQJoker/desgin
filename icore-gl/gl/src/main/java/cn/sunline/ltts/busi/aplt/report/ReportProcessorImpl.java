package cn.sunline.ltts.busi.aplt.report;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.ltts.report.GlReport;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.core.expression.ExpressionEvaluator;
import cn.sunline.adp.core.expression.ExpressionEvaluatorFactory;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.ReportUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_CYCLETYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaApFile;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.Glp_reportDao;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.Gls_reportDao;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glp_report;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.gls_report;
import cn.sunline.adp.cedar.busi.sdk.report.ReportProcessor;
import cn.sunline.adp.cedar.busi.sdk.component.BaseComp;
import cn.sunline.ltts.sys.dict.GlDict;

public class ReportProcessorImpl implements ReportProcessor {
	private static BizLog BIZLOG = BizLogUtil.getBizLog(ReportProcessorImpl.class);
	private final static String REPORT_COMP_ID = "AbstractComponent.FileTransfer";

	@Override
	public String process(String reportId, Map<String, Object> input, String brchno, String interval, String fileName, String chineseName) {
		if (SysUtil.getCurrentSystemType() == SystemType.onl) {
			interval = StringUtil.nullable(interval, E_CYCLETYPE.DAY.getValue());
			brchno = StringUtil.nullable(brchno, CommToolsAplt.prcRunEnvs().getTranbr());
		} else if (StringUtil.isEmpty(interval)) {
			throw GlError.GL.E0116(reportId);
		}

		glp_report report = Glp_reportDao.selectOne_odb1(reportId, E_CYCLETYPE.get(interval), true);
		String tranDate = GlReport.getReportTranDate();
		BIZLOG.debug("通过ReportTools获取报表交易日期=" + tranDate);
		String relativeFineName = getRelativeFileName(input, report, fileName);
		BaseComp.FileTransfer transfer = SysUtil.getInstance(BaseComp.FileTransfer.class, REPORT_COMP_ID);
		File localefile = getLocaleFile(input, report, transfer, brchno, relativeFineName);
		// 生成报表文件
		try {
			Boolean generated = ReportUtil.generate(reportId, localefile, input);
			if (!generated) {
				BIZLOG.error("未取到数据，不生成报表[" + reportId + "]");
				if (SysUtil.getCurrentSystemType() == SystemType.onl) {
					ApPubErr.APPUB.E0005(OdbFactory.getTable(glp_report.class).getLongname(), GlDict.A.report_id.getLongName(), reportId);

				} else {
					return null;
				}
			} else {
				BIZLOG.debug("报表绝对路径=" + localefile.getAbsolutePath() + "报表生成成功！>>>>>>>");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		// 报表文件上传

		File remotefile = getRemoteFile(input, report, transfer, tranDate, brchno, relativeFineName);
		String relativePath = tranDate + File.separatorChar + brchno + File.separatorChar + relativeFineName;

		if (SysUtil.getCurrentSystemType() == SystemType.batch) {
			relativePath = tranDate + File.separatorChar + brchno + File.separatorChar + relativeFineName;
		}
		if (report.getUpload_ind() == E_YESORNO.YES) {
			BIZLOG.debug("报表文件正在上传>>>>>now upload!");
			BIZLOG.debug("remotefile [%s]", remotefile);
			FaApFile.upload(relativePath, relativePath);
			BIZLOG.debug("报表文件上传成功>>>>now success!");
		}

		// 登记流水日志
		if (report.getRegister_log_ind() == E_YESORNO.YES) {
			registReportLog(reportId, localefile, report, input, remotefile, brchno, interval, relativeFineName, chineseName);
		}
		// TODO 报表路径
		// BizUtil.getTrxRunEnvs().setBblujing(tranDate + File.separatorChar +
		// brchno + File.separatorChar + relativeFineName);
		return localefile.getAbsolutePath();
	}

	public String process(String reportId, Map<String, Object> input, String brchno, String interval, String fileName) {
		return process(reportId, input, brchno, interval, fileName, null);
	}

	/**
	 * 
	 * @param obj
	 * @param report
	 * @param control
	 * @return
	 */
	private File getLocaleFile(Object input, glp_report control, BaseComp.FileTransfer transfer, String brchno, String relativeFineName) {

		String tranDate = GlReport.getReportTranDate();
		String localeFile = tranDate + File.separatorChar + brchno + File.separatorChar + relativeFineName;
		String localDir = transfer.workDirectory();

		File file = null;
		if (localDir.endsWith(String.valueOf(File.separatorChar))) {
			file = new File(localDir + localeFile);
		} else {
			file = new File(localDir + File.separatorChar + localeFile);
		}

		return file;
	}

	private String getRelativeFileName(Object input, glp_report report, String fileName) {
		ExpressionEvaluator ee = ExpressionEvaluatorFactory.getInstance();
		String filenamepattern = ReportUtil.getPathPattern(report.getReport_id());

		String fileSuffix = "";

		if (CommUtil.isNotNull(report.getReport_file_type())) {
			fileSuffix = report.getReport_file_type().getValue();
			if (CommUtil.equals(fileSuffix, "excel")) {
				fileSuffix = "xls";
			}
		}

		Map<String, Object> content = new HashMap<String, Object>();
		if (SysUtil.getCurrentSystemType() == SystemType.onl) {
			// TODO
			// if(CommUtil.equals("", fileSuffix)){
			// fileName = CommTools.prcRunEnvs().getXitongbs().toString() + "_"
			// + CommTools.prcRunEnvs().getJiaoyigy()
			// + "_" + CommTools.prcRunEnvs().getJiaoyisj() + "_" +
			// control.getBaobbhao();
			// }
			// else{
			// fileName = CommTools.prcRunEnvs().getXitongbs().toString() + "_"
			// + CommTools.prcRunEnvs().getJiaoyigy()
			// + "_" + CommTools.prcRunEnvs().getJiaoyisj() + "_" +
			// control.getBaobbhao() + "."+ fileSuffix;
			// }
		} else {
			if (StringUtil.isEmpty(fileName)) {
				if (StringUtil.isNotEmpty(report.getReport_no())) {
					if (CommUtil.equals("", fileSuffix)) {
						fileName = report.getReport_no();
					} else {
						fileName = report.getReport_no() + "." + fileSuffix;
					}
				} else {
					if (CommUtil.equals("", fileSuffix)) {
						fileName = report.getReport_id();
					} else {
						fileName = report.getReport_id() + "." + fileSuffix;
					}
				}
			} else {
				if (!CommUtil.equals("", fileSuffix)) {
					fileName = fileName + "." + fileSuffix;
				}
			}
		}

		String relativePath = "";
		if (StringUtil.isNotEmpty(filenamepattern)) {
			Object _relativePath = ee.eval(filenamepattern, input, content);
			BIZLOG.debug("相对路径[" + filenamepattern + "]求值为[" + _relativePath + "]，参数为[" + input + "]");
			if (StringUtil.isNotEmpty(_relativePath)) {
				relativePath = ((String) _relativePath).replace('/', File.separatorChar).replace('\\', '/');
			}
			if (StringUtil.isNotEmpty(relativePath)) {
				if (relativePath.endsWith("/")) {
					fileName = relativePath + fileName;
				} else {
					fileName = relativePath + '/' + fileName;
				}
			}
		}
		return fileName;
	}

	private void registReportLog(String reportId, File localefile, glp_report control, Map<String, Object> input, File remoteFile, String brchno, String interval, String relativeFineName,
			String chineseName) {
		gls_report logInfo = SysUtil.getInstance(gls_report.class);
		logInfo.setTrxn_date(GlReport.getReportTranDate());
		brchno = StringUtil.nullable(brchno, CommToolsAplt.prcRunEnvs().getTranbr());
		logInfo.setBranch_id(brchno);
		if (CommUtil.isNull(relativeFineName)) {
			logInfo.setReport_id(reportId);
		} else {
			logInfo.setReport_id(relativeFineName);
		}
		gls_report tblKsys_bblius = Gls_reportDao.selectOne_odb1(logInfo.getTrxn_date(), logInfo.getBranch_id(), logInfo.getReport_id(), false);
		
		
		if (CommUtil.isNotNull(tblKsys_bblius)) {
			logInfo = tblKsys_bblius;
		}

		logInfo.setRemote_file_path(remoteFile.getAbsolutePath());
		logInfo.setLocal_file_path(localefile.getAbsolutePath());

		if (CommUtil.isNull(chineseName)) {
			logInfo.setReport_base_name(ReportUtil.getReportLongName(reportId));
		} else {
			logInfo.setReport_base_name(chineseName);
		}
		String tranDate = logInfo.getTrxn_date();
		String relativePath = tranDate + "/" + brchno + "/" + relativeFineName;
		if (SysUtil.getCurrentSystemType() == SystemType.batch) {
			relativePath = tranDate + "/" + brchno + "/" + relativeFineName;// 跟上传路径保持一致
		}
		logInfo.setRelative_path(relativePath);
		logInfo.setGen_frequency(E_CYCLETYPE.get(StringUtil.nullable(interval, E_CYCLETYPE.DAY.getValue())));

		if (CommUtil.isNotNull(tblKsys_bblius)) {
			Gls_reportDao.updateOne_odb1(logInfo);
		} else {
			Gls_reportDao.insert(logInfo);
		}
		

	}

	public File getRemoteFile(Object input, glp_report control, BaseComp.FileTransfer transfer, String tranDate, String brchno, String relativeFineName) {
		File file = new File("/" + tranDate + "/" + brchno + "/" + relativeFineName);
		return file;
	}
}
