package cn.sunline.ltts.busi.aptran.batchtran;


import cn.sunline.ltts.busi.aplt.coderule.ApDayendPoint;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_DYEDCT;

/**
 * 系统换日操作
 */

public class ap01DataProcessor extends
		cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.Ap01.Input, cn.sunline.ltts.busi.aptran.batchtran.Ap01.Property> {
	
	
	
	@Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.Ap01.Input input, cn.sunline.ltts.busi.aptran.batchtran.Ap01.Property property) {

		AppSydt tblKapp_sysdat = DateTools.chgSystemDate();

		// 登记日终控制点 日切结束
		ApDayendPoint.register(tblKapp_sysdat.getSystdt(), E_DYEDCT.RIQJS);
		
		
	}
		

}

	
