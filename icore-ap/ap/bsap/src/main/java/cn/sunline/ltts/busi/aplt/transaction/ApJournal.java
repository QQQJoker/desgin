package cn.sunline.ltts.busi.aplt.transaction;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.namedsql.BaseApltDao;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvntDao;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTran;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTranDao;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpTxns;
import cn.sunline.ltts.busi.aplt.tools.BaseEnvUtil;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RverInfo;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SQHOTG;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DTXNST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_RVFXST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 交易流水及交易事件登记处理
 */
public class ApJournal {

	private static final BizLog bizlog = BizLogUtil.getBizLog(ApJournal.class);
//	private static ThreadLocal<List<KnbEvnt>> listKnbEvnt = new ThreadLocal<List<KnbEvnt>>() {
//		@Override
//		protected List<KnbEvnt> initialValue() {
//			return new ArrayList<KnbEvnt>();
//		}
//	};
//	private static ThreadLocal<List<KnbEvnt>> updateKnbEvnt = new ThreadLocal<List<KnbEvnt>>() {
//		@Override
//		protected List<KnbEvnt> initialValue() {
//			return new ArrayList<KnbEvnt>();
//		}
//	};

	public static void addKnbEvnt(KnbEvnt evnt) {
		//listKnbEvnt.get().add(evnt);
	    KnbEvntDao.insert(evnt);
	}
	public static void updateKnbEvnt(KnbEvnt evnt) {
//		updateKnbEvnt.get().add(evnt);
		KnbEvntDao.updateOne_odb3(evnt);
	}	

	/**
	 * 调用批量插入功能，保存交易事件
	 * 
	 * @return
	 */
//	public static void saveKnbEvnts() {
//		_saveKnbEvntsAdd();
//		_saveKnbEvntsUpdate();
//	}
	
//	private static int _saveKnbEvntsAdd() {
//		if (listKnbEvnt.get() == null || listKnbEvnt.get().size() <= 0)
//			return 0;
//
//		try {
//			if (bizlog.isDebugEnabled())
//				bizlog.debug("准备批量保存交易事件(KnbEvnt),当前事件记录有：" + listKnbEvnt.get());
//			// 批量插入
//			DaoUtil.insertBatch(KnbEvnt.class, listKnbEvnt.get());
//			// 返回批量插入的总记录数
//			if (bizlog.isDebugEnabled())
//				bizlog.debug("批量保存交易事件(KnbEvnt)成功，总记录为:" + DaoContext.get().getRowcount());
//			return DaoContext.get().getRowcount();
//
//		} catch (Exception e) {
//			bizlog.error("批量保存交易事件(KnbEvnt)失败！", e);
//			throw e;
//		} finally {
//			listKnbEvnt.get().clear();
//		}
//	}

//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	private static int _saveKnbEvntsUpdate() {
//		if (updateKnbEvnt.get() == null || updateKnbEvnt.get().size() <= 0)
//			return 0;
//
//		try {
//			if (bizlog.isDebugEnabled())
//				bizlog.debug("准备批量保存修改的交易事件(KnbEvnt),当前事件记录有：" + updateKnbEvnt.get());
//			// 批量修改
//			DaoUtil.updateBatch("BaseAplt.updateKnbEvntStatus", (List)updateKnbEvnt.get());
//			// 返回批量修改的总记录数
//			if (bizlog.isDebugEnabled())
//				bizlog.debug("批量保存修改的交易事件(KnbEvnt)成功，总记录为:" + DaoContext.get().getRowcount());
//			
//			return DaoContext.get().getRowcount();
//		} catch (Exception e) {
//			bizlog.error("批量保存修改的交易事件(KnbEvnt)失败！", e);
//			throw e;
//		} finally {
//			updateKnbEvnt.get().clear();
//		}
//	}	
	
	public static int updateKnsTran(KnsTran tran) {
		return KnsTranDao.updateOne_odb1(tran);
	}

	/**
	 * 保存交易流水
	 * 
	 */
	public static void saveKnsTran(KnsTran tran) {
		if (tran == null)
			tran = SysUtil.getInstance(KnsTran.class);
		RunEnvsComm trxRun = SysUtil.getTrxRunEnvs();
		tran.setCorpno(trxRun.getCorpno()); // 法人代码
		tran.setTrandt(trxRun.getTrandt()); // 交易日期
		tran.setTransq(trxRun.getTransq()); // 交易流水
		tran.setMntrsq(trxRun.getMntrsq()); // 主交易流水
		tran.setMntrfg(trxRun.getMntrfg()); // 是否主交易
		tran.setSystcd(SysUtil.getSubSystemId());// 子系统编号
		tran.setCdcnno(trxRun.getCdcnno());// DCN编号
		tran.setDtxnst(E_DTXNST.PROCESSING); // 分布式交易状态

		tran.setPrcscd(trxRun.getPrcscd()); // 交易码
		tran.setAptrtp(trxRun.getAptrtp()); // 交易类型
		tran.setTrantm(trxRun.getTrantm()); // 交易时间
		tran.setTranus(trxRun.getTranus()); // 交易柜员
		tran.setBusisq(trxRun.getBusisq()); // 业务流水
		tran.setTrbrch(trxRun.getTranbr()); // 交易机构
		tran.setServtp(trxRun.getServtp()); // 交易渠道
		tran.setCrcycd(trxRun.getCrcycd()); // 币种
		tran.setTranam(trxRun.getTranam()); // 交易金额

		
		KnpTxns txns = BaseEnvUtil.getTransactionDefine(trxRun.getPrcscd()); // 2017.5.12
																				// 根据外部处理码查询交易控制信息
		if (txns.getRverfg() != E_YES___.YES) {
			tran.setRviast(E_YES___.NO); // 是否主动冲正
			tran.setRvcdfg(txns.getSacotg()); // 当日抹账允许标志
			tran.setRvodfg(txns.getNecotg()); // 隔日抹账允许标志
			tran.setRvcdrs(null); // 当日冲正拒绝原因
			tran.setRvodrs(null); // 隔日冲正拒绝原因
			tran.setRvfxst(E_RVFXST.NONE); // 冲补账状态
			tran.setRverdt(null); // 冲正日期
			tran.setRverus(null); // 冲正柜员
			tran.setRverbr(null); // 冲正机构
			tran.setRversq(null); // 冲正交易流水号
			tran.setOrigdt(null); // 原错误日期
			tran.setOrigsq(null); // 原错账流水号
		} else {
			RverInfo rverInfo = trxRun.getRvervo();
			tran.setRviast(rverInfo.getRviast()); // 是否主动冲正
			tran.setRvcdfg(txns.getSacotg()); // 当日抹账允许标志
			tran.setRvodfg(txns.getNecotg()); // 隔日抹账允许标志
			tran.setRvcdrs(rverInfo.getRvcdrs()); // 当日冲正拒绝原因
			tran.setRvodrs(rverInfo.getRvodrs()); // 隔日冲正拒绝原因
			tran.setRvfxst(rverInfo.getRvfxst()); // 冲补账状态
			tran.setRverdt(rverInfo.getRverdt()); // 冲正日期
			tran.setRverus(rverInfo.getRverus()); // 冲正柜员
			tran.setRverbr(rverInfo.getRverbr()); // 冲正机构
			tran.setRversq(rverInfo.getRversq()); // 冲正交易流水号
			tran.setOrigdt(rverInfo.getOrigdt()); // 原错误日期
			tran.setOrigsq(rverInfo.getOrigsq()); // 原错账流水号
		}

		// TODO
		// tran.setCstrfg(); // 现转标志
		// tran.setPostst(); // 账务流水入账状态
		// tran.setPostvo(); // 入账套号
		// tran.setChacno(); // 对账单号
		// tran.setVochfg(); // 特殊传票标志

		tran.setAccofg(txns.getTrantp() == E_TRANTP.JRJY ? E_YES___.YES : E_YES___.NO); // 是否账务流水
		tran.setSqhotg(E_SQHOTG.WGD); // 勾对标志
		tran.setHookus(null); // 勾对柜员
		tran.setSpaco1(null); // 备用字段1
		tran.setSpaco2(null); // 备用字段2
		tran.setSpaco3(null); // 备用字段3
		tran.setSpaco4(null); // 备用字段4
		tran.setSpaco5(null); // 备用字段5
		tran.setSpaco6(null); // 备用字段6
		tran.setTmstmp(trxRun.getTmstmp()); // 时间戳

		KnsTranDao.insert(tran);
	}
	
	public static void updateKnsTran(String trandt ,String transq, E_DTXNST dtxnst) {
		BaseApltDao.updateKnsTranStatus(trandt, transq, dtxnst);
	}
}
