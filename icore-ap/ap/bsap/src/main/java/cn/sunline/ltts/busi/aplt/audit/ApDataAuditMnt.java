package cn.sunline.ltts.busi.aplt.audit;

import cn.sunline.ltts.aplt.namedsql.ApBookDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.type.ApBook.ApAuditDetailInfo;
import cn.sunline.ltts.busi.aplt.type.ApBook.ApAuditInfo;
import cn.sunline.ltts.busi.aplt.type.ApBook.ApQueryAuditCondition;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TABLETYPE;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

public class ApDataAuditMnt {

	/**
	 * 审计详情日志查询
	 * 
	 * @param busi_audit_seq
	 * @param audit_type
	 * @return
	 */
	public static Options<ApAuditDetailInfo> queryAuditDetailLog(
			String busiAuditSeq, E_TABLETYPE auditType) {
		Options<ApAuditDetailInfo> ret = new DefaultOptions<ApAuditDetailInfo>();

		RunEnvsComm runEnvs = CommTools.prcRunEnvs();

		if (auditType == E_TABLETYPE.BUSINESS) {

			Page<ApAuditDetailInfo> auditPage = ApBookDao
					.selBusinessAuditDetail(runEnvs.getCorpno(), busiAuditSeq,
							runEnvs.getPageno(), runEnvs.getPgsize(),
							runEnvs.getCounts(), false);
			ret.setValues((auditPage.getRecords()));

		} else if (auditType == E_TABLETYPE.PARAMETER) {
			Page<ApAuditDetailInfo> auditPage = ApBookDao
					.selParameterAuditDetail(runEnvs.getCorpno(), busiAuditSeq,
							runEnvs.getPageno(), runEnvs.getPgsize(),
							runEnvs.getCounts(), false);
			ret.setValues((auditPage.getRecords()));
		}

		return ret;
	}

	/**
	 * 审计日志查询
	 * 
	 * @param queryCondition
	 * @return
	 */
	public static Options<ApAuditInfo> queryAuditLog(
			ApQueryAuditCondition queryCondition) {
		Options<ApAuditInfo> ret = new DefaultOptions<ApAuditInfo>();

		RunEnvsComm runEnvs = CommTools.prcRunEnvs();

		if (queryCondition.getAudttp() == E_TABLETYPE.BUSINESS) {

			Page<ApAuditInfo> auditPage = ApBookDao.selBusinessAudit(
					runEnvs.getCorpno(), queryCondition.getTrandt(),
					queryCondition.getTransq(), queryCondition.getPrcscd(),
					queryCondition.getTablna(), runEnvs.getPageno(),
					runEnvs.getPgsize(), runEnvs.getCounts(), false);
			ret.setValues((auditPage.getRecords()));

		} else if (queryCondition.getAudttp() == E_TABLETYPE.PARAMETER) {
			Page<ApAuditInfo> auditPage = ApBookDao.selParameterAudit(
					runEnvs.getCorpno(), queryCondition.getTrandt(),
					queryCondition.getTransq(), queryCondition.getPrcscd(),
					queryCondition.getTablna(), runEnvs.getPageno(),
					runEnvs.getPgsize(), runEnvs.getCounts(), false);
			ret.setValues((auditPage.getRecords()));
		}

		return ret;
	}

}
