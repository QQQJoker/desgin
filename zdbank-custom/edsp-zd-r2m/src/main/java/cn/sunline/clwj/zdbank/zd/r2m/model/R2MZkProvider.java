package cn.sunline.clwj.zdbank.zd.r2m.model;

public class R2MZkProvider {

	private String appName;
	private String zkConnectionStr;
	private String password;
	private int zkTimeout;
	private int zkSessionTimeout;
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getZkConnectionStr() {
		return zkConnectionStr;
	}
	public void setZkConnectionStr(String zkConnectionStr) {
		this.zkConnectionStr = zkConnectionStr;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getZkTimeout() {
		return zkTimeout;
	}
	public void setZkTimeout(int zkTimeout) {
		this.zkTimeout = zkTimeout;
	}
	public int getZkSessionTimeout() {
		return zkSessionTimeout;
	}
	public void setZkSessionTimeout(int zkSessionTimeout) {
		this.zkSessionTimeout = zkSessionTimeout;
	}

}
