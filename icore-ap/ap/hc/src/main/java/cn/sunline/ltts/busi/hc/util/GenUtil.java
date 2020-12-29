package cn.sunline.ltts.busi.hc.util;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.bsap.util.DaoSplitInvokeUtil;
import cn.sunline.ltts.busi.hc.tables.HcbLimitOccu.HcbLmoc;
import cn.sunline.ltts.busi.hc.tables.HcbPendDeal.HcbPedl;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn;
import cn.sunline.ltts.busi.iobus.type.hc.IoHotCtrlType.IoChkHotCtrlIn;
import cn.sunline.ltts.busi.sys.errors.HcError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DEALSS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DETLSS;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_NESTYN;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_SYNCST;

/**
 * 热点控制参数
 * @author Xiaoyu Luo
 *
 */
public class GenUtil {

	/**
	 * 功能说明：根据流水hash计算要插入的表
	 * 
	 * @param parmcd
	 * @param pmkey1
	 * @param pmkey2
	 * @param pmkey3
	 * @param str
	 * @return
	 */
	public static String getHashValue(String str, String tableName) {
		KnpPara knpPara = getKnpPara("HotCtrl", tableName, "TableCount", "%");
		Integer tableCount = Integer.valueOf(knpPara.getPmval1());
		Integer hash = (str.hashCode() & 0x7FFFFFFF % tableCount);
		String transqHash = hash.toString();
		return transqHash;
	}

	/**
	 * 功能说明：查询配置参数
	 * 
	 * @param parmcd
	 * @param pmkey1
	 * @param pmkey2
	 * @param pmkey3
	 * @return
	 */
	public static KnpPara getKnpPara(String parmcd, String pmkey1,
			String pmkey2, String pmkey3) {
		KnpPara knpPara = CommTools.KnpParaQryByCorpno(parmcd, pmkey1, pmkey2,
				pmkey3, false);
		if (CommUtil.isNull(knpPara)) {
			throw HcError.HcGen.E0003();
		}
		return knpPara;
	}

	public static HcbLmoc addHcbLmoc(IoChkHotCtrlIn ioChkHotCtrlIn,
			E_DEALSS dealss, E_NESTYN nestyn, String lmocHash) {
		String trandt = CommTools.prcRunEnvs().getTrandt();
		String transq = CommTools.prcRunEnvs().getTransq();
		HcbLmoc hcbLmoc = SysUtil.getInstance(HcbLmoc.class);
		hcbLmoc.setTrandt(trandt);
		hcbLmoc.setTransq(transq);
		hcbLmoc.setDetlsq(transq);
		hcbLmoc.setHctype(ioChkHotCtrlIn.getHctype());
		hcbLmoc.setHcmain(ioChkHotCtrlIn.getHcacct());
		hcbLmoc.setTranam(ioChkHotCtrlIn.getTranam());
		hcbLmoc.setAmntcd(ioChkHotCtrlIn.getAmntcd());
		hcbLmoc.setDealss(dealss);
		hcbLmoc.setNestyn(nestyn);
		hcbLmoc.setCorpno(CommTools.getTranCorpno());
		DaoSplitInvokeUtil.insert(hcbLmoc, lmocHash);
		return hcbLmoc;
	}

	/**
	 * 功能说明：热点额度待处理明细表
	 * 
	 * @param hcstin
	 *            冲正输入要素
	 */
	public static void regHcbPedl(IoChkHotCtrlIn hotCtrlIn, String pedlHash) {
		String trandt = CommTools.prcRunEnvs().getTrandt();
		String transq = CommTools.prcRunEnvs().getTransq();
		HcbPedl hcbPedl = SysUtil.getInstance(HcbPedl.class);
		hcbPedl.setTrandt(trandt);
		hcbPedl.setCdcnno(DcnUtil.getCurrDCN());
		hcbPedl.setTransq(transq);
		hcbPedl.setDetlsq(transq);
		hcbPedl.setHctype(hotCtrlIn.getHctype());
		hcbPedl.setAmntcd(hotCtrlIn.getAmntcd());
		hcbPedl.setHcmain(hotCtrlIn.getHcacct());
		hcbPedl.setTranam(hotCtrlIn.getTranam());
		hcbPedl.setNestyn(E_NESTYN.CZ);
		hcbPedl.setDealtm(new Integer(1)); //TODO 处理次数累计原因
		hcbPedl.setDetlss(E_DETLSS.WCL);
		hcbPedl.setSyncst(E_SYNCST.PEND);
		hcbPedl.setCorpno(CommTools.getTranCorpno());
		DaoSplitInvokeUtil.insert(hcbPedl, pedlHash);
	}

	/**
	 * 功能说明：热点控制redis额度占用
	 * 
	 * @param ioChkHotCtrlIn
	 *            热点控制输入要素
	 */
	public static void redisOccupy(IoChkHotCtrlIn ioChkHotCtrlIn) {		
		HcpDefn hcpd=HotCtrlUtil.checkHotMainAndReturn(ioChkHotCtrlIn.getHcacct());	
		if(CommUtil.isNotNull(hcpd)){
			HotCtrlCacheUtil.confirmHotCtrlData(ioChkHotCtrlIn.getHcacct(),hcpd.getBlncdr(),ioChkHotCtrlIn.getAmntcd(),
					ioChkHotCtrlIn.getTranam());
		}
	}

	/**
	 * 功能说明：热点控制redis额度释放
	 * 
	 * @param ioChkHotCtrlIn
	 *            热点控制输入要素
	 */
	public static void redisRelease(IoChkHotCtrlIn ioChkHotCtrlIn) {
		HcpDefn hcpd=HotCtrlUtil.checkHotMainAndReturn(ioChkHotCtrlIn.getHcacct());
		HotCtrlCacheUtil.releaseHotCtrlData(ioChkHotCtrlIn.getHcacct(),hcpd.getBlncdr(),ioChkHotCtrlIn.getAmntcd(),
				ioChkHotCtrlIn.getTranam());
	}

	/**
	 * 功能说明：redis明细删除
	 * 
	 * @param ioChkHotCtrlIn
	 */
	public static void delDetail(IoChkHotCtrlIn ioChkHotCtrlIn) {
		String transq = CommToolsAplt.prcRunEnvs().getTransq();
		HotCtrlCacheUtil.removeHotCtrlData(ioChkHotCtrlIn.getHcacct(), transq);
	}
	
	/**
	 * 功能说明：可用金额校验
	 * 
	 */
	public static void chkAvailablebalance(BigDecimal avaiam, BigDecimal tranam) {
		if (CommUtil.compare(avaiam, tranam) < 0 ) {
			throw HcError.HcGen.E0004();
		}
	}
}
