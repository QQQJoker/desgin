package cn.sunline.ltts.gns;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.metadata.base.pollThread.AbstractPollThread;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdt;

/** 
* @author zhoujiawen: 
* @version 创建时间：2020年10月20日 下午2:04:55 
* 类说明 
*/
public class AcdtPollThread extends AbstractPollThread {

	private static final SysLog log = SysLogUtil.getSysLog(AcdtPollThread.class);

	
	public AcdtPollThread(String threadId, String threadName, String longname, Long delay, Long timeInterval) {
		super(threadId, threadName, longname, delay, timeInterval);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process() {
		// TODO Auto-generated method stub
		List<AppAcdt> acdts = DaoUtil.selectAll(AppAcdt.class);
		log.info("会计日期轮询线程开始更新");
		AcdtHolder.updateAcdt(acdts);
		log.info("会计日期轮询线程更新结束");
	}

}
