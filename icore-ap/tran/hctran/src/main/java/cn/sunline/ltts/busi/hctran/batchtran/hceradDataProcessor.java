
package cn.sunline.ltts.busi.hctran.batchtran;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.hc.namedsql.HcBatchSqlDao;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbFailSumy;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbFailSumyDao;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbSumy;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpSydt;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcpSydtDao;
import cn.sunline.ltts.busi.hc.util.HotCtrlCacheUtil;
import cn.sunline.ltts.busi.hc.util.HotCtrlUtil;
import cn.sunline.ltts.busi.sys.errors.HcError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AMNTCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_BLNCDR;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HCTYPE;
	 
/**
  * @author jizhirong
  * 热点控制日终差错调整
  * 管理节点将 热点额度节点汇总表 中上日所有节点余额轧差后求出余额，与缓存中发生额对比，
	如果有差错，以数据库轧差余额为准，更新缓存，并登记 热点额度汇总差错表
  *
  */


public class hceradDataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Property, E_HCTYPE, cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbSumy> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(hceradDataProcessor.class);
		private static String corpno;
		private static String lastdt;
		/**
		 * 交易前处理
		 */
		@Override
		public void beforeTranProcess(String taskId, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Property property) {
			corpno = CommTools.prcRunEnvs().getCorpno();
//			AppSydt appSydt = AppSydtDao.selectOne_odb1(corpno, true);//核心日期
			HcpSydt hcpSydt = HcpSydtDao.selectOne_odb1(corpno, true);//热点日期
			
			//获取上个交易日期
			lastdt = hcpSydt.getHclsdt();
			bizlog.info("处理法人："+corpno+"上日："+lastdt+"发生额数据");
		}
	
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
		public void process(String jobId, int index, HcbSumy hcbSumy, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Property property) {
			String hcmain = hcbSumy.getHcmain();//热点主体
			bizlog.info("开始处理热点主体["+hcmain+"]上日发生额");
			//获取上日借方发生额
			BigDecimal dsummam = HcBatchSqlDao.countHcbSumySummamByAmtcd(corpno, E_AMNTCD.DR, hcmain,lastdt, false);
			dsummam = dsummam == null ? BigDecimal.ZERO:dsummam;
			//获取上日贷款发生额
			BigDecimal csummam = HcBatchSqlDao.countHcbSumySummamByAmtcd(corpno, E_AMNTCD.CR, hcmain,lastdt, false);
			csummam = csummam == null ? BigDecimal.ZERO:csummam;
			//获取轧差余额
			BigDecimal summam = BigDecimal.ZERO;
//			E_AMNTCD amntcd = null;//轧差后余额方向
			if(CommUtil.compare(csummam, dsummam)>=0){
				//余额在贷方
				summam = csummam.subtract(dsummam);
			}else{
				//余额在借方
				summam = dsummam.subtract(csummam);
			}
			// 获取缓存中上日发生额
			BigDecimal rsummam = HotCtrlCacheUtil.getHotCtrlAccrualData(hcmain, lastdt);
			bizlog.info("数据库上日发生额:"+summam+";缓存上日发生额："+rsummam);
			E_BLNCDR blncdr = HotCtrlUtil.getHotCtrlBlncdr(hcmain);
			if(blncdr != null){
				bizlog.info("缓存余额方向："+blncdr.getLongName().toString());
				//轧差余额与缓存中上日发生额对比
				if(CommUtil.compare(rsummam, summam) !=0 ){
					//登记热点额度汇总差错表
					registerHcbFailSumy(property, hcmain,rsummam, summam, blncdr);
					//更新缓存余额
					updateRedisBalance(hcmain,rsummam,summam);
				}
			}
		}
		
		/**
		 * 异常处理
		 */
		@Override
		public void jobExceptionProcess(String taskId, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Property property, String jobId, HcbSumy hcbSumy, Throwable t) {
			
		}
		
		
		/**
		 * 获取数据遍历器。
		 * 按热点账户类型拆分
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public CursorBatchDataWalker<E_HCTYPE> getBatchDataWalker(cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Property property) {
			
			Params param = new Params();
			param.put("trandt", lastdt);
			param.put("corpno", corpno);
			return new CursorBatchDataWalker<E_HCTYPE>(HcBatchSqlDao.namedsql_listHcbSumyHctype,param);
		}
		
		/**
		 * 获取作业数据遍历器
		 * 按热点主体拆分
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @param dataItem 批次数据项
		 * @return
		 */
		public BatchDataWalker<HcbSumy> getJobBatchDataWalker(cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Input input, cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Property property,E_HCTYPE dataItem) {
			property.setHctype(dataItem);
			Params param = new Params();
			param.put("hctype", dataItem);
			param.put("trandt", lastdt);
			param.put("corpno", corpno);
			return new CursorBatchDataWalker<HcbSumy>(HcBatchSqlDao.namedsql_listHcbSumyByHctype,param);
		}
		
		/**
		 * 获取热点主体余额方向转换为记账方向
		 * @return
		 */
		private E_AMNTCD changDirection(E_BLNCDR blncdr){
			E_AMNTCD ramntcd = null;
			//余额方向转换为记账方向
			if(blncdr == E_BLNCDR.CR){
				ramntcd = E_AMNTCD.DR;
			} else if(blncdr == E_BLNCDR.DR){
				ramntcd = E_AMNTCD.CR;
			}else{
				throw HcError.HcGen.E0005();
			}
			return ramntcd;
		}
		
		/**
		 * 更新redis余额，以数据库为准
		 * @param hcmain 热点主体
		 * @param summam redis上日发生额
		 * @param summam 轧差后上日发生额
		 */
		private void updateRedisBalance(String hcmain,BigDecimal rsummam,BigDecimal summam){
			//只需更新缓存余额，如果缓存发生额<轧差余额，缓存余额减少，否则增加
			HotCtrlCacheUtil.setBalanceData(hcmain, rsummam.subtract(summam));
		}
		
		/**
		 * 登记热点额度汇总差错表
		 * @param property 全局属性
		 * @param hcmain 热点主体
		 * @param summam 轧差金额
		 * @param amntcd 轧差余额方向
		 * @param rsummam 缓存余额
		 */
		private void registerHcbFailSumy(
				cn.sunline.ltts.busi.hctran.batchtran.intf.Hcerad.Property property,
				String hcmain, BigDecimal rsummam,BigDecimal summam, E_BLNCDR blncdr
				) {
			BigDecimal ajstam = BigDecimal.ZERO;//调整金额
			E_AMNTCD ajstdt = null;//调整方向
			if(CommUtil.compare(rsummam, summam) < 0){
				//缓存发生额 < 轧差余额 ，热点余额需减少,往反方向调整
				ajstam = summam.subtract(rsummam);
				ajstdt = changDirection(blncdr);
			}else{
				//缓存发生额 > 轧差余额 ，热点余额需增加，往正向调整
				ajstam = rsummam.subtract(summam);
				if(blncdr == E_BLNCDR.CR){
					ajstdt = E_AMNTCD.CR;
				}else if(blncdr == E_BLNCDR.DR){
					ajstdt = E_AMNTCD.DR;
				}else{
					throw HcError.HcGen.E0005();
				}
			}
			
			//登记热点额度汇总差错表
			HcbFailSumy hcbFailSumy = CommTools.getInstance(HcbFailSumy.class);
			hcbFailSumy.setAjstam(ajstam);
			hcbFailSumy.setAjstdt(ajstdt);
			hcbFailSumy.setCdcnno(CommToolsAplt.prcRunEnvs().getCdcnno());
			hcbFailSumy.setHcmain(hcmain);
			hcbFailSumy.setHctype(property.getHctype());
			hcbFailSumy.setTranam(ajstam);
			HcbFailSumyDao.insert(hcbFailSumy);
		}
}


