package cn.sunline.ltts.busi.aptran.batchtran.dayend;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aptran.namedsql.ReportSqlDao;

	 /**
	  * 报表数据定时生成
	  * 报表数据定时生成
	  *
	  */

public class reportDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Report.Input, cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Report.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Report.Input input, cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Report.Property property) {
		//获取当前日期
		 String date = DateTools.dateAdd(-1,CommToolsAplt.prcRunEnvs().getTrandt());
		 Integer erorcd = 1;
		 String errmsg = "";
		 
		 ReportSqlDao.GetBaseData(date);
		 
		 ReportSqlDao.prcMakeDate(date, date ,erorcd ,errmsg);
	}

}


