
package cn.sunline.ltts.busi.fatran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApProdAccountingInfo;
import cn.sunline.ltts.busi.fa.namedsql.FaLoanAccountingDao;
import cn.sunline.ltts.busi.fa.parm.FaSysService;
import cn.sunline.ltts.busi.fa.prod.accounting.FaProdBookAccountingSeq;
import cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Input;
import cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Property;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SYSTEMSERVICESTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQSTATE;

	 /**
	  * 产品事件流水汇总及入账
	  * @author 
	  * @Date 
	  */

public class fat4DataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Property, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApProdAccountingInfo> {
	
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
		public void process(String jobId, int index, 
				cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApProdAccountingInfo dataItem, 
				cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Input input, 
				cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Property property) {
			
			// 判断系统状态
			if ( FaSysService.getSysStatus() != E_SYSTEMSERVICESTATUS.ON ) {
				bizlog.method("sys status is close, exit! >>>>>>>>>>>>>>>>>>>>");
				return;
			}
			
			// 获取批次号
			// String batchNo = FaBookAccountingSeq.geneTotalBatchNo();

			// 对单笔流水解析并入账
			FaProdBookAccountingSeq.bookAccountingSeq(dataItem.getTrxn_seq());
			
		}
		
		// 如果处理失败则更新为失败
		@Override
		public void jobExceptionProcess(String taskId, Input input, Property property, String jobId, ApProdAccountingInfo dataItem, Throwable t) {
			bizlog.method(" fat4DataProcessor.jobExceptionProcess begin >>>>>>>>>>>>>>>>");
			// 对单笔流水解析并入账
			FaLoanAccountingDao.updStatusOfProdSeq(dataItem.getTrxn_seq(), E_TRXNSEQSTATE.BOOKFAILED);
			bizlog.method(" fat4DataProcessor.jobExceptionProcess end <<<<<<<<<<<<<<<<");
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo> getBatchDataWalker(
				cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Input input, 
				cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Property property) {
			
			// 查询已解析状态的流水散列值
			Params parm = new Params();
			parm.add("org_id", property.getCorpno());
			parm.add("trxn_date", property.getTrxn_date());
			parm.add("sys_no", property.getSys_no());
			parm.add("trxn_seq_status", E_TRXNSEQSTATE.RECORDED);
			
			return new CursorBatchDataWalker<ApDataGroupNo>(FaLoanAccountingDao.namedsql_lstHashValueFromProdAccounting, parm);
		}
		
		/**
		 * 获取作业数据遍历器
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @param dataItem 批次数据项
		 * @return
		 */
		public BatchDataWalker<cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApProdAccountingInfo> getJobBatchDataWalker(
				cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Input input, 
				cn.sunline.ltts.busi.fatran.batchtran.intf.Fat4.Property property, 
				cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo dataItem) {
			
			// 查询已解析的流水中中散列值中的流水号
			Params parm = new Params();
			parm.add("org_id", property.getCorpno());
			parm.add("trxn_date", property.getTrxn_date());
			parm.add("sys_no", property.getSys_no());
			parm.add("hash_value", dataItem.getHash_value());
			parm.add("trxn_seq_status", E_TRXNSEQSTATE.RECORDED);
			
			return new CursorBatchDataWalker<ApProdAccountingInfo>(FaLoanAccountingDao.namedsql_lstTranSeqFromProdAccounting, parm);
		}
		
		/**
		 * 定位该批量处理数据关键要素：日期，系统编号，法人代码
		 */
		@Override
		public void beforeTranProcess(String taskId, Input input, Property property) {
			
			bizlog.method(" fat4DataProcessor.beforeTranProcess begin >>>>>>>>>>>>>>>>");
			
 			property.setCorpno(CommToolsAplt.prcRunEnvs().getCorpno());			// 法人代码
			property.setTrxn_date(CommToolsAplt.prcRunEnvs().getTrandt());		// 交易日期
			
			// 查询该批量处理数据的系统编号
			KnpPara sysNo = KnpParaDao.selectOne_odb1("ProdAccountingData", "sysNo", "batch", 
					CommToolsAplt.prcRunEnvs().getPrcscd(), CommToolsAplt.prcRunEnvs().getCorpno(), false);
			if(CommUtil.isNull(sysNo)) {
				throw GlError.GL.E0229();
			}
			property.setSys_no(sysNo.getPmval1());
			
			bizlog.method(" fat4DataProcessor.beforeTranProcess end <<<<<<<<<<<<<<<<");
		}
	  

}


