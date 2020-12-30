package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.account.FaOpenAccount;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountDao;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTSEQSTATE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
	 /**
	  * 专用账户批量开户
	  *
	  */

public class gl75DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.gl.gltran.batchtran.intf.Gl75.Input, cn.sunline.ltts.gl.gltran.batchtran.intf.Gl75.Property, cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctDefineInfo> {
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctDefineInfo dataItem, cn.sunline.ltts.gl.gltran.batchtran.intf.Gl75.Input input, cn.sunline.ltts.gl.gltran.batchtran.intf.Gl75.Property property) {
			//TODO:
			//为空终止方法
			if( CommUtil.isNull(dataItem) ){
				return;
			}
			
			String orgId = CommToolsAplt.prcRunEnvs().getCorpno();
			FaOpenAccount.prcbatchOpen(orgId, dataItem);
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctDefineInfo> getBatchDataWalker(cn.sunline.ltts.gl.gltran.batchtran.intf.Gl75.Input input, cn.sunline.ltts.gl.gltran.batchtran.intf.Gl75.Property property) {
			//TODO:		
			Params para = new Params();
			para.add("org_id", CommToolsAplt.prcRunEnvs().getCorpno());
			para.add("auto_batch_open_ind", E_YESORNO.YES);
			para.add("acct_seq_state", E_ACCTSEQSTATE.NORMAL);
			
			//
			return new CursorBatchDataWalker<cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctDefineInfo>(FaAccountDao.namedsql_lstValidAcct, para);
		}

}


