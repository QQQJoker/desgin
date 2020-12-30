package cn.sunline.ltts.busi.gl.serviceimpl;

import cn.sunline.ltts.busi.gl.parm.GlReserveQuery;
 /**
  * 缴存登记薄查询
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvGlReserveQueryImpl", longname="缴存登记薄查询", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvGlReserveQueryImpl implements cn.sunline.ltts.busi.gl.servicetype.SrvGlReserveQuery{
 /**
  * 查询
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.gl.type.GlParm.GlReserveQueryOut> reserveQuery( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveQueryIn queryIn){
		return GlReserveQuery.reserveQuery(queryIn);
	}
}

