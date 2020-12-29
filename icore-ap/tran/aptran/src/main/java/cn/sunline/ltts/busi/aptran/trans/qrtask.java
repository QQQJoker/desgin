package cn.sunline.ltts.busi.aptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aptran.namedsql.TaskMainDao;
import cn.sunline.ltts.busi.aptran.type.TaskInfo.Task;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class qrtask {

	public static void queryTaskInfo(
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrtask.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrtask.Output output) {
		
		if(CommUtil.isNull(input.getTaskid())){
			if(CommUtil.isNull(input.getTktrdt())){
				throw ApError.Sys.E0001("任务运行日期不能为空。");
			}
			
			if(CommUtil.isNull(input.getTkgpid())){
				throw ApError.Sys.E0001("任务所属组号不能为空。");
			}
			
			if(CommUtil.isNull(input.getTkprcd())){
				throw ApError.Sys.E0001("批量交易码不能为空。");
			}
		}else{
			if(CommUtil.isNotNull(input.getTktrdt())){
				throw ApError.Sys.E0001("批次号已填写，任务运行日期不需要填写。");
			}
			
			if(CommUtil.isNotNull(input.getTkgpid())){
				throw ApError.Sys.E0001("批次号已填写，任务所属组号不需要填写。");
			}
			
			if(CommUtil.isNotNull(input.getTkprcd())){
				throw ApError.Sys.E0001("批次号已填写，批量交易码不需要填写。");
			}
		}
		
		List<Task> lstTasks = TaskMainDao.selTaskInfos(input.getTaskid(), input.getTktrdt(), input.getTkprcd(), input.getTkprcd(), false);
		
		output.getTkinfo().setLstask(new DefaultOptions<>(lstTasks));
	}
}
