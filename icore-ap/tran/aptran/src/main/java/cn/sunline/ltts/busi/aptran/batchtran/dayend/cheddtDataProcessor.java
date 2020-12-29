package cn.sunline.ltts.busi.aptran.batchtran.dayend;

import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_PILJYZHT;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.Tsp_date_managementDao;

	 /**
	  * 修改日终日期
	  *
	  */

public class cheddtDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Cheddt.Input, cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Cheddt.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Cheddt.Input input, cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Cheddt.Property property) {
//		 DcnDataUtil.updatePlrzrqgl(input.getTran_flow_id(), input.getChaxriqi(), input.getCorporate_code());
		//更新日终日期及状态
		KSysBatchTable.tsp_date_management tblKsysPlrzrqgl = KSysBatchTable.Tsp_date_managementDao.selectOne_odb_1(input.getTran_flow_id(), input.getCorpno(), false);
		tblKsysPlrzrqgl.setDayend_flow_status(E_PILJYZHT.success.getValue());
		Tsp_date_managementDao.updateOne_odb_1(tblKsysPlrzrqgl);
	}

}


