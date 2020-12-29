package cn.sunline.ltts.busi.aplt.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataTransfer;
import cn.sunline.clwj.oss.api.OssFactory;
import cn.sunline.clwj.oss.model.MsFileInfo;
import cn.sunline.clwj.oss.model.MsTransferFileInfo;


public class MsBatchDataTransferImpl implements BatchDataTransfer {

	private static final BizLog log = BizLogUtil.getBizLog(MsBatchDataTransferImpl.class);

	@Override
	public Map<String, Object> upload(String path, String dataFileName) {

		MsTransferFileInfo upFile = new MsTransferFileInfo();
		upFile.setLocalFile(new MsFileInfo(path, dataFileName));

		// 远程目录
		String remotePath = OssFactory.get().create().getRemotePath();

		upFile.setRemoteFile(new MsFileInfo(remotePath, dataFileName));
		OssFactory.get().create().upload(upFile);

		if (log.isDebugEnabled()) {
			log.debug("upload finished:" + upFile);
		}

		Map<String, Object> ret = new HashMap<String, Object>();
		// 必须把远程文件名返回
		ret.put(FileUtil.Remote_File_Name, upFile.getRemoteFile().getFileFullName());
		return ret;

	}

	@Override
	public Map<String, Object> download(String localPath, String dataFileName, Map<String, Object> properties) {

		MsTransferFileInfo downFile = new MsTransferFileInfo();
		if (CommUtil.isNull(localPath)) {
			localPath = getWorkDir();
		}
		downFile.setLocalFile(new MsFileInfo(localPath, dataFileName));
		downFile.setRemoteFile(new MsFileInfo((String) properties.get(FileUtil.Remote_File_Name)));
		OssFactory.get().create().download(downFile);

		return Collections.emptyMap();
	}

	@Override
	public String getWorkDir() {

		return OssFactory.get().create().getLocalkPath();
	}

}
