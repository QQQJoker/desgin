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
import cn.sunline.ltts.busi.dp.servicetype.IoDpAcctSvcType;
import cn.sunline.ltts.busi.hc.namedsql.HcQuerySqlDao;
import cn.sunline.ltts.busi.hc.tables.HcbFailDetl.HcbFldl;
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

public class HcbFldlToDetailProcesser extends LttsTimerProcessor{
	
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(HcbFldlToDetailProcesser.class);

	@Override
	public void process(String arg0, DataArea paramData) {
		BIZLOG.info("======HcbFldlToDetailProcesser其他异常处理开始======");
		String tabnum=HotCtrlUtil.getTabnum(paramData);//分表号
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		boolean hasData = false;//是否有待处理数据
		String corpno = runEnvs.getCorpno();
		String trandt = HotCtrlUtil.getHotCtrlDate();
		KnpPara knpPara1 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "TIME",true);
		KnpPara knpPara2 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "SELECT_LIMIT",true);
		KnpPara knpPara3 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "UPDATE_LIMIT",true);
		int processTime = Integer.parseInt(knpPara1.getPmval1());//秒,运行时间
		int waitTime = Integer.parseInt(knpPara1.getPmval3());//秒，等待时间
		int failCunt=Integer.parseInt(knpPara2.getPmval4());//失败次数
		int limiCunt=Integer.parseInt(knpPara2.getPmval2());//查询条数
		int updateCunt=Integer.parseInt(knpPara3.getPmval1());//更改限制条数需要比查询条数多
		long startTime = System.currentTimeMillis();
		do {	
			updHcbFldlStatus(tabnum,corpno,trandt,limiCunt,failCunt,updateCunt);
			//DaoUtil.beginTransaction();
			//处理失败次数达到上限的数据
			//processFailTooMany(tabnum,failCunt,updateCunt);
			//余额已处理数据 更新为 其他处理中
			//HcQuerySqlDao.UpdHcbFldlStatus("hcb_fldl"+tabnum,E_DETLSS.QTZ, E_DETLSS.YCL, trandt, corpno,limiCunt);
			//BIZLOG.info("HcbFldlToDetailProcesser待处理数据[%s]条", updCount);
			//DaoUtil.commitTransaction();
			//TODO判断条件更换为个数
			//if(updCount > 0) {		
			//其他处理中 数据
			List<HcbFldl> hcbFldlList = HcQuerySqlDao.SelHcbFldlListByStatus("hcb_fldl"+tabnum,E_DETLSS.QTZ, trandt, corpno, false);
			if(CommUtil.isNotNull(hcbFldlList) && hcbFldlList.size() > 0) {
				BIZLOG.info("HcbFldlToDetailProcesser其他处理中数据[%s]条", hcbFldlList.size());
				//余额已处理 数据 生成账单或流水
				processBalList(hcbFldlList,tabnum);
				hasData = true;
			} else {
				hasData = false;
			}
			if(!hasData) {
				// 如果无数据,则等待10秒
				try {
					Thread.sleep(waitTime*1000);
				} catch (InterruptedException e) {
					BIZLOG.error("[%s]", e.toString());
				}
			}
			long endTime = System.currentTimeMillis();
			if((endTime - startTime)/1000 > processTime) {
				break;
			}
			
		} while (true);
		BIZLOG.info("======HcbFldlToDetailProcesser其他异常处理结束======");
	}

	//独立事务
	public void updHcbFldlStatus(final String tabnum,final String corpno, final String trandt,final int limiCunt,final int failCount,final int updateCunt) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				//1、处理失败次数达到上限的数据	
				BIZLOG.debug("==================处理失败次数达到上限的数据==================");
				HcQuerySqlDao.UpdHcbFldlSsToFail("hcb_fldl"+tabnum,CommTools.prcRunEnvs().getCorpno(),failCount,E_DETLSS.MXSB,E_DETLSS.YCL,updateCunt);
				//2、登记占用明细表
				BIZLOG.debug("==================更新状态==================");
				HcQuerySqlDao.UpdHcbFldlStatus("hcb_fldl"+tabnum,E_DETLSS.QTZ, E_DETLSS.YCL, trandt, corpno,limiCunt);
				return null;
			}
		});
	}
	
	//独立事务
	public void processAcct(final String acct,final List<HcbFldl> balDetailList) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				processAcctDp(acct,balDetailList);
				//processAcsqDp(acct,balDetailList);
				return null;
			}
		});
		processAcsqDp(acct,balDetailList);
	}
	/**
	 * 失败明细表处理失败次数达到上限，更改状态
	 * 
	 */
	/*private void processFailTooMany(String tabnum,int failCunt,int updateCunt) {
		BIZLOG.info("======处理失败次数达到上限的数据开始======");
		//处理失败次数达到上限的数据
		//KnpPara knpPara = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "FAIL_COUNT",true);
		//int count = Integer.parseInt(knpPara.getPmval1());//最大失败次数	
		HcQuerySqlDao.UpdHcbFldlSsToFail("hcb_fldl"+tabnum,CommTools.prcRunEnvs().getCorpno(),failCunt,E_DETLSS.MXSB,E_DETLSS.YCL,updateCunt);
		BIZLOG.info("======处理失败次数达到上限的数据结束======");
	}*/

	/**
	 * 余额明细生成账单或流水
	 * 
	 */
	/*private void processBalList(List<HcbFldl> hcbFldlList,String tabnum) {
		E_HCTYPE[] values = E_HCTYPE.values();
		for (int i = 0; i < values.length; i++) {
			if(values[i] == E_HCTYPE.CL) {//额度
				processCL(hcbFldlList,tabnum);
			} else if(values[i] == E_HCTYPE.DP) {//账户 TODO 可再细分为结算户／内部户等
				
			}
		}
	}*/

	/**
	 * 余额明细生成账单或流水
	 * 按主体账号 分别处理账单 
	 * @author jiangyaming
	 * 2018年4月12日 下午2:58:04
	 */
	private void processBalList(List<HcbFldl> balDetailList,String tabnum) {
		HashSet<String> hcAcctListCL = new HashSet<>();
		HashSet<String> hcAcctListDP = new HashSet<>();
		HashSet<String> hcAcctListIN = new HashSet<>();
		for(HcbFldl entity:balDetailList) {
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
				processAcct(acct,balDetailList);
				//DaoUtil.beginTransaction();
				//processAcctDp(acct,balDetailList);
				//processAcsqDp(acct,balDetailList);
				//DaoUtil.commitTransaction();
				
			} catch (Exception e) {
				BIZLOG.error("[%s]", "生成余额账单 并生产流水 失败"+e);
				updateBalDetailStatus(acct,E_DETLSS.YCL,tabnum);
				continue;
			}
			updateBalDetailStatus(acct,E_DETLSS.QTY,tabnum);
		}
	}

	/**
	 * 按主体账号 分别处理账单
	 * 
	 */
	/*private void processCL(List<HcbFldl> hcbFldlList,String tabnum) {
		BIZLOG.info("======按主体账号 分别处理账单开始======");
		HashSet<String> hcAcctList = new HashSet<>();
		for(HcbFldl entity:hcbFldlList) {
			hcAcctList.add(entity.getHcmain());
		}
		BIZLOG.info("======hcAcctList:[%s]======",hcAcctList);
		Iterator<String> iterator = hcAcctList.iterator();
		while (iterator.hasNext()) {
			String acct = iterator.next();
			processAcct(acct,hcbFldlList,tabnum);
		}
		BIZLOG.info("======按主体账号 分别处理账单结束======");
	}
*/
	/**
	 * 处理单个热点账户 生成连续账单或余额列表
	 * 
	 */
	/*private void processAcct(String acct, List<HcbFldl> hcbFldlList,String tabnum) {
		BIZLOG.info("======处理单个热点账户 生成连续账单或余额列表开始[%s]======",acct);
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		String corpno = runEnvs.getCorpno();
		//String trandt = HotCtrlUtil.getHotCtrlDate();
		//查询余额
		HcbBlceDetl hcbBlceDetl = HcQuerySqlDao.selHcbBlceDetl(corpno,acct,false);
		//查询余额表
		HcbBlce hcbBlce = HcbBlceDao.selectOne_odb1(acct, true);
		boolean isInit = false;
		BigDecimal remain = BigDecimal.ZERO;
		if(CommUtil.isNotNull(hcbBlceDetl)) {
			BIZLOG.info("最近一条明细[%s]", hcbBlceDetl);
			remain = hcbBlceDetl.getRemain();
		} else {
			remain = hcbBlce.getBalnce();
			isInit = true;
		}
		//BigDecimal remain = hcbBlceDetl.getRemain();
		List<HcbBlceDetl> blceList = new ArrayList<HcbBlceDetl>();
		for(HcbFldl hcbFldl:hcbFldlList) {
			if(CommUtil.compare(hcbFldl.getHcmain(),acct) == 0) {//加工成账单
				HcbBlceDetl detail = SysUtil.getInstance(HcbBlceDetl.class);
				detail.setAmntcd(hcbFldl.getAmntcd());
				detail.setCorpno(hcbFldl.getCorpno());
				detail.setHcacct(acct);
				detail.setDetlss(E_DETLSS.WCL);
				detail.setTranam(hcbFldl.getTranam());
				detail.setTrandt(hcbFldl.getTrandt());
				detail.setTransq(hcbFldl.getTransq());
				if(!isInit) {
					if(CommUtil.compare(hcbBlceDetl.getAmntcd(), detail.getAmntcd())== 0) {
						remain = remain.add(hcbFldl.getTranam());
					} else {
						remain = remain.subtract(hcbFldl.getTranam());
					}		
				}
				detail.setRemain(remain);
				blceList.add(detail);
			}
		}
		if(blceList.size() > 0) {
			BIZLOG.info("======处理单个热点账户 生成连续账单或余额列表[%s]======",blceList);
			try {
				DaoUtil.insertBatch(HcbBlceDetl.class, blceList);
				//更新待处理表状态 其他处理中->其他已处理
//				HcQuerySqlDao.updHcbPedlStatus(corpno, trandt, E_DETLSS.QTY, E_DETLSS.QTZ);
				updateBalDetailStatus(acct,E_DETLSS.QTY,tabnum);
				BIZLOG.info("======处理单个热点账户 生成连续账单或余额列表成功qty======");
			} catch (Exception e) {
				BIZLOG.error("[%s]", "生成余额账单失败");
				BIZLOG.error("[%s]", e.toString());
				//更新待处理表状态 其他处理中->余额已处理
//				HcQuerySqlDao.updHcbPedlStatus(corpno, trandt, E_DETLSS.YCL, E_DETLSS.QTZ);
				updateBalDetailStatus(acct,E_DETLSS.YCL,tabnum);
				BIZLOG.info("======处理单个热点账户 生成连续账单或余额列表失败ycl======");
			}
			
		}
		BIZLOG.info("======处理单个热点账户 生成连续账单或余额列表结束[%s]",acct);
	}*/

	private void updateBalDetailStatus(String acct, E_DETLSS status,String tabnum) {
		BIZLOG.info("======updateBalDetailStatus--[%s]开始======",acct);
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		String corpno = runEnvs.getCorpno();
		String trandt =HotCtrlUtil.getHotCtrlDate();
		if(status == E_DETLSS.YCL) {//余额更新失败，次数++，状态改为已处理	
			HcQuerySqlDao.UpdHcbFldlCount("hcb_fldl"+tabnum,E_DETLSS.YCL, E_DETLSS.QTZ, trandt, corpno);
			BIZLOG.info("======余额表更新失败======");
		} else if(status == E_DETLSS.QTY){//余额更细成功，状态改为余额已处理
			HcQuerySqlDao.UpdHcbFldlStatus("hcb_fldl"+tabnum,status,E_DETLSS.QTZ, trandt, corpno,null);
			BIZLOG.info("======余额表更新成功======");
		}
		BIZLOG.info("======updateBalDetailStatus--[%s]结束======",acct);
	}
	
	
	/**
	 * 登记会计流水
	 * @author jiangyaming
	 * 2018年4月24日 上午11:26:55
	 */
	private void processAcsqDp(String acctno, List<HcbFldl> balDetailList) {
		List<IoAccounttingIntf> list = new ArrayList<>();
		for(HcbFldl hcbFldl:balDetailList) {
			if(CommUtil.compare(hcbFldl.getHcmain(),acctno) == 0) {
				//登记会计流水
		        IoAccounttingIntf cplIoAccounttingIntf = SysUtil
		                .getInstance(IoAccounttingIntf.class);
		        cplIoAccounttingIntf.setCorpno(hcbFldl.getCorpno());
		        cplIoAccounttingIntf.setAcctno(acctno);
		        cplIoAccounttingIntf.setTrandt(hcbFldl.getTrandt());
		        cplIoAccounttingIntf.setTransq(hcbFldl.getTransq());
		        cplIoAccounttingIntf.setTranam(hcbFldl.getTranam());
		        cplIoAccounttingIntf.setAcctdt(hcbFldl.getTrandt());
		        cplIoAccounttingIntf.setAmntcd(hcbFldl.getAmntcd());
//		        cplIoAccounttingIntf.setAcctbr();
//		        cplIoAccounttingIntf.setDtitcd();
//		        cplIoAccounttingIntf.setCrcycd();
		        if(CommUtil.compare(hcbFldl.getAmntcd(),E_AMNTCD.CR) == 0) {
		        	cplIoAccounttingIntf.setAmntcd(E_AMNTCD.CR);
				} else if(CommUtil.compare(hcbFldl.getAmntcd(),E_AMNTCD.DR) == 0) {
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
	private void processAcctDp(String acctno, List<HcbFldl> balDetailList) {
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
		for(HcbFldl hcbFldl:balDetailList) {
			if(CommUtil.compare(hcbFldl.getHcmain(),acctno) == 0) {//加工成账单
				IoDpKnlBill knlBill = SysUtil.getInstance(IoDpKnlBill.class);
				if(CommUtil.compare(hcbFldl.getAmntcd(),E_AMNTCD.CR) == 0) {
					acctbl = acctbl.add(hcbFldl.getTranam());
				} else if(CommUtil.compare(hcbFldl.getAmntcd(),E_AMNTCD.DR) == 0) {
					acctbl = acctbl.subtract(hcbFldl.getTranam());
				}
				knlBill.setAcctbl(acctbl);
				knlBill.setAcctno(acctno);
				knlBill.setAmntcd(hcbFldl.getAmntcd());
				knlBill.setTrandt(hcbFldl.getTrandt());
				knlBill.setTransq(hcbFldl.getTransq());
				knlBill.setTranam(hcbFldl.getTranam());
				knlBill.setCorpno(hcbFldl.getCorpno());
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
	private void processAcct(String acct, List<HcbFldl> balDetailList,String tabnum) {
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
		for(HcbFldl hcbFldl:balDetailList) {
			if(CommUtil.compare(hcbFldl.getHcmain(),acct) == 0) {//加工成账单
				HcbBlceDetl detail = SysUtil.getInstance(HcbBlceDetl.class);
				detail.setCorpno(hcbFldl.getCorpno());
				detail.setHcacct(acct);
				detail.setDetlss(E_DETLSS.WCL);
				detail.setTranam(hcbFldl.getTranam());
				detail.setTrandt(hcbFldl.getTrandt());
				detail.setTransq(hcbFldl.getTransq());
				detail.setAmntcd(hcbFldl.getAmntcd());
				if(!isInit) {
					if(CommUtil.compare(hcpDefn.getBlncdr().toString(), detail.getAmntcd().toString()) == 0) {
						remain = remain.add(hcbFldl.getTranam());
					} else {
						remain = remain.subtract(hcbFldl.getTranam());
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


}
