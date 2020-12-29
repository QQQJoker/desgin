package cn.sunline.ltts.busi.hc.serviceimpl;

import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbSumy;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbSumyDao;
import cn.sunline.ltts.busi.hc.type.HcBatchType.HcBatchReportIn;
import cn.sunline.ltts.busi.hc.util.FileUtil;

/**
 * 热点账户批量服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "HcBatchSrvImpl", longname = "热点账户批量服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class HcBatchSrvImpl implements cn.sunline.ltts.busi.hc.servicetype.HcBatchSrv {

    /**
     * 热点账户日终汇总上日明细
     * 
     */
    public void prcHcBatchReport(HcBatchReportIn cplHcBatchReportIn) {
       
        HcbSumy tblHcbSumy = CommTools.getInstance(HcbSumy.class);
        tblHcbSumy.setCorpno(cplHcBatchReportIn.getCorpno()); // 法人代码
        tblHcbSumy.setCdcnno(cplHcBatchReportIn.getCdcnno()); // 当前DCN编号
        tblHcbSumy.setHcmain(cplHcBatchReportIn.getHcmain()); // 热点主体号
        tblHcbSumy.setHctype(cplHcBatchReportIn.getHctype()); // 热点类型
        tblHcbSumy.setTrandt(cplHcBatchReportIn.getTrandt()); // 交易日期
        tblHcbSumy.setSummam(cplHcBatchReportIn.getSummam()); // 汇总金额
        tblHcbSumy.setAmntcd(cplHcBatchReportIn.getAmntcd()); // 余额方向

        HcbSumyDao.insert(tblHcbSumy);
    }

    /*热点账户文件数据导入库中*/
    public void prcHcDataMerging( String fileph,String tablnm){
    	new FileUtil(tablnm);
		FileUtil.readFileContent(fileph);
    }


}
