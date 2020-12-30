package cn.sunline.ltts.busi.gl.serviceimpl;

import cn.sunline.ltts.busi.gl.parm.GlReserveCycle;
import cn.sunline.ltts.busi.gl.parm.GlReserveCycleMnt;
 /**
  * 准备金缴存周期维护
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvGlReserveCycleImpl", longname="准备金缴存周期维护", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvGlReserveCycleImpl implements cn.sunline.ltts.busi.gl.servicetype.SrvGlReserveCycle{
 /**
  * 增加一条缴存周期的信息
  *
  */
	public void addReserveCycle( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveCycleInfo addIn){
		GlReserveCycleMnt.addReserveCycle(addIn);
	}
 /**
  * 维护一条缴存周期信息
  *
  */
	public void mntReserveCycle( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveCycleInfo modifyIn){
		GlReserveCycleMnt.mntReserveCycle(modifyIn);
	}
 /**
  * 删除一条缴存周期信息
  *
  */
	public void delReserveCycle( String ccy_code,  Long recdver){
		GlReserveCycleMnt.delReserveCycle(ccy_code, recdver);
	}
 /**
  * 查询缴存周期明细列表
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.gl.type.GlParm.GlReserveCycleInfo> queryReserveCycleInfo( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveCycleInfo queryIn){
		return GlReserveCycle.queryReserveCycleInfo(queryIn);
		
	}
}

