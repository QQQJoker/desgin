package cn.sunline.ltts.busi.aptran.batchtran.test;

import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch;

	 /**
	  * 基于DcnFileDataWalker实现的批量交易例子。
	  *
	  */

public class ap92DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.aplt.batchtran.intf.Ap92.Input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap92.Property, IoApBatch.ApDcnForPldlmx> {

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
	public void process(String jobId, int index, IoApBatch.ApDcnForPldlmx dataItem, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap92.Input input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap92.Property property) {
		//TODO:
	}
	
	/**
	 * 获取数据遍历器。
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<IoApBatch.ApDcnForPldlmx> getBatchDataWalker(cn.sunline.ltts.busi.aplt.batchtran.intf.Ap92.Input input, cn.sunline.ltts.busi.aplt.batchtran.intf.Ap92.Property property) {
	    // 遍历文件数据按DCN编号拆分
//	    return new DcnFileBatchDataWalker<IoApBatch.ApDcnForPldlmx>(
//        /* 文件名 */
//        "xxx.txt",
//        /* 字符集 */
//        "GB18030",
//        /* 多少笔进行一次GNS批量查询，提高查询性能 */
//        100,
//        /* 数据构造器（先对行数据进行解析，然后进行GNS批量查询）*/
//        new DataBuilderWithLineParseAndDcnBatchQuery<IoApBatch.ApDcnForPldlmx>() {
//            private List<ApDcnForPldlmx> parseLines(int start, List<String> lines) {
//                List<ApDcnForPldlmx> ret = new ArrayList<ApDcnForPldlmx>();
//                // TODO 行解析
//                return ret;
//            }
//            
//            @Override
//            public List<ApDcnForPldlmx> build(int start, List<String> lines) {
//                // 行数据解析
//                List<ApDcnForPldlmx> items = parseLines(start, lines);
//                
//                // 获取账号列表
//                List<String> accountNoList = new ArrayList<String>();
//                for (ApDcnForPldlmx item : items) {
//                    accountNoList.add(item.getCustac());
//                }
//                
//                // GNS批量查询
////                try {
//                    //List<String> dcnNos = DMBUtil.getIntanse().findDcnNoByAccountNo(accountNoList);
//                    List<String> dcnNos = DMBUtil.getIntanse().findAllDcnNosWithAdmin();
//                    int index = 0;
//                    for (String dcnNo : dcnNos) {
//                        // 获取对应的数据项并设置DCN编号
//                        items.get(index).setDcnnum(dcnNo);
//                        index++;
//                    } // for
////                } catch (GNSAccessException e) {
////                    throw new RuntimeException("GNS批量查询失败", e);
////                }
//                
//                return items;
//            }
//        });
		return null;
	}

}


