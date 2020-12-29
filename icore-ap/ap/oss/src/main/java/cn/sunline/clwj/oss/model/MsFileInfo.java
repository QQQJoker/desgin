package cn.sunline.clwj.oss.model;

import java.io.File;
import java.util.Date;

import org.springframework.stereotype.Component;

import cn.sunline.clwj.oss.util.MsStringUtil;

@Component
public class MsFileInfo {

	private String filePath = ""; // 文件路径
	private String fileName; //文件名
	private String fileFullName = ""; //文件全名（包括路径）
	

	private boolean checkmd5 = false; //是否生成md5
	private String md5; //md5值
	private boolean updateOk = false; //是否生成.ok文件
	private String okFlag = ".ok"; //.ok文件格式
	private String okFile; //.ok文件全名

	private long size; // 文件大小
	private Date lastModified; // 文件最后修改时间
	private String storageClass; // 文件存储的对应类FullClassName
    
	public MsFileInfo() {

	}

	public MsFileInfo(String fpath, String fileName) {

		if (!MsStringUtil.isEmpty(fpath)) {
			filePath = fpath;
		}
		if (!MsStringUtil.isEmpty(fileName)) {
			this.fileName = fileName;
		}

		concatToFullName(); // 按路径和文件名构建对象时，自动拼全文件名
	}

	public MsFileInfo(String fFullName) {
		if (!MsStringUtil.isEmpty(fFullName)) {
			fileFullName = fFullName;
		}

		splitToShortName(); // 按全文件名构建对象时，自动拆分路径和文件名
	}

	public void concatToFullName() {

		if (MsStringUtil.isEmpty(filePath)) {
			fileFullName = fileName;
		} else if (filePath.endsWith(File.separator)) {
			fileFullName = filePath.concat(fileName);
		} else {
			fileFullName = filePath.concat(File.separator).concat(fileName);
		}
	}

	public void splitToShortName() {
		// 以最后一个文件分隔符为分界，前半部分为目录；后半部分为文件名
		if (!MsStringUtil.isEmpty(fileFullName)) {
			int index = fileFullName.lastIndexOf(File.separatorChar);
			if (index < 0) {
				filePath = "";
				fileName = fileFullName;
			} else {
				filePath = fileFullName.substring(0, index + 1);
				fileName = fileFullName.substring(index + 1);
			}
		}
	}
	
	public long countSize() {
		// TODO
		
		return size;
	}
	
	public String createMd5() {
		// TODO
		
		return md5;
	}

	public String createOk() {
		// TODO
		
		return okFile;
	}

	// ---------------------getter & setter-----------------------
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileFullName() {
		return fileFullName;
	}

	public void setFileFullName(String fileFullName) {
		this.fileFullName = fileFullName;
	}

	public boolean isCheckmd5() {
		return checkmd5;
	}

	public void setCheckmd5(boolean checkmd5) {
		this.checkmd5 = checkmd5;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public boolean isUpdateOk() {
		return updateOk;
	}

	public void setUpdateOk(boolean updateOk) {
		this.updateOk = updateOk;
	}

	public String getOkFlag() {
		return okFlag;
	}

	public void setOkFlag(String okFlag) {
		this.okFlag = okFlag;
	}

	public String getOkFile() {
		return okFile;
	}

	public void setOkFile(String okFile) {
		this.okFile = okFile;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getStorageClass() {
		return storageClass;
	}

	public void setStorageClass(String storageClass) {
		this.storageClass = storageClass;
	}

	@Override
	public String toString() {
		return "MsFileInfo [filePath=" + filePath + ", fileNmae=" + fileName + ", fileFullName=" + fileFullName
				+ ", checkmd5=" + checkmd5 + ", md5=" + md5 + ", updateOk=" + updateOk + ", okFlag=" + okFlag
				+ ", okFile=" + okFile + ", size=" + size + ", lastModified=" + lastModified + ", storageClass="
				+ storageClass + "]";
	}
	
}
