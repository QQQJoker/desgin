package cn.sunline.ltts.busi.gl.gltran.batchtran;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.metadata.base.dao.CursorHandler;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.aplt.tools.FtpHelper;
import cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl101.Input;
import cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl101.Property;
import cn.sunline.ltts.busi.gl.namedsql.GlFileDao;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_GLDATEINTERVAL;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REPORTTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.glb_gl_report;
	 /**
	  * 生成核心并账文件
	  *
	  */

public class gl101DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl101.Input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl101.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl101.Input input, cn.sunline.ltts.busi.gl.gltran.batchtran.intf.Gl101.Property property) {
		
 
		String trandt = CommTools.prcRunEnvs().getTrandt(); //获取当前交易日期
		String brchno = CommTools.prcRunEnvs().getTranbr();
		String file_name = brchno + "-CRDT-OPNBRGL-" + trandt;
		 
		KnpPara path = CommTools.KnpParaQryByCorpno("GL101.file", "path", "%", "%", false);
		KnpPara mege = CommTools.KnpParaQryByCorpno("GL101.file", "no_merge", "%", "%", false);
		
		List<String> lstMege = new ArrayList<>();
		
		if(CommUtil.isNotNull(mege) && CommUtil.isNotNull(mege.getPmval1())){
			lstMege = Arrays.asList(mege.getPmval1().split(","));
		}
		
		final List<String> ls = lstMege;
		
		String file_lcpath = path.getPmval1();
		String file_rtpath = path.getPmval2();
		property.setFilena(file_name);
		property.setLcpath(file_lcpath);
		property.setRtpath(file_rtpath);
		 
		final LttsFileWriter file = new LttsFileWriter(file_lcpath, file_name);
		file.open();
		try {
				
			Params params = new Params();
			params.add("trxn_date", trandt);
			params.add("report_type", E_REPORTTYPE.BUSINETT_TYPE);
			params.add("gl_date_interval", E_GLDATEINTERVAL.DAILY);
			params.add("end_gl_code_ind", E_YESORNO.YES);
			//年结时，取试算报表数据
			String trxnDate = CommTools.prcRunEnvs().getTrandt();
			if (DateTools2.isLastDay("Y", trxnDate)) {
				params.add("report_type", E_REPORTTYPE.TRIAL_TYPE);
			}
			
			DaoUtil.selectList(GlFileDao.namedsql_lstGlbGLReportByDate, params, new CursorHandler<glb_gl_report>() {

				@Override
				public boolean handle(int arg0, glb_gl_report entity) {
					
					if(ls.contains(entity.getGl_code())){
						return true;
					}
					
					StringBuffer sb = new StringBuffer();
					if(!CommUtil.equals(entity.getCurrent_debit_amt(), BigDecimal.ZERO)){ //本期借方发生不等于0
						sb.setLength(0);//清空sb
						sb.append(entity.getBranch_id()); //6位长度
						sb.append("898121"); //6位长度
						sb.append(CommUtil.rpad(entity.getGl_code(), 20, " "));//科目编号，20位
						if(CommUtil.compare(entity.getCurrent_debit_amt(), BigDecimal.ZERO) > 0){
							sb.append(CommUtil.lpad(
									entity.getCurrent_debit_amt().multiply(BigDecimal.valueOf(100)).longValue()+"", 
									12, " ")); //12位金额
							if(entity.getOn_bal_sheet_ind() == E_YESORNO.YES){ //表内科目
								sb.append(E_DEBITCREDIT.DEBIT.getValue());
								sb.append(CommUtil.lpad(BigDecimal.ZERO.toString(), 12, " "));
								sb.append(E_DEBITCREDIT.CREDIT.getValue());
							}else{
								sb.append(E_DEBITCREDIT.RV.getValue());
								sb.append(CommUtil.lpad(BigDecimal.ZERO.toString(), 12, " "));
								sb.append(E_DEBITCREDIT.PY.getValue());
							}
						} else {
							sb.append(CommUtil.lpad(BigDecimal.ZERO.toString(), 12, " "));
							
							if(entity.getOn_bal_sheet_ind() == E_YESORNO.YES){ //表内科目
								sb.append(E_DEBITCREDIT.DEBIT.getValue());
								sb.append(CommUtil.lpad(
										entity.getCurrent_debit_amt().abs().multiply(BigDecimal.valueOf(100)).longValue()+"", 
										12, " "));
								sb.append(E_DEBITCREDIT.CREDIT.getValue());
							}else{
								sb.append(E_DEBITCREDIT.RV.getValue());
								sb.append(CommUtil.lpad(
										entity.getCurrent_debit_amt().abs().multiply(BigDecimal.valueOf(100)).longValue()+"", 
										12, " "));
								sb.append(E_DEBITCREDIT.PY.getValue());
							}
						}
						
						sb.append(CommUtil.lpad("1", 8, " "));
						sb.append(CommUtil.lpad("1", 8, " "));
						sb.append(CommUtil.lpad("", 50, " "));
						
						
						String line = sb.toString();
						file.write(line);
					}
					
					if(!CommUtil.equals(entity.getCurrent_credit_amt(), BigDecimal.ZERO)){ //本期贷方发生额
						sb.setLength(0);//清空sb
						sb.append(entity.getBranch_id()); //6位长度
						sb.append("898121"); //6位长度
						sb.append(CommUtil.rpad(entity.getGl_code(), 20, " "));//科目编号，20位
						
						if(CommUtil.compare(entity.getCurrent_credit_amt(), BigDecimal.ZERO) > 0){
							sb.append(CommUtil.lpad(BigDecimal.ZERO.toString(), 12, " "));
							
							if(entity.getOn_bal_sheet_ind() == E_YESORNO.YES){ //表内科目
								sb.append(E_DEBITCREDIT.DEBIT.getValue());
								sb.append(CommUtil.lpad(
										entity.getCurrent_credit_amt().multiply(BigDecimal.valueOf(100)).longValue()+"", 
										12, " "));
								sb.append(E_DEBITCREDIT.CREDIT.getValue());
							}else{
								sb.append(E_DEBITCREDIT.RV.getValue());
								sb.append(CommUtil.lpad(
										entity.getCurrent_credit_amt().multiply(BigDecimal.valueOf(100)).longValue()+"", 
										12, " "));
								sb.append(E_DEBITCREDIT.PY.getValue());
							}
						} else {
							sb.append(CommUtil.lpad(
									entity.getCurrent_credit_amt().abs().multiply(BigDecimal.valueOf(100)).longValue()+"", 
									12, " ")); //12位金额
							if(entity.getOn_bal_sheet_ind() == E_YESORNO.YES){ //表内科目
								sb.append(E_DEBITCREDIT.DEBIT.getValue());
								sb.append(CommUtil.lpad(BigDecimal.ZERO.toString(), 12, " "));
								sb.append(E_DEBITCREDIT.CREDIT.getValue());
							}else{
								sb.append(E_DEBITCREDIT.RV.getValue());
								sb.append(CommUtil.lpad(BigDecimal.ZERO.toString(), 12, " "));
								sb.append(E_DEBITCREDIT.PY.getValue());
							}
						}
						
						sb.append(CommUtil.lpad("1", 8, " "));
						sb.append(CommUtil.lpad("1", 8, " "));
						sb.append(CommUtil.lpad("", 50, " "));
						
						
						String line = sb.toString();
						file.write(line);
					}
					
					
					
					
					return true;
				}
				
			});
			
		} finally {
			file.close();
		}

	}

	@Override
	public void afterTranProcess(String taskId, Input input, Property property) {
		
		KnpPara sftp = CommTools.KnpParaQryByCorpno("GL101.file", "sftp", "%", "%", false); //sftp-ip-端口-用户名-密码
		
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
		/*
		SftpInfo sftpInfo = CommTools.getInstance(SftpInfo.class);
		sftpInfo.setLcfile(property.getFilena());
		sftpInfo.setLcpath(property.getLcpath());
		sftpInfo.setRmaddr(sftp.getPmval1());
		sftpInfo.setRmfile(property.getFilena());
		sftpInfo.setRmname(sftp.getPmval3());
		sftpInfo.setRmpass(sftp.getPmval4());
		sftpInfo.setRmpath(property.getRtpath());
		sftpInfo.setRmport(Convert.toInteger(sftp.getPmval2()));
		sftpInfo.setTimeot(5000);
		
		SftpTools.upload(sftpInfo);
		
		sftpInfo.setRmfile(okFile);
		
		InputStream in = new StringBufferInputStream("");
		
		
		SftpTools.makeOK(sftpInfo, in);
		*/
		//super.afterTranProcess(taskId, input, property);
	}
}


