
package cn.sunline.ltts.busi.gl.gltran.trans.businessQuery;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.gltran.trans.businessQuery.intf.Gl8106.Output.List01;
import cn.sunline.ltts.busi.gl.namedsql.GlTransferProfitDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.glb_income_seq;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class gl8106 {

    private static final BizLog bizlog = BizLogUtil.getBizLog(gl8106.class);
    
    public static void query( final cn.sunline.ltts.busi.gl.gltran.trans.businessQuery.intf.Gl8106.Input input,  
            final cn.sunline.ltts.busi.gl.gltran.trans.businessQuery.intf.Gl8106.Property property,  final cn.sunline.ltts.busi.gl.gltran.trans.businessQuery.intf.Gl8106.Output output){
        bizlog.debug("input[%s]", input);
        bizlog.method("gl8106.query begin>>>>>>>>>>>>>>>>>>");
        
        //非空检查
        RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
        long pageno = runEnvs.getPageno();
        long pgsize = runEnvs.getPgsize();
		
        //查询损益结转明细登记簿
        Page<glb_income_seq> page = GlTransferProfitDao.lstGlbIncomeSeq(input.getTrxndt(), input.getBrchno(), 
        		runEnvs.getCorpno(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
        
        List01 list01 = null;
        for(glb_income_seq item:page.getRecords()){
            list01 = SysUtil.getInstance(List01.class);
            CommUtil.copyProperties(list01, item);
            output.getList01().add(list01);
        }
        
        runEnvs.setCounts(page.getRecordCount());
        
        bizlog.method("gl8106.query end>>>>>>>>>>>>>>>>>>");
    }
}
