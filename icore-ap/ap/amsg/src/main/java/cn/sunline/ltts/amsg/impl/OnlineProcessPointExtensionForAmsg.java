//package cn.sunline.ltts.amsg.impl;
//
//import java.util.List;
//
//import cn.sunline.adp.cedar.base.engine.RequestData;
//import cn.sunline.adp.cedar.base.engine.ResponseData;
//import cn.sunline.adp.cedar.base.logging.BizLog;
//import cn.sunline.adp.cedar.base.logging.BizLogUtil;
//import cn.sunline.adp.cedar.base.logging.SysLog;
//import cn.sunline.adp.cedar.base.logging.SysLogUtil;
//import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
//import cn.sunline.ltts.amsg.util.SMSUtil;
//import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
//import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;
//import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
//import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;
//import cn.sunline.ltts.plugin.online.api.OnlineProcessPointExtension;
//import cn.sunline.ltts.plugin.online.facade.OnlineFacade.OnlineFacadeHelper;
//
///**
// * 
// * 
// */
//public class OnlineProcessPointExtensionForAmsg implements OnlineProcessPointExtension {
//    public static final SysLog log = SysLogUtil.getSysLog(OnlineProcessPointExtensionForAmsg.class);
//    //短信相关专项日志
//    public static final BizLog smsLog = BizLogUtil.getBizLog("sms", OnlineProcessPointExtensionForAmsg.class);
//
//    @Override
//    public void flowProcessBefore() {
//
//    }
//
//    @Override
//    public void flowProcessAfter() {
//        // TODO Auto-generated method stub
//    }
//
//    @Override
//    public void dbMainTransactionRollbackBefore(ResponseData responseData) {
//
//    }
//
//    @Override
//    public void dbMainTransactionRollbackAfter(ResponseData responseData) {
//        try {
//            for (ApMessageComplexType.SMSCType sms : getSMSMessageListFromRunEnvs()) {
//                if (sms.getMsgopt() == E_MSGOPT.FAIL || sms.getMsgopt() == E_MSGOPT.BOTH)//只发送失败的
//                    SMSUtil.sendSMSMessage(sms);
//            }
//        } catch (Exception e) {
//            smsLog.error("dbMainTransactionRollbackAfter发送短信失败", e);
//        }
//
//        try {
//            // 交易失败后发送
//            AsyncMessageUtil.publishOrSave(E_MSGOPT.FAIL);
//        } catch (Exception e) {
//            log.error("主交易事务回滚，发送失败的异步消息失败！e:", e);
//        }
//    }
//
//    private List<ApMessageComplexType.SMSCType> getSMSMessageListFromRunEnvs() {
//        RunEnvsComm env = SysUtil.getTrxRunEnvs();
//        return env.getSmsmsg().getValues();
//    }
//
//    @Override
//    public void dbMainTransactionCommitBefore() {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void dbMainTransactionCommitAfter() {
//        try {
//            for (ApMessageComplexType.SMSCType sms : getSMSMessageListFromRunEnvs()) {
//                if (sms.getMsgopt() == E_MSGOPT.SUCESS || sms.getMsgopt() == E_MSGOPT.BOTH)//只发送成功的
//                    SMSUtil.sendSMSMessage(sms);
//            }
//        } catch (Exception e) {
//            smsLog.error("dbMainTransactionCommitAfter发送短信失败", e);
//        }
//
//        try {
//            // 交易成功后发送
//            AsyncMessageUtil.publishOrSave(E_MSGOPT.SUCESS);
//        } catch (Exception e) {
//            log.error("主交易事务已提交，但发送异步消息失败！e:", e);
//        }
//    }
//
//    @Override
//    public void afterWorking(final ResponseData response) {
//    }
//
//    @Override
//    public void beforeWorking(OnlineFacadeHelper arg0, RequestData arg1) {
//    }
//}
