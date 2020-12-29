package cn.sunline.ltts.busi.aplt.impl;

import cn.sunline.adp.cedar.server.batch.engine.sequence.BatchTaskSequence;
import cn.sunline.adp.cedar.base.engine.data.DataArea;

/**
 * 批量交易回调点
 * 
 */
public class BatchTransactionProcessCallBackImplSample extends AbstractBatchTransactionProcessCallBackImpl {

	/**
	 * 实现获取批量任务标识和交易日期的方法
	 */
	@Override
	public BatchTaskSequence initBatchTaskSequence(DataArea dataArea) {
		return super.initBatchTaskSequence(dataArea);
	}

	@Override
	public void afterBizEnvInJob(DataArea dataArea) {
		super.afterBizEnvInJob(dataArea);
	}

	@Override
	public void afterBizEnvInTran(DataArea dataArea) {
		super.afterBizEnvInTran(dataArea);
	}

	@Override
	public void beforeBizEnvInJob(DataArea dataArea) {
		super.beforeBizEnvInJob(dataArea);
	}

	@Override
	public void beforeBizEnvInTran(DataArea dataArea) {
		super.beforeBizEnvInTran(dataArea);
	}

}
