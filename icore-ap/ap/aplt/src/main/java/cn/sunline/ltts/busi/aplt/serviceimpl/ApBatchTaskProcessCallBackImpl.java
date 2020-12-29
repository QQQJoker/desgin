package cn.sunline.ltts.busi.aplt.serviceimpl;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ap.iobus.type.IoApBatchFileStruct;
import cn.sunline.ltts.busi.aplt.coderule.ApDCN;
import cn.sunline.ltts.busi.aplt.tools.BatchTools;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatchReg;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
 /**
  * 批量任务控制服务定义
  * 测试
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class ApBatchTaskProcessCallBackImpl implements cn.sunline.adp.cedar.server.batch.servicetype.BatchTaskProcessCallBack{
	private static final SysLog log = SysLogUtil.getSysLog(ApBatchTaskProcessCallBackImpl.class);
 /**
  * 异常处理
  *
  */
	public void processException(String pljypich, String cuowxinx, String cuowduiz){
		IoApFileBatchReg apFileBatchReg = SysUtil.getInstance(IoApFileBatchReg.class);
		IoApBatchFileStruct.IoApWjplrwxxInfo rwxxInfo = apFileBatchReg.selWjplrwxx(pljypich, ApDCN.getMyDcnNo());
		rwxxInfo.setFlbtst(ApBaseEnumType.E_FLBTST.CLSB);
		rwxxInfo.setErrotx(cuowxinx);
		rwxxInfo.setErrosk(cuowduiz);
		rwxxInfo.setSuccnm((long) 0);
		rwxxInfo.setFailnm((long) 0);
		rwxxInfo.setTotanm((long) 0);
		String shijneir = SysUtil.serialize(rwxxInfo);
		//BatchTools.fireAdmBatchEvent("fileCountRegAdm", shijneir);
	}
	
 /**
  * 批量交易组执行结束后执行（无论成功或者失败）
  *
  */
	public void doFinally( String pljypich){
		log.info("调用任务批次[%s]的结束后处理服务", pljypich);
	}
}

