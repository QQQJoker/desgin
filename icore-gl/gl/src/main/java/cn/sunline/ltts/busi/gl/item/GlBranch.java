package cn.sunline.ltts.busi.gl.item;
import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.core.exception.AdpDaoNoDataFoundException;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.fa.namedsql.FaSettleDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.gl.namedsql.GlBranchDao;
import cn.sunline.ltts.busi.gl.settlement.GlSettle;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.Gla_branch_glDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.gla_branch_gl;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlItemGL;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlSubjectBal;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlSubjectBalInfo;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlSubjectBalQueryIn;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlTranData;
import cn.sunline.ltts.busi.gl.type.GlBranch.GlVochBalData;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_BALPROP;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.sys.dict.GlDict;
/**
 * <p>
 * 文件功能说明：
 *       			
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
public class GlBranch {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlBranch.class);
	
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
	public static void prcBefore(String orgId, String trxnDate, String branchId) {

		BIZLOG.method("prcBefore begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]  trxnDate[%s] branchId[%s]", orgId, trxnDate, branchId);
		
		//检查总账凭证是否已清算
		checkSettleExistsByState(orgId, trxnDate);
		//检查总账凭证是否平衡
		checkkVoucher(orgId, trxnDate, branchId);
		// 先删除（支持重复处理）
		GlBranchDao.delBranchGl(orgId, trxnDate, branchId);
		// 生成试算数据
		genTrialData(orgId, trxnDate, branchId);

		BIZLOG.method("prcBefore end <<<<<<<<<<<<<<<<<<<<");
	}
	
	/**
	 * @Author songhao
	 *         <p>
	 *         <li>2017年12月13日-下午14:40:21</li>
	 *         <li>功能说明：生成试算数据</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 */
	public static void genTrialData(String orgId, String trxnDate, String branchId) {

		BIZLOG.method("genTrialData begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]  trxnDate[%s] branchId[%s]", orgId, trxnDate, branchId);
		
		// 生成试算数据
		GlBranchDao.genTrialData(orgId, branchId, trxnDate, DateTools.getTransTimestamp());

		BIZLOG.method("genTrialData end <<<<<<<<<<<<<<<<<<<<");
	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月6日-下午7:51:21</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 */
	private static void checkkVoucher(String orgId, String trxnDate, String branchId) {
		
		String settFlatFlag = FaTools.getSettFlatFlag();
		if (settFlatFlag==null) {
			throw GlError.GL.E0077();  //系统清算补平标志参数设置错误
		}
		// 借贷方扎差——仅返回不平衡的数据
		E_SETTSTATE settstatus ;
		if (CommUtil.compare(settFlatFlag, E_YESORNO.YES.getValue()) == 0 ) {
			settstatus = E_SETTSTATE.LIGUIDATED_AND_FLATED;
		} else {
			settstatus = E_SETTSTATE.LIGUIDATED_WAIT_FLAT;
		}
		List<GlVochBalData> lstVochBalData = GlBranchDao.lstVoucherNotBalList(orgId, trxnDate, settstatus, false);

		if (lstVochBalData.size() > 0) {
			// 币种串
			//String ccyCode = "";
			StringBuilder ccyCode = new StringBuilder();

			for (GlVochBalData cplInfo : lstVochBalData) {
				BIZLOG.parm("trxnDate[%s] ccyCode=[%s] debit_amt=[%s] credit_amt=[%s]", trxnDate, cplInfo.getCcy_code(), cplInfo.getDebit_amt(),cplInfo.getCredit_amt());
				ccyCode.append(cplInfo.getCcy_code()).append(";");
			}
			//交易日期[yyyymmdd]货币代码[]不平衡
			throw GlError.GL.E0075(trxnDate, ccyCode.toString());  
		}
	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月6日-下午7:51:25</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 */
	private static void checkSettleExistsByState(String orgId, String trxnDate) {
		BIZLOG.method("checkSettleExistsByState begin >>>>>>>>>>>>>>>>>>>>");
		
		if ( GlSettle.checkSettleExistsByState(orgId, trxnDate, E_SETTSTATE.NO_LIQUIDATION)) {
			//日期[%s]存在有未清算的总账柜员流水
			throw GlError.GL.E0076(trxnDate);  
		}			
		//Y补平,N不补平
		String settFlatFlag = FaTools.getSettFlatFlag();
		if (settFlatFlag==null) {
			throw GlError.GL.E0077();   //系统清算补平标志参数未设置
		}
		BIZLOG.parm("settFlatFlag [%s] ",settFlatFlag);
		if (CommUtil.compare(settFlatFlag, E_YESORNO.YES.getValue()) != 0 
				&& CommUtil.compare(settFlatFlag, E_YESORNO.NO.getValue()) != 0) {
			throw GlError.GL.E0078();  //系统清算补平标志参数设置错误 
		}
		// 是否补平标志为补平
		if (CommUtil.compare(settFlatFlag, E_YESORNO.YES.getValue()) == 0) {
			// 是否存在清算状态不为'已清算补平'的数据
			String count = FaSettleDao.cntSettleCountByStateNonExists(orgId, trxnDate, E_SETTSTATE.LIGUIDATED_AND_FLATED, false);

			if (Integer.parseInt(count) > 0) {
				//日期[%s]存在有非清算已补平的总账柜员流水
				throw GlError.GL.E0079(trxnDate); 
			}
		} else if (CommUtil.compare(settFlatFlag, E_YESORNO.NO.getValue()) == 0) {
			// 是否存在清算状态不为'清算待补平'的数据
			String count = FaSettleDao.cntSettleCountByStateNonExists(orgId, trxnDate, E_SETTSTATE.LIGUIDATED_WAIT_FLAT, false);

			if (Integer.parseInt(count) > 0) {
				//日期[%s]存在有非已清算待补平的总账柜员流水
				throw GlError.GL.E0080(trxnDate);  //
			}
		}

		BIZLOG.method("checkSettleExistsByState end <<<<<<<<<<<<<<<<<<<<");
	}
	
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月6日-下午7:51:34</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param branchId
	 * @param report_type
	 */
	public static void genBranchGl(String orgId, String trxnDate, String branchId, E_REPORTTYPE reportType) {

		BIZLOG.method("genBranchGl begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm("orgId [%s]  trxnDate[%s]branchId [%s]", orgId, trxnDate, branchId);

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String lastDate = runEnvs.getLstrdt();
		BIZLOG.debug("lastDate--->[%s]", lastDate);

		// 生成初始数据
		GlBranchDao.insInitData(orgId, trxnDate, branchId, lastDate , reportType);

		// 检查是否补平标志参数，如需补平，则取流水状态为4-已清算补平；否则取3-清算待补平
		//Y补平,N不补平
		String settFlatFlag = FaTools.getSettFlatFlag();
		if (settFlatFlag==null) {
			throw GlError.GL.E0077();  //系统清算补平标志参数设置错误
		}
		BIZLOG.parm("settFlatFlag [%s] ",settFlatFlag);
		if (CommUtil.compare(settFlatFlag, E_YESORNO.YES.getValue()) != 0 
				&& CommUtil.compare(settFlatFlag, E_YESORNO.NO.getValue()) != 0) {
			throw GlError.GL.E0078();   //系统清算补平标志参数设置错误 
		}
		
		E_SETTSTATE settState = E_SETTSTATE.LIGUIDATED_WAIT_FLAT;
		if (CommUtil.compare(settFlatFlag, E_YESORNO.YES.getValue()) == 0) {
			settState = E_SETTSTATE.LIGUIDATED_AND_FLATED;
		}

		List<GlTranData> lstTranData = GlBranchDao.lstTranData(orgId, trxnDate, branchId, settState,  false);
		BIZLOG.debug("transaction data lstTranData [%s]", lstTranData);
		
		int insCnt = 0, updCnt = 0;
		
		if (lstTranData.size() > 0){
			for (GlTranData cplTran : lstTranData) {
				boolean bIns = false; /* 插入标记 */
				fap_accounting_subject tabSubjectInfo = Fap_accounting_subjectDao.selectOne_odb1(cplTran.getGl_code(), true);
				gla_branch_gl tabGL = SysUtil.getInstance(gla_branch_gl.class);
				
				/* 取总账记录,取到做更新，未取到做插入 */
				try {
					bIns = false;
					tabGL = Gla_branch_glDao.selectOne_odb1(reportType, trxnDate, branchId, cplTran.getCcy_code(), cplTran.getGl_code(), true);
					
				} catch (AdpDaoNoDataFoundException e) {
	
					// 无期初、又无发生则不做任何处理
					if (CommUtil.equals(cplTran.getDebit_amt(), BigDecimal.ZERO)
							&& CommUtil.equals(cplTran.getCredit_amt(),BigDecimal.ZERO)) {
						continue;
					}
	
					tabGL.setReport_type(reportType);
					tabGL.setCorpno(orgId);
					tabGL.setTrxn_date(trxnDate);
					tabGL.setBranch_id(branchId);
					tabGL.setCcy_code(cplTran.getCcy_code());
					tabGL.setGl_code(cplTran.getGl_code());
					tabGL.setOn_bal_sheet_ind(tabSubjectInfo.getOn_bal_sheet_ind());
					tabGL.setPrev_debit_bal(BigDecimal.ZERO);
					tabGL.setPrev_credit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_debit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_credit_amt(BigDecimal.ZERO);
					tabGL.setCurrent_debit_bal(BigDecimal.ZERO);
					tabGL.setCurrent_credit_bal(BigDecimal.ZERO);
					bIns = true;
				} catch (Exception e) {
					// 读取机构总账数据失败
					throw GlError.GL.E0082( OdbFactory.getTable(gla_branch_gl.class).getLongname());
				}
				// 发生额处理
				tabGL.setCurrent_debit_amt(cplTran.getDebit_amt());
				tabGL.setCurrent_credit_amt(cplTran.getCredit_amt());

				// 余额计算
				GlSubjectBal cplBal  = calSubjectBal(tabSubjectInfo.getBal_prop(),tabGL.getPrev_debit_bal(), 
						tabGL.getPrev_credit_bal(),cplTran.getDebit_amt(), cplTran.getCredit_amt());
				// 贷方余额处理
				tabGL.setCurrent_debit_bal(cplBal.getCurrent_debit_bal());
				tabGL.setCurrent_credit_bal(cplBal.getCurrent_credit_bal());
				tabGL.setRecdver(1l);
				BIZLOG.debug("aaaaaaaaaaaaaaaaaaaaaaaaa=[%s]", tabGL);
				/* 更新数据库 */
				if (bIns) {
					Gla_branch_glDao.insert(tabGL);
					insCnt = insCnt + 1;
				} else {
					Gla_branch_glDao.updateOne_odb1(tabGL);
					updCnt = updCnt + 1;
				}
			}
		
			BIZLOG.debug("gen branch gl process: date%s branch%s occur record%s add count数据%s条, update count%s条",
					trxnDate, branchId, lstTranData.size(), insCnt, updCnt);
			// 平衡检查
			chkGlBal(orgId, reportType, trxnDate, branchId );
		
			BIZLOG.method("genBranchGl end <<<<<<<<<<<<<<<<<<<<");
		}
	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月6日-下午7:51:42</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param orgId
	 * @param reportType
	 * @param trxnDate
	 * @param branchId
	 */
	private static void chkGlBal(String orgId, E_REPORTTYPE reportType, String trxnDate,	String branchId  ) {

		BIZLOG.method("chkGlBal begin >>>>>>>>>>>>>>>>>>>>");

		// 横向平衡检查，返回不平衡的总账信息 ,前三 条
		List<GlItemGL> lstGlBalData = GlBranchDao.lstErrorBalData(orgId, reportType, trxnDate, branchId, 1, 3, false);
		
		long count = lstGlBalData.size();
		if (count > 0) {
			for (GlItemGL cplInfo : lstGlBalData) {
				BIZLOG.error("...branch[%s]ccy_code[%s]subject_no[%s]:[%][%][%s][%s][%][%s][%s]",
						cplInfo.getBranch_id(), cplInfo.getCcy_code(), cplInfo.getGl_code(), 
						cplInfo.getPrev_debit_bal(), cplInfo.getPrev_credit_bal(), cplInfo.getCurrent_debit_amt(), 
						cplInfo.getCurrent_credit_amt(), cplInfo.getCurrent_debit_bal(), cplInfo.getCurrent_credit_bal());
			}
			//不平衡总笔数[${count}]日期[${trxnDate}]货币代码[${ccyCode}]机构[${branch}]科目[${subject}]
			throw GlError.GL.E0081(count,trxnDate,lstGlBalData.get(0).getCcy_code(),lstGlBalData.get(0).getBranch_id(),lstGlBalData.get(0).getGl_code());  //
		}

		BIZLOG.method("chkGlBal end <<<<<<<<<<<<<<<<<<<<");
	}
	
	/**
	 * @Author ThinkPad
	 *         <p>
	 *         <li>2017年3月6日-下午7:51:45</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param balProp
	 * @param last_term_debit_bal
	 * @param Prev_credit_bal
	 * @param cur_term_debit_amt
	 * @param Current_credit_amt
	 * @return
	 */
	public static GlSubjectBal calSubjectBal(E_BALPROP balProp, BigDecimal lastTermDebitBal, BigDecimal prevCreditBal, BigDecimal curTermDebitAmt, BigDecimal currentCreditAmt) {

		BIZLOG.method(" calSubjectBal >>>>>>>>>>>>Begin>>>>>>>>>>>>");
		BIZLOG.parm("balProp [%s],last_term_debit_bal [%s],Prev_credit_bal [%s],cur_term_debit_amt [%s],bigBqdffshe [%s]", 
				balProp, lastTermDebitBal, prevCreditBal, curTermDebitAmt, currentCreditAmt);
		// 逻辑余额 = 上期借方 - 上期贷方 + 本期借方发生 - 本期贷方发生
		BigDecimal bigLogic = lastTermDebitBal.subtract(prevCreditBal);
		bigLogic = bigLogic.add(curTermDebitAmt);
		bigLogic = bigLogic.subtract(currentCreditAmt);

		GlSubjectBal cplBal = SysUtil.getInstance(GlSubjectBal.class);

		// 借方余额处理
		cplBal.setCurrent_debit_bal(BigDecimal.ZERO);

		switch (balProp) {
		case DEBIT:
			cplBal.setCurrent_debit_bal(bigLogic);
			break;
		case CREDIT:
			break;
		case NETTING:
			// 逻辑余额大余零
			if (CommUtil.compare(bigLogic, BigDecimal.ZERO) > 0)
				cplBal.setCurrent_debit_bal(bigLogic);
			break;
		case BOTH_SIDES:
			cplBal.setCurrent_debit_bal(bigLogic);
			break;
		default:
			break;
		}

		// 贷方余额处理
		cplBal.setCurrent_credit_bal(cplBal.getCurrent_debit_bal().subtract(bigLogic));

		BIZLOG.parm("cplBal [%s]", cplBal);
		BIZLOG.method("calSubjectBal <<<<<<<<<<<<End<<<<<<<<<<<<");
		return cplBal;
	}
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年4月6日-上午11:16:55</li>
	 *         <li>功能说明：查询指定科目余额详细信息</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static GlSubjectBalInfo getBalInfo( GlSubjectBalQueryIn queryIn ) {
		
		CommTools.fieldNotNull(queryIn.getReport_type(), GlDict.A.report_type.getId(), GlDict.A.report_type.getLongName());
		CommTools.fieldNotNull(queryIn.getTrxn_date(), GlDict.A.trxn_date.getId(), GlDict.A.trxn_date.getLongName() );
		CommTools.fieldNotNull(queryIn.getBranch_id(), GlDict.A.branch_id.getId(), GlDict.A.branch_id.getLongName());
		CommTools.fieldNotNull(queryIn.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		CommTools.fieldNotNull(queryIn.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		
		GlSubjectBalInfo info = SysUtil.getInstance(GlSubjectBalInfo.class);
		
		gla_branch_gl tableInfo = Gla_branch_glDao.selectOne_odb1(queryIn.getReport_type(), queryIn.getTrxn_date(), queryIn.getBranch_id(), queryIn.getCcy_code(), queryIn.getGl_code(), false);
		
		if( tableInfo == null ){
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(gla_branch_gl.class).getLongname(), GlDict.A.gl_code.getId(), queryIn.getGl_code());
		}
		
		info.setReport_type(tableInfo.getReport_type());  //报表类型
		info.setTrxn_date(tableInfo.getTrxn_date());  //交易日期
		info.setBranch_id(tableInfo.getBranch_id());  //机构号
		info.setCcy_code(tableInfo.getCcy_code());  //货币代码
		info.setGl_code(tableInfo.getGl_code());  //科目号
		info.setOn_bal_sheet_ind(tableInfo.getOn_bal_sheet_ind());  //表内标志
		info.setPrev_debit_bal(tableInfo.getPrev_debit_bal());  //上期借方余额
		info.setPrev_credit_bal(tableInfo.getPrev_credit_bal());  //上期贷方余额
		info.setCurrent_debit_amt(tableInfo.getCurrent_debit_amt());  //本期借方发生额
		info.setCurrent_credit_amt(tableInfo.getCurrent_credit_amt());  //本期贷方发生额
		info.setCurrent_debit_bal(tableInfo.getCurrent_debit_bal());  //本期借方余额
		info.setCurrent_credit_bal(tableInfo.getCurrent_credit_bal());  //本期贷方余额
		
		return info;

	}
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年4月6日-上午11:32:40</li>
	 *         <li>功能说明：科目余额信息列表查询</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static Options<GlSubjectBalInfo> queryBalInfo( GlSubjectBalQueryIn queryIn ) {
        BIZLOG.debug("queryOut[%s]", queryIn);
        BIZLOG.method("SrvGlBranchSubjectBal.queryBalInfo begin>>>>>>>>>>>>>>>>>>");
        
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		
		//日期为空查询当前日期
		if( CommUtil.isNull(queryIn.getTrxn_date()) )
			queryIn.setTrxn_date( runEnvs.getTrandt() );
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<GlSubjectBalInfo> page = GlBranchDao.lstSubjectBal(orgId, 
				queryIn.getReport_type(), queryIn.getTrxn_date(), queryIn.getBranch_id(),
				queryIn.getCcy_code(), queryIn.getGl_code(), queryIn.getOn_bal_sheet_ind(),
				(pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		
		Options<GlSubjectBalInfo> list = new DefaultOptions<GlSubjectBalInfo>();
		list.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

        BIZLOG.debug("queryOut[%s]", list);
        BIZLOG.method("SrvGlBranchSubjectBal.queryBalInfo end>>>>>>>>>>>>>>>>>>");
        
		return list;
		
	}

}
