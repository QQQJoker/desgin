package cn.sunline.clwj.hist.partition;

import cn.sunline.clwj.hist.partition.impl.DBTypeDB2PartitionImpl;
import cn.sunline.clwj.hist.partition.impl.DBTypeMysqlPartitionImpl;
import cn.sunline.clwj.hist.partition.impl.DefaultPartitionImpl;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DBTYPE;

/** 
* @author zhoujiawen: 
* @version 创建时间：2020年8月25日 下午5:32:23 
* 类说明 数据库类型工厂
*/
public class DBTypeFactory {

	public static IDBTypePartition getDBType(E_DBTYPE dbtype){
		if (E_DBTYPE.DB2 == dbtype) {
			return new DBTypeDB2PartitionImpl();
		} else if (E_DBTYPE.MYSQL == dbtype) {
			return new DBTypeMysqlPartitionImpl();
		} else {
			throw ExceptionUtil.wrapThrow("[%s]添加表分区暂未实现", dbtype.getValue());
			//throw ExceptionUtil.wrapThrow("[%s] adding table partitions is not implemented temporarily", dbtype.getValue());
		}
	}
	
	public static IDBTypePartition getDefaultPartitionImpl() {
		return new DefaultPartitionImpl();
	}
}
