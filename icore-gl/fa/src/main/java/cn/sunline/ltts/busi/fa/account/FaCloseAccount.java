package cn.sunline.ltts.busi.fa.account;

import java.math.BigDecimal;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.Faa_accountDao;
import cn.sunline.ltts.busi.fa.tables.TabFaAccount.faa_account;
import cn.sunline.ltts.busi.fa.type.ComFaAccount.FaAcctInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTSTATUS;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_ACCTTYPE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.sys.dict.GlDict;

public class FaCloseAccount {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(FaCloseAccount.class);

	public static FaAcctInfo closeAccount(String acctNo, Long recdver) {
		BIZLOG.method("FaCloseAccount.closeAccount begin>>>>>>>>>>");
		BIZLOG.debug("acct_no[%s]", acctNo);

		FaAcctInfo closeOut = SysUtil.getInstance(FaAcctInfo.class);

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		// 判断输入不能为空
		CommTools.fieldNotNull(acctNo, GlDict.A.acct_no.getId(), GlDict.A.acct_no.getLongName());
		CommTools.fieldNotNull(recdver, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 取出原有数据
		faa_account oldAcctTable = Faa_accountDao.selectOne_odb1(acctNo, false);

		if (oldAcctTable == null) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(faa_account.class).getLongname(), GlDict.A.acct_no.getLongName(), acctNo);
		}

		// 将原纪录赋值给新表，对新表进行操作
		faa_account newAcctTable = CommTools.clone(faa_account.class, oldAcctTable);// 克隆OldObject，防止公共字段丢失
		// 已销户账户不允许再次销户
		if(oldAcctTable.getAcct_status()== E_ACCTSTATUS.CLOSE){
		    throw GlError.GL.E0208(oldAcctTable.getAcct_no());
		}
		// 对比版本号
		if (CommUtil.compare(recdver, newAcctTable.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(faa_account.class).getName());
		}

		// 账户余额必须为零
		if (CommUtil.compare(newAcctTable.getAcct_bal(), new BigDecimal(0)) != 0) {
			// 账户[${acctNo}]余额不为零，不能进行销户操作。
			throw GlError.GL.E0021(acctNo);
		}

		// 不可以对基准账户做关闭处理
		if (newAcctTable.getAcct_type() == E_ACCTTYPE.BASE_ACCOUNT) {
			// 账户[${acctNo}]为基准户，不可以手工销户
			throw GlError.GL.E0022(acctNo);
		}

		newAcctTable.setAcct_status(E_ACCTSTATUS.CLOSE);
		newAcctTable.setClose_acct_brch(runEnvs.getTranbr());
		newAcctTable.setClose_acct_date(runEnvs.getTrandt());
		newAcctTable.setClose_acct_seq(runEnvs.getTransq());
		newAcctTable.setClose_acct_user(runEnvs.getTranus());

		// 先登记审计
		int i = ApDataAudit.regLogOnUpdateBusiness(oldAcctTable, newAcctTable);
		if (i == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(faa_account.class).getLongname());
		}

		Faa_accountDao.updateOne_odb1(newAcctTable);

		FaOpenAccount.getAcctCom(closeOut, newAcctTable);

		BIZLOG.debug("closeOut[%s]", closeOut);
		BIZLOG.method("FaCloseAccount.closeAccount end>>>>>>>>>>");

		return closeOut;

	}
}
