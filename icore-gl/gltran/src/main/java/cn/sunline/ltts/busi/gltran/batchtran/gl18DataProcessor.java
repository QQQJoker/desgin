package cn.sunline.ltts.busi.gltran.batchtran;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.gl.regBook.GlRegBook;
import cn.sunline.ltts.busi.iobus.servicetype.pb.IoSrvPbBranch;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo;

/**
 * 应收应付利息红字补计提
 */

public class gl18DataProcessor
		extends
		AbstractBatchDataProcessor<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl18.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl18.Property, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl18DataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(String jobId, int index, cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo dataItem, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl18.Input input,
			cn.sunline.ltts.busi.gltran.batchtran.intf.Gl18.Property property) {
		bizlog.method("gl18 begin>>>>>>>>>>>>>>>");
		bizlog.debug("branchId[%s]", dataItem.getBrchno());
	
		GlRegBook.redInkAccure(dataItem.getBrchno());
		
		bizlog.method("gl18 end>>>>>>>>>>>>>>>");

	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoBrchInfo> getBatchDataWalker(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl18.Input input,
			cn.sunline.ltts.busi.gltran.batchtran.intf.Gl18.Property property) {

		// 调用服务查询非虚拟机构类机构信息
		Options<IoBrchInfo> branchList = SysUtil.getInstance(IoSrvPbBranch.class).getRealBranchList();
		return new ListBatchDataWalker<>(branchList);
	}

}
