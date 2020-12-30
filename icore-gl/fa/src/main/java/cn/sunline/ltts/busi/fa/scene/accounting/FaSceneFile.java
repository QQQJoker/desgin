package cn.sunline.ltts.busi.fa.scene.accounting;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.fa.parm.FaSysService;
import cn.sunline.ltts.busi.fa.servicetype.SrvFaLnAccountingEvent;
import cn.sunline.ltts.busi.fa.servicetype.SrvFaLnAccountingEvent.analysisLnAccountingEvent.Output;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_accounting_scene_fail_seqDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_scene_fail_seq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_scene_seq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_original_voch;
import cn.sunline.ltts.busi.fa.type.ComFaFile.FaFileDown;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneInfo;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneResult;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingTxsInfo;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_RVERFG;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SYSTEMSERVICESTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQSTATE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEAL_STATUS;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.busi.sys.type.TxEnumType.E_CUSTTP;
import cn.sunline.ltts.busi.sys.type.TxEnumType.E_SOUSYS;
import cn.sunline.ltts.busi.tx.tables.TabTx.TxsBusiAdde;
import cn.sunline.ltts.busi.tx.tools.TxTools;
import cn.sunline.ltts.fa.util.FaApBatch;
import cn.sunline.ltts.fa.util.FaApFile;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;

public class FaSceneFile {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(FaSceneFile.class);

	/**
	 * 
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年11月16日-下午2:06:21</li>
	 *         <li>功能说明：读取产品交易事件文件</li>
	 *         </p>
	 * @param faFileDown
	 */
	public static void doAccountingSceneEventFile(FaFileDown faFileDown) {
		
		// 检查总账服务状态
		if (FaSysService.getSysStatus() != E_SYSTEMSERVICESTATUS.ON) {
			bizlog.method("sys status is close, exit! >>>>>>>>>>>>>>>>>>>>");
			bizlog.method("doAccountingProdEventFile end >>>>>>>>>>>>>>>>>>>>");
			return;
		}
		
		// 定位本地路径
		String localFileName = getLocalFileName(faFileDown);
		
		// 读取文件信息
		List<String> lines = FaApFile.readFile(new File(localFileName));
		bizlog.debug("lines.size()=[%s]", lines.size());
		
		String busiBatchCode = faFileDown.getBusi_batch_code(); // 文件批量号
		
		// 空文件直接更新状态
		if(lines.isEmpty()) {
			FaApBatch.setbatchReqStatus(busiBatchCode, E_FILEDEALSTATUS.FAILCHECK_OTHER);
			return;
		}

		int totalCount = 0; 						// 总记录数
		BigDecimal totalAmt = BigDecimal.ZERO; 		// 总金额
		
		// 检验文件记录条数是否为0
		if(checkHeadInfo(lines.get(0), busiBatchCode)) {

			String sysNo = faFileDown.getSys_no(); 			// 系统编号
			
			// 异常处理机制：1：直接报错，不往下走  2：更新状态为失败，不影响其他 
			String analysisError = FaTools.getLnAnalysisError(); 
			if (CommUtil.isNull(analysisError)) {
				analysisError = FaConst.GL_CODE_ANALYSIS_ERROR;
			}
			
			// 逐条处理文件体
			List<String> fileBodys = lines.subList(1, lines.size());
			bizlog.debug("fileBodys=[%s]", fileBodys);
			
			int countParm = FaTools.getBatchSubmitCount();	// 获取数据库批量提交笔数参数
			ArrayList<fab_accounting_scene_seq> sceneSeqList = new ArrayList<fab_accounting_scene_seq>();  // 产品交易流水登记簿
			List<fab_original_voch> vochList = new ArrayList<fab_original_voch>();  // 外系统原始凭证
			List<TxsBusiAdde> txsAddeList = new ArrayList<TxsBusiAdde>();  // 营改增明细

			boolean toUpdStatus = true;
			
			for (String line : fileBodys) {

				// 截取赋值
				fab_accounting_scene_seq sceneSeq = transferToSceneSeq(line);
				
				// 调用解析文件服务
				Options<FaLnAccountingSceneResult> results = new DefaultOptions<FaLnAccountingSceneResult>();
				Options<FaLnAccountingTxsInfo> txsInfos = new DefaultOptions<FaLnAccountingTxsInfo>();
				try {
					
					DefaultOptions<FaLnAccountingSceneInfo> infos = new DefaultOptions<FaLnAccountingSceneInfo>();
					FaLnAccountingSceneInfo info = SysUtil.getInstance(FaLnAccountingSceneInfo.class);
					CommUtil.copyProperties(info, sceneSeq);
					infos.add(info);
					Output analyOutput = SysUtil.getInstance(SrvFaLnAccountingEvent.class).analysisLnAccountingEvent(infos);
					
					results = analyOutput.getResultList();
					txsInfos = analyOutput.getTxsInfoList();
				}catch (Exception e) {
					
					bizlog.debug("not set sysNo=[%s] scene_code=[%s] product_code=[%s] bal_type[%s]", sysNo, 
							sceneSeq.getScene_code(), sceneSeq.getProduct_code(), sceneSeq.getBal_type());
					bizlog.debug("analysisLnAccountingEvent:[%s]",  e);
					
					sceneSeq.setTrxn_seq_status(E_TRXNSEQSTATE.AYNASYFAILURE); // 更改状态
					insertFabAccountingSceneFaiSeq(sceneSeq, e);  // 插入失败流水登记簿
					
					if(CommUtil.equals(FaConst.GL_CODE_ANALYSIS_ERROR, analysisError)) {
						throw GlError.GL.E0238(sysNo, sceneSeq.getScene_code(), sceneSeq.getProduct_code(), sceneSeq.getBal_type(),
								sceneSeq.getLoan_term(), sceneSeq.getTrxn_date());
					}
					
					continue;
				}
				
				// 解析场景事件登记外系统原始记账凭证
				if(!results.isEmpty()) {
					vochList.addAll(assemVochList(sceneSeq, results));
				}
				
				// 解析场景事件登记营改增明细
				if(!txsInfos.isEmpty()) {
					txsAddeList.addAll(assemTxsAddeList(sceneSeq, txsInfos));
				}
				
				sceneSeq.setRecdver(1L);
				sceneSeq.setTrxn_seq_status(E_TRXNSEQSTATE.AYNASIED);   // 解析状态已登记
				sceneSeqList.add(sceneSeq);
				
				totalCount++;
				totalAmt = totalAmt.add(sceneSeq.getTrxn_amt());
				
				// 达到事务提交记录数则先插入
				if (sceneSeqList.size() == countParm) {
					toUpdStatus = insertSeqInfo(sceneSeqList, vochList, txsAddeList, busiBatchCode);
					if(!toUpdStatus) {
						return;
					}
				}	
				
			}
			
			// 插入数量不足事务提交记录数的数据
			if (sceneSeqList.size() > 0) {
				toUpdStatus = insertSeqInfo(sceneSeqList, vochList, txsAddeList, busiBatchCode);
			}
			
			// 校验头体数据、更新状态 TODO 此处是否需要校验总金额和累计金额比对
			if(toUpdStatus) {
				updateStatusByImport(busiBatchCode, lines.get(0), totalCount, totalAmt);
			}
			
		} 
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月24日-上午11:28:02</li>
	 *         <li>功能说明：根据解析结果组装营改增明细</li>
	 *         </p>
	 * @param sceneSeq
	 * @param txsInfos
	 * @return
	 */
	private static List<TxsBusiAdde> assemTxsAddeList(fab_accounting_scene_seq sceneSeq, Options<FaLnAccountingTxsInfo> txsInfos) {
		
		bizlog.method(" FaSceneFile.assemTxsAddeList begin >>>>>>>>>>>>>>>>");
		
		ArrayList<TxsBusiAdde> txsAddeList = new ArrayList<TxsBusiAdde>();
		
		long sortno = TxTools.getMaxSortNoOfTxsBusiAdde(sceneSeq.getLoan_no());
		for (FaLnAccountingTxsInfo info : txsInfos) {
			
			TxsBusiAdde entity = SysUtil.getInstance(TxsBusiAdde.class);
			entity.setCustno(sceneSeq.getCust_no());	// 客户号
			entity.setSortno(sortno + 1);				// 序号
			sortno = sortno + 1;
			entity.setCusttp(E_CUSTTP.PERSONAL);		// 客户类型
			entity.setCustac(null);						// 电子账号
			entity.setAcctno(sceneSeq.getLoan_no());	// 借据号
			entity.setHsheam(info.getTaxin_amount());	// 含税金额
			entity.setBhseam(info.getTaxout_amount()); 	// 不含税金额        
			entity.setYstxam(info.getTax_amount());		// 应收税额
			entity.setSjysam(info.getReal_tax_amount());			// 实际应收税额
			entity.setTaxrat(info.getTax_rate());		// 税率
			entity.setFinadt(sceneSeq.getTrxn_date()); 	// 账务日期 
			entity.setTransq(sceneSeq.getTrxn_seq());	// 账务流水
			entity.setInpusq(sceneSeq.getTrxn_seq());	// 上送系统流水
			entity.setRverfg(E_RVERFG.NONE);			// 暂时不冲税
			entity.setPrcscd(CommTools.prcRunEnvs().getPrcscd());	// 交易码
			entity.setBilltp(info.getBill_type());		// 开票类型
			entity.setItemcd(info.getGl_code()); 		// 科目
			entity.setAcctcd(info.getTax_code());		// 税码
			entity.setBktpro(E_BLTYPE.VAT);				// 余额属性
			entity.setTranev(info.getTran_event());		// 交易事件
			entity.setBrchno(FaConst.LOAN_ACCT_BRANCH); // 机构号
			entity.setTranus(null);						// 交易柜员
			entity.setSousys(E_SOUSYS.INBANK);			// 系统来源
			entity.setCrcycd(info.getTrxn_ccy());       // 币种
			
			txsAddeList.add(entity);
		}
		
		bizlog.debug("sceneSeq [%s],解析后的营改增明细 txsAddeList[%s]", sceneSeq.getTrxn_seq(), txsAddeList);
		bizlog.method(" FaSceneFile.assemTxsAddeList end <<<<<<<<<<<<<<<<");
		return txsAddeList;
	}

	/**
	 * 
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年11月16日-下午2:05:37</li>
	 *         <li>功能说明：修改文件批量请求薄状态</li>
	 *         </p>
	 * @param busiBatchCode
	 * @param firstLine
	 * @param totalCount
	 * @param totalAmt
	 */
	private static void updateStatusByImport(String busiBatchCode, String firstLine, int totalCount, BigDecimal totalAmt) {
		bizlog.method(" FaSceneFile.updateStatusByImport begin >>>>>>>>>>>>>>>>");

		String[] firstLineSplits = firstLine.split(FaConst.LOAN_SEPARATION_CHARACTER);
		FaApBatch.setStatusByImport(busiBatchCode, Long.valueOf(firstLineSplits[0]), new BigDecimal(firstLineSplits[1]), totalCount, totalAmt);
		
		bizlog.method(" FaSceneFile.updateStatusByImport end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年11月16日-下午2:04:13</li>
	 *         <li>功能说明：将读到的一条数据转化为场景流水</li>
	 *         </p>
	 * @param line
	 * @return
	 */
	private static fab_accounting_scene_seq transferToSceneSeq(String line) {
		
		bizlog.method(" FaSceneFile.tranFerSceneSeq begin >>>>>>>>>>>>>>>>");
		
		fab_accounting_scene_seq sceneSeq = SysUtil.getInstance(fab_accounting_scene_seq.class);
		
		String[] lineSplits = line.split(FaConst.LOAN_SEPARATION_CHARACTER);
		sceneSeq.setTrxn_seq(lineSplits[0]);					// 交易流水号
		sceneSeq.setBusi_seq(lineSplits[1]);					// 业务流水
		sceneSeq.setData_sort(Long.valueOf(lineSplits[2]));		// 流水组内序号
		sceneSeq.setTrxn_date(lineSplits[3]);					// 交易/账务 日期
		sceneSeq.setLoan_no(lineSplits[4]);						// 借据号
		sceneSeq.setScene_code(lineSplits[5]);					// 场景编号
		sceneSeq.setProduct_code(lineSplits[6]);				// 产品编号
		sceneSeq.setBal_type(lineSplits[7]);					// 金额类别
		sceneSeq.setLoan_term(lineSplits[8]);					// 期限类别
		sceneSeq.setTrxn_amt(new BigDecimal(lineSplits[9]));	// 交易金额
		sceneSeq.setTax_type(lineSplits[10]);					// 税种代码
		sceneSeq.setCust_no(lineSplits[11]);					// 客户号
		sceneSeq.setSys_no(FaConst.LOAN_SYSTEM);				// 系统编号
		sceneSeq.setAcct_branch(FaConst.LOAN_ACCT_BRANCH);		// 账务机构
		sceneSeq.setTrxn_ccy(FaConst.LOAN_TRXN_CCY);			// 币种
		sceneSeq.setHash_value(Long.valueOf(lineSplits[0]) % 100);  // hash 值
		sceneSeq.setTrxn_seq_status(E_TRXNSEQSTATE.AYNASIED);	// 状态为 已解析
			
		bizlog.debug("sceneSeq [%s]", sceneSeq);
		bizlog.method(" FaSceneFile.tranFerSceneSeq end <<<<<<<<<<<<<<<<");
		
		return sceneSeq;
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月16日-下午2:04:25</li>
	 *         <li>功能说明：组装原始凭证</li>
	 *         </p>
	 * @param sceneSeq
	 * @param results
	 * @return
	 */
	public static List<fab_original_voch> assemVochList(fab_accounting_scene_seq sceneSeq, Options<FaLnAccountingSceneResult> results) {
		
		bizlog.method(" FaSceneFile.assemVochList begin >>>>>>>>>>>>>>>>");

		List<fab_original_voch> vochList = new ArrayList<fab_original_voch>();
		for (FaLnAccountingSceneResult result : results) {
			fab_original_voch voch = SysUtil.getInstance(fab_original_voch.class);
			voch.setTrxn_date(sceneSeq.getTrxn_date()); 		// 交易日期
			voch.setSys_no(sceneSeq.getSys_no()); 				// 系统编号
			voch.setTrxn_seq(sceneSeq.getTrxn_seq()); 			// 交易流水
			voch.setData_sort(sceneSeq.getData_sort()); 		// 数据序号
			// voch.setAcct_no(sceneSeq.getAcct_no()); TODO 暂时没有
			// voch.setSub_acct_seq(sceneSeq.getSub_acct_seq());
			voch.setBusi_seq(sceneSeq.getBusi_seq()); 			// 业务流水
			voch.setAcct_branch(sceneSeq.getAcct_branch()); 	// 账务机构
			voch.setTrxn_ccy(sceneSeq.getTrxn_ccy()); 			// 交易币种
			voch.setGl_code(result.getGl_code()); 				// 科目号
			voch.setDebit_credit(result.getDebit_credit()); 	// 记账方向
			voch.setTrxn_amt(result.getTrxn_amt()); 			// 交易金额
			voch.setCorpno(sceneSeq.getCorpno()); 				// 法人代码
			voch.setRecdver(1l);
			vochList.add(voch);
		}
		
		bizlog.method(" FaSceneFile.assemVochList end <<<<<<<<<<<<<<<<");
		return vochList;
	}

	/**
	 * 
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年11月16日-下午2:04:41</li>
	 *         <li>功能说明：解析失败的流水备份到解析失败流水登记簿</li>
	 *         </p>
	 * @param sceneSeq
	 * @param e 
	 */
	public static void insertFabAccountingSceneFaiSeq(fab_accounting_scene_seq sceneSeq, Exception e) {
		
		bizlog.method(" FaSceneFile.insertFabAccountingSceneFaiSeq begin >>>>>>>>>>>>>>>>");
		DBTools.exeInNewTransation(new RunnableWithReturn<Void>() {
            @Override
            public Void execute() {
            	
            	fab_accounting_scene_fail_seq failSeq = SysUtil.getInstance(fab_accounting_scene_fail_seq.class);
            	CommUtil.copyProperties(failSeq, sceneSeq);
            	
            	// 错误信息
            	int messageLength = e.getMessage().length() >= 1000 ? 1000 : e.getMessage().length();
            	failSeq.setError_message(e.getMessage().substring(0, messageLength));
            	
            	// 错误堆栈
            	String stackTrace = ExceptionUtil.dumpStackTrace(e.fillInStackTrace());
            	int errorLength = stackTrace.length() >= 4000 ? 4000 :stackTrace.length();
            	failSeq.setError_stack(stackTrace.substring(0,errorLength));
            	
            	failSeq.setDeal_status(E_DEAL_STATUS.TODO);
            	failSeq.setRecdver(1L);
            	Fab_accounting_scene_fail_seqDao.insert(failSeq);
            	return null;
            }
		});
		bizlog.method(" FaSceneFile.insertFabAccountingSceneFaiSeq end <<<<<<<<<<<<<<<<");
		
	}

	/**
	 * 
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年11月16日-下午2:04:52</li>
	 *         <li>功能说明：插入表数据</li>
	 *         </p>
	 * @param sceneSeqList
	 * @param vochList
	 * @param txsAddeList 
	 * @param busiBatchCode
	 */
	private static boolean insertSeqInfo(ArrayList<fab_accounting_scene_seq> sceneSeqList, List<fab_original_voch> vochList, List<TxsBusiAdde> txsAddeList, String busiBatchCode) {
		
		bizlog.method(" FaProdFile.insertSeqInfo begin >>>>>>>>>>>>>>>>");
		
		boolean toUpdStatus = true;
		
		try {
			
			// 文件明细信息批量插入表
			DaoUtil.insertBatch(fab_accounting_scene_seq.class, sceneSeqList);
			sceneSeqList.clear();
			
			// 文件明细信息批量插入表
			DaoUtil.insertBatch(fab_original_voch.class, vochList);
			vochList.clear();
			
			// 营改增明细表
			DaoUtil.insertBatch(TxsBusiAdde.class, txsAddeList);
			txsAddeList.clear();
			
		} catch (Exception e) {
			DBTools.rollback();
			// 导入明细表异常、更新状态
			FaApBatch.setInsertErrorByImport(busiBatchCode, e.toString());
			toUpdStatus = false;
		}
		
		bizlog.parm("toUpdStatus", toUpdStatus);
		bizlog.method(" FaProdFile.insertSeqInfo end <<<<<<<<<<<<<<<<");
		return toUpdStatus;
		
	}

	/**
	 * 
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年11月16日-下午2:05:04</li>
	 *         <li>功能说明：检查文件头信息</li>
	 *         </p>
	 * @param firstLine
	 * @param busiBatchCode
	 * @return
	 */
	private static boolean checkHeadInfo(String firstLine, String busiBatchCode) {
		
		bizlog.method(" FaProdFile.checkTotalCount begin >>>>>>>>>>>>>>>>");
		
		boolean toDeal = true;		// 是否需要处理详情
		
		String[] firstLineSplits = firstLine.split(FaConst.LOAN_SEPARATION_CHARACTER);
		if(2 != firstLineSplits.length) {
			toDeal = false;
			FaApBatch.setbatchReqStatus(busiBatchCode, E_FILEDEALSTATUS.FAILCHECK_FORMAT);
		}
		if(0 == Long.valueOf(firstLineSplits[0]) 
				&& CommUtil.compare(new BigDecimal(firstLineSplits[1]), BigDecimal.ZERO) == 0) {
			toDeal = false;
			FaApBatch.setStatusByImport(busiBatchCode, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO);
		}
		
		bizlog.parm("toDeal[%s]", toDeal);
		bizlog.method(" FaProdFile.checkTotalCount end <<<<<<<<<<<<<<<<");
		
		return toDeal;
		
	}

	/**
	 * 
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年11月16日-下午2:05:14</li>
	 *         <li>功能说明：定位本地路径</li>
	 *         </p>
	 * @param faFileDown
	 * @return
	 */
	private static String getLocalFileName(FaFileDown faFileDown) {
		
		// 定位原始路径
		String localFileName = FaApFile.getFileFullPath(faFileDown.getFile_local_path(), faFileDown.getFile_name());
		
		// 定位远程路径
		/*if(CommUtil.equals("Y", FaTools.getRemoteDir())) {
			localFileName = FaApFile.getLocalHome(localFileName);
		}*/
		
		bizlog.debug("localFileName [%s]", localFileName);
		return localFileName;
		
	}
	
}
