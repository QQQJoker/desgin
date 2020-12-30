package cn.sunline.ltts.busi.fa.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaParmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaSubjectInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaAccountingSubject {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaBranchAgentMnt.class);

	/**
	 * @Author dengyu
	 *         <p>
	 *         <li>2017年2月28日-上午11:31:41</li>
	 *         <li>功能说明：检查上送的科目信息中空值的合法性</li>
	 *         </p>
	 * @param subjectInfo
	 */
	public static void checkSubjectNull(FaSubjectInfo subjectInfo) {

		CommTools.fieldNotNull(subjectInfo.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		CommTools.fieldNotNull(subjectInfo.getGl_code_desc(), GlDict.A.gl_code_desc.getId(), GlDict.A.gl_code_desc.getLongName());
		CommTools.fieldNotNull(subjectInfo.getGl_code_type(), GlDict.A.gl_code_type.getId(), GlDict.A.gl_code_type.getLongName());
		CommTools.fieldNotNull(subjectInfo.getGl_code_level(), GlDict.A.gl_code_level.getId(), GlDict.A.gl_code_level.getLongName());
		CommTools.fieldNotNull(subjectInfo.getEnd_gl_code_ind(), GlDict.A.end_gl_code_ind.getId(), GlDict.A.end_gl_code_ind.getLongName());
//		CommTools.fieldNotNull(subjectInfo.getIdentifier_code(), GlDict.A.identifier_code.getId(), GlDict.A.identifier_code.getLongName());
		CommTools.fieldNotNull(subjectInfo.getBal_prop(), GlDict.A.bal_prop.getId(), GlDict.A.bal_prop.getLongName());
		CommTools.fieldNotNull(subjectInfo.getOn_bal_sheet_ind(), GlDict.A.on_bal_sheet_ind.getId(), GlDict.A.on_bal_sheet_ind.getLongName());
		CommTools.fieldNotNull(subjectInfo.getSimple_list_display_ind(), GlDict.A.simple_list_display_ind.getId(), GlDict.A.simple_list_display_ind.getLongName());
		CommTools.fieldNotNull(subjectInfo.getBal_check_ind(), GlDict.A.bal_check_ind.getId(), GlDict.A.bal_check_ind.getLongName());
		CommTools.fieldNotNull(subjectInfo.getDebit_manual_allow(), GlDict.A.debit_manual_allow.getId(), GlDict.A.debit_manual_allow.getLongName());
		CommTools.fieldNotNull(subjectInfo.getCredit_manual_allow(), GlDict.A.credit_manual_allow.getId(), GlDict.A.credit_manual_allow.getLongName());

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-上午9:24:16</li>
	 *         <li>功能说明：此方法用于判断科目信息是否在数据库中存在。</li>
	 *         </p>
	 * @param subjectNo
	 * @return 没有记录返回false 有记录返回true
	 */
	public static boolean checkSubjectExists(String subjectNo) {

		fap_accounting_subject info = Fap_accounting_subjectDao.selectOne_odb1(subjectNo, false);

		// 没有记录返回false，存在返回true。
		return (info == null) ? false : true;

	}

	/**
	 * @Author dengyu
	 *         <p>
	 *         <li>2017年2月28日-上午11:34:00</li>
	 *         <li>功能说明：此方法用于核对上送的科目信息中的每个字段值的合法性,包括为空值检查 存在性检查</li>
	 *         </p>
	 * @param subjectInfo
	 */
	public static void checkSubjecttValidity(FaSubjectInfo subjectInfo) {

		// 空值合法性
		FaAccountingSubject.checkSubjectNull(subjectInfo);

		// 科目号的值、长度、科目级别必须符合4+2模式
		/*int subjlen = subjectInfo.getGl_code().length();
		int subjlevel = subjectInfo.getGl_code_level().intValue();

		if (subjlen != 2 + subjlevel * 2) {
			throw GlError.GL.E0009(subjectInfo.getGl_code());
		}
		 
		// 若科目非一级科目时：则上级科目不能为空，上级科目必须是上一级科目号，并且上级科目必须在fap_accounting_subject表存在
		if (subjlevel != 1) {
			// 上级科目不为空
			CommTools.fieldNotNull(subjectInfo.getUpper_lvl_gl_code(), GlDict.A.upper_lvl_gl_code.getId(), GlDict.A.upper_lvl_gl_code.getLongName());

			// 上级科目必须是上一级科目号
			if (subjectInfo.getGl_code().substring(0, subjlevel * 2 - 1).compareTo(subjectInfo.getUpper_lvl_gl_code()) != 0) {
				throw GlError.GL.E0010(subjectInfo.getUpper_lvl_gl_code());
			}
		 	
			// 上级科目必须在fap_accounting_subject表存在
			if (!checkSubjectExists(subjectInfo.getUpper_lvl_gl_code())) {

				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), subjectInfo.getUpper_lvl_gl_code());
			}

			// 科目类别、余额性质、表内标志、简表展示标识必须与其上级科目保持一致。
			fap_accounting_subject info = Fap_accounting_subjectDao.selectOne_odb1(subjectInfo.getUpper_lvl_gl_code(), false);

			if( info == null){
				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getLongName(), subjectInfo.getUpper_lvl_gl_code());

			}
			if (subjectInfo.getGl_code_type() != info.getGl_code_type()) {

				throw GlError.GL.E0011(GlDict.A.gl_code_type.getLongName(),info.getGl_code_type().toString(),subjectInfo.getGl_code_type().toString());

			}
			if (subjectInfo.getBal_prop() != info.getBal_prop()) {

				throw GlError.GL.E0011(GlDict.A.bal_prop.getLongName(),info.getBal_prop().toString(),subjectInfo.getBal_prop().toString());

			}
			if (subjectInfo.getOn_bal_sheet_ind() != info.getOn_bal_sheet_ind()) {

				throw GlError.GL.E0011(GlDict.A.on_bal_sheet_ind.getLongName(),info.getOn_bal_sheet_ind().toString(),subjectInfo.getOn_bal_sheet_ind().toString());

			}
			if (subjectInfo.getSimple_list_display_ind() != info.getSimple_list_display_ind()) {

				throw GlError.GL.E0011(GlDict.A.simple_list_display_ind.getLongName(),info.getSimple_list_display_ind().toString(),subjectInfo.getSimple_list_display_ind().toString());

			}

		}
		*/
		int subjlevel = subjectInfo.getGl_code_level().intValue();
		if (subjlevel != 1) {
			// 上级科目不为空
			CommTools.fieldNotNull(subjectInfo.getUpper_lvl_gl_code(), GlDict.A.upper_lvl_gl_code.getId(), GlDict.A.upper_lvl_gl_code.getLongName());
		 	
			// 上级科目必须在fap_accounting_subject表存在
			if (!checkSubjectExists(subjectInfo.getUpper_lvl_gl_code())) {
				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), subjectInfo.getUpper_lvl_gl_code());
			}

			// 科目类别、余额性质、表内标志、简表展示标识必须与其上级科目保持一致。
			fap_accounting_subject info = Fap_accounting_subjectDao.selectOne_odb1(subjectInfo.getUpper_lvl_gl_code(), false);
			if( info == null){
				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getLongName(), subjectInfo.getUpper_lvl_gl_code());
			}
			if (subjectInfo.getGl_code_type() != info.getGl_code_type()) {
				throw GlError.GL.E0011(GlDict.A.gl_code_type.getLongName(),info.getGl_code_type().toString(),subjectInfo.getGl_code_type().toString());
			}
			if (subjectInfo.getBal_prop() != info.getBal_prop()) {
				throw GlError.GL.E0011(GlDict.A.bal_prop.getLongName(),info.getBal_prop().toString(),subjectInfo.getBal_prop().toString());
			}
			if (subjectInfo.getOn_bal_sheet_ind() != info.getOn_bal_sheet_ind()) {
				throw GlError.GL.E0011(GlDict.A.on_bal_sheet_ind.getLongName(),info.getOn_bal_sheet_ind().toString(),subjectInfo.getOn_bal_sheet_ind().toString());
			}
			if (subjectInfo.getSimple_list_display_ind() != info.getSimple_list_display_ind()) {
				throw GlError.GL.E0011(GlDict.A.simple_list_display_ind.getLongName(),info.getSimple_list_display_ind().toString(),subjectInfo.getSimple_list_display_ind().toString());
			}
		} else {//一级科目没有不需要上级科目
			if(CommUtil.isNotNull(subjectInfo.getUpper_lvl_gl_code())) {
				throw GlError.GL.E0010(subjectInfo.getUpper_lvl_gl_code());
			}
		}
		

		// 允许记账的系统必须在充许的下拉列表清单中。
		//ApDropList.exists(FaConst.SYS_NO, subjectInfo.getAllow_accounting_sys(), true);

		// 当对开标志等于Y时：对方科目、对开账户识别方式不能为空，并且对方科目必须在fap_accounting_subject表存在
		if (subjectInfo.getOpp_open_ind() == E_YESORNO.YES) {

			CommTools.fieldNotNull(subjectInfo.getOffset_gl_code(), GlDict.A.offset_gl_code.getId(), GlDict.A.offset_gl_code.getLongName());
			CommTools.fieldNotNull(subjectInfo.getOpp_open_way(), GlDict.A.opp_open_way.getId(), GlDict.A.opp_open_way.getLongName());
			if (!checkSubjectExists(subjectInfo.getOffset_gl_code())) {

				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.offset_gl_code.getId(), subjectInfo.getOffset_gl_code());

			}
		}

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月3日-上午11:26:05</li>
	 *         <li>功能说明：根据科目号获取科目详细信息</li>
	 *         </p>
	 * @param subjectNo
	 *            科目号
	 * @return
	 */
	public static FaSubjectInfo getSubjectInfo(String subjectNo) {

		// 检查科目信息是否在数据库中存在
		if (!checkSubjectExists(subjectNo)) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getLongName(), subjectNo);
		}

		// 取出待查询的记录
		fap_accounting_subject tableInfo = Fap_accounting_subjectDao.selectOne_odb1(subjectNo, false);

		FaSubjectInfo info = SysUtil.getInstance(FaSubjectInfo.class);

		info.setGl_code(tableInfo.getGl_code()); // 科目号
		info.setGl_code_desc(tableInfo.getGl_code_desc()); // 科目名称
		info.setUpper_lvl_gl_code(tableInfo.getUpper_lvl_gl_code()); // 上级科目
		info.setGl_code_type(tableInfo.getGl_code_type()); // 科目类别
		info.setGl_code_level(tableInfo.getGl_code_level()); // 科目级别
		info.setEnd_gl_code_ind(tableInfo.getEnd_gl_code_ind()); // 末层科目标志
		info.setIdentifier_code(tableInfo.getIdentifier_code()); // 识别码
		info.setBal_prop(tableInfo.getBal_prop()); // 余额性质
		info.setOn_bal_sheet_ind(tableInfo.getOn_bal_sheet_ind()); // 表内标志
		info.setSimple_list_display_ind(tableInfo.getSimple_list_display_ind()); // 简表展示标识
		info.setBal_check_ind(tableInfo.getBal_check_ind()); // 余额检查标志
		info.setDebit_manual_allow(tableInfo.getDebit_manual_allow()); // 借方手工记账许可
		info.setCredit_manual_allow(tableInfo.getCredit_manual_allow()); // 贷方手工记账许可
		info.setManual_open_acct_mode(tableInfo.getManual_open_acct_mode()); // 手工开户受理模式
		info.setAllow_accounting_sys(tableInfo.getAllow_accounting_sys()); // 允许记账的系统
		info.setValid_ind(tableInfo.getValid_ind()); // 有效标志
		info.setOpp_open_ind(tableInfo.getOpp_open_ind()); // 对开标志
		info.setOffset_gl_code(tableInfo.getOffset_gl_code()); // 对方科目
		info.setOpp_open_way(tableInfo.getOpp_open_way()); // 对开账户识别方式
		info.setTmstmp(tableInfo.getTmstmp()); 
		info.setCorpno(tableInfo.getCorpno());// 法人代码
		info.setRecdver(tableInfo.getRecdver());//数据版本
		info.setCreate_date(tableInfo.getCreate_date());// 建立日期

		return info;

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月3日-上午11:22:48</li>
	 *         <li>功能说明：科目信息列表查询</li>
	 *         </p>
	 * @param subjectInfo
	 * @return
	 */
	public static Options<FaSubjectInfo> querySubjectList(FaSubjectInfo subjectInfo) {

		bizlog.method(" querySubjectList begin >>>>>>>>>>>>>>>>");
		bizlog.debug("subjectInfo[%s]", subjectInfo);

		// 取得法人代码
		//String orgId = ApOrg.getReferenceOrgId(app_business_parameter.class);
		// 取得公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<FaSubjectInfo> page = FaParmDao.lstSubInfo(orgId, subjectInfo.getGl_code(), subjectInfo.getGl_code_desc(), 
				subjectInfo.getUpper_lvl_gl_code(), subjectInfo.getGl_code_type(), subjectInfo.getGl_code_level(), 
				subjectInfo.getEnd_gl_code_ind(), subjectInfo.getIdentifier_code(), subjectInfo.getBal_prop(),
				subjectInfo.getOn_bal_sheet_ind(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		
		runEnvs.setCounts(page.getRecordCount());
		Options<FaSubjectInfo> queryList = new DefaultOptions<FaSubjectInfo>();
		queryList.setValues(page.getRecords());
	
		return queryList;

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月4日-下午2:58:16</li>
	 *         <li>功能说明：判断该科目是否允许记账</li>
	 *         </p>
	 * @param subjNo
	 *            科目号
	 * @param sysNo
	 *            允许记账的系统号
	 */
	public static void checkSubjectAccounting(String subjNo, String sysNo) {

		fap_accounting_subject info = Fap_accounting_subjectDao.selectOne_odb1(subjNo, false);

		// 检查科目是否存在
		if (info == null) {

			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getLongName(), subjNo);

		}
		// 只有末级科目允许记账
		if (info.getEnd_gl_code_ind() == E_YESORNO.NO) {
			
			throw GlError.GL.E0038(subjNo);
			
		}
		
		//当允许记账的标志不存在下拉列表中的时候不允许记账, 为空允许记账
		if (CommUtil.isNotNull(info.getAllow_accounting_sys()) && CommUtil.compare(info.getAllow_accounting_sys(), sysNo) !=0) {
			
			throw GlError.GL.E0039(subjNo,sysNo,info.getAllow_accounting_sys());
			
		}
		//有效标志为N时 不允许记账
		if (info.getValid_ind() == E_YESORNO.NO) {

			throw GlError.GL.E0040(subjNo);
			
		}
		//对开科目标志为Y 时 不允许记账 
		if (info.getOpp_open_ind() == E_YESORNO.YES) {
			throw GlError.GL.E0035(subjNo);
		}

	}

}
