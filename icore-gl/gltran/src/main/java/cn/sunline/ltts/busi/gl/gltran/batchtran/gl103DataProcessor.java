package cn.sunline.ltts.busi.gl.gltran.batchtran;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.edsp.base.util.file.FileUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.namedsql.GlFileDao;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
	 /**
	  * 绩效系统总账抽数
	  *
	  */

public class gl103DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl103.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl103.Property> {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl103DataProcessor.class);
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl103.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl103.Property property) {
		 
		 // 取上日总账数据，如果交易日期有传
		 String trandt =CommUtil.nvl(input.getTrandt(),  CommToolsAplt.prcRunEnvs().getLstrdt());
		 // 
		 KnpPara para = CommTools.KnpParaQryByCorpno("twxx.file", "%", "%", "%", true); // 绩效抽数文件夹
		 String upfeph = para.getPmval1() + trandt + File.separator ; //上传路径+日期
		 
		 String upfena = "ZZ3.dat"; 
		 String okfile = "ZZ3.ok";
		 
		 try {
			FileUtil.createDir(upfeph);
		} catch (IOException e) {
			bizlog.debug("create dir", e);
			e.printStackTrace();
		}
		 FileUtil.createFile(upfeph, upfena, true);
		 StringBuffer body = new StringBuffer();
		 List<HashMap> glReportList = GlFileDao.lstGlReportData(trandt, false);
		 if(null != glReportList && glReportList.size() > 0)
		 {
			 for(int i=0;i<glReportList.size();i++)
			 {
				 Map<String,Object> map = glReportList.get(i);
				 body.append(CommUtil.nvl(map.get("trxn_date"), trandt)).append("|");
				 body.append(CommUtil.nvl(map.get("gl_code"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("branch_id"), "898118")).append("|");	 
				 body.append(CommUtil.nvl(map.get("ccy_code"), "CNY")).append("|");
				 body.append(CommUtil.nvl(map.get("subkno"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("current_debit_amt"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("current_credit_amt"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("prev_debit_bal"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("prev_credit_bal"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("current_debit_bal"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("current_credit_bal"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("subj_bal"), "0")).append("\n"); 
			 } 
		 }
		 
		 FileUtil.writeToFile(upfeph+upfena, body.toString(), "UTF-8", true);
		 FileUtil.createFile(upfeph, okfile, true);
	}

}


