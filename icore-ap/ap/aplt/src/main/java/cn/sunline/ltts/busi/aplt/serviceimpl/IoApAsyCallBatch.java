package cn.sunline.ltts.busi.aplt.serviceimpl;


import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.namedsql.ApSysBatchDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.adp.cedar.base.engine.data.DataArea;


/**
 * 异步调用批量程序服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoApAsyCallBatch", longname = "异步调用批量程序服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoApAsyCallBatch implements
		cn.sunline.ltts.busi.iobus.servicetype.ap.IoApAsyCallBatch {
	/**
	 * 通用异步调用批量程序
	 * @return 
	 * 
	 */
	public String prcAsyCallBtch(
			final cn.sunline.ltts.busi.iobus.servicetype.ap.IoApAsyCallBatch.prcAsyCallBtch.Input input) {
			if(CommUtil.isNull(input.getBttype())){
				throw Aplt.E0000("批量业务类型不能为空");
			}
			
			KnpPara tblknp_para = CommTools.KnpParaQryByCorpno("Batch", "type", input.getBttype().getValue(), "%", false);
			if(CommUtil.isNull(tblknp_para)){
				throw Aplt.E0000("批量业务类型未定义");
			}
			
			DataArea dataArea = DataArea.buildWithEmpty();
			dataArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, CommTools.prcRunEnvs().getTrandt());
			String taskId = BatchUtil.getTaskId();
			String tkgpid = tblknp_para.getPmkey1();
			String tranId =	tblknp_para.getPmkey2();
			
			if(CommUtil.isNull(tkgpid)){
				throw Aplt.E0000("批量交易组ID未定义");
			}
			
			if(CommUtil.isNull(tranId)){
				throw Aplt.E0000("批量交易ID未定义");
			}
			
			
			BatchUtil.submitAndRunBatchTran(taskId, tkgpid, tranId, dataArea);
			
			return taskId;
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件处理状态，请求文件下载后更新(请求文件下载中)
	 * @author douwenbo
	 * @date 2016年6月18日-下午4:54:14
	 * @param taskid
	 */
	@Override
	public void updateFileBeforeDownload(String taskid) {
		ApSysBatchDao.upKapbWjplxxbToPlwenjzt(taskid, E_BTFEST.DOWNING);
		
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件处理状态，请求文件下载后更新(请求文件下载成功)
	 * @author douwenbo
	 * @date 2016年6月20日-上午8:48:58
	 * @param taskid
	 */
	@Override
	public void updateFileAfterDownload(String taskid) {
		ApSysBatchDao.upKapbWjplxxbToPlwenjzt(taskid, E_BTFEST.DOWNSUCC);
		
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件处理状态，请求文件入库前更新(请求文件解析中)
	 * @author douwenbo
	 * @date 2016年6月20日-上午8:49:24
	 * @param taskid
	 */
	@Override
	public void updateFileBeforeParse(String taskid) {
		ApSysBatchDao.upKapbWjplxxbToPlwenjzt(taskid, E_BTFEST.PARSEING);
		
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件处理状态，请求文件入库前更新(请求文件解析完成)
	 * @author douwenbo
	 * @date 2016年6月20日-下午9:51:54
	 * @param taskid
	 */
	@Override
	public void updateFileAfterParse(String taskid) {
		ApSysBatchDao.upKapbWjplxxbToPlwenjzt(taskid, E_BTFEST.PARSESUCC);
		
	}

	/**
	 * 
	 * 功能描述：更新文件主表处理的处理总笔数
	 * @author douwenbo
	 * @date 2016年6月21日-上午9:05:34
	 * @param taskid
	 * @param chulizbs
	 */
	@Override
	public void updateTotlAfterParse(String taskid, Long chulizbs) {
		ApSysBatchDao.upKapbWjplxxbToChulizbs(taskid, chulizbs);
		
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件处理状态，请求文件下载后更新(结果文件生成中)
	 * @author douwenbo
	 * @date 2016年6月21日-上午11:04:42
	 * @param taskid
	 */
	@Override
	public void updateFileBeforeResult(String taskid) {
		ApSysBatchDao.upKapbWjplxxbToPlwenjzt(taskid, E_BTFEST.RESTING);
		
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件处理状态，请求文件下载后更新(结果文件生成成功)
	 * @author douwenbo
	 * @date 2016年6月21日-下午2:38:38
	 * @param taskid
	 */
	@Override
	public void updateFileAfterResult(String taskid) {
		ApSysBatchDao.upKapbWjplxxbToPlwenjzt(taskid, E_BTFEST.RESTSUCC);
		
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件下载路径
	 * @author douwenbo
	 * @date 2016年6月21日-下午3:49:14
	 * @param taskid
	 * @param downpath
	 */
	@Override
	public void updateFileDownPath(String taskid, String downpath) {
		ApSysBatchDao.upKapbWjplxxbToDownpath(taskid, downpath);
		
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件上传路径
	 * @author douwenbo
	 * @date 2016年6月21日-下午3:57:52
	 * @param taskid
	 * @param uppath
	 */
	@Override
	public void updateFileUpPath(String taskid, String uppath) {
		ApSysBatchDao.upKapbWjplxxbToUppath(taskid, uppath);
		
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件处理状态，请求文件下载后更新(业务处理中)
	 * @author douwenbo
	 * @date 2016年6月21日-下午4:11:39
	 * @param taskid
	 */
	@Override
	public void updateFileBeforeBusi(String taskid) {
		ApSysBatchDao.upKapbWjplxxbToPlwenjzt(taskid, E_BTFEST.BUSIING);
		
	}

	/**
	 * 
	 * 功能描述：更新kapb_wjplxxb文件批量信息表批量文件处理状态，请求文件下载后更新(业务处理完成)
	 * @author douwenbo
	 * @date 2016年6月21日-下午4:11:47
	 * @param taskid
	 */
	@Override
	public void updateFileAfterBusi(String taskid) {
		ApSysBatchDao.upKapbWjplxxbToPlwenjzt(taskid, E_BTFEST.BUSISUCC);
		
	}}
