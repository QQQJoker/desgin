package cn.sunline.ltts.busi.hctran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.ApltEngineContext;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.hc.namedsql.HcBatchSqlDao;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpSydt;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpSydtDao;
import cn.sunline.ltts.busi.hc.util.HotCtrlUtil;
import cn.sunline.ltts.busi.sys.errors.HcError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_DETLSS;

public class hcdateDataProcessor extends
        BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.hctran.batchtran.intf.Hcdate.Input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcdate.Property> {

    /**
     * 批次数据项处理逻辑。
     * 
     * @param input 批量交易输入接口
     * @param property 批量交易属性接口
     */
    private static final BizLog bizlog = BizLogUtil.getBizLog(hcdateDataProcessor.class);

    @Override
    public void process(cn.sunline.ltts.busi.hctran.batchtran.intf.Hcdate.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcdate.Property property) {
        HcpSydt tblHcpSydt = HcpSydtDao.selectOneWithLock_odb1(CommTools.getTranCorpno(), true);
        
        KnpPara tblKnpPara = CommTools.KnpParaQryByCorpno("hcdate", "HcbPendDeal", "tablesname", "number", false);
        String sTableName = tblKnpPara.getPmval1(); // 表名
        
        int tableCnt = Integer.parseInt(tblKnpPara.getPmval2()); // 表个数
        //0表
        long lRecord = HcBatchSqlDao.selHcbPedlByTrandt(sTableName, tblHcpSydt.getHcsydt(), E_DETLSS.QTY,CommTools.getTranCorpno(), false);
        if(lRecord > 0) {
        	bizlog.info("表[%s]存在未处理完成数据[%s]条", sTableName, lRecord);
        }
    	//0以上表
        for (int i = 1; i < tableCnt; i++) {        	
            //sTableName = new StringBuilder(sTableName).append(i).toString();    
            long count = HcBatchSqlDao.selHcbPedlByTrandt(sTableName+i, tblHcpSydt.getHcsydt(), E_DETLSS.QTY,CommTools.getTranCorpno(), false);
            if(count > 0) {
            	bizlog.info("表[%s]存在未处理完成数据[%s]条", sTableName+i, count);
                lRecord += count;
            }
        }

        if (CommUtil.compare(lRecord, 0L) == 0) {
            bizlog.info("=============热点账户开始日切===============");
            bizlog.info("=============" + CommTools.getTranCorpno() + "===============");
            
            tblHcpSydt.setHcbfdt(tblHcpSydt.getHclsdt());
            tblHcpSydt.setHclsdt(tblHcpSydt.getHcsydt());

            if ("".equals(tblHcpSydt.getHcnxdt())) {
                throw HcError.HcComm.E0001();
            } else {
                tblHcpSydt.setHcsydt(tblHcpSydt.getHcnxdt());
            }

            tblHcpSydt.setHcnxdt(tblHcpSydt.getHcafdt());
            tblHcpSydt.setHcafdt(DateTools.calDateByTerm(tblHcpSydt.getHcafdt(), "1D"));
            HcpSydtDao.updateOne_odb1(tblHcpSydt);
            //缓存日期
            ApltEngineContext.getTxnTempObjMap().put(HotCtrlUtil.HOT_CTRL_DATE_KEY, tblHcpSydt.getHcsydt());
            bizlog.info("=============热点账户日切结束===============");
        } else {
            bizlog.info("=========热点额度待处理明细表还有 " + lRecord + " 条记录未处理完成不能切日==========");
            throw HcError.HcComm.E0002();
        }
    }
}
