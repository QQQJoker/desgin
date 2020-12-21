package cn.sunline.clwj.zdbank.fmq.plugin;

import cn.sunline.adp.cedar.base.boot.plugin.PluginSupport;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.clwj.zdbank.fmq.config.FmqConfig;
import cn.sunline.clwj.zdbank.fmq.server.FmqProducerServer;

public class FmqPlugin extends PluginSupport {

	private static final SysLog log = SysLogUtil.getSysLog(FmqPlugin.class);

	public static FmqConfig config;

	private static FmqProducerServer fmqProducerServer;

	@Override
	public boolean initPlugin() {
		config = EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager().getConfig(FmqConfig.class);
		
		if (config != null) {
			if (config.getProducerApp() != null) {
				fmqProducerServer = new FmqProducerServer(config);
			}
			return true;
		}
		log.warn("末找到配置，FMQ服务不启动");
		return false;
	}

	@Override
	public void shutdownPlugin() {

		if (fmqProducerServer != null) {
			fmqProducerServer.shutdown();
		}

	}

	@Override
	public void startupPlugin() {
		log.info("==========FMQ插件启动开始=================");
		if (fmqProducerServer != null) {
			fmqProducerServer.start();
			log.info("生产者启动成功");
		}
		log.info("==========FMQ插件启动结束=================");
	}

	public static FmqProducerServer getProducer() {
		return fmqProducerServer;
	}

}
