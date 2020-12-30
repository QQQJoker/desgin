package cn.sunline.ltts.busi.gl.fatran.trans.subject;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.fa.accounting.FaSetAccountApply;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;


public class fa8104 {
    private static final BizLog BIZLOG = BizLogUtil.getBizLog(fa8104.class);
    /**
     * <p>Title:queryAccountingSubject </p>
     * <p>Description:	科目信息查询</p>
     * @author MZA
     * @date   2018年2月4日 
     * @param input
     * @param property
     * @param output
     */
    public static void queryAccountingSubject( final cn.sunline.ltts.busi.gl.fatran.trans.subject.intf.Fa8104.Input input,  final cn.sunline.ltts.busi.gl.fatran.trans.subject.intf.Fa8104.Property property,  final cn.sunline.ltts.busi.gl.fatran.trans.subject.intf.Fa8104.Output output){
        BIZLOG.method("queryAccountingSubject begin>>>>>>>>>");
        fap_accounting_subject fapSubject = SysUtil.getInstance(fap_accounting_subject.class);
        //判断科目号是否存在
        if(CommUtil.isNotNull(input.getGl_code())){
            fapSubject = Fap_accounting_subjectDao.selectOne_odb1(input.getGl_code(), false);
            if(CommUtil.isNull(fapSubject)){
                throw GlError.GL.E0201(input.getGl_code());
            }
        }else{
            throw GlError.GL.E0201(input.getGl_code());
        }
        //设置输出参数
        output.setGl_code_desc(fapSubject.getGl_code_desc());
        output.setDebit_manual_allow(fapSubject.getDebit_manual_allow());
        output.setCredit_manual_allow(fapSubject.getCredit_manual_allow());
        
        
        BIZLOG.method("queryAccountingSubject end>>>>>>>>>");
        
        
    }
}
