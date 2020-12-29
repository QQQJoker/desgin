package cn.sunline.ltts.busi.aptran.trans.dayend;

import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.aptran.dayend.DayEndTools;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.adp.cedar.base.logging.BizLog;

public class hxdyed {

	public static final BizLog bizlog = LogManager.getBizLog(hxdyed.class);

	public static void cbmainDayEnd() {
		String flowType = "hx_dayend";
		dayEndCheck(CommTools.getFrdm(), flowType);
		DayEndTools.beginFlow(CommTools.getFrdm(), flowType);	
	}

	// 核心日终前检查
	private static void dayEndCheck(String corpno, String flowType) {
		String edctdt = DayEndTools.getDayEndDate(corpno, flowType);
		// 日终批量执行时间控制
		KnpPara parm = KnpParaDao.selectOne_odb1("dayEndCheck", "%", "%", "%", corpno, false);
		// 日终批量执行时间控制
		if (parm != null && "Y".equals(parm.getPmval1())) {
			String trdate = DateUtil.getNow("yyyyMMdd");// 机器日期
			int cpflag = DateUtil.compareDate(edctdt, trdate);
			if (cpflag > 0) {
				throw ApError.Aplt.E0000("日终日期[" + edctdt + "]大于机器日期[" + trdate
						+ "]不合法");
			} else if (cpflag == 0) {
				String sTime = DateUtil.getNow("HH:mm:ss");
				if (DateUtil.compareDate(sTime,"HH:mm:ss", "20:00:00","HH:mm:ss",0) < 0) {
					throw ApError.Aplt.E0000("日终日期[" + edctdt
							+ "]与机器日期相等,但是当前时间[" + sTime
							+ "]早于20:00:00不合法,本次日终批量将不执行！");
				}
			}
		}
	}
}
