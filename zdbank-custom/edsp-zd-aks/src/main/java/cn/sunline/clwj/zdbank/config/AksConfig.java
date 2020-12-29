package cn.sunline.clwj.zdbank.config;

import javax.xml.bind.annotation.XmlRootElement;

import cn.sunline.adp.cedar.base.plugin.config.SystemAppConfig;
import cn.sunline.adp.metadata.model.annotation.ComplexType;
import cn.sunline.adp.metadata.model.annotation.Element;
import cn.sunline.adp.metadata.model.annotation.Index;

@XmlRootElement
@Index @ComplexType(longname="aks服务",owner=SystemAppConfig.class,tags="xml")
public class AksConfig {

	@Element(longname="应用ID",defaultValue="true")
	private String appid;

	@Element(longname="应用名",defaultValue="true")
	private String appname;

	@Element(longname="别名",defaultValue="true")
	private String aliasname;
	
	@Element(longname="密钥",defaultValue="true")
	private String secure;

	@Element(longname="接口",defaultValue="true")
	private String inferface;

	@Element(longname="协议",defaultValue="true")
	private String protocol;

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getAliasname() {
		return aliasname;
	}

	public void setAliasname(String aliasname) {
		this.aliasname = aliasname;
	}

	public String getInferface() {
		return inferface;
	}

	public void setInferface(String inferface) {
		this.inferface = inferface;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getSecure() {
		return secure;
	}

	public void setSecure(String secure) {
		this.secure = secure;
	}

}
