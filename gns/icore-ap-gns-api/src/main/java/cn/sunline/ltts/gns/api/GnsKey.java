package cn.sunline.ltts.gns.api;

/**
 * 路由映射要素信息(对应app_gns表)
 *
 */
public class GnsKey {
	
	/**路由映射键*/
	private String gnskey;
	/**路由映射类型*/
	private String gnstyp;
	/**路由映射识别标志*/
	private String gnsopt;
	/**路由映射渠道标识*/
	private String gnschn;
	/**路由映射值*/
	private String gnsval;
	/**路由映射状态*/
	private String gnssts;
	
	public static GnsKey build() {
		GnsKey instance = new GnsKey();
		return instance;
	}
	
	// Builder
	public GnsKey gnskey(String gnskey) {
		this.gnskey = gnskey;
		return this;
	}
	public GnsKey gnstyp(String gnstyp) {
		this.gnstyp = gnstyp;
		return this;
	}
	public GnsKey gnsopt(String gnsopt) {
		this.gnsopt = gnsopt;
		return this;
	}
	public GnsKey gnschn(String gnschn) {
		this.gnschn = gnschn;
		return this;
	}
	public GnsKey gnssts(String gnssts) {
		this.gnssts = gnssts;
		return this;
	}
	public GnsKey gnsval(String gnsval) {
		this.gnsval = gnsval;
		return this;
	}
	
	// Get&Set
	public String getGnskey() {
		return gnskey;
	}
	public void setGnskey(String gnskey) {
		this.gnskey = gnskey;
	}
	public String getGnstyp() {
		return gnstyp;
	}
	public void setGnstyp(String gnstyp) {
		this.gnstyp = gnstyp;
	}
	public String getGnsopt() {
		return gnsopt;
	}
	public void setGnsopt(String gnsopt) {
		this.gnsopt = gnsopt;
	}
	public String getGnschn() {
		return gnschn;
	}
	public void setGnschn(String gnschn) {
		this.gnschn = gnschn;
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


	@Override
	public String toString() {
		return "GnsKey [gnskey=" + gnskey + ", gnstyp=" + gnstyp + ", gnsopt=" + gnsopt + ", gnschn=" + gnschn
				+  ", gnsval=" + gnsval + ", gnssts=" + gnssts + "]";
	}
}
