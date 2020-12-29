//package cn.sunline.ltts.busi.aptran.trans;
//
//import java.math.BigDecimal;
//
//
//import cn.sunline.ltts.biz.global.CommUtil;
//import cn.sunline.ltts.biz.global.SysUtil;
//import cn.sunline.ltts.busi.aplt.coderule.ApTransSerail;
//import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
//import cn.sunline.ltts.busi.aplt.tools.CommTools;
//import cn.sunline.ltts.busi.aplt.type.ApDefineType.ApBusiInfo;
//import cn.sunline.ltts.busi.aptran.transdef.Api999;
//import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_HOLZJZBZ;
//import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_JIEDAIBZ;
//import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_JIZHFANX;
//import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_JZFXLEIX;
//import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_TIZHLEIX;
//import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_XIANZHBZ;
//
//public class api999 {
//
//    public static void prcBusineseField(final Api999.Input Input, final Api999.Property Property,
//            final Api999.Output Output) {
//
//        ApBusiInfo cplInput = SysUtil.getInstance(ApBusiInfo.class);
//
//        cplInput.setJiaoyikh(Input.getJiaoyikh());
//        cplInput.setJiaoyizh(Input.getJiaoyizh());// 交易账号
//        cplInput.setCrcycd(Input.getCrcycd());// 货币代号
//        cplInput.setJiaoyije(Input.getJiaoyije());// 转出金额
//        cplInput.setBeizhuxx(Input.getBeizhuxx());// 备注信息
//        cplInput.setBeiyzid1(Input.getCuozhriq());// 备用字段1
//        //cplInput.setBeiyzid2(Input.getDuifzhho());// 备用字段2
//        //cplInput.setBeiyzid3(Input.getDfzhhumc());// 备用字段3
//        //cplInput.setBeiyzid4(BigDecimal.ZERO);// 备用字段4
//        //cplInput.setBeiyzid5(BigDecimal.ZERO);// 备用字段5
//        //cplInput.setBeiyzid6(BigDecimal.ZERO);// 备用字段6
//
//        ApTransSerail.prcBusineseField(cplInput);
//        Output.setChulxinx("调账交易处理成功！");
//
//    }
//
//    public static void calRelTranam(final cn.sunline.ltts.busi.aptran.transdef.Api999.Input Input, final cn.sunline.ltts.busi.aptran.transdef.Api999.Property Property,
//            final cn.sunline.ltts.busi.aptran.transdef.Api999.Output Output) {
//
//        if (Input.getXianzzbz() == E_XIANZHBZ.XJ) {
//            //throw Dept.E9999("目前本交易不支持现金业务");
//        }
//        if (Property.getZijlaylx() == E_JZFXLEIX.DXZ
//                && Property.getZijquxlx() == E_JZFXLEIX.DXZ) {
//            //throw Dept.E9999("目前本交易不支持代销账对代销账业务");
//        }
//        if (CommUtil.compare(Input.getJiaoyije(), BigDecimal.ZERO) <= 0) {
//            //throw Dept.E9999("交易金额必须大于零!");
//        }
//        if (CommUtil.isNull(Input.getCuozhriq())) {
//            if (CommUtil.compare(Input.getCuozhriq(), CommToolsAplt.prcRunEnvs().getTrandt()) > 0) {
//               // throw Dept.E9999("错账日期不能大于交易日期!");
//            }
//        }
//        //平衡检查(简单) 
//        if (Input.getJyjdaibz() == Input.getDfjdaibz()) {
//            if (Input.getJyhljzbz() == Input.getDfhljzbz()) {
//                //throw Dept.E9999("借贷方向相同则红蓝字不能相同!");
//            }
//        }
//        else {
//            if (Input.getJyhljzbz() != Input.getDfhljzbz()) {
//                //throw Dept.E9999("借贷方向不相同则红蓝字必须相同!");
//            }
//        }
//
//        //因为内部户和代销账都没有红蓝字标志
//        if (Input.getJyhljzbz() == E_HOLZJZBZ.HZ) {
//            Property.setZchujine(Input.getJiaoyije().negate());// 可用余额
//        }
//        else {
//            Property.setZchujine(Input.getJiaoyije());// 交易金额
//        }
//
//        if (Input.getDfhljzbz() == E_HOLZJZBZ.HZ) {
//            Property.setZhrujine(Input.getJiaoyije().negate());// 可用余额
//        } else {
//            Property.setZhrujine(Input.getJiaoyije());// 交易金额
//        }
//        //输出摘要
//        if (Input.getTizhleix() == E_TIZHLEIX.CHCZ) {
//            Property.setZhaiyodm(ApUtil.SMRY_CODE_REPAIR);
//            Property.setZhaiyoms(E_TIZHLEIX.CHCZ.getLongName());
//        } else {
//            Property.setZhaiyodm(ApUtil.SMRY_CODE_AJUST);
//            Property.setZhaiyoms(E_TIZHLEIX.CZTZ.getLongName());
//        }
//        //借贷标志
//        if (Input.getJyjdaibz() == E_JIZHFANX.D) {
//            Property.setZchujdbz(E_JIEDAIBZ.D);
//        } else if (Input.getJyjdaibz() == E_JIZHFANX.C) {
//            Property.setZchujdbz(E_JIEDAIBZ.C);
//        }
//
//        if (Input.getDfjdaibz() == E_JIZHFANX.D) {
//            Property.setZhrujdbz(E_JIEDAIBZ.D);
//        } else if (Input.getDfjdaibz() == E_JIZHFANX.C) {
//            Property.setZhrujdbz(E_JIEDAIBZ.C);
//        }
//    }
//
// 
// 
//}
