package cn.sunline.ltts.busi.fa.parm;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_sys_defineDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_sys_define;
import cn.sunline.ltts.busi.fa.type.ComFaLoanAccounting.FaLoanAccountingEventInfo;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaLoanAccountingEvent {

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月15日-上午10:59:36</li>
	 *         <li>功能说明：贷款会计分录解析调试输入非空校验</li>
	 *         </p>
	 * @param analysisIn
	 */
	public static void checkNull(FaLoanAccountingEventInfo analysisIn) {
		CommTools.fieldNotNull(analysisIn.getProduct_code(), GlDict.A.product_code.getId(), GlDict.A.product_code.getLongName()); // 产品编号
		CommTools.fieldNotNull(analysisIn.getEvent_code(), GlDict.A.event_code.getId(), GlDict.A.event_code.getLongName()); // 交易编号
		CommTools.fieldNotNull(analysisIn.getAccount_status(), GlDict.A.account_status.getId(), GlDict.A.account_status.getLongName()); // 贷款账户状态
		CommTools.fieldNotNull(analysisIn.getTax_separate_flag(), GlDict.A.tax_separate_flag.getId(), GlDict.A.tax_separate_flag.getLongName()); // 会计核算分离标志
		CommTools.fieldNotNull(analysisIn.getCurrency_code(), GlDict.A.currency_code.getId(), GlDict.A.currency_code.getLongName()); // 币种
		CommTools.fieldNotNull(analysisIn.getTran_amount(), GlDict.A.tran_amount.getId(), GlDict.A.tran_amount.getLongName());  // 交易金额
		if(CommUtil.compare(analysisIn.getTran_amount(), BigDecimal.ZERO) <= 0) {
			throw GlError.GL.E0041();
		}
	}
	
	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月15日-下午4:40:17</li>
	 *         <li>功能说明：校验系统编号合法性</li>
	 *         </p>
	 * @param systcd
	 */
	public static void existsFapSysDefine(String systcd) {
		fap_sys_define fapdef = Fap_sys_defineDao.selectOne_odb1(systcd, false);
		if(CommUtil.isNull(fapdef)){
			throw GlError.GL.E0200(systcd);
		}
	}
	
}
