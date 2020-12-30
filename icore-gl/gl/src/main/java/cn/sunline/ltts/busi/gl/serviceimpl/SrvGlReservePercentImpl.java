package cn.sunline.ltts.busi.gl.serviceimpl;

import cn.sunline.ltts.busi.gl.parm.GlReservePercent;
import cn.sunline.ltts.busi.gl.parm.GlReservePercentMnt;
 /**
  * 准备金缴存比率维护
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvGlReservePercentImpl", longname="准备金缴存比率维护", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvGlReservePercentImpl implements cn.sunline.ltts.busi.gl.servicetype.SrvGlReservePercent{
 /**
  * 增加准备金缴存比率信息
  *
  */
	public void addPercentInfo( final cn.sunline.ltts.busi.gl.type.GlParm.GlReservePercentInfo addIn){
		GlReservePercentMnt.addPercentInfo(addIn);
	}
 /**
  * 修改准备金缴存比率信息
  *
  */
	public void mntPercentInfo( final cn.sunline.ltts.busi.gl.type.GlParm.GlReservePercentInfo modifyIn){
		GlReservePercentMnt.mntPercentInfo(modifyIn);
	}
 /**
  * 删除一条准备金比率信息
  *
  */
	public void delPercentInfo( String branch_id,  String ccy_code,  cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE reserve_type,  Long recdver){
		GlReservePercentMnt.delPercentInfo(branch_id, ccy_code, reserve_type, recdver);
	}
 /**
  * 查询准备金比率明细信息
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.gl.type.GlParm.GlReservePercentInfo> queryReservePercentInfo( final cn.sunline.ltts.busi.gl.type.GlParm.GlReservePercentInfo queryIn){
		return GlReservePercent.queryReservePercentInfo(queryIn);
	}
}

