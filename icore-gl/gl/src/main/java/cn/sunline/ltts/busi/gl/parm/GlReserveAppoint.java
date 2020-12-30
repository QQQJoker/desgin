package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcy;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcyDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.gl.namedsql.GlParmDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_appointDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_appoint;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveAppointInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveAppoint {

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午2:27:11</li>
	 *         <li>功能说明：检查字段不为空</li>
	 *         </p>
	 * @param appointInfo
	 *            指定日期缴存定义复合类型
	 */
	public static void checkNull(GlReserveAppointInfo appointInfo) {
		CommTools.fieldNotNull(appointInfo.getAppoint_date(), GlDict.A.appoint_date.getId(), GlDict.A.appoint_date.getLongName());
		CommTools.fieldNotNull(appointInfo.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午2:36:58</li>
	 *         <li>功能说明：查询指定记录</li>
	 *         </p>
	 * @param appoint_date
	 *            指定日期
	 * @param ccy_code
	 *            货币代码
	 * @return 没有记录返回false，存在返回true。
	 */
	public static boolean checkExists(String appointDate, String ccyCode) {
		glp_reserve_appoint info = Glp_reserve_appointDao.selectOne_odb1(appointDate, ccyCode, false);

		// 没有记录返回false，存在返回true。
		return (info == null) ? false : true;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午3:19:12</li>
	 *         <li>功能说明：检查字段合法性</li>
	 *         </p>
	 * @param appointInfo
	 *            指定日期缴存定义复合类型
	 */
	public static void checkVadility(GlReserveAppointInfo appointInfo) {
		// 指定日期：大于或等于当前系统日期
		if (CommUtil.compare(appointInfo.getAppoint_date(), CommToolsAplt.prcRunEnvs().getTrandt()) < 0) {
			throw GlError.GL.E0020(appointInfo.getAppoint_date(), CommToolsAplt.prcRunEnvs().getTrandt());
		}

		// 指定日期与固定周期重叠
		if (DateTools2.isLastDay("T", appointInfo.getAppoint_date()) || DateTools2.isLastDay("M", appointInfo.getAppoint_date()) || DateTools2.isLastDay("Q", appointInfo.getAppoint_date())) {
			throw GlError.GL.E0062();
		}

		// 货币代码必须在app_crcy中存在
		AppCrcy appCrcy = AppCrcyDao.selectOne_odb1(appointInfo.getCcy_code(), false);
		 if(CommUtil.isNull(appCrcy)) {
			 throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppCrcy.class).getLongname(),GlDict.A.ccy_code.getLongName(),appointInfo.getCcy_code() );
		 }
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午3:31:30</li>
	 *         <li>功能说明：查询指定记录</li>
	 *         </p>
	 * @param appoint_date
	 *            指定缴存日期
	 * @param ccy_code
	 *            货币代码
	 * @return 指定日期缴存定义复合类型
	 */
	public static GlReserveAppointInfo getGlReserveAppointInfo(String appointDate, String ccyCode) {

		// 记录不存在报错
		if (!checkExists(appointDate, ccyCode)) {
			throw ApPubErr.APPUB.E0024(OdbFactory.getTable(glp_reserve_appoint.class).getLongname(), GlDict.A.appoint_date.getId(), appointDate, GlDict.A.ccy_code.getId(), ccyCode);
		}

		glp_reserve_appoint tableInfo = Glp_reserve_appointDao.selectOne_odb1(appointDate, ccyCode, false);
		GlReserveAppointInfo info = SysUtil.getInstance(GlReserveAppointInfo.class);

		info.setAppoint_date(tableInfo.getAppoint_date());
		info.setCcy_code(tableInfo.getCcy_code());
		info.setCorpno(tableInfo.getCorpno());
		info.setRecdver(tableInfo.getRecdver());

		return info;

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月24日-上午9:52:39</li>
	 *         <li>功能说明：查询指定日期缴存明细列表</li>
	 *         </p>
	 * @param appointInfo
	 *            指定日期缴存复合类型
	 * @return
	 */
	public static Options<GlReserveAppointInfo> queryGlReserveAppointInfo(GlReserveAppointInfo appointInfo) {

		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<GlReserveAppointInfo> page = GlParmDao.lstAllReserveAppoint(orgId, appointInfo.getAppoint_date(), 
				appointInfo.getCcy_code(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);

		Options<GlReserveAppointInfo> list = new DefaultOptions<GlReserveAppointInfo>();
		list.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

		return list;

	}
}
