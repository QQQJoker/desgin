package cn.sunline.ltts.busi.fa.parm;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_branch_settlementDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_branch_agent;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_branch_settlement;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchSettlementInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.sys.dict.GlDict;
public class FaBranchSettlementMnt {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaBranchSettlementMnt.class);
	
	/**
	 * 
	 * @Author dengyu
	 *         <p>
	 *         <li>2017年2月25日-上午11:51:34</li>
	 *         <li>功能说明：增加清算关系组织结构信息</li>
	 *         </p>
	 * @param branchSettlement
	 */
	
	public static void addBranchSettlement( FaBranchSettlementInfo branchSettlement ) {
		
		bizlog.method("addBranchSettlement begin >>>>>>>>>>>>>>>>");
		bizlog.debug("addBranchSettlement[%s]", branchSettlement);
		
		//1.检查送上的字段是否为空
		FaBranchSettlement.checkBranchSettlementNull(branchSettlement);
		
		//2.判断合法性
		FaBranchSettlement.checkBranchSettlementValidity(branchSettlement);
		
		//3.判断主键及唯一索引值是否存在
		if( FaBranchSettlement.checkBranchSettlementExists( branchSettlement.getCcy_code(), branchSettlement.getAcct_branch()) )
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(fap_branch_settlement.class).getLongname(), branchSettlement.getCcy_code()+" "+branchSettlement.getAcct_branch());
					
		//4.插入数据
		fap_branch_settlement info = SysUtil.getInstance(fap_branch_settlement.class);
		
		info.setCcy_code(branchSettlement.getCcy_code());  //货币代码
		info.setAcct_branch(branchSettlement.getAcct_branch());  //账务机构
		info.setParent_brch_id(branchSettlement.getParent_brch_id());  //上级机构
		info.setBusi_relation_level(branchSettlement.getBusi_relation_level());  //业务关系级别
		info.setGl_code(branchSettlement.getGl_code());  //科目号
		info.setUpper_lvl_gl_code(branchSettlement.getUpper_lvl_gl_code());  //上级科目
		info.setRecdver(1l);//默认为1
		
		Fap_branch_settlementDao.insert(info);
		
		// 登记审计
		ApDataAudit.regLogOnInsertParameter(info);
		bizlog.method(" FaBranchSettlementMnt.addBranchSettlement end >>>>>>>>>>>>>>>>");
		
		
	}
	
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年2月27日-下午7:25:19</li>
	 *         <li>功能说明：维护清算关系组织结构信息</li>
	 *         </p>
	 * @param branchSettlement
	 */
	public static void modifyBranchSettlement( FaBranchSettlementInfo branchSettlement ) {
		bizlog.method("modifyBranchSettlement begin >>>>>>>>>>>>>>>>>>>>");
		// 判断不充许为空值的字段是否合法；
		FaBranchSettlement.checkBranchSettlementNull(branchSettlement);
		
		//检查合法性
		FaBranchSettlement.checkBranchSettlementValidity(branchSettlement);		
		fap_branch_settlement oldInfo = Fap_branch_settlementDao.selectOne_odb1(branchSettlement.getCcy_code(), branchSettlement.getAcct_branch(), false);
		
		if( oldInfo ==null )
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_branch_settlement.class).getLongname(),
					GlDict.A.ccy_code.getLongName()+" "+GlDict.A.acct_branch.getLongName(), 
					branchSettlement.getCcy_code()+" "+branchSettlement.getAcct_branch());
		
		//判断数据版本
		if (CommUtil.compare(branchSettlement.getRecdver(), oldInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_branch_agent.class).getLongname());
		}
		
		fap_branch_settlement mntInfo =  CommTools.clone(fap_branch_settlement.class, oldInfo);
		
		mntInfo.setParent_brch_id(branchSettlement.getParent_brch_id());  //上级机构
		mntInfo.setBusi_relation_level(branchSettlement.getBusi_relation_level());  //业务关系级别
		mntInfo.setGl_code(branchSettlement.getGl_code());  //科目号
		mntInfo.setUpper_lvl_gl_code(branchSettlement.getUpper_lvl_gl_code());  //上级科目

		// 登记审计
		int i = ApDataAudit.regLogOnUpdateParameter(oldInfo, mntInfo);
		if (i == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(KnpPara.class).getLongname());
		}
		
		mntInfo.setRecdver(mntInfo.getRecdver()+1);
		Fap_branch_settlementDao.updateOne_odb1(mntInfo);

		bizlog.method("modifyBranchSettlement end <<<<<<<<<<<<<<<<<<<<");
		
		
	}
	

	/**
	 * @Author dengyu
	 *         <p>
	 *         <li>2017年2月27日-下午7:36:44</li>
	 *         <li>功能说明：删除清算关系组织结构信息</li>
	 *         </p>
	 * @param branchSettlement
	 */
	public static void deleteBranchSettlement( FaBranchSettlementInfo branchSettlement ) {
		
		fap_branch_settlement deletInfo = Fap_branch_settlementDao.selectOne_odb1(branchSettlement.getCcy_code(), branchSettlement.getAcct_branch(), false);
		
		//判断主键值是否已存在
		if( deletInfo == null )
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_branch_settlement.class).getLongname(), GlDict.A.ccy_code.getLongName()+" "+GlDict.A.acct_branch.getLongName(), branchSettlement.getCcy_code()+" "+branchSettlement.getAcct_branch());		
		
		// 版本号非空校验
		CommTools.fieldNotNull(deletInfo.getRecdver(), BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(branchSettlement.getRecdver(), deletInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_branch_settlement.class).getName());
		}
		
		// 根据账务机构删除一条记录
		Fap_branch_settlementDao.deleteOne_odb1(branchSettlement.getCcy_code(), branchSettlement.getAcct_branch());

		// 登记审计
		ApDataAudit.regLogOnDeleteParameter(deletInfo);
		
		bizlog.method(" deleteBranchSettlement  end  >>>>>>>>>>>>>>>>>>> ");
		
	}
	
}
