package cn.sunline.ltts.busi.fatran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.clwj.oss.api.OssFactory;
import cn.sunline.clwj.oss.model.MsFileInfo;
import cn.sunline.clwj.oss.model.MsTransferFileInfo;
import cn.sunline.clwj.oss.spi.MsTransfer;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApRecvFileIn;
import cn.sunline.ltts.busi.fa.namedsql.FaFileDao;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaApFile;
import cn.sunline.ltts.fa.util.FaApFileRecv;

/**
 * 文件接收
 */

public class fa02DataProcessor
		extends
		AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.fatran.batchtran.intf.Fa02.Input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fa02.Property, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApRecvFileIn> {

	private static final BizLog bizlog = BizLogUtil.getBizLog(fa02DataProcessor.class);
	private static final Object lock = new Object();

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(String jobId, int index, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApRecvFileIn dataItem, cn.sunline.ltts.busi.fatran.batchtran.intf.Fa02.Input input,
	        cn.sunline.ltts.busi.fatran.batchtran.intf.Fa02.Property property) {

		bizlog.method("ap02DataProcessor  begin processing.....dataItem=[%s]", dataItem);

		// 获取服务器文件路径
		//String remoteFileName = FaApFile.getFileFullPath(dataItem.getFile_server_path(), dataItem.getFile_name());
		
		// 获取本地文件路径
		//String localFileName = FaApFile.getFileFullPath(dataItem.getFile_local_path(), dataItem.getFile_name());
		
		/*synchronized (lock) {
			File file = new File(localFileName);
			if (file.exists() && file.isFile()) {
				if (bizlog.isDebugEnabled())
					bizlog.debug("To download the file[%s] already exists", localFileName);
				return;
			}
		}*/

		boolean sucessFlag = true;

		try {
			// 返回本地路径
			//FaApFile.download(CommToolsAplt.prcRunEnvs().getTrandt(), localFileName, remoteFileName);
			
			MsTransfer create = OssFactory.get().create("default");
			if (create == null) {
				bizlog.debug("OSS 对象初始化失败！");
			}
			MsTransferFileInfo downFile = new MsTransferFileInfo();
			downFile.setLocalFile(new MsFileInfo(dataItem.getFile_local_path(), dataItem.getFile_name()));
			downFile.setRemoteFile(new MsFileInfo(dataItem.getFile_server_path(), dataItem.getFile_name()));
			
			create.download(downFile);
		}
		catch (Exception e) {
			sucessFlag = false;
			bizlog.error("File Download fail  >>>>>>>>>>>>>>>");
		}

		// 接收成功、更新文件接收登记薄
		if (sucessFlag) {

			FaApFileRecv.modify(dataItem.getFile_id(), E_YESORNO.YES);
		}

		bizlog.method("ap02DataProcessor  end processing.....dataItem=[%s]", dataItem);

	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo> getBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.intf.Fa02.Input input,
	        cn.sunline.ltts.busi.fatran.batchtran.intf.Fa02.Property property) {

		Params parm = new Params();
		parm.add("receive_ind", E_YESORNO.NO);

		return new CursorBatchDataWalker<ApDataGroupNo>(FaFileDao.namedsql_lstGroupIdForRecv, parm);
	}

	/**
	 * 获取作业数据遍历器
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @param dataItem
	 *            批次数据项
	 * @return
	 */
	public BatchDataWalker<cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApRecvFileIn> getJobBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.intf.Fa02.Input input,
	        cn.sunline.ltts.busi.fatran.batchtran.intf.Fa02.Property property, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo dataItem) {

		Params parm = new Params();
		parm.add("hash_value", dataItem.getHash_value());
		parm.add("receive_ind", E_YESORNO.NO);
		parm.add("org_id", CommTools.prcRunEnvs().getCorpno());

		return new CursorBatchDataWalker<ApRecvFileIn>(FaFileDao.namedsql_lstFileReceiveData, parm);
	}
}
