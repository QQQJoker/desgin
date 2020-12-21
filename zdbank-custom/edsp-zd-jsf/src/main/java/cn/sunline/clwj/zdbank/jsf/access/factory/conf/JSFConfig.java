package cn.sunline.clwj.zdbank.jsf.access.factory.conf;

import javax.xml.bind.annotation.XmlRootElement;

import cn.sunline.adp.metadata.model.annotation.Index;

@Index
@XmlRootElement
public class JSFConfig {

	/* 注册URL */
	private String registryURL;

	/* api所在包 */
	private String apiPackage = "cn.sunline.edsp.busi.jsfapi";

	/* 接出初始化API */
	private String outBoundApi = "Msap:vb.cocif.vb.dev,Public:vb.cocom.vb.dev";

	/* registryId */
	private String registryId;

	/* serviceAliasName */
	private String serviceAliasName;

	private String consumerAddress;

	public String getRegistryURL() {
		return registryURL;
	}

	public void setRegistryURL(String registryURL) {
		this.registryURL = registryURL;
	}

	public String getApiPackage() {
		return apiPackage;
	}

	public void setApiPackage(String apiPackage) {
		this.apiPackage = apiPackage;
	}

	public String getOutBoundApi() {
		return outBoundApi;
	}

	public void setOutBoundApi(String outBoundApi) {
		this.outBoundApi = outBoundApi;
	}

	public String getRegistryId() {
		return registryId;
	}

	public void setRegistryId(String registryId) {
		this.registryId = registryId;
	}

	public String getServiceAliasName() {
		return serviceAliasName;
	}

	public void setServiceAliasName(String serviceAliasName) {
		this.serviceAliasName = serviceAliasName;
	}

	public String getConsumerAddress() {
		return consumerAddress;
	}

	public void setConsumerAddress(String consumerAddress) {
		this.consumerAddress = consumerAddress;
	}

	@Override
	public String toString() {
		return "JSFConfig [registryURL=" + registryURL + ", apiPackage=" + apiPackage + ", outBoundApi=" + outBoundApi
				+ ", registryId=" + registryId + ", serviceAliasName=" + serviceAliasName + ", consumerAddress="
				+ consumerAddress + "]";
	}

}
