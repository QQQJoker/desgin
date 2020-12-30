
package cn.sunline.ltts.busi.fa.acctingvoch;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaSettleDao;
import cn.sunline.ltts.busi.fa.type.ComFaAcctingVoch.FaQueryAcctingVochIn;
import cn.sunline.ltts.busi.fa.type.ComFaAcctingVoch.FaQueryAcctingVochOut;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author pc
 *         <p>
 *         <li>2017年4月28日-上午9:08:47</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228 pc：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaAcctVoch {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaAcctVoch.class);

	/**
	 * @Author pc
	 *         <p>
	 *         <li>2017年4月28日-上午9:09:10</li>
	 *         <li>功能说明：总账凭证查询</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static Options<FaQueryAcctingVochOut> queryAcctVoch(FaQueryAcctingVochIn queryIn) {		
		bizlog.method("FaAcctVoch.queryAcctVoch>>begin>>>>>>>>>>>>>>>>");
		bizlog.debug("queryIn[%s]", queryIn);
		
		//总账凭证查询
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		Page<FaQueryAcctingVochOut> AcctingVochList=FaSettleDao.lstAcctingVoch(queryIn.getTrxn_date(), 
				queryIn.getTrxn_seq(), queryIn.getSys_no(), queryIn.getAcct_branch(), queryIn.getTrxn_ccy(), 
				queryIn.getGl_code(), queryIn.getAcct_seq(), queryIn.getAcct_no(), queryIn.getDebit_credit(), 
				queryIn.getExchg_method(), queryIn.getOn_bal_sheet_ind(), runEnvs.getCorpno(), 
				(pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		
		runEnvs.setCounts(AcctingVochList.getRecordCount());
		
		Options<FaQueryAcctingVochOut> output = new DefaultOptions<FaQueryAcctingVochOut>();
		output.setValues(AcctingVochList.getRecords());
		
		bizlog.debug("output[%s]", output);
		bizlog.method("FaAcctVoch.queryAcctVoch>>end>>>>>>>>>>>>>>>>");
		return output;
	}

}
