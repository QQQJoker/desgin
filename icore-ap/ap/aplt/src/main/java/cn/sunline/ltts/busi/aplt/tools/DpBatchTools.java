package cn.sunline.ltts.busi.aplt.tools;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jdjr.cds.driver.jdbc.CdsHelper;
import com.jdjr.cds.driver.rulebase.SplitKeyLocation;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.clwj.zdbank.cds.util.ShardingUtil;
import cn.sunline.edsp.base.lang.Params;

/**
 * <p>Title: SaBatchTools</p>
 * <p>Description: 批量记录条数分组并发工具类</p>
 * 
 * @author liuych
 * @date 2018年1月18日
 */
public class DpBatchTools {

    // 日志信息
    private static final BizLog bizlog = BizLogUtil.getBizLog(DpBatchTools.class);

    // map键值
    private static final String KEY = "sortno";

    // 字符分隔符
    private static final String SFENGF = ":";
    
    public static String SPLIT_KEY = "splitKey";


    /**
     * <p>Title: getBatchDataWalkerNew</p>
     * <p>Description: 获取数据遍历器内部封装(修订)</p>
     * 
     * @author liuych
     * @date 2018年1月18日
     * @param BFJL
     *        每个并发任务最大记录数
     * @param sNamedsql
     *        (临时文件数据准备的NameSql):程序为NameSql添加默认参数(默认参数为法人代码和查询不到记录是否报错,
     *        当调用程序没有传入这两个参数时,此方法自动赋值)
     * @param params
     *        NameSql对应的参数
     * @return ListBatchDataWalker
     */
    public static ListBatchDataWalker<Map> getBatchDataWalkerNew(long BFJL, String sNamedsql, Params params) {
        bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
        bizlog.parm("BFJL [%s]", BFJL);
        bizlog.parm("sNamedsql [%s]", sNamedsql);
        bizlog.parm("params [%s]", params);

        // 参数初始化
        if (CommUtil.isNull(params)) {
            params = new Params();
        }

        // 法人代码
        if (CommUtil.isNull(params.get("corpno"))) {
            params.add("corpno", CommTools.prcRunEnvs().getCorpno()); // 法人代码
        }

        // 每个并发任务最大记录数
        if (CommUtil.isNull(params.get("zdzybf"))) {
            params.add("zdzybf", BFJL); // 每个并发任务最大记录数
        }

        // 准备临时数据(保存List中)
        List<String> lst = DaoUtil.selectAll(sNamedsql, params);

        if (CommUtil.isNull(lst)) {
            lst = new ArrayList<String>();
        }

        // 结果集合实例化
        List<Map> lstMap = new ArrayList<Map>();
        String sStart = "";

        Map<String, String> mapResult;

        // 结果集赋值
        for (String sEnd : lst) {
            // map返回结果
            mapResult = new HashMap();

            // 添加结果集
            mapResult.put(KEY, sStart + SFENGF + sEnd);
            lstMap.add(mapResult);
            sStart = sEnd;
        }

        // 最后一个并发
        mapResult = new HashMap();
        mapResult.put(KEY, sStart + SFENGF + "");
        lstMap.add(mapResult);

        bizlog.parm("lstMap [%s]", lstMap);
        bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
        return new ListBatchDataWalker<java.util.Map>(lstMap);
    }

    /**
     * <p>Title: getJobBatchDataWalkerNew</p>
     * <p>Description: 获取作业数据遍历器内部封装(修订)</p>
     * 
     * @author liuych
     * @date 2018年1月18日
     * @param dataItem
     *        数据项
     * @param sNamedsql
     *        循环单条处理的sql
     * @param params
     *        程序为NameSql添加默认参数(默认参数为法人代码,起始ID,终止ID,查询不到记录是否报错,
     *        当调用程序没有传入这些个参数时,此方法自动赋值)
     * @return
     */
    public static <T> CursorBatchDataWalker<T> getJobBatchDataWalkerNew(Map<String, String> dataItem, String sNamedsql, Params params) {
        bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
        bizlog.parm("dataItem [%s]", dataItem);
        bizlog.parm("sNamedsql [%s]", sNamedsql);
        bizlog.parm("params [%s]", params);

        // 没有需要调整应加减利息的数据
        if (CommUtil.isNull(dataItem) || dataItem.size() == 0) {
            bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
            return null;
        }

        // 键值获取
        String sKeys = dataItem.get(KEY);

        // 键值解析
        String[] ArrayKey = sKeys.split(SFENGF);

        // 参数初始化
        if (CommUtil.isNull(params)) {
            params = new Params();
        }

        // 法人代码
        if (CommUtil.isNull(params.get("corpno"))) {
            params.add("corpno", CommTools.prcRunEnvs().getCorpno()); // 法人代码
        }

        // 起始ID
        if (CommUtil.isNull(params.get("qissjkid"))) {
            if (CommUtil.isNull(ArrayKey) || ArrayKey.length == 0) {
                params.add("qissjkid", ""); // 起始ID
            }
            else {
                params.add("qissjkid", ArrayKey[0]); // 起始ID
            }
        }

        // 终止ID
        if (CommUtil.isNull(params.get("zhzsjkid"))) {
            if (CommUtil.isNull(ArrayKey) || ArrayKey.length <= 1) {
                params.add("zhzsjkid", ""); // 终止ID
            }
            else {
                params.add("zhzsjkid", ArrayKey[1]);// 终止ID
            }
        }

        // 查不到记录是否报错
        if (CommUtil.isNull(params.get("nullException"))) {
            params.add("nullException", false); // 查不到记录是否报错
        }

        bizlog.parm("params [%s]", params);
        bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
        return new CursorBatchDataWalker<T>(sNamedsql, params);
    }

    
    /**
     * 批量交易数据获取器
     * 批量按KEY拆分模式，拆分键为splitKey
     * 获取分片表的数据库实例名及表的分片编号
     * @return
     */
    public static ListBatchDataWalker<Map> getBatchDataWalkerCDS() {
    	
    	bizlog.method(">>>>>>>>>>>> getBatchDataWalkerCDS Begin >>>>>>>>>>>>");
		List<Map> l = new ArrayList<>();
		List<BatchSplitKeyEntity> lstEntity = getSplitTableInfo();
		
		for (BatchSplitKeyEntity entity : lstEntity) {
			Map<String, String> m = new HashMap<>();
			m.put(SPLIT_KEY, SysUtil.serialize(entity));
			l.add(m);
		}
		bizlog.method("<<<<<<<<<<<< getBatchDataWalkerCDS End <<<<<<<<<<<<");
		return new ListBatchDataWalker<java.util.Map>(l);
    }
    /**
     * 交易作业数据获取器，获取分发数据，并组装SQL
     * SQL查询的数据集中应返回数据表中的分片键字段值,并在process方法进行设置
     * 分片表需要在表前拼接"${tbgrop}.",需要在表前拼接"_${suffix}"
     * 例如：${tbgrop}.knb_cbdl_${suffix}
     * @param dataItem 主控分发数据 BatchSplitKeyEntity
     * @param sNamedsql 数据获取SQLID
     * @param params 输入参数
     * @return
     */
    public static <T> CursorBatchDataWalker<T> getJobBatchDataWalkerCDS(Map<String, String> dataItem, String sNamedsql, Params params) {
    	bizlog.method(">>>>>>>>>>>> getJobBatchDataWalkerCDS Begin >>>>>>>>>>>>");
    	BatchSplitKeyEntity entity = SysUtil.deserialize(dataItem.get(SPLIT_KEY).toString(), BatchSplitKeyEntity.class);
		
		params.add("suffix", entity.getSuffix());
		params.add("tbgrop", entity.getDbgrop());
		bizlog.method(">>>>>>>>>>>> getJobBatchDataWalkerCDS END >>>>>>>>>>>>");
		return new CursorBatchDataWalker<T>(sNamedsql, params);
		
    }
    /**
     * 获取分片表相关的信息
     * @return
     */
    public static List<BatchSplitKeyEntity> getSplitTableInfo() {
    	String clusterId = ShardingUtil.getRouteConfig().getClusterId();
		String tableName = ShardingUtil.getRouteConfig().getSktable();
		return getSplitTableInfo(clusterId, tableName);
    }
    
    /**
     * 获取分片表相关的信息
     * @return
     */
    private static List<BatchSplitKeyEntity> getSplitTableInfo(String clusterId, String tableName) {
    	
		bizlog.method("clusterID:[%s], tableName:[%s]", clusterId, tableName);
		Set<SplitKeyLocation> st = new HashSet<>();
		try {
			st = CdsHelper.getTableLocation(clusterId, tableName);
		} catch (SQLException e) {
			bizlog.error("======>>>获取分片表数据失败");
			throw new RuntimeException(e);
		}
		Iterator<SplitKeyLocation> i = st.iterator();
		List<BatchSplitKeyEntity> l = new ArrayList<>();
		while (i.hasNext()) {
			SplitKeyLocation k = i.next();
			BatchSplitKeyEntity entity = SysUtil.getInstance(BatchSplitKeyEntity.class);
			entity.setDbgrop(k.getDbConnGroup().getGroupEntity().getGroupName());
			entity.setSuffix(k.getTableSuffix());
			bizlog.method(String.format("数据库库名:[%s],数据表名:[%s]", k.getDbConnGroup().getGroupEntity().getGroupName(), k.getTableSuffix()));
			l.add(entity);
		}
		
		return l;
    }
    
}
