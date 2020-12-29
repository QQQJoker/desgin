package cn.sunline.ltts.busi.aptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aptran.namedsql.AppPropDao;
import cn.sunline.ltts.busi.aptran.type.AppPropRule.AppPropCtrlApplOut;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class qrctal {

	public static void qrctal(
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrctal.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrctal.Output output) {

		if (CommUtil.isNull(input.getBtchno())) {
			throw ApError.Aplt.E0000("批次号不能为空");
		}
		if(CommUtil.isNull(input.getPageno())){
			throw ApError.Aplt.E0000("当前页数");
		}
		if(CommUtil.isNull(input.getPagect())){
			throw ApError.Aplt.E0000("每页记录数");
		}
		long pageno = input.getPageno();
		long pagesize = input.getPagect();

	//	List<AppPropCtrlAppl> appPropCtrlApplList = AppPropCtrlApplDao.selectAll_odb3(input.getBtchno(), false);

		List<AppPropCtrlApplOut> appPropCtrlApplList=AppPropDao.GetPropCtrlAppl(CommTools.getTranCorpno(), input.getBtchno(),input.getDatatp(), input.getOptype(),(pageno - 1)*pagesize, pagesize, false);
			
		if (CommUtil.isNull(appPropCtrlApplList)) {

			throw ApError.Aplt.E0000("该批次无属性控制相关申请");

		} else {
		
			for (AppPropCtrlApplOut appPropAppl : appPropCtrlApplList) {
				
				output.getApctal().add(appPropAppl);
				/*AppPropCtrlApplOut appRuleApplOut = SysUtil
						.getInstance(AppPropCtrlApplOut.class);
				appRuleApplOut.setBtchno(appPropAppl.getBtchno());
				appRuleApplOut.setChckdt(appPropAppl.getChckdt());
				appRuleApplOut.setChcksq(appPropAppl.getChcksq());
				appRuleApplOut.setChckus(appPropAppl.getChckus());
				appRuleApplOut.setDatatp(appPropAppl.getDatatp());
				appRuleApplOut.setEnmuid(appPropAppl.getEnmuid());
				appRuleApplOut.setFildcd(appPropAppl.getFildcd());
				appRuleApplOut.setFildcv(appPropAppl.getFildcv());
				appRuleApplOut.setFildmu(appPropAppl.getFildmu());
				appRuleApplOut.setFildnm(appPropAppl.getFildnm());
				appRuleApplOut.setFildtp(appPropAppl.getFildtp());
				appRuleApplOut.setFildtx(appPropAppl.getFildtx());
				appRuleApplOut.setFildvl(appPropAppl.getFildvl());
				appRuleApplOut.setFinlfg(appPropAppl.getFinlfg());
				appRuleApplOut.setNullab(appPropAppl.getNullab());
				appRuleApplOut.setNullfg(appPropAppl.getNullfg());
				appRuleApplOut.setOptype(appPropAppl.getOptype());
				appRuleApplOut.setProdcd(appPropAppl.getProdcd());
				appRuleApplOut.setTablcd(appPropAppl.getTablcd());
				appRuleApplOut.setTempcd(appPropAppl.getTempcd());
				appRuleApplOut.setTrandt(appPropAppl.getTrandt());
				appRuleApplOut.setTransq(appPropAppl.getTransq());

				output.getApctal().add(appRuleApplOut);*/
			}
            long count = appPropCtrlApplList.size();
            CommToolsAplt.prcRunEnvs().setCounts(count);
		}
	}
}
