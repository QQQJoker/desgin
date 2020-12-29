package cn.sunline.ltts.busi.aplt.transaction;

import java.util.Map;

import cn.sunline.adp.cedar.base.engine.data.DataArea;

/**
 * 
 * <p>Title:ApSMSProcessor</p>
 * <p>Description:  短息信息接口 </p>
 * @author Cuijia
 * @date   2017年8月31日
 */
public interface ApSMSProcessor {

    /**
     * 
     * <p>Title:process </p>
     * <p>Description:	短息模版参数处理方法</p>
     * @author Cuijia
     * @date   2017年8月31日 
     * @param dataArea 交易数据区
     * @param smsParm 短息模版参数
     * @return  手机号
     */
    String process(DataArea dataArea,Map<String,String> smsParm);
}
