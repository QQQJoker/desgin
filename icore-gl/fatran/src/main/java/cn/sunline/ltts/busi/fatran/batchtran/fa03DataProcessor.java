package cn.sunline.ltts.busi.fatran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApSendFileIn;
import cn.sunline.ltts.busi.fa.namedsql.FaFileDao;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaApFileSend;

/**
 * 文件上传
 */

public class fa03DataProcessor
		extends
		AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.fatran.batchtran.intf.Fa03.Input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fa03.Property, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApSendFileIn> {

	private static final BizLog bizlog = BizLogUtil.getBizLog(fa03DataProcessor.class);

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
	public void process(String jobId, int index, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApSendFileIn dataItem, cn.sunline.ltts.busi.fatran.batchtran.intf.Fa03.Input input,
			cn.sunline.ltts.busi.fatran.batchtran.intf.Fa03.Property property) {

		bizlog.method("ap03DataProcessor  begin processing.....dataItem=[%s]", dataItem);

		// 上传成功, 更新文件发送薄
		if (FaApFileSend.upload(dataItem.getFile_name(), dataItem.getFile_local_path(), dataItem.getFile_server_path(), dataItem.getAppend_ok_ind())) {

			FaApFileSend.modify(dataItem.getFile_id(), E_YESORNO.YES);
		}

		bizlog.method("ap03DataProcessor  end processing.....dataItem=[%s]", dataItem);

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
	public BatchDataWalker<cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo> getBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.intf.Fa03.Input input,
			cn.sunline.ltts.busi.fatran.batchtran.intf.Fa03.Property property) {

		Params parm = new Params();
		parm.add("send_ind", E_YESORNO.NO);

		return new CursorBatchDataWalker<ApDataGroupNo>(FaFileDao.namedsql_lstGroupIdForSend, parm);
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
	public BatchDataWalker<cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApSendFileIn> getJobBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.intf.Fa03.Input input,
			cn.sunline.ltts.busi.fatran.batchtran.intf.Fa03.Property property, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo dataItem) {

		Params parm = new Params();
		parm.add("hash_value", dataItem.getHash_value());
		parm.add("send_ind", E_YESORNO.NO);

		return new CursorBatchDataWalker<ApSendFileIn>(FaFileDao.namedsql_lstFileSendData, parm);
	}

}
