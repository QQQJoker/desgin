package cn.sunline.ltts.busi.aplt.serviceimpl;

import cn.sunline.ltts.busi.aplt.tools.BatchTools;
import cn.sunline.ltts.busi.iobus.servicetype.ap.IoBatchEvent;
 /**
  * 批量数据汇报事件服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class ApBatchEventImpl implements IoBatchEvent{
 /**
  * R/C-DCN或ADM批量数据汇报ADM事件服务
  * ADM数据汇报 (R/C-DCN DepositBatch -> DepositAdmBatch)
  */
	@Override
	public void onAdmBatchEvent(String fwsxgjzi,  String shijneir) {
	    BatchTools.fireEvent(fwsxgjzi, shijneir);
	}

	/**
	 * GL批量数据汇报ADM事件服务
	 * ADM数据汇报 (ADM-DCN DepositGL -> DepositAdmBatch)
	 */
	@Override
	public void onAdmBatchEventForGL(String fwsxgjzi, String shijneir) {
		BatchTools.fireEvent(fwsxgjzi, shijneir);
	}

	/**
	 * GL批量数据汇报R/C-DCN事件服务
	 * R/C-DCN数据汇报 (ADM-DCN DepositGL -> DepositBatch)
	 */
	@Override
	public void onBatchEvent(String fwsxgjzi, String shijneir) {
		BatchTools.fireEvent(fwsxgjzi, shijneir);
	}
	
	/**
	  * R/C-DCN批量数据汇报同步服务
	 * @return 
	  *
	  */
	@Override
	public String onBatchService(String dcn_num,String fwsxgjzi, String shijneir) {
		return BatchTools.fireService(fwsxgjzi, shijneir);
	}

	/**
	  * ADM批量数据汇报同步服务
	  *
	  */
	@Override
	public String onAdmBatchService(String fwsxgjzi, String shijneir) {
		return BatchTools.fireService(fwsxgjzi, shijneir);
	}
}

