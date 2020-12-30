package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.parm.FaAccountingEvent;
import cn.sunline.ltts.busi.fa.parm.FaAccountingEventMnt;

/**
 * 会计核算相关服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "SrvFaAccountingEventImpl", longname = "会计核算相关服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaAccountingEventImpl implements cn.sunline.ltts.busi.fa.servicetype.SrvFaAccountingEvent {
	/**
	 * 增加会计事件核算定义
	 * 
	 */
	public void addAccountingEvent(final cn.sunline.ltts.busi.fa.type.ComFaParm.FaAccountingEventInfo addIn) {
		FaAccountingEventMnt.addAccountingEvent(addIn);
	}

	/**
	 * 维护会计事件核算定义
	 * 
	 */
	public void mntAccountingEvent(final cn.sunline.ltts.busi.fa.type.ComFaParm.FaAccountingEventInfo modifyIn) {
		FaAccountingEventMnt.mntAccountingEvent(modifyIn);
	}

	/**
	 * 删除一条记录
	 * 
	 */
	public void delAccountingEvent(String sys_no, String accounting_alias, String bal_attributes, Long recdver) {
		FaAccountingEventMnt.delAccountingEvent(sys_no, accounting_alias, bal_attributes, recdver);
	}

	/**
	 * 批量查询会计事件定义
	 * 
	 */
	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaParm.FaAccountingEventInfo> queryEvent(final cn.sunline.ltts.busi.fa.type.ComFaParm.FaAccountingEventInfo queryIn) {

		return FaAccountingEvent.queryEvent(queryIn);

	}
}
