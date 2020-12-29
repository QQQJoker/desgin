package cn.sunline.ltts.busi.aplt.spi;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.busi.bsap.type.knaAcsqInfos.knaAcsqInfo;
import cn.sunline.ltts.busi.aplt.cleardate.ApClearDate;
import cn.sunline.ltts.busi.aplt.impl.AbstractTransactionProcessCallBackImpl;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.aplt.tools.GlDateTools;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoPbCommSvr;
import cn.sunline.ltts.busi.sys.errors.InError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SERVTP;

/**
 * 应用平台联机交易回调样例
 * <p>
 * 复制此sample到自己的aplt项目改名为TransactionProcessCallBackImpl，并在aplt.plugin.xml插件中定义扩展
 * <p>
 * 注意:应用平台基础项目中没有此aplt插件，必须在自己的aplt中提供
 * 
 * @author yanggx 2017-05-24
 */
public class TransactionProcessCallBackImpl extends AbstractTransactionProcessCallBackImpl {
	private static final BizLog bizlog = BizLogUtil.getBizLog(TransactionProcessCallBackImpl.class);


	/**
	 * 交易环境前处理，公共环境变量赋值。
	 */
	@Override
	public void beforeBizEnv(DataArea dataArea) {
		super.beforeBizEnv(dataArea);

		// 本业务系统的内容
		RunEnvs trxRun = SysUtil.getTrxRunEnvs();
		// 清算日期
		ApSysDateStru cplClDate = ApClearDate.getClearDateInfo();
		// 获得指定法人的日期
		trxRun.setClenum(cplClDate.getClenum());
		trxRun.setClerdt(cplClDate.getSystdt());

		ApSysDateStru sysDate = DateTools.getDateInfo();
		trxRun.setYreddt(sysDate.getYreddt());
		trxRun.setTransq(CommUtil.isNull(trxRun.getTransq()) ? trxRun.getInpusq() : trxRun.getTransq());
		// 总账子系统运行时，才获取会计日期
		if (DcnUtil.isGL()) {
			ApSysDateStru cplDate = GlDateTools.getGlDateInfo();
			trxRun.setGldate(cplDate.getSystdt());
			trxRun.setLsgldt(cplDate.getLastdt());
			trxRun.setNxgldt(cplDate.getNextdt());
			
			trxRun.setLstrdt(cplDate.getLastdt());
			trxRun.setTrandt(cplDate.getSystdt());
			trxRun.setNxtrdt(cplDate.getNextdt());
			
			if (SysUtil.getCurrentSystemType() == SystemType.batch) {
				trxRun.setInpudt(cplDate.getSystdt());
			}
		}
		/*
		// 获取中心法人清算机构
		IoBrchInfo ctbrInfo = SysUtil.getInstance(IoSrvPbBranch.class).getCenterBranch(trxRun.getTranbr());
		trxRun.setCentbr(ctbrInfo.getBrchno());

		trxRun.getAuthvo().setAuthfg(E_AUTHFG.NO); // 默认为不授权？
		// 增加调用授权登记服务
		IoSrvPbPrivilege permSvc = SysUtil.getInstance(IoSrvPbPrivilege.class);
		// 授权柜员检查
		permSvc.rgstPermBeforTran(dataArea.getInput());
		if(CommUtil.equals("deduct",  SysUtil.getInnerServiceCode())){
			permSvc.procPermAferTran(E_YES___.NO);
		}
		*/
		
	}

	/**
	 * 交易前处理。可在此处进行事前权限检查。
	 */
	@Override
	public void beforeProcess(DataArea dataArea) {
		super.beforeProcess(dataArea); // 必须调用super的方法
		RunEnvs trxRun = SysUtil.getTrxRunEnvs();
		if(trxRun.getServtp() == E_SERVTP.TE) {			
			SysUtil.getRemoteInstance(IoPbCommSvr.class).tellAuthServ();
		}
	}

	/**
	 * 业务服务环境前处理，公共环境变量赋值。
	 */
	@Override
	public void beforeBizServiceEnv(DataArea dataArea) {
		super.beforeBizServiceEnv(dataArea);// 必须调用super的方法

		// TODO 本业务系统的内容
	}

	/**
	 * 业务服务环境后处理，公共响应变量往报文赋值。
	 */
	@Override
	public void afterBizServiceEnv(DataArea dataArea) {
		super.afterBizServiceEnv(dataArea); // 必须调用super的方法

		// TODO 本业务系统的内容
	}

	/**
	 * 交易成功处理完成后调用。压力测试时可以在这里清除压力测试数据
	 */
	@Override
	public void afterProcess(DataArea dataArea) {
		super.afterProcess(dataArea);// 必须调用super的方法
		
		//ApAmsgFMQUtil.handleFMQMessage(CommToolsAplt.prcRunEnvs().getMqinfo());
		int x = CommToolsAplt.prcRunEnvs().getKacsqs().size();
		bizlog.debug("======交易平衡数据条数*****x***==============="+x);
		bizlog.debug("======交易平衡性检查[%s]===============", CommToolsAplt.prcRunEnvs().getKacsqs().toString());
		
		bizlog.debug("======交易平衡性检查002[%s]===============", 
				EngineContext.getEngineRuntimeContext().getTrxRunEnvsHelper().getData().get("knasqs"));
		
		
		if(CommToolsAplt.prcRunEnvs().getKacsqs().size() > 0) {
			BigDecimal total = BigDecimal.ZERO;
			//存在数据
			for(int i = 0 ; i <CommToolsAplt.prcRunEnvs().getKacsqs().size();i++) {
				knaAcsqInfo knaAcsqInfo = CommToolsAplt.prcRunEnvs().getKacsqs().get(i);
				//进行判断
				if(CommUtil.equals(knaAcsqInfo.getIoflag(),"I")){//表内
					if(knaAcsqInfo.getAmntcd() == BaseEnumType.E_AMNTCD.CR) {
						total = total.add(knaAcsqInfo.getTranam());//贷+正数
					}else {
						total = total.add(knaAcsqInfo.getTranam().negate());//借+负数
					}
				}
			}
			if(!CommUtil.equals(total, BigDecimal.ZERO)) {//总和不为0
				bizlog.debug("======交易平衡性检查,不平数据[%s]===============", CommToolsAplt.prcRunEnvs().getKacsqs().toString());
				throw InError.busi.E0119();
			}
		}
		
		
		
	}

	/**
	 * 交易环境后处理，公共响应变量往报文赋值。
	 */
	@Override
	public void afterBizEnv(DataArea dataArea) {
		super.afterBizEnv(dataArea); // 必须调用super的方法

		// TODO 本业务系统的内容
	}

	/**
	 * 返回数据组包前处理,通常用于实现错误码、错误信息的映射
	 */
	@Override
	public void beforePkgFormat(ResponseData response) {
		super.beforePkgFormat(response);

		// TODO 本业务系统的内容
	}

	@Override
	public void exceptionProcess(DataArea dataArea, Throwable e) {
		bizlog.error("交易处理失败", e);
		super.exceptionProcess(dataArea, e);

		// TODO 本业务系统的内容
	}

}
