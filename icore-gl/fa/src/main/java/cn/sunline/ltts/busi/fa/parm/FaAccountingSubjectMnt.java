package cn.sunline.ltts.busi.fa.parm;

import java.util.Date;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountDao;
import cn.sunline.ltts.busi.fa.namedsql.FaAccountingDao;
import cn.sunline.ltts.busi.fa.namedsql.FaParmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaSubjectInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaAccountingSubjectMnt {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaBranchSettlementMnt.class);

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年2月28日-上午11:40:59</li>
	 *         <li>功能说明：新增一条科目信息</li>
	 *         </p>
	 * @param subjectInfo
	 *            科目信息符合类型
	 */
	public static void addSubject(FaSubjectInfo subjectInfo) {

		// 必须输入的值是否为空
		FaAccountingSubject.checkSubjectNull(subjectInfo);

		// 输入值的合法性
		FaAccountingSubject.checkSubjecttValidity(subjectInfo);

		// 末级科目标志的值，必须在数据表fap_accounting_subject符合规则。(送上的数据的末级科目标志只能是Y)
		if (subjectInfo.getEnd_gl_code_ind() == E_YESORNO.NO) {
			throw GlError.GL.E0012(GlDict.A.end_gl_code_ind.getLongName());
		}
		
		// 检查是否存在,存在报错
		if (FaAccountingSubject.checkSubjectExists(subjectInfo.getGl_code())) {
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), subjectInfo.getGl_code());
		}
		//检查科目长度是否合法
		checkSubjectIsIllege(subjectInfo);
		
		String identifier_code = genIdentifierCode(subjectInfo.getGl_code());
		bizlog.debug("genIdentifierCode:", identifier_code);
		subjectInfo.setIdentifier_code(identifier_code);
		// 插入数据
		fap_accounting_subject info = SysUtil.getInstance(fap_accounting_subject.class);

		info.setGl_code(subjectInfo.getGl_code()); // 科目号
		info.setGl_code_desc(subjectInfo.getGl_code_desc()); // 科目名称
		info.setUpper_lvl_gl_code(subjectInfo.getUpper_lvl_gl_code()); // 上级科目
		info.setGl_code_type(subjectInfo.getGl_code_type()); // 科目类别
		info.setGl_code_level(subjectInfo.getGl_code_level()); // 科目级别
		info.setEnd_gl_code_ind(subjectInfo.getEnd_gl_code_ind()); // 末层科目标志
		info.setIdentifier_code(subjectInfo.getIdentifier_code()); // 识别码
		info.setBal_prop(subjectInfo.getBal_prop()); // 余额性质
		info.setOn_bal_sheet_ind(subjectInfo.getOn_bal_sheet_ind()); // 表内标志
		info.setSimple_list_display_ind(subjectInfo.getSimple_list_display_ind()); // 简表展示标识
		info.setBal_check_ind(subjectInfo.getBal_check_ind()); // 余额检查标志
		info.setDebit_manual_allow(subjectInfo.getDebit_manual_allow()); // 借方手工记账许可
		info.setCredit_manual_allow(subjectInfo.getCredit_manual_allow()); // 贷方手工记账许可
		info.setManual_open_acct_mode(subjectInfo.getManual_open_acct_mode()); // 手工开户受理模式
		info.setAllow_accounting_sys(subjectInfo.getAllow_accounting_sys()); // 允许记账的系统
		info.setValid_ind(subjectInfo.getValid_ind()); // 有效标志
		info.setOpp_open_ind(E_YESORNO.NO); // 对开标志
		info.setOffset_gl_code(subjectInfo.getOffset_gl_code()); // 对方科目
		info.setOpp_open_way(subjectInfo.getOpp_open_way()); // 对开账户识别方式
//		info.setCreate_date(subjectInfo.getCreate_date()); // 建立日期
		info.setCreate_date(CommToolsAplt.prcRunEnvs().getTrandt());// 建立日期
		info.setRecdver(1l);//版本号默认1

		Fap_accounting_subjectDao.insert(info);

		// 修改末级科目标志(传入的末级科目标志一定为Y,且非1级科目的上级科目一定存在,当传入一个末级科目,修改其上级科目的标志为N)
		if (subjectInfo.getGl_code_level() != 1) {

			fap_accounting_subject oldEndInd = Fap_accounting_subjectDao.selectOne_odb1(subjectInfo.getUpper_lvl_gl_code(), false);
			fap_accounting_subject newEndInd = CommTools.clone(fap_accounting_subject.class, oldEndInd);
			newEndInd.setEnd_gl_code_ind(E_YESORNO.NO);
			Fap_accounting_subjectDao.updateOne_odb1(newEndInd);

		}

		// 登记审计
		ApDataAudit.regLogOnInsertParameter(info);
		bizlog.method(" addSubject end >>>>>>>>>>>>>>>>");

	}

	/**  
    * @Title: checkSubjectIsIllege  
    * @Description: 校验科目号长度是否合法  
    * @Author xionglz
    * @param @param subjectInfo     
    * @return void    返回类型  
    * @throws  
    */  
    private static void checkSubjectIsIllege(FaSubjectInfo subjectInfo) {
        //一级科目为a位，n级科目为a+len(n-1)
        int inglcode = 0;//接收输入科目号位数
        long trglcode=  0;//接收正确科目号位数
        inglcode = subjectInfo.getGl_code().toString().length();
        trglcode = Integer.parseInt(FaTools.getFirstLevelSubjectLength())
                        +Integer.parseInt(FaTools.getIncreaseSubjectLength())
                        *(subjectInfo.getGl_code_level()-1);
        if(CommUtil.compare(inglcode, (int)trglcode)!=0){
            throw GlError.GL.E0205(subjectInfo.getGl_code(), inglcode, subjectInfo.getGl_code_level());
        }
        
    }

    /**
	 * 生成识别码
	 * @param gl_code
	 * @return
	 */
	private static String genIdentifierCode(String gl_code) {
		int topGlCodeLen = Integer.parseInt(FaTools.getFirstLevelSubjectLength());
		String topGlCode = gl_code.substring(0, topGlCodeLen);
		String org_id = CommToolsAplt.prcRunEnvs().getCorpno();
		int count = FaAccountingDao.cntGlCodeNum(topGlCode, org_id, false);
		count++;
		String suffix = autoGenericCode(count,4);
		return topGlCode + suffix;
	}

	/**
	 * 不够位数的在前面补0，保留len的长度位数字
	 * @param value
	 * @param len
	 * @return
	 */
    private static String autoGenericCode(int value, int len) {
        String result = "";
        result = String.format("%0" + len + "d", value);
        return result;
    }
    
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-上午9:57:53</li>
	 *         <li>功能说明：增加多条科目信息</li>
	 *         </p>
	 * @param subjectInfoList
	 *            科目信息明细列表
	 */
	public static void addSubjectList(List<FaSubjectInfo> subjectInfoList) {

		// 传入的list 科目号+上级科目的升序排序
		CommTools.listSort(subjectInfoList, true, GlDict.A.gl_code.getId(), GlDict.A.upper_lvl_gl_code.getId());

		for (FaSubjectInfo info : subjectInfoList) {

			addSubject(info);

		}
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-上午9:56:18</li>
	 *         <li>功能说明：维护一条科目信息</li>
	 *         </p>
	 * @param subjectInfo
	 *            科目信息符合类型
	 */
	public static void modifySubject(FaSubjectInfo subjectInfo) {

		// 检查不能为空的字段
		FaAccountingSubject.checkSubjectNull(subjectInfo);

		// 检查主键唯一索引,不存在报错
		if (!FaAccountingSubject.checkSubjectExists(subjectInfo.getGl_code())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), subjectInfo.getGl_code());
		}

		// 检查合法性
		FaAccountingSubject.checkSubjecttValidity(subjectInfo);
		
		// 找出待维护的字段
		fap_accounting_subject oldInfo = Fap_accounting_subjectDao.selectOne_odb1(subjectInfo.getGl_code(), false);
		// 克隆
		fap_accounting_subject mntInfo = CommTools.clone(fap_accounting_subject.class, oldInfo);

		// 对比版本号
		if (CommUtil.compare(mntInfo.getRecdver(), subjectInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_accounting_subject.class).getName());
		}
		
		// 检查当年是否有发生额
		if (checkAmount(subjectInfo.getGl_code())) {
			// 维护字段
			mntInfo.setGl_code_desc(subjectInfo.getGl_code_desc()); // 科目名称
			mntInfo.setSimple_list_display_ind(subjectInfo.getSimple_list_display_ind()); // 简表展示标识
			mntInfo.setBal_check_ind(subjectInfo.getBal_check_ind()); // 余额检查标志
			mntInfo.setDebit_manual_allow(subjectInfo.getDebit_manual_allow()); // 借方手工记账许可
			mntInfo.setCredit_manual_allow(subjectInfo.getCredit_manual_allow()); // 贷方手工记账许可
			mntInfo.setManual_open_acct_mode(subjectInfo.getManual_open_acct_mode()); // 手工开户受理模式
			mntInfo.setAllow_accounting_sys(subjectInfo.getAllow_accounting_sys()); // 允许记账的系统
			mntInfo.setOpp_open_ind(subjectInfo.getOpp_open_ind()); // 对开标志
			mntInfo.setOffset_gl_code(subjectInfo.getOffset_gl_code()); // 对方科目
			mntInfo.setOpp_open_way(subjectInfo.getOpp_open_way()); // 对开账户识别方式

			// 判断不允许维护的字段
			// 1.上级科目不允许维护
			if (CommUtil.compare(oldInfo.getUpper_lvl_gl_code(), subjectInfo.getUpper_lvl_gl_code()) != 0) {
				throw GlError.GL.E0014(GlDict.A.upper_lvl_gl_code.getLongName());
			}
			// 2.科目类别不允许维护
			if (oldInfo.getGl_code_type() != subjectInfo.getGl_code_type()) {
				throw GlError.GL.E0014(GlDict.A.gl_code_type.getLongName());
			}
			// 3.科目级别不允许维护
			if (CommUtil.compare(oldInfo.getGl_code_level(), subjectInfo.getGl_code_level()) != 0) {
				throw GlError.GL.E0014(GlDict.A.gl_code_level.getLongName());
			}
			// 4.末级科目标志不允许维护
			if (CommUtil.compare(oldInfo.getEnd_gl_code_ind(), subjectInfo.getEnd_gl_code_ind()) != 0) {
				throw GlError.GL.E0014(GlDict.A.end_gl_code_ind.getLongName());
			}
			// 5.识别码不允许维护
			if (CommUtil.compare(oldInfo.getIdentifier_code(), subjectInfo.getIdentifier_code()) != 0) {
				throw GlError.GL.E0014(GlDict.A.identifier_code.getLongName());
			}
			// 6.余额性质不允许维护
			if (CommUtil.compare(oldInfo.getBal_prop(), subjectInfo.getBal_prop()) != 0) {
				throw GlError.GL.E0014(GlDict.A.bal_prop.getLongName());
			}
			// 7.表内标志不允许维护
			if (oldInfo.getOn_bal_sheet_ind() != subjectInfo.getOn_bal_sheet_ind()) {
				throw GlError.GL.E0014(GlDict.A.on_bal_sheet_ind.getLongName());
			}
			// 8.有效标志不允许维护
			if (oldInfo.getValid_ind() != subjectInfo.getValid_ind()) {
				throw GlError.GL.E0014(GlDict.A.valid_ind.getLongName());
			}
			// 9.创建日期不允许维护
			if (!CommUtil.equals(oldInfo.getCreate_date(), subjectInfo.getCreate_date())) {
				throw GlError.GL.E0014(GlDict.A.create_date.getLongName());
			}

			// 登记审计
			int i = ApDataAudit.regLogOnUpdateParameter(oldInfo, mntInfo);
			if (i == 0) {
				throw ApPubErr.APPUB.E0023(OdbFactory.getTable(fap_accounting_subject.class).getLongname());
			}
			mntInfo.setRecdver(subjectInfo.getRecdver()+1);
			Fap_accounting_subjectDao.updateOne_odb1(mntInfo);

			bizlog.method("modifyBranchSettlement end <<<<<<<<<<<<<<<<<<<<");

		}
		else {

			// 维护字段
			mntInfo.setGl_code_desc(subjectInfo.getGl_code_desc()); // 科目名称
			mntInfo.setBal_check_ind(subjectInfo.getBal_check_ind()); // 余额检查标志
			mntInfo.setDebit_manual_allow(subjectInfo.getDebit_manual_allow()); // 借方手工记账许可
			mntInfo.setCredit_manual_allow(subjectInfo.getCredit_manual_allow()); // 贷方手工记账许可
			mntInfo.setManual_open_acct_mode(subjectInfo.getManual_open_acct_mode()); // 手工开户受理模式
			mntInfo.setAllow_accounting_sys(subjectInfo.getAllow_accounting_sys()); // 允许记账的系统
			mntInfo.setOpp_open_ind(subjectInfo.getOpp_open_ind()); // 对开标志
			mntInfo.setOffset_gl_code(subjectInfo.getOffset_gl_code()); // 对方科目
			mntInfo.setOpp_open_way(subjectInfo.getOpp_open_way()); // 对开账户识别方式

			// 判断不允许维护的字段
			// 1.上级科目不允许维护
			if (CommUtil.compare(oldInfo.getUpper_lvl_gl_code(), subjectInfo.getUpper_lvl_gl_code()) != 0) {
				throw GlError.GL.E0014(GlDict.A.upper_lvl_gl_code.getLongName());
			}
			// 2.科目类别不允许维护
			if (oldInfo.getGl_code_type() != subjectInfo.getGl_code_type()) {
				throw GlError.GL.E0014(GlDict.A.gl_code_type.getLongName());
			}
			// 3.科目级别不允许维护
			if (CommUtil.compare(oldInfo.getGl_code_level(), subjectInfo.getGl_code_level()) != 0) {
				throw GlError.GL.E0014(GlDict.A.gl_code_level.getLongName());
			}
			// 4.末级科目标志不允许维护
			if (CommUtil.compare(oldInfo.getEnd_gl_code_ind(), subjectInfo.getEnd_gl_code_ind()) != 0) {
				throw GlError.GL.E0014(GlDict.A.end_gl_code_ind.getLongName());
			}
			// 5.识别码不允许维护
			if (CommUtil.compare(oldInfo.getIdentifier_code(), subjectInfo.getIdentifier_code()) != 0) {
				throw GlError.GL.E0014(GlDict.A.identifier_code.getLongName());
			}
			// 6.余额性质不允许维护
			if (CommUtil.compare(oldInfo.getBal_prop(), subjectInfo.getBal_prop()) != 0) {
				throw GlError.GL.E0014(GlDict.A.bal_prop.getLongName());
			}
			// 7.表内标志不允许维护
			if (oldInfo.getOn_bal_sheet_ind() != subjectInfo.getOn_bal_sheet_ind()) {
				throw GlError.GL.E0014(GlDict.A.on_bal_sheet_ind.getLongName());
			}
			// 8.有效标志不允许维护
			if (oldInfo.getValid_ind() != subjectInfo.getValid_ind()) {
				throw GlError.GL.E0014(GlDict.A.valid_ind.getLongName());
			}
			// 9.创建日期不允许维护
			if (!CommUtil.equals(oldInfo.getCreate_date(), subjectInfo.getCreate_date())) {
				throw GlError.GL.E0014(GlDict.A.create_date.getLongName());
			}
			// 10.简表展示标识不允许维护
			if (CommUtil.compare(oldInfo.getSimple_list_display_ind(), subjectInfo.getSimple_list_display_ind()) != 0) {
				throw GlError.GL.E0014(GlDict.A.simple_list_display_ind.getLongName());
			}

			// 登记审计
			int i = ApDataAudit.regLogOnUpdateParameter(oldInfo, mntInfo);
			if (i == 0) {
				throw ApPubErr.APPUB.E0023(OdbFactory.getTable(fap_accounting_subject.class).getLongname());
			}

			mntInfo.setRecdver(subjectInfo.getRecdver()+1);
			Fap_accounting_subjectDao.updateOne_odb1(mntInfo);

			bizlog.method("modifyBranchSettlement end <<<<<<<<<<<<<<<<<<<<");

		}

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-上午10:00:12</li>
	 *         <li>功能说明：维护多条科目信息</li>
	 *         </p>
	 * @param subjectInfoList
	 *            科目信息明细列表
	 */
	public static void modifySubjectList(List<FaSubjectInfo> subjectInfoList) {

		// 传入的list 科目号+上级科目的升序排序
		CommTools.listSort(subjectInfoList, true, GlDict.A.gl_code.getId(), GlDict.A.upper_lvl_gl_code.getId());

		for (FaSubjectInfo mntinfo : subjectInfoList) {

			modifySubject(mntinfo);

		}
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-上午9:33:10</li>
	 *         <li>功能说明：此方法用于删除一条科目信息</li>
	 *         </p>
	 * @param subjectNo
	 *            科目号
	 * @param dataVersion
	 *            数据版本
	 */
	public static void deleteSubject(String subjectNo, Long dataVersion) {

		// 判断记录是否存在,若不存在报错
		if (!FaAccountingSubject.checkSubjectExists(subjectNo)) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		}

		// 删除科目当年不能有余额
//		faa_account balance = Faa_accountDao.selectOne_odb1(subjectNo, true);
		//查询科目下账户状态为正常的账户数量
		String counts = FaAccountDao.cntNormalAccountCount(CommToolsAplt.prcRunEnvs().getCorpno(),subjectNo, false);
		if(Integer.parseInt(counts)>0){
		    throw GlError.GL.E0206(subjectNo, counts);
		}
		
		String corpno = CommToolsAplt.prcRunEnvs().getCorpno();
		Integer bal = FaAccountDao.selGlCodeBal(subjectNo,corpno, false);
		if (CommUtil.isNotNull(bal) && bal != 0) {
			throw GlError.GL.E0013(subjectNo);
		}
		// 删除科目当年不能有发生额
		if (checkAmount(subjectNo)) {
			// 该科目[${fieldValue}]当年存在发生额,不允许删除
			throw GlError.GL.E0015(subjectNo);
		}

		// 删除的科目必须是末级科目
		fap_accounting_subject deletSub = Fap_accounting_subjectDao.selectOne_odb1(subjectNo, false);
		if (deletSub.getEnd_gl_code_ind() == E_YESORNO.NO) {
			throw GlError.GL.E0012(GlDict.A.end_gl_code_ind.getLongName());
		}
		

		// 版本号非空校验
		CommTools.fieldNotNull(dataVersion, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比数据版本
		if (CommUtil.compare(dataVersion, deletSub.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_accounting_subject.class).getName());
		}

		// 删除数据
		Fap_accounting_subjectDao.deleteOne_odb1(subjectNo);
		// 通过查询上级科目字段是否有与待删除科目的相同,判断该科目是否有同级科目,如果没有相同科目,则修改上级科目的末级科目标志
		String orgId = CommToolsAplt.prcRunEnvs().getCorpno();

		int i = FaParmDao.selJuniorSubjuect(orgId, deletSub.getUpper_lvl_gl_code(), false);
		if (i == 0) {

			fap_accounting_subject oldEndInd = Fap_accounting_subjectDao.selectOne_odb1(deletSub.getUpper_lvl_gl_code(), false);
			if(CommUtil.isNull(oldEndInd)) {//上级科目为空的时候不处理（即不更新上级科目是否末级科目标志）
				return;
			}
			fap_accounting_subject newEndInd = CommTools.clone(fap_accounting_subject.class, oldEndInd);
			newEndInd.setEnd_gl_code_ind(E_YESORNO.YES);

			// 登记审计
			ApDataAudit.regLogOnUpdateBusiness(oldEndInd, newEndInd);
			Fap_accounting_subjectDao.updateOne_odb1(newEndInd);

				}
			// 登记审计
			ApDataAudit.regLogOnDeleteParameter(deletSub);

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-上午9:41:37</li>
	 *         <li>功能说明：此方法用于删除一条或多条科目信息</li>
	 *         </p>
	 * @param subjectInfoList
	 *            科目信息明细列表
	 */
	public static void deleteSubjectList(List<FaSubjectInfo> subjectInfoList) {

		// 传入的list 科目号+上级科目的降序排序
		CommTools.listSort(subjectInfoList, false, GlDict.A.gl_code.getId(), GlDict.A.upper_lvl_gl_code.getId());

		for (FaSubjectInfo info : subjectInfoList) {
			deleteSubject(info.getGl_code(), info.getRecdver());
		}

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月3日-下午2:28:28</li>
	 *         <li>功能说明：检查该科目当年是否有发生额</li>
	 *         </p>
	 * @param subjectNo
	 * @return 没有记录返回false，存在返回true。
	 */
	private static boolean checkAmount(String subjectNo) {
		String corpno = CommToolsAplt.prcRunEnvs().getCorpno();
		// 没有记录返回false，存在返回true。
		/*return (FaParmDao.selSubjectByDate(corpno,subjectNo,
				DateUtil.formatDate(DateTools2.getYearFirt(DateUtil.getNow()),"yyyyMMdd"),  
				CommToolsAplt.prcRunEnvs().getTrandt(), false) == null) ? false : true;*/
		Integer cnt = FaParmDao.selSubjectCntByDate(corpno,subjectNo,
				DateUtil.formatDate(DateTools2.getYearFirt(new Date()),"yyyyMMdd"),  
				CommToolsAplt.prcRunEnvs().getTrandt(), false);
		if(CommUtil.isNull(cnt) || cnt == 0) {
			return false;
		}
		return true;
	}

}
