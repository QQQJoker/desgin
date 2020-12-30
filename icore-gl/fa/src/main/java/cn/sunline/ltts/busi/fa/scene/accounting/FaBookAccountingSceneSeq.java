package cn.sunline.ltts.busi.fa.scene.accounting;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApBuffer;
import cn.sunline.ltts.busi.aplt.tools.ApKnpGlbl;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.namedsql.FaLnAccountingDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.Fab_accounting_seq_sumDao;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_seq_sum;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaAccountingData;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;

public class FaBookAccountingSceneSeq {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(FaBookAccountingSceneSeq.class);

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年4月10日-下午8:26:01</li>
	 *         <li>功能说明：场景流水记账</li>
	 *         </p>
	 */
	public static void bookAccountingSceneSeq(Long hashValue, String batchNo) {

		BIZLOG.method("bookAccountingSceneSeq >>>begin>>>>>>>>>>>>");

		// 每一次处理的笔数限制
		int totalCount = Integer.valueOf(ApKnpGlbl.getKnpGlbl(FaConst.BATCH_DO_COUNT, "%").getPmval1());
		
		// 更新状态和批次号
		String trxnDate = CommToolsAplt.prcRunEnvs().getTrandt();
		String orgid = CommToolsAplt.prcRunEnvs().getCorpno();
		FaLnAccountingDao.updTotalBatchNo(batchNo, trxnDate, orgid, (long) totalCount, E_TRXNSEQSTATE.AYNASIED, hashValue);

		// 根据批次号取出数据 FaAccountingData
		List<FaAccountingData> accountingDatas = FaLnAccountingDao.lstAccountingData(trxnDate, batchNo, false);
		if (accountingDatas.size() == 0) {
			BIZLOG.method("bookAccountingSceneSeq >>>end>>>>>>>>>>>> accountingDatas.size = 0");
			return;
		}
		
		// 生成柜员流水
        String tellerSeq = null;

		// 汇总并登记流水汇总簿
		if (accountingDatas.size() > 0) {

			List<FaSingleAccountingCheckIn> accountingList = new ArrayList<FaSingleAccountingCheckIn>();

			for (FaAccountingData accountingData : accountingDatas) {
			    
			    // 生成柜员流水
	            tellerSeq = FaAccounting.getTellerSeq();

	            // 登记柜员流水  
	            FaRegTellerSeq regSeqIn = SysUtil.getInstance(FaRegTellerSeq.class);
	            regSeqIn.setSys_no(accountingData.getSys_no()); // 系统编号
	            regSeqIn.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
	            regSeqIn.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
	            regSeqIn.setRemark("bookAccountingSceneSeq"); // 备注
	            regSeqIn.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
	            // regSeqIn.setSett_batch_no(); //清算批次号
	            regSeqIn.setReversal_status(E_REVERSALSTATE.NOMARL); // 冲账状态
	            regSeqIn.setTrxn_seq(tellerSeq); // 交易流水
	            FaAccounting.regTellerSeq(regSeqIn);

				fab_accounting_seq_sum seqSum = SysUtil.getInstance(fab_accounting_seq_sum.class);

				seqSum.setTrxn_date(trxnDate); // 交易日期
				seqSum.setSys_no(accountingData.getSys_no()); // 系统编号
				seqSum.setAcct_branch(accountingData.getAcct_branch()); // 账务机构
				seqSum.setTrxn_ccy(accountingData.getTrxn_ccy()); // 交易币种
				seqSum.setGl_code(accountingData.getGl_code()); // 科目号
				seqSum.setDebit_credit(accountingData.getDebit_credit()); // 记账方向
				seqSum.setTotal_count(accountingData.getTotal_count()); // 总记录数
				seqSum.setTotal_amt(accountingData.getTotal_amt()); // 总金额
				seqSum.setAccounting_date(trxnDate); // 记账日期
				
				if(CommUtil.isNull(accountingData.getSub_acct_seq())){
				    accountingData.setSub_acct_seq(ApKnpPara.getKnpPara("ACCT_SEQ", "BASE_ACCOUNT").getPmval1());//设置默认序号
				}
				seqSum.setSub_acct_seq(accountingData.getSub_acct_seq());//子序号

				seqSum.setAccounting_seq(tellerSeq); // 记账流水
				seqSum.setTotal_batch_no(batchNo); // 汇总批次号
				seqSum.setCorpno(orgid); // 法人代码
				seqSum.setRecdver(1l);//版本号
				Fab_accounting_seq_sumDao.insert(seqSum);

				FaSingleAccountingCheckIn accountingSingle = SysUtil.getInstance(FaSingleAccountingCheckIn.class);

				// accountingSingle.setAcct_no(); //账号
				accountingSingle.setSys_no(accountingData.getSys_no()); // 系统编号
				accountingSingle.setAcct_branch(accountingData.getAcct_branch()); // 账务机构
				accountingSingle.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT); // 账户分类
				accountingSingle.setAcct_seq(accountingData.getSub_acct_seq()); //账户序号
				accountingSingle.setCcy_code(accountingData.getTrxn_ccy()); // 货币代码
				accountingSingle.setDebit_credit(accountingData.getDebit_credit()); // 记账方向
				accountingSingle.setAccounting_amt(accountingData.getTotal_amt()); // 记账金额
				// accountingSingle.setSummary_code("GL001"); //摘要代码 // TODO
				accountingSingle.setGl_code(accountingData.getGl_code()); // 科目号
				accountingSingle.setRemark("bookLnAccountingSceneSeq"); // 备注

				accountingList.add(accountingSingle);
			}
			
			BIZLOG.debug("accountingList=[%s]", accountingList);
			// 记账复合类型
			FaAccounting.bookMultiAccounting(accountingList, tellerSeq, 1L, accountingDatas.get(0).getSys_no(), "bookAccountingSeq", true);

		}
		
		// 更改场景流水表状态为已处理
		FaLnAccountingDao.updTrxnSeqStatus(trxnDate, batchNo, E_TRXNSEQSTATE.SUMMARY);

		BIZLOG.method("bookAccountingSceneSeq >>>end>>>>>>>>>>>>");
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月2日-下午6:31:54</li>
	 *         <li>功能说明：生成汇总批次号</li>
	 *         </p>
	 * @param ccyInfo
	 * @param subjectInfo
	 * @param submitAcct
	 * @return
	 */
	public static String geneTotalBatchNo() {

		ApBuffer.clear();

		String accountNo = ApSeq.genSeq("TOTAL_BATCH_NO"); // 生成内部账号

		BIZLOG.debug("TOTAL_BATCH_NO=[%s]", accountNo);

		return accountNo;

	}
}
