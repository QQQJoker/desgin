
package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.fa.parm.FaLnAccountingEventMnt;
import cn.sunline.ltts.busi.fa.servicetype.SrvFaLnAccountingEvent.analysisLnAccountingEvent.Output;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnFapAccountingprodSceneInfo;

/**
  * 贷款会计核算服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFaLnAccountingEventImpl", longname="贷款会计核算服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaLnAccountingEventImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaLnAccountingEvent{
 /**
  * 增加贷款会计事件核算定义
  *
  */
	public void addLnAccountingEvent(final Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_scene> addIn){
		FaLnAccountingEventMnt.addLnAccountingEvent(addIn);
	}
 /**
  * 维护场景核算事件定义
  *
  */
	public void mntLnAccountingEvent(final Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_scene> modifyIn){
		FaLnAccountingEventMnt.mntLnAccountingEvent(modifyIn);
	}
 /**
  * 删除场景核算事件定义
  *
  */
	public void delLnAccountingEvent(final Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_scene> deleteIn){
		FaLnAccountingEventMnt.delLnAccountingEvent(deleteIn);
	}
 /**
  * 查询场景会计事件定义
  *
  */
	public Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_scene> queryLnAccountingEvent(FaLnFapAccountingprodSceneInfo queryIn){
		return FaLnAccountingEventMnt.queryLoanEvent(queryIn);
	}
 /**
  * 场景核算事件解析调试
  *
  */
	public Output analysisLnAccountingEvent( final Options<cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneInfo> analysisIn){
		return FaLnAccountingEventMnt.analysisLoanEvents(analysisIn);
	}
	
}

