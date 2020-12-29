package cn.sunline.ltts.amsg.api;

import java.util.List;

import cn.sunline.adp.cedar.base.boot.plugin.IReplaceExtension;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;

public interface SMSSender extends IReplaceExtension {

    public static String POINT = "Amsg.SMSSender";

    /**
     * 发送多条短信
     * 
     * @param smses
     */
    public void sendSMSes(List<ApMessageComplexType.SMSCType> smses);

 

}
