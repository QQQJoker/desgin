package cn.sunline.ltts.busi.aplt.coderule;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnbEodt;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnbEodtDao;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BCSTEP;

public class ApBatchTrialRegist {


	/**
	 * @Author yuhch
	 *         <p>
	 *         <li>2015年2月7日-下午2:13:35</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param sChckdt
	 *            核对日期
	 * @param sBctrds
	 *            批量交易状态描述
	 * @param eBcstep
	 *            批量交易步骤标志
	 * @param eBcstep
	 *            批量交易批次号
	 */
	public static void registGLBatchTrial(String sChckdt, String sBctrds, E_BCSTEP eBcstep, String sBtchno) {

		KnbEodt tblPlgjrz = SysUtil.getInstance(KnbEodt.class);
		
		tblPlgjrz.setSequnm(Long.parseLong(SysUtil.nextValue("KnbEodt_seq")));
		
		tblPlgjrz.setTrandt(CommToolsAplt.prcRunEnvs().getGldate());
		
		tblPlgjrz.setBcstep(eBcstep);
		tblPlgjrz.setBtchno(sBtchno);
		tblPlgjrz.setBctrds(sBctrds);
		tblPlgjrz.setChckdt(sChckdt);
		
		KnbEodtDao.insert(tblPlgjrz);
		
	}
	
}
