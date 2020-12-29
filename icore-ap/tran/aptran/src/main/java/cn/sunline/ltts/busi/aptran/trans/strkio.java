package cn.sunline.ltts.busi.aptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aptran.namedsql.StrikeSqlsDao;
import cn.sunline.ltts.busi.iobus.type.IoDpTable.IoDpKnlBill;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKcdProd;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcal;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaCacd;
import cn.sunline.ltts.busi.iobus.type.pb.IoBrComplexType.IoKupTppr;
import cn.sunline.ltts.busi.sys.errors.DpError;
import cn.sunline.ltts.busi.sys.type.ApSmsEnumType.E_SMSMODEL;
import cn.sunline.ltts.busi.sys.type.ApSmsType.KupTppr;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PRODST;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_MSDLST;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_SMGTYP;
import cn.sunline.ltts.busi.sys.type.PbEnumType.E_TYPCOD;

public class strkio {

	public static void sendMsg(
			final cn.sunline.ltts.busi.aptran.trans.intf.Strkio.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Strkio.Property property,
			final cn.sunline.ltts.busi.aptran.trans.intf.Strkio.Output output) {

		// 获取交易码
		String origsq = input.getOrigsq();
		String origdt = input.getOrigdt();
		// knb_evnt 通过交易日期交易流水 得到 交易码 交易金额 客户账号 （得到手机号）
		KnbEvnt knbEvnt = StrikeSqlsDao.selKnbEvnt(origdt, origsq, false);
		if (CommUtil.isNull(knbEvnt)) {
			return;
		}
		String prcscd = knbEvnt.getPrcscd();
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

		}

	}
}
