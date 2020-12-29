package cn.sunline.ltts.busi.aptran.batchtran;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.adp.cedar.base.logging.BizLog;

	 /**
	  * 返回文件上传至NFS
	  * 将反盘文件copy到NFS目录
	  *
	  */

public class filecpDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Filecp.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Filecp.Property> {
  
	
	private static BizLog log = LogManager.getBizLog(filecpDataProcessor.class);
	
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Filecp.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Filecp.Property property) {
		
		 
		 
		log.debug("===========开始文件复制=========="); 
		 
		String filesq = input.getFilesq(); //文件批次号
		
		kapb_wjplxxb filetab = Kapb_wjplxxbDao.selectOne_odb1(filesq, true);
		
		
		String local_pathname = filetab.getLocaph() + filetab.getUpfena();
		String remot_pathname = filetab.getUpfeph() + filetab.getUpfena();
		
		log.debug("===========本地文件：[%s]",local_pathname);
		log.debug("===========目标文件：[%s]",remot_pathname); 
		
		File local = null;
		File remot = new File(remot_pathname);
		
		
		
		try {
			local = new File(local_pathname);
		} catch (Exception e) {
			
			String errotx = "找不到反盘文件[" + local_pathname + "]";
			filetab.setErrotx(errotx);
			filetab.setBtfest(E_BTFEST.FAIL);
			Kapb_wjplxxbDao.updateOne_odb1(filetab);
			
			log.debug("===========文件复制错误:[%s]", errotx); 
			
			throw ApError.Aplt.E0000(errotx);
		}
		
		
		if(remot.exists()){
			remot.delete();
		}
		
		try {
						
			FileUtils.copyFile(local, remot);
			filetab.setErrotx(E_BTFEST.SUCC.getLongName());
			filetab.setBtfest(E_BTFEST.SUCC);
		} catch (IOException e) {
			filetab.setErrotx(e.getLocalizedMessage());
			filetab.setBtfest(E_BTFEST.FAIL);
			log.debug("===========文件复制错误:[%s]", e.getLocalizedMessage()); 
		}
		//需要生成.ok文件加上文件類型即可
		if(filetab.getFiletp() ==  E_FILETP.DP021300){			
			String filenm = filetab.getUpfena()+".ok" ;  
			LttsFileWriter file = new LttsFileWriter(filetab.getUpfeph(), filenm, "UTF-8");
			file.open();
			try{
				String ret = "ok";					
				file.write(ret);	
			}finally{
				file.close();
			}
		}		
		Kapb_wjplxxbDao.updateOne_odb1(filetab);
		
		log.debug("===========文件复制结束=========="); 
	}

}


