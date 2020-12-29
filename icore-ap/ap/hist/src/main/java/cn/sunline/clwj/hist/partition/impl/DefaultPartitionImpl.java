package cn.sunline.clwj.hist.partition.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.clwj.hist.partition.IDBTypePartition;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.busi.aplt.tools.ApKnpGlbl;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;

/**
 * @author zhoujiawen:
 * @version 创建时间：2020年8月25日 下午5:33:51 类说明
 */
public class DefaultPartitionImpl implements IDBTypePartition {

	private static final BizLog log = BizLogUtil.getBizLog(DefaultPartitionImpl.class);

	@Override
	public void addRangePartition(String tbname, String ptname, String bgn_date, String end_date) {

		String dbtype = ApKnpGlbl.getKnpGlbl("Ap.hist", "dbtype").getPmval1();
		String sqltemplate = ApKnpGlbl.getKnpGlbl("addRangePartition", dbtype).getPmval1();

		// 传送过来的日期按照BD2的分区方式计算的，MYSQL和Oracle需要往后加一天
		end_date = DateTools.dateAdd("D", end_date, 1);

		String sql = StringUtil.format(sqltemplate, tbname, ptname, end_date);

		DBTools.executeSQL(sql);

		log.info("create partition [%s] execute sql: [%s]", ptname, sql);

	}

	@Override
	public void dropPartitionData(String tbname, String ptname) {

		String dbtype = ApKnpGlbl.getKnpGlbl("Ms.hist", "dbtype").getPmval1();
		String sqltemplate = ApKnpGlbl.getKnpGlbl("dropPartitionData", dbtype).getPmval1();

		String sql = StringUtil.format(sqltemplate, tbname, ptname);

		DBTools.executeSQL(sql);

		log.info("drop partition [%s] execute sql: [%s]", ptname, sql);

	}

	@Override
	public void detachPartitionData(String tbname, String ptname, String htbname) {

		String dbtype = ApKnpGlbl.getKnpGlbl("Ms.hist", "dbtype").getPmval1();
		String sqltemplate = ApKnpGlbl.getKnpGlbl("detachPartitionData", dbtype).getPmval1();

		String sql = StringUtil.format(sqltemplate, tbname, ptname, htbname);

		DBTools.executeSQL(sql);

		log.info("detach partition [%s], execute sql: [%s]", ptname, sql);

	}

	@Override
	public void grantSelect(String htbname, String selectUser, int retry) {

	}

}
