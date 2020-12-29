package cn.sunline.ltts.busi.aplt.serviceimpl;

import cn.sunline.ltts.busi.aplt.tools.BatchTools;
import cn.sunline.ltts.busi.iobus.servicetype.ap.IoGLEvent;
 /**
  * GL数据汇报事件服务实现（入口）
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class ApGLEventImpl implements IoGLEvent{
    /**
     * GL数据汇报 (R/C-DCN DepositBatch -> DepositGL)
     */
	public void onGLEvent(String fwsxgjzi, String shijneir){
	    BatchTools.fireEvent(fwsxgjzi, shijneir);
	}

	/**
	 * GL数据汇报 (ADM-DCN DepositAdmBatch -> DepositGL)
	 */
    @Override
    public void onGLEventForAdm(String fwsxgjzi, String shijneir) {
        BatchTools.fireEvent(fwsxgjzi, shijneir);
    }

    /**
	  * ADM批量数据汇报同步服务
	  *
	  */
	@Override
	public String onGLService(String fwsxgjzi, String shijneir) {
		return BatchTools.fireService(fwsxgjzi, shijneir);
	}
}

