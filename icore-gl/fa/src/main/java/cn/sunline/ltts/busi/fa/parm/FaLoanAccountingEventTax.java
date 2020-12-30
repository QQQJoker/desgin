package cn.sunline.ltts.busi.fa.parm;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaLoanAccountingDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_tax_rate;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingTxsInfo;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_VALID_STATUS;

/**
 * 
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author 
 *         <p>
 *         <li>2020年9月15日-下午6:52:50</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2020年9月15日：贷款核算事件中税处理模块</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaLoanAccountingEventTax {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaLoanAccountingEventMnt.class);

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月15日-下午6:58:44</li>
	 *         <li>功能说明：根据税码计算税金</li>
	 *         </p>
	 * @param tran_amount  交易金额
	 * @param tax_code	税码
	 * @param string2 
	 * @param effect_date 
	 * @param trxn_ccy 
	 * @return
	 */
	public static FaLnAccountingTxsInfo calTaxAmount(BigDecimal tran_amount, String tax_code, String effect_date, String invalid_date, String trxn_ccy) {
		
		bizlog.method(" FaLoanAccountingEventTax.calTaxAmount begin >>>>>>>>>>>>>>>>");
		
		// 根据税码查询税率
		String org_id = CommToolsAplt.prcRunEnvs().getCorpno();
		String tranbr = CommToolsAplt.prcRunEnvs().getTranbr();
		fap_tax_rate fapTaxRate = FaLoanAccountingDao.selFapTaxRate(org_id, tranbr, tax_code, effect_date, 
				invalid_date, E_VALID_STATUS.VALID, false);
		if(CommUtil.isNull(fapTaxRate)) {
			throw GlError.GL.E0230(tax_code);
		}
		
		// 计算税金
		BigDecimal taxRate = fapTaxRate.getTax_rate();
		BigDecimal realAmount = tran_amount.divide(
				BigDecimal.ONE.add(taxRate), ApUtil.DEFAULT_DivScale, BigDecimal.ROUND_HALF_UP).multiply(taxRate);
		BigDecimal taxAmount = CommUtil.round(realAmount, 2);		// 两位小数
		
		// 返回营改增明细
		FaLnAccountingTxsInfo txsInfo = SysUtil.getInstance(FaLnAccountingTxsInfo.class);
		txsInfo.setTrxn_ccy(trxn_ccy);				// 币种
		txsInfo.setTax_rate(taxRate);				// 税率
		txsInfo.setTaxin_amount(tran_amount);		// 含税金额
		txsInfo.setTaxout_amount(tran_amount.subtract(taxAmount));	// 不含税金额
		txsInfo.setReal_tax_amount(realAmount);		// 实际应收税额
		txsInfo.setTax_amount(taxAmount);          	// 应收税额
		txsInfo.setBill_type(fapTaxRate.getBill_type());  			// 开票类型
		txsInfo.setGl_code(fapTaxRate.getGl_code());				// 科目
		txsInfo.setTax_code(fapTaxRate.getTax_code());				// 税码
		txsInfo.setTran_event(fapTaxRate.getBusiness_code());		// 交易时间/业务代码
		
		bizlog.debug("txsInfo[%s]", txsInfo);
		bizlog.method(" FaLoanAccountingEventTax.calTaxAmount end <<<<<<<<<<<<<<<<");
		return txsInfo;
		
	}
	
}
