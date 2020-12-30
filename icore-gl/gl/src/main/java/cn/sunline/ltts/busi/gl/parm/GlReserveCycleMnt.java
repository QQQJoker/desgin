package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_cycleDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_cycle;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveCycleInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveCycleMnt {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlReserveCycleMnt.class);

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午2:07:15</li>
	 *         <li>功能说明：增加一条缴存周期的信息</li>
	 *         </p>
	 * @param reserveCycleInfo
	 *            缴存周期的复合类型
	 */
	public static void addReserveCycle(GlReserveCycleInfo reserveCycleInfo) {
		// 传入值不为空
		GlReserveCycle.checkNull(reserveCycleInfo);
		// 合法性检查
		GlReserveCycle.checkValidity(reserveCycleInfo);

		// 存在记录报错
		if (GlReserveCycle.checkExsit(reserveCycleInfo.getCcy_code())) {
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(glp_reserve_cycle.class).getLongname(), reserveCycleInfo.getCcy_code());
		}

		glp_reserve_cycle info = SysUtil.getInstance(glp_reserve_cycle.class);

		info.setCcy_code(reserveCycleInfo.getCcy_code());
		info.setDeposit_cycle(reserveCycleInfo.getDeposit_cycle());

		Glp_reserve_cycleDao.insert(info);
		// 登记审计
		ApDataAudit.regLogOnInsertParameter(info);
		BIZLOG.method(" addReserveCycle end >>>>>>>>>>>>>>>>");
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午2:28:28</li>
	 *         <li>功能说明：维护一条缴存周期信息</li>
	 *         </p>
	 * @param reserveCycleInfo
	 *            缴存周期的复合类型
	 */
	public static void mntReserveCycle(GlReserveCycleInfo reserveCycleInfo) {

		GlReserveCycle.checkNull(reserveCycleInfo);

		// 不存在记录报错
		if (!GlReserveCycle.checkExsit(reserveCycleInfo.getCcy_code())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(glp_reserve_cycle.class).getLongname(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		}

		// 找出待修改的记录
		glp_reserve_cycle oldInfo = Glp_reserve_cycleDao.selectOneWithLock_odb1(reserveCycleInfo.getCcy_code(), false);
		glp_reserve_cycle mntInfo = CommTools.clone(glp_reserve_cycle.class, oldInfo);

		// 版本号非空校验
		CommTools.fieldNotNull(reserveCycleInfo.getRecdver(), BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比数据版本
		if (CommUtil.compare(reserveCycleInfo.getRecdver(), mntInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_cycle.class).getName());
		}

		mntInfo.setDeposit_cycle(reserveCycleInfo.getDeposit_cycle());
		// 登记审计
		if (ApDataAudit.regLogOnUpdateParameter(oldInfo, mntInfo) == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(glp_reserve_cycle.class).getLongname());
		}

		Glp_reserve_cycleDao.updateOne_odb1(mntInfo);
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月20日-下午2:33:46</li>
	 *         <li>功能说明：删除一条缴存周期信息</li>
	 *         </p>
	 *         reserveCycleInfo
	 * @param reserveCycleInfo
	 *            缴存周期的复合类型
	 */
	public static void delReserveCycle(String ccyCode, Long recdver) {

		// 不存在记录报错
		if (!GlReserveCycle.checkExsit(ccyCode)) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(glp_reserve_cycle.class).getLongname(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		}

		// 找出待删除的记录
		glp_reserve_cycle delInfo = Glp_reserve_cycleDao.selectOne_odb1(ccyCode, false);

		// 版本号非空校验
		CommTools.fieldNotNull(recdver, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(recdver, delInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_cycle.class).getName());
		}

		Glp_reserve_cycleDao.deleteOne_odb1(ccyCode);

		ApDataAudit.regLogOnDeleteParameter(delInfo);

	}

}
