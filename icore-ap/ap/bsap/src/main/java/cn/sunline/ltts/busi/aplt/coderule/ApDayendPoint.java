package cn.sunline.ltts.busi.aplt.coderule;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnbEodp;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnbEodpDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_DYEDCT;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author Administrator
 *         <p>
 *         <li>2015年1月16日-下午1:02:59</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>日终时间点处理逻辑</li>
 *         <li>20140228Administrator：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */

public class ApDayendPoint {

	// 日终某个时点的登记
	public static void register(String trandt, E_DYEDCT dyedct) {

		KnbEodp tblKnbEodp = KnbEodpDao.selectOne_odb1(trandt, dyedct, false);

		if (CommUtil.isNull(tblKnbEodp)) {
			tblKnbEodp = SysUtil.getInstance(KnbEodp.class);

			tblKnbEodp.setTrandt(trandt);
			tblKnbEodp.setDyedct(dyedct);
			
			KnbEodpDao.insert(tblKnbEodp);
		}
	}

	// 判断是否经历某个日终时间点
	public static boolean exists(String trandt, E_DYEDCT dyedct) {

		KnbEodp tblKnbEodp = KnbEodpDao.selectOne_odb1(trandt, dyedct, false);

		if (CommUtil.isNotNull(tblKnbEodp))
			return true;
		else
			return false;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2015年1月16日-下午1:15:50</li>
	 *         <li>功能说明：销户窗口判断</li>
	 *         </p>
	 * @param sJiaoyirq
	 * @return true 许可 false 禁止
	 */
	public static boolean canCloseAccount(String trandt) {

		// 判断是否处于“已日切，但未完成计息”区间
		boolean bBetween = betweenFlag(trandt, E_DYEDCT.RIQJS, E_DYEDCT.JIXIJS);

		// 处于该区间禁止销户
		if (bBetween)
			return false;
		else
			return true;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2015年1月16日-下午1:07:11</li>
	 *         <li>功能说明：判断日终是否正处于某个区间</li>
	 *         </p>
	 * @param eStartPoint
	 *            开始点
	 * @param eEndPoint
	 * @return
	 */
	private static boolean betweenFlag(String trandt, E_DYEDCT eStartPoint, E_DYEDCT eEndPoint) {

		boolean bResult = false;

		// 已经开始了前点，但未结束后点
		if (exists(trandt, eStartPoint) && !exists(trandt, eEndPoint))
			bResult = true;

		return bResult;
	}

}
