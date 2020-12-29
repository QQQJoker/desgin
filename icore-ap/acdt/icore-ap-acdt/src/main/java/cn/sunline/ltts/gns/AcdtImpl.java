package cn.sunline.ltts.gns;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdt;
import cn.sunline.ltts.gns.api.AcdtApi;
import cn.sunline.ltts.gns.api.AcdtInf;

@Component
public class AcdtImpl implements AcdtApi {

	@Override
	public String getCurrentDate() {
		Date date=new Date();
		return getCurrentDate(date);
	}

	@Override
	public AcdtInf getAcdt() {
		Date date=new Date();
		return getAcdt(date);
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCurrentDate(Date ntpdate) {
		// TODO Auto-generated method stub
		return getAcdt(ntpdate).getSystdt();
	}

	@Override
	public String getCurrentDate(String ntpdate) {
		// TODO Auto-generated method stub
		return getAcdt(ntpdate).getSystdt();
	}

	@Override
	public AcdtInf getAcdt(Date ntpdate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return getAcdt(formatter.format(ntpdate));
	}

	@Override
	public AcdtInf getAcdt(String ntpdate) {
		AppAcdt acdt=AcdtHolder.getAcdt(ntpdate);
		AcdtInf acdtInf=SysUtil.getInstance(AcdtInf.class);
		acdtInf.setAfnxdt(acdt.getAfnxdt());
		acdtInf.setBflsdt(acdt.getBflsdt());
		acdtInf.setLastdt(acdt.getLastdt());
		acdtInf.setNextdt(acdt.getNextdt());
		acdtInf.setSystdt(acdt.getSystdt());
		acdtInf.setYreddt(acdt.getYreddt());
		acdtInf.setValidt(acdt.getValidt());
		return acdtInf;
	}
	
	@Override
	public AcdtInf getNewAcdt() {
		AppAcdt acdt=AcdtHolder.getNewAcdt();
		AcdtInf acdtInf=SysUtil.getInstance(AcdtInf.class);
		acdtInf.setAfnxdt(acdt.getAfnxdt());
		acdtInf.setBflsdt(acdt.getBflsdt());
		acdtInf.setLastdt(acdt.getLastdt());
		acdtInf.setNextdt(acdt.getNextdt());
		acdtInf.setSystdt(acdt.getSystdt());
		acdtInf.setYreddt(acdt.getYreddt());
		acdtInf.setValidt(acdt.getValidt());
		return acdtInf;
	}
	
}
