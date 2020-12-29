package cn.sunline.ltts.busi.aplt.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.support.PropertiesLoaderUtils;

import cn.sunline.adp.cedar.base.boot.plugin.PluginSupport;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.engine.online.config.OnlineEngineConfig;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.adp.metadata.loader.util.ModelFactoryUtil;
import cn.sunline.adp.metadata.model.Element;
import cn.sunline.adp.metadata.model.ComplexType;
import cn.sunline.adp.metadata.model.database.Table;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tools.ParamUtil;
import cn.sunline.ltts.busi.bsap.util.GsnUtil;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;

public class BaseApltPlugin extends PluginSupport {
	
	static Properties errorModuleMap;
	static Map<String, Object> allSubSystem = new ConcurrentHashMap<String, Object>();
	static Map<String, Object> localSubSystem = new ConcurrentHashMap<String, Object>();


	static SystemExtConfig config = null;

	public static SystemExtConfig getSystemExtConfig() {
		return config;
	}

    private static List<Element> elsForShareMapping = new ArrayList<>(); // 共享字段映射字段模型集合

    public static List<Element> getElsForShareMapping() {
        return elsForShareMapping;
    }
	
	private static final SysLog log = SysLogUtil.getSysLog(BaseApltPlugin.class);

	@Override
	public boolean initPlugin() {
		try {
			errorModuleMap = PropertiesLoaderUtils.loadAllProperties("errormodule.map.properties");
		} catch (IOException e) {
			throw ExceptionUtil.wrapThrow("加载错误码映射文件失败", e);
		}

		// 加载子系统相关设置
		try {

			config = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager()
					.getConfig(SystemExtConfig.class);

			if (config != null) {
				// 本系统所属子系统
				if (!CommUtil.isNull(config.getAllSubSystem())) {
					String keys[] = config.getAllSubSystem().split(",");
					for (String key : keys) {
						if (!CommUtil.isNull(key))
							allSubSystem.put(key, key);
					}
				}
				// 批量调联机，走本地调用的子系统
				if (!CommUtil.isNull(config.getLocalSubSystem())) {
					String keys[] = config.getLocalSubSystem().split(",");
					for (String key : keys) {
						if (!CommUtil.isNull(key))
							localSubSystem.put(key, key);
					}
				}
			}

			if (!allSubSystem.containsKey(SysUtil.getSubSystemId()))
				allSubSystem.put(SysUtil.getSubSystemId(), SysUtil.getSubSystemId());

			if (!localSubSystem.containsKey(SysUtil.getSubSystemId()))
				localSubSystem.put(SysUtil.getSubSystemId(), SysUtil.getSubSystemId());


		} catch (Exception e) {
			throw ExceptionUtil.wrapThrow("加载系统扩展配置失败", e);
		}

		return true;
	}

	public static boolean containsInAllSubSystem(String targetId) {
		if (CommUtil.isNull(targetId))
			return false;
		else
			return allSubSystem.containsKey(targetId);
	}

	public static boolean containsInLocalsubSystem(String targetId) {
		if (CommUtil.isNull(targetId))
			return false;
		else
			return localSubSystem.containsKey(targetId);
	}

	public static Properties getErrorModuleMap() {
		return errorModuleMap;
	}



	public static void setTableCahceByName(Map<String, Table> tableCahceByName) {
		BaseApltPlugin.tableCahceByName = tableCahceByName;
	}

	/**
	 * 通过表名称获取对应的Clazz
	 * 
	 * @param tbname
	 * @return
	 */
	public static Class<?> getTableClazzByName(String tbname) {
		return tableCahceByName.get(tbname).getJavaClass();
	}

	public static Table getTableByName(Class tbClazz) {
		return OdbFactory.getTable(tbClazz);
	}

	static Map<String, Table> tableCahceByName = new HashMap<>();

	@Override
	public void startupPlugin() {
		
		OnlineEngineConfig config = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager().getConfig(OnlineEngineConfig.class);
        ComplexType envsType = ModelFactoryUtil.getModelFactory().getModel(ComplexType.class, config.getRunEnvSchemaName());
        KnpPara para=ParamUtil.getPublicParm(CoreUtil.getDefaultTenantId(),"SHARERUNENV"); // MsBasicNSQLDao.selMspParameter("SHARERUNENV", "*", false);
        String shareEnv = CommUtil.isNull(para)?null:para.getPmval1();
        if (shareEnv != null) {
            for (String key : shareEnv.split(",")) {
                for (Element e : envsType.getAllElements()) {
                    if (e.getId().equals(key)) {
                        log.debug("add share field：[%s]", key);
                        elsForShareMapping.add(e);
                    }
                }
            }
        }

		try {
			GsnUtil.startVM();
		} catch (Exception e) {
			log.error("启动JVM时，将从APB_SNVM同步序号失败！", e);
		}
	}

	@Override
	public void shutdownPlugin() {
		try {
			GsnUtil.stopVM();
		} catch (Exception e) {
			log.error("停止JVM时，将序号同步到APB_SNVM失败！", e);
		}
	}

}
