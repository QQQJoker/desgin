package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.cleardate.ApClearDate;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppCldt;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.AppCldtDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApSysDateStru;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.iobus.type.dp.IoCaTypGenEAccountInfo.IoCaBatchWarnInfo;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;

import com.alibaba.fastjson.JSON;

	 /**
	  * 换日前清算日期检查
	  * 系统日期切换前需要保证清算日期比系统日期大
	  *
	  */

public class clerdtckDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Clerdtck.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Clerdtck.Property> {
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Clerdtck.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Clerdtck.Property property) {
		String trandt = CommToolsAplt.prcRunEnvs().getTrandt();
		 String clerdt ="";
		 ApSysDateStru cplKapp_clrdat = ApClearDate.getClearDateInfo();
		 clerdt = cplKapp_clrdat.getSystdt();
		 if(CommUtil.compare(clerdt, trandt)<=0){
			 //如果清算日期不大于系统日期，则强制切换清算日期
			 	AppCldt tblKapp_clrdat = AppCldtDao.selectOneWithLock_odb1(CommTools.getCenterCorpno(), true);		 
				tblKapp_clrdat.setBflsdt(tblKapp_clrdat.getLastdt());
				tblKapp_clrdat.setLastdt(tblKapp_clrdat.getSystdt());
				
				if ("".equals(tblKapp_clrdat.getNextdt())) {
					throw Sys.E0003();
				}
				else {
					tblKapp_clrdat.setSystdt(tblKapp_clrdat.getNextdt());
				}

				tblKapp_clrdat.setNextdt(tblKapp_clrdat.getAfnxdt());
				tblKapp_clrdat.setAfnxdt(DateTools.calDateByTerm(tblKapp_clrdat.getAfnxdt(), "1D"));
				tblKapp_clrdat.setYreddt(DateTools.calDateByFreq(tblKapp_clrdat.getSystdt(), "1YAE"));
				tblKapp_clrdat.setClenum(1);//初始化场次
				
				AppCldtDao.updateOne_odb1(tblKapp_clrdat);			 
				
				//监控预警
				KnpPara para = CommTools.KnpParaQryByCorpno("DAYENDNOTICE", "%", "%", "%", true);
				
				String bdid = para.getPmval1();// 服务绑定ID
				
				String mssdid = CommTools.getMessageId();// 随机生成消息ID
				
				String mesdna = para.getPmval2();// 媒介名称
				
				IoCaOtherService caOtherService = SysUtil.getInstanceProxyByBind (IoCaOtherService.class, bdid);
				
				IoCaOtherService.IoCaDayEndFailNotice.InputSetter mqInput = CommTools.getInstance(IoCaOtherService.IoCaDayEndFailNotice.InputSetter.class);
				
				String timetm = DateTools.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS000");
				IoCaBatchWarnInfo content = SysUtil.getInstance(IoCaBatchWarnInfo.class);
				content.setPljioyma("clerdtck");
				content.setPljyzbsh("90000");
				content.setPljyzwmc("换日前清算检查");
				content.setErrmsg("清算日期小于当前系统日期");
				content.setTrantm(timetm);
				
				// 发送消息
				mqInput.setMsgid(mssdid); // 消息ID
				mqInput.setMdname(mesdna); // 媒介名称
				mqInput.setTypeCode("NAS");
				mqInput.setTypeName("网络金融核心平台-电子账户核心系统");
				mqInput.setItemId("NAS_BATCH_WARN");
				mqInput.setItemName("电子账户核心批量执行错误预警");
				
				String str =JSON.toJSONString(content);
				mqInput.setContent(str);
				
				mqInput.setWarnTime(timetm);
				
				caOtherService.dayEndFailNotice(mqInput);	
		 }
		 
	}
	

}


