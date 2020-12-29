package cn.sunline.ltts.busi.aplt.batchfile;

import cn.sunline.adp.cedar.service.router.drs.util.RouteUtil;

public interface FileBatchSqlit {
	
	public String downloadFile();
	
	public void headerProcess(Long totanm);
	
	public <T> void bodyProcess(RouteUtil.BizKeyType type ,String drsFiled,T body);
	
	public <T> void afterReadFileTranProcess(T header,String filedName);
	
	public void readFileTranExceptionProcess(Throwable t);
}
