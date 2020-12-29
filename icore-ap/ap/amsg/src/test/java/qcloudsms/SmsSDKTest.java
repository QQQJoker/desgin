package qcloudsms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.qcloud.sms.SmsMultiSender;
import com.qcloud.sms.SmsMultiSenderResult;
import com.qcloud.sms.SmsSingleSenderFactory;
import com.qcloud.sms.SmsSingleSenderResult;
import com.qcloud.sms.SmsStatusPullCallbackResult;
import com.qcloud.sms.SmsStatusPullReplyResult;
import com.qcloud.sms.SmsStatusPuller;
import com.qcloud.sms.SmsVoicePromptSender;
import com.qcloud.sms.SmsVoicePromptSenderResult;
import com.qcloud.sms.SmsVoiceVerifyCodeSender;
import com.qcloud.sms.SmsVoiceVerifyCodeSenderResult;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.amsg.serviceimpl.IoApAsyncMessageImpl;

public class SmsSDKTest {
    static int appid = 1400036859;
    static String appkey = "8ba5c6a96dd09a7fbcb144b7e19e7ae6";
    String phoneNumber1 = "17767209750";

    @Test
    public void test_send() throws Exception {
        //普通单发
        SmsSingleSenderResult singleSenderResult = SmsSingleSenderFactory.getInstance().send(0, "86", phoneNumber1, "金谷云平台客户操作结果：金谷农商银行提醒：您尾号为0502的电子账户已休眠暂停使用，您可以向电子账户转入任意金额激活账户，即可继续为您服务！", "", "");
        System.out.println("返回结果："+singleSenderResult);
    }
    
    @Test
    public void test() throws Exception {
        if (CommUtil.equals("1", null)) {
           System.out.println("1");
        }
        else{
            System.out.println("2");
        }
    }
    @Test
    public void test_sendWithParam() throws Exception {
        Map<String, Object> test = new HashMap<>();
        test.put("cardno", "0502");
        test.put("brchna", "金谷农商银行");
        Map<String, Object> context = new HashMap<>();
        context.putAll(test);
        String sendSMSContent = IoApAsyncMessageImpl.getMsgTemplet(context, "${brchna}提醒：您尾号为${cardno}的电子账户已休眠暂停使用，您可以向电子账户转入任意金额激活账户，即可继续为您服务！");
   
        //普通单发
        System.out.println("sendSMSContent1："+sendSMSContent);
       // SmsSingleSenderResult singleSenderResult = singleSender.sendWithParam("86", phoneNumber1, 30789, params1, "", "", "");
        SmsSingleSenderResult singleSenderResult = SmsSingleSenderFactory.getInstance().send(0, "86", phoneNumber1, "金谷云平台客户操作结果："+sendSMSContent, "", "");
        System.out.println("返回结果："+singleSenderResult);
    }
    @Test
    public void test_sendWithParam1() throws Exception {
        ArrayList<String> params = new ArrayList<>();
        params.add("SS信用联社");
        params.add("5");
        
        //普通单发
        SmsSingleSenderResult singleSenderResult = SmsSingleSenderFactory.getInstance().sendWithParam("86", phoneNumber1, 30789, params, "", "", "");
        System.out.println("返回结果："+singleSenderResult);
    }
    

    public static void main(String[] args) {
        try {
            //请根据实际 appid 和 appkey 进行开发，以下只作为演示 sdk 使用
            int appid = 1400036859;
            String appkey = "8ba5c6a96dd09a7fbcb144b7e19e7ae6";

            String phoneNumber1 = "17767209750";
            String phoneNumber2 = "17767209750";
            String phoneNumber3 = "17767209750";
            int tmplId = 30789;

            //初始化单发
            SmsSingleSenderResult singleSenderResult;

            //普通单发
            singleSenderResult = SmsSingleSenderFactory.getInstance().send(0, "86", phoneNumber1, "您注册的验证码：1234", "", "");
            System.out.println(singleSenderResult);

            //指定模板单发
            //假设短信模板 id 为 1，模板内容为：测试短信，{1}，{2}，{3}，上学。
            ArrayList<String> params = new ArrayList<>();
            params.add("指定模板单发");
            params.add("深圳");
            params.add("小明");
            singleSenderResult = SmsSingleSenderFactory.getInstance().sendWithParam("86", phoneNumber2, tmplId, params, "", "", "");
            System.out.println(singleSenderResult);

            // 初始化群发
            SmsMultiSender multiSender = new SmsMultiSender(appid, appkey);
            SmsMultiSenderResult multiSenderResult;

            // 普通群发
            // 下面是 3 个假设的号码
            ArrayList<String> phoneNumbers = new ArrayList<>();
            phoneNumbers.add(phoneNumber1);
            phoneNumbers.add(phoneNumber2);
            phoneNumbers.add(phoneNumber3);
            multiSenderResult = multiSender.send(0, "86", phoneNumbers, "测试短信，普通群发，深圳，小明，上学。", "", "");
            System.out.println(multiSenderResult);

            // 指定模板群发
            // 假设短信模板 id 为 1，模板内容为：测试短信，{1}，{2}，{3}，上学。
            params = new ArrayList<>();
            params.add("指定模板群发");
            params.add("深圳");
            params.add("小明");
            multiSenderResult = multiSender.sendWithParam("86", phoneNumbers, tmplId, params, "", "", "");
            System.out.println(multiSenderResult);

            //拉取短信回执和回复
            SmsStatusPuller pullstatus = new SmsStatusPuller(appid, appkey);
            SmsStatusPullCallbackResult callback_result = pullstatus.pullCallback(10);
            System.out.println(callback_result);
            SmsStatusPullReplyResult reply_result = pullstatus.pullReply(10);
            System.out.println(reply_result);

            // 发送通知内容
            SmsVoicePromptSender smsVoicePromtSender = new SmsVoicePromptSender(appid, appkey);
            SmsVoicePromptSenderResult smsSingleVoiceSenderResult = smsVoicePromtSender.send("86", phoneNumber1, 2, 2, "欢迎使用", "");
            System.out.println(smsSingleVoiceSenderResult);

            //语音验证码发送
            SmsVoiceVerifyCodeSender smsVoiceVerifyCodeSender = new SmsVoiceVerifyCodeSender(appid, appkey);
            SmsVoiceVerifyCodeSenderResult smsVoiceVerifyCodeSenderResult = smsVoiceVerifyCodeSender.send("86", phoneNumber1, "123", 2, "");
            System.out.println(smsVoiceVerifyCodeSenderResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
