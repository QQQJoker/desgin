package cn.sunline.ltts.busi.bsap.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.ltts.busi.aplt.plugin.BaseApltPlugin;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.metadata.model.database.Table;

/**
 * <p>
 * 文件功能说明：表对象反射调用工具类
 * </p>
 */
public abstract class DaoInvokeUtil {

	private static final BizLog bizlog = BizLogUtil.getBizLog(DaoInvokeUtil.class);

	/**
	 * @param entity
	 *            表实体（分表需要共用同一表对象保存数据）
	 * @return
	 */
	public static int insert(Object entity) {
		return (Integer) execute(entity.getClass(), "insert", entity);
	}

	public static int insert(String tableName, Map<Object, Object> para) {
		Object entity = convert(tableName, para);
		return (Integer) execute(entity.getClass(), "insert", entity);
	}

	/**
	 * @param entity
	 *            表实体（分表需要共用同一表对象保存数据）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @return
	 */
	public static int update(Object entity, String method) {
		return (Integer) execute(entity.getClass(), method, entity);
	}

	public static int update(String tableName, Map<Object, Object> para, String method) {
		Object entity = convert(tableName, para);
		return (Integer) execute(entity.getClass(), method, entity);
	}

	/**
	 * @param clazz
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param objects
	 *            输入参数（查询条件）
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T selectOne(Class<T> clazz, String method, Object... objects) {
		return (T) execute(clazz, method, objects);
	}

	@SuppressWarnings("unchecked")
	public static <T> T selectOne(String tableName, String method, Object... objects) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		return (T) execute(clazz, method, objects);
	}

	/**
	 * @param clazz
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param objects
	 *            输入参数（查询条件）
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T selectOneWithLock(Class<T> clazz, String method, Object... objects) {
		return (T) execute(clazz, method, objects);
	}

	@SuppressWarnings("unchecked")
	public static <T> T selectOneWithLock(String tableName, String method, Object... objects) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		return (T) execute(clazz, method, objects);
	}
	
	/**
	 * @param clazz
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param objects
	 *            输入参数（查询条件）
	 * @return
	 */
	public static Object selectAll(Class<?> clazz, String method, Object... objects) {
		return execute(clazz, method, objects);
	}
	
	public static Object selectAll(String tableName, String method, Object... objects) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		return execute(clazz, method, objects);
	}

	/**
	 * @param clazz
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param objects
	 *            输入参数（查询条件）
	 * @return
	 */
	public static int deleteOne(Class<?> clazz, String method, Object... objects) {
		return (Integer) execute(clazz, method, objects);
	}
	
	public static Object deleteOne(String tableName, String method, Object... objects) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		return (Integer) execute(clazz, method, objects);
	}

	/**
	 * @param beanClass
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param objects
	 *            输入参数
	 * @return
	 */
	private static Object execute(Class<?> beanClass, String method, Object... objects) {
		Class<?> daoClass = getDaoClass(beanClass);
		bizlog.debug("类名[%s]调用方法[%s]", daoClass.getSimpleName(), method);
		Object result = null;
		try {
			Class<?>[] classArray = null;
			if (CommUtil.isNotNull(objects) && objects.length > 0) {
				classArray = new Class<?>[objects.length];
				for (int i = 0; i < objects.length; i++) {
					if (objects[i] instanceof Boolean) {
						classArray[i] = boolean.class;
						continue;
					}
					String reqObjclassName = objects[i].getClass().getName();
					classArray[i] = Class.forName(reqObjclassName.split("\\$\\$")[0]);
				}
			}
			if (CommUtil.isNotNull(classArray)) {
				Method m = daoClass.getDeclaredMethod(method, classArray);
				result = m.invoke(SysUtil.getInstance(daoClass), objects);
			} else {
				Method m = daoClass.getDeclaredMethod(method);
				result = m.invoke(SysUtil.getInstance(daoClass));
			}

			if (CommUtil.isNull(result)) {
				return null;
			}

		} catch (AdpBusinessException e) {
			bizlog.error("业务异常", e);
			throw e;
		} catch (NoSuchMethodException e) {
			bizlog.error("找不到类方法异常", e);
			throw Aplt.E0000("找不到类方法异常");
		} catch (ClassNotFoundException e1) {
			bizlog.error("找不到类异常", e1);
			throw Aplt.E0000("找不到类异常");
		} catch (SecurityException e) {
			bizlog.error("获取方法名失败", e);
			throw Aplt.E0000("获取方法名失败");
		} catch (IllegalAccessException e) {
			bizlog.error("调用失败", e);
			throw Aplt.E0000("调用失败");
		} catch (IllegalArgumentException e) {
			bizlog.error("调用失败", e);
			throw Aplt.E0000("调用失败");
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException) e.getTargetException();
			} else {
				bizlog.error("调用失败", e);
				throw Aplt.E0000("调用失败");
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T convert(String tableName, Map<Object, Object> para) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		Object entity = SysUtil.getInstance(clazz);
		CommUtil.copyPropertiesWithTypeConvert(entity, para, true);
		return (T)entity;
	}
	
	/**
	 * @param clazz 表结构java类
	 * @return 表DAO的java类
	 */
	private static Class<?> getDaoClass(Class<?> clazz) {
		StringBuffer className = new StringBuffer();
		String[] strArray = clazz.getName().split("\\$");
		className.append(strArray[0]);
		className.append("$");
		Table table = OdbFactory.getTable(clazz);
//		className.append(strArray[1].substring(0, 1).toUpperCase());
//		className.append(strArray[1].substring(1, strArray[1].length()));
		className.append(table.getId());
		className.append("Dao");
		Class<?> clazzRet = null;
		try {
			clazzRet = Class.forName(className.toString());
		}
		catch (ClassNotFoundException e) {
			bizlog.error("找不到类异常", e);
			throw Aplt.E0000("找不到类异常");
		}
		return clazzRet;
	}	
	
}
