package cn.sunline.ltts.busi.aptran.batchtran;

import java.io.File;
import java.io.IOException;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DataTools;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
	 /**
	  * 数仓跑数后压缩数据包
	  * @author wuzhixiang
	  * @data March 7
	  */

public class zipdataDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.Zipdata.Input, cn.sunline.ltts.busi.aptran.batchtran.Zipdata.Property> {
	  private static final BizLog bizlog = LogManager.getBizLog(DataTools.class);
	  private String lstdt  = CommToolsAplt.prcRunEnvs().getLstrdt();// 上次交易日期

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * 
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.Zipdata.Input input, cn.sunline.ltts.busi.aptran.batchtran.Zipdata.Property property) {
			KnpPara  tbl_knpPara = CommTools.getInstance(KnpPara.class);
			tbl_knpPara = CommTools.KnpParaQryByCorpno("DATAPRO", "add", "%", "%", true);
			String flpath = tbl_knpPara.getPmval2();//路径加上日期		 
            String tarname = "NK_"+lstdt+".tar";
            
			bizlog.debug("*****************压缩开始**************************");
			
			//String command = "/data/shell/comp.sh "+flpath.concat(File.separator).concat(tarname)+" "+flpath.concat(File.separator).concat(lstdt);
			String command = tbl_knpPara.getPmval3()+" "+flpath.concat(File.separator).concat(tarname)+" "+flpath.concat(File.separator).concat(lstdt);
			bizlog.debug("***************command:" +command+ "***********************");
			try {
				 Process process = Runtime.getRuntime().exec(command);
				 
				 process.waitFor();
				
			} catch (IOException e) {
				
				bizlog.debug(e.getMessage());
				throw ApError.Aplt.E0042(command);
				
			} catch (InterruptedException e) {
				
				bizlog.debug(e.getMessage());
				throw ApError.Aplt.E0042(command);
			}
			bizlog.debug("*****************压缩结束始**************************");
			
			String tarpathnm= tbl_knpPara.getPmval2().concat(File.separator).concat(tarname);
			File f = new File(tarpathnm);
			String filenm = "NK_"+lstdt +".FLAG" ;   	  		 
			final LttsFileWriter file = new LttsFileWriter(flpath, filenm, "UTF-8");				
			file.open();				
			try{								
				String ret  =tarname +" "+f.length();					
				file.write(ret);					
			}finally{					
				file.close();
		}		
	}
}
 


