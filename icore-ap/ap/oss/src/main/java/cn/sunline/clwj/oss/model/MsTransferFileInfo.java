package cn.sunline.clwj.oss.model;

import org.springframework.stereotype.Component;

@Component
public class MsTransferFileInfo {

	private MsFileInfo localFile;
	private MsFileInfo remoteFile;

	
	public MsTransferFileInfo() {
	}

	public MsTransferFileInfo(MsFileInfo lf, MsFileInfo rf) {
		localFile = lf;
		remoteFile = rf;
	}

	public MsFileInfo getLocalFile() {
		return localFile;
	}

	public void setLocalFile(MsFileInfo localFile) {
		this.localFile = localFile;
	}

	public MsFileInfo getRemoteFile() {
		return remoteFile;
	}

	public void setRemoteFile(MsFileInfo remoteFile) {
		this.remoteFile = remoteFile;
	}

	@Override
	public String toString() {
		return "MsTransferFileInfo [localFile=" + localFile + ", remoteFile=" + remoteFile + "]";
	}

}
