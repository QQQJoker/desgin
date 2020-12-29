package cn.sunline.clwj.hist.partition.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.clwj.hist.partition.IDBTypePartition;
import cn.sunline.ltts.busi.aplt.tools.DBTools;

/** 
* @author zhoujiawen: 
* @version 创建时间：2020年8月25日 下午5:33:11 
* 类说明 
*/
public class DBTypeDB2PartitionImpl implements IDBTypePartition {

	private static final BizLog log = BizLogUtil.getBizLog(DBTypeDB2PartitionImpl.class);
	
	@Override
	public void addRangePartition(String tableName, String partitionName, String bgn_date, String end_date) {
		StringBuilder sql = new StringBuilder();
		sql.append("alter table ").append(tableName).append(" add partition ").append(partitionName).append(" starting '").append(bgn_date).append("' ending '").append(end_date).append("'");

		DBTools.executeSQL(sql.toString());
		log.info("create new partition [%s] execute sql :[%s]", partitionName, sql.toString());
	}

	@Override
	public void dropPartitionData(String tableName, String partitionName) {

	}

	@Override
	public void detachPartitionData(String tableName, String partitionName, String historyTableName) {
		StringBuilder sql = new StringBuilder();
		sql.append("alter table ").append(tableName).append(" detach partition ").append(partitionName).append(" into ").append(historyTableName);

		DBTools.executeSQL(sql.toString());
		log.info("detach the partition [%s] execute sql :[%s]", partitionName, sql.toString());
	}
	
	@Override
	public void grantSelect(String historyTableName, String selectUser, int retry) {
		int errorCount = 0;
		String sql = "";
		while (true) {
			try {
				Thread.sleep(1000);

				// 将上上次分区表加载到历史表内
				String grantSql = "grant select on table " + historyTableName + " to user " + selectUser;
				DBTools.executeSQL(grantSql);

				String testSql = "select 1 from " + historyTableName;
				DBTools.executeSQL(testSql);

				log.info("grant select on [%s] execute sql :[%s]", historyTableName, sql.toString());

				break;
			}
			catch (Exception e) {
				errorCount = errorCount + 1;
				if (errorCount == retry) {
					throw new AdpBusinessException("9999", "Data partition mount failure", e);
				}
			}
		}
	}

}
