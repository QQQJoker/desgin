
package cn.sunline.ltts.busi.bsap.serviceimpl;

import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.ltts.busi.ap.iobus.type.IoApReverseType.IoApReverseIn;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.transaction.ApConfirm;
import cn.sunline.ltts.busi.aplt.transaction.ApStrike;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
  * TXC分布式事务扩展
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoTXCServiceImpl", longname="TXC分布式事务扩展", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoTXCServiceImpl implements cn.sunline.adp.cedar.service.servicetype.TXCService{
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(IoTXCServiceImpl.class);
	
 /**
  * 冲正服务
  *
  */
	public void rollback(final cn.sunline.adp.cedar.service.servicetype.TXCService.rollback.Input input){
       if(CommTools.isCZTrans() ) return;  //冲正交易不能再冲正
		
       Map<String, Object> inputHeader = JsonUtil.parse(input.getInput_head());
       
		IoApReverseIn cplRvIn = SysUtil.getInstance(IoApReverseIn.class);
		
		cplRvIn.setOtradt(inputHeader.get("").toString());  // TODO 获取原交易日期
		cplRvIn.setOtrasq(inputHeader.get("").toString());  // TODO 获取原交易流水
		cplRvIn.setOtsqtp(E_YES___.NO);  //根据当前系统的交易流水进行冲正
		String rvercd = ApStrike.prcRollback8(cplRvIn, E_YES___.YES, null); //平台触发的冲正请求，允许外调冲正
		
		if (bizlog.isDebugEnabled())
			bizlog.debug("调用冲正返回：" + rvercd);
	}
	
 /**
  * 二次提交服务
  *
  */
	public void commit2(final cn.sunline.adp.cedar.service.servicetype.TXCService.commit2.Input input){
		String trandt = CommTools.prcRunEnvs().getInpudt();
		String transq = null;
		ApConfirm.confirm(trandt, transq);
	}
}

