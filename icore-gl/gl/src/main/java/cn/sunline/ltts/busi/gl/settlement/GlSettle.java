package cn.sunline.ltts.busi.gl.settlement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApBuffer;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.account.FaOpenAccount;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.namedsql.FaSettleDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_branch_settlementDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_branch_settlement;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_settlementDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_settlement;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.gl.type.GlSettle.GlSettleData;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.SettlePrepData;
import cn.sunline.ltts.sys.dict.GlDict;



/**
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author ThinkPad
 *         <p>
 *         <li>2017年3月2日-下午7:07:43</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年3月2日-ThinkPad：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
/**
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author ThinkPad
 *         <p>
 *         <li>2017年3月4日-上午9:45:28</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年3月4日-ThinkPad：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class GlSettle {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(GlSettle.class);
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月4日-上午9:45:05</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 */
	public static void prcSettlement() {
		bizlog.method("prcSettlement >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		String orgId = CommToolsAplt.prcRunEnvs().getCorpno();
		
		String settType = FaTools.getSettType();
		
		if (settType==null 
				|| ((CommUtil.compare(settType, FaConst.SETT_SINGLE_POINT) != 0) 
				&& CommUtil.compare(settType, FaConst.SETT_MULTI_LEVEL) != 0))
		{
			//系统清算方式设置错误 
			throw GlError.GL.E0083();  //
		}
		
		bizlog.debug("settlement type=[%s]", settType);
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getTrandt();
		
	//	ApSeq.clearBuffer(); // 清空缓存区
		ApBuffer.clear();
		String settBatchNo = ApSeq.genSeq("SETT_BATCH_NO"); // 生成清算批次号
		bizlog.debug("gensettBatchNo=[%s]", settBatchNo);
		
		/* 数据准备 */
		List<SettlePrepData> lstPrepData = getSettlementData(trxnDate, settBatchNo, orgId);
		lstPrepData = getUnSettledList(lstPrepData);//筛选机构不平的数据 jym add
		
		if (lstPrepData.size()<=0){
			// 清算检查只考虑表内数据，如有表外数据也应该将清算状态更新处理
			bizlog.debug("batch settlement record count 0");
		} else {
			//统一生成一次柜员流水  //TODO
			if (CommUtil.compare(settType, FaConst.SETT_SINGLE_POINT) == 0 ) {
				//单点清算
				prcSinglePointSettle(settType, orgId, trxnDate, settBatchNo, lstPrepData);
			} else {
				//多级清算
				prcMultiLevelSettle(settType, orgId, trxnDate, settBatchNo, lstPrepData);
			}
		}
		
		/* 按批次修改清算状态 */
		FaSettleDao.updBatchSettled(trxnDate, orgId, settBatchNo);
		
		// 对已清算待补平的分录做补平处理 TODO 
		if (CommUtil.compare(settType, FaConst.SETT_MULTI_LEVEL) == 0 ) 
			GlSettleFlat.prcMultiSettleFlat(orgId, trxnDate, settBatchNo);
		

		bizlog.method("prcSettlement >>>>>>>>>>>>End>>>>>>>>>>>>");
	}
	
	/**
	 * 获取需要清算的机构数据
	 * @author jiangyaming
	 * 2018年5月17日 下午7:00:47
	 */
	private static List<SettlePrepData> getUnSettledList(List<SettlePrepData> lstPrepData) {
		List<SettlePrepData> unSettledList = new ArrayList<>();
		if(CommUtil.isNotNull(lstPrepData)) {
			for(SettlePrepData glPrepData : lstPrepData) {
				if(CommUtil.compare(glPrepData.getAccounting_amt(), BigDecimal.ZERO) != 0) {//存在不平的机构账
					unSettledList.add(glPrepData);
				}
			}
		}
		return unSettledList;
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月4日-上午9:45:11</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param trxnDate
	 * @param settBatchNo
	 * @param orgId
	 * @return
	 */
	private static List<SettlePrepData> getSettlementData(String trxnDate, String settBatchNo, String orgId) {
		bizlog.method("getSettlementData >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("trxnDate [%s],settBatchNo [%s]",trxnDate,settBatchNo);

		/* 收集未清算数据 */
		int rowCnt = FaSettleDao.updGatherBatchNo(settBatchNo, orgId, trxnDate);

		/* 按收集数据获得待清算数据 */
		List<SettlePrepData> lstSettlePrepData;
		if (rowCnt > 0)
			lstSettlePrepData = FaSettleDao.lstSettBatchNoList(orgId, trxnDate, settBatchNo, false);
		else
			lstSettlePrepData = new ArrayList<SettlePrepData>();
		
		bizlog.parm("lstSettlePrepData [%s]", lstSettlePrepData);

		bizlog.method("getSettlementData <<<<<<<<<<<<End<<<<<<<<<<<<");
		
		return lstSettlePrepData;
	}
	
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午11:42:50</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param settType
	 * @param orgId
	 * @param trxnDate
	 * @param settBatchNo
	 * @param lstData
	 */
	private static void prcSinglePointSettle(String settType, String orgId, String trxnDate, String settBatchNo, List<SettlePrepData> lstData ) {
		bizlog.method("prcSinglePointSettle >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		
		bizlog.parm("orgId [%s],trxnDate [%s],settBatchNo [%s]", 
				orgId,trxnDate,settBatchNo);
		bizlog.parm("lstData = [%s]", lstData);
		
		//获取借方清算科目号
		String settSubjectNoD = FaTools.getSettSubjectNoD();
		//获取贷方清算科目号
		String settSubjectNoC = FaTools.getSettSubjectNoC();
		
		if (settSubjectNoD==null || settSubjectNoC==null)
		{
			//单点清算补平科目参数没有设置
			throw GlError.GL.E0112();  
		}
		fap_accounting_subject subjectInfoD = Fap_accounting_subjectDao.selectOne_odb1(settSubjectNoD, false);
		if (subjectInfoD == null) 
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		
		fap_accounting_subject subjectInfoC = Fap_accounting_subjectDao.selectOne_odb1(settSubjectNoC, false);
		if (subjectInfoC == null) 
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		
		for (SettlePrepData cplData : lstData) {

			bizlog.debug("settle process..ccy code[%s], branch[%s], amount[%s] ", cplData.getCcy_code(), cplData.getAcct_branch(), cplData.getAccounting_amt());

			prcSettlerBranch(settType, orgId, settBatchNo, cplData, settSubjectNoD, settSubjectNoC);
		}
		bizlog.method("prcSinglePointSettle >>>>>>>>>>>>end>>>>>>>>>>>>");
		return ;
	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年4月1日-下午2:15:58</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param settType
	 * @param orgId
	 * @param settBatchNo
	 * @param cplData
	 * @param subjectNoD
	 * @param subjectNoC
	 */
	private static void prcSettlerBranch(String settType, String orgId, String settBatchNo,  SettlePrepData cplData, String subjectNoD, String subjectNoC ) {
		bizlog.method("prcSettlerBranch >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("orgId [%s],settBatchNo [%s]", orgId, settBatchNo );
		bizlog.parm("cplData [%s]", cplData);
		/* 机构下级含自身扎差金额为零 */
		if (CommUtil.compare(cplData.getAccounting_amt(), BigDecimal.ZERO) == 0){
			
			bizlog.parm("branch[%s]netting amount is zero，exis settle ", cplData.getAcct_branch());
			
			bizlog.method("prcClearBranch <<<<<<<<<<<<End<<<<<<<<<<<<");
			
			return;
		}
		
		String subjectNo = CommUtil.compare(cplData.getAccounting_amt(), BigDecimal.ZERO) > 0 ? subjectNoC : subjectNoD;
		
		// 每清算一个机构登记一个流水  //TODO
		//FaAccounting.regJiaoyils(true, cplData.getHesuanjg(), "", AcHeadwords.AcHeadWord.sWord024.getLongName());
		
		//fap_branch_settlement tabSettleBranch = Fap_branch_settlementDao.selectOne_odb1(cplData.getCcy_code(), cplData.getAcct_branch(), true);
		String tellerSeq;
		if (CommUtil.compare(settType, FaConst.SETT_MULTI_LEVEL) == 0 ) {
			fap_branch_settlement tabSettleBranch = getBranchSettleInfo(cplData.getCcy_code(), cplData.getAcct_branch());
			//记账
			tellerSeq = prcKeep(settType, cplData.getCcy_code(), cplData.getAccounting_amt(), tabSettleBranch.getGl_code(), tabSettleBranch.getUpper_lvl_gl_code(), cplData.getParent_brch_id(), cplData.getAcct_branch(), null );
		}
		else
		{
			tellerSeq = prcKeep(settType, cplData.getCcy_code(), cplData.getAccounting_amt(), subjectNo, null, cplData.getParent_brch_id(), cplData.getAcct_branch(), null );
		}

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getTrandt();

		// 登记清算登记簿
		regSettleBook(orgId, trxnDate, tellerSeq, settBatchNo, cplData, cplData.getAcct_branch());
		
		// 根据返回记账流水号，更新交易头流水的清算批次号
		FaSettleDao.updSettleBatchNo(settBatchNo, orgId, trxnDate, tellerSeq);
		
		
		bizlog.method("prcSettlerBranch <<<<<<<<<<<<End<<<<<<<<<<<<");

		return;
	}

	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午11:42:26</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param settType
	 * @param orgId
	 * @param settBatchNo
	 * @param cplData
	 */
	private static void prcSettlerBranch(String settType, String orgId, String settBatchNo,  SettlePrepData cplData ) {
		
		prcSettlerBranch(settType, orgId, settBatchNo, cplData, null, null);
		return ;
	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月4日-上午9:45:32</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param trxnSeq
	 * @param settBatchNo
	 * @param cplData
	 * @param seniorBranch
	 */
	private static void regSettleBook(String orgId, String trxnDate, String trxnSeq, String settBatchNo, SettlePrepData cplData, String seniorBranch ) {

		bizlog.method("regSettleBook >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("cplData [%s]", cplData);
		bizlog.parm("orgId [%s],trxnDate [%s],trxnSeq [%s],settBatchNo [%s],seniorBranch [%s]", 
				orgId, trxnDate, trxnSeq, settBatchNo, seniorBranch);
		
		/* 清算登记薄赋值 */
		fab_settlement tblSettlement = SysUtil.getInstance(fab_settlement.class);
		
		tblSettlement.setSett_batch_no(settBatchNo);
		tblSettlement.setTrxn_date(trxnDate);
		tblSettlement.setSett_accounting_seq(trxnSeq);
		tblSettlement.setCcy_code(cplData.getCcy_code());
		tblSettlement.setSett_branch(seniorBranch);
		tblSettlement.setAcct_branch(cplData.getAcct_branch());
		tblSettlement.setDebit_credit(CommUtil.compare(cplData.getAccounting_amt(), BigDecimal.ZERO) > 0 ? E_DEBITCREDIT.CREDIT : E_DEBITCREDIT.DEBIT);
		tblSettlement.setDebit_amt(tblSettlement.getDebit_credit() == E_DEBITCREDIT.DEBIT? BigDecimal.ZERO.subtract(cplData.getAccounting_amt()) : BigDecimal.ZERO);
		tblSettlement.setCredit_amt(tblSettlement.getDebit_credit() == E_DEBITCREDIT.CREDIT ? cplData.getAccounting_amt() : BigDecimal.ZERO);
		tblSettlement.setSett_amt(cplData.getAccounting_amt());

		/* 登记清算登记薄 */
		Fab_settlementDao.insert(tblSettlement);
		
		bizlog.method("regSettleBook <<<<<<<<<<<<End<<<<<<<<<<<<");
	}
	
	
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午11:43:10</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param settType
	 * @param orgId
	 * @param trxnDate
	 * @param settBatchNo
	 * @param lstData
	 */
	private static void prcMultiLevelSettle(String settType, String orgId, String trxnDate, String settBatchNo, List<SettlePrepData> lstData ) {
		bizlog.method("prcMultiLevelSettle >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		
		bizlog.parm("orgId [%s],trxnDate [%s],settBatchNo [%s]", 
				orgId,trxnDate,settBatchNo);
		bizlog.parm("lstData = [%s]", lstData);
		
		// 分解成层级清算数据
		GlSettleData cplSettleData = genLevelData(lstData);
		
		int count = 0;

		// 逐层清算
		if (cplSettleData.getLstlevel5().size() > 0) {
			prcLevelSettle(settType, orgId, trxnDate, cplSettleData.getLstlevel5(), 5, cplSettleData, settBatchNo);
			count++;
		}
		if (cplSettleData.getLstlevel4().size() > 0) {
			prcLevelSettle(settType, orgId, trxnDate, cplSettleData.getLstlevel4(), 4, cplSettleData, settBatchNo);
			count++;
		}
		if (cplSettleData.getLstlevel3().size() > 0) {
			prcLevelSettle(settType, orgId, trxnDate, cplSettleData.getLstlevel3(), 3, cplSettleData, settBatchNo);
			count++;
		}

		if (cplSettleData.getLstlevel2().size() > 0) {
			prcLevelSettle(settType, orgId, trxnDate, cplSettleData.getLstlevel2(), 2, cplSettleData, settBatchNo);
			count++;
		}

		if (cplSettleData.getLstlevel1().size() > 0) {
			prcLevelSettle(settType, orgId, trxnDate, cplSettleData.getLstlevel1(), 1, cplSettleData, settBatchNo);
			count++;
		}
		
		if (count > 0 && cplSettleData.getLstlevel0().size() > 0) {

			for (SettlePrepData cplData : cplSettleData.getLstlevel0()) {

				if (CommUtil.compare(cplData.getAccounting_amt(), BigDecimal.ZERO) != 0) {
					bizlog.debug("zero branch.. %s, %s", cplData.getAcct_branch(), cplData.getAccounting_amt());
					
					bizlog.method("prcMultiLevelSettle end <<<<<<<<<<<<<<<<<<<<");
					//零层机构不平衡 
					throw GlError.GL.E0085();  
				}
			}
		}
		
		bizlog.method("prcMultiLevelSettle end <<<<<<<<<<<<<<<<<<<<");

		return ;
	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月4日-上午9:45:51</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param lstPrepData
	 * @return
	 */
	private static GlSettleData genLevelData(List<SettlePrepData> lstPrepData) {
		
		bizlog.method("genLevelData >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("lstPrepData-----> [%s]", lstPrepData);

		GlSettleData cplSettleData = SysUtil.getInstance(GlSettleData.class);
		
		for (SettlePrepData cplData : lstPrepData) {
			// 清算数据
			bizlog.debug("cplData---in>>>>>>-->[%s]" , cplData);
		
			//fap_branch_settlement tabBranchSettlement = Fap_branch_settlementDao.selectOne_odb1(cplData.getCcy_code(), cplData.getAcct_branch(), true);
			fap_branch_settlement tabBranchSettlement = getBranchSettleInfo(cplData.getCcy_code(), cplData.getAcct_branch());
			if (CommUtil.isNotNull(tabBranchSettlement.getParent_brch_id()))
				cplData.setParent_brch_id(tabBranchSettlement.getParent_brch_id());
			
			if (tabBranchSettlement.getBusi_relation_level() == 5)
				cplSettleData.getLstlevel5().add(cplData);
			else if (tabBranchSettlement.getBusi_relation_level() == 4)
				cplSettleData.getLstlevel4().add(cplData);
			else if (tabBranchSettlement.getBusi_relation_level() == 3)
				cplSettleData.getLstlevel3().add(cplData);
			else if (tabBranchSettlement.getBusi_relation_level() == 2)
				cplSettleData.getLstlevel2().add(cplData);
			else if (tabBranchSettlement.getBusi_relation_level() == 1)
				cplSettleData.getLstlevel1().add(cplData);
			else if (tabBranchSettlement.getBusi_relation_level() == 0)
				cplSettleData.getLstlevel0().add(cplData);
			
		}

		bizlog.parm("genLevelData out<<<<< [%s]", cplSettleData);
		bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
		
		return cplSettleData;

	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午11:41:26</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param ccyCode
	 * @param branchId
	 * @return
	 */
	private static fap_branch_settlement getBranchSettleInfo(String ccyCode, String branchId) {
		
		fap_branch_settlement tabBranchSettle = Fap_branch_settlementDao.selectOne_odb1(ccyCode, branchId, false);
		if (CommUtil.isNull(tabBranchSettle))
			tabBranchSettle = Fap_branch_settlementDao.selectOne_odb1(ApConstants.WILDCARD, branchId, false);
		
		
		return tabBranchSettle;
	}
	
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午11:43:24</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param settType
	 * @param orgId
	 * @param trxnDate
	 * @param lstData
	 * @param level
	 * @param cplSettleData
	 * @param settBatchNo
	 */
	private static void prcLevelSettle(String settType, String orgId, String trxnDate, List<SettlePrepData> lstData, long level, GlSettleData cplSettleData, String settBatchNo) {
		bizlog.method("prcLevelSettle >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		
		bizlog.parm("sFarendma [%s],sJiaoyirq [%s],lLevel [%s],sBatchNo [%s]", 
				orgId,trxnDate,String.valueOf(level),settBatchNo);
		bizlog.parm("lstData  [%s]", lstData);
		
		bizlog.parm("cplSettleData [%s]", cplSettleData);
		
		for (SettlePrepData cplData : lstData) {

			bizlog.debug("settle process..lLevel:[%s] ccy code[%s], branch[%s], amount[%s] ", level, cplData.getCcy_code(), cplData.getAcct_branch(), cplData.getAccounting_amt());

			prcSettlerBranch(settType, orgId, settBatchNo, cplData);
			String upBranch = cplData.getParent_brch_id();
			if (CommUtil.isNotNull(upBranch)) {
				if (level == 5) {
					addNewBranch(upBranch, cplData.getCcy_code(), cplData.getAccounting_amt(), cplSettleData.getLstlevel4());
				}
				else if (level == 4) {
					addNewBranch(upBranch, cplData.getCcy_code(), cplData.getAccounting_amt(), cplSettleData.getLstlevel3());
				}
				else if (level == 3) {
					addNewBranch(upBranch, cplData.getCcy_code(), cplData.getAccounting_amt(), cplSettleData.getLstlevel2());
				}
				else if (level == 2) {
					addNewBranch(upBranch, cplData.getCcy_code(), cplData.getAccounting_amt(), cplSettleData.getLstlevel1());
				}
				else if (level == 1) {
					addNewBranch(upBranch, cplData.getCcy_code(), cplData.getAccounting_amt(), cplSettleData.getLstlevel0());
				}
				else {
					;
				}
			}
		}

		bizlog.method("prcLevelSettle <<<<<<<<<<<<End<<<<<<<<<<<<");
	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月4日-上午9:46:02</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param acctBranch
	 * @param ccyCode
	 * @param amt
	 * @param lstPrepData
	 */
	private static void addNewBranch(String acctBranch, String ccyCode, BigDecimal amt, List<SettlePrepData> lstPrepData) {
		
		bizlog.method("addNewBranch >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("acctBranch [%s],ccyCode [%s],BigDecimal [%s]", acctBranch,ccyCode,amt);
		bizlog.parm("lstPrepData [%s]", lstPrepData);

		int flag = 0;

		for (int i = 0; i < lstPrepData.size(); i++) {

			SettlePrepData cplOldBr = lstPrepData.get(i);
			
			bizlog.debug("cplOldBr--addNewBranch-> [%s]", cplOldBr);
			
			if (cplOldBr.getAcct_branch().equals(acctBranch) && cplOldBr.getCcy_code().equals(ccyCode)) {

				cplOldBr.setAccounting_amt(cplOldBr.getAccounting_amt().add(amt));

				lstPrepData.set(i, cplOldBr);

				flag++;
			}
		}

		if (flag == 0) {

			SettlePrepData cplNewBr = SysUtil.getInstance(SettlePrepData.class);

			cplNewBr.setAcct_branch(acctBranch);
			cplNewBr.setCcy_code(ccyCode);
			cplNewBr.setAccounting_amt(amt);

			lstPrepData.add(cplNewBr);
		}

		bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
	}
	
	
	public static boolean checkSettleExistsByState(String orgId, String trxnDate, E_SETTSTATE settState){
		bizlog.method("checkSettleExistsByState begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("sJiaoyirq [%s],eQingshzt [%s]", orgId, trxnDate, settState);
		 
		boolean flag = false;
		
		String count = FaSettleDao.cntSettleCountByState( orgId, trxnDate, settState, false);
		
		if(Integer.parseInt(count)>0)
			flag = true;
		
		bizlog.method("checkSettleExistsByState end <<<<<<<<<<<<<<<<<<<<");
		return flag;
	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月18日-上午11:43:36</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param settType
	 * @param ccyCode
	 * @param amt
	 * @param subjectNo
	 * @param seniorSubjectNo
	 * @param seniorBrchId
	 * @param branchId
	 * @param tellerSeq
	 * @return
	 */
	private static String prcKeep(String settType, String ccyCode, BigDecimal amt, String subjectNo, String seniorSubjectNo, String seniorBrchId, String branchId, String tellerSeq ) {

		bizlog.method("prcKeep >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("ccyCode [%s],subjectNo [%s],seniorSubjectNo [%s]", ccyCode, subjectNo, seniorSubjectNo);
				
		// 登记柜员流水 
	    if (CommUtil.isNull(tellerSeq)) {
			tellerSeq = FaAccounting.getTellerSeq();
			FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
			regTellerSeq.setSys_no(FaConst.GL_SYSTEM); // 系统编号
			regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
			regTellerSeq.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
			regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
			regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
			regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水
	
			FaAccounting.regTellerSeq(regTellerSeq);
	    }
	        
		
	    FaAcctInfo acctInfo ;
	    // 当前机构处理 
		E_DEBITCREDIT direction = CommUtil.compare(amt, BigDecimal.ZERO) > 0 ? E_DEBITCREDIT.CREDIT : E_DEBITCREDIT.DEBIT;
		
		// 取出账号信息
		acctInfo = FaOpenAccount.getAcctBySubject(FaConst.GL_SYSTEM, ccyCode, branchId, subjectNo);
		bizlog.debug("acctInfo[%s]", acctInfo);
		String acctNo =acctInfo.getAcct_no(); // 本级账号
		
	    if (CommUtil.compare(settType, FaConst.SETT_MULTI_LEVEL) == 0 ) {
	    	List<FaSingleAccountingCheckIn> accountingDoInList = new ArrayList<FaSingleAccountingCheckIn>();
			
			// 本级机构
		    FaAccounting.getAccountingCheckByAcct(accountingDoInList ,acctNo, direction, amt.abs(), "", "branch settle");  //TODO 摘要码没写
		    
	    	//多点清算时,记上级机构
	    	direction = CommUtil.compare(amt, BigDecimal.ZERO) > 0 ? E_DEBITCREDIT.DEBIT : E_DEBITCREDIT.CREDIT;
	    	acctInfo = FaOpenAccount.getAcctBySubject(FaConst.GL_SYSTEM, ccyCode, seniorBrchId, seniorSubjectNo);
			bizlog.debug("acctInfo[%s]", acctInfo);
			String seniorAcctNo =acctInfo.getAcct_no(); // ;
			FaAccounting.getAccountingCheckByAcct(accountingDoInList, seniorAcctNo,direction, amt.abs(), "", "branch settle");  //TODO 摘要码没写  //
			FaAccounting.bookMultiAccounting(accountingDoInList, tellerSeq, 2l, FaConst.GL_SYSTEM, "", true);
		}
	    else {
	    	//单点清算时只记一记账
	    	FaSingleAccountingCheckIn accountingInfo = SysUtil.getInstance(FaSingleAccountingCheckIn.class);
	    	accountingInfo.setAcct_no(acctNo); // 账号
	    	accountingInfo.setDebit_credit(direction); // 记账方向
	    	accountingInfo.setAccounting_amt(amt.abs()); // 记账金额
	    	accountingInfo.setSummary_code(""); // 摘要代码TODO
	    	accountingInfo.setRemark("branch settle"); // 备注
	    	FaAccounting.singleAccounting(accountingInfo, tellerSeq, 1L);
	    }
	    	
		
		bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
		return tellerSeq;
	}

}
