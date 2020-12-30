package cn.sunline.ltts.busi.gl.serviceimpl;


import cn.sunline.ltts.busi.gl.exchangeb.Exchange;
import cn.sunline.ltts.busi.gl.type.GlExchange.GlExchangeRate;
import cn.sunline.ltts.busi.gl.type.GlExchange.GlExchangeRateIn;
import cn.sunline.ltts.busi.gl.type.GlExchange.GlExchangeRateInfo;
import cn.sunline.edsp.base.lang.Options;
 /**
  * 折算汇率相关服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvGlExchangeBImpl", longname="折算汇率相关服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvGlExchangeBImpl implements cn.sunline.ltts.busi.gl.servicetype.SrvExchangB{
 /**
  * 添加一条折算汇率信息
  *
  */
	@Override
	public void addGlExchangeRate(GlExchangeRate addIn) {
		// TODO Auto-generated method stub
		Exchange.addExchangeRateInfo(addIn);
	}
 /**
  * 折算汇率相关信息维护
  *
  */
	@Override
	public Options<GlExchangeRate> selExchangeRate(Options<GlExchangeRate> queryIn) {
		// TODO Auto-generated method stub
		return Exchange.getExchangeRateList(queryIn);
	}

	@Override
	public Options<GlExchangeRateInfo> selExchageRateInfo(GlExchangeRateIn queryIn) {
		// TODO Auto-generated method stub
		return Exchange.getExchangeRateInfo(queryIn);
	}
}





