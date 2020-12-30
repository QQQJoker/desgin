package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcy;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.parm.FaAccountingSubject;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.gl.namedsql.GlParmDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_indexDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_index;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_percent;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveIndexInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveIndex {

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午7:16:19</li>
	 *         <li>功能说明：非空字段的检查</li>
	 *         </p>
	 * @param indexInfo
	 *            准备金缴存指标相关复合类型
	 */
	public static void checkNull(GlReserveIndexInfo indexInfo) {

		CommTools.fieldNotNull(indexInfo.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		CommTools.fieldNotNull(indexInfo.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		CommTools.fieldNotNull(indexInfo.getGl_code(), GlDict.A.reserve_type.getId(), GlDict.A.reserve_type.getLongName());
		CommTools.fieldNotNull(indexInfo.getCalc_factor(), GlDict.A.calc_factor.getId(), GlDict.A.calc_factor.getLongName());
		CommTools.fieldNotNull(indexInfo.getPaired_gl_code(), GlDict.A.paired_gl_code.getId(), GlDict.A.paired_gl_code.getLongName());
		CommTools.fieldNotNull(indexInfo.getReserve_percent(), GlDict.A.reserve_percent.getId(), GlDict.A.reserve_percent.getLongName());

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午7:20:23</li>
	 *         <li>功能说明：检查指定记录是否存在</li>
	 *         </p>
	 * @param ccy_code
	 *            货币代码
	 * @param subject_no
	 *            科目号
	 * @param reserve_type
	 *            准备金缴存种类
	 * @return 没有记录返回false，存在返回true。
	 */
	public static boolean checkExsits(String ccyCode, String subjectNo, E_RESERVEAMTTYPE reserveType) {
		glp_reserve_index info = Glp_reserve_indexDao.selectOne_odb1(ccyCode, subjectNo, reserveType, false);

		// 没有记录返回false，存在返回true。
		return (info == null) ? false : true;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午7:22:12</li>
	 *         <li>功能说明：检查传入字段的合法性</li>
	 *         </p>
	 * @param indexInfo
	 *            准备金缴存指标相关复合类型
	 */
	public static void checkValidity(GlReserveIndexInfo indexInfo) {

		// 货币代码必须在app_currency中存在
		if (!CommTools.existsApCurrency(indexInfo.getCcy_code())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppCrcy.class).getLongname(), GlDict.A.ccy_code.getLongName(), indexInfo.getCcy_code());
		}
		// 科目号必须在fap_accounting_subject中存在
		if (!FaAccountingSubject.checkSubjectExists(indexInfo.getGl_code())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(fap_accounting_subject.class).getLongname(), GlDict.A.gl_code.getId(), indexInfo.getGl_code());
		}
		if(indexInfo.getReserve_percent().doubleValue() > 100 || indexInfo.getReserve_percent().doubleValue() <= 0) {
			throw GlError.GL.E0214();
		}
		// TODO
		// reserve_percent 字段需要在 表glp_reserve_percent 中存在?
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-上午9:52:19</li>
	 *         <li>功能说明：查询指定缴存指标信息</li>
	 *         </p>
	 * @param ccy_code
	 *            货币代码
	 * @param subject_no
	 *            科目号
	 * @param reserve_type
	 *            缴存类型
	 * @return
	 */
	public static GlReserveIndexInfo getGlReserveIndexInfo(String ccyCode, String glCode, E_RESERVEAMTTYPE reserveType) {

		// 检查指定记录是否存在,不存在报错
		if (!checkExsits(ccyCode, glCode, reserveType)) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_percent.class).getLongname(), GlDict.A.ccy_code.getId(), ccyCode, GlDict.A.gl_code.getId(), glCode,
					GlDict.A.reserve_type.getId(), reserveType.getValue());
		}

		glp_reserve_index tableInfo = Glp_reserve_indexDao.selectOne_odb1(ccyCode, glCode, reserveType, false);

		GlReserveIndexInfo info = SysUtil.getInstance(GlReserveIndexInfo.class);

		info.setCcy_code(tableInfo.getCcy_code()); // 货币代码
		info.setGl_code(tableInfo.getGl_code()); // 科目号
		info.setReserve_type(tableInfo.getReserve_type()); // 准备金种类
		info.setCalc_factor(tableInfo.getCalc_factor()); // 计算因子
		info.setPaired_gl_code(tableInfo.getPaired_gl_code()); // 配对科目
		info.setReserve_percent(tableInfo.getReserve_percent()); // 准备金比率
		info.setCorpno(tableInfo.getCorpno()); // 法人代码
		info.setRecdver(tableInfo.getRecdver());

		return info;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-上午9:48:15</li>
	 *         <li>功能说明：通过指定条件查询缴存指标明细</li>
	 *         </p>
	 * @param indexInfo
	 *            准备金缴存指标相关复合类型
	 * @return
	 */
	public static Options<GlReserveIndexInfo> queryGlReserveIndexInfo(GlReserveIndexInfo indexInfo) {

		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		if(CommUtil.isNotNull(indexInfo.getReserve_percent()) &&
				(indexInfo.getReserve_percent().doubleValue() > 100 || indexInfo.getReserve_percent().doubleValue() <= 0)) {
			throw GlError.GL.E0214();
		}
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<GlReserveIndexInfo> page = GlParmDao.lstAllReserveIndex(orgId, indexInfo.getCcy_code(), indexInfo.getGl_code(), indexInfo.getReserve_type(), indexInfo.getCalc_factor(),
				indexInfo.getPaired_gl_code(), indexInfo.getReserve_percent(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);

		Options<GlReserveIndexInfo> list = new DefaultOptions<GlReserveIndexInfo>();
		list.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

		return list;

	}

}
