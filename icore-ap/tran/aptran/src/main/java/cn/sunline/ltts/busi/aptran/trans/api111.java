package cn.sunline.ltts.busi.aptran.trans;

import java.math.BigDecimal;
import java.util.List;

import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.ap.iobus.type.IoApReverseType.IoApReverseIn;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsRedu;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsReduDao;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTran;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTranDao;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrike;
import cn.sunline.ltts.busi.aptran.namedsql.StrikeSqlsDao;
import cn.sunline.ltts.busi.aptran.transdef.Api111.Output.Acvoch;
import cn.sunline.ltts.busi.iobus.servicetype.ac.IoAcAccountServ;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlBill;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnaAcct;
import cn.sunline.ltts.busi.iobus.type.IoInTable.IoGlKnpBusi;
import cn.sunline.ltts.busi.iobus.type.ac.IoAcServType.IoKnsAcsqInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKcdProd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcal;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCust;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoKupTppr;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.DpError;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.AcVouchType.AccountVouch;
import cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_SMSMODEL;
import cn.sunline.ltts.busi.sys.type.ApSmsType.KupTppr;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SERVTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.ltts.busi.sys.type.InEnumType.E_ISPAYA;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_MSDLST;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_SMGTYP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_TYPCOD;

public class api111 {

	/**
	 * 通用冲正服务
	 * @param input
	 * @param property
	 * @param output
	 */
	public static void strike(final cn.sunline.ltts.busi.aptran.transdef.Api111.Input input,
			final cn.sunline.ltts.busi.aptran.transdef.Api111.Property property,
			final cn.sunline.ltts.busi.aptran.transdef.Api111.Output output) {
		
		IoApReverseIn cplRvIn = SysUtil.getInstance(IoApReverseIn.class);
		cplRvIn.setOtradt(input.getTrandt());
		cplRvIn.setOtrasq(input.getTransq());
		cplRvIn.setCorrfg(input.getCorrfg());
		cplRvIn.setStacps(input.getStacps());
		cplRvIn.setVobkfg(input.getVobkfg());
		
		if(CommUtil.isNull(input.getStacps())){
			cplRvIn.setStacps(E_STACPS.ACCOUT);
		}
		
		if(CommUtil.isNull(input.getTransq())){
			throw ApError.Aplt.E0000("原交易流水不能为空！");
		}
		
		
		CommTools.prcRunEnvs().setRemark(input.getReason());//备注信息
		cplRvIn.setOtsqtp(E_YES___.YES);
		SysUtil.rollbackGlobal(input.getTransq());
		output.setRvrttx("冲账交易处理成功");
		output.setMntrdt(CommToolsAplt.prcRunEnvs().getTrandt());
		output.setMntrsq(CommToolsAplt.prcRunEnvs().getTransq());
		output.setSmrycd(input.getReason());
		//登记外围登记簿用字段处理 add by chenjk 20190408
		property.setMaindt(CommTools.prcRunEnvs().getTrandt());
		property.setMainsq(CommTools.prcRunEnvs().getTransq());
		/**
		 * cuijia
		 * 交易调用冲正，默认是使用调用流水进行冲正
		 */
		/*cplRvIn.setOtsqtp(E_YES___.YES);
		if(CommTools.prcRunEnvs().getServtp() == E_SERVTP.EB){ //后管使用核心流水来冲正
			if (CommTools.isFlowTran()) {
	        	KnsRedu redu = KnsReduDao.selectOne_odb3(cplRvIn.getOtrasq(), cplRvIn.getOtradt(), false);
	        	if (redu == null) {
	                throw ApError.Sys.E9005(cplRvIn.getOtrasq());
	            }
	        	
	        	cplRvIn.setOtradt(redu.getInpudt());// 原交易日期
	        	cplRvIn.setOtrasq(redu.getInpusq());// 原交易流水
        	}
		}
		
		AccountVouch tb = SysUtil.getInstance(AccountVouch.class);
	
		output.setRvrtcd(ApStrike.prcRollback8(cplRvIn));
		output.setRvrttx("冲账交易处理成功");
//		List<IoKnsAcsqInfo> listAcsq = StrikeSqlsDao.seltransq(
//				CommToolsAplt.prcRunEnvs().getTrandt(), CommToolsAplt.prcRunEnvs().getTransq(), CommToolsAplt.prcRunEnvs().getCorpno(), false);
		Options<IoKnsAcsqInfo> listAcsq = SysUtil.getInstance(IoAcAccountServ.class).getKnsAcsqByMntrsq(CommToolsAplt.prcRunEnvs().getTrandt(), CommToolsAplt.prcRunEnvs().getTransq(), CommToolsAplt.prcRunEnvs().getCorpno());
		//jym add 交易金额字段名称不为tranam，登记交易流水表交易金额字段tranam取值为0，手动修改
		BigDecimal tranam = BigDecimal.ZERO;
		
		for(IoKnsAcsqInfo tbKnsAcsq:listAcsq){//会计凭证供打印使用
			Acvoch acvoch = SysUtil.getInstance(Acvoch.class);
			acvoch.setTranam(tbKnsAcsq.getTranam());
			acvoch.setAmntcd(tbKnsAcsq.getAmntcd());
			acvoch.setDtitcd(tbKnsAcsq.getDtitcd());
			if(CommUtil.compare(ApAcctRoutTools.getRouteType(tbKnsAcsq.getAcctno()),E_ACCTROUTTYPE.INSIDE)==0){//内部户账号
				IoGlKnaAcct tbIoGlKnaAcct = StrikeSqlsDao.selGlKnaAcct(tbKnsAcsq.getAcctno(), CommToolsAplt.prcRunEnvs().getCorpno(), false);
				acvoch.setAcctno(tbKnsAcsq.getAcctno());
				acvoch.setAcctna(tbIoGlKnaAcct.getAcctna());
				acvoch.setIspaya(tbIoGlKnaAcct.getIspaya());
				acvoch.setCrcycd(tbKnsAcsq.getCrcycd());				
			}else{//客户账
				IoCaKnaAcdc tbIoCaKnaAcdc = StrikeSqlsDao.selknaacdc(tbKnsAcsq.getCuacno(), E_DPACST.NORMAL, CommToolsAplt.prcRunEnvs().getCorpno(), false);
				IoCaKnaCust tbIoCaKnaCust = StrikeSqlsDao.selKnaCustBycustac(tbKnsAcsq.getCuacno(), CommToolsAplt.prcRunEnvs().getCorpno(), false);
				if(CommUtil.isNull(tbIoCaKnaAcdc)||CommUtil.isNull(tbIoCaKnaCust)){
					IoGlKnpBusi tblknpbusi= StrikeSqlsDao.selknpbusi(tbKnsAcsq.getCuacno(), CommToolsAplt.prcRunEnvs().getCorpno(), false);
					if(CommUtil.isNotNull(tblknpbusi)){
						acvoch.setAcctno(tblknpbusi.getBusino());
						acvoch.setAcctna(tblknpbusi.getBusina());
						acvoch.setCrcycd(tbKnsAcsq.getCrcycd());
						acvoch.setIspaya(E_ISPAYA._0);
					}
				}
				if(CommUtil.isNotNull(tbIoCaKnaAcdc)&&CommUtil.isNotNull(tbIoCaKnaCust)){
					acvoch.setAcctno(tbIoCaKnaAcdc.getCardno());
					acvoch.setAcctna(tbIoCaKnaCust.getCustna());
					acvoch.setCrcycd(tbKnsAcsq.getCrcycd());
					acvoch.setIspaya(E_ISPAYA._0);
				}
				
			}
			
			output.getAcvoch().add(acvoch);
			
			//jym add
			tranam = tranam.add(new BigDecimal(Math.abs(tbKnsAcsq.getTranam().doubleValue())));
	
		}
		output.setMntrdt(CommToolsAplt.prcRunEnvs().getTrandt());
		output.setMntrsq(CommToolsAplt.prcRunEnvs().getTransq());
		output.setSmrycd(input.getReason());
		
		//登记外围登记簿用字段处理 add by chenjk 20190408
		property.setMaindt(CommTools.prcRunEnvs().getTrandt());
		property.setMainsq(CommTools.prcRunEnvs().getTransq());
		
		//jym add 交易金额 = 总绝对值金额／2
		tranam = tranam.divide(new BigDecimal(2));
		RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		KnsTran knsTran = KnsTranDao.selectOne_odb1(runEnvs.getTransq(), runEnvs.getTrandt(), true);
		knsTran.setTranam(tranam);
		KnsTranDao.updateOne_odb1(knsTran);*/
	}

	public static void sendMsg(
			 final cn.sunline.ltts.busi.aptran.transdef.Api111.Input input,  final cn.sunline.ltts.busi.aptran.transdef.Api111.Property property,  final cn.sunline.ltts.busi.aptran.transdef.Api111.Output output) {

		/*// 获取交易码
		String origsq = input.getTransq();
		String origdt = input.getTrandt();
		String prcscd = null;
		if(CommTools.prcRunEnvs().getServtp() == E_SERVTP.EB){ //后管使用核心流水来冲正
//			if (EngineContext.peek().getRequestContext().isFlowTran()) {
			if (CommTools.isFlowTran()) {
	        	KnsRedu redu = KnsReduDao.selectOne_odb3(origsq, origdt, false);
	        	if (redu == null) {
	                return;
	            }
	        	prcscd = redu.getPrcscd();
        	}
		}else{
//			if (EngineContext.peek().getRequestContext().isFlowTran()) {
			if (CommTools.isFlowTran()) {
	        	KnsRedu redu = KnsReduDao.selectFirst_odb4(origsq, origdt, false);
	        	if (redu == null) {
	                return;
	            }else{
	            	origsq = redu.getTransq();
	            	origdt = redu.getTrandt();
	            	prcscd = redu.getPrcscd();
	            }
        	}
		}
		if (CommUtil.equals("tranin", prcscd)) {

			KupTppr kuptrrp = StrikeSqlsDao.selKupTpprByPrcscd(prcscd, false);
			if (CommUtil.isNull(kuptrrp)) {
				throw DpError.DeptComm.BNAS2240();
			}
			IoDpKnlBill knlbill = StrikeSqlsDao.selKnlBillByTransq(origsq,
					false);
			String acctna = knlbill.getAcctna();
			String acctbl = knlbill.getAcctbl().toString();// 余额
			String cardno = knlbill.getOpcuac();// 卡号
			String custac = knlbill.getCustac(); //
			String tranam = knlbill.getTranam().abs().toString();// 交易金额
			String endCaardNo = null;
			if (cardno.length() > 4) {
				endCaardNo = cardno.substring(cardno.length() - 4,
						cardno.length());
			}
			//查询绑定卡卡号  判断行内行外
            IoCaKnaCacd knaCacd = StrikeSqlsDao.selKnaCacdByCustac(custac,true);
            String   wbcard =    knaCacd.getCardno() ;
            E_SMSMODEL  mgtpid = null;
            List<IoCaKcdProd> kcdProdList =  StrikeSqlsDao.selKcdProdByProdst(E_PRODST.NORMAL, true);
		    for (IoCaKcdProd kcdProd : kcdProdList) {
		    	String  acrdbn = kcdProd.getCardbn();
		    	if(wbcard.length()>acrdbn.length()&&wbcard.substring(0,acrdbn.length()-1).equals(acrdbn)){
		    		//行内入金冲正	
		    		  mgtpid = E_SMSMODEL.TRANOTIN;
		    	}else{
		    	   //行外入金冲正
		    		  mgtpid = E_SMSMODEL.TRANOTOUT;
		    	}
		    	
			}
			// 查询手机号
			IoCaKnaAcal knaAcal = StrikeSqlsDao.selknaAcalbycustac(CommTools
					.prcRunEnvs().getCorpno(), custac, false);
			if (CommUtil.isNull(knaAcal)) {
				throw DpError.DeptComm.BNAS2241();
			}
			String telno = knaAcal.getAcalno();

			String date = CommToolsAplt.prcRunEnvs().getTrandt()
					.substring(0, 4)
					+ "年"
					+ CommToolsAplt.prcRunEnvs().getTrandt().substring(4, 6)
					+ "月"
					+ CommToolsAplt.prcRunEnvs().getTrandt().substring(6, 8)
					+ "日"
					+ CommToolsAplt.prcRunEnvs().getTrantm().substring(0, 2)
					+ ":"
					+ CommToolsAplt.prcRunEnvs().getTrantm().substring(2, 4)
					+ ":"
					+ CommToolsAplt.prcRunEnvs().getTrantm().substring(4, 6);
		    //短信模板查询
			IoKupTppr kupTppr = StrikeSqlsDao.selKupTpprByMgtpid(mgtpid.getValue(), false);
			if (CommUtil.isNull(kupTppr)) {
				throw DpError.DeptComm.BNAS2240();
			}
			String SMSmuban = kupTppr.getMgvers();
			String phpara = kupTppr.getMsginfo();

			phpara = phpara.replace("#cardno#", endCaardNo)
					.replace("#date#", date).replace("#tranam#", tranam)
					.replace("#acctbl#", acctbl.toString())
					.replace("#custname#", acctna);
			String transq = CommToolsAplt.prcRunEnvs().getTrandt()
					+ CommTools.getSequence("sedmsg", 10);

			StrikeSqlsDao.insertKupSmsd(E_TYPCOD.SMS.toString(), telno, phpara,
					E_SMGTYP.CHANBL.toString(), null, SMSmuban, CommToolsAplt
							.prcRunEnvs().getTranbr(), E_MSDLST.PENDING, null,
					CommToolsAplt.prcRunEnvs().getTrandt(), transq, null);
		} else if (CommUtil.equals("tranot", prcscd)) {

			KupTppr kuptrrp = StrikeSqlsDao.selKupTpprByPrcscd(prcscd, false);
			if (CommUtil.isNull(kuptrrp)) {
				throw DpError.DeptComm.BNAS2240();
			}
			IoDpKnlBill knlbill = StrikeSqlsDao.selKnlBillByTransq(origsq,
					false);
			String acctbl = knlbill.getAcctbl().toString();// 余额
			String cardno = knlbill.getOpcuac();// 卡号
			String custac = knlbill.getCustac(); //
			String tranam = knlbill.getTranam().abs().toString();// 交易金额
			String endCaardNo = null;
			if (cardno.length() > 4) {
				endCaardNo = cardno.substring(cardno.length() - 4,
						cardno.length());

			}
			//查询绑定卡卡号  判断行内行外
            IoCaKnaCacd knaCacd = StrikeSqlsDao.selKnaCacdByCustac(custac,true);
            String   wbcard =    knaCacd.getCardno() ;
            E_SMSMODEL  mgtpid = null;
            List<IoCaKcdProd> kcdProdList =  StrikeSqlsDao.selKcdProdByProdst(E_PRODST.NORMAL, true);
		    for (IoCaKcdProd kcdProd : kcdProdList) {
		    	String  acrdbn = kcdProd.getCardbn();
		    	if(wbcard.length()>acrdbn.length()&&wbcard.substring(0,acrdbn.length()-1).equals(acrdbn)){
		    		//行内入金冲正	
		    		  mgtpid = E_SMSMODEL.TRANOTIN;
		    	}else{
		    	   //行外入金冲正
		    		  mgtpid = E_SMSMODEL.TRANOTOUT;
		    	}
			}
			// 查询手机号
			IoCaKnaAcal knaAcal = StrikeSqlsDao.selknaAcalbycustac(CommTools
					.prcRunEnvs().getCorpno(), custac, false);
			if (CommUtil.isNull(knaAcal)) {
				throw DpError.DeptComm.BNAS2241();
			}
			String telno = knaAcal.getAcalno();

			String date = CommToolsAplt.prcRunEnvs().getTrandt()
					.substring(0, 4)
					+ "年"
					+ CommToolsAplt.prcRunEnvs().getTrandt().substring(4, 6)
					+ "月"
					+ CommToolsAplt.prcRunEnvs().getTrandt().substring(6, 8)
					+ "日"
					+ CommToolsAplt.prcRunEnvs().getTrantm().substring(0, 2)
					+ ":"
					+ CommToolsAplt.prcRunEnvs().getTrantm().substring(2, 4)
					+ ":"
					+ CommToolsAplt.prcRunEnvs().getTrantm().substring(4, 6);

		    //短信模板查询
			IoKupTppr kupTppr = StrikeSqlsDao.selKupTpprByMgtpid(mgtpid.getValue(), false);
			if (CommUtil.isNull(kupTppr)) {
				throw DpError.DeptComm.BNAS2240();
			}
			String SMSmuban = kupTppr.getMgvers();
			String phpara = kupTppr.getMsginfo();

			phpara = phpara.replace("#cardno#", endCaardNo).replace("#date#", date).replace("#tranam#", tranam).replace("#acctbl#", acctbl.toString());
			String transq = CommTools.prcRunEnvs().getCdcnno()+CommToolsAplt.prcRunEnvs().getTrandt()+CommTools.getSequence("sedmsg", 10);

			// 待发短信表新增数据
			StrikeSqlsDao.insertKupSmsd(E_TYPCOD.SMS.toString(), telno, phpara,E_SMGTYP.CHANBL.toString(), null, SMSmuban, CommToolsAplt.prcRunEnvs().getTranbr(), E_MSDLST.PENDING, null,
					CommToolsAplt.prcRunEnvs().getTrandt(), transq, null);

		}*/

	}

}