package cn.sunline.ltts.busi.fatran.trans.summary;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppSmry;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppSmryDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;


public class insmry {

	public static void insertAppSmry( final cn.sunline.ltts.busi.fatran.trans.summary.intf.Insmry.Input input,  final cn.sunline.ltts.busi.fatran.trans.summary.intf.Insmry.Output output){
		String corpno = CommTools.prcRunEnvs().getCorpno();
		AppSmry smry = SysUtil.getInstance(AppSmry.class);
		smry.setCorpno(corpno);
		smry.setSmrycd(input.getSmrycd());
		smry.setSmryds(input.getSmryds());
		smry.setRemark(input.getRemark());
		AppSmryDao.insert(smry);
	}
}
