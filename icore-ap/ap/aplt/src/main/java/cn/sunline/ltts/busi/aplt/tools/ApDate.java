package cn.sunline.ltts.busi.aplt.tools;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppGldt;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppGldtDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydtDao;
import cn.sunline.ltts.busi.sys.dict.ApDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;

public class ApDate {

    /**
     * @Author HongBiao
     *         <p>
     *         <li>2016年12月8日-下午5:32:01</li>
     *         <li>功能说明:对当前业务法人做日切处理</li>
     *         </p>
     */
    public static void swithSysDate() {

        RunEnvs runEnvs = SysUtil.getTrxRunEnvs();

        // 当前系统日期信息
        String corpno = runEnvs.getCorpno();
        AppSydt dateInfo = AppSydtDao.selectOneWithLock_odb1(corpno, false);

        if (dateInfo == null) {
            throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppSydt.class).getLongname(), ApDict.Aplt.corpno.getLongName(), corpno);
        }

        String beforeDate = dateInfo.getLastdt();
        String lastDate = dateInfo.getSystdt(); // 系统上日日期使用系统当前日期
        String trxnDate = dateInfo.getNextdt(); // 系统当前日期使用系统下日日期
        String nextDate =  dateInfo.getAfnxdt();
        String afterDate = DateTools2.dateAdd("day", nextDate, 1);// 系统下日日期使用系统当前日期加一天。
        String balSheetDate = DateTools2.lastDay("Y", trxnDate); // 年节日使用当前年份最后一天

        // 日切系统日期修改
        dateInfo.setBflsdt(beforeDate);
        dateInfo.setLastdt(lastDate);
        dateInfo.setSystdt(trxnDate);
        dateInfo.setNextdt(nextDate);
        dateInfo.setAfnxdt(afterDate);
        dateInfo.setYreddt(balSheetDate);
        dateInfo.setTimetm(DateTools.getTransTimestamp());
        if(CommUtil.isNull(dateInfo.getParmvi())) {
        	dateInfo.setParmvi("1");
        } else {
        	dateInfo.setParmvi(String.valueOf(Long.parseLong(dateInfo.getParmvi()) + 1));
        }
        
        AppSydtDao.updateOne_odb1(dateInfo);
        
        // 更新环境变量日期
        runEnvs.setTrandt(trxnDate);
        runEnvs.setNxtrdt(nextDate);
        runEnvs.setLstrdt(lastDate);
        runEnvs.setYreddt(balSheetDate);

    }
    
    /**
     * 
     * <p>Title:swithGlDate </p>
     * <p>Description:	总账日期切日</p>
     * @author cuijia
     * @date   2017年6月24日
     */
    public static void swithGlDate() {

        RunEnvs runEnvs = SysUtil.getTrxRunEnvs();
        // 当前系统日期信息
        String corpno = runEnvs.getCorpno();
        AppGldt dateInfo = AppGldtDao.selectOneWithLock_odb1(corpno, false);

        if (dateInfo == null) {
            throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppSydt.class).getLongname(), ApDict.Aplt.corpno.getLongName(), corpno);
        }

        String beforeDate = dateInfo.getLastdt();
        String lastDate = dateInfo.getSystdt(); // 系统上日日期使用系统当前日期
        String trxnDate = dateInfo.getNextdt(); // 系统当前日期使用系统下日日期
        String nextDate =  dateInfo.getAfnxdt();
        String afterDate = DateTools2.dateAdd("day", nextDate, 1);// 系统下日日期使用系统当前日期加一天。
        String balSheetDate = DateTools2.lastDay("Y", trxnDate); // 年节日使用当前年份最后一天

        // 日切系统日期修改
        dateInfo.setBflsdt(beforeDate);
        dateInfo.setLastdt(lastDate);
        dateInfo.setSystdt(trxnDate);
        dateInfo.setNextdt(nextDate);
        dateInfo.setAfnxdt(afterDate);
        dateInfo.setYreddt(balSheetDate);
        dateInfo.setTimetm(DateTools.getTransTimestamp());
        dateInfo.setParmvi(dateInfo.getParmvi() + 1);
        
        AppGldtDao.updateOne_odb1(dateInfo);
        
        // 更新环境变量日期
        runEnvs.setGldate(trxnDate);
        runEnvs.setNxgldt(nextDate);
        runEnvs.setLsgldt(lastDate);
        runEnvs.setYreddt(balSheetDate);

    }
    
    /**
	 * 
	 * @Author caipc
	 *         <p>
	 *         <li>2017年4月17日-下午7:56:26</li>
	 *         <li>功能说明：新增系统日期</li>
	 *         </p>
	 * @param orgId
	 * @param trxnDate
	 * @param bal_sheet_date
	 */
	public static List<String> addApDate(String orgId){
		return null;
	}
    
    /**
	 * @Author HongBiao
	 *         <p>
	 *         <li>2016年12月8日-下午5:32:01</li>
	 *         <li>功能说明：获取系统日期信息,系统日期不存在时抛出异常</li>
	 *         </p>
	 * @param orgId
	 *            业务法人ID
	 * @return 系统日期对象
	 */
	public static AppSydt getInfo(String corpno) {
		
		AppSydt dateInfo = AppSydtDao.selectOneWithLock_odb1(corpno, false);

        if (dateInfo == null) {
            throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppSydt.class).getLongname(), ApDict.Aplt.corpno.getLongName(), corpno);
        }

		return dateInfo;
	}
}
