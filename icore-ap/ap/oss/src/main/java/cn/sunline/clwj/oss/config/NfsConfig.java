package cn.sunline.clwj.oss.config;

import java.util.List;


public class NfsConfig{
	
	private boolean enabled;
	
	private String defaultConfigId;
	
	private String defaultLocalPath;

	private List<NfsConnectionConfig> nfsConnectionConfigs;

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

	public List<NfsConnectionConfig> getNfsConnectionConfigs() {
		return nfsConnectionConfigs;
	}

	public void setNfsConnectionConfigs(List<NfsConnectionConfig> nfsConnectionConfigs) {
		this.nfsConnectionConfigs = nfsConnectionConfigs;
	}

	@Override
	public String toString() {
		return "NfsConfig [enable=" + enabled + ", defaultConfigId=" + defaultConfigId + ", defaultLocalPath="
				+ defaultLocalPath + ", nfsConnectionConfigs=" + nfsConnectionConfigs + "]";
	}

}
