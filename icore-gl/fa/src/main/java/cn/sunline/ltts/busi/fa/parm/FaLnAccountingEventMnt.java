package cn.sunline.ltts.busi.fa.parm;

import java.math.BigDecimal;
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
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaLnAccountingDao;
import cn.sunline.ltts.busi.fa.servicetype.SrvFaLnAccountingEvent.analysisLnAccountingEvent.Output;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_prod_sceneDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_event_parm;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_scene;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneInfo;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneResult;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingTxsInfo;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnFapAccountingprodSceneInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_AMOUNT_DIRECTION_SIGN;
import cn.sunline.ltts.sys.dict.GlDict;

/**
 * 
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author 
 *         <p>
 *         <li>2020年10月20日-下午2:54:07</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>场景核算事件管理：增、删、改、查、试解析</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaLnAccountingEventMnt {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaLnAccountingEventMnt.class);

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年10月20日-下午2:54:54</li>
	 *         <li>功能说明：新增场景核算事件</li>
	 *         </p>
	 * @param addIn
	 */
	public static void addLnAccountingEvent(Options<fap_accounting_prod_scene> addIn) {
		
		bizlog.method(" FaLnAccountingEventMnt.addLnAccountingEvent begin >>>>>>>>>>>>>>>>");
		
		// 集合非空校验
		if(addIn.isEmpty()) {
			throw GlError.GL.E0219();
		}
		
		// 获取公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		
		for(fap_accounting_prod_scene scene : addIn) {
			
			// 非空字段判断
			FaLnAccountingEvent.checkNull(scene);
			
			// 机构校验
			FaLoanAccountingEvent.existsFapSysDefine(scene.getSys_no());
			
			// 科目合法性校验
			if(!FaAccountingSubject.checkSubjectExists(scene.getGl_code())) {
					throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), 
							GlDict.A.error_gl_code.getId(), scene.getGl_code());
			}
			
			// 唯一合法性检查
			fap_accounting_prod_scene prodScene = Fap_accounting_prod_sceneDao.selectOne_odb1(scene.getScene_code(), 
					scene.getProduct_code(), scene.getBal_type(), scene.getLoan_term(), scene.getData_sort(), scene.getEffect_date(), false);
			if(CommUtil.isNotNull(prodScene)) {
				throw ApPubErr.APPUB.E0019(OdbFactory.getTable(fap_accounting_prod_scene.class).getLongname(), 
						scene.getScene_code() + " " + scene.getProduct_code() + " " + scene.getBal_type() 
						+ " " + scene.getLoan_term() + " " + scene.getData_sort() + " " + scene.getEffect_date());
			}
			
			// 新增记录
			fap_accounting_prod_scene entity = SysUtil.getInstance(fap_accounting_prod_scene.class);
			CommUtil.copyProperties(entity, scene);
			entity.setTran_user(runEnvs.getTranus());  //新增柜员
			entity.setAuth_user(runEnvs.getCkbsus());  //新增授权柜员(串)
			entity.setInit_date(runEnvs.getTrandt());  //初始化日期
			entity.setRecdver(1l);  //记录版本号
			Fap_accounting_prod_sceneDao.insert(entity);
			
			// 登记审计
			ApDataAudit.regLogOnInsertParameter(scene);
		}
		
		bizlog.method(" FaLnAccountingEventMnt.addLnAccountingEvent end <<<<<<<<<<<<<<<<");
		
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年10月20日-下午4:50:51</li>
	 *         <li>功能说明：维护场景核算事件</li>
	 *         <li>只能修改失效日期</li>
	 *         </p>
	 * @param modifyIn
	 */
	public static void mntLnAccountingEvent(Options<fap_accounting_prod_scene> modifyIn) {
		
		bizlog.method(" FaLnAccountingEventMnt.mntLnAccountingEvent begin >>>>>>>>>>>>>>>>");
		
		// 集合非空校验
		if(modifyIn.isEmpty()) {
			throw GlError.GL.E0219();
		}
		
		// 获取公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 遍历逐个处理
		for (fap_accounting_prod_scene scene : modifyIn) {
			
			// 非空字段判断
			FaLnAccountingEvent.checkNull(scene);
			
			// 机构校验
			FaLoanAccountingEvent.existsFapSysDefine(runEnvs.getInpucd());
			
			// 查询记录是否存在
			fap_accounting_prod_scene prodScene = Fap_accounting_prod_sceneDao.selectOne_odb1(scene.getScene_code(), 
					scene.getProduct_code(), scene.getBal_type(), scene.getBal_type(), scene.getData_sort(), scene.getEffect_date(), false);
			if(CommUtil.isNull(prodScene)) {
				throw GlError.GL.E0231(scene.getScene_code(), 
					scene.getProduct_code(), scene.getBal_type(), scene.getBal_type(), scene.getData_sort(), scene.getEffect_date());
			}
			
			// 关键信息校验：科目号
			if(!CommUtil.equals(scene.getGl_code(), prodScene.getGl_code())) {
				throw GlError.GL.E0232(scene.getGl_code(), prodScene.getGl_code());
			}
			
			//对比数据版本
			if( CommUtil.compare(prodScene.getRecdver(), scene.getRecdver())  !=0 ){
				throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_accounting_prod_scene.class).getLongname());
			}
			
			// 登记审计
			if ( ApDataAudit.regLogOnUpdateParameter(prodScene, scene) == 0) {
				throw ApPubErr.APPUB.E0023(OdbFactory.getTable(fap_accounting_prod_scene.class).getLongname());
			}
			
			// 生效记录只能做停用（修改失效日期）
 			if (CommUtil.compare(scene.getInvalid_date(), runEnvs.getTrandt()) <= 0) {
				throw GlError.GL.E0227();
			}
			
			prodScene.setRecdver(prodScene.getRecdver() + 1);           // 记录版本号
			prodScene.setInvalid_date(scene.getInvalid_date());    		// 失效日期
			prodScene.setLast_tran_user(runEnvs.getTranus());			// 修改柜员	
			prodScene.setLast_auth_user(runEnvs.getCkbsus());			// 修改复核柜员
			Fap_accounting_prod_sceneDao.updateOne_odb1(prodScene);
		}
		
		bizlog.method(" FaLnAccountingEventMnt.mntLnAccountingEvent end <<<<<<<<<<<<<<<<");
		
	}

	/**
	 * 
	 * @Author
	 *         <p>
	 *         <li>2020年10月20日-下午5:34:40</li>
	 *         <li>功能说明：删除场景核算定义表</li>
	 *         </p>
	 * @param deleteIn
	 */
	public static void delLnAccountingEvent(Options<fap_accounting_prod_scene> deleteIn) {
		
		bizlog.method(" FaLnAccountingEventMnt.delLnAccountingEvent begin >>>>>>>>>>>>>>>>");

		// 集合非空校验
		if(deleteIn.isEmpty()) {
			throw GlError.GL.E0219();
		}
		
		// 逐个处理
		for (fap_accounting_prod_scene scene : deleteIn) {
			
			// 非空校验
			FaLnAccountingEvent.checkNull(scene);
			
			// 记录校验
			fap_accounting_prod_scene prodScene = Fap_accounting_prod_sceneDao.selectOne_odb1(scene.getScene_code(), 
					scene.getProduct_code(), scene.getBal_type(), scene.getLoan_term(), scene.getData_sort(), scene.getEffect_date(), false);
			if(CommUtil.isNull(prodScene)) {
				throw GlError.GL.E0231(scene.getScene_code(), 
					scene.getProduct_code(), scene.getBal_type(), scene.getLoan_term(), scene.getData_sort(), scene.getEffect_date());
			}
			
			// 对比数据版本
			if (CommUtil.compare(scene.getRecdver(), prodScene.getRecdver()) != 0) {
				throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_accounting_event_parm.class).getName());
			}
			
			// 直接删除
			Fap_accounting_prod_sceneDao.deleteOne_odb1(scene.getScene_code(), 
					scene.getProduct_code(), scene.getBal_type(), scene.getLoan_term(), scene.getData_sort(), scene.getEffect_date());
			
			// 登记审计
			ApDataAudit.regLogOnDeleteParameter(prodScene);
			
		}
		
		bizlog.method(" FaLnAccountingEventMnt.delLnAccountingEvent end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年10月20日-下午5:46:47</li>
	 *         <li>功能说明：场景核算定义查询</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static Options<fap_accounting_prod_scene> queryLoanEvent(FaLnFapAccountingprodSceneInfo queryIn) {
		
		bizlog.method(" FaLnAccountingEventMnt.queryLoanEvent begin >>>>>>>>>>>>>>>>");
		
		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<fap_accounting_prod_scene> page = FaLnAccountingDao.lstFapAccountingProdSceneList(orgId, 
				queryIn.getSys_no(), queryIn.getScene_code(), queryIn.getProduct_code(), queryIn.getBal_type(), 
				queryIn.getLoan_term(), queryIn.getGl_code(), queryIn.getEffect_date(), queryIn.getInvalid_date(), 
				(pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		
		runEnvs.setCounts(page.getRecordCount());
		
		bizlog.method(" FaLnAccountingEventMnt.queryLoanEvent end <<<<<<<<<<<<<<<<");
		
		return new DefaultOptions<>(page.getRecords());
		
	}


	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年10月22日-下午7:10:21</li>
	 *         <li>功能说明：解析场景事件流水</li>
	 *         </p>
	 * @param analysisIn
	 * @return
	 */
	public static Output analysisLoanEvents(Options<FaLnAccountingSceneInfo> analysisIn) {
		
		bizlog.method(" FaLnAccountingEventMnt.analysisLoanEvents begin >>>>>>>>>>>>>>>>");
		
		// 非空校验
		if(analysisIn.isEmpty()) {
			throw GlError.GL.E0218();
		}
		
		// 定义输出结果
		Output output = SysUtil.getInstance(Output.class); 
		Options<FaLnAccountingSceneResult> resultList = output.getResultList();
		Options<FaLnAccountingTxsInfo> txsInfoList = output.getTxsInfoList();
		
		// 遍历逐个解析
		for (FaLnAccountingSceneInfo info : analysisIn) {
			Output singleOutPut = analysisLoanEvent(info);
			resultList.addAll(singleOutPut.getResultList());
			txsInfoList.addAll(singleOutPut.getTxsInfoList());
		}
		
		bizlog.debug("所有事件解析结果[%s]", output);
		bizlog.method(" FaLnAccountingEventMnt.analysisLoanEvents end <<<<<<<<<<<<<<<<");
		
		return output;
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年10月22日-下午7:13:26</li>
	 *         <li>功能说明：解析单笔场景事件流水</li>
	 *         </p>
	 * @param info
	 * @return
	 */
	private static Output analysisLoanEvent(FaLnAccountingSceneInfo info) {
		
		bizlog.method(" FaLnAccountingEventMnt.analysisLoanEvent begin >>>>>>>>>>>>>>>>");
		
		// 数据非空校验
		FaLnAccountingEvent.checkNull(info);
		
		// 数据合法性校验
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		FaLoanAccountingEvent.existsFapSysDefine(info.getSys_no());
		
		// 根据产品编号、交易编码查询分录配置
		List<fap_accounting_prod_scene> sceneList = FaLnAccountingDao.lstFapAccountingProdSceneInfos(
				runEnvs.getCorpno(), info.getSys_no(), info.getScene_code(), info.getProduct_code(),
				info.getBal_type(), info.getLoan_term(), info.getTrxn_date(), false);
		if(sceneList.isEmpty()) {
			throw GlError.GL.E0233(info.getSys_no(), info.getScene_code(), info.getProduct_code(), 
					info.getBal_type(), info.getLoan_term(), info.getTrxn_date());
		}
		
		bizlog.debug("ruleList[%s]", sceneList);
		
		// 解析会计分录
		Output output = SysUtil.getInstance(Output.class);  // 定义临时输出结果
		Options<FaLnAccountingSceneResult> resultList = output.getResultList();
		Options<FaLnAccountingTxsInfo> txsInfoList = output.getTxsInfoList();
		
		// 已计算过税金
		boolean taxDone = false;
		BigDecimal taxAmount = BigDecimal.ZERO;
		
		for (fap_accounting_prod_scene scene : sceneList) {
			
			BigDecimal amount = info.getTrxn_amt().abs();  // 记账金额初始化
			if(E_YES___.YES == scene.getTax_separate_flag()) {
				
				if(!taxDone) {	
					
					// 未计算过税金，则需要
					FaLnAccountingTxsInfo txsInfo = FaLoanAccountingEventTax.calTaxAmount(info.getTrxn_amt().abs(),
							scene.getTax_code(), info.getTrxn_date(), info.getTrxn_date(), info.getTrxn_ccy());
					taxAmount = txsInfo.getTax_amount();
					txsInfoList.add(txsInfo);
					
					taxDone = true;
				}
				
				amount = CommUtil.isNull(scene.getTax_code()) ? taxAmount : info.getTrxn_amt().abs().subtract(taxAmount);
				
			}
			
			// 直接解析
			FaLnAccountingSceneResult result = SysUtil.getInstance(FaLnAccountingSceneResult.class);
			result = assemBaseSubjectTranInfo(info.getTrxn_ccy(), amount, scene);
			
			resultList.add(result);
			
		}
		
		// 返回结果
		bizlog.debug("单事件解析结果[%s]", output);
		bizlog.method(" FaLnAccountingEventMnt.analysisLoanEvent end <<<<<<<<<<<<<<<<");
		
		return output;
	}
	
	// 组装基础记账分录
	private static FaLnAccountingSceneResult assemBaseSubjectTranInfo(String trxn_ccy, BigDecimal tran_amount,
			fap_accounting_prod_scene scene) {
		
		bizlog.method(" FaLoanAccountingEventMnt.assemSubjectTranDetail begin >>>>>>>>>>>>>>>>");
		
		fap_accounting_subject subject = Fap_accounting_subjectDao.selectOne_odb1(scene.getGl_code(), true);
		if(CommUtil.isNull(subject)) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), scene.getGl_code());
		}
		
		// 处理得到最终记账金额
		BigDecimal accountingAmount = fixFinalAccountingAmount(tran_amount, scene.getAmount_direction_sign());
		
		FaLnAccountingSceneResult result = SysUtil.getInstance(FaLnAccountingSceneResult.class);
		result.setTrxn_amt(accountingAmount); 					// 交易金额	 
		result.setGl_code(scene.getGl_code());					// 科目号
		result.setData_sort(scene.getData_sort());   			// 数据序号
		result.setGl_code_desc(subject.getGl_code_desc());		// 科目名称
		result.setTrxn_ccy(trxn_ccy);							// 币种
		result.setDebit_credit(scene.getDebit_credit());		// 借贷方向
		
		bizlog.method(" FaLoanAccountingEventMnt.assemSubjectTranDetail end <<<<<<<<<<<<<<<<");
		return result;
		
	}

	// 处理得到最终记账金额
	private static BigDecimal fixFinalAccountingAmount(BigDecimal tran_amount, E_AMOUNT_DIRECTION_SIGN amount_direction_sign) {
		
		bizlog.method(" FaLnAccountingEventMnt.fixFinalAccountingAmount begin >>>>>>>>>>>>>>>>");
		
		BigDecimal accountingAmount = BigDecimal.ZERO;
		switch (amount_direction_sign) {
			case POSITIVE:
				accountingAmount = tran_amount.abs();
				break;
			case NEGATIVE:
				accountingAmount = tran_amount.abs().negate();
				break;
			case ORIGINAL:
				accountingAmount = tran_amount;
				break;
			default:
				accountingAmount = tran_amount;
				break;
		}
		
		bizlog.method(" FaLnAccountingEventMnt.fixFinalAccountingAmount end <<<<<<<<<<<<<<<<");
		
		return accountingAmount;
	}
	
}
