package cn.sunline.ltts.busi.hctran.batch;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.hc.util.HotCtrlUtil;

/**
 * 热点控制定时释放差错额度
 * @author jizhirong 
 * 20180409
 *
 */
public class TimedReleasErrorLine extends LttsTimerProcessor{

	private static BizLog bizLog = LogManager.getBizLog(TimedReleasErrorLine.class);
	@Override
	public void process(String paramString, DataArea paramDataArea) {	
		//String tabnum = paramDataArea.getInput().get("tabnum").toString();
		String tabnum=HotCtrlUtil.getTabnum(paramDataArea);//分表号
		bizLog.info("开始释放额度占用表分表序号["+tabnum+"]数据");
		new ReleasErrorLineHandle(tabnum).handle();
	}
}
