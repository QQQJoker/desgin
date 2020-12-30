package cn.sunline.ltts.busi.gl.serviceimpl;

import cn.sunline.ltts.busi.gl.parm.GlReserveLevelDefine;
import cn.sunline.ltts.busi.gl.parm.GlReserveLevelDefineMnt;
 /**
  * 准备金缴存层级定义维护
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvGlReserveLevelDefineImpl", longname="准备金缴存层级定义维护", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvGlReserveLevelDefineImpl implements cn.sunline.ltts.busi.gl.servicetype.SrvGlReserveLevelDefine{
 /**
  * 增加一条指定信息
  *
  */
	public void addReserveLevelDefine( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveLevelInfo addIn){
		GlReserveLevelDefineMnt.addReserveLevelDefine(addIn);
	}
 /**
  * 维护指定信息
  *
  */
	public void mntReserveLevelDefine( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveLevelInfo modifyIn){
		GlReserveLevelDefineMnt.mntReserveLevelDefine(modifyIn);
	}
 /**
  * 删除指定记录
  *
  */
	public void delReserveLevelDefine( Long deposit_paid_level,  cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE reserve_type,  cn.sunline.ltts.busi.sys.type.GlBusinessType.E_DEPOSITPAIDBUSIPROP deposit_paid_busi_prop,  Long recdver){
		GlReserveLevelDefineMnt.delReserveLevelDefine(deposit_paid_level, reserve_type, deposit_paid_busi_prop, recdver);
	}
 /**
  * 查询明细列表
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.gl.type.GlParm.GlReserveLevelInfo> queryGlReserveLevelInfo( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveLevelInfo queryIn){
		return GlReserveLevelDefine.queryGlReserveLevelInfo(queryIn);
	}
}

