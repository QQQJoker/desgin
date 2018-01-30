package com.joker.design.prototype;

public class Mail1 implements Cloneable{

	private String receiver;
	
	private String subject;
	
	private String appellation;
	
	private String context;
	
	private String tail;
	
	public Mail1(AdvTemplate advTemplate) {
		this.context = advTemplate.getAdvContext();
		this.subject = advTemplate.getAdvSubject();
	}
	
	// 重写clone方法
	@Override
	protected Mail1 clone(){
		Mail1 mail = null;
		try {
			
			mail = (Mail1)super.clone();
			
		}catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		return mail;
	}



	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getAppellation() {
		return appellation;
	}

	public void setAppellation(String appellation) {
		this.appellation = appellation;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getTail() {
		return tail;
	}

	public void setTail(String tail) {
		this.tail = tail;
	}
	
}
