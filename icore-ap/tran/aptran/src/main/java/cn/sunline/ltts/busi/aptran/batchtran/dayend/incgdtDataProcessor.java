
package cn.sunline.ltts.busi.aptran.batchtran.dayend;

import cn.sunline.ltts.acdt.ApAcdt.AppAcdt;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdtDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Incgdt.Input;
import cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Incgdt.Property;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.gns.AcdtHolder;
import cn.sunline.ltts.gns.AcdtImpl;
import cn.sunline.ltts.gns.api.AcdtApi;
import cn.sunline.ltts.gns.api.AcdtInf;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;

/**
 * 添加账务日期（测试环境）
 * 
 * @author
 * @Date
 */

public class incgdtDataProcessor extends
		BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Incgdt.Input, cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Incgdt.Property> {

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Incgdt.Input input, cn.sunline.ltts.busi.aptran.batchtran.dayend.intf.Incgdt.Property property) {
		 
		 SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 AcdtApi acdt =new AcdtImpl();
		 AcdtInf acdtinf=acdt.getNewAcdt();
		 
		 try {
			 // 增加测试环境代码：如果是测试环境，则直接切日，校验时间以当前时间为准
			 KnpPara knpPara = KnpParaDao.selectOne_odb1("Day.Parms", "incgdt", "environment", "isTest", CommTools.prcRunEnvs().getCorpno(), false);
			 if(CommUtil.isNotNull(knpPara) && CommUtil.equals("1", knpPara.getPmval1())) {
				 
				 knpPara.setPmval3(CommTools.prcRunEnvs().getTrandt());
				 KnpParaDao.updateOne_odb1(knpPara);		// 之前生效日期
				 
				 String validTime = formatter.format(new Date());
				 AppAcdt entity = SysUtil.getInstance(AppAcdt.class);
				 entity.setValidt(validTime);
				 entity.setBflsdt(acdtinf.getLastdt());
				 entity.setLastdt(acdtinf.getSystdt());
				 entity.setSystdt(acdtinf.getNextdt());
				 entity.setNextdt(acdtinf.getAfnxdt());
				 entity.setAfnxdt(DateTools.calDateByTerm(acdtinf.getAfnxdt(), "1D"));
				 entity.setYreddt(DateTools.calDateByFreq(acdtinf.getSystdt(), "1YAE"));
				 entity.setUpdttm(validTime);
				 AppAcdtDao.insert(entity);
				 
				 knpPara.setPmval4(entity.getSystdt());		// 当前生效日期
				 KnpParaDao.updateOne_odb1(knpPara);
			 }
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
		 
	}
	 
	 @Override
	public void afterTranProcess(String taskId, Input input, Property property) {
		 /*AcdtHolder.reflushDate();
		 KnpPara knpPara = KnpParaDao.selectOne_odb1("Day.Parms", "incgdt", "environment", "isTest", CommTools.prcRunEnvs().getCorpno(), false);
		 if(CommUtil.isNotNull(knpPara) && CommUtil.equals("1", knpPara.getPmval1())) {
			 knpPara.setPmval5(CommTools.prcRunEnvs().getTrandt());		// 当前生效日期
			 KnpParaDao.updateOne_odb1(knpPara);
		 }*/
	}
}
