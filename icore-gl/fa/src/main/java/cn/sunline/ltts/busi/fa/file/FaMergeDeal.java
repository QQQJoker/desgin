package cn.sunline.ltts.busi.fa.file;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.core.expression.ExpressionEvaluator;
import cn.sunline.adp.core.expression.ExpressionEvaluatorFactory;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DaoTools;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_file;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.apb_merge_receive;
import cn.sunline.ltts.busi.gl.fa.tables.TabFaRecLedger.fab_merge_detail;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.fa.util.FaApFile;

public class FaMergeDeal {

	/**
	* @Title: genFilename  
	* @Description: 按文件名称组成解析文件名称  
	* @param @param file_name_part
	* @param @return    设定文件  
	* @auther zhangan
	* @return String    返回类型  
	* @throws
	 */
	public static String genFilename(apb_merge_file tblMergeFile){
		
		String trandt = CommTools.prcRunEnvs().getTrandt();
		ExpressionEvaluator ee = ExpressionEvaluatorFactory.getInstance();
		Params param = new Params();
		
		param.put("sys_no", tblMergeFile.getSys_no());
		param.put("channel_id", tblMergeFile.getChannel_id());
		param.put("trandt", trandt);
		
		String file_name = ee.evalText(tblMergeFile.getFile_name_part(), param, param);
		
		return file_name;
	}
	
	/**
	* @Title: alyMergeFile  
	* @Description: 读取文件入库，并校验文件借贷平衡  
	* @param @param apb_merge_receive 文件接收明细记录  
	* @auther zhangan
	* @return void    返回类型  
	* @throws
	 */
	public static void alyMergeFile(apb_merge_receive mergeFile){
		
		String trxn_date = CommTools.prcRunEnvs().getTrandt();
		
		List<fab_merge_detail> lstMerge = new ArrayList<>();
		
		String busi_batch_code = mergeFile.getBusi_batch_code();
		String sys_no = mergeFile.getSys_no();
		String channel_id = mergeFile.getChannel_id();
		String file_path_name = mergeFile.getFile_local_path() + mergeFile.getFile_local_name();
		
		File file = new File(file_path_name);
		
		
		BigDecimal totalDebitAmt = BigDecimal.ZERO;
		BigDecimal totalCreitAmt = BigDecimal.ZERO;
		
		List<String> lstDetail = FaApFile.readFile(file);
		for(String str : lstDetail){
			fab_merge_detail detail = CommTools.getInstance(fab_merge_detail.class);
			
			/**
			 * 	正文
				名字：898118-CRDT-OPNBRGL-20210510
				内容：898118898121202005 77294034D 0C 1 1 
				银行编号1, 6||开户网点代号7, 6||科目编号13, 20||当日借方发生额33, 12||当日借方发生额符号45, 1||当日贷方发生额46, 12||当日贷方发生额符号58, 1||当日借方笔数59, 8||当日贷方笔数67, 8||保留字段75, 50 
				结束文件：
				名字：END-898118-CRDT-OPNBRGL-20210510
			 */
			String branch_id = str.substring(0, 6).trim(); //银行编号
			String org_id = str.substring(6, 12).trim(); //开户网点代号
			String gl_code = str.substring(12, 32).trim(); //科目
			BigDecimal curr_debit_amt = ConvertUtil.toBigDecimal(str.substring(32, 44).trim()).divide(BigDecimal.valueOf(100.00)); //当日借方发生额
			E_DEBITCREDIT curr_debit_amntcd = CommUtil.toEnum(E_DEBITCREDIT.class, str.substring(44, 45).trim()); //当日借方借贷标志
			BigDecimal curr_credit_amt = ConvertUtil.toBigDecimal(str.substring(45, 57).trim()).divide(BigDecimal.valueOf(100.00));; //当日贷方发生额
			E_DEBITCREDIT curr_credit_amntcd = CommUtil.toEnum(E_DEBITCREDIT.class, str.substring(57, 58).trim()); //当日贷方借贷标志
			long debit_count = ConvertUtil.toLong(str.substring(58, 66).trim()); //借方笔数
			long creit_count = ConvertUtil.toLong(str.substring(66, 74).trim()); //贷方笔数
			String merge_standy = str.substring(74).trim(); //预留字段
			
			detail.setSys_no(sys_no);
			detail.setChannel_id(channel_id);
			detail.setBusi_batch_code(busi_batch_code);
			detail.setFile_handling_status(E_FILEDEALSTATUS.CHECKED);
			detail.setTrxn_date(trxn_date);
			detail.setTrxn_ccy(CommTools.getDefineCurrency());
			
			detail.setBranch_id(branch_id);
			detail.setTrxn_branch(org_id);
			detail.setGl_code(gl_code);
			detail.setCurrent_debit_amt(curr_debit_amt);
			detail.setCurr_debit_amntcd(curr_debit_amntcd);
			detail.setCurrent_credit_amt(curr_credit_amt);
			detail.setCurr_credit_amntcd(curr_credit_amntcd);
			detail.setDebit_count(debit_count);
			detail.setCreit_count(creit_count);
			detail.setMerge_standy(merge_standy);
			
			totalDebitAmt = totalDebitAmt.add(detail.getCurrent_debit_amt());
			totalCreitAmt = totalCreitAmt.add(detail.getCurrent_credit_amt());
			
			lstMerge.add(detail);
		}
		
		if(CommUtil.equals(totalDebitAmt, totalCreitAmt)){
			DaoTools.execBatchInsert(fab_merge_detail.class, lstMerge);
			mergeFile.setFile_handling_status(E_FILEDEALSTATUS.CHECKED);
		}else{
			mergeFile.setFile_handling_status(E_FILEDEALSTATUS.FAILCHECK_INSERT);
			mergeFile.setError_text("并账明细借贷方发生额不平");
		}
		
		lstDetail.clear();
		
	}
	
}
