package cn.sunline.clwj.hist.partition.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.clwj.hist.partition.IDBTypePartition;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;

/**
 * @author zhoujiawen:
 * @version 创建时间：2020年8月25日 下午5:33:36 类说明
 */
public class DBTypeMysqlPartitionImpl implements IDBTypePartition {

	private static final BizLog log = BizLogUtil.getBizLog(DBTypeDB2PartitionImpl.class);

	@Override
	public void addRangePartition(String tableName, String partitionName, String bgn_date, String end_date) {
		StringBuilder sql = new StringBuilder();

		// 传送过来的日期按照BD2的分区方式计算的，MYSQL和Oracle需要往后加一天
		end_date = DateTools.dateAdd("D", end_date, 1);
		sql.append("alter table ").append(tableName).append(" ADD PARTITION (PARTITION ").append(partitionName)
				.append(" VALUES LESS THAN (\'").append(end_date).append("\'))");

		DBTools.executeSQL(sql.toString());
		log.info("create partition [%s] execute sql: [%s]", partitionName, sql.toString());
	}

	@Override
	public void dropPartitionData(String tableName, String partitionName) {
		StringBuilder sql = new StringBuilder();
		sql.append("alter table ").append(tableName).append(" drop partition ").append(partitionName);
		DBTools.executeSQL(sql.toString());
		log.info("drop partition [%s] execute sql: [%s]", partitionName, sql.toString());
	}

	@Override
	public void detachPartitionData(String tableName, String partitionName, String historyTableName) {
		String sql = "alter table " + tableName + " EXCHANGE PARTITION " + partitionName + " with table "
				+ historyTableName;
		DBTools.executeSQL(sql.toString());
		log.info("detach partition [%s], execute sql: [%s]", partitionName, sql.toString());
	}

	@Override
	public void grantSelect(String historyTableName, String selectUser, int times) {

	}
}
