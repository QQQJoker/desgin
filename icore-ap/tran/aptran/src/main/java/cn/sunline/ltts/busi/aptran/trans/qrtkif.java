package cn.sunline.ltts.busi.aptran.trans;

import java.util.List;

import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aptran.namedsql.TaskMainDao;
import cn.sunline.ltts.busi.aptran.type.TaskInfo.BatchTranDefine;

public class qrtkif {

	public static void queryBatchTranDefine(
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrtkif.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrtkif.Output output) {
		
		List<BatchTranDefine> lstBatchTrans = TaskMainDao.selBatchTransDefine(
				input.getTkgpid(), null, input.getTkprcd(), false);
		output.getBttrif().setLsbttr(
				new DefaultOptions<BatchTranDefine>(lstBatchTrans));
	}
}
