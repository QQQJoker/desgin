package cn.sunline.clwj.zdbank.jsf.access.factory.conf;

import com.jd.jsf.gd.config.RegistryConfig;

public class JSFRegistryConfig {

	private static JSFRegistryConfig instance = new JSFRegistryConfig();

	private RegistryConfig jsfRegistry;

	private JSFRegistryConfig() {
	}

	public static JSFRegistryConfig getInstance() {
		return instance;
	}

	public RegistryConfig getJsfRegistry() {
		return jsfRegistry;
	}

	public void setJsfRegistry(RegistryConfig jsfRegistry) {
		this.jsfRegistry = jsfRegistry;
	}
}
