package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.gl.item.GlGeneralLedger;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.fa.util.FaTools;
	 /**
	  * 生成业务总账-账务机构
	  *
	  */

public class gl51DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl51.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl51.Property, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo> {
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo dataItem, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl51.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl51.Property property) {

			RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
			String trxnDate = runEnvs.getTrandt();
			String orgId = runEnvs.getCorpno();
			
			E_REPORTTYPE reportType = E_REPORTTYPE.BUSINETT_TYPE; 
			if(DateTools2.isLastDay("Y", trxnDate) && !FaTools.getYearendStatus()){
				reportType = E_REPORTTYPE.TRIAL_TYPE;
			}
			
			
			GlGeneralLedger.genBusiGL(orgId, reportType, trxnDate, dataItem.getBrchno());
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo> getBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl51.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl51.Property property) {
			// 调用服务查询非虚拟机构类机构信息
			Options<IoBrchInfo> branchList = SysUtil.getInstance(IoSrvPbBranch.class).getRealBranchList();
			return new ListBatchDataWalker<>(branchList);
		}

}

