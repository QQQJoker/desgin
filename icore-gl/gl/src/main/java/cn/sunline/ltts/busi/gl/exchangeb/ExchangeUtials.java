package cn.sunline.ltts.busi.gl.exchangeb;

import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.glb_exchange_rate;
import cn.sunline.ltts.busi.gl.type.GlExchange.GlExchangeRate;
import cn.sunline.ltts.sys.dict.GlDict;

/**
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author pc
 *         <p>
 *         <li>2017年5月2日-下午6:57:58</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228  pc：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class ExchangeUtials {

	/**
	 * @Author pc
	 *         <p>
	 *         <li>2017年5月2日-下午6:58:54</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param addIn
	 */
	public static void checkBusinessValidity(GlExchangeRate addIn) {
		//判断货币代码是否为空
		CommTools.fieldNotNull(addIn.getCcy_code(), GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		//判断折算汇率是否为空
		CommTools.fieldNotNull(addIn.getExchange_rate(), GlDict.A.exchange_rate.getId(), GlDict.A.exchange_rate.getLongName());
		//判断交易日期是否为空
		CommTools.fieldNotNull(addIn.getTrxn_date(),GlDict.A.trxn_date.getId(), GlDict.A.trxn_date.getLongName());
		//判断折合币种是否为空
		CommTools.fieldNotNull(addIn.getExchange_ccy_code(), GlDict.A.exchange_ccy_code.getId(), GlDict.A.exchange_ccy_code.getLongName());
		//判断折算单位是否为空
		CommTools.fieldNotNull(addIn.getExchange_rate_unit(), GlDict.A.exchange_rate_unit.getId(), GlDict.A.exchange_rate_unit.getLongName());
		
	}

	/**
	 * @Author pc
	 *         <p>
	 *         <li>2017年5月6日-上午10:16:50</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param tabExgRate
	 */
	public static void checkUpdateValidity(glb_exchange_rate tabExgRate) {
		//判断货币代码是否为空
		CommTools.fieldNotNull(tabExgRate.getCcy_code(),GlDict.A.ccy_code.getId(), GlDict.A.ccy_code.getLongName());
		//判断交易日期是否为空
		CommTools.fieldNotNull(tabExgRate.getTrxn_date(),GlDict.A.trxn_date.getId(), GlDict.A.trxn_date.getLongName());
		//判断折合币种是否为空
		CommTools.fieldNotNull(tabExgRate.getExchange_ccy_code(), GlDict.A.exchange_ccy_code.getId(), GlDict.A.exchange_ccy_code.getLongName());
		//判断交易日期是否为空
		CommTools.fieldNotNull(tabExgRate.getTrxn_date(),GlDict.A.trxn_date.getId(), GlDict.A.trxn_date.getLongName());
		//判断折算单位是否为空
		CommTools.fieldNotNull(tabExgRate.getExchange_rate_unit(), GlDict.A.exchange_rate_unit.getId(), GlDict.A.exchange_rate_unit.getLongName());				
	}

}
