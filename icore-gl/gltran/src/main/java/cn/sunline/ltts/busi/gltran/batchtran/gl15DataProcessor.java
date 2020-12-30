
package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaRegBookDao;
import cn.sunline.ltts.busi.gl.regBook.GlRegBook;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCRUETYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ANALYSISSTATE;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 存款差额计提
 */

public class gl15DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl15.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl15.Property, cn.sunline.ltts.busi.gl.type.ComFaRegBook.FaAccrueBook> {
	
		private static final BizLog bizlog = BizLogUtil.getBizLog(gl15DataProcessor.class);
		
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.gl.type.ComFaRegBook.FaAccrueBook dataItem, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl15.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl15.Property property) {			bizlog.method("gl15 begin>>>>>>>>>>>>>>>");
			bizlog.debug("dataItem[%s]", dataItem);

			GlRegBook.depositAccure(dataItem);

			bizlog.method("gl15 end>>>>>>>>>>>>>>>");
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.gl.type.ComFaRegBook.FaAccrueBook> getBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl15.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl15.Property property) {
		Params para = new Params();
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		para.add("org_id", runEnvs.getCorpno());
		para.add("accrue_date", runEnvs.getTrandt());
		para.add("accrue_type", E_ACCRUETYPE.DEPOSIT_INTEREST_PAYBLE);
		para.add("analysis_state", E_ANALYSISSTATE.AYNASIED);
		
		return new CursorBatchDataWalker<>(FaRegBookDao.namedsql_lstAccureBookGroup, para);
	}

}


