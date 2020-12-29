package cn.sunline.ltts.amsg.sms;

import java.util.HashMap;
import java.util.Map;

import cn.sunline.ltts.amsg.util.SMSUtil;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.cedar.base.boot.spi.BootProcessPointExtension;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTempDefi;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTempDefiDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SMSTYP;

import com.qcloud.sms.SmsSenderConfig;

/**
 * 用于统计启动耗时
 * 
 * @author lizhs
 * @date 2017年8月1日
 */
public class StartupElapsed implements BootProcessPointExtension {
    public static long starttime;
    private static final String  Meteid="10000";
    @Override
    public void serverStartBefore() {
        starttime = System.currentTimeMillis();
    }

    //这个只会在启动成功后才回调
    @Override
    public void serverStartAfter() {
        if( !SmsSenderConfig.isEnabled() )
        	return;

        //申请对应短信摸版 ${vmid}系统已经启动成功，启动总耗时为${elapsed}
        AppTempDefi templ = AppTempDefiDao.selectOne_odb1(Meteid, false);
        //默认插入
        if(templ==null){
            AppTempDefi temp = SysUtil.getInstance(AppTempDefi.class);
            temp.setCorpno(CommTools.getTranCorpno());
            temp.setMeteid(Meteid);
            temp.setMetemp("金谷云平台客户操作结果：${vmid}系统已经在机器${ip}启动成功，启动总耗时为${elapsed}");
            temp.setSmstyp(E_SMSTYP.NORMAL);
            AppTempDefiDao.insert(temp);
        }
        
        if (StringUtil.isNotEmpty(SmsSenderConfig.getPhoneNumber()) && StringUtil.isNotEmpty(SmsSenderConfig.getTemplId())) {
            long endTime = System.currentTimeMillis();
            long startupElapsed = (endTime - starttime) / 1000;
            try {
                ApMessageComplexType.SMSCType  sms=SysUtil.getInstance(ApMessageComplexType.SMSCType.class);
                sms.setMeteid(Meteid);
                sms.setMobile( SmsSenderConfig.getPhoneNumber());
                sms.setMsgopt(E_MSGOPT.BOTH);
                Map<String,String> msgparm=new HashMap<>();
                msgparm.put("vmid", SysUtil.getVmid());
                msgparm.put("ip", SysUtil.getIp());
                msgparm.put("elapsed",startupElapsed + "s");
                sms.setMsgparm(msgparm);
                sms.setNacode("86");
                SMSUtil.sendSMSMessage(sms);
            } catch (Exception e) {
                throw ExceptionUtil.wrapThrow("发送启动短信失败", e);
            }
        }
    }

}
