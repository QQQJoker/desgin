package cn.sunline.ltts.busi.aplt.impl;

import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.BizConstant;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.engine.online.InServiceController;
import cn.sunline.adp.cedar.engine.online.InServiceController.ServiceCategory;
import cn.sunline.adp.cedar.engine.online.spi.impl.DefaultOnlineEngineExtensionPoint;
import cn.sunline.adp.vine.base.util.lang.StringUtils;
import cn.sunline.ltts.busi.aplt.pckg.PckgUtil;
import cn.sunline.ltts.busi.aplt.plugin.BaseApltPlugin;
import cn.sunline.ltts.busi.aplt.tools.BaseEnvUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.aplt.tools.SeqUtil;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;

public abstract class AbstractTransactionProcessCallBackImpl extends DefaultOnlineEngineExtensionPoint {
    
    private static final SysLog log = PckgUtil.getLog();
	private static final BizLog bizlog = BizLogUtil.getBizLog(AbstractTransactionProcessCallBackImpl.class);


	@Override
	public void afterBizEnv(RequestData requestData, ResponseData responseData, InServiceController serviceController) {
		
		super.afterBizEnv(requestData, responseData, serviceController); 
		
		if(ServiceCategory.T == serviceController.getServiceCategory()) {
			this.afterBizEnv(requestData.getBody());
		}else {
			this.afterBizServiceEnv(requestData.getBody());
		}
		
	}

	@Override
	public void afterProcess(DataArea dataArea, ServiceCategory serviceExecuteMode) {
		super.afterProcess(dataArea, serviceExecuteMode);
		if(ServiceCategory.T == serviceExecuteMode) {
			this.afterProcess(dataArea);
		}
		
	}

	@Override
	public void beforeBizEnv(RequestData requestData, InServiceController serviceController) {
		super.beforeBizEnv(requestData, serviceController);
		if(ServiceCategory.T == serviceController.getServiceCategory()) {
			this.beforeBizEnv(requestData.getBody());
		}else {
			this.beforeBizServiceEnv(requestData.getBody());
		}
	}

	@Override
	public void beforePkgFormat(ResponseData response, ServiceCategory serviceExecuteMode) {
		
		super.beforePkgFormat(response, serviceExecuteMode);
		
		if(serviceExecuteMode == ServiceCategory.T) {
			this.beforePkgFormat(response);
		}
		
	}

	@Override
	public void beforeProcess(DataArea dataArea, ServiceCategory serviceExecuteMode) {
		
		super.beforeProcess(dataArea, serviceExecuteMode);
		
		if(serviceExecuteMode == ServiceCategory.T) {
			this.beforeProcess(dataArea);
		}
	
	}

	@Override
	public void exceptionProcess(DataArea dataArea, Throwable e, ServiceCategory serviceExecuteMode) {
		super.exceptionProcess(dataArea, e, serviceExecuteMode);
		if(serviceExecuteMode == ServiceCategory.T) {
			this.exceptionProcess(dataArea, e);
		}
		
	}

	/**
	 * 交易环境前处理，公共环境变量赋值。
	 */
	public void beforeBizEnv(DataArea dataArea) {

		// 设置公共运行区数据
		BaseEnvUtil.setRunEnvs(dataArea);

	}


	/**
	 * 业务服务环境前处理，公共环境变量赋值。
	 */
	public void beforeBizServiceEnv(DataArea dataArea) {

		RunEnvsComm runEnvsComm = SysUtil.getTrxRunEnvs();
		if (bizlog.isDebugEnabled()) {
			bizlog.debug("服务调用beforeBizServiceEnv，runEnvsComm=" + runEnvsComm);
		}

		if (CommUtil.isNull(runEnvsComm.getCorpno())) {
			runEnvsComm.setCorpno(SysUtil.getDefaultTenantId());
//			throw Aplt.E0000("跨节点调用法人代码不能为空！");
		}
		// 当前DCN
		runEnvsComm.setCdcnno(DcnUtil.getCurrDCN());
		if (bizlog.isDebugEnabled()) {
			bizlog.debug("当前DCN节点设置为" + runEnvsComm.getCdcnno());
		}

		// 交易日期：不同节点有自己的日期
		ApSysDateStru cplDate = DateTools.getDateInfo();
		runEnvsComm.setTrandt(cplDate.getSystdt());
		runEnvsComm.setLstrdt(cplDate.getLastdt());
		runEnvsComm.setNxtrdt(cplDate.getNextdt());

		// 交易流水
		runEnvsComm.setTransq(SeqUtil.getTransqFromEnvs());

		runEnvsComm.setInpusq(CommTools.getBranchSeq());
	
		if (bizlog.isDebugEnabled()) {
			bizlog.debug("服务调用环境初始化，mntrsq=" + runEnvsComm.getMntrsq() + ", transq=" + runEnvsComm.getTransq());
		}

	}

	
	


	
	


	/**
	 * 返回数据组包前处理,通常用于实现错误码、错误信息的映射
	 */

	public void beforePkgFormat(final ResponseData response) {
		try {
			String mappingErrorCode = mapRetCode(response.getHeaderData().getString(BizConstant.erorcd));
			response.getHeaderData().setString(BizConstant.erorcd, mappingErrorCode);
			
//			String errorMsg = response.getHeaderData().getString(BizConstant.erortx); // 取错误信息
			
//			response.getBody().getSystem().setString(BizConstant.erortx, new String(errorMsg.getBytes("UTF8")));// 设置错误信息
		} catch (Exception e) {
			// 不能对外抛异常
			bizlog.error("错误码映射报错", e);
		}
	}


	/******************* 以下私有方法 ***********************/
	public String mapRetCode(String retCode) {
		// 根据行里的要求自定义
		if (bizlog.isDebugEnabled())
			bizlog.debug("==========错误码============" + retCode);
		// if (GlobalServiceContext.get().getStatus().isRunWithIDE())
		// return "ide-0000" + retCode;

		if (StringUtils.isEmpty(retCode))
			return retCode;

		// 先匹配具体的错误码，没有就匹配模块错误码
		String mapCode = "";
		mapCode = BaseApltPlugin.getErrorModuleMap().getProperty(retCode);
		if (mapCode != null) {
			if (bizlog.isDebugEnabled())
				bizlog.debug("========错误码====mapCode=======" + mapCode);
			return mapCode;
		}

		int idx = retCode.indexOf(".");
		if (idx > 0) {
			String moduleCode = retCode.substring(0, idx);
			if (bizlog.isDebugEnabled())
				bizlog.debug("==========错误码====moduleCode========" + moduleCode);
			mapCode = BaseApltPlugin.getErrorModuleMap().getProperty(moduleCode);
			if (mapCode != null)
				return mapCode;
			// throw ExceptionUtil.wrapThrow("错误码[%s]没有对应的映射码", retCode); TODO
			// 浙江农信没映射报错，很多错误码没定义防止出错，先屏蔽
		}

		return retCode;
	}

	public void exceptionProcess(DataArea dataArea, Throwable e) {
		
		
	}

	public void afterBizEnv(DataArea dataArea) {
	
		
	}

	public void afterProcess(DataArea dataArea) {
	
		
	}

	public void afterBizServiceEnv(DataArea dataArea) {
		
		
	}

	public void beforeProcess(DataArea dataArea) {
	
		
	}
}