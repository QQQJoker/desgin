package cn.sunline.ltts.busi.fa.fabaccure;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountingDao;
import cn.sunline.ltts.busi.fa.original.FaQueryOriginal;
import cn.sunline.ltts.busi.fa.type.ComFabAccrue.FabAccureQueryIn;
import cn.sunline.ltts.busi.fa.type.ComFabAccrue.FabAccureQueryOut;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author pc
 *         <p>
 *         <li>2017年4月28日-下午1:04:46</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228 pc：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FabAccure {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaQueryOriginal.class);

	/**
	 * @Author pc
	 *         <p>
	 *         <li>2017年4月28日-下午1:04:56</li>
	 *         <li>功能说明：计提登记簿查询</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static Options<FabAccureQueryOut> queryFabAccu(FabAccureQueryIn queryIn) {
		bizlog.method("FabAccure.queryFabAccu>>begin>>>>>>>>>>>>>>");
		bizlog.debug("queryIn[%s]", queryIn);
		
		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		Page<FabAccureQueryOut> FabAccureList = FaAccountingDao.lstFabAccureInfo(queryIn.getSys_no(), 
				queryIn.getAccrue_date(), queryIn.getAccrue_type(), queryIn.getAcct_branch(),
				queryIn.getCcy_code(), queryIn.getBudget_inst_amt(), queryIn.getAccrue_gl_code(), 
				queryIn.getAnalysis_state(), queryIn.getOffset_gl_code(), runEnvs.getCorpno(),
				(pageno - 1) * pgsize, pgsize, runEnvs.getCounts(),false);
		
		runEnvs.setCounts(FabAccureList.getRecordCount());
		Options<FabAccureQueryOut> output=new DefaultOptions<FabAccureQueryOut> ();
		
		output.setValues(FabAccureList.getRecords());
		
		bizlog.debug("output[%s]",output);
		bizlog.method("FabAccure.queryFabAccu>>end>>>>>>>>>>>>>>");
		
		return output;
	}

}
