//package cn.sunline.ltts.busi.aptran.trans;
//
//import cn.sunline.ltts.biz.global.CommUtil;
//import cn.sunline.ltts.biz.global.SysUtil;
//import cn.sunline.ltts.busi.aplt.tables.SysDbTable.Kapp_clrdatDao;
//import cn.sunline.ltts.busi.aplt.tables.SysDbTable.kapp_clrdat;
//import cn.sunline.ltts.busi.aplt.tables.SysDbTable.kapp_kjidat;
//import cn.sunline.ltts.busi.aplt.tools.CommTools;
//import cn.sunline.ltts.busi.aplt.tools.LogManager;
//import cn.sunline.ltts.busi.dp.servicetype.IoDpAcctSvcType;
//import cn.sunline.ltts.busi.in.type.InacTransComplex.IaAcdrInfo;
//import cn.sunline.ltts.busi.in.type.InacTransComplex.IaTransOutPro;
//import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpSrvQryTableInfo;
//import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpStrikeSvcType;
//import cn.sunline.ltts.busi.iobus.servicetype.in.IoInAccount;
//import cn.sunline.ltts.busi.iobus.servicetype.pb.IoBrchSvcType;
//import cn.sunline.ltts.busi.iobus.type.IoDpTable.InknlcnapotDetl;
//import cn.sunline.ltts.busi.sdk.util.DaoUtil;
//import cn.sunline.ltts.busi.sys.errors.DpError;
//import cn.sunline.ltts.busi.sys.tables.KnpParaTable;
//import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
//import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
//import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CLACTP;
//import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SMRYCD;
//import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANST;
//import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TRANTP;
//import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
//import cn.sunline.ltts.busi.sys.type.DpEnumType.E_IOTYPE;
//import cn.sunline.ltts.busi.sys.type.DpEnumType.E_STACPS;
//import cn.sunline.ltts.busi.sys.type.DpEnumType.E_SUBSYS;
//import cn.sunline.ltts.core.api.logging.BizLog;
//
//
//public class cnapre {
//	
//	private static final BizLog bizlog = LogManager.getBizLog(cnapre.class);
//
//	public static void strike( final cn.sunline.ltts.busi.aptran.trans.intf.Cnapre.Input input,  final cn.sunline.ltts.busi.aptran.trans.intf.Cnapre.Output output){
//		
//		preTranCheck(input); //交易前检查
//		
//		IoDpSrvQryTableInfo ioDpSrvQryTableInfo = CommTools.getInstance(IoDpSrvQryTableInfo.class);
//		
//		//根据六要素判断是否存在原交易
//		InknlcnapotDetl knlCnapot = ioDpSrvQryTableInfo.knl_cnapot_selectOne_odb1(input.getMsetdt(), input.getMsetsq(), input.getPyercd(), input.getIotype(), input.getCrdbtg(), E_TRANST.NORMAL, false);
//		
//		if(CommUtil.isNull(knlCnapot)){
//			
//			knlCnapot = ioDpSrvQryTableInfo.knl_cnapot_selectOne_odb1(input.getMsetdt(), input.getMsetsq(), input.getPyercd(), input.getIotype(), input.getCrdbtg(), E_TRANST.STRIKED, false);
//			
//			if(CommUtil.isNotNull(knlCnapot)){
//				CommToolsAplt.prcRunEnvs().setRemark("交易已冲正");
//				output.setAcctbr(CommToolsAplt.prcRunEnvs().getTranbr()); 
//				return;
//			}
//			
//			InknlcnapotDetl inknlcnapotDetl = CommTools.getInstance(InknlcnapotDetl.class);
//			IoDpAcctSvcType IoDpAcctSvcType = CommTools.getInstance(IoDpAcctSvcType.class);
//			inknlcnapotDetl.setSubsys(input.getSubsys());//原清算渠道
//			inknlcnapotDetl.setMsetdt(input.getMsetdt());//原委托日期
//			inknlcnapotDetl.setMsetsq(input.getMsetsq());//原交易序号
//			inknlcnapotDetl.setCrdbtg(input.getCrdbtg().getValue());//原借贷标志
//			inknlcnapotDetl.setMesgtp(input.getMesgtp()); //原报文编号
//			inknlcnapotDetl.setIotype(input.getIotype()); //原往来标志
//			inknlcnapotDetl.setCrcycd(input.getCrcycd()); //原币种
//			inknlcnapotDetl.setCstrfg(input.getCstrfg()); //原现转标志
//			inknlcnapotDetl.setCsextg(input.getCsextg()); //原钞汇属性
//			inknlcnapotDetl.setPyercd(input.getPyercd()); //原发起行行号
//			inknlcnapotDetl.setPyeecd(input.getPyeecd()); //原接受行行号
//			inknlcnapotDetl.setPyerac(input.getPyerac()); //原付款人账号
//			inknlcnapotDetl.setPyerna(input.getPyerna()); //原付款人名称
//			inknlcnapotDetl.setPyeeac(input.getPyeeac()); //原收款人账号
//			inknlcnapotDetl.setPyeena(input.getPyeeac()); //原收款人名称
//			inknlcnapotDetl.setPriotp(input.getPriotp()); //原加急标志
//			inknlcnapotDetl.setAfeetg(input.getAfeetg()); //原收费标志
//			inknlcnapotDetl.setTranam(input.getTranam()); //原发生额
//			inknlcnapotDetl.setAfeeam(input.getAfeeam()); //原手续费
//			inknlcnapotDetl.setFeeam1(input.getFeeam1()); //原汇划费
//			inknlcnapotDetl.setChfcnb(input.getChfcnb()); //原对账分类编号
//			inknlcnapotDetl.setFrondt(input.getFrondt()); //支付前置日期
//			inknlcnapotDetl.setFronsq(input.getFronsq()); //支付前置流水
//			inknlcnapotDetl.setBrchno(input.getBrchno()); //交易机构
//			inknlcnapotDetl.setUserid(input.getUserid()); //录入柜员
//			inknlcnapotDetl.setAuthus(input.getAuthus()); //授权柜员
//			inknlcnapotDetl.setStatus(E_TRANST.STRIKED);
//			
//			IoDpAcctSvcType.saveIOknlnapot(inknlcnapotDetl);
//			CommToolsAplt.prcRunEnvs().setRemark("交易已冲正");
//			
//		}else{
//			try{
////				if(true){
////					throw new Exception("测试挂账");
////				}
//				
//				
//				bizlog.debug("大小额冲正交易开始==========");
//				String trandt = CommToolsAplt.prcRunEnvs().getTrandt();
//				
//				if(CommUtil.equals(knlCnapot.getTrandt(), trandt)){
//					bizlog.debug("大小额当日冲正交易开始==========");
//					api100.strike2(knlCnapot.getTransq(),E_YES___.YES, E_STACPS.POSITIVE);
//					api100.prcBusineseField2(knlCnapot.getTransq(), "");
//				}else{
//					bizlog.debug("大小额隔日冲正交易开始==========");
//					CommTools.getInstance(IoDpStrikeSvcType.class).procCnapreStrikeDieb(knlCnapot);
//				}
//				
//				
//			}catch(Exception e){
//				DaoUtil.rollbackTransaction();
//				String errmes = e.getLocalizedMessage();
//				bizlog.error("===输出错误堆栈===");
//				e.printStackTrace();
//				
//				if(CommUtil.isNotNull(errmes)){
//					
//					int index = errmes.indexOf("]");		
//					if(index >= 0){					
//						errmes = errmes.substring(index + 1).replace("]", "").replace("[", "");
//					}
//				}
//				bizlog.debug("<<======大小额冲正失败挂账:[%s]", errmes);
//				CommTools.getInstance(IoDpStrikeSvcType.class).procCnapreStrikeHold(knlCnapot, errmes);
//			}
//			
//			//更新大小额往来账明细登记簿
//			knlCnapot.setStatus(E_TRANST.STRIKED);
//			ioDpSrvQryTableInfo.knl_cnapot_updateOne_odb1(knlCnapot);
//			
//			output.setHostdt(CommToolsAplt.prcRunEnvs().getTrandt());
//			output.setHostsq(CommToolsAplt.prcRunEnvs().getTransq());
//			
//			//add 20161219 songlw 增加返回参数 
//			//查询 清算日期信息
//			kapp_clrdat tblKappClrdat = Kapp_clrdatDao.selectOne_odb1(CommTools.getCenterFrdm(kapp_kjidat.class), true);
//			output.setClerdt(tblKappClrdat.getSystdt());
//			output.setClerod(tblKappClrdat.getClenum());
//			output.setRetrsq(knlCnapot.getTransq());
//			output.setRetrdt(knlCnapot.getTrandt());
//			output.setRecldt(knlCnapot.getClerdt());
//			output.setReclod(knlCnapot.getClenum());
//		}
//		
//		//输出
//		output.setAcctbr(CommToolsAplt.prcRunEnvs().getTranbr()); 
//		
//	}
//	
//	
//	//交易前检查
//	private static void preTranCheck(final cn.sunline.ltts.busi.aptran.trans.intf.Cnapre.Input input){
//		
//		if(CommUtil.isNull(input.getSubsys())){
//			throw DpError.DeptComm.E9027("原交易渠道");
//		}
//		
//		if(CommUtil.isNull(input.getMsetdt())){
//			throw DpError.DeptComm.E9027("原委托日期");
//		}
//		
//		if(CommUtil.isNull(input.getMsetsq())){
//			throw DpError.DeptComm.E9027("原交易序号");
//		}
//		
//		if(CommUtil.isNull(input.getCrdbtg())){
//			throw DpError.DeptComm.E9027("原借贷标志");
//		}
//		
//		if(CommUtil.isNull(input.getIotype())){
//			throw DpError.DeptComm.E9027("原往来标志");
//		}
//		
//		if(CommUtil.isNull(input.getCstrfg())){
//			throw DpError.DeptComm.E9027("原现转标志");
//		}
//		
//		if(CommUtil.isNull(input.getCsextg())){
//			throw DpError.DeptComm.E9027("原钞汇属性");
//		}
//		
//		if(CommUtil.isNull(input.getPyercd())){
//			throw DpError.DeptComm.E9027("原发起行行号");
//		}
//		
//		if(CommUtil.isNull(input.getPyeecd())){
//			throw DpError.DeptComm.E9027("原接收行行号");
//		}
//		
//		if(CommUtil.isNull(input.getPyerac())){
//			throw DpError.DeptComm.E9027("原付款人账号");
//		}
//		
//		if(CommUtil.isNull(input.getPyerna())){
//			throw DpError.DeptComm.E9027("原付款人名称");
//		}
//		
//		if(CommUtil.isNull(input.getPyeeac())){
//			throw DpError.DeptComm.E9027("原收款人账号");
//		}
//		
//		if(CommUtil.isNull(input.getPyeena())){
//			throw DpError.DeptComm.E9027("原收款人名称");
//		}
//		
//		if(CommUtil.isNull(input.getPriotp())){
//			throw DpError.DeptComm.E9027("原加急标志");
//		}
//		
////		if(CommUtil.isNull(input.getAfeetg())){
////			throw DpError.DeptComm.E9027("原收费标志");
////		}
//		
//		if(CommUtil.isNull(input.getTranam())){
//			throw DpError.DeptComm.E9027("原发生额 ");
//		}
//		
//		if(CommUtil.isNull(input.getAfeeam())){
//			throw DpError.DeptComm.E9027("原手续费 ");
//		}
//		
//		if(CommUtil.isNull(input.getFeeam1())){
//			throw DpError.DeptComm.E9027("原汇划费 ");
//		}
//		
//		if(CommUtil.isNull(input.getChfcnb())){
//			//throw DpError.DeptComm.E9027("原对账分类编号");
//		}
//		
//		if(CommUtil.isNull(input.getFrondt())){
//			throw DpError.DeptComm.E9027("支付前置日期");
//		}
//		
//		if(CommUtil.isNull(input.getFronsq())){
//			throw DpError.DeptComm.E9027("支付前置流水号");
//		}
//		
//		if(CommUtil.isNull(input.getBrchno())){
//			throw DpError.DeptComm.E9027("交易机构号");
//		}
//		
//		if(CommUtil.isNull(input.getUserid())){
//			throw DpError.DeptComm.E9027("录入柜员");
//		}
//		
////		if(CommUtil.isNull(input.getAuthus())){
////			throw DpError.DeptComm.E9027("授权柜员");
////		}
//		
//	}
//	
//}
//
