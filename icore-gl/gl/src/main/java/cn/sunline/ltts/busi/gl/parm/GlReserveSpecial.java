package cn.sunline.ltts.busi.gl.parm;

import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcy;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.namedsql.GlParmDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.Glp_reserve_specialDao;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_percent;
import cn.sunline.ltts.busi.gl.tables.TabGLParm.glp_reserve_special;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveSpecialInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;

public class GlReserveSpecial {


	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午4:33:32</li>
	 *         <li>功能说明：字段不为空检查</li>
	 *         </p>
	 * @param specialInfo
	 *            特殊对外缴存定义复合类型
	 */
	public static void checkNull(GlReserveSpecialInfo specialInfo) {
		
		CommTools.fieldNotNull(specialInfo.getBranch_id(), GlDict.A.branch_id.getId(), GlDict.A.branch_id.getLongName());
		CommTools.fieldNotNull(specialInfo.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		CommTools.fieldNotNull(specialInfo.getReserve_type(), GlDict.A.reserve_type.getId(), GlDict.A.reserve_type.getLongName());

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午4:38:52</li>
	 *         <li>功能说明：检查指定记录是否存在</li>
	 *         </p>
	 * @param branch_id
	 *            机构号
	 * @param ccy_code
	 *            货币代码
	 * @param reserve_type
	 *            准备金种类
	 * @return 没有记录返回false，存在返回true。
	 */
	public static boolean checkExsits(String branchId, String ccyCode, E_RESERVEAMTTYPE reserveType) {
		glp_reserve_special info = Glp_reserve_specialDao.selectOne_odb1(branchId, ccyCode, reserveType, false);

		// 没有记录返回false，存在返回true。
		return (info == null) ? false : true;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午4:42:56</li>
	 *         <li>功能说明：检查送上字段的合法性</li>
	 *         </p>
	 * @param specialInfo
	 *            特殊对外缴存定义复合类型
	 */
	public static void checkVadility(GlReserveSpecialInfo specialInfo) {
		// 货币代码必须在app_currency中存在
		if (!CommTools.existsApCurrency(specialInfo.getCcy_code())) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppCrcy.class).getLongname(), GlDict.A.ccy_code.getLongName(), specialInfo.getCcy_code());
		}
		// if( !ApBranch.exists(specialInfo.getBranch_id()) )
		// throw
		// ApPubErr.APPUB.E0005(OdbFactory.getTable(apb_branch.class).getLongname(),
		// GlDict.A.branch_id.getId(), specialInfo.getBranch_id());
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午5:11:03</li>
	 *         <li>功能说明：获取指定特殊对外缴存定义信息</li>
	 *         </p>
	 * @param branch_id
	 *            机构号
	 * @param ccy_code
	 *            货币代码
	 * @param reserve_type
	 *            准备金种类
	 * @return
	 */
	public static GlReserveSpecialInfo getGlReserveSpecialInfo(String branchId, String ccyCode, E_RESERVEAMTTYPE reserveType) {

		// 记录不存在报错
		if (!GlReserveSpecial.checkExsits(branchId, ccyCode, reserveType)) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_special.class).getLongname(), GlDict.A.branch_id.getId(), branchId, GlDict.A.ccy_code.getId(), ccyCode,
					GlDict.A.reserve_type.getId(), reserveType.getValue());
		}

		glp_reserve_special tableInfo = Glp_reserve_specialDao.selectOne_odb1(branchId, ccyCode, reserveType, false);
		GlReserveSpecialInfo info = SysUtil.getInstance(GlReserveSpecialInfo.class);

		info.setBranch_id(tableInfo.getBranch_id()); // 机构号
		info.setCcy_code(tableInfo.getCcy_code()); // 货币代码
		info.setReserve_type(tableInfo.getReserve_type()); // 准备金种类
		info.setCorpno(tableInfo.getCorpno()); // 法人代码
		info.setRecdver(tableInfo.getRecdver());

		return info;

	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月21日-下午5:11:36</li>
	 *         <li>功能说明：查询特殊对外缴存定义信息明细列表</li>
	 *         </p>
	 * @param specialInfo
	 *            特殊对外缴存定义复合类型
	 * @return 明细列表
	 */
	public static Options<GlReserveSpecialInfo> queryGlReserveSpecialInfo(GlReserveSpecialInfo specialInfo) {

		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<GlReserveSpecialInfo> page = GlParmDao.lstAllReserveSpecial(orgId, specialInfo.getBranch_id(), 
				specialInfo.getCcy_code(), specialInfo.getReserve_type(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);

		Options<GlReserveSpecialInfo> list = new DefaultOptions<GlReserveSpecialInfo>();
		list.setValues(page.getRecords());
		runEnvs.setCounts(page.getRecordCount());

		return list;

	}

	public static void addGlReserveSpecialInfo(GlReserveSpecialInfo addIn) {
		// 判断不能为空的字段
		checkNull(addIn);
		// 检查合法性
		checkVadility(addIn);
		// 检查记录存在报错
		if (checkExsits(addIn.getBranch_id(), addIn.getCcy_code(), addIn.getReserve_type())) {
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(glp_reserve_percent.class).getLongname(),
					addIn.getBranch_id() + " " + addIn.getCcy_code() + " " + addIn.getReserve_type());
		}
		glp_reserve_special info = SysUtil.getInstance(glp_reserve_special.class);
		info.setBranch_id(addIn.getBranch_id());
		info.setCcy_code(addIn.getCcy_code());
		info.setCorpno(addIn.getCorpno());
		info.setRecdver(addIn.getRecdver());
		info.setReserve_type(addIn.getReserve_type());
		info.setTmstmp(DateUtil.getNow(null));
		
		// 插入数据
		Glp_reserve_specialDao.insert(info);
		// 登记审计
		ApDataAudit.regLogOnInsertParameter(info);
	}

	public static void delGlReserveSpecialInfo(String branchId, String ccyCode, E_RESERVEAMTTYPE reserveType, Long recdver) {
		// 指定记录不存在报错
		if (!checkExsits(branchId, ccyCode, reserveType)) {
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(glp_reserve_special.class).getLongname(), GlDict.A.ccy_code.getId(), ccyCode, 
					GlDict.A.branch_id.getId(), branchId,GlDict.A.reserve_type.getId(), reserveType.getValue());
		}

		// 找出待删除的信息
		glp_reserve_special delInfo = Glp_reserve_specialDao.selectOne_odb1(branchId,ccyCode, reserveType, false);

		// 版本号非空校验
		CommTools.fieldNotNull(recdver, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比版本号
		if (CommUtil.compare(delInfo.getRecdver(), recdver) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(glp_reserve_special.class).getLongname());
		}

		// 删除信息
		Glp_reserve_specialDao.deleteOne_odb1(branchId, ccyCode, reserveType);
		// 登记审计
		ApDataAudit.regLogOnDeleteParameter(delInfo);
	}

}
