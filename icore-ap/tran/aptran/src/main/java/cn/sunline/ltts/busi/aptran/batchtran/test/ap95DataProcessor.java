package cn.sunline.ltts.busi.aptran.batchtran.test;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.adp.cedar.base.logging.BizLog;

	 /**
	  * 内存数据遍历测试批量交易
	  *
	  */

public class ap95DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.aplt.batchtran.intf.Ap95.Input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap95.Property, cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch.ApDCN> {
	private final static BizLog log = LogManager.getBizLog(ap95DataProcessor.class);
	  /**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch.ApDCN dataItem, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap95.Input input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap95.Property property) {
			log.debug("ap95单笔处理逻辑： dcn=" + dataItem.getCdcnno());
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch.ApDCN> getBatchDataWalker(cn.sunline.ltts.busi.aplt.batchtran.intf.Ap95.Input input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap95.Property property) {
			List<IoApBatch.ApDCN> data = new ArrayList<>();
			
			IoApBatch.ApDCN a00 = SysUtil.getInstance(IoApBatch.ApDCN.class);
			a00.setCdcnno("A00");
			data.add(a00);

			IoApBatch.ApDCN a10 = SysUtil.getInstance(IoApBatch.ApDCN.class);
			a10.setCdcnno("A10");
			data.add(a10);
			
			return new ListBatchDataWalker<IoApBatch.ApDCN>(data);
		}

}


