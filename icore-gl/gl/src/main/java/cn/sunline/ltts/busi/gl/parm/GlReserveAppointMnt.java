package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_appointDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_appoint;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveAppointInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveAppointMnt {

	// private static final BizLog bizlog =
	// BizLogUtil.getBizLog(GlReserveAppointMnt.class);

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午3:56:36</li>
	 *         <li>功能说明：增加一条数据</li>
	 *         </p>
	 * @param appointInfo
	 */
	public static void addlGlReserveAppointInfo(GlReserveAppointInfo appointInfo) {

		// 空值检查
		GlReserveAppoint.checkNull(appointInfo);

		// 合法性检查
		GlReserveAppoint.checkVadility(appointInfo);

		// 指定记录存在报错
		if (GlReserveAppoint.checkExists(appointInfo.getAppoint_date(), appointInfo.getCcy_code())) {
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(glp_reserve_appoint.class).getLongname(), appointInfo.getAppoint_date() + " " + appointInfo.getCcy_code());
		}

		glp_reserve_appoint info = SysUtil.getInstance(glp_reserve_appoint.class);

		info.setAppoint_date(appointInfo.getAppoint_date());
		info.setCcy_code(appointInfo.getCcy_code());

		Glp_reserve_appointDao.insert(info);
		ApDataAudit.regLogOnInsertParameter(info);

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月22日-下午3:06:57</li>
	 *         <li>功能说明：删除指定记录</li>
	 *         </p>
	 * @param appoint_date
	 *            指定日期
	 * @param ccy_code
	 *            货币代码
	 * @param recdver
	 *            数据版本
	 */
	public static void delGlReserveAppointInfo(String appointDate, String ccyCode, Long recdver) {

		// 记录不存在报错
		if (!GlReserveAppoint.checkExists(appointDate, ccyCode)) {
			throw ApPubErr.APPUB.E0024(OdbFactory.getTable(glp_reserve_appoint.class).getLongname(), GlDict.A.appoint_date.getId(), appointDate, GlDict.A.ccy_code.getId(), ccyCode);
		}

		// 找出删除记录
		glp_reserve_appoint delInfo = Glp_reserve_appointDao.selectOne_odb1(appointDate, ccyCode, false);

		// 版本号非空校验
		CommTools.fieldNotNull(recdver, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(delInfo.getRecdver(), recdver) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_appoint.class).getName());
		}

		Glp_reserve_appointDao.deleteOne_odb1(appointDate, ccyCode);

		ApDataAudit.regLogOnDeleteParameter(delInfo);
	}

}
