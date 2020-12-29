package cn.sunline.ltts.busi.aplt.plugin;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import cn.sunline.adp.metadata.model.annotation.ComplexType;
import cn.sunline.adp.metadata.model.annotation.Element;
import cn.sunline.adp.metadata.model.annotation.Index;
import cn.sunline.adp.cedar.net.tcp.socket.util.SystemSrvConfig;

@ComplexType(longname="系统扩展配置",owner=SystemSrvConfig.class)
@XmlRootElement @Index
public class SystemExtConfig {
	
	@Element(longname="所有子系统列表",defaultValue="")
	private String allSubSystem;
	
	@Element(longname="本地调用子系统列表",defaultValue="")
	private String localSubSystem;
	
	@Element(longname="分布式数据库集群ID",defaultValue="")
	private String distributeDBClusterName;
	
	@Element(longname="确定SK基准表",defaultValue="")
	private String shardingkeyBaseTab;
	
	@Element(longname="SQL指定分表注释格式",defaultValue="")
	private String shardingkeySQLFmt;
	
	@Element(longname="是否启动分库分表",defaultValue="")
	private Boolean shardingEnable;
	
	@Element(longname="默认分表类型",defaultValue="")
	private String defaultShardingType;
	
	@XmlAttribute
	public String getAllSubSystem() {
		return allSubSystem;
	}

	public void setAllSubSystem(String allSubSystem) {
		this.allSubSystem = allSubSystem;
	}

	@XmlAttribute
	public String getLocalSubSystem() {
		return localSubSystem;
	}

	public void setLocalSubSystem(String localSubSystem) {
		this.localSubSystem = localSubSystem;
	}
	
	@XmlAttribute
	public String getDistributeDBClusterName() {
		return distributeDBClusterName;
	}

	public void setDistributeDBClusterName(String distributeDBClusterName) {
		this.distributeDBClusterName = distributeDBClusterName;
	}

	@XmlAttribute
	public String getShardingkeyBaseTab() {
		return shardingkeyBaseTab;
	}

	public void setShardingkeyBaseTab(String shardingkeyBaseTab) {
		this.shardingkeyBaseTab = shardingkeyBaseTab;
	}

	@XmlAttribute
	public String getShardingkeySQLFmt() {
		return shardingkeySQLFmt;
	}

	public void setShardingkeySQLFmt(String shardingkeySQLFmt) {
		this.shardingkeySQLFmt = shardingkeySQLFmt;
	}

	@XmlAttribute
	public Boolean getShardingEnable() {
		return shardingEnable;
	}

	public void setShardingEnable(Boolean shardingEnable) {
		this.shardingEnable = shardingEnable;
	}

	@XmlAttribute
	public String getDefaultShardingType() {
		return defaultShardingType;
	}

	public void setDefaultShardingType(String defaultShardingType) {
		this.defaultShardingType = defaultShardingType;
	}
	
}
