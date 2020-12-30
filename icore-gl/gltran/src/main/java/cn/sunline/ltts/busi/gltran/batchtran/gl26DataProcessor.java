package cn.sunline.ltts.busi.gltran.batchtran;

import java.util.List;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApCurrency;
import cn.sunline.ltts.busi.gl.item.GlYearEndTransProfit;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;

/**
 * 利润划转
 */

public class gl26DataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.gltran.batchtran.intf.Gl26.Input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl26.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(gl26DataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.gltran.batchtran.intf.Gl26.Input input, cn.sunline.ltts.busi.gltran.batchtran.intf.Gl26.Property property) {
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String trxnDate = runEnvs.getTrandt();

		if (DateTools2.isLastDay("Y", trxnDate)) {

			List<ApCurrency> ccyList = CommTools.listApCurrency();
		
			List<KnpPara> sysList = ApKnpPara.listKnpPara(FaConst.SYS_NO, false);
			for (ApCurrency cplInfo : ccyList) {

				for (KnpPara sysNoList : sysList) {
					GlYearEndTransProfit.profitUp(cplInfo.getCrcycd(), sysNoList.getPmval1());
				}
			}

		}
		bizlog.method("gl25 process [%s] end>>>>>>>>>>>>");
	}
}
