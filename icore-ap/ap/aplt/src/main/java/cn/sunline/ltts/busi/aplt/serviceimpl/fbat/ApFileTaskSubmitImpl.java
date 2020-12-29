package cn.sunline.ltts.busi.aplt.serviceimpl.fbat;

import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.ap.iobus.type.IoApBatchFileStruct;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BizParmInfo;
import cn.sunline.ltts.busi.aplt.servicetype.ApFileTask;
import cn.sunline.ltts.busi.aplt.servicetype.ApFileTask.FileBatchExecutTaskSubmit.Output;
import cn.sunline.ltts.busi.aplt.servicetype.ApFileTask.FileInteractiveReqByServer.Input;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplsubDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplsub;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.FileBatchTools;
import cn.sunline.ltts.busi.aplt.tools.FileTools;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatchReg;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FLBTST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
 /**
  * 文件批量提交服务
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class ApFileTaskSubmitImpl implements ApFileTask{

	//private static final BizLog bizlog = LogManager.getBizLog(ApFileTaskSubmitImpl.class);
	@Override
	public void fileBatchExecutTaskSubmit(String entrno, String btflmd,
			String wenjiabs, String md5maaaa, Options<BizParmInfo> canshulb,
			Output Output) {

		//TODO: 业务要素检查
	    
		// 提交批量交易组
		//String groupId = getTaskType(plwenjlx);
		
		//IoDpSrvOut ioDpSrv = SysUtil.getInstance(IoDpSrvOut.class);
		//String groupId = ioDpSrv.IoDpFileTypeBatchID(plwenjlx,E_DCNBIASH.ADCN).getPljyzbsh();
		String groupId = "";
		//bizlog.debug("groupId:"+groupId);      
	    // 登记“文件批量信息”表
	    TrxEnvs.RunEnvs runEnv = CommToolsAplt.prcRunEnvs();
	    
        IoApBatchFileStruct.IoApWjplrwxxInfo wjplrwxx = SysUtil.getInstance(IoApBatchFileStruct.IoApWjplrwxxInfo.class);
        wjplrwxx.setEntrno(entrno);
        wjplrwxx.setBtflmd(btflmd);
        wjplrwxx.setBttrno(groupId); //批量交易组编号
        wjplrwxx.setBtlach(runEnv.getServtp().getValue()); //渠道号
        wjplrwxx.setBtladt(runEnv.getTrandt()); //发起日期
        wjplrwxx.setTrandt(runEnv.getTrandt());
        wjplrwxx.setFlbtst(E_FLBTST.ZZPD);
       
        IoApFileBatchReg apFileBatchReg = SysUtil.getInstance(IoApFileBatchReg.class);
        apFileBatchReg.saveIoApWjplxx(wjplrwxx);
	    
		// 业务参数列表转换到数据库
        //IoDpSrvOut srvOut=SysUtil.getInstance(IoDpSrvOut.class);
        //Map<String,Object> yingzzhi=srvOut.IoDpBatchParmChg(plwenjlx);
        /*
        if (yingzzhi.isEmpty()){
        	dataArea = FileBatchTools.getBizParamInfo(canshulb);
        }else{
        	dataArea = FileBatchTools.getBizParamInfo(canshulb,yingzzhi);
        }
        */
//        dataArea = FileBatchTools.getBizParamInfo(canshulb);
//		dataArea.put(ApDict.Aplt.farendma.toString(), runEnv.getCorpno());  //法人代码
//		dataArea.put(ApDict.Aplt.jiaoyirq.toString(), runEnv.getTrandt());  //交易日期
//		dataArea.put(ApDict.Aplt.jiaoyigy.toString(), runEnv.getTranus());  //交易柜员
//		dataArea.put(ApDict.Aplt.jiaoyijg.toString(), runEnv.getTranbr());  //交易机构
		
		
		
//		dataArea.put(ApDict.Bat.wenjxxlb.toString(), FileBatchTools.getFileInfo(wenjiabs, md5maaaa)); //文件信息 
//		dataArea.put(ApDict.Aplt.qdaoleix.toString(), runEnv.getServtp()); //渠道号
//		dataArea.put(ApDict.Bat.weituoho.toString(), weituoho);  //委托号 
//		dataArea.put(ApDict.Bat.plwenjlx.toString(), plwenjlx); //批量文件类型
		
//		BatchTools.submitTask(weituoho, groupId, dataArea);
		
		Output.setErrocd(""); //错误代码
		Output.setErrotx(""); //失败原因
		Output.setSuccfg(BaseEnumType.E_YES___.YES); //成功标志
		Output.setEntrno(entrno); //委托编号
		Output.setBtflmd(btflmd); //文件类型
	}

    @Override
	public ApFileTask.QueryFileBatchStatus.Output queryFileBatchStatus(String entrno, String wenjanlx) {
    	ApFileTask.QueryFileBatchStatus.Output Output = SysUtil.getInstance(ApFileTask.QueryFileBatchStatus.Output.class);
		// 根据委托号查询文件批量信息
		IoApFileBatchReg apFileBatchReg = SysUtil.getInstance(IoApFileBatchReg.class);
		IoApBatchFileStruct.IoApWjplrwxxInfo wjplxx =  apFileBatchReg.selIoApWjplxx(entrno);
		
		if (wjplxx == null) 
			throw ApError.Aplt.E0000("委托号[" + entrno + "]的任务未提交。");
		
		Output.setBtflmd(wjplxx.getBtflmd()); //批量文件类型
		Output.setSuccnm(wjplxx.getSuccnm()); //成功笔数
		Output.setFailnm(wjplxx.getFailnm()); //失败笔数 
		Output.setEntrno(entrno); //委托号
		Output.setTotanm(wjplxx.getTotanm()); //成功笔数
		Output.setFlbtst(wjplxx.getFlbtst()); //文件批量状态
		
		if (wjplxx.getFlbtst() == E_FLBTST.CLCG) {
			Map<String, Object> wenjiaxx = FileBatchTools.fileToMap(wjplxx.getFiletx());
			Output.setMdcode((String) wenjiaxx.get(FileTools.MD5)); //MD5
			Output.setFileid((String) wenjiaxx.get(FileTools.FILE_ID)); //fileId
			
			
		//正在处理
		} else if (wjplxx.getFlbtst() == E_FLBTST.ZZCL) {
			List<IoApBatchFileStruct.IoApWjplrwxxInfo> wjplrwxxInfoList = apFileBatchReg.selIoApWjplrwxxLst(entrno);
			 
			long succnm = 0;
			long failnm = 0;
			
			IoApFileBatch fileBatch = SysUtil.getRemoteInstance(IoApFileBatch.class);
			
			 for (IoApBatchFileStruct.IoApWjplrwxxInfo wjplrwxx : wjplrwxxInfoList) {
				 IoApFileBatchType.FileBatchResultInfo dcnWjplrwxx = fileBatch.getStatus(wjplrwxx.getEntrno(), wjplrwxx.getCdcnno());
				 
				 succnm = succnm + dcnWjplrwxx.getSuccnm();
				 failnm = failnm + dcnWjplrwxx.getFailnm();
			 }
			 
			 Output.setSuccnm(succnm);
			 Output.setFailnm(failnm);
			 Output.setTotanm(succnm + failnm);
		
		}
		
		return Output;
	}

	@Override
	public void FileInteractiveReqByServer(
			Input input,
			cn.sunline.ltts.busi.aplt.servicetype.ApFileTask.FileInteractiveReqByServer.Output output) {
		// TODO Auto-generated method stub
		
	}
	/**
     * 零售节点文件批量执行失败后变更主节点状态
     */
	public void updateKapbPlwjxxb( final cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb wplxxb){
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>(){
			@Override
			public Void execute() {
				//更新子表
				kapb_wjplsub wjplsub = Kapb_wjplsubDao.selectOneWithLock_odb1(wplxxb.getBtchno(), true);
				wjplsub.setErrotx(wplxxb.getErrotx());
				wjplsub.setBtfest(wplxxb.getBtfest());
				wjplsub.setSuccnm(wplxxb.getSuccnm());
				wjplsub.setDistnm(wplxxb.getDistnm());
				wjplsub.setFailnm(wplxxb.getFailnm());
				Kapb_wjplsubDao.updateOne_odb1(wjplsub);
				
//				ApBatchResultNumber apBatchResultNumber = ApSysBatchDao.countBatchResultByKapbWjplsub(null, wjplsub.getBtchno(), false);
				kapb_wjplxxb tbwjplxxb= Kapb_wjplxxbDao.selectOneWithLock_odb1(wjplsub.getBtchno(), true);
				tbwjplxxb.setErrotx(tbwjplxxb.getErrotx()+wjplsub.getTdcnno()+":"+wplxxb.getErrotx()+";");
				tbwjplxxb.setBtfest(wplxxb.getBtfest());
				//更新文件信息表
				tbwjplxxb.setSuccnm(tbwjplxxb.getSuccnm()+wplxxb.getSuccnm());
				//				tbwjplxxb.setDistnm(tbwjplxxb.getDistnm()+wplxxb.getDistnm());
				tbwjplxxb.setFailnm(tbwjplxxb.getFailnm()+wplxxb.getFailnm());
				/*if(CommUtil.isNotNull(apBatchResultNumber)){
				}*/
				Kapb_wjplxxbDao.updateOne_odb1(tbwjplxxb);
				return null;
			}
		});
	}

}

