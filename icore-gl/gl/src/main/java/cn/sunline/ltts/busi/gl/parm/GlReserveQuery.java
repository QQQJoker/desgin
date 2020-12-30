package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.namedsql.GlParmDao;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveQueryIn;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveQueryOut;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

public class GlReserveQuery {

	public static Options<GlReserveQueryOut> reserveQuery(GlReserveQueryIn queryIn) {

		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		// 起始日期和截止日期均不为空则查询该区间,否则查询当前日期
		Page<GlReserveQueryOut> page = null;
		Options<GlReserveQueryOut> list = new DefaultOptions<GlReserveQueryOut>();
		if (queryIn.getStart_date() != null || queryIn.getEnd_date() != null) {
			
			page = GlParmDao.lstIntervalReserve(orgId, queryIn.getStart_date(), 
					queryIn.getEnd_date(), queryIn.getRegister_no(), queryIn.getDeposit_busi_type(),
					queryIn.getDeposit_level(), queryIn.getReserve_type(), queryIn.getCcy_code(), 
					queryIn.getDeposit_brch(), queryIn.getDeposit_parent_brch(), queryIn.getCurrent_deposit_amt(),
					queryIn.getDeposit_status(), queryIn.getAccounting_seq(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
			list.setValues(page.getRecords());
			runEnvs.setCounts(page.getRecordCount());
		} else {
			
			page = GlParmDao.lstAllReserve(orgId, runEnvs.getTrandt(), queryIn.getRegister_no(), 
					queryIn.getDeposit_busi_type(), queryIn.getDeposit_level(), queryIn.getReserve_type(), 
					queryIn.getCcy_code(), queryIn.getDeposit_brch(), queryIn.getDeposit_parent_brch(), 
					queryIn.getCurrent_deposit_amt(), queryIn.getDeposit_status(), queryIn.getAccounting_seq(), 
					(pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		}
		list.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

		return list;

	}
}
