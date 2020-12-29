package cn.sunline.ltts.amsg.fmq.entity;

import cn.sunline.ltts.amsg.fmq.FmqConstant;

/*
 * 账户休眠
 */
public class MBespntEntity extends IFMQMessage{

	private String custac;

	public MBespntEntity() {
		this.setScenno(FmqConstant.BESPNT_001);
	}
	
	public String getCustac() {
		return custac;
	}

	public void setCustac(String custac) {
		this.custac = custac;
	}
	
}
