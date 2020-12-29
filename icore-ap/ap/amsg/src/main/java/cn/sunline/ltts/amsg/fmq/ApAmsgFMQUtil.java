package cn.sunline.ltts.amsg.fmq;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.type.SPEnumType.E_PROCSTATUS;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppMessToMQ;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppMessToMQDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessHdMQ;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessHdMQDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.sys.parm.TrxAmsgEnvs.Request;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;


public class ApAmsgFMQUtil {

	private static final String regxStr = "\\$\\{\\w+\\}"; // \w 用于匹配字母，数字或下划线等字符
	private static final String Head = "head";
	private static final String Body = "body";
	private static final BizLog bizlog = BizLogUtil.getBizLog(CommTools.class);

	/**
	 * 报文组建
	 * 
	 * @param message
	 * @return Map
	 */
	public static String buildTransferMessage(Object body, String Topcid) {

		// TODO 报文三段式改为二段式，等架构适配报文格式  流水是否需要重置待思考  ale 20201128
		Map<String, Object> pckg = new HashMap<>();

		Request head = SysUtil.getInstance(Request.class);
		head.setBusisq(CommTools.prcRunEnvs().getBusisq());// 交易流水
		head.setInpudt(CommTools.prcRunEnvs().getInpudt());// 上送系统编号
		head.setServno(CommTools.prcRunEnvs().getServno());// 交易渠道号
		head.setServtp(CommTools.prcRunEnvs().getServtp());// 交易渠道
		head.setCorpno(CommTools.prcRunEnvs().getCorpno());// 交易法人代码
		head.setTranus(CommTools.prcRunEnvs().getTranus());// 交易柜员
		head.setInpucd(CommTools.prcRunEnvs().getInpucd());// 上送系统编号
		head.setTranbr(CommTools.prcRunEnvs().getTranbr());// 交易机构
		head.setBusiseqno(CommTools.prcRunEnvs().getBusisq());
		head.setCallseqno(CommTools.prcRunEnvs().getBusisq()); // TODO 取值待商量
		head.setServicecode(Topcid);
		head.setGlbSeqno(CommTools.prcRunEnvs().getBusisq());
		head.setSysSeqno(CommTools.prcRunEnvs().getBusisq());// TODO 取值待商量

		pckg.put(Head, CommUtil.toMap(head));
		pckg.put(Body, body);

		bizlog.debug("**********************组建报文为：[%s]*********************", pckg);

		return SysUtil.serialize(pckg);
	}

	/**
	 * fmq登记薄赋值
	 * 
	 * @param message
	 * @param topcId
	 */
	public static void insertToFMQ(String Message, String topcId) {

		ApsMessHdMQ apsMessHdmq = SysUtil.getInstance(ApsMessHdMQ.class);

		apsMessHdmq.setMesseq(1);
		apsMessHdmq.setMesstp(Message);
		apsMessHdmq.setTopcid(topcId);
		apsMessHdmq.setCorpno(CommTools.prcRunEnvs().getCorpno());
		apsMessHdmq.setPrcscd(CommTools.prcRunEnvs().getPrcscd());
		apsMessHdmq.setSdcnid(CommTools.prcRunEnvs().getCdcnno());
		apsMessHdmq.setSystid(CommTools.prcRunEnvs().getInpucd());
		apsMessHdmq.setTmstmp(CommTools.prcRunEnvs().getTmstmp());
		apsMessHdmq.setTrandt(CommTools.prcRunEnvs().getTrandt());
		apsMessHdmq.setTransq(CommTools.prcRunEnvs().getTransq());
		apsMessHdmq.setBusisq(CommTools.prcRunEnvs().getBusisq());
		apsMessHdmq.setUserid(CommTools.prcRunEnvs().getTranus());
		apsMessHdmq.setHandst(E_PROCSTATUS.W);

		bizlog.debug("**********************组建报文为：[%s]*********************", apsMessHdmq);

		ApsMessHdMQDao.insert(apsMessHdmq);
	}
	
	/**
	 * 替换字符串
	 * 
	 * @param prcscd
	 * @param topcId
	 * @param sendFMQMap
	 * @return Map<String, Object>
	 */
	public static Map<String, Object> replaceMessageContent(String prcscd, String scenno, Map<String, Object> sendFMQMap) {

		String meteTp = "";
		
		Map<String, Object> transferMap = new HashMap<String, Object>();
		
		AppMessToMQ messInfo = AppMessToMQDao.selectOne_odb1(prcscd, scenno, true);

		if (messInfo.getIsrepl() == E_YES___.YES) {

			Map<String, Object> toStrMap = new HashMap<String, Object>();
			for (String keys : sendFMQMap.keySet()) {
				String value = sendFMQMap.get(keys).toString();
				toStrMap.put(keys, value);
			}
			meteTp = processMsgContent(messInfo.getMetemp(), toStrMap);

			transferMap.put("sendmg", meteTp);
		}else {
			return sendFMQMap;
		}
		return transferMap;
	}

	/**
	 * 匹配字符串 ${xxxxxx}
	 * 
	 * @param meteMp
	 * @param params
	 * @return
	 */
	private static String processMsgContent(String meteMp, Map<String, Object> params) {
		StringBuffer sbf = SysUtil.getInstance(StringBuffer.class);
		Matcher m = Pattern.compile(regxStr).matcher(meteMp);
		while (m.find()) {
			String param = m.group();
			Object value = params.get(param.substring(2, param.length() - 1));
			m.appendReplacement(sbf, value == null ? "" : value.toString());
		}
		m.appendTail(sbf);
		return sbf.toString();
	}
}
