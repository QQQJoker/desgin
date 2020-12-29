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

public class FtpConfigLoader {

	private static final Logger log = LogManager.getLogger(FtpConfigLoader.class);
	private static FtpConfig config = null;

	private static final String ENABLE_KEY = "ftp.enable";
	private static final String DEFAULT_ID_KEY = "ftp.defaultID";
	private static final String DEFAULT_PATH_KEY = "ftp.defaultPath";
	private static final String PREFIX_KEY = "ftp.id[%s].";
	private static final String ID_KEY = "id";
	private static final String IP_KEY = "ip";
	private static final String PORT_KEY = "port";
	private static final String USER_KEY = "user";
	private static final String PASSWD_KEY = "passwd";
	private static final String REMOTE_PATH_KEY = "remotePath";
	private static final String LOCAL_PATH_KEY = "localPath";
	private static final String RETRY_KEY = "retryKey"; //重试次数
	private static final String RETRY_INTERVAL_KEY = "retryIntervalKey"; //重试间隔
	private static final String DATA_TIMEOUT_KEY = "dataTimeout"; //客户端超时时间
	private static final String CONN_TIMEOUT_KEY = "connTimeout"; //连接超时时间

	private FtpConfigLoader() {
	}

	public static FtpConfig getConfig() {
		if (config == null) {
			config = SpringContextHolder.getBean(FtpConfig.class);
			if(config == null){
				load();
			}
		}
		return config;
	}

	private static void load() {
		try {
			Properties properties = new Properties();
			InputStream is = FtpConfigLoader.class.getClassLoader().getResourceAsStream("oss-ftp.properties");
			if (is == null) {
				throw new RuntimeException("config file : oss-ftp.properties not found");
			}
			properties.load(new InputStreamReader(is, "UTF-8"));
			// 读取完毕关闭流
			is.close();
			if (log.isDebugEnabled()) {
				log.debug("FtpConfigLoader load() properties:" + properties);
			}

			parse(properties);

		} catch (IOException e) {
			log.error("FtpConfigLoader load error!", e);
			throw new RuntimeException(e);
		}
	}

	private static void parse(Properties p) {
		if ("false".equalsIgnoreCase((String) p.get(ENABLE_KEY))) {
			throw new RuntimeException("FTP配置项为开启：" + ENABLE_KEY);
		}
		config = new FtpConfig();
		config.setEnabled(true);
		config.setDefaultConfigId((String) p.get(DEFAULT_ID_KEY));
		config.setDefaultLocalPath((String) p.get(DEFAULT_PATH_KEY));

		parseConnections(p);
	}

	private static void parseConnections(Properties p) {

		List<FtpConnectionConfig> list = new ArrayList<>();
		config.setFtpConnectionConfigs(list);

		int i = 0;
		String prefix = null;
		FtpConnectionConfig connConfig = null;
		while (true) {
			prefix = MsStringUtil.format(PREFIX_KEY, i + "");
			if (!p.containsKey(prefix + ID_KEY)) {
				break;
			}

			connConfig = new FtpConnectionConfig();
			connConfig.setId((String) p.get(prefix + ID_KEY));
			connConfig.setServerIp((String) p.get(prefix + IP_KEY));
			connConfig.setServerPort(Integer.parseInt((String) p.get(prefix + PORT_KEY)));
			connConfig.setUserName((String) p.get(prefix + USER_KEY));
			connConfig.setPassword((String) p.get(prefix + PASSWD_KEY));
			connConfig.setRemoteHome((String) p.get(prefix + REMOTE_PATH_KEY));
			connConfig.setLocalHome((String) p.get(prefix + LOCAL_PATH_KEY));
			connConfig.setRetryTime(Integer.parseInt((String) p.get(prefix + RETRY_KEY)));
			connConfig.setRetryInterval(Integer.parseInt((String) p.get(prefix + RETRY_INTERVAL_KEY)));
			connConfig.setDataTimeoutInMs(Integer.parseInt((String) p.get(prefix + DATA_TIMEOUT_KEY)));
			connConfig.setConnTimeoutInMs(Integer.parseInt((String) p.get(prefix + CONN_TIMEOUT_KEY)));

			if (log.isDebugEnabled()) {
				log.debug("FtpConfigLoader parseConnections i=[" + i + "], connConfig:" + connConfig);
			}

			list.add(connConfig);
			i++;
		}
	}
}
