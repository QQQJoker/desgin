package cn.sunline.ltts.busi.fa.parm;

import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaParmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_branch_agentDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_branch_agent;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchInfo;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaBranchAgent {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaBranchAgent.class);

	/**
	 * @Author dengyu
	 *         <p>
	 *         <li>2017年2月27日-下午8:12:53</li>
	 *         <li>功能说明：财务机构代理信息列表查询s</li>
	 *         </p>
	 * @param queryIn 传入值
	 * @return
	 */
	public static Options<FaBranchInfo> queryBranchAgentList(FaBranchInfo queryIn) {

		bizlog.method(" queryFaBranchInfo begin >>>>>>>>>>>>>>>>");
		bizlog.debug("BranchInfo[%s]", queryIn);

		
		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<FaBranchInfo> page = FaParmDao.lstBranchInfoList(orgId, queryIn.getAcct_branch(), queryIn.getAgent_brch_id(),
				(pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		
		Options<FaBranchInfo> queryList = new DefaultOptions<FaBranchInfo>();
		queryList.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

		return queryList;

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月1日-下午3:27:40</li>
	 *         <li>功能说明：此方法用于判断该账务机构是否在数据库中存在</li>
	 *         </p>
	 * @param acctBranch
	 *            fap_branch_agent表主键
	 * @return 没有记录 false 有返回true。
	 */
	public static boolean checkBranchExist(String acctBranch) {

		fap_branch_agent acctBranchInfo = Fap_branch_agentDao.selectOne_odb1(acctBranch, false);

		// 没有记录返回false，否则返回true。
		return (acctBranchInfo == null) ? false : true;

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月1日-下午3:28:09</li>
	 *         <li>功能说明：判断该代理机构或账务是否在apb_branch表中存在记录,不存在报错</li>
	 *         </p>
	 * @param faBranchInfo
	 *                      机构相关符合类型
	 */
	/*
	public static void checkExisInApbbranch(FaBranchInfo faBranchInfo) {

		if (!ApBranch.exists(faBranchInfo.getAcct_branch())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(apb_branch.class).getLongname(), GlDict.A.agent_brch_id.getLongName(), faBranchInfo.getAgent_brch_id());
		}
		if (!ApBranch.exists(faBranchInfo.getAgent_brch_id())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(apb_branch.class).getLongname(), GlDict.A.acct_branch.getLongName(), faBranchInfo.getAgent_brch_id());
		}

	}
	*/

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月1日-下午3:28:38</li>
	 *         <li>功能说明：判断该代理机构的相关信息是否为空</li>
	 *         </p>
	 * @param faBranchInfo
	 */
	public static void checkNull(FaBranchInfo faBranchInfo) {

		// 检查账务机构是否为空
		CommTools.fieldNotNull(faBranchInfo.getAcct_branch(), GlDict.A.acct_branch.getId(), GlDict.A.acct_branch.getLongName());

		// 检查代理机构是否为空
		CommTools.fieldNotNull(faBranchInfo.getAgent_brch_id(), GlDict.A.agent_brch_id.getId(), GlDict.A.agent_brch_id.getLongName());

	}
}
