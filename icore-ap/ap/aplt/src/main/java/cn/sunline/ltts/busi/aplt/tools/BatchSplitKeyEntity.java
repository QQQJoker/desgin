package cn.sunline.ltts.busi.aplt.tools;

public class BatchSplitKeyEntity {
	
	private String dbgrop; //数据库实例名称
	private String suffix; //数据表后缀分片号
	
	public String getDbgrop() {
		return dbgrop;
	}
	public void setDbgrop(String dbgrop) {
		this.dbgrop = dbgrop;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
}
