package cn.sunline.ltts.busi.bsap.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.plugin.BaseApltPlugin;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.metadata.model.database.Table;

/**
 * <p>
 * 文件功能说明：分表对象反射调用工具类
 * <li>比如：将KnsTran分为32张表，KnsTran为0号,KnsTran1为2号表...，通过流水号32Hash获得分表号
 * <li>那么：通过DaoSplitInvokeUtil.insert(knsTran, "10");来完成对11号表的insert操作...
 * <li>或： 通过DaoSplitInvokeUtil.insert("kns_tran",knsTranMap,"10");
 * <li>通过DaoSplitInvokeUtil.selectOne(KnsTran.class, "selectOne_odb1",
 * 10,trandt,transq);按日期和流水号参数来完成对11号表的selectOne操作...
 * </p>
 */
public abstract class DaoSplitInvokeUtil {

	private static final BizLog bizlog = BizLogUtil.getBizLog(DaoSplitInvokeUtil.class);

	/**
	 * 插入分表记录
	 * 
	 * @param entity
	 *            表实体（分表需要共用同一表对象保存数据）
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @return
	 */
	public static int insert(Object entity, String setCode) {
		Object covEntity = covTableBean(entity, setCode);
		//return (Integer) execute(entity.getClass(), "insert", setCode, entity);
		return (Integer) execute(entity.getClass(), "insert", setCode, covEntity);
	}

	/**
	 * 插入分表记录
	 * 
	 * @param tableName
	 *            必须为不带分表号的表明（区分大小写）
	 * @param para
	 *            Map对象参数
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @return
	 */
	public static int insert(String tableName, Map<Object, Object> para, String setCode) {
		Object entity = convert(tableName, para);
		return (Integer) execute(entity.getClass(), "insert", setCode, entity);
	}

	/**
	 * @param entity
	 *            表实体（分表需要共用同一表对象保存数据）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @return
	 */
	public static int update(Object entity, String method, String setCode) {
		
		Object covEntity = covTableBean(entity, setCode);
		return (Integer) execute(entity.getClass(), method, setCode, covEntity);
		//return (Integer) execute(entity.getClass(), method, setCode, entity);
	}

	/**
	 * 修改分表记录
	 * 
	 * @param tableName
	 *            必须为不带分表号的表明（区分大小写,如：kns_tran或kns_tran11）
	 * @param para
	 *            Map对象参数
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @return
	 */
	public static int update(String tableName, Map<Object, Object> para, String method, String setCode) {
		Object entity = convert(tableName, para);
		return (Integer) execute(entity.getClass(), method, setCode, entity);
	}

	/**
	 * @param clazz
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @param objects
	 *            输入参数（查询条件）
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T selectOne(Class<T> clazz, String method, String setCode, Object... objects) {
		return (T) execute(clazz, method, setCode, objects);	
	}

	/**
	 * 查询分表一条记录记录
	 * 
	 * @param tableName
	 *            必须为不带分表号的表明（区分大小写）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @param objects
	 *            输入参数（查询条件）
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T selectOne(String tableName, String method, String setCode, Object... objects) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		return (T) execute(clazz, method, setCode, objects);
	}

	/**
	 * @param clazz
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @param objects
	 *            输入参数（查询条件）
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T selectOneWithLock(Class<T> clazz, String method, String setCode, Object... objects) {
		return (T) execute(clazz, method, setCode, objects);
	}

	/**
	 * 带锁查询分表一条记录记录
	 * 
	 * @param tableName
	 *            必须为不带分表号的表明（区分大小写,如：kns_tran或kns_tran11）
	 * @param para
	 *            Map对象参数
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T selectOneWithLock(String tableName, String method, String setCode, Object... objects) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		return (T) execute(clazz, method, setCode, objects);
	}

	/**
	 * @param clazz
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @param objects
	 *            输入参数（查询条件）
	 * @return
	 */
	public static Object selectAll(Class<?> clazz, String method, String setCode, Object... objects) {
		return execute(clazz, method, setCode, objects);
	}

	public static Object selectAll(String tableName, String method, String setCode, Object... objects) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		return execute(clazz, method, setCode, objects);
	}

	/**
	 * @param clazz
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @param objects
	 *            输入参数（查询条件）
	 * @return
	 */
	public static int deleteOne(Class<?> clazz, String method, String setCode, Object... objects) {
		return (Integer) execute(clazz, method, setCode, objects);
	}

	public static Object deleteOne(String tableName, String method, String setCode, Object... objects) {
		Class<?> clazz = BaseApltPlugin.getTableClazzByName(tableName);
		return (Integer) execute(clazz, method, setCode, objects);
	}

	/**
	 * @param beanClass
	 *            表实体类（用于获取表DAO类及输出类型指定）
	 * @param method
	 *            调用的方法名（通过反射调用方法）
	 * @param setCode
	 *            分表号(注：0表示第1号分表，且表名不带'0')
	 * @param objects
	 *            输入参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Object execute(Class<?> beanClass, String method, String setCode, Object... objects) {

		Class<?> targetClass = getDaoClass(beanClass, setCode);
		bizlog.debug("类名[%s]调用方法[%s]", targetClass.getSimpleName(), method);
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
				Method m = targetClass.getDeclaredMethod(method, classArray);
				result = m.invoke(SysUtil.getInstance(targetClass), objects);
			} else {
				Method m = targetClass.getDeclaredMethod(method);
				result = m.invoke(SysUtil.getInstance(targetClass));
			}

			if (CommUtil.isNull(result)) {
				return null;
			}

			// 如果返回为List，需要转化原表对象返回，目前只支持返回对象类型为特定
			if (result instanceof List) {
				List<Object> oldList = (List<Object>) result;
				List<Object> newList = new ArrayList<Object>();
				for (Object oldObj : oldList) {
					Object newObj = SysUtil.getInstance(beanClass);
					CommUtil.copyProperties(newObj, oldObj);
					newList.add(newObj);
				}
				return newList;
			}

		/*	// 如果调用返回的对象为分表对象，需要转为原表对象再返回
			if (isSameTableClass(result.getClass(), beanClass)) {
				Object retObj = SysUtil.getInstance(beanClass);
				CommUtil.copyProperties(retObj, result);
				return retObj;
			}*/	
			// 如果调用返回的对象为分表对象，需要转为原表对象再返回
			if (isSameTableClass(result.getClass(), getTableBeanClass(beanClass, setCode))) {
				Object retObj = SysUtil.getInstance(beanClass);
				CommUtil.copyProperties(retObj, result);
				return retObj;
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
		return (T) entity;
	}

	/**
	 * 获得分表的Dao类
	 * 
	 * @param clazz
	 *            表结构java类
	 * @param setCode
	 *            分表号
	 * @return 表DAO的java类
	 */
	private static Class<?> getDaoClass(Class<?> clazz, String setCode) {
		StringBuffer className = new StringBuffer();
		String[] strArray = clazz.getName().split("\\$");
		className.append(strArray[0]);
		className.append("$");
		Table table = OdbFactory.getTable(clazz);
		className.append(table.getId());
		if (setCode != null && !CommUtil.equals(setCode, "0") && !CommUtil.equals(setCode, "")) {
			className.append(setCode);
		}
		className.append("Dao");
		Class<?> clazzRet = null;
		try {
			clazzRet = Class.forName(className.toString());
		} catch (ClassNotFoundException e) {
			bizlog.error("找不到类异常", e);
			throw Aplt.E0000("找不到类异常");
		}
		return clazzRet;
	}

	/*private static Class<?> getDaoClass(Class<?> clazz, String setCode) {
		if (clazz.getSimpleName().endsWith(setCode)) { // 以分表号结尾的认为是分表对象，而不是原表对象，则按分表取类对象
			return _getDaoClass(clazz, null);
		} else {
			return _getDaoClass(clazz, setCode);
		}
	}
*/
	/**
	 * 判断两个类是不是仅仅分表号不同的表对象
	 * 
	 * @param clazz1
	 * @param clazz2
	 * @return
	 */
	private static boolean isSameTableClass(Class<?> clazz1, Class<?> clazz2) {
		if (CommUtil.isNull(clazz1) || CommUtil.isNull(clazz2)) {
			return false;
		}
		if (CommUtil.equals(clazz1.getName().split("\\$\\$")[0], clazz2.getName().split("\\$\\$")[0])) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * 
	 * @param obj
	 * @param postfix
	 * @return
	 */
	private static Object covTableBean(Object obj, String postfix) {
		bizlog.debug("转化前类名[%s]", obj.getClass().getSimpleName());
		StringBuffer className = new StringBuffer();
		className.append(obj.getClass().getName().split("\\$\\$")[0]);
		if(!CommUtil.equals(postfix, "0") && !CommUtil.equals(postfix, "")){
			className.append(postfix);
		}
		Object tableBean = null;
		try {
			tableBean = SysUtil.getInstance(Class.forName(className.toString()));
			CommUtil.copyProperties(tableBean, obj);
			bizlog.debug("转化后类名[%s]", tableBean.getClass().getSimpleName());
		}
		catch (ClassNotFoundException e) {
			bizlog.error("找不到类异常", e);
			throw Aplt.E0000("找不到类异常");
		}
		return tableBean;
	}
	
	/**
	 * @param clazz
	 * @param postfix
	 * @return
	 */
	private static Class<?> getTableBeanClass(Class<?> clazz, String postfix) {
		StringBuffer className = new StringBuffer();
		className.append(clazz.getName().split("\\$\\$")[0]);
		if(!postfix.equals("0") && !postfix.equals("")){
			className.append(postfix);
		}
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
