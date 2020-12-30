package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.item.GlBranch;
import cn.sunline.ltts.busi.gl.item.GlDepositPaid;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 准备金处理
 * 
 */

public class gl80DataProcessor extends
        BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl80.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl80.Property> {
    private static final BizLog bizlog = BizLogUtil.getBizLog(GlBranch.class);

    /**
     * 批次数据项处理逻辑。
     * 
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     */
    @Override
    public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl80.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl80.Property property) {
        bizlog.method("gl80 process [%s] Begin>>>>>>>>>>>>");

        RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
        String trxnDate = runEnvs.getTrandt();
        String orgId = runEnvs.getCorpno();

        GlDepositPaid.prcMain(orgId, trxnDate);

        bizlog.method("gl80 process [%s] end>>>>>>>>>>>>");
    }

}
