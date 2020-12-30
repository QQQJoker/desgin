package cn.sunline.ltts.busi.fa.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_branch_agentDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_branch_agent;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaBranchAgentMnt {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaBranchAgentMnt.class);

	/**
	 * 增加代理机构信息
	 */

	public static void addBranchAgent( FaBranchInfo faBranchInfo ) {
		
		bizlog.method(" TaBusinessMnt.addBusiness begin >>>>>>>>>>>>>>>>");
		bizlog.debug("faBranchInfo[%s]", faBranchInfo);
		
		//1.检查不允许为空的字段
		FaBranchAgent.checkNull(faBranchInfo);
		
		//2.检查重名
		if( faBranchInfo.getAcct_branch().equals(faBranchInfo.getAgent_brch_id()) ){
			// 代理机构[${agentBranch}]不能与账务机构[${acctBranch}]相同
			throw GlError.GL.E0001(faBranchInfo.getAgent_brch_id());
		}
		
		//3.判断主键数据库里是否存在,如果存在则报错 
		if (FaBranchAgent.checkBranchExist(faBranchInfo.getAcct_branch())) {	
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(fap_branch_agent.class).getLongname(), faBranchInfo.getAcct_branch());
		}
		
		//4.判断该代理机构或账务是否在apb_branch表中存在记录
		//FaBranchAgent.checkExisInApbbranch(faBranchInfo);
		
		fap_branch_agent branchTable = SysUtil.getInstance(fap_branch_agent.class);		
		branchTable.setAcct_branch(faBranchInfo.getAcct_branch());  //账务机构
		branchTable.setAgent_brch_id(faBranchInfo.getAgent_brch_id());  //代理机构
		
		//插入数据
		Fap_branch_agentDao.insert(branchTable);
		
		// 登记审计
		ApDataAudit.regLogOnInsertParameter(branchTable);
		bizlog.method(" TaBusinessMnt.addBusiness end >>>>>>>>>>>>>>>>");
		
	}


	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月1日-下午5:10:49</li>
	 *         <li>功能说明：维护某笔账务代理机构信息</li>
	 *         </p>
	 * @param faBranchInfo  机构信息符合类型
	 */
	public static void modifyBranchAgent(FaBranchInfo faBranchInfo)  { 
		
		bizlog.method(" FaBranchAgentMnt.modifyBranchAgent begin >>>>>>>>>>>>>>>>");
		bizlog.debug("faBranchInfo[%s]", faBranchInfo);

		//1.检查不允许为空的字段是否合法
		FaBranchAgent.checkNull(faBranchInfo);
		
		//2.检查重名
		if( faBranchInfo.getAcct_branch().equals(faBranchInfo.getAgent_brch_id()) ){
			// 代理机构[${agentBranch}]不能与账务机构[${acctBranch}]相同
			throw GlError.GL.E0001(faBranchInfo.getAgent_brch_id());
		}
		
		//3.判断主键数据库里是否存在,如果不存在则报错 
		if ( !FaBranchAgent.checkBranchExist(faBranchInfo.getAcct_branch())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_branch_agent.class).getLongname(), GlDict.A.acct_branch.getId() , faBranchInfo.getAcct_branch());
		}
		
		//4.判断该代理机构或账务机构是否在apb_branch表中存在记录
		//FaBranchAgent.checkExisInApbbranch(faBranchInfo);
		
		//5.找出待修改的记录
		fap_branch_agent oldBranchAgent = Fap_branch_agentDao.selectOne_odb1(faBranchInfo.getAcct_branch(), false);
		
		// 判断数据版本号是否是最新
		if (CommUtil.compare(faBranchInfo.getRecdver(), oldBranchAgent.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_branch_agent.class).getLongname());
		}
		
		//防止公共字段丢失，克隆
		fap_branch_agent mntBranchTable = CommTools.clone(fap_branch_agent.class, oldBranchAgent);

		
		mntBranchTable.setAgent_brch_id(faBranchInfo.getAgent_brch_id());
		mntBranchTable.setRecdver(mntBranchTable.getRecdver()); // 数据版本号
		
		
		// 登记审计
		int i = ApDataAudit.regLogOnUpdateParameter(oldBranchAgent, mntBranchTable);
		if (i == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(fap_branch_agent.class).getLongname());
		}

		Fap_branch_agentDao.updateOne_odb1(mntBranchTable);

		bizlog.method("modifyBranchAgent end <<<<<<<<<<<<<<<<<<<<");
	}



	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月1日-下午5:12:38</li>
	 *         <li>功能说明：删除账务机构</li>
	 *         </p>
	 * @param acct_branch 账务机构
	 * @param dataVersion 数据版本
	 */
	public static void deleteBranchAgent(String acct_branch, Long dataVersion) {

		fap_branch_agent delAcctBranch = Fap_branch_agentDao.selectOne_odb1(acct_branch, false);

		// 账务机构号非空检验

		if (delAcctBranch == null) {
			throw ApPubErr.APPUB.E0024(OdbFactory.getTable(fap_branch_agent.class).getLongname(), GlDict.A.acct_branch.getLongName(), acct_branch, acct_branch, acct_branch);
		}
		// 版本号非空校验
		CommTools.fieldNotNull(dataVersion, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(dataVersion, delAcctBranch.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_branch_agent.class).getName());
		}	

		// 登记审计
		ApDataAudit.regLogOnDeleteParameter(delAcctBranch);
		
		// 根据主键删除一条记录
		Fap_branch_agentDao.deleteOne_odb1(acct_branch);
		bizlog.method("  deleteBranchAgent end  >>>>>>>>>>>>>>>>>>>>> ");
	}

}
