package cn.sunline.ltts.busi.fatran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.fa.namedsql.FaFileDao;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEPROCTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILERECVWAY;
import cn.sunline.ltts.fa.util.FaApFile;
	 /**
	  * 批量同步远程目录文件
	  *
	  */

public class fa04DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.fatran.batchtran.intf.Fa04.Input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fa04.Property, cn.sunline.ltts.busi.fa.tables.TabFaFile.app_batch> {
		private static final BizLog bizlog = BizLogUtil.getBizLog(fa04DataProcessor.class);

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
		public void process(String jobId, int index, cn.sunline.ltts.busi.fa.tables.TabFaFile.app_batch dataItem, cn.sunline.ltts.busi.fatran.batchtran.intf.Fa04.Input input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fa04.Property property) {
					
			try {
				FaApFile.syncRemoteFile2Local(dataItem.getBusi_batch_id());
			} catch (AdpBusinessException e) {
				bizlog.debug("AdpBusinessException e[5s]", e);
				DBTools.rollback();
			}

		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.fa.tables.TabFaFile.app_batch> getBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.intf.Fa04.Input input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fa04.Property property) {
			Params params = new Params();
			params.put("file_proc_type", E_FILEPROCTYPE.RECV_REQUEST);
			params.put("file_recv_way", E_FILERECVWAY.APPOINT_DIURECTORY);
			//RunEnvs runEnvs = BizUtil.getTrxRunEnvs();

			// 设置公共运行变量
			//runEnvs.setTemp_date(runEnvs.getTrxn_date());
			return new CursorBatchDataWalker<>(FaFileDao.namedsql_lstSyscBatchIdList, params);
			 
		}

}


