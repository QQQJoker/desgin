package cn.sunline.ltts.busi.aptran.tools;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.model.database.Field;
import cn.sunline.ltts.busi.aplt.plugin.BaseApltPlugin;

public class ResultSetMapper<T> {


	private static final BizLog bizlog = BizLogUtil.getBizLog(ResultSetMapper.class);
	
	public static List<Object> mapRersultSetToObject(ResultSet rs,
			String tableName) {
		bizlog.method(">>>>>>>>>>>>>>>>> mapRersultSetToObject begin <<<<<<<<<<<<<<<<<<");
		List<Object> outputList = null;
		try {
			// make sure resultset is not null
			if (rs != null) {
				// get all the attributes of outputClass

				List<Field> propTableElements = FileexTools
						.getFields(tableName);
				while (rs.next()) {
					Class<?> clazz = BaseApltPlugin
							.getTableClazzByName(tableName);
					Object bean = SysUtil.getInstance(clazz);
					for (Field field : propTableElements) {
						Object columnValue = rs.getObject(field.getId());
						BeanUtils.setProperty(bean, field.getId(), columnValue);
					}
					if (outputList == null) {
						outputList = new ArrayList<Object>();
					}
					outputList.add(bean);
				}

			} else {
				return null;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		bizlog.method(">>>>>>>>>>>>>>>>> mapRersultSetToObject end <<<<<<<<<<<<<<<<<<");
		return outputList;
	}
}
