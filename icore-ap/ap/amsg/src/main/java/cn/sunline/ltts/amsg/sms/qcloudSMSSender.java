package cn.sunline.ltts.amsg.sms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.qcloud.sms.SmsSingleSenderFactory;
import com.qcloud.sms.SmsSingleSenderResult;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.amsg.api.SMSSender;
import cn.sunline.ltts.amsg.serviceimpl.IoApAsyncMessageImpl;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTempDefi;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTempDefiDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsShorFail;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsShorFailDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsShorSucc;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsShorSuccDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SMSTYP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 腾讯云互联网接口短信发送实现
 * 
 * @author szyanggx
 * 
 */
public class qcloudSMSSender implements SMSSender {
    public static final BizLog smsLog = BizLogUtil.getBizLog("sms", qcloudSMSSender.class);

    @Override
    public void sendSMSes(List<ApMessageComplexType.SMSCType> smses) {
        for (ApMessageComplexType.SMSCType sms : smses) {
            SmsSingleSenderResult result = new SmsSingleSenderResult();
            try {
                AppTempDefi templ = AppTempDefiDao.selectOne_odb1(sms.getMeteid(), true);
                if (templ.getTemptype().equals(E_YES___.NO)) {//固定模版
                    int tempID = Integer.parseInt(templ.getMeteid());
                    ArrayList<String> params = new ArrayList<String>();
                    if (CommUtil.isNotNull(templ.getRemark())) {
                        String[] paraList = templ.getRemark().split(",");
                        for (int i = 0; i < paraList.length; i++) {
                            params.add(sms.getMsgparm().get(paraList[i]).toString());
                        }
                    }
                    result = SmsSingleSenderFactory.getInstance().sendWithParam(sms.getNacode(), sms.getMobile(), tempID, params, "", "", "");
                } else {//万能模版
                    HashMap<String, Object> context = new HashMap<>();
                    context.put("runEvn", CommUtil.toMap(CommTools.prcRunEnvs()));
                    context.putAll(sms.getMsgparm());
                    String sendSMSContent = IoApAsyncMessageImpl.getMsgTemplet(context, templ.getMetemp());
                    int smsType = 0;
                    if (templ.getSmstyp() == E_SMSTYP.MARKETING) {
                        smsType = 1;
                    }
                    result = SmsSingleSenderFactory.getInstance().send(smsType, sms.getNacode(), sms.getMobile(), sendSMSContent, "", "");

                }
                if (result.result == 0) {
                    smsLog.debug("发送[%s]短信成功", CommTools.toJson(sms));
                    saveShorSucc(sms);
                }
                else {
                    smsLog.error("###发送[%s]短信失败###", CommTools.toJson(sms));
                    smsLog.debug("腾讯云返回错误码:[%s]", result.result + result.errMsg);
                    saveShorFail(sms);
                }

            } catch (Exception e) {
                //TODO 对接日志监控平台
                saveShorFail(sms);
                smsLog.error("###发送[%s]短信失败###", e, CommTools.toJson(sms));
                throw new RuntimeException();
            }
        }
    }

    private static void saveShorSucc(ApMessageComplexType.SMSCType smsMessage) {
        smsLog.debug("开始保存短信到发送成功表!");
        ApsShorSucc succ = SysUtil.getInstance(ApsShorSucc.class);
        String messid = CommTools.getMessageId();
        succ.setMessid(messid);
        succ.setMessty(smsMessage.getMessty());
        succ.setMobile(smsMessage.getMobile());
        succ.setNacode(smsMessage.getNacode());
        succ.setTemplid(smsMessage.getMeteid());
        succ.setPara(smsMessage.getMsgparm().toString());
        ApsShorSuccDao.insert(succ);
    }

    private static void saveShorFail(ApMessageComplexType.SMSCType smsMessage) {
        smsLog.debug("开始保存短信到发送失败表!");
        ApsShorFail fail = SysUtil.getInstance(ApsShorFail.class);
        String messid = CommTools.getMessageId();
        fail.setMessid(messid);
        fail.setMessty(smsMessage.getMessty());
        fail.setMobile(smsMessage.getMobile());
        fail.setNacode(smsMessage.getNacode());
        fail.setTemplid(smsMessage.getMeteid());
        fail.setPara(smsMessage.getMsgparm().toString());
        ApsShorFailDao.insert(fail);
    }
}
