//package cn.sunline.ltts.busi.aptran.trans;
//
//import cn.sunline.ltts.biz.out.tables.JTATable.kaps_zjyils;
//import cn.sunline.ltts.busi.sys.errors.ApError;
//
//public class api112 {
//
//    public static void commit2(final cn.sunline.ltts.busi.aptran.transdef.Api112.Input Input, final cn.sunline.ltts.busi.aptran.transdef.Api112.Property Property,
//            final cn.sunline.ltts.busi.aptran.transdef.Api112.Output Output) {
////        kaps_zjyils userTsn = JtaUtils.selectUserTsnById(Input.getYszjylsh());
//    	 kaps_zjyils userTsn=null;
//        if (userTsn == null)//说明db已经rollback，直接跳出
//            throw ApError.Aplt.E0000("异步解止付时,未找到主交易流水");
//        //加载子事务日志
//        try {
////            JtaUtils.process2Commit(userTsn);
//        } catch (Exception e) {
//            throw ApError.Aplt.E0000("异步解止付失败", e);
//        }
//    }
//}
