package cn.sunline.ltts.busi.aplt.batchfile;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;


public interface FileBatchMerge<B,H> {
	
	public String getMergeFileName();
	
	public H getHeader(String filedName);
	
	public BatchDataWalker<B> getFileBodyDataWalker();
	
	public void afterWriteFileTranProcess();
	
	public void writeFileTranExceptionProcess(Throwable t);
}
