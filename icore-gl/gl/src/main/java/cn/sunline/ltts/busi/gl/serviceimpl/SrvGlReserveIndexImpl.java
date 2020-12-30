package cn.sunline.ltts.busi.gl.serviceimpl;

import cn.sunline.ltts.busi.gl.parm.GlReserveIndex;
import cn.sunline.ltts.busi.gl.parm.GlReserveIndexMnt;
 /**
  * 准备金缴存指标维护
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvGlReserveIndexImpl", longname="准备金缴存指标维护", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvGlReserveIndexImpl implements cn.sunline.ltts.busi.gl.servicetype.SrvGlReserveIndex{
 /**
  * 增加一条缴存指标信息
  *
  */
	public void addReserveIndex( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveIndexInfo addIn){
		GlReserveIndexMnt.addReserveIndex(addIn);
	}
 /**
  * 维护一条缴存指标信息
  *
  */
	public void mntReserveIndex( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveIndexInfo modifyIn){
		GlReserveIndexMnt.mntReserveIndex(modifyIn);
	}
 /**
  * 删除指定记录
  *
  */
	public void delReserveIndex( String ccy_code,  String subject_no,  cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE reserve_type,  Long recdver){
		GlReserveIndexMnt.delReserveIndex(ccy_code, subject_no, reserve_type, recdver);
	}
 /**
  * 查询缴存指标明细
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.gl.type.GlParm.GlReserveIndexInfo> queryGlReserveIndexInfo( final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveIndexInfo queryIn){
		return GlReserveIndex.queryGlReserveIndexInfo(queryIn);
	}
}

