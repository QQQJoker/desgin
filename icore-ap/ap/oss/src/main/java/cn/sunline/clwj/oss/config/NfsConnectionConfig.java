package cn.sunline.clwj.oss.config;

public class NfsConnectionConfig {
	
	private String id;
	
    private String remoteHome;
    
    private String localHome;
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRemoteHome() {
		return remoteHome;
	}

	public void setRemoteHome(String remoteHome) {
		this.remoteHome = remoteHome;
	}

	public String getLocalHome() {
		return localHome;
	}

	public void setLocalHome(String localHome) {
		this.localHome = localHome;
	}
}
