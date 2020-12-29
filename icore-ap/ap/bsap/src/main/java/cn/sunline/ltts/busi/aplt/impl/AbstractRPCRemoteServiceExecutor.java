package cn.sunline.ltts.busi.aplt.impl;

import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.engine.service.ServiceRequest;
import cn.sunline.adp.cedar.base.engine.service.ServiceResponse;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.SystemParams;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.engine.online.config.OnlineEngineConfigManager;
import cn.sunline.adp.cedar.service.executor.ServiceExecutorContext;
import cn.sunline.adp.cedar.service.remote.controller.RemoteServiceController;
import cn.sunline.adp.cedar.service.remote.executor.DefaultRemoteServiceExecutor;
import cn.sunline.adp.metadata.base.util.CommUtil_;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageRealInfo;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_EVNTLV;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public abstract class AbstractRPCRemoteServiceExecutor extends DefaultRemoteServiceExecutor{
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(AbstractRPCRemoteServiceExecutor.class);

	@Override
	public void callRemoteAfter(ServiceExecutorContext context, RemoteServiceController remoteServiceController) {
		// TODO Auto-generated method stub
		super.callRemoteAfter(context, remoteServiceController);
		ServiceResponse response = context.getServiceResponse();

		if ( response != null) {
			RunEnvsComm evns = SysUtil.getTrxRunEnvs();
			String erorcd = (String) response.getResponseData().getSystem().get(HeaderDataConstants.NAME_ERORCD);
			String erortx = (String) response.getResponseData().getSystem().get(HeaderDataConstants.NAME_ERORTX);
			//String fullid = request.getController().getModelId(); 
			//TODO
			String fullid = context.getServiceController().getInnerServiceModel().getId();
			if (OnlineEngineConfigManager.get().getSuccessCode().equals(erorcd)) {
				if (bizlog.isInfoEnabled())
					bizlog.info("执行服务[%s]进行外调成功！", fullid);
				// 从返回中获得外调的服务或交易的事件类型
				E_EVNTLV sevnlv = E_EVNTLV
						.get(response.getResponseData().getCommRes().get(ApConstants.EVNTLV_NAME_KEY));
				if (bizlog.isInfoEnabled())
					bizlog.info("执行服务[%s]进行外调成功返回的事件级别[evntlv]为[%s]", fullid, sevnlv);
				evns.setSevnlv(sevnlv);
			} else {
				bizlog.error("执行服务[%s]进行外调失败，返回错误码[%s],错误信息[%s]", fullid, erorcd, erortx);
				throw ApError.Aplt.E0400(fullid, erorcd, erortx);
			}

			// 处理跨节点调用带回的异步消息 // 2017.6.19
			// 远程调用返回来的消息不能直接强转为复杂类型，需要拷贝或使用Mapping映射
			RunEnvsComm env = SysUtil.getInstance(RunEnvsComm.class);
			CommUtil_.copyPropertiesWithTypeConvert(env, response.getResponseData().getCommRes());
			if (env.getMsgcnt() != null && env.getMsgcnt() > 0) { // 消息数大于0时处理
				List<MessageRealInfo> mriList = env.getMsgdcn();
				if (mriList != null) { // 消息列表
					for (MessageRealInfo mri : mriList)
						AsyncMessageUtil.add(mri, env);
				}
			}
		}
	}

	@Override
	public void callRemoteBefore(ServiceExecutorContext context, RemoteServiceController remoteServiceController) {
		super.callRemoteBefore(context, remoteServiceController);
		ServiceRequest request = context.getServiceRequest();
		RunEnvsComm trxRun = CommTools.prcRunEnvs();

		Map<String,Object> sys = EngineContext.getEngineRuntimeContext().getRunDataArea().getSystem();
		String targetCorpno = context.getServiceRouteResult().getTargetCorpno();
		if( CommUtil.isNull(targetCorpno) ) {
			targetCorpno = SystemParams.get().getTenantId();
		}
		
		request.getRequestBody().getSystem().put(ApConstants.COPRNO_NAME_KEY, targetCorpno); // 交易法人代码,注意不一定是当前的交易法人
		request.getRequestBody().getSystem().put(ApConstants.LANGCD_NAME_KEY, sys.get(ApConstants.LANGCD_NAME_KEY)); // 语言代码
		request.getRequestBody().getSystem().put(ApConstants.TDCNNO_NAME_KEY, context.getServiceRouteResult().getTargetDCN()); // 目标DCN号
		//request.getRequestBody().getSystem().put(ParamType, targetCorpno);  //TODO
		
			// CommReq
			/* 2017-12-18 modify begin：由于分布式跨节点服务或交易调用时，外调其他DCN或系统时需要使用此字段来获得调用方DCN或系统的系统日期，故赋值为外调方系统日期 */
			request.getRequestBody().getCommReq().put(ApConstants.INPUDT_NAME_KEY, trxRun.getTrandt()); // 上送系统日期
			/*modify end*/
			request.getRequestBody().getCommReq().put(ApConstants.INPUSQ_NAME_KEY, trxRun.getCallsq()); // 上送系统流水20170808-transq改为callsq
			request.getRequestBody().getCommReq().put(ApConstants.INPUCD_NAME_KEY, SysUtil.getSystemId()); // 上送系统编码
			
			// 判断调用远程是否跨法人
			request.getRequestBody().getCommReq().put(ApConstants.COPRNO_NAME_KEY, targetCorpno);
			if (CommUtil.isNotNull(targetCorpno)){
			    if (!CommUtil.equals(targetCorpno, trxRun.getCorpno())) {
	                request.getRequestBody().getCommReq().put(ApConstants.XCOPFG_NAME_KEY, E_YES___.YES);
	                //把目标法人带到目标DCN
	                trxRun.setXcopfg(E_YES___.YES);
			    }
			}
			
			// 判断调用远程是否跨DCN
			if (!CommUtil.equals(context.getServiceRouteResult().getTargetDCN(), trxRun.getCdcnno())) {
				request.getRequestBody().getCommReq().put(ApConstants.XDCNFG_NAME_KEY, E_YES___.YES);
				//解开 @jizhirong 20171121
				trxRun.setXdcnfg(E_YES___.YES);//20170805新增,跨DCN此标识也为0,重新赋值为1
				//record.setTargetDCN(request.getRouter().getTargetDCN());
			}
			trxRun.setMntrfg(E_YES___.NO);


	}

	@Override
	public void callRemoteException(ServiceExecutorContext context, RemoteServiceController remoteServiceController, Exception originalEx) {
		// TODO Auto-generated method stub
		super.callRemoteException(context, remoteServiceController, originalEx);
	}
	
	

}
