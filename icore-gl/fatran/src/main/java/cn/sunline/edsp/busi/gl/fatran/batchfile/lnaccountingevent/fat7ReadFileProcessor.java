
package cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent;
import java.math.BigDecimal;
import java.util.ArrayList;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.fa.parm.FaSysService;
import cn.sunline.ltts.busi.fa.scene.accounting.FaSceneFile;
import cn.sunline.ltts.busi.fa.servicetype.SrvFaLnAccountingEvent;
import cn.sunline.ltts.busi.fa.servicetype.SrvFaLnAccountingEvent.analysisLnAccountingEvent.Output;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_accounting_scene_fail_seqDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_accounting_scene_seqDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_scene_fail_seq;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_scene_seq;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneInfo;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneResult;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SYSTEMSERVICESTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQSTATE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEAL_STATUS;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.fa.util.FaApBatch;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.edsp.base.lang.*;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.adp.cedar.server.batch.file.ReadFileProcessor;

	 /**
	  * 读取及处理场景事件流水文件
	  * 文件批形式
	  *
	  */
public class fat7ReadFileProcessor extends ReadFileProcessor<
	cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input,
	cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property,
	cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.fat7.Header,
	cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_scene_seq,
	cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.fat7.Foot>{
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(fat7ReadFileProcessor.class);
	
	/**
	  *  获取待处理文件（通常为下载）
	  *  
	  *  @return 文件路径 
	  **/
	public String downloadFile(cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property) {
			//TODO
			return null;
	}

    /**
	  *  解析文件头后转换为对应的javabean对象后，提供处理。
	  *  @parm header 文件头对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *
	  */
	public boolean headerProcess(cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.fat7.Header head ,cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property) {
		
		// 检查总账服务状态
		if (FaSysService.getSysStatus() != E_SYSTEMSERVICESTATUS.ON) {
			bizlog.method("sys status is close, exit! >>>>>>>>>>>>>>>>>>>>");
			throw GlError.GL.E0234();
		}
		
		String busiBatchCode = "";  // TODO 1:文件批量号 2:测试一下如何才能更新状态并不走文件体
		
		// 判断是否为空文件
		if(CommUtil.isNull(head)) {
			FaApBatch.setbatchReqStatus(busiBatchCode, E_FILEDEALSTATUS.FAILCHECK_OTHER);
		}
		
		// 检查文件头
		Long totalCount = head.getHead_total_count();
		BigDecimal totalAmnt = head.getHead_total_amt();
		if(CommUtil.isNull(totalCount) || CommUtil.isNull(totalAmnt)) {
			FaApBatch.setbatchReqStatus(busiBatchCode, E_FILEDEALSTATUS.FAILCHECK_FORMAT);
		}
		
		if(0 == totalCount) {
			FaApBatch.setStatusByImport(busiBatchCode, 0, BigDecimal.ZERO, 0, BigDecimal.ZERO);
		}
		
	 	return false;
	}
	
	 /**
	  *  解析文件体后转换为对应的javabean对象后，提供处理。
	  *  @parm body 文件体对象
	  *  @return 后续是否执行自动入库操作。--该返回值仅在配置了mapping属性后才有效。
	  *  注：该方法将会分发到作业服务器进行并发执行，需要配置交易控制器中的执行模式为"3"
	  */
	public boolean bodyProcess(int index, cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_scene_seq sceneSeq , cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property) {
		
		// 判断文件是否中断或重复处理，数据已经存在
		fab_accounting_scene_seq seq = Fab_accounting_scene_seqDao.selectOne_odb1(FaConst.LOAN_SYSTEM, sceneSeq.getTrxn_seq(), sceneSeq.getData_sort(), false);
		if(CommUtil.isNotNull(seq)) {
			return true;
		}
		
		// 调用解析流水服务 解析+入账 需要提取为服务，将来可能改为消息队列形式 TODO
		Options<FaLnAccountingSceneResult> results = new DefaultOptions<FaLnAccountingSceneResult>();
		try {
			
			FaLnAccountingSceneInfo info = SysUtil.getInstance(FaLnAccountingSceneInfo.class);
			CommUtil.copyProperties(info, sceneSeq);
			ArrayList<FaLnAccountingSceneInfo> infoList = new ArrayList<FaLnAccountingSceneInfo>();
			infoList.add(info);
			Options<FaLnAccountingSceneResult> analysisRsults = SysUtil.getInstance(SrvFaLnAccountingEvent.class)
					.analysisLnAccountingEvent(new DefaultOptions<>(infoList)).getResultList();
			results.addAll(analysisRsults);
			
		}catch (Exception e) {
			
			bizlog.method("not set sysNo=[%s] scene_code=[%s] product_code=[%s] bal_type[%s]", FaConst.LOAN_SYSTEM, 
					sceneSeq.getScene_code(), sceneSeq.getProduct_code(), sceneSeq.getBal_type());
			sceneSeq.setTrxn_seq_status(E_TRXNSEQSTATE.AYNASYFAILURE); // 更改状态
			FaSceneFile.insertFabAccountingSceneFaiSeq(sceneSeq, e);  // 插入失败流水登记簿
			
		}
		
		// 解析场景事件登记外系统原始记账凭证
		if(!results.isEmpty()) {
			FaSceneFile.assemVochList(sceneSeq, results);
		}
		
		// 插入流水表
		sceneSeq.setRecdver(1L);
		sceneSeq.setTrxn_seq_status(E_TRXNSEQSTATE.RECORDED);   // 解析状态已登记
		Fab_accounting_scene_seqDao.insert(sceneSeq);
		
 		return true;
	}
	/**
	 * 解析文件尾后转换为对应的javabean对象后，提供处理。
	 * 
	 * @param foot 文件尾对象
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 后续是否执行自动入库操作。该返回值仅在配置了mapping属性后才有效。
	 */
	public boolean footProcess(cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.fat7.Foot foot ,cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property) {
	 	//TODO
	 	return false;
	}
	
	/**
	 * 读文件交易前处理
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeReadFileTranProcess(String taskId, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property) {}

	/**
	 * 文件头解析异常处理(包括文件的下载、文件的打开、文件头的解析)
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void headerResolveExceptionProcess(String taskId, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	
	/**
	 * 文件体一个批次处理并入库后回调(调用时间与设置的事务提交间隔相关)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param addSuccessCount
	 * @param addErrorCount
	 */
	public void afterBodyResolveCommitProcess(String taskId, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property,
			String jobId, int addSuccessCount, int addErrorCount) {}

	/**
	 * 文件体单行记录解析异常处理器
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param line
	 * @param t
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property,
			String jobId, String line, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 文件体一个批次解析异常处理器
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 * @param t
	 */
	public void bodyResolveExceptionProcess(String taskId, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount, Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 读文件交易处理结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterReadFileTranProcess(String taskId, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property) {}

	/**
	 * 文件体一个批次解析结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param jobId
	 * @param totalSuccessCount
	 * @param totalErrorCount
	 */
	public void afterBodyResolveProcess(String taskId, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property,
			String jobId, int totalSuccessCount, int totalErrorCount) {}

	/**
	 * 读文件交易处理异常后回调(交易的所有异常都会进入到该方法，包括之前已处理过并继续抛出的异常)
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void readFileTranExceptionProcess(String taskId, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Input input, cn.sunline.edsp.busi.gl.fatran.batchfile.lnaccountingevent.intf.Fat7.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
}

