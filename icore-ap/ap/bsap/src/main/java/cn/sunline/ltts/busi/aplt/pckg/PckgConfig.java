package cn.sunline.ltts.busi.aplt.pckg;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import cn.sunline.adp.cedar.net.tcp.socket.util.SystemSrvConfig;
import cn.sunline.adp.metadata.model.annotation.ComplexType;
import cn.sunline.adp.metadata.model.annotation.Element;
import cn.sunline.adp.metadata.model.annotation.Index;

@ComplexType(longname="系统报文登记配置",owner=SystemSrvConfig.class)
@XmlRootElement @Index
public class PckgConfig {
	
	@Element(longname="是否登记",defaultValue="true")
	private boolean regFlag;
	/**
	 * MQ 基于第三方MQ实现（暂时未实现）
	 * BQ BlockMQ 基于JVM内部实现（目前已经实现）
	 */
	@Element(longname="异步方式",defaultValue="true",description="MQ,BQ")
	private String asynType;
	
	@Element(longname="是否异步",defaultValue="true")
	private boolean asynFlag;
	
	@Element(longname="是否启用大字段副表",defaultValue="false")
	private boolean useClobTab;
	
	@Element(longname="是否登记上下报文",defaultValue="true")
	private boolean allFlag;
	
	@Element(longname="异步队列深度",defaultValue="500")
	private int quequeSize;
	
	@Element(longname="异步线程数",defaultValue="2")
	private int threadNum;

	@Element(longname="是否批量登记",defaultValue="true")
	private boolean byInsertBatch;
	
	@Element(longname="批量登记笔数",defaultValue="50")
	private int batchSize;
	
	@XmlAttribute
	public boolean isRegFlag() {
		return regFlag;
	}

	public void setRegFlag(boolean regFlag) {
		this.regFlag = regFlag;
	}
	
	@XmlAttribute
	public boolean isAsynFlag() {
		return asynFlag;
	}

	public void setAsynFlag(boolean asynFlag) {
		this.asynFlag = asynFlag;
	}
	
	@XmlAttribute
	public String getAsynType() {
		return asynType;
	}

	public void setAsynType(String asynType) {
		this.asynType = asynType;
	}

	@XmlAttribute
	public boolean isUseClobTab() {
		return useClobTab;
	}

	public void setUseClobTab(boolean useClobTab) {
		this.useClobTab = useClobTab;
	}
	
	@XmlAttribute
	public boolean isAllFlag() {
		return allFlag;
	}

	public void setAllFlag(boolean allFlag) {
		this.allFlag = allFlag;
	}

	@XmlAttribute
	public int getQuequeSize() {
		return quequeSize;
	}

	public void setQuequeSize(int quequeSize) {
		this.quequeSize = quequeSize;
	}

	@XmlAttribute
	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	@XmlAttribute
	public boolean isByInsertBatch() {
		return byInsertBatch;
	}

	public void setByInsertBatch(boolean byInsertBatch) {
		this.byInsertBatch = byInsertBatch;
	}

	@XmlAttribute
	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
}
