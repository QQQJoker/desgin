package cn.sunline.ltts.busi.aplt.tools;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import cn.sunline.adp.cedar.base.boot.plugin.util.ExtensionUtil;
import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.custom.comm.biz.util.BatchCallBizServiceUtil;
import cn.sunline.adp.cedar.flowtran.model.TransactionConf;
import cn.sunline.adp.cedar.server.online.tables.ServerOnlineTable.tsp_service_in_log;
import cn.sunline.adp.core.profile.ProfileSwitcher;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.busi.aplt.namedsql.BaseApltDao;
import cn.sunline.ltts.busi.aplt.pckg.PckgUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPckg;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsRedu;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsReduDao;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpTxns;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpTxnsDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppChannel;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCorp;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCorpDao;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.bsap.BsapProcessManager;
import cn.sunline.ltts.busi.bsap.type.GsnType.GsnKey;
import cn.sunline.ltts.busi.bsap.util.GsnUtil;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoTellWbSvcType;
import cn.sunline.ltts.busi.iobus.type.pb.IoTellComplexType.IoTlComp_TELLER;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_APTRTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_EVNTLV;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ROUTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SERVTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TXNSTS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public class BaseEnvUtil {

	private static final BizLog bizlog = BizLogUtil.getBizLog(BaseEnvUtil.class);
	private static final SysLog log = SysLogUtil.getSysLog("sys.onl.dataArea");

	private static final String _KEY_REDU_NORMAL = "_KEY_REDU_NORMAL";
	private static final String _KEY_REDU_STRIKE = "_KEY_REDU_STRIKE";

	public static void setRunEnvs(DataArea dataArea) {

		if (bizlog.isMethodEnabled())
			bizlog.method("========setRunEnvs Begin========");

		RunEnvsComm runEnvsComm = SysUtil.getTrxRunEnvs();
		if (CommUtil.isNull(runEnvsComm.getCorpno())) { // 如果CommReq中法人为空，则从系统头中获取
			String corpno = dataArea.getSystem().getString(ApConstants.COPRNO_NAME_KEY);
			bizlog.info("从公共运行变量中取法人为空，这时候从sys区域取法人corpno");
			if (!StringUtil.isBlank(corpno)) {
				runEnvsComm.setCorpno(corpno);
			}
		}
		String prcscd = SysUtil.getInnerServiceCode();
		// String prcscd = dataArea.getSystem().getString(ApConstants.PRCSCD_NAME_KEY);
		runEnvsComm.setPrcscd(prcscd);
		runEnvsComm.setSevnlv(E_EVNTLV.NONE); // 默认事件级别为不适用
		runEnvsComm.setEvntlv(E_EVNTLV.NONE); // 默认事件级别为不适用
		// 如果交易接口input对象中存在tranam字段，则默认为runEvns的交易金额，后续业务流程中可以根据需要进行覆盖，此处仅用于登记kns_tran
		String tranam = CommUtil.nvl(dataArea.getInput().get(ApConstants.TRANAM_NAME_KEY), "0").toString();
		if (tranam != null && tranam != "0") {
			runEnvsComm.setTranam(ConvertUtil.toBigDecimal(dataArea.getInput().get(ApConstants.TRANAM_NAME_KEY)));
		}
		// 例如开户，只有币种没有交易金额，不能按照交易金额是否为空来判断
		if (dataArea.getInput().getString(ApConstants.CRCYCD_NAME_KEY) != null) {
			runEnvsComm.setCrcycd(dataArea.getInput().getString(ApConstants.CRCYCD_NAME_KEY));
		}
		
		// 1.系统代码及DCN号
		runEnvsComm.setSystcd(SysUtil.getSubSystemId()); // 当前系统编号
		if (CommUtil.isNull(runEnvsComm.getInpucd())) {
			if (SysUtil.getCurrentSystemType() == SystemType.batch) { // 若批量，为空时默认为当前子系统
				runEnvsComm.setInpucd(SysUtil.getSubSystemId());
			} else {
				throw Aplt.E0000("上送系统不能为空！");
			}
		} 
		
		runEnvsComm.setXdcnfg(E_YES___.NO); // 跨DCN标志：默认为否
		runEnvsComm.setMntrfg(E_YES___.YES);// 是否主调交易

		runEnvsComm.setCdcnno(DcnUtil.getCurrDCN()); // 当前DCN号
		runEnvsComm.setMdcnno(DcnUtil.getCurrDCN()); // 主DCN号

		// 2.路由信息
		if (CommUtil.isNull(runEnvsComm.getRouttp())) {
			runEnvsComm.setRouttp(E_ROUTTP.AccountNo); // 默认按账号路由
		}
		
		runEnvsComm.setDosvfg(E_YES___.NO); // 首个DO服务执行标志

		// 3.法人
		// 获取中心法人方法
		runEnvsComm.setCtcono(CommTools.getCenterCorpno());
		// 若未传入交易法人代码，则以中心法人处理
		if (CommUtil.isNull(runEnvsComm.getCorpno())) {
			if (SysUtil.getCurrentSystemType() == SystemType.batch) {
				runEnvsComm.setCorpno(runEnvsComm.getCtcono());
			} else {
				// 只有“是否由DRS支持多法人模式”为“否”时，才有客户化扩展实现法人号
				if (!CommTools.isDistributedSystem() && !BsapUtil.isMutilCorpnoMode()) {
					// 调用扩展点获得法人代码
					BsapProcessManager process = ExtensionUtil.getExtensionPointImpl(BsapProcessManager.POINT);
					runEnvsComm.setCorpno(process.getCorpno(dataArea));
				} else {
					// 若未扩展获得中心法人,或者为空
					runEnvsComm.setCorpno(CommTools.getCenterCorpno());
				}
			}
		}
		// 检查法人号是否存在
		KnpPara corpPara = CommTools.KnpParaQryByCorpno("system.config", SysUtil.getSubSystemId(), "corpno", "%",
				false);
		if (corpPara != null) {
			if ("1".equals(corpPara.getPmval2())) {
				AppCorp corp = AppCorpDao.selectOne_odb1(runEnvsComm.getCorpno(), false);
				if (corp == null || CommUtil.isNull(corp.getCorpno()))
					throw Aplt.E0000("交易法人不存在！");
			}
		}

		// 用于多法人支持：对于客户信息和客户额度信息表，全法人公用或各法人独立不同模式时，
		// 查询条件中的法人并非当前交易法人，继承KapCommSpec公共字段的表启用，初始化默认为当前交易法人，应用自行设置值
		runEnvsComm.setSpcono(runEnvsComm.getCorpno());
		runEnvsComm.setXcopfg(E_YES___.NO); // 交易初始化时，默认不跨法人

		// 4.日期
		ApSysDateStru cplDate = DateTools.getDateInfo(); // 交易日期
		
		if(SysUtil.getCurrentSystemType() == SystemType.onl && runEnvsComm.getInpudt() != null 
				&& !runEnvsComm.getInpudt().equals(cplDate.getSystdt()) && runEnvsComm.getServtp() != E_SERVTP.EB && runEnvsComm.getServtp() != E_SERVTP.TE) { // 上送交易日期和当前交易日期不一致报错,剔除统一后管
			
			KnpPara sysdateCheck = CommTools.KnpParaQryByCorpno("system.config", SysUtil.getSubSystemId(), "date", "%",false);
			if (sysdateCheck != null) {
				if ("1".equals(sysdateCheck.getPmval2())) {
				 // 用户客户中心特殊系统不进行校验
				}
			}else {
				throw Aplt.E0000("上送交易日期和当前交易日期不一致：上送系统日期：["+runEnvsComm.getInpudt()+"],当前日期：["+cplDate.getSystdt()+"]!!!");

			}
		}
		runEnvsComm.setTrandt(cplDate.getSystdt());
		runEnvsComm.setLstrdt(cplDate.getLastdt());
		runEnvsComm.setNxtrdt(cplDate.getNextdt());

		// 批量设置交易取交易日期为上送日期
		if (SysUtil.getCurrentSystemType() == SystemType.batch) {
			runEnvsComm.setInpudt(cplDate.getSystdt());
		} else {
			if (CommUtil.isNull(runEnvsComm.getInpudt())) {
				throw Aplt.E0000("上送交易日期不能为空！");
			}
		}
		
		// 总账子系统运行时，才获取会计日期
		if (DcnUtil.isGL()) {
			ApSysDateStru glCplDate = DateTools.getGlDateInfo();
			
			runEnvsComm.setLstrdt(glCplDate.getLastdt());
			runEnvsComm.setTrandt(glCplDate.getSystdt());
			runEnvsComm.setNxtrdt(glCplDate.getNextdt());
			
			if (SysUtil.getCurrentSystemType() == SystemType.batch) {
				runEnvsComm.setInpudt(cplDate.getSystdt());
			}
		}

		// 4.机构、柜员、交易码、渠道
		// String prcscd = runEnvsComm.getPrcscd();
		if (SysUtil.getCurrentSystemType() == SystemType.onl) {

			TransactionConf conf = OdbFactory.get().getOdbManager(TransactionConf.class)
					.selectByKey(runEnvsComm.getPrcscd());

			if (conf != null) {
				runEnvsComm.setPrcsna(conf.getLongname());
				if ("query".equals(conf.getKind())) {// ops ap0XXX交易
					runEnvsComm.setAptrtp(E_APTRTP.CXJY);
				} else if ("account".equals(conf.getKind())) {// ops ap0XXX交易
					runEnvsComm.setAptrtp(E_APTRTP.get(E_APTRTP.WHJY));
				} else {
					runEnvsComm.setAptrtp(E_APTRTP.get(conf.getKind()));
				}
			}
			runEnvsComm.setLttscd(runEnvsComm.getPrcscd());

			String tranbr = runEnvsComm.getTranbr();
			if (CommUtil.isNull(tranbr)) {
				tranbr = CommTools.getDefaultTranbr();
			}

			String tranus = runEnvsComm.getTranus();
			boolean chkTranus = false; // 是否检查柜员
			KnpPara knpPara = CommTools.KnpParaQryByCorpno("system.config", SysUtil.getSubSystemId(), "tranus", "%",
					false);
			if (knpPara != null) {
				if (CommUtil.isNull(tranus)) {
					tranus = knpPara.getPmval1();
				}
				chkTranus = ("1".equals(knpPara.getPmkey2()) ? true : false);
			}
			if (CommUtil.isNull(tranus)) {
				throw Aplt.E0000("交易柜员不能为空！");
			} else if (chkTranus) { // 调用公共模块服务检查柜员是否存在 Add by YangGX 2017-07-31
				IoTellWbSvcType tellSvc = SysUtil.getInstance(IoTellWbSvcType.class);
				IoTlComp_TELLER tellInfo = tellSvc.tlGetInfo(tranus);
				if (tellInfo == null || CommUtil.isNull(tellInfo.getUserid())) {
					throw Aplt.E0000("交易柜员不存在！");
				} else if (CommUtil.isNull(tranbr)) {
					tranbr = tellInfo.getBrchno();
					if (CommUtil.isNull(tranbr))
						throw Aplt.E0000("交易机构不能为空！");
				}
			}

			if (CommUtil.isNull(tranbr)) {
				// 只需检查即可
				IoTellWbSvcType tellSvc = SysUtil.getInstance(IoTellWbSvcType.class);
				IoTlComp_TELLER tellInfo = tellSvc.tlGetInfo(tranus);
				tranbr = tellInfo.getBrchno();
				if (CommUtil.isNull(tranbr))
					throw Aplt.E0000("交易机构不能为空！");
			}
			runEnvsComm.setTranus(tranus);
			runEnvsComm.setTranbr(tranbr);


			runEnvsComm.setMntrsq(SeqUtil.getTransqFromEnvs());
			runEnvsComm.setTransq(runEnvsComm.getMntrsq());
			
			runEnvsComm.setBusisq(dataArea.getCommReq().getString(HeaderDataConstants.BUSI_SEQ_NO));
			runEnvsComm.setInpusq(dataArea.getCommReq().getString(HeaderDataConstants.CALL_SEQ_NO));

			runEnvsComm.setTrantm(DateTools.getCurrentLocalTime());// 交易时间
			runEnvsComm.setTmstmp(DateTools.getTransTimestamp());// 交易时间戳
			
			AppChannel channel  = ParamUtil.getChannel(dataArea.getCommReq().getString("servno"),false); // 渠道初始化待处理
			if(channel == null) {
				runEnvsComm.setServno(ApConstants.DEFAULT_CHANNEL);
			}else {
				runEnvsComm.setServno(channel.getChanid());
			}
			
		} else if (SysUtil.getCurrentSystemType() == SystemType.batch) {
			if (CommUtil.isNull(prcscd)) {
				prcscd = (String) dataArea.getSystem().get(BatchConfigConstant.BATCH_TRAN_ID);
				runEnvsComm.setPrcscd(prcscd);
			}
			runEnvsComm.setLttscd(prcscd); // 2017-10-31 修复批量交易没有lttscd

			// 批量默认法人、机构和柜员 Add 2017-09-04
			String corpno = (String) dataArea.getCommReq().get(ApConstants.CORPNO_NAME_KEY);
			if (CommUtil.isNull(corpno)) {
				KnpPara batchCorp = CommTools.KnpParaQryByCorpno("system.batch", SysUtil.getSystemId(), "corpno", "%",
						false);
				if (batchCorp != null) {
					corpno = batchCorp.getPmval1();
				}
				if (CommUtil.isNull(corpno)) {
					throw Aplt.E0000("批量交易环境初始化错误，无法人代码！");
				}
			}
			String tranbr = runEnvsComm.getTranbr();
			if (CommUtil.isNull(tranbr)) {
				KnpPara brchPara = CommTools.KnpParaQryByCorpno("system.batch", SysUtil.getSystemId(), "tranbr", "%",
						false);
				if (brchPara != null) {
					tranbr = brchPara.getPmval1();
					runEnvsComm.setTranbr(tranbr);
				}
				if (CommUtil.isNull(tranbr)) {
					throw Aplt.E0000("批量交易环境初始化错误，无交易机构！");
				}
			}
			String tranus = runEnvsComm.getTranus();
			if (CommUtil.isNull(tranus)) {
				KnpPara userPara = CommTools.KnpParaQryByCorpno("system.batch", SysUtil.getSystemId(), "tranus", "%",
						false);
				if (userPara != null) {
					tranus = userPara.getPmval1();
					runEnvsComm.setTranus(tranus);
				}
				if (CommUtil.isNull(tranus)) {
					throw Aplt.E0000("批量交易环境初始化错误，无交易柜员！");
				}
			}
			// Add end

			if (CommUtil.isNull(runEnvsComm.getServtp())) {
				if (bizlog.isDebugEnabled())
					bizlog.debug("批量交易渠道号为空则设置为电子账户系统！");
				runEnvsComm.setServtp(E_SERVTP.NK);
			}
			runEnvsComm.setTrantm(DateTools.getCurrentLocalTime());// 交易时间
			runEnvsComm.setTmstmp(DateTools.getTransTimestamp());// 交易时间戳

			// 批量需要产生流水号
			runEnvsComm.setMntrsq(SeqUtil.getTransqFromEnvs());
			runEnvsComm.setTransq(runEnvsComm.getMntrsq());
			runEnvsComm.setBusisq(runEnvsComm.getMntrsq());// jym add 批量外调时需要业务流水号
			runEnvsComm.setInpusq(runEnvsComm.getMntrsq());

			// 平台写死的字段名称与iCore的字段不同：如日期、法人等 2017-06-20
			Map<String, Object> map = CommUtil.toMap(runEnvsComm);
			putFrwKeyValue(map, BatchCallBizServiceUtil.JIAOYIRQ, ApConstants.TRANDT_NAME_KEY, runEnvsComm.getTrandt());
			putFrwKeyValue(map, BatchCallBizServiceUtil.FARENDMA, ApConstants.COPRNO_NAME_KEY, runEnvsComm.getCorpno());
			putFrwKeyValue(map, BatchCallBizServiceUtil.JIAOYIMA, ApConstants.PRCSCD_NAME_KEY, runEnvsComm.getPrcscd());
		} else {
			throw Aplt.E0000("暂不支持此系统状态，请检查启动配置！");
		}

		// 5.其他：默认页码和页容量
		if (CommUtil.isNull(runEnvsComm.getPgsize()) || runEnvsComm.getPgsize() <= 0) {
			runEnvsComm.setPgsize(ApConstants.DEFAULT_PAGE_SIZE);
		}
		if (CommUtil.isNull(runEnvsComm.getPageno()) || runEnvsComm.getPageno() <= 0) {
			runEnvsComm.setPageno(1);
		}

		if (log.isInfoEnabled())
			log.info("公共运行变量：%s", runEnvsComm.toString());
		if (bizlog.isMethodEnabled())
			bizlog.method("========setRunEnvs End========");
	}

	public static KnpTxns getTransactionDefine(String prcscd) {
		KnpTxns tblKnpTxns = KnpTxnsDao.selectOne_odb1(prcscd, false);
		if (tblKnpTxns == null)
			throw Aplt.E0019(prcscd);
		return tblKnpTxns;
	}

	public static boolean isRedu(String prcscd) {
		boolean redufg = getTransactionDefine(prcscd).getRedufg() == E_YES___.YES;
		if (!redufg && getTransactionDefine(prcscd).getTrantp() != E_TRANTP.CXJY)
			throw Aplt.E0001("交易码[" + prcscd + "]为非查询交易，不允许配置为不防重！");
		return redufg;
	}

	public static boolean isRegistPckg(String prcscd) {
		return getTransactionDefine(prcscd).getPckgfg() == E_YES___.YES;
	}

	/**
	 * 防重处理 kns_redu状态变化：
	 * <li>首次开始执行则P-处理中，若处理成功则S-成功，若处理失败有两种情况：</li>
	 * <li>1.自动发起冲正处理，若冲正未完全完成，则改为U-失败且冲正未成功；完全完成在为F-失败</li>
	 * <li>2.若对成功的交易发起冲正，冲正完成则为K-已冲正；若部分冲正，则为N-成功且冲正未完整</li>
	 */
	public static void doReduplicative() {
		if (SysUtil.isReadOnly())
			return;

		RunEnvsComm runEnvsComm = SysUtil.getTrxRunEnvs();
		List<KnsRedu> traninList = KnsReduDao.selectAll_odb4(runEnvsComm.getInpusq(), runEnvsComm.getInpudt(), false);
		int totalTimes = 0; // 重复次数或错误次数
		if (traninList != null) {
			if (!isRedu(runEnvsComm.getPrcscd())) {// 如果不需要防重，记录同一上送流水号一共有多少笔（包含失败和成功）
				totalTimes = traninList.size();
			} else { // 如果需要防重，则只能成功一次，且处理中和部分失败不允许重做，若超过最大重试次数在拒绝
				for (KnsRedu in : traninList) {
					if (in.getTxnsts() == E_TXNSTS.SUCCESS) // 已成功
						throw ApError.Sys.E9000(runEnvsComm.getInpusq());
					else if (in.getTxnsts() == E_TXNSTS.PROCESSING) // 处理中
						throw ApError.Sys.E9001(runEnvsComm.getInpusq());
					else if (in.getTxnsts() == E_TXNSTS.UNKNOW) // 交易失败，但分布式冲正未完全成功
						throw ApError.Sys.E9002(runEnvsComm.getInpusq());
					else if (in.getTxnsts() == E_TXNSTS.UNSTRIK) // 原交易成功，但对原交易冲正未完整
						throw ApError.Sys.E9004(runEnvsComm.getInpusq());
					else
						totalTimes++;
				}
				if (totalTimes >= ApConstants.MAX_TXN_TRY_TIMES) // 超过重试次数
					throw ApError.Sys.E9003(runEnvsComm.getInpusq(), ApConstants.MAX_TXN_TRY_TIMES);
			}
		}

		// 登记防重
		final KnsRedu redu = SysUtil.getInstance(KnsRedu.class);
		runEnvsComm.setRetrtm(totalTimes + 1); // 放入RunEnv,以备交易完成后进行错误更改
		redu.setInpudt(runEnvsComm.getInpudt());
		redu.setInpusq(runEnvsComm.getInpusq());
		redu.setRetrtm(runEnvsComm.getRetrtm());
		redu.setTrandt(runEnvsComm.getTrandt());
		redu.setTransq(CommUtil.isNull(runEnvsComm.getTransq()) ? runEnvsComm.getInpusq() : runEnvsComm.getTransq());
		redu.setPckgdt(runEnvsComm.getPckgdt());
		redu.setPckgsq(runEnvsComm.getPckgsq());
		redu.setCorpno(runEnvsComm.getCorpno());
		redu.setCdcnno(runEnvsComm.getCdcnno());
		redu.setBusisq(runEnvsComm.getBusisq());
		redu.setMntrsq(runEnvsComm.getMntrsq());
		redu.setPrcscd(runEnvsComm.getPrcscd());
		redu.setTrantm(runEnvsComm.getTrantm());
		redu.setTxnsts(E_TXNSTS.PROCESSING);// 处理中
		redu.setErrocd(null);
		setKnsRedu(false, redu);// 将防重信息写到线程缓存中，以便更新时使用,false表示当前交易，而不是被冲正交易

		DaoUtil.executeInNewTransation(new RunnableWithReturn<Integer>() {
			@Override
			public Integer execute() {
				KnsReduDao.insert(redu);
				return null;
			}
		});
	}

	// 将防重表中的错误码从“处理中”改为“成功”或失败
	// TODO 冲正未完整情况
	public static void updateReduplicative(final E_TXNSTS oldTxnsts, final E_TXNSTS newTxnsts, final String errorCode,
			boolean byNewTransaction) {
		final RunEnvsComm runEnvsComm = SysUtil.getTrxRunEnvs();
		if (byNewTransaction) {
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {
					if (CommUtil.isNull(errorCode)) // 未提供错误码不更新
						BaseApltDao.updateKnsReduNoErorcd(runEnvsComm.getInpudt(), runEnvsComm.getInpusq(), oldTxnsts,
								newTxnsts);
					else
						BaseApltDao.updateKnsRedu(runEnvsComm.getInpudt(), runEnvsComm.getInpusq(), oldTxnsts,
								newTxnsts, errorCode);
					return null;
				}
			});
		} else {
			if (CommUtil.isNull(errorCode)) // 未提供错误码不更新
				BaseApltDao.updateKnsReduNoErorcd(runEnvsComm.getInpudt(), runEnvsComm.getInpusq(), oldTxnsts,
						newTxnsts);
			else
				BaseApltDao.updateKnsRedu(runEnvsComm.getInpudt(), runEnvsComm.getInpusq(), oldTxnsts, newTxnsts,
						errorCode);
		}
	}

	/**
	 * 设置服务调用的被调方RunEnvs 调用方必须将：上送交易日期、上送系统编号、上送系统流水号、重发次数等必要信息准确上送
	 * 
	 * @param dataArea
	 */
	public static void setServiceRunEnvs(DataArea dataArea) {
		RunEnvsComm runEnvsComm = SysUtil.getTrxRunEnvs();

		runEnvsComm.setSystcd(SysUtil.getSubSystemId()); // 当前子系统编号
		runEnvsComm.setDosvfg(E_YES___.NO);

		// 取当前DCN交易日期
		// RunEnvsComm.setLstrdt();
		ApSysDateStru cplDate = DateTools.getDateInfo(); // 交易日期
		runEnvsComm.setTrandt(cplDate.getSystdt());
		runEnvsComm.setLstrdt(cplDate.getLastdt());
		runEnvsComm.setNxtrdt(cplDate.getNextdt());
	}
	
	
	
	/**
	 * 临时保存防重信息
	 * 
	 * @param isStrike
	 *            是否被冲正的防重信息
	 * @param redu
	 */
	public static void setTspServiceIn(boolean isStrike, tsp_service_in_log tspServiceInLog) {
	
		if (isStrike) {
			if (ApltEngineContext.getTxnTempObjMap().containsKey(_KEY_REDU_STRIKE))
				throw ApError.Aplt.E0000("服务对象[冲正]已经存在，不能再设置！");
			ApltEngineContext.getTxnTempObjMap().put(_KEY_REDU_STRIKE, tspServiceInLog);
		} else {
			if (ApltEngineContext.getTxnTempObjMap().containsKey(_KEY_REDU_NORMAL))
				throw ApError.Aplt.E0000("服务对象已经存在，不能再设置！");
			ApltEngineContext.getTxnTempObjMap().put(_KEY_REDU_NORMAL, tspServiceInLog);
		}
	}

	
	
	
	/**
	 * 临时保存防重信息
	 * 
	 * @param isStrike
	 *            是否被冲正的防重信息
	 * @param redu
	 */
	public static void setKnsRedu(boolean isStrike, KnsRedu redu) {
		if (isStrike) {
			if (ApltEngineContext.getTxnTempObjMap().containsKey(_KEY_REDU_STRIKE))
				throw ApError.Aplt.E0000("防重对象[冲正]已经存在，不能再设置！");
			ApltEngineContext.getTxnTempObjMap().put(_KEY_REDU_STRIKE, redu);
		} else {
			if (ApltEngineContext.getTxnTempObjMap().containsKey(_KEY_REDU_NORMAL))
				throw ApError.Aplt.E0000("防重对象已经存在，不能再设置！");
			ApltEngineContext.getTxnTempObjMap().put(_KEY_REDU_NORMAL, redu);
		}
	}

	public static KnsRedu getKnsRedu(boolean isStrike) {
		KnsRedu redu = null;
		if (isStrike) {
			redu = (KnsRedu) ApltEngineContext.getTxnTempObjMap().get(_KEY_REDU_STRIKE);
		} else {
			redu = (KnsRedu) ApltEngineContext.getTxnTempObjMap().get(_KEY_REDU_NORMAL);
		}
		if (CommUtil.isNull(redu))
			throw ApError.Aplt.E0000("防重对象不存在！");
		return redu;
	}

	private static void putFrwKeyValue(Map<String, Object> map, String frwKey, String myKey, Object myValue) {
		if (!CommUtil.equals(frwKey, myKey))
			map.put(frwKey, myValue);
	}

	public static boolean isPocNoRedu() {
		KnpPara para = KnpParaDao.selectOne_odb1("poc.no.redu", "%", "%", "%", SysUtil.getDefaultTenantId(), false);

		if (para == null || "0".equals(para.getPmval1())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 特别说明： 架构含义——登记报文流水； 用于防重和报文流水登记
	 */
	public static void registPackageSequence(String pckgsq, String pckgdt, RequestData request, ResponseData response,
			java.util.Date beginTime, Throwable cause, boolean autonomous) {

		if (SysUtil.isReadOnly())
			return;

		// 0.准备错误信息
		RunEnvsComm trxRun = SysUtil.getTrxRunEnvs();
		String errocd = response.getHeaderData().getResponseCode();// 错误代码
		String errosk = "";// 错误堆栈
		String errotx = "";// 错误信息
		// 交易错误使用独立事务登记：若“系统参数.选项=cdc=true”时，游标采用独立事务，说明是使用分布式数据库的且不支持跨分片游标，在必须使用独立事务
		boolean byNewTransaction = ProfileSwitcher.get().enabledCursorDependConnection;
		try {
			if (cause != null) {
				byNewTransaction = true; // 交易错误使用独立事务登记
				errocd = response.getBody().getSystem().getString("erorcd");
				errotx = StringUtil.maxstr(response.getBody().getSystem().getString("erortx"), SysUtil.getDbEncoding(),
						BaseDict.Comm.errotx.getLength());
				errosk = StringUtil.maxstr(ExceptionUtil.getStackTraceMessage(cause), SysUtil.getDbEncoding(),
						BaseDict.Comm.errosk.getLength());
			}

			// 2. 将防重表中的错误码从“处理中”改为“成功”或“失败”
			E_TXNSTS newTxnsts = E_TXNSTS.get(response.getHeaderData().getRetStatus());
			// 若原状态不为PROCESS更新不到，不报错。
			if (!BaseEnvUtil.isPocNoRedu())
				BaseEnvUtil.updateReduplicative(E_TXNSTS.PROCESSING, newTxnsts, errocd, byNewTransaction);
		} catch (Exception e) {
			throw e;
		}

		E_YES___ pckgfg = BaseEnvUtil.getTransactionDefine(trxRun.getPrcscd()).getPckgfg();
		if (pckgfg != E_YES___.YES) {
			if (bizlog.isDebugEnabled())
				bizlog.debug("交易[" + trxRun.getPrcscd() + "]配置为不登记报文流水！故跳过");
			return;
		}
		// 2. 登记交易报文：同步或异步
		KnsPckg pckg = SysUtil.getInstance(KnsPckg.class);
		CommUtil.copyProperties(pckg, trxRun);
		pckg.setPckgdt(pckgdt);
		pckg.setPckgsq(trxRun.getPckgsq());
		pckg.setTrandt(trxRun.getTrandt());
		pckg.setTransq(trxRun.getTransq());
		pckg.setTrbrch(trxRun.getTranbr());

		pckg.setHostdt(DateTools.getSystemDate()); // 主机日期
		pckg.setSepsno(SysUtil.getChannelId());// 服务通道号
		pckg.setCorpno(trxRun.getCorpno());
		pckg.setPrcscd(trxRun.getPrcscd());
		pckg.setHostip(SysUtil.getSvcId());

		pckg.setRequme(JsonUtil.format(request.getData())); // 请求报文
		pckg.setRespme(JsonUtil.format(response.getData())); // 响应报文
		pckg.setErrocd(errocd);
		pckg.setErrosk(errosk);
		pckg.setErrotx(errotx);

		// 耗时计算，尽量靠后面，相对准确
		long endTime = System.currentTimeMillis();
		pckg.setStartm(Long.valueOf(DateUtil.formatDate(beginTime, "HHmmssSSS")));// 开始时间
		pckg.setEndttm(endTime); // 结束时间
		pckg.setTrantc(endTime - beginTime.getTime());// 交易耗时

		if (log.isDebugEnabled())
			log.debug("To put Queue pckg is:" + pckg);
		// 将报文放入队列或直接登记
		PckgUtil.put(pckg, byNewTransaction);
	}

	public static String getPckgsqByGsn(String pckgdt) {
		GsnKey key = SysUtil.getInstance(GsnKey.class);
		key.setSncode("pckgsq");
		key.setSystcd(SysUtil.getSystemId());
		key.setTrandt(pckgdt);
		key.setCorpno(null);
		key.setDcnnoo(DcnUtil.getCurrDCN());
		return GsnUtil.genSerialNumber(key);
	}

}
