package cn.sunline.clwj.oss.config;

import java.util.List;

public class SftpConfig {

	private boolean enabled;

	private String defaultConfigId;

	private String defaultLocalPath;

	private List<SftpConnectionConfig> sftpConnectionConfigs;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDefaultConfigId() {
		return defaultConfigId;
	}

	public void setDefaultConfigId(String defaultConfigId) {
		this.defaultConfigId = defaultConfigId;
	}

	public String getDefaultLocalPath() {
		return defaultLocalPath;
	}

	public void setDefaultLocalPath(String defaultLocalPath) {
		this.defaultLocalPath = defaultLocalPath;
	}

	public List<SftpConnectionConfig> getSftpConnectionConfigs() {
		return sftpConnectionConfigs;
	}

	public void setSftpConnectionConfigs(List<SftpConnectionConfig> sftpConnectionConfigs) {
		this.sftpConnectionConfigs = sftpConnectionConfigs;
	}

	@Override
	public String toString() {
		return "SftpConfig [enable=" + enabled + ", defaultConfigId=" + defaultConfigId + ", defaultLocalPath="
				+ defaultLocalPath + ", sftpConnectionConfigs=" + sftpConnectionConfigs + "]";
	}

}
