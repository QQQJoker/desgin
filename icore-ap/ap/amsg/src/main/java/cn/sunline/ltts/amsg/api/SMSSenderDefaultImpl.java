package cn.sunline.ltts.amsg.api;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;

public class SMSSenderDefaultImpl implements SMSSender {
	private static final BizLog log = BizLogUtil.getBizLog(SMSSenderDefaultImpl.class);
	

	@Override
	public void sendSMSes(List<ApMessageComplexType.SMSCType> smses) {
		log.info("发送万能模版短信实现：" + smses);
	}


}
