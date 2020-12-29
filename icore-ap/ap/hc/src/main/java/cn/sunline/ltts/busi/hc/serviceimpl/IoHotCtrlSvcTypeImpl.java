package cn.sunline.ltts.busi.hc.serviceimpl;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.bsap.util.DaoSplitInvokeUtil;
import cn.sunline.ltts.busi.hc.tables.HcbLimitOccu.HcbLmoc;
import cn.sunline.ltts.busi.hc.tables.HcbPendDeal.HcbPedl;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbFile;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbFileDao;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefnDao;
import cn.sunline.ltts.busi.hc.util.GenUtil;
import cn.sunline.ltts.busi.hc.util.HotCtrlCacheUtil;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoChkHotCtrlIn;
import cn.sunline.ltts.busi.sys.errors.HcError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DEALSS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DETLSS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HCSTAS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HCTYPE;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_NESTYN;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_SYNCST;

/**
 * 热点控制服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoHotCtrlSvcTypeImpl", longname = "热点控制服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoHotCtrlSvcTypeImpl implements
		cn.sunline.ltts.busi.iobus.servicetype.hc.IoHotCtrlSvcType {

	private static final BizLog bizlog = LogManager.getBizLog(IoHotCtrlSvcTypeImpl.class);
	private static String trandt;
	private static String transq;
	private static String hash;
	//private static String pedlHash;

	/**
	 * 功能说明：热点账户校验
	 * 
	 * @author Xiaoyu Luo
	 * @param hotinp热点账户控制输入要素
	 */
	public void chkHotCtrl(
			final cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoChkHotCtrlIn hotinp) {
		trandt = CommTools.prcRunEnvs().getTrandt();
		transq = CommToolsAplt.prcRunEnvs().getTransq();
		//lmocHash = GenUtil.getHashValue(transq, "HcbLmoc");
		//pedlHash = GenUtil.getHashValue(transq, "HcbPedl");
		// 输入校验
		checkInput(hotinp);
		// 热点定义表查询
		HcpDefn hcpDefn = HcpDefnDao.selectOne_odb1(hotinp.getHcacct(), false);
		if (CommUtil.isNull(hcpDefn)) {
			throw HcError.HcGen.E0002("热点定义信息");
		}

		//if (hcpDefn.getHctype() == E_HCTYPE.CL) {
			//按产品号hash
			//lmocHash = GenUtil.getHashValue(hotinp.getHcacct(), "HcbLmoc");
			//pedlHash = GenUtil.getHashValue(hotinp.getHcacct(), "HcbPedl");
		    hash=GenUtil.getHashValue(hotinp.getHcacct(), "HcbLmoc");
		    // 热点控制 热点类型额度处理	
			if (hotinp.getAmntcd() == E_AMNTCD.DR) {
				try {		
					// 1、登记占用明细表(独立事务)
					regHcbLmocIndependentTransaction(hotinp);
					// 获取redis可用余额
					BigDecimal avaiam = HotCtrlCacheUtil.getHotCtrlCurBalData(hotinp.getHcacct());
					GenUtil.chkAvailablebalance(avaiam, hotinp.getTranam());
					// 2、redis额度占用
					hotinp.setTranam(hotinp.getTranam());	
					GenUtil.redisOccupy(hotinp);
					// 3、登记热点额度待处理
					regHcbPedl(hotinp, avaiam);		
					// 4、成功：修改占用额度明细（状态为“成功”）（随主事务提交）
				    HcbLmoc hcbLmoc = DaoSplitInvokeUtil.selectOne(HcbLmoc.class,"selectOne_odb2", hash, trandt,transq,false);
					hcbLmoc.setDealss(E_DEALSS.CG);
					DaoSplitInvokeUtil.update(hcbLmoc, "updateOne_odb2", hash);
					// 5、成功确认redis占用额度并删除明细
					//GenUtil.delDetail(hotinp);
					}catch (Exception e) {
						// 功能说明：失败，修改占用额度明细（状态为“失败”）（独立事务）
						hotCtrlLmocFail(hotinp);
						throw HcError.HcGen.E0000(e.toString());			
					}
				}else if (hotinp.getAmntcd() == E_AMNTCD.CR) {
					// 1、登记额度占用明细表（随主事务提交）
					regHcbLmoc(hotinp);
					// 2、redis额度释放
					BigDecimal avaiam = HotCtrlCacheUtil.getHotCtrlCurBalData(hotinp.getHcacct());
					GenUtil.redisOccupy(hotinp);
					//GenUtil.redisRelease(hotinp);
					// 3、登记热点额度待处理
					regHcbPedl(hotinp, avaiam);
				} 
			/*}else {
				lmocHash = GenUtil.getHashValue(transq, "HcbLmoc");
				pedlHash = GenUtil.getHashValue(transq, "HcbPedl");	
		        // TODO 热点控制 热点类型账户处理
			}*/

//		if (hcpDefn.getHctype() == E_HCTYPE.CL) {
		//按产品号hash
		/*lmocHash = GenUtil.getHashValue(hotinp.getHcacct(), "HcbLmoc");
		pedlHash = GenUtil.getHashValue(hotinp.getHcacct(), "HcbPedl");
	    // 热点控制 热点类型额度处理	
		if (hotinp.getAmntcd() == E_AMNTCD.DR) {
			try {		
				// 1、登记占用明细表(独立事务)
				regHcbLmocIndependentTransaction(hotinp);
				// 获取redis可用余额
				BigDecimal avaiam = HotCtrlCacheUtil.getHotCtrlCurBalData(hotinp.getHcacct());
				GenUtil.chkAvailablebalance(avaiam, hotinp.getTranam());
				// 2、redis额度占用
				hotinp.setTranam(hotinp.getTranam());	
				GenUtil.redisOccupy(hotinp);
				// 3、登记热点额度待处理
				regHcbPedl(hotinp, avaiam);		
				// 4、成功：修改占用额度明细（状态为“成功”）（随主事务提交）
			    HcbLmoc hcbLmoc = DaoSplitInvokeUtil.selectOne(HcbLmoc.class,"selectOne_odb2", lmocHash, trandt,transq,false);
				hcbLmoc.setDealss(E_DEALSS.CG);
				DaoSplitInvokeUtil.update(hcbLmoc, "updateOne_odb2", lmocHash);
				// 5、成功确认redis占用额度并删除明细
				GenUtil.delDetail(hotinp);
				}catch (Exception e) {
					// 功能说明：失败，修改占用额度明细（状态为“失败”）（独立事务）
					hotCtrlLmocFail(hotinp);
					throw HcError.HcGen.E0000(e.toString());			
				}
			}else if (hotinp.getAmntcd() == E_AMNTCD.CR) {
				// 1、登记额度占用明细表（随主事务提交）
				regHcbLmoc(hotinp);
				// 2、redis额度释放
				BigDecimal avaiam = HotCtrlCacheUtil.getHotCtrlCurBalData(hotinp.getHcacct());
				//GenUtil.redisOccupy(hotinp);
				GenUtil.redisRelease(hotinp);
				// 3、登记热点额度待处理
				regHcbPedl(hotinp, avaiam);
			} */
//			}
//		else {
//				lmocHash = GenUtil.getHashValue(transq, "HcbLmoc");
//				pedlHash = GenUtil.getHashValue(transq, "HcbPedl");	
//		        // TODO 热点控制 热点类型账户处理
//			}

	}

	/**
	 * 功能说明： 输入要素校验
	 * 
	 * @author Xiaoyu Luo
	 * @param hotInput
	 */
	public void checkInput(IoChkHotCtrlIn hotInput) {
		if (CommUtil.isNull(hotInput.getHcacct())) {
			HcError.HcGen.E0001("账号或产品号");
		}
		if (CommUtil.isNull(hotInput.getHctype())) {
			HcError.HcGen.E0001(hotInput.getHctype().getLongName());
		}
		if (CommUtil.isNull(hotInput.getAmntcd())) {
			HcError.HcGen.E0001(hotInput.getAmntcd().getLongName());
		}
		if (hotInput.getHctype() == E_HCTYPE.CL) {
			if (CommUtil.isNull(hotInput.getTranam())) {
				HcError.HcGen.E0001("交易金额");
			}
		}
	}

	/**
	 * 功能说明：独立事务登记额度占用表
	 * 
	 * @author Xiaoyu Luo
	 * @param hotCtrlIn登记占用表输入要素
	 * 
	 */
	public void regHcbLmocIndependentTransaction(final IoChkHotCtrlIn hotCtrlIn) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				// 1、登记占用明细表
				bizlog.debug("==================登记占用明细表==================");
				GenUtil.addHcbLmoc(hotCtrlIn, E_DEALSS.ZY, E_NESTYN.BCZ,hash);
				return null;
			}
		});
	}

	/**
	 * 功能说明：随主事务登记额度占用表
	 * 
	 * @author Xiaoyu Luo
	 * @param hotCtrlIn登记占用表输入要素
	 * 
	 */
	public void regHcbLmoc(final IoChkHotCtrlIn hotCtrlIn) {
		// 1、登记占用明细表
		bizlog.debug("==================登记占用明细表==================");
		GenUtil.addHcbLmoc(hotCtrlIn, E_DEALSS.CG, E_NESTYN.BCZ, hash);
	}

	/**
	 * 功能说明：登记热点额度待处理明细表
	 * 
	 * @author Xiaoyu Luo
	 * @param hotCtrlIn
	 * @param availableBalance
	 */
	public void regHcbPedl(IoChkHotCtrlIn hotCtrlIn, BigDecimal availableBalance) {
		bizlog.debug("==================登记余额明细表==================");
		HcbPedl hcbPedl = SysUtil.getInstance(HcbPedl.class);
		hcbPedl.setTrandt(trandt);
		hcbPedl.setCdcnno(DcnUtil.getCurrDCN());
		hcbPedl.setTransq(transq);
		hcbPedl.setDetlsq(transq);
		hcbPedl.setHctype(hotCtrlIn.getHctype());
		hcbPedl.setAmntcd(hotCtrlIn.getAmntcd());
		hcbPedl.setHcmain(hotCtrlIn.getHcacct());
		hcbPedl.setTranam(hotCtrlIn.getTranam());
		hcbPedl.setNestyn(E_NESTYN.BCZ);
		hcbPedl.setDealtm(new Integer(1));
		hcbPedl.setDetlss(E_DETLSS.WCL);
		hcbPedl.setSyncst(E_SYNCST.PEND);
		hcbPedl.setCorpno(CommTools.getTranCorpno());
		DaoSplitInvokeUtil.insert(hcbPedl, hash);
	}

	/**
	 * 功能说明：失败，修改占用额度明细（状态为“失败”）（独立事务）
	 * 
	 * @author Xiaoyu Luo
	 */
	public void hotCtrlLmocFail(
			final cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoChkHotCtrlIn hotinp) {
		// 4、独力事务更改占用表状态为失败
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				// 修改占用额度明细状态
			    HcbLmoc hcbLmoc = DaoSplitInvokeUtil.selectOne(HcbLmoc.class,"selectOne_odb2", hash, trandt,transq,true);				
				hcbLmoc.setDealss(E_DEALSS.SB);	
				DaoSplitInvokeUtil.update(hcbLmoc, "updateOne_odb2", hash);
				return null;
			}
		});
		// 5、失败释放redis占用额度并删除明细
		//GenUtil.delDetail(hotinp);
	}

	/**
	 * 功能说明：判断是否为热点账户
	 * @param acctno 账户信息
	 * @return output 返回是否存在信息
	 */
	public E_YES___ selHcpDefn(String acctno) {
		HcpDefn hcpDefn = HcpDefnDao.selectOne_odb1(acctno, false);
		boolean valid = true;//是否生效
		if(CommUtil.isNull(hcpDefn) || hcpDefn.getHcstas()==E_HCSTAS.SX) {
			valid = false;
		}
		if (valid) {
			return E_YES___.YES;
		} else {
			return E_YES___.NO;
		}
	}

	/**
	 * 功能说明：同步管理节点的文件生成记录
	 * 
	 */
	public void addHcbFile( final cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IohcbFileIn filein){

		bizlog.debug("==================登记文件记录表==================");

		HcbFile hcbfile=SysUtil.getInstance(HcbFile.class);
		hcbfile.setFlpath(filein.getFlpath());
        hcbfile.setCorpno(filein.getCorpno());
        hcbfile.setFlname(filein.getFlname());
        hcbfile.setHsread(filein.getHsread());
        hcbfile.setTrandt(filein.getTrandt());

        hcbfile.setTablnm(filein.getTabnum());
        HcbFileDao.insert(hcbfile);   
        //insertHcbFile(hcbfile);
        bizlog.debug("==================登记文件记录表==================");
	}
	/*public void insertHcbFile(final HcbFile hcbfile){
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				bizlog.debug("==================独立事务登记文件记录表==================");
				//文件记录输入要素
				HcbFileDao.insert(hcbfile); 			
			    return null;
			}
		});
	}*/
	
}
