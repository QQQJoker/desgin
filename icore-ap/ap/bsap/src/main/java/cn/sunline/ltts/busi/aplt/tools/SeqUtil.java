package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.bsap.type.GsnType.GsnKey;
import cn.sunline.ltts.busi.bsap.util.GsnUtil;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;

public class SeqUtil {
	private static final BizLog log = BizLogUtil.getBizLog(SeqUtil.class);
	
	private static final String TRAN_SEQ_SPLIT = "_"; // 交易流水号分隔
	private static final String TRAN_SEQ＿MAIN = "000";// 主流水序号
	private static final String TRAN_SEQ＿KEY = "";// 主流水序号
	private static final int TRAN_SEQ_LEN = 11; // 流水号序号长度

	/**
	 * 获得主流水号，通过RunEnv
	 * 
	 * @return
	 */
	public static String getTransqFromEnvs() {
		RunEnvsComm trxRun = SysUtil.getTrxRunEnvs();
		return getTransq(trxRun.getTrandt(), SysUtil.getSystemId(), trxRun.getCdcnno(), trxRun.getCorpno());
	}

	/**
	 * 获得调用流水号，通过RunEnv
	 * 
	 * @return
	 */
	public static String getNextCallTransqFromEnvs() {
		RunEnvsComm trxRun = SysUtil.getTrxRunEnvs();
		return getNextCallTransq(trxRun.getTransq(), trxRun.getTrandt(), SysUtil.getSystemId(), trxRun.getCdcnno(), trxRun.getCorpno());
	}

	/**
	 * 根据输入获得主流水号
	 * 
	 * @return
	 */
	public static String getTransq(String trandt, String systid, String dcnno, String corpno) {
		GsnKey key = SysUtil.getInstance(GsnKey.class);
		key.setSncode(ApConstants.TRANSQ_NAME_KEY);
		key.setSystcd(systid);
		key.setTrandt(trandt);
		key.setCorpno(corpno);
		key.setDcnnoo(dcnno);
		return GsnUtil.genSerialNumber(key);
	}

	/**
	 * 获得下一调用流水号
	 * 
	 * @return
	 */
	public static String getNextCallTransq(String mntrsq, String trandt, String systid, String dcnno, String corpno) {
		String basesq = mntrsq;
		if (CommUtil.isNull(mntrsq)) {
			basesq = getTransq(trandt, systid, dcnno, corpno);
		} else {
			//basesq = basesq.substring(0, basesq.length() - 3);
		}
		long sq = getCacheKeySeq(basesq);

		String callsq = basesq + CommUtil.lpad(sq + "", 3, "0");
		if (log.isDebugEnabled())
			log.debug("========本次外调流水号为[" + callsq + "]");

		return callsq;
	}

	// 从交易级缓存中获得递增序号
	public static long getCacheKeySeq(String keyName) {
		return CommTools.getCurrentThreadSeq(ApConstants.TRANSQ_NAME_KEY, keyName);
	}

	// 初始化
	public static long resetCacheKeySeq(String keyName) {
		return CommTools.setSequenceNoByKeyName(ApConstants.TRANSQ_NAME_KEY, keyName, 0L, false);
	}
	/**
	 * 获得报文流水号，通过RunEnv
	 * 
	 * @return
	 */
	public static String getPckgsq(String pckgdt) {
		// 报文流水中3位调用序号为法人代码 2017-08-14 YangGX
		return getBaseSq(ApConstants.PACKSQ_NAME_KEY, pckgdt, SysUtil.getSystemId(), DcnUtil.getCurrDCN())
				+ SysUtil.getDefaultTenantId();//.getChannelId();
	}
	

	private static String getBaseSq(String keyName, String trandt, String systid, String dcnno) {
		StringBuffer sb = new StringBuffer(keyName);
		sb.append(TRAN_SEQ_SPLIT).append(trandt);
		sb.append(TRAN_SEQ_SPLIT).append(systid);
		sb.append(TRAN_SEQ_SPLIT).append(dcnno);

		if( log.isDebugEnabled() ) {
			log.debug("getBaseSq key:" + sb);
		}
		
		StringBuffer sq = new StringBuffer(SysUtil.nextValue(sb.toString()));
		if( log.isDebugEnabled() ) {
			log.debug("getBaseSq sq:" + sq);
		}
		
		return trandt + systid + dcnno + CommUtil.lpad(sq.toString(), TRAN_SEQ_LEN, "0");
	}
	
	public static String genPkgSeq()
	       {
	       return genSeq(BaseEnumType.E_SEQDEF.bsap_pkg_seq.getId(), BaseEnumType.E_SEQDEF.bsap_pkg_seq.getValue(), false, 10);
	       }



	 public static String genMainTranSeq()
	       {
	      return genSeq(BaseEnumType.E_SEQDEF.bsap_tran_seq.getId(), BaseEnumType.E_SEQDEF.bsap_tran_seq.getValue(), true, 18);
	       }
	
	 public static String genSeq(String seqKey, String type, boolean isTrandt, int seqLen) {
		    StringBuilder sb = new StringBuilder();
		    String subSystem;
		    if (isTrandt) {
		        subSystem = CommTools.prcRunEnvs().getTrandt();
		        sb.append(subSystem);
		    }

		    sb.append(type);
		    subSystem = SysUtil.getSubSystemId();
		    sb.append(subSystem);
		    String seqValue = SysUtil.nextValue(seqKey);
		    String seq = CommUtil.lpad(seqValue, seqLen, "0");
		    sb.append(seq);
		    return sb.toString();
		}


}
