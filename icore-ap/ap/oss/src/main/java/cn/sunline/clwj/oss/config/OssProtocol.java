package cn.sunline.clwj.oss.config;

public enum OssProtocol {

	sftp("sftp",""),
	ftp("ftp",""),
	nfs("nfs",""),
	sshj("sshj","");
	
	private String protocolId;
	private String protocolName;
	
	private OssProtocol(String protocolId, String protocolName) {
		this.setProtocolId(protocolId);
		this.setProtocolName(protocolName);
	}

	public String getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

	public String getProtocolId() {
		return protocolId;
	}

	public void setProtocolId(String protocolId) {
		this.protocolId = protocolId;
	}
}
