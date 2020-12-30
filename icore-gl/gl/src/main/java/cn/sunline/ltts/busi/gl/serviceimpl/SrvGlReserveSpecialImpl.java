package cn.sunline.ltts.busi.gl.serviceimpl;

import cn.sunline.ltts.busi.gl.parm.GlReserveSpecial;
import cn.sunline.ltts.busi.gl.type.GlParm.GlReserveSpecialInfo;
import cn.sunline.edsp.base.lang.Options;

/**
 * 特殊缴存信息维护
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "SrvGlReserveSpecialImpl", longname = "特殊缴存信息维护", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvGlReserveSpecialImpl implements cn.sunline.ltts.busi.gl.servicetype.SrvGlReserveSpecial {
	/**
	 * 增加一条特殊对外缴存定义
	 * 
	 */
	public void addGlReserveSpecialInfo(final cn.sunline.ltts.busi.gl.type.GlParm.GlReserveSpecialInfo addIn) {
		GlReserveSpecial.addGlReserveSpecialInfo(addIn);
	}

	/**
	 * 删除一条指定记录
	 * 
	 */
	public void delGlReserveSpecialInfo(String branchId, String ccyCode, cn.sunline.ltts.busi.sys.type.GlBusinessType.E_RESERVEAMTTYPE reserveType, Long recdver) {
		GlReserveSpecial.delGlReserveSpecialInfo(branchId,ccyCode,reserveType,recdver);
	}

	/**
	 * 查询特殊对外缴存定义信息明细列表
	 * 
	 */
	public Options<GlReserveSpecialInfo> queryGlReserveSpecialInfo(final GlReserveSpecialInfo queryIn) {
		return GlReserveSpecial.queryGlReserveSpecialInfo(queryIn);
	}
}
