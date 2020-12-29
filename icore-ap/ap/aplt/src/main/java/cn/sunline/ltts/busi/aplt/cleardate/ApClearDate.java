package cn.sunline.ltts.busi.aplt.cleardate;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppCldt;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppCldtDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;

public class ApClearDate {
	/**
	 * @Author T
	 *         <p>
	 *         <li>功能说明：获取清算日期</li>
	 *         </p>
	 * @return 日期信息的复合类型
	 */
	public static ApSysDateStru getClearDateInfo() {

		AppCldt tblKapp_clrdat = AppCldtDao
				.selectOne_odb1(CommTools.getTranCorpno(), false);

		if (tblKapp_clrdat == null)
			throw Sys.E0001("获取当前清算日期失败，无对应记录[" + CommTools.getCenterCorpno() + "]");

		ApSysDateStru cplSysDateInfo = SysUtil.getInstance(ApSysDateStru.class);

		cplSysDateInfo.setHostdt(DateTools.getSystemDate());
		cplSysDateInfo.setTrantm(DateTools.getTransTimestamp());

		cplSysDateInfo.setBflsdt(tblKapp_clrdat.getBflsdt());
		cplSysDateInfo.setLastdt(tblKapp_clrdat.getLastdt());
		cplSysDateInfo.setSystdt(tblKapp_clrdat.getSystdt());
		cplSysDateInfo.setNextdt(tblKapp_clrdat.getNextdt());
		cplSysDateInfo.setAfnxdt(tblKapp_clrdat.getAfnxdt());
		cplSysDateInfo.setYreddt(tblKapp_clrdat.getYreddt());
		cplSysDateInfo.setClenum(tblKapp_clrdat.getClenum());

		return cplSysDateInfo;
	}

	/**
	 * @Author T
	 *         <p>
	 *         <li>功能说明：切换清算日期</li>
	 *         </p>
	 */
	public static AppCldt chgClearDate() {

		AppCldt tblKapp_clrdat = AppCldtDao.selectOneWithLock_odb1(
				CommTools.getCenterCorpno(), true);

		KnpPara tblkna_para = CommTools.KnpParaQryByCorpno("APTRAN", "ap011", "%",
				"%", false);

		if (CommUtil.compare(tblkna_para.getPmval1(), tblKapp_clrdat
				.getClenum().toString()) == 0) {
			tblKapp_clrdat.setBflsdt(tblKapp_clrdat.getLastdt());
			tblKapp_clrdat.setLastdt(tblKapp_clrdat.getSystdt());

			if ("".equals(tblKapp_clrdat.getNextdt())) {
				throw Sys.E0003();
			} else {
				tblKapp_clrdat.setSystdt(tblKapp_clrdat.getNextdt());
			}

			tblKapp_clrdat.setNextdt(tblKapp_clrdat.getAfnxdt());
			tblKapp_clrdat.setAfnxdt(DateTools.calDateByTerm(
					tblKapp_clrdat.getAfnxdt(), "1D"));
			tblKapp_clrdat.setYreddt(DateTools.calDateByFreq(
					tblKapp_clrdat.getSystdt(), "1YAE"));
			tblKapp_clrdat.setClenum(1);// 初始化场次

			AppCldtDao.updateOne_odb1(tblKapp_clrdat);

			return tblKapp_clrdat;

		} else {

			tblKapp_clrdat.setClenum(tblKapp_clrdat.getClenum() + 1);

			return tblKapp_clrdat;
		}
	}
}
