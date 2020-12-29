package cn.sunline.clwj.oss.config;

public class OssConfig{
	
	private boolean enabled = false;
	private String ossImpl;
	
	
	public String getOssImpl() {
		return ossImpl;
	}

	public void setOssImpl(String ossImpl) {
		this.ossImpl = ossImpl;
	}

	private String configImpl;
	

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getConfigImpl() {
		return configImpl;
	}

	public void setConfigImpl(String configImpl) {
		this.configImpl = configImpl;
	}


	@Override
	public String toString() {
		return "OssConfig [enabled=" + enabled + ",configImpl=" + configImpl + "]";
	}

}
