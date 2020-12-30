package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.parm.FaBranchAgent;
import cn.sunline.ltts.busi.fa.parm.FaBranchAgentMnt;
 /**
  * 账务代理机构信息维护
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFaBranchAgentImpl", longname="账务代理机构信息维护", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaBranchAgentImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaBranchAgent{
 /**
  * 新增一条账务代理机构信息
  *
  */
	public void addBranchAgent( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchInfo addIn){
		FaBranchAgentMnt.addBranchAgent(addIn);
	}
 /**
  * 维护一条账务代理机构信息
  *
  */
	public void modifyBranchAgent( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchInfo modifyIn){
		FaBranchAgentMnt.modifyBranchAgent(modifyIn);
	}
 /**
  * 删除一条账务代理机构信息
  *
  */
	public void deleteBranchAgent( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchInfo deletIn){
		FaBranchAgentMnt.deleteBranchAgent(deletIn.getAcct_branch(), deletIn.getRecdver());
	}
 /**
  * 查询账务代理机构信息列表
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchInfo> queryBranchAgentList( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchInfo quryIn){
		return FaBranchAgent.queryBranchAgentList(quryIn);
	}
}

