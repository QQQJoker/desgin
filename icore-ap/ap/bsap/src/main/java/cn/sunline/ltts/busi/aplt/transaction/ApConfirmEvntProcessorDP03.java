package cn.sunline.ltts.busi.aplt.transaction;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApConfirmEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.dp.IoDpFrozSvcType;
import cn.sunline.ltts.busi.iobus.type.dp.IoDpFrozComplexType.IoDpStUfIn;

/**
 * 存入保全解付
 * 
 * @author cuijia
 *
 */
public class ApConfirmEvntProcessorDP03 implements ApConfirmEvntProcessor {

	@Override
	public void process(KnbEvnt evnt) {
		IoDpStUfIn unfrozn = SysUtil.getInstance(IoDpStUfIn.class);
		//unfrozn.setInpusq(evnt.getCallsq());
		//unfrozn.setInpudt(evnt.getTrandt());
		//外调交易不登记防重流水表,且无法取得上送系统日期,所以二次提交直接使用交易流水和交易日期
		//unfrozn.setMntrsq(evnt.getTransq());
		unfrozn.setMntrsq(evnt.getMntrsq());
		unfrozn.setTrandt(evnt.getTrandt());
		SysUtil.getInstance(IoDpFrozSvcType.class).IoDpStUf(unfrozn);
		
	}

}
