package cn.sunline.ltts.busi.aptran.trans;

public class smtbat {

//	public static void BatchFileSubmit( final cn.sunline.ltts.busi.aptran.trans.intf.Smtbat.Input input,  final cn.sunline.ltts.busi.aptran.trans.intf.Smtbat.Output output){
//		
//		if(CommUtil.isNull(input.getSource())){
//			throw ApError.Sys.E0001("来源系统标识不能为空");
//		}
//		if(CommUtil.isNull(input.getBusseq())){
//			throw ApError.Sys.E0001("业务流水不能为空");
//		}
//		if(CommUtil.isNull(input.getAcctdt())){
//			throw ApError.Sys.E0001("业务日期不能为空");
//		}
//		if(CommUtil.isNull(input.getDataid())){
//			throw ApError.Sys.E0001("数据类型不能为空");
//		}
//		if(CommUtil.isNull(input.getTarget())){
//			throw ApError.Sys.E0001("目标系统标识不能为空");
//		}
//		
//		String trandt = CommToolsAplt.prcRunEnvs().getTrandt();
//		String transq = CommToolsAplt.prcRunEnvs().getMntrsq();
//		
//		String busseq = input.getBusseq(); //流水
//		String acctdt = input.getAcctdt(); //日期
//		
//		E_FILETP filetp = input.getDataid();
//		knp_conf conf = Knp_confDao.selectOne_odb1(filetp, false);
//		if(CommUtil.isNull(conf)){
//			throw ApError.Aplt.E0040();
//		}
//		KnpPara para = CommTools.KnpParaQryByCorpno("Batch.File", "%", "%", "%", true);
//		//2017-11-08 增加
//		boolean issued = conf.getIssued() == E_YES___.YES?true:false; //是否下发，即：做拆分的
//		boolean issubf = input.getIssubf() == E_YES___.YES?true:false; //是否子文件
//		
//		//重复提交检查
//		if( !issued || issubf ) {  //2017-11-08 增加：不是下发 或 为子文件 可以进行重复检查，否则不可以（则在ADM上跑的拆分下发动作不可以做重复检查）
//			if( !dubSmt(busseq)){
//				output.setBusseq(busseq);
//				return;
//			}
//		}
//		
//		String source = input.getSource().getValue().toUpperCase();
//		//本地工作目录
//		//String user_home = CommTools.getInstance(NFSFileTransfer.class).workDirectory();
//		//String user_home = System.getProperty("user.home");
//		String user_home = para.getPmval2(); //工作目录
//		
//		String sep = File.separator; //路径分隔符
//		
//		String nfs_ph = para.getPmval1();
//		String downna = ""; //上游系统上送文件名
//		String downph = ""; //上游系统上送文件路径
//		String upfena = ""; //返回文件名称
//		String filena = ""; //文件名称
//		
//		String relaph =  E_SYSCCD.NAS + sep+ input.getSource()+ sep + trandt; //相对路径
//		//String fileph = localDir + relaph + sep; //返回文件本地存放路径
//		String upfeph = nfs_ph + relaph + sep; //返回文件路径
//		String locaph = user_home + relaph + sep;
//		
//		knp_buss buss = CommTools.getInstance(knp_buss.class);
//		buss.setBusseq(busseq);
//		buss.setFiletp(filetp);
//		buss.setAcctdt(acctdt);
//		buss.setCursys(E_SYSCCD.NAS);
//		buss.setDescrp(filetp.getLongName());
//		//buss.setFilels(JSON.parseObject(input.getFileList()));
//		buss.setSendst(E_YES___.NO);
//		buss.setSource(conf.getSource());
//		buss.setTarget(conf.getTarget());
//		buss.setTrandt(trandt);
//		buss.setIssend(conf.getIssend());
//		buss.setStatus("");
//		buss.setSendnm(0);//第一次发送默认为0
//		Knp_bussDao.insert(buss);
//		
//		
//		
//		List<BatchFileSubmit> batch = input.getFileList();
//		
//		if(CommUtil.isNull(batch) || batch.size() <= 0){ //没有文件
//			String seqno = CommTools.getSequence("smtbatseq", 8);
//			String filesq = trandt.concat(filetp.getValue()).concat(CommUtil.lpad(seqno, 12, "0"));
//			filena = source.concat("_").concat(filetp.getValue()).concat("_").concat(trandt).concat("_").concat(seqno).concat(".txt");
//			
//			upfena = filena;
//			
//			DataArea area = DataArea.buildWithEmpty();
//			//报文头参数
//			area.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, trandt); //批量交易日期
//			area.getSystem().setString(BatchConfigConstant.TASK_ID, filesq); //批量交易批次号
//			area.getCommReq().setString("corpno", CommToolsAplt.prcRunEnvs().getCorpno());
//			area.getSystem().setString("corpno", CommToolsAplt.prcRunEnvs().getCorpno());
//			area.getCommReq().setString("tranbr", CommToolsAplt.prcRunEnvs().getTranbr());
//			area.getSystem().setString("tranbr", CommToolsAplt.prcRunEnvs().getTranbr());
//			area.getCommReq().setString("tranus", CommToolsAplt.prcRunEnvs().getTranus());
//			area.getSystem().setString("tranus", CommToolsAplt.prcRunEnvs().getTranus());
//			//报文体参数
//			area.getInput().setString(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);	//文件批次号
//			area.getInput().setString(ApBatchFileParams.BATCH_PMS_ACCTDT, acctdt); //账务日期
//			
//			kapb_wjplxxb wjb = CommTools.getInstance(kapb_wjplxxb.class);
//			wjb.setBusseq(busseq);	//业务流水
//			wjb.setBtchno(filesq);	//文件批次号
//			wjb.setAcctdt(acctdt);	//业务日期
//			wjb.setTrandt(trandt);	//交易日期
//			wjb.setDownna(downna);		//下载文件名
//			wjb.setDownph(downph);		//下载文件路径
//			wjb.setUpfena(upfena);	//返回文件名
//			wjb.setUpfeph(upfeph);	//返回文件路径
//			wjb.setFiletp(filetp);	//文件类型
//			wjb.setLocaph(locaph);	//文件本地存放路径
//			if( issued && !issubf ) {  //2017-11-08 如果是下发且不是子文件，则直接为“待下发”状态，没有文件处理是，由定时任务下发
//				wjb.setBtfest(E_BTFEST.WAIT_DISTRIBUTE);
//			} else {
//				wjb.setBtfest(E_BTFEST.DING); //文件处理状态
//			}
//			Kapb_wjplxxbDao.insert(wjb);
//			
//			//提交批量组
//			if( !issued || issubf ) {  //2017-11-08 如果不是下发或子文件，按原来方式创建批量进行处理
// 				BatchUtil.submitAndRunBatchTranGroup(filesq, conf.getBachcd(), area, false);
//			}
//
//		}else{
//			for(BatchFileSubmit bat : batch){
//				String params = bat.getParams();
//				if(CommUtil.isNull(bat.getFilenm())){ //没有文件名上送
//					String seqno = CommTools.getSequence("fileseq", 5);
//					filena = source.concat("_").concat(filetp.getValue()).concat("_").concat(trandt).concat("_").concat(seqno).concat(".txt");
//					upfena = filena;
//				}else{
//					filena = bat.getFilenm();
//					downna = filena;
//					upfena = filena.concat(".RET");
//				}
//				
//				if(bat.getFlpath().startsWith(File.separator)){
//					downph = nfs_ph + bat.getFlpath().substring(File.separator.length()); //拼接下载路径
//				}else{
//					downph = nfs_ph + bat.getFlpath();
//				}
//				
//				DataArea area = DataArea.buildWithEmpty();
//				String filesq = "";
//				String filedt = "";
//				try{
//					JSONObject obj = JSON.parseObject(params);
//					filesq = obj.getString(ApBatchFileParams.BATCH_PMS_FILESQ);
//					filedt = obj.getString(ApBatchFileParams.BATCH_PMS_TRANDT);
//					
//					if(filetp == E_FILETP.CA010300){ //电子账户当日开户信息查询
//						area.getInput().setString(ApBatchFileParams.BATCH_PMS_BRCHNO, obj.getString(ApBatchFileParams.BATCH_PMS_BRCHNO));
//						area.getInput().setString(ApBatchFileParams.BATCH_PMS_MAXNUM, obj.getString(ApBatchFileParams.BATCH_PMS_MAXNUM));
//						area.getInput().setString(ApBatchFileParams.BATCH_PMS_RELTID, obj.getString(ApBatchFileParams.BATCH_PMS_RELTID));
//						area.getInput().setString(ApBatchFileParams.BATCH_PMS_OPENDT, obj.getString(ApBatchFileParams.BATCH_PMS_OPENDT));	
//
//					}else if(filetp == E_FILETP.FN050100){//成立清算
//						int length = downna.length();
//						String seqno = downna.substring(length-10,length-4);
//						//返回文件名称
//						upfena = "NAS" + "_" + E_FILETP.FN050100 + "_" + acctdt + "_" + CommTools.getSequence("fileseq", 5) + "_" + seqno + ".txt" ;
//						
//						KnpPara tbl_knpPara = CommTools.KnpParaQryByCorpno("FNTRAN", "FILEPH", "%","%", true);
//						
//						if(CommUtil.isNull(tbl_knpPara)){
//							throw ApError.Sys.E0001("理财清算回执文件路径未配置");
//						}
//						
//						//组装成立清算返回路径
//						upfeph = tbl_knpPara.getPmval1() + E_FILETP.FN050100 + File.separator + acctdt + File.separator;
//						
//					
//					}else if(filetp == E_FILETP.FN050200){//到期清算
//						int length = downna.length();
//						String seqno = downna.substring(length-10,length-4);
//						//文件名称
//						upfena = "NAS" + "_" + E_FILETP.FN050200 + "_" + acctdt + "_" + CommTools.getSequence("fileseq", 5) + "_" + seqno + ".txt";
//						
//						KnpPara tbl_knpPara = CommTools.KnpParaQryByCorpno("FNTRAN", "FILEPH", "%","%", true);
//						
//						if(CommUtil.isNull(tbl_knpPara)){
//							throw ApError.Sys.E0001("理财清算回执文件路径未配置");
//						}
//						
//						//组装到期清算返回路径
//						upfeph = tbl_knpPara.getPmval1() + E_FILETP.FN050200 + File.separator + acctdt + File.separator;
//						
//					}else if(filetp==E_FILETP.DP959300){//支付平台通知核心
//						String chekdt = obj.getString(ApBatchFileParams.BATCH_PMS_CHCKDT);
//						area.getSystem().setString("prcscd", "payckawt");
//						area.getInput().setString("chckdt",chekdt);
//					}
//					
//				} catch(Exception e) {
//					throw ApError.Aplt.E0041();
//				}
//				//报文头参数
//				area.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, trandt); //批量交易日期
//				area.getSystem().setString(BatchConfigConstant.TASK_ID, filesq); //批量交易批次号
//				area.getCommReq().setString(ApBatchFileParams.BATCH_PMS_CORPNO, CommToolsAplt.prcRunEnvs().getCorpno());
//				area.getCommReq().setString(ApBatchFileParams.BATCH_PMS_TRANBR, CommToolsAplt.prcRunEnvs().getTranbr());
//				area.getCommReq().setString(ApBatchFileParams.BATCH_PMS_TRANUS, CommToolsAplt.prcRunEnvs().getTranus());
//				
//				//报文体参数
//				area.getInput().setString(ApBatchFileParams.BATCH_PMS_FILESQ, filesq);	//文件批次号
//				area.getInput().setString(ApBatchFileParams.BATCH_PMS_ACCTDT, acctdt);  //账务日期
//				//文件多次重发校验  add by wuzx 20170301 -beg 
//				/*if(CommUtil.isNotNull(downna)&&CommUtil.isNotNull(downph)){
//					kapb_wjplxxb wjpl = Kapb_wjplxxbDao.selectOne_odb3(downna, downph, false);
//					if(CommUtil.isNotNull(wjpl)){
//						throw ApError.Sys.E0001("文件["+downna+"]已存在,无需再次通知！");
//					} //无法适应外围系统是定时任务不停调用的情况，暂时去掉这个校验，modified by xieqq
//				}*/
//				//文件多次重发校验  add by wuzx 20170301 -end
//				kapb_wjplxxb wjb = CommTools.getInstance(kapb_wjplxxb.class);
//				wjb.setBusseq(busseq);	//业务流水
//				wjb.setBtchno(filesq);	//文件批次号
//				wjb.setAcctdt(acctdt);	//业务日期
//				wjb.setTrandt(trandt);	//交易日期
//				wjb.setDownna(downna);		//下载文件名
//				wjb.setDownph(downph);		//下载文件路径
//				wjb.setUpfena(upfena);	//返回文件名
//				wjb.setUpfeph(upfeph);	//返回文件路径
//				wjb.setFiletp(filetp);	//文件类型
//				wjb.setLocaph(locaph);	//文件本地存放路径
//				wjb.setFiletx(params);
//				
//				//2017-11-08 如果是下发且不是子文件，则直接为“待拆分”状态，且创建拆分批量交易
//				String bachcd = conf.getBachcd();
//				if( issued && !issubf ) {  
//					wjb.setBtfest(E_BTFEST.WAIT_SPLIT);
//					bachcd = conf.getSpltcd();  //拆分批量交易码
//				} else {
//					wjb.setBtfest(E_BTFEST.DING); //文件处理状态
//				}
//				Kapb_wjplxxbDao.insert(wjb);
//				
//				//提交批量组
//				BatchUtil.submitAndRunBatchTranGroup(filesq, bachcd, area, false);
//			}
//		}
//		
//		output.setBusseq(busseq);
//	}
//	
//	/**
//	 * @Title: dubSmt 
//	 * @Description: 重复提交检查并提交  
//	 * @param busseq
//	 * @author zhangan
//	 * @date 2017年1月16日 上午11:24:17 
//	 * @version V2.3.0
//	 */
//	private static boolean dubSmt(String busseq){
//		int fail = 0;
//		int succ = 0;
//		int total = 0;
//		
//		knp_buss buss = Knp_bussDao.selectOne_odb1(busseq, false);
//		if(CommUtil.isNull(buss)){
//			return true;
//		}
//		
//		List<kapb_wjplxxb> fail_batch = new ArrayList<>();
//		
//		int cnt = ApltTabDao.selKapbWjplxxbCnt(busseq, true);
//		
//		List<kapb_wjplxxb> btwj = ApltTabDao.selKapbWjplxxbDetl(busseq, false);
//		for(kapb_wjplxxb val : btwj){
//			if(val.getBtfest() == E_BTFEST.SUCC){
//				succ = succ + 1;
//			}else if(val.getBtfest() == E_BTFEST.FAIL){
//				fail = fail + 1;
//				fail_batch.add(val);
//			}
//		}
//		
//		total = fail + succ;
//		
//		if(total < cnt){
//			throw ApError.Aplt.E0051();
//		}
//		
//		if(fail > 0){
//			
//			for(kapb_wjplxxb val : fail_batch){
//				ksys_plrenw plrenw = Ksys_plrenwDao.selectOne_odb_1(val.getBtchno(), true);
//				
//				plrenw.setJiaoyzht(E_PILJYZHT.onprocess);
//				Ksys_plrenwDao.updateOne_odb_1(plrenw);
//				
//				val.setBtfest(E_BTFEST.DING);
//				Kapb_wjplxxbDao.updateOne_odb1(val);
//			}
//			
//			buss.setSendst(E_YES___.NO);
//			Knp_bussDao.updateOne_odb1(buss);
//		}else{
//			buss.setSendst(E_YES___.NO);
//			Knp_bussDao.updateOne_odb1(buss);
//		}
//		
//		return false;
//	}
}
