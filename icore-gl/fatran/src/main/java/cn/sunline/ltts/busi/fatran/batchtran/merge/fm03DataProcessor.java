package cn.sunline.ltts.busi.fatran.batchtran.merge;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.gl.fa.namedsql.FaMergeDao;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.fab_merge_detail;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;
	 /**
	  * 获取并账明细并入账
	  *
	  */

public class fm03DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm03.Input, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm03.Property, String> {
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
		public void process(String jobId, int index, String dataItem, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm03.Input input, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm03.Property property) {
			String busi_batch_code = dataItem;
			String trxn_date = CommTools.prcRunEnvs().getTrandt();
			String tellerSeq = null; //总账柜员流水
			String remark = "外部系统并账";
			
			List<fab_merge_detail> lstDetail = FaMergeDao.lstMergeDetailByBatchCode(trxn_date, E_FILEDEALSTATUS.CHECKED, busi_batch_code, false);
			if(CommUtil.isNull(lstDetail)){
				return;
			}
			List<FaSingleAccountingCheckIn> accountingList = new ArrayList<FaSingleAccountingCheckIn>();
			for(fab_merge_detail detail : lstDetail){
				
				BigDecimal total_amt = BigDecimal.ZERO;
				E_DEBITCREDIT debit_credit = null;
				if(CommUtil.compare(detail.getCurrent_debit_amt(), detail.getCurrent_credit_amt()) > 0){
					debit_credit = detail.getCurr_debit_amntcd();
					total_amt = detail.getCurrent_debit_amt().subtract(detail.getCurrent_credit_amt());
				}else if(CommUtil.compare(detail.getCurrent_debit_amt(), detail.getCurrent_credit_amt()) < 0){
					debit_credit = detail.getCurr_credit_amntcd();
					total_amt = detail.getCurrent_credit_amt().subtract(detail.getCurrent_debit_amt());
				}else{
					continue;
				}
				
				
				
				FaSingleAccountingCheckIn accountingSingle = SysUtil.getInstance(FaSingleAccountingCheckIn.class);

				// accountingSingle.setAcct_no(); //账号
				accountingSingle.setSys_no(detail.getSys_no()); // 系统编号
				accountingSingle.setAcct_branch(detail.getBranch_id()); // 账务机构
				accountingSingle.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT); // 账户分类
				//accountingSingle.setAcct_seq(accountingData.getSub_acct_seq()); //账户序号 开基准账户可以不传账户序号
				accountingSingle.setCcy_code(detail.getTrxn_ccy()); // 货币代码
				accountingSingle.setDebit_credit(debit_credit); // 记账方向
				accountingSingle.setAccounting_amt(total_amt); // 记账金额
				// accountingSingle.setSummary_code("GL001"); //摘要代码 // TODO
				accountingSingle.setGl_code(detail.getGl_code()); // 科目号
				accountingSingle.setRemark(remark); // 备注

				accountingList.add(accountingSingle);
				
				
			}
			
			// 重新生成柜员流水
			if(accountingList.size() > 0){
				tellerSeq = FaAccounting.getTellerSeq();
	
				// 登记柜员流水
				FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
				regTellerSeq.setSys_no(lstDetail.get(0).getSys_no()); // 系统编号
				regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
				// regTellerSeq.setTrxn_subject(); //交易主体
				regTellerSeq.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
				regTellerSeq.setRemark(remark); // 备注
				regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
				// regTellerSeq.setSett_batch_no(); //清算批次号
				regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
				// regTellerSeq.setOriginal_trxn_date(); //原日期
				// regTellerSeq.setOriginal_trxn_seq(); //原交易流水
				// regTellerSeq.setCheck_teller(); //复核柜员
				regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水
	
				FaAccounting.regTellerSeq(regTellerSeq);
				
				FaAccounting.bookMultiAccounting(accountingList, tellerSeq, 1L, lstDetail.get(0).getSys_no(), remark, true);
			}
			
			FaMergeDao.updMergeDetailStatus(trxn_date, E_FILEDEALSTATUS.SUCCESS, busi_batch_code, tellerSeq);
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<String> getBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm03.Input input, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm03.Property property) {
			String trxn_date = CommTools.prcRunEnvs().getTrandt();
			
			Params param = new Params();
			param.put("trxn_date", trxn_date);
			param.put("file_handling_status", E_FILEDEALSTATUS.CHECKED);
			
			return new CursorBatchDataWalker<String>(FaMergeDao.namedsql_lstMergeBatchCode, param);
		}

}


