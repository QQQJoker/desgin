package cn.sunline.ltts.busi.hc.serviceimpl;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.bsap.util.DaoSplitInvokeUtil;
import cn.sunline.ltts.busi.hc.tables.HcbLimitOccu.HcbLmoc;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefnDao;
import cn.sunline.ltts.busi.hc.util.GenUtil;
import cn.sunline.ltts.busi.hc.util.HotCtrlCacheUtil;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoChkHotCtrlIn;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoHotCtrlStrikeIn;
import cn.sunline.ltts.busi.sys.errors.HcError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DEALSS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_NESTYN;

/**
 * 热点控制冲账服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoHotCtrlStrikeSvcTypeImpl", longname = "热点控制冲账服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoHotCtrlStrikeSvcTypeImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlStrikeSvcType {

	private static final BizLog bizlog = LogManager.getBizLog(IoHotCtrlStrikeSvcTypeImpl.class);

	private static String trandt;

	private static String transq;

	private static String hash;

	//private static String pedlHash;

	/**
	 * 功能说明：热点控制冲账
	 * 
	 * @author Xiaoyu Luo
	 * @param hcstin
	 *            冲正输入要素
	 */
	@Override
	public void hotCtrlGeneralStrike(IoHotCtrlStrikeIn hcstin) {
		// 输入检查及处理
		trandt = CommToolsAplt.prcRunEnvs().getTrandt();
		transq = CommToolsAplt.prcRunEnvs().getTransq();
		//根据热点主体hash
		hash=GenUtil.getHashValue(hcstin.getHcacct(), "HcbLmoc");
		// 热点定义表查询
		HcpDefn hcpDefn = HcpDefnDao.selectOne_odb1(hcstin.getHcacct(), false);
		if (CommUtil.isNull(hcpDefn)) {
			HcError.HcGen.E0002("热点定义信息");
		}
		//Object[] objects = { hcstin.getTrandt(), hcstin.getTransq() };
		//HcbLmoc hcbLmoc = DaoSplitInvokeUtil.selectOne(HcbLmoc.class,
		//		"selectOne_odb2", hash, hcstin.getTrandt(), hcstin.getTransq(),true);
		IoChkHotCtrlIn ioChkHotCtrlIn = SysUtil.getInstance(IoChkHotCtrlIn.class);
		ioChkHotCtrlIn.setHcacct(hcstin.getHcacct());
		ioChkHotCtrlIn.setHctype(hcpDefn.getHctype());
		ioChkHotCtrlIn.setTranam(hcstin.getTranam().negate());
		//ioChkHotCtrlIn.setTranam(hcbLmoc.getTranam().negate());
		ioChkHotCtrlIn.setAmntcd(hcstin.getAmntcd());

		// 热点控制余额处理
		if (hcstin.getAmntcd() == E_AMNTCD.DR) {
			// 校验及处理
			chkStrike(hcstin);
			// 登记额度占用表
			regHcbLmoc(ioChkHotCtrlIn);
			// 登记额度待处理明细
			GenUtil.regHcbPedl(ioChkHotCtrlIn, hash);
			// redis额度占用
			GenUtil.redisOccupy(ioChkHotCtrlIn);
		} else if (hcstin.getAmntcd() == E_AMNTCD.CR) {
			try {
				// 更新额度占用冲正状态
				regCHcbLmoc(ioChkHotCtrlIn);
				// 贷方（余额增加）冲正额度检查
				BigDecimal avaiam = HotCtrlCacheUtil
						.getHotCtrlCurBalData(ioChkHotCtrlIn.getHcacct());
				GenUtil.chkAvailablebalance(avaiam,
						hcstin.getTranam());
				// redis额度占用
				GenUtil.redisOccupy(ioChkHotCtrlIn);

				// 校验及处理
				chkStrike(hcstin);
			} catch (Exception e) {
				// 失败处理方法
				hotCtrlGeneralStrikeFail(hcstin);
				throw HcError.HcGen.E0000(e.toString());
			}

			// 更新占用表状态为成功
//			HcbLmoc hcbLmocNew = DaoSplitInvokeUtil.selectOne(
//					HcbLmoc.class, "selectOne_odb2", hash, hcstin.getTrandt(), hcstin.getTransq(),true);
//			hcbLmocNew.setDealss(E_DEALSS.CG);
//			DaoSplitInvokeUtil.update(hcbLmocNew, "updateOne_odb2",
//					hash);

			// 登记额度待处理明细
			GenUtil.regHcbPedl(ioChkHotCtrlIn, hash);
		}
	}

	/**
	 * 功能说明：其他检查及处理
	 * 
	 * @author Xiaoyu Luo
	 * @param 冲正输入要素
	 */
	public void chkStrike(IoHotCtrlStrikeIn ioHotCtrlStrikeIn) {
		if (CommUtil.isNull(ioHotCtrlStrikeIn.getHcacct())) {
			HcError.HcGen.E0001("账号或产品号");
		}

		if (CommUtil.isNull(ioHotCtrlStrikeIn.getAmntcd())) {
			HcError.HcGen.E0001(ioHotCtrlStrikeIn.getAmntcd().getLongName());
		}

		if (CommUtil.isNull(ioHotCtrlStrikeIn.getTrandt())) {
			HcError.HcGen.E0001("交易日期");
		}

		if (CommUtil.isNull(ioHotCtrlStrikeIn.getTransq())) {
			HcError.HcGen.E0001("交易流水");
		}
	}

	/**
	 * 功能说明：热点额度占用表
	 * 
	 * @author Xiaoyu Luo
	 * @param hcstin
	 *            冲正输入要素
	 */
	public void regHcbLmoc(IoChkHotCtrlIn hotCtrlIn) {
		// 1、登记占用明细表
		bizlog.debug("==================登记占用明细表==================");
		GenUtil.addHcbLmoc(hotCtrlIn, E_DEALSS.CG, E_NESTYN.CZ, hash);
	}

	/**
	 * 功能说明：热点额度占用表
	 * 
	 * @author Xiaoyu Luo
	 * @param hcstin
	 *            冲正输入要素
	 */
	public void regCHcbLmoc(final IoChkHotCtrlIn hotCtrlIn) {
		// 1、登记占用明细表
		bizlog.debug("==================登记占用明细表==================");
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				GenUtil.addHcbLmoc(hotCtrlIn, E_DEALSS.TZ, E_NESTYN.CZ,
						hash);
				return null;
			}
		});
	}

	/**
	 * 功能说明：热点控制失败处理
	 * 
	 * @param hotinp
	 */
	public void hotCtrlGeneralStrikeFail(
			final cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoHotCtrlStrikeIn hotinp) {
		//Object[] objects = { trandt, transq };
		HcbLmoc hcbLmoc = DaoSplitInvokeUtil.selectOne(HcbLmoc.class,
				"selectOne_odb2", hash, trandt, transq,true);

		IoChkHotCtrlIn ioChkHotCtrlIn = SysUtil
				.getInstance(IoChkHotCtrlIn.class);
		ioChkHotCtrlIn.setAmntcd(hotinp.getAmntcd());
		ioChkHotCtrlIn.setHcacct(hotinp.getHcacct());
		ioChkHotCtrlIn.setHctype(hcbLmoc.getHctype());
		ioChkHotCtrlIn.setTranam(hcbLmoc.getTranam());

		// 异常：释放Redis占用额度
		GenUtil.redisRelease(ioChkHotCtrlIn);

		// 异常：更新额度占用表状态为“失败”
		hcbLmoc.setDealss(E_DEALSS.SB);
		DaoSplitInvokeUtil.update(hcbLmoc, "updateOne_odb2", hash);

	}
}
