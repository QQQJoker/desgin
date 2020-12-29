package cn.sunline.ltts.busi.aptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.amsg.service.IoApAsyncMessage;

public class msgmti {

	public static void processMti(final cn.sunline.ltts.busi.aptran.trans.intf.Msgmti.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Msgmti.Output output) {
		// 此交易为消费端收到消息后，通过publish进行处理，因为服务还会用于跨节点调用
		IoApAsyncMessage process = SysUtil.getInstance(IoApAsyncMessage.class);
		process.publish(input.getMtinfo());
	}
}
