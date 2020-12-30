package cn.sunline.ltts.busi.fa.file;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.fa.parm.FaSysService;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.Apb_batch_requestDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.apb_batch_request;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_event_parmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_event_parm;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_seq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_original_voch;
import cn.sunline.ltts.busi.fa.type.ComFaFile.FaFileDown;
import cn.sunline.ltts.busi.fa.type.ComFaFile.FaFileHead;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SYSTEMSERVICESTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQSTATE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaApBatch;
import cn.sunline.ltts.fa.util.FaApFile;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;

public class FaFile {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(FaFile.class);

	public static void doAccountingEventFile(FaFileDown fileDown) {
		BIZLOG.method("doAccountingEventFile begin >>>>>>>>>>>>>>>>>>>>");

		if (FaSysService.getSysStatus() != E_SYSTEMSERVICESTATUS.ON) {

			BIZLOG.method("sys status is close, exit! >>>>>>>>>>>>>>>>>>>>");
			BIZLOG.method("doAccountingEventFile end >>>>>>>>>>>>>>>>>>>>");
			return;
		}
		// 取出批量请求簿的信息
		apb_batch_request requestData = Apb_batch_requestDao.selectOneWithLock_odb1(fileDown.getBusi_batch_code(), false);

		// 本地地址
		String localFileName = FaApFile.getFileFullPath(fileDown.getFile_local_path(), fileDown.getFile_name());
		
		//系统编号
		String sysNo = fileDown.getSys_no();
		
		/*if("Y".equals(FaTools.getRemoteDir())){
		    localFileName = FaApFile.getLocalHome(localFileName);
		}*/

		BIZLOG.debug("localFileName [%s]", localFileName);
		// 读取文件信息
		List<String> fileDataList = FaApFile.readFile(new File(localFileName));
		BIZLOG.debug("fileDataList.size()=[%s]", fileDataList.size());
		if (fileDataList.size() > 0) {
			
			int totalCount = 0; // 总记录数
			BigDecimal totalAmt = BigDecimal.ZERO; // 总金额
			
			// 若文件内容为0，登记总笔数0
			if(CommUtil.compare(fileDataList.get(0), "0") == 0){
				// 校验头体数据、更新状态
				FaApBatch.setStatusByImport(fileDown.getBusi_batch_code(), 0, BigDecimal.ZERO, totalCount, totalAmt);
			}else{
				// 取文件头信息
				FaFileHead headInfo = SysUtil.deserialize(fileDataList.get(0), FaFileHead.class);

				// 如果文件头格式不符
				if (CommUtil.isNull(headInfo.getHead_total_count()) || CommUtil.isNull(headInfo.getHead_total_amt())) {

					// 更新文件请求登记薄文件处理状态
					FaApBatch.setFormatErrorByImport(fileDown.getBusi_batch_code());

					return;
				}
				BIZLOG.debug("fileHead[%s]", headInfo);

				// 文件体信息
				List<String> fileBodys = fileDataList.subList(1, fileDataList.size());
				BIZLOG.debug("fileBodys=[%s]", fileBodys);
				// 记账流水登记薄
				List<fab_accounting_seq> accountingList = new ArrayList<fab_accounting_seq>();

				// 外系统原始凭证
				List<fab_original_voch> vochList = new ArrayList<fab_original_voch>();

				// 获取数据库批量提交笔数参数
				int countParm = FaTools.getBatchSubmitCount();

				String analysisError = FaTools.getAnalysisError();
				if (CommUtil.isNull(analysisError)) {
					analysisError = FaConst.GL_CODE_ANALYSIS_ERROR;
				}

				for (String fileBody : fileBodys) {

					totalCount++;

					//String sysNo = FaConst.CORE_SYSTEM;

					// 反序列化
					fab_accounting_seq accountingSeq = SysUtil.deserialize(fileBody, fab_accounting_seq.class);

					accountingSeq.setSys_no(sysNo);
					accountingSeq.setTrxn_seq_status(E_TRXNSEQSTATE.AYNASIED);

					// 查询会计核算参数表，获得相应科目
					fap_accounting_event_parm accountingEventParm = Fap_accounting_event_parmDao.selectOne_odb1(accountingSeq.getSys_no(), accountingSeq.getAccounting_alias(),
							accountingSeq.getBal_attributes(), false);

					// 核算参数没有配置：则交易流水状态改为失败
					if (accountingEventParm == null) {
						if (CommUtil.equals(analysisError, FaConst.GL_CODE_ANALYSIS_ERROR)) {
							throw GlError.GL.E0067(sysNo, accountingSeq.getAccounting_alias(), accountingSeq.getBal_attributes());
						} else {
							BIZLOG.method("not set sysNo=[%s] Accounting_alias=[%s] Bal_attributes=[%s]", sysNo, accountingSeq.getAccounting_alias(), accountingSeq.getBal_attributes());
							accountingSeq.setTrxn_seq_status(E_TRXNSEQSTATE.AYNASYFAILURE);
						}
					} else {
						accountingSeq.setGl_code(accountingEventParm.getGl_code());
						accountingSeq.setAcct_no(accountingEventParm.getAcct_no());
						accountingSeq.setSub_acct_seq(accountingEventParm.getAcct_seq());
					}

					totalAmt = totalAmt.add(accountingSeq.getTrxn_amt());
					accountingSeq.setRecdver(1l);
					accountingList.add(accountingSeq);

					// 外系统凭证
					fab_original_voch voch = SysUtil.getInstance(fab_original_voch.class);
					voch.setTrxn_date(accountingSeq.getTrxn_date()); // 交易日期
					voch.setSys_no(accountingSeq.getSys_no()); // 系统编号
					voch.setTrxn_seq(accountingSeq.getTrxn_seq()); // 交易流水
					voch.setData_sort(accountingSeq.getData_sort()); // 数据序号
					voch.setAcct_no(accountingSeq.getAcct_no());
					voch.setSub_acct_seq(accountingSeq.getSub_acct_seq());
					voch.setBusi_seq(accountingSeq.getBusi_seq()); // 业务流水
					voch.setAcct_branch(accountingSeq.getAcct_branch()); // 账务机构
					voch.setTrxn_ccy(accountingSeq.getTrxn_ccy()); // 交易币种
					voch.setGl_code(accountingSeq.getGl_code()); // 科目号
					voch.setDebit_credit(accountingSeq.getDebit_credit()); // 记账方向
					voch.setTrxn_amt(accountingSeq.getTrxn_amt()); // 交易金额
					voch.setCorpno(accountingSeq.getCorpno()); // 法人代码
					voch.setRecdver(1l);
					vochList.add(voch);

					if (accountingList.size() == countParm) {
						try {
							// 文件明细信息批量插入表
							DaoUtil.insertBatch(fab_accounting_seq.class, accountingList);
							accountingList.clear();
							DaoUtil.insertBatch(fab_original_voch.class, vochList);
							vochList.clear();

						} catch (Exception e) {

							DBTools.rollback();
							// 导入明细表异常、更新状态
							FaApBatch.setInsertErrorByImport(requestData.getBusi_batch_code(), e.toString());
							return;

						}
					}

				}
				if (accountingList.size() > 0) {
					try {
						// 文件明细信息批量插入表
						DaoUtil.insertBatch(fab_accounting_seq.class, accountingList);
						accountingList.clear();
						DaoUtil.insertBatch(fab_original_voch.class, vochList);
						vochList.clear();
					} catch (Exception e) {

						DBTools.rollback();
						// 导入明细表异常、更新状态
						FaApBatch.setInsertErrorByImport(requestData.getBusi_batch_code(), e.toString());
						return;

					}
				}
				
				// 校验头体数据、更新状态
				FaApBatch.setStatusByImport(fileDown.getBusi_batch_code(), headInfo.getHead_total_count(), headInfo.getHead_total_amt(), totalCount, totalAmt);
			}

		} else {
			// 更新状态
			FaApBatch.setbatchReqStatus(fileDown.getBusi_batch_code(), E_FILEDEALSTATUS.FAILCHECK_OTHER);
		}

		BIZLOG.method("doAccountingEventFile end >>>>>>>>>>>>>>>>>>>>");
	}

}
