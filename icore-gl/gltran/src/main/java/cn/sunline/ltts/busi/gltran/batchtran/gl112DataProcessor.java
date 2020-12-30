
package cn.sunline.ltts.busi.gltran.batchtran;
import cn.sunline.edsp.base.lang.*;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FtpHelper;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl112.Input;
import cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl112.Property;
import cn.sunline.ltts.busi.gl.namedsql.GlBranchDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.gla_branch_gl;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;

	 /**
	  * 生成总账余额文件
	  * @author 
	  * @Date 
	  */

public class gl112DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl112.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl112.Property> {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl112DataProcessor.class);
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl112.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl112.Property property) {
		 
		 //获取上日交易日期
		 String lstrdt = CommTools.prcRunEnvs().getLstrdt();
		 
		 // 定位文件路径
		 KnpPara path = CommTools.KnpParaQryByCorpno("GL112.file", "path", "%", "%", false);
		 String filePath = path.getPmval1();	// 本地路径
		 String fineName = path.getPmval3() + lstrdt + path.getPmval4();
		 
		 property.setFilena(fineName);
	     property.setLcpath(path.getPmval1());
		 property.setRtpath(path.getPmval2());

		 // 写入文件
		 final LttsFileWriter file = new LttsFileWriter(filePath, fineName);
		 file.open();
		 
		 try {
			 
			 E_REPORTTYPE reportType = E_REPORTTYPE.BUSINETT_TYPE; 
			 if(DateTools2.isLastDay("Y", lstrdt) && !FaTools.getYearendStatus()){
				 reportType = E_REPORTTYPE.TRIAL_TYPE;
			 }
			 
			 Params params = new Params();
			 params.add("trxn_date", lstrdt);
			 params.add("report_type", reportType);
			 params.add("end_gl_code_ind", E_YESORNO.YES);
			 params.add("org_id", CommTools.prcRunEnvs().getCorpno());
			 DaoUtil.selectList(GlBranchDao.namedsql_lstGlaBranchGlByDate, params, new CursorHandler<gla_branch_gl>() {
				 
				 @Override
				 public boolean handle(int arg0, gla_branch_gl glaGl) {
					 
					 StringBuffer sb = new StringBuffer();
					 sb.append(glaGl.getTrxn_date()).append(",");				// 账务日期
					 sb.append(glaGl.getBranch_id()).append(",");				// 机构号
					 sb.append(glaGl.getCcy_code()).append(",");				// 币种
					 sb.append(glaGl.getGl_code()).append(",");					// 科目编号
					 sb.append(glaGl.getPrev_debit_bal()).append(",");			// 期初借方
					 sb.append(glaGl.getPrev_credit_bal()).append(",");			// 期初贷方
					 sb.append(glaGl.getCurrent_debit_amt()).append(",");		// 发生借方
					 sb.append(glaGl.getCurrent_credit_amt()).append(",");		// 发生贷方
					 sb.append(glaGl.getCurrent_debit_bal()).append(",");		// 期末借方
					 sb.append(glaGl.getCurrent_credit_bal()).append(",");		// 期末贷方										
																		
					 String line = sb.toString();
					 file.write(line);
					 
					 return true;
				 };
			 });
			 
		 }finally {
			 if(CommUtil.isNotNull(file))
			 file.close();
		 }
			 
	 }
	 
	 
	 @Override
	 public void afterTranProcess(String taskId, Input input, Property property) {
		 bizlog.method(" gl112DataProcessor.afterTranProcess begin >>>>>>>>>>>>>>>>");
		 
		 KnpPara sftp = CommTools.KnpParaQryByCorpno("GL112.file", "sftp", "%", "%", false); //sftp-ip-端口-用户名-密码
			
		 String okFile = "END-" + property.getFilena();
		 			
		 final LttsFileWriter file = new LttsFileWriter(property.getLcpath(), okFile);
		 file.open();
		 file.write("");
		 file.close();

		 FtpHelper.login(sftp.getPmval1(), ConvertUtil.toInteger(sftp.getPmval2()), sftp.getPmval3(), sftp.getPmval4());

		 String localFileName = property.getLcpath() + property.getFilena();
		 String ftpDirName = property.getRtpath();
		 String ftpFileName = property.getFilena();
		 FtpHelper.uploadFile(localFileName, ftpDirName, ftpFileName);

		 localFileName = property.getLcpath() + okFile;
		 ftpFileName = okFile;
		 FtpHelper.uploadFile(localFileName, ftpDirName, ftpFileName);

		 FtpHelper.closeFtpConnection();
		 
		 bizlog.method(" gl112DataProcessor.afterTranProcess end <<<<<<<<<<<<<<<<");
	 }

}


