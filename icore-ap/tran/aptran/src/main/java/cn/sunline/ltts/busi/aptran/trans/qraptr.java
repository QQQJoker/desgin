package cn.sunline.ltts.busi.aptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppPropCtrl;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppPropCtrlDao;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aptran.type.AppPropRule.AppPropCtrlOut;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class qraptr {

	public static void qrctrl(
			final cn.sunline.ltts.busi.aptran.trans.intf.Qraptr.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Qraptr.Output output) {

		if (CommUtil.isNull(input.getProdcd())) {
			throw ApError.Aplt.E0000("输入产品代码不可为空");
		}

		if (CommUtil.isNull(input.getTablcd())) {
			throw ApError.Aplt.E0000("输入产品属性表不可为空");
		}

		List<AppPropCtrl> appPropCtrl = AppPropCtrlDao.selectAll_odb2(
				input.getProdcd(), input.getTablcd(), false);

		if (CommUtil.isNull(appPropCtrl)) {

			throw ApError.Aplt.E0000("查询的产品属性控制无记录");

		} else {

			for (AppPropCtrl propCtrl : appPropCtrl) {
				AppPropCtrlOut appPropCtrlOut = SysUtil
						.getInstance(AppPropCtrlOut.class);
				appPropCtrlOut.setProdcd(propCtrl.getProdcd());
				appPropCtrlOut.setTablcd(propCtrl.getTablcd());
				appPropCtrlOut.setFildcd(propCtrl.getFildcd());
				appPropCtrlOut.setFildnm(propCtrl.getFildnm());
				appPropCtrlOut.setFildtp(propCtrl.getFildtp());
				appPropCtrlOut.setFinlfg(propCtrl.getFinlfg());
				appPropCtrlOut.setNullfg(propCtrl.getNullfg());
				appPropCtrlOut.setFildvl(propCtrl.getFildvl());
				appPropCtrlOut.setFildmu(propCtrl.getFildmu());
				appPropCtrlOut.setFildcv(propCtrl.getFildcv());
				appPropCtrlOut.setFildtx(propCtrl.getFildtx());
				appPropCtrlOut.setNullab(propCtrl.getNullab());
				appPropCtrlOut.setEnmuid(propCtrl.getEnmuid());
				appPropCtrlOut.setTempcd(propCtrl.getTempcd());
				output.getAppcts().getApppct().add(appPropCtrlOut);

			}
            long count = appPropCtrl.size();
            CommToolsAplt.prcRunEnvs().setCounts(count);
		}
	}

}
