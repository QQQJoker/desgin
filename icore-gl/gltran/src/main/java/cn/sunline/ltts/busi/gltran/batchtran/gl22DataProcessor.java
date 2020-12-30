
package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.edsp.base.lang.*;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.account.FaQueryAccount;
import cn.sunline.ltts.busi.fa.namedsql.FaLnAccountingDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_lnledger_check_resultDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_lnledger_check_result;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctBal;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ANALYSISSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_LEDGERCHECKRESULT;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLnLedgerCheckSeqInfo;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;

/**
 * 总分余额核对(贷款）
 * 
 * @author
 * @Date
 */

public class gl22DataProcessor extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl22.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl22.Property, cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLnLedgerCheckSeqInfo> {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl22DataProcessor.class);
	
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
		public void process(String jobId, int index, cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLnLedgerCheckSeqInfo dataItem, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl22.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl22.Property property) {
			
			// 定义总分核对结果复合类型
			fab_lnledger_check_result entity = SysUtil.getInstance(fab_lnledger_check_result.class);
			CommUtil.copyProperties(entity, dataItem);
			
			// 查询各分户余额
			FaAcctBal glBalance = FaQueryAccount.queryBaseAccountBal(CommToolsAplt.prcRunEnvs().getCorpno(), 
					dataItem.getSys_no(), dataItem.getAcct_branch(), dataItem.getGl_code(), 
					dataItem.getTrxn_ccy(), E_ACCTTYPE.BASE_ACCOUNT);
			
			if ( CommUtil.isNull(glBalance)) {
				glBalance = SysUtil.getInstance(FaAcctBal.class);
				glBalance.setBal_direction(dataItem.getLedger_bal_direction());
				glBalance.setAcct_bal(BigDecimal.ZERO);
				bizlog.method("dataItem.getLedger_bal_direction() [%s] ", dataItem.getLedger_bal_direction());
			}
			
			entity.setGl_bal_direction(glBalance.getBal_direction()); // 总账余额方向
			entity.setGl_acct_bal(glBalance.getAcct_bal()); // 总账账户余额

			// 核对结果
			if ((CommUtil.compare(glBalance.getAcct_bal(), dataItem.getLedger_acct_bal()) == 0
					&& CommUtil.compare(glBalance.getBal_direction(), dataItem.getLedger_bal_direction()) == 0) 
				|| (CommUtil.compare(glBalance.getAcct_bal(), dataItem.getLedger_acct_bal()) == 0
					&& CommUtil.compare(glBalance.getAcct_bal(), BigDecimal.ZERO) == 0 
				||(CommUtil.compare(glBalance.getBal_direction(), dataItem.getLedger_bal_direction()) != 0
					&& CommUtil.compare(glBalance.getAcct_bal().add(dataItem.getLedger_acct_bal()), BigDecimal.ZERO) == 0 ))){
				
				entity.setLedger_check_result(E_LEDGERCHECKRESULT.CHECEK_SUCCESS); // 核对结果成功
			}else {
				
				String ledgerCheckError = FaTools.getLedgerCheckError();;
				bizlog.method("ledgerCheckError [%s] ", ledgerCheckError);
				bizlog.method("gldirection [%s]<>[%s]; glAcct_bal [%s]<>[%s] ", glBalance.getBal_direction(), dataItem.getLedger_bal_direction(), glBalance.getAcct_bal(), dataItem.getLedger_acct_bal());
				// //1报错(缺省)0不报错
				// 日期[%s]总分核对流水核对结果失败记录数[%d]
				if (ledgerCheckError == null || CommUtil.compare(ledgerCheckError, FaConst.LEDGERC_CHECK_ERROR) == 0) {
					throw GlError.GL.E0242(dataItem.getTrxn_date(), 1l, entity.getGl_code());
				}else {
					entity.setLedger_check_result(E_LEDGERCHECKRESULT.CHECEK_FAIL);//核对结果失败
				}
			}

			// 插入账户余额核对信息
			entity.setRecdver(1L);
			Fab_lnledger_check_resultDao.insert(entity);
			
			// 更新贷款分户核对结果
			FaLnAccountingDao.updLnLedgerBalanceStatus(E_ANALYSISSTATE.SUMMARY, dataItem.getTrxn_date(), dataItem.getOrg_id(), dataItem.getSys_no(), dataItem.getAcct_branch(), dataItem.getTrxn_ccy(), dataItem.getGl_code());

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
	public BatchDataWalker<cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLnLedgerCheckSeqInfo> getBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl22.Input input,
			cn.sunline.ltts.busi.gltran.batchtran.intf.Gl22.Property property) {

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		Params para = new Params();

		para.add("org_id", runEnvs.getCorpno());
		para.add("trxn_date", runEnvs.getTrandt());
		para.add("sys_no", FaConst.LOAN_SYSTEM);

		return new CursorBatchDataWalker<FaLnLedgerCheckSeqInfo>(FaLnAccountingDao.namedsql_lstLnLedgerBalance, para);
	}

}
