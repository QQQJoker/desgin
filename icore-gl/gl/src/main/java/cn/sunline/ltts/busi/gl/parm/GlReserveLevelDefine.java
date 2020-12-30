package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.parm.FaAccountingSubject;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.gl.namedsql.GlParmDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_level_defineDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_level_define;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveLevelInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_DEPOSITPAIDBUSIPROP;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveLevelDefine {

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-上午10:55:31</li>
	 *         <li>功能说明：检查不为空的字段</li>
	 *         </p>
	 * @param levelInfo
	 *            缴存层级定义的复合类型
	 */
	public static void checkNull(GlReserveLevelInfo levelInfo) {

		CommTools.fieldNotNull(levelInfo.getDeposit_level(), GlDict.A.deposit_level.getId(), GlDict.A.deposit_level.getLongName());
		CommTools.fieldNotNull(levelInfo.getReserve_type(), GlDict.A.reserve_type.getId(), GlDict.A.reserve_type.getLongName());
		CommTools.fieldNotNull(levelInfo.getDeposit_busi_type(), GlDict.A.deposit_level.getId(), GlDict.A.deposit_level.getLongName());
		CommTools.fieldNotNull(levelInfo.getDeposit_processing_category(), GlDict.A.deposit_processing_category.getId(), GlDict.A.deposit_processing_category.getLongName());
		// 当缴存款处理类别等于2时,科目号和上级科目必须输入
		if (CommUtil.compare(levelInfo.getDeposit_processing_category().getValue(), "2") == 0) {
			CommTools.fieldNotNull(levelInfo.getAccounting_gl_code(), GlDict.A.accounting_gl_code.getId(), GlDict.A.accounting_gl_code.getLongName());
			CommTools.fieldNotNull(levelInfo.getParent_accounting_gl_code(), GlDict.A.parent_accounting_gl_code.getId(), GlDict.A.parent_accounting_gl_code.getLongName());
		}

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-上午11:13:19</li>
	 *         <li>功能说明：检查指定教训层级信息是否存在</li>
	 *         </p>
	 * @param deposit_paid_level
	 *            缴存层级
	 * @param reserve_type
	 *            准备金种类
	 * @param deposit_busi_type
	 *            缴存款业务性质
	 * @return 没有记录返回false，存在返回true。
	 */
	public static boolean checkExists(Long depositPaidLevel, E_RESERVEAMTTYPE reserveType, E_DEPOSITPAIDBUSIPROP depositBusiType) {
		glp_reserve_level_define info = Glp_reserve_level_defineDao.selectOne_odb1(depositPaidLevel, reserveType, depositBusiType, false);

		// 没有记录返回false，存在返回true。
		return (info == null) ? false : true;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-上午11:44:06</li>
	 *         <li>功能说明：检查合法性</li>
	 *         </p>
	 * @param levelInfo
	 *            缴存层级定义的复合类型
	 */
	public static void checkVadility(GlReserveLevelInfo levelInfo) {
		// 当科目号不为空时,科目号需存在
		if (levelInfo.getAccounting_gl_code() != null) {
			if (!FaAccountingSubject.checkSubjectExists(levelInfo.getAccounting_gl_code())) {
				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.accounting_gl_code.getId(), levelInfo.getAccounting_gl_code());
			}
		}
		if (levelInfo.getParent_accounting_gl_code() != null) {
			if (!FaAccountingSubject.checkSubjectExists(levelInfo.getParent_accounting_gl_code())) {
				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.parent_accounting_gl_code.getId(), levelInfo.getParent_accounting_gl_code());
			}
		}

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-上午11:45:08</li>
	 *         <li>功能说明 : 查询指定记录</li>
	 *         </p>
	 * @param deposit_paid_level
	 *            缴存层级
	 * @param reserve_type
	 *            reserve_type 缴存款处理类别
	 * @param deposit_busi_type
	 *            缴存款业务性质
	 * @return 缴存层级定义的复合类型
	 */
	public static GlReserveLevelInfo getGlReserveLevelInfo(Long depositPaidLevel, E_RESERVEAMTTYPE reserveType, E_DEPOSITPAIDBUSIPROP depositBusiType) {

		// 查询指定记录是否存在
		if (!checkExists(depositPaidLevel, reserveType, depositBusiType)) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_level_define.class).getLongname(), GlDict.A.deposit_level.getId(), depositPaidLevel.toString(), GlDict.A.reserve_type.getId(),
					reserveType.getValue(), GlDict.A.deposit_busi_type.getId(), depositBusiType.getValue());
		}

		glp_reserve_level_define tableInfo = Glp_reserve_level_defineDao.selectOne_odb1(depositPaidLevel, reserveType, depositBusiType, false);

		GlReserveLevelInfo info = SysUtil.getInstance(GlReserveLevelInfo.class);

		info.setDeposit_level(tableInfo.getDeposit_level()); // 缴存层级
		info.setReserve_type(tableInfo.getReserve_type()); // 准备金种类
		info.setDeposit_busi_type(tableInfo.getDeposit_busi_type()); // 缴存款业务性质
		info.setDeposit_processing_category(tableInfo.getDeposit_processing_category()); // 缴存款处理类别
		info.setAccounting_gl_code(tableInfo.getAccounting_gl_code()); // 记账科目号
		info.setParent_accounting_gl_code(tableInfo.getParent_accounting_gl_code()); // 上级记账科目号
		info.setCorpno(tableInfo.getCorpno()); // 法人代码
		info.setRecdver(tableInfo.getRecdver()); // 数据版本号

		return info;

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午1:06:45</li>
	 *         <li>功能说明：查询明细列表</li>
	 *         </p>
	 * @param levelInfo
	 *            缴存层级定义的复合类型
	 * @return 明细列表
	 */
	public static Options<GlReserveLevelInfo> queryGlReserveLevelInfo(GlReserveLevelInfo levelInfo) {

		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<GlReserveLevelInfo> page = GlParmDao.lstAllLevelDefine(orgId, levelInfo.getDeposit_level(), 
				levelInfo.getReserve_type(), levelInfo.getDeposit_busi_type(), levelInfo.getDeposit_processing_category(), 
				levelInfo.getAccounting_gl_code(), levelInfo.getParent_accounting_gl_code(), 
				(pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);

		Options<GlReserveLevelInfo> list = new DefaultOptions<GlReserveLevelInfo>();
		runEnvs.setCounts(page.getRecordCount());
		list.setValues(page.getRecords());

		return list;

	}

}
