package cn.sunline.ltts.busi.gl.fatran.trans.accountingevent;

import cn.sunline.ltts.busi.fa.parm.FaAccountingEvent;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaAccountingEventInfo;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaAccountingEventQryInfo;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class fa8144 {
	static final BizLog bizlog = BizLogUtil.getBizLog(fa8144.class);
	/**
	 * 
	 * @Title: qryAccountingEvent 
	 * @author zhangweihao
	 * @Description: 核算代码和余额属性查询会计核算事件
	 * @param inInfo
	 * @param ouInfo 
	 * @return void
	 * @date 2018年8月30日 上午9:49:40 
	 * @throws
	 */
	public static void qryAccountingEvent( final cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaParm.FaAccountingEventQryInfo> qrycdt,  final cn.sunline.ltts.busi.gl.fatran.trans.accountingevent.intf.Fa8144.Output output){
		bizlog.info("-------------核算代码和余额属性查询会计核算事件开始--------");
		Options<FaAccountingEventInfo> eventInfos = new DefaultOptions<FaAccountingEventInfo>();
		for(FaAccountingEventQryInfo info : qrycdt){
			FaAccountingEventInfo eventInfo = FaAccountingEvent.getAccountingEventInfo(info.getSys_no(), 
					info.getAccounting_alias(), info.getBal_attributes());
			eventInfos.add(eventInfo);
		}
		
		output.setOuInfo(eventInfos);
		bizlog.info("-------------核算代码和余额属性查询会计核算事件结束--------");
	}
}
