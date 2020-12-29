package cn.sunline.ltts.busi.bsap.serviceimpl;

import java.math.BigDecimal;
import java.text.NumberFormat;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.type.KBaseEnumType;
import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_PILJYZHT;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.ltts.aplt.namedsql.ApBookDao;
import cn.sunline.ltts.busi.aplt.namedsql.BaseApltDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbEodt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbEodtDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydtDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApsEodtDetl;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApsEodtDetlDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.type.ApBook;
import cn.sunline.ltts.busi.bsap.servicetype.IoApReportService.AfterTransRport.Input;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_EODTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRUE__;

/**
 * 批量后的汇报服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoApReportServiceImpl", longname = "批量后的汇报服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoApReportServiceImpl implements cn.sunline.ltts.busi.bsap.servicetype.IoApReportService {

	// 增加总账日终流程常量
	public static final String GL_DAYEND = "gl_dayend";
	public static final String GL_YEAREND = "gl_yearend";
	private static final BizLog bizlog = BizLogUtil.getBizLog(IoApReportServiceImpl.class);
	
    public void AfterTransRport(Input input) {
        String flowid = input.getFlowid(); 
        String flowdt = input.getFlowdt(); 
        String systcd = input.getSystcd();
        String corpno = input.getCorpno(); 
        String dcnnum = input.getDcnnum(); 
        E_TRUE__ sucess = input.getSucess();
        long txntal = input.getTxntal();
        
        if (E_TRUE__.TRUE.equals(sucess)) {
            BaseApltDao.updateApsEodtDetlforSuc(corpno, flowid, systcd, dcnnum, flowdt);
            EdspCoreBeanUtil.getDBConnectionManager().commit();
        }
        
        ApBook.ApQueryTna queryTna = ApBookDao.selApsEodtDetlfortal(corpno, flowid, flowdt, dcnnum, systcd, false);
        long txnsuc = 0L;
        if(queryTna != null ){
            txnsuc =  queryTna.getTxnsuc();
        }
        Float pecent = 0F;
        E_PILJYZHT dcnste = KBaseEnumType.E_PILJYZHT.onprocess;
        if (E_TRUE__.TRUE.equals(sucess)) {
            if (txntal == 0) {
                dcnste = KBaseEnumType.E_PILJYZHT.success;
                pecent = 100f;
            } else {
                // 创建一个数值格式化对象
                NumberFormat numberFormat = NumberFormat.getInstance();
                // 设置精确到小数点后2位
                numberFormat.setMaximumFractionDigits(2);
                pecent = Float.parseFloat(numberFormat.format((float) txnsuc / (float) txntal * 100));

                if ( pecent >= 100) {
                    dcnste = KBaseEnumType.E_PILJYZHT.success;
                    pecent = 100F;
                } else if (pecent == 0) {
                    dcnste = KBaseEnumType.E_PILJYZHT.failure;
                } else {
                    dcnste = KBaseEnumType.E_PILJYZHT.processing;
                }
            }
        } else {
            dcnste = KBaseEnumType.E_PILJYZHT.failure;
        }
        //得到时间戳
        RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
        
        if (ApBookDao.selApsEodtDetlfordcn(dcnnum, corpno, flowid, systcd, flowdt, false).size() == 0) {
            ApsEodtDetl apsEodtDetl = CommTools.getInstance(ApsEodtDetl.class);
            apsEodtDetl.setCorpno(corpno);
            apsEodtDetl.setDcnnum(dcnnum);
            apsEodtDetl.setDcnste(dcnste);
            apsEodtDetl.setFlowdt(flowdt);
            apsEodtDetl.setFlowid(flowid);
            apsEodtDetl.setPecent(new BigDecimal(pecent));
            apsEodtDetl.setSystcd(systcd);
            apsEodtDetl.setTxnsuc(txnsuc);
            apsEodtDetl.setTxntal(txntal);
            apsEodtDetl.setTmstmp(runEnvs.getTmstmp());
            ApsEodtDetlDao.insert(apsEodtDetl);
        } else {
            //更新批量日终批量执行明细表完成度和时间戳
            BaseApltDao.updateApsEodtDetl(dcnste, txntal, 
                    new BigDecimal(pecent), //完成度
                    runEnvs.getTmstmp(), //时间戳
                    corpno, flowid, systcd, dcnnum, flowdt);
        }
        EdspCoreBeanUtil.getDBConnectionManager().commit();
    }

    
    public void AfterProcessReport(cn.sunline.ltts.busi.bsap.servicetype.IoApReportService.AfterProcessReport.Input input) {
        String flowid = input.getFlowid();
        String flowdt = input.getFlowdt();
        String systcd = input.getSystcd();
        String corpno = input.getCorpno();
        E_EODTP flowtp = input.getFlowtp();
        ApBook.ApQueryApbEodt apbEodt = ApBookDao.selApbEodt(corpno, flowid, systcd, false);
        //判断日期
        if ("".equals(flowdt) || flowdt == null) {
            throw Sys.E0001("输入的交易日期flowdt不能为空");
        }
        
        if (apbEodt == null) {
            RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
            ApbEodt apb = CommTools.getInstance(ApbEodt.class);
            apb.setCorpno(corpno);
            apb.setFlowdt(flowdt);
            apb.setFlowid(flowid);
            apb.setFlowst(BaseEnumType.E_FLOWST.FAD);
            apb.setFlowtp(flowtp);
            apb.setSystcd(systcd);
            apb.setTmstmp(runEnvs.getTmstmp());
            ApbEodtDao.insert(apb);
            EdspCoreBeanUtil.getDBConnectionManager().commit();
        }
         apbEodt = ApBookDao.selApbEodt(corpno, flowid, systcd, false);
        //流程状态已经完成
        if (BaseEnumType.E_FLOWST.SUS.equals(apbEodt.getFlowst())) {
            throw Sys.E0001("输入的交易流程状态已经完成");
        }
        
        // 总账系统的汇报服务处理
        if(GL_DAYEND.equals(flowid) || GL_YEAREND.equals(flowid)){
        	bizlog.info("进入总账系统的汇报服务处理。。");
        	 AppSydt appSydt = AppSydtDao.selectOne_odb1(corpno, false);
        	 //得到时间戳
            RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
            bizlog.info("批量后服务中，更新本次跑批日期的日终状态，同时初始化下一日期的日终状态");
            if(GL_YEAREND.equals(flowid)){
                BaseApltDao.updateApbEodtFlowst(corpno, flowid, appSydt.getSystdt(), systcd, runEnvs.getTmstmp());
                return;
            }
            BaseApltDao.updateApbEodtFlowst(corpno, flowid, appSydt.getLastdt(), systcd, runEnvs.getTmstmp());
        	return;
        }
        //日终
        if (BaseEnumType.E_EODTP.EOD.equals(flowtp)) {
            //得到时间戳
            RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
            //更新日终状态为成功 
            BaseApltDao.updateApbEodtFlowst(corpno, flowid, flowdt, systcd, runEnvs.getTmstmp());
            //日终管理时间
            String eod_flowdt = ApBookDao.selApbEodtDateForFlowtp(corpno, BaseEnumType.E_EODTP.EOD, systcd, false);
            ApBook.AppSydtQueryDate appSydtDate = ApBookDao.selAppSydt(corpno, false);
            /*//日切管理时间
            String swd_flowdt = ApBookDao.selApbEodtDateForFlowtp(corpno, BaseEnumType.E_EODTP.SWD, systcd, false);
            if (swd_flowdt == null || swd_flowdt.isEmpty()) {
                swd_flowdt = "0";
            }*/
            //日终管理时间与日切管理时间比较
            if (Integer.parseInt(appSydtDate.getSystdt()) >= Integer.parseInt(eod_flowdt)) {
                //更新日切管理时间为日终管理时间并且更新管理状态为未跑状态
                BaseApltDao.updateApbEodtFlowdtForFlowtp(appSydtDate.getSystdt(), corpno, BaseEnumType.E_EODTP.SWD, systcd, runEnvs.getTmstmp());
            } else {
                BaseApltDao.updateApbEodtFlowdtForFlowtp(eod_flowdt, corpno, BaseEnumType.E_EODTP.SWD, systcd, runEnvs.getTmstmp());
            }
        }
        //日切
        else if (BaseEnumType.E_EODTP.SWD.equals(flowtp)) {
            //得到时间戳
            RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
            //更新日切状态为成功
            BaseApltDao.updateApbEodtFlowst(corpno, flowid, flowdt, systcd, runEnvs.getTmstmp());
            //日终管理时间
            String swd_flowdt = ApBookDao.selApbEodtDateForFlowtp(corpno, BaseEnumType.E_EODTP.SWD, systcd, false);
            //日初管理时间
            String bod_flowdt = ApBookDao.selApbEodtDateForFlowtp(corpno, BaseEnumType.E_EODTP.BOD, systcd, false);
            if (bod_flowdt == null || bod_flowdt.isEmpty()) {
                bod_flowdt = "0";
            }
            //ApBook.AppSydtQueryDate appSydtDate = ApBookDao.selAppSydt(corpno, false);
            //日终管理时间与日初管理时间比较
            if (Integer.parseInt(bod_flowdt) < Integer.parseInt(swd_flowdt)) {
                //更新日初管理时间为日终管理时间并且更新管理状态为未跑状态
                BaseApltDao.updateApbEodtFlowdtForFlowtp(swd_flowdt, corpno, BaseEnumType.E_EODTP.BOD, systcd, runEnvs.getTmstmp());
            } else {
                BaseApltDao.updateApbEodtFlowdtForFlowtp(bod_flowdt, corpno, BaseEnumType.E_EODTP.BOD, systcd, runEnvs.getTmstmp());
            }
        }
        //日初
        else if (BaseEnumType.E_EODTP.BOD.equals(flowtp)) {
            RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs(); //得到时间戳
            //更新日初管理状态成功
            BaseApltDao.updateApbEodtFlowst(corpno, flowid, flowdt, systcd, runEnvs.getTmstmp());
            //查询系统日期
            ApBook.AppSydtQueryDate appSydtDate = ApBookDao.selAppSydt(corpno, false);
            //日切流程后系统日期已经是T+1日；与批量交易日期作比较
            if (Integer.parseInt(flowdt) <= Integer.parseInt(appSydtDate.getSystdt())) {
                //更新日终管理时间为下一交易系统时间并且更新管理状态为未跑状态
                BaseApltDao.updateApbEodtFlowdt(appSydtDate.getSystdt(), corpno, systcd, runEnvs.getTmstmp());
            } else {
                //更新日终管理时间为下一交易系统时间并且更新管理状态为未跑状态
                BaseApltDao.updateApbEodtFlowdt(appSydtDate.getNextdt(), corpno, systcd, runEnvs.getTmstmp());
            }

        }
        //无多个流程
        else {
            //得到时间戳
            RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
            //单流程下更新管理状态成功
            BaseApltDao.updateApbEodtFlowst(corpno, flowid, flowdt, systcd, runEnvs.getTmstmp());
            //查询系统日期
            ApBook.AppSydtQueryDate appSydtDate = ApBookDao.selAppSydt(corpno, false);
            //日切流程后系统日期已经是T+1日；与批量交易日期作比较
            if (Integer.parseInt(flowdt) <= Integer.parseInt(appSydtDate.getSystdt())) {
                //单流程下更新管理时间为下一交易系统时间并且更新管理状态为未跑状态
                BaseApltDao.updateApbEodtFlowdtForFlowid(appSydtDate.getSystdt(), corpno, flowid, systcd, runEnvs.getTmstmp());
            } else {
                //单流程下更新管理时间为下一交易系统时间并且更新管理状态为未跑状态
                BaseApltDao.updateApbEodtFlowdtForFlowid(appSydtDate.getNextdt(), corpno, flowid, systcd, runEnvs.getTmstmp());
            }
        }
    }

}
