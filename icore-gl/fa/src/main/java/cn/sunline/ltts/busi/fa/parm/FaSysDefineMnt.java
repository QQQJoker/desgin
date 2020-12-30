package cn.sunline.ltts.busi.fa.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_sys_defineDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_branch_agent;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_sys_define;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaSysDefineInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaSysDefineMnt {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaSysDefineMnt.class);

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-下午4:10:20</li>
	 *         <li>功能说明：增加一条系统定义</li>
	 *         </p>
	 * @param FaSysDefineInfo
	 *            表fap_sys_define的复合类型
	 */
	public static void addSysDefine(FaSysDefineInfo sysDefineInfo) {

		// 判断不允许为空的字段是否合法
		FaSysDefine.checkSysDefineNull(sysDefineInfo);

		// 判断主键唯一索引是否存在,存在报错
		if ( FaSysDefine.checkSysDefineExists(sysDefineInfo.getSys_no())) {
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(fap_sys_define.class).getLongname(), GlDict.A.sys_no.getLongName());
		}

		// 检查合法性
		FaSysDefine.checkSysDefineValidity(sysDefineInfo);

		fap_sys_define info = SysUtil.getInstance(fap_sys_define.class);

		// 插入数据
		info.setSys_no(sysDefineInfo.getSys_no()); // 系统编号
		info.setSys_name(sysDefineInfo.getSys_name()); // 系统名称
		info.setSystem_service_status(sysDefineInfo.getSystem_service_status()); // 系统服务状态
		info.setSystem_date(sysDefineInfo.getSystem_date()); // 系统日期
		info.setBusi_seq_format(sysDefineInfo.getBusi_seq_format()); // 业务流水组织形式
		info.setLedger_clearing_gl_code(sysDefineInfo.getLedger_clearing_gl_code()); // 与总账往来科目
		info.setError_gl_code(sysDefineInfo.getError_gl_code()); // 差错补偿科目
		info.setRemark(sysDefineInfo.getRemark()); // 备注
		info.setRecdver(1l);//版本号默认为1

		Fap_sys_defineDao.insert(info);

		// 登记审计
		ApDataAudit.regLogOnInsertParameter(info);

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-下午4:10:42</li>
	 *         <li>功能说明：维护一条系统定义</li>
	 *         </p>
	 * @param FaSysDefineInfo
	 *            表fap_sys_define的复合类型
	 */
	public static void modifySysDefine(FaSysDefineInfo sysDefineInfo) {

		// 检查不允许为空的字段
		FaSysDefine.checkSysDefineNull(sysDefineInfo);

		// 检查字段合法性
		FaSysDefine.checkSysDefineValidity(sysDefineInfo);

		// 找出原有数据
		fap_sys_define oldInfo = Fap_sys_defineDao.selectOne_odb1(sysDefineInfo.getSys_no(), true);

		// 将原纪录赋值给新表，对新表进行操作
		fap_sys_define mntInfo = CommTools.clone(fap_sys_define.class, oldInfo);

		// 对比版本号
		if (CommUtil.compare(mntInfo.getRecdver(), sysDefineInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_sys_define.class).getName());
		}

		// 对比不允许修改的数据
		unableModify(sysDefineInfo, mntInfo);
		
		//更新数据
		mntInfo.setSys_name(sysDefineInfo.getSys_name());//修改系统名称
		mntInfo.setSystem_date(sysDefineInfo.getSystem_date());//修改系统日期
		mntInfo.setLedger_clearing_gl_code(sysDefineInfo.getLedger_clearing_gl_code());//修改来往总账科目
		mntInfo.setRemark(sysDefineInfo.getRemark());//修改备注
		
		//登记审计
		int i = ApDataAudit.regLogOnUpdateBusiness(oldInfo, mntInfo);
		if (i == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(faa_account.class).getLongname());
		}
		
		Fap_sys_defineDao.updateOne_odb1(mntInfo);
		
		bizlog.method("modifySysDefine end>>>>>>>>>>>>>>>>>>");
		
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-下午4:11:02</li>
	 *         <li>功能说明：删除一条系统定义</li>
	 *         </p>
	 * @param sysNo
	 *            主键
	 */
	public static void deleteSysDefine(String sysNo, Long dataVersion) {

		// 不存在记录报错
		if (!FaSysDefine.checkSysDefineExists(sysNo)) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_sys_define.class).getLongname(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
		}

		// 版本号非空校验
		CommTools.fieldNotNull(dataVersion, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		fap_sys_define deletInfo = Fap_sys_defineDao.selectOne_odb1(sysNo, false);
		if (CommUtil.compare(dataVersion, deletInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_branch_agent.class).getName());
		}

		// 根据主键删除一条记录
		Fap_sys_defineDao.deleteOne_odb1(sysNo);

		// 登记审计
		ApDataAudit.regLogOnDeleteParameter(deletInfo);

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月2日-下午7:53:33</li>
	 *         <li>功能说明：判断不允许维护的字段</li>
	 *         </p>
	 * @param sysDefineInfo 输入的系统定义复合类型
	 * @param mntInfo 待修改的系统定义复合类型
	 */
	private static void unableModify(FaSysDefineInfo sysDefineInfo, fap_sys_define mntInfo) {

		// 系统服务状态不允许维护
		if (CommUtil.compare(sysDefineInfo.getSystem_service_status(), mntInfo.getSystem_service_status()) != 0) {
			throw GlError.GL.E0014(GlDict.A.system_service_status.getLongName());
		}

		// 业务流水组织形式
		if (CommUtil.compare(sysDefineInfo.getBusi_seq_format(), mntInfo.getBusi_seq_format()) != 0) {
			throw GlError.GL.E0014(GlDict.A.busi_seq_format.getLongName());			
		}

		// 差错补偿科目
		if (CommUtil.compare(sysDefineInfo.getError_gl_code(), mntInfo.getError_gl_code()) != 0) {
			throw GlError.GL.E0014(GlDict.A.error_gl_code.getLongName());			
		}


	}

}
