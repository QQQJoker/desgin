package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_indexDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_index;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_percent;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveIndexInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveIndexMnt {

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-上午10:04:16</li>
	 *         <li>功能说明：增加一条缴存指标信息</li>
	 *         </p>
	 * @param indexInfo
	 *            准备金缴存指标相关复合类型
	 */
	public static void addReserveIndex(GlReserveIndexInfo indexInfo) {

		// 检查非空字段
		GlReserveIndex.checkNull(indexInfo);
		// 检查合法性
		GlReserveIndex.checkValidity(indexInfo);
		// 检查对应主键是否存在
		if (GlReserveIndex.checkExsits(indexInfo.getCcy_code(), indexInfo.getGl_code(), indexInfo.getReserve_type())) {
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(glp_reserve_percent.class).getLongname(),
					indexInfo.getCcy_code() + " " + indexInfo.getGl_code() + " " + indexInfo.getReserve_type());
		}

		glp_reserve_index info = SysUtil.getInstance(glp_reserve_index.class);

		info.setCcy_code(indexInfo.getCcy_code()); // 货币代码
		info.setGl_code(indexInfo.getGl_code()); // 科目号
		info.setReserve_type(indexInfo.getReserve_type()); // 准备金种类
		info.setCalc_factor(indexInfo.getCalc_factor()); // 计算因子
		info.setPaired_gl_code(indexInfo.getPaired_gl_code()); // 配对科目
		info.setReserve_percent(indexInfo.getReserve_percent()); // 准备金比率
		info.setCorpno(indexInfo.getCorpno()); // 法人代码

		// 插入数据
		Glp_reserve_indexDao.insert(info);
		// 登记审计
		ApDataAudit.regLogOnInsertParameter(info);

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-上午10:29:51</li>
	 *         <li>功能说明：维护一条缴存指标信息</li>
	 *         </p>
	 * @param indexInfo
	 */
	public static void mntReserveIndex(GlReserveIndexInfo indexInfo) {

		// 字段不为空
		GlReserveIndex.checkNull(indexInfo);
		// 检查合法性
		GlReserveIndex.checkValidity(indexInfo);
		// 找出待修改的记录.不存在报错
		if (!GlReserveIndex.checkExsits(indexInfo.getCcy_code(), indexInfo.getGl_code(), indexInfo.getReserve_type())) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_percent.class).getLongname(), GlDict.A.ccy_code.getId(), indexInfo.getCcy_code(), GlDict.A.gl_code.getId(),
					indexInfo.getGl_code(), GlDict.A.reserve_type.getId(), indexInfo.getReserve_type().getValue());
		}

		glp_reserve_index oldInfo = Glp_reserve_indexDao.selectOne_odb1(indexInfo.getCcy_code(), indexInfo.getGl_code(), indexInfo.getReserve_type(), false);

		glp_reserve_index mntInfo = CommTools.clone(glp_reserve_index.class, oldInfo);

		// 版本号非空校验
		CommTools.fieldNotNull(oldInfo.getRecdver(), BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比数据版本
		if (CommUtil.compare(oldInfo.getRecdver(), indexInfo.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_index.class).getLongname());
		}
		
		mntInfo.setCcy_code(indexInfo.getCcy_code()); // 货币代码
		mntInfo.setGl_code(indexInfo.getGl_code()); // 科目号
		mntInfo.setReserve_type(indexInfo.getReserve_type()); // 准备金种类
		mntInfo.setCalc_factor(indexInfo.getCalc_factor()); // 计算因子
		mntInfo.setPaired_gl_code(indexInfo.getPaired_gl_code()); // 配对科目
		mntInfo.setReserve_percent(indexInfo.getReserve_percent()); // 准备金比率

		if(oldInfo.getReserve_percent().doubleValue() == indexInfo.getReserve_percent().doubleValue()) { 
			//与数据库保持一致，避免未改任何参数情况下，因数值相同精度不同而登记修改
			oldInfo.setReserve_percent(mntInfo.getReserve_percent());
		}
		// 登记审计
		if (ApDataAudit.regLogOnUpdateParameter(oldInfo, mntInfo) == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(glp_reserve_index.class).getLongname());
		}
		// 更新
		Glp_reserve_indexDao.updateOne_odb1(mntInfo);
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-上午10:12:26</li>
	 *         <li>功能说明：删除指定记录</li>
	 *         </p>
	 * @param ccy_code
	 *            货币代码
	 * @param subject_no
	 *            科目号
	 * @param reserve_type
	 *            缴存种类
	 */
	public static void delReserveIndex(String ccyCode, String subjectNo, E_RESERVEAMTTYPE reserveType, Long recdver) {
		// 指定记录不存在报错
		if (!GlReserveIndex.checkExsits(ccyCode, subjectNo, reserveType)) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_percent.class).getLongname(), GlDict.A.ccy_code.getId(), ccyCode, GlDict.A.gl_code.getId(), subjectNo,
					GlDict.A.reserve_type.getId(), reserveType.getValue());
		}

		// 找出待删除的信息
		glp_reserve_index delInfo = Glp_reserve_indexDao.selectOne_odb1(ccyCode, subjectNo, reserveType, false);

		// 版本号非空校验
		CommTools.fieldNotNull(recdver, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(delInfo.getRecdver(), recdver) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_index.class).getLongname());
		}

		// 删除信息
		Glp_reserve_indexDao.deleteOne_odb1(ccyCode, subjectNo, reserveType);
		// 登记审计
		ApDataAudit.regLogOnDeleteParameter(delInfo);
	}

}
