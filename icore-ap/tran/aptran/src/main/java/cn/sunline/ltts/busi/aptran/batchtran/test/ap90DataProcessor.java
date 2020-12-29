package cn.sunline.ltts.busi.aptran.batchtran.test;

import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.adp.cedar.base.logging.BizLog;

/**
 * 按所有DCN编号进行拆分的批量交易例子
 * 
 */
public class ap90DataProcessor extends
        AbstractBatchDataProcessor<cn.sunline.ltts.busi.aplt.batchtran.intf.Ap90.Input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap90.Property, IoApBatch.ApDCN> {
    private static final BizLog bizlog = LogManager.getBizLog(ap90DataProcessor.class);

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
    public void process(String jobId, int index, IoApBatch.ApDCN dataItem, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap90.Input input,
            cn.sunline.ltts.busi.aplt.batchtran.intf.Ap90.Property property) {
        String dcnNo = dataItem.getCdcnno();
        bizlog.debug("dcnNo=%s", dcnNo);
        // TODO: 调用DCN服务实现处理逻辑
    }

    /**
     * 获取数据遍历器。
     * 
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     * @return 数据遍历器
     */
    @Override
    public BatchDataWalker<IoApBatch.ApDCN> getBatchDataWalker(cn.sunline.ltts.busi.aplt.batchtran.intf.Ap90.Input input,
            cn.sunline.ltts.busi.aplt.batchtran.intf.Ap90.Property property) {
        // 按所有DCN遍历
//        return new DcnNoBatchDataWalker<IoApBatch.ApDCN>(
//        /* DCN编号数据转换器 */
//        new DcnDataBuilder<IoApBatch.ApDCN>() {
//            @Override
//            public ApDCN process(int index, String dcnNo) {
//                IoApBatch.ApDCN ret = SysUtil.getInstance(IoApBatch.ApDCN.class);
//                ret.setDcnnum(dcnNo);
//                return ret;
//            }
//        });
    	return null;
    }

}
