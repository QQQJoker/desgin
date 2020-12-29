package cn.sunline.ltts.busi.hctran.batch;

import java.io.File;
import java.util.List;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.hc.servicetype.HcBatchSrv;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbFile;
import cn.sunline.ltts.busi.hc.tables.HotCtrl.HcbFileDao;
import cn.sunline.ltts.busi.hc.util.HotCtrlUtil;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.HcEnumType.E_HSREAD;

public class HcGenerateReadFile extends LttsTimerProcessor{
	private static final BizLog BIZLOG = BizLogUtil.getBizLog(HcGenerateReadFile.class);
	@Override
	public void process(String arg0, DataArea paramData) {
		BIZLOG.info("HcGenerateReadFile begin process");
		String tabnum=HotCtrlUtil.getTabnum(paramData);//分表号

		boolean hasData = false;//是否有待读取文件
		KnpPara knpPara1 = ApKnpPara.getKnpPara("HOT_BAL_PROCESS", "TIME",true);
		//KnpPara knpPara = CommTools.KnpParaQryByCorpno("hc", "hcb_pedl", "%", "%", true);
		int processTime = Integer.parseInt(knpPara1.getPmval1());//秒,运行时间
		int waitTime = Integer.parseInt(knpPara1.getPmval3());//秒，等待时间
		long startTime = System.currentTimeMillis();
		//RunEnvsComm runEnvs = CommTools.prcRunEnvs();
		//String trandt = HotCtrlUtil.getHotCtrlDate();
		do {	
			List<HcbFile> hcbFileList=HcbFileDao.selectAll_odb1(E_HSREAD.WD ,tabnum, false);
			if(CommUtil.isNotNull(hcbFileList) && hcbFileList.size() > 0) {
				BIZLOG.info("<<<处理中数据[%s]条>>>", hcbFileList.size());
				hasData = true;
				prcHcData(hcbFileList,tabnum);	
			} else {
				hasData = false;
			}
			if(!hasData) {
				// 如果无数据,则等待waitTime
				try {
					BIZLOG.info("HcGenerateReadFile.process begin waiting：" + waitTime + "秒");
					Thread.sleep(waitTime*1000);
				} catch (InterruptedException e) {
					BIZLOG.error("[%s]", e.toString());
				}
			}
			long endTime = System.currentTimeMillis();
			BIZLOG.info("HcGenerateReadFile.process time：" + processTime + "秒");
			if((endTime - startTime)/1000 > processTime) {
				break;
			}
		} while (true);
		BIZLOG.info("HcGenerateReadFile end process");
	}
	
	public void prcHcData(final List<HcbFile> hcbFileList,final String tabnum ) {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			public Void execute() {
				for(HcbFile hcbfile:hcbFileList){
					 /*获取管理节点*/
		            HcBatchSrv hcBatchSrv = SysUtil.getInstance(HcBatchSrv.class);          
		            String filePath = hcbfile.getFlpath();
		            String fileName = hcbfile.getFlname();
		            BIZLOG.info("HcGenerateReadFile 读取文件信息导入库中");		           
		            hcBatchSrv.prcHcDataMerging(filePath + File.separator + fileName,tabnum);
		            hcbfile.setHsread(E_HSREAD.YD);
		            HcbFileDao.updateOne_odb2(hcbfile);
				}	
				 return null;
			}
		});
	}
		
}
