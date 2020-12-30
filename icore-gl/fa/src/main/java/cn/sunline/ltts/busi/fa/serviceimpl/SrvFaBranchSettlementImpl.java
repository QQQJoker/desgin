package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.parm.FaBranchSettlement;
import cn.sunline.ltts.busi.fa.parm.FaBranchSettlementMnt;
 /**
  * 清算关系组织结构维护
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFaBranchSettlementImpl", longname="清算关系组织结构维护", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaBranchSettlementImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaBranchSettlement{
 /**
  * 新增一条清算关系组织结构信息
  *
  */
	public void addBranchSettlement( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchSettlementInfo addIn){
		FaBranchSettlementMnt.addBranchSettlement(addIn);	
	}
 /**
  * 维护一条清算关系组织结构信息
  *
  */
	public void modifyBranchSettlement( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchSettlementInfo modifyIn){
		FaBranchSettlementMnt.modifyBranchSettlement(modifyIn);
	}
 /**
  * 删除一条清算关系组织结构信息
  *
  */
	public void deleteBranchAgent( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchSettlementInfo deleteIn){
		FaBranchSettlementMnt.deleteBranchSettlement(deleteIn);
	}
 /**
  * 获取指定清算关系组织结构对应的所有信息
  *
  */
	public cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchSettlementInfo getBranchSettement( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchSettlementInfo getInfo){
		return FaBranchSettlement.getBranchSettlement(getInfo.getAcct_branch(), getInfo.getCcy_code());
	}
 /**
  * 查询清算关系组织结构信息列表
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchSettlementInfo> queryBranchSettementList( final cn.sunline.ltts.busi.fa.type.ComFaParm.FaBranchSettlementInfo queryIn){
		return FaBranchSettlement.queryBranchSettlementList(queryIn);
	}
}

