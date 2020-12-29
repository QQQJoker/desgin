package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.aplt.coderule.ApDayendPoint;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_DYEDCT;

/**
 * 核心系统日终结束
 */

public class ap30DataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Ap30.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap30.Property> {

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Ap30.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Ap30.Property property) {

		// 登记日终控制点
		ApDayendPoint.register(CommToolsAplt.prcRunEnvs().getTrandt(), E_DYEDCT.OVER);
	}

}
