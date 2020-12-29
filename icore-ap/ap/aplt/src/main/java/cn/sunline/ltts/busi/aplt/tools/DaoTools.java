package cn.sunline.ltts.busi.aplt.tools;

import java.sql.Statement;
import java.util.List;

import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.dao.datasource.conn.DBConnectionManagerWrapper;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class DaoTools {
	private static final BizLog bizlog = BizLogUtil.getBizLog(DaoTools.class);
	/**
	 * 执行DDL语句特定使用，其他情况不要使用
	 * 注意说明：执行DDL语句不支持事务控制
	 * @param ddl
	 */
	public static void executeDDL(String ddl){
		try {
			Statement st = SysUtil.getInstance(DBConnectionManagerWrapper.class).getConnection().createStatement();
			st.execute(ddl);
			st.close();
		}
		catch (Throwable e) {
			bizlog.error(e.getMessage(),e);
			e.printStackTrace();
			throw ExceptionUtil.wrapThrow("执行DDL失败",e);
		}
	}
	/**
	 * 批量插入数据
	 * @param clazz
	 * @param list
	 */
	@SuppressWarnings("unchecked")
	public static void  execBatchInsert(Class clazz,List<?> list){
		try{
			DaoUtil.insertBatch(clazz, list);
		}catch(Exception e){
			bizlog.error(e.getMessage(),e);
			throw ExceptionUtil.wrapThrow("批量插入"+clazz+"失败",e);
		}
	}
	/**
	 * 批量更新数据
	 * @param clazz
	 * @param list
	 */
	public static void  execBatchUpdate(String namedsql,List<Object> list){
		try{
			DaoUtil.updateBatch(namedsql, list);
		}catch(Exception e){
			bizlog.error(e.getMessage(),e);
			throw ExceptionUtil.wrapThrow("命名sql"+namedsql+"批量更新失败",e);
		}
	}
}
