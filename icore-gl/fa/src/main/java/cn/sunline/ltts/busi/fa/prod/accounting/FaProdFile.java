package cn.sunline.ltts.busi.fa.prod.accounting;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.fa.namedsql.FaLoanAccountingDao;
import cn.sunline.ltts.busi.fa.parm.FaSysService;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_prod_mappingDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_mapping;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_rule;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_prod_seq;
import cn.sunline.ltts.busi.fa.type.ComFaFile.FaFileDown;
import cn.sunline.ltts.busi.fa.type.ComFaFile.FaFileHead;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SYSTEMSERVICESTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQSTATE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.fa.util.FaApBatch;
import cn.sunline.ltts.fa.util.FaApFile;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaProdFile {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(FaProdFile.class);

	/**
	 * 读取产品交易事件文件
	 * @param dataItem
	 */
	public static void doAccountingProdEventFile(FaFileDown faFileDown) {
		
		// 检查总账服务状态
		if (FaSysService.getSysStatus() != E_SYSTEMSERVICESTATUS.ON) {
			bizlog.method("sys status is close, exit! >>>>>>>>>>>>>>>>>>>>");
			bizlog.method("doAccountingProdEventFile end >>>>>>>>>>>>>>>>>>>>");
			return;
		}
		
		// 定位本地路径
		String localFileName = getLocalFileName(faFileDown);
		
		// TODO 测试一下1万行数据，现有方式是否可行，换一种方式：读取第一行，然后用FileUtil.readFile(	file,new FileDataExecutor()){  }
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

			RunEnvs runEnvs = CommToolsAplt.prcRunEnvs(); 	// 公共变量
			String sysNo = faFileDown.getSys_no(); 			// 系统编号
			
			// 异常处理机制：1：直接报错，不往下走  2：更新状态为失败，不影响其他 TODO 需要确认执行策略：失败是否中断
			String analysisError = FaTools.getAnalysisError(); 
			if (CommUtil.isNull(analysisError)) {
				analysisError = FaConst.GL_CODE_ANALYSIS_ERROR;
			}
			
			// 逐条处理文件体
			List<String> fileBodys = lines.subList(1, lines.size());
			bizlog.debug("fileBodys=[%s]", fileBodys);
			
			int countParm = FaTools.getBatchSubmitCount();	// 获取数据库批量提交笔数参数
			ArrayList<fab_accounting_prod_seq> prodSeqList = new ArrayList<fab_accounting_prod_seq>();  // 产品交易流水登记簿
			
			for (String line : fileBodys) {

				// 反序列化赋值
				fab_accounting_prod_seq prodSeq = SysUtil.deserialize(line, fab_accounting_prod_seq.class);
				
				// 可售产品 与 核算产品 映射
				prodSeq.setProduct_code(mappingProd(prodSeq));
				
				// 判断 产品编号、交易编码分录 是否配置
				List<fap_accounting_prod_rule> ruleList = FaLoanAccountingDao.lstFapAccountingProdRule(runEnvs.getCorpno(), 
						runEnvs.getInpucd(), prodSeq.getProduct_code(), prodSeq.getEvent_code(), prodSeq.getAccount_status(), 
						prodSeq.getTrxn_date(), prodSeq.getTrxn_date(), false);
				if(ruleList.isEmpty()) {
					if (CommUtil.equals(analysisError, FaConst.GL_CODE_ANALYSIS_ERROR)) {
						throw ApPubErr.APPUB.E0025(OdbFactory.getTable(fap_accounting_prod_rule.class).getLongname(),
								GlDict.A.sys_no.getId()	, runEnvs.getSystcd(),
								GlDict.A.product_code.getId(), prodSeq.getProduct_code(), 
								GlDict.A.event_code.getId(), prodSeq.getEvent_code());
					} else {
						bizlog.method("not set sysNo=[%s] product_code=[%s] event_code=[%s]", sysNo, prodSeq.getProduct_code(), prodSeq.getEvent_code());
						prodSeq.setTrxn_seq_status(E_TRXNSEQSTATE.AYNASYFAILURE);
					}
					
				}
				
				prodSeq.setSys_no(sysNo);		// 系统编号
				prodSeq.setTrxn_seq_status(E_TRXNSEQSTATE.RECORDED);   // 解析状态已登记
				prodSeq.setData_sort(ruleList.get(0).getData_sort());  // 序号
				prodSeqList.add(prodSeq);
				
				totalCount++;
				totalAmt = totalAmt.add(prodSeq.getTrxn_amt());
				
				// 达到事务提交记录数则先插入
				if (prodSeqList.size() == countParm) {
					insertSeqInfo(prodSeqList, busiBatchCode);
				}	
				
			}
			
			// 插入数量不足事务提交记录数的数据
			if (prodSeqList.size() > 0) {
				insertSeqInfo(prodSeqList, busiBatchCode);
			}
			
			// 校验头体数据、更新状态
			FaFileHead headInfo = SysUtil.deserialize(lines.get(0), FaFileHead.class);
			FaApBatch.setStatusByImport(busiBatchCode, headInfo.getHead_total_count(), headInfo.getHead_total_amt(), totalCount, totalAmt);
			
		} 
	}

	// 可售产品 与 核算产品 映射
	private static String mappingProd(fab_accounting_prod_seq prodSeq) {
		
		bizlog.method(" FaProdFile.mappingProd begin >>>>>>>>>>>>>>>>");
		
		fap_accounting_prod_mapping prodMapping = Fap_accounting_prod_mappingDao.selectOne_odb1(prodSeq.getSys_no(), prodSeq.getProduct_code(), false);
		if(CommUtil.isNull(prodMapping)) {
			throw GlError.GL.E0228(prodSeq.getProduct_code());
		}
		
		bizlog.method(" FaProdFile.mappingProd end <<<<<<<<<<<<<<<<");
		
		return prodMapping.getProduct_code();
	}

	// 插入表数据
	private static void insertSeqInfo(ArrayList<fab_accounting_prod_seq> prodSeqList, String busiBatchCode) {
		
		bizlog.method(" FaProdFile.insertSeqInfo begin >>>>>>>>>>>>>>>>");
		
		try {
			
			// 文件明细信息批量插入表
			DaoUtil.insertBatch(fab_accounting_prod_seq.class, prodSeqList);
			prodSeqList.clear();
			
		} catch (Exception e) {

			DBTools.rollback();
			// 导入明细表异常、更新状态
			FaApBatch.setInsertErrorByImport(busiBatchCode, e.toString());
			return;

		}
		
		bizlog.method(" FaProdFile.insertSeqInfo end <<<<<<<<<<<<<<<<");
		
	}

	// 检查文件头信息
	private static boolean checkHeadInfo(String firstLine, String busiBatchCode) {
		
		bizlog.method(" FaProdFile.checkTotalCount begin >>>>>>>>>>>>>>>>");
		
		boolean toDeal = true;		// 是否需要处理详情
		
		// 总条数为 0 ，直接更新状态
		if(CommUtil.equals("0", firstLine)) {
			toDeal = false;
			FaApBatch.setStatusByImport(busiBatchCode, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO);
		}
		
		// 总条数、总金额、有误，直接更新状态
		FaFileHead headInfo = SysUtil.deserialize(firstLine, FaFileHead.class);
		if (CommUtil.isNull(headInfo.getHead_total_count()) || CommUtil.isNull(headInfo.getHead_total_amt())) {
			toDeal = false;
			FaApBatch.setbatchReqStatus(busiBatchCode, E_FILEDEALSTATUS.FAILCHECK_FORMAT);
		}
		
		bizlog.parm("toDeal[%s]", toDeal);
		bizlog.method(" FaProdFile.checkTotalCount end <<<<<<<<<<<<<<<<");
		
		return toDeal;
		
	}

	// 定位本地
	private static String getLocalFileName(FaFileDown faFileDown) {
		
		// 定位原始路径
		String localFileName = FaApFile.getFullPath(faFileDown.getFile_local_path(), faFileDown.getFile_name());
		
		// 定位远程路径
		if(CommUtil.equals("Y", FaTools.getRemoteDir())) {
			localFileName = FaApFile.getLocalHome(localFileName);
		}
		
		bizlog.debug("localFileName [%s]", localFileName);
		return localFileName;
		
	}
	
}
