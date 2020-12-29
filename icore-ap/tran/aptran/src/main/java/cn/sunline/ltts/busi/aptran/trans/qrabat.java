package cn.sunline.ltts.busi.aptran.trans;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.type.ApBook.ApAuditInfo;
import cn.sunline.ltts.busi.aptran.namedsql.AppPropDao;
import cn.sunline.ltts.busi.aptran.trans.intf.Qrabat.Output.Apatif;
import cn.sunline.ltts.busi.sys.errors.ApError;

public class qrabat {

	public static void qrabat(
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrabat.Input input,
			final cn.sunline.ltts.busi.aptran.trans.intf.Qrabat.Output output) {
		
		if(CommUtil.isNull(input.getTrandt()) && CommUtil.isNull(input.getTransq()) &&CommUtil.isNull(input.getLttscd()) &&CommUtil.isNull(input.getTrsvtp()) &&CommUtil.isNull(input.getUserno())){
			throw ApError.Aplt.E0000("输入条件不可同时为空");
		}
		
		long count = (CommTools.prcRunEnvs().getPgsize()<=0)? 10:CommTools.prcRunEnvs().getPgsize();//y容量		
		long start = (CommTools.prcRunEnvs().getPageno()<=0)? 0:(CommTools.prcRunEnvs().getPageno() - 1) * count;//页码
		
		
		List<ApAuditInfo> apAuditInfo= AppPropDao.GetApDataAudit(input.getTrandt(), input.getTransq(), input.getLttscd(),
				input.getTrsvtp(), input.getUserno(), start, count, false);
		
		if(CommUtil.isNull(apAuditInfo)){	
			throw ApError.Aplt.E0000("未查询到相关记录");
		}else{
			long sumcount=AppPropDao.SelCountApAudit(input.getTrandt(), input.getTransq(), input.getLttscd(),
					input.getTrsvtp(), input.getUserno(), false);
			output.setCount(sumcount);
			CommTools.prcRunEnvs().setCounts(sumcount);
			
			for(ApAuditInfo apAI:apAuditInfo){	
				Apatif ap=SysUtil.getInstance(Apatif.class);
				ap.setAudseq(apAI.getAudseq());
				ap.setBusisq(apAI.getBusisq());
				ap.setLttscd(apAI.getLttscd());
				ap.setPrcsna(apAI.getPrcsna());
				ap.setTrsvtp(apAI.getTrsvtp());
				ap.setTablds(apAI.getTablds());
				ap.setTablna(apAI.getTablna());
				ap.setTrandt(apAI.getTrandt());
				ap.setTransq(apAI.getTransq());
				ap.setUserno(apAI.getUserno());
				output.getApatif().add(ap);	
			}	
		}
	}
}
