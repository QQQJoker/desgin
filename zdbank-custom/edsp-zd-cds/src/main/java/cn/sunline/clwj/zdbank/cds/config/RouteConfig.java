package cn.sunline.clwj.zdbank.cds.config;

import javax.xml.bind.annotation.XmlRootElement;

import cn.sunline.adp.cedar.base.plugin.config.SystemAppConfig;
import cn.sunline.adp.metadata.model.annotation.ComplexType;
import cn.sunline.adp.metadata.model.annotation.Element;
import cn.sunline.adp.metadata.model.annotation.Index;

@XmlRootElement
@Index @ComplexType(longname="分布式路由服务",owner=SystemAppConfig.class,tags="xml")
public class RouteConfig {
	
	@Element(longname="集群id",defaultValue="true")
	private String clusterId;

	@Element(longname="基准表名",defaultValue="true")
	private String sktable;

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getSktable() {
		return sktable;
	}

	public void setSktable(String sktable) {
		this.sktable = sktable;
	}
	
	
	
}