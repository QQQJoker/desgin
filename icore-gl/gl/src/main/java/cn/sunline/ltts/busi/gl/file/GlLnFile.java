package cn.sunline.ltts.busi.gl.file;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.namedsql.FaLnAccountingDao;
import cn.sunline.ltts.busi.fa.servicetype.SrvFaLnAccountingEvent;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_sys_defineDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_sys_define;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_ledger_check_seq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_lnledger_check_acseq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_lnledger_check_seq;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneInfo;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneResult;
import cn.sunline.ltts.busi.gl.namedsql.GlFileDao;
import cn.sunline.ltts.busi.gl.namedsql.GlLnFileDao;
import cn.sunline.ltts.busi.gl.type.GlFile.GlCheckRecord;
import cn.sunline.ltts.busi.gl.type.GlFile.GlFileHead;
import cn.sunline.ltts.busi.gl.type.GlFile.GlFileLaodCom;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.EnumUtils;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ANALYSISSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_BALPROP;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_BATCHTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaApBatch;
import cn.sunline.ltts.fa.util.FaApFile;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaCalcuBalanceIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLnLedgerCheckSeqInfo;
import cn.sunline.ltts.sys.dict.GlDict;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class GlLnFile {

	private static final BizLog bizlog = BizLogUtil.getBizLog(GlLnFile.class);
	
	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月2日-下午5:58:08</li>
	 *         <li>功能说明：读取网贷分户余额文件</li>
	 *         </p>
	 */
	public static void doLnLedgerBalFile() {
		
 		bizlog.method(" GlFile.doLnLedgerBalFile begin >>>>>>>>>>>>>>>>");

		List<GlFileLaodCom> fileLoads = new ArrayList<GlFileLaodCom>();

		// 从系统杂项表中取出等待时间, 单位为秒 30
		int waitTime = FaTools.getWaitTime();

		int sleepTime = 0;
		KnpPara para = ApKnpPara.getKnpPara("system.dcn", "dcn", "LNLEDGERBAL");
		int fileCnt = Integer.parseInt(para.getPmval1());
		do {

			// 从平台登记的表中取出已下载文件的信息
			fileLoads = GlFileDao.lstFileLoanDown(CommToolsAplt.prcRunEnvs().getCorpno(), E_BATCHTYPE.LN_LEDGER_DOWN, E_FILEDEALSTATUS.UNCHECK, E_YESORNO.YES, false);

			// 查询到数据，表明平台定时任务已下载数据，退出循环
            if (fileLoads.size() == fileCnt) {
                break;
            }

            // 如果无数据,则睡眠1秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            	Thread.currentThread().interrupt();
            }

			// 总睡眠时间
			sleepTime = sleepTime + 1;

			// 超时，
			if (sleepTime > waitTime) {
				throw GlError.GL.E0114();
			}

		}
		while (true);

		// 文件个数不止一个
		if (fileLoads.size() != fileCnt) {
			throw GlError.GL.E0108(E_BATCHTYPE.LN_LEDGER_DOWN.getLongName());

		}

		String analysisError = FaTools.getAnalysisError();
		if (CommUtil.isNull(analysisError)) {
			analysisError = FaConst.GL_CODE_ANALYSIS_ERROR;
		}
		// 读取文件，解析到数据库
		for (GlFileLaodCom fileLoad : fileLoads) {

			// 文件本地路径
			String localFileName = FaApFile.getFileFullPath(fileLoad.getFile_local_path(), fileLoad.getFile_name());

			if("Y".equals(FaTools.getRemoteDir())){
			    localFileName = FaApFile.getLocalHome(localFileName);
			}
			
			// 读取文件信息
			List<String> fileDataList = FaApFile.readFile(new File(localFileName));
			if (CommUtil.isNotNull(fileDataList) && fileDataList.size() > 0) {
				
				// 取文件头信息
				GlFileHead headInfo = GlLnFileSustain.checkHeadInfo(fileDataList.get(0), fileLoad.getBusi_batch_code());

				String sysNo = fileLoad.getSys_no();	// 系统编号
				String dcn = GlFile.getDcnByFileName(fileLoad.getFile_name());	// dcn 号
				
				// 文件体信息
				List<String> fileBodys = fileDataList.subList(1, fileDataList.size());

				// 文件体数据存在
				List<fab_lnledger_check_seq> seqList = new ArrayList<fab_lnledger_check_seq>();
				List<fab_lnledger_check_acseq> acseqList = new ArrayList<fab_lnledger_check_acseq>();
				for (String fileBody : fileBodys) {

					// 组装行数据为分户余额流水对象
					fab_lnledger_check_seq lederData = transferToFabLedgerCheckSeq(fileBody);
					lederData.setSys_no(sysNo); 								// 系统编号
					lederData.setAnalysis_state(E_ANALYSISSTATE.AYNASIED);		// 解析状态
					lederData.setCdcnno(dcn);									// dcn编号
					lederData.setRecord_no(CommTools.getCurrentThreadSeq(fab_ledger_check_seq.class, "fab_lnledger_check_seq"));   // 数据序号
					lederData.setId(Long.valueOf(sysNo + lederData.getTrxn_date() + lederData.getRecord_no()));					   // id
					lederData.setRecdver(1L);									// 版本号
					seqList.add(lederData);
					
					// 解析科目
					// 调用解析文件服务
					Options<FaLnAccountingSceneResult> results = new DefaultOptions<FaLnAccountingSceneResult>();
					try {
						
						FaLnAccountingSceneInfo info = SysUtil.getInstance(FaLnAccountingSceneInfo.class);
						CommUtil.copyProperties(info, lederData);
						ArrayList<FaLnAccountingSceneInfo> infoList = new ArrayList<FaLnAccountingSceneInfo>();
						infoList.add(info);
						results = SysUtil.getInstance(SrvFaLnAccountingEvent.class)
								.analysisLnAccountingEvent(new DefaultOptions<>(infoList)).getResultList();
						
					}catch (Exception e) {
						
						bizlog.debug("not set prod_scene:sysNo=[%s] scene_code=[%s] product_code=[%s] bal_type[%s]", sysNo, 
								lederData.getScene_code(), lederData.getProduct_code(), lederData.getBal_type(), lederData.getLoan_term());
						GlLnFileSustain.insertFailLnLedger(lederData);  // 解析失败余额流水记录落地
						
						if(CommUtil.equals("1", analysisError)) 
						throw GlError.GL.E0238(lederData.getSys_no(), lederData.getScene_code(), lederData.getProduct_code(), 
								lederData.getBal_type(), lederData.getLoan_term(), lederData.getTrxn_date());
					}

					// 组装返回结果
					if(!results.isEmpty()) {
						acseqList.addAll(assemAcseqList(lederData, results));
					}

					// 50一提交，批量插入文件明细
					if (seqList.size() == 50) {
						bizlog.debug("seqList[%s],acseqList[%s]", seqList, acseqList);
						insertSeq(seqList, acseqList, fileLoad.getBusi_batch_code());
					}

				}

				// 不足50数据，批量插入文件明细
				if (!seqList.isEmpty()) {
					bizlog.debug("seqList[%s],acseqList[%s]", seqList, acseqList);
					insertSeq(seqList, acseqList, fileLoad.getBusi_batch_code());
				}
				
				// 获取文件汇总的文件头信息
				GlFileHead fileHeadInfo = GlLnFileDao.selFileHeadInfoFromLnLedger(CommToolsAplt.prcRunEnvs().getCorpno(), CommToolsAplt.prcRunEnvs().getTrandt(),
						dcn,false);
				// 校验头体数据、更新状态
				boolean status = FaApBatch.returnStatusByImport(fileLoad.getBusi_batch_code(), headInfo.getHead_total_count(), headInfo.getHead_total_amt(),
						fileHeadInfo.getHead_total_count(), fileHeadInfo.getHead_total_amt());

				if (status == false) {
					GlError.GL.E0070(headInfo.getHead_total_count(), fileHeadInfo.getHead_total_count(), headInfo.getHead_total_amt(), fileHeadInfo.getHead_total_amt());
				}

			} else {
				throw GlError.GL.E0069(fileLoad.getFile_name());
			}
		}
		
		bizlog.method(" GlFile.doLnLedgerBalFile end <<<<<<<<<<<<<<<<");
	}
	
	/**
	 * 
	 * @param busiBatchCode 
	 * @Author
	 *         <p>
	 *         <li>2020年11月9日-下午3:41:04</li>
	 *         <li>功能说明：插入表数据</li>
	 *         </p>
	 * @param sceneSeqList
	 * @param vochList
	 * @param busiBatchCode
	 */
	private static void insertSeq(List<fab_lnledger_check_seq> seqList, List<fab_lnledger_check_acseq> acseqList, String busiBatchCode) {
		
		bizlog.method(" GlLnFile.insertSeq begin >>>>>>>>>>>>>>>>");
		
		try {
			DaoUtil.insertBatch(fab_lnledger_check_seq.class, seqList);
			seqList.clear();
			DaoUtil.insertBatch(fab_lnledger_check_acseq.class, acseqList);
			acseqList.clear();
		} catch (Exception e) {
			DBTools.rollback();
			// 导入明细表异常、更新状态
			FaApBatch.setInsertErrorByImport(busiBatchCode, e.toString());
			return;
		}
		
		bizlog.method(" GlLnFile.insertSeq end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月3日-下午2:33:40</li>
	 *         <li>功能说明：根据解析结果组装分户余额数据</li>
	 *         </p>
	 * @param lederData
	 * @param results
	 * @return
	 */
	private static List<fab_lnledger_check_acseq> assemAcseqList(fab_lnledger_check_seq lederData, Options<FaLnAccountingSceneResult> results) {
		
		bizlog.method(" GlFile.assemlnledgerDataList begin >>>>>>>>>>>>>>>>");
		
		ArrayList<fab_lnledger_check_acseq> acseqList = new ArrayList<fab_lnledger_check_acseq>();
		for (FaLnAccountingSceneResult result : results) {
			fab_lnledger_check_acseq acseq = SysUtil.getInstance(fab_lnledger_check_acseq.class);
			acseq.setId(lederData.getId());						// id
			acseq.setTrxn_date(lederData.getTrxn_date());		// 交易日期
			acseq.setDebit_credit(result.getDebit_credit());    // 科目记账方向
			acseq.setGl_code(result.getGl_code());			  	// 科目号
			acseq.setTrxn_amt(result.getTrxn_amt());			// 记账金额
			
			FaCalcuBalanceIn bal = calSubjectBal(result);
			acseq.setAcct_bal(bal.getAcct_bal());				// 余额
			acseq.setBal_debit_credit(bal.getDebit_credit());	// 分账余额方向
			
			acseq.setRecdver(1L);								// 版本号
			acseqList.add(acseq);
		}
		
		bizlog.debug("fab_lnledger_check_seq[%s],acseqList[%s]", lederData, acseqList);
		bizlog.method(" GlFile.assemlnledgerDataList end <<<<<<<<<<<<<<<<");
		
		return acseqList;
	}
	
	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月29日-下午6:02:44</li>
	 *         <li>功能说明：根据 科目发生额 计算 科目余额</li>
	 *         </p>
	 * @param result
	 * @return
	 */
	private static FaCalcuBalanceIn calSubjectBal(FaLnAccountingSceneResult result) {
		bizlog.method(" GlLnFile.calSubjectBal begin >>>>>>>>>>>>>>>>");
		
		// 查询科目
		fap_accounting_subject subjectInfo = Fap_accounting_subjectDao.selectOne_odb1(result.getGl_code(), false);
		if(CommUtil.isNull(subjectInfo)){
		    ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getLongName(), result.getGl_code());
		}
		
		// 查询 科目上日逻辑余额，初始到借方
		FaLnLedgerCheckSeqInfo lastBal = FaLnAccountingDao.qryLastLnLedgerBalance(
				CommTools.prcRunEnvs().getLstrdt(), result.getGl_code(), CommTools.prcRunEnvs().getCorpno(), false);
		
		// 计算 当前余额
		FaCalcuBalanceIn calBalanceIn = SysUtil.getInstance(FaCalcuBalanceIn.class);
		calBalanceIn.setDebit_credit(result.getDebit_credit());					// 记账方向
		calBalanceIn.setAccounting_amt(result.getTrxn_amt());					// 记账金额
		calBalanceIn.setBal_direction(lastBal.getLedger_bal_direction());		// 分户上日逻辑余额方向
		calBalanceIn.setAcct_bal(lastBal.getLedger_acct_bal());					// 上日逻辑余额
		calBalanceIn.setBal_prop(subjectInfo.getBal_prop());					// 余额性质
		FaAccounting.calcuBalance(calBalanceIn);
		
		// 如果科目余额实际是贷方，则转为贷方
		if(E_BALPROP.CREDIT == subjectInfo.getBal_prop()) {
			calBalanceIn.setBal_direction(E_DEBITCREDIT.CREDIT);
			calBalanceIn.setAcct_bal(calBalanceIn.getAcct_bal().multiply(new BigDecimal(-1)));
		}
		
		bizlog.debug(" FaCalcuBalanceIn [%s]:" + calBalanceIn);
		bizlog.method(" GlLnFile.calSubjectBal end <<<<<<<<<<<<<<<<");
		return calBalanceIn;
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月3日-下午4:35:25</li>
	 *         <li>功能说明：读取贷款校验文件</li>
	 *         </p>
	 */
	public static void doCheckLnAccountingFile() {
		
		bizlog.method(" GlFile.doCheckLnAccountingFile begin >>>>>>>>>>>>>>>>");
		
		// 从平台登记的表中取出已下载文件的信息
		GlFileLaodCom fileLoad = getGlFileLaodCom();
		
		// 读取文件并汇总：文件类型汇总数据
		List<GlCheckRecord> fileCheckRecordList = getFileRecord(fileLoad);

		// 计算落地数据中：文件类型汇总数据
		List<GlCheckRecord> glCheckRecordedList = getLocalRecord();
		
		// 对比数据
		GlLnFileSustain.checkDataOfFileAndRecord(fileCheckRecordList, glCheckRecordedList);
		
		// 更改文件请求簿
		FaApBatch.setbatchReqStatus(fileLoad.getBusi_batch_code(), E_FILEDEALSTATUS.SUCCESS);
		
		// 更改该系统日期
		fap_sys_define tabSysDefine = Fap_sys_defineDao.selectOne_odb1(FaConst.LOAN_SYSTEM, true);
		tabSysDefine.setSystem_date(CommToolsAplt.prcRunEnvs().getNxtrdt());
		
		bizlog.method(" GlFile.doCheckLnAccountingFile end <<<<<<<<<<<<<<<<");
		
	}

	/**
	 * 
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年11月6日-下午2:09:35</li>
	 *         <li>功能说明：读取文件中信息并汇总</li>
	 *         </p>
	 * @param fileLoad
	 * @return
	 */
	private static List<GlCheckRecord> getFileRecord(GlFileLaodCom fileLoad) {
		
		bizlog.method(" GlLnFile.getFileRecord begin >>>>>>>>>>>>>>>>");
		
		String localFileName = FaApFile.getFileFullPath(fileLoad.getFile_local_path(), fileLoad.getFile_name());
		if("Y".equals(FaTools.getRemoteDir())){
		    localFileName = FaApFile.getLocalHome(localFileName);
		}
		bizlog.debug("localFileName [%s]", localFileName);
		
		// 读取文件中各：文件类型汇总数据
		List<GlCheckRecord> fileCheckRecordList = new ArrayList<GlCheckRecord>(); // 文件信息
		List<String> fileDataList = FaApFile.readFile(new File(localFileName));
		if (!fileDataList.isEmpty()) {
			for (String fileBody : fileDataList) {
				// 将行数据转化为核心对账文件复合类型
				GlCheckRecord lederData = transferToGlCheckRecord(fileBody);
				bizlog.debug("lederData[%s]", lederData);
				fileCheckRecordList.add(lederData);
			}
		}
		bizlog.debug("文件中：lederData[%s]", fileCheckRecordList);
		
		fileCheckRecordList = GlFile.getGlCheckRecordSum(fileCheckRecordList);	//将各节点数据汇总
		bizlog.debug("汇总后：lederData[%s]", fileCheckRecordList);
		
		bizlog.method(" GlLnFile.getFileRecord end <<<<<<<<<<<<<<<<");
		
		return fileCheckRecordList;
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月6日-下午2:05:58</li>
	 *         <li>功能说明：获取待处理文件信息</li>
	 *         </p>
	 * @return
	 */
	private static GlFileLaodCom getGlFileLaodCom() {
		
		bizlog.method(" GlLnFile.getGlFileLaodCom begin >>>>>>>>>>>>>>>>");
		
		List<GlFileLaodCom> fileLoads = GlFileDao.lstFileLoanDown(CommToolsAplt.prcRunEnvs().getCorpno(), 
				E_BATCHTYPE.LN_CHECK_DOWN, E_FILEDEALSTATUS.UNCHECK, E_YESORNO.YES, false);
		
		// 获取配置处理文件个数: 1
		KnpPara para = ApKnpPara.getKnpPara("system.dcn", "dcn", "CHECKLNRECORD");
		int fileCnt = Integer.parseInt(para.getPmval1());
		if (fileLoads.size() != fileCnt) {
		    throw GlError.GL.E0115(E_BATCHTYPE.LN_CHECK_DOWN.getLongName());
		}
		
		bizlog.method(" GlLnFile.getGlFileLaodCom end <<<<<<<<<<<<<<<<");
		
		return fileLoads.get(0);
	}

	/**
	 * 
	 * @Author
	 *         <p>
	 *         <li>2020年11月3日-下午5:37:04</li>
	 *         <li>功能说明：根据文件场景查询落地数据</li>
	 *         </p>
	 * @return
	 */
	private static List<GlCheckRecord> getLocalRecord() {
		
		bizlog.method(" GlLnFile.getLocalRecord begin >>>>>>>>>>>>>>>>");
		
		ArrayList<GlCheckRecord> recordList = new ArrayList<GlCheckRecord>();  // 定义接受变量
		
		String sysNo = FaConst.LOAN_SYSTEM;  						// 系统编号
		String trxnDate = CommToolsAplt.prcRunEnvs().getTrandt();	// 交易日期
		
		// 场景流水落地数据
		recordList.addAll(GlLnFileDao.lstCheckDataFromAccountingSceneSeq(
				CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, sysNo, false));
		
		// 分户余额落地数据
		recordList.addAll(GlLnFileDao.lstCheckDataFromLnLeger(
				CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, sysNo, false));
		
		bizlog.debug("recordList[%s]", recordList);
		bizlog.method(" GlLnFile.getLocalRecord end <<<<<<<<<<<<<<<<");
		
		return recordList;
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月3日-下午5:14:51</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param line
	 * @return
	 */
	private static GlCheckRecord transferToGlCheckRecord(String line) {
		
		bizlog.method(" GlLnFile.transferToGlCheckRecord begin >>>>>>>>>>>>>>>>");
		
		GlCheckRecord record = SysUtil.getInstance(GlCheckRecord.class);
		String[] lineSplits = line.split(FaConst.LOAN_SEPARATION_CHARACTER);	
		record.setTrxn_date(lineSplits[0]);											// 交易日期
		record.setFile_type(EnumUtils.toEnum(E_BATCHTYPE.class, lineSplits[1]));	// 文件类型
		record.setRecord_number(Long.valueOf(lineSplits[2]));						// 记录笔数
		record.setTotal_amt(new BigDecimal(lineSplits[3]));							// 总金额
		
		bizlog.method(" GlLnFile.transferToGlCheckRecord end <<<<<<<<<<<<<<<<");
		
		return record;
	}
	
	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年11月3日-上午10:41:41</li>
	 *         <li>功能说明：将一行数据转化为分户余额信息</li>
	 *         </p>
	 * @param fileBody
	 * @return
	 */
	private static fab_lnledger_check_seq transferToFabLedgerCheckSeq(String line) {
		
		bizlog.method(" GlFileSustain.transferToFabLedgerCheckSeq begin >>>>>>>>>>>>>>>>");
		
		fab_lnledger_check_seq entity = SysUtil.getInstance(fab_lnledger_check_seq.class);
		
		String[] lineSplits = line.split(FaConst.LOAN_SEPARATION_CHARACTER);
		entity.setTrxn_date(lineSplits[0]);           		// 交易日期
		entity.setAcct_branch(FaConst.LOAN_ACCT_BRANCH);    // 账务机构
		entity.setTrxn_ccy(FaConst.LOAN_TRXN_CCY);          // 交易币种
		entity.setScene_code(lineSplits[1]);           		// 场景编号
		entity.setProduct_code(lineSplits[2]);          	// 产品编号
		entity.setBal_type(lineSplits[3]);           		// 金额类别
		entity.setLoan_term(lineSplits[4]);           		// 贷款期限
		entity.setTrxn_amt(new BigDecimal(lineSplits[5]));  // 发生额
		
		bizlog.method(" GlFileSustain.transferToFabLedgerCheckSeq end <<<<<<<<<<<<<<<<");
		
		return entity;
	}
	
}
