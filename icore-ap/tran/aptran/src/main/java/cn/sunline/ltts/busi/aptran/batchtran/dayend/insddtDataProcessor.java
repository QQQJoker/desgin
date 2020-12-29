
package cn.sunline.ltts.busi.aptran.batchtran.dayend;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdt;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdtDao;
import cn.sunline.ltts.gns.AcdtImpl;
import cn.sunline.ltts.gns.api.AcdtApi;
import cn.sunline.ltts.gns.api.AcdtInf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
	 /**
	  * 添加账务日期
	  * @author 
	  * @Date 
	  */

public class insddtDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Insddt.Input, cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Insddt.Property> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(insddtDataProcessor.class);
  
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Insddt.Input input, cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Insddt.Property property) {
		//TODO
		 //当前日期加一天
		 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 SimpleDateFormat formatter2 = new SimpleDateFormat("yyyyMMdd");
		 //获取日期表最后一条记录
		 AcdtApi acdt =new AcdtImpl();
		 AcdtInf acdtinf=acdt.getAcdt();
		try {
			//获取生效日期202001102 00：00:00
			Date date = formatter.parse(acdtinf.getValidt());
			Calendar cld = Calendar.getInstance();
			cld.setTime(date); 
			cld.add(Calendar.DATE, 1); 
			String validt = formatter.format(cld.getTime());
			//获取上上次交易日期20201028
			Date date1 = formatter2.parse(acdtinf.getBflsdt());
			Calendar cld1 = Calendar.getInstance();
			cld1.setTime(date1); //20201028
			cld1.add(Calendar.DATE, 1);//20201029
			String bflsdt = formatter2.format(cld1.getTime());
			//获取上次交易日期
			cld1.add(Calendar.DATE, 1);//20201030
			String lastdt = formatter2.format(cld1.getTime());
			//获取系统日期
			cld1.add(Calendar.DATE, 1);//20201031
			String systdt = formatter2.format(cld1.getTime());
			//获取下一天日期
			cld1.add(Calendar.DATE, 1);//20201101
			String nextdt = formatter2.format(cld1.getTime());
			//获取下下天日期
			cld1.add(Calendar.DATE, 1);//20201102
			String afnxdt = formatter2.format(cld1.getTime());
			//获取年终结算日期
			//获取年份
			String year = validt.substring(0, 4);
			String time = "1231";
			String yreddt = year.concat(time);
			//获取时间戳
			String upddtm = formatter.format(new Date());
			//打印日志
			bizlog.debug("validt="+validt+"bflsdt="+bflsdt+"lastdt="+lastdt+"systdt="+systdt+"nextdt="+nextdt+"afnxdt="+afnxdt+"yreddt="+yreddt+"upddtm="+upddtm);
			//根据生效日期查表看是否有记录
			AppAcdt acdt2 =  AppAcdtDao.selectOne_odb1(validt, false);
			//为空就新增
			if(CommUtil.isNull(acdt2)) {
				 AppAcdt appAcdt = SysUtil.getInstance(AppAcdt.class);
				 appAcdt.setValidt(validt);
				 appAcdt.setBflsdt(bflsdt);
				 appAcdt.setLastdt(lastdt);
				 appAcdt.setSystdt(systdt);
				 appAcdt.setNextdt(nextdt);
				 appAcdt.setAfnxdt(afnxdt);
				 appAcdt.setYreddt(yreddt);
				 appAcdt.setUpdttm(upddtm);
				 AppAcdtDao.insert(appAcdt);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}



