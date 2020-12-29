package cn.sunline.clwj.oss.config;

public class FtpConnectionConfig {
	
	private String id;
	
	private int connTimeoutInMs;
	
	private String serverIp;
	
	private int serverPort;
	
    private String userName;
    
    private String password;
    
    private String remoteHome;
    
    private String localHome;
    
    private int retryTime;
    
    private int retryInterval;
    
    private int dataTimeoutInMs;
    
    public String getServerIp() {
		return serverIp;
	}
    
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
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

	public int getRetryTime() {
		return retryTime;
	}
	
	public void setRetryTime(int retryTime) {
		this.retryTime = retryTime;
	}
	
	public int getRetryInterval() {
		return retryInterval;
	}
	
	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}
	
	public int getDataTimeoutInMs() {
		return dataTimeoutInMs;
	}
	
	public void setDataTimeoutInMs(int dataTimeoutInMs) {
		this.dataTimeoutInMs = dataTimeoutInMs;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public int getConnTimeoutInMs() {
		return connTimeoutInMs;
	}
	
	public void setConnTimeoutInMs(int connTimeoutInMs) {
		this.connTimeoutInMs = connTimeoutInMs;
	}

}
