package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_level_defineDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_level_define;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_percent;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveLevelInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_DEPOSITPAIDBUSIPROP;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveLevelDefineMnt {

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月22日-下午2:01:09</li>
	 *         <li>功能说明：增加一条指定信息</li>
	 *         </p>
	 * @param levelInfo
	 *            缴存等级复合类型
	 */
	public static void addReserveLevelDefine(GlReserveLevelInfo levelInfo) {
		// 检查不不能为空的字段
		GlReserveLevelDefine.checkNull(levelInfo);
		// 检查记录存在报错
		if (GlReserveLevelDefine.checkExists(levelInfo.getDeposit_level(), levelInfo.getReserve_type(), levelInfo.getDeposit_busi_type())) {
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(glp_reserve_percent.class).getLongname(),
					levelInfo.getDeposit_level() + " " + levelInfo.getReserve_type() + " " + levelInfo.getDeposit_busi_type());
		}

		// 检查字段合法性
		GlReserveLevelDefine.checkVadility(levelInfo);

		glp_reserve_level_define info = SysUtil.getInstance(glp_reserve_level_define.class);

		info.setDeposit_level(levelInfo.getDeposit_level()); // 缴存层级
		info.setReserve_type(levelInfo.getReserve_type()); // 准备金种类
		info.setDeposit_busi_type(levelInfo.getDeposit_busi_type()); // 缴存款业务性质
		info.setDeposit_processing_category(levelInfo.getDeposit_processing_category()); // 缴存款处理类别
		info.setAccounting_gl_code(levelInfo.getAccounting_gl_code()); // 记账科目号
		info.setParent_accounting_gl_code(levelInfo.getParent_accounting_gl_code()); // 上级记账科目号

		Glp_reserve_level_defineDao.insert(info);

		ApDataAudit.regLogOnInsertParameter(info);
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月22日-下午2:00:55</li>
	 *         <li>功能说明：维护指定信息</li>
	 *         </p>
	 * @param levelInfo
	 *            缴存等级复合类型
	 */
	public static void mntReserveLevelDefine(GlReserveLevelInfo levelInfo) {
		// 检查不为空的字段
		GlReserveLevelDefine.checkNull(levelInfo);
		// 记录不存在报错
		if (!GlReserveLevelDefine.checkExists(levelInfo.getDeposit_level(), levelInfo.getReserve_type(), levelInfo.getDeposit_busi_type())) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_level_define.class).getLongname(), GlDict.A.deposit_level.getId(), levelInfo.getDeposit_level().toString(),
					GlDict.A.reserve_type.getId(), levelInfo.getReserve_type().getValue(), GlDict.A.deposit_busi_type.getId(), levelInfo.getDeposit_busi_type().getValue());
		}

		// 检查合法性
		GlReserveLevelDefine.checkVadility(levelInfo);

		// 找出待维护的记录
		glp_reserve_level_define oldInfo = Glp_reserve_level_defineDao.selectOne_odb1(levelInfo.getDeposit_level(), levelInfo.getReserve_type(), levelInfo.getDeposit_busi_type(), false);

		glp_reserve_level_define mntInfo = CommTools.clone(glp_reserve_level_define.class, oldInfo);

		// 版本号非空校验
		CommTools.fieldNotNull(mntInfo.getRecdver(), BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(oldInfo.getRecdver(), levelInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_level_define.class).getLongname());
		}
		// 修改记录
		mntInfo.setDeposit_processing_category(levelInfo.getDeposit_processing_category()); // 缴存款处理类别
		mntInfo.setAccounting_gl_code(levelInfo.getAccounting_gl_code()); // 记账科目号
		mntInfo.setParent_accounting_gl_code(levelInfo.getParent_accounting_gl_code()); // 上级记账科目号

		// 登记审计
		if (ApDataAudit.regLogOnUpdateParameter(oldInfo, mntInfo) == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(glp_reserve_level_define.class).getLongname());
		}

		Glp_reserve_level_defineDao.updateOne_odb1(mntInfo);
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月22日-下午1:24:38</li>
	 *         <li>功能说明：删除指定记录</li>
	 *         </p>
	 * @param deposit_paid_level
	 *            缴存等级
	 * @param reserve_type
	 *            准备金种类
	 * @param deposit_busi_type
	 * @param recdver
	 *            数据版本
	 */
	public static void delReserveLevelDefine(Long depositPaidLevel, E_RESERVEAMTTYPE reserveType, E_DEPOSITPAIDBUSIPROP depositBusiType, Long recdver) {

		// 指定记录不存在报错
		if (!GlReserveLevelDefine.checkExists(depositPaidLevel, reserveType, depositBusiType)) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_level_define.class).getLongname(), GlDict.A.deposit_level.getId(), depositPaidLevel.toString(), GlDict.A.reserve_type.getId(),
					reserveType.getValue(), GlDict.A.deposit_busi_type.getId(), depositBusiType.getValue());
		}

		// 找出待删除记录
		glp_reserve_level_define delInfo = Glp_reserve_level_defineDao.selectOne_odb1(depositPaidLevel, reserveType, depositBusiType, false);

		// 版本号非空校验
		CommTools.fieldNotNull(recdver, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(delInfo.getRecdver(), recdver) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_level_define.class).getLongname());
		}

		// 删除信息
		Glp_reserve_level_defineDao.deleteOne_odb1(depositPaidLevel, reserveType, depositBusiType);

		ApDataAudit.regLogOnDeleteParameter(delInfo);
	}

}
