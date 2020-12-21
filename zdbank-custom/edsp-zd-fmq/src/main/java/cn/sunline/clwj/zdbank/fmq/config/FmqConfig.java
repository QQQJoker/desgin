package cn.sunline.clwj.zdbank.fmq.config;

import javax.xml.bind.annotation.XmlRootElement;

import cn.sunline.adp.metadata.model.annotation.Index;

@Index
@XmlRootElement
public class FmqConfig {

	private String producerApp;

	private String producerAddress;

	private String producerUser;

	private String producerPasswd;

	private String consumerApp;
	
	private String consumerUser;
	
	private String consumerPasswd;
	
	private int sendTimeout;

	private int connectionTimeout;

	private boolean isEopll;

	public String getProducerApp() {
		return producerApp;
	}

	public void setProducerApp(String producerApp) {
		this.producerApp = producerApp;
	}


	public String getProducerAddress() {
		return producerAddress;
	}

	public void setProducerAddress(String producerAddress) {
		this.producerAddress = producerAddress;
	}

	public String getProducerUser() {
		return producerUser;
	}

	public void setProducerUser(String producerUser) {
		this.producerUser = producerUser;
	}

	public String getProducerPasswd() {
		return producerPasswd;
	}

	public void setProducerPasswd(String producerPasswd) {
		this.producerPasswd = producerPasswd;
	}

	public int getSendTimeout() {
		return sendTimeout;
	}

	public void setSendTimeout(int sendTimeout) {
		this.sendTimeout = sendTimeout;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public boolean isEopll() {
		return isEopll;
	}

	public void setEopll(boolean isEopll) {
		this.isEopll = isEopll;
	}

	public String getConsumerApp() {
		return consumerApp;
	}

	public void setConsumerApp(String consumerApp) {
		this.consumerApp = consumerApp;
	}

	public String getConsumerUser() {
		return consumerUser;
	}

	public void setConsumerUser(String consumerUser) {
		this.consumerUser = consumerUser;
	}

	public String getConsumerPasswd() {
		return consumerPasswd;
	}

	public void setConsumerPasswd(String conusermPasswd) {
		this.consumerPasswd = conusermPasswd;
	}
	
	
}