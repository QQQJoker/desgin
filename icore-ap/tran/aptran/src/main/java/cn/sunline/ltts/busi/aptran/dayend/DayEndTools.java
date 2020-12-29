package cn.sunline.ltts.busi.aptran.dayend;

import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_PILJYZHT;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.custom.comm.biz.util.DcnDataUtil;
import cn.sunline.adp.cedar.custom.comm.servicetype.DayEndBatch;
import cn.sunline.adp.cedar.custom.comm.type.DayEndBatchType.TaskProcessIn;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.Tsp_date_managementDao;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.Tsp_taskDao;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.tools.GlDateTools;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class DayEndTools {
public static String getDayEndDate(String corpno,String flowType){
		
		String trandt = null;
		
		KSysBatchTable.tsp_date_management tblKsysPlrzrqgl = KSysBatchTable.Tsp_date_managementDao.selectOne_odb_1(flowType, corpno, false);
		
		if(tblKsysPlrzrqgl == null||CommUtil.equals(tblKsysPlrzrqgl.getDayend_flow_status(), "success")){
	    	if(StringUtil.isNotEmpty(flowType) && flowType.startsWith("gl_dayend")) {
	    		trandt = GlDateTools.getGlDateInfo().getSystdt();
	    	}else if(StringUtil.isNotEmpty(flowType) && flowType.startsWith("hx")){
	    		trandt = DateTools.getDateInfo().getSystdt();
	    	}

			//为空则新增日期信息
	    	if(tblKsysPlrzrqgl == null){
	    		DcnDataUtil.registerPlrzrqgl(flowType, trandt, corpno, "");
	    	}
	    }else{
	    	trandt =  tblKsysPlrzrqgl.getDayend_flow_date();
	    }
		return trandt;
	}
	
	//开始流程
	public static void beginFlow(String corpno,String flowType){
		String edctdt = getDayEndDate(corpno, flowType);
		
		//查询当天查询日期日终运行状态
		String queryBatchId = flowType + "_" + edctdt;
		KSysBatchTable.tsp_task queryRunInfo = Tsp_taskDao.selectOne_odb_1(queryBatchId, false);
		if(CommUtil.isNotNull(queryRunInfo)){
			if(queryRunInfo.getTran_state() != E_PILJYZHT.failure){
				throw ApError.Aplt.E0000("批量流程["+queryBatchId+"]正在运行，不允许再次提交");
			}
		}
		
		//总账日终日期不能大于核心日期
		if(CommUtil.equals(flowType, "gl_dayend") && (CommUtil.compare(edctdt, getDayEndDate(corpno,"hx_dayend")) >= 0)){
			throw ApError.Aplt.E0000("总账日终日期["+edctdt+"]必须小于核心日终日期["+getDayEndDate(corpno,"hx_dayend")+"]");
		}

		//更新日终日期及状态
		KSysBatchTable.tsp_date_management tblKsysPlrzrqgl = KSysBatchTable.Tsp_date_managementDao.selectOne_odb_1(flowType, corpno, false);
		tblKsysPlrzrqgl.setDayend_flow_date(edctdt);
		tblKsysPlrzrqgl.setDayend_flow_status(E_PILJYZHT.processing.getValue());
		Tsp_date_managementDao.updateOne_odb_1(tblKsysPlrzrqgl);
		
		TaskProcessIn dotaskin = SysUtil.getInstance(TaskProcessIn.class);
		 dotaskin.setQuery_date(edctdt);
		 dotaskin.setCorporate_code(corpno);
		 dotaskin.setTran_flow_id(flowType);
		 DayEndBatch dayendbatch = SysUtil.getInstance(DayEndBatch.class);
		 dayendbatch.doTaskProcess(dotaskin);
	}
}
