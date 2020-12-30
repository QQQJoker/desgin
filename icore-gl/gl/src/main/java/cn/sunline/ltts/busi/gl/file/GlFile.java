package cn.sunline.ltts.busi.gl.file;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.Apb_batch_requestDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.apb_batch_request;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_event_parmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_sys_defineDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_event_parm;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_sys_define;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accrue;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_ledger_check_seq;
import cn.sunline.ltts.busi.gl.namedsql.GlFileDao;
import cn.sunline.ltts.busi.gl.regBook.GlRegBook;
import cn.sunline.ltts.busi.gl.type.GlFile.GlCheckRecord;
import cn.sunline.ltts.busi.gl.type.GlFile.GlFileHead;
import cn.sunline.ltts.busi.gl.type.GlFile.GlFileLaodCom;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ANALYSISSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_BALPROP;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_BATCHTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaApBatch;
import cn.sunline.ltts.fa.util.FaApFile;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlFile {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlFile.class);

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年4月10日-上午11:15:30</li>
	 *         <li>功能说明：导入计提数据文件，解析到数据库表中，日终步骤</li>
	 *         </p>
	 */
	public static void doAccureFile() {

		List<GlFileLaodCom> fileLoads = new ArrayList<GlFileLaodCom>();

		// 从系统杂项表中取出等待时间, 单位为秒 10
		int waitTime = FaTools.getWaitTime();

		int sleepTime = 0;
		//KnpPara para = ApKnpPara.getKnpPara("system.dcn", "dcn",true);
		KnpPara para = ApKnpPara.getKnpPara("system.dcn", "dcn", "ACCURE");
		int fileCnt = Integer.parseInt(para.getPmval1());
		do {

			// 从平台登记的表中取出已下载文件的信息
			fileLoads = GlFileDao.lstFileLoanDown(CommToolsAplt.prcRunEnvs().getCorpno(), E_BATCHTYPE.ACCRUE_DOWN, E_FILEDEALSTATUS.UNCHECK, E_YESORNO.YES, false);
			
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
				throw GlError.GL.E0068();
			}

		}
		while (true);

		if (fileLoads.size() != fileCnt) {
			throw GlError.GL.E0108(E_BATCHTYPE.ACCRUE_DOWN.getLongName());
		}

		String analysisError = FaTools.getAnalysisError();
		if (CommUtil.isNull(analysisError))
			analysisError = FaConst.GL_CODE_ANALYSIS_ERROR;
		
		long vochRecordNo = 0L;
		// 读取文件，解析到数据库,每天只有一个计提文件传入
		for (GlFileLaodCom fileLoad : fileLoads) {
		    
		    //系统编号
	        String sysNo = fileLoad.getSys_no();

			// 取出批量请求簿的信息
			//apb_batch_request requestData = Apb_batch_requestDao.selectOneWithLock_odb1(fileLoad.getBusi_batch_code(), false);

			String localFileName = FaApFile.getFileFullPath(fileLoad.getFile_local_path(), fileLoad.getFile_name());

			// 本地地址
		//	String localFileName = ApFile.getFileFullPath(fileDown.getFile_local_path(), fileDown.getFile_name());
			
			if("Y".equals(FaTools.getRemoteDir())){
			    localFileName = FaApFile.getLocalHome(localFileName);
			}
			
			// 读取文件信息
			List<String> fileDataList = FaApFile.readFile(new File(localFileName));

//			long vochRecordNo = 0L;

			if (CommUtil.isNotNull(fileDataList) && fileDataList.size() > 0) {

				// 取文件头信息
				GlFileHead headInfo = SysUtil.deserialize(fileDataList.get(0), GlFileHead.class);

				// 如果文件头格式不符
				if (CommUtil.isNull(headInfo.getHead_total_count()) || CommUtil.isNull(headInfo.getHead_total_amt())) {
					// 更新文件请求登记薄文件处理状态
					FaApBatch.setFormatErrorByImport(fileLoad.getBusi_batch_code());
					return;
				}

				//String sysNo = FaConst.CORE_SYSTEM; // 只取核心系统编号

				// 文件体信息
				List<String> fileBodys = fileDataList.subList(1, fileDataList.size());
				List<fab_accrue> accureDataList = new ArrayList<fab_accrue>();

				String dcn = getDcnByFileName(fileLoad.getFile_name());
				
				// 文件体信息处理
				for (String fileBody : fileBodys) {

					fab_accrue accureData = SysUtil.deserialize(fileBody, fab_accrue.class);

					accureData.setSys_no(sysNo); // 系统编号
					accureData.setAccrue_date(CommToolsAplt.prcRunEnvs().getTrandt()); // 计提日期
					accureData.setRecord_no(CommTools.getCurrentThreadSeq(fab_accrue.class,"fab_accrue")); // 记录号

					accureData.setAnalysis_state(E_ANALYSISSTATE.AYNASIED);
					// 查询会计核算参数表，获得相应科目
					fap_accounting_event_parm accountingEventParm = Fap_accounting_event_parmDao.selectOne_odb1(accureData.getSys_no(), accureData.getAccounting_alias(),
							accureData.getBal_attributes(), false);

					if (accountingEventParm == null) {
						//if (CommUtil.equals(analysisError, FaConst.GL_CODE_ANALYSIS_ERROR))
							throw GlError.GL.E0067(sysNo, accureData.getAccounting_alias(), accureData.getBal_attributes());
						//else
						//	accureData.setAnalysis_state(E_ANALYSISSTATE.FAILURE);
					}
					else {
						// 取计提科目
						accureData.setAccrue_gl_code(accountingEventParm.getGl_code()); // 计提科目
						accureData.setOffset_gl_code(accountingEventParm.getOffset_gl_code()); // 对手科目
					}
					accureData.setAcct_seq(accountingEventParm.getAcct_seq());//账户序号
					
					vochRecordNo++;
					// 登记记外系统凭证
					GlRegBook.regOriginalaVochFromAccrue(accureData, vochRecordNo, headInfo.getHead_total_count());   
					accureData.setCdcnno(dcn); //添加dcn号，区分计提数据来源；按文件核对头体数据时用到；
					accureData.setRecdver(1L); // 版本号
					accureDataList.add(accureData);

					if (accureDataList.size() == 50) {
						//try {
							// 文件明细信息批量插入表
							DaoUtil.insertBatch(fab_accrue.class, accureDataList);
							accureDataList.clear();
						//}
						//catch (Exception e) {
						//	DBUtil.rollBack();
							// 导入明细表异常、更新状态
						//	ApBatch.setInsertErrorByImport(requestData.getBusi_batch_code());
						//	return;
						//}
					}
				}

				// 存有数据
				if (CommUtil.isNotNull(accureDataList) && accureDataList.size() > 0) {
					//try {
						// 文件明细信息批量插入表
						DaoUtil.insertBatch(fab_accrue.class, accureDataList);
						accureDataList.clear();
					//}
					//catch (Exception e) {
					//	DBUtil.rollBack();
						// 导入明细表异常、更新状态
					//	ApBatch.setInsertErrorByImport(requestData.getBusi_batch_code());
					//	return;
					//}

				}
				// 获取文件汇总的文件头信息
				GlFileHead fileHeadInfo = GlFileDao.selFileHeadInfoFromAccure(CommToolsAplt.prcRunEnvs().getCorpno(), CommToolsAplt.prcRunEnvs().getTrandt(),dcn, false);

				// 校验头体数据、更新状态
				boolean status = FaApBatch.returnStatusByImport(fileLoad.getBusi_batch_code(), headInfo.getHead_total_count(), headInfo.getHead_total_amt(),
						fileHeadInfo.getHead_total_count(), fileHeadInfo.getHead_total_amt());

				if (status == false) {
				    throw GlError.GL.E0070(headInfo.getHead_total_count(), fileHeadInfo.getHead_total_count(), headInfo.getHead_total_amt(), fileHeadInfo.getHead_total_amt());
				}
			}else{
				throw GlError.GL.E0069(fileLoad.getFile_name());
			}
			CommTools.genNewSerail();//rambo add
	}
}
	/**
	 * 根据文件名获取dcn号
	 * @param fileName
	 * @return
	 */
	public static String getDcnByFileName(String fileName) {
		int index = fileName.lastIndexOf(".");
		return fileName.substring(index - 3, index);
	}
	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年4月10日-上午11:16:12</li>
	 *         <li>功能说明：导入分户账余额文件，解析到数据库表中(日终步骤)</li>
	 *         </p>
	 */
	public static void doLedgerBalFile() {

		List<GlFileLaodCom> fileLoads = new ArrayList<GlFileLaodCom>();

		// 从系统杂项表中取出等待时间, 单位为秒 10
		int waitTime = FaTools.getWaitTime();

		int sleepTime = 0;
		//KnpPara para = ApKnpPara.getKnpPara("system.dcn", "dcn",true);
		KnpPara para = ApKnpPara.getKnpPara("system.dcn", "dcn", "LEDGERBAL");
		int fileCnt = Integer.parseInt(para.getPmval1());
		do {

			// 从平台登记的表中取出已下载文件的信息
			fileLoads = GlFileDao.lstFileLoanDown(CommToolsAplt.prcRunEnvs().getCorpno(), E_BATCHTYPE.LEDGER_DOWN, E_FILEDEALSTATUS.UNCHECK, E_YESORNO.YES, false);

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
			throw GlError.GL.E0108(E_BATCHTYPE.LEDGER_DOWN.getLongName());

		}

		String analysisError = FaTools.getAnalysisError();
		if (CommUtil.isNull(analysisError)) {
			analysisError = FaConst.GL_CODE_ANALYSIS_ERROR;
		}
		// 读取文件，解析到数据库
		for (GlFileLaodCom fileLoad : fileLoads) {

			// 取出批量请求簿的信息
			//apb_batch_request requestData = Apb_batch_requestDao.selectOneWithLock_odb1(fileLoad.getBusi_batch_code(), false);

			// 文件本地路径
			String localFileName = FaApFile.getFileFullPath(fileLoad.getFile_local_path(), fileLoad.getFile_name());

			if("Y".equals(FaTools.getRemoteDir())){
			    localFileName = FaApFile.getLocalHome(localFileName);
			}
			
			// 读取文件信息
			List<String> fileDataList = FaApFile.readFile(new File(localFileName));
			if (CommUtil.isNotNull(fileDataList) && fileDataList.size() > 0) {
				// 取文件头信息
				GlFileHead headInfo = SysUtil.deserialize(fileDataList.get(0), GlFileHead.class);

				// 如果文件头格式不符
				if (CommUtil.isNull(headInfo.getHead_total_count()) || CommUtil.isNull(headInfo.getHead_total_amt())) {
					// 更新文件请求登记薄文件处理状态
					FaApBatch.setFormatErrorByImport(fileLoad.getBusi_batch_code());
					throw GlError.GL.E0113();
					
				}
				BIZLOG.debug("fileHead[%s]", headInfo);

				// 开始处理文件体信息
				//String sysNo = FaConst.CORE_SYSTEM; // 暂只取核心系统编号
				String sysNo = fileLoad.getSys_no();
				//dcn号
				String dcn = getDcnByFileName(fileLoad.getFile_name());
				// 文件体信息
				List<String> fileBodys = fileDataList.subList(1, fileDataList.size());


				// 文件体数据存在
				List<fab_ledger_check_seq> ledgerDataList = new ArrayList<fab_ledger_check_seq>();
				for (String fileBody : fileBodys) {

					// 反序列化
					fab_ledger_check_seq lederData = SysUtil.deserialize(fileBody, fab_ledger_check_seq.class);

					lederData.setSys_no(sysNo); // 系统编号
					lederData.setRecord_no(CommTools.getCurrentThreadSeq(fab_ledger_check_seq.class, "fab_ledger_check_seq")); // 记录号

					lederData.setAnalysis_state(E_ANALYSISSTATE.AYNASIED);
					
					lederData.setCdcnno(dcn);
					// 解析科目
					fap_accounting_event_parm accountingEventParm = Fap_accounting_event_parmDao.selectOne_odb1(lederData.getSys_no(), lederData.getAccounting_alias(),
							lederData.getBal_attributes(), false);

					if (accountingEventParm == null) {
							throw GlError.GL.E0067(sysNo, lederData.getAccounting_alias(), lederData.getBal_attributes());
					} else {
						lederData.setGl_code(accountingEventParm.getGl_code());
						// 查询科目
						fap_accounting_subject subjectInfo = Fap_accounting_subjectDao.selectOne_odb1(accountingEventParm.getGl_code(), false);
						if(CommUtil.isNull(subjectInfo)){
						    ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getLongName(), accountingEventParm.getGl_code());
						}
						
						if (CommUtil.isNull(subjectInfo.getGl_code()))
							ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getLongName(), subjectInfo.getGl_code());

						// 如果是轧差或者双方科目，且余额方向为空，则报错
						
						if (CommUtil.isNull(lederData.getBal_type())) {
							if (subjectInfo.getBal_prop() == E_BALPROP.NETTING || subjectInfo.getBal_prop() == E_BALPROP.BOTH_SIDES)
							throw GlError.GL.E0071(accountingEventParm.getGl_code());	
						// // 余额性质为借方，则余额方向为借 若没有上送,则取科目余额方赂
						

							if (subjectInfo.getBal_prop() == E_BALPROP.DEBIT) {
								lederData.setBal_type(E_DEBITCREDIT.DEBIT);
							} else {
								lederData.setBal_type(E_DEBITCREDIT.CREDIT);
							}
						}
					}
					
					lederData.setRecdver(1L);		// 数据版本号
					ledgerDataList.add(lederData);
					if (ledgerDataList.size() == 50) {

						//try {
							BIZLOG.debug("ledgerDataList[%s]", ledgerDataList);
							// 文件明细信息批量插入表
							DaoUtil.insertBatch(fab_ledger_check_seq.class, ledgerDataList);
							ledgerDataList.clear();
						//}
						//catch (Exception e) {
							
						//	e.printStackTrace();
						//	DBUtil.rollBack();
							// 导入明细表异常、更新状态
						//	ApBatch.setInsertErrorByImport(requestData.getBusi_batch_code());
						//	return;

						//}

					}

				}

				// 存有数据
				if (CommUtil.isNotNull(ledgerDataList) && ledgerDataList.size() > 0) {
					//try {
						
						BIZLOG.debug("ledgerDataList[%s]", ledgerDataList);
						// 文件明细信息批量插入表
						DaoUtil.insertBatch(fab_ledger_check_seq.class, ledgerDataList);
						ledgerDataList.clear();

					//}
					//catch (Exception e) {
					//	e.printStackTrace();
					//	DBUtil.rollBack();
						// 导入明细表异常、更新状态
					//	ApBatch.setInsertErrorByImport(requestData.getBusi_batch_code());
					//	return;
					//}
				}
				// 获取文件汇总的文件头信息
				GlFileHead fileHeadInfo = GlFileDao.selFileHeadInfoFromLedger(CommToolsAplt.prcRunEnvs().getCorpno(), CommToolsAplt.prcRunEnvs().getTrandt(),
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
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年4月17日-下午4:34:44</li>
	 *         <li>功能说明：对账文件导入</li>
	 *         </p>
	 */
	public static void doCheckAccountingFile() {

		BIZLOG.method("doCheckAccountingFile begin>>>>>>>>>");

		String trxnDate = CommToolsAplt.prcRunEnvs().getTrandt();

		// 文件记录信息
		List<GlFileLaodCom> fileLoads = new ArrayList<GlFileLaodCom>();

		// 从平台登记的表中取出已下载文件的信息
		fileLoads = GlFileDao.lstFileLoanDown(CommToolsAplt.prcRunEnvs().getCorpno(), E_BATCHTYPE.CHECK_DOWN, E_FILEDEALSTATUS.UNCHECK, E_YESORNO.YES, false);
		
		//KnpPara para = ApKnpPara.getKnpPara("system.dcn", "dcn",true);
		KnpPara para = ApKnpPara.getKnpPara("system.dcn", "dcn", "CHECKRECORD");
		int fileCnt = Integer.parseInt(para.getPmval1());
		// 每天只有一个对账文件传入
		if (fileLoads.size() == fileCnt) {//fileLoads暂时只有核心系统（接入其他系统，需要按系统对账）
			List<GlCheckRecord> fileCheckRecordList = new ArrayList<GlCheckRecord>(); // 文件信息	
			// 读取文件，解析到数据库
			for (GlFileLaodCom fileLoad : fileLoads) {

				// 取出批量请求簿的信息
//				apb_batch_request requestData = Apb_batch_requestDao.selectOneWithLock_odb1(fileLoad.getBusi_batch_code(), false);

				// 文件本地路径
				String localFileName = FaApFile.getFileFullPath(fileLoad.getFile_local_path(), fileLoad.getFile_name());

				// 文件本地路径
			//	String localFileName = ApFile.getFileFullPath(fileLoad.getFile_local_path(), fileLoad.getFile_name());

				if("Y".equals(FaTools.getRemoteDir())){
				    localFileName = FaApFile.getLocalHome(localFileName);
				}
				
				
				BIZLOG.debug("localFileName [%s]", localFileName);
				// 读取文件信息，一行为一个String
				List<String> fileDataList = FaApFile.readFile(new File(localFileName));

//				List<GlCheckRecord> fileCheckRecordList = new ArrayList<GlCheckRecord>(); // 文件信息		
				
				if (CommUtil.isNotNull(fileDataList) && fileDataList.size() > 0) {

					// 文件体数据存在
					for (String fileBody : fileDataList) {
						// 对账文件为空，退出
						if(CommUtil.compare(fileBody, "{}") == 0){
							continue;
						}
						// 反序列化
						GlCheckRecord lederData = SysUtil.deserialize(fileBody, GlCheckRecord.class);
						BIZLOG.debug("lederData[%s]", lederData);
						
						fileCheckRecordList.add(lederData);

					}
				}
				
			}
			fileCheckRecordList = getGlCheckRecordSum(fileCheckRecordList);//将各节点数据汇总
			// 计算落地数据
			List<GlCheckRecord> glCheckRecordedList = new ArrayList<GlCheckRecord>();

			// 系统使用核心系统
			String sysNo = FaConst.CORE_SYSTEM;
//			String sysNo = fileLoad.getSys_no();
			
			
			List<GlCheckRecord> accountingDataAccure = GlFileDao.lstCheckDataFromAccountingSeq(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, sysNo, false);
			// 会计事件的落地数据
			if (CommUtil.isNotNull(accountingDataAccure) && accountingDataAccure.size() > 0) {
				glCheckRecordedList.addAll(accountingDataAccure);
			}
			
			BIZLOG.debug("glCheckRecordedList[%s]", glCheckRecordedList);
			// 计提的落地数据
			List<GlCheckRecord> checkDataAccure = GlFileDao.lstCheckDataFromAccure(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, sysNo, false);
			if (CommUtil.isNotNull(checkDataAccure) && checkDataAccure.size() > 0) {
				glCheckRecordedList.addAll(checkDataAccure);
			}	
			BIZLOG.debug("checkDataAccure[%s]", checkDataAccure);

			// 分户账余额的落地数据
			List<GlCheckRecord> checkDataLedger = GlFileDao.lstCheckDataFromLeger(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, sysNo, false);
			if (CommUtil.isNotNull(checkDataLedger) && checkDataLedger.size() > 0) {
				glCheckRecordedList.addAll(checkDataLedger);
			}
			BIZLOG.debug("checkDataLedger[%s]", checkDataLedger);

			// 比较文件记录的数据和落地数据是否一致
			// 判断大小是否相等
			BIZLOG.debug("fileCheckRecordList[%s]", fileCheckRecordList);
			BIZLOG.debug("glCheckRecordedList[%s]", glCheckRecordedList);
			if (fileCheckRecordList.size() != glCheckRecordedList.size()) {

				throw GlError.GL.E0102();

			}

			// 转换成以交易日期、文件类型和会计主体为key的map
			Map<String, GlCheckRecord> fileDatas = toMap(fileCheckRecordList);
			Map<String, GlCheckRecord> glDatas = toMap(glCheckRecordedList);

			BIZLOG.method("fileDatas[%s]", fileDatas);
			BIZLOG.method("glDatas[%s]", glDatas);
			// 以文件内容为基准，根据key值进行判断
			for (String key : fileDatas.keySet()) {
				
				GlCheckRecord fileData = fileDatas.get(key);
				BIZLOG.method("key=%s, fileData.getRecord_number()=%s, fileData.getTotal_amt()=%s", key, fileData.getRecord_number(), fileData.getTotal_amt());
				
				GlCheckRecord glData = glDatas.get(key);
				if (CommUtil.isNull(glData)) {
					BIZLOG.method("key=[%s]", key);
					throw GlError.GL.E0102();
				}

				// 取出类里的数据，并进行比较
				long glRecordNum = glData.getRecord_number();
				long fileRecordNum = fileData.getRecord_number();
				if (CommUtil.compare(glRecordNum, fileRecordNum) != 0) {
					BIZLOG.method("glRecordNum=[%s]<>fileRecordNum=[%s]", glRecordNum, fileRecordNum);
					throw GlError.GL.E0102();
				}
				BigDecimal glAmt = glData.getTotal_amt();
				BigDecimal fileAmt = fileData.getTotal_amt();

				if (CommUtil.compare(glAmt, fileAmt) != 0) {
					BIZLOG.method("glAmt=[%s]<>fileAmt=[%s]", glAmt, fileAmt);
					throw GlError.GL.E0102();
				}
			}
			for (GlFileLaodCom fileLoad : fileLoads) {
				// 取出批量请求簿的信息
				apb_batch_request requestData = Apb_batch_requestDao.selectOneWithLock_odb1(fileLoad.getBusi_batch_code(), false);
				FaApBatch.setbatchReqStatus(requestData.getBusi_batch_code(), E_FILEDEALSTATUS.SUCCESS);
			}
			fap_sys_define tabSysDefine = Fap_sys_defineDao.selectOne_odb1(sysNo, true);
			tabSysDefine.setSystem_date(CommToolsAplt.prcRunEnvs().getNxtrdt());
		} /*else if (fileLoads.size() > 1) {
			// 对账文件应只有一个
			throw GlError.GL.E0108(E_BATCHTYPE.CHECK_DOWN.getLongName());
		}*/ else {
		    throw GlError.GL.E0115(E_BATCHTYPE.CHECK_DOWN.getLongName());
		}

	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年5月3日-下午1:30:21</li>
	 *         <li>功能说明：转换成主键拼成的key的map对象</li>
	 *         </p>
	 * @param CheckRecordList
	 */
	private static Map<String, GlCheckRecord> toMap(List<GlCheckRecord> checkRecordList) {

		Map<String, GlCheckRecord> checkMap = new HashMap<String, GlCheckRecord>();

		for (GlCheckRecord CheckRecord : checkRecordList) {

			StringBuilder key = new StringBuilder(CheckRecord.getTrxn_date());
			key.append(CheckRecord.getFile_type());
			key.append(CheckRecord.getAccounting_subject());

			checkMap.put(key.toString(), CheckRecord);
		}
		return checkMap;
	}

	public static List<GlCheckRecord> getGlCheckRecordSum(List<GlCheckRecord> list) {
		List<GlCheckRecord> result = new ArrayList<GlCheckRecord>();
		Map<String, GlCheckRecord> checkMap = new HashMap<String, GlCheckRecord>();
		for(GlCheckRecord entity : list) {
			StringBuilder key = new StringBuilder(entity.getTrxn_date());
			key.append(entity.getFile_type());
			key.append(entity.getAccounting_subject());

			GlCheckRecord glCheckRecord = checkMap.get(key.toString());
			if(glCheckRecord != null) {
				glCheckRecord.setRecord_number(glCheckRecord.getRecord_number() + entity.getRecord_number());
				glCheckRecord.setTotal_amt(glCheckRecord.getTotal_amt().add(entity.getTotal_amt()));
			} else {
				checkMap.put(key.toString(), entity);
			}
		}
		for(GlCheckRecord entity:checkMap.values()) {
			result.add(entity);
		}
		return result;
	}
	
}
