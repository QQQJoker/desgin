package cn.sunline.ltts.busi.aplt.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.metadata.model.RestrictionType;
import cn.sunline.adp.metadata.model.database.Field;
import cn.sunline.adp.metadata.model.database.Table;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppPropCtrl;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppPropCtrlDao;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_FILDTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_NULLFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public class PropUtil {
	private static final BizLog bizlog = BizLogUtil.getBizLog(PropUtil.class);
	private static final String _SEPARATOR_STR = ",";// 分隔符

	/**
	 * 根据指定的类型获得相应的对象，并进行赋值。
	 * 
	 * @param clazz
	 *            指定类型
	 * @param src
	 *            原数据
	 * @return 指定类型对象实例
	 */
	public static <T> T getAssignTypeFromObject(Class<T> clazz, Object src) {
		T ret = SysUtil.getInstance(clazz);
		CommUtil.copyPropertiesWithTypeConvert(ret, src, false);
		if (bizlog.isInfoEnabled())
			bizlog.info("属性对象[" + clazz.getSimpleName() + "]结果为：" + ret);
		return ret;
	}

	/**
	 * 检查并覆盖产品属性
	 * <p>
	 * 注意：此限制配置，不对产品配置是否正确进行核查
	 * 
	 * @param clazz
	 *            用于返回的账户属性表类型
	 * @param prodcd
	 *            产品代码
	 * @param acctProp
	 *            账户属性
	 * @param fileProp
	 *            产品属性
	 * @return 返回账户属性表对象
	 */
	public static <T> T checkAndFixPropValue(Class<T> clazz, String prodcd, Object acctProp, Object fileProp) {

		Map<String, Object> acctMap = CommUtil.toMap(acctProp);
		Map<String, Object> fileMap = CommUtil.toMap(fileProp);
		Map<String, Object> retMap = new HashMap<String, Object>();

		Table table = OdbFactory.getTable(clazz); // 获得表定义
		Map<String, AppPropCtrl> ctrlMap = getCtrl(prodcd, table.getName()); // 获得属性控制定义
		for (Field f : table.getAllElements()) {
			String key = f.getId();
			AppPropCtrl ctrl = ctrlMap.get(key);
			Object obj;
			if (CommUtil.isNull(ctrl)) {
                obj = getValueIfCtrlNull(key, acctMap, fileMap);
            } else {
                obj = checkAndGetFieldValue(key, ctrl, acctMap, fileMap);
            }
			retMap.put(key, obj);
		}
		if (bizlog.isInfoEnabled())
			bizlog.info("获得的属性对象retMap：" + retMap);

		return getAssignTypeFromObject(clazz, retMap);
	}
	
	/**
	 * 重组两个属性，如果inputProp对应属性有值，则替换dbProp对应属性，最终返回dbProp
	 * @param clazz
	 * @param inputProp
	 * @param dbProp
	 * @return
	 */
	public static <T> T propExchange(Class<T> clazz,Object inputProp,Object dbProp){
		Map<String, Object> inputPropMap = CommUtil.toMap(inputProp);
		Map<String, Object> dbPropMap = new HashMap<>();
		dbPropMap.putAll(CommUtil.toMap(dbProp));
		for(String fieldName:inputPropMap.keySet()){
			Object fieldValue = inputPropMap.get(fieldName);
			if(CommUtil.isNotNull(fieldValue)){
				dbPropMap.put(fieldName, fieldValue);
			}
		}
		return getAssignTypeFromObject(clazz,dbPropMap);
	}
	
	/**
	 * 重组两个属性，如果inputProp对应属性有值，则替换dbProp对应属性，最终返回dbProp
	 * @param clazz
	 * @param inputProp
	 * @param dbProp
	 * @return
	 */
	public static <T> T propExchange(Class dpClazz,Class<T> clazz,Object inputProp,Object dbProp){
		Map<String, Object> inputPropMap = CommUtil.toMap(inputProp);
		Map<String, Object> dbPropMap = CommUtil.toMap(dbProp);
		Table table = OdbFactory.getTable(dpClazz); // 获得表定义
		for (Field field : table.getAllElements()) {
			String fieldName = field.getId();
			Object fieldValue = inputPropMap.get(fieldName);
			if(CommUtil.isNotNull(fieldValue)){
				dbPropMap.put(fieldName, fieldValue);	
			}
		}
		return getAssignTypeFromObject(clazz,dbPropMap);
	}

	// 未定义控制：检查并获得属性值
	private static Object checkAndGetFieldValue(String fildcd, AppPropCtrl ctrl, Map<String, Object> acctMap,
			Map<String, Object> fileMap) {

		// 获得属性值
		boolean acctExists = acctMap.containsKey(fildcd); // 账户属性存在标志
		boolean fileExists = fileMap.containsKey(fildcd); // 产品属性存在标志
		Object acctVal = acctMap.get(fildcd);
		Object fileVal = fileMap.get(fildcd);

//		if (ctrl == null) {
//			return CommUtil.isNotNull(acctVal) ? acctVal : fileVal;
//		}

		RetVal retVal = null;
		if (acctExists && fileExists) {
			retVal = getOverrideValue(ctrl, acctVal, fileVal);
		} else if (acctExists) {
			retVal = getOverrideValue(ctrl, acctVal, null);
		} else if (fileExists) {
			retVal = new RetVal();
			retVal.value = fileVal;
			retVal.fromProduct = true;
		} else {
			retVal = new RetVal();
			retVal.value = null;
		}

		// 若直接从产品属性中获得的属性值，不需要检查，直接返回（此限制配置，不对产品配置是否正确进行核查）
		if (retVal.fromProduct) {
			return retVal.value;
		}

		// 若属性值为空，若允许直接返回，否则报错
		if (CommUtil.isNull(retVal.value)) {
			if (ctrl.getNullab() == E_YES___.NO)
				throw ApError.Aplt.E0000("产品[" + ctrl.getProdcd() + "]属性[" + ctrl.getFildcd() + "]不允许为空！");
			else
				return retVal.value;
		}

		// 检查属性值
		String fildcv = ctrl.getFildcv(); // 限制值
		if (CommUtil.isNull(fildcv)) {
			return retVal.value;
		}

		if (ctrl.getFildmu() == E_YES___.YES) { // 多值限制
			String values[] = fildcv.split(_SEPARATOR_STR); // 若多值，则使用逗号分隔
			checkRestriction(ctrl, retVal.value, values);
		} else { // 非多值限制
			String values[] = { fildcv };
			checkRestriction(ctrl, retVal.value, values);
		}

		return retVal.value;
	}

    // 获得属性值
    // 未定义控制：输入优先，若为空，则按属性定义
    private static Object getValueIfCtrlNull(String fildcd, Map<String, Object> acctMap,
            Map<String, Object> fileMap) {

        Object acctVal = acctMap.get(fildcd);
        // 获得属性值
        if (CommUtil.isNull(fileMap)) {
            return acctVal;
        } else {
            Object fileVal = fileMap.get(fildcd);
            return CommUtil.isNotNull(acctVal) ? acctVal : fileVal;
        }

    }
	/**
	 * 检查值是否满足限制范围
	 * 
	 * @param ctrl
	 *            限制定义
	 * @param targetVal
	 *            目标值
	 * @param ctrlVal
	 *            定义的限制值（可能是多值）
	 */
	private static void checkRestriction(AppPropCtrl ctrl, Object targetVal, String[] ctrlVal) {
		boolean valid = false;

		if (ctrl.getFildtp() == E_FILDTP.ENUM) { // 枚举类型
			String enumid = ctrl.getEnmuid(); // 枚举的fullID
			Class<?> enumCls = OdbFactory.get().getOdbManager(RestrictionType.class).selectByKey(enumid).getJavaClass();
			if (enumCls.isAssignableFrom(targetVal.getClass())) {
				String t = String.valueOf(targetVal);
				for (String val : ctrlVal) {
					if (CommUtil.compare(t, val) == 0) {
						valid = true;
						break;
					}
				}
			} else {
				throw ApError.Aplt.E0000("产品[" + ctrl.getProdcd() + "]属性[" + ctrl.getFildcd() + "]不是指定的枚举类型！");
			}
		} else if (ctrl.getFildtp() == E_FILDTP.INT) { // 整型
			Integer i = ConvertUtil.toInteger(targetVal);
			for (String val : ctrlVal) {
				if (CommUtil.compare(i, ConvertUtil.toInteger(val)) == 0)
					valid = true;
			}
		} else if (ctrl.getFildtp() == E_FILDTP.LONG) {
			Long t = ConvertUtil.toLong(targetVal);
			for (String val : ctrlVal) {
				if (CommUtil.compare(t, ConvertUtil.toLong(val)) == 0) {
					valid = true;
					break;
				}
			}
		} else { // 默认STRING
			for (String val : ctrlVal) {
				if (CommUtil.compare(String.valueOf(targetVal), val) == 0) {
					valid = true;
					break;
				}
			}
		}

		if (!valid) {
			throw ApError.Aplt.E0000("产品[" + ctrl.getProdcd() + "]属性[" + ctrl.getFildcd() + "]值不符合控制要求！");
		}

	}

	// 获得属性值：考虑重载和空是否覆盖
	private static RetVal getOverrideValue(AppPropCtrl ctrl, Object acctVal, Object fileVal) {
		RetVal retVal = new RetVal();
		if (E_YES___.YES == ctrl.getFinlfg()) { // 不允许重载
			if (CommUtil.isNotNull(acctVal) && CommUtil.compare(String.valueOf(acctVal), String.valueOf(fileVal)) != 0) {
				bizlog.error("产品[" + ctrl.getProdcd() + "]属性[" + ctrl.getFildcd() + "]的控制为固定值，不允许输入值！");
				throw ApError.Aplt.E0000("产品[" + ctrl.getProdcd() + "]属性[" + ctrl.getFildcd() + "]的控制为固定值，不允许输入值！");
			} else {
				retVal.fromProduct = true;
				retVal.value = fileVal;
				return retVal;
			}
		} else {// 允许重载
			if (E_NULLFG.two == ctrl.getNullfg()) { // 空值覆盖
				retVal.fromProduct = false;
				retVal.value = acctVal;
				return retVal;
			} else if(E_NULLFG.one == ctrl.getNullfg()) {
				if (CommUtil.isNull(acctVal)) {// 空值不覆盖,则使用属性定义的
					retVal.fromProduct = true;
					retVal.value = fileVal;
					return retVal;
				} else {
					retVal.fromProduct = false;
					retVal.value = acctVal;
					return retVal;
				}
			}else{// 空值不覆盖,则使用属性定义的,但覆盖属性的0（针对查询属性值null的金额类、长整形初始化为0的情况）
				if (CommUtil.isNull(acctVal)) {
					if(CommUtil.compare(Float.parseFloat(fileVal.toString()), 0f)==0){
						retVal.fromProduct = false;
						retVal.value = acctVal;
						return retVal;
					}
					retVal.fromProduct = true;
					retVal.value = fileVal;
					return retVal;
				} else {
					retVal.fromProduct = false;
					retVal.value = acctVal;
					return retVal;
				}
			}
		}
	}
	
	// 获得属性控制定义
	private static Map<String, AppPropCtrl> getCtrl(String prodcd, String tablcd) {
		Map<String, AppPropCtrl> m = new HashMap<String, AppPropCtrl>();
		List<AppPropCtrl> list = AppPropCtrlDao.selectAll_odb2(prodcd, tablcd, false);
		if (list != null) {
			for (AppPropCtrl ctrl : list) {
				m.put(ctrl.getFildcd(), ctrl);
			}
		} else {
			bizlog.error("警告：未找到产品[%s]对产品属性表[%s]的控制定义，请核实表名大小写和参数定义是否正确！", prodcd, tablcd);
		}

		return m;
	}

	public static class RetVal {
		boolean fromProduct = false;
		Object value;
	}
}
