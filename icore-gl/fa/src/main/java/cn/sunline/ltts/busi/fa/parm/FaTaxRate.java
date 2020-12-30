package cn.sunline.ltts.busi.fa.parm;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.servicetype.SrvFaTaxRate.delTaxRate.Input;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_tax_rate;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaTaxRate {

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月18日-下午5:14:27</li>
	 *         <li>功能说明：对税率信息进行基本校验</li>
	 *         </p>
	 * @param rate
	 */
	public static void checkRateNull(fap_tax_rate rate) {
		
		// 检查不能为空的值
		CommTools.fieldNotNull(rate.getBranch_code(), GlDict.A.branch_code.getId(), GlDict.A.branch_code.getLongName());
		CommTools.fieldNotNull(rate.getBusiness_code(), GlDict.A.business_code.getId(), GlDict.A.business_code.getLongName());
		CommTools.fieldNotNull(rate.getCal_tax_method_code(), GlDict.A.cal_tax_method_code.getId(), GlDict.A.cal_tax_method_code.getLongName());
		CommTools.fieldNotNull(rate.getEffect_date(), GlDict.A.effect_date.getId(), GlDict.A.effect_date.getLongName());
		CommTools.fieldNotNull(rate.getInvalid_date(), GlDict.A.invalid_date.getId(), GlDict.A.invalid_date.getLongName());
		CommTools.fieldNotNull(rate.getTax_rate(), GlDict.A.tax_rate.getId(), GlDict.A.tax_rate.getLongName());
		if(CommUtil.compare(rate.getTax_rate(), BigDecimal.ZERO) <= 0) {
			throw GlError.GL.E0220();
		}
		
		// 基本日期校验
		String trandt = CommTools.prcRunEnvs().getTrandt();
		if(CommUtil.compare(rate.getEffect_date(), trandt) < 0) {
			throw GlError.GL.E0221(rate.getEffect_date(), trandt);
		}
		if(CommUtil.compare(rate.getInvalid_date(), rate.getInvalid_date()) < 0) {
			throw GlError.GL.E0222(rate.getEffect_date(), rate.getInvalid_date());
		}
		
	}

	// 基本要素非空验证
	public static void checkBaseNull(Input input) {
		CommTools.fieldNotNull(input.getBranch_code(), GlDict.A.branch_code.getId(), GlDict.A.branch_code.getLongName());
		CommTools.fieldNotNull(input.getBusiness_code(), GlDict.A.business_code.getId(), GlDict.A.business_code.getLongName());
		CommTools.fieldNotNull(input.getCal_tax_method_code(), GlDict.A.cal_tax_method_code.getId(), GlDict.A.cal_tax_method_code.getLongName());
		CommTools.fieldNotNull(input.getEffect_date(), GlDict.A.effect_date.getId(), GlDict.A.effect_date.getLongName());
	}
	
	
	
	
	
}
