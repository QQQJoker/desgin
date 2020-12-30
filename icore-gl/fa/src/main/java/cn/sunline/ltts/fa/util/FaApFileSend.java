package cn.sunline.ltts.fa.util;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApBatchSend;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.Apb_batch_sendDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.apb_batch_receive;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.apb_batch_send;
import cn.sunline.ltts.busi.sys.errors.ApPubErr.APPUB;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaApFileSend {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaApFileSend.class);

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月14日-下午7:23:59</li>
	 *         <li>功能说明：生成一个文件Id</li>
	 *         </p>
	 * @return fileId
	 */
	public static String genFileId() {

		return ApSeq.genSeq("FILE_ID");
	}

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月14日-下午7:23:59</li>
	 *         <li>功能说明：登记文件发送簿</li>
	 *         </p>
	 * @param fileName
	 * @param remoteDirCode
	 * @param localDirCode
	 * @param appendOkInd
	 * @return
	 */
	public static String register(String fileName, String remoteDirCode, String localDirCode, E_YESORNO appendOkInd) {

		String fileId = ApSeq.genSeq("FILE_ID");

		register(fileId, fileName, remoteDirCode, localDirCode, appendOkInd);

		return fileId;
	}

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月14日-下午7:23:59</li>
	 *         <li>功能说明：登记文件发送簿(已预先产生文件ID)</li>
	 *         </p>
	 * @param fileId
	 * @param fileName
	 * @param remoteDirCode
	 * @param localDirCode
	 * @param appendOkInd
	 * @return
	 */
	public static void register(String fileId, String fileName, String remoteDirCode, String localDirCode, E_YESORNO appendOkInd) {

		apb_batch_send batchSend = SysUtil.getInstance(apb_batch_send.class);

		String localPath = FaApFile.getFullPath(localDirCode);
		String serverPath = FaApFile.getFullPath(remoteDirCode);

		batchSend.setFile_id(fileId); // 文件ID
		batchSend.setFile_name(fileName); // 文件名称
		batchSend.setFile_server_path(serverPath); // 服务器路径
		batchSend.setSend_ind(E_YESORNO.NO); // 文件收妥标志
		batchSend.setFile_local_path(localPath); // 本地路径
		batchSend.setAppend_ok_ind(appendOkInd); // 是否追加Ok文件
		batchSend.setHash_value(CommTools.getGroupHashValue("SEND_HASH_VALUE", fileId)); // 散列值
		batchSend.setSend_count(0L);

		// 登记文件发送登记薄
		Apb_batch_sendDao.insert(batchSend);
	}

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月4日-上午9:44:02</li>
	 *         <li>功能说明：更新文件发送簿</li>
	 *         </p>
	 * @param fileId
	 * @param sendInd
	 */
	public static void modify(String fileId, E_YESORNO sendInd) {
		bizlog.method("modifySend begin >>>>>>>>>>>>>>>>");

		// 非空字段检查
		CommTools.fieldNotNull(fileId, GlDict.A.file_id.getId(), GlDict.A.file_id.getDescription());

		// 更新文件收妥标志 、文件接收次数
		apb_batch_send batchSend = Apb_batch_sendDao.selectOneWithLock_odb1(fileId, true);

		batchSend.setSend_ind(sendInd);
		batchSend.setSend_count(batchSend.getSend_count() + 1); // 文件发送次数

		// 更新文件发送登记薄
		Apb_batch_sendDao.updateOne_odb1(batchSend);

		bizlog.method("modifySend end <<<<<<<<<<<<<<<<<<<<");

	}

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月15日-下午5:23:49</li>
	 *         <li>功能说明：获取文件发送薄信息</li>
	 *         </p>
	 * @param fileId
	 * @return ApBatchSend
	 */
	public static ApBatchSend getFileInfo(String fileId) {

		// 非空字段检查
		CommTools.fieldNotNull(fileId, GlDict.A.file_id.getId(), GlDict.A.file_id.getDescription());

		apb_batch_send batchSend = Apb_batch_sendDao.selectOne_odb1(fileId, false);

		if (batchSend == null)
			throw APPUB.E0005(OdbFactory.getTable(apb_batch_receive.class).getLongname(), GlDict.A.file_id.getLongName(), fileId);

		ApBatchSend sendInfo = SysUtil.getInstance(ApBatchSend.class);

		sendInfo.setFile_local_path(batchSend.getFile_local_path());
		sendInfo.setFile_name(batchSend.getFile_name());
		sendInfo.setFile_server_path(batchSend.getFile_server_path());
		sendInfo.setHash_value(batchSend.getHash_value());
		sendInfo.setSend_ind(batchSend.getSend_ind());
		sendInfo.setSend_count(batchSend.getSend_count());
		sendInfo.setAppend_ok_ind(batchSend.getAppend_ok_ind());

		return sendInfo;

	}

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月15日-下午5:23:49</li>
	 *         <li>功能说明：文件上传处理</li>
	 *         </p>
	 * @param fileName
	 * @param localPath
	 * @param serverPath
	 * @param appendOkInd
	 * @return boolean
	 */
	public static boolean upload(String fileName, String localPath, String serverPath, E_YESORNO appendOkInd) {

		// 获取本地文件路径
		String localFileName = FaApFile.getFileFullPath(localPath, fileName);

		// 获取服务器文件路径
		String remoteFileName = FaApFile.getFileFullPath(serverPath, fileName);

		boolean sucess = true;

		try {
			if (appendOkInd == E_YESORNO.YES) {
				// 上传文件，追加.ok 文件
				FaApFile.upload(localFileName, remoteFileName, true);
			}
			else {
				// 上传文件
				FaApFile.upload(localFileName, remoteFileName);
			}
		}
		catch (Exception e) {

			sucess = false;
			bizlog.error("File Upload fail,Exception :[%s] ", e);
		}

		return sucess;
	}

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月14日-下午7:23:59</li>
	 *         <li>功能说明：联机发送文件</li>
	 *         </p>
	 * @param fileName
	 * @param remoteDirCode
	 * @param localDirCode
	 * @param appendOkInd
	 * @return
	 */
	public static String sendProcess(String fileName, String remoteDirCode, String localDirCode, E_YESORNO appendOkInd) {

		String fileId = ApSeq.genSeq("FILE_ID");

		sendProcess(fileId, fileName, remoteDirCode, localDirCode, appendOkInd);

		return fileId;
	}

	/**
	 * @Author yangdl
	 *         <p>
	 *         <li>2017年3月14日-下午7:23:59</li>
	 *         <li>功能说明：联机发送文件(已预先产生文件ID)</li>
	 *         </p>
	 * @param fileId
	 * @param fileName
	 * @param remoteDirCode
	 * @param localDirCode
	 * @param appendOkInd
	 * @return
	 */
	public static void sendProcess(String fileId, String fileName, String remoteDirCode, String localDirCode, E_YESORNO appendOkInd) {

		register(fileId, fileName, remoteDirCode, localDirCode, appendOkInd);

		apb_batch_send fileInfo = Apb_batch_sendDao.selectOne_odb1(fileId, true);

		if (upload(fileName, fileInfo.getFile_local_path(), fileInfo.getFile_server_path(), appendOkInd)) {

			FaApFileSend.modify(fileId, E_YESORNO.YES);
		}
		else {
			throw GlError.GL.E0045(fileId, fileName);
		}
	}
}
