package cn.sunline.ltts.gns;

import java.util.List;

import cn.sunline.adp.cedar.base.boot.spi.BootProcessPointExtension;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.metadata.loader.redis.servicetype.RedisRemoteService.selectAll;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdt;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;
import oracle.net.aso.i;

/** 
* @author zhoujiawen: 
* @version 创建时间：2020年10月19日 下午4:04:55 
* 类说明 
*/
public class AcdtExtension implements BootProcessPointExtension  {
	public static final String POINT = "process.boot.acdtImpl";
	private static final SysLog log = SysLogUtil.getSysLog(AcdtExtension.class);
	@Override
	public void serverStartBefore() {
		// TODO Auto-generated method stub
	}

	@Override
	public void serverStartAfter() {
		// TODO Auto-generated method stub
		log.info("会计日期初始化");
		List<AppAcdt> list = AcdtHolder.getAppAcdt();
		if(list==null) {
			throw Sys.E0001("会计日期表数据为空！");
		}
		AcdtHolder.init(0, list);
	}

}
