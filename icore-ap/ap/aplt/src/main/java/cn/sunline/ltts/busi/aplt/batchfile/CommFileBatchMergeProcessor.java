package cn.sunline.ltts.busi.aplt.batchfile;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.BatchTaskContext;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.ListBatchDataWalker;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.metadata.mybatis.util.EdspDaoExceptionWrapUtil;
import cn.sunline.edsp.base.file.FileDataExecutor;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.file.FileUtil;
import cn.sunline.edsp.base.util.reflection.BeanUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplsubDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplsub;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * 通用文件合并处理
 * @author jizhirong
 * @param <T>
 *
 */
public class CommFileBatchMergeProcessor<B,H> implements FileBatchMerge<B,H>{
//	private final static BizLog BIZLOG = LogManager.getBizLog(CommFileBatchMergeProcessor.class);
	private kapb_wjplxxb wjplxxb;//初始化文件信息表
	private FileBatchTranTemplateProcessor batchFileTranProcess;//初始化文件模板处理
	private List<B> bodys= new ArrayList<>();
	private H header;
	public CommFileBatchMergeProcessor(kapb_wjplxxb wjplxxb){
		this.wjplxxb = wjplxxb;
		this.batchFileTranProcess = new FileBatchTranTemplateProcessor(BatchTaskContext.get().getCurrrentTranId());//文件模板映射处理
	}
	/**
	 * 获取待合并文件名与路径
	 * @return
	 */
	public String getMergeFileName(){
		return wjplxxb.getUpfeph()+wjplxxb.getUpfena();
	}
	
	/**
	 * 解析所有的子文件
	 * 生成文件头与文件体信息
	 */
	public H getHeader(String filedName){
		List<kapb_wjplsub> wjplsubs = Kapb_wjplsubDao.selectAll_odb5(wjplxxb.getBtchno(),E_BTFEST.WAIT_MERGE, false);
		long counts = 0L;
		for(kapb_wjplsub wjplsub:wjplsubs){
			FileUtil.readFile(wjplsub.getUpfeph()+wjplsub.getUpfena(), new FileDataExecutor() {
				@Override
				public void process(int index, String line) {
					if(CommUtil.isNotNull(line)){
						if(index != 1) {
							bodys.add((B)batchFileTranProcess.getBodyOfLine(line));
						}else{
							header = batchFileTranProcess.getHeadOfLine(line);
						}
					}
						
				}
			},"UTF-8");
			long totanm = wjplsub.getTotanm()==null?0L:wjplsub.getTotanm().longValue();
			counts += totanm;
		}
		BeanUtil.setProperty(header, filedName, counts);//文件头总数字段，重新赋值
		return header;
	}
	/**
	 * 获取文件体数据处理器
	 * @return
	 */
	public BatchDataWalker<B> getFileBodyDataWalker(){
		return new ListBatchDataWalker<B>(bodys); 
	}
	
	/**
	 * 合并成功处理
	 */
	public void afterWriteFileTranProcess(){
		mergeAfterProcess("交易处理成功",E_BTFEST.SUCC);
	}
	/**
	 * 合并失败处理
	 */
	public void writeFileTranExceptionProcess(Throwable t){
		final String error = "文件合并失败："+t.getMessage();
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				mergeAfterProcess(error,E_BTFEST.FAIL);
				return null;
			}
		});
//		throw LangUtil.wrapThrow(t);
		EdspDaoExceptionWrapUtil.daoExceptionWrap("文件合并失败", new Exception(t));
	}
	
	/**
	 * 登记处理结果
	 * @param msg
	 * @param btfest 
	 */
	private void mergeAfterProcess(String msg,E_BTFEST btfest) {
		final String filesq = wjplxxb.getBtchno();
		List<kapb_wjplsub> wjsubs = Kapb_wjplsubDao.selectAll_odb5(filesq,E_BTFEST.WAIT_MERGE, false);
		 long distnm = 0; //处理总笔数
		 long failnm = 0; //失败总笔数
		 long succnm = 0; //成功总笔数
		 long totanm = 0; //总笔数
		 for(kapb_wjplsub wjplsub:wjsubs){
			 distnm = distnm + wjplsub.getDistnm();
			 failnm = failnm + wjplsub.getFailnm();
			 succnm = succnm + wjplsub.getSuccnm();
			 totanm = totanm + wjplsub.getTotanm();
			 wjplsub.setBtfest(btfest);
			 Kapb_wjplsubDao.updateOne_odb1(wjplsub);
		 }
		
		 //7、更新文件信息表文件状态为交易成功
		 wjplxxb.setDistnm(distnm); 
		 wjplxxb.setFailnm(failnm);
		 wjplxxb.setSuccnm(succnm); 
		 wjplxxb.setTotanm(totanm);
		 wjplxxb.setBtfest(btfest);
		 wjplxxb.setErrotx(msg);
		 if(totanm != succnm){
			 wjplxxb.setBtfest(E_BTFEST.FAIL);
			 wjplxxb.setErrotx("成功数与总数不等"); 
		 }
		 Kapb_wjplxxbDao.updateOne_odb1(wjplxxb);
	}
}
