package cn.sunline.ltts.amsg.fmq.entity;

import java.math.BigDecimal;

import cn.sunline.ltts.amsg.fmq.FmqConstant;
/*
 * 通用一借一贷消息结构
 */
public class MCzacctEntity extends IFMQMessage{

	private String acctno;
	private String virtno;
	private BigDecimal tranam;
	private BigDecimal acctbl;
	
	public MCzacctEntity() {
		this.setScenno(FmqConstant.CZACCT_001);
	}
	
	public String getAcctno() {
		return acctno;
	}
	public void setAcctno(String acctno) {
		this.acctno = acctno;
	}
	public String getVirtno() {
		return virtno;
	}
	public void setVirtno(String virtno) {
		this.virtno = virtno;
	}
	public BigDecimal getTranam() {
		return tranam;
	}
	public void setTranam(BigDecimal tranam) {
		this.tranam = tranam;
	}
	public BigDecimal getAcctbl() {
		return acctbl;
	}
	public void setAcctbl(BigDecimal bigDecimal) {
		this.acctbl = bigDecimal;
	}
}
