package cn.sunline.clwj.oss.spi;

import java.util.List;

import cn.sunline.clwj.oss.model.MsFileInfo;
import cn.sunline.clwj.oss.model.MsTransferFileInfo;

public interface MsTransfer {

	/**
	 * 初始化
	 * 
	 * @param protocolId
	 *            协议ID
	 * @param fileInfo
	 *            文件信息
	 */
	public void init(String configId);

	/**
	 * 上传文件
	 * @param upFile-文件传输对象（包括本地和远程）
	 */
	public void upload(MsTransferFileInfo upFile);
	/**
	 * 上传文件
	 * @param upFile-文件传输对象（包括本地和远程）
	 * @param okfile-是否同步生成OK文件并上传
	 */
	public void upload(MsTransferFileInfo upFile, boolean okfile);
	/**
	 * 下载文件
	 * @param downFile-文件传输对象（包括本地和远程）
	 */
	public void download(MsTransferFileInfo downFile);
	/**
	 * 下载文件
	 * @param downFile-文件传输对象（包括本地和远程）
	 * @param okfile-是否同步下载OK文件
	 */
	public void download(MsTransferFileInfo downFile, boolean okfile);
	/**
	 * 删除文件
	 * @param isLocal-是否本地
	 * @param delFile-被删除的文件对象
	 */
	public void delete(boolean isLocal, MsFileInfo delFile);

	/**
	 * 列出指定路径的文件列表
	 * @param isLocal-是否本地
	 * @param filePath-指定路径
	 * @return
	 */
	public List<MsFileInfo> listAllFiles(boolean isLocal, String filePath);

	/**
	 * 获得本地默认路径（即：对应实现的配置）
	 * @return
	 */
	public String getLocalkPath();

	/**
	 * 获得远程默认路径（即：对应实现的配置）
	 * @return
	 */
	public String getRemotePath();
}
