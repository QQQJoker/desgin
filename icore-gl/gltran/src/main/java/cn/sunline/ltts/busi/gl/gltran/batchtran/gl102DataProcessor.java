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
	  * 绩效系统科目抽数
	  *
	  */

public class gl102DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl102.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl102.Property> {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl102DataProcessor.class);
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl102.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl102.Property property) {
		//TODO:
		 String lstrdt = CommToolsAplt.prcRunEnvs().getLstrdt();
		 
		 KnpPara para = CommTools.KnpParaQryByCorpno("twxx.file", "%", "%", "%", true); // 绩效抽数文件夹
		 String upfeph = para.getPmval1() + lstrdt + File.separator ; //上传路径+日期
		 
		 String upfena = "KM3.dat"; 
		 String okfile = "KM3.ok";
		 
		 try {
			FileUtil.createDir(upfeph);
		} catch (IOException e) {
			bizlog.debug("create dir", e);
			e.printStackTrace();
		}
		 FileUtil.createFile(upfeph, upfena, true);
		 StringBuffer body = new StringBuffer();
		 List<HashMap> subjectList = GlFileDao.lstSubjectData(false);
		 if(null != subjectList && subjectList.size() > 0)
		 {
			 for(int i=0;i<subjectList.size();i++)
			 {
				 Map<String,Object> map = subjectList.get(i);
				 body.append(CommUtil.nvl(map.get("gl_code"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("kmkzzz"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("gl_code_desc"), " ")).append("|");	 
				 body.append(map.get("gl_code_level")).append("|");
				 body.append(CommUtil.nvl(map.get("upper_lvl_gl_code"), " ")).append("|");
				 body.append(CommUtil.nvl(map.get("on_bal_sheet_ind"), " ")).append("\n"); 
			 }
			 
		 }
		 
		 FileUtil.writeToFile(upfeph + upfena, body.toString(), "UTF-8", true);
		 FileUtil.createFile(upfeph, okfile, true);
	}

}


