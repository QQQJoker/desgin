package cn.sunline.ltts.busi.fa.accounting;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountingDao;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctingSeqIn;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctingSeqOut;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author pc
 *         <p>
 *         <li>2018年7月23日-下午17:58:46</li>
 *         <li>创建记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>       
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaAccountSeq {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaAccountSeq.class);

	/**
	 * @Author pc
	 *         <p>
	 *         <li>2018年7月23日-下午6:04:56</li>
	 *         <li>功能说明：总账明细查询</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static Options<FaAcctingSeqOut> queryFabSeq(FaAcctingSeqIn queryIn) {
		bizlog.method("FaAccountSeq.queryFabSeq>>begin>>>>>>>>>>>>>>");
		bizlog.debug("queryIn[%s]", queryIn);
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		Options<FaAcctingSeqOut> output=new DefaultOptions<FaAcctingSeqOut> ();		

	    if(queryIn.getIsQuan()==E_YESORNO.YES){
	    	List<FaAcctingSeqOut> FabAccountSeqList = FaAccountingDao.lstFaacountSeq1(queryIn.getTrxn_date(), queryIn.getTrxn_seq(), queryIn.getAcct_branch(),
					queryIn.getGl_code(), queryIn.getTrxn_ccy(), false);
	    	output.setValues(FabAccountSeqList);
	    	bizlog.debug("output[%s]","输************************"+FabAccountSeqList.size()+"*********************************************************************");
	    }else{
	    	Page<FaAcctingSeqOut> FabAccountSeqList = FaAccountingDao.lstFaacountSeq(queryIn.getTrxn_date(), queryIn.getTrxn_seq(), queryIn.getAcct_branch(),
					queryIn.getGl_code(), queryIn.getTrxn_ccy(), (long)(runEnvs.getPageno() - 1)*runEnvs.getPgsize(), (long)runEnvs.getPgsize(), (long)runEnvs.getCounts(), false);
			runEnvs.setCounts(FabAccountSeqList.getRecordCount());
			output.addAll(FabAccountSeqList.getRecords());

	    }
		
		
		bizlog.debug("output[%s]",output);
		bizlog.method("FaAccountSeq.queryFabSeq>>end>>>>>>>>>>>>>>");
		
		return output;
	}

}
