package cn.sunline.ltts.busi.aplt.impl;

import java.util.HashMap;
import java.util.Map;

import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.FileUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataTransfer;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.component.AbstractComponent;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.component.BaseComp;

public class ApBatchFileTransferImpl implements BatchDataTransfer {
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApBatchFileTransferImpl.class);
	
	/**
     * 
     * @Author Dell
     *         <p>
     *         <li>2016年1月26日-下午2:20:39</li>
     *         <li>功能说明：批量文件上传</li>
     *         </p>
     * @param path 本地文件绝对路径（不含文件名）
     * @param dataFileName 文件名
     * @return 
     */
	@Override
	public Map<String, Object> upload(String path, String dataFileName) {
		Map<String, Object> properties = new HashMap<String, Object>();
//		getFileTransfer().setLocalDirectory(getFileTransfer().getBatchLocalDir());
//		getFileTransfer().remoteDirectory(getFileTransfer().getBatchRemoteDir());
		String relativeFile = getRelativePath(getFileTransfer().workDirectory(), path) + dataFileName;
		properties.put(FileUtil.Remote_File_Name, relativeFile);
		getFileTransfer().upload(relativeFile,relativeFile);
		return properties;
		
	}
    
	/**
     * 
     * @Author Dell
     *         <p>
     *         <li>2016年1月26日-下午2:20:39</li>
     *         <li>功能说明：批量文件下载</li>
     *         </p>
     * @param localPath 本地绝对路径（不含文件名）
     * @param dataFileName 文件名
     * @param properties
     * @return 
     */
	@Override
	public Map<String, Object> download(String localPath, String dataFileName,
			Map<String, Object> properties) {
		String relativeFile = getRelativePath(getFileTransfer().workDirectory(), localPath) + dataFileName;
		getFileTransfer().download(relativeFile,relativeFile);
		return properties;
	}
    
	/**
     * 
     * @Author Dell
     *         <p>
     *         <li>2016年1月26日-下午2:20:39</li>
     *         <li>功能说明：获取批量文件本地工作目录</li>
     *         </p>
     * @return 批量文件本地工作目录
     */
	@Override
	public String getWorkDir() {
		return getFileTransfer().workDirectory();
	}
    
	/**
     * 
     * @Author Dell
     *         <p>
     *         <li>2016年1月26日-下午2:20:39</li>
     *         <li>功能说明：获取相对路径</li>
     *         </p>
     * @param workDir 本地工作目录
     * @param path  路径
     * @return 相对路径
     */
	public static String getRelativePath(String workDir, String path) {
		if (StringUtil.isEmpty(workDir)) return path;
		if (path == null) return path;
		if (!path.startsWith(workDir)) 
			return path;
		
		return path.substring(workDir.length());
	}
	
	/**
	 * 
	 * @Author Dell
	 *         <p>
	 *         <li>2016年1月26日-下午2:22:28</li>
	 *         <li>功能说明：获取文件传输组件实现</li>
	 *         </p>
	 * @return 文件传输组件实现
	 */
	public static BaseComp.FileTransfer getFileTransfer() {
		bizlog.method("getFileTransfer begin >>>>>>>>>>>>>>>>>>>>");
		BaseComp.FileTransfer abFT = SysUtil.getInstance(
				BaseComp.FileTransfer.class, AbstractComponent.FileTransfer);
		bizlog.parm("abFT [%s]", abFT);
		bizlog.method("getFileTransfer end >>>>>>>>>>>>>>>>>>>>");
		return abFT;
	}
}
