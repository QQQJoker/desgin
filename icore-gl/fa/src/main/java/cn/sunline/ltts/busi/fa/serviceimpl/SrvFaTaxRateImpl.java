
package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.fa.parm.FaTaxRateMnt;

/**
  * 税率维护实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFaTaxRateImpl", longname="税率维护实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaTaxRateImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaTaxRate{
 /**
  * 增加税率
  *
  */
	public String addTaxRate(final Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_tax_rate> addIn){
		return FaTaxRateMnt.addTaxRate(addIn);
	}
 /**
  * 维护税率
  *
  */
	public void mntTaxRate(final Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_tax_rate> modifyIn){
		FaTaxRateMnt.mntTaxRate(modifyIn);
	}
 /**
  * 删除一条记录
  *
  */
	public void delTaxRate(final cn.sunline.ltts.busi.fa.servicetype.SrvFaTaxRate.delTaxRate.Input input){
		FaTaxRateMnt.delTaxRate(input);
	}
 /**
  * 查询税率集
  *
  */
	public void queryTaxRateList(final cn.sunline.ltts.busi.fa.servicetype.SrvFaTaxRate.queryTaxRateList.Input input, final cn.sunline.ltts.busi.fa.servicetype.SrvFaTaxRate.queryTaxRateList.Output output){
		FaTaxRateMnt.queryTaxRateList(input, output);
	}

}

