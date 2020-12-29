package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppGldt;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppGldtDao;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author T
 *         <p>
 *         <li>2014年3月11日-下午4:07:51</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228Name：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class GlDateTools {

	/**
	 * <p>
	 * <li>2015年1月28日-下午12:45:21</li>
	 * <li>功能说明：获取总账系统日期</li>
	 * </p>
	 * 
	 * @return
	 */
	public static ApSysDateStru getGlDateInfo() {

		String sFrdm = CommTools.getTranCorpno();
		AppGldt tblKapp_kjidat = AppGldtDao.selectOne_odb1(sFrdm, true);

		if (tblKapp_kjidat == null)
			throw Sys.E0001("读取会计系统日期失败，无对应记录[" + sFrdm + "]");

		ApSysDateStru cplSysDateInfo = SysUtil.getInstance(ApSysDateStru.class);

		cplSysDateInfo.setHostdt(DateTools.getSystemDate());
		cplSysDateInfo.setTrantm(DateTools.getTransTimestamp());

		cplSysDateInfo.setSystdt(tblKapp_kjidat.getSystdt());
		cplSysDateInfo.setLastdt(tblKapp_kjidat.getLastdt());
		cplSysDateInfo.setBflsdt(tblKapp_kjidat.getBflsdt());
		cplSysDateInfo.setNextdt(tblKapp_kjidat.getNextdt());
		cplSysDateInfo.setAfnxdt(tblKapp_kjidat.getAfnxdt());
		cplSysDateInfo.setYreddt(tblKapp_kjidat.getYreddt());

		return cplSysDateInfo;
	}

	public static ApSysDateStru getGlLastDateInfo() {

		String sFrdm = CommTools.getTranCorpno();
		AppGldt tblKapp_kjidat = AppGldtDao.selectOne_odb1(sFrdm, true);

		if (tblKapp_kjidat == null)
			throw Sys.E0001("读取会计系统日期失败，无对应记录[" + sFrdm + "]");

		ApSysDateStru cplSysDateInfo = SysUtil.getInstance(ApSysDateStru.class);

		cplSysDateInfo.setHostdt(DateTools.getSystemDate());
		cplSysDateInfo.setTrantm(DateTools.getTransTimestamp());

		cplSysDateInfo.setSystdt(tblKapp_kjidat.getLastdt());
		cplSysDateInfo.setLastdt(tblKapp_kjidat.getBflsdt());
		cplSysDateInfo.setBflsdt(DateTools.calDateByTerm(tblKapp_kjidat.getBflsdt(), "-1D"));
		cplSysDateInfo.setNextdt(tblKapp_kjidat.getSystdt());
		cplSysDateInfo.setAfnxdt(tblKapp_kjidat.getNextdt());
		cplSysDateInfo.setYreddt(tblKapp_kjidat.getYreddt());

		return cplSysDateInfo;
	}
	
}
