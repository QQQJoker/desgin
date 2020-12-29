package cn.sunline.ltts.amsg.serviceimpl;

import java.util.Map;

import org.aspectj.util.LangUtil;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.core.expression.ExpressionEvaluator;
import cn.sunline.adp.core.expression.ExpressionEvaluatorFactory;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.amsg.namedsql.ApMsgNsqlDao;
import cn.sunline.ltts.amsg.servicetype.IoApDcnMessageProcess;
import cn.sunline.ltts.amsg.servicetype.IoApTopicReceipt;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTempDefi;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTempDefiDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTopiDefi;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTopiDefiDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessData;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessDataDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessSucc;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessSuccDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessUndo;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessUndoDao;
import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType.SMSCType;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageTotalInfo;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYNCST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_METYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGACT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGMOD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import io.netty.util.internal.StringUtil;

/**
 * 异步消息服务
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "AsyncMessageSenderImpl", longname = "短信处理发送服务", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoApAsyncMessageImpl implements cn.sunline.ltts.amsg.service.IoApAsyncMessage {
	private static final BizLog log = BizLogUtil.getBizLog(IoApAsyncMessageImpl.class);

	/**
	 * 发布消息或跨节点执行
	 * <p>
	 * 注意：
	 * <li>跨系统发布消息时，在生产者端，此服务为发布服务，不需要执行此方法，消费者有对应交易进行消费；
	 * <li>跨节点发布消息时，在生产者端，仅仅此服务的调用入口，此方法由消费者执行（即：跨节点异步发送消息）；
	 */

	@Override
	public void publish(final MessageTotalInfo mti) {
		final IoApTopicReceipt tr = SysUtil.getInstance(IoApTopicReceipt.class);
		final String dcnno = CommTools.prcRunEnvs().getCdcnno();
		final String corpno = CommTools.prcRunEnvs().getCorpno();

		// 此处只有跨节点服务调用过来时才会执行；否则是执行交易
		AppTopiDefi defi = AppTopiDefiDao.selectOne_odb1(mti.getRealInfo().getMtopic(), SysUtil.getSystemId(), true);

		// 订阅模式的先登记消费者
		if (defi.getMsgmod() == E_MSGMOD.TOPIC) {
			// 独立事务，目的是收妥消息
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				public Void execute() {
					tr.subscriberProcess(mti);
					return null;
				}
			});
		}

		// 单个事务内的消息全部收全了才能进行下一步操作，否者直接结束操作
		String affaid = mti.getRealInfo().getAffaid();// 事务ID
		int affcnt = mti.getAffcnt();// 事务内消息总数
		int reciveCnt = ApMsgNsqlDao.selTopiMessCountByAffaid(affaid, corpno, true);// 已经接收的条数
		// 如果是点对点模式或者是订阅模式并且收妥所以消息
		log.debug("消费者开始消费消息!");
		if (defi.getMsgmod() == E_MSGMOD.QUEUE || affcnt == reciveCnt) {
			try {
				IoApDcnMessageProcess processor = SysUtil.getInstance(IoApDcnMessageProcess.class, defi.getSvcimp());
				// IoApDcnMessageProcess processor = new
				// IoApDcnMessageProcessSQLImpl();
				processor.process(mti);
			} catch (Exception e) {
				// 如果消费失败状态置为失败
				if (defi.getMsgmod() == E_MSGMOD.TOPIC) {
					tr.receiptPushlisher(mti.getRealInfo().getAffaid(), corpno, dcnno, SysUtil.getSystemId(),
							E_SYNCST.FAIL);
				}
				throw ApError.Aplt.E0000("跨节点方式消费异步消息失败，e:", e);
			}
			// 消费成功
			if (defi.getMsgmod() == E_MSGMOD.TOPIC) {
				tr.receiptPushlisher(mti.getRealInfo().getAffaid(), corpno, dcnno, SysUtil.getSystemId(), E_SYNCST.SUCC);
			}
		}
	}

	@Override
	public void publishMri(final cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageRealInfo mrinfo) {
		// 此处只有跨节点服务调用过来时才会执行；否则是执行交易
		AppTopiDefi defi = AppTopiDefiDao.selectOne_odb1(mrinfo.getMtopic(), SysUtil.getSystemId(), true);

		if (defi.getMsgmod() == E_MSGMOD.TOPIC) {
			throw ApError.Aplt.E0000("发布订阅模式，SVCOPT不能配置为[2-异步消息]！");
		}
		  log.debug("消费者开始消费消息!");
		try {
			IoApDcnMessageProcess processor = SysUtil.getInstance(IoApDcnMessageProcess.class, defi.getSvcimp());
			processor.processMri(mrinfo);
		} catch (Exception e) {
			throw ApError.Aplt.E0000("跨节点方式消费异步消息失败2，e:", e);
		}
	}
	@Override
    public void saveMessSucc(MessageTotalInfo mti) {
        ApsMessSucc succ= SysUtil.getInstance(ApsMessSucc.class);
        succ.setTrandt(mti.getRefeInfo().getTrandt()); // 交易日期（分区）
        succ.setBusisq(mti.getRefeInfo().getBusisq()); // 业务流水号
        succ.setTransq(mti.getRefeInfo().getTransq()); // 交易流水号
        succ.setMesseq(mti.getRealInfo().getMsgseq()); // 消息产生序号
        succ.setSosyco(mti.getRefeInfo().getSystcd()); // 源系统代码
        succ.setTopcid(mti.getRealInfo().getMtopic()); // 消息主题ID
        succ.setMessid(mti.getRealInfo().getMessid()); // 消息ID
        succ.setSdcnid(mti.getRefeInfo().getCdcnno()); // 源DCN号
        succ.setSovmid(mti.getRefeInfo().getVmidxx()); // 源VMID
        succ.setSourip(mti.getRefeInfo().getHostip()); // 源主机
        succ.setDevmid(null); // 收到VMID
        succ.setDestip(null); // 收到主机
        succ.setDestdt(null); // 收到时间
        succ.setPrvmid(null); // 处理VMID
        ApsMessSuccDao.insert(succ);
    }
	@Override
	public void saveMessage(MessageTotalInfo mti) {

		ApsMessUndo undo = SysUtil.getInstance(ApsMessUndo.class);
		undo.setTrandt(mti.getRefeInfo().getTrandt()); // 交易日期（分区）
		undo.setBusisq(mti.getRefeInfo().getBusisq()); // 业务流水号
		undo.setTransq(mti.getRefeInfo().getTransq()); // 交易流水号
		undo.setMesseq(mti.getRealInfo().getMsgseq()); // 消息产生序号
		undo.setSosyco(mti.getRefeInfo().getSystcd()); // 源系统代码
		undo.setTopcid(mti.getRealInfo().getMtopic()); // 消息主题ID
		undo.setMessid(mti.getRealInfo().getMessid()); // 消息ID
		//基于消息ID，创建一个groupid
		int groupcount = 0 ,groupsize =0;
		try{
			KnpPara knpPara = CommTools.KnpParaQryByCorpno("BatchPublishMessgaeGroup", "groupcount", "groupsize", "%", true);
			groupcount = Integer.valueOf(knpPara.getPmval1());
			groupsize =  Integer.valueOf(knpPara.getPmval2());
		}catch(NumberFormatException e){
			throw ExceptionUtil.wrapThrow("批量发送消息分组公共业务参数表配置pmva1,pmval2有误，请检查", e);
		}
		int groupid = mti.getRealInfo().getMessid().hashCode()%(groupcount*groupsize);
		if(groupid < 0) groupid *= -1;
		undo.setGroupid(groupid);
		undo.setSdcnid(mti.getRefeInfo().getCdcnno()); // 源DCN号
		undo.setSovmid(mti.getRefeInfo().getVmidxx()); // 源VMID
		undo.setSourip(mti.getRefeInfo().getHostip()); // 源主机
		undo.setDevmid(null); // 收到VMID
		undo.setDestip(null); // 收到主机
		undo.setDestdt(null); // 收到时间
		undo.setPrvmid(null); // 处理VMID
		ApsMessUndoDao.insert(undo);

		ApsMessData data = SysUtil.getInstance(ApsMessData.class);
		data.setTrandt(mti.getRefeInfo().getTrandt()); // 交易日期（分区）
		data.setMessid(undo.getMessid()); // 消息ID
		data.setTopcid(mti.getRealInfo().getMtopic()); // 消息主题ID

		// 分布式异步消息汇总 中的消息类型和这个表中消息类型有啥区别
		/**
		 * @author zhangli
		 * @date 20170629
		 */
		AppTopiDefi topiDefi = AppTopiDefiDao
				.selectOne_odb1(mti.getRealInfo().getMtopic(), SysUtil.getSystemId(), true);
		data.setMetype(topiDefi.getMetype()); // 消息类型
		// data.setMd5chk(); // MD5校验
		// data.setPremod(); // 压缩方式
		data.setMesobj(mti.getRealInfo().getMsgtyp()); // 消息对象类
		// data.setAfpres(); // 消息压缩内容
		
		if (topiDefi.getSvcopt() == BaseEnumType.E_SVCOPT.MTI) {
			data.setMedata(SysUtil.serialize(mti));
		}else if (topiDefi.getSvcopt() == BaseEnumType.E_SVCOPT.MRI) {
			data.setMedata(SysUtil.serialize(mti.getRealInfo()));
		}else if (topiDefi.getSvcopt() == BaseEnumType.E_SVCOPT.USR) {
			data.setMedata(SysUtil.serialize(mti.getRealInfo().getMsgobj())); // // 消息明文内容
		}
		ApsMessDataDao.insert(data);
	}

	/**
	 * 发送短信
	 */
	@Override
	public void sendMessage(MessageTotalInfo mtinfo) {

	    String meteids[] = this.getMeteids(mtinfo.getRealInfo().getMtopic());
		if (meteids == null || meteids.length == 0)
			throw ApError.Aplt.E0000("消息主题为[" + mtinfo.getRealInfo().getMtopic() + "]配置为[短信]，但短信模板为空！");
		// 发送多模板消息
		this.sendSMSes(meteids, mtinfo);
	}

	/**
	 * 直接调用短信接口发送指定内容消息
	 * 
	 * @param sms
	 */
	@Override
	public void sendSMS(SMSCType sms) {
		if (log.isDebugEnabled())
			log.debug("待发送短信内容为[%s].", sms);

		// SMSSender sender =
		// ExtensionUtil.getExtensionPointImpl(SMSSender.POINT);
		//
		// sender.sendSMS(sms);

		if (log.isDebugEnabled())
			log.debug("短信发送成功！");
	}

	/**
	 * 直接调用短信接口发送指定内容消息
	 * 
	 * @param sms
	 */
	@Override
	public void sendSMSes(Options<SMSCType> smses) {
		if (log.isDebugEnabled())
			log.debug("待发送批量短信内容为[%s].", smses);
		// SMSSender sender =
		// ExtensionUtil.getExtensionPointImpl(SMSSender.POINT);
		// sender.sendSMSes(smses);

		if (log.isDebugEnabled())
			log.debug("批量短信发送成功！");
	}

	/********************************* 以下为私有发方法 ***********************************/
	// 获取短信模板
	private String[] getMeteids(String mtopic) {

		AppTopiDefi defi = AppTopiDefiDao.selectOne_odb1(mtopic, SysUtil.getSubSystemId(), true);

		// 只要作为消费端时，才能进行处理
		if (defi.getMsgact() != E_MSGACT.CONSUMER) {
			log.info("获取短信模板时，消息处理方式为非[消费方]，不获取短信模板！");
			return null;
		}

		if (defi.getMetype() == E_METYPE.SMS && defi.getIsmein() == E_YES___.YES) {
			if (log.isDebugEnabled())
				log.debug("消息为[短信]类型，短信模板ID为[%s].", defi.getMeteid());

			if (CommUtil.isNull(defi.getMeteid()))
				throw ApError.Aplt.E0000("消息主题为[" + mtopic + "]配置为[短信]，但短信模板为空！");

			String meteids[] = defi.getMeteid().split(",");
			if (meteids.length <= 0)
				throw ApError.Aplt.E0000("消息主题为[" + mtopic + "]配置为[短信]，但短信模板配置不正确！");
			return meteids;
		} else {
			log.info("获取短信模板时，非短信类型，不获取短信模板！");
			return null;
		}
	}

	// 发送多条消息
	private void sendSMSes(String meteids[], MessageTotalInfo mtinfo) {
		Options<SMSCType> smses = new DefaultOptions<SMSCType>();
		for (String meteid : meteids) {
			AppTempDefi tempDefi = AppTempDefiDao.selectOne_odb1(meteid, true);
			if (log.isDebugEnabled())
				log.debug("短信模板为[%s].", tempDefi.getMetemp());

			// String smstxt =
			// getMsgTemplet(CommUtil.toMap(mtinfo.getRealInfo().getMsgobj()),
			// tempDefi.getMetemp());

			SMSCType sms = SysUtil.getInstance(SMSCType.class);
			sms.setMeteid(tempDefi.getMeteid());
			String mobile = mtinfo.getRealInfo().getMobile();
			if (StringUtil.length(mobile) != 11)
				throw ApError.Aplt.E0000("发送短信的手机号不是【11】位，不正确！");
			sms.setMobile(mobile);
			sms.setMsgopt(mtinfo.getRealInfo().getMsgopt());
			sms.setMsgparm(CommUtil.toMap(mtinfo.getRealInfo().getMsgobj()));
			String nacode = mtinfo.getRealInfo().getNacode();
			sms.setNacode(CommUtil.isNull(nacode) ? "86" : nacode);
			smses.add(sms);
			if (log.isDebugEnabled())
				log.debug("产生的送短信内容为[%s].", sms);
		}
		this.sendSMSes(smses);
	}

	/** 解析短信模板-->待发短信 */
	public static String getMsgTemplet(Map<String, Object> map, String temp) {

		Params ps = new Params().addAll(map);
		ExpressionEvaluator ee = ExpressionEvaluatorFactory.getInstance();
		try {
			return (String) ee.eval(temp, ps, ps);
		} catch (Exception e) {
			throw new IllegalArgumentException("解析短信模板[" + temp + "]错误, cause by: " + e.getMessage(), e);
		}
	}
	  /* private List<ApMessageComplexType.SMSCType> getSMSMessageListFromRunEnvs() {
	        RunEnvsComm env = SysUtil.getTrxRunEnvs();
	        return env.getSmsmsg().getValues();
	    }*/
    @Override
    public void afterBatchSendSMS() {
        // TODO Auto-generated method stub
       /* try {     //短信使用不同接口,这块注释
            log.debug("[批量交易后处理]开始处理短信！");
            for (ApMessageComplexType.SMSCType sms : getSMSMessageListFromRunEnvs()) {
                if (sms.getMsgopt() == E_MSGOPT.SUCESS || sms.getMsgopt() == E_MSGOPT.BOTH)//只发送成功的
                    SMSUtil.sendSMSMessage(sms);
            }
        } catch (Exception e) {
            log.error("afterBatchTranExecute发送短信失败", e);
        }*/
    	  //只有批量调此方法，联机不需要掉此方法
        boolean b = SystemType.batch == SysUtil.getCurrentSystemType();
        if(b){
        	   try {
                   // 交易成功后发送,手动入库消息
                   log.debug("[批量交易后处理]开始处理消息！");
                   AsyncMessageUtil.publishOrSave(E_MSGOPT.SUCESS);
               } catch (Exception e) {
                   log.error("afterBatchTranExecute发送异步消息失败！e:", e);
               }
        }
    
    }

}
