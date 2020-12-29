package cn.sunline.ltts.busi.aplt.transaction;

import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;

/**
 * 实现类规则：ApConfirmEvntProcessor+交易事件,如：ApConfirmEvntProcessorOCAL-外调二次提交
 */
public interface ApConfirmEvntProcessor {
	
	/**
	 * 事件接口
	 * @param evnt 业务事件
	 */
	public void process(KnbEvnt evnt);

}
