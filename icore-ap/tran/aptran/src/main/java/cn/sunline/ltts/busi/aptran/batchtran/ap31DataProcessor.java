package cn.sunline.ltts.busi.aptran.batchtran;

import java.util.HashMap;
import java.util.Map;

import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.namedsql.ApCommAPIDao;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;

	 /**
	  * 日终表分区
	  *
	  */

public class ap31DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Ap31.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap31.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Ap31.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap31.Property property) {
		 Map<String, Object> result = new HashMap<String, Object>();
		 result.put("errorTxt", "");
		 DaoUtil.executeProcForQuery(ApCommAPIDao.namedsql_mntn_partition, result);	
		 if(!result.isEmpty() && StringUtil.isNotEmpty(result.get("errorTxt")))
			 throw ApError.Aplt.E0000("日终表分区失败：" + result.get("errorTxt"));
	}

}


