package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcy;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.namedsql.GlParmDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_percentDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_percent;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReservePercentInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReservePercent {

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午3:08:53</li>
	 *         <li>功能说明：检查不能为空的字段</li>
	 *         </p>
	 * @param percentInfo
	 *            缴存比率复合类型
	 */
	public static void checkNull(GlReservePercentInfo percentInfo) {

		CommTools.fieldNotNull(percentInfo.getBranch_id(), GlDict.A.branch_id.getId(), GlDict.A.branch_id.getLongName());
		CommTools.fieldNotNull(percentInfo.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		CommTools.fieldNotNull(percentInfo.getBank_intn_dep_add_percent(), GlDict.A.bank_intn_dep_add_percent.getId(), GlDict.A.bank_intn_dep_add_percent.getLongName());
		CommTools.fieldNotNull(percentInfo.getReserve_percent(), GlDict.A.reserve_percent.getId(), GlDict.A.reserve_percent.getLongName());
		CommTools.fieldNotNull(percentInfo.getReserve_type(), GlDict.A.reserve_type.getId(), GlDict.A.reserve_type.getLongName());

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午3:16:11</li>
	 *         <li>功能说明：判断指定记录是否存在</li>
	 *         </p>
	 * @param percentInfo
	 *            缴存比率复合类型
	 * @return 没有记录返回false，存在返回true。
	 */
	public static boolean checkExsits(String branchId, String ccy_code, E_RESERVEAMTTYPE reserveType) {
		glp_reserve_percent info = Glp_reserve_percentDao.selectOne_odb1(branchId, ccy_code, reserveType, false);

		// 没有记录返回false，存在返回true。
		return (info == null) ? false : true;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午3:47:23</li>
	 *         <li>功能说明：检查送上字段的合法性</li>
	 *         </p>
	 * @param percentInfo
	 *            缴存比率复合类型
	 */
	public static void checkValidity(GlReservePercentInfo percentInfo) {
		// 货币代码必须在app_currency中存在
		if (!CommTools.existsApCurrency(percentInfo.getCcy_code())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppCrcy.class).getLongname(), GlDict.A.ccy_code.getLongName(), percentInfo.getCcy_code());
		}
		if(CommUtil.isNotNull(percentInfo.getReserve_percent())&&
				(percentInfo.getReserve_percent().doubleValue() > 100 || percentInfo.getReserve_percent().doubleValue() <= 0)) {
			throw GlError.GL.E0214();
		}
		if(CommUtil.isNotNull(percentInfo.getBank_intn_dep_add_percent()) && 
				(percentInfo.getBank_intn_dep_add_percent().doubleValue() > 100 || percentInfo.getBank_intn_dep_add_percent().doubleValue() <= 0)) {
			throw GlError.GL.E0214();
		}
		// 机构号必须在表中存在
		// if( !ApBranch.exists(percentInfo.getBranch_id()) )
		// throw
		// ApPubErr.APPUB.E0005(OdbFactory.getTable(apb_branch.class).getLongname(),
		// GlDict.A.branchId.getId(), GlDict.A.branchId.getLongName());

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午3:54:42</li>
	 *         <li>功能说明：查询指定缴存比率记录</li>
	 *         </p>
	 * @param branchId
	 *            账务机构
	 * @param ccy_code
	 *            货币代码
	 * @param reserve_type
	 *            准备金种类
	 * @return 一条记录
	 */
	public static GlReservePercentInfo getReservePercentInfo(String branchId, String ccyCode, E_RESERVEAMTTYPE reserveType) {

		// 判断指定记录仪是否存在
		if (!checkExsits(branchId, ccyCode, reserveType)) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_percent.class).getLongname(), GlDict.A.branch_id.getId(), branchId, GlDict.A.ccy_code.getId(), ccyCode,
					GlDict.A.reserve_type.getId(), reserveType.getValue());
		}

		glp_reserve_percent tableInfo = Glp_reserve_percentDao.selectOne_odb1(branchId, ccyCode, reserveType, false);

		GlReservePercentInfo info = SysUtil.getInstance(GlReservePercentInfo.class);

		info.setBranch_id(tableInfo.getBranch_id()); // 机构号
		info.setCcy_code(tableInfo.getCcy_code()); // 货币代码
		info.setReserve_type(tableInfo.getReserve_type()); // 准备金种类
		info.setReserve_percent(tableInfo.getReserve_percent()); // 准备金比率
		info.setBank_intn_dep_add_percent(tableInfo.getBank_intn_dep_add_percent()); // 行内缴存追加比率
		info.setRecdver(tableInfo.getRecdver());

		return info;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午4:35:13</li>
	 *         <li>功能说明：查询缴存比率明细列表</li>
	 *         </p>
	 * @param percentInfo
	 *            缴存比率复合类型
	 * @return 明细列表
	 */
	public static Options<GlReservePercentInfo> queryReservePercentInfo(GlReservePercentInfo percentInfo) {

		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		if(CommUtil.isNotNull(percentInfo.getReserve_percent())&&
				(percentInfo.getReserve_percent().doubleValue() > 100 || percentInfo.getReserve_percent().doubleValue() <= 0)) {
			throw GlError.GL.E0214();
		}
		if(CommUtil.isNotNull(percentInfo.getBank_intn_dep_add_percent()) && 
				(percentInfo.getBank_intn_dep_add_percent().doubleValue() > 100 || percentInfo.getBank_intn_dep_add_percent().doubleValue() <= 0)) {
			throw GlError.GL.E0214();
		}
		
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		Page<GlReservePercentInfo> page = GlParmDao.lstAllReservePercent(orgId, percentInfo.getBranch_id(), 
				percentInfo.getCcy_code(), percentInfo.getReserve_type(), percentInfo.getReserve_percent(),
				percentInfo.getBank_intn_dep_add_percent(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);

		Options<GlReservePercentInfo> info = new DefaultOptions<GlReservePercentInfo>();
		info.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

		return info;

	}

}
