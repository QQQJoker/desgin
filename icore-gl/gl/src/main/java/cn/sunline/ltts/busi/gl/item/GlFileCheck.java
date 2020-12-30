package cn.sunline.ltts.busi.gl.item;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaRegBookDao;
import cn.sunline.ltts.busi.gl.namedsql.GlFileDao;
import cn.sunline.ltts.busi.gl.type.GlFile.GlFileLaodCom;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_BATCHTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaTools;

public class GlFileCheck {

    /**
     * <p>Title:doCheckFiles </p>
     * <p>Description:	检查存贷计提文件是否存在</p>
     * @author MZA
     * @date   2018年2月23日 
     */
    public static void doCheckFiles(){
        List<GlFileLaodCom> fileLoads = new ArrayList<GlFileLaodCom>();

        // 从系统杂项表中取出等待时间, 单位为秒 10
        int waitTime = FaTools.getWaitTime();
        int sleepTime = 0;
        do {
            // 从平台登记的表中取出已下载文件的信息
            fileLoads = GlFileDao.lstFileLoanDown(CommToolsAplt.prcRunEnvs().getCorpno(), E_BATCHTYPE.ACCRUE_DOWN, E_FILEDEALSTATUS.UNCHECK, E_YESORNO.YES, false);
            // 查询到数据，表明平台定时任务已下载数据，退出循环
            if (fileLoads.size() > 0) {
                break;
            }

            // 如果无数据,则睡眠1秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            	Thread.currentThread().interrupt();
            }
            // 总睡眠时间
            sleepTime = sleepTime + 1;
            // 超时，
            if (sleepTime > waitTime) {
                throw GlError.GL.E0210();
            }
        }
        while (true);

    }
    
    /**
     * <p>Title:doCheckBalFile </p>
     * <p>Description:	检查分户账余额文件是否存在</p>
     * @author MZA
     * @date   2018年2月23日 
     */
    public static void doCheckBalFile(){
        List<GlFileLaodCom> fileLoads = new ArrayList<GlFileLaodCom>();

        // 从系统杂项表中取出等待时间, 单位为秒 10
        int waitTime = FaTools.getWaitTime();
        int sleepTime = 0;
        do {
            // 从平台登记的表中取出已下载文件的信息
            fileLoads = GlFileDao.lstFileLoanDown(CommToolsAplt.prcRunEnvs().getCorpno(), E_BATCHTYPE.LEDGER_DOWN, E_FILEDEALSTATUS.UNCHECK, E_YESORNO.YES, false);
            // 查询到数据，表明平台定时任务已下载数据，退出循环
            if (fileLoads.size() > 0) {
                break;
            }
            // 如果无数据,则睡眠1秒
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            	Thread.currentThread().interrupt();
            }
            // 总睡眠时间
            sleepTime = sleepTime + 1;
            // 超时，
            if (sleepTime > waitTime) {
                throw GlError.GL.E0211();
            }
        }
        while (true);
    }
    
    /**
     * <p>Title:doCheckAccountingFile </p>
     * <p>Description:	检查对账文件是否存在</p>
     * @author MZA
     * @date   2018年2月23日 
     */
    public static void doCheckAccountingFile(){
       
        // 文件记录信息
        List<GlFileLaodCom> fileLoads = new ArrayList<GlFileLaodCom>();

        // 从平台登记的表中取出已下载文件的信息
        fileLoads = GlFileDao.lstFileLoanDown(CommToolsAplt.prcRunEnvs().getCorpno(), E_BATCHTYPE.CHECK_DOWN, E_FILEDEALSTATUS.UNCHECK, E_YESORNO.YES, false);
        
        if(fileLoads.size()>1){
         // 对账文件应只有一个
            throw GlError.GL.E0108(E_BATCHTYPE.CHECK_DOWN.getLongName());
        }else if(fileLoads.size() != 1){
            throw GlError.GL.E0212();
        }
    }
    
    /**
     * <p>Title:doCheckEodBefore </p>
     * <p>Description:	会计流水检查</p>
     * @author MZA
     * @date   2018年2月23日 
     */
    public static void doCheckEodBefore(){
        String trxnDate = CommTools.prcRunEnvs().getTrandt();
        
        // 当日有原始凭证科目号为空的总笔数
        long originalVoch = FaRegBookDao.cntCountOriginalVoch(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
        if (originalVoch > 0) {
            // 还有未处理会计事件记录[%d]
            throw GlError.GL.E0213(originalVoch);
        }
        
        // 当日有非已汇总的会计事件流水
        long accountedSeq = FaRegBookDao.cntAccountedSeq(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
        if (accountedSeq > 0) {
            // 还有未处理会计事件记录[%d]
            throw GlError.GL.E0213(accountedSeq);
        }
        
        // 当日总分核对流水总数
        long ledgerCheckSeq = FaRegBookDao.cntCountLedgerCheckSeq(CommToolsAplt.prcRunEnvs().getCorpno(), trxnDate, false);
        if (ledgerCheckSeq > 0) {
            // 还有未处理总分核对流水记录[%d]
            throw GlError.GL.E0213(ledgerCheckSeq);
        }
        
    }
}






