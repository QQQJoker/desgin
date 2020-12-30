package cn.sunline.ltts.busi.gl.serviceimpl;

import cn.sunline.ltts.busi.gl.item.GlBranch;
 /**
  * 科目余额
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvGlBranchSubjectBalImpl", longname="科目余额", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvGlBranchSubjectBalImpl implements cn.sunline.ltts.busi.gl.servicetype.SrvGlBranchSubjectBal{
 /**
  * 单条信息查询
  *
  */
	public cn.sunline.ltts.busi.gl.type.GlBranch.GlSubjectBalInfo getBalInfo( final cn.sunline.ltts.busi.gl.type.GlBranch.GlSubjectBalQueryIn queryIn){
		return GlBranch.getBalInfo(queryIn);
	}
 /**
  * 科目余额信息列表查询
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.gl.type.GlBranch.GlSubjectBalInfo> queryBalInfo( final cn.sunline.ltts.busi.gl.type.GlBranch.GlSubjectBalQueryIn queryIn){
		return GlBranch.queryBalInfo(queryIn);
	}
}

