package cn.sunline.clwj.zdbank.config;

import cn.sunline.adp.boot.cedar.CedarMain;

public class Application {
	
	public static void main(String[] args) {
		 //启动方法
        System.setProperty("ltts.vmid", "onl3");
        System.setProperty("ltts.home", System.getProperty("user.dir"));
        System.setProperty("ltts.log.home", "logs");
        System.setProperty("log4j.configurationFile", "log4j2.xml");
        CedarMain.main(args);   
	}
	
}
