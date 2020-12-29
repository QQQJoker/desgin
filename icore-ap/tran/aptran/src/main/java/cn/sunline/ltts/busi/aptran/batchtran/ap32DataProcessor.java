package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.service.router.drs.util.CustomDRSUtil.TargetInfo;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.ApbDrssTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.bsap.namedsql.SysPublicDao;

/**
 * drs注册失败重注册处理
 * 
 */

public class ap32DataProcessor extends
        AbstractBatchDataProcessor<cn.sunline.ltts.busi.aptran.batchtran.intf.Ap32.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap32.Property, cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbDrss> {
    /**
     * 批次数据项处理逻辑。
     * 
     * @param job 批次作业ID
     * @param index 批次作业第几笔数据(从1开始)
     * @param dataItem 批次数据项
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     */
    @Override
    public void process(String jobId, int index, cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbDrss dataItem, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap32.Input input,
            cn.sunline.ltts.busi.aptran.batchtran.intf.Ap32.Property property) {
        TargetInfo targetDO = new TargetInfo();
        targetDO.setCorpno(dataItem.getCorpno());
        targetDO.setDcnNo(dataItem.getCdcnno());
        if (CommUtil.equals(dataItem.getDrstyp(), "DRS_IDTFNO")) {
        	DcnUtil.registRouteByIDCard(dataItem.getAcctno(), targetDO);
        }
        /*  TODO 注册其它字段需要带主字段 20200620 jym
        if (CommUtil.equals(dataItem.getDrstyp(), "DRS_CARDNO")) {
        	DcnUtil.registRouteByCard(dataItem.getAcctno(), targetDO);
        }
        if (CommUtil.equals(dataItem.getDrstyp(), "DRS_CUSTAC")) {
        	DcnUtil.registRouteByAccount(dataItem.getAcctno(), targetDO);
        }*/
        ApbDrssTools.setDrsStatusSuccess(dataItem.getAcctno());
    }

    /**
     * 获取数据遍历器。
     * 
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     * @return 数据遍历器
     */
    @Override
    public BatchDataWalker<cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbDrss> getBatchDataWalker(cn.sunline.ltts.busi.aptran.batchtran.intf.Ap32.Input input,
            cn.sunline.ltts.busi.aptran.batchtran.intf.Ap32.Property property) {
        Params params = new Params();
        return new CursorBatchDataWalker<cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbDrss>(SysPublicDao.namedsql_listApbDrss, params);
    }

}
