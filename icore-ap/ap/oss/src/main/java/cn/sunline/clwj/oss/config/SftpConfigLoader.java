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

public class SftpConfigLoader {

	private static final Logger log = LogManager.getLogger(SftpConfigLoader.class);
	private static SftpConfig config = null;

	private static final String ENABLE_KEY = "sftp.enable";
	private static final String DEFAULT_ID_KEY = "sftp.defaultID";
	private static final String DEFAULT_PATH_KEY = "sftp.defaultPath";
	private static final String PREFIX_KEY = "sftp.id[%s].";
	private static final String ID_KEY = "id";
	private static final String IP_KEY = "ip";
	private static final String PORT_KEY = "port";
	private static final String USER_KEY = "user";
	private static final String PASSWD_KEY = "passwd";
	private static final String REMOTE_PATH_KEY = "remotePath";
	private static final String LOCAL_PATH_KEY = "localPath";
	private static final String CONN_TIMEOUT_KEY = "timeout";
	private static final String KEY_PATH_KEY = "keyPath";

	private SftpConfigLoader() {
	}

	public static SftpConfig getConfig() {
		if (config == null) {
			config = SpringContextHolder.getBean(SftpConfig.class);
			load();
		}
		return config;
	}

	private static void load() {
		try {
			Properties properties = new Properties();
			InputStream is = SftpConfigLoader.class.getClassLoader().getResourceAsStream("oss-sftp.properties");
			if(is==null) {
				throw new RuntimeException("config file : oss-sftp.properties not found"); 
			}
			properties.load(new InputStreamReader(is, "UTF-8"));
			// 读取完毕关闭流
			is.close();
			if (log.isDebugEnabled()) {
				log.debug("SftpConfigLoader load() properties:" + properties);
			}

			parse(properties);

		} catch (IOException e) {
			log.error("SftpConfigLoader load error!", e);
			throw new RuntimeException(e);
		}
	}

	private static void parse(Properties p) {
		if ("false".equalsIgnoreCase((String) p.get(ENABLE_KEY))) {
			throw new RuntimeException("SFTP配置项为开启：" + ENABLE_KEY);
		}
		config = new SftpConfig();
		config.setEnabled(true);
		config.setDefaultConfigId((String) p.get(DEFAULT_ID_KEY));
		config.setDefaultLocalPath((String) p.get(DEFAULT_PATH_KEY));

		parseConnections(p);
	}

	private static void parseConnections(Properties p) {

		List<SftpConnectionConfig> list = new ArrayList<>();
		config.setSftpConnectionConfigs(list);

		int i = 0;
		String prefix = null;
		SftpConnectionConfig connConfig = null;
		while (true) {
			prefix = MsStringUtil.format(PREFIX_KEY, i + "");
			if (!p.containsKey(prefix + ID_KEY)) {
				break;
			}

			connConfig = new SftpConnectionConfig();
			connConfig.setId((String) p.get(prefix + ID_KEY));
			connConfig.setServerIp((String) p.get(prefix + IP_KEY));
			connConfig.setServerPort(Integer.parseInt((String) p.get(prefix + PORT_KEY)));
			connConfig.setUserName((String) p.get(prefix + USER_KEY));
			connConfig.setPassword((String) p.get(prefix + PASSWD_KEY));
			connConfig.setRemoteHome((String) p.get(prefix + REMOTE_PATH_KEY));
			connConfig.setLocalHome((String) p.get(prefix + LOCAL_PATH_KEY));
			connConfig.setConnTimeoutInMs(Integer.parseInt((String) p.get(prefix + CONN_TIMEOUT_KEY)));
			connConfig.setKeyPath((String) p.get(prefix + KEY_PATH_KEY));

			if (log.isDebugEnabled()) {
				log.debug("SftpConfigLoader parseConnections i=[" + i + "], connConfig:" + connConfig);
			}

			list.add(connConfig);
			i++;
		}
	}
}
