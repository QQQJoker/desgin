package cn.sunline.clwj.oss.impl;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.oss.impl.inner.SftpFileTransferClientImpl;
import cn.sunline.clwj.oss.model.MsFileInfo;
import cn.sunline.clwj.oss.model.MsTransferFileInfo;
import cn.sunline.clwj.oss.spi.MsTransfer;

public class MsTransferSftpImpl implements MsTransfer {
	SftpFileTransferClientImpl innerImpl = new SftpFileTransferClientImpl();

	@Override
	public void init(String configId) {
		innerImpl.init(configId);
	}

	@Override
	public void upload(MsTransferFileInfo upFile) {
		innerImpl.upload(upFile.getLocalFile().getFileFullName(), upFile.getRemoteFile().getFileFullName(),
				upFile.getLocalFile().isUpdateOk());
	}

	@Override
	public void download(MsTransferFileInfo downFile) {
		innerImpl.download(downFile.getLocalFile().getFileFullName(), downFile.getRemoteFile().getFileFullName(),
				downFile.getRemoteFile().isUpdateOk());
	}

	@Override
	public void delete(boolean isLocal, MsFileInfo delFile) {
		if (!isLocal)
			innerImpl.delete(delFile.getFileFullName());
		else {
			innerImpl.deleteLoal(delFile.getFileFullName());
		}
	}

	@Override
	public List<MsFileInfo> listAllFiles(boolean isLocal, String filePath) {
		List<MsFileInfo> ret = new ArrayList<>();

		List<String> filenames = null;
		if (isLocal)
			filenames = innerImpl.getLocalFileList(filePath);
		else
			filenames = innerImpl.getRemoteFileList(filePath);

		for (String filename : filenames) {
			MsFileInfo file = new MsFileInfo(filePath, filename);
			ret.add(file);
		}
		return ret; 
	}

	@Override
	public String getLocalkPath() {
		return innerImpl.getLocalHome();
	}

	@Override
	public String getRemotePath() {
		return innerImpl.getRemoteHome();
	}

	@Override
	public void upload(MsTransferFileInfo upFile, boolean okfile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void download(MsTransferFileInfo downFile, boolean okfile) {
		// TODO Auto-generated method stub
		
	}

}
