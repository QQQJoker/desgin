
package cn.sunline.ltts.busi.fatran.batchtran.lnaccountingevent;
import cn.sunline.edsp.base.lang.*;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo;
import cn.sunline.ltts.busi.fa.namedsql.FaFileDao;
import cn.sunline.ltts.busi.fa.scene.accounting.FaSceneFile;
import cn.sunline.ltts.busi.fa.type.ComFaFile.FaFileDown;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_BATCHTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessorWithJobDataItem;
	 /**
	  * 读取及处理场景事件流水文件
	  * @author 
	  * @Date 
	  */

public class fat5DataProcessor extends
  AbstractBatchDataProcessorWithJobDataItem<cn.sunline.ltts.busi.fatran.batchtran.intf.Fat5.Input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat5.Property, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo, cn.sunline.ltts.busi.fa.type.ComFaFile.FaFileDown> {
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.fa.type.ComFaFile.FaFileDown dataItem, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat5.Input input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat5.Property property) {
			
			// 解析文件
			FaSceneFile.doAccountingSceneEventFile(dataItem);
			
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo> getBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.intf.Fat5.Input input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat5.Property property) {
			
			Params parm = new Params();
			parm.add("org_id", CommToolsAplt.prcRunEnvs().getCorpno());
			parm.add("busi_batch_type", E_BATCHTYPE.SCENE_EVENT_DOWN);
			parm.add("receive_ind", E_YESORNO.YES);
			parm.add("file_handling_status", E_FILEDEALSTATUS.UNCHECK);
			return new CursorBatchDataWalker<ApDataGroupNo>(FaFileDao.namedsql_lstFileHashInfo, parm);
			
		}
		
		/**
		 * 获取作业数据遍历器
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @param dataItem 批次数据项
		 * @return
		 */
		public BatchDataWalker<cn.sunline.ltts.busi.fa.type.ComFaFile.FaFileDown> getJobBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.intf.Fat5.Input input, cn.sunline.ltts.busi.fatran.batchtran.intf.Fat5.Property property, cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApDataGroupNo dataItem) {
			
			Params parm = new Params();
			parm.add("org_id", CommToolsAplt.prcRunEnvs().getCorpno());
			parm.add("busi_batch_type", E_BATCHTYPE.SCENE_EVENT_DOWN);
			parm.add("file_handling_status", E_FILEDEALSTATUS.UNCHECK);
			parm.add("receive_ind", E_YESORNO.YES);
			parm.add("hash_value", dataItem.getHash_value());
			return new CursorBatchDataWalker<FaFileDown>(FaFileDao.namedsql_lstFileDownInfo, parm);
			
		}
	  

}


