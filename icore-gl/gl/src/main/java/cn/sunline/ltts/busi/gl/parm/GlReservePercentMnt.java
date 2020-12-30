package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_percentDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_percent;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReservePercentInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReservePercentMnt {

	// private static final BizLog bizlog =
	// BizLogUtil.getBizLog(GlReservePercentMnt.class);

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午4:54:22</li>
	 *         <li>功能说明：增加一条准备金缴存比率记录</li>
	 *         </p>
	 * @param percentInfo
	 *            准备金缴存比率复合类型
	 */
	public static void addPercentInfo(GlReservePercentInfo percentInfo) {

		// 非空字段检查
		GlReservePercent.checkNull(percentInfo);

		// 检查合法性
		GlReservePercent.checkValidity(percentInfo);

		// 记录存在报错
		if (GlReservePercent.checkExsits(percentInfo.getBranch_id(), percentInfo.getCcy_code(), percentInfo.getReserve_type())) {
			// TODO
			// 错误码未定义
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(glp_reserve_percent.class).getLongname(), "");
		}

		glp_reserve_percent info = SysUtil.getInstance(glp_reserve_percent.class);

		info.setBranch_id(percentInfo.getBranch_id()); // 机构号
		info.setCcy_code(percentInfo.getCcy_code()); // 货币代码
		info.setReserve_type(percentInfo.getReserve_type()); // 准备金种类
		info.setReserve_percent(percentInfo.getReserve_percent()); // 准备金比率
		info.setBank_intn_dep_add_percent(percentInfo.getBank_intn_dep_add_percent()); // 行内缴存追加比率

		Glp_reserve_percentDao.insert(info);

		// 登记审计
		ApDataAudit.regLogOnInsertParameter(info);

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午6:27:06</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param percentInfo
	 *            准备金缴存比率复合类型
	 */
	public static void mntPercentInfo(GlReservePercentInfo percentInfo) {
		// 判断不能为空的字段
		GlReservePercent.checkNull(percentInfo);

		// 检查合法性
		GlReservePercent.checkValidity(percentInfo);

		// 找出待修改的信息,不存在报错
		if (!GlReservePercent.checkExsits(percentInfo.getBranch_id(), percentInfo.getCcy_code(), percentInfo.getReserve_type())) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_percent.class).getLongname(), GlDict.A.branch_id.getId(), percentInfo.getBranch_id(), GlDict.A.ccy_code.getId(),
					percentInfo.getCcy_code(), GlDict.A.reserve_type.getId(), percentInfo.getReserve_type().getValue());
		}
		glp_reserve_percent oldInfo = Glp_reserve_percentDao.selectOne_odb1(percentInfo.getBranch_id(), percentInfo.getCcy_code(), percentInfo.getReserve_type(), false);

		// 克隆
		glp_reserve_percent mntInfo = CommTools.clone(glp_reserve_percent.class, oldInfo);

		// 版本号非空校验
		CommTools.fieldNotNull(mntInfo.getRecdver(), BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比数据版本
		if (CommUtil.compare(oldInfo.getRecdver(), percentInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_percent.class).getLongname());
		}

		mntInfo.setBranch_id(percentInfo.getBranch_id()); // 机构号
		mntInfo.setCcy_code(percentInfo.getCcy_code()); // 货币代码
		mntInfo.setReserve_type(percentInfo.getReserve_type()); // 准备金种类
		mntInfo.setReserve_percent(percentInfo.getReserve_percent()); // 准备金比率
		mntInfo.setBank_intn_dep_add_percent(percentInfo.getBank_intn_dep_add_percent()); // 行内缴存追加比率

		if(oldInfo.getReserve_percent().doubleValue() == percentInfo.getReserve_percent().doubleValue()) { 
			//与数据库保持一致，避免未改任何参数情况下，因数值相同精度不同而登记修改
			oldInfo.setReserve_percent(mntInfo.getReserve_percent());
		}
		if(oldInfo.getBank_intn_dep_add_percent().doubleValue() == percentInfo.getBank_intn_dep_add_percent().doubleValue()) { 
			//与数据库保持一致，避免未改任何参数情况下，因数值相同精度不同而登记修改
			oldInfo.setBank_intn_dep_add_percent(mntInfo.getBank_intn_dep_add_percent());
		}
		if (ApDataAudit.regLogOnUpdateParameter(oldInfo, mntInfo) == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(glp_reserve_percent.class).getLongname());
		}

		Glp_reserve_percentDao.updateOne_odb1(mntInfo);
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午6:26:09</li>
	 *         <li>功能说明：删除一条准备金缴存比率信息</li>
	 *         </p>
	 * @param branch_id
	 *            机构号
	 * @param ccy_code
	 *            货币代码
	 * @param reserve_type
	 *            准备金类别
	 * @param recdver
	 *            版本号
	 */
	public static void delPercentInfo(String branchId, String ccyCode, E_RESERVEAMTTYPE reserveType, Long recdver) {

		// 记录不存在报错
		if (!GlReservePercent.checkExsits(branchId, ccyCode, reserveType)) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_percent.class).getLongname(), GlDict.A.branch_id.getId(), branchId, GlDict.A.ccy_code.getId(), ccyCode,
					GlDict.A.reserve_type.getId(), reserveType.getValue());
		}

		// 找出待删除的信息
		glp_reserve_percent delInfo = Glp_reserve_percentDao.selectOne_odb1(branchId, ccyCode, reserveType, false);

		// 版本号非空校验
		CommTools.fieldNotNull(recdver, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(delInfo.getRecdver(), recdver) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_percent.class).getLongname());
		}

		// 删除信息
		Glp_reserve_percentDao.deleteOne_odb1(branchId, ccyCode, reserveType);

		// 登记审计
		ApDataAudit.regLogOnDeleteParameter(delInfo);
	}

}
