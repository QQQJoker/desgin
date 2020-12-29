package cn.sunline.ltts.busi.aptran.batchtran.dayend;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.custom.comm.type.DayEndBatchType.TaskProcessIn;
import cn.sunline.adp.cedar.server.batch.BatchTaskContext;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.Tsp_taskDao;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.tsp_task;
import cn.sunline.ltts.aplt.namedsql.ApBookDao;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppGldt;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppGldtDao;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.aplt.tools.BatchTools;
import cn.sunline.ltts.busi.aplt.type.ApBook.AppSydtQueryDate;
import cn.sunline.ltts.busi.sys.errors.ApError;

/**
 * 日终执行成功汇报ADM
 * 
 */

public class ap34DataProcessor extends
        BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Ap34.Input, cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Ap34.Property> {

    /**
     * 批次数据项处理逻辑。
     * 
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     */
    @Override
    public void process(cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Ap34.Input input, cn.sunline.ltts.busi.tatran.batchtran.dayend.intf.Ap34.Property property) {
        TaskProcessIn dotaskin = SysUtil.getInstance(TaskProcessIn.class);
//        String flowType = input.getTran_flow_id();
//        String farendma = input.getCorpno();
        tsp_task task = Tsp_taskDao.selectOne_odb_1(BatchTaskContext.get().getTaskId(), false);
        String flowType = task.getTran_flow_id();
        String farendma = task.getCorporate_code();
   //     String farendma = CommTools.getTranCorpno();
        //		 dotaskin.setdcn_num(DMBUtil.getIntanse().getConfiguredDcnNo());
        dotaskin.setDcn_num(task.getSystem_code());
        dotaskin.setCorporate_code(farendma);
        dotaskin.setTran_flow_id(flowType);

        String chaxriqi = "";
        //		 Map<String, Object> hxInfo = SysBatchDao.queryDayEndDate(farendma, true);
        //Map<String, Object> hxInfo = SysBatchDao.queryDayEndDate(farendma, dotaskin.getdcn_num(), true);
        AppSydtQueryDate hxInfo = ApBookDao.selAppSydt(farendma, true);
        if (ApConstants.hx_before.equals(flowType)) {
            chaxriqi =   hxInfo.getSystdt();
            dotaskin.setQuery_date(chaxriqi);
            String shijneir = SysUtil.serialize(dotaskin);
            BatchTools.fireAdmBatchEvent(ApConstants.dayendRepADM_event_key, shijneir);
        } else if (ApConstants.hx_swday.equals(flowType)) {
            chaxriqi = hxInfo.getLastdt();
            dotaskin.setQuery_date(chaxriqi);
            String shijneir = SysUtil.serialize(dotaskin);
            BatchTools.fireAdmBatchEvent(ApConstants.dayendRepADM_event_key, shijneir);
        } else if (ApConstants.hx_dayend.equals(flowType)) {
            chaxriqi = hxInfo.getLastdt();
            dotaskin.setQuery_date(chaxriqi);
            String shijneir = SysUtil.serialize(dotaskin);
            BatchTools.fireAdmBatchEvent(ApConstants.dayendRepADM_event_key, shijneir);
        } else if (ApConstants.gl_dayend.equals(flowType)) {
            //			 Map<String, Object> glInfo = SysBatchDao.queryGLDate(farendma, false);
            AppGldt glInfo = AppGldtDao.selectOne_odb1(farendma, true);
            chaxriqi = (String) glInfo.getSystdt();
            dotaskin.setQuery_date(chaxriqi);
            String shijneir = SysUtil.serialize(dotaskin);
            BatchTools.fireAdmBatchEvent(ApConstants.dayendGLRepADM_event_key, shijneir);
            //TODO:更新日终日期表
        } else
            throw ApError.Aplt.E0000("输入的批量流程类型有误，期待：[hx_before或hx_dayend或gl_dayend]，实际：[" + flowType + "]！");

    }

}
