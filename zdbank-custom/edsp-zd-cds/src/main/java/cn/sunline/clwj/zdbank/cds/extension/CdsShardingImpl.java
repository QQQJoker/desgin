package cn.sunline.clwj.zdbank.cds.extension;

import java.sql.SQLException;

import com.jdjr.cds.driver.jdbc.CdsHelper;
import com.jdjr.cds.driver.rulebase.SplitKeyLocation;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.service.engine.spi.IServiceShardingManager;
import cn.sunline.adp.metadata.base.dao.OperateTypeEnum;
import cn.sunline.clwj.zdbank.cds.util.CDSConstants;
import cn.sunline.clwj.zdbank.cds.util.ShardingUtil;

public class CdsShardingImpl implements IServiceShardingManager{

	private static final SysLog logger = SysLogUtil.getSysLog(CdsShardingImpl.class);

	
	@Override
	public boolean isSameShardingGroup(String paramString1, String paramString2) {
		
		if(ShardingUtil.getRouteConfig() == null) {  // 没有开启路由，没有跨库事务
			return true;
		}
		
		// 如果某个分片值是999 说明是有事务操作孤立库，也是属于跨库，需要提交事务
		if(CDSConstants.DEFAULT_SHARDING_ID.equals(paramString1) || CDSConstants.DEFAULT_SHARDING_ID.equals(paramString2) ) {
			return false;
		}
		
		String tableName = ShardingUtil.getRouteConfig().getSktable();
		String clusterName = ShardingUtil.getRouteConfig().getClusterId();

        SplitKeyLocation shardOne = null;
        SplitKeyLocation shardTwo = null;
       
		try {
			shardOne = CdsHelper.getTableLocation(clusterName, tableName, paramString1);
			shardTwo = CdsHelper.getTableLocation(clusterName, tableName, paramString2);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		String shardDb1 = shardOne.getDbConnGroup().getActiveConnUnit().getIp()+shardOne.getDbConnGroup().getActiveConnUnit().getDbName();
		
		String shardDb2 = shardTwo.getDbConnGroup().getActiveConnUnit().getIp()+shardTwo.getDbConnGroup().getActiveConnUnit().getDbName();
		
		boolean falg = CommUtil.compare(shardDb1, shardDb2) == 0;
		
		logger.debug("进行分片判断:主分片[%s]-组[%s],当前分片[%s]-组[%s]，结果[%s].",paramString1,shardDb1,paramString2,shardDb2);
		return falg;
	}

	@Override
	public String getSqlShardingKeyString(String paramString1, OperateTypeEnum paramOperateTypeEnum,
			String paramString2) {
		return null;
	}

}
