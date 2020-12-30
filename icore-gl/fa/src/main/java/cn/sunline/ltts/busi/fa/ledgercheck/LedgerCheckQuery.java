package cn.sunline.ltts.busi.fa.ledgercheck;

import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaSettleDao;
import cn.sunline.ltts.busi.fa.original.FaQueryOriginal;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLedgerCheckIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLedgerCheckOut;

/**
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author pc
 *         <p>
 *         <li>2017年5月5日-下午1:28:56</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228  pc：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class LedgerCheckQuery {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(FaQueryOriginal.class);
	/**
	 * @Author pc
	 *         <p>
	 *         <li>2017年5月5日-下午1:29:07</li>
	 *         <li>功能说明：总分核对查询</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static Options<FaLedgerCheckOut> QueryLedgerCheckInfo(FaLedgerCheckIn queryIn) {
		
		BIZLOG.method("LedgerCheckQuery.QueryLedgerCheckInfo>>begin>>>>>>>>>>>>>");
		BIZLOG.debug("queryIn[%s]", queryIn);
		
		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		Page<FaLedgerCheckOut> LedgerCheckInfo= FaSettleDao.lstFabLedgerCheck(queryIn.getSys_no(), queryIn.getTrxn_date(), queryIn.getAcct_branch(), queryIn.getCcy_code(), queryIn.getAccounting_subject(), queryIn.getAccounting_alias(), queryIn.getBal_attributes(), queryIn.getGl_code(), queryIn.getAnalysis_state(),
				queryIn.getLedger_check_result(), runEnvs.getCorpno(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		
		runEnvs.setCounts(LedgerCheckInfo.getRecordCount());
		
		Options<FaLedgerCheckOut> output=new DefaultOptions<FaLedgerCheckOut>();
		output.setValues(LedgerCheckInfo.getRecords());
		
		BIZLOG.method("LedgerCheckQuery.QueryLedgerCheckInfo>>end>>>>>>>>>>>>>");
		BIZLOG.debug("output[%s]", output);
		
		return output;
	}

}
