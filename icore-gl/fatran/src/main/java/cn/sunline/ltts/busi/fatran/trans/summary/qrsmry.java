package cn.sunline.ltts.busi.fatran.trans.summary;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppSmry;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaParmDao;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;


public class qrsmry {

	/*
	 * 查询摘要码列表
	 */
	public static void querySmryList( final cn.sunline.ltts.busi.fatran.trans.summary.intf.Qrsmry.Input input,  final cn.sunline.ltts.busi.fatran.trans.summary.intf.Qrsmry.Output output){
		String corpno = CommToolsAplt.prcRunEnvs().getCorpno();
		List<AppSmry> selSmryList = FaParmDao.lstSmryList(input.getSmrycd(), input.getSmryds(),corpno, false);
		if(CommUtil.isNotNull(selSmryList) && selSmryList.size() > 0) {
			Options<AppSmry> list01 = new DefaultOptions<AppSmry>();
			for(AppSmry appSmry:selSmryList) {
				list01.add(appSmry);
			}
			output.setList01(list01);
		}
	}
}
