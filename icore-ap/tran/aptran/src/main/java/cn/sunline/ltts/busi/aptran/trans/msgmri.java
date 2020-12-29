package cn.sunline.ltts.busi.aptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.amsg.service.IoApAsyncMessage;

public class msgmri {

	public static void processMri(final cn.sunline.ltts.busi.aptran.trans.intf.Msgmri.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Msgmri.Output output) {
		// 此交易为消费端收到消息后，通过publishMri进行处理，因为服务还会用于跨节点调用
		IoApAsyncMessage process = SysUtil.getInstance(IoApAsyncMessage.class);
		process.publishMri(input.getMrinfo());
	}
}
