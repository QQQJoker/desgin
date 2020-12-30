package cn.sunline.ltts.busi.fa.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaParmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_sys_defineDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_sys_define;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaSysDefineInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaSysDefine {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaSysDefine.class);

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-下午4:13:45</li>
	 *         <li>功能说明：检查传入值的空值合法性,不充许为空的字段若没上送需报错</li>
	 *         </p>
	 * @param sysDefineInfo
	 *            表fap_sys_define的符合类型
	 */
	public static void checkSysDefineNull(FaSysDefineInfo sysDefineInfo) {

		CommTools.fieldNotNull(sysDefineInfo.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
		CommTools.fieldNotNull(sysDefineInfo.getSys_name(), GlDict.A.sys_name.getId(), GlDict.A.sys_name.getLongName());
		CommTools.fieldNotNull(sysDefineInfo.getSystem_service_status(), GlDict.A.system_service_status.getId(), GlDict.A.system_service_status.getLongName());
		CommTools.fieldNotNull(sysDefineInfo.getSystem_date(), GlDict.A.system_date.getId(), GlDict.A.system_date.getLongName());
		CommTools.fieldNotNull(sysDefineInfo.getBusi_seq_format(), GlDict.A.busi_seq_format.getId(), GlDict.A.busi_seq_format.getLongName());
		CommTools.fieldNotNull(sysDefineInfo.getLedger_clearing_gl_code(), GlDict.A.ledger_clearing_gl_code.getId(), GlDict.A.ledger_clearing_gl_code.getLongName());
		CommTools.fieldNotNull(sysDefineInfo.getError_gl_code(), GlDict.A.error_gl_code.getId(), GlDict.A.error_gl_code.getLongName());
		CommTools.fieldNotNull(sysDefineInfo.getRemark(), GlDict.A.remark.getId(), GlDict.A.remark.getLongName());
		
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-下午4:14:34</li>
	 *         <li>功能说明：此方法用于判断该系统定义信息是否在数据库中存在</li>
	 *         </p>
	 * @param sysNo
	 *            表fap_sys_define的主键
	 * @return 没有记录返回false，存在返回true。
	 */
	public static boolean checkSysDefineExists(String sysNo) {

		fap_sys_define info = Fap_sys_defineDao.selectOne_odb1(sysNo, false);
		// 没有记录返回false，存在返回true。
		return (info == null) ? false : true;

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-下午4:16:12</li>
	 *         <li>功能说明：核对上送的系统定义信息中的每个字段值的合法性</li>
	 *         </p>
	 * @param sysDefineInfo
	 *            表fap_sys_define的复合类型
	 */
	public static void checkSysDefineValidity(FaSysDefineInfo sysDefineInfo) {

		// 判断下拉列表是否存在
		//ApDropList.exists(FaConst.SYS_NO, sysDefineInfo.getSys_no(), true);

		// 检查上送的与总账往来科目、差错补偿科目必须在fap_accounting_subject存在,不存在报错
		if (!FaAccountingSubject.checkSubjectExists(sysDefineInfo.getLedger_clearing_gl_code())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.error_gl_code.getId(), sysDefineInfo.getLedger_clearing_gl_code());
		}

		if (!FaAccountingSubject.checkSubjectExists(sysDefineInfo.getError_gl_code())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.error_gl_code.getId(), sysDefineInfo.getError_gl_code());
		}

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-下午4:16:43</li>
	 *         <li>功能说明：根据主键字段值获取对应的系统定义信息</li>
	 *         </p>
	 * @param sysNo
	 *            表fap_sys_define的主键
	 * @return 表fap_sys_define的符合类型
	 */
	public static FaSysDefineInfo getSysDefine(String sysNo) {

		// 根据主键检查信息是否存在
		if (!checkSysDefineExists(sysNo)) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_sys_define.class).getLongname(), GlDict.A.sys_no.getId(), sysNo);
		}

		fap_sys_define tableInfo = Fap_sys_defineDao.selectOne_odb1(sysNo, false);

		FaSysDefineInfo info = SysUtil.getInstance(FaSysDefineInfo.class);

		// f赋值
		info.setCorpno(tableInfo.getCorpno());// 法人代码
		info.setSys_no(tableInfo.getSys_no()); // 系统编号
		info.setSys_name(tableInfo.getSys_name()); // 系统名称
		info.setSystem_service_status(tableInfo.getSystem_service_status()); // 系统服务状态
		info.setSystem_date(tableInfo.getSystem_date()); // 系统日期
		info.setBusi_seq_format(tableInfo.getBusi_seq_format()); // 业务流水组织形式
		info.setLedger_clearing_gl_code(tableInfo.getLedger_clearing_gl_code()); // 与总账往来科目
		info.setError_gl_code(tableInfo.getError_gl_code()); // 差错补偿科目
		info.setRemark(tableInfo.getRemark()); // 备注
		info.setRecdver(tableInfo.getRecdver());

		return info;

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月6日-下午1:43:34</li>
	 *         <li>功能说明：系统定义信息列表查询</li>
	 *         </p>
	 * @param sysDefineInfo
	 * @return
	 */
	public static Options<FaSysDefineInfo> querySysDefineList(FaSysDefineInfo sysDefineInfo) {

		bizlog.method(" FaSysDefine.querySysDefineList begin >>>>>>>>>>>>>>>>");
		bizlog.debug("brchSetLent=[%s]", sysDefineInfo);

		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		Page<FaSysDefineInfo> page = FaParmDao.lstSysDefineList(runEnvs.getCorpno(), sysDefineInfo.getSys_no(), sysDefineInfo.getSys_name(),
				sysDefineInfo.getSystem_service_status(), sysDefineInfo.getSystem_date(), sysDefineInfo.getBusi_seq_format(), sysDefineInfo.getLedger_clearing_gl_code(),
				sysDefineInfo.getError_gl_code(), sysDefineInfo.getRemark(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);

		Options<FaSysDefineInfo> queryList = new DefaultOptions<FaSysDefineInfo>();
		queryList.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

		return queryList;

	}

}
