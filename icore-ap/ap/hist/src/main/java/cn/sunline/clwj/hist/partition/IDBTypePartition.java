package cn.sunline.clwj.hist.partition;

/**
 * 分区操作接口类
 * 
 * @author 39lizhensan
 *
 */
public interface IDBTypePartition {
	/**
	 * 增加范围分区
	 * 
	 * @param tbname
	 *            表名
	 * @param ptname
	 *            表分区名
	 * @param bgn_date
	 *            分区开始日期
	 * @param end_date
	 *            分区结束日期
	 */
	public void addRangePartition(String tbname, String ptname, String bgn_date, String end_date);

	/**
	 * 删除分区数据
	 * 
	 * @param tbname
	 *            表名
	 * @param ptname
	 *            表分区名
	 * @return
	 */
	public void dropPartitionData(String tbname, String ptname);

	/**
	 * 转存分区数据
	 * 
	 * @param tbname
	 *            表名
	 * @param ptname
	 *            表分区名
	 * @param htbname
	 *            历史表名
	 * @return
	 */
	public void detachPartitionData(String tbname, String ptname, String htbname);

	/**
	 * @param htbname
	 *            历史表名称
	 * @param selectUser
	 *            查询用户用户名称
	 * @param retry
	 *            重试次数
	 */
	public void grantSelect(String htbname, String selectUser, int retry);
}
