package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.gl.report.GlReports;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTNAME;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_BRCHTP;
import cn.sunline.ltts.fa.util.FaTools;

/**
 * 生成三大报表数据-账务机构
 * 
 */

public class gl55DataProcessor extends
        AbstractBatchDataProcessor<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl55.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl55.Property, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo> {
    private static final BizLog bizlog = BizLogUtil.getBizLog(gl55DataProcessor.class);

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
    public void process(String jobId, int index, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo dataItem, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl55.Input input,
            cn.sunline.ltts.busi.gltran.batchtran.intf.Gl55.Property property) {

        bizlog.method("gl55DataProcessor begin>>>>>>>>");
        bizlog.debug("branchId[%s]", dataItem.getBrchno());
        RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
        String orgId = runEnvs.getCorpno();
        
        
        E_REPORTNAME[] reportNameList = E_REPORTNAME.values();
        // 单独生成某段时间报表使用
        if(CommUtil.isNotNull(input.getBegndt()) && CommUtil.isNotNull(input.getEndddt())){
        	String trxnDate = input.getBegndt();
        	E_REPORTTYPE reportType = E_REPORTTYPE.BUSINETT_TYPE; 
			if(DateTools2.isLastDay("Y", trxnDate) && !FaTools.getYearendStatus()){
				reportType = E_REPORTTYPE.TRIAL_TYPE;
			}
        	while(true){
        		bizlog.debug("[%s]日三大报表数据生成开始", trxnDate);
                GlReports.genThreeReportsMain(orgId, reportType, reportNameList, trxnDate, dataItem.getBrchno());
                bizlog.debug("[%s]日三大报表数据生成结束", trxnDate);
        		trxnDate = DateTools2.dateAdd("day", trxnDate, 1);
        		// 大于结束日期则跳出
        		if(CommUtil.compare(trxnDate, input.getEndddt()) > 0){
        			break;
        		}
        	}
        }else{
            String trxnDate = runEnvs.getTrandt();
            E_REPORTTYPE reportType = E_REPORTTYPE.BUSINETT_TYPE; 
			if(DateTools2.isLastDay("Y", trxnDate) && !FaTools.getYearendStatus()){
				reportType = E_REPORTTYPE.TRIAL_TYPE;
			}
            bizlog.debug("[%s]日三大报表数据生成开始", trxnDate);
            GlReports.genThreeReportsMain(orgId, reportType, reportNameList, trxnDate, dataItem.getBrchno());
            bizlog.debug("[%s]日三大报表数据生成结束", trxnDate);
        }

        bizlog.method("gl55DataProcessor end>>>>>>>>");

    }

    /**
     * 获取数据遍历器。
     * 
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     * @return 数据遍历器
     */
    @Override
    public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo> getBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl55.Input input,
            cn.sunline.ltts.busi.gltran.batchtran.intf.Gl55.Property property) {

        // 调用服务查询账务机构列表
    	Options<IoBrchInfo> branchList = SysUtil.getInstance(IoSrvPbBranch.class).getBranchListByBrchtp(E_BRCHTP.ACCT, CommToolsAplt.prcRunEnvs().getCorpno());
		return new ListBatchDataWalker<>(branchList);
    }

}
