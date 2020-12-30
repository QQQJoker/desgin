
package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.fa.parm.FaLoanAccountingEventMnt;
import cn.sunline.ltts.busi.fa.type.ComFaLoanAccounting.FaLoanAccountingEventInfo;
import cn.sunline.ltts.busi.fa.type.ComFaLoanAccounting.FaLoanAccountingEventResult;

/**
  * 贷款会计核算服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="SrvFaLoanAccountingEventImpl", longname="贷款会计核算服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaLoanAccountingEventImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaLoanAccountingEvent{
 /**
  * 增加贷款会计事件核算定义
  *
  */
	public void addLoanAccountingEvent( final Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_rule> addIn){
		FaLoanAccountingEventMnt.addLoanAccountingEvent(addIn);
	}
 /**
  * 维护贷款会计事件核算定义
  *
  */
	public void mntLoanAccountingEvent(final Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_rule> modifyIn){
		FaLoanAccountingEventMnt.mntLoanAccountingEvent(modifyIn);
	}
 /**
  * 删除贷款核算事件定义
  *
  */
	public void delLoanAccountingEvent(final Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_rule> deleteIn){
		FaLoanAccountingEventMnt.delLoanAccountingEvent(deleteIn);
	}
 /**
  * 查询贷款会计事件定义
  *
  */
	public Options<cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_rule> queryLoanEvent( final cn.sunline.ltts.busi.fa.type.ComFaLoanAccounting.FaLoanFapAccountingprodruleqry queryIn){
		return FaLoanAccountingEventMnt.queryLoanEvent(queryIn);
	}
 /**
  * 贷款会计事件解析调试
  *
  */
	@Override
	public Options<FaLoanAccountingEventResult> checkAnalysisLoanEvent(Options<FaLoanAccountingEventInfo> analysisIn) {
		return FaLoanAccountingEventMnt.checkAnalysisLoanEvents(analysisIn);
	}

}

