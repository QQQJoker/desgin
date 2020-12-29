package cn.sunline.ltts.busi.aplt.coderule;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_DYEDCT;

/**
 * <p>
 * 文件功能说明： DCN相关逻辑
 * </p>
 * 
 * @Author Administrator
 *         <p>
 *         <li>2015年2月10日-上午9:20:10</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228Administrator：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class ApDCN {

	private static final BizLog bizlog = BizLogUtil.getBizLog(ApDCN.class);

//	// 获取指定DCN的日期
//	public static ApDcnDate getDcnDate(String sDcnbianh) {
//
//		// 从堆栈中取
//		int i = findDcnIndex(sDcnbianh);
//
//		if (i == -1) {
//
//			bizlog.debug("当前DCN号 = [%s]", sDcnbianh);
//
//			List<ApDcnDate> lstDcnDate = CommTools.prcRunEnvs().getDcndate();
//
//			for (int j = 0; j < lstDcnDate.size(); j++) {
//				bizlog.debug("堆栈中DCN(%s) = [%s]", j, lstDcnDate.get(j).getDcn_no());
//			}
//
//			throw ApError.Sys.E0001("DCN编号[" + sDcnbianh + "]未在Dcn堆栈中登记");
//		}
//
//		return CommTools.prcRunEnvs().getDcndate().get(i);
//
//	}
//
//	// 将DCN日期设置到堆栈
//	public static void setDcnDate(String sDcnbianh) {
//
//		int i = findDcnIndex(sDcnbianh);
//
//		// 不存在，则设置
//		if (i == -1) {
//			ApDcnDate cplDcnDate = SysUtil.getInstance(ApDcnDate.class);
//
//			// 主调DCN号
//			String sZddcnhao = CommTools.prcRunEnvs().getMn_dcn();
//
//			if (CommUtil.equals(sZddcnhao, sDcnbianh)) {
//
//				cplDcnDate.setDcn_no(sDcnbianh);
//
//				cplDcnDate.setTrandt(CommTools.prcRunEnvs().getTrandt());
//				cplDcnDate.setLstrdt(CommTools.prcRunEnvs().getLstrdt());
//				cplDcnDate.setNxtrdt(CommTools.prcRunEnvs().getNxtrdt());
//				cplDcnDate.setTrandt(CommTools.prcRunEnvs().getTrandt());
//				cplDcnDate.setLstrdt(CommTools.prcRunEnvs().getLstrdt());
//				cplDcnDate.setNxtrdt(CommTools.prcRunEnvs().getNxtrdt());
//
//			}
//			else {
//				ApSysDateStru cplDate = DateTools.getDateInfo();
//
//				// 设置交易系统日期
//				cplDcnDate.setDcn_no(sDcnbianh);
//
//				cplDcnDate.setTrandt(cplDate.getSystdt());
//				cplDcnDate.setLstrdt(cplDate.getLastdt());
//				cplDcnDate.setNxtrdt(cplDate.getNextdt());
//
//				// 设置总账日期
//				cplDate = DateTools.getGlDateInfo();
//
//				cplDcnDate.setGldate(cplDate.getSystdt());
//				cplDcnDate.setLsgldt(cplDate.getLastdt());
//				cplDcnDate.setNxgldt(cplDate.getNextdt());
//
//			}
//
//			CommTools.prcRunEnvs().getDcndate().add(cplDcnDate);
//
//		}
//	}
//
//	// 在堆栈中查找DCN序号
//	public static int findDcnIndex(String sDcnbianh) {
//
//		List<ApDcnDate> lstDcnDate = CommTools.prcRunEnvs().getDcndate();
//
//		if (CommUtil.isNotNull(lstDcnDate)) {
//
//			for (int i = 0; i < lstDcnDate.size(); i++) {
//
//				if (CommUtil.equals(lstDcnDate.get(i).getDcn_no(), sDcnbianh)) {
//					return i;
//				}
//			}
//		}
//
//		// 未找到
//		return -1;
//	}
//
	/**
	 * @Author cuijia
	 *         <p>
	 *         <li>2014年12月19日-下午4:03:34</li>
	 *         <li>获得会计流水应入账日期</li>
	 *         </p>
	 * @param acctno
	 *            负债账号、内部账号、现金台账号、凭证台账号
	 * @param trandt
	 *            交易日期
	 * @param lastdt
	 *            余额更新日期（过上日余额的配套日期）
	 * @return
	 */
	public static String getAccountDate(String acctno, String trandt, String lastdt) {

		bizlog.parm("获得会计流水应入账日期: 账号[%s], 交易日期[%s], 账户最后余额更新日期 [%s]", acctno, trandt, lastdt);

		String acctdt = trandt; // 应入账日期
		// 若单个账户已日切，则取余额更新日期作为应入账日期（过账后发生上日账务）
		if (CommUtil.compare(lastdt, trandt) > 0) {
			int iDays = DateTimeUtil.dateDiff("dd", trandt, lastdt);
			if (iDays > 1) {
				throw Aplt.E0000("错误的日期逻辑，交易日期[" + trandt + "]，账号[" + acctno + "]最后余额更新日期[" + lastdt + "]两者日期差超过一天");
			}
			acctdt = lastdt;
		}
		else {
			// 上送系统日期为主调节点DCN的系统日期
			String mainDcnDate = CommTools.prcRunEnvs().getInpudt();
			// 当前DCN的日期 大于 主交易日期
			if (CommUtil.compare(trandt,mainDcnDate) > 0) {
				// 判断当前DCN是否开始计息
				// 已经开始计息, 则以当前DCN的系统日期做为应入账日期
				if (ApDayendPoint.exists(trandt, E_DYEDCT.JIXIKS)) {
					acctdt = trandt;
				}
			}
		}

		bizlog.parm("账号[%s], 应入账日期 [%s]", acctno, acctdt);

		return acctdt;
	}
	
	// 单DCN获取实际入账日期
		public static String getAccountDateOneDCN(String acctno, String trandt,
				String upbldt) {
			{

				String acctdt = trandt; // 应入账日期[默认为交易日期]
	//
//				// 日终跑批应入账日期就是交易日期
//				if (CommUtil.equals(CommTools.prcRunEnvs().getServtp(),
//						ApUtil.DP_DAYEND_CHANNEL))
//					return acctdt;
	//
//				// 若单个账户已过账
				if (CommUtil.compare(upbldt, trandt) > 0) {

					int iDays = DateTimeUtil.dateDiff("dd", trandt, upbldt);
					ApSysDateStru cplDateStru = DateTools.getDateInfo();
					String sysdat = cplDateStru.getSystdt(); //当前交易日期
					String lstrdt = cplDateStru.getLastdt(); //上次交易日期
					//计算当前系统日期与上日日期之间人天数差
					int diffDays = DateTimeUtil.dateDiff("dd", lstrdt, sysdat);
					
					if (iDays > diffDays) {
						throw Aplt.E0000("错误的日期逻辑，交易日期[" + trandt + "]，账号["
								+ acctno + "]最后余额更新日期[" + upbldt + "]两者日期差超过一天");
					}

//					acctdt = upbldt;
				}

				
				return acctdt;

			}
		}
	
	/**
	 * 获取本DCN编号。
	 * 
	 * @return 本DCN编号
	 */
	public static String getMyDcnNo() {
//		return DMBUtil.getIntanse().getConfiguredDcnNo();
		return CommTools.getMySysId();
	}
}
