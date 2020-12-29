package cn.sunline.ltts.busi.aplt.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.aplt.namedsql.cifCommNamedSqlDao;
import cn.sunline.ltts.busi.aplt.servicetype.CifCommService;
import cn.sunline.ltts.busi.aplt.tables.CifCommFieldTable.Cif_file_rgstDao;
import cn.sunline.ltts.busi.aplt.tables.CifCommFieldTable.Cif_warn_infoDao;
import cn.sunline.ltts.busi.aplt.tables.CifCommFieldTable.cif_file_rgst;
import cn.sunline.ltts.busi.aplt.tables.CifCommFieldTable.cif_warn_info;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpGlbl;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpGlblDao;
import cn.sunline.ltts.busi.iobus.type.us.IoUsComplexType.CifFilePlat;
import cn.sunline.ltts.busi.sys.errors.UsError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SERVTP;
import cn.sunline.ltts.busi.sys.type.CfEnumType.E_FAILTYPE;
import cn.sunline.ltts.busi.sys.type.CfEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.CfEnumType.E_FLJHST;
import cn.sunline.ltts.busi.sys.type.CfEnumType.E_SYSNAME;
import cn.sunline.ltts.busi.sys.type.CfEnumType.E_SYSTEMTP;
import cn.sunline.ltts.busi.sys.type.CfEnumType.E_WARNTY;

public class CifFileTools {
	private static final BizLog bizlog = BizLogUtil.getBizLog(CifFileTools.class);

	/**
	 * (非 Javadoc) 
	 * <p>Title: fileInteractionRequest</p> 
	 * <p>Description: 文件交互通知请求(联机使用)</p> 
	 * @param source 源系统
	 * @param target 目标系统
	 * @param dataid 数据类型
	 * @param busseq 业务流水
	 * @param filenm 文件名
	 * @param flpath 文件路径
	 * @param params 业务参数域（json）
	 * @see cn.sunline.ltts.busi.iobus.servicetype.pr.IoCommInfoSvcType#fileInteractionRequest(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 **/
	public static void FileInteractionRequestBatch(String source,  String target,  String dataid,  String busseq,  E_FLJHST status,  String descri,  String filenm,  String flpath,  String params,String acctdt){
		if (CommUtil.isNull(source)) {
			throw UsError.UsComm.E0006("参数source不能为空");
		}
		if (CommUtil.isNull(target)) {
			throw UsError.UsComm.E0006("参数target不能为空");
		}
		if (CommUtil.isNull(dataid)) {
			throw UsError.UsComm.E0006("参数dataid不能为空");
		}
		if (CommUtil.isNull(filenm)) {
			throw UsError.UsComm.E0006("参数filenm不能为空");
		}
		if (CommUtil.isNull(flpath)) {
			throw UsError.UsComm.E0006("参数flpath不能为空");
		}
		if (CommUtil.isNull(acctdt)) {
			throw UsError.UsComm.E0006("参数acctdt不能为空");
		}
		
		//设置虚拟机构和柜员
		CifTools.setRunEnvs(E_SERVTP.CIF,  E_SYSTEMTP.CIF);
		//主机地址
		String hostName = SysUtil.getIp();
//		CommTools.prcRunEnvs().setHostip(hostName);//暂时不用
		
		FileInteractionRequest(source, target, dataid, busseq, status, descri, filenm, flpath, params, acctdt, "share_file_path");
	}
	
	public static void FileInteractionRequest(String source,  String target,  String dataid,  String busseq,  E_FLJHST status,  String descri,  String filenm,  String flpath,  String params, String acctdt, String pathky){
		//外调对应系统文件交互分装服务
		CifCommService cifComonService = SysUtil.getInstance(CifCommService.class);
		CifCommService.FileInteractiveReqByServer.InputSetter fileInput = SysUtil.getInstance(CifCommService.FileInteractiveReqByServer.InputSetter.class);
		CifCommService.FileInteractiveReqByServer.Output fileOutput = SysUtil.getInstance(CifCommService.FileInteractiveReqByServer.Output.class);
		
		CifFilePlat filePlat = SysUtil.getInstance(CifFilePlat.class);   //文件集合符合类型
		List<CifFilePlat> fileList = new ArrayList<CifFilePlat>();  //文件list
		Options<CifFilePlat> op = SysUtil.getInstance(Options.class);  //option对象


		String pathpr = CifTools.getParam("share_file_path");//交互根路径
		bizlog.debug(" 文件交互公共方法，交互根路径 = %s  ", pathpr);
		
		//文件MD5加密处理
		File newfile = new File(pathpr+flpath+filenm);
				
		bizlog.debug("===========newfile: " + newfile + "=============");
		
		String filemd = null;
		try{
			filemd = MD5EncryptUtil.getFileMD5String(newfile);
			bizlog.debug("===========filemd: " + filemd + "=============");
		}catch(Exception e){
			throw UsError.UsComm.E0006("文件MD5校验异常");
		}
		filePlat.setFilemd(filemd);
		filePlat.setFilenm(filenm);
		filePlat.setFlpath(flpath);
		filePlat.setParams(params);
		
		fileList.add(filePlat);
		op.addAll(fileList);
		
		fileInput.setAcctdt(acctdt);//会计日期
		fileInput.setBusseq(busseq);//业务流水
		fileInput.setDataid(dataid);//数据类型
		fileInput.setDescri(descri);//结果描述
		fileInput.setFileList(op);//文件集合
		fileInput.setSource(E_SYSNAME.get(source));//源系统
		fileInput.setStatus(status);//交互状态
		fileInput.setTarget(E_SYSNAME.get(target));//目标系统
		
		//登记文件登记簿
		cif_file_rgst cif_file_rgst =SysUtil.getInstance(cif_file_rgst.class);
		String tranno = CifTools.getfilesq();//文件批次号
		cif_file_rgst.setTranno(tranno);

		cif_file_rgst.setAcctdt(acctdt);
		
		bizlog.debug("===============================trandt.toString():  "+acctdt +"=====批量消息通知公共外调服务============="); 
		cif_file_rgst.setDataid(dataid);
		cif_file_rgst.setFilemd(filemd);
		cif_file_rgst.setFilenm(filenm);
		cif_file_rgst.setFilest(E_FILEST.DJ);
		cif_file_rgst.setFlpath(flpath);
		cif_file_rgst.setSource(E_SYSNAME.get(source));
		cif_file_rgst.setTarget(E_SYSNAME.get(target));
		cif_file_rgst.setParams(params);
		cif_file_rgst.setTrandt(DateTools.getSystemDate());
		cif_file_rgst.setBusseq("init");
		cif_file_rgst.setTrantm(DateTools.getCurrentLocalTime());
		Cif_file_rgstDao.insert(cif_file_rgst);
		
		//调用文件交互通知外调分装服务
		cifComonService.FileInteractiveReqByServer(fileInput, fileOutput);
		
		//根据外调结果进行相应业务处理
		resultDeal(busseq,tranno,fileInput,fileOutput);
		
	}
	
	/*
	 * 外调对应文件系统结果处理
	 */
	public static void resultDeal (String busseq,String tranno,CifCommService.FileInteractiveReqByServer.InputSetter input,CifCommService.FileInteractiveReqByServer.Output out){
		String rebusseq = busseq;
		String dataid = input.getDataid();
		E_SYSNAME target = input.getTarget();
		E_SYSNAME source = input.getSource();
		
		if (CommUtil.isNull(busseq)) {
			rebusseq = out.getBusseq();
		}
		
		bizlog.debug("----------------------------业务流水-------------------------------------rebusseq："+rebusseq);
		String dlcode = CommTools.prcRunEnvs().getErorcd();
		String dldesc = CommTools.prcRunEnvs().getErortx();
		bizlog.debug(" 文件交互公共方法，交互错误码 = %s  ", dlcode);
		bizlog.debug(" 文件交互公共方法，交互结果 = %s  ", dldesc);
		if (CommUtil.equals(dlcode, E_FAILTYPE.success.getValue())) {
			cifCommNamedSqlDao.updSendFileRgst(tranno,E_FILEST.FS.getValue(),rebusseq,"文件交互发送成功",DateTools.getCurrentDateTime());
			
			// 根据文件类型查询对应批量交易信息
			KnpGlbl kp = KnpGlblDao.selectOne_odb1("file_interactive", dataid,target.getValue(),source.getValue(), false);
			
			String prcscd = String.valueOf(kp.getPmkey1());  //批量交易码
			String groupId = String.valueOf(kp.getPmkey2()); //批量交易组
			String rqflag = String.valueOf(kp.getPmkey3());  //发送标识  1-是  0-否
			//将原来收到的请求改为已回执
			if (CommUtil.isNotNull(kp)){
				if (CommUtil.equals(rqflag, "0")  ) {
		        	int reqnum = cifCommNamedSqlDao.sel_filergst_by_busseq(E_FILEST.SL.toString(), busseq, target.getValue(), source.getValue(), false);
		        	if (reqnum > 0) {
		        		cifCommNamedSqlDao.upd_filergst_by_busseq(E_FILEST.HZ.getValue(), E_FILEST.SL.getValue(), busseq, target.getValue(), source.getValue());
		    		}
				}
			}
			
		//响应方超时
		}else if(CommUtil.equals(dlcode, E_FAILTYPE.responseTimeOut.getValue())){
			cifCommNamedSqlDao.updSendFileRgst(tranno,E_FILEST.FSCS.getValue(),rebusseq,dldesc,DateTools.getCurrentDateTime());
			cif_warn_info tblrps_warn_info = SysUtil.getInstance(cif_warn_info.class);
			tblrps_warn_info.setTranno(tranno);
			tblrps_warn_info.setTrancd(dataid);
			tblrps_warn_info.setTrannm(0);//交易笔数
			tblrps_warn_info.setWarnty(E_WARNTY.FSCS);
//			tblrps_warn_info.setErinfo("文件请求通知重发超限");
			tblrps_warn_info.setErinfo(E_WARNTY.FSCS.getLongName());
			Cif_warn_infoDao.insert(tblrps_warn_info);
		}
		//异常或错误
		else {
			cifCommNamedSqlDao.updSendFileRgst(tranno,E_FILEST.SB.getValue(),rebusseq,dldesc,DateTools.getCurrentDateTime());
		}
	}
	
}
