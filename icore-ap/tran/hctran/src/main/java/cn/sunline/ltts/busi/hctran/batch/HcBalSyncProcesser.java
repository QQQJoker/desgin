package cn.sunline.ltts.busi.hctran.batch;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.bsap.util.DaoSplitInvokeUtil;
import cn.sunline.ltts.busi.dp.servicetype.IoDpAcctSvcType;
import cn.sunline.ltts.busi.hc.namedsql.HcQuerySqlDao;
import cn.sunline.ltts.busi.hc.tables.HcbFailDetl.HcbFldl;
import cn.sunline.ltts.busi.hc.tables.HcbPendDeal.HcbPedl;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbBlce;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbBlceDao;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefnDao;
import cn.sunline.ltts.busi.hc.util.HotCtrlUtil;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
import cn.sunline.ltts.busi.iobus.type.HcTimerComplex.HcBalSum;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnaAcct;
import cn.sunline.ltts.busi.sys.errors.HcError;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLNCDR;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DETLSS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HCTYPE;

/**
 * 余额同步定时任务
 * 
 * @author jiangyaming 2018年4月9日 上午11:19:01
 */
public class HcBalSyncProcesser extends LttsTimerProcessor {
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(HcBalSyncProcesser.class);
	
	@Override
	public void beforeBizEnv(DataArea dataArea) {
		super.beforeBizEnv(dataArea);
	}

	@Override
	public void process(String arg0, DataArea paramData) {
		BIZLOG.info("HcBalSyncProcesser begin process");
		String tabnum=HotCtrlUtil.getTabnum(paramData);//分表号
		BIZLOG.info("======获取分表号[%s]======", tabnum);
		boolean hasData = false;//是否有待处理数据
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
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		String corpno = runEnvs.getCorpno();
		String trandt = HotCtrlUtil.getHotCtrlDate();
		do {
			//String lastdt = runEnvs.getLstrdt();
			//DaoUtil.beginTransaction();
			//updateBalDayendCL(trandt,lastdt,corpno);
			//处理失败次数达到上限的数据
			//processFailTooMany(tabnum,failCunt,updateCunt);
			//int updCount = 0;
			try {
			/*	List<HcbPedl> hcbPedlList=HcQuerySqlDao.selBalDetailList("hcb_pedl"+tabnum, trandt, corpno, E_DETLSS.WCL,null,limiCunt, false);
				//List<HcbPedl> list=(List<HcbPedl>) DaoSplitInvokeUtil.selectAll(HcbPedl.class, "selectAll_odb5", tabnum, trandt,E_DETLSS.WCL,false);
				if(CommUtil.isNotNull(hcbPedlList)){
					for(HcbPedl hcbpedl:hcbPedlList){
						//获取待处理数据 并处理数据
						hcbpedl.setDetlss(E_DETLSS.CLZ);
						DaoSplitInvokeUtil.update(hcbpedl, "updateOne_odb1", tabnum);
					}
					updCount=hcbPedlList.size();
				}*/	
				//HcQuerySqlDao.updHcbPedlStatus("hcb_pedl"+tabnum,corpno, trandt, E_DETLSS.CLZ, E_DETLSS.WCL,limiCunt);
				updHcbPedlStatus(tabnum,corpno, trandt,limiCunt,failCunt,updateCunt);
			} catch (Exception e) {
				BIZLOG.error("更新状态为处理中失败", e);
			}
			//BIZLOG.info("待处理数据[%s]条", updCount);
			//DaoUtil.commitTransaction();
			//汇总余额处理中数据
			List<HcBalSum> balSumList = HcQuerySqlDao.selSumDoingBalList("hcb_pedl"+tabnum,trandt, corpno, false);
			if(CommUtil.isNotNull(balSumList) && balSumList.size() > 0) {
				BIZLOG.info("<<<处理中数据[%s]条>>>", balSumList.size());
				//处理汇总余额 更新到余额表；以及处理失败次数过多数据
				processBal(balSumList,tabnum);
				hasData = true;
			} else {
				hasData = false;
			}
			if(!hasData) {
				// 如果无数据,则等待waitTime
				try {
					BIZLOG.info("HcBalSyncProcesser.process begin waiting：" + waitTime + "秒");
					Thread.sleep(waitTime*1000);
				} catch (InterruptedException e) {
					BIZLOG.error("[%s]", e.toString());
				}
			}
			long endTime = System.currentTimeMillis();
			BIZLOG.info("HcBalSyncProcesser.process time：" + processTime + "秒");
			if((endTime - startTime)/1000 > processTime) {
				break;
			}
		} while (true);
		BIZLOG.info("HcBalSyncProcesser end process");
	}

	
	//独立事务
	public void updHcbPedlStatus(final String tabnum,final String corpno, final String trandt,final int limiCunt,final int failCunt,final int updateCunt) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				//1、处理失败次数达到上限的数据
				BIZLOG.debug("==================处理失败次数达到上限的数据==================");
				processFailTooMany(tabnum,failCunt,updateCunt);
				//2、登记占用明细表
				BIZLOG.debug("==================更新状态==================");
				 // 分布式
		        if (CommTools.isDistributedSystem()) {		    
		        	   if (DcnUtil.isAdminDcn(DcnUtil.getCurrDCN())) { // 管理节点
		        		   HcQuerySqlDao.updHcbPedlStatus("hcb_pedl"+tabnum,corpno, trandt, E_DETLSS.CLZ, E_DETLSS.WCL,limiCunt,null);
		        	   }else{//零售节点
		        		   HcQuerySqlDao.updHcbPedlStatus("hcb_pedl"+tabnum,corpno, trandt, E_DETLSS.CLZ, E_DETLSS.WCL,limiCunt,E_HCTYPE.CL);
		        	   }     	
		        }else{
		        	//集中式
		        	HcQuerySqlDao.updHcbPedlStatus("hcb_pedl"+tabnum,corpno, trandt, E_DETLSS.CLZ, E_DETLSS.WCL,limiCunt,null);
		        }
				
				return null;
			}
		});
	}
	/**
	 * 额度余额过账
	 * @author jiangyaming
	 * 2018年4月13日 上午11:46:22
	 */
	/*private void updateBalDayendCL(String trandt,String lastdt, String corpno) {
		BIZLOG.info("updateBalDayend begin");
		List<HcbBlce> hcbBlceList = HcQuerySqlDao.selHcbBlceList(corpno, trandt, false);
		List<HcbBlce> lastHcbBlceList = HcQuerySqlDao.selHcbBlceList(corpno, lastdt, false);
		if(CommUtil.isNull(hcbBlceList) || hcbBlceList.size() == 0) {//已切日
			if(CommUtil.isNotNull(lastHcbBlceList)) {
				for(HcbBlce hcbBlce : lastHcbBlceList) {
					//hcbBlce.setLastdt(hcbBlce.getTrandt());
					//hcbBlce.setTrandt(trandt);
					//hcbBlce.setLsblce(hcbBlce.getBalnce());
//					hcbBlce.setBalnce(hcbBlce.getLsblce());
				}
				DaoUtil.insertBatch(HcbBlce.class, lastHcbBlceList);
			} else {//当日上日均无余额 则为初始化状态，插入余额信息
				HcQuerySqlDao.initHcbBlceCL(corpno, trandt, lastdt);
			}
		}
		BIZLOG.info("updateBalDayend end");
	}*/

	/**
	 * 待处理表处理失败次数达到上限，转存到失败表
	 * @author jiangyaming
	 * 2018年4月12日 上午10:56:02
	 */
	private void processFailTooMany(String tabnum,int failCunt,int limiCunt) {
		BIZLOG.info("processFailTooMany begin");
		//处理失败次数达到上限的数据
		//RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		//String corpno = runEnvs.getCorpno();
		String trandt = HotCtrlUtil.getHotCtrlDate();
		//int count = Integer.parseInt(knpPara.getPmval1());//失败次数
		//查出待处理明细表相关数据转到失败表
		List<HcbPedl> hcbPedlList=HcQuerySqlDao.selBalDetailList("hcb_pedl"+tabnum, trandt, CommTools.prcRunEnvs().getCorpno(), E_DETLSS.WCL,failCunt,limiCunt, false);
		//List<HcbPedl> hcbPedlList=(List<HcbPedl>) DaoSplitInvokeUtil.selectAll(HcbPedl.class, "selectAll_odb4", tabnum, trandt,failCunt,E_DETLSS.WCL,false);
		if(CommUtil.isNotNull(hcbPedlList)){
			for(HcbPedl hcbPedl:hcbPedlList){	
				HcbFldl hcbFldl=SysUtil.getInstance(HcbFldl.class);
				hcbFldl.setAmntcd(hcbPedl.getAmntcd());
				hcbFldl.setCdcnno(hcbPedl.getCdcnno());
				hcbFldl.setCorpno(hcbPedl.getCorpno());
				hcbFldl.setDealtm(0);
				hcbFldl.setDetlsq(hcbPedl.getDetlsq());
				hcbFldl.setDetlss(E_DETLSS.WCL);
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
		//转存到失败表
		//HcQuerySqlDao.transferFailTooMany("hcb_fldl"+tabnum,"hcb_pedl"+tabnum,CommTools.prcRunEnvs().getCorpno(),trandt,failCunt,E_DETLSS.WCL);
		//删除待处理表数据
		//HcQuerySqlDao.delFailTooMany("hcb_pedl"+tabnum,CommTools.prcRunEnvs().getCorpno(),trandt,failCunt,E_DETLSS.WCL);
		BIZLOG.info("processFailTooMany end");
	}

	/**
	 * 循环处理额度余额汇总(额度类型)
	 * @author jiangyaming
	 * 2018年4月10日 下午5:35:45
	 */
	/*private void processBal(List<HcBalSum> balSumList,String tabnum) {
		BIZLOG.info("processBal [%s]",balSumList);
		updateBal(tabnum,balSumList);
		for(HcBalSum balSum : balSumList) {
			String hcMain = balSum.getHcmain();
			BigDecimal tranam = balSum.getTranam();
			DaoUtil.beginTransaction();
			if(CommUtil.compare(tranam, BigDecimal.ZERO) == 0) {//余额不变
				BIZLOG.info("账号：[%s] 轧差余额为0",hcMain);
				updateBalDetailStatus(tabnum,hcMain,E_DETLSS.YCL);//更新待处理余额表状态
				continue;
			} else {
				updateBal(hcMain,tranam,balSum.getHctype(),tabnum);//更新余额 
			}
			DaoUtil.commitTransaction();	
	}*/	

	//独立事务
	public void processBal(final List<HcBalSum> balSumList, final String tabnum) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {			
				for(HcBalSum balSum : balSumList) {
					String hcMain = balSum.getHcmain();
					BigDecimal tranam = balSum.getTranam();
					if(CommUtil.compare(tranam, BigDecimal.ZERO) == 0) {//余额不变
						BIZLOG.info("账号：[%s] 轧差余额为0",hcMain);
						updateBalDetailStatus(tabnum,hcMain,E_DETLSS.YCL);//更新待处理余额表状态
						continue;
					} else {
						updateBal(hcMain,tranam,balSum.getHctype(),tabnum);//更新余额 
					}				
				}				
				return null;
			}
		});
	}
	
	/**
	 * 更新待处理明细状态  成功：处理中->余额已处理，失败：更新失败次数++ 或者 次数达到上线 处理中->未处理
	 * @author jiangyaming
	 * 2018年4月11日 下午5:20:34
	 */
	private void updateBalDetailStatus(String tabnum,String hcMain,E_DETLSS status) {
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		String corpno = runEnvs.getCorpno();
		String trandt = HotCtrlUtil.getHotCtrlDate();
		BIZLOG.info("更新待处理明细状态 账号：[%s] ，状态：[%s]",hcMain,status);
		if(status == E_DETLSS.WCL) {//余额更新失败，次数++，状态改为未处理
			HcQuerySqlDao.updHcbPedlFailCountAndStatus("hcb_pedl"+tabnum,corpno,trandt,hcMain,status,E_DETLSS.CLZ);
		} else if(status == E_DETLSS.YCL){//余额更细成功，状态改为余额已处理
	        HcQuerySqlDao.updHcbPedlStatus("hcb_pedl"+tabnum,corpno, trandt, status, E_DETLSS.CLZ,null,null);	
	        }
	}

	/**
	 * 更新余额表
	 * @author jiangyaming
	 * 2018年4月12日 上午11:19:19
	 */
	private void updateBal(String hcMain,BigDecimal tranam,E_HCTYPE hcType,String tabnum) {
		BIZLOG.debug("updateBal() hcMain:[%s],tranam:[%s],hcType:[%s]", hcMain,tranam,hcType);
		if(CommUtil.compare(hcType,E_HCTYPE.CL) == 0) {//额度
			processCL(hcMain,tranam,tabnum);
		} else if(CommUtil.compare(hcType,E_HCTYPE.DP) == 0) {//存款
			processDP(hcMain,tranam,tabnum);
		} else if(CommUtil.compare(hcType,E_HCTYPE.IN) == 0) {//内部户
			//TODO
		}
	}
	
	//活期户kna_acct热点余额处理
	private void processDP(String hcMain, BigDecimal tranam,String tabnum) {
		IoDpSrvQryTableInfo qryTableInfo = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
		IoDpKnaAcct knaAcct = qryTableInfo.getKnaAcctByOdb1(hcMain, true);//改为只从数据库中获取负债账户信息  jizhirong 20180518
		String trandt = HotCtrlUtil.getHotCtrlDate();
		if(CommUtil.compare(knaAcct.getUpbldt(),trandt) < 0) {
			knaAcct.setLastbl(knaAcct.getOnlnbl());
			//更新积数
			updateAcin(knaAcct.getAcctno(),knaAcct.getOnlnbl(),trandt);
		}
		if(CommUtil.isNull(knaAcct.getUpbldt())){
			knaAcct.setLstrdt(trandt);
		}
		// 如果最后一笔交易时间小于等于交易日期，需要更新最后交易日期和流水
		if (CommUtil.compare(knaAcct.getLstrdt(), trandt) <= 0) {
			knaAcct.setLstrdt(trandt);
			knaAcct.setLstrsq(CommToolsAplt.prcRunEnvs().getMntrsq());
		}
		knaAcct.setUpbldt(trandt);
		BigDecimal onlnbl = knaAcct.getOnlnbl();
		BIZLOG.info("负债账号[%s]更新前余额[%s]", hcMain, tranam);
		onlnbl = onlnbl.subtract(tranam);//tranam 正数则为借方发生额  负数则为贷方发生额
		BIZLOG.info("负债账号[%s],发生额[%s],更新后余额[%s]", hcMain, tranam, tranam);
		knaAcct.setOnlnbl(onlnbl);
		BIZLOG.info("更新负债账号余额：[%s]",knaAcct);
		try {
			qryTableInfo.updateKnaAcctOdb1(knaAcct);//更新余额
		} catch (Exception e) {//余额更新失败
			updateBalDetailStatus(tabnum,hcMain,E_DETLSS.WCL);
			return;
		}
		updateBalDetailStatus(tabnum,hcMain,E_DETLSS.YCL);//更新待处理余额表状态
	}

	private void updateAcin(String acctno,BigDecimal onlnbl,String acctdt) {
		IoDpAcctSvcType svcType = SysUtil.getInstance(IoDpAcctSvcType.class);
		svcType.updateKnaAcin(acctno, onlnbl, acctdt);
	}

	//贷款额度余额处理
	private void processCL(String hcMain,BigDecimal tranam,String tabnum) {
		//String trandt = HotCtrlUtil.getHotCtrlDate();
		HcbBlce entity = HcbBlceDao.selectOne_odb1(hcMain, false);
		HcpDefn hcpDefn=HcpDefnDao.selectOne_odb1(hcMain, true);
		BigDecimal bal = BigDecimal.ZERO;
		if(CommUtil.isNotNull(entity)) {
			bal = entity.getBalnce();
		}else{
			throw HcError.HcGen.E0000("该热点主体余额记录不存在");
		}
		BIZLOG.info("更新余额表,余额[%s],账号[%s],发生额[%s]", bal, hcMain, tranam);
		if(hcpDefn.getBlncdr() == E_BLNCDR.DR){
			bal = bal.add(tranam);
		} else if(hcpDefn.getBlncdr() == E_BLNCDR.CR) {
			bal = bal.subtract(tranam);
		}
		entity.setBalnce(bal);
		BIZLOG.info("更新额度余额：[%s]",entity);
		try {
			HcbBlceDao.updateOne_odb1(entity);//更新余额
		} catch (Exception e) {//余额更新失败
			updateBalDetailStatus(tabnum,hcMain,E_DETLSS.WCL);
			return;
		}
		updateBalDetailStatus(tabnum,hcMain,E_DETLSS.YCL);//更新待处理余额表状态
	}

	@Override
	public void afterBizEnv(DataArea dataArea) {
		super.afterBizEnv(dataArea);
	}

}
