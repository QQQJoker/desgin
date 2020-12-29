package cn.sunline.clwj.oss.config;

public class SftpConnectionConfig {
	
	private String id;
	
	private int connTimeoutInMs;
	
	private String serverIp;
	
	private int serverPort;
	
    private String userName;
    
    private String password;
    
    private String remoteHome;
    
    private String localHome;
    
    private String keyPath;
    
    public SftpConnectionConfig(){}
    
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

	public String getKeyPath() {
		return keyPath;
	}
	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}

	
	@Override
	public String toString() {
		return String.format("username:[%s], ip:[%s], port:[%s]", userName, serverIp, serverPort);
	}
}
