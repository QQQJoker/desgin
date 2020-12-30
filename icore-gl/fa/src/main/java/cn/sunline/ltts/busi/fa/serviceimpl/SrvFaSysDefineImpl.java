package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.parm.FaSysDefine;
import cn.sunline.ltts.busi.fa.parm.FaSysDefineMnt;
 /**
  * 系统定义维护
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFaSysDefineImpl", longname="系统定义维护", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaSysDefineImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaSysDefine{
 /**
  * 新增一条系统定义信息
  *
  */
	public void addSysDefine( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaSysDefineInfo addIn){
		FaSysDefineMnt.addSysDefine(addIn);
	}
 /**
  * 维护一条系统定义信息
  *
  */
	public void modifySysDefine( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaSysDefineInfo modifyIn){
		FaSysDefineMnt.modifySysDefine(modifyIn);
	}
 /**
  * 删除一条系统定义信息
  *
  */
	public void deleteSysDefine( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaSysDefineInfo deleteIn){
		FaSysDefineMnt.deleteSysDefine(deleteIn.getSys_no(), deleteIn.getRecdver());
	}
 /**
  * 获取指定系统定义对应的所有信息
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaParm.FaSysDefineInfo> getSysDefine( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaSysDefineInfo queryIn){
		return FaSysDefine.querySysDefineList(queryIn);
	}
}

