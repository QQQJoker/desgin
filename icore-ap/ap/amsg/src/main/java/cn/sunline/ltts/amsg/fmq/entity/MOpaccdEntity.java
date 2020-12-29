package cn.sunline.ltts.amsg.fmq.entity;

import cn.sunline.ltts.amsg.fmq.FmqConstant;
import cn.sunline.ltts.busi.sys.type.CaEnumType.E_OPACRT;

/*
 * 开户消息输出MQ消息结构
 */
public class MOpaccdEntity extends IFMQMessage{

	private String cardno;
	private E_OPACRT opacrt;
	private String brchno;
	private String tmstmp;
	
	public MOpaccdEntity () {
		this.setScenno(FmqConstant.OPACCD_001);
	}
	
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public E_OPACRT getOpacrt() {
		return opacrt;
	}
	public void setOpacrt(E_OPACRT opacrt) {
		this.opacrt = opacrt;
	}
	public String getBrchno() {
		return brchno;
	}
	public void setBrchno(String brchno) {
		this.brchno = brchno;
	}
	public String getTmstmp() {
		return tmstmp;
	}
	public void setTmstmp(String tmstmp) {
		this.tmstmp = tmstmp;
	}
	
	
}
