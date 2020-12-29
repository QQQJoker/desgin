package cn.sunline.ltts.busi.aptran.batchtran.test;

import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch;
//import cn.sunline.ltts.srv.dmb.DMBUtil;

	 /**
	  * 基于DcnCursorBatchDataWalker实现的批量交易例子。
	  *
	  */

public class ap91DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.aplt.batchtran.intf.Ap91.Input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap91.Property, IoApBatch.ApDcnForPldlmx> {

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
	public void process(String jobId, int index, IoApBatch.ApDcnForPldlmx dataItem, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap91.Input input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap91.Property property) {
		//TODO:
	}
	
	/**
	 * 获取数据遍历器。
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<IoApBatch.ApDcnForPldlmx> getBatchDataWalker(cn.sunline.ltts.busi.aplt.batchtran.intf.Ap91.Input input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap91.Property property) {
	    // 基于游标获取数据
//		return new DcnCursorBatchDataWalker<IoApBatch.ApDcnForPldlmx>(
//	        /* 命名SQL FULL ID */
//	        "xxx.xxx",
//	        /* 查询参数 */
//	        new Params(),
//	        /* 多少笔进行一次GNS批量查询 */
//	        100,
//	        /* GNS批量查询及数据转换器 */
//	        new DataBuilderWichDcnBatchQuery<IoApBatch.ApDcnForPldlmx>() {
//                @Override
//                public List<ApDcnForPldlmx> build(int start, List<ApDcnForPldlmx> items) {                        
//                    // 遍历数据项获取账号列表
//                    List<String> accountNoList = new ArrayList<String>();
//                    for (ApDcnForPldlmx item : items) {
//                        accountNoList.add(item.getCustac());
//                    }
//                    // 批量查询
////                    try {
//                        //List<String> dcnNos = DMBUtil.getIntanse().findDcnNoByAccountNo(accountNoList);
//                        List<String> dcnNos = DMBUtil.getIntanse().findAllDcnNosWithAdmin();
//                        int index = 0;
//                        for (String dcnNo : dcnNos) {
//                            // 获取对应的数据项并设置DCN编号
//                            items.get(index).setDcnnum(dcnNo);
//                            index++;
//                        } // for
////                    } catch (GNSAccessException e) {
////                        throw new RuntimeException("GNS批量查询失败", e);
////                    }
//                    
//                    return items;
//                }
//         });
		return null;
	}

}


