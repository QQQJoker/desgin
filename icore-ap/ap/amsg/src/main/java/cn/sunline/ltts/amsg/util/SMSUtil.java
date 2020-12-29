package cn.sunline.ltts.amsg.util;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.util.LangUtil;

import com.alibaba.fastjson.JSONObject;

import cn.sunline.adp.cedar.base.boot.plugin.util.ExtensionUtil;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.amsg.api.SMSSender;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsShorUndo;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsShorUndoDao;
import cn.sunline.ltts.busi.aplt.tools.ApAPI;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType.SMSCType;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_VALID_;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

@ApAPI
public class SMSUtil {
    //短信相关专项日志
    private static final BizLog smsLog = BizLogUtil.getBizLog("sms", SMSUtil.class);

    /**
     * <pre>
     * 描述：增加待通知的短信消息
     * 备注:联机交易使用此方法
     * 1、支持<b>跨DCN</b>交易
     * 2、只有在交易<b>成功</b>后才会真正发送短信
     * </pre>
     */
    public static void addTodoSMSMessage(ApMessageComplexType.SMSCType smsMessageInfo) {
        RunEnvsComm env = SysUtil.getTrxRunEnvs();
        env.getSmsmsg().add(smsMessageInfo);
    }

    /**
     * 发送短信
     * 
     * @param smsMessageList 发送多条
     */
    public static void sendSMSMessage(List<ApMessageComplexType.SMSCType> smsMessageList) {
        try {
            SMSSender smsSender = ExtensionUtil.getExtensionPointImpl(SMSSender.POINT,"process.SMSSender.TCloud");
            boolean b = SystemType.batch == SysUtil.getCurrentSystemType();
            if (b) {//批量入库20170915
                saveBatch(smsMessageList);
            } else {
                smsSender.sendSMSes(smsMessageList);
            }

        } catch (Exception e) {
            //发送短信失败不影响这个交易的事务
            smsLog.error("发送短信失败", e);
        }
    }

    /**
     * 发送短信
     * 
     * @param smsMessage 发送单条
     */
    public static void sendSMSMessage(ApMessageComplexType.SMSCType smsMessage) {
        List<ApMessageComplexType.SMSCType> list = new ArrayList<>();
        list.add(smsMessage);
        sendSMSMessage(list);
    }

  
    private static void saveBatch(List<ApMessageComplexType.SMSCType> smsMessageList) {
        for (SMSCType data : smsMessageList) {
            if (smsLog.isDebugEnabled())
                smsLog.debug("准备保存短信[%s]", data);
            final ApsShorUndo undo = SysUtil.getInstance(ApsShorUndo.class);
            String messid = CommTools.getMessageId();
            //基于消息ID，创建一个groupid
            int groupcount = 0, groupsize = 0;
            try {
                KnpPara knpPara = CommTools.KnpParaQryByCorpno("BatchPublishSMSGroup", "groupcount", "groupsize", "%", true);
                groupcount = Integer.valueOf(knpPara.getPmval1());
                groupsize = Integer.valueOf(knpPara.getPmval2());
            } catch (NumberFormatException e) {
                throw ExceptionUtil.wrapThrow("批量发送消息分组公共业务参数表配置pmva1,pmval2有误，请检查", e);
            }
            int groupid = messid.hashCode() % (groupcount * groupsize);
            if (groupid < 0)
                groupid *= -1;
            JSONObject json=new JSONObject(data.getMsgparm());
            undo.setGroupid(groupid);
            undo.setMessid(messid);
            undo.setMessst(E_VALID_.VALID);
            if(CommUtil.isNull(data.getMessty())){
                undo.setMessty(E_YES___.YES);  
            }else{
                undo.setMessty(data.getMessty());    
            }
            undo.setMobile(data.getMobile());
            undo.setNacode(data.getNacode());
            undo.setTemplid(data.getMeteid());
            undo.setPara(json.toJSONString());
            
            DaoUtil.executeInNewTransation(new RunnableWithReturn<Integer>() {
				@Override
				public Integer execute() {
					smsLog.debug("采用独立事务提交未处理短信信息，短息ID为：【%s】",undo.getMessid());
					return  ApsShorUndoDao.insert(undo);
				}
			});
          
        }
        if (smsLog.isDebugEnabled())
            smsLog.debug("保存短信完毕！");
    }
}
