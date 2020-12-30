package cn.sunline.ltts.busi.fatran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo;
import cn.sunline.ltts.busi.fa.accounting.FaBookAccountingSeq;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountingDao;
import cn.sunline.ltts.busi.fa.parm.FaSysService;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SYSTEMSERVICESTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQSTATE;

/**
 * 会计事件流水汇总及入账
 */

public class fat2DataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.fatran.batchtran.intf.Fat2.Input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat2.Property, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo> {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(fat2DataProcessor.class);

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
	public void process(String jobId, int index, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo dataItem, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat2.Input input,
			cn.sunline.ltts.busi.fatran.batchtran.intf.Fat2.Property property) {
		
		if ( FaSysService.getSysStatus() != E_SYSTEMSERVICESTATUS.ON ) {
			
			BIZLOG.method("sys status is close, exit! >>>>>>>>>>>>>>>>>>>>");
			return;
		}
		// FaBookAccountingSeq.bookAccountingSeq(dataItem.getHash_value());
		// 生成汇总批次号
		String batchNo = FaBookAccountingSeq.geneTotalBatchNo();

		//try {
			// 记账
			FaBookAccountingSeq.bookAccountingSeq(dataItem.getHash_value(), batchNo);
		//}
		//catch (Exception e) {
		//	BIZLOG.error(" bookAccountingSeq error >>>>>>>>>>", e);
		//	FaAccountingDao.updFailureStatus(batchNo);
		//}
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
	public BatchDataWalker<cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo> getBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.intf.Fat2.Input input,
			cn.sunline.ltts.busi.fatran.batchtran.intf.Fat2.Property property) {
		Params parm = new Params();
		parm.add("org_id", CommToolsAplt.prcRunEnvs().getCorpno());
		parm.add("trxn_date", CommToolsAplt.prcRunEnvs().getTrandt());
		parm.add("trxn_seq_status", E_TRXNSEQSTATE.AYNASIED);

		return new CursorBatchDataWalker<ApDataGroupNo>(FaAccountingDao.namedsql_lstHashValueFromAccounting, parm);
	}

}
