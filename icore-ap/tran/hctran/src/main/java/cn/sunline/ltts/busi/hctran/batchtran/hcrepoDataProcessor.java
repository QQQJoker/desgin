
package cn.sunline.ltts.busi.hctran.batchtran;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSydtDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.hc.namedsql.HcBatchSqlDao;
import cn.sunline.ltts.busi.hc.servicetype.HcBatchSrv;
import cn.sunline.ltts.busi.hc.tables.HotCtrl;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefnDao;
import cn.sunline.ltts.busi.hc.type.HcBatchType.HcBatchReportIn;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
	 /**
	  * 热点账户日终汇总上日明细
	  *
	  */


public class hcrepoDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.hctran.batchtran.intf.Hcrepo.Input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcrepo.Property, cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbBlceDetl> {
	  /**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbBlceDetl dataItem, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcrepo.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcrepo.Property property) {
			AppSydt tblAppSydt = AppSydtDao.selectOne_odb1(CommTools.prcRunEnvs().getCorpno(), false);
	        // 热点账户日终汇总上日明细输入信息
	        HcBatchReportIn tpHcBatchReportIn = CommTools.getInstance(HcBatchReportIn.class);
	        String sCdcnno = DcnUtil.getCurrDCN(); // 当前DCN编号
	        HcpDefn tblHcpDefn = HcpDefnDao.selectOne_odb1(dataItem.getHcacct(), false);

	        BigDecimal sumCR =BigDecimal.ZERO;
	        BigDecimal sumDR =BigDecimal.ZERO;
	        // 贷方总发生额
	        BigDecimal sumCRn = HcBatchSqlDao.selHcbBlceDetlBytrandt(dataItem.getHcacct(), tblAppSydt.getLastdt(), E_AMNTCD.CR,CommTools.getTranCorpno(), false);
	        if(CommUtil.isNotNull(sumCRn)){
	        	sumCR=sumCRn;
	        }
	        // 借方总发生额
	        BigDecimal sumDRn = HcBatchSqlDao.selHcbBlceDetlBytrandt(dataItem.getHcacct(), tblAppSydt.getLastdt(), E_AMNTCD.DR,CommTools.getTranCorpno(), false);
	        if(CommUtil.isNotNull(sumDRn)){
	        	sumDR=sumDRn;
	        }
	        
	        E_AMNTCD eAmntcd = null; // 余额方向
	        BigDecimal bigSumman = BigDecimal.ZERO; // 汇总金额

	        if (CommUtil.compare(sumCR, sumDR) > 0) {
	            eAmntcd = E_AMNTCD.CR;
	            bigSumman = sumCR.subtract(sumDR);
	        } else {
	            eAmntcd = E_AMNTCD.DR;
	            bigSumman = sumDR.subtract(sumCR);
	        }

	        tpHcBatchReportIn.setCorpno(CommTools.prcRunEnvs().getCorpno()); // 法人
	        tpHcBatchReportIn.setCdcnno(sCdcnno); // DCN编号
	        tpHcBatchReportIn.setHcmain(dataItem.getHcacct()); // 热点主体号
	        tpHcBatchReportIn.setHctype(tblHcpDefn.getHctype()); // 热点类型
	        tpHcBatchReportIn.setSummam(bigSumman); // 汇总金额
	        tpHcBatchReportIn.setAmntcd(eAmntcd); // 余额方向
	        tpHcBatchReportIn.setTrandt(tblAppSydt.getLastdt()); // 交易日期

	        // 集中式
	        if (!CommTools.isDistributedSystem()) {

	            // 本地汇总
	            CommTools.getInstance(HcBatchSrv.class).prcHcBatchReport(tpHcBatchReportIn);

	        } else { // 分布式

	            if (DcnUtil.isAdminDcn(sCdcnno)) { // 管理节点

	                // 本地汇总
	                CommTools.getInstance(HcBatchSrv.class).prcHcBatchReport(tpHcBatchReportIn);

	            } else { // 零售节点

	                // 本地汇总
	                CommTools.getInstance(HcBatchSrv.class).prcHcBatchReport(tpHcBatchReportIn);
	                // 管理节点汇总
	                CommTools.getRemoteInstance(HcBatchSrv.class).prcHcBatchReport(tpHcBatchReportIn);

	            }
	        }
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbBlceDetl> getBatchDataWalker(cn.sunline.ltts.busi.hctran.batchtran.intf.Hcrepo.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcrepo.Property property) {
			AppSydt tblAppSydt = AppSydtDao.selectOne_odb1(CommTools.prcRunEnvs().getCorpno(), false);
	        Params params = new Params();
	        params.put("trandt", tblAppSydt.getLastdt());
	        params.put("corpno", CommTools.getTranCorpno());
	        //params.put("detlss", E_DETLSS.QCL);
	        return new CursorBatchDataWalker<HotCtrl.HcbBlceDetl>(HcBatchSqlDao.namedsql_selHcbBlceDetlForKey, params);
		}

}


