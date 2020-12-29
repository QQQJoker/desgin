package cn.sunline.ltts.amsg.fmq.entity;

import cn.sunline.ltts.amsg.fmq.FmqConstant;

/**
 * 
 * @author 触发Pss 订单MQ发送请求
 *
 */
public class MPssRecodrEntity extends IFMQMessage{

	private String custno;
	private String custna;
	private String ordrno;
	private String orsseq;
	
	public MPssRecodrEntity() {
		this.setScenno(FmqConstant.PSS_RECODR_040);
	}
	
	public String getCustno() {
		return custno;
	}
	public void setCustno(String custno) {
		this.custno = custno;
	}
	public String getCustna() {
		return custna;
	}
	public void setCustna(String custna) {
		this.custna = custna;
	}
	public String getOrdrno() {
		return ordrno;
	}
	public void setOrdrno(String ordrno) {
		this.ordrno = ordrno;
	}
	public String getOrsseq() {
		return orsseq;
	}
	public void setOrsseq(String orsseq) {
		this.orsseq = orsseq;
	}
	
}
