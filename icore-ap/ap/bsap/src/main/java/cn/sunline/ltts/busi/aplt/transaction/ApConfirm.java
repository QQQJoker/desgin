package cn.sunline.ltts.busi.aplt.transaction;

import java.util.List;


import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC;
import cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.confirm.InputSetter;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvntDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CALLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_EVNTLV;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class ApConfirm {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApConfirm.class);

	public static void confirm(String trandt, String transq) {

		if (bizlog.isDebugEnabled())
			bizlog.debug("二次提交处理开始=====================");
		//List<KnbEvnt> list = KnbEvntDao.selectAll_odb2(trandt, transq, E_CALLST.F_SUCCESS, false);
		List<KnbEvnt> list = KnbEvntDao.selectAll_odb1(trandt, transq, false);
		if (list != null) {
			for (KnbEvnt evnt : list) {
				if (needProcess(evnt)) {
					procOne(evnt);
				} else {
					if (bizlog.isInfoEnabled())
						bizlog.info("事件不需要做二次提交，跳过！！evnt:" + evnt);
				}
			}
		}
		if (bizlog.isDebugEnabled())
			bizlog.debug("二次提交处理结束=====================");
	}

	private static boolean needProcess(KnbEvnt evnt) {
		
		//只有一次调用成功的才能二次提交  (外调交易才判断是否一次调用成功,未外调的交易不需要判断)
		if( (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_OUTSVC) == 0 
				|| CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_OUTTXN) == 0)
				&& (evnt.getCallst() != E_CALLST.F_SUCCESS && evnt.getCallst() != E_CALLST.NONE) )
			return false;
		
		//只有为资金入账事件的才需要二次提交
		if( evnt.getEvntlv() != E_EVNTLV.CREDIT )
			return false;
		
		if(CommTools.prcRunEnvs().getXdcnfg() == E_YES___.NO){
			return false;
		}
		
        if(evnt.getTranev() == "IN04"){
			return false;
		}
		// TODO 对事件提供检查
		return true;
	}

	private static void procOne(KnbEvnt evnt) {

		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_OUTSVC) == 0) {
			doRemoteConfirm(evnt, "IoApTransactionTCC.confirm" + ApUtil.TRANS_EVENT_OUTSVC);
			if (bizlog.isInfoEnabled())
				bizlog.info("外调服务二次提交成功,被确认外调流水号[" + evnt.getCallsq() + "]");
			return;
		}
		if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_OUTTXN) == 0) {
			doRemoteConfirm(evnt, "IoApTransactionTCC.confirm" + ApUtil.TRANS_EVENT_OUTTXN);
			if (bizlog.isInfoEnabled())
				bizlog.info("外调交易二次提交成功,被确认外调流水号[" + evnt.getCallsq() + "]");
			return;
		}

		try {
			//TODO 注意：暂时未提供本地确认的实现
			String impl = ApConfirmEvntProcessor.class.getName() + evnt.getTranev();
			ApConfirmEvntProcessor processor = (ApConfirmEvntProcessor) Class.forName(impl).newInstance();
			processor.process(evnt);
		} catch (InstantiationException e) {
			throw Aplt.E0000("事件[" + evnt.getTranev() + "]二次提交处理失败", e);
		} catch (IllegalAccessException e) {
			throw Aplt.E0000("事件[" + evnt.getTranev() + "]二次提交处理失败", e);
		} catch (ClassNotFoundException e) {
			if (bizlog.isInfoEnabled())
				bizlog.info("事件[" + evnt.getTranev() + "]未提供二次实现，使用默认二次提交处理！", e);
			throw Aplt.E0000("事件[" + evnt.getTranev() + "]未提供二次实现", e);
		} catch (Exception e) {
			throw e;
		}
	}

	private static void doRemoteConfirm(KnbEvnt evnt, String bindid) {
		if (bizlog.isDebugEnabled()) {
			bizlog.debug("外调服务处理开始=====================, 准备的外调流水号:" + evnt.getCallsq());
		}
		// 事件关键字1=是否跨DCN 事件关键字2=服务ID 事件关键字3=子系统编号 事件关键字4=目标DCN 事件关键字5=目标法人代码

		try {
			// 跨节点调用服务
			IoApTransactionTCC tcc = SysUtil.getInstanceProxyByBind(IoApTransactionTCC.class, bindid);
			InputSetter input = SysUtil.getInstance(InputSetter.class);
			input.setOrigdt(evnt.getTrandt());
			input.setOrigsq(evnt.getCallsq());
			input.setTdcnno(evnt.getEvent4()); // 路由DCN节点
			// TODO 外调服务定位

			IoApTransactionTCC.confirm.Output output = SysUtil.getInstance(IoApTransactionTCC.confirm.Output.class);
			tcc.confirm(input, output);
			
			
		} catch (Exception e) {
			bizlog.error("外调进行二次提交失败，被确认外调流水号[" + evnt.getCallsq() + "].", e);
			throw Aplt.E0000("外调进行二次提交失败，被确认外调流水号[" + evnt.getCallsq() + "].", e);
		}

		if (bizlog.isDebugEnabled())
			bizlog.debug("外调服务处理结束=====================");

	}
}
