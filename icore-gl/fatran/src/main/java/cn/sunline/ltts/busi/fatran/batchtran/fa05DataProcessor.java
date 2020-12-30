
package cn.sunline.ltts.busi.fatran.batchtran;
import java.io.File;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.clwj.oss.api.OssFactory;
import cn.sunline.clwj.oss.model.MsFileInfo;
import cn.sunline.clwj.oss.model.MsTransferFileInfo;
import cn.sunline.clwj.oss.spi.MsTransfer;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_BALPROP;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_MANUALOPENACCTMODE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SUBJECTTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.fa.util.FaApFile;
import cn.sunline.ltts.fa.util.FaConst;
	 /**
	  * 同步总账科目信息
	  * @author 
	  * @Date 
	  */

public class fa05DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.edsp.busi.gl.fatran.batchtran.intf.Fa05.Input, cn.sunline.edsp.busi.gl.fatran.batchtran.intf.Fa05.Property> {
  private static final BizLog bizlog = BizLogUtil.getBizLog(fa05DataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.edsp.busi.gl.fatran.batchtran.intf.Fa05.Input input, cn.sunline.edsp.busi.gl.fatran.batchtran.intf.Fa05.Property property) {
		
		MsTransfer create = OssFactory.get().create("default");
		if (create == null) {
			bizlog.debug("OSS 对象初始化失败！");
		}
		
		KnpPara cplKnpPara = KnpParaDao.selectOne_odb1("GLfa05.file", "%", "%", "%", CommTools.prcRunEnvs().getCorpno(), true);
		
		String remotepath = cplKnpPara.getPmval1();
		String localpath = cplKnpPara.getPmval2();
		String filename = cplKnpPara.getPmval3();

		Boolean isFileUploadFinished = false;
		try {
			List<MsFileInfo> chkExistFileList = create.listAllFiles(false, remotepath);
			bizlog.debug("------------获取文件列表：" + chkExistFileList);
				
			for(int i=0; i<chkExistFileList.size(); i++) {
				MsFileInfo tempItem = chkExistFileList.get(i);
				String fileFullName = tempItem.getFileName();
				if(CommUtil.equals(filename, fileFullName)) {
					isFileUploadFinished = true;
				}
			}
	            
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		
		
		// 下载文件
		if(isFileUploadFinished) {
			MsTransferFileInfo downFile = new MsTransferFileInfo();
			downFile.setLocalFile(new MsFileInfo(localpath, filename));
			downFile.setRemoteFile(new MsFileInfo(remotepath, filename));
			
			create.download(downFile);
			
			bizlog.debug("------------下载文件结束：" + downFile);
			
			// 读取文件信息
			List<String> fileDataList = FaApFile.readFile(new File(localpath.concat("/").concat(filename)));
			bizlog.debug("fileDataList.size()=[%s]", fileDataList.size());
			if (fileDataList.size() > 0) {
				//首先清空会计科目数据
				Fap_accounting_subjectDao.delete();
				
				for (String line : fileDataList) {
					// 会计科目
					fap_accounting_subject subjectList = SysUtil.getInstance(fap_accounting_subject.class);
					String[] lineSplits = line.split(FaConst.SEPARATION_CHARACTER);
					subjectList.setGl_code(lineSplits[2]);  //科目号
					subjectList.setGl_code_desc(lineSplits[3]);  //科目名称
					subjectList.setUpper_lvl_gl_code(lineSplits[2].substring(0, 6));  //上级科目
					
					switch (lineSplits[4]) {
					case "Z001" ://资产类
						subjectList.setGl_code_type(E_SUBJECTTYPE.ASSETS);  //科目类别
						subjectList.setBal_prop(E_BALPROP.DEBIT);  //余额性质
						subjectList.setOn_bal_sheet_ind(E_YESORNO.YES);  //表内标志
						break;
					case "Z002" ://负债类
						subjectList.setGl_code_type(E_SUBJECTTYPE.LIABILITY);  //科目类别
						subjectList.setBal_prop(E_BALPROP.CREDIT);  //余额性质
						subjectList.setOn_bal_sheet_ind(E_YESORNO.YES);  //表内标志
						break;
					case "Z003" ://共同类
						subjectList.setGl_code_type(E_SUBJECTTYPE.ASSET_AND_LIABILITY);  //科目类别
						subjectList.setBal_prop(E_BALPROP.NETTING);  //余额性质
						subjectList.setOn_bal_sheet_ind(E_YESORNO.YES);  //表内标志
						break;
					case "Z004" ://所有者权益类
						subjectList.setGl_code_type(E_SUBJECTTYPE.OWNER_EQUITY);  //科目类别
						subjectList.setBal_prop(E_BALPROP.CREDIT);  //余额性质
						subjectList.setOn_bal_sheet_ind(E_YESORNO.YES);  //表内标志
						break;
					case "Z005" ://损益类
						subjectList.setGl_code_type(E_SUBJECTTYPE.PEOFIT_AND_LOSS);  //科目类别
						subjectList.setBal_prop(E_BALPROP.NETTING);  //余额性质
						subjectList.setOn_bal_sheet_ind(E_YESORNO.YES);  //表内标志
						break;
					case "Z006" ://表外科目
						subjectList.setGl_code_type(E_SUBJECTTYPE.OFF_BALANCE);  //科目类别
						subjectList.setBal_prop(E_BALPROP.DEBIT);  //余额性质
						subjectList.setOn_bal_sheet_ind(E_YESORNO.NO);  //表内标志
						break;
					case "Z007" ://表外科目
						subjectList.setGl_code_type(E_SUBJECTTYPE.OFF_BALANCE);  //科目类别
						subjectList.setBal_prop(E_BALPROP.DEBIT);  //余额性质
						subjectList.setOn_bal_sheet_ind(E_YESORNO.NO);  //表内标志
						break;
					}
					
					subjectList.setGl_code_level(Long.valueOf("3"));  //科目级别
					subjectList.setEnd_gl_code_ind(E_YESORNO.YES);  //末层科目标志
					subjectList.setIdentifier_code(lineSplits[2].substring(0, 8));  //识别码
					
					subjectList.setSimple_list_display_ind(E_YESORNO.YES);  //简表展示标识
					subjectList.setBal_check_ind(E_YESORNO.NO);  //余额检查标志
					subjectList.setDebit_manual_allow(E_YESORNO.YES);  //借方手工记账许可
					subjectList.setCredit_manual_allow(E_YESORNO.YES);  //贷方手工记账许可
					subjectList.setManual_open_acct_mode(E_MANUALOPENACCTMODE.SELF_OPENING);  //手工开户受理模式
					subjectList.setAllow_accounting_sys("");  //允许记账的系统
					subjectList.setValid_ind(E_YESORNO.YES);  //有效标志
					subjectList.setOpp_open_ind(E_YESORNO.NO);  //对开标志
					subjectList.setOffset_gl_code("");  //对方科目
					subjectList.setOpp_open_way(null);  //对开账户识别方式
					subjectList.setCreate_date(CommTools.prcRunEnvs().getTrandt());  //建立日期
					subjectList.setRecdver(Long.valueOf("0"));  //记录版本号
					
					Fap_accounting_subjectDao.insert(subjectList);
	
				}
					
			}
		}
	 }
}

