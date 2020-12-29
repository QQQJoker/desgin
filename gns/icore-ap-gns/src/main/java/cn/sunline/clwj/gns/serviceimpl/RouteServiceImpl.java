
package cn.sunline.clwj.gns.serviceimpl;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.gns.api.GnsApi;
import cn.sunline.ltts.gns.api.GnsKey;

/**
  * 路由映射服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="RouteServiceImpl", longname="路由映射服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class RouteServiceImpl implements cn.sunline.edsp.busi.icore.ap.gns.api.servicetype.RouteService{
 /**
  * 注册路由映射信息
  *
  */
	public void registRouteMappingInfo(String gnskey, cn.sunline.ltts.gns.type.ApGnsEnumType.E_GNSOPT gnsopt, String gnstyp, String gnschn, String gnsval){
		
		GnsApi gns = SysUtil.getInstance(GnsApi.class);
		
		GnsKey gnsKey = SysUtil.getInstance(GnsKey.class);
		gnsKey.setGnschn(gnschn);
		gnsKey.setGnskey(gnskey);
		gnsKey.setGnsopt(gnsopt.getValue());
		gnsKey.setGnstyp(gnstyp);
		gnsKey.setGnsval(gnsval);
			
		gns.add(gnsKey);
		
		
	}
 /**
  * 查询路由映射信息
  *
  */
	public String querRouteMappingInfo(String gnskey, cn.sunline.ltts.gns.type.ApGnsEnumType.E_GNSOPT gnsopt, String gnstyp, String gnschn){
		
        GnsApi gns = SysUtil.getInstance(GnsApi.class);
		
		GnsKey gnsKey = SysUtil.getInstance(GnsKey.class);
		gnsKey.setGnschn(gnschn);
		gnsKey.setGnskey(gnskey);
		gnsKey.setGnsopt(gnsopt.getValue());
		gnsKey.setGnstyp(gnstyp);
		
		gns.query(gnsKey);
		
		return gnschn;
	}
}

