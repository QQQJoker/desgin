package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.original.FaQueryOriginal;
import cn.sunline.ltts.busi.fa.type.ComFaOriginalVoch.FaQueryOriginalVochListOut;
import cn.sunline.edsp.base.lang.Options;
 /**
  * 外系统维护实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFaOriginalVochImpl", longname="外系统维护实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaOriginalVochImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaOriginal{
 /**
  * 查询外系统明细
  *
  */
	public Options<FaQueryOriginalVochListOut> queryOriginalVochList(final cn.sunline.ltts.busi.fa.type.ComFaOriginalVoch.FaQueryOrigVochListIn queryIn){
		return FaQueryOriginal.queryOriginalList(queryIn);
		
	}
}

