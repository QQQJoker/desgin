package cn.sunline.ltts.gns.api;

/**
 * 路由映射信息
 *
 */
public class GnsInf extends GnsKey {
	
	private String algtyp;
	private String sharid;
	
	public static GnsInf build() {
		GnsInf instance = new GnsInf();
		return instance;
	}
	
	// Builder
	public GnsInf algtyp(String algtyp) {
		this.algtyp = algtyp;
		return this;
	}
	public GnsInf sharid(String sharid) {
		this.sharid = sharid;
		return this;
	}	
	
	// Get&Set
	public String getAlgtyp() {
		return algtyp;
	}
	public void setAlgtyp(String algtyp) {
		this.algtyp = algtyp;
	}
	public String getSharid() {
		return sharid;
	}
	public void setSharid(String sharid) {
		this.sharid = sharid;
	}

	@Override
	public String toString() {
		return "GnsInf [algtyp=" + algtyp + ", sharid=" + sharid + ",  " + super.toString() + "]";
	}
	
}
