package cn.sunline.ltts.busi.aptran.sms.imp;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.transaction.ApSMSProcessor;
import cn.sunline.ltts.busi.aptran.namedsql.StrikeSqlsDao;
import cn.sunline.ltts.busi.iobus.servicetype.ac.IoAcAccountServ;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaSrvGenEAccountInfo;
import cn.sunline.ltts.busi.iobus.type.ac.IoAcServType.IoKnsAcsqInfo;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTable.IoCaKnaAcdc;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaQryMesgOut;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DPACST;

public class APSMSProcessorapi111 implements ApSMSProcessor {
	 private static final BizLog bizlog = BizLogUtil.getBizLog(APSMSProcessorapi111.class);
	@Override
	public String process(DataArea dataArea, Map<String, String> smsParm) {
		String trandt = CommToolsAplt.prcRunEnvs().getTrandt();//系统时间
		//获取会计流水
//		List<IoKnsAcsqInfo> listAcsq = StrikeSqlsDao.seltransq(
//				CommToolsAplt.prcRunEnvs().getTrandt(), CommToolsAplt.prcRunEnvs().getTransq(), CommToolsAplt.prcRunEnvs().getCorpno(), true);
//		
		Options<IoKnsAcsqInfo> listAcsq = SysUtil.getInstance(IoAcAccountServ.class).getKnsAcsqByMntrsq(CommToolsAplt.prcRunEnvs().getTrandt(), CommToolsAplt.prcRunEnvs().getTransq(), CommToolsAplt.prcRunEnvs().getCorpno());
		//导入电子账户发送短信实现类
		IoCaQryMesgOut mesgOut = CommTools.getInstance(IoCaQryMesgOut.class);
		IoCaSrvGenEAccountInfo eAccountInfo = CommTools.getInstance(IoCaSrvGenEAccountInfo.class);
		  
		    String year = trandt.substring(0,4);
			String month = trandt.substring(4,6);
			String day = trandt.substring(6,8);
			String hour = CommToolsAplt.prcRunEnvs().getTrantm().substring(0,2);
			String minute = CommToolsAplt.prcRunEnvs().getTrantm().substring(2,4);
			bizlog.debug("****************分钟"+minute+"****************");//使错误明显
			String second = CommToolsAplt.prcRunEnvs().getTrantm().substring(4,6);
			String date = year+"年"+ month+"月"+day+"日"+hour+":"+minute+":"+second;
			bizlog.debug("****************日期"+date+"****************");
		for(IoKnsAcsqInfo tbKnsAcsq:listAcsq){//会计凭证供打印使用
			if(CommUtil.compare(ApAcctRoutTools.getRouteType(tbKnsAcsq.getAcctno()),E_ACCTROUTTYPE.INSIDE)==0){//内部户账号
				//借用预开户不发短信特殊处理
				 bizlog.debug("****************开户人脸识别或身份识别失败,预开户不发送短信****************");//使错误明显
				 smsParm.put("mesflg", "0");
			}else{//客户账
				IoCaKnaAcdc tbIoCaKnaAcdc = StrikeSqlsDao.selknaacdc(tbKnsAcsq.getCuacno(), E_DPACST.NORMAL, CommToolsAplt.prcRunEnvs().getCorpno(), false);
				if(CommUtil.isNotNull(tbIoCaKnaAcdc)){
				mesgOut = eAccountInfo.selMesgInfo(tbIoCaKnaAcdc.getCustac(), dataArea.getCommReq().getString("trandt"), 
						CommToolsAplt.prcRunEnvs().getTrantm());
			               String tranam = tbKnsAcsq.getTranam().abs().toString();
			               //StringBuilder sb = new StringBuilder();
			              
						    smsParm.put("brchna", mesgOut.getBrchna());// 机构名
					        smsParm.put("cardno", mesgOut.getLastnm());// 尾号
					        smsParm.put("date", date);// 短信时间
					        if(CommUtil.compare(tbKnsAcsq.getTranam(), BigDecimal.ZERO) >0){
					        	smsParm.put("tranam",tranam);// 交易金额
					        	smsParm.put("trantp", "转入");// 交易类型
					        }else{
					        	smsParm.put("tranam",tranam);// 交易金额
					        	smsParm.put("trantp", "转出");// 交易类型
					        }
					        
					
				}else{
					//借用预开户不发短信特殊处理
					 bizlog.debug("****************开户人脸识别或身份识别失败,预开户不发送短信****************");//使错误明显
					 smsParm.put("mesflg", "0");
				}
			}
			
			
	
		}
		return mesgOut.getAcalno();
		
		
		
		
	}
}