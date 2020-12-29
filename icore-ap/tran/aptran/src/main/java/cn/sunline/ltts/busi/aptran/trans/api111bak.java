//package cn.sunline.ltts.busi.aptran.trans;
//
//import cn.sunline.ltts.busi.aplt.tools.CommTools;
//import cn.sunline.ltts.busi.aplt.tools.LogManager;
//import cn.sunline.ltts.busi.aptran.transdef.Api100;
//import cn.sunline.ltts.busi.aptran.transdef.Api111.Input;
//import cn.sunline.ltts.core.api.logging.BizLog;
//
//public class api111 {
//	private static final BizLog bizlog = LogManager.getBizLog(api111.class);
//
//	public static void prcBusineseField(final cn.sunline.ltts.busi.aptran.transdef.Api111.Input Input,
//			final cn.sunline.ltts.busi.aptran.transdef.Api111.Property Property,
//			final cn.sunline.ltts.busi.aptran.transdef.Api111.Output Output) {
//
//		String transq = Input.getTransq();
//
//		/*
//		 * if(CommTools.isCounterChannel()){
//		 * 
//		 * IoKapsGmjyls cplUsInfo=
//		 * CommTools.getInstance(IoApReverseImpl.class).qryKapsGmjylsInfo
//		 * (CommToolsAplt.prcRunEnvs().getTrandt(), Input.getTransq(),null);
//		 * 
//		 * if(CommUtil.isNull(cplUsInfo)){
//		 * 
//		 * throw DeptAcct.E9999("柜员流水["+Input.getTransq()+"]信息未找到！"); }
//		 * 
//		 * transq=cplUsInfo.getMtrasq();//主交易流水信息
//		 * 
//		 * }
//		 */
//		String ret = Api100.prcBusineseField2(transq, Input.getReason());
//		// 判断渠道类型是否柜面
//		if (CommTools.isCounterChannel()) {
//			// 登记冲正流水登记簿
//			Api100.registKapsStrk(transq, Input.getReason());
//		}
//		Output.setChulxinx(ret);
//	}
//
//	public static void strike(final cn.sunline.ltts.busi.aptran.transdef.Api111.Input input,
//			final cn.sunline.ltts.busi.aptran.transdef.Api111.Property property,
//			final cn.sunline.ltts.busi.aptran.transdef.Api111.Output output) {
//		
//		CommToolsAplt.prcRunEnvs().setRemark(Input.getReason());// 把备注带到账单层
//		String transq = Input.getTransq();
//		bizlog.debug("冲正交易开始==========");
//		/*
//		 * if(CommTools.isCounterChannel()){
//		 * 
//		 * IoKapsGmjyls cplUsInfo=
//		 * CommTools.getInstance(IoApReverseImpl.class).qryKapsGmjylsInfo
//		 * (CommToolsAplt.prcRunEnvs().getTelldt(), Input.getTransq(),null);
//		 * 
//		 * if(CommUtil.isNull(cplUsInfo)){
//		 * 
//		 * throw DeptAcct.E9999("柜员流水["+Input.getTransq()+"]信息未找到！"); }
//		 * 
//		 * transq=cplUsInfo.getMtrasq();//主交易流水信息
//		 * 
//		 * }
//		 */
//		Api100.strike2(transq, Input.getIsmani(), Input.getStacps());
//	}
//}
