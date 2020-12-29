package cn.sunline.ltts.busi.aplt.serviceimpl.fbat;

import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatchNotify;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_WENJPLZT;

 /**
  * 通用文件批量异步事件
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class ApFileBatchNotifyImpl implements IoApFileBatchNotify{
 
	/**
	  * DepositAdmBatch文件批量处理结束事件（ADM发布给渠道系统）
	  *
	  */
	@Override
	public void admFinish(String dcn_num, String plwenjlx, String weituoho,
			E_WENJPLZT wenjplzt, Long zongbish, Long chenggbs, Long shibaibs,
			String wenjiabs, String md5maaaa) {
		//IoApFileBatchReg fileBatchReg = SysUtil.getInstance(IoApFileBatchReg.class);
		
	}
	
}

