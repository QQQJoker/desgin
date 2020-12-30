package cn.sunline.ltts.busi.fa.account;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.Faa_accountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTSTATUS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
import cn.sunline.ltts.sys.dict.GlDict;

/**
 * <p>
 * 文件功能说明：维护总账账户
 * </p>
 * 
 * @Author Administrator
 *         <p>
 *         <li>2017年2月27日-下午4:36:52</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年2月27日-Administrator：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaModifyAccount {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaModifyAccount.class);

	public static FaAcctInfo modifyAcct(FaAcctInfo modifyIn) {

		bizlog.method("FaModifyAccount.modifyAcct  begin>>>>>>>>>>>>>>>>>>");
		bizlog.debug("modifyIn[%s]", modifyIn);
		FaAcctInfo modifyOut = SysUtil.getInstance(FaAcctInfo.class);
		// 判断输入不能为空
		CommTools.fieldNotNull(modifyIn.getAcct_no(), GlDict.A.acct_no.getId(), GlDict.A.acct_no.getLongName());
		CommTools.fieldNotNull(modifyIn.getRecdver(), BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 取出原有数据
		faa_account oldAcctTable = Faa_accountDao.selectOne_odb1(modifyIn.getAcct_no(), false);

		if (oldAcctTable == null) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(faa_account.class).getLongname(), GlDict.A.acct_no.getLongName(), modifyIn.getAcct_no());
		}

		// 将原纪录赋值给新表，对新表进行操作
		faa_account newAcctTable = CommTools.clone(faa_account.class, oldAcctTable);// 克隆OldObject，防止公共字段丢失

		// 对比版本号 rambo delete
		/*if (CommUtil.compare(modifyIn.getRecdver(), newAcctTable.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(faa_account.class).getName());
		}*/

		// String system = ApDropList.getText(FaConst.SYS_NO,
		// newAcctTable.getSys_no());

		// 外系统账户不允许修改
		if (CommUtil.compare(FaConst.GL_SYSTEM, newAcctTable.getSys_no()) != 0) {
			throw GlError.GL.E0024(newAcctTable.getSys_no(), newAcctTable.getAcct_no());
		}
		if (newAcctTable.getAcct_status() == E_ACCTSTATUS.CLOSE) {
			// 账户[${acctNo}]已销户，不能进行维护
			throw GlError.GL.E0025(modifyIn.getAcct_no());
		}

		unEnableModify(modifyIn, newAcctTable);

		if (CommUtil.isNotNull(modifyIn.getAcct_name())) {
			newAcctTable.setAcct_name(modifyIn.getAcct_name());
		}
		if (CommUtil.isNotNull(modifyIn.getDebit_manual_allow())) {
			newAcctTable.setDebit_manual_allow(modifyIn.getDebit_manual_allow());
		}
		if (CommUtil.isNotNull(modifyIn.getCredit_manual_allow())) {
			newAcctTable.setCredit_manual_allow(modifyIn.getCredit_manual_allow());
		}
		if (CommUtil.isNotNull(modifyIn.getRemark())) {
			newAcctTable.setRemark(modifyIn.getRemark());
		}
		// 先登记审计
		int i = ApDataAudit.regLogOnUpdateBusiness(oldAcctTable, newAcctTable);
		if (i == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(faa_account.class).getLongname());
		}

		Faa_accountDao.updateOne_odb1(newAcctTable);
		// 登记审计
	    ApDataAudit.regLogOnUpdateParameter(oldAcctTable,newAcctTable);

		FaOpenAccount.getAcctCom(modifyOut, newAcctTable);

		bizlog.debug("modifyOut[%s]", modifyOut);
		bizlog.method("FaModifyAccount.modifyAcct end>>>>>>>>>>>>>>>>>>");

		return modifyOut;
	}

	/**
	 * @Author Administrator
	 *         <p>
	 *         <li>2017年2月27日-下午3:09:59</li>
	 *         <li>判断不允许修改字段</li>
	 *         </p>
	 * @param modifyIn
	 * @param newAcctTable
	 */
	private static void unEnableModify(FaAcctInfo modifyIn, faa_account newAcctTable) {
		if (CommUtil.compare(modifyIn.getSys_no(), newAcctTable.getSys_no()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023(GlDict.A.sys_no.getLongName(), newAcctTable.getSys_no());

		}
		if (CommUtil.compare(modifyIn.getAcct_branch(), newAcctTable.getAcct_branch()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023( GlDict.A.acct_branch.getLongName(), newAcctTable.getAcct_branch());

		}
		if (CommUtil.compare(modifyIn.getGl_code(), newAcctTable.getGl_code()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023( GlDict.A.gl_code.getLongName(), newAcctTable.getGl_code());

		}

		if (CommUtil.compare(modifyIn.getCcy_code(), newAcctTable.getCcy_code()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023( GlDict.A.ccy_code.getLongName(), newAcctTable.getCcy_code());

		}
		if (CommUtil.compare(modifyIn.getAcct_seq(), newAcctTable.getAcct_seq()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023( GlDict.A.acct_seq.getLongName(), newAcctTable.getAcct_seq());

		}
		if (CommUtil.compare(modifyIn.getAcct_type(), newAcctTable.getAcct_type()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023( GlDict.A.acct_type.getLongName(), newAcctTable.getAcct_type().getLongName());

		}
		if (CommUtil.compare(modifyIn.getBal_direction(), newAcctTable.getBal_direction()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023( GlDict.A.bal_direction.getLongName(), newAcctTable.getBal_direction().getLongName());

		}
		if (CommUtil.compare(modifyIn.getBal_prop(), newAcctTable.getBal_prop()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023( GlDict.A.bal_prop.getLongName(), newAcctTable.getBal_prop().getLongName());

		}
		if (CommUtil.compare(modifyIn.getBal_check_ind(), newAcctTable.getBal_check_ind()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023( GlDict.A.bal_check_ind.getLongName(), newAcctTable.getBal_check_ind().getLongName());

		}
		if (CommUtil.compare(modifyIn.getOn_bal_sheet_ind(), newAcctTable.getOn_bal_sheet_ind()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023(GlDict.A.on_bal_sheet_ind.getLongName(), newAcctTable.getOn_bal_sheet_ind().getLongName());

		}
		if (CommUtil.compare(modifyIn.getAcct_status(), newAcctTable.getAcct_status()) != 0) {
			// 总账账户[${acctNo}] [${fieldName}]-[${fieldValue}]不允许维护。
			throw GlError.GL.E0023( GlDict.A.acct_status.getLongName(), newAcctTable.getAcct_status().getLongName());

		}
	}

}
