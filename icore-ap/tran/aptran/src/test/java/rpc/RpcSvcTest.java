//package rpc;
//
//import org.apache.logging.log4j.core.config.ConfigurationFactory;
//
//import cn.sunline.edsp.microcore.Bootstrap;
//import cn.sunline.ltts.core.api.config.ConfigManagerFactory;
//
//public class RpcSvcTest {
//	 public static void main(String[] args) {
//    	System.setProperty(ConfigManagerFactory.SETTING_FILE,"setting.dev.properties");
//    	System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "ltts_log_dev.xml");
//		System.setProperty("ltts.home", System.getProperty("user.dir"));
//		System.setProperty("ltts.vmid", "app");
//		System.setProperty("ltts.log.home", "logs");
//    	Bootstrap.start();
//	 }
//}