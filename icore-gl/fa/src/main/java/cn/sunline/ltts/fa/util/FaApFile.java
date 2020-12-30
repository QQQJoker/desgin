package cn.sunline.ltts.fa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.clwj.oss.api.OssFactory;
import cn.sunline.clwj.oss.model.MsFileInfo;
import cn.sunline.clwj.oss.spi.MsTransfer;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.util.file.FileUtil;
import cn.sunline.ltts.busi.aplt.component.AbstractComponent;
import cn.sunline.ltts.busi.aplt.component.FaBaseComp;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApFileIn;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApRemoteFileList;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.Apb_batch_receiveDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.App_batchDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.App_directoryDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.apb_batch_receive;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.app_batch;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.app_directory;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DIRPARMTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaApFile {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaApFile.class);

	private static final String FILE_OK = ".ok";
	
	private static final String FILE_TXT = ".txt";
	
	private static final int DEFAULT_BUFFER_SIZE = 2048;

	private static boolean exists(String dirCode, boolean flag) {

		//ApDropList.exists(ApConstants.DIR_CODE, dirCode, flag);

		List<app_directory> dirInfo = App_directoryDao.selectAll_odb1(dirCode, false);
		if (dirInfo.size() <= 0 && flag) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(app_directory.class).getLongname(), GlDict.A.dir_code.getLongName(), dirCode);
		}
		return dirInfo.size() <= 0 ? false : true;
	}

	/**
	 * <p>
	 * <li>2016年1月6日-上午10:44:55</li>
	 * <li>功能说明：读文件</li>
	 * </p>
	 * 
	 * @param file
	 * @return 读取文件列表
	 */
	public static List<String> readFile(File file) {
		final List<String> lines = new ArrayList<String>();
		FileUtil.readFile(file.getAbsolutePath(), new FileDataExecutor() {

			@Override
			public void process(int index, String line) {
				lines.add(line);
			}

		});
		return lines;
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年2月9日-下午4:59:27</li>
	 *         <li>功能说明：根据目录编码获取目录路径</li>
	 *         </p>
	 * @param dirCode
	 * @return 目录路径
	 */
	public static String getFullPath(String dirCode) {
		return getFullPath(dirCode, "");
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年2月9日-下午4:59:27</li>
	 *         <li>功能说明：根据目录编码、文件名获取带路径的文件名</li>
	 *         </p>
	 * @param dirCode
	 *            目录编码
	 * @param fileName
	 *            文件名
	 * @return 含路径的文件名
	 */
	public static String getFullPath(String dirCode, String fileName) {

		bizlog.method("getFileNameById begin >>>>>>>>>>>>>>>>>>>>");

		// 判断是否存在, 不存在抛错
		exists(dirCode, true);

		String filePath = genFileName(dirCode, fileName);

		bizlog.method("getFileNameById end >>>>>>>>>>>>>>>>>>>>");
		return filePath;
	}

	private static String genFileName(String dirCode, String fileName) {
		StringBuilder sb = new StringBuilder();
		List<app_directory> directoryList = App_directoryDao.selectAll_odb1(dirCode, false);

		for (app_directory directory : directoryList) {
			E_DIRPARMTYPE type = directory.getDir_parm_type();
			String parmValue = directory.getParm_value();

			if (type == E_DIRPARMTYPE.RUNENV) {// 公共运行变量
				sb.append(CommTools.getTrxRunEnvsValue(parmValue));
				sb.append(File.separator);
			}
			else if (type == E_DIRPARMTYPE.FIXED) {// 固定值
				sb.append(parmValue);
				sb.append(File.separator);
			}
		}
		return sb.append(fileName).toString();
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年2月9日-下午3:56:35</li>
	 *         <li>功能说明：文件下载</li>
	 *         </p>
	 * @param tranDate
	 * @param localFileName
	 * @param remoteFileName
	 * @return
	 */
	public static String download(String tranDate, String localFileName, String remoteFileName) {
		bizlog.method("download begin >>>>>>>>>>>>>>>>>>>>");
		String sysId = CommToolsAplt.prcRunEnvs().getInpucd();
		String fullPath = null;
		bizlog.parm(" sysId [%s],  tranDate  [%s],  localFileName [%s],  remoteFileName  [%s]", sysId, tranDate, localFileName, remoteFileName);

		//判断ftp开关，Y-开，否则取本地
        if("Y".equals(FaTools.getRemoteDir())){
            FaBaseComp.FileTransfer transfer = getFileTransfer();
            transfer.download(sysId, tranDate, localFileName, remoteFileName);
            fullPath = getFileFullPath(transfer.workDirectory(), localFileName);
        }else{
            copyFile(remoteFileName, localFileName);
            fullPath = localFileName;
        }
		bizlog.parm("fullPath [%s]", fullPath);
		bizlog.method("download end <<<<<<<<<<<<<<<<<<<<");
		return fullPath;
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年2月9日-下午3:56:51</li>
	 *         <li>功能说明：文件上传</li>
	 *         </p>
	 * @param localFileName
	 *            相对路径
	 * @param remoteFileName
	 *            相对路径
	 * @return
	 */
	public static void upload(String localFileName, String remoteFileName) {
		bizlog.method("upload begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("localFileName  [%s],  remoteFileName [%s]", localFileName, remoteFileName);

		upload(localFileName, remoteFileName, false);

		bizlog.method("upload end >>>>>>>>>>>>>>>>>>>>");
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年2月9日-下午3:56:51</li>
	 *         <li>功能说明：文件上传</li>
	 *         </p>
	 * @param localFileName
	 *            相对路径
	 * @param remoteFileName
	 *            相对路径
	 * @return
	 */
	public static void upload(String localFileName, String remoteFileName, boolean uploadOk) {
		bizlog.method("upload begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.parm(" tranDate [%s],  localFileName  [%s],  remoteFileName [%s]", CommToolsAplt.prcRunEnvs().getTrandt(), localFileName, remoteFileName);

		getFileTransfer().upload(localFileName, remoteFileName);

		if (uploadOk) {
			File okFile = new File(getLocalHome() + File.separator + localFileName + FILE_OK);
			if (!okFile.exists()) {
				try {
					boolean createNewFile = okFile.createNewFile();
					bizlog.debug("create new file", createNewFile);
				}
				catch (IOException e) {
					throw GlError.GL.E0105();
				}
			}
			getFileTransfer().upload(localFileName + FILE_OK, remoteFileName + FILE_OK);
		}

		bizlog.method("upload end >>>>>>>>>>>>>>>>>>>>");
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年2月9日-下午4:09:23</li>
	 *         <li>功能说明：获取文件传输组件</li>
	 *         </p>
	 * @return
	 */
	public static FaBaseComp.FileTransfer getFileTransfer() {
		bizlog.method("getFileTransfer begin >>>>>>>>>>>>>>>>>>>>");

		FaBaseComp.FileTransfer abFT = SysUtil.getInstance(FaBaseComp.FileTransfer.class, AbstractComponent.FileTransfer);
		bizlog.parm("abFT [%s]", abFT);

		bizlog.method("getFileTransfer end >>>>>>>>>>>>>>>>>>>>");
		return abFT;
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年2月9日-下午5:59:09</li>
	 *         <li>功能说明：获取文件全路径</li>
	 *         </p>
	 * @param rootDir
	 * @param fileName
	 * @return
	 */
	public static String getFileFullPath(String rootDir, String fileName) {
		bizlog.method("getFileFullPath begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.parm(" rootDir[%s],  fileName [%s]", rootDir, fileName);

		String sFullPath = FileUtil.getFullPath(rootDir, fileName);

		bizlog.parm("sFullPath [%s]", sFullPath);
		bizlog.method("getFileFullPath end >>>>>>>>>>>>>>>>>>>>");
		return sFullPath;
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年3月2日-下午7:58:45</li>
	 *         <li>功能说明：获取本地的绝对路径部分/li>
	 *         </p>
	 * @return
	 */
	public static String getLocalHome() {
		return getFileTransfer().workDirectory();

	}
	
	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年3月2日-下午7:58:45</li>
	 *         <li>功能说明：获取本地的绝对路径部分/li>
	 *         </p>
	 * @return
	 */
	public static String getLocalHome(String fileName) {
		return getFileTransfer().workDirectory()+File.separator+fileName;

	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年3月2日-下午7:58:45</li>
	 *         <li>功能说明：获取远程的绝对路径部分/li>
	 *         </p>
	 * @return
	 */
	public static String getRemoteHome() {
		return getFileTransfer().getRemoteDirectory();
	}
	

	/**
	 * @Author liuzf@sunline.cn
	 *         <p>
	 *         <li>2017年3月17日-下午6:05:36</li>
	 *         <li>功能说明：同步远程文件到本地</li>
	 *         </p>
	 * @param fileRecvId
	 * @return
	 */
	public static void syncRemoteFile2Local(String busiBatchId) {
		bizlog.method(" ApFile.syncRemoteFile2Local begin >>>>>>>>>>>>>>>>");
		// busiBatchId 需要在 app_batch 有定义
		app_batch appBatch = App_batchDao.selectOne_odb1(busiBatchId, false);
		if (appBatch == null) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(app_batch.class).getLongname(), GlDict.A.busi_batch_id.getLongName(), busiBatchId);
		}

		// 远程服务器是系统缺省的文件服务器
		String remoteDir = getFullPath(appBatch.getRemote_dir_code());
		String localPath = getFullPath(appBatch.getLocal_dir_code());
		//String localDir = getLocalHome() + localPath;
		String localDir = localPath;

		// 本地无local_dir_code 目录则自动创建，创建失败需要抛出
		File dirFile = new File(localDir);
		if (!dirFile.exists()) {
			if (!dirFile.mkdirs())
				throw GlError.GL.E0104(localDir);
		}

		// 获取远程 *.ok 列表中本地不存在的清单
		ApRemoteFileList apRemoteFileList = getRemoteFileList(remoteDir, FILE_OK);

		for (String fileNames : apRemoteFileList.getFile_name()) {
			File file = new File(getFileFullPath(localDir, fileNames.substring(0, fileNames.lastIndexOf(FILE_OK)).concat(FILE_TXT)));
			if (!file.exists()) {
				apb_batch_receive tblApbBatchReceive = Apb_batch_receiveDao.selectFirst_odb3(file.getName(), remoteDir, E_YESORNO.NO, false);
				if (tblApbBatchReceive == null) {// 文件不存在且未请求下载
					bizlog.debug("fileName[%s] not exits, register now!", file.getName());
					// 生成文件下载请求
					ApFileIn apFileIn = CommTools.getInstance(ApFileIn.class);
					apFileIn.setBusi_batch_code(ApSeq.genSeq("BUSI_BATCH_CODE"));// 文件批量号
					apFileIn.setBusi_batch_id(appBatch.getBusi_batch_id());// 文件批量业务ID
					apFileIn.setFile_name(file.getName());// 文件名称
					apFileIn.setFile_server_path(remoteDir);// 服务器路径
					apFileIn.setTiming_process_ind(E_YESORNO.NO);
					// 调用文件批量申请
					FaApBatch.fileBatchApply(apFileIn);
				}
			}
		}
		bizlog.method(" ApFile.syncRemoteFile2Local end <<<<<<<<<<<<<<<<");
	}
	
	/**
	 * 获取指定远程路径下指定规则文件列表
	 * 
	 * <p>Title:getRemoteFileList </p>
	 * <p>Description:	</p>
	 * @author songhao
	 * @date   2017年8月29日 
	 * @param remoteDir 远程路径
	 * @param fileRegs 匹配规则
	 * @return
	 */
	public static ApRemoteFileList getRemoteFileList(String remoteDir, String fileRegs){
	    bizlog.method("getRemoteFileList begin >>>>>>>>>>>>>>>>>>>>");
        bizlog.parm(" remoteDir[%s],  fileRegs [%s]", remoteDir, fileRegs);
	    
	    ApRemoteFileList apRemoteFileList = CommTools.getInstance(ApRemoteFileList.class);;
	    List<String> listNames = new ArrayList<String>();
	    
	    //判断ftp开关，Y-开，否则取本地
	    if("Y".equals(FaTools.getRemoteDir())){
	        //apRemoteFileList= getFileTransfer().getRemoteFileList(remoteDir, FILE_OK); 
	        
	        MsTransfer create = OssFactory.get().create("default");
			if (create == null) {
				bizlog.debug("OSS 对象初始化失败！");
			}
			
	        try {
	        	List<MsFileInfo> resList = create.listAllFiles(false, remoteDir);
	            for(int i = 0; i < resList.size(); i++ ){
	                if(resList.get(i).getFileName().endsWith(fileRegs)){
	                    listNames.add(resList.get(i).getFileName());
	                    bizlog.info(remoteDir+"目录下文件第"+i+"个：["+resList.get(i).getFileName()+"]");
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        
	        apRemoteFileList.getFile_name().addAll(listNames);
	        
	    }else{
	        File file=new File(remoteDir);
	        File[] tempList = file.listFiles();
	        if(CommUtil.isNotNull(tempList)){
	            for (int i = 0; i < tempList.length; i++) {
	                if (tempList[i].isFile() && tempList[i].getName().endsWith(fileRegs)) {
	                    bizlog.debug("getRemoteFileList >>>>>>>>>>>>>>>>>>>> 远程路径[%s]下文件[%s]", remoteDir, tempList[i].getName());
	                    listNames.add(tempList[i].getName());
	                }
	            }
	            apRemoteFileList.getFile_name().addAll(listNames); 
	        } else {
	        	bizlog.info("apRemoteFileList:[%s] empty", apRemoteFileList);
	        }
	    }
	    bizlog.method("getRemoteFileList end >>>>>>>>>>>>>>>>>>>>");
	    return apRemoteFileList;
	}
	
	/**
     * 复制单个文件 
     * <p>Title:copyFile </p>
     * <p>Description:  </p>
     * @author songhao
     * @date   2017年8月29日 
     * @param oldPath 原文件路径 如：c:/fqf.txt 
     * @param newPath 复制后路径 如：f:/fqf.txt 
     */
    public static void copyFile(String oldPath, String newPath) {
        bizlog.method("copyFile begin <<<<<<<<<<<<<<<<<<<<");
        bizlog.parm(" oldPath[%s],  fileRegs [%s]", oldPath, newPath);
        InputStream fis = null;
        OutputStream fos = null;
        int byteread = 0;
        try {
            File oldfile = new File(oldPath); 
            if (oldfile.exists()) { //文件存在时 
                fis = new FileInputStream(oldPath); //读入原文件 
                
                try {
                    fos = new FileOutputStream(newPath); 
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    while ( (byteread = fis.read(buffer)) != -1) { 
                    	fos.write(buffer, 0, byteread); 
                    }
                } catch (IOException e) {
                	bizlog.debug("Exception e[5s]", e);
                } finally {
                    if (fos != null) {
                        try {
                        	fos.close();
                        } catch (IOException e) {
                        	bizlog.debug("Exception e[5s]", e);
                        }
                    }
                }
            }
            bizlog.debug("原文件[%s]复制到[%s]执行成功", oldPath, newPath);
        } 
        catch (Exception e) {
            bizlog.debug("Exception e[5s]", e);
        }finally{
        	if (fis != null) {
                try {
                	fis.close();
                } catch (IOException e) {
                	bizlog.debug("Exception e[5s]", e);
                }
            }
        }
        bizlog.method("copyFile end <<<<<<<<<<<<<<<<<<<<");
    }

}
