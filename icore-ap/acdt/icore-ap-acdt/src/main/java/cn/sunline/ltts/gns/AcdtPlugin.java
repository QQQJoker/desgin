package cn.sunline.ltts.gns;

import cn.sunline.adp.cedar.base.boot.plugin.IPlugin;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.metadata.base.pollThread.AbstractPollThread;

/** 
* @author zhoujiawen: 
* @version 创建时间：2020年10月19日 下午3:52:49 
* 类说明 
*/
public class AcdtPlugin extends IPlugin {
	
	private static final SysLog log = SysLogUtil.getSysLog(AcdtPlugin.class);
	
	private AbstractPollThread acdtPollThread;
	@Override
	public boolean initPlugin() {
		return true;
	}

	@Override
	public void startupPlugin() {
		Long time = 3600L;
		acdtPollThread =new AcdtPollThread("acdtThread", "会计日期规则轮询线程", "轮询线程", time,time );
		acdtPollThread.init();
		acdtPollThread.startup();
		log.info("会计日期插件启动成功！");
	}

	@Override
	public void shutdownPlugin() {
		acdtPollThread.shutdown();
	}

}
