package cn.sunline.ltts.busi.bsap.serviceimpl;

import cn.sunline.ltts.aplt.namedsql.ApBookDao;

/**
 * 批量后的汇报服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoApDayendBatchQueryImpl", longname = "日终批量查询相关", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoApDayendBatchQueryImpl implements cn.sunline.ltts.busi.bsap.servicetype.IoApDayendBatchQuery {

	@Override
	public Integer queryDayendBatchCount(String cdcnno, String xitongbs, String farendma, String pljylcbs) {
		Long count = ApBookDao.selKsysJykzhqCount(xitongbs,farendma,pljylcbs,false);
		if(count != null) {
			return count.intValue();
		}
		return 0;
	}

}
