package cn.sunline.ltts.busi.fatran.batchtran;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.namedsql.FaSettleDao;
import cn.sunline.ltts.busi.fa.type.ComFaAcctInit.ComFaAcctInitOt;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;

/**
 * 分户账余额初始化
 */
public class init01DataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.fatran.batchtran.intf.Init01.Input, cn.sunline.ltts.busi.fatran.batchtran.intf.Init01.Property> {
	
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.fatran.batchtran.intf.Init01.Input input, cn.sunline.ltts.busi.fatran.batchtran.intf.Init01.Property property) {
		
//		KnpPara paraCompInd = CommTools.KnpParaQryByCorpno(FaConst.KEY_INIT_COMP_IND, "%","%","%", false);
//		if (CommUtil.isNull(paraCompInd) || FaConst.INIT_COMP_IND_YES.equals(paraCompInd.getPmval1())) {
//			bizlog.info(">>==============初始化分户账余额已完成, 跳过本次处理.");
//			return;
//		}

		String acct_branch = loadBranchNo();//机构号
		//读取文件
		List<ComFaAcctInitOt> list = FaSettleDao.lstAllInitData(false);
		if (CommUtil.isNotNull(list) && list.size() > 0) {
			List<FaSingleAccountingCheckIn> accountingList = new ArrayList<FaSingleAccountingCheckIn>();
			for (ComFaAcctInitOt dto : list) {
				accountingList.add(buildDto(dto, acct_branch));
			}
			//记账
			FaAccounting.bookMultiAccounting(accountingList, "1", 1L, FaConst.CORE_SYSTEM , "bookAccountingSeq", false);
			
			//更新完成标识
//			paraCompInd.setPmval1(FaConst.INIT_COMP_IND_YES);
//			KnpParaDao.updateOne_odb1(paraCompInd);
		}
	}
	 
	 /**
	 * 获取批量交易机构号
	 */
	private String loadBranchNo(){
		KnpPara corpPara = CommTools.KnpParaQryByCorpno("system.batch", SysUtil.getSystemId(), "tranbr", "%", false);
		return corpPara.getPmval1();
	 }
	
	/**
	 * 组装记账数据
	 */
	private FaSingleAccountingCheckIn buildDto(ComFaAcctInitOt dto, String acct_branch){
		FaSingleAccountingCheckIn accountingSingle = SysUtil.getInstance(FaSingleAccountingCheckIn.class);
		accountingSingle.setSys_no(FaConst.CORE_SYSTEM);//系统编号
		accountingSingle.setAcct_branch(acct_branch); // 账务机构
		accountingSingle.setAcct_type(E_ACCTTYPE.BASE_ACCOUNT); // 账户分类
		accountingSingle.setCcy_code(BaseEnumType.E_CRCYCD.RMB.toString()); // 货币代码
		accountingSingle.setDebit_credit(dto.getDebit_credit()); // 记账方向
		accountingSingle.setAccounting_amt(dto.getTrxn_amt()); // 记账金额
		//fap_accounting_event_parm accountingEventParm = Fap_accounting_event_parmDao.selectOne_odb1(FaConst.CORE_SYSTEM, dto.getAccounting_alias(),
		//		dto.getBal_attributes(), false);
		//if(CommUtil.isNull(accountingEventParm)){
		//	throw GlError.GL.E0067(FaConst.CORE_SYSTEM, dto.getAccounting_alias(), dto.getBal_attributes());
		//}
		accountingSingle.setGl_code(dto.getGl_code());
		//账户序号不传,设置默认
		accountingSingle.setAcct_seq("00001");
		accountingSingle.setRemark("bookAccountingSeq"); // 备注
		
		return accountingSingle;
	}
}


/**
 INSERT INTO KSYS_JYZKZQ (XITONGBS, FARENDMA, PLJYZBSH, ZUBIEZWM, RWYXXKTJ, RWYXHDFW)
VALUES ('420', '999', '1200', '分户账余额初始化', NULL, NULL);

INSERT INTO KSYS_JYKZHQ (XITONGBS, FARENDMA, PLJYZBSH, BUZHOUHA, PLJIOYMA, PLJYZWMC, ZHIXBZHI, YLPLJYLB, JYYXXKTJ, CHLCISHU, SHBZHDBZ, SHIWTJJG, SHUJCFMS, SHUJCFZJ, PLZYZXMS, ZUIDZYBF, RIZHIJIB, ZUOYCFTJ, PLJYYXMS, NGFTIAOG, SFWENJPL, JIAOYILX)
VALUES ('420', '999', '1200', 10, 'init01', '分户账余额初始化', '1', NULL, NULL, 0, '1', 1, '9', NULL, '1', 0, 3, NULL, '0', '0', '0', NULL);

insert into ksys_plrenw (XITONGBS, FARENDMA, PLJYPICH, PLRWZXPC, PLJYTJRQ, JIAOYIRQ, DQJIOYRQ, PLJYLCBS, LIUCBUZH, PLJYZBSH, PILJYBSS, LJHAOSHI, JIAOYZHT, PLZXMOSH, PLRWZDBZ, PLRWTJSJ, PLRWUYXJ, JYKSSHIJ, KSHISHJC, JYJSSHIJ, JSHISHJC, XUNIJIBS, IPDIZHII, ZHUJIMIC, SHUJUQUU, QSLCBZHA, QSZXXHAO, QSPLJYZU, QSBUZHOU, CUOWXINX, CUOWDUIZ, FUWBIAOZ, ZXTONGBH)
values ('420', '999', 'InitBatch.20180922888888', '1537147814736', '20180922', to_date('22-09-2018', 'dd-mm-yyyy'), '20180922', null, 0, '1200', null, 2295, 'failure', '3', null, null, 5, '2018-09-17 09:30:14:732', 1537147814732, '2018-09-17 09:30:15:883', 1537147815883, null, null, null, '{"input":{},"sys":{"groupId":"1200","prcscd":"init01","pljypich":"InitBatch.20180922888888"},"comm_req":{"timerName":"科目账余额初始化","busi_branch_id":"898118","corpno":"999","initiator_system":"999","sponsor_system":"999","busi_teller_id":"9993001","jiaoyirq":"20180922","busi_org_id":"999","caller_system":"999","channel_id":"999"}}', 0, 0, null, 0, null, null, '10.91.131.42#ubadmin#batGL01DEV', '42002');



INSERT INTO KNP_PARA (CORPNO, PARMCD, PMKEY1, PMKEY2, PMKEY3, PMVAL1, PMVAL2, PMVAL3, PMVAL4, PMVAL5, TMSTMP)
VALUES ('999', 'INIT_COMPLETE', '%', '%', '%', '0', NULL, NULL, NULL, '分户账余额初始化标示0-否 1-是', NULL);

INSERT INTO KSYS_PLRENW (XITONGBS, FARENDMA, PLJYPICH, PLRWZXPC, PLJYTJRQ, JIAOYIRQ, DQJIOYRQ, PLJYLCBS, LIUCBUZH, PLJYZBSH, PILJYBSS, LJHAOSHI, JIAOYZHT, PLZXMOSH, PLRWZDBZ, PLRWTJSJ, PLRWUYXJ, JYKSSHIJ, KSHISHJC, JYJSSHIJ, JSHISHJC, XUNIJIBS, IPDIZHII, ZHUJIMIC, SHUJUQUU, QSLCBZHA, QSZXXHAO, QSPLJYZU, QSBUZHOU, CUOWXINX, CUOWDUIZ, FUWBIAOZ, ZXTONGBH)
VALUES ('420', '999', 'gl_dayend_20180913_999', '1536387028888', '20180913', to_date('13-09-2018', 'dd-mm-yyyy'), '20180913', 'gl_dayend', 0, null, null, null, 'failure', '1', null, null, 5, null, null, null, null, null, null, null, '{"input":{"dcnbianh":null,"pljylcbs":"gl_dayend","rzglriqi":"20180913","farendma":"999"},"comm_req":{"farendma":"999","corpno":"999","tranbr":"898118","servno":"999","tranus":"9993001","jiaoyirq":"20180913"},"sys":{"jiaoyirq":"20180913"}}', 0, 0, null, 0, null, null, '10.91.131.7#ubadmin#batGL01DEV', '42002');

 * */

