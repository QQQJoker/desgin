package cn.sunline.ltts.busi.gl.item;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.fa.namedsql.FaRegBookDao;
import cn.sunline.ltts.busi.fa.namedsql.FaSettleDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_scene_seq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_seq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_ledger_check_seq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_lnledger_check_result;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_lnledger_check_seq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_original_voch;
import cn.sunline.ltts.busi.gl.namedsql.GlExchangeDao;
import cn.sunline.ltts.busi.gl.namedsql.GlTrialDao;
import cn.sunline.ltts.busi.gl.type.GlComplexType.GlDayEndProcessControl;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author ThinkPad
 *         <p>
 *         <li>2017年3月6日-下午3:10:17</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年3月6日-ThinkPad：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class GlCheck {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlCheck.class);

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月6日-下午7:51:17</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 */
	public static void prcEodBefore(String orgId, boolean trialFlag) {

		BIZLOG.method("prcEodBefore begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]  trialFlag[%s] ", orgId, trialFlag);

		//ApDateInfo dateInfo = ApDate.getInfo(ApOrg.getReferenceOrgId(app_date.class));
		
		// 获取总账日期
		ApSysDateStru dateInfo = DateTools.getGlDateInfo();

		String sysDate = dateInfo.getSystdt();
		String trxnDate = CommToolsAplt.prcRunEnvs().getTrandt();

		if (!CommUtil.equals(trxnDate, sysDate))
			throw GlError.GL.E0016(trxnDate, dateInfo.getSystdt());

		// 除总账系统外的其他业务系统必须先过日终   在gl06有导入每日对账文件,核心有生成这个对账文件,就表示核心日切了.
		//List<FaSysDefineInfo> list = FaParmDao.selOtherSys(true);
		//for (FaSysDefineInfo info : list)
		//	if (CommUtil.compare(info.getSys_no(), FaConst.GL_SYSTEM) != 0 && CommUtil.compare(info.getSystem_date(), sysDate) <= 0)
		//		throw GlError.GL.E0017(info.getSys_no(), info.getSystem_date(), sysDate);

		String endDate = DateTimeUtil.lastDay(sysDate, "Y");
		if (sysDate.equals(endDate) && trialFlag) {
			// 年终决算日总账日终前必须先年终试算
			GlDayEndProcessControl dayEnd = GlTrialDao.selLastBatch("gl_yearend", CommToolsAplt.prcRunEnvs().getCorpno(), false);
			BIZLOG.debug(">>>>>>>>>>>>coredate[%s]", sysDate);
			if (CommUtil.isNotNull(dayEnd)) {
				List<String> liststatus = GlTrialDao.lstDayEndStatus(dayEnd.getTran_code(), dayEnd.getStep_id(), trxnDate, dayEnd.getTran_group_id(), CommToolsAplt.prcRunEnvs().getCorpno(), false);
				if (liststatus.size() > 0 ) {
					BIZLOG.debug(">>>>>>>>>>>>dayEndStutas[%s]", liststatus.get(0));
				}
				if (CommUtil.isNull(liststatus) || "failure".equals(liststatus.get(0))) {
					throw GlError.GL.E0018();
				}
			} else {
				throw GlError.GL.E0018();
			}
		}

		String settType = FaTools.getSettType();

		if (settType == null || ((CommUtil.compare(settType, FaConst.SETT_SINGLE_POINT) != 0) && CommUtil.compare(settType, FaConst.SETT_MULTI_LEVEL) != 0)) {
			// 系统清算方式设置错误
			throw GlError.GL.E0083(); //
		}

		if (CommUtil.compare(settType, FaConst.SETT_MULTI_LEVEL) == 0) {
			// TODO
			long countBranch = FaSettleDao.cntCountBranchSettle(CommToolsAplt.prcRunEnvs().getCorpno(), false);
			if (countBranch < 0) {
				// 机构清算关系没有设置
				throw GlError.GL.E0088();
			}
		}
		
		// 当日有原始凭证科目号为空的总笔数
		long originalVoch = FaRegBookDao.cntCountOriginalVoch(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
		if (originalVoch > 0) {
			// 日期[%s]还有未处理会计事件记录[%d]
			throw GlError.GL.E0072(trxnDate, OdbFactory.getTable(fab_original_voch.class).getLongname(), originalVoch);
		}

		// 当日有非已汇总的会计事件流水
		long accountedSeq = FaRegBookDao.cntAccountedSeq(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
		if (accountedSeq > 0) {
			// 日期[%s]还有未处理会计事件记录[%d]
			throw GlError.GL.E0072(trxnDate, OdbFactory.getTable(fab_accounting_seq.class).getLongname(), accountedSeq);
		}
		// 当日贷款计提登记总数,ABC三种类型必须都存在,否则报错
		/* 不一定存在
		long typeACount = FaRegBookDao.selCountAccrue(CommToolsAplt.prcRunEnvs().getCorpno(), E_ACCRUETYPE.DEPOSIT_INTEREST_PAYBLE, trxnDate, false);// A存款应付利息计提
		if (typeACount == 0) {
			// 日期[%s]存款应付利息计提记录不存在
			throw GlError.GL.E0072(trxnDate, E_ACCRUETYPE.DEPOSIT_INTEREST_PAYBLE.getLongName(), typeACount); 
		}
		
		long typeBCount = FaRegBookDao.selCountAccrue(CommToolsAplt.prcRunEnvs().getCorpno(), E_ACCRUETYPE.LOAN_INTEREST_RECEIVABLE, trxnDate, false);// B
		if (typeBCount == 0) {
			// 日期[%s]贷款应付利息计提记录不存
			throw GlError.GL.E0072(trxnDate, E_ACCRUETYPE.LOAN_INTEREST_RECEIVABLE.getLongName(), typeBCount);
		}
		
		long typeCCount = FaRegBookDao.selCountAccrue(CommToolsAplt.prcRunEnvs().getCorpno(), E_ACCRUETYPE.LOAN_LOST_PROVISION, trxnDate, false);// C
		if (typeCCount == 0) {
			// 日期[%s]贷款损失准备计提记录不存
			throw GlError.GL.E0101(trxnDate, E_ACCRUETYPE.LOAN_LOST_PROVISION.getLongName()); 
		}
		*/

		// 当日总分核对流水总数
		long ledgerCheckSeq = FaRegBookDao.cntCountLedgerCheckSeq(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
		if (ledgerCheckSeq > 0) {
			// 日期[%s]还有未处理总分核对流水记录[%d]
			throw GlError.GL.E0072(trxnDate, OdbFactory.getTable(fab_ledger_check_seq.class).getLongname(), ledgerCheckSeq);
		}

		long ledgerCheckResult = FaRegBookDao.cntCountLedgerCheckResult(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
		if (ledgerCheckResult > 0) {

			String ledgerCheckError = FaTools.getLedgerCheckError();
			BIZLOG.parm("ledgerCheckError [%s] ", ledgerCheckError);
			// //1报错(缺省)0不报错
			// 日期[%s]总分核对流水核对结果失败记录数[%d]
			if (ledgerCheckError == null || CommUtil.compare(ledgerCheckError, FaConst.LEDGERC_CHECK_ERROR) == 0) {
				throw GlError.GL.E0073(trxnDate, ledgerCheckResult);
			}
		}
		
		// 增加贷款相关检查
		// 当日有非已汇总的场景事件流水
		long notAccountedSceneSeq = FaRegBookDao.cntAccountedSeq(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
		if (notAccountedSceneSeq > 0) {
			// 日期[%s]还有未处理会计事件记录[%d]
			throw GlError.GL.E0072(trxnDate, OdbFactory.getTable(fab_accounting_scene_seq.class).getLongname(), accountedSeq);
		}
		
		// 当日总分核对流水总数
		long notCheckedLnLedger = FaRegBookDao.cntCountLnLedgerCheckSeq(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
		if (notCheckedLnLedger > 0) {
			// 日期[%s]还有未处理总分核对流水记录[%d]
			throw GlError.GL.E0072(trxnDate, OdbFactory.getTable(fab_lnledger_check_seq.class).getLongname(), ledgerCheckSeq);
		}
		
		// 总分核对结果
		long notCheckedLnLedgerResult = FaRegBookDao.cntCountlnLedgerCheckResult(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
		if (notCheckedLnLedgerResult > 0) {

			String ledgerCheckError = FaTools.getLedgerCheckError();
			BIZLOG.parm("ledgerCheckError [%s] ", ledgerCheckError);
			// //1报错(缺省)0不报错
			// 日期[%s]总分核对流水核对结果失败记录数[%d]
			if (ledgerCheckError == null || CommUtil.compare(ledgerCheckError, FaConst.LEDGERC_CHECK_ERROR) == 0) {
				throw GlError.GL.E0241(OdbFactory.getTable(fab_lnledger_check_result.class).getLongname(), trxnDate, notCheckedLnLedgerResult);
			}
		}
		
		
		//检查折算汇率是否已录入
		checkExchangeRate();

		BIZLOG.method("prcEodBefore end <<<<<<<<<<<<<<<<<<<<");
	}
	
	public static void checkExchangeRate() {

		BIZLOG.method("checkExchangeRate begin >>>>>>>>>>>>>>>>>>>>");

		String trxnDate = CommToolsAplt.prcRunEnvs().getTrandt();

		String exchangeRateMode = FaTools.getExchangeRateMode();
		BIZLOG.parm("exchangeRateMode [%s] ", exchangeRateMode);
		if (exchangeRateMode == null) {
			exchangeRateMode = FaConst.EXCHANGE_RATE_MODE_CORE;
		}

		// 当日折算汇率记录总数(判断是否存在 SCY,SUS两种类型的记录,存在表示已入账)
		// List<ApDropListInfo> dropList =
		// ApDropList.getItems(FaConst.EX_CCY_CODE);
		//List<ApDropListInfo> ccyList = ApDropList.getItems(FaConst.EX_CCY_CODE);
		List<KnpPara> ccyList = ApKnpPara.listKnpPara(FaConst.EX_CCY_CODE, false);
		//List<String> dropList = new ArrayList<String>();
		//dropList.add(FaConst.EX_CCY_CODE_SCY);
		//dropList.add(FaConst.EX_CCY_CODE_SUS);

		if (ccyList.size() > 0) {
			int count;
			String ccyCode;
			for (KnpPara cplInfo : ccyList) {
				ccyCode = cplInfo.getPmval1();
				if (CommUtil.compare(exchangeRateMode, FaConst.EXCHANGE_RATE_MODE_MANUAL) == 0) {
					// 手功维护
					count = GlExchangeDao.cntAllExchangeCcy(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, ccyCode, false);
				} else {
					count = GlExchangeDao.cntExchangeCurrencyCode(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, ccyCode, false);
				}
				if (count <= 0L) {
					// 折算币种[%s]没有对应的折算汇率
					throw GlError.GL.E0074(trxnDate, ccyCode); //TODO需还原
				}
			}
		}

		BIZLOG.method("checkExchangeRate end <<<<<<<<<<<<<<<<<<<<");
	}

}
