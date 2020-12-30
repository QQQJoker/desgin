package cn.sunline.ltts.busi.gl.settlement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.account.FaOpenAccount;
import cn.sunline.ltts.busi.fa.accounting.FaAccounting;
import cn.sunline.ltts.busi.fa.namedsql.FaSettleFlatDao;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_REVERSALSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SETTSTATE;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_TRXNSEQTYPE;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DEBITCREDIT;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.fa.util.FaTools;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaRegTellerSeq;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.FaSingleAccountingCheckIn;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.SettlePrepData;
import cn.sunline.ltts.gl.fa.type.ComFaAccounting.SettlePrepFlatData;

/**
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author ThinkPad
 *         <p>
 *         <li>2017年3月2日-下午7:07:43</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年3月2日-ThinkPad：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
/**
 * <p>
 * 文件功能说明：
 * 
 * </p>
 * 
 * @Author ThinkPad
 *         <p>
 *         <li>2017年3月4日-上午9:45:28</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年3月4日-ThinkPad：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class GlSettleFlat {

    private static final BizLog bizlog = BizLogUtil.getBizLog(GlSettleFlat.class);

    /**
     * @Author ThinkPad
     *         <p>
     *         <li>2017年3月4日-下午2:01:13</li>
     *         <li>功能说明：使用list的循环</li>
     *         </p>
     * @param orgId
     * @param trxnDate
     * @param settBatchNo
     */
    public static void prcMultiSettleFlat(String orgId, String trxnDate, String settBatchNo) {
        bizlog.method("prcSettlementFlat >>>>>>>>>>>>Begin>>>>>>>>>>>>");
        bizlog.parm("orgId [%s] trxnDate [%s],trxnSeq [%s]", orgId, trxnDate, settBatchNo);

        String settFlatFlag = FaTools.getSettFlatFlag();

        if (settFlatFlag == null)
            throw GlError.GL.E0077(); //系统清算补平标志参数设置错误 

        bizlog.parm("settFlatFlag [%s] ", settFlatFlag);
        if (CommUtil.compare(settFlatFlag, E_YESORNO.YES.getValue()) != 0
                && CommUtil.compare(settFlatFlag, E_YESORNO.NO.getValue()) != 0)
            throw GlError.GL.E0078(); //系统清算补平标志参数设置错误 

        String settFlatType = FaTools.getSettFlatType();
        if (settFlatType == null)
            throw GlError.GL.E0086(); //系统清算补平参数设置错误 

        bizlog.parm("settFlatType [%s]", settFlatType);
        if (CommUtil.compare(settFlatType, FaConst.SETT_FLAT_REAL) != 0
                && CommUtil.compare(settFlatType, FaConst.SETT_FLAT_SINGLE) != 0
                && CommUtil.compare(settFlatType, FaConst.SETT_FLAT_NETTING) != 0)
            throw GlError.GL.E0087(); //系统多级清算补平参数设置错误 

        if (CommUtil.compare(E_YESORNO.YES.getValue(), settFlatFlag) == 0 && CommUtil.compare(FaConst.SETT_FLAT_REAL, settFlatType) != 0) {
            /* 数据准备 */
            List<SettlePrepData> lstPrepData = FaSettleFlatDao.lstSettBatchNoList(orgId, trxnDate, settBatchNo, false);

            if (CommUtil.isNull(lstPrepData) && lstPrepData.size() > 0) {
                bizlog.parm("flag settle data is null，exis settle ");
                FaSettleFlatDao.updBatchSettled(trxnDate, orgId, settBatchNo);
                bizlog.method("prcMultiSettleFlat >>>>>>>>>>>>End>>>>>>>>>>>>");
                return;
            }

            List<SettlePrepFlatData> lstPrepFlatData = new ArrayList<SettlePrepFlatData>();
            if (CommUtil.compare(FaConst.SETT_FLAT_SINGLE, settFlatType) == 0) {
                //1批量逐笔补平
                lstPrepFlatData = FaSettleFlatDao.lstSettDataSingle(orgId, trxnDate, settBatchNo, false);

            }
            else if (CommUtil.compare(FaConst.SETT_FLAT_NETTING, settFlatType) == 0) {
                //批量扎差补平
                lstPrepFlatData = FaSettleFlatDao.lstSettDataNetting(orgId, trxnDate, settBatchNo, false);
            }
            if (CommUtil.isNull(lstPrepFlatData)){
                bizlog.parm("flag settle data is null，exis settle ");
                FaSettleFlatDao.updBatchSettled(trxnDate, orgId, settBatchNo);
                bizlog.method("prcMultiSettleFlat >>>>>>>>>>>>End>>>>>>>>>>>>");
                return;
            }

            bizlog.debug("批次清算数据:[%s]", lstPrepData);

            /* 执行批次清算补平 */
            
            prcSettleFlatList(orgId, trxnDate, settBatchNo, lstPrepFlatData);

            // 更新交易清算状态 
            FaSettleFlatDao.updBatchSettled(trxnDate, orgId, settBatchNo);

        }
        else if (CommUtil.compare(E_YESORNO.YES.getValue(), settFlatFlag) == 0 && CommUtil.compare(FaConst.SETT_FLAT_REAL, settFlatType) == 0) {
            bizlog.debug("SETT_FLAT_FLAG parm is Y，SETT_FLAT_TYPE parm is 0 flag real，");
            FaSettleFlatDao.updBatchSettled(trxnDate, orgId, settBatchNo);
        }

        bizlog.method("prcMultiSettleFlat >>>>>>>>>>>>End>>>>>>>>>>>>");
    }

    /**
     * @Author ThinkPad
     *         <p>
     *         <li>2017年3月4日-下午2:01:07</li>
     *         <li>功能说明：使用list的循环</li>
     *         </p>
     * @param orgId
     * @param trxnDate
     * @param settBatchNo
     * @param lstPrepData
     */
    private static void prcSettleFlatList(String orgId, String trxnDate, String settBatchNo, List<SettlePrepFlatData> lstPrepData) {

        bizlog.method("prcSettleFlatList >>>>>>>>>>>>Begin>>>>>>>>>>>>");

        bizlog.parm("orgId [%s],trxnDate [%s], settBatchNo [%s]", orgId, trxnDate, settBatchNo);
        bizlog.parm("lstPrepData---prcClearMutiple_BP-->[%s]", lstPrepData);

        //  按清算补平数据循环，通过补平科目做补平处理
        //if (CommUtil.isNotNull(lstPrepData) && lstPrepData.size() > 0) {
        if (!lstPrepData.isEmpty()) {
            // 取清算补平科目参数 
            String settFlatSubject = FaTools.getSettSubject();
            bizlog.parm("settFlatSubject-->[%s]", settFlatSubject);
            for (SettlePrepFlatData cplData : lstPrepData) {

                // 借贷发生额同时做补平处理
                prcSettleFlatAccounting(cplData.getCcy_code(), cplData.getDebit_amt(), cplData.getCredit_amt(), cplData.getAcct_branch(), settFlatSubject);

            }

        }
        bizlog.method("prcSettleFlatList <<<<<<<<<<<<End<<<<<<<<<<<<");
    }

    /**
     * @Author ThinkPad
     *         <p>
     *         <li>2017年3月4日-下午2:01:04</li>
     *         <li>功能说明：使用list的循环</li>
     *         </p>
     * @param ccyCode
     * @param debitAmt
     * @param creditAmt
     * @param acctBranch
     * @param settFlatSubject
     */
    private static void prcSettleFlatAccounting(String ccyCode, BigDecimal debitAmt, BigDecimal creditAmt, String branchId, String settFlatSubject) {
        bizlog.method("prcSettleFlatAccounting >>>>>>>>>>>>Begin>>>>>>>>>>>>");

        bizlog.parm("ccyCode [%s],debitAmt [%s],creditAmt [%s],branchId [%s],sJizhkemh [%s]",
                ccyCode, debitAmt, creditAmt, branchId, settFlatSubject);

        if (CommUtil.compare(BigDecimal.ZERO, debitAmt) == 0 && CommUtil.compare(BigDecimal.ZERO, creditAmt) == 0)
            return;

        String tellerSeq = FaAccounting.getTellerSeq();
        FaRegTellerSeq regTellerSeq = SysUtil.getInstance(FaRegTellerSeq.class);
        regTellerSeq.setSys_no(FaConst.GL_SYSTEM); // 系统编号
        regTellerSeq.setTrxn_seq_type(E_TRXNSEQTYPE.SYSTEM_ACCOUNTING); // 交易流水类型
        regTellerSeq.setBusi_ref_no(CommToolsAplt.prcRunEnvs().getTransq()); // 业务参考号
        regTellerSeq.setSett_status(E_SETTSTATE.NO_LIQUIDATION); // 清算
        regTellerSeq.setReversal_status(E_REVERSALSTATE.NONE); // 冲账状态
        regTellerSeq.setTrxn_seq(tellerSeq); // 交易流水

        FaAccounting.regTellerSeq(regTellerSeq);

        FaAcctInfo acctInfo;
        // 当前机构处理 
        E_DEBITCREDIT direction;

        // 取出账号信息
        acctInfo = FaOpenAccount.getAcctBySubject(FaConst.GL_SYSTEM, ccyCode, branchId, settFlatSubject);
        bizlog.debug("acctInfo[%s]", acctInfo);
        String acctNo = acctInfo.getAcct_no(); // 

        //单点清算时只记一记账
        FaSingleAccountingCheckIn accountingInfo = SysUtil.getInstance(FaSingleAccountingCheckIn.class);

        if (CommUtil.compare(BigDecimal.ZERO, debitAmt) != 0) {
            direction = CommUtil.compare(debitAmt, BigDecimal.ZERO) > 0 ? E_DEBITCREDIT.CREDIT : E_DEBITCREDIT.DEBIT;
            accountingInfo.setAcct_no(acctNo); // 账号
            accountingInfo.setDebit_credit(direction); // 记账方向
            accountingInfo.setAccounting_amt(debitAmt.abs()); // 记账金额
            accountingInfo.setSummary_code(""); // 摘要代码TODO
            accountingInfo.setRemark("branch settle flat"); // 备注
            FaAccounting.singleAccounting(accountingInfo, tellerSeq, 1L);
        }
        if (CommUtil.compare(BigDecimal.ZERO, creditAmt) != 0) {
            direction = CommUtil.compare(creditAmt, BigDecimal.ZERO) > 0 ? E_DEBITCREDIT.DEBIT : E_DEBITCREDIT.CREDIT;
            accountingInfo.setAcct_no(acctNo); // 账号
            accountingInfo.setDebit_credit(direction); // 记账方向
            accountingInfo.setAccounting_amt(creditAmt.abs()); // 记账金额
            accountingInfo.setSummary_code(""); // 摘要代码TODO
            accountingInfo.setRemark("branch settle flat"); // 备注
            FaAccounting.singleAccounting(accountingInfo, tellerSeq, 1L);
        }

        bizlog.method("prcSettleFlatAccounting <<<<<<<<<<<<End<<<<<<<<<<<<");
    }

}
