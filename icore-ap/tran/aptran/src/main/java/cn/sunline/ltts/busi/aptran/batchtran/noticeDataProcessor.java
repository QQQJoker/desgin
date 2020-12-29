package cn.sunline.ltts.busi.aptran.batchtran;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.targetList;
import cn.sunline.ltts.busi.aplt.namedsql.ApltTabDao;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_bussDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_confDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_conf_detlDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_conf;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_conf_detl;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoCaOtherService;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

	 /**
	  * 文件批量执行成功后通知数据子系统
	  * 定时执行，扫描文件批量信息表，同一个业务编号下的所有文件均执行成功后，向数据子系统发送通知消息
	  *
	  */

public class noticeDataProcessor extends
  AbstractBatchDataProcessor<cn.sunline.ltts.busi.aptran.batchtran.intf.Notice.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Notice.Property, cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_buss> {
	  
	  
	  private static BizLog log = LogManager.getBizLog(noticeDataProcessor.class);
	  /**
		 * 批次数据项处理逻辑。
		 * 
		 * @param job 批次作业ID
		 * @param index  批次作业第几笔数据(从1开始)
		 * @param dataItem 批次数据项
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 */
		@Override
		public void process(String jobId, int index, cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_buss dataItem, cn.sunline.ltts.busi.aptran.batchtran.intf.Notice.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Notice.Property property) {
			int fail = 0;
			int succ = 0;
			String desc = "成功";
			E_FILEST filest = E_FILEST.SUCC;
			
			IoCaOtherService CaOtherService =  SysUtil.getInstanceProxyByBind(IoCaOtherService.class,"opaccdid");
			
			knp_conf conf = Knp_confDao.selectOne_odb1(dataItem.getFiletp(), false);
			
			int cnt = ApltTabDao.selKapbWjplxxbCnt(dataItem.getBusseq(), true);
			
			List<kapb_wjplxxb> succ_btwj = new ArrayList<kapb_wjplxxb>();
			
			List<kapb_wjplxxb> btwj = ApltTabDao.selKapbWjplxxbDetl(dataItem.getBusseq(), false);
			for(kapb_wjplxxb val : btwj){
				if(val.getBtfest() == E_BTFEST.SUCC){
					succ = succ + 1;
					succ_btwj.add(val);
				}else if(val.getBtfest() == E_BTFEST.FAIL){
					filest = E_FILEST.FAIL; 
					desc = val.getErrotx();
					fail = fail + 1;
				}
				
			}
			
			if(fail + succ < cnt || fail == cnt){
				return;
			}
						
			desc = CommUtil.nvl(desc, "交易失败");
			
			KnpPara  tbl_knpPara = CommTools.getInstance(KnpPara.class); 
			tbl_knpPara = CommTools.KnpParaQryByCorpno("Batch.File", "%", "%", "%", true);
			
			
			log.debug("<<=====柜员========>>：".concat(CommToolsAplt.prcRunEnvs().getTranus()));
			log.debug("<<=====机构========>>：".concat(CommToolsAplt.prcRunEnvs().getTranbr()));
			log.debug("<<=====流水========>>：".concat(CommToolsAplt.prcRunEnvs().getMntrsq()));
			log.debug("<<=====流水========>>：".concat(CommToolsAplt.prcRunEnvs().getTransq()));
			log.debug("<<=====业务流水=====>>：".concat(dataItem.getBusseq()));
			log.debug("<<=====业务日期=====>>：".concat(dataItem.getAcctdt()));
			log.debug("<<=====文件类型=====>>：".concat(dataItem.getFiletp().getValue()));
			log.debug("<<=====类型名称=====>>：".concat(dataItem.getFiletp().getLongName()));
			
			if(conf.getIssued() == E_YES___.YES){
				List<knp_conf_detl> lst_detl = Knp_conf_detlDao.selectAll_odb1(dataItem.getFiletp(), false);
				if(CommUtil.isNull(lst_detl)){
					log.debug("未找到对应批量文件配置明细表记录");
					return;
//					throw ApError.Aplt.E0000("");
				}
				
				Options<targetList> taglst = new DefaultOptions<>();
				
				for(knp_conf_detl detl : lst_detl){
					
					targetList  target = CommTools.getInstance(targetList.class); 
					target.setTarget(detl.getTarget()); 
					
					taglst.add(target);
				}
				
				for(kapb_wjplxxb pl : succ_btwj){
					String md5 = ""; // MD5值
					try {
						md5 = MD5EncryptUtil.getFileMD5String(new File(pl.getUpfeph().concat(pl.getUpfena())));
					} catch (Exception e) {
						
						String errotx = "找不到文件[%s]" + pl.getUpfeph().concat(pl.getUpfena());
						
						pl.setErrotx(errotx);
						Kapb_wjplxxbDao.updateOne_odb1(pl);
						
						log.debug(errotx);
						return;
//						throw ApError.Aplt.E0042(pl.getUpfeph().concat(pl.getUpfena()));
					}
					
					try{
						if(dataItem.getSendnm() <= 3){  //发送次数少于3，则继续发送
						CaOtherService.callDataSysNoticeToAll(E_SYSCCD.NAS, dataItem.getFiletp(), pl.getUpfena(), md5, dataItem.getAcctdt(), taglst);
						}
					}catch(Exception e){
						
						dataItem.setStatus("0");
						dataItem.setFilels(e.getMessage());
						dataItem.setSendnm(dataItem.getSendnm() + 1); //通知次数
						log.debug(e.getMessage());
						return;
					}
				}
			}else{
				Options<BatchFileSubmit> optSmt = new DefaultOptions<>();
				if(conf.getIsupfe() == E_YES___.YES){ //有反盘文件产生
					for(kapb_wjplxxb val : succ_btwj){
						String params  =  val.getFiletx();
						String md5 = ""; // MD5值
						try {				
							md5 = MD5EncryptUtil.getFileMD5String(new File(val.getUpfeph().concat(val.getUpfena())));
						} catch (Exception e) {
							String errotx = "找不到文件[%s]" + val.getUpfeph().concat(val.getUpfena());
							
							val.setErrotx(errotx);
							Kapb_wjplxxbDao.updateOne_odb1(val);
							
							log.debug(errotx);
							return;
//							throw ApError.Aplt.E0042(val.getUpfeph().concat(val.getUpfena()));
						}
						// 获取电子账户系统生成相对路径
						
						String relaph = val.getUpfeph().substring(tbl_knpPara.getPmval1().length()-1);
						BatchFileSubmit smt = CommTools.getInstance(BatchFileSubmit.class);
						smt.setFilenm(val.getUpfena());
						smt.setFlpath(relaph);
						smt.setFilemd(md5);
		                
						Map<String, Object> map = new HashMap<String, Object>();
						if(CommUtil.isNotNull(params)){
							map =JsonUtil.parse(params);
						}
						map.put(ApBatchFileParams.BATCH_PMS_FILESQ, val.getBtchno());
						map.put(ApBatchFileParams.BATCH_PMS_TRANDT, val.getTrandt());
		               
						smt.setParams(JSON.toJSONString(map));
		
						optSmt.add(smt);
					}
				}
				
				
				String busseq = dataItem.getBusseq();
				
				if(conf.getIsbuss() == E_YES___.NO){
					busseq = null;
				}
				//modify by wuzx-20170216-批量通知文件子系统增加发送次数控制 --beg
				try{
					if(dataItem.getSendnm() <= 3){  //发送次数少于3，则继续发送
						CaOtherService.callDataSysNotice(E_SYSCCD.NAS, dataItem.getTarget(), dataItem.getFiletp(), busseq, dataItem.getAcctdt(), filest.getValue(), desc, optSmt);					
					}else{
						return;
					}					
				}catch(Exception e){
					dataItem.setStatus("0");
					dataItem.setFilels(e.getMessage());
					dataItem.setSendnm(dataItem.getSendnm() + 1); //通知次数
					Knp_bussDao.updateOne_odb1(dataItem);
					log.debug(e.getMessage());
					return;
				}				
			     //modify by wuzx-20170216-批量通知文件子系统增加发送次数控制 --end
			}
			
			dataItem.setSendst(E_YES___.YES);
			dataItem.setStatus("1");
			dataItem.setSntaid(this.getTaskId()); //add 20170313 登记通知批次号
			Knp_bussDao.updateOne_odb1(dataItem);
		}
		
		
		
		
		/**
		 * 获取数据遍历器。
		 * @param input 批量交易输入接口
		 * @param property 批量交易属性接口
		 * @return 数据遍历器
		 */
		@Override
		public BatchDataWalker<cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_buss> getBatchDataWalker(cn.sunline.ltts.busi.aptran.batchtran.intf.Notice.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Notice.Property property) {
			
			
			Params params = new Params();
			
			params.add("issend", E_YES___.YES);
			params.add("sendst", E_YES___.NO);
		//	params.add("trandt", CommToolsAplt.prcRunEnvs().getTrandt());
			
			return new CursorBatchDataWalker<cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_buss>(ApltTabDao.namedsql_selKapbWjplxxbByBusseq, params);
		}

}


