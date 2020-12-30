package cn.sunline.ltts.busi.fatran.batchtran.merge;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch.SftpInfo;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.SftpTools;
import cn.sunline.ltts.busi.fa.file.FaMergeDeal;
import cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm02.Input;
import cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm02.Property;
import cn.sunline.ltts.busi.gl.fa.namedsql.FaMergeDao;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.Apb_merge_fileDao;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.Apb_merge_receiveDao;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_file;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEPROTOCOL;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_MERGETYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
	 /**
	  * 下载并账文件入库并解析
	  *
	  */

public class fm02DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm02.Input, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm02.Property, cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_receive> {
	  
	  @Override
		public void beforeTranProcess(String taskId, Input input, Property property) {
			super.beforeTranProcess(taskId, input, property);
			
			String trxn_date = CommTools.prcRunEnvs().getTrandt();
			//校验文件接收登记簿中，当日的记录数与文件定义表中记录数是否一致
			int iCount = FaMergeDao.cntMergeReceiveCount(trxn_date, false);
			int cCount = FaMergeDao.cntMergeFileCount(E_YESORNO.YES, false);
			if(CommUtil.compare(iCount, cCount) != 0){
				throw GlError.GL.E0217();
			}
			
			property.setTrxn_date(trxn_date);
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_receive dataItem, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm02.Input input, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm02.Property property) {

			apb_merge_file fileInfo = Apb_merge_fileDao.selectOne_odb2(dataItem.getSys_no(), dataItem.getChannel_id(), dataItem.getFile_merge_type(), true);
			
			if(CommUtil.equals(fileInfo.getFile_tranfe_protocol(), E_FILEPROTOCOL.SFTP.getValue())){
				//调用文件传输组建下载文件
				SftpInfo sftpInfo = CommTools.getInstance(SftpInfo.class);
				
				sftpInfo.setRmaddr(fileInfo.getFile_server_ip());
				sftpInfo.setRmport(ConvertUtil.toInteger(fileInfo.getFile_server_port()));
				sftpInfo.setRmname(fileInfo.getFile_server_userna());
				sftpInfo.setRmpass(fileInfo.getFile_server_passwd());
				sftpInfo.setRmpath(dataItem.getFile_server_path());
				sftpInfo.setRmfile(dataItem.getFile_server_name());
				sftpInfo.setLcpath(dataItem.getFile_local_path());
				sftpInfo.setLcfile(dataItem.getFile_local_name());
				
				SftpTools.download(sftpInfo);
				
				if(dataItem.getFile_merge_type() == E_MERGETYPE.OCCUR){
					FaMergeDeal.alyMergeFile(dataItem); //解析并账文件，进行借贷平衡检查并入库
				}else if(dataItem.getFile_merge_type() == E_MERGETYPE.OKFILE){
					dataItem.setFile_handling_status(E_FILEDEALSTATUS.CHECKED);
				}
				
				Apb_merge_receiveDao.updateOne_odb1(dataItem);
			}
		}
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_receive> getBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm02.Input input, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm02.Property property) {
			
			Params params = new Params();
			params.put("succ_stat", E_FILEDEALSTATUS.SUCCESS);
			params.put("chck_stat", E_FILEDEALSTATUS.CHECKED);
			
			return new CursorBatchDataWalker<>(FaMergeDao.namedsql_lstMergeToDownload, params);
		}

}


