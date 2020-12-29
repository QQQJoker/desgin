package cn.sunline.ltts.busi.aplt.serviceimpl;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpSvcx;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpSvcxDao;
import cn.sunline.ltts.busi.iobus.type.IoApPubComplex.KnpSvcxCt;

/**
 * 应用基础平台通用服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoApPubServiceImpl", longname = "应用基础平台通用服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoApPubServiceImpl implements cn.sunline.ltts.busi.iobus.servicetype.IoApPubService {
	/**
	 * 查询服务实现定义
	 * 
	 */
	public KnpSvcxCt getKnpSvcxByKey(String svtpid, String svimky) {

		KnpSvcx svcx = KnpSvcxDao.selectOne_odb1(svtpid, svimky, false);
		KnpSvcxCt svcxct = SysUtil.getInstance(KnpSvcxCt.class);
		svcxct.setSvtpid(svcx.getSvtpid());
		svcxct.setSvimky(svcx.getSvimky());
		svcxct.setSvimid(svcx.getSvimid());
		svcxct.setRemark(svcx.getRemark());
		return svcxct;
	}
}
