package cn.sunline.ltts.busi.fa.serviceimpl;

import cn.sunline.ltts.busi.fa.account.FaCloseAccount;
import cn.sunline.ltts.busi.fa.account.FaModifyAccount;
import cn.sunline.ltts.busi.fa.account.FaOpenAccount;
import cn.sunline.ltts.busi.fa.account.FaQueryAccount;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;

/**
 * 总账账户维护
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "SrvFaAccountImpl", longname = "总账账户维护", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SrvFaAccountImpl implements cn.sunline.ltts.busi.fatran.servicetype.SrvFaAccount {
	/**
	 * 总账开户
	 * 
	 */
	public cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo openAccount(final cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo openAccountIn) {
		return FaOpenAccount.openAccount(openAccountIn);
	}

	@Override
	public FaAcctInfo queryAccount(String acct_no) {

		return FaQueryAccount.queryAccount(acct_no);

	}

	public cn.sunline.edsp.base.lang.Options<cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo> queryAccountList(final cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo queryIn) {

		return FaQueryAccount.queryAccountList(queryIn);
	}

	public cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo closeAccount(String acct_no, Long recdver) {
		return FaCloseAccount.closeAccount(acct_no, recdver);
	}

	public cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo modifyAcct(final cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo modifyIn) {
		return FaModifyAccount.modifyAcct(modifyIn);
	}

}
