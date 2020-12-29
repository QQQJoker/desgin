package cn.sunline.ltts.busi.bsap.serviceimpl;


import org.apache.commons.lang3.RandomStringUtils;

import cn.sunline.adp.boot.cedar.launch.IOnlineIDERunner;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.SystemParams;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.Tsp_taskDao;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.tsp_task;
import cn.sunline.ltts.aplt.namedsql.ApBookDao;
import cn.sunline.ltts.busi.aplt.junit.AbstractTest;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.bsap.servicetype.IoApReportService;
import cn.sunline.ltts.busi.bsap.servicetype.IoApReportService.AfterTransRport;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRUE__;

/**
 * 批量组后的汇报ADM服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "RpAdBatchTaskCallBackImpl", longname = "批量后的汇报ADM服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class RpAdBatchTaskCallBackImpl implements cn.sunline.ltts.busi.bsap.servicetype.RpAdBatchTaskCallBack {

    private static final BizLog bizlog = BizLogUtil.getBizLog(RpAdBatchTaskCallBackImpl.class);

    /**
     * 异常处理
     * 
     */
    public void processException(String pljypich, String cuowxinx, String cuowduiz) {
        /*IoApFileBatchReg apFileBatchReg = SysUtil.getInstance(IoApFileBatchReg.class);
        IoApBatchFileStruct.IoApWjplrwxxInfo rwxxInfo = apFileBatchReg.selWjplrwxx(pljypich, ApDCN.getMyDcnNo());
        rwxxInfo.setFlbtst(ApBaseEnumType.E_FLBTST.CLSB);
        rwxxInfo.setErrotx(cuowxinx);
        rwxxInfo.setErrosk(cuowduiz);
        rwxxInfo.setSuccnm((long) 0);
        rwxxInfo.setFailnm((long) 0);
        rwxxInfo.setTotanm((long) 0);
        String shijneir = SysUtil.serialize(rwxxInfo);
        BatchTools.fireAdmBatchEvent("fileCountRegAdm", shijneir);*/
        bizlog.method("=============批量交易执行异常后回调处执行（开始）===============");
        processingTotal(pljypich, E_TRUE__.FALSE);
        bizlog.method("=============批量交易执行异常后回调处执行（结束）===============");

    }

    /**
     * 批量交易执行结束后执行（无论成功或者失败）
     * 
     */
    public void doFinally(String pljypich) {
        bizlog.method("=============批量交易执行结束后回调处执行（开始）===============");
        processingTotal(pljypich, E_TRUE__.TRUE);
        bizlog.method("=============批量交易执行结束后回调处执行（结束）===============");

    }

    /**
     * 
     * @param pljypich
     * @param sucess
     */
   
    private void processingTotal(String pljypich, E_TRUE__ sucess) {
        if (AbstractTest.ideRun || IOnlineIDERunner.isIDERun())//ide运行没有改记录
            return;

        tsp_task task = Tsp_taskDao.selectOne_odb_1(pljypich, false);

        //      String flowdt = task.getTran_date() ;  //modified by xieqq 20170804 to test dayEnd
        String flowdt = task.getTran_date(); //TODO wait for reset  String flowdt = task.getTran_date()
        String flowid = task.getTran_flow_id();
        if(CommUtil.isNull(flowid)){
        	bizlog.info("批次号为【"+pljypich+"】的任务，为非流程批量任务，在批量交易后处理方法中，不登记日终批量执行明细表（aps_eodt_detl）！");
        	return;
        }
        String systcd = task.getSystem_code();
        String corpno = task.getCorporate_code();
        String dcnnum = DcnUtil.getCurrDCN();
        
        long txntal = ApBookDao.selKsysJykzhq(systcd, corpno, pljypich, false);
        AfterTransRport.InputSetter input = CommTools.getInstance(AfterTransRport.InputSetter.class);
        input.setCorpno(corpno);
        input.setDcnnum(dcnnum);
        input.setFlowdt(flowdt);
        input.setFlowid(flowid);
        input.setSucess(sucess);
        input.setSystcd(systcd);
        input.setTxntal(txntal);
        
        //对于是否分布式系统做出判断
        IoApReportService reportService = CommTools.getInstance(IoApReportService.class);
        if (!SystemParams.get().isDistributedSystem()) {
            reportService.AfterTransRport(input);
        } else {
            if(DcnUtil.isAdminDcn(dcnnum)){
                reportService.AfterTransRport(input);
            }else{
            	try{
	        		 IoApReportService reportServiceADM = CommTools.getRemoteInstance(IoApReportService.class);
	                 RunEnvsComm runEnvsComm = SysUtil.getTrxRunEnvs();
	                 if(CommUtil.isNull(runEnvsComm.getBusisq())){
	                 	runEnvsComm.setBusisq(RandomStringUtils.randomNumeric(32));
	     			 }
	                 reportServiceADM.AfterTransRport(input);
            	}catch(Exception e){
            		throw new RuntimeException("["+dcnnum+"]节点执行当前批量交易后,汇报进度给管理节点时失败",e);
            	}
                bizlog.info("["+dcnnum+"]节点执行当前批量交易后，在回调处成功汇报进度给管理节点");
            }
        }
    }
}
