
package cn.sunline.ltts.busi.gltran.batchtran;
import cn.sunline.edsp.base.lang.*;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.aplt.tools.FtpHelper;
import cn.sunline.ltts.busi.fa.tables.TabFaRegBook.fab_accounting_voch;
import cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl111.Input;
import cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl111.Property;
import cn.sunline.ltts.busi.gl.namedsql.GlBranchDao;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
	 /**
	  * 生成总账对账文件
	  * @author 
	  * @Date 
	  */

public class gl111DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl111.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl111.Property> {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl111DataProcessor.class);
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	 public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl111.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl111.Property property) {
		 
		 //获取上日交易日期
		 String lstrdt = CommTools.prcRunEnvs().getLstrdt();
		 
		 // 定位文件路径
		 KnpPara path = CommTools.KnpParaQryByCorpno("GL111.file", "path", "%", "%", false);
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
			 DaoUtil.selectList(GlBranchDao.namedsql_lstFabAccountingVochByDate, params, new CursorHandler<fab_accounting_voch>() {

				@Override
				public boolean handle(int arg0, fab_accounting_voch voch) {
					
					StringBuffer sb = new StringBuffer();
					
					CommTools.genNewSerail();	
					String transq = CommTools.prcRunEnvs().getTransq();	    // 生成新流水

					// 生成新的流水
					sb.append(transq).append(",");  						// 行号 
					sb.append(lstrdt).append(",");							// 账务日期
					sb.append(lstrdt + "00001").append(",");  	   			// 交易流水号 
					sb.append(lstrdt).append(",");							// 批号
					
					String voucherType = ""; 
					if(CommUtil.equals(FaConst.LOAN_SYSTEM, voch.getSys_no())) {
						voucherType = "Z1";
					}else if(CommUtil.equals(FaConst.CORE_SYSTEM, voch.getSys_no())){
						voucherType = "Z2";
					}
					sb.append(voucherType).append(",");					    // 凭证类型：Z1:贷款；Z2:存款
					sb.append("1000").append(",");							// 公司代码
					sb.append(lstrdt).append(",");					        // 过账日期
					sb.append(lstrdt).append(",");					        // 凭证日期
					sb.append(lstrdt.substring(4, 6)).append(",");	        // 会计期间
					sb.append("CNY").append(",");							// 货币
					sb.append(",");											// 凭证抬头文本
					sb.append(",");											// 业务类型
					sb.append(",");											// 类别
					sb.append(",");											// 业务范围
					sb.append(",");											// 成本中心
					sb.append(",");											// 利润中心
					sb.append(voch.getGl_code()).append(",");				// 科目编号
					sb.append(",");	
					
					// 发生额
					if(E_DEBITCREDIT.DEBIT == voch.getDebit_credit()) {
						if(CommUtil.compare(voch.getAccounting_amt(), BigDecimal.ZERO) >= 0) {
							sb.append("40").append(",");									// 记账方向
							sb.append(voch.getAccounting_amt()).append(","); 				// 金额
							sb.append(",");													// 反记账
						}else if(CommUtil.compare(voch.getAccounting_amt(), BigDecimal.ZERO) < 0){
							sb.append("50").append(",");									// 记账方向
							sb.append(voch.getAccounting_amt().negate()).append(","); 		// 金额
							sb.append("X").append(",");										// 反记账
						}
					}else if(E_DEBITCREDIT.CREDIT == voch.getDebit_credit()) {
						if(CommUtil.compare(voch.getAccounting_amt(), BigDecimal.ZERO) >= 0) {
							sb.append("50").append(",");									// 记账方向
							sb.append(voch.getAccounting_amt()).append(","); 				// 金额
							sb.append(",");													// 反记账
						}else if(CommUtil.compare(voch.getAccounting_amt(), BigDecimal.ZERO) < 0){
							sb.append("40").append(",");									// 记账方向
							sb.append(voch.getAccounting_amt().negate()).append(","); 		// 金额
							sb.append("X").append(",");										// 反记账
						}
					}
					
					sb.append(",,,,,,,,,,,,,,,");											// 预留字段
					file.write(sb.toString());
					
					return true;
					
				}
			 });
		
		}finally {
			if(CommUtil.isNotNull(file))
				file.close();
		}
		 
	 }
	 
	@Override
	 public void afterTranProcess(String taskId, Input input, Property property) {
	     bizlog.method(" gl111DataProcessor.afterTranProcess begin >>>>>>>>>>>>>>>>");
	     
	     KnpPara sftp = CommTools.KnpParaQryByCorpno("GL111.file", "sftp", "%", "%", false); //sftp-ip-端口-用户名-密码
			
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
		
		 bizlog.method(" gl111DataProcessor.afterTranProcess end <<<<<<<<<<<<<<<<");
	 }
	 
}


