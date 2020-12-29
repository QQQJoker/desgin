package cn.sunline.ltts.busi.aptran.batchtran;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.Tsp_taskDao;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.tsp_task;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.ltts.busi.aplt.tools.DBTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.aptran.namedsql.TaskMainDao;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
	 /**
	  * 日终自动跑批
	  *
	  */

public class apautoDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.intf.Apauto.Input, cn.sunline.ltts.busi.aptran.batchtran.intf.Apauto.Property> {
  
	
	private static final BizLog biglog = LogManager.getBizLog(apautoDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.intf.Apauto.Input input, cn.sunline.ltts.busi.aptran.batchtran.intf.Apauto.Property property) {
		 
		 String sysid = SysUtil.getSystemId(); 
		 
		 if(CommUtil.isNull(input.getTargdt())){
			 throw Aplt.E0000("目标日期不能为空！");
		 }
		 
		 
		 
		 if(CommUtil.equals(sysid, "010")){
			 while(true){
				 
				 //hx_before日终日期
				 tsp_task bfPlrenw = TaskMainDao.selKsysPlrenw("hx_before", false);
				 int bfdate = Integer.parseInt(bfPlrenw.getTask_commit_date());
				 String bfstat = bfPlrenw.getTran_state().getValue();
				 
				 //hx_swday日终日期
				 tsp_task swPlrenw = TaskMainDao.selKsysPlrenw("hx_swday", false);
				 int swdate = Integer.parseInt(swPlrenw.getTask_commit_date());
				 String swstat = swPlrenw.getTran_state().getValue();
				 
				 //hx_dayend日终日期
				 tsp_task dyPlrenw = TaskMainDao.selKsysPlrenw("hx_dayend", false);
				 int dydate = Integer.parseInt(dyPlrenw.getTask_commit_date());
				 String dystat = dyPlrenw.getTran_state().getValue();
				 
				 
				 //获取三个日期中最小日期
				 String trandt = String.valueOf(Math.min(Math.min(bfdate, swdate), dydate));
				 
				 //如果日终日期大于等于目标日期，则停止调用
				 if(Integer.parseInt(trandt) >= Integer.parseInt(input.getTargdt())){
					 break;
				 }
				 
				 DataArea dataArea = DataArea.buildWithEmpty();
				 dataArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, DateTools.covDateToString(DateTools2.addDays(DateTools.covStringToDate(trandt), 1)));
				 dataArea.getInput().setString("farendma", "999");
				 
				 if(bfdate == swdate && bfdate == dydate){
					 //三个日期一致,判断hx_dayend 是否执行成功 hx_dayend_20180301_999
					 String taskid_bf = dyPlrenw.getTask_num();
					 if(CommUtil.equals(dystat,"success")){
						 taskid_bf = "hx_before_" + DateTools.covDateToString(DateTools2.addDays(DateTools.covStringToDate(String.valueOf(bfdate)), 1)) + "_999";
						 dataArea.getInput().setString("pljylcbs", "hx_before");
						 biglog.info(bfdate+"日终开始");
						 BatchUtil.submitAndRunBatchTranFlow(taskid_bf, "hx_before", dataArea, false);
					 }else{
						 
						 TaskMainDao.updKsysPlrenw("onprocess", taskid_bf);
						 DBTools.commit();
					 }
					 
					 //等待hx_before跑完
					 while(true){
						 tsp_task tblPlrenw = Tsp_taskDao.selectOne_odb_1(taskid_bf, false);
						 String status = tblPlrenw.getTran_state().toString();
						 if(status.equals("failure")){
							 throw Aplt.E0000(taskid_bf + "hx_before流程运行失败！");
						 }else if(status.equals("success")){
							 break;
						 }else{
							 try{
								 Thread.sleep(1000);
							 }catch(InterruptedException e){
								 e.printStackTrace();
							 }
						 }
					 }
				 }else if(swdate < bfdate && swdate == dydate){
					 
					 String taskid_sw = bfPlrenw.getTask_num();
					 if(CommUtil.equals(bfstat,"success")){
						 taskid_sw = "hx_swday_" + DateTools.covDateToString(DateTools2.addDays(DateTools.covStringToDate(String.valueOf(swdate)), 1)) + "_999";
						 dataArea.getInput().setString("pljylcbs", "hx_swday");
						 BatchUtil.submitAndRunBatchTranFlow(taskid_sw, "hx_swday", dataArea, false);
					 }else{
						 TaskMainDao.updKsysPlrenw("onprocess", taskid_sw);
						 DBTools.commit();
					 }
					 
					 
					 //等待hx_dayend跑完
					 while(true){
						 tsp_task tblPlrenw = Tsp_taskDao.selectOne_odb_1(taskid_sw, false);
						 String status = tblPlrenw.getTran_state().toString();
						 if(status.equals("failure")){
							 throw Aplt.E0000(taskid_sw + "hx_swday流程运行失败！");
						 }else if(status.equals("success")){
							 biglog.info(bfdate+"日终结束");
							 break;
						 }else{
							 try{
								 Thread.sleep(1000);
							 }catch(InterruptedException e){
								 e.printStackTrace();
							 }
						 }
					 }
				 }else if(dydate < swdate && swdate == bfdate){
					 
					 String taskid_dy = swPlrenw.getTask_num();
					 if(CommUtil.equals(swstat,"success")){
						 taskid_dy = "hx_dayend_" + DateTools.covDateToString(DateTools2.addDays(DateTools.covStringToDate(String.valueOf(dydate)), 1)) + "_999";
						 dataArea.getInput().setString("pljylcbs", "hx_dayend");
						 BatchUtil.submitAndRunBatchTranFlow(taskid_dy, "hx_dayend", dataArea, false);
					 }else{
						 TaskMainDao.updKsysPlrenw("onprocess", taskid_dy);
						 DBTools.commit();
					 }
					 
					 
					//等待hx_dayend跑完
					 while(true){
						 tsp_task tblPlrenw = Tsp_taskDao.selectOne_odb_1(taskid_dy, false);
						 String status = tblPlrenw.getTran_state().toString();
						 if(status.equals("failure")){
							 throw Aplt.E0000(taskid_dy + "hx_dayend流程运行失败！");
						 }else if(status.equals("success")){
							 break;
						 }else{
							 try{
								 Thread.sleep(1000);
							 }catch(InterruptedException e){
								 e.printStackTrace();
							 }
						 }
					 }
				 }
			 }
		 } else if(CommUtil.equals(sysid, "420")) {
			 
			 DBTools.commit();
			 
			 while(true){
			 
				//hx_dayend日终日期
				 tsp_task dyPlrenw = TaskMainDao.selKsysPlrenw("gl_dayend", false);
				 String trandt = dyPlrenw.getTask_commit_date();
				 String dystat = dyPlrenw.getTran_state().getValue();
				 String taskid = dyPlrenw.getTask_num();
				 
				//如果日终日期大于等于目标日期，则停止调用
				 if(Integer.parseInt(trandt) >= Integer.parseInt(input.getTargdt())){
					 break;
				 }
				 
				 DataArea dataArea = DataArea.buildWithEmpty();
				 dataArea.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, DateTools.covDateToString(DateTools2.addDays(DateTools.covStringToDate(trandt), 1)));
				 dataArea.getCommReq().setString("farendma", "999");
				 dataArea.getCommReq().setString("corpno", "999");
				 dataArea.getCommReq().setString("tranbr", "898118");
				 dataArea.getCommReq().setString("servno", "999");
				 dataArea.getCommReq().setString("tranus", "9993001");
				 //dataArea.getSystem().setString("corpno", "999");
				 dataArea.getSystem().setString(BatchConfigConstant.BATCH_TRAN_DATE,  DateTools.covDateToString(DateTools2.addDays(DateTools.covStringToDate(trandt), 1)));
				 dataArea.getInput().setString("farendma", "999");
				 
				 if(CommUtil.equals(dystat,"success")){
					 
					 taskid = "gl_dayend_" + DateTools.covDateToString(DateTools2.addDays(DateTools.covStringToDate(String.valueOf(trandt)), 1)) + "_999";
					 dataArea.getInput().setString("pljylcbs", "gl_dayend");
					 BatchUtil.submitAndRunBatchTranFlow(taskid, "gl_dayend", dataArea, false);
				 }else{
					 TaskMainDao.updKsysPlrenw("onprocess", taskid);
					 DBTools.commit();
				 }
				//等待hx_dayend跑完
				 while(true){
					 tsp_task tblPlrenw = Tsp_taskDao.selectOne_odb_1(taskid, false);
					 String status = tblPlrenw.getTran_state().toString();
					 if(status.equals("failure")){
						 throw Aplt.E0000(taskid + "hx_dayend流程运行失败！");
					 }else if(status.equals("success")){
						 DBTools.commit();
						 try{
							 Thread.sleep(18000);
						 }catch(InterruptedException e){
							 e.printStackTrace();
						 }
						 break;
					 }else{
						 try{
							 Thread.sleep(18000);
						 }catch(InterruptedException e){
							 e.printStackTrace();
						 }
					 }
				 }
				 
			 }
		 }
		 
		 
		 
		 
	}

	 
	 
	 
}
/** mdy by zhangan
insert into tsp_task (XITONGBS, FARENDMA, PLJYPICH, PLRWZXPC, PLJYTJRQ, JIAOYIRQ, DQJIOYRQ, PLJYLCBS, LIUCBUZH, PLJYZBSH, PILJYBSS, LJHAOSHI, JIAOYZHT, PLZXMOSH, PLRWZDBZ, PLRWTJSJ, PLRWUYXJ, JYKSSHIJ, KSHISHJC, JYJSSHIJ, JSHISHJC, XUNIJIBS, IPDIZHII, ZHUJIMIC, SHUJUQUU, QSLCBZHA, QSZXXHAO, QSPLJYZU, QSBUZHOU, CUOWXINX, CUOWDUIZ, FUWBIAOZ, ZXTONGBH)
values ('010', '999', 'APAUTO_20180810', '1533649482580', '20171117', to_date('26-09-2018', 'dd-mm-yyyy'), '20180926', null, 0, 'AUTO01', 'apauto', 556506, 'onprocess', '4', null, '2018-07-13 21:36:46', 5, '2018-08-07 21:44:42:573', 1533649482573, '2018-08-07 21:45:48:354', 1533649548354, null, null, null, '{"input":{"targdt":"20180621","farendma":"999","pljypich":"APAUTO_20180809","jiaoyirq":"20180926","pljyzbsh":"AUTO01","xitongbs":"010"},"comm_req":{"farendma":"999","jiaoyirq":"20170922"},"sys":{"jiaoyirq":"20170826","pljypich":"APAUTO_20180809"}}', 0, 0, null, 0, null, null, '10.91.130.34#ubadmin#batAdm01DEV', '01008');


--自动跑批批量交易插入交易控制器表中
insert into ksys_jykzhq (XITONGBS, FARENDMA, PLJYZBSH, BUZHOUHA, PLJIOYMA, PLJYZWMC, ZHIXBZHI, YLPLJYLB, JYYXXKTJ, CHLCISHU, SHBZHDBZ, SHIWTJJG, SHUJCFMS, SHUJCFZJ, PLZYZXMS, ZUIDZYBF, RIZHIJIB, ZUOYCFTJ, PLJYYXMS, NGFTIAOG, SFWENJPL, JIAOYILX)
values ('010', '999', 'AUTO01', 10, 'apauto', '自动跑批', '1', null, null, 0, '1', 1, '9', null, '1', 0, 3, null, '0', '0', '0', null);

INSERT INTO NASDB.KSYS_JYZKZQ
(XITONGBS, FARENDMA, PLJYZBSH, ZUBIEZWM, RWYXXKTJ, RWYXHDFW)
VALUES('010', '999', 'AUTO01', '自动跑批', NULL, NULL);


 */




/**
 * 小总账  跑批参数
insert into tsp_task (XITONGBS, FARENDMA, PLJYPICH, PLRWZXPC, PLJYTJRQ, JIAOYIRQ, DQJIOYRQ, PLJYLCBS, LIUCBUZH, PLJYZBSH, PILJYBSS, LJHAOSHI, JIAOYZHT, PLZXMOSH, PLRWZDBZ, PLRWTJSJ, PLRWUYXJ, JYKSSHIJ, KSHISHJC, JYJSSHIJ, JSHISHJC, XUNIJIBS, IPDIZHII, ZHUJIMIC, SHUJUQUU, QSLCBZHA, QSZXXHAO, QSPLJYZU, QSBUZHOU, CUOWXINX, CUOWDUIZ, FUWBIAOZ, ZXTONGBH)
values ('420', '999', 'APAUTO_20180808888888', '1536847405084', '20171117', to_date('26-09-2018', 'dd-mm-yyyy'), '20180926', null, 0, 'AUTO01', 'apauto', 801172, 'onprocess', '4', null, '2018-07-13 21:36:46', 5, '2018-09-13 22:03:25:062', 1536847405063, '2018-09-13 22:05:10:413', 1536847510413, null, null, null, '{"input":{"targdt":"20190908","farendma":"999","pljypich":"apauto","jiaoyirq":"20180926","pljyzbsh":"AUTO01","xitongbs":"420"},"comm_req":{"servno":"999","corpno":"999","farendma":"999","jiaoyirq":"20170922"},"sys":{"corpno":"999","jiaoyirq":"20170826","pljypich":"APAUTO_20180808888888"}}', 0, 0, null, 0, null, null, '10.91.131.6#ubadmin#batGL01DEV', '42002');

insert into ksys_jykzhq (XITONGBS, FARENDMA, PLJYZBSH, BUZHOUHA, PLJIOYMA, PLJYZWMC, ZHIXBZHI, YLPLJYLB, JYYXXKTJ, CHLCISHU, SHBZHDBZ, SHIWTJJG, SHUJCFMS, SHUJCFZJ, PLZYZXMS, ZUIDZYBF, RIZHIJIB, ZUOYCFTJ, PLJYYXMS, NGFTIAOG, SFWENJPL, JIAOYILX)
values ('420', '999', 'AUTO01', 10, 'apauto', '自动跑批', '1', null, null, 1, '1', 1, '9', null, '1', 0, 3, null, '0', '0', '0', '1');

insert into ksys_jyzkzq (XITONGBS, FARENDMA, PLJYZBSH, ZUBIEZWM, RWYXXKTJ, RWYXHDFW)
values ('420', '999', 'AUTO01', '自动跑批', null, null);

 * */
