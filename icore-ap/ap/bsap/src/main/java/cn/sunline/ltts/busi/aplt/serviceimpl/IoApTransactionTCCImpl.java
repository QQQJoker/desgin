package cn.sunline.ltts.busi.aplt.serviceimpl;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ap.iobus.type.IoApReverseType.IoApReverseIn;
import cn.sunline.ltts.busi.aplt.transaction.ApConfirm;
import cn.sunline.ltts.busi.aplt.transaction.ApStrike;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;

/**
 * 分布式TCC事务两阶段提交服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoApTransactionTCCImpl", longname = "分布式TCC事务两阶段提交服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoApTransactionTCCImpl implements cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC {
	/**
	 * 业务事务二次确认
	 * 
	 */
	public void confirm(final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.confirm.Input input,
			final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.confirm.Output output) {
		ApConfirm.confirm(input.getOrigdt(), input.getOrigsq());
	}

	/**
	 * 业务事务二次撤销 此服务有两种调用场景：交易处理当时失败平台调起和冲正交易直接调起，远程标记为false;
	 * 处理外调事件时，外调远程的此服务时，远程标记为true。
	 * 
	 */
	public void rollbackOSVC(final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.rollbackOSVC.Input input,
			final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.rollbackOSVC.Output output) {

		IoApReverseIn cplRvIn = SysUtil.getInstance(IoApReverseIn.class);
		cplRvIn.setOtradt(input.getOrigdt());
		cplRvIn.setOtrasq(input.getOrigsq());
		output.setRvrtcd(ApStrike.prcRollback8(cplRvIn, input.getOcalfg()));
	}

	/**
	 * 业务事务二次撤销 此服务有两种调用场景：交易处理当时失败平台调起和冲正交易直接调起，远程标记为false;
	 * 处理外调事件时，外调远程的此服务时，远程标记为true。
	 * 
	 */
	public void rollbackOTXN(final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.rollbackOTXN.Input input,
			final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.rollbackOTXN.Output output) {
		//注意：这个方法空实现，因为此服务必须配置为外调交易方式，不需要实现
//		IoApReverseIn cplRvIn = SysUtil.getInstance(IoApReverseIn.class);
//		cplRvIn.setOtradt(input.getOrigdt());
//		cplRvIn.setOtrasq(input.getOrigsq());
//		output.setRvrtcd(ApStrike.prcRollback8(cplRvIn, input.getOcalfg()));
	}

	/**
	 * 业务事务二次撤销前检查
	 * 
	 */
	public void checkBeforeCancel(
			final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.checkBeforeCancel.Input input,
			final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.checkBeforeCancel.Output output) {
		throw Aplt.E0000("暂不支持[业务事务二次撤销前检查]");
	}

	/**
	 * 超时确认
	 * 
	 */
	public void confirmTimeOut(
			final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.confirmTimeOut.Input input,
			final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.confirmTimeOut.Output output) {
		// if (JunitTestHelper.enableTest && !JunitTestHelper.canConfirm) {
		// throw Aplt.E0000("构件confirm出错测试");
		// }
		// kaps_jioybw entry =
		// Kaps_jioybwDao.selectOne_kaps_jioybw_xtdiayls_index(Input.getYxitdyls(),false);
		//
		// if (entry == null) {
		// DataArea dateArea = DataArea.buildWithEmpty();
		// MapListDataContext sysObj = dateArea.getSystem();
		// sysObj.put(PkgConfigConstants.NAME_ERORCD, SYS.confirm.F_E0002);
		// sysObj.put(PkgConfigConstants.NAME_ERORTX, "正在处理中");
		// sysObj.put(PkgConfigConstants.NAME_PCKGSQ, "00000000");
		// dateArea.setSystem(sysObj);
		// Output.setYfhbaow(JSONWrapper.get().format(dateArea));
		// } else {
		// Output.setYfhbaow(entry.getXyingbw1());
		// }

		throw Aplt.E0000("暂不支持");
	}

	/**
	 * 冲正失败回调
	 * 
	 */
	public void exceptionStrikeCallBack(
			final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.exceptionStrikeCallBack.Input input,
			final cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.exceptionStrikeCallBack.Output output) {
		//TODO 若需则补充，如浙江农信的冲正失败后要求挂账处理。
	}
}
