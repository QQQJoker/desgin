package cn.sunline.ltts.busi.aptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.core.exception.AdpDaoException;
import cn.sunline.adp.core.exception.AdpDaoNoDataFoundException;
import cn.sunline.adp.core.exception.AdpDaoTooManyRowsException;
import cn.sunline.ltts.busi.aptran.namedsql.StrikeSqlsDao;
import cn.sunline.ltts.busi.serv.type.TransInfo.MainTransInfo;
import cn.sunline.ltts.busi.sys.errors.InError;

public class tranif {

	public static void GetTransInfo(String transq,
			final cn.sunline.ltts.busi.aptran.trans.intf.Tranif.Output output) {
		if (CommUtil.isNull(transq)) {
			throw InError.comm.E0005("主交易流水");
		}
		MainTransInfo info = SysUtil.getInstance(MainTransInfo.class);
		try {
			info = StrikeSqlsDao.seltransInfoByTransq(transq, true);
		} catch (AdpDaoNoDataFoundException e) {
			throw InError.comm.E0003("主交易流水[" + transq + "]不存在");
		} catch (AdpDaoTooManyRowsException e) {
			throw InError.comm.E0003("查询交易信息出错，记录太多");
		} catch (AdpDaoException e) {
			throw InError.comm.E0003("查询交易信息出错，其他错误");
		}

		output.setTranus(info.getTranus());
		output.setTranam(info.getTranam());
		output.setTrancd(info.getTrancd());
		output.setTrantp(info.getTrantp());
		output.setPrcscd(info.getPrcscd());
		output.setTranna(info.getTranna());
		output.setTrandt(info.getTrandt());
		output.setTrantm(info.getTrantm());
		output.setTranac(info.getTranac());
		output.setTrandt(info.getTrandt());
	}
}
