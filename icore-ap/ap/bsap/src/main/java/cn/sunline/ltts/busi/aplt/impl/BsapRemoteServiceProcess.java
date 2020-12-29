package cn.sunline.ltts.busi.aplt.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.engine.datamapping.DataMappingUtil;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.service.executor.ServiceExecutorContext;
import cn.sunline.adp.cedar.service.remote.controller.RemoteServiceController;
import cn.sunline.adp.cedar.service.remote.exception.RemoteTimeoutException;
import cn.sunline.adp.cedar.service.remote.executor.DefaultRemoteServiceExecutor;
import cn.sunline.adp.metadata.model.datainterface.DataMapping;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.busi.aplt.plugin.BaseApltPlugin;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.ParamUtil;

@SPIMeta(id = "msremote")
@Order(1000)
public class BsapRemoteServiceProcess extends DefaultRemoteServiceExecutor {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(BsapRemoteServiceProcess.class);
	
	@Override
	public void callRemoteAfter(ServiceExecutorContext serviceExecutorContext, RemoteServiceController serviceController) {
		putSharefieldToRunEnv(serviceExecutorContext.getServiceResponse().getResponseData().getCommRes());
		super.callRemoteAfter(serviceExecutorContext, serviceController);
	}

	@Override
	public void callRemoteBefore(ServiceExecutorContext serviceExecutorContext, RemoteServiceController serviceController) {
		super.callRemoteBefore(serviceExecutorContext, serviceController);
	}

	@Override
	public void callRemoteException(ServiceExecutorContext arg0, RemoteServiceController arg1, Exception arg2) {
		// TODO Auto-generated method stub
		super.callRemoteException(arg0, arg1, arg2);
	}

	@Override
	public void execute(ServiceExecutorContext arg0) {
		// TODO Auto-generated method stub
		super.execute(arg0);
	}

	@Override
	public void processTimeout(ServiceExecutorContext arg0, RemoteTimeoutException arg1, RemoteServiceController arg2) {
		// TODO Auto-generated method stub
		super.processTimeout(arg0, arg1, arg2);
	}
	
	private void putSharefieldToRunEnv(Map<String, Object> comm_res) {
		
		//从参数表获取参数
		String shareEnv=ParamUtil.getPublicParmWithoutCorpno("SHARERUNENV").getPmval1();
		Map<String, Object> shareMap = new HashMap<>();
		if (shareEnv != null) {
			for (String key : shareEnv.split(",")) {
				shareMap.put(key, comm_res.get(key));
			}
		}
		
		if (shareMap.isEmpty())
			return;
		
		Map<String, Object> runEnv = CommUtil.toMap(CommTools.prcRunEnvs());
		
		bizlog.debug("comm_res to RunEnv dataMapping begin [%s]", runEnv);
		DataMappingUtil.dataMapping(BaseApltPlugin.getElsForShareMapping(), BaseApltPlugin.getElsForShareMapping(),shareMap,runEnv,
				 DataMapping.ByInterfaceTrue, true);
		bizlog.debug("comm_res to RunEnv dataMapping end [%s]", runEnv);


	}

}
