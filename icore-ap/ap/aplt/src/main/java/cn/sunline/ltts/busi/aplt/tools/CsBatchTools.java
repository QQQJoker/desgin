package cn.sunline.ltts.busi.aplt.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Params;

/**
 * 批量记录条数分组并发工具类
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author ex_kjkfb_liuych
 *         <p>
 *         <li>2016年3月4日-上午11:14:41</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228ex_kjkfb_liuych：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */

public class CsBatchTools {

	// 日志
	private static final BizLog bizlog = BizLogUtil.getBizLog(CsBatchTools.class);

	// map键值
	private static final String KEY = "xuhao";

	// 字符分割符
	private static final String SFENGF = ":";

	/**
	 * 获取数据遍历器内部封装(修订)
	 * 
	 * @Author ex_kjkfb_liuych
	 *         <p>
	 *         <li>2016年4月9日-上午9:25:28</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param lBfjl
	 *            每个并发任务最大记录数
	 * @param sNamedsql
	 *            (临时文件数据准备的NameSql):程序为NameSql添加默认参数(默认参数为法人代码和查询不到记录是否报错,
	 *            当调用程序没有传入这两个参数时,此方法自动赋值)
	 * @param params
	 *            NameSql对应的参数
	 * @return
	 */
	public static ListBatchDataWalker<Map<String, String>> getBatchDataWalkerNew(long BFJL, String sNamedsql, Params params) {
		bizlog.method(">>>>>>>>>>>>Begin>>>>>>>>>>>>");
		bizlog.parm("BFJL [%s]", BFJL);
		bizlog.parm("sNamedsql [%s]", sNamedsql);
		bizlog.parm("params [%s]", params);

		// 参数初始化
		if (CommUtil.isNull(params)) {
			params = new Params();
		}

		// 法人代码
		if (CommUtil.isNull(params.get("farendma"))) {
			params.add("farendma", CommTools.prcRunEnvs().getCorpno()); // 法人代码
		}

		// 每个并发任务最大记录数
		if (CommUtil.isNull(params.get("zuxuhaoo"))) {
			params.add("zuxuhaoo", BFJL); // 每个并发任务最大记录数
		}

		// 准备临时数据(保存List中)
		List<String> lst = DaoUtil.selectAll(sNamedsql, params);

		if (CommUtil.isNull(lst)) {
			lst = new ArrayList<String>();
		}

		// 结果集合实例化
		List<Map<String, String>> lstMap = new ArrayList<Map<String, String>>();
		String sStart = "";

		Map<String, String> mapResult;

		// 结果集赋值
		for (String sEnd : lst) {
			// map返回结果
			mapResult = new HashMap<String, String>();

			// 添加结果集
			mapResult.put(KEY, sStart + SFENGF + sEnd);
			lstMap.add(mapResult);
			sStart = sEnd;
		}

		// 最后一个并发
		mapResult = new HashMap<String, String>();
		mapResult.put(KEY, sStart + SFENGF + "");
		lstMap.add(mapResult);

		bizlog.parm("lstMap [%s]", lstMap);
		bizlog.method("<<<<<<<<<<<<End<<<<<<<<<<<<");
		return new ListBatchDataWalker<Map<String, String>>(lstMap);
	}

	/**
	 * 获取作业数据遍历器内部封装(修订)
	 * 
	 * @Author ex_kjkfb_liuych
	 *         <p>
	 *         <li>2016年4月9日-上午9:27:42</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param dataItem
	 *            数据项
	 * @param sNamedsql
	 *            循环单条处理的sql
	 * @param params
	 *            程序为NameSql添加默认参数(默认参数为法人代码,起始ID,终止ID,查询不到记录是否报错,
	 *            当调用程序没有传入这些个参数时,此方法自动赋值)
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
		if (CommUtil.isNull(params.get("farendma"))) {
			params.add("farendma", CommTools.prcRunEnvs().getCorpno()); // 法人代码
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

}
