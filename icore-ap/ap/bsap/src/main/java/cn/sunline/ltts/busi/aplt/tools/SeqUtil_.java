package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

public class SeqUtil_ {
	private static final BizLog log = BizLogUtil.getBizLog(SeqUtil.class);
	private static final String TRAN_SEQ_SPLIT = "_"; // 交易流水号分隔
	private static final String TRAN_SEQ＿MAIN = "000";// 主流水序号
	private static final String TRAN_SEQ＿KEY = "";// 主流水序号
	private static final int TRAN_SEQ_LEN = 8; // 流水号序号长度

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

	/**
	 * 获得主流水号，通过RunEnv
	 * 
	 * @return
	 */
	public static String getTransqFromEnvs() {
		RunEnvsComm trxRun = SysUtil.getTrxRunEnvs();
		return getTransq(trxRun.getTrandt(), SysUtil.getSystemId(), trxRun.getCdcnno());
	}

	/**
	 * 获得调用流水号，通过RunEnv
	 * 
	 * @return
	 */
	public static String getNextCallTransqFromEnvs() {
		RunEnvsComm trxRun = SysUtil.getTrxRunEnvs();
		return getNextCallTransq(trxRun.getTransq(), trxRun.getTrandt(), SysUtil.getSystemId(), trxRun.getCdcnno());
	}

	/**
	 * 根据输入获得主流水号
	 * 
	 * @return
	 */
	public static String getTransq(String trandt, String systid, String dcnno) {
		return getBaseSq(ApConstants.TRANSQ_NAME_KEY, trandt, systid, dcnno) + TRAN_SEQ＿MAIN;
	}

	/**
	 * 获得下一调用流水号
	 * 
	 * @return
	 */
	public static String getNextCallTransq(String mntrsq, String trandt, String systid, String dcnno) {
		String basesq = mntrsq;
		if (CommUtil.isNull(mntrsq)) {
			basesq = getBaseSq(ApConstants.TRANSQ_NAME_KEY, trandt, systid, dcnno);
		} else {
			basesq = basesq.substring(0, basesq.length() - 3);
		}
		long sq = getCacheKeySeq(basesq);

		String callsq = basesq + CommUtil.lpad(sq + "", 3, "0");
		if (log.isDebugEnabled())
			log.debug("========本次外调流水号为[" + callsq + "]");

		return callsq;
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
		
		String corpno = SysUtil.getDefaultTenantId();
		try {
			corpno = CommTools.getTranCorpno();
		} catch( Exception e ) {
			log.error("生成流水号时，获得法人代码失败！");
		}
		
		return trandt + systid + dcnno + corpno + CommUtil.lpad(sq.toString(), TRAN_SEQ_LEN, "0");
	}

	// 从交易级缓存中获得递增序号
	public static long getCacheKeySeq(String keyName) {
		return CommTools.getCurrentThreadSeq(TRAN_SEQ＿KEY, keyName);
	}

	// 初始化
	public static long resetCacheKeySeq(String keyName) {
		return CommTools.setSequenceNoByKeyName(TRAN_SEQ＿KEY, keyName, 0L, false);
	}

}
