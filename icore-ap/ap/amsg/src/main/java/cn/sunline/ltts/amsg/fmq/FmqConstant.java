package cn.sunline.ltts.amsg.fmq;

import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppMessToMQ;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppMessToMQDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;

public class FmqConstant {

	public static String CZACCT_001 = "czacct_001"; //内部户一借一贷虚拟子户入金消息通知
	public static String BESPNT_001 = "bespnt_001"; //账户休眠前通知
	public static String CMACCT_001 = "cmacct_001";
	
	public static String OPACCD_001 = "opaccd_001";
	public static String PSS_RECODR_040 = "pss_recodr_040";
	
	
	public static AppMessToMQ getMsgInfo(String scenno) {
		String prcscd = CommTools.prcRunEnvs().getLttscd();
		AppMessToMQ msgInfo = AppMessToMQDao.selectOne_odb1(prcscd, scenno, false);
		
		return msgInfo;
	}
}
