package cn.sunline.ltts.busi.fa.original;

import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaOriginVochDao;
import cn.sunline.ltts.busi.fa.type.ComFaOriginalVoch.FaQueryOrigVochListIn;
import cn.sunline.ltts.busi.fa.type.ComFaOriginalVoch.FaQueryOriginalVochListOut;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
public class FaQueryOriginal {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(FaQueryOriginal.class);
/**
 * 
 * @Author pc
 *         <p>
 *         <li>2017年4月27日-下午3:53:33</li>
 *         <li>功能说明：外系统原始凭证查询</li>
 *         </p>
 * @param queryIn
 * @return
 */
	public static Options<FaQueryOriginalVochListOut> queryOriginalList(FaQueryOrigVochListIn queryIn) {
		
		bizlog.method("FaQueryOriginal.queryOriginalList>>begin>>>>>>>>>>>");
		bizlog.debug("queryIn[%s]", queryIn);
		
		// 获取公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		Page<FaQueryOriginalVochListOut> tabOriginalInfo = FaOriginVochDao.lstOrigVoch(queryIn.getTrxn_date(), 
				queryIn.getSys_no(), queryIn.getTrxn_seq(), queryIn.getAcct_no(), queryIn.getSub_acct_seq(), 
				queryIn.getBusi_seq(), queryIn.getAcct_branch(), queryIn.getTrxn_ccy(), queryIn.getGl_code(), 
				queryIn.getDebit_credit(), runEnvs.getCorpno(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		
		runEnvs.setCounts(tabOriginalInfo.getRecordCount());
		
		Options<FaQueryOriginalVochListOut> output = new DefaultOptions<FaQueryOriginalVochListOut>();
		output.setValues(tabOriginalInfo.getRecords());
		
		bizlog.debug("output[%s]", output);
		bizlog.method("FaQueryOriginal.queryOriginalList>>end>>>>>>>>>>>");
		
		return output;

	}

}
