package cn.sunline.ltts.fa.util;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApBatchRecv;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.Apb_batch_receiveDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.apb_batch_receive;
import cn.sunline.ltts.busi.sys.errors.ApPubErr.APPUB;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.sys.dict.GlDict;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author yangdl
 *         <p>
 *         <li>2017年3月3日-下午5:07:56</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年3月3日-yangdl：文件接收相关</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */

public class FaApFileRecv {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaApFileRecv.class);

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月4日-上午9:44:02</li>
	 *         <li>功能说明：登记文件接收簿</li>
	 *         </p>
	 * @param fileName
	 * @param serverPath
	 * @param dirCode
	 */
	public static String register(String fileName, String serverPath, String localDirCode) {

		apb_batch_receive batchRecv = SysUtil.getInstance(apb_batch_receive.class);

		String fileId = ApSeq.genSeq("FILE_ID");
		String localPath = FaApFile.getFullPath(localDirCode);
		//String serverPath = ApFile.getFullPath(remoteDirCode);

		batchRecv.setFile_id(fileId); // 文件ID
		batchRecv.setFile_name(fileName); // 文件名称
		batchRecv.setFile_server_path(serverPath); // 服务器路径
		batchRecv.setReceive_ind(E_YESORNO.NO); // 文件收妥标志
		batchRecv.setFile_local_path(localPath); // 本地路径
		batchRecv.setHash_value(CommTools.getGroupHashValue("RECEIVE_HASH_VALUE", fileId)); // 散列值
		batchRecv.setRecevice_count(0L);
		batchRecv.setRecdver(1l);

		// 登记文件批量接收登记薄
		Apb_batch_receiveDao.insert(batchRecv);

		return fileId;
	}

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月4日-上午9:44:02</li>
	 *         <li>功能说明：更新文件接收簿</li>
	 *         </p>
	 * @param fileId
	 * @param receiveInd
	 */
	public static void modify(String fileId, E_YESORNO receiveInd) {

		bizlog.method(" modifyRecv begin >>>>>>>>>>>>>>>>");

		// 非空字段检查
		CommTools.fieldNotNull(fileId, GlDict.A.file_id.getId(), GlDict.A.file_id.getDescription());

		// 更新文件收妥标志 、文件接收次数
		apb_batch_receive batcRecv = Apb_batch_receiveDao.selectOneWithLock_odb1(fileId, true);

		batcRecv.setReceive_ind(receiveInd);
		batcRecv.setRecevice_count(batcRecv.getRecevice_count() + 1); // 文件接收次数

		Apb_batch_receiveDao.updateOne_odb1(batcRecv);

		bizlog.method("modifyRecv end <<<<<<<<<<<<<<<<<<<<");

	}

	/**
	 * 
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月15日-下午5:23:49</li>
	 *         <li>功能说明：获取文件接收薄信息</li>
	 *         </p>
	 * @param fileId
	 * @return ApBatchRecv
	 */
	public static ApBatchRecv getFileInfo(String fileId) {

		// 非空字段检查
		CommTools.fieldNotNull(fileId, GlDict.A.file_id.getId(), GlDict.A.file_id.getDescription());

		apb_batch_receive	batchRecv =Apb_batch_receiveDao.selectOne_odb1(fileId, false);
		
		if(batchRecv == null)
			throw APPUB.E0005(OdbFactory.getTable(apb_batch_receive.class).getLongname(), GlDict.A.file_id.getLongName(), fileId);
		
		ApBatchRecv  recvInfo  =  SysUtil.getInstance(ApBatchRecv.class);
		
		recvInfo.setFile_local_path(batchRecv.getFile_local_path());
		recvInfo.setFile_name(batchRecv.getFile_name());
		recvInfo.setFile_server_path(batchRecv.getFile_server_path());
		recvInfo.setHash_value(batchRecv.getHash_value());
		recvInfo.setReceive_ind(batchRecv.getReceive_ind());
		recvInfo.setRecevice_count(batchRecv.getRecevice_count());

		return recvInfo;

	}
}
