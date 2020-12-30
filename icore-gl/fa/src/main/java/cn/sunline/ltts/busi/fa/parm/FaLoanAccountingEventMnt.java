package cn.sunline.ltts.busi.fa.parm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaLoanAccountingDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_event_parm;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_prod_ruleDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_rule;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingTxsInfo;
import cn.sunline.ltts.busi.fa.type.ComFaLoanAccounting.FaLoanAccountingEventInfo;
import cn.sunline.ltts.busi.fa.type.ComFaLoanAccounting.FaLoanAccountingEventResult;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CRCYCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_RED_REVERSE_FLAG;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaLoanAccountingEventMnt {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaLoanAccountingEventMnt.class);
	
	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月15日-上午10:56:17</li>
	 *         <li>功能说明：贷款会计核算事件解析调试</li>
	 *         </p>
	 * @param analysisIn
	 * @return
	 */
	public static Options<FaLoanAccountingEventResult> checkAnalysisLoanEvent(FaLoanAccountingEventInfo analysisIn) {
		
		bizlog.method(" FaLoanAccountingEventMnt.checkAnalysisLoanEvent begin >>>>>>>>>>>>>>>>");
		
		// 数据非空校验
		FaLoanAccountingEvent.checkNull(analysisIn);
		
		// 数据合法性校验
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		FaLoanAccountingEvent.existsFapSysDefine(analysisIn.getSys_no());
		
		// 根据产品编号、交易编码查询分录配置
		List<fap_accounting_prod_rule> ruleList = FaLoanAccountingDao.lstFapAccountingProdRule(runEnvs.getCorpno(), 
				analysisIn.getSys_no(), analysisIn.getProduct_code(), analysisIn.getEvent_code(), analysisIn.getAccount_status(), 
				analysisIn.getTrxn_date(), analysisIn.getTrxn_date(), false);
		if(ruleList.isEmpty()) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(fap_accounting_prod_rule.class).getLongname(),
					GlDict.A.sys_no.getId()	, runEnvs.getSystcd(),
					GlDict.A.product_code.getId(), analysisIn.getProduct_code(), 
					GlDict.A.event_code.getId(), analysisIn.getEvent_code());
		}
		
		bizlog.debug("ruleList[%s]", ruleList);
		
		// 解析会计分录
		ArrayList<FaLoanAccountingEventResult> resultList = new ArrayList<FaLoanAccountingEventResult>();
		// BigDecimal debitAmount = BigDecimal.ZERO;		// 借方发生额
		// BigDecimal creditAmount = BigDecimal.ZERO;		// 贷方发生额
		
		// 定义增值税事件
		E_YES___ taxFlag = E_YES___.NO;
		String taxEventCode = "";
		String taxCode = "";
		
		for (fap_accounting_prod_rule rule : ruleList) {
			
			// 基础解析
			ArrayList<FaLoanAccountingEventResult> tranList = analysisLoanRule(analysisIn.getCurrency_code(), analysisIn.getTran_amount(), rule);
			resultList.addAll(tranList);
			
			// 税解析要素准备
			if(E_YES___.YES == rule.getTax_separate_flag()) {
				taxFlag = E_YES___.YES;
				taxEventCode = rule.getTax_event_code();
				taxCode = rule.getTax_code();
			}
			
		}
		
		// 税解析
		if(E_YES___.YES == taxFlag){
			
			// 根据产品编号、交易编码查询税分录配置
			List<fap_accounting_prod_rule> taxRuleList = FaLoanAccountingDao.lstFapAccountingProdRule(runEnvs.getCorpno(), 
					analysisIn.getSys_no(), analysisIn.getProduct_code(), taxEventCode, analysisIn.getAccount_status(), 
					analysisIn.getTrxn_date(), analysisIn.getTrxn_date(), false);
			if(taxRuleList.isEmpty()) {
				throw ApPubErr.APPUB.E0025(OdbFactory.getTable(fap_accounting_prod_rule.class).getLongname(),
						GlDict.A.sys_no.getId()	, runEnvs.getSystcd(),
						GlDict.A.product_code.getId(), analysisIn.getProduct_code(), 
						GlDict.A.event_code.getId(), taxEventCode);
			}
			
			// 取出税事件信息
			// 计算税金
			FaLnAccountingTxsInfo txsInfo = FaLoanAccountingEventTax.calTaxAmount(analysisIn.getTran_amount(),
					taxCode, analysisIn.getTrxn_date(), analysisIn.getTrxn_date(), analysisIn.getCurrency_code().toString());
			BigDecimal taxAmount = txsInfo.getTax_amount();
			bizlog.debug("taxAmount[%s]", taxAmount);
			
			for (fap_accounting_prod_rule taxRule : taxRuleList) {
				
				ArrayList<FaLoanAccountingEventResult> taxList = analysisLoanRule(analysisIn.getCurrency_code(), taxAmount, taxRule);
				resultList.addAll(taxList);
				
			}
			
		}
		
		// TODO 交易平衡检查
		/*bizlog.debug("debitAmount[%s],creditAmount[%s]", debitAmount, creditAmount);
		if(CommUtil.compare(debitAmount, creditAmount) != 0) {
			throw GlError.GL.E0052();
		}*/
		
		// 返回结果
		bizlog.method(" FaLoanAccountingEventMnt.checkAnalysisLoanEvent end <<<<<<<<<<<<<<<<");
		
		return new DefaultOptions<>(resultList);
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月15日-下午5:44:49</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param currency_code		币种
	 * @param tran_amount	事件交易金额
	 * @param rule	贷款核算规则定义
	 * @param direction 
	 * @return
	 */
	private static FaLoanAccountingEventResult assemBaseSubjectTranInfo(E_CRCYCD currency_code, BigDecimal tran_amount,
			fap_accounting_prod_rule rule, E_DEBITCREDIT direction) {
		
		bizlog.method(" FaLoanAccountingEventMnt.assemSubjectTranDetail begin >>>>>>>>>>>>>>>>");
		
		// 科目名称
		String gl_code = E_DEBITCREDIT.DEBIT == direction ? rule.getDebit_subject() : rule.getCredit_subject();
		fap_accounting_subject subject = Fap_accounting_subjectDao.selectOne_odb1(gl_code, true);
		
		FaLoanAccountingEventResult result = SysUtil.getInstance(FaLoanAccountingEventResult.class);
		result.setTran_amount(E_RED_REVERSE_FLAG.YES == rule.getRed_reverse_flag() ? tran_amount.negate() : tran_amount);	 // 交易金额
		result.setDebit_credit(direction);		// 借贷方向
		result.setGl_code(gl_code);	// 科目号
		result.setGl_code_desc(subject.getGl_code_desc());	// 科目名称
		result.setTrxn_ccy(currency_code.toString());		// 币种
		
		bizlog.method(" FaLoanAccountingEventMnt.assemSubjectTranDetail end <<<<<<<<<<<<<<<<");
		
		return result;
		
	}
	
	/**
	 * 
	 * @Author L
	 *         <p>
	 *         <li>2020年9月15日-下午7:23:44</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 */
	public static ArrayList<FaLoanAccountingEventResult> analysisLoanRule(E_CRCYCD currency_code, 
			BigDecimal tran_amount, fap_accounting_prod_rule rule) {

		// 定义
		ArrayList<FaLoanAccountingEventResult> resultList = new ArrayList<FaLoanAccountingEventResult>();
		
		// 借方科目
		if(CommUtil.isNotNull(rule.getDebit_subject())) {
			FaLoanAccountingEventResult result = assemBaseSubjectTranInfo(currency_code, tran_amount, rule, E_DEBITCREDIT.DEBIT);
			resultList.add(result);
		}
		
		// 贷方科目
		if(CommUtil.isNotNull(rule.getCredit_subject())) {
			FaLoanAccountingEventResult result = assemBaseSubjectTranInfo(currency_code, tran_amount, rule, E_DEBITCREDIT.CREDIT);
			resultList.add(result);
		}
		
		return resultList;
		
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月16日-上午11:21:27</li>
	 *         <li>功能说明：解析所有产品事件</li>
	 *         </p>
	 * @param analysisIn
	 * @return
	 */
	public static Options<FaLoanAccountingEventResult> checkAnalysisLoanEvents(
			Options<FaLoanAccountingEventInfo> analysisIn) {
		bizlog.method(" FaLoanAccountingEventMnt.checkAnalysisLoanEvents begin >>>>>>>>>>>>>>>>");
		
		// 非空校验
		if(analysisIn.isEmpty()) {
			throw GlError.GL.E0218();
		}
		
		// 遍历逐个解析
		ArrayList<FaLoanAccountingEventResult> resultList = new ArrayList<FaLoanAccountingEventResult>();
		for (FaLoanAccountingEventInfo info : analysisIn) {
			Options<FaLoanAccountingEventResult> result = checkAnalysisLoanEvent(info);
			resultList.addAll(result);
		}
		
		bizlog.method(" FaLoanAccountingEventMnt.checkAnalysisLoanEvents end <<<<<<<<<<<<<<<<");
		return new DefaultOptions<>(resultList);
		
	}
	
	/**
	 * 
	 * @Author rongjie
	 *         <p>
	 *         <li>2020年9月16日-下午3:04:29</li>
	 *         <li>功能说明：产品核算事件建立</li>
	 *         </p>
	 * @param eventInfo
	 */
	public static void addLoanAccountingEvent(Options<fap_accounting_prod_rule> addeventInfo) {
		bizlog.method(" FaLoanAccountingEventMnt.addLoanAccountingEvent begin >>>>>>>>>>>>>>>>");

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		
		for(fap_accounting_prod_rule event : addeventInfo) {
			
			//非空字段判断
			CommTools.fieldNotNull(event.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
			CommTools.fieldNotNull(event.getProduct_code(), GlDict.A.product_code.getId(), GlDict.A.product_code.getLongName());
			CommTools.fieldNotNull(event.getEvent_code(), GlDict.A.event_code.getId(), GlDict.A.event_code.getLongName());
			CommTools.fieldNotNull(event.getRed_reverse_flag(), GlDict.A.red_reverse_flag.getId(), GlDict.A.red_reverse_flag.getLongName());
			CommTools.fieldNotNull(event.getTax_separate_flag(), GlDict.A.tax_separate_flag.getId(), GlDict.A.tax_separate_flag.getLongName());
			
			//唯一合法性检查
			fap_accounting_prod_rule fap_accounting_prod_rule_info = Fap_accounting_prod_ruleDao.selectOne_odb2(event.getSys_no(), event.getProduct_code(), event.getEvent_code(), event.getData_sort(), event.getEffect_date(), false);

			if(CommUtil.isNotNull(fap_accounting_prod_rule_info)) {
				throw ApPubErr.APPUB.E0019(OdbFactory.getTable(fap_accounting_prod_rule.class).getLongname(), event.getSys_no()+" "+event.getProduct_code()+" "+event.getEvent_code()+" "+event.getData_sort()+" "+event.getEffect_date());
			}
			
			//借方科目合法性校验
			if(CommUtil.isNotNull(event.getDebit_subject())) {
				if (!FaAccountingSubject.checkSubjectExists(event.getDebit_subject())) {
					throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.error_gl_code.getId(), event.getDebit_subject());
				}
			}
			//贷方科目合法性校验
			if(CommUtil.isNotNull(event.getCredit_subject())) {
				if (!FaAccountingSubject.checkSubjectExists(event.getCredit_subject())) {
					throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.error_gl_code.getId(), event.getCredit_subject());
				}
			}
			
			
			fap_accounting_prod_rule info = SysUtil.getInstance(fap_accounting_prod_rule.class);
			
			info.setSys_no(event.getSys_no());  //系统编号
			info.setProduct_code(event.getProduct_code());  //产品编号
			info.setProduct_name(event.getProduct_name());  //产品名称
			info.setEvent_code(event.getEvent_code());  //交易编码
			info.setEvent_name(event.getEvent_name());  //交易名称
			info.setData_sort(event.getData_sort());//数据序号
			info.setAccount_status(event.getAccount_status());  //贷款账户状态
			info.setDebit_subject(event.getDebit_subject());  //借方科目
			info.setCredit_subject(event.getCredit_subject());  //贷方科目
			info.setRed_reverse_flag(event.getRed_reverse_flag());  //红字标识
			info.setTax_separate_flag(event.getTax_separate_flag());  //增值税分离标识
			info.setTax_event_code(event.getTax_event_code());// 增值税交易编码
			info.setTax_code(event.getTax_code());  //税码
			info.setEffect_date(event.getEffect_date());  //生效日期
			info.setInvalid_date(event.getInvalid_date());  //失效日期
			info.setTran_user(runEnvs.getTranus());  //新增柜员
			info.setAuth_user(runEnvs.getCkbsus());  //新增授权柜员(串)
			info.setInit_date(runEnvs.getTrandt());  //初始化日期
			info.setSpare_column_one(event.getSpare_column_one());  //备用字段1
			info.setSpare_column_two(event.getSpare_column_two());  //备用字段2
			info.setSpare_column_three(event.getSpare_column_three());  //备用字段3
			info.setSpare_column_four(event.getSpare_column_four());  //备用字段4
			info.setSpare_column_five(event.getSpare_column_five());  //备用字段5
			info.setSpare_column_six(event.getSpare_column_six());  //备用字段6
			info.setSpare_column_seven(event.getSpare_column_seven());  //备用字段7
			info.setSpare_column_eight(event.getSpare_column_eight());  //备用字段8
			info.setSpare_column_nine(event.getSpare_column_nine());  //备用字段9
			info.setSpare_column_ten(event.getSpare_column_ten());  //备用字段10
			info.setCorpno(runEnvs.getCorpno());  //法人代码
			info.setRecdver(1l);  //记录版本号
			
			Fap_accounting_prod_ruleDao.insert(info);
			
			// 登记审计
			ApDataAudit.regLogOnInsertParameter(info);
		}
		
		bizlog.method(" FaLoanAccountingEventMnt.addLoanAccountingEvent end <<<<<<<<<<<<<<<<");
	}
	
	/**
	 * 
	 * @Author rongjie
	 *         <p>
	 *         <li>2020年9月16日-下午3:06:13</li>
	 *         <li>功能说明：产品核算事件维护</li>
	 *         </p>
	 * @param eventInfo
	 */
	
	public static void mntLoanAccountingEvent(Options<fap_accounting_prod_rule> mnteventInfo) {
		bizlog.method(" FaLoanAccountingEventMnt.mntLoanAccountingEvent begin >>>>>>>>>>>>>>>>");

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		
		for(fap_accounting_prod_rule event : mnteventInfo) {

			//借方科目合法性校验
			if(CommUtil.isNotNull(event.getDebit_subject())) {
				if (!FaAccountingSubject.checkSubjectExists(event.getDebit_subject())) {
					throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.error_gl_code.getId(), event.getDebit_subject());
				}
			}
			//贷方科目合法性校验
			if(CommUtil.isNotNull(event.getCredit_subject())) {
				if (!FaAccountingSubject.checkSubjectExists(event.getCredit_subject())) {
					throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.error_gl_code.getId(), event.getCredit_subject());
				}
			}
			fap_accounting_prod_rule oldInfo = Fap_accounting_prod_ruleDao.selectOne_odb2(event.getSys_no(), event.getProduct_code(), event.getEvent_code(), event.getData_sort(), event.getEffect_date(), false);
			//克隆
			fap_accounting_prod_rule mntInfo = CommTools.clone(fap_accounting_prod_rule.class, oldInfo);
			
			mntInfo.setAccount_status(event.getAccount_status());  //贷款账户状态
			mntInfo.setDebit_subject(event.getDebit_subject());  //借方科目
			mntInfo.setCredit_subject(event.getCredit_subject());  //贷方科目
			mntInfo.setRed_reverse_flag(event.getRed_reverse_flag());  //红字标识
			mntInfo.setTax_separate_flag(event.getTax_separate_flag());  //增值税分离标识
			mntInfo.setTax_event_code(event.getTax_event_code());  //增值税交易编码
			mntInfo.setTax_code(event.getTax_code());  //税码
			mntInfo.setEffect_date(event.getEffect_date());  //生效日期
			mntInfo.setInvalid_date(event.getInvalid_date());  //失效日期
			mntInfo.setLast_tran_user(runEnvs.getTranus());  //最后修改柜员
			mntInfo.setSpare_column_one(event.getSpare_column_one());  //备用字段1
			mntInfo.setSpare_column_two(event.getSpare_column_two());  //备用字段2
			mntInfo.setSpare_column_three(event.getSpare_column_three());  //备用字段3
			mntInfo.setSpare_column_four(event.getSpare_column_four());  //备用字段4
			mntInfo.setSpare_column_five(event.getSpare_column_five());  //备用字段5
			mntInfo.setSpare_column_six(event.getSpare_column_six());  //备用字段6
			mntInfo.setSpare_column_seven(event.getSpare_column_seven());  //备用字段7
			mntInfo.setSpare_column_eight(event.getSpare_column_eight());  //备用字段8
			mntInfo.setSpare_column_nine(event.getSpare_column_nine());  //备用字段9
			mntInfo.setSpare_column_ten(event.getSpare_column_ten());  //备用字段10

			//对比数据版本
			if( CommUtil.compare(oldInfo.getRecdver(), event.getRecdver())  !=0 ){
				throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_accounting_event_parm.class).getLongname());
			}
			
			// 登记审计
			if ( ApDataAudit.regLogOnUpdateParameter(oldInfo, mntInfo) == 0) {
				throw ApPubErr.APPUB.E0023(OdbFactory.getTable(fap_accounting_event_parm.class).getLongname());
			}
			
			mntInfo.setRecdver(event.getRecdver()+1);
			Fap_accounting_prod_ruleDao.updateOne_odb2(mntInfo);
			
		}
		
		bizlog.method(" FaLoanAccountingEventMnt.mntLoanAccountingEvent end <<<<<<<<<<<<<<<<");
	}
	
	/**
	 * 
	 * @Author rongjie
	 *         <p>
	 *         <li>2020年9月16日-下午3:06:23</li>
	 *         <li>功能说明：产品核算事件删除</li>
	 *         </p>
	 * @param eventInfo
	 */
	public static void delLoanAccountingEvent(Options<fap_accounting_prod_rule> deleventInfo) {
		bizlog.method(" FaLoanAccountingEventMnt.delLoanAccountingEvent begin >>>>>>>>>>>>>>>>");
		
		for(fap_accounting_prod_rule event: deleventInfo) {
			fap_accounting_prod_rule deletInfo = Fap_accounting_prod_ruleDao.selectOne_odb2(event.getSys_no(), event.getProduct_code(), event.getEvent_code(), event.getData_sort(), event.getEffect_date(), false);
			
			// 版本号非空校验
			CommTools.fieldNotNull(event.getRecdver(), BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

			// 对比数据版本
			if (CommUtil.compare(event.getRecdver(), deletInfo.getRecdver()) != 0) {
				throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_accounting_event_parm.class).getName());
			}

			// 删除数据
			Fap_accounting_prod_ruleDao.deleteOne_odb2(event.getSys_no(), event.getProduct_code(), event.getEvent_code(), event.getData_sort(), event.getEffect_date());

			// 登记审计
			ApDataAudit.regLogOnDeleteParameter(deletInfo);
		}
		
		bizlog.method(" FaLoanAccountingEventMnt.delLoanAccountingEvent end <<<<<<<<<<<<<<<<");
	}
	/**
	 * 
	 * @Author rongjie
	 *         <p>
	 *         <li>2020年9月17日-下午8:03:00</li>
	 *         <li>功能说明：产品核算事件查询</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_rule> queryLoanEvent( final cn.sunline.ltts.busi.fa.type.ComFaLoanAccounting.FaLoanFapAccountingprodruleqry queryIn){
		bizlog.method(" FaLoanAccountingEventMnt.queryLoanEvent begin >>>>>>>>>>>>>>>>");

		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<fap_accounting_prod_rule> page = FaLoanAccountingDao.lstFapAccountingProdRulelist(orgId, queryIn.getSys_no(), 
				queryIn.getProduct_code(), queryIn.getEvent_code(), queryIn.getAccount_status(), queryIn.getEffect_date(), 
				queryIn.getInvalid_date(), queryIn.getDebit_subject(), queryIn.getCredit_subject(), 
				(pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		
		runEnvs.setCounts(page.getRecordCount());
		Options<fap_accounting_prod_rule> info = new DefaultOptions<fap_accounting_prod_rule>();
		info.setValues(page.getRecords());
		
		bizlog.method(" FaLoanAccountingEventMnt.queryLoanEvent end <<<<<<<<<<<<<<<<");
		return info;
	}

}
