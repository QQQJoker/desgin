
package cn.sunline.ltts.busi.hctran.batchtran;


import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.hc.namedsql.HcQuerySqlDao;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefnDao;
import cn.sunline.ltts.busi.hc.util.HotCtrlCacheUtil;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HCSTAS;
	
/**
	  * 日终热点缓存生效失效
	  * 日终查询热点定义表失效日期，如果和当前日期相等，则清除该热点主体缓存中数据，并将缓存状态改为失效。
	  *	如果生效日期与当前日期相等，则将缓存状态改为生效
	  *	@jizhirong 
	  * 20180508
	  */


public class hcremvDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.hctran.batchtran.intf.Hcremv.Input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcremv.Property, cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(hcremvDataProcessor.class);
	private static String trandt = CommToolsAplt.prcRunEnvs().getTrandt();
	private static String corpno = CommToolsAplt.prcRunEnvs().getCorpno();
	/**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param hcpDefn 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpDefn data, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcremv.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcremv.Property property) {
			HcpDefn hcpDefn = SysUtil.getInstance(HcpDefn.class);
			if(CommUtil.compare(trandt, data.getExpidt()) ==0){
				//日终失效
				//删除缓存中余额
				bizlog.info("开始清除["+data.getHcmain()+"]缓存数据，解除热点");
				HotCtrlCacheUtil.removeHotCtrlData(data.getHcmain());
				hcpDefn.setHcstas(E_HCSTAS.SX);
				HcpDefnDao.updateOne_odb1(hcpDefn);//更新热点定义表为失效
			}
			
			if(CommUtil.compare(trandt, data.getEffcdt()) ==0){
				//日终生效
				bizlog.info("开始设置["+data.getHcmain()+"]为热点");
				hcpDefn.setHcstas(E_HCSTAS.ZC);
				HcpDefnDao.updateOne_odb1(hcpDefn);//更新热点定义表为生效
				//HotCtrlCacheUtil.setBalanceData(data.getHcmain(), BigDecimal.ZERO);
			}
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<HcpDefn> getBatchDataWalker(cn.sunline.ltts.busi.hctran.batchtran.intf.Hcremv.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcremv.Property property) {
			Params param = new Params();
			param.put("trandt", trandt);
			param.put("corpno", corpno);
			return new CursorBatchDataWalker<HcpDefn>(HcQuerySqlDao.namedsql_listHcpDefnExpidt,param);
		}

}


