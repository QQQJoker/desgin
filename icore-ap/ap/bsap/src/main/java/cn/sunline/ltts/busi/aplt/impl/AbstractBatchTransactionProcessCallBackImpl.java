package cn.sunline.ltts.busi.aplt.impl;

import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.aplt.namedsql.ApBookDao;
import cn.sunline.adp.cedar.server.batch.DefaultBatchTransactionProcessCallBack;
import cn.sunline.adp.cedar.server.batch.engine.sequence.BatchTaskSequence;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.base.dict.KsDict;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydtDao;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.BaseEnvUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.bsap.servicetype.RpAdBatchTaskCallBack;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_CORPLEVEL;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.engine.data.DataArea;

/**
 * 批量交易回调点
 * 
 */
public class AbstractBatchTransactionProcessCallBackImpl extends DefaultBatchTransactionProcessCallBack {
	private static final BizLog bizlog = BizLogUtil.getBizLog(AbstractBatchTransactionProcessCallBackImpl.class);

	/**
	 * 实现获取批量任务标识和交易日期的方法
	 */
	@Override
	public BatchTaskSequence initBatchTaskSequence(DataArea dataArea) {
		BatchTaskSequence bts = new BatchTaskSequence();

		bts.setTaskid(BatchUtil.getTaskId());
		String corpno = (String) dataArea.getCommReq().get(ApConstants.CORPNO_NAME_KEY);
		if( CommUtil.isNull(corpno) ) {
			Integer count = ApBookDao.countAppCorpByCorplv(E_CORPLEVEL.SECOND, false);
			if(count != null && count >=2){
				throw Aplt.E0000("批量交易多二级法人必须上送法人代码！");
			}
			KnpPara corpPara = CommTools.KnpParaQryByCorpno("system.batch", SysUtil.getSystemId(), "corpno", "%",false);
			if( corpPara != null ) {
				corpno = corpPara.getPmval1();
			}
			if( CommUtil.isNull(corpno) ) {
				throw Aplt.E0000("批量交易初始化批量流水错误，无法人代码！");
			}
		}
		
		String sysdate = DateTools.getDateInfo().getSystdt();
		if (sysdate != null && StringUtil.isNotEmpty(sysdate))
			bts.setTrandt(sysdate);

		if (StringUtil.isEmpty(bts.getTrandt()))
			throw ApError.Aplt.E0000("未找到法人[" + corpno + "]系统日期！");

		return bts;
	}

	@Override
	public void afterBizEnvInJob(DataArea dataArea) {
		bizlog.method("========afterBizEnvInJob Begin========");
		super.afterBizEnvInJob(dataArea);
		bizlog.method("========afterBizEnvInJob End========");
	}

	@Override
	public void afterBizEnvInTran(DataArea dataArea) {
		bizlog.method("========afterBizEnvInTran Begin========");
		
		super.afterBizEnvInTran(dataArea);
		
		bizlog.method("========afterBizEnvInTran End========");
	}

	@Override
	public void beforeBizEnvInJob(DataArea dataArea) {
		bizlog.method("========beforeBizEnvInJob Begin========");
		
		super.beforeBizEnvInJob(dataArea);
		
		// 交易初始化时只初始化日期及机构信息
		BaseEnvUtil.setRunEnvs(dataArea);
		
		// 初始化系统调用流水号
		CommTools.genNewSerail();
		bizlog.method("========beforeBizEnvInJob End========");
	}

	@Override
	public void beforeBizEnvInTran(DataArea dataArea) {
		bizlog.method("========beforeBizEnvInTran Begin========");
		
		super.beforeBizEnvInTran(dataArea);

		// 交易初始化时只初始化日期及机构信息
		BaseEnvUtil.setRunEnvs(dataArea);

		// 初始化系统调用流水号
		CommTools.genNewSerail();

		bizlog.method("========beforeBizEnvInTran End========");
	}

	
	@Override
	public void afterBatchTranExecute(DataArea dataArea) {
		if(!SysUtil.isDistributedSystem()) {
			return;
		}
		// TODO Auto-generated method stub
	    try {
	        RpAdBatchTaskCallBack rpadCallBack = SysUtil.getInstance(RpAdBatchTaskCallBack.class);
	        rpadCallBack.doFinally(dataArea.getSystem().getString(KsDict.BtDict.task_num.getId()));
        } catch (Exception e) {
           throw new RuntimeException(e);
        }finally{
           /* try {   //批量消息   20180116 add by xiongwei  修改原因:联机批量消息统一使用add方法
                // 入库消息
                bizlog.debug("[批量交易后处理]开始处理消息！");
                AsyncMessageUtil.publishOrSave(E_MSGOPT.SUCESS);
            } catch (Exception e) {
                bizlog.error("[批量交易后处理]发送异步消息失败！e:", e);
            }*/
        }
	};
	
	@Override
	public void tranExceptionProcess(DataArea dataArea, Throwable t) {
		if(!SysUtil.isDistributedSystem()) {
			return;
		}
		// TODO Auto-generated method stub
		try{
			RpAdBatchTaskCallBack rpadCallBack = SysUtil.getInstance(RpAdBatchTaskCallBack.class);
			StringBuffer sb = new StringBuffer();
			 StackTraceElement[] stackArray= t.getStackTrace();
			 for(int i = 0; i<  stackArray.length; i++){
				 StackTraceElement element = stackArray[i];
				 sb.append(element.toString() + "\n");
			 }
			rpadCallBack.processException(dataArea.getSystem().getString(KsDict.BtDict.task_num.getId()), t.getMessage(), sb.toString());
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
