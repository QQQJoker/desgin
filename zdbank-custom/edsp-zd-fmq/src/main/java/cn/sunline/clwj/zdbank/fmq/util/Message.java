package cn.sunline.clwj.zdbank.fmq.util;

public class Message {
	
	/**消息唯一标识，业务流水号，交易流水号等*/
	private String busiId;
	
	/**消息主题ID*/
	private String topicId;
	
	/**消息体内容*/
	private String messageBody;
	
	public Message(String busiId,String topicId,String messageBody) {
		this.busiId=busiId;
		this.topicId=topicId;
		this.messageBody=messageBody;
	}

	public String getBusiId() {
		return busiId;
	}

	public void setBusiId(String busiId) {
		this.busiId = busiId;
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}
	
	

}
