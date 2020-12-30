package cn.sunline.ltts.busi.gltran.batchtran;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.account.FaQueryAccount;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountingDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_ledger_check_resultDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_ledger_check_result;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctBal;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ANALYSISSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_LEDGERCHECKRESULT;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;

/**
 * 总分余额核对
 */

public class gl20DataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl20.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl20.Property, cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLedgerCheckSeqInfo> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl20DataProcessor.class);
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
	public void process(String jobId, int index, cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLedgerCheckSeqInfo dataItem,
			cn.sunline.ltts.busi.gltran.batchtran.intf.Gl20.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl20.Property property) {
		
		// bizlog.method("gl20 dataItem [%s] ", dataItem);
		// 获取科目号
		//FaAccountingEventInfo subjectInfo = FaAccountingEvent.getAccountingEventInfo(dataItem.getSys_no(), dataItem.getAccounting_alias(), dataItem.getBal_attributes());

		// 汇总数据
		fab_ledger_check_result info = SysUtil.getInstance(fab_ledger_check_result.class);

		info.setSys_no(dataItem.getSys_no()); // 系统编号
		info.setTrxn_date(dataItem.getTrxn_date()); // 交易日期
		info.setAcct_branch(dataItem.getAcct_branch()); // 账务机构
		info.setCcy_code(dataItem.getCcy_code()); // 货币代码
		info.setAccounting_alias(dataItem.getAccounting_alias()); // 核算别名
		info.setBal_attributes(dataItem.getBal_attributes()); // 余额属性
		info.setLedger_bal_direction(dataItem.getLedger_bal_direction()); // 分户余额方向
		info.setLedger_acct_bal(dataItem.getLedger_acct_bal()); // 分户账户余额
		info.setGl_code(dataItem.getGl_code()); // 科目号

		// 从 faa_account 获取总账分账户余额
		//FaAcctInfo glBalance = FaAccountingDao.selGlBalance(ApOrg.getReferenceOrgId(faa_account.class), dataItem.getSys_no(), dataItem.getAcct_branch(), subjectInfo.getGl_code(), dataItem.getCcy_code(), true);
		FaAcctBal glBalance = FaQueryAccount.queryBaseAccountBal(CommToolsAplt.prcRunEnvs().getCorpno(), dataItem.getSys_no(), dataItem.getAcct_branch(), dataItem.getGl_code(), dataItem.getCcy_code(), E_ACCTTYPE.BASE_ACCOUNT);
		
		if ( CommUtil.isNull(glBalance)) {
			glBalance = SysUtil.getInstance(FaAcctBal.class);
			glBalance.setBal_direction(dataItem.getLedger_bal_direction());
			glBalance.setAcct_bal(BigDecimal.ZERO);
			bizlog.method("dataItem.getLedger_bal_direction() [%s] ", dataItem.getLedger_bal_direction());
		}
		info.setGl_bal_direction(glBalance.getBal_direction()); // 总账余额方向
		info.setGl_acct_bal(glBalance.getAcct_bal()); // 总账账户余额

		// 核对结果
		/*if ( (CommUtil.compare(glBalance.getAcct_bal(), dataItem.getLedger_acct_bal()) == 0
				&& CommUtil.compare(glBalance.getBal_direction(), dataItem.getLedger_bal_direction()) == 0) 
			|| (CommUtil.compare(glBalance.getAcct_bal(), dataItem.getLedger_acct_bal()) == 0
				&& CommUtil.compare(glBalance.getAcct_bal(), BigDecimal.ZERO) == 0 )  
			)
		{*/
		if ( (CommUtil.compare(glBalance.getAcct_bal(), dataItem.getLedger_acct_bal()) == 0
				&& CommUtil.compare(glBalance.getBal_direction(), dataItem.getLedger_bal_direction()) == 0) 
			|| (CommUtil.compare(glBalance.getAcct_bal(), dataItem.getLedger_acct_bal()) == 0
				&& CommUtil.compare(glBalance.getAcct_bal(), BigDecimal.ZERO) == 0 
			||(CommUtil.compare(glBalance.getBal_direction(), dataItem.getLedger_bal_direction()) != 0
				&& CommUtil.compare(glBalance.getAcct_bal().add(dataItem.getLedger_acct_bal()), BigDecimal.ZERO) == 0	))  
			)
		{
			info.setLedger_check_result(E_LEDGERCHECKRESULT.CHECEK_SUCCESS); // 核对结果成功
		}
		else {
			
			String ledgerCheckError = FaTools.getLedgerCheckError();;
			bizlog.method("ledgerCheckError [%s] ", ledgerCheckError);
			bizlog.method("gldirection [%s]<>[%s]; glAcct_bal [%s]<>[%s] ", glBalance.getBal_direction(), dataItem.getLedger_bal_direction(), glBalance.getAcct_bal(), dataItem.getLedger_acct_bal());
			// //1报错(缺省)0不报错
			// 日期[%s]总分核对流水核对结果失败记录数[%d]
			if (ledgerCheckError == null || CommUtil.compare(ledgerCheckError, FaConst.LEDGERC_CHECK_ERROR) == 0) {
				throw GlError.GL.E0073(dataItem.getTrxn_date(), 1l);
			}
			else
				info.setLedger_check_result(E_LEDGERCHECKRESULT.CHECEK_FAIL);//核对结果失败
		}

		info.setRecdver(1L);	// 数据版本号
		Fab_ledger_check_resultDao.insert(info);
		
		FaAccountingDao.updLedgerBalanceStatus(E_ANALYSISSTATE.SUMMARY, dataItem.getTrxn_date(), dataItem.getOrg_id(), dataItem.getSys_no(), dataItem.getAcct_branch(), dataItem.getCcy_code(), dataItem.getGl_code(), dataItem.getLedger_bal_direction());

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
	public BatchDataWalker<cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLedgerCheckSeqInfo> getBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl20.Input input,
			cn.sunline.ltts.busi.gltran.batchtran.intf.Gl20.Property property) {
		
	    RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		Params para = new Params();
		para.add("org_id", runEnvs.getCorpno());
		para.add("trxn_date", runEnvs.getTrandt());
		para.add("sys_no", FaConst.CORE_SYSTEM);

		return new CursorBatchDataWalker<cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLedgerCheckSeqInfo>(FaAccountingDao.namedsql_lstLedgerBalance, para);
	}

}
