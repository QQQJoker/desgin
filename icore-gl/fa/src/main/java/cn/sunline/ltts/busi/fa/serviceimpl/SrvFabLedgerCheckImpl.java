package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.ledgercheck.LedgerCheckQuery;
 /**
  * 总账核算相关服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFabLedgerCheckImpl", longname="总账核算相关服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFabLedgerCheckImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFabLedgerCheck{
 /**
  * 总账核算信息查询
  *
  */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLedgerCheckOut> queryFabLedgerCheck(final cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaLedgerCheckIn queryIn){
		return LedgerCheckQuery.QueryLedgerCheckInfo(queryIn);
		
	}
}

