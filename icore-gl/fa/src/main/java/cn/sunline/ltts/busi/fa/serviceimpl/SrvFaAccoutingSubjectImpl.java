package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.parm.FaAccountingSubject;
import cn.sunline.ltts.busi.fa.parm.FaAccountingSubjectMnt;
 /**
  * 科目信息维护
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFaAccoutingSubjectImpl", longname="科目信息维护", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaAccoutingSubjectImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaAccoutingSubject{
 /**
  * 新增一条或多条科目信息
  *
  */
	public void addSubjectList( final cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaParm.FaSubjectInfo> addIn){
		FaAccountingSubjectMnt.addSubjectList(addIn);
	}
 /**
  * 维护一条或多条科目信息
  *
  */
	public void modifySubjectList( final cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaParm.FaSubjectInfo> modifyIn){
		FaAccountingSubjectMnt.modifySubjectList(modifyIn);
	}
 /**
  * 删除一条或多条科目信息
  *
  */
	public void deleteSubjectList( final cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaParm.FaSubjectInfo> deleteIn){
		FaAccountingSubjectMnt.deleteSubjectList(deleteIn);
	}
 /**
  * 获取指定科目号对应的所有信息
  *
  */
	public cn.sunline.ltts.busi.fa.type.ComFaParm.FaSubjectInfo getSubjectInfo( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaSubjectInfo getInfo){
		return FaAccountingSubject.getSubjectInfo(getInfo.getGl_code());
	}
 /**
  * 查询科目信息明细
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaParm.FaSubjectInfo> querySubjectList( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaSubjectInfo queryIn){
		return FaAccountingSubject.querySubjectList(queryIn);
	}
}

