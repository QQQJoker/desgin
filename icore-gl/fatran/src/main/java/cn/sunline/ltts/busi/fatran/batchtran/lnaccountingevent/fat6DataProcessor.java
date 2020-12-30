
package cn.sunline.ltts.busi.fatran.batchtran.lnaccountingevent;
import cn.sunline.edsp.base.lang.*;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo;
import cn.sunline.ltts.busi.fa.accounting.FaBookAccountingSeq;
import cn.sunline.ltts.busi.fa.namedsql.FaLnAccountingDao;
import cn.sunline.ltts.busi.fa.parm.FaSysService;
import cn.sunline.ltts.busi.fa.scene.accounting.FaBookAccountingSceneSeq;
import cn.sunline.ltts.busi.fatran.batchtran.fat2DataProcessor;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SYSTEMSERVICESTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQSTATE;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
	 /**
	  * 场景事件流水汇总及入账
	  * @author 
	  * @Date 
	  */

public class fat6DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.fatran.batchtran.intf.Fat6.Input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat6.Property, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo> {
	
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(fat6DataProcessor.class);

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
		public void process(String jobId, int index, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo dataItem, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat6.Input input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat6.Property property) {
			
			// 判断核算服务状态
			if ( FaSysService.getSysStatus() != E_SYSTEMSERVICESTATUS.ON ) {
				BIZLOG.method("sys status is close, exit! >>>>>>>>>>>>>>>>>>>>");
				return;
			}
			
			// 生成汇总批次号
			String batchNo = FaBookAccountingSeq.geneTotalBatchNo(FaConst.LN_TOTAL_BATCH_NO);
			
			// 记账
			FaBookAccountingSceneSeq.bookAccountingSceneSeq(dataItem.getHash_value(), batchNo);
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo> getBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.intf.Fat6.Input input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat6.Property property) {
			
			Params parm = new Params();
			parm.add("org_id", CommToolsAplt.prcRunEnvs().getCorpno());
			parm.add("trxn_date", CommToolsAplt.prcRunEnvs().getTrandt());
			parm.add("trxn_seq_status", E_TRXNSEQSTATE.AYNASIED);
			return new CursorBatchDataWalker<ApDataGroupNo>(FaLnAccountingDao.namedsql_lstHashValueFromSceneSeq, parm);
		}

}


