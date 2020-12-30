package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcy;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.namedsql.GlParmDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_cycleDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_cycle;
import cn.sunline.ltts.busi.gl.type.GlParm;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveCycleInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveCycle {

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-上午10:48:19</li>
	 *         <li>功能说明：检查不能为空字段</li>
	 *         </p>
	 * @param reserveCycleInfo
	 *            缴存金周期符合类型
	 */
	public static void checkNull(GlReserveCycleInfo reserveCycleInfo) {
		CommTools.fieldNotNull(reserveCycleInfo.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-上午11:00:00</li>
	 *         <li>功能说明：检查制定数据是否存在</li>
	 *         </p>
	 * @param ccy_code
	 *            没有记录返回false，存在返回true。
	 */
	public static boolean checkExsit(String ccy_code) {
		glp_reserve_cycle info = Glp_reserve_cycleDao.selectOne_odb1(ccy_code, false);

		// 没有记录返回false，存在返回true。
		return (info == null) ? false : true;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-上午10:58:15</li>
	 *         <li>功能说明：检查传入值的合法性</li>
	 *         </p>
	 * @param reserveCycleInfo
	 *            缴存金周期符合类型
	 */
	public static void checkValidity(GlReserveCycleInfo reserveCycleInfo) {
		// 货币代码必须在app_currency中存在
		if (!CommTools.existsApCurrency(reserveCycleInfo.getCcy_code())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppCrcy.class).getLongname(), GlDict.A.ccy_code.getLongName(), reserveCycleInfo.getCcy_code());
		}
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午1:31:25</li>
	 *         <li>功能说明：根据主键查询指定缴存金周期</li>
	 *         </p>
	 * @param ccy_code
	 *            货币代码
	 * @return 缴存金周期符合类型
	 */
	public static GlReserveCycleInfo getReserveCycleInfo(String ccyCode) {
		// 无记录报错
		if (!checkExsit(ccyCode)) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(glp_reserve_cycle.class).getLongname(), GlDict.A.ccy_code.getId(), ccyCode);
		}
		glp_reserve_cycle tableInfo = Glp_reserve_cycleDao.selectOne_odb1(ccyCode, false);

		GlReserveCycleInfo info = SysUtil.getInstance(GlReserveCycleInfo.class);

		info.setCcy_code(tableInfo.getCcy_code());
		info.setDeposit_cycle(tableInfo.getDeposit_cycle());
		info.setCorpno(tableInfo.getCorpno());
		info.setRecdver(tableInfo.getRecdver());

		return info;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-上午11:32:09</li>
	 *         <li>功能说明：查询缴存周期明细列表</li>
	 *         </p>
	 * @param reserveCycleInfo
	 *            缴存周期复合类型
	 * @return 列表明细
	 */
	public static Options<GlReserveCycleInfo> queryReserveCycleInfo(GlReserveCycleInfo reserveCycleInfo) {

		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<GlReserveCycleInfo> page = GlParmDao.lstAllReserveCycle(orgId, reserveCycleInfo.getCcy_code(), 
				reserveCycleInfo.getDeposit_cycle(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);

		Options<GlReserveCycleInfo> info = new DefaultOptions<GlParm.GlReserveCycleInfo>();
		info.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

		return info;
	}

}
