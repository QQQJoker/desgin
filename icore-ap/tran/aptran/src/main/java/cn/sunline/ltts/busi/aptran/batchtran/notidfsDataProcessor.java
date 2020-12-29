package cn.sunline.ltts.busi.aptran.batchtran;

import java.io.File;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.aplt.tools.LogManager;

	 /**
	  * 系统换日后通知文件子系统
	  *
	  */

public class notidfsDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.Notidfs.Input, cn.sunline.ltts.busi.aptran.batchtran.Notidfs.Property> {
	private static BizLog log = LogManager.getBizLog(notidfsDataProcessor.class); 
	private kapb_wjplxxb filetab = CommTools.getInstance(kapb_wjplxxb.class);
	/**
	 * 批次数据项处理逻辑。
	 * @author wuzhixiang
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.Notidfs.Input input, cn.sunline.ltts.busi.aptran.batchtran.Notidfs.Property property) {
		   
		    String bflsdt = CommToolsAplt.prcRunEnvs().getLstrdt();//上次交易日期
		    String filenm = bflsdt+".nas" ;   	  
		    String flpath = CommTools.KnpParaQryByCorpno("NOTIDFS", "dpfile","01", "%",true).getPmval2()+File.separator + "nas" + File.separator;			 
			final LttsFileWriter file = new LttsFileWriter(flpath, filenm, "UTF-8");
			file.open();
			try{								
				String ret  ="ok";
				file.write(ret);
			}finally{
				file.close();
			}		
	}

}


