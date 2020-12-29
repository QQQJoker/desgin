package cn.sunline.clwj.oss.impl;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.clwj.oss.impl.inner.NfsFileTransferClientImpl;
import cn.sunline.clwj.oss.model.MsFileInfo;
import cn.sunline.clwj.oss.model.MsTransferFileInfo;
import cn.sunline.clwj.oss.spi.MsTransfer;

/**
 * 
 * <pre>
 * <p>标题:MsTransferNfsImpl</p>
 * <p>描述:   NSF 实现 </p>
 * 作者 lizhs
 * 创建日期   2019年4月15日
 * 修改记录： 
 * 1、将老的版本实现迁移过来
 * </pre>
 */
public class MsTransferNfsImpl implements MsTransfer {
 
	NfsFileTransferClientImpl innerImpl = new NfsFileTransferClientImpl();

	@Override
	public void init(String configId) {
		innerImpl.init(configId);
	}

	@Override
	public void upload(MsTransferFileInfo upFile) {
		innerImpl.upload(upFile.getLocalFile().getFileFullName(), upFile.getRemoteFile().getFileFullName(), upFile.getLocalFile().isUpdateOk());
	}

	@Override
	public void download(MsTransferFileInfo downFile) {
		innerImpl.download(downFile.getLocalFile().getFileFullName(), downFile.getRemoteFile().getFileFullName(), downFile.getRemoteFile().isUpdateOk());
	}

	@Override
	public void delete(boolean isLocal, MsFileInfo delFile) {
		if(!isLocal)
			innerImpl.delete(delFile.getFileFullName());
		else {
			innerImpl.deleteLoal(delFile.getFileFullName());
		}
	}


	@Override
	public List<MsFileInfo> listAllFiles(boolean isLocal, String filePath) {
		List<MsFileInfo> ret=new ArrayList<>();
		
		List<String> filenames=null;
		if(isLocal)  
			filenames=innerImpl.getLocalFileList(filePath);
		 else
			filenames=innerImpl.getRemoteFileList(filePath);
		
		for(String filename:filenames) {
			MsFileInfo  file=new MsFileInfo(filePath,filename);
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
