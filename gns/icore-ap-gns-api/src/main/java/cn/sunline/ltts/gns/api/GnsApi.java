package cn.sunline.ltts.gns.api;

import java.util.List;

public interface GnsApi {
	
	/**
	 * 增加路由映射要素
	 * @param gnsKey 路由映射要素
	 */
	public void add(GnsKey gnsKey);
	
	/**
	 * 批量增加路由映射要素
	 * @param gnsKey 路由映射要素
	 */
	public void add(List<GnsKey> gnsKey);
		
	/**
	 * 查询路由映射信息
	 * @param gns
	 * @return
	 */
	public GnsRes query(GnsKey gnsKey);
	
	
	/**
	 * 更新路由映射信息
	 * @param gns
	 * @return
	 */
	public void update(GnsKey gnsKey);
	
	
}
