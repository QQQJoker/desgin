package cn.sunline.clwj.oss.config;

import java.util.List;

public class FtpConfig {

	private boolean enabled;

	private String defaultConfigId;

	private String defaultLocalPath;

	private List<FtpConnectionConfig> ftpConnectionConfigs;

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

	public List<FtpConnectionConfig> getFtpConnectionConfigs() {
		return ftpConnectionConfigs;
	}

	public void setFtpConnectionConfigs(List<FtpConnectionConfig> ftpConnectionConfigs) {
		this.ftpConnectionConfigs = ftpConnectionConfigs;
	}

	@Override
	public String toString() {
		return "FtpConfig [enable=" + enabled + ", defaultConfigId=" + defaultConfigId + ", defaultLocalPath="
				+ defaultLocalPath + ", ftpConnectionConfigs=" + ftpConnectionConfigs + "]";
	}

}
