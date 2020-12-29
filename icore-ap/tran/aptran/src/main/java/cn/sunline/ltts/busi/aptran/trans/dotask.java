package cn.sunline.ltts.busi.aptran.trans;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;

public class dotask {

	public static void registTask(
			final cn.sunline.ltts.busi.aptran.trans.intf.Dotask.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Dotask.Output output) {
		
		String taskId = CommUtil.nvl(input.getTaskid(), BatchUtil.getTaskId());// 批次号
		String trandt = CommToolsAplt.prcRunEnvs().getTrandt();
		// Map<String, Object> dataArea = new HashMap<String, Object>();// 数据区
		// dataArea.put(BatchConfigConstant.BATCH_TRAN_DATE, trandt);
		DataArea dataArea = DataArea.buildWithEmpty();
		dataArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE,
				trandt);
		BatchUtil.submitAndRunBatchTran(taskId, input.getTkgpid(),
				input.getTkprcd(), dataArea);
		output.setTaskid(taskId);
	}
}
