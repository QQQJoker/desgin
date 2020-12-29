package cn.sunline.clwj.oss.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.sunline.clwj.oss.util.MsStringUtil;
import cn.sunline.clwj.oss.util.SpringContextHolder;

public class NfsConfigLoader {

	private static final Logger log = LogManager.getLogger(NfsConfigLoader.class);
	private static NfsConfig config = null;

	private static final String ENABLE_KEY = "nfs文件服务.enable";
	private static final String DEFAULT_ID_KEY = "nfs文件服务.默认协议ID";
	private static final String DEFAULT_PATH_KEY = "nfs文件服务.本地根路径";
	private static final String PREFIX_KEY = "nfs文件服务.id[%s].";
	private static final String ID_KEY = "id";
	private static final String REMOTE_PATH_KEY = "远程文件根目录";
	private static final String LOCAL_PATH_KEY = "本地文件根目录";

	private NfsConfigLoader() {
	}

	public static NfsConfig getConfig() {
		if (config == null) {
			config = SpringContextHolder.getBean(NfsConfig.class);
			if(config == null){
				load();
			}
		}
		return config;
	}

	private static void load() {
		try {
			Properties properties = new Properties();
			InputStream is = NfsConfigLoader.class.getClassLoader().getResourceAsStream("oss-nfs.properties");
			if (is == null) {
				throw new RuntimeException("config file : oss-nfs.properties not found");
			}
			properties.load(new InputStreamReader(is, "UTF-8"));
			// 读取完毕关闭流
			is.close();

			if (log.isDebugEnabled()) {
				log.debug("NfsConfigLoader load() properties:" + properties);
			}

			parse(properties);

		} catch (IOException e) {
			log.error("NfsConfigLoader load error!", e);
			throw new RuntimeException(e);
		}
	}

	private static void parse(Properties p) {
		if ("false".equalsIgnoreCase((String) p.get(ENABLE_KEY))) {
			throw new RuntimeException("NFS配置项为开启：" + ENABLE_KEY);
		}
		config = new NfsConfig();
		config.setEnabled(true);
		config.setDefaultConfigId((String) p.get(DEFAULT_ID_KEY));
		config.setDefaultLocalPath((String) p.get(DEFAULT_PATH_KEY));

		parseDetal(p);
	}

	private static void parseDetal(Properties p) {

		List<NfsConnectionConfig> list = new ArrayList<>();
		config.setNfsConnectionConfigs(list);

		int i = 0;
		String prefix = null;
		NfsConnectionConfig connConfig = null;
		while (true) {
			prefix = MsStringUtil.format(PREFIX_KEY, i + "");
			if (!p.containsKey(prefix + ID_KEY)) {
				break;
			}

			connConfig = new NfsConnectionConfig();
			connConfig.setId((String) p.get(prefix + ID_KEY));
			connConfig.setRemoteHome((String) p.get(prefix + REMOTE_PATH_KEY));
			connConfig.setLocalHome((String) p.get(prefix + LOCAL_PATH_KEY));

			if (log.isDebugEnabled()) {
				log.debug("NfsConfigLoader parseDetal i=[" + i + "], connConfig:" + connConfig);
			}

			list.add(connConfig);
			i++;
		}
	}
}
