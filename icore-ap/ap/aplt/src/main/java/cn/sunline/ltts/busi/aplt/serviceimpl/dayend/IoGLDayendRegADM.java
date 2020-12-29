package cn.sunline.ltts.busi.aplt.serviceimpl.dayend;

import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_PILJYZHT;
import cn.sunline.adp.cedar.busi.sdk.biz.global.DateTimeUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.custom.comm.biz.util.DcnDataUtil;
import cn.sunline.adp.cedar.custom.comm.type.DayEndBatchType.TaskProcessIn;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
 /**
  * 小总账日终执行成功汇报ADM
  *
  */
@cn.sunline.adp.core.annotation.Generated
public class IoGLDayendRegADM implements cn.sunline.ltts.busi.iobus.servicetype.ap.IoApEvent{
 /**
  * 通用的事件处理服务
  *
  */
	public void onEvent(String shijneir){ 
		TaskProcessIn dotaskin = SysUtil.getInstance(TaskProcessIn.class);
		dotaskin = SysUtil.deserialize(shijneir, TaskProcessIn.class);
		String gl_chaxriqi = dotaskin.getQuery_date();
//		String hx_chaxriqi = DcnDataUtil.queryAllDcnDayEnd(dotaskin.getCorporate_code(), "hx_dayend");
		 String last_gl_chaxriqi = DateTimeUtil.mountDate(gl_chaxriqi, 1);
		/****
		 * IMS指标字段定义
		 */
		String flowId = ApConstants.gl_dayend;
		String returnMessage = null;
		
		//TODO:更新核心换日后日期信息,独立事务
		KSysBatchTable.tsp_date_management plrzrqgl = KSysBatchTable.Tsp_date_managementDao.selectOne_odb_1(dotaskin.getTran_flow_id(), dotaskin.getCorporate_code(), false);
	    if(plrzrqgl == null)
	    	DcnDataUtil.registerPlrzrqgl(dotaskin.getTran_flow_id(), last_gl_chaxriqi, dotaskin.getCorporate_code(), E_PILJYZHT.success.getValue());
	    else
			DcnDataUtil.updatePlrzrqgl(dotaskin.getTran_flow_id(), last_gl_chaxriqi, dotaskin.getCorporate_code());
	    
	   
//	    returnMessage = "日终日期:["+ last_gl_chaxriqi +"] 小总账日终执行成功！";
//		RealReportUtil.alertIMSMsg("flow", flowId, "success", returnMessage);
		
//		//20150202 添加控制是否发送总账日终开始执行请求
//		if(BatchTools.canRunGLDayEnd(E_PLKZHIBZ.DZ.getValue())){
//			BatchTools.addOrUpdBatchCondition(E_PLKZHIBZ.DZ.getValue(), E_SHIFOUBZ.NO.getValue());
//		}
		
//		//判断小总账系统的当前日终日期与核心系统的日终日期比较，当小总账系统的日终日期大于等于核心系统的日终日期时不作处理。
//		if(CommUtil.compare(gl_chaxriqi, hx_chaxriqi) == 0 || CommUtil.compare(gl_chaxriqi, hx_chaxriqi) > 0){
//			//小总帐的当前日终日期大于或者是等于核心的日终日期汇报
//			returnMessage = "日终日期:["+ last_gl_chaxriqi +"] 小总账日终执行成功, 大于或等于核心的日终日期["+hx_chaxriqi+"]不继续执行小总账日终！";
//			RealReportUtil.alertIMSMsg("flow", flowId, "success", returnMessage);
//			
//			//20150129 添加控制是否发送总账日终开始执行请求
//			if(BatchTools.canRunGLDayEnd(E_PLKZHIBZ.DZ.getValue())){
//				BatchTools.addOrUpdBatchCondition(E_PLKZHIBZ.DZ.getValue(), E_SHIFOUBZ.NO.getValue());
//			}
//			return;
//		}else{
//			returnMessage = "日终日期:["+ last_gl_chaxriqi +"] 小总账日终执行成功, 小于当前的核心日终日期["+hx_chaxriqi+"],将继续执行下一日小总账日终的日终批量";
//			RealReportUtil.alertIMSMsg("flow", flowId, "success", returnMessage);
////			BatchTools.fireGLEvent("dayendRepGL", shijneir);
//			
//			//20140124 往GL注册核心日终批量完成通知
//			//TODO:是否要循环执行？？？？
//			if(BatchTools.canRunGLDayEnd(E_PLKZHIBZ.DZ.getValue())){
//				BatchTools.fireGLService("GLBatchBegin", gl_chaxriqi);
//			}
//		}
		
	}
	
}

