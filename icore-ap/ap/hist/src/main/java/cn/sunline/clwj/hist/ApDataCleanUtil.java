package cn.sunline.clwj.hist;

import java.util.List;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.hist.partition.DBTypeFactory;
import cn.sunline.clwj.hist.partition.IDBTypePartition;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.busi.bsap.namedsql.ApHistNSQLDao;
import cn.sunline.edsp.busi.bsap.tables.ApHistTable.AplDataClean;
import cn.sunline.edsp.busi.bsap.tables.ApHistTable.AplDataCleanDao;
import cn.sunline.edsp.busi.bsap.tables.ApHistTable.AppDataClean;
import cn.sunline.edsp.busi.bsap.tables.ApHistTable.AppDataCleanDao;
import cn.sunline.ltts.busi.aplt.tools.ApKnpGlbl;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DOTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PARTMODE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRUE__;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * @author zhoujiawen:
 * @version 创建时间：2020年8月25日 下午5:31:12 类说明
 */
public class ApDataCleanUtil {

	// private static final BizLog log =
	// BizLogUtil.getBizLog(MspDataCleanUtil.class);

	private static final String PARTITION_PREFIX = "p";

	/**
	 * 转储到历史表
	 * 
	 * @param currDate
	 *            当前日期
	 */
	public static void histTablePartition(String currDate) {
		// 查询所有配置需要转储处理表参数
		List<AppDataClean> appDataCleanList = AppDataCleanDao.selectAll_odb1(E_YES___.YES, false);
		for (AppDataClean appDataClean : appDataCleanList) {
			tablePartitionHandle(appDataClean, currDate, E_DOTYPE.TOHIS);
		}
	}

	/**
	 * 删除表分区
	 * 
	 * @param currDate
	 */
	public static void dropTablePartition(String currDate) {
		// 表分区转换时，分区会被直接删除掉，所以在这里需要过滤掉需要转储的表
		List<AppDataClean> appDataCleanList = AppDataCleanDao.selectAll_odb1(E_YES___.NO, false);
		for (AppDataClean appDataClean : appDataCleanList) {
			tablePartitionHandle(appDataClean, currDate, E_DOTYPE.DROP);
		}
	}

	/**
	 * 添加表分区
	 * 
	 * @param currDate
	 */
	public static void addTablePartition(String currDate) {
		List<AppDataClean> appDataCleanList = ApHistNSQLDao.listMspDataClean(false);
		for (AppDataClean appDataClean : appDataCleanList) {
			tablePartitionHandle(appDataClean, currDate, E_DOTYPE.CREATE);
		}
	}

	private static void tablePartitionHandle(AppDataClean appDataClean, String currDate, E_DOTYPE operationType) {
		E_PARTMODE partition_mode = appDataClean.getParmod();// 分区模式
		String table_name = appDataClean.getTablna();// 表名称

		// 如果为按月分区模式,则只有当月最后一天才做转储
		if (partition_mode == E_PARTMODE.MONTH && !DateTools.isLastDay("M", currDate)) {
			return;
		}
		// 当前否是已经转储表分区, 已经转储则跳出
		AplDataClean aplDataClean = AplDataCleanDao.selectOne_odb1(table_name, operationType, E_YES___.YES,
				currDate, false);
		if (aplDataClean != null) {
			return;
		}

		// 获取当前系统数据库类型
		String dbtype = ApKnpGlbl.getKnpGlbl("Ms.hist", "dbtype").getPmval1();
		// IDBTypePartition partitionHandler =
		// DBTypeFactory.getDBType(E_DBTYPE.valueOf(dbtype));
		IDBTypePartition partitionHandler = DBTypeFactory.getDefaultPartitionImpl();
		String partitionName = "";
		try {
			if (operationType == E_DOTYPE.CREATE) {
				// partitionName = MsHistNSQLDao.getNewestPartitionName(table_name, true);
				partitionName = ApKnpGlbl.getKnpGlbl("getNewestPartitionName", dbtype).getPmval1();
				String newPartitionName = getNextPartitionName(partitionName, partition_mode);
				String startDay = "";
				String endDay = "";
				if (partition_mode == E_PARTMODE.DAY) {
					startDay = newPartitionName.replaceFirst(PARTITION_PREFIX, "");
					endDay = startDay;
				} else {
					startDay = DateTools.firstDay("M", newPartitionName.replaceFirst(PARTITION_PREFIX, ""));
					endDay = DateTools.lastDay("M", newPartitionName.replaceFirst(PARTITION_PREFIX, ""));
				}

				partitionHandler.addRangePartition(table_name, newPartitionName, startDay, endDay);
				partitionName = newPartitionName;// 方便登记
			} else if (operationType == E_DOTYPE.TOHIS) {
				// partitionName = MsHistNSQLDao.getOldestPartitionName(table_name, true);
				partitionName = ApKnpGlbl.getKnpGlbl("getOldestPartitionName", dbtype).getPmval1();
				String history_table_name = appDataClean.getHitana();
				partitionHandler.detachPartitionData(table_name, partitionName, history_table_name);
			} else {
				// partitionName = MsHistNSQLDao.getOldestPartitionName(table_name, true);
				partitionName = ApKnpGlbl.getKnpGlbl("getOldestPartitionName", dbtype).getPmval1();
				partitionHandler.dropPartitionData(table_name, partitionName);
			}
			saveDoLog(table_name, partitionName, E_YES___.YES, operationType, currDate);
		} catch (Exception e) {
			saveDoLog(table_name, partitionName, E_YES___.NO, operationType, currDate);
			throw e;
		}

	}

	private static String getNextPartitionName(String currentPartitionName, E_PARTMODE partitionMode) {
		String nextPartitionName = "";
		if (partitionMode == E_PARTMODE.DAY) {
			nextPartitionName = PARTITION_PREFIX
					+ DateTools.dateAdd("D", currentPartitionName.replaceFirst(PARTITION_PREFIX, ""), 1);
		} else {
			nextPartitionName = PARTITION_PREFIX
					+ DateTools.dateAdd("M", currentPartitionName.replaceFirst(PARTITION_PREFIX, ""), 1);
		}

		return nextPartitionName;
	}

	/**
	 * 添加表分区操作日志
	 * 
	 * @param tbname
	 * @param tpname
	 * @param succfg
	 * @param dotype
	 */
	private static void saveDoLog(String tablna, String table_partition_name, E_YES___ sucess, E_DOTYPE dotype,
			String currDate) {
		final AplDataClean dolg = SysUtil.getInstance(AplDataClean.class);
		dolg.setTablna(tablna);
		dolg.setPartna(table_partition_name);
		dolg.setSuccfg(sucess);
		dolg.setDotype(dotype);
		dolg.setOperdt(currDate);
		DBTools.exeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				AplDataCleanDao.insert(dolg);
				return null;
			}
		});
	}
}
