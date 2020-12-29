package cn.sunline.ltts.gns.api;

import java.util.Date;

/**
 * 会计日期对象
 *
 */
public class AcdtInf {
	
    private String validt;  //生效时间
    private Date vaildate;	//生效时间
    private String bflsdt;//上上日日期
    private String lastdt;  //上日日期
    private String systdt;  //当前日期
    private String nextdt;  //下一日起
    private String afnxdt;//下下日日期
    private String yreddt;//年终决算日期
    private String updttm;  //记录修改时间

    public String getBflsdt() {
		return bflsdt;
	}

	public void setBflsdt(String bflsdt) {
		this.bflsdt = bflsdt;
	}

	public String getAfnxdt() {
		return afnxdt;
	}

	public void setAfnxdt(String afnxdt) {
		this.afnxdt = afnxdt;
	}

	public String getYreddt() {
		return yreddt;
	}

	public void setYreddt(String yreddt) {
		this.yreddt = yreddt;
	}

	public static AcdtInf build() {
    	AcdtInf instance = new AcdtInf();
    	return instance;
    }
	
    //Builder
    public AcdtInf validt(String validt) {
    	this.validt = validt;
    	return this;
    }
    public AcdtInf lastdt(String lastdt) {
    	this.lastdt = lastdt;
    	return this;
    }
    public AcdtInf systdt(String systdt) {
    	this.systdt = systdt;
    	return this;
    }
    public AcdtInf nextdt(String nextdt) {
    	this.nextdt = nextdt;
    	return this;
    }
    public AcdtInf updttm(String updttm) {
    	this.updttm = updttm;
    	return this;
    }

    // Get&Set
	public String getValidt() {
		return validt;
	}

	public void setValidt(String validt) {
		this.validt = validt;
	}

	public String getLastdt() {
		return lastdt;
	}

	public void setLastdt(String lastdt) {
		this.lastdt = lastdt;
	}

	public String getSystdt() {
		return systdt;
	}

	public void setSystdt(String systdt) {
		this.systdt = systdt;
	}

	public String getNextdt() {
		return nextdt;
	}

	public void setNextdt(String nextdt) {
		this.nextdt = nextdt;
	}

	public String getUpdttm() {
		return updttm;
	}

	public void setUpdttm(String updttm) {
		this.updttm = updttm;
	}

	public Date getVaildate() {
		return vaildate;
	}

	public void setVaildate(Date vaildate) {
		this.vaildate = vaildate;
	}

	@Override
	public String toString() {
		return "AcdtInf [validt=" + validt + ", vaildate=" + vaildate + ", lastdt=" + lastdt + ", systdt=" + systdt
				+ ", nextdt=" + nextdt + ", updttm=" + updttm + "]";
	}


}
