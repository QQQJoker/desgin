package cn.sunline.clwj.zdbank.jsf.access.out.conf;

import javax.xml.bind.annotation.XmlRootElement;
import cn.sunline.adp.metadata.model.annotation.Index;

@Index
@XmlRootElement
public class HttpConfig {
	/*网关请求路径*/
	private String url;
	
	/*请求用户id*/
	private String userId;
	
	/*请求用户类型*/
	private String userType;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}
	
	
}
