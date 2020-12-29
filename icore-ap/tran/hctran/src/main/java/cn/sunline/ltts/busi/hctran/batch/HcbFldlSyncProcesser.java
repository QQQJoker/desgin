package cn.sunline.ltts.busi.hctran.batch;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.dp.servicetype.IoDpAcctSvcType;
import cn.sunline.ltts.busi.hc.namedsql.HcQuerySqlDao;
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

public class HcbFldlSyncProcesser extends LttsTimerProcessor {
	
	private static BizLog log = LogManager.getBizLog(HcbFldlSyncProcesser.class);
	@Override
	public void process(String param, DataArea paramData) {
		    log.info("======HcbFldlSyncProcesser异常处理开始======");
			String tabnum=HotCtrlUtil.getTabnum(paramData);//分表号
			boolean hasData = false;//是否有待处理数据
			KnpPara knpPara1 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "TIME",true);
			KnpPara knpPara2 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "SELECT_LIMIT",true);
			KnpPara knpPara3 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "UPDATE_LIMIT",true);
			//KnpPara knpPara3 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "FAIL_COUNT",true);
			int processTime = Integer.parseInt(knpPara1.getPmval1());//秒,运行时间
			int waitTime = Integer.parseInt(knpPara1.getPmval3());//秒，等待时间
			int failCunt=Integer.parseInt(knpPara2.getPmval4());//失败次数
			int limiCunt=Integer.parseInt(knpPara2.getPmval2());//查询条数
			int updateCunt=Integer.parseInt(knpPara3.getPmval1());//更改限制条数需要比查询条数多
			long startTime = System.currentTimeMillis();
			RunEnvsComm runEnvs = CommTools.prcRunEnvs();
			String corpno = runEnvs.getCorpno();
			String trandt = HotCtrlUtil.getHotCtrlDate();
			do {
				updHcbFldlStatus(tabnum,corpno,trandt,limiCunt,failCunt,updateCunt);
				//DaoUtil.beginTransaction();
				//updateBalDayend(trandt,corpno);
				//处理失败次数达到上限的数据				
				//processFailTooMany(tabnum,failCunt,updateCunt);
				//获取待处理数据 并处理数据
				//HcQuerySqlDao.UpdHcbFldlStatus("hcb_fldl"+tabnum,E_DETLSS.CLZ, E_DETLSS.WCL, trandt, corpno,limiCunt);
				//log.info("======获取待处理数据[%s]条======", updCount);
				//DaoUtil.commitTransaction();
				//if(updCount > 0) {
				//汇总余额处理中数据		
		        List<HcBalSum> balSumList = HcQuerySqlDao.SelHcbFldlSum("hcb_fldl"+tabnum,E_DETLSS.CLZ, trandt, corpno,false);
				if(CommUtil.isNotNull(balSumList) && balSumList.size() > 0) {
					log.info("======获取处理中数据[%s]条======", balSumList.size());
				//处理汇总余额 更新到余额表；以及处理失败次数过多数据
					//processBalList(balSumList,tabnum);
					processBal(balSumList,tabnum);
					hasData = true;
				} else {
					hasData = false;
				}
				if(!hasData) {
					// 如果无数据,则等待10秒
					try {
						Thread.sleep(waitTime*1000);
					} catch (InterruptedException e) {
						log.error("[%s]", e.toString());
					}
				}
				long endTime = System.currentTimeMillis();
				if((endTime - startTime)/1000 > processTime) {
					break;
				}
			} while (true);
			log.info("======HcbFldlSyncProcesser异常处理结束======");
		}

		//独立事务
		public void updHcbFldlStatus(final String tabnum,final String corpno, final String trandt,final int limiCunt,final int failCount,final int updateCunt) {
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				public Void execute() {
					//1、处理失败次数达到上限的数据	
					log.debug("==================处理失败次数达到上限的数据==================");
					HcQuerySqlDao.UpdHcbFldlSsToFail("hcb_fldl"+tabnum,CommTools.prcRunEnvs().getCorpno(),failCount,E_DETLSS.YESB,E_DETLSS.WCL,updateCunt);
					//2、登记占用明细表
					log.debug("==================更新状态==================");
					HcQuerySqlDao.UpdHcbFldlStatus("hcb_fldl"+tabnum,E_DETLSS.CLZ, E_DETLSS.WCL, trandt, corpno,limiCunt);
					return null;
				}
			});
		}
		
		//独立事务
		public void processBal(final List<HcBalSum> balSumList, final String tabnum) {
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				public Void execute() {
					for(HcBalSum balSum : balSumList) {
						String hcMain = balSum.getHcmain();
						BigDecimal tranam = balSum.getTranam();
						if(CommUtil.compare(tranam, BigDecimal.ZERO) == 0) {//余额不变
							log.info("账号：[%s] 轧差余额为0",hcMain);
							updateBalDetailStatus(hcMain,E_DETLSS.YCL,tabnum);//更新待处理余额表状态
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
		 * 循环处理额度余额汇总(额度类型)
		 * @author jiangyaming
		 * 2018年4月10日 下午5:35:45
		 */
		/*private void processBal(List<HcBalSum> balSumList,String tabnum) {
			log.info("processBal [%s]",balSumList);
			for(HcBalSum balSum : balSumList) {
				String hcMain = balSum.getHcmain();
				BigDecimal tranam = balSum.getTranam();
				DaoUtil.beginTransaction();
				if(CommUtil.compare(tranam, BigDecimal.ZERO) == 0) {//余额不变
					log.info("账号：[%s] 轧差余额为0",hcMain);
					updateBalDetailStatus(hcMain,E_DETLSS.YCL,tabnum);//更新待处理余额表状态
					continue;
				} else {
					updateBal(hcMain,tranam,balSum.getHctype(),tabnum);//更新余额 
				}
				DaoUtil.commitTransaction();	
		}*/
		/**
		 * 更新余额表
		 * @author jiangyaming
		 * 2018年4月12日 上午11:19:19
		 */
		private void updateBal(String hcMain,BigDecimal tranam,E_HCTYPE hcType,String tabnum) {
			log.debug("updateBal() hcMain:[%s],tranam:[%s],hcType:[%s]", hcMain,tranam,hcType);
			if(CommUtil.compare(hcType,E_HCTYPE.CL) == 0) {//额度
				processCL(hcMain,tranam,tabnum);
			} else if(CommUtil.compare(hcType,E_HCTYPE.DP) == 0) {//存款
				processDP(hcMain,tranam,tabnum);
			} else if(CommUtil.compare(hcType,E_HCTYPE.IN) == 0) {//内部户
				//TODO
			}
		}
	
	
		/**
		 * 失败明细表失败次数达到上限改状态为失败
		 *
		 */
		/*private void processFailTooMany(String tabnum,int failCount,int updateCunt) {
			log.info("======处理失败次数达到上限的数据开始======");
			//处理失败次数达到上限的数据
			//KnpPara knpPara = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "FAIL_COUNT",true);
			//int count = Integer.parseInt(knpPara.getPmval1());//失败次数		
			HcQuerySqlDao.UpdHcbFldlSsToFail("hcb_fldl"+tabnum,CommTools.prcRunEnvs().getCorpno(),failCount,E_DETLSS.YESB,E_DETLSS.WCL,updateCunt);
			log.info("======处理失败次数达到上限的数据结束======");
		}*/

		//活期户kna_acct热点余额处理
		/*private void processDP(String hcMain, BigDecimal tranam,String tabnum) {
			IoDpSrvQryTableInfo qryTableInfo = SysUtil.getInstance(IoDpSrvQryTableInfo.class);
			IoDpKnaAcct knaAcct = qryTableInfo.getKnaAcctByOdb1(hcMain, true);//改为只从数据库中获取负债账户信息  jizhirong 20180518
			String trandt = HotCtrlUtil.getHotCtrlDate();
			if(CommUtil.compare(knaAcct.getUpbldt(),trandt) < 0) {
				knaAcct.setLstrdt(knaAcct.getUpbldt());
				knaAcct.setLastbl(knaAcct.getOnlnbl());
			}
			
			if(CommUtil.isNull(knaAcct.getUpbldt())){
				knaAcct.setLstrdt(trandt);
			}
			knaAcct.setUpbldt(trandt);
			BigDecimal onlnbl = knaAcct.getOnlnbl();
			log.info("负债账号[%s]更新前余额[%s]", hcMain, tranam);
			onlnbl = onlnbl.subtract(tranam);//tranam 正数则为借方发生额  负数则为贷方发生额
			log.info("负债账号[%s],发生额[%s],更新后余额[%s]", hcMain, tranam, tranam);
			knaAcct.setOnlnbl(onlnbl);
			log.info("更新负债账号余额：[%s]",knaAcct);
			try {
				qryTableInfo.updateKnaAcctOdb1(knaAcct);//更新余额
			} catch (Exception e) {//余额更新失败
				updateBalDetailStatus(hcMain,E_DETLSS.WCL,tabnum);
				return;
			}
			updateBalDetailStatus(hcMain,E_DETLSS.YCL,tabnum);//更新待处理余额表状态
		}
		*/
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
			log.info("负债账号[%s]更新前余额[%s]", hcMain, tranam);
			onlnbl = onlnbl.subtract(tranam);//tranam 正数则为借方发生额  负数则为贷方发生额
			log.info("负债账号[%s],发生额[%s],更新后余额[%s]", hcMain, tranam, tranam);
			knaAcct.setOnlnbl(onlnbl);
			log.info("更新负债账号余额：[%s]",knaAcct);
			try {
				qryTableInfo.updateKnaAcctOdb1(knaAcct);//更新余额
			} catch (Exception e) {//余额更新失败
				updateBalDetailStatus(hcMain,E_DETLSS.WCL,tabnum);
				return;
			}
			updateBalDetailStatus(hcMain,E_DETLSS.YCL,tabnum);//更新待处理余额表状态
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
			log.info("更新余额表,余额[%s],账号[%s],发生额[%s]", bal, hcMain, tranam);
			if(hcpDefn.getBlncdr() == E_BLNCDR.DR){
				bal = bal.add(tranam);
			} else if(hcpDefn.getBlncdr() == E_BLNCDR.CR) {
				bal = bal.subtract(tranam);
			}
			entity.setBalnce(bal);
			log.info("更新额度余额：[%s]",entity);
			try {
				HcbBlceDao.updateOne_odb1(entity);//更新余额
			} catch (Exception e) {//余额更新失败
				updateBalDetailStatus(hcMain,E_DETLSS.WCL,tabnum);
				return;
			}
			updateBalDetailStatus(hcMain,E_DETLSS.YCL,tabnum);//更新待处理余额表状态
		}
		
		
		/**
		 * 更新失败表明细状态  成功：处理中->余额已处理，失败：更新失败次数++ 或者 次数达到上线 处理中->未处理
		 *
		 */
		private void updateBalDetailStatus(String hcMain,E_DETLSS status,String tabnum) {
			log.info("======更新失败表明细状态开始主体号[%s]======",hcMain);
			RunEnvsComm runEnvs = CommTools.prcRunEnvs();
			String corpno = runEnvs.getCorpno();
			String trandt =HotCtrlUtil.getHotCtrlDate();
			if(status == E_DETLSS.WCL) {//余额更新失败，次数++，状态改为未处理
				log.info("======余额更新失败，次数++，状态改为未处理======");
				HcQuerySqlDao.UpdHcbFldlCount("hcb_fldl"+tabnum,E_DETLSS.WCL,E_DETLSS.CLZ, trandt, corpno);
			} else if(status == E_DETLSS.YCL){//余额更细成功，状态改为余额已处理
				log.info("======余额更新成功，状态改为余额已处理======");
				HcQuerySqlDao.UpdHcbFldlStatus("hcb_fldl"+tabnum,status,E_DETLSS.CLZ, trandt, corpno,null);
			}
			log.info("======更新失败表明细状态结束======");
		}

		/**
		 * 循环处理额度余额汇总(额度类型)
		 *
		 */
		/*private void processCL(List<HcBalSum> balSumList,String tabnum) {
			log.info("======循环处理额度余额汇总开始======");
			for(HcBalSum balSum : balSumList) {
				String hcMain = balSum.getHcmain();
				BigDecimal tranam = balSum.getTranam();
				DaoUtil.beginTransaction();
				if(CommUtil.compare(tranam, BigDecimal.ZERO) == 0) {//余额不变
					updateBalDetailStatus(hcMain,E_DETLSS.YCL,tabnum);//更新失败明细表状态
					continue;
				} else {
					updateBal(hcMain,tranam,tabnum);//更新余额 
				}
				DaoUtil.commitTransaction();
			}
			log.info("======循环处理额度余额汇总结束======");
		}*/

		/**
		 * 余额过账
		 *
		 */
		/*private void updateBalDayend(String trandt, String corpno) {
			log.info("======余额过账开始======");
			List<HcbBlce> hcbBlceList = HcQuerySqlDao.selHcbBlceList(corpno, trandt, false);
			if(CommUtil.isNull(hcbBlceList) || hcbBlceList.size() == 0) {//已切日
				for(HcbBlce hcbBlce : hcbBlceList) {
					hcbBlce.setLastdt(hcbBlce.getTrandt());
					hcbBlce.setTrandt(trandt);
					hcbBlce.setLsblce(hcbBlce.getBalnce());
					hcbBlce.setBalnce(BigDecimal.ZERO);
				}
				DaoUtil.insertBatch(HcbBlce.class, hcbBlceList);
			}
			log.info("======余额过账结束======");
		}*/
		/**
		 * 待处理余额明细处理
		 * 
		 */
		/*private void processBalList(List<HcBalSum> balSumList,String tabnum) {
			E_HCTYPE[] values = E_HCTYPE.values();
			for (int i = 0; i < values.length; i++) {
				if(values[i] == E_HCTYPE.CL) {//额度
					processCL(balSumList,tabnum);
				} else if(values[i] == E_HCTYPE.DP) {//账户 TODO 可再细分为结算户／内部户等
					
				}
			}
		}
*/
		/**
		 * 更新余额表
		 * 
		 */
		/*private void updateBal(String hcMain, BigDecimal tranam,String tabnum) {
			log.info("======更新余额表开始[%s]======",hcMain);
			//String trandt =  HotCtrlUtil.getHotCtrlDate();;
			HcbBlce entity = HcbBlceDao.selectOne_odb1(hcMain, false);
			HcpDefn hcpDefn=HcpDefnDao.selectOne_odb1(hcMain, true);
			BigDecimal bal = BigDecimal.ZERO;	
			if(CommUtil.isNotNull(entity)) {
				bal = entity.getBalnce();
			}else{
				throw HcError.HcGen.E0000("该热点主体余额记录不存在");
			}
			if(hcpDefn.getBlncdr() == E_BLNCDR.DR){
				bal=bal.add(tranam);
			} else if(hcpDefn.getBlncdr() == E_BLNCDR.CR) {
				bal=bal.subtract(tranam);
			}
			entity.setBalnce(bal);
			try {
				HcbBlceDao.updateOne_odb1(entity);//更新余额
				updateBalDetailStatus(hcMain,E_DETLSS.YCL,tabnum);//更新失败表状态
				log.info("======更新余额表成功,YCL======");
			} catch (Exception e) {//余额更新失败
				updateBalDetailStatus(hcMain,E_DETLSS.WCL,tabnum);
				log.info("======更新余额表失败,WCL======");
			}
			log.info("======更新余额表结束[%s]======",hcMain);
		}*/
		

	}

