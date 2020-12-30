package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_specialDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_special;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveSpecialInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveSpecialMnt {

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午5:12:14</li>
	 *         <li>功能说明：增加一条特殊对外缴存定义</li>
	 *         </p>
	 * @param specialInfo
	 *            特殊对外缴存定义复合类型
	 */
	public static void addGlReserveSpecialInfo(GlReserveSpecialInfo specialInfo) {
		// 检查不能为空的字段
		GlReserveSpecial.checkNull(specialInfo);
		// 检查合法性
		GlReserveSpecial.checkVadility(specialInfo);
		// 记录存在则报错
		if (GlReserveSpecial.checkExsits(specialInfo.getBranch_id(), specialInfo.getCcy_code(), specialInfo.getReserve_type())) {
			throw ApPubErr.APPUB
					.E0019(OdbFactory.getTable(glp_reserve_special.class).getLongname(), specialInfo.getBranch_id() + " " + specialInfo.getCcy_code() + " " + specialInfo.getReserve_type());
		}

		glp_reserve_special info = SysUtil.getInstance(glp_reserve_special.class);

		info.setBranch_id(specialInfo.getBranch_id()); // 机构号
		info.setCcy_code(specialInfo.getCcy_code()); // 货币代码
		info.setReserve_type(specialInfo.getReserve_type()); // 准备金种类

		Glp_reserve_specialDao.insert(info);
		ApDataAudit.regLogOnInsertParameter(info);
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午6:44:58</li>
	 *         <li>功能说明：删除一条指定记录</li>
	 *         </p>
	 * @param branch_id
	 *            机构号
	 * @param ccy_code
	 *            货币代码
	 * @param reserve_type
	 *            准备金种类
	 * @param recdver
	 *            数据版本
	 */
	public static void delGlReserveSpecialInfo(String branchId, String ccyCode, E_RESERVEAMTTYPE reserveType, Long recdver) {

		// 找出待删除的记录
		if (!GlReserveSpecial.checkExsits(branchId, ccyCode, reserveType)) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_special.class).getLongname(), GlDict.A.branch_id.getId(), branchId, GlDict.A.ccy_code.getId(), ccyCode,
					GlDict.A.reserve_type.getId(), reserveType.getValue());
		}

		glp_reserve_special delInfo = Glp_reserve_specialDao.selectOne_odb1(branchId, ccyCode, reserveType, false);

		// 版本号非空校验
		CommTools.fieldNotNull(recdver, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(delInfo.getRecdver(), recdver) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_special.class).getName());
		}

		Glp_reserve_specialDao.deleteOne_odb1(branchId, ccyCode, reserveType);
		// 登记审计
		ApDataAudit.regLogOnDeleteParameter(delInfo);

	}

}
