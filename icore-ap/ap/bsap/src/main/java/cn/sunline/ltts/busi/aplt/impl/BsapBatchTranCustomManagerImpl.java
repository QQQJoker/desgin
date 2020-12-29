/*package cn.sunline.ltts.busi.aplt.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.type.KBaseEnumType;
import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_PILJYZHT;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.tsp_flow_definition;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.custom.dayend.plugin.type.CustomDayEndBatchType.DayEndFlowInfo;
import cn.sunline.edsp.custom.dayend.plugin.type.CustomDayEndBatchType.MultiCorpnoDayEndStatusOut;
import cn.sunline.edsp.plugin.custom.model.DBTranDateInfo;
import cn.sunline.edsp.plugin.custom.spi.IBatchTranCustomManager;
import cn.sunline.ltts.aplt.namedsql.ApBookDao;
import cn.sunline.ltts.busi.aplt.namedsql.BaseApltDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbEodt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbEodtDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydtDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApsEodtDetl;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApsEodtDetlDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.aplt.type.ApBook;
import cn.sunline.ltts.busi.bsap.servicetype.IoApDayendBatchQuery;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_FLOWST;

*//**
 * 
 * <p>
 * Title:BsapBatchTranCustomManagerImpl
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @author XX
 * @date 2017年9月22日
 *//*
@SPIMeta(id = "BsapBatchTranCustomManagerImpl")
@Order(10)
public class BsapBatchTranCustomManagerImpl implements IBatchTranCustomManager {

	// 批量查询公共参数表的参数标志
	public static final String PARMCDZX = "EOD_ZHXXH"; // 查询执行序号
	public static final String PARMCDJY = "EOD_PLJYCS"; // 查询校验参数
	public static final String SP = "_";
	public static final String BF = "before";
	public static final String SW = "swday";
	public static final String DE = "dayend";
	public static final String ZHXXH_0 = "0";
	public static final String ZHXXH_1 = "1";
	public static final String ZHXXH_2 = "2";

	// 增加总账的pljylcbs 常量
	public static final String GL_DAYEND = "gl_dayend";
	public static final String GL_YEAREND = "gl_yearend";

	private static final BizLog bizlog = BizLogUtil.getBizLog(BsapBatchTranCustomManagerImpl.class);

	*//**
	 * 根据系统编号得到流程标识
	 *//*
	@Override
	public List<DayEndFlowInfo> getFlowIdsForSystemId(String systcd) {
		bizlog.method("getFlowIdsForSystemId>>>>>>>>>>>>>" + systcd);
		// 根据系统编号查询流程ID和法人代码
		List<ApBook.ApQueryKsysPllcdy> ksysPllcdyList = ApBookDao.selKsysPllcdy(systcd, false);
		if (CommUtil.isNull(ksysPllcdyList)) {
			throw new RuntimeException("系统编号:[" + systcd + "]的批量流程定义表(ksys_pllcdy)没有数据，请检查");
		}
		// 遍历查到的集合查询公共参数表得到执行序号
		List<DayEndFlowInfo> dayEndFlowInfoList = new ArrayList<DayEndFlowInfo>();
		for (int a = 0; a < ksysPllcdyList.size(); a++) {
			// 日终流程定义对象
			DayEndFlowInfo dayEndFlowInfo = SysUtil.getInstance(DayEndFlowInfo.class);
			String pljylcbs = ksysPllcdyList.get(a).getPljylcbs(); // 批量交易流程ID
			String zhongwmc = ksysPllcdyList.get(a).getZhongwmc(); // 中文名字
			
			 * KnpPara knpPara =
			 * ApBookDao.selKnpParaForzhxbz(CommTools.prcRunEnvs().getCorpno(),PARMCDZX,
			 * systcd, pljylcbs, false); if(CommUtil.isNull(knpPara)){ throw new
			 * RuntimeException("查询参数表parmcd=EOD_ZHXXH,pmkey1="+systcd+",pmkey2="+pljylcbs+
			 * "的记录为空，请检查"); }
			 * dayEndFlowInfo.setExenum(Integer.parseInt(knpPara.getPmval1())); //执行序号
			 
			dayEndFlowInfo.setFlowid(pljylcbs); // 批量交易流程ID
			dayEndFlowInfo.setChinna(zhongwmc); // 中文名字
			dayEndFlowInfoList.add(dayEndFlowInfo);
		}
		bizlog.method("getFlowIdsForSystemId end>>>>>>>>>>>>>");
		return dayEndFlowInfoList;
	}

	*//**
	 * 根据流程标识和系统标识获取日期
	 *//*
	@Override
	public List<String> getDayEndDatesByFlowID(String systcd, String flowId) {
		bizlog.method("getDayEndDatesByFlowID>>>>>>>>>>>>>" + systcd + "," + flowId);
		// 查询日终批量日期管理表得到批量流程日终日期
		List<ApBook.ApQueryApbEodCor> corEndDatesList = ApBookDao.selApbEodtDate(flowId, systcd, false);
		// 查询所有法人
		List<String> corpList = ApBookDao.selAppCorp(false);
		List<String> corpList1 = new ArrayList<String>();
		// 得到日终批量日期管理表中的法人
		for (int a = 0; a < corEndDatesList.size(); a++) {
			corpList1.add(corEndDatesList.get(a).getCorpno());
		}
		// 判断法人的个数相不相等
		if (!corpList1.containsAll(corpList)) {
			RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
			List<String> corpList2 = new ArrayList<String>();
			// 得到日终批量日期管理表中不存在的法人
			for (String corp : corpList) {
				if (!corpList1.contains(corp)) {
					corpList2.add(corp);
				}
			}
			// 初始化
			for (int b = 0; b < corpList2.size(); b++) {
				ApbEodt apb = CommTools.getInstance(ApbEodt.class);
				apb.setCorpno(corpList2.get(b));
				apb.setFlowid(flowId);
				apb.setFlowst(BaseEnumType.E_FLOWST.FAD);
				apb.setSystcd(systcd);
				apb.setTmstmp(runEnvs.getTmstmp());
				KnpPara knpPara = ApBookDao.selKnpParaForzhxbz(CommTools.prcRunEnvs().getCorpno(), PARMCDZX, systcd,
						flowId, false);
				if (CommUtil.isNull(knpPara)) {
					throw new RuntimeException(
							"查询参数表parmcd=EOD_ZHXXH,pmkey1=" + systcd + ",pmkey2=" + flowId + "的记录为空，请检查");
				}
				if (ZHXXH_0.equals(knpPara.getPmval1())) {
					apb.setFlowtp(BaseEnumType.E_EODTP.EOD);
					apb.setFlowdt((AppSydtDao.selectOne_odb1(corpList2.get(b), false)).getSystdt());
				} else if (ZHXXH_1.equals(knpPara.getPmval1())) {
					if (GL_YEAREND.equals(flowId)) {// 年结试算日期为年末
						apb.setFlowtp(BaseEnumType.E_EODTP.SWD);
						apb.setFlowdt((AppSydtDao.selectOne_odb1(corpList2.get(b), false)).getYreddt());
					} else {
						apb.setFlowtp(BaseEnumType.E_EODTP.SWD);
						apb.setFlowdt((AppSydtDao.selectOne_odb1(corpList2.get(b), false)).getSystdt());
					}
				} else if (ZHXXH_2.equals(knpPara.getPmval1())) {
					apb.setFlowtp(BaseEnumType.E_EODTP.BOD);
					apb.setFlowdt((AppSydtDao.selectOne_odb1(corpList2.get(b), false)).getLastdt());
				}

				ApbEodtDao.insert(apb);
			}

		} else {
			if (GL_DAYEND.equals(flowId)) {
				bizlog.debug(flowId + "总账系统再次ap0800查询时,如果上次日期记录的日终状态成功，则更新apb_eodt上次日期记录为当前日期记录，并将日终状态成功改为未开始");
				List<ApbEodt> list = ApBookDao.selApb_eodt(systcd, flowId, true);
				RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
				for (ApbEodt apbEodt : list) {
					if (apbEodt.getFlowst() == E_FLOWST.SUS) {
						String corpno_tmp = apbEodt.getCorpno();
						BaseApltDao.updateApbEodtFlowdtForFlowid(
								(AppSydtDao.selectOne_odb1(corpno_tmp, true).getSystdt()), corpno_tmp, flowId, systcd,
								runEnvs.getTmstmp());
					}
				}
			}
			if (GL_YEAREND.equals(flowId)) {
				bizlog.debug(flowId + "总账系统再次ap0800查询时,如果上次日期记录的日终状态成功，则更新apb_eodt上次日期记录为当前日期记录，并将日终状态成功改为未开始");
				List<ApbEodt> list = ApBookDao.selApb_eodt(systcd, flowId, true);
				RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
				for (ApbEodt apbEodt : list) {
					if (apbEodt.getFlowst() == E_FLOWST.SUS) {
						String corpno_tmp = apbEodt.getCorpno();
						BaseApltDao.updateApbEodtFlowdtForFlowid(
								(AppSydtDao.selectOne_odb1(corpno_tmp, true).getYreddt()), corpno_tmp, flowId, systcd,
								runEnvs.getTmstmp());
					}
				}
			}
		}
		// 重新查询表中的记录
		corEndDatesList.clear();
		corEndDatesList = ApBookDao.selApbEodtDate(flowId, systcd, false);

		// 得到日期集合
		List<String> dayEndDatesList = new ArrayList<String>();

		for (int a = 0; a < corEndDatesList.size(); a++) {
			dayEndDatesList.add(corEndDatesList.get(a).getFlowdt());
		}
		// 去除重复日期
		List<String> dateList = new ArrayList<String>();
		for (String date : dayEndDatesList) {
			if (!dateList.contains(date)) {
				dateList.add(date);
			}
		}
		bizlog.method("getDayEndDatesByFlowID>>>>>>>>>>>>>end");
		return dateList;
	}

	*//**
	 * 根据法人代码、流程标识给数据区设置日期时间
	 *//*
	@Override
	public DBTranDateInfo getDBTranDateInfo(String flowid, String corpno, String systcd, DBTranDateInfo dateInfo) {
		// 根据法人代码和系统编号查询查询日终批量日期管理表得到交易日期 查询交易系统日期参数表
		ApBook.AppSydtQueryDate queryDateInfo = ApBookDao.selAppSydt(corpno, false);

		dateInfo.setLastLastTranDate(queryDateInfo.getBflsdt()); // 上上次交易日期
		dateInfo.setLastTranDate(queryDateInfo.getLastdt()); // 上次交易日期
		dateInfo.setTranDate(queryDateInfo.getSystdt()); // 系统日期
		dateInfo.setNextTranDate(queryDateInfo.getNextdt()); // 下次交易日期
		return dateInfo;
	}

	*//**
	 * 给数据区设置交易机构、渠道号和交易柜员
	 *//*
	@Override
	public DataArea getDayEndDataArea(DataArea dataArea) {
		bizlog.method("getDayEndDataArea>>>>>>>>>>>>>" + dataArea);
		String corpno = (String) dataArea.getCommReq().get("corporate_code");
		KnpPara knpPara = ApBookDao.selKnpParaForPLJY(PARMCDJY, corpno, false);
		if (CommUtil.isNull(knpPara)) {
			throw new RuntimeException("查询参数表parmcd=EOD_PLJYCS,pmkey1=" + corpno + "的记录为空，请检查");
		}
		dataArea.getCommReq().put("corpno", corpno); // 交易法人
		dataArea.getCommReq().put("tranbr", knpPara.getPmval1()); // 交易机构
		dataArea.getCommReq().put("servno", knpPara.getPmval2()); // 渠道号
		dataArea.getCommReq().put("tranus", knpPara.getPmval3()); // 交易柜员

		return dataArea;
	}

	*//**
	 * 查询日终明细信息
	 *//*
	@Override
	public MultiCorpnoDayEndStatusOut getMultiCorpnoDayEndStu(String systcd, String flowId, String tranDate,
			String corpno) {
		bizlog.method("getMultiCorpnoDayEndStu>>>>>>>>>>>>>" + systcd + "," + flowId + "," + tranDate + "," + corpno);
		// 查询交易信息明细aps_eodt_detl
		List<ApBook.ApQueryApsEodtDetl> apsEodtDetlList = ApBookDao.selApsEodtDetl(corpno, systcd, tranDate, flowId,
				DcnUtil.getCurrDCN(), false);

		ApsEodtDetl apsEodtDetl = CommTools.getInstance(ApsEodtDetl.class);
		if(apsEodtDetlList == null || apsEodtDetlList.size()==0) {
			// 初始化表
			if (!SysUtil.isDistributedSystem()) {//集中式
				apsEodtDetl.setCorpno(corpno);
				apsEodtDetl.setDcnnum(DcnUtil.getCurrDCN());
				apsEodtDetl.setDcnste(KBaseEnumType.E_PILJYZHT.onprocess);
				apsEodtDetl.setFlowdt(tranDate);
				apsEodtDetl.setFlowid(flowId);
				apsEodtDetl.setPecent(new BigDecimal(0));
				apsEodtDetl.setSystcd(systcd);
				apsEodtDetl.setTxnsuc(0L);
				apsEodtDetl.setTxntal(0L);
				ApsEodtDetlDao.insert(apsEodtDetl);
			} else { //单元化
				List<String> dcnNos = DcnUtil.findAllDcnNosWithAdmin();
				IoApDayendBatchQuery dayendBatchQuery = SysUtil.getRemoteInstance(IoApDayendBatchQuery.class);
				for (int c = 0; c < dcnNos.size(); c++) {
					String dcnNo = dcnNos.get(c);
					apsEodtDetl.setCorpno(corpno);
					apsEodtDetl.setDcnnum(dcnNo);
					apsEodtDetl.setDcnste(KBaseEnumType.E_PILJYZHT.onprocess);
					apsEodtDetl.setFlowdt(tranDate);
					apsEodtDetl.setFlowid(flowId);
					apsEodtDetl.setPecent(new BigDecimal(0));
					apsEodtDetl.setSystcd(systcd);
					apsEodtDetl.setTxnsuc(0L);
					if (DcnUtil.isAdminDcn(dcnNos.get(c))) {
						bizlog.info("初始化apsEodtDetl时，管理节点交易总数可以正常统计出来");
						apsEodtDetl.setTxntal(ApBookDao.selKsysJykzhqCount(systcd, corpno, flowId, true));
					} else {
						// 查询零售节点交易总数 + 管理节点交易总数
						Integer count = dayendBatchQuery.queryDayendBatchCount(dcnNo, systcd, corpno, flowId);
						bizlog.info("初始化apsEodtDetl时，零售节点" + dcnNo + "交易总数:" + count);
						apsEodtDetl.setTxntal(count.longValue());
					}
					ApsEodtDetlDao.insert(apsEodtDetl);
				}
			}
		}
		// 重新查询记录数
		apsEodtDetlList.clear();
		apsEodtDetlList = ApBookDao.selApsEodtDetlNoDcn(corpno , systcd , tranDate , flowId , false);

		bizlog.info("apsEodtDetlList[%s]", apsEodtDetlList);
		
		// 多法人日终查询服务输出结构
		List<MultiCorpnoDayEndStatusOut> multiCorpnoDayEndStatusOutList = new ArrayList<MultiCorpnoDayEndStatusOut>();
		MultiCorpnoDayEndStatusOut multiCorpnoDayEndStatusOut = SysUtil.getInstance(MultiCorpnoDayEndStatusOut.class);
		
		multiCorpnoDayEndStatusOut.setCorpno(corpno); // 法人代码
		multiCorpnoDayEndStatusOut.setTrandt(tranDate); // 当前交易日期

		long txntal = 0L;// 有效交易个数
		long txnsuc = 0L;// 成功交易个数
		E_PILJYZHT dcnste = E_PILJYZHT.onprocess;
		Set<E_PILJYZHT> dcnStatus = new HashSet<>();
		for (int i = 0; i < apsEodtDetlList.size(); i++) {
			txntal += apsEodtDetlList.get(i).getTxntal();
			txnsuc += apsEodtDetlList.get(i).getTxnsuc();
			dcnStatus.add(apsEodtDetl.getDcnste());
		}
		if(dcnStatus.contains(E_PILJYZHT.processing)) {
			multiCorpnoDayEndStatusOut.setTranst(E_PILJYZHT.processing);
		} else if(dcnStatus.contains(E_PILJYZHT.distributing)) {
			multiCorpnoDayEndStatusOut.setTranst(E_PILJYZHT.distributing);
		} else if(dcnStatus.contains(E_PILJYZHT.failure)) {
			multiCorpnoDayEndStatusOut.setTranst(E_PILJYZHT.failure);
		} else {
			multiCorpnoDayEndStatusOut.setTranst(E_PILJYZHT.onprocess);
		}
		if(txnsuc==txntal) {
			multiCorpnoDayEndStatusOut.setTranst(E_PILJYZHT.success);
		}
		
		// 创建一个数值格式化对象
		NumberFormat numberFormat = NumberFormat.getInstance();
		// 设置精确到小数点后2位
		numberFormat.setMaximumFractionDigits(2);
		String percent = null;
		if (txntal == 0) {
			percent = "0";
		} else {
			percent = numberFormat.format((float) txnsuc / (float) txntal * 100);
		}
		bizlog.info("corpno:[%s],txntal:[%s],txnsuc[%s],dcnste[%s]", corpno, txntal, txnsuc, dcnste);
		multiCorpnoDayEndStatusOut.setProces(percent); // 流程执行进度
		multiCorpnoDayEndStatusOut.setTranst(dcnste); // 调度状态
		multiCorpnoDayEndStatusOut.setSucpro(txnsuc + "/" + txntal); // 完成交易数比例
		multiCorpnoDayEndStatusOutList.add(multiCorpnoDayEndStatusOut);
		bizlog.method("getMultiCorpnoDayEndStu>>>>>>>>>>>>>end");
		return multiCorpnoDayEndStatusOut;
	}

	*//**
	 * 根据模式选择执行的法人列表
	 *//*
	@Override
	public List<String> getAllExecCorpnos(String execMode, String systcd, String flowId, String tranDate,
			Options<String> corpnos) {
		bizlog.method("getAllExecCorpnos>>>>>>>>>>>>>" + execMode + "," + systcd + "," + flowId + "," + tranDate + ","
				+ corpnos);
		// 查询当前交易系统日期法人集合
		List<String> corpnoList = ApBookDao.selApbEodtCorpno(systcd, tranDate, flowId, false);
		switch (execMode) {
		// 全选
		case "1":
			corpnos.clear();
			corpnos.addAll(corpnoList);
			break;
		// 只选部分
		case "2":
			break;
		// 反选
		case "3":
			List<String> corpList = new ArrayList<String>();

			for (String corp : corpnoList) {
				if (!corpnos.contains(corp)) {
					corpList.add(corp);
				}
			}
			corpnos.clear();
			corpnos.addAll(corpnoList);
			break;
		// 选择这部分
		default:
		}
		bizlog.method("getAllExecCorpnos>>>>>>>>>>>>>end");
		return corpnos;
	}

	*//**
	 * 批量提交前的校验
	 * <p>
	 * Title:checkCorpnos
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @author XX
	 * @date 2017年9月22日
	 * @param systcd
	 * @param flowId
	 * @param tranDate
	 * @param corpnos
	 * @return
	 *//*
	public List<String> getAllExeTaskCorpnos(String systcd, String flowId, String tranDate, Options<String> corpnos) {
		bizlog.method("getAllExeTaskCorpnos>>>>>>>>>>>>>" + systcd + "," + flowId + "," + tranDate + "," + corpnos);
		List<String> corList = new ArrayList<String>();
		List<String> corSusList = new ArrayList<String>(); // 校验成功的法人列表
		corList.addAll(corpnos);
		String[] fids = flowId.split(SP); // 切割流程标识
		List<ApsEodtDetl> apsEodtDetlList = new ArrayList<ApsEodtDetl>();

		if (GL_YEAREND.equals(flowId)) {
			bizlog.info("当天跑总账系统的总账年结流程时，每跑一次都要清除当天aps_eodt_detl表中的数据");
			for (String corpno : corList) {
				AppSydt appSydt = AppSydtDao.selectOne_odb1(corpno, false);
				apsEodtDetlList = ApBookDao.selApsEodtDetlById(appSydt.getSystdt(), flowId.toString(), systcd, corpno,
						false);
				if (apsEodtDetlList == null || apsEodtDetlList.size() == 0) {
					continue;
				} else {
					for (int i = 0; i < apsEodtDetlList.size(); i++) {
						ApsEodtDetlDao.deleteOne_odb_1(apsEodtDetlList.get(i).getFlowid(),
								apsEodtDetlList.get(i).getFlowdt(), apsEodtDetlList.get(i).getCorpno());
					}
				}
			}
			bizlog.info("进入总账系统的总账年结流控制，但该流程不需要流程控制,有效的法人有：【" + corpnos + "】");
			return corpnos;
		}
		if (GL_DAYEND.equals(flowId)) {
			bizlog.info("进入总账系统的流程控制......");
			// 遍历法人
			for (String corpno : corList) {
				AppSydt appSydt = AppSydtDao.selectOne_odb1(corpno, false);
				apsEodtDetlList = ApBookDao.selApsEodtDetlById(appSydt.getLastdt(), flowId.toString(), systcd, corpno,
						false);
				if (apsEodtDetlList == null) {
					continue;
				}
				// 第一次初始化没有前日的跑批记录
				if (CommUtil.isNull(apsEodtDetlList) || apsEodtDetlList.size() == 0) {
					corSusList.add(corpno);
				} else {
					// set去重
					Set dcnSteSet = new HashSet();
					for (int b = 0; b < apsEodtDetlList.size(); b++) {
						dcnSteSet.add(apsEodtDetlList.get(b).getDcnste());
					}
					Iterator it = dcnSteSet.iterator();
					if (it.hasNext()) {
						// 所有的DCN成功只有一个结果success
						Object object = it.next();
						if (E_PILJYZHT.success.equals(object) && dcnSteSet.size() == 1) {
							corSusList.add(corpno);
						} else if (E_PILJYZHT.failure.equals(object) && dcnSteSet.size() == 1) {
							bizlog.info("总账日终流程在换日后跑后面的交易失败，此时日期已经切过了，为了能失败续跑，此时不应该加流程校验");
							corSusList.add(corpno);
						}
					}
				}
			}
			bizlog.info("进入总账日终流程检验，检验完后，有效的法人有：【" + corSusList + "】");
			return corSusList;
		}
		// 日终前流程
		if (BF.equals(fids[1])) {
			// %是为了SQL中like关键字加的
			StringBuffer flowid = new StringBuffer();
			flowid.append(fids[0]).append("%");
			List<ApsEodtDetl> apsEodtDetlList_min = new ArrayList<ApsEodtDetl>();
			// 遍历所有法人
			for (String corpno : corList) {
				// 当前法人的系统时间
				AppSydt appSydt = AppSydtDao.selectOne_odb1(corpno, false);
				// 得到日终流程
				List<tsp_flow_definition> pllcdyList = ApBookDao.selKsysPllcdyById(flowid.toString(), systcd, corpno,
						false);
				// 遍历日终流程信息
				for (int a = 0; a < pllcdyList.size(); a++) {
					// 当前流程的所有dcn状态
					apsEodtDetlList_min = ApBookDao.selApsEodtDetlById(appSydt.getLastdt(),
							pllcdyList.get(a).getTran_flow_id(), systcd, corpno, false);
					// 多个流程的保存在一起
					apsEodtDetlList.addAll(apsEodtDetlList_min);
				}
				// 第一次初始化没有前日的跑批记录
				if (CommUtil.isNull(apsEodtDetlList) || apsEodtDetlList.size() == 0 || apsEodtDetlList.size() == 1) {
					corSusList.add(corpno);
				} else {
					// set去重
					Set dcnSteSet = new HashSet();
					for (int b = 0; b < apsEodtDetlList.size(); b++) {
						dcnSteSet.add(apsEodtDetlList.get(b).getDcnste());
					}
					Iterator it = dcnSteSet.iterator();
					if (it.hasNext()) {
						// 所有的DCN成功只有一个结果success
						if (E_PILJYZHT.success.equals(it.next()) && dcnSteSet.size() == 1) {
							corSusList.add(corpno);
						}
					}
				}
			}
			bizlog.info("进入日终前流程检验，检验完后，有效的法人有：【" + corSusList + "】");
		}
		// 日切流程
		else if (SW.equals(fids[1])) {
			// 拼接日前流程
			StringBuffer flowid = new StringBuffer();
			flowid.append(fids[0]).append(SP).append(BF);
			// 遍历法人
			for (String corpno : corList) {
				apsEodtDetlList = ApBookDao.selApsEodtDetlById(tranDate, flowid.toString(), systcd, corpno, false);
				if (apsEodtDetlList == null) {
					continue;
				}
				// set去重
				Set dcnSteSet = new HashSet();
				for (int b = 0; b < apsEodtDetlList.size(); b++) {
					dcnSteSet.add(apsEodtDetlList.get(b).getDcnste());
				}
				Iterator it = dcnSteSet.iterator();
				if (it.hasNext()) {
					// 所有的DCN成功只有一个结果success
					if (E_PILJYZHT.success.equals(it.next()) && dcnSteSet.size() == 1) {
						corSusList.add(corpno);
					}
				}
			}
			bizlog.info("进入换日流程检验，检验完后，有效的法人有：【" + corSusList + "】");
		}
		// 日终后流程
		else if (DE.equals(fids[1])) {
			// 拼接成日切流程
			StringBuffer flowid = new StringBuffer();
			flowid.append(fids[0]).append(SP).append(SW);
			// 遍历法人
			for (String corpno : corList) {
				apsEodtDetlList = ApBookDao.selApsEodtDetlById(tranDate, flowid.toString(), systcd, corpno, false);
				if (apsEodtDetlList == null) {
					continue;
				}
				// set去重
				Set dcnSteSet = new HashSet();
				for (int b = 0; b < apsEodtDetlList.size(); b++) {
					dcnSteSet.add(apsEodtDetlList.get(b).getDcnste());
				}
				Iterator it = dcnSteSet.iterator();
				if (it.hasNext()) {
					// 所有的DCN成功只有一个结果success
					if (E_PILJYZHT.success.equals(it.next()) && dcnSteSet.size() == 1) {
						corSusList.add(corpno);
					}
				}
			}
			bizlog.info("进入日终后流程检验，检验完后，有效的法人有：【" + corSusList + "】");
		}
		// 单流程时候前面都不能过
		else {
			// 遍历法人
			for (String corpno : corList) {
				// 当前法人时间
				AppSydt appSydt = AppSydtDao.selectOne_odb1(corpno, false);
				apsEodtDetlList = ApBookDao.selApsEodtDetlById(appSydt.getLastdt(), flowId, systcd, corpno, false);

				// if(apsEodtDetlList == null){
				// continue;
				// }
				// 第一次初始化没有前日的跑批记录
				if (CommUtil.isNull(apsEodtDetlList) || apsEodtDetlList.size() == 0) {
					corSusList.add(corpno);
				} else {
					// set去重
					Set dcnSteSet = new HashSet();
					for (int b = 0; b < apsEodtDetlList.size(); b++) {
						dcnSteSet.add(apsEodtDetlList.get(b).getDcnste());
					}
					Iterator it = dcnSteSet.iterator();
					if (it.hasNext()) {
						// 所有的DCN成功只有一个结果success
						if (E_PILJYZHT.success.equals(it.next()) && dcnSteSet.size() == 1) {
							corSusList.add(corpno);
						}
					}
				}
			}
		}
		bizlog.method("getAllExeTaskCorpnos>>>>>>>>>>>>>end");
		return corSusList;
	}

	@Override
	public List<String> getAllManageCorpnos() {
		bizlog.method("getAllManageCorpnos>>>>>>>>>>>>>");
		List<String> result = new ArrayList<>();
		final List<String> selAppCorp = ApBookDao.selAppCorp(true);
		selAppCorp.forEach(corpno -> {
			result.add("R-" + corpno);
		});
		return result;
	}

	@Override
	public String getDayEndDateByCorpno(String flowId, String corpno, String systcd) {
		bizlog.method("getDayEndDateByCorpno>>>>>>>>>>>>>" + flowId + "," + corpno + "," + systcd);
		AppSydt sydt = AppSydtDao.selectOne_odb1(corpno, false);

		return sydt.getSystdt();
	}

	@Override
	public List<String> getExecCorpnosByManageCorpno(String systcd, String emanad, String corpno) {
		bizlog.method("getExecCorpnosByManageCorpno>>>>>>>>>>>>>" + systcd + "," + emanad + "," + corpno);
		List<String> result = Arrays.asList(corpno);
		// List<String> result = ApBookDao.selAppCorp(true);
		return result;
	}

	@Override
	public void initializationDayEndStatusOut(String flowid, String emanad, String prodid, String corpno,
			List<String> dcnsno) {
		// TODO Auto-generated method stub

	}

}
*/