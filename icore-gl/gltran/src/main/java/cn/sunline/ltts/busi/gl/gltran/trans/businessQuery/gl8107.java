package cn.sunline.ltts.busi.gl.gltran.trans.businessQuery;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.gltran.trans.businessQuery.intf.Gl8107.Output.List01;
import cn.sunline.ltts.busi.gl.namedsql.GlReportDao;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.gl.gl.tables.TabGLReport.gls_report;
import cn.sunline.ltts.sys.dict.GlDict;


public class gl8107 {

    private static final BizLog BIZLOG = BizLogUtil.getBizLog(gl8106.class);
	public static void query( final cn.sunline.ltts.busi.gl.gltran.trans.businessQuery.intf.Gl8107.Input input,  final cn.sunline.ltts.busi.gl.gltran.trans.businessQuery.intf.Gl8107.Property property,  final cn.sunline.ltts.busi.gl.gltran.trans.businessQuery.intf.Gl8107.Output output){
		BIZLOG.debug("input[%s]", input);
		BIZLOG.method("gl8107.query begin>>>>>>>>>>>>>>>>>>");
		
		//非空检查并获取公共变量
        CommTools.fieldNotNull(input.getTrxndt(), GlDict.A.trxn_date.getId(), GlDict.A.trxn_date.getLongName());
        RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
        long pageno = runEnvs.getPageno();
        long pgsize = runEnvs.getPgsize();
		
        Page<gls_report> page = GlReportDao.lstGlsReport(input.getTrxndt(), input.getBrchno(), 
        		runEnvs.getCorpno(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
        
        List01 list01 = null;
        for(gls_report item:page.getRecords()){
            list01 = SysUtil.getInstance(List01.class);
            CommUtil.copyProperties(list01, item);
            output.getList01().add(list01);
        }
        
        runEnvs.setCounts(page.getRecordCount());
        
		BIZLOG.method("gl8107.query end>>>>>>>>>>>>>>>>>>");
	}
}
