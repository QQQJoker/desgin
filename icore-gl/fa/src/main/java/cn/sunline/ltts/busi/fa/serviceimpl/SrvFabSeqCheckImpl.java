package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.accounting.FaAccountSeq;
 /**
  * 总账流水查询相关服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFabSeqCheckImpl", longname="总账流水相关服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFabSeqCheckImpl implements cn.sunline.ltts.busi.gl.fa.servicetype.SrvFabSeqCheck{
 /**
  * 总账柜员流水查询
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctingSeqOut> queryFabSeqCheck( final cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctingSeqIn queryIn,  final cn.sunline.ltts.busi.gl.fa.servicetype.SrvFabSeqCheck.queryFabSeqCheck.Property property){

		return FaAccountSeq.queryFabSeq(queryIn);
		
		
	}
}

