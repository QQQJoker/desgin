package cn.sunline.ltts.busi.aptran.batchfile.datapro;

import java.io.File;
import java.util.Map;

import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.cedar.server.batch.file.SimpleWriteFileBatchDataProcessor;
import cn.sunline.ltts.busi.aplt.namedsql.ApSysBatchDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.FileTools.LttsFileWriter;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.edsp.base.lang.Params;

	 /**
	  * 交易信息表数仓抽数
	  *
	  */

public class  jioyxxWriteFileProcessor extends SimpleWriteFileBatchDataProcessor<cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input,cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property,cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.jioyxx.Header,cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.jioyxx.Body,cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.jioyxx.Foot,java.util.Map>{
	private kapb_wjplxxb filetab = CommTools.getInstance(kapb_wjplxxb.class);
	private String tblsrc = "kapp_jioyxx";// 表名
	KnpPara  tbl = CommTools.KnpParaQryByCorpno(tblsrc, "%", "%", "%", true);
	private String datatp = tbl.getPmval1();// 增全量标识
	private String trandt = CommToolsAplt.prcRunEnvs().getTrandt();// 交易日期
	private String lstdt  = CommToolsAplt.prcRunEnvs().getLstrdt();// 上次交易日期
	private String filename = tblsrc+"_" + "AAAAA_" + lstdt + "_" + datatp + "_NAS" + ".txt";
	private int totalRecords;//总记录数
	/**
	 * 获取生成文件的文件名(含路径)
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public String getFileName(cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property){
		KnpPara  tbl_knpPara = CommTools.getInstance(KnpPara.class); 
		tbl_knpPara = CommTools.KnpParaQryByCorpno("DATAPRO", datatp, "%", "%", true);
		//String filepath = tbl_knpPara.getPmval2();
		String filepath = tbl_knpPara.getPmval2()+File.separator+lstdt;//路径加上日期
		String pathname= filepath.concat(File.separator).concat(filename);
		String seqno = CommTools.getSequence("fileseq", 5);
		String filesq = trandt.concat(CommUtil.lpad(seqno, 12, "0"));
		//将文件路径信息插入文件批量信息表
		filetab.setBusseq(input.getBusseq());
		filetab.setBtfest(E_BTFEST.DOWNSUCC);
		filetab.setDownph(filepath);
	    filetab.setFiletp(E_FILETP.DATAPRO);
		filetab.setDownna(filename);
		filetab.setUpfeph(filepath);
		filetab.setBtchno(filesq);
		filetab.setUpfena(filename);
		Kapb_wjplxxbDao.insert(filetab);

		return pathname;
	}

	/**
	 * 返回文件头信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.jioyxx.Header getHeader(cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 基于游标的文件数据遍历器
	 * 返回文件体数据遍历器
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return 
	 * 注：写文件体支持并发查数据库和写文件，最后合并，所以如果需要有顺序的需自带排序功能
	 */
	public BatchDataWalker<java.util.Map> getFileBodyDataWalker(cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property){
		Params param = new Params();
		param.put("tblsrc", tblsrc);// 表名
		if(datatp.equals("add")){
			param.put("mtdate", lstdt);// 维护日期
		}
		
		return new CursorBatchDataWalker<Map>(ApSysBatchDao.namedsql_selTableInfo, param);
	}

	/**
	 * 写文件体的每条记录前提供回调处理
	 * 
	 * @param index 序号，从1开始
	 * @param body 文件体对象
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * 
	 */
	public void bodyProcess(int index, java.util.Map dateItem , cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.jioyxx.Body body, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property) {
		totalRecords= index;
	}
	
	
	/**
	 * 返回文件尾信息
	 * @param input 批量交易的输入接口
	 * @param property批量交易的属性接口
	 * @return
	 */
	public cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.jioyxx.Foot getFoot(cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property){
		//TODO
		return null;
	}
	
	/**
	 * 上传生成的批量文件
	 * @param input 批量交易的输入接口
	 * @param property 批量交易的属性接口
	 */
	public void uploadFile(cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property){
		//TODO
	}
	
	/**
	 * 写文件前处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void beforeWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property) {
		
	}
	
	/**
	 * 写文件头异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeHeaderExceptionProcess(String taskId, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 写文件体(单笔)异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param dataItem
	 * @param t
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property, java.util.Map dateItem,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 写文件体异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeBodyExceptionProcess(String taskId, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}
	
	/**
	 * 写文件尾异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFootExceptionProcess(String taskId, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

	/**
	 * 写文件交易结束后回调
	 * @param taskId
	 * @param input
	 * @param property
	 */
	public void afterWriteFileTranProcess(String taskId, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property){
		KnpPara  tbl_knpPara = CommTools.getInstance(KnpPara.class);
		tbl_knpPara = CommTools.KnpParaQryByCorpno("DATAPRO", datatp, "%", "%", true);
		   String filename = tblsrc+"_" + "AAAAA_" + lstdt + "_" + datatp + "_NAS" + ".txt";	
		    String filenm2 = tblsrc+"_" + "AAAAA_" + lstdt + "_" + datatp + "_NAS" + ".flg";   	  
		    String flpath = tbl_knpPara.getPmval2()+File.separator+lstdt;//路径加上日期		 
		    String pathname= flpath.concat(File.separator).concat(filename);
		    File f = new File(pathname);			
			final LttsFileWriter file = new LttsFileWriter(flpath, filenm2, "UTF-8");
			file.open();
			try{	
				String ret  =filename  +" "+f.length()  +" "+totalRecords;
				file.write(ret);
			}finally{
				file.close();
			}
	}
	
	/**
	 * 写文件交易异常处理回调
	 * @param taskId
	 * @param input
	 * @param property
	 * @param t
	 */
	public void writeFileTranExceptionProcess(String taskId, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Input input, cn.sunline.ltts.busi.aptran.batchfile.datapro.intf.Jioyxx.Property property,
			Throwable t) {
		throw ExceptionUtil.wrapThrow(t);
	}

}

