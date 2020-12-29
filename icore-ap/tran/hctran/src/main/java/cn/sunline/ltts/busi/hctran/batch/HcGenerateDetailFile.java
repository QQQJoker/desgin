package cn.sunline.ltts.busi.hctran.batch;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.hc.namedsql.HcQuerySqlDao;
import cn.sunline.ltts.busi.hc.util.FileUtil;
import cn.sunline.ltts.busi.hc.util.HotCtrlUtil;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;

public class HcGenerateDetailFile extends LttsTimerProcessor {
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(HcGenerateDetailFile.class);
    @Override
    public void process(String arg0, DataArea paramData) {
        KnpPara knpPara = CommTools.KnpParaQryByCorpno("hc", "hcb_pedl", "%", "%", true);
        KnpPara knpPara2 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "SELECT_LIMIT_SYNC",true);
		KnpPara knpPara3 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "UPDATE_LIMIT_SYNC",true);
		KnpPara knpPara4 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "TIME",true);
		int updateCunt=Integer.parseInt(knpPara3.getPmval3());//更改已同步限制条数
		int limiCunt=Integer.parseInt(knpPara2.getPmval1());//查询条数				
		int processTime = Integer.parseInt(knpPara4.getPmval1());//秒,运行时间
		int waitTime = Integer.parseInt(knpPara4.getPmval3());//秒，等待时间
		long startTime = System.currentTimeMillis();
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		String corpno = runEnvs.getCorpno();
		boolean hasData = false;//是否有待读取文件
		String tabnum=HotCtrlUtil.getTabnum(paramData);//表号		
        String tableName = "hcb_pedl"+tabnum; // 表名     
        do {      
            // 修改待同步的待处理明细为同步中 
           //HcQuerySqlDao.updHcbPedlSyncstZtO(tableName,limiCunt,corpno);
           updHcbPedlSyncstZt(tableName,limiCunt,corpno);
           long count=HcQuerySqlDao.SelHcbPedlSync(tableName, false);
           if(count>0){
        	   BIZLOG.info("************** 读取表数据导出文件 ********************");
               FileUtil.wirteTableContent(knpPara, tableName,HcQuerySqlDao.namedsql_listHcbPedl,tabnum); 
               updHcbPedlSync(tableName,updateCunt,corpno);
               hasData=true;	
           }
	       if(!hasData){
				// 如果无数据,则等待waitTime
				try {
					BIZLOG.info("HcGenerateDetailFile.process begin waiting：" + waitTime + "秒");
					Thread.sleep(waitTime*1000);
				} catch (InterruptedException e) {
					BIZLOG.error("[%s]", e.toString());
				}
			}
	    	long endTime = System.currentTimeMillis();
			BIZLOG.info("HcGenerateDetailFile.process time：" + processTime + "秒");
			if((endTime - startTime)/1000 > processTime) {
				break;
			}
	       }while (true);
	       BIZLOG.info("HcGenerateDetailFile end process");         
    	}

    
    public void updHcbPedlSync(final String tableName,final int updateCunt,final String corpno) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {

				 BIZLOG.info("HcGenerateDetailFile独立事务更改同步成功");

				 HcQuerySqlDao.updHcbPedlSyncstOtT(tableName,updateCunt,corpno);
				 return null;
			}
		});
	}
    public void updHcbPedlSyncstZt(final String tableName,final int limiCunt,final String corpno) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				 BIZLOG.info("HcGenerateDetailFile独立事务更改同步中");
				 HcQuerySqlDao.updHcbPedlSyncstZtO(tableName,limiCunt,corpno);
				 return null;
			}
		});
	}

}
