package cn.sunline.ltts.busi.aptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aptran.namedsql.AppPropDao;
import cn.sunline.ltts.busi.aptran.type.AppPropRule.AppRuleApplOut;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class qrappl {

	public static void qrappl(
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrappl.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrappl.Output output) {

		if(CommUtil.isNull(input.getBtchno())){
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
		//List<AppRuleAppl> appRuleApplList=AppRuleApplDao.selectAll_odb4(input.getBtchno(), false);
		
		List<AppRuleApplOut> appRuleApplList=AppPropDao.GetRuleApplData(CommTools.getTranCorpno(), input.getBtchno(), input.getDatatp(),input.getOptype(), (pageno - 1)*pagesize, pagesize, false);
				
		if(CommUtil.isNull(appRuleApplList)){
			
			throw ApError.Aplt.E0000("该批次无规则申请");
			
		}else{
										
			for(AppRuleApplOut appRuleAppl:appRuleApplList){			
				/*AppRuleApplOut appRuleApplOut=SysUtil.getInstance(AppRuleApplOut.class);
				appRuleApplOut.setBtchno(appRuleAppl.getBtchno());
				appRuleApplOut.setChckdt(appRuleAppl.getChckdt());			
				appRuleApplOut.setChcksq(appRuleAppl.getChcksq());		
				appRuleApplOut.setChckus(appRuleAppl.getChckus());
				appRuleApplOut.setContxt(appRuleAppl.getContxt());
				appRuleApplOut.setDatatp(appRuleAppl.getDatatp());
				appRuleApplOut.setGrupcd(appRuleAppl.getGrupcd());
				appRuleApplOut.setOptype(appRuleAppl.getOptype());
				appRuleApplOut.setRulecd(appRuleAppl.getRulecd());
				appRuleApplOut.setRulesq(appRuleAppl.getRulesq());
				appRuleApplOut.setRuletx(appRuleAppl.getRuletx());
				appRuleApplOut.setTrandt(appRuleAppl.getTrandt());
				appRuleApplOut.setTransq(appRuleAppl.getTransq());
				
				output.getApappl().add(appRuleApplOut);*/
				output.getApappl().add(appRuleAppl);
				
			}
			long count = appRuleApplList.size();
			CommToolsAplt.prcRunEnvs().setCounts(count);
		}	
	}
}
