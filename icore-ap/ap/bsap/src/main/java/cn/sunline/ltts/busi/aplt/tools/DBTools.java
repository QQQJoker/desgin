package cn.sunline.ltts.busi.aplt.tools;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;

/**
 * <p>
 * 文件功能说明：
 * 
 * </p>
 * 
 * @Author T
 *         <p>
 *         <li>2014年6月20日-下午5:36:11</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228T：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class DBTools {
    private static final BizLog bizlog = BizLogUtil.getBizLog("binlogSync", DBTools.class);

    public static <T> T exeInNewTransation(RunnableWithReturn<T> run) {
        return DaoUtil.executeInNewTransation(run);
    }

    /**
     * 
     * @Author Administrator
     *         <p>
     *         <li>2015年1月20日-上午10:57:34</li>
     *         <li>功能说明：提交一个事物，并新起一个</li>
     *         </p>
     */
    public static void commit() {

        DaoUtil.commitTransaction();

        DaoUtil.beginTransaction();
    }

    /**
     * 
     * @Author Administrator
     *         <p>
     *         <li>2015年1月20日-上午10:57:52</li>
     *         <li>功能说明：回滚一个事物，并新起一个</li>
     *         </p>
     */
    public static void rollback() {

        DaoUtil.rollbackTransaction();

        DaoUtil.beginTransaction();
    }

    /**
     * 注意说明：执行DDL语句不支持事务控制
     * 非特殊情况，不建议使用
     * 
     * @param sql
     */
    public static void executeSQL(String sql) {
        try {
            Statement st = EdspCoreBeanUtil.getDBConnectionManager().getConnection().createStatement();
            st.execute(sql);
        } catch (Throwable e) {
            throw ExceptionUtil.wrapThrow("执行SQL失败", e);
        }
    }

    /* public static void executeSQL(String sql,Object ... params) {
         try {
             PreparedStatement prepareStatement = EdspCoreBeanUtil.getDBConnectionManager().getConnection().prepareStatement(sql);
             for(int i=0;i<params.length;i++)
                 prepareStatement.setObject(i+1, params[i]);
             prepareStatement.execute();
         } catch (Throwable e) {
             throw ExceptionUtil.wrapThrow("执行SQL失败", e);
         }
     }*/

    public static void executeSQL(String sql, Object[] params) {

        try {

            PreparedStatement prepareStatement = EdspCoreBeanUtil.getDBConnectionManager().getConnection().prepareStatement(sql);

            for (int i = 0; i < params.length; i++)
            {
                prepareStatement.setObject(i + 1, params[i]);
            }
            prepareStatement.execute();
        } catch (Throwable e) {
            bizlog.error("prepareStatement.execute()执行SQL失败,异常:[%s]", e);
            throw ExceptionUtil.wrapThrow("执行SQL失败", e);

        }

    }
    
    public static ResultSet executeQuery(String sql, Object[] params){
    	ResultSet resultSet = null;
        try {
            PreparedStatement prepareStatement = EdspCoreBeanUtil.getDBConnectionManager().getConnection().prepareStatement(sql);

            for (int i = 0; i < params.length; i++){
                if(params[i].equals("null")){
                    prepareStatement.setObject(i + 1, null);
                }else{
                    prepareStatement.setObject(i + 1, params[i]);
                }
            }
            resultSet = prepareStatement.executeQuery();
        } catch (Throwable e) {
            throw ExceptionUtil.wrapThrow("执行SQL失败", e);
        }
		return resultSet;
    	
    }
    public static ResultSet executeQuery(String sql){
    	ResultSet resultSet = null;
        try {
            PreparedStatement prepareStatement = EdspCoreBeanUtil.getDBConnectionManager().getConnection().prepareStatement(sql);
            resultSet = prepareStatement.executeQuery();
        } catch (Throwable e) {
            throw ExceptionUtil.wrapThrow("执行SQL失败", e);
        }
		return resultSet;
    	
    }
}
