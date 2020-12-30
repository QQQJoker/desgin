package cn.sunline.ltts.busi.fa.parm;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaParmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_branch_settlementDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_branch_settlement;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchSettlementInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;
public class FaBranchSettlement {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaBranchSettlement.class);
	
	/**
	 * 
	 * @Author DENGYU
	 *         <p>
	 *         <li>2017年2月25日-上午11:57:33</li>
	 *         <li>功能说明：检查上送的清算关系组织结构信息中空值的合法性</li>
	 *         </p>
	 */
	public static void checkBranchSettlementNull( FaBranchSettlementInfo branchSettlement ) {

		CommTools.fieldNotNull(branchSettlement.getCcy_code(),GlDict.A.ccy_code.getId() , GlDict.A.ccy_code.getLongName());
		CommTools.fieldNotNull(branchSettlement.getAcct_branch(), GlDict.A.acct_branch.getId(), GlDict.A.acct_branch.getLongName());
		CommTools.fieldNotNull(branchSettlement.getParent_brch_id(), GlDict.A.parent_brch_id.getId(),GlDict.A.parent_brch_id.getLongName());
		CommTools.fieldNotNull(branchSettlement.getBusi_relation_level(), GlDict.A.busi_relation_level.getId(), GlDict.A.busi_relation_level.getLongName());
		CommTools.fieldNotNull(branchSettlement.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		CommTools.fieldNotNull(branchSettlement.getUpper_lvl_gl_code(), GlDict.A.upper_lvl_gl_code.getId(), GlDict.A.upper_lvl_gl_code.getLongName());
		
	}
	

	/**
	 * @Author DENGYU
	 *         <p>
	 *         <li>2017年2月25日-下午4:55:03</li>
	 *         <li>功能说明：判断该清算关系组织结构信息是否在数据库中存在</li>
	 *         </p>
	 * @param ccyCode  acctBranch  主键索引
	 * @return 没有记录返回false, 有记录返回true。
	 */
	public static boolean checkBranchSettlementExists( String ccyCode , String acctBranch  ) {
			
		fap_branch_settlement faBrchSettInfo = 	Fap_branch_settlementDao.selectOne_odb1(ccyCode, acctBranch, false);
		
		// 没有记录返回false，否则返回true。
		return (faBrchSettInfo == null) ? false : true;
		
	}
	
	//
	/**
	 * @Author DENGYU
	 *         <p>
	 *         <li>2017年2月25日-下午4:55:40</li>
	 *         <li>功能说明：核对上送的清算关系组织结构信息中的每个字段值的合法性</li>
	 *         </p>
	 * @param branchSettlement
	 */
	public static void checkBranchSettlementValidity( FaBranchSettlementInfo branchSettlement ) {
		
		//货币代码必须在app_currency中存在
	    /*
		if( !ApCurrency.exists(branchSettlement.getCcy_code()) )
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(app_currency.class).getLongname(),GlDict.A.ccy_code.getLongName() ,branchSettlement.getCcy_code() );
		
		//账务机构，上级机构必须在apb_branch存在
		
		
		if( !ApBranch.exists(branchSettlement.getAcct_branch()) )
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(apb_branch.class).getLongname(),GlDict.A.acct_branch.getLongName() ,branchSettlement.getAcct_branch() );
		if( !ApBranch.exists(branchSettlement.getParent_brch_id()) )
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(apb_branch.class).getLongname(),GlDict.A.parent_brch_id.getLongName() ,branchSettlement.getParent_brch_id() );
		*/
		//记账科目号、上级记账科目必须在fap_accounting_subject中存在		
		if( !FaAccountingSubject.checkSubjectExists(branchSettlement.getGl_code()) )
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(),GlDict.A.gl_code.getLongName() ,branchSettlement.getAcct_branch() );
		
		if( !FaAccountingSubject.checkSubjectExists(branchSettlement.getUpper_lvl_gl_code()) )
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(),GlDict.A.upper_lvl_gl_code.getLongName() ,branchSettlement.getUpper_lvl_gl_code() );
		
		//上送的账务机构，上级机构不能相同
		if( branchSettlement.getAcct_branch().equals(branchSettlement.getParent_brch_id()) )
			throw GlError.GL.E0001(branchSettlement.getAcct_branch());
			
		//上送的业务关系级别大于等于1时，向上检查其上级机构构需存在。
		long level = branchSettlement.getBusi_relation_level();
		
		if( level > 1 ){
			if( !checkBranchSettlementExists( branchSettlement.getCcy_code() , branchSettlement.getParent_brch_id()  ) ){
				
				throw ApPubErr.APPUB.E0024(OdbFactory.getTable(fap_branch_settlement.class).getLongname(),
						GlDict.A.ccy_code.getId() , branchSettlement.getCcy_code(),
						GlDict.A.acct_no.getId() , branchSettlement.getParent_brch_id());
				
			}
		}
		
	}
	

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月4日-上午10:06:13</li>
	 *         <li>功能说明：根据货币代码+账务机构获取对应的清算关系组织结构信息</li>
	 *         </p>
	 * @param acctBranch 账务机构
	 * @param ccyCode 货币代码
	 * @return
	 */
	public static FaBranchSettlementInfo getBranchSettlement( String acctBranch, String ccyCode ) {
		
		bizlog.method(" FabranchSettlement.getBranchSettlement begin >>>>>>>>>>>>>>>>");
		bizlog.debug("acct_branch[%s] ccy_code[%s]",acctBranch,ccyCode);
		
		fap_branch_settlement faBrchSettInfo = 	Fap_branch_settlementDao.selectOne_odb1( acctBranch , ccyCode , false);	
		
		//无对应记录则报错
		if(faBrchSettInfo == null){
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_branch_settlement.class).getLongname(), 
					GlDict.A.acct_branch.getLongName(),
					acctBranch );
		}
					
		FaBranchSettlementInfo info = SysUtil.getInstance(FaBranchSettlementInfo.class);
		
		info.setCcy_code(faBrchSettInfo.getCcy_code());  //货币代码
		info.setAcct_branch(faBrchSettInfo.getAcct_branch());  //账务机构
		info.setParent_brch_id(faBrchSettInfo.getParent_brch_id());  //上级机构
		info.setBusi_relation_level(faBrchSettInfo.getBusi_relation_level());  //业务关系级别
		info.setGl_code(faBrchSettInfo.getGl_code());  //科目号
		info.setUpper_lvl_gl_code(faBrchSettInfo.getUpper_lvl_gl_code());  //上级科目
		info.setRecdver(faBrchSettInfo.getRecdver());//数据版本

		return info;
		
	}
	
	/**
	 * @Author dengyu
	 *         <p>
	 *         <li>2017年2月27日-下午8:19:18</li>
	 *         <li>功能说明：根据查询条件获取对应的清算关系组织结构信息</li>
	 *         </p>
	 * @param brchSetLent 清算关系组织结构
	 * @return 清算关系组织结构清单
	 */
	public static Options<FaBranchSettlementInfo> queryBranchSettlementList(FaBranchSettlementInfo brchSetLent) {
		bizlog.method(" FaBranchSettlement.queryBranchSettlementList begin >>>>>>>>>>>>>>>>");		
		bizlog.debug("brchSetLent=[%s]", brchSetLent);
		
		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<FaBranchSettlementInfo> page =  FaParmDao.lstBrchSettlementInfo(runEnvs.getCorpno(), brchSetLent.getCcy_code(), 
				brchSetLent.getAcct_branch(), brchSetLent.getParent_brch_id(), brchSetLent.getBusi_relation_level(), 
				brchSetLent.getGl_code(), brchSetLent.getUpper_lvl_gl_code(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);	
		
		Options<FaBranchSettlementInfo> queryList = new DefaultOptions<FaBranchSettlementInfo>();
		queryList.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());
		
		bizlog.method(" FaBranchSettlement.queryBranchSettlementList end <<<<<<<<<<<<<<<<");
		return queryList;
	}
}
