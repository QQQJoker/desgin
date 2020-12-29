package cn.sunline.ltts.gns.api;

/**
 * 路由映射查询结果
 *
 */
public class GnsRes {

	/** 路由映射值 */
	private String gnsval;
	/** 路由映射状态 */
	private String gnssts;

	public static GnsRes build() {
		GnsRes instance = new GnsRes();
		return instance;
	}

	// Builder
	public GnsRes val(String gnsval) {
		this.gnsval = gnsval;
		return this;
	}

	public String getGnsval() {
		return gnsval;
	}

	public void setGnsval(String gnsval) {
		this.gnsval = gnsval;
	}

	public String getGnssts() {
		return gnssts;
	}

	public void setGnssts(String gnssts) {
		this.gnssts = gnssts;
	}

	public GnsRes sts(String gnssts) {
		this.gnssts = gnssts;
		return this;
	}

	@Override
	public String toString() {
		return "GnsRes [gnsval=" + gnsval + ", gnssts=" + gnssts + "]";
	}

}
