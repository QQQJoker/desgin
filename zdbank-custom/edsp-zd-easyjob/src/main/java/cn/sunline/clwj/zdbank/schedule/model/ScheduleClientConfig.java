package cn.sunline.clwj.zdbank.schedule.model;

import org.springframework.lang.NonNull;

public class ScheduleClientConfig {

	@NonNull
	private String host;
	@NonNull
	private String appId;
	@NonNull
	private String secret;

	private String tenantId;
	private String group;
	private boolean autoStart = true;
	private Integer intervalMill;
	private Integer maxConcurrentNo;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getSecret() {
		return secret;
	}
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public boolean isAutoStart() {
		return autoStart;
	}
	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}
	public Integer getIntervalMill() {
		return intervalMill;
	}
	public void setIntervalMill(Integer intervalMill) {
		this.intervalMill = intervalMill;
	}
	public Integer getMaxConcurrentNo() {
		return maxConcurrentNo;
	}
	public void setMaxConcurrentNo(Integer maxConcurrentNo) {
		this.maxConcurrentNo = maxConcurrentNo;
	}

}
