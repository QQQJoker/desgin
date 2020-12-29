package cn.sunline.clwj.oss.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.sunline.clwj.oss.config.OssConfig;
import cn.sunline.clwj.oss.config.OssProtocol;
import cn.sunline.clwj.oss.impl.MsTransferFtpImpl;
import cn.sunline.clwj.oss.impl.MsTransferNfsImpl;
import cn.sunline.clwj.oss.impl.MsTransferSftpImpl;
import cn.sunline.clwj.oss.impl.MsTransferSshjImpl;
import cn.sunline.clwj.oss.spi.MsTransfer;
import cn.sunline.clwj.oss.util.SpringContextHolder;

public class OssFactory {

	private static OssFactory instance = null;
	private static OssConfig config = null;
	private static final Logger log = LogManager.getLogger(OssFactory.class);
	private static final Integer _LOCK_ = 0;
	private static final String NULL_KEY = "_NULL_";
	private static Map<String, MsTransfer> map = new ConcurrentHashMap<>();
	
	private static boolean springConfig = true;

	private OssFactory() {
		
		config = SpringContextHolder.getBean(OssConfig.class);
		
		if (config == null) {
			
			springConfig = false;
			Properties properties = loadConfig();
			config = parseConfig(properties);
		}
	}

	public static OssFactory get() {
		if (instance == null) {
			synchronized (_LOCK_) {
				if (instance == null) {
					instance = new OssFactory();
				}
			}
			if (!config.isEnabled()) {
				throw new RuntimeException("未配置启用OSS，配置文件：oss.properties！");
			}
		}
		return instance;
	}

	/**
	 * 创建对象存储传输SPI接口实例(均默认)
	 * 
	 * @return
	 */
	public MsTransfer create() {
		return create(OssProtocol.sshj, null);
	}
	
	/**
	 * 创建对象存储传输SPI接口实例,默认使用sshj实现是sftp协议
	 * 
	 * @param configId-默认协议ID或默认OSS源
	 * @return
	 */
	public MsTransfer create(String configId) {
		return create(OssProtocol.sshj, configId);
	}
	
	/**
	 * 按协议创建链接实例，默认协议id为default
	 * 
	 * @param configId-默认协议ID或默认OSS源,模式使用sshj实现是sftp协议
	 * @return
	 */
	public MsTransfer create(OssProtocol protocol) {
		return create(protocol, null);
	}
	/**
	 * 按协议类型获取链接
	 * @param protocol
	 * @param configId
	 * @return
	 */
	public MsTransfer create (OssProtocol protocol, String configId) {
		// 不指定ID，则按具体实现的默认配置，因为此处的Map必须要Key，所以给定固定值
		String mapId = protocol.getProtocolId() + "_" + (configId == null ? NULL_KEY : configId);
		if (!map.containsKey(mapId)) {
			try {
				map.put(mapId, init(protocol, configId));
			} catch (Exception e) {
				throw new RuntimeException("not find configId:" + configId, e);
			}
		}
		return map.get(mapId);
	}

	
	/**
	 * 加载配置文件
	 * @return
	 */
	private Properties loadConfig() {
		try {
			Properties properties = new Properties();
			// 使用ClassLoader加载properties配置文件生成对应的输入流
			properties.load(new InputStreamReader(OssFactory.class.getClassLoader().getResourceAsStream("oss.properties"), "UTF-8"));
			return properties;
		} catch (IOException e) {
			log.error("OssFactory loadConfig error!", e);
			throw new RuntimeException(e);
		}
	}

	private OssConfig parseConfig(Properties properties) {
		OssConfig cfg = new OssConfig();
		cfg.setEnabled("true".equalsIgnoreCase((String) properties.get("OSS.enable")));
		cfg.setOssImpl((String) properties.get("OSS.DefaultImpl"));
		
		return cfg;
	}

	private MsTransfer init(OssProtocol protocolId, String configId) {
		MsTransfer api = null;
		
		switch(protocolId.getProtocolId()) {
		case "nfs" : 
			api = new MsTransferNfsImpl();
			api.init(configId);
			break;
		case "sftp" :
			api = new MsTransferSftpImpl();
			api.init(configId);
			break;
		case "ftp" :
			api = new MsTransferFtpImpl();
			api.init(configId);
			break;
		case "sshj" :
			api = new MsTransferSshjImpl();
			api.init(configId);
			break;
		default:
			try {
				api = (MsTransfer) Class.forName(config.getOssImpl()).newInstance();
				api.init(configId);
			} catch (InstantiationException e) {
				log.error("init InstantiationException, configId=" + configId, e);
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				log.error("init IllegalAccessException, configId=" + configId, e);
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				log.error("init ClassNotFoundException, configId=" + configId, e);
				throw new RuntimeException(e);
			}
		}
		
		/*if ("nfs".equals(config.getNfsImpl()) || MsTransferNfsImpl.class.getName().equals(config.getNfsImpl())) {
			api = new MsTransferNfsImpl();
			api.init(configId);
		} else if ("sftp".equals(config.getSftpImpl()) || MsTransferSftpImpl.class.getName().equals(config.getSftpImpl())) {
			api = new MsTransferSftpImpl();
			api.init(configId);
		} else if ("ftp".equals(config.getFtpImpl()) || MsTransferFtpImpl.class.getName().equals(config.getFtpImpl())) {
			api = new MsTransferFtpImpl();
			api.init(configId);
		} else if ("sshj".equals(config.getSshjImpl()) || MsTransferSshjImpl.class.getName().equals(config.getSshjImpl())) {
			api = new MsTransferSshjImpl();
			api.init(configId);
		} else {
			try {
				api = (MsTransfer) Class.forName(config.getOssImpl()).newInstance();
				api.init(configId);
			} catch (InstantiationException e) {
				log.error("init InstantiationException, configId=" + configId, e);
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				log.error("init IllegalAccessException, configId=" + configId, e);
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				log.error("init ClassNotFoundException, configId=" + configId, e);
				throw new RuntimeException(e);
			}
		}*/
		return api;
	}

	public static boolean isSpringConfig() {
		return springConfig;
	}
	
}
