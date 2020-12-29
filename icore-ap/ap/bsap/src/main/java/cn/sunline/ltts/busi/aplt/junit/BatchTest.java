package cn.sunline.ltts.busi.aplt.junit;

import org.junit.Before;
import org.junit.BeforeClass;

import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.logging.LogConfigManager;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.Tsp_taskDao;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.tsp_task;
import cn.sunline.adp.cedar.server.batch.util.BatchTaskSubmitInfo;
import cn.sunline.adp.metadata.base.config.ConfigConstants;
import cn.sunline.adp.cedar.base.type.KBaseEnumType;
import cn.sunline.ltts.busi.aplt.tools.ApAPI;
import cn.sunline.adp.cedar.server.batch.errors.EngineBatchPluginErrorDef.SP_EB;
import cn.sunline.adp.cedar.base.engine.data.DataArea;

/**
 * <pre>
 * 用于单元测试 批量交易
 * 
 * 说明：整个测试过程平台只会启动一次
 * 
 * 例子：
 * bat.batchgTest 测试批量的例子
 * 
 * </pre>
 * 
 * @author lizhs
 * @date 2017年6月21日
 */
@ApAPI
public class BatchTest extends AbstractTest {
    private RunMode runMode = RunMode.trans;

    public void setRunMode(RunMode runMode) {
        this.runMode = runMode;
    }

    //    setting.dev_batch.properties
    @BeforeClass
    public static void init2() {
        System.setProperty(ConfigConstants.SETTING_FILE, "setting.dev_batch.properties");
    }

    @Override
    public ResponseData _call() {

        DataArea taskArea = DataArea.buildWithData(requestPkgMap);
        String prcscd = taskArea.getSystem().getString("prcscd");
        String groupId = taskArea.getSystem().getString("groupId");
        String flowId = taskArea.getSystem().getString("flowId");

        if (StringUtil.isEmpty(prcscd) && StringUtil.isEmpty(groupId)) {
            //throw new IllegalArgumentException("必须配置批量交易组id或者批量交易码");
            throw SP_EB.E066();
        }

        String taskId = "junit_" + SysUtil.nextValue("junit_taskId");//UUID.randomUUID().toString();
        System.out.println("###批次号：" + taskId);
        //        String taskId = taskArea.getInput().getString(BatchConfigConstant.TASK_ID);

        //        if (StringUtil.isEmpty(taskId))
        //            taskId = BatchUtil.getTaskId();
        taskArea.getSystem().add(BatchConfigConstant.TASK_ID, taskId);

        taskArea.getSystem().add(BatchConfigConstant.BATCH_TRAN_DATE, taskArea.getCommReq().getString(BatchConfigConstant.BATCH_TRAN_DATE));
        taskArea.getSystem().add(BatchConfigConstant.BATCH_TENANT_ID, taskArea.getCommReq().getString(BatchConfigConstant.BATCH_TENANT_ID)); //frw 2.6.4
        //taskArea.getSystem().add(BatchConfigConstant.BATCH_LEGAL_PERSON, taskArea.getCommReq().getString(BatchConfigConstant.BATCH_LEGAL_PERSON));
        //        DataArea dataArea = DataArea.buildWithInput(taskArea.getInput().getMap());
        //        dataArea.setSystem(taskArea.getSystem().getMap());
        //        dataArea.setCommReq(taskArea.getCommReq().getMap());

        if (runMode == RunMode.trans) {
            if (StringUtil.isEmpty(prcscd) && StringUtil.isEmpty(groupId)) {
                //throw new IllegalArgumentException("必须配置批量交易组id或者批量交易码");
                throw SP_EB.E066();
            }

            BatchTaskSubmitInfo taskInfo = BatchTaskSubmitInfo.createBatchTranTask(taskId, groupId, prcscd, taskArea);
            cn.sunline.adp.cedar.server.batch.util.BatchUtil.submitAndRunTask(taskInfo, true);

            //            BatchResources.get().getBatchTaskInfoManager().registBatchTaskInfo(taskInfo);
            //            BatchResources.get().getBatchTaskInfoManager().getBatchTaskInfo(batchTaskId);
            //            BatchTaskInfo batchTaskInfo = BatchResources.get().getBatchTaskInfoManager().getBatchTaskInfo(taskId);
            //            BatchTaskExecuteInfo batchTaskExecuteInfo = BatchResources.get().getBatchTaskExecuteInfoManager().createBatchTaskExecuteInfo(batchTaskInfo);
            //            BatchTaskEngine taskEngine = new BatchTaskEngine(new DefaultBatchTaskExecutorRouter(), batchTaskExecuteInfo);
            //            taskEngine.runDebug(groupId, prcscd, taskArea);
        }
        if (runMode == RunMode.group) {
            if (StringUtil.isEmpty(groupId)) {
                throw ExceptionUtil.wrapThrow("批量交易组[groupId]不能为空");
            }
            BatchTaskSubmitInfo taskInfo = BatchTaskSubmitInfo.createBatchTranGroupTask(taskId, groupId, taskArea);//(taskId, flowId, taskArea);
            cn.sunline.adp.cedar.server.batch.util.BatchUtil.submitAndRunTask(taskInfo, true);

            //            BatchTaskInfo batchTaskInfo = BatchResources.get().getBatchTaskInfoManager().getBatchTaskInfo(taskId);
            //            BatchTaskExecuteInfo batchTaskExecuteInfo = BatchResources.get().getBatchTaskExecuteInfoManager().createBatchTaskExecuteInfo(batchTaskInfo);
            //            BatchTaskEngine taskEngine = new BatchTaskEngine(new DefaultBatchTaskExecutorRouter(), batchTaskExecuteInfo);
            //            taskEngine.runDebug(groupId, null, taskArea);
        }
        if (runMode == RunMode.flow) {
            if (StringUtil.isEmpty(flowId)) {
                throw ExceptionUtil.wrapThrow("批量流程ID[flowId]不能为空");
            }

            BatchTaskSubmitInfo taskInfo = BatchTaskSubmitInfo.createBatchFlowTask(taskId, flowId, taskArea);
            cn.sunline.adp.cedar.server.batch.util.BatchUtil.submitAndRunTask(taskInfo, true);

            //            BatchTaskInfo batchTaskInfo = BatchResources.get().getBatchTaskInfoManager().getBatchTaskInfo(taskId);
            //            BatchTaskExecuteInfo batchTaskExecuteInfo = BatchResources.get().getBatchTaskExecuteInfoManager().createBatchTaskExecuteInfo(batchTaskInfo);
            //            BatchTaskEngine taskEngine = new BatchTaskEngine(new DefaultBatchTaskExecutorRouter(), batchTaskExecuteInfo);
            //            taskEngine.runBatchTranFlow(flowId, taskArea);
        }
        //等待
        while (true) {
            tsp_task task = Tsp_taskDao.selectOne_odb_1(taskId, false);
            if (task.getTran_state() == KBaseEnumType.E_PILJYZHT.success)
                break;
            if (task.getTran_state() == KBaseEnumType.E_PILJYZHT.failure) {
                throw ExceptionUtil.wrapThrow("跑批失败  " + task.getError_message()+" "+task.getError_stack());
            }
            else
                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return null;
    }

    @Before
    public void init() {
        //        LogConfigManager.get().setCurrentLogType(LogType.busi_batch);
        LogConfigManager.get().setCurrentSystemType(SystemType.batch);
    }

    public static enum RunMode {
        trans, group, flow;
    }

}
