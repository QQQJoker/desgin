package cn.sunline.ltts.busi.fatran.batchtran.merge;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApBatch.SftpInfo;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.aplt.tools.SftpTools;
import cn.sunline.ltts.busi.fa.file.FaMergeDeal;
import cn.sunline.ltts.busi.gl.fa.namedsql.FaMergeDao;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.Apb_merge_fileDao;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.Apb_merge_receiveDao;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_file;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_receive;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEPROTOCOL;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_MERGETYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaApFile;
	 /**
	  * 批量同步外系统并账文件
	  *扫描文件是否存在，存在则登记文件接收表
	  */

public class fm01DataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm01.Input, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm01.Property, cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_file> {
	  
	  private static final BizLog log = LogManager.getBizLog(fm01DataProcessor.class);
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
		public void process(String jobId, int index, cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_file dataItem, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm01.Input input, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm01.Property property) {
			
			String protocol = dataItem.getFile_tranfe_protocol();
			String trxn_date = CommTools.prcRunEnvs().getTrandt();
			
			String sys_no = dataItem.getSys_no();
			String channel_id = dataItem.getChannel_id();
			
			
			String file_server_name = FaMergeDeal.genFilename(dataItem); //服务器上的文件名称
			
			//获取真实路径地址
			String remoteDir = FaApFile.getFullPath(dataItem.getRemote_dir_code());
			String localPath = FaApFile.getFullPath(dataItem.getLocal_dir_code());
			
			// 本地无local_dir_code 目录则自动创建，创建失败需要抛出
			File dirFile = new File(localPath);
			if (!dirFile.exists()) {
				if (!dirFile.mkdirs())
					throw GlError.GL.E0104(localPath);
			}
			
			
			if(CommUtil.equals(protocol, E_FILEPROTOCOL.SFTP.getValue())){ //sftp文件传输
				SftpInfo sftpInfo = CommTools.getInstance(SftpInfo.class);
				sftpInfo.setRmaddr(dataItem.getFile_server_ip());
				sftpInfo.setRmname(dataItem.getFile_server_userna());
				sftpInfo.setRmpass(dataItem.getFile_server_passwd());
				sftpInfo.setRmport(ConvertUtil.toInteger(dataItem.getFile_server_port()));
				sftpInfo.setRmfile(file_server_name);
				sftpInfo.setRmpath(remoteDir);
				
				boolean isExist = SftpTools.isRemoteExist(sftpInfo); //检查远程文件是否存在
				if(isExist){
					String busi_batch_code = ApSeq.genSeq("BUSI_BATCH_CODE");
					List<apb_merge_file> lstFile = Apb_merge_fileDao.selectAll_odb1(sys_no, channel_id, true);
					for(apb_merge_file merge : lstFile){
						apb_merge_receive tblMergeReceive = CommTools.getInstance(apb_merge_receive.class);
						tblMergeReceive.setBusi_batch_code(busi_batch_code);
						tblMergeReceive.setChannel_id(merge.getChannel_id());
						tblMergeReceive.setTrxn_date(trxn_date);
						tblMergeReceive.setFile_handling_status(E_FILEDEALSTATUS.UNCHECK);
						tblMergeReceive.setFile_local_name(FaMergeDeal.genFilename(merge)); //本地文件名
						tblMergeReceive.setFile_local_path(localPath);
						tblMergeReceive.setFile_merge_type(merge.getFile_merge_type());
						tblMergeReceive.setFile_server_name(FaMergeDeal.genFilename(merge));
						tblMergeReceive.setFile_server_path(remoteDir);
						tblMergeReceive.setFilebody_total_amt(BigDecimal.ZERO);
						tblMergeReceive.setFilebody_total_count(0L);
						tblMergeReceive.setHead_total_amt(BigDecimal.ZERO);
						tblMergeReceive.setHead_total_count(0L);
						tblMergeReceive.setSuccess_total_amt(BigDecimal.ZERO);
						tblMergeReceive.setSuccess_total_count(0L);
						tblMergeReceive.setSys_no(merge.getSys_no());
						tblMergeReceive.setError_text("");
						
						Apb_merge_receiveDao.insert(tblMergeReceive);
					}
				} else {
					log.debug("并账文件路径：[%s],标志文件：[%s]不存在", remoteDir, file_server_name);
					throw GlError.GL.E0216(file_server_name);
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
		public BatchDataWalker<cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_file> getBatchDataWalker(cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm01.Input input, cn.sunline.ltts.busi.fatran.batchtran.merge.intf.Fm01.Property property) {
			
			String trxn_date = CommTools.prcRunEnvs().getTrandt();
			Params param = new Params();
			param.put("file_merge_type", E_MERGETYPE.OKFILE);
			param.put("file_is_merge", E_YESORNO.YES);
			param.put("trxn_date", trxn_date);
			return new CursorBatchDataWalker<apb_merge_file>(FaMergeDao.namedsql_lstMergeOkfileList, param);
		}

}


