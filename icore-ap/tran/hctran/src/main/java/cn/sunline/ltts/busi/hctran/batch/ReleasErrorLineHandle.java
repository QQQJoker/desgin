package cn.sunline.ltts.busi.hctran.batch;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTran;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTranDao;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.bsap.util.DaoSplitInvokeUtil;
import cn.sunline.ltts.busi.hc.namedsql.HcQuerySqlDao;
import cn.sunline.ltts.busi.hc.tables.HcbLimitOccu.HcbLmoc;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbNris;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbNrisDao;
import cn.sunline.ltts.busi.hc.util.HotCtrlCacheUtil;
import cn.sunline.ltts.busi.hc.util.HotCtrlUtil;
import cn.sunline.ltts.busi.sys.errors.HcError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DTXNST;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DEALSS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_NESTYN;

/**
 * 释放差错额度处理类
 * @author jizhirong
 * 20180416
 */
public class ReleasErrorLineHandle{
	
	private static BizLog bizLog = LogManager.getBizLog(ReleasErrorLineHandle.class);
	private String tabnum;//分表序号
	public ReleasErrorLineHandle(String tabnum){
		this.tabnum = tabnum;
	}
	/**
	 * 释放额度处理
	 */
	public void handle() {
		KnpPara knpPara1 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "TIME",true);
		KnpPara knpPara2 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "SELECT_LIMIT",true);
		//KnpPara knpPara3 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "FAIL_COUNT",true);
		int processTime = Integer.parseInt(knpPara1.getPmval1());//秒,运行时间
		int waitTime = Integer.parseInt(knpPara1.getPmval3());//秒，等待时间
		int limiCunt=Integer.parseInt(knpPara2.getPmval3());//查询条数
		long startTime = System.currentTimeMillis();
	do {
		KnpPara knpPara = CommTools.KnpParaQryByCorpno("TimedReleasErrorLine", "%", "%", "%", false);
		int timesd = 3;//默认3分钟
		if(CommUtil.isNotNull(knpPara)){
			timesd = Integer.parseInt(knpPara.getPmval1());
		}
		String hcb_lmoc = "hcb_lmoc"+tabnum;
		String hcb_pedl = "hcb_pedl"+tabnum;
		String trandt = CommToolsAplt.prcRunEnvs().getTrandt();
		String corpno = CommToolsAplt.prcRunEnvs().getCorpno();
		//String tmstmp = DateTools.getCurrentTimestamp21();
		String tmstmp = DateTools.getCurrentTimestamp("yyyyMMddHHmmss.SSSSSS");
		bizLog.info("处理表数据hcb_lmoc"+tabnum);
		//查询三分钟以上额度占用表已占用的额度信息
		List<HcbLmoc> listHcbLmoc = HcQuerySqlDao.listHcbLmocs(E_DEALSS.ZY,tmstmp,timesd,hcb_lmoc,hcb_pedl,trandt,corpno,limiCunt,false);
		if(!listHcbLmoc.isEmpty()){		
			//releas("hcb_lmoc", listHcbLmoc);	
			release(listHcbLmoc);	
		}else{
			
			// 如果无数据,则等待waitTime
			try {
				bizLog.info("TimedReleasErrorLine.process begin waiting：" + waitTime + "秒");
				Thread.sleep(waitTime*1000);
			} catch (InterruptedException e) {
				bizLog.error("[%s]", e.toString());
			}
			
		}
		long endTime = System.currentTimeMillis();
		bizLog.info("TimedReleasErrorLine.process time：" + processTime + "秒");
		if((endTime - startTime)/1000 > processTime) {
			break;
		}
		} while (true);
	}
	/**
	 * 
	 * @param hcb_lmoc 额度占用表分表名
	 * @param listHcbLmoc 符合条件的额度占用表信息
	 */
	/*private void releas(String hcb_lmoc, List<HcbLmoc> listHcbLmoc) {
		for (HcbLmoc hcbLmoc : listHcbLmoc){
			DaoUtil.beginTransaction();
			//当前交易失败
			KnsTran knsTran = KnsTranDao.selectOne_odb1(hcbLmoc.getTransq(), hcbLmoc.getTrandt(), false);
			if(CommUtil.isNull(knsTran) || knsTran.getDtxnst() == E_DTXNST.FAILED){
				bizLog.info("交易流水开始释放缓存");
				try{
					// 释放缓存额度
				HotCtrlCacheUtil.releaseHotCtrlData(hcbLmoc.getHcmain(), HotCtrlUtil.getHotCtrlBlncdr(hcbLmoc.getHcmain()), hcbLmoc.getAmntcd(), hcbLmoc.getTranam());
					//修改额度占用表状态为释放额度
					//E_DEALSS dealss = E_DEALSS.TZ;
					//Map<Object, Object> map = new HashMap<Object, Object>();
					hcbLmoc.setDealss(E_DEALSS.TZ);
					//map.put("dealss", dealss);
					//DaoSplitInvokeUtil.update(hcb_lmoc, map,"update_odb1",tabnum);//变更额度占用表状态
					DaoSplitInvokeUtil.update(hcbLmoc, "updateOne_odb2", tabnum);
					//登记热点额度未释放差错表
					HcbNris hcbNris = CommTools.getInstance(HcbNris.class);
					CommUtil.copyProperties(hcbNris, hcbLmoc);
					hcbNris.setCdcnno(CommToolsAplt.prcRunEnvs().getCdcnno());
					hcbNris.setNestyn(E_NESTYN.CZ);
					HcbNrisDao.insert(hcbNris);
				}catch (Exception e) {
					HotCtrlCacheUtil.confirmHotCtrlData(hcbLmoc.getHcmain(),HotCtrlUtil.getHotCtrlBlncdr(hcbLmoc.getHcmain()),getOppsiteAmntcd(hcbLmoc.getAmntcd()),
							hcbLmoc.getTranam());	
					throw HcError.HcGen.E0000("定时余额差错处理失败");
				}
				
			}else if(CommUtil.isNotNull(knsTran) && knsTran.getDtxnst() == E_DTXNST.S_SUCCESS){
				//Map<Object, Object> map = new HashMap<Object, Object>();
				//E_DEALSS dealss = E_DEALSS.CG;
				hcbLmoc.setDealss(E_DEALSS.CG);
				//map.put("dealss", dealss);
				//DaoSplitInvokeUtil.update(hcb_lmoc, map,"update_odb1",tabnum);//变更额度占用表状态
				DaoSplitInvokeUtil.update(hcbLmoc, "updateOne_odb2", tabnum);
			}
			DaoUtil.commitTransaction();
		}
	}
	*/
	//独立事务
	public void release(final List<HcbLmoc> listHcbLmoc) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				for (HcbLmoc hcbLmoc : listHcbLmoc){			
					//当前交易失败
					KnsTran knsTran = KnsTranDao.selectOne_odb1(hcbLmoc.getTransq(), hcbLmoc.getTrandt(), false);
					if(CommUtil.isNull(knsTran) || knsTran.getDtxnst() == E_DTXNST.FAILED){
						bizLog.info("交易流水开始释放缓存");
						try{
							// 释放缓存额度
						    HotCtrlCacheUtil.releaseHotCtrlData(hcbLmoc.getHcmain(), HotCtrlUtil.getHotCtrlBlncdr(hcbLmoc.getHcmain()), hcbLmoc.getAmntcd(), hcbLmoc.getTranam());
							//修改额度占用表状态为释放额度
							//E_DEALSS dealss = E_DEALSS.TZ;
							//Map<Object, Object> map = new HashMap<Object, Object>();
							hcbLmoc.setDealss(E_DEALSS.TZ);
							//map.put("dealss", dealss);
							//DaoSplitInvokeUtil.update(hcb_lmoc, map,"update_odb1",tabnum);//变更额度占用表状态
							DaoSplitInvokeUtil.update(hcbLmoc, "updateOne_odb2", tabnum);
							//登记热点额度未释放差错表
							HcbNris hcbNris = CommTools.getInstance(HcbNris.class);
							CommUtil.copyProperties(hcbNris, hcbLmoc);
							hcbNris.setCdcnno(CommToolsAplt.prcRunEnvs().getCdcnno());
							hcbNris.setNestyn(E_NESTYN.CZ);
							HcbNrisDao.insert(hcbNris);
						}catch (Exception e) {
							HotCtrlCacheUtil.confirmHotCtrlData(hcbLmoc.getHcmain(),HotCtrlUtil.getHotCtrlBlncdr(hcbLmoc.getHcmain()),getOppsiteAmntcd(hcbLmoc.getAmntcd()),
									hcbLmoc.getTranam());	
							throw HcError.HcGen.E0000("定时余额差错处理失败");
						}
						
					}else if(CommUtil.isNotNull(knsTran) && knsTran.getDtxnst() == E_DTXNST.S_SUCCESS){
						//Map<Object, Object> map = new HashMap<Object, Object>();
						//E_DEALSS dealss = E_DEALSS.CG;
						hcbLmoc.setDealss(E_DEALSS.CG);
						//map.put("dealss", dealss);
						//DaoSplitInvokeUtil.update(hcb_lmoc, map,"update_odb1",tabnum);//变更额度占用表状态
						DaoSplitInvokeUtil.update(hcbLmoc, "updateOne_odb2", tabnum);
					}
				}
				return null;
			}
		});
	}
	
	
	private E_AMNTCD getOppsiteAmntcd(E_AMNTCD amntcd) {
		if(CommUtil.compare(amntcd, E_AMNTCD.CR) == 0) {
			return E_AMNTCD.DR;
		} else if(CommUtil.compare(amntcd, E_AMNTCD.DR) == 0) {
			return E_AMNTCD.CR;
		}
		return null;
	}
}
