package cn.sunline.ltts.busi.gl.serviceimpl;

import cn.sunline.ltts.busi.gl.parm.GlReserveAppoint;
import cn.sunline.ltts.busi.gl.parm.GlReserveAppointMnt;
 /**
  * 指定日期缴存维护
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvGlReserveAppointImpl", longname="指定日期缴存维护", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvGlReserveAppointImpl implements cn.sunline.ltts.busi.gl.servicetype.SrvGlReserveAppoint{
 /**
  * 增加一条数据
  *
  */
	public void addlGlReserveAppointInfo( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveAppointInfo addIn){
		GlReserveAppointMnt.addlGlReserveAppointInfo(addIn);
	}
 /**
  * 删除指定记录
  *
  */
	public void delGlReserveAppointInfo( String appoint_date,  String ccy_code,  Long recdver){
		GlReserveAppointMnt.delGlReserveAppointInfo(appoint_date, ccy_code, recdver);
	}
 /**
  * 查询指定日期缴存明细列表
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.gl.type.GlParm.GlReserveAppointInfo> queryGlReserveAppointInfo( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveAppointInfo queryIn){
		return GlReserveAppoint.queryGlReserveAppointInfo( queryIn );
		
	}
}

