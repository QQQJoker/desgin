package cn.sunline.ltts.busi.hctran.batch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.bsap.util.DaoSplitInvokeUtil;
import cn.sunline.ltts.busi.dp.servicetype.IoDpAcctSvcType;
import cn.sunline.ltts.busi.hc.namedsql.HcQuerySqlDao;
import cn.sunline.ltts.busi.hc.tables.HcbFailDetl.HcbFldl;
import cn.sunline.ltts.busi.hc.tables.HcbPendDeal.HcbPedl;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbBlceDetl;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefnDao;
import cn.sunline.ltts.busi.hc.util.HotCtrlUtil;
import cn.sunline.ltts.busi.iobus.servicetype.ac.IoAcAccountServ;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoAccountSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.servicetype.ln.IoLnQryLoanInfo;
import cn.sunline.ltts.busi.iobus.servicetype.ln.IoLnQryLoanInfo.IoLnQryRisk;
import cn.sunline.ltts.busi.iobus.servicetype.ln.IoLnQryLoanInfo.IoLnQryRisk.Output;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlBill;
import cn.sunline.ltts.busi.iobus.type.ac.IoAcServType.IoAccounttingIntf;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLTYPE;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DETLSS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HCTYPE;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATOWTP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_ATSQTP;
/**
 * 更新到连续的明细或账单
 * @author jiangyaming
 * 2018年4月12日 下午1:38:39
 */
public class HcBalToDetailProcesser extends LttsTimerProcessor{

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(HcBalToDetailProcesser.class);
	
	@Override
	public void process(String arg0, DataArea paramData) {
		BIZLOG.info("HcBalToDetailProcesser begin process");
		String tabnum=HotCtrlUtil.getTabnum(paramData);//分表号
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();	
		boolean hasData = false;//是否有待处理数据
		String corpno = runEnvs.getCorpno();
		String trandt = HotCtrlUtil.getHotCtrlDate();
		KnpPara knpPara1 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "TIME",true);
		KnpPara knpPara2 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "SELECT_LIMIT",true);
		KnpPara knpPara3 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "UPDATE_LIMIT",true);
		//KnpPara knpPara3 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "FAIL_COUNT",true);
		int processTime = Integer.parseInt(knpPara1.getPmval1());//秒,运行时间
		int waitTime = Integer.parseInt(knpPara1.getPmval3());//秒，等待时间
		int failCunt=Integer.parseInt(knpPara2.getPmval4());//失败次数
		int limiCunt=Integer.parseInt(knpPara2.getPmval1());//查询条数
		int updateCunt=Integer.parseInt(knpPara3.getPmval1());//更改限制条数需要比查询条数多
		long startTime = System.currentTimeMillis();
		do {
			//DaoUtil.beginTransaction();
			//处理失败次数达到上限的数据
			//processFailTooMany(tabnum,failCunt,updateCunt);
			//余额已处理数据 更新为 其他处理中
			//int count = 0;
			try {
				updHcbPedlStatus(tabnum,corpno, trandt,limiCunt,failCunt,updateCunt);
				//HcQuerySqlDao.updHcbPedlStatus("hcb_pedl"+tabnum,corpno, trandt, E_DETLSS.QTZ, E_DETLSS.YCL,limiCunt);
				//List<HcbPedl> balDetailListYCL=HcQuerySqlDao.selBalDetailList("hcb_pedl"+tabnum, trandt, corpno, E_DETLSS.YCL,null,limiCunt, false);
			} catch (Exception e) {
				BIZLOG.error("更新状态为处理中失败", e);
			}
			//BIZLOG.info("HcBalToDetailProcesser待处理数据[%s]条", count);
			//DaoUtil.commitTransaction();
			//if(count > 0) {
				//其他处理中 数据
			List<HcbPedl> balDetailList=HcQuerySqlDao.selBalDetailList("hcb_pedl"+tabnum, trandt, corpno, E_DETLSS.QTZ,null,limiCunt, false);
			//List<HcbPedl> balDetailList = HcQuerySqlDao.selBalDetailList("hcb_pedl"+tabnum,trandt, corpno, E_DETLSS.QTZ, false);
				//余额已处理 数据 生成账单或流水
				if(CommUtil.isNotNull(balDetailList) && balDetailList.size() > 0) {
					BIZLOG.info("HcBalToDetailProcesser其他处理中数据[%s]条", balDetailList.size());
					processBalList(balDetailList,tabnum);
					hasData = true;
				}else{		
				    hasData = false;
				}
			if(!hasData) {
				// 如果无数据,则等待waitTime
				BIZLOG.info("HcbFldlToDetailProcesser.waitTime：" + waitTime + "秒");
				try {
					Thread.sleep(waitTime*1000);
				} catch (InterruptedException e) {
					BIZLOG.error("[%s]", e.toString());
				}
			}
			long endTime = System.currentTimeMillis();
			BIZLOG.info("HcbFldlToDetailProcesser.processTime：" + processTime + "秒");
			if((endTime - startTime)/1000 > processTime) {
				break;
			}
		} while (true);
		BIZLOG.info("HcBalToDetailProcesser end process");
	}

	//独立事务
	public void updHcbPedlStatus(final String tabnum,final String corpno, final String trandt,final int limiCunt,final int failCunt,final int updateCunt) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				//处理失败次数达到上限的数据
				BIZLOG.debug("==================处理失败次数达到上限的数据==================");
				processFailTooMany(tabnum,failCunt,updateCunt);
				// 1、登记占用明细表
				BIZLOG.debug("==================更新状态==================");
				HcQuerySqlDao.updHcbPedlStatus("hcb_pedl"+tabnum,corpno, trandt, E_DETLSS.QTZ, E_DETLSS.YCL,limiCunt,null);
				return null;
			}
		});
	}
	
	
	//独立事务
	public void processAcct(final String acct,final List<HcbPedl> balDetailList) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				processAcctDp(acct,balDetailList);
//				processAcsqDp(acct,balDetailList);已是独立事务 jizhirong 20180530
				return null;
			}
		});
		processAcsqDp(acct,balDetailList);
	}
	
	/**
	 * 待处理表处理失败次数达到上限，转存到失败表
	 * @author jiangyaming
	 * 2018年4月12日 上午10:56:02
	 */
	private void processFailTooMany(String tabnum,int failCunt,int limiCunt) {
		//处理失败次数达到上限的数据
		//RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		//String corpno = runEnvs.getCorpno();
		String trandt = HotCtrlUtil.getHotCtrlDate();
		//KnpPara knpPara = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "FAIL_COUNT",true);
		//long count = Long.parseLong(knpPara.getPmval1());//失败次数
		//转存到失败表
		//HcQuerySqlDao.transferFailTooMany("hcb_fldl"+tabnum,"hcb_pedl"+tabnum,corpno,trandt,count,E_DETLSS.YCL);
		//删除待处理表数据
		//HcQuerySqlDao.delFailTooMany("hcb_pedl"+tabnum,corpno,trandt,count,E_DETLSS.YCL);
		//int count = Integer.parseInt(knpPara.getPmval1());//失败次数
		//查出待处理明细表相关数据转到失败表
		List<HcbPedl> hcbPedlList=HcQuerySqlDao.selBalDetailList("hcb_pedl"+tabnum, trandt, CommTools.prcRunEnvs().getCorpno(), E_DETLSS.YCL,failCunt,limiCunt, false);
		//List<HcbPedl> hcbPedlList=(List<HcbPedl>) DaoSplitInvokeUtil.selectAll(HcbPedl.class, "selectAll_odb4", tabnum, trandt,failCunt,E_DETLSS.YCL,false);
		if(CommUtil.isNotNull(hcbPedlList)){
			for(HcbPedl hcbPedl:hcbPedlList){	
				HcbFldl hcbFldl=SysUtil.getInstance(HcbFldl.class);
				hcbFldl.setAmntcd(hcbPedl.getAmntcd());
				hcbFldl.setCdcnno(hcbPedl.getCdcnno());
				hcbFldl.setCorpno(hcbPedl.getCorpno());
				hcbFldl.setDealtm(0);
				hcbFldl.setDetlsq(hcbPedl.getDetlsq());
				hcbFldl.setDetlss(E_DETLSS.YCL);
				hcbFldl.setHcmain(hcbPedl.getHcmain());
				hcbFldl.setHctype(hcbPedl.getHctype());
				hcbFldl.setNestyn(hcbPedl.getNestyn());
				hcbFldl.setTranam(hcbPedl.getTranam());
				hcbFldl.setTrandt(hcbPedl.getTrandt());
				hcbFldl.setTransq(hcbPedl.getTransq());
				DaoSplitInvokeUtil.insert(hcbFldl, tabnum);
				//删除待处理表数据
				DaoSplitInvokeUtil.deleteOne(HcbPedl.class, "deleteOne_odb1", tabnum,hcbPedl.getTrandt(), hcbPedl.getTransq());
			}
		}
	}

	/**
	 * 余额明细生成账单或流水
	 * 按主体账号 分别处理账单 
	 * @author jiangyaming
	 * 2018年4月12日 下午2:58:04
	 */
	private void processBalList(List<HcbPedl> balDetailList,String tabnum) {
		HashSet<String> hcAcctListCL = new HashSet<>();
		HashSet<String> hcAcctListDP = new HashSet<>();
		HashSet<String> hcAcctListIN = new HashSet<>();
		for(HcbPedl entity:balDetailList) {
			if(CommUtil.compare(entity.getHctype(),E_HCTYPE.CL) == 0) {
				hcAcctListCL.add(entity.getHcmain());
			} else if(CommUtil.compare(entity.getHctype(),E_HCTYPE.DP) == 0) {
				hcAcctListDP.add(entity.getHcmain());
			} else if(CommUtil.compare(entity.getHctype(),E_HCTYPE.IN) == 0) {
				hcAcctListIN.add(entity.getHcmain());
			}
		}
		Iterator<String> iteratorCL = hcAcctListCL.iterator();
		while (iteratorCL.hasNext()) {
			String acct = iteratorCL.next();
			processAcct(acct,balDetailList,tabnum);
		}
		Iterator<String> iteratorDP = hcAcctListDP.iterator();
		while (iteratorDP.hasNext()) {
			String acct = iteratorDP.next();
			try {
				//DaoUtil.beginTransaction();
				//processAcctDp(acct,balDetailList);
				//processAcsqDp(acct,balDetailList);
				//DaoUtil.commitTransaction();
				processAcct(acct,balDetailList);
				
			} catch (Exception e) {
				BIZLOG.error("[%s]", "生成余额账单 并生产流水 失败"+e);
				updateBalDetailStatus(acct,E_DETLSS.YCL,tabnum);
				continue;
			}
			updateBalDetailStatus(acct,E_DETLSS.QTY,tabnum);
		}
	}

	/**
	 * 登记会计流水
	 * @author jiangyaming
	 * 2018年4月24日 上午11:26:55
	 */
	private void processAcsqDp(String acctno, List<HcbPedl> balDetailList) {
		List<IoAccounttingIntf> list = new ArrayList<>();
		for(HcbPedl hcbPedl:balDetailList) {
			if(CommUtil.compare(hcbPedl.getHcmain(),acctno) == 0) {
				//登记会计流水
		        IoAccounttingIntf cplIoAccounttingIntf = SysUtil
		                .getInstance(IoAccounttingIntf.class);
		        cplIoAccounttingIntf.setCorpno(hcbPedl.getCorpno());
		        cplIoAccounttingIntf.setAcctno(acctno);
		        cplIoAccounttingIntf.setTrandt(hcbPedl.getTrandt());
		        cplIoAccounttingIntf.setTransq(hcbPedl.getTransq());
		        cplIoAccounttingIntf.setTranam(hcbPedl.getTranam());
		        cplIoAccounttingIntf.setAcctdt(hcbPedl.getTrandt());
		        cplIoAccounttingIntf.setAmntcd(hcbPedl.getAmntcd());
//		        cplIoAccounttingIntf.setAcctbr();
//		        cplIoAccounttingIntf.setDtitcd();
//		        cplIoAccounttingIntf.setCrcycd();
		        if(CommUtil.compare(hcbPedl.getAmntcd(),E_AMNTCD.CR) == 0) {
		        	cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
				} else if(CommUtil.compare(hcbPedl.getAmntcd(),E_AMNTCD.DR) == 0) {
					cplIoAccounttingIntf.setAmntcd(E_AMNTCD.DR);
				}
		        cplIoAccounttingIntf.setAtowtp(E_ATOWTP.DP);
		        cplIoAccounttingIntf.setTrsqtp(E_ATSQTP.ACCOUNT);
		        cplIoAccounttingIntf.setBltype(E_BLTYPE.BALANCE);
		        // 登记交易信息，供总账解析
		        if (CommUtil.equals(
		                "1",
		                CommTools.KnpParaQryByCorpno("GlAnalysis", "switch", "%", "%",
		                        true).getPmval1())) {
		            KnpPara para = SysUtil.getInstance(KnpPara.class);
		            para = CommTools.KnpParaQryByCorpno("GlAnalysis", "1010000", "%", "%",
		                    true);
		            cplIoAccounttingIntf.setTranms(para.getPmval1());// 登记交易信息 20160701
		                                                             // 产品增加
		        }
		        list.add(cplIoAccounttingIntf);
			}
		}
		if(CommUtil.isNotNull(list)) {
			IoAcAccountServ svcType = SysUtil.getInstance(IoAcAccountServ.class);
			Options<IoAccounttingIntf> data = new DefaultOptions<>();
			data.setValues(list);
			svcType.ioAccounttingHot(data);
		}
	}

	/**
	 * 处理单个热点账户 生成连续账单或余额列表 负债账户
	 * @author jiangyaming
	 * 2018年4月23日 下午4:36:07
	 */
	private void processAcctDp(String acctno, List<HcbPedl> balDetailList) {
		BIZLOG.info("processAcctDP begin 活期账号[[%s],总明细条数[[%s]", acctno, balDetailList.size());
		IoDpAcctSvcType svcType = SysUtil.getInstance(IoDpAcctSvcType.class);
		IoDpKnlBill knlBillRecent = svcType.getRecentKnlBill(acctno);
		BigDecimal acctbl = BigDecimal.ZERO;
		long detlsq = 0;
		if(CommUtil.isNotNull(knlBillRecent)) {
			acctbl = knlBillRecent.getAcctbl();
			detlsq = knlBillRecent.getDetlsq() + 1;
		}
		List<IoDpKnlBill> knlBillList = new ArrayList<IoDpKnlBill>();
		IoDpKnaAcct knaAcct = SysUtil.getRemoteInstance(IoDpSrvQryTableInfo.class).getKnaAcctOdb1(acctno, true);
		for(HcbPedl hcbPedl:balDetailList) {
			if(CommUtil.compare(hcbPedl.getHcmain(),acctno) == 0) {//加工成账单
				IoDpKnlBill knlBill = SysUtil.getInstance(IoDpKnlBill.class);
				if(CommUtil.compare(hcbPedl.getAmntcd(),E_AMNTCD.CR) == 0) {
					acctbl = acctbl.add(hcbPedl.getTranam());
				} else if(CommUtil.compare(hcbPedl.getAmntcd(),E_AMNTCD.DR) == 0) {
					acctbl = acctbl.subtract(hcbPedl.getTranam());
				}
				knlBill.setAcctbl(acctbl);
				knlBill.setAcctno(acctno);
				knlBill.setAmntcd(hcbPedl.getAmntcd());
				knlBill.setTrandt(hcbPedl.getTrandt());
				knlBill.setTransq(hcbPedl.getTransq());
				knlBill.setTranam(hcbPedl.getTranam());
				knlBill.setCorpno(hcbPedl.getCorpno());
				knlBill.setDetlsq(++detlsq);
				//新增   @jizhirong 20180515
				knlBill.setTrancy(CommToolsAplt.prcRunEnvs().getCrcycd());
				knlBill.setCustac(knaAcct.getCustac());
				knlBill.setIntrcd(CommToolsAplt.prcRunEnvs().getPrcscd());
				knlBill.setAcctna(knaAcct.getAcctna());
				knlBillList.add(knlBill);
			}
		}
		if(knlBillList.size() > 0) {
			Options<IoDpKnlBill> option = new DefaultOptions<>();
			option.setValues(knlBillList);
			svcType.batchInsertFirstKnlBill(option);
//			try {
//				Options<IoDpKnlBill> option = new DefaultOptions<>();
//				option.setValues(knlBillList);
//				svcType.batchInsertFirstKnlBill(option);
//				updateBalDetailStatus(acctno,E_DETLSS.QTY);
//			} catch (Exception e) {
//				BIZLOG.error("[%s]", "生成余额账单失败"+e);
//				updateBalDetailStatus(acctno,E_DETLSS.YCL);
//			}
		}
		BIZLOG.info("processAcctDp end");
		
	}

	/**
	 * 处理单个热点账户 生成连续账单或余额列表
	 * @author jiangyaming
	 * 2018年4月12日 下午2:41:21
	 */
	private void processAcct(String acct, List<HcbPedl> balDetailList,String tabnum) {
		BIZLOG.info("processAcct begin 账号[[%s],总明细条数[[%s]", acct,balDetailList.size());
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		//String trandt = HotCtrlUtil.getHotCtrlDate();
		String corpno = runEnvs.getCorpno();
		//查询最近一条余额明细
		HcbBlceDetl hcbBlceDetl = HcQuerySqlDao.selHcbBlceDetl(corpno,acct,false);
		//查询余额表
		//HcbBlce hcbBlce = HcbBlceDao.selectOne_odb1(acct, true);
		HcpDefn hcpDefn=HcpDefnDao.selectOne_odb1(acct, true);
		BigDecimal remain = BigDecimal.ZERO;
		boolean isInit = false;
		if(CommUtil.isNotNull(hcbBlceDetl)) {
			BIZLOG.info("最近一条明细[%s]", hcbBlceDetl);
			remain = hcbBlceDetl.getRemain();
		} else {
			
			try{
				//第一条记录余额从保证金参数查询得出
				Output riskout=SysUtil.getInstance(IoLnQryRisk.Output.class);
				SysUtil.getInstance(IoLnQryLoanInfo.class).qryLnbRisk(acct, riskout);
				//remain = hcbBlce.getBalnce();
				remain = riskout.getRiskam();
			} catch (Exception e) {
				BIZLOG.error("[%s]", "生成余额账单失败"+e);
				updateBalDetailStatus(acct,E_DETLSS.YCL,tabnum);
			}		
			isInit = true;
		}
		List<HcbBlceDetl> blceList = new ArrayList<HcbBlceDetl>();
		for(HcbPedl hcbPedl:balDetailList) {
			if(CommUtil.compare(hcbPedl.getHcmain(),acct) == 0) {//加工成账单
				HcbBlceDetl detail = SysUtil.getInstance(HcbBlceDetl.class);
				detail.setCorpno(hcbPedl.getCorpno());
				detail.setHcacct(acct);
				detail.setDetlss(E_DETLSS.WCL);
				detail.setTranam(hcbPedl.getTranam());
				detail.setTrandt(hcbPedl.getTrandt());
				detail.setTransq(hcbPedl.getTransq());
				detail.setAmntcd(hcbPedl.getAmntcd());
				if(!isInit) {
					if(CommUtil.compare(hcpDefn.getBlncdr().toString(), detail.getAmntcd().toString()) == 0) {
						remain = remain.add(hcbPedl.getTranam());
					} else {
						remain = remain.subtract(hcbPedl.getTranam());
					}
				}
				isInit = false;
				detail.setRemain(remain);
				detail.setTmstmp(DateTools.getCurrentTimestamp21());
				blceList.add(detail);
			}
		}
		if(blceList.size() > 0) {
			try {
				DaoUtil.insertBatch(HcbBlceDetl.class, blceList);
				updateBalDetailStatus(acct,E_DETLSS.QTY,tabnum);
			} catch (Exception e) {
				BIZLOG.error("[%s]", "生成余额账单失败"+e);
				updateBalDetailStatus(acct,E_DETLSS.YCL,tabnum);
			}
			
		}
		BIZLOG.info("processAcct end");
	}

	private void updateBalDetailStatus(String acct, E_DETLSS status,String tabnum) {
		BIZLOG.info("账号[%s],更新已处理的明细状态[%s]，",acct, status);
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		String corpno = runEnvs.getCorpno();
		String trandt = HotCtrlUtil.getHotCtrlDate();
		if(status == E_DETLSS.YCL) {//余额更新失败，次数++，状态改为未处理
			HcQuerySqlDao.updHcbPedlFailCountAndStatus("hcb_pedl"+tabnum,corpno,trandt,acct,status,E_DETLSS.QTZ);
		} else if(status == E_DETLSS.QTY){//余额更细成功，状态改为余额已处理
			HcQuerySqlDao.updHcbPedlStatus("hcb_pedl"+tabnum,corpno, trandt, status, E_DETLSS.QTZ,null,null);
		}
	}

}
