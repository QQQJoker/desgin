package cn.sunline.ltts.busi.aptran.tools;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.model.database.Field;
import cn.sunline.adp.metadata.model.database.Table;
import cn.sunline.ltts.busi.aplt.plugin.BaseApltPlugin;
import cn.sunline.ltts.busi.aplt.tables.SysSynFileTables.AppDataMove;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;

public class FileexTools {

	private static final String lstrdt = CommToolsAplt.prcRunEnvs().getLstrdt();

	private static final String separators = "|@|";

	public static List<?> selectObject(String sql, String className) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Object[] objects = {};
		DBTools.executeSQL(sql);
		ResultSet resultSet = DBTools.executeQuery(sql, objects);
		ResultSetMetaData md;
		try {
			md = resultSet.getMetaData(); // 获得结果集结构信息,元数据
			int columnCount = md.getColumnCount(); // 获得列数
			while (resultSet.next()) {
				Map<String, Object> rowData = new HashMap<String, Object>();
				for (int i = 1; i <= columnCount; i++) {
					rowData.put(md.getColumnName(i), resultSet.getObject(i));
				}
				list.add(rowData);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	public static List<?> selectObject(String sql, String className,
			Object... objects) {
		DBTools.executeSQL(sql);
		ResultSet resultSet = DBTools.executeQuery(sql, objects);
		List<?> list = (List<?>) ResultSetMapper.mapRersultSetToObject(
				resultSet, className);
		return list;
	}

	// 生成文件
	public static LttsFileWriter createFile(AppDataMove appDataMove) {
		KnpPara knpPara = CommTools.KnpParaQryByCorpno("SyncFile", "ApFile", "%",
				"%", false);
		if (CommUtil.isNull(knpPara)) {
			throw ApError.Aplt.E0001("没有配置文件路劲，请核对！！！");
		}
		String filePath = knpPara.getPmval1() + lstrdt;
		String fileName = appDataMove.getDaorbm() + "_"
				+ appDataMove.getDacdcn() + "_" + appDataMove.getDardcn() + "_"
				+ lstrdt + knpPara.getPmval4();
		return new LttsFileWriter(filePath, fileName, "UTF-8");
	}

	/**
	 * 
	 * <p>
	 * Title:appendEntity
	 * </p>
	 * <p>
	 * Description: 根据给定的数组、对象，拼接出要输出的数据字符串
	 * </p>
	 * 
	 * @author Xiaoyu Luo
	 * @date 2017年12月11日
	 * @param fields
	 * @param entity
	 * @return
	 */
	public static StringBuilder appendEntity(String tableName, Object entity) {
		List<Field> fields = getFields(tableName);
		Map<String, Object> prodMap = CommUtil.toMap(entity);
		StringBuilder prodInfo = SysUtil.getInstance(StringBuilder.class);
		int index = 0;
		for (Field f : fields) {
			Object fieldObj = prodMap.get(f.getId());
			String fieldName = f.getId();
			String fieldInfo;
			String fieldValue = CommUtil.isNotNull(fieldObj) ? fieldObj
					.toString() : "";
			fieldInfo = fieldName.toUpperCase() + "=" + fieldValue;
			prodInfo.append(fieldInfo);
			if (index < fields.size() - 1) {
				prodInfo.append(separators);
			}
			index++;
		}
		prodInfo = prodInfo.append("/n");
		return prodInfo;
	}

	public static List<Field> getFields(String tableName) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		Table propTableByName = BaseApltPlugin.getTableByName(clazz);// 属性表模型
		List<Field> propTableElements = propTableByName.getAllElements();// 属性表字段
		return propTableElements;
	}
}
