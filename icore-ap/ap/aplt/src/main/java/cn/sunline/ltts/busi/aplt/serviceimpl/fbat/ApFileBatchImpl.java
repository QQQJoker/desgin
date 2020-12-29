package cn.sunline.ltts.busi.aplt.serviceimpl.fbat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.ap.iobus.type.IoApBatchFileStruct;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.targetList;
import cn.sunline.ltts.busi.aplt.coderule.ApDCN;
import cn.sunline.ltts.busi.aplt.namedsql.ApltTabDao;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.para.ApBatchFilePath;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_bachDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_confDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_conf_detlDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_bach;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_conf;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_conf_detl;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatch;
import cn.sunline.ltts.busi.iobus.servicetype.ap.fbat.IoApFileBatchReg;
import cn.sunline.ltts.busi.sys.dict.ApDict;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FLBTST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

import com.alibaba.fastjson.JSON;
 /**
  * 通用文件批量业务服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class ApFileBatchImpl implements IoApFileBatch{
	
	private static final SysLog log = SysLogUtil.getSysLog(ApFileBatchImpl.class);
	private static final BizLog bizlog = BizLogUtil.getBizLog(ApFileBatchImpl.class);
 /**
  * DCN文件批量状态查询
  *
  */
	@Override
	public IoApFileBatchType.FileBatchResultInfo getStatus( String weituoho, String dcn_num){
		log.info("当前DCN编号为[%s],需要查询DCN编号为[%s]的文件批量状态。", ApDCN.getMyDcnNo(), dcn_num);
	    // 2014.12.23 R/C-DCN登记DCN有子文件批量信息
		IoApFileBatchType.FileBatchResultInfo result = SysUtil.getInstance(IoApFileBatchType.FileBatchResultInfo.class);
		
		IoApFileBatchReg apFileBatchReg = SysUtil.getInstance(IoApFileBatchReg.class);
		IoApBatchFileStruct.IoApWjplrwxxInfo wjplxx = apFileBatchReg.selWjplrwxx(weituoho, dcn_num);
		if (wjplxx == null) {
			throw ApError.Aplt.E0000("在DCN[" + ApDCN.getMyDcnNo() + "]未找到委托号[" + weituoho + "],DCN编号[" + dcn_num + "]对应的文件批量任务!");
		} else {
			CommUtil.copyProperties(result, wjplxx);
		}
		
		return result;
	}
	
     /**
      * DCN文件批量提交
      *
      */
	@Override
	public void doTaskProcess( final IoApFileBatchType.FileBatchSubmitInfo plrwtjxx){
	    TrxEnvs.RunEnvs runEnv = CommToolsAplt.prcRunEnvs();
		// 2014.12.23 R/C-DCN需要登记DCN子文件批量信息
	    IoApFileBatchReg apFileBatchReg = SysUtil.getInstance(IoApFileBatchReg.class);
	    // 先查询一次
	    IoApBatchFileStruct.IoApWjplrwxxInfo wjplxx = apFileBatchReg.selWjplrwxx(plrwtjxx.getEntrno(), plrwtjxx.getCdcnno());
	    if (wjplxx == null) {
	    	bizlog.debug("doTaskProcess_dcnNo_init=" + plrwtjxx.getCdcnno());
	        wjplxx = SysUtil.getInstance(IoApBatchFileStruct.IoApWjplrwxxInfo.class);
	        wjplxx.setEntrno(plrwtjxx.getEntrno());
	        wjplxx.setTrandt(runEnv.getTrandt());
	        wjplxx.setFlbtst(E_FLBTST.ZZPD);
	     
	        wjplxx.setMdcnno(plrwtjxx.getMdcnno());
	        wjplxx.setCdcnno(plrwtjxx.getCdcnno());
	        wjplxx.setTotanm(plrwtjxx.getTotanm());
	        
            apFileBatchReg.saveIoApWjplrwxx(wjplxx);
	    } else {
	    	bizlog.debug("doTaskProcess_dcnNo_update=" + plrwtjxx.getCdcnno() + " 状态:" + wjplxx.getFlbtst());
	        switch (wjplxx.getFlbtst()) {
	        case CLSB:
	            // 对于失败的任务，若再次提交则只需将状态更新为“正在排队处理”
                wjplxx.setFlbtst(E_FLBTST.ZZPD);
                apFileBatchReg.saveIoApWjplrwxx(wjplxx);
	            break;
	        case ZZPD:
	        case ZZCL:
	        	throw ExceptionUtil.wrapThrow("暂不支持该操作");
//	        	if (DMBUtil.getIntanse().isAdminDcn(DMBUtil.getIntanse().getConfiguredDcnNo())){
//	        		bizlog.debug("doTaskProcess_dcnNo_ADM=" + plrwtjxx.getDcnnum() + " 状态:" + wjplxx.getFlbtst());
//	        		break;
//	        	}
	        case CLCG:
	            // 对于正在处理 或 处理成功 的任务，再次提交时直接返回即可。
	            return;
	        default:
	            throw ApError.Aplt.E0000("委托号[" + plrwtjxx.getEntrno() + "]对应的文件批量任务已提交，请不要重复提交!");
	        }
	    }
        
        // 提交DCN子文件批量
        Map<String, Object> dataArea = new HashMap<String, Object>();
        
        dataArea.put("ApDict.Bat.wenjxxlb.toString()", plrwtjxx.getFilist());//文件信息  TODO 无此类型，临时避免报错
        
        dataArea.put(ApDict.Aplt.corpno.toString(), runEnv.getCorpno());  //法人代码
		dataArea.put(BaseDict.Comm.trandt.getId(), runEnv.getTrandt());  //交易日期
		dataArea.put(BaseDict.Comm.tranus.getId(), runEnv.getTranus());  //交易柜员
		dataArea.put(BaseDict.Comm.tranbr.getId(), runEnv.getTranbr());  //交易机构
		dataArea.put(BaseDict.Comm.servtp.getId(), runEnv.getServtp()); //渠道号
		
//        dataArea.putAll(CommUtil.toMap(plrwtjxx));
//        if (DMBUtil.getIntanse().isAdminDcn(DMBUtil.getIntanse().getConfiguredDcnNo())){
//        	bizlog.debug("doTaskProcess_ADMdcnNo=" + plrwtjxx.getdcn_num());
//        	BatchTools.submitTask(plrwtjxx.getWeituoho()+"_ADM", plrwtjxx.getPljyzbsh(), dataArea);
//        }else{
//        	bizlog.debug("doTaskProcess_RDCNdcnNo=" + plrwtjxx.getdcn_num());
//        	BatchTools.submitTask(plrwtjxx.getWeituoho(), plrwtjxx.getPljyzbsh(), dataArea);
//        }
	}

	/***
	 * 批量提交处理结束后通知数据子系统
	 * 
	 */
	@Override
	public void doBatchSubmitBack(String busseq,String filesq, String trandt, String filenm, String flpath,String status, String descri) {
		
		knp_bach bach = Knp_bachDao.selectOne_odb1(filesq, trandt, true);
		
		bach.setFilenm(filenm);
		bach.setFlpath(flpath);
		bach.setDatast(E_FLBTST.CLCG);
		Knp_bachDao.updateOne_odb1(bach);
		
		if(ApltTabDao.selKnpBachByDatast(busseq, true) > 0){
			log.dataArea("<<======未完成的批量条数为:".concat(ApltTabDao.selKnpBachByDatast(busseq, false).toString()));
			return;
		}
		
		//String busseq = bach.getBusisq();	//业务流水
		E_FILETP filetp = bach.getDataid(); //数据类型
		String acctdt = bach.getAcctdt();	//会计日期
		
		boolean isable = true; //是否发送通知标志
		E_SYSCCD source = null;
		E_SYSCCD target = null;
		Options<BatchFileSubmit> optSmt = new DefaultOptions<>();
		
		List<knp_bach> lstBach = Knp_bachDao.selectAll_odb2(busseq, true);
		for(knp_bach val : lstBach){
			
			log.debug("<<===========lstBach = " + lstBach + "=================>>");
			log.debug("<<===========val.getDatast() = " + val.getDatast() + "===================>>");
			
//			if(val.getDatast() == E_FLBTST.ZZCL){
//				isable = false;
//				break;
//			}
			if(CommUtil.isNotNull(val.getFlpath())&&CommUtil.isNotNull(val.getFilenm())){
			String md5 = ""; 	//MD5值
			try {
				md5 = MD5EncryptUtil.getFileMD5String(new File(val.getFlpath().concat(val.getFilenm())));
			} 
		
			catch (Exception e) {
				throw ApError.Aplt.E0042(val.getFilenm());
			}
			
			//target = val.getSource(); //交换目标和源的位子
			//source = val.getTarget();
			
			//获取电子账户系统生成相对路径
			String fiPath = ApBatchFilePath.getApBatchDownFilePath(val.getFlpath());
			
			BatchFileSubmit smt = CommTools.getInstance(BatchFileSubmit.class);
			smt.setFilenm(val.getFilenm());
			smt.setFlpath(fiPath);
			smt.setFilemd(md5);
			
			Map<String,Object> map = new HashMap<String,Object>();
			
			map.put(ApBatchFileParams.BATCH_PMS_FILESQ, val.getFilesq());
			map.put(ApBatchFileParams.BATCH_PMS_TRANDT, val.getTrandt());
			
			smt.setParams(JSON.toJSONString(map));
			
			optSmt.add(smt);
		}
		else{
				break;
			}
		}
		
		log.debug("<<=============isable = " + isable + "======================>>");
		
		if(isable){
			log.debug("<<=====柜员=====>>：".concat(CommToolsAplt.prcRunEnvs().getTranus()));
			log.debug("<<=====机构=====>>：".concat(CommToolsAplt.prcRunEnvs().getTranbr()));
			log.debug("<<=====主流水=====>>：".concat(CommToolsAplt.prcRunEnvs().getMntrsq()));
			log.debug("<<=====流水=====>>：".concat(CommToolsAplt.prcRunEnvs().getTransq()));
			
			CommToolsAplt.prcRunEnvs().setTrandt(trandt);
			
			knp_conf conf = Knp_confDao.selectOne_odb1(filetp, true);
			if(CommUtil.isNull(conf.getSource())){
				throw ApError.Aplt.E0043();
			}
			if(CommUtil.isNull(conf.getTarget())){
				throw ApError.Aplt.E0044();
			}
			if(E_SYSCCD.NAS == conf.getSource()){ 
				//如果源系统是电子账户系统，则不需要交换目标和源的位置
				
				source = conf.getSource();
				target = conf.getTarget();
				
				busseq = null;
			}
			else{
				source = conf.getTarget();
				target = conf.getSource();
			}
			
//			IoCaOtherService CaOtherService =  SysUtil.getInstanceProxyByBind(IoCaOtherService.class,"opaccdid");
//			CaOtherService.callDataSysNotice(source, target, filetp, busseq, acctdt, status, descri, optSmt);
		}
	}
	@Override
	public void doBatchSubmitBackToAll(E_FILETP dataid, String filenm, String flpath) {
		
		String trandt = CommToolsAplt.prcRunEnvs().getTrandt();
		
		knp_conf conf = Knp_confDao.selectOne_odb1(dataid, true);
		List<knp_conf_detl> lst_detl = Knp_conf_detlDao.selectAll_odb1(dataid, true);
		
		Options<targetList> taglst = new DefaultOptions<>();
		
		for(knp_conf_detl detl : lst_detl){
			
			targetList  target = CommTools.getInstance(targetList.class); 
			target.setTarget(detl.getTarget()); 
			
			taglst.add(target);
		}
		
		String pathname = flpath + File.separator + filenm;
		String md5 = "";
		try {
			md5 = MD5EncryptUtil.getFileMD5String(new File(pathname));
			
//			CommTools.getInstance(IoCaOtherService.class).callDataSysNoticeToAll(conf.getSource(), dataid, filenm, md5, trandt, taglst);
		} catch (Exception e) {
			
			throw ApError.Aplt.E0042(filenm);
		}
		
	}
	
	public void doBatchSubmitBackNotice(String status, String descri,
			E_SYSCCD target, E_FILETP filetp, Options<BatchFileSubmit> fileList){
		
		String trandt = CommToolsAplt.prcRunEnvs().getTrandt();
		E_SYSCCD source = E_SYSCCD.NAS;
		String busseq = CommToolsAplt.prcRunEnvs().getMntrsq();
		CommToolsAplt.prcRunEnvs().setBusisq(busseq);
		String acctdt = trandt;
		log.debug("<<=============target.getValue() = " + target.getValue() + "======================>>");
		KnpPara bind = KnpParaDao.selectOne_odb1(target.getValue(), "%", "%", "%","999", false);
		if (CommUtil.isNull(bind)){
			throw ApError.Aplt.E0000("外调服务接口未配置，请在knp_para表中配置系统"+target+"对应的外调服务绑定ID");
		}
//		IoCaOtherService CaOtherService =  SysUtil.getInstanceProxyByBind(IoCaOtherService.class,bind.getPmval1());
//		CaOtherService.callDataSysNotice(source, target, filetp, busseq, acctdt, status, descri, fileList); 
		
	}
/*	public void doBatchSubmitBackSynNotice(String dataid, String filenm, String filemd,String acctdt,Options<targetList> targetList){
		E_SYSCCD source = E_SYSCCD.NAS;
		
		IoCaOtherService CaOtherService =  SysUtil.getInstanceProxyByBind(IoCaOtherService.class,"opaccdid");
		CaOtherService.callDataSysSynNotice(source, acctdt, dataid, filenm, filemd, targetList);
	}*/
	/**
	 * 
	 * @param status  状态
	 * @param descri  错误信息
	 * @param target  目标系统
	 * @param filetp  数据类型
	 * @param busseq  业务流水
	 * @param acctdt  业务日期
	 * @param fileList 文件数据集 
	 */
	public void returnBatchSubmitNotice(String status, String descri, E_SYSCCD target,
			E_FILETP filetp, String busseq, String acctdt,Options<BatchFileSubmit> fileList){
		
//		IoCaOtherService CaOtherService =  SysUtil.getInstanceProxyByBind(IoCaOtherService.class,"opaccdid");
//		CaOtherService.callDataSysNotice(E_SYSCCD.NAS, target, filetp, busseq, acctdt, status, descri, fileList); 
	}

	public void doBatchSubmitBackSynNotice(E_SYSCCD source, String acctdt,
			E_FILETP dataid, String filenm, String filemd,
			Options<targetList> targetList) {
		
//		IoCaOtherService CaOtherService =  SysUtil.getInstanceProxyByBind(IoCaOtherService.class,"opaccdwd");
//		CaOtherService.callDataSysSynNotice(source, acctdt, dataid, filenm, filemd, targetList);
		
	}
}

