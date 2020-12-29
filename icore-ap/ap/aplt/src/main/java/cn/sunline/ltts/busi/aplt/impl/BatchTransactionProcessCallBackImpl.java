package cn.sunline.ltts.busi.aplt.impl;

import java.util.HashMap;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.sequence.BatchTaskSequence;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.bsap.servicetype.IoBatchBackSendMsg;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 批量交易回调点
 * 
 */
public class BatchTransactionProcessCallBackImpl extends AbstractBatchTransactionProcessCallBackImpl {

	private static final BizLog bizlog = BizLogUtil.getBizLog(BatchTransactionProcessCallBackImpl.class);
	
	/**
	 * 实现获取批量任务标识和交易日期的方法
	 */
	@Override
	public BatchTaskSequence initBatchTaskSequence(DataArea dataArea) {
		return super.initBatchTaskSequence(dataArea);
	}

	@Override
	public void afterBizEnvInJob(DataArea dataArea) {
		super.afterBizEnvInJob(dataArea);
	}

	@Override
	public void afterBizEnvInTran(DataArea dataArea) {
		super.afterBizEnvInTran(dataArea);
	}

	@Override
	public void beforeBizEnvInJob(DataArea dataArea) {
		super.beforeBizEnvInJob(dataArea);
		RunEnvs trxRun = SysUtil.getTrxRunEnvs();
		ApSysDateStru sysDate = DateTools.getDateInfo();
        trxRun.setYreddt(sysDate.getYreddt());
	}

	@Override
	public void beforeBizEnvInTran(DataArea dataArea) {
		super.beforeBizEnvInTran(dataArea);
		RunEnvs trxRun = SysUtil.getTrxRunEnvs();
        ApSysDateStru sysDate = DateTools.getDateInfo();
        trxRun.setYreddt(sysDate.getYreddt());
	}
	
	@Override
	public void tranExceptionProcess(DataArea dataArea, Throwable t) {
		try {
			super.tranExceptionProcess(dataArea, t);
		} finally {
			//获得批量交易码
			String prcscd = (String) dataArea.getSystem().get(BatchConfigConstant.BATCH_TRAN_ID);
			if( CommUtil.isNotNull(prcscd)) { //非空且打开，查询是否定义此批量交易需要发送短信
				KnpPara trans = CommTools.KnpParaQryByCorpno("BatchMes", prcscd, "%", "%", false);
				if( trans != null && "1".equals(trans.getPmval1())) {
					this.sendMessage(dataArea, t);
				}
			}
		}
	}
	
	//构造并发送批量异常短信
	private void sendMessage(DataArea dataArea, Throwable t) {
		KnpPara knpPara = CommTools.KnpParaQryByCorpno("BatchMes", "meteid", "%", "%", false);
		if( knpPara == null ) {
			bizlog.error("未定义批量异常发送短信的模板及手机参数！");
			return;
		}

		Map<String,String> msgParm = new HashMap<String,String>();
		 String tranbr =  dataArea.getCommReq().getString("tranbr");
		 String groupId =  dataArea.getSystem().getString("groupId");
		 String brchna = SysUtil.getInstance(IoSrvPbBranch.class).getBranch(tranbr).getBrchna();
		 String servip =  CommTools.getSysIp();
		 String prcsna = CommTools.prcRunEnvs().getPrcsna();
		 String tmstmp = CommTools.prcRunEnvs().getTmstmp();
		 String error = t.getMessage();
		 if(CommUtil.isNull(error)){
			 error = "null";
		 }
		 String date = tmstmp.substring(4,6)+"月"+tmstmp.substring(6,8)+"日"+tmstmp.substring(8,10)+":"+tmstmp.substring(10,12)+":"+tmstmp.substring(12,14)+"秒";
		 msgParm.put("brchna", brchna);
		 msgParm.put("servip", servip);
		 msgParm.put("prcsna", prcsna);
		 msgParm.put("date", date);
		 msgParm.put("groupId", groupId);
		 msgParm.put("error", error);
		ApMessageComplexType.SMSCType smscTypeDO = SysUtil.getInstance(ApMessageComplexType.SMSCType.class);
		smscTypeDO.setMeteid(knpPara.getPmval1()); //短息模版ID
	    smscTypeDO.setMobile(knpPara.getPmval2());
	    smscTypeDO.setMsgopt(BaseEnumType.E_MSGOPT.SUCESS);
	    smscTypeDO.setNacode(knpPara.getPmval3());
	    smscTypeDO.setMessty(E_YES___.YES);
	    smscTypeDO.setMsgparm(msgParm);
	    IoBatchBackSendMsg ioBatchBackSendMsg  = SysUtil.getInstance(IoBatchBackSendMsg.class);
	    ioBatchBackSendMsg.batchExceptionSendMsg(smscTypeDO);
	}

}
