package cn.sunline.ltts.busi.aptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppRule;
import cn.sunline.ltts.busi.aplt.tables.SysPropTable.AppRuleDao;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aptran.type.AppPropRule.AppRuleOut;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class qraprl {

	public static void qraprl(
			final cn.sunline.ltts.busi.aptran.trans.intf.Qraprl.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Qraprl.Output output) {

		if (CommUtil.isNull(input.getGrupcd())) {
			throw ApError.Aplt.E0000("输入规则分组代码不可为空");
		}

		List<AppRule> apprules = AppRuleDao.selectAll_odb3(input.getGrupcd(),
				false);

		if (CommUtil.isNull(apprules)) {
			throw ApError.Aplt.E0000("此规则分组无规则记录");
		} else {

			output.setGrupcd(input.getGrupcd());

			for (AppRule appRule : apprules) {

				AppRuleOut AppRule = SysUtil.getInstance(AppRuleOut.class);

				AppRule.setContxt(appRule.getContxt());
				AppRule.setGrupcd(appRule.getGrupcd());
				AppRule.setRulecd(appRule.getRulecd());
				AppRule.setRulesq(appRule.getRulesq());
				AppRule.setRuletx(appRule.getRuletx());

				output.getArules().getAprule().add(AppRule);
			}
            long count = apprules.size();
            CommToolsAplt.prcRunEnvs().setCounts(count);

		}
	}

}
