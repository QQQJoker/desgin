package cn.sunline.clwj.zdbank.zd.r2m.model;

public class R2MCacheClient {

	private int maxRedirections;
	private int redisTimeOut;

	public int getMaxRedirections() {
		return maxRedirections;
	}

	public void setMaxRedirections(int maxRedirections) {
		this.maxRedirections = maxRedirections;
	}

	public int getRedisTimeOut() {
		return redisTimeOut;
	}

	public void setRedisTimeOut(int redisTimeOut) {
		this.redisTimeOut = redisTimeOut;
	}
	
}
