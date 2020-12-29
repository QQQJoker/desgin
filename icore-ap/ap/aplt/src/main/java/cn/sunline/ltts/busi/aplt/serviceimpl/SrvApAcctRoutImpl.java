package cn.sunline.ltts.busi.aplt.serviceimpl;

import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;

/**
 * 账号路由服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "SrvApAcctRoutImpl", longname = "账号路由服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvApAcctRoutImpl implements cn.sunline.ltts.busi.aplt.servicetype.SrvApAcctRout {
    /**
     * 获取账户所属法人
     * 
     */
    public void getAcctCorpno(String acctno, final cn.sunline.ltts.busi.aplt.servicetype.SrvApAcctRout.GetAcctCorpno.Output output) {
        output.setCorpno(ApAcctRoutTools.getAcctCorpno(acctno));
    }
}
