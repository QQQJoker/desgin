package cn.sunline.ltts.busi.aplt.batchfile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.BatchTaskContext;
import cn.sunline.adp.cedar.service.router.drs.util.RouteUtil;
import cn.sunline.adp.metadata.mybatis.util.EdspDaoExceptionWrapUtil;
import cn.sunline.edsp.base.file.FileProcessor;
import cn.sunline.edsp.base.util.reflection.BeanUtil;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplsubDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplsub;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.FileBatchTools;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.DpError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * 通用文件拆分处理
 * @author jizhirong
 *
 */
public class CommFileBatchSplitProcessor implements FileBatchSqlit{
	private final static BizLog BIZLOG = LogManager.getBizLog(CommFileBatchSplitProcessor.class);
	private kapb_wjplxxb wjplxxb;//初始化文件信息表
	private Map<String,FileProcessor> writeTempFileMap = new HashMap<>();//DCN--文件体写文件流
	private FileBatchTranTemplateProcessor batchFileTranProcess;//初始化文件模板处理
	private Map<String,Long> countLineByDcn = new HashMap<>();//DCN---文件体行数
	
	
	public CommFileBatchSplitProcessor(kapb_wjplxxb wjplxxb){
		this.wjplxxb = wjplxxb;
		writeTempFileMap = FileBatchTools.getTempFileProcessors(wjplxxb);
		batchFileTranProcess = new FileBatchTranTemplateProcessor(BatchTaskContext.get().getCurrrentTranId());//文件模板映射处理
	}
	
	/**
	 * 获取待拆分文件路径
	 * @return
	 */
	public String getDownloadFilePath(){
		return wjplxxb.getDownph() + wjplxxb.getDownna();
	}
	
	/**
	 * 文件头处理后处理,变更文件批量信息表状态以及总处理数
	 */
	public void afterHeaderProcess(Long totanm){
		wjplxxb.setTotanm(totanm);
		wjplxxb.setBtfest(E_BTFEST.PARSESUCC);
		Kapb_wjplxxbDao.updateOne_odb1(wjplxxb);
	}
	
	/**
	 * 文件体处理，将待拆分文件体信息，写入对应的节点中
	 * @param body 待拆分文件体
	 * @param dcn 
	 */
	public <T> void bodyProcess(RouteUtil.BizKeyType type ,String drsFiled,T body){
		String content = batchFileTranProcess.getLineStrOfFileBody(body);//获取一行文件体
//		String dcn = getDcnno(type,drsFiled);//TODO update
		String dcn = null;
		if(CommUtil.isNotNull(content) && CommUtil.isNotNull(dcn)) {
			FileProcessor fp = writeTempFileMap.get(dcn);//DCN---写文件体流
			if(fp == null) {
				throw DpError.DeptComm.BNAS9966();
			}
			try {
				fp.write(content);
				long count= countLineByDcn.get(dcn)==null?0:countLineByDcn.get(dcn).longValue();
				count++;//统计节点对应文件体行数
				countLineByDcn.put(dcn, count);
			} catch (Exception e) {
				fp.close();
				BIZLOG.info("写文件异常", e);
				throw DpError.DeptComm.BNAS9966();
			}
		}
	}
	/**
	 * 文件拆分后处理，将拆分后的文件体与文件头拼接
	 * @param header 文件头
	 * @param filedName 文件头总记录总行数对应的字段名
	 */
	public <T> void afterBodyResolveProcess(T header,String filedName){
		splitAfterProcessors(header,filedName);
	}
	
	/**
	 * 拆分交易异常处理
	 */
	public void readFileTranExceptionProcess(Throwable t){
		for (String dcnno:writeTempFileMap.keySet()){
			if(writeTempFileMap.get(dcnno) != null){
				writeTempFileMap.get(dcnno).close();
			}
			String splitPath = wjplxxb.getDownph()+"split"+File.separator;//默认的文件拆分路径
			String tempDownna = dcnno+"_temp_"+wjplxxb.getDownna();
			File sfile = new File(splitPath+tempDownna);//删除临时文件
			sfile.delete();
		}
		FileBatchTools.updateWjplxxbToFail(wjplxxb,t);
//		throw LangUtil.wrapThrow(t);
		EdspDaoExceptionWrapUtil.daoExceptionWrap("拆分交易异常", new Exception(t));
	}
	
	/**
	 * 拆分文件后处理
	 * 1.文件体，文件头合并，生成最终拆分文件
	 * 2.登记批量文件子信息表
	 * @param header 文件头
	 * @param filedName 文件头总记录总行数对应的字段名
	 */
	private <T> void splitAfterProcessors(T header,String filedName){
		
		String splitPath = wjplxxb.getDownph()+"split"+File.separator;//默认的文件拆分路径
		long spltTotanm = 0;
		for (String dcnno:writeTempFileMap.keySet()){
			long count = countLineByDcn.get(dcnno)==null?0:countLineByDcn.get(dcnno).longValue();
			if(count<=0){//如果节点未分配到文件，不处理
				continue;
			}
			spltTotanm += count;
			if(writeTempFileMap.get(dcnno) != null){
				writeTempFileMap.get(dcnno).close();
			}
			String downna = dcnno+"_"+wjplxxb.getDownna();
			String tempDownna = dcnno+"_temp_"+wjplxxb.getDownna();
			
			BeanUtil.setProperty(header, filedName, count);//文件总数字段，重新赋值
			String head = batchFileTranProcess.getLineStrOfFileHead(header);//生成文件头
			
			FileBatchTools.generateFile(head,splitPath+downna,splitPath+tempDownna);//合并文件头+文件体
			
			//登记批量文件子信息表
			kapb_wjplsub wjplsub = SysUtil.getInstance(kapb_wjplsub.class);
			CommUtil.copyProperties(wjplsub, wjplxxb);
//			String downm5 = "";
//			wjplsub.setDownm5(downm5);
			wjplsub.setTdcnno(dcnno);
			wjplsub.setDownph(splitPath);
			wjplsub.setDownna(downna);
			wjplsub.setSubbno(wjplxxb.getBtchno() + "_" + dcnno); // 子批次号
			JSONObject obj = JSON.parseObject(wjplsub.getFiletx());
			obj.getString(ApBatchFileParams.BATCH_PMS_FILESQ);
			obj.put(ApBatchFileParams.BATCH_PMS_FILESQ, wjplxxb.getBtchno() + "_" + dcnno);
			String filetx = JSON.toJSONString(obj);
			wjplsub.setUpfena("");// 由各个零售节点返回文件名字
			wjplsub.setUpfeph(splitPath);  //拆分后的文件路径
			wjplsub.setFiletx(filetx);
			wjplsub.setTotanm(countLineByDcn.get(dcnno));
			wjplsub.setBtfest(E_BTFEST.WAIT_DISTRIBUTE); //拆分完成，更新状态为待下发，由定时任务完成扫描下发。
			Kapb_wjplsubDao.insert(wjplsub);
		}
		long totanm = wjplxxb.getTotanm().longValue();
		if(totanm != spltTotanm){
			//文件总记录数，与拆分后文件总记录数不等，报错
			ApError.Aplt.E0421(totanm, spltTotanm);
		}
		clear();
	}
	/**
	 * 根据路由字段类型与路由字段找到对应的DCN，如果不存在，则按HASH分配
	 * @param type 路由字段类型
	 * @param drsFiled 路由字段
	 * @return DCN
	 */
	/*private String getDcnno(RouteUtil.BizKeyType type,String drsFiled){
		TargetInfo ret = null;
		try {
			ret = CustomDRSUtil.findDcnNoByRouterKey(drsFiled,type);
		} catch (Exception e) {
			
		}
		String dcn = "";
		if(CommUtil.isNull(ret)) {
			dcn = DcnUtil.getRDcnNosByHash(drsFiled);
		}else{
			dcn = ret.getDcnNo();
		}
		return dcn;
	}*/
	private void clear(){
		writeTempFileMap.clear();
		countLineByDcn.clear();
	}

	@Override
	public String downloadFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void headerProcess(Long totanm) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void afterReadFileTranProcess(T header, String filedName) {
		// TODO Auto-generated method stub
		
	}
}
