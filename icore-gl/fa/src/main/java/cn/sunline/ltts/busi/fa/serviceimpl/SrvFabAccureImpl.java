package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.fabaccure.FabAccure;
 /**
  * 计提登记簿相关服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFabAccureImpl", longname="计提登记簿相关服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFabAccureImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFabAccure{
 /**
  * 计提登记簿信息查询服务
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFabAccrue.FabAccureQueryOut> FabAccureQueryList(final cn.sunline.ltts.busi.fa.type.ComFabAccrue.FabAccureQueryIn queryIn){
		return FabAccure.queryFabAccu(queryIn);
		
	}
}

