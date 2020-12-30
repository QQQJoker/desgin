package cn.sunline.ltts.busi.gl.item;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.account.FaOpenAccount;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.gl.namedsql.GlTransferProfitDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.Glb_income_seqDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.glb_income_seq;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.IoCompPbBranch.PbBranchUpLow;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.busi.sys.type.PbEnumType;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaUpdateBalanceIn;

public class GlYearEndTransProfit {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlYearEndTransProfit.class);

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月11日-下午3:17:01</li>
	 *         <li>功能说明：损益结转到本年利润</li>
	 *         </p>
	 */
	public static void transferProftByBranch(String orgId, String branchId) {

		BIZLOG.method("transferProftByBranch>>>>>>>bengin>>>>>>>");

		
		List<faa_account> acctTableList = GlTransferProfitDao.lstWaitTransferProfit(orgId, branchId, false);

		if (acctTableList.isEmpty()) {
			BIZLOG.debug("there is no  account whose balance need to be tranferd  on branchId[%s] ");
			return;
		}

		// 登记柜员流水,一个机构共用一个柜员流水
		String tellerSeq = FaAccounting.getTellerSeq();

		FaRegTellerSeq regTellerSeqIn = SysUtil.getInstance(FaRegTellerSeq.class);
		regTellerSeqIn.setSys_no(FaConst.GL_SYSTEM); // 系统编号
		regTellerSeqIn.setTrxn_seq_type(E_TRXNSEQTYPE.MANUAL_ACCOUNTING); // 交易流水类型
		regTellerSeqIn.setTrxn_subject(branchId); // 交易主体
		regTellerSeqIn.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
		regTellerSeqIn.setReversal_status(E_REVERSALSTATE.NOMARL); // 冲账状态
		regTellerSeqIn.setTrxn_seq(tellerSeq);
		regTellerSeqIn.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号

		FaAccounting.regTellerSeq(regTellerSeqIn);

		// 本年利润科目参数
		String profitSubject = FaTools.getProfitSubject();
		if (profitSubject == null) {
			throw GlError.GL.E0053(); // 本年利润科目参数未设置
		}
		BIZLOG.parm("profitSubject [%s] ", profitSubject);

		int totalCount = acctTableList.size();

		List<FaSingleAccountingCheckIn> accountingCheckInList = new ArrayList<FaSingleAccountingCheckIn>();

		for (faa_account acctTable : acctTableList) {

			E_DEBITCREDIT accoutingDirect; // 损益账户记账方向
			E_DEBITCREDIT profitDirect; // 本年利润记账方向

			// 损益账户反方向记账，本年利润记账方向与损益账户记账方向相反
			if (acctTable.getBal_direction() == E_DEBITCREDIT.DEBIT) {
				accoutingDirect = E_DEBITCREDIT.CREDIT;
				profitDirect = E_DEBITCREDIT.DEBIT;
			} else {
				accoutingDirect = E_DEBITCREDIT.DEBIT;
				profitDirect = E_DEBITCREDIT.CREDIT;

			}
			// 获取损益账户记账复合类型
			FaAccounting.getAccountingCheckByAcct(accountingCheckInList, acctTable.getAcct_no(), accoutingDirect, acctTable.getAcct_bal(), "", "");

			// 根据科目号获取本年利润基准账户信息
			FaAcctInfo profitAcct = FaOpenAccount.getAcctBySubject(acctTable.getSys_no(), acctTable.getCcy_code(), acctTable.getAcct_branch(), profitSubject);

			// 本年利润账户记账复合类型
			FaAccounting.getAccountingCheckByAcct(accountingCheckInList, profitAcct.getAcct_no(), profitDirect, acctTable.getAcct_bal(), "", "");

			//登记损益结转流水登记簿
			registerIncomeSeq(CommToolsAplt.prcRunEnvs().getTrandt(), acctTable.getAcct_no(), acctTable.getAcct_branch(), acctTable.getGl_code(),acctTable.getAcct_name(),
			        accoutingDirect,  acctTable.getCcy_code(), acctTable.getAcct_bal(), profitAcct.getAcct_no(), profitAcct.getGl_code(),profitAcct.getAcct_name(),acctTable.getAcct_branch(),
			        CommToolsAplt.prcRunEnvs().getTransq());
			        
		}
		// 记账，不需要再次登记柜员流水
		FaAccounting.bookMultiAccounting(accountingCheckInList, tellerSeq, (long) totalCount, FaConst.GL_SYSTEM, "Transfer Profit",true);
		
		BIZLOG.method("transferProftByBranch>>>>>>>end>>>>>>>");

	}

	public static void profitUp(String ccyCode, String sysNo) {

		BIZLOG.method("profitUp begin >>>>>>>>>>>>>>>>>>>>");
		BIZLOG.parm(" ccyCode[%s] ", ccyCode);

		//String brchRelationCode = ApSystemParm.getValue(FaConst.KEY_ACCOUNT_RELATION, ApConst.WILDCARD);
		//if (brchRelationCode == null)
		//	throw GlError.GL.E0054(); // 账务机构关系代码参数未设置
		//BIZLOG.parm("brchRelationCode [%s] ", brchRelationCode);

		String ccyFlag = ApConstants.WILDCARD;

		//app_branch_relation tabRelation = App_branch_relationDao.selectOne_odb1(brchRelationCode, true);
		//if (tabRelation.getDiff_ccy_ind() == E_YESORNO.YES) {
		//	ccyFlag = ccyCode;
		//}
		
		if(CommUtil.equals(FaTools.getDiffCcyInd(), E_YESORNO.YES.getValue())){
		    ccyFlag = ccyCode;
		}

		// 财务关系代码参数
		//String reportRelation = ApSystemParm.getValue(FaConst.KEY_ACCOUNT_RELATION, ApConst.WILDCARD);
		//if (reportRelation == null)
		//	throw GlError.GL.E0055(); // 财务关系代码参数未设置
		//BIZLOG.parm("reportRelation [%s] ", reportRelation);

		/*String topBranchId = SysUtil.getInstance(IoSrvPbBranch.class).getRootBranch(ccyFlag, PbEnumType.E_BRMPTP.B);
		//String topBranchId = ApBranch.getRoot(reportRelation, ccyFlag);
		if (topBranchId == null)
			throw GlError.GL.E0056(); // 获取财务关系根机构失败

		String orgId = CommToolsAplt.prcRunEnvs().getCorpno();

		// 某机构的某种机构关系下最大级数
		long brchMaxLevel = SysUtil.getInstance(IoSrvPbBranch.class).getBranchMaxLevel(ccyFlag, PbEnumType.E_BRMPTP.B, topBranchId);
		//long brchMaxLevel = ApBranchDao.selBranchMaxLevel(orgId, brchRelationCode, topBranchId, ccyFlag, false);
		if (CommUtil.isNull(brchMaxLevel) || brchMaxLevel <= 0)
			throw GlError.GL.E0057(); // 账务机构关系代码级数读取出错

		// 对每个机构划转
		for (long i = brchMaxLevel; i > 0; i--) {
			bizlog.parm("Level[%s]", i);

			// 根据机构关系代码及级数获取机构清单
			List<PbBranchUpLow> brchList = SysUtil.getInstance(IoSrvPbBranch.class).getBranchListByLevel(ccyFlag, PbEnumType.E_BRMPTP.B, topBranchId, i);
            if (brchList.size() <= 0) {
                throw GlError.GL.E0058(i);  // //读取[%s]级机构失败
            }
            for (PbBranchUpLow cplInfo : brchList) {
                bizlog.parm("senior branch[%s]junior branch[%s]", cplInfo.getUppebr(), cplInfo.getLowebr());
                if (cplInfo.getUppebr().equals(cplInfo.getLowebr())) {
                    //上下级机构不能相同
                    throw GlError.GL.E0059(cplInfo.getUppebr());  // 
                }

				// 利润上划
				profitUpOnBranch(orgId, cplInfo.getUppebr(), cplInfo.getLowebr(), ccyCode, sysNo);
			}
		}*/
		
		//当前法人
        String corpno = CommToolsAplt.prcRunEnvs().getCorpno();
        //机构对照关系
        PbEnumType.E_BRMPTP brmptp = PbEnumType.E_BRMPTP.B;
		//获取当前法人下最大机构层级数 (注：当前法人下无层级关系的不作处理 )
        long brchMaxLevel = SysUtil.getInstance(IoSrvPbBranch.class).getBranchMaxLevelByCorpno(ccyFlag, brmptp, corpno);
        for (long i = brchMaxLevel; i > 0; i--) {
            BIZLOG.parm("Level[%s]", i);
            //获取当前法人下当前层级的上级机构列表，没有则不作处理
            List<PbBranchUpLow> brchList = SysUtil.getInstance(IoSrvPbBranch.class).getBranchListByLevelAndCorpno(
                    ccyFlag, brmptp, corpno, i);
            for (PbBranchUpLow cplInfo : brchList) {
                BIZLOG.parm("cplInfo[%s]", cplInfo);
                //利润上划
                profitUpOnBranch(corpno, cplInfo.getUppebr(), cplInfo.getLowebr(), ccyCode, sysNo);
            }
        }
        
        //add by sh 20170921 金谷项目需求：增加 本年利润科目 划转到 与线下核心往来科目
        String topBranchId = SysUtil.getInstance(IoSrvPbBranch.class).getRootBranch(ccyFlag, brmptp);
        if (topBranchId == null){
            throw GlError.GL.E0056(); //获取财务关系根机构失败
        }
        //本年利润划转到与总行往来    
        profitTransferToHead(corpno, topBranchId, ccyCode, sysNo);
        
		BIZLOG.method("profitUp end <<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年3月13日-下午2:37:07</li>
	 *         <li>功能说明：利润上划</li>
	 *         </p>
	 * @param orgId
	 */
	public static void profitUpOnBranch(String orgId, String seniorBranchId, String juniorBranchId, String ccyCode, String sysNo) {

		String subjectNo = FaTools.getProfitSubject();
		
		FaAcctInfo openIn = SysUtil.getInstance(FaAcctInfo.class);
		FaAcctInfo  juniorInfo = SysUtil.getInstance(FaAcctInfo.class);
		openIn.setSys_no(sysNo); // 系统编号
		openIn.setCcy_code(ccyCode); // 货币代码
		openIn.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT); // 账户分类
		openIn.setAcct_branch(juniorBranchId); // 财务机构
		openIn.setGl_code(subjectNo); // 科目号
		openIn.setOn_bal_sheet_ind(E_YESORNO.YES);
		openIn.setOpen_acct_ind(E_YESORNO.YES);

		// 不存在自动开户
		juniorInfo = FaOpenAccount.openBaseAccount(openIn, false);
		if (CommUtil.isNull(juniorInfo) || CommUtil.isNull(juniorInfo.getAcct_no()) 
				|| CommUtil.isNull(juniorInfo.getAcct_bal()) 
				|| CommUtil.compare(juniorInfo.getAcct_bal(), BigDecimal.ZERO) == 0 ) {
			BIZLOG.parm("juniorBranchId[%s] profit is zero, exit", juniorBranchId);
			return ;
		}
			
		BigDecimal accountingAmt = juniorInfo.getAcct_bal();
		//不存在自动开户
		openIn.setAcct_branch(seniorBranchId); // 财务机构
		FaAcctInfo seniorInfo = FaOpenAccount.openBaseAccount(openIn);

		E_DEBITCREDIT juniorDirect;
		E_DEBITCREDIT soniorDirect;
		// 下级机构记账方向，与账户余额方向相反
		if (juniorInfo.getBal_direction() == E_DEBITCREDIT.DEBIT) {
			juniorDirect = E_DEBITCREDIT.CREDIT;
			soniorDirect = E_DEBITCREDIT.DEBIT;
		} else {
			juniorDirect = E_DEBITCREDIT.DEBIT;
			soniorDirect = E_DEBITCREDIT.CREDIT;

		}
		
		if ( CommUtil.compare(accountingAmt, BigDecimal.ZERO) != 0 ) {
			List<FaSingleAccountingCheckIn> singleAccountingList = new ArrayList<FaSingleAccountingCheckIn>();
	
			// 下级账户记账复合类型
			FaAccounting.getAccountingCheckByAcct(singleAccountingList, juniorInfo.getAcct_no(), juniorDirect, accountingAmt, "", "profitUpOnBranch");
	
			// 上级账户记账复合类型
			FaAccounting.getAccountingCheckByAcct(singleAccountingList, seniorInfo.getAcct_no(), soniorDirect, accountingAmt, "", "profitUpOnBranch");
	
			// 登记柜员流水
			String tellerSeq = FaAccounting.getTellerSeq();
	
			FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
			regTellerSeq.setSys_no(sysNo); // 系统编号
			regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
			// regTellerSeq.setTrxn_subject(); //交易主体
			regTellerSeq.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
			regTellerSeq.setRemark("profitUpOnBranch"); // 备注
			regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
			// regTellerSeq.setSett_batch_no(); //清算批次号
			regTellerSeq.setReversal_status(E_REVERSALSTATE.NOMARL); // 冲账状态
			// regTellerSeq.setOriginal_trxn_date(); //原日期
			// regTellerSeq.setOriginal_trxn_seq(); //原交易流水
			// regTellerSeq.setCheck_teller(); //复核柜员
			regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水
	
			FaAccounting.regTellerSeq(regTellerSeq);
	
			FaAccounting.bookMultiAccounting(singleAccountingList, tellerSeq, 2l, sysNo, "profitUpOnBranch",true);
		}

	}
	

    
    /**
     * 
     * <p>登记损益结转明细登记簿</p>
     * <p>Description:  </p>
     * @author songhao
     * @date   2017年9月20日 
     * @param trxndt 交易日期
     * @param acctno 损益账号
     * @param brchno 损益账户记账机构
     * @param glcode 科目号
     * @param amntcd 记账方向
     * @param crcycd 币种
     * @param tranam 交易金额
     * @param carrac 结转账号
     * @param carrbr 结转账号记账机构
     * @param carrsq 结转流水
     */
    public static void registerIncomeSeq(String trxndt, String acctno, String brchno, String glcode,String acctna, E_DEBITCREDIT amntcd, 
            String crcycd, BigDecimal tranam, String carrac,String carrcd,String carrna, String carrbr, String carrsq){
        glb_income_seq tbl_glb_income_seq = SysUtil.getInstance(glb_income_seq.class);
        tbl_glb_income_seq.setTrxndt(trxndt);
        tbl_glb_income_seq.setAcctno(acctno);
        tbl_glb_income_seq.setBrchno(brchno);
        tbl_glb_income_seq.setGlcode(glcode);
        tbl_glb_income_seq.setAcctna(acctna);//科目名称
        tbl_glb_income_seq.setAmntcd(amntcd);
        tbl_glb_income_seq.setCrcycd(crcycd);
        tbl_glb_income_seq.setTranam(tranam);
        tbl_glb_income_seq.setCarrbr(carrbr);
        tbl_glb_income_seq.setCarrac(carrac);
        tbl_glb_income_seq.setCarrcd(carrcd);//结转科目
        tbl_glb_income_seq.setCarrna(carrna);//结转科目名称
        tbl_glb_income_seq.setCarrsq(carrsq);
        tbl_glb_income_seq.setRecdver(1L);    // 数据版本号
        
        Glb_income_seqDao.insert(tbl_glb_income_seq);
    }
    
    /**
     * 
     * <p> 本年利润划转到与总行往来 </p>
     * <p>Description:	</p>
     * @author songhao
     * @date   2017年9月21日 
     * @param orgId 法人号
     * @param topBranchId 根机构
     * @param ccyCode  币种
     * @param sysNo 系统编号
     */
    public static void profitTransferToHead(String orgId, String topBranchId, String ccyCode, String sysNo) {

        String headSubjectNo = FaTools.getHeadSubject();//与总行往来
        String profileSubjectNo = FaTools.getProfitSubject();//本年利润
        
        FaAcctInfo openIn = SysUtil.getInstance(FaAcctInfo.class);
        FaAcctInfo  profileInfo = SysUtil.getInstance(FaAcctInfo.class);
        openIn.setSys_no(sysNo); // 系统编号
        openIn.setCcy_code(ccyCode); // 货币代码
        openIn.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT); // 账户分类
        openIn.setAcct_branch(topBranchId); // 财务机构
        openIn.setGl_code(profileSubjectNo); // 科目号
        openIn.setOn_bal_sheet_ind(E_YESORNO.YES);
        openIn.setOpen_acct_ind(E_YESORNO.YES);

        // 不存在自动开户
        profileInfo = FaOpenAccount.openBaseAccount(openIn, false);
        if (CommUtil.isNull(profileInfo) || CommUtil.isNull(profileInfo.getAcct_no()) 
                || CommUtil.isNull(profileInfo.getAcct_bal()) 
                || CommUtil.compare(profileInfo.getAcct_bal(), BigDecimal.ZERO) == 0 ) {
            BIZLOG.parm("topBranchId[%s] profit is zero, exit", topBranchId);
            return ;
        }
            
        BigDecimal accountingAmt = profileInfo.getAcct_bal();
        
        //获取与总行往来科目，不存在自动开户
        openIn.setGl_code(headSubjectNo); // 科目号
        FaAcctInfo headInfo = FaOpenAccount.openBaseAccount(openIn);

        E_DEBITCREDIT profileDirect;
        E_DEBITCREDIT headDirect;
        //本年利润科目记账方向，与账户余额方向相反
        if (profileInfo.getBal_direction() == E_DEBITCREDIT.DEBIT) {
            profileDirect = E_DEBITCREDIT.CREDIT;
            headDirect = E_DEBITCREDIT.DEBIT;
        }
        else {
            profileDirect = E_DEBITCREDIT.DEBIT;
            headDirect = E_DEBITCREDIT.CREDIT;
        }
        
        if ( CommUtil.compare(accountingAmt, BigDecimal.ZERO) != 0 ) {
            List<FaSingleAccountingCheckIn> singleAccountingList = new ArrayList<FaSingleAccountingCheckIn>();
    
            // 本年利润账户记账复合类型
            FaAccounting.getAccountingCheckByAcct(singleAccountingList, profileInfo.getAcct_no(), profileDirect, accountingAmt, "", "profitTransferToHead");
    
            // 与总行往来账户记账复合类型
            FaAccounting.getAccountingCheckByAcct(singleAccountingList, headInfo.getAcct_no(), headDirect, accountingAmt, "", "profitTransferToHead");
    
            // 登记柜员流水
            String tellerSeq = FaAccounting.getTellerSeq();
    
            FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
            regTellerSeq.setSys_no(sysNo); // 系统编号
            regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
            // regTellerSeq.setTrxn_subject(); //交易主体
            regTellerSeq.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
            regTellerSeq.setRemark("profitTransferToHead"); // 备注
            regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
            // regTellerSeq.setSett_batch_no(); //清算批次号
            regTellerSeq.setReversal_status(E_REVERSALSTATE.NOMARL); // 冲账状态
            // regTellerSeq.setOriginal_trxn_date(); //原日期
            // regTellerSeq.setOriginal_trxn_seq(); //原交易流水
            // regTellerSeq.setCheck_teller(); //复核柜员
            regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水
    
            FaAccounting.regTellerSeq(regTellerSeq);
    
            FaAccounting.bookMultiAccounting(singleAccountingList, tellerSeq, 2l, sysNo, "profitTransferToHead",true);
        }

    }

    /**
     * 
     * @Author
     *         <p>
     *         <li>2020年11月10日-下午4:16:43</li>
     *         <li>功能说明：损益类科目清零操作</li>
     *         </p>
     * @param orgId
     * @param brchno
     */
	public static void transferProfitToZero(String orgId, String brchno) {
		
		BIZLOG.method(" GlYearEndTransProfit.transferProftToZero begin >>>>>>>>>>>>>>>>");
		
		// 查询待损益结转账户
		List<faa_account> acctTableList = GlTransferProfitDao.lstWaitTransferProfit(orgId, brchno, false);
		if (acctTableList.isEmpty()) {
			BIZLOG.debug("there is no  account whose balance need to be tranferd  on branchId[%s] ");
			return;
		}
		
		// 本年利润科目参数未设置
		String profitSubject = FaTools.getProfitSubject(); 
		if (profitSubject == null) {
			throw GlError.GL.E0053(); 
		}
		BIZLOG.parm("profitSubject [%s] ", profitSubject);
		
		List<FaSingleAccountingCheckIn> accountingCheckInList = new ArrayList<FaSingleAccountingCheckIn>();

		for (faa_account acctTable : acctTableList) {

			E_DEBITCREDIT accoutingDirect; // 损益账户记账方向
			E_DEBITCREDIT profitDirect; // 本年利润记账方向

			// 损益账户反方向记账，本年利润记账方向与损益账户记账方向相反
			if (acctTable.getBal_direction() == E_DEBITCREDIT.DEBIT) {
				accoutingDirect = E_DEBITCREDIT.CREDIT;
				profitDirect = E_DEBITCREDIT.DEBIT;
			} else {
				accoutingDirect = E_DEBITCREDIT.DEBIT;
				profitDirect = E_DEBITCREDIT.CREDIT;

			}
			// 获取损益账户记账复合类型
			FaAccounting.getAccountingCheckByAcct(accountingCheckInList, acctTable.getAcct_no(), accoutingDirect, acctTable.getAcct_bal(), "", "");

			// 根据科目号获取本年利润基准账户信息
			FaAcctInfo profitAcct = FaOpenAccount.getAcctBySubject(acctTable.getSys_no(), acctTable.getCcy_code(), acctTable.getAcct_branch(), profitSubject);

			// 本年利润账户记账复合类型
			FaAccounting.getAccountingCheckByAcct(accountingCheckInList, profitAcct.getAcct_no(), profitDirect, acctTable.getAcct_bal(), "", "");
			
			// 更新账户余额
			FaUpdateBalanceIn updateBalanceIn = SysUtil.getInstance(FaUpdateBalanceIn.class);
			updateBalanceIn.setAccounting_amt(acctTable.getAcct_bal());
			updateBalanceIn.setAcct_no(acctTable.getAcct_no());
			updateBalanceIn.setDebit_credit(accoutingDirect);
			FaAccounting.updateBalance(updateBalanceIn);
			
			// 登记损益结转流水登记簿
			registerIncomeSeq(CommToolsAplt.prcRunEnvs().getTrandt(), acctTable.getAcct_no(), acctTable.getAcct_branch(), acctTable.getGl_code(),acctTable.getAcct_name(),
			        accoutingDirect,  acctTable.getCcy_code(), acctTable.getAcct_bal(), profitAcct.getAcct_no(), profitAcct.getGl_code(),profitAcct.getAcct_name(),acctTable.getAcct_branch(),
			        CommToolsAplt.prcRunEnvs().getTransq());
		}
		
		BIZLOG.method(" GlYearEndTransProfit.transferProftToZero end <<<<<<<<<<<<<<<<");
		
	}
	
}
