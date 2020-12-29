package cn.sunline.ltts.busi.aptran.batchtran;

import java.util.List;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataProcessorWithoutDataItem;
import cn.sunline.ltts.busi.ap.iobus.type.IoApReverseType.IoApReverseIn;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsRedu;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsReduDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.aplt.transaction.ApStrike;
import cn.sunline.ltts.busi.bsap.namedsql.SysPublicDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TXNSTS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;


	 /**
	  * 批量异步冲正，联机交易冲正失败或者超时的情况下，定时轮询冲正
	  *
	  */
public class api111batDataProcessor extends
  BatchDataProcessorWithoutDataItem<cn.sunline.ltts.busi.aptran.batchtran.Api111bat.Input, cn.sunline.ltts.busi.aptran.batchtran.Api111bat.Property> {
	private static final BizLog bizlog = LogManager.getBizLog(api111batDataProcessor.class);
	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param input 批量交易输入接口
	 * @param property 批量交易属性接口
	 */
	 @Override
	public void process(cn.sunline.ltts.busi.aptran.batchtran.Api111bat.Input input, cn.sunline.ltts.busi.aptran.batchtran.Api111bat.Property property) {
		String corpno = CommTools.prcRunEnvs().getCorpno();
		String trandt = CommTools.prcRunEnvs().getTrandt();
		List<KnsRedu> failKnsRedu = SysPublicDao.selFailKnsRedu(corpno, trandt, false);
		for(KnsRedu knsRedu : failKnsRedu) {
			CommTools.prcRunEnvs().setBusisq(knsRedu.getBusisq());
			IoApReverseIn cplRvIn = SysUtil.getInstance(IoApReverseIn.class);
			cplRvIn.setOtradt(knsRedu.getInpudt());
			cplRvIn.setOtrasq(knsRedu.getInpusq());
			cplRvIn.setOtsqtp(E_YES___.YES);
			cplRvIn.setStacps(E_STACPS.ACCOUT);
//			cplRvIn.setCorrfg(input.getCorrfg());
//			cplRvIn.setStacps(input.getStacps());
//			cplRvIn.setVobkfg(input.getVobkfg());
			//String 成功00，不存在10，已经冲正20
			bizlog.info("knsRedu:[%s]",knsRedu);
			String result = ApStrike.prcRollback8(cplRvIn, E_YES___.YES, null);
			bizlog.info("result:[%s]", result);
			if(CommUtil.equals("00", result)) {
				knsRedu.setTxnsts(E_TXNSTS.STRIKED);
				KnsReduDao.updateOne_odb1(knsRedu);
			}
		}
		
	}

}


