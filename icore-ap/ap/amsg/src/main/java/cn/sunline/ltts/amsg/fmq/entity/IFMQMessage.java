package cn.sunline.ltts.amsg.fmq.entity;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.amsg.fmq.ApAmsgFMQUtil;
import cn.sunline.ltts.amsg.fmq.FmqConstant;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppMessToMQ;

public abstract  class IFMQMessage {
	private static final BizLog bizlog = BizLogUtil.getBizLog(IFMQMessage.class);

	private String scenno; //场景编号
	private static final String Head = "head";
	private static final String Body = "body";
	
	public void submitMessage(IFMQMessage msg) {
		// String message = SysUtil.serialize(msg);
		AppMessToMQ getSerCodeItem = FmqConstant.getMsgInfo(scenno);
		String message = ApAmsgFMQUtil.buildTransferMessage(msg, getSerCodeItem.getTopcid());
		AppMessToMQ msgInfo = FmqConstant.getMsgInfo(msg.getScenno());
		
		if (CommUtil.isNull(msgInfo)) {
			return;
		}
		ApAmsgFMQUtil.insertToFMQ(message, msgInfo.getTopcid());
	}
	
	public <T extends IFMQMessage> String custom(T message) {
		
		return null;
	}
	
	public String getScenno() {
		return scenno;
	}
	
	public void setScenno(String scenno) {
		this.scenno = scenno;
	}
	
}
