package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.acctingvoch.FaAcctVoch;
import cn.sunline.ltts.busi.fa.type.ComFaAcctingVoch.FaQueryAcctingVochIn;
import cn.sunline.ltts.busi.fa.type.ComFaAcctingVoch.FaQueryAcctingVochOut;
import cn.sunline.edsp.base.lang.Options;
 /**
  * 总账凭证相关服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFaAcctingVochImpl", longname="总账凭证相关服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaAcctingVochImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaAcctingVoch{
 /**
  * 总账凭证查询服务
  *
  */
@Override
public Options<FaQueryAcctingVochOut> FaQueryAcctingVoch(FaQueryAcctingVochIn queryIn) {
	// TODO Auto-generated method stub
	return FaAcctVoch.queryAcctVoch(queryIn);
}
}

