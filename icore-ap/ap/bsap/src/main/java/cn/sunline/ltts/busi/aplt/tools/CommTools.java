package cn.sunline.ltts.busi.aplt.tools;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.RequestHeaderData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.engine.sequence.SequenceManager;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.service.engine.ServiceEngine;
import cn.sunline.adp.core.bean.descriptor.ModelPropertyDescriptor;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.adp.core.exception.AdpDaoNoDataFoundException;
import cn.sunline.adp.core.profile.ProfileSwitcher;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.metadata.model.database.DbIndex;
import cn.sunline.adp.metadata.model.database.DbIndex.Index;
import cn.sunline.adp.metadata.model.database.Table;
import cn.sunline.clwj.zdbank.fmq.util.FmqUtil;
import cn.sunline.clwj.zdbank.fmq.util.Message;
import cn.sunline.clwj.zdbank.fmq.util.MessageEngineContext;
import cn.sunline.edsp.base.lang.ByRef;
import cn.sunline.edsp.base.lang.DefaultEnum;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpTxns;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpTxnsDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCorp;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCorpDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcy;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCrcyDao;
import cn.sunline.ltts.busi.aplt.type.SysCommTools.ApCurrency;
import cn.sunline.ltts.busi.bsap.namedsql.SysPublicDao;
import cn.sunline.ltts.busi.sys.dict.ApDict;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_CORPLEVEL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_APTRTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CARRTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_IDTFTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SMIDET;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * <p>
 * 文件功能说明：应用平台常用技术方法工具包
 * </p>
 * 
 * @Author <p>
 *         <li>2014年3月4日-下午5:15:40</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228Name：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class CommTools {

	private static final BizLog bizlog = BizLogUtil.getBizLog(CommTools.class);

	/**
	 * 获取当前请求对象
	 * 
	 * @return
	 */
	public static RequestData getCurrentRequestData() {
		return EngineContext.getRequestData();
	}
	/**
	 * 获取当前请求头对象
	 * 
	 * @return
	 */
	public static RequestHeaderData getRequestHeaderData() {
		return getCurrentRequestData().getRequestHeader();
	}
	
	/**
	 * 得到交易法人
	 * 
	 * @return
	 */
	public static String getDefaultTranbr() {
		String tranbr = null;
		KnpPara knpPara = CommTools.KnpParaQryByCorpno("system.config", SysUtil.getSubSystemId(), "tranbr", "%", false);
		if (knpPara != null) {
			tranbr = knpPara.getPmval1();
		}
		return tranbr;
	}

	/**
	 * @Author <p>
	 *         <li>2014年3月4日-下午5:14:20</li>
	 *         <li>功能说明：List多字段的正向排序。</li>
	 *         </p>
	 * @param list
	 *            待排序的List对象
	 * @param isAsc
	 *            是否升序 true - 升序 false - 降序
	 * @param fields
	 *            排序字段数组，可变参数
	 */
	// @BusiComponent(name="listSort",longnme="List多字段的正向排序",type=Type.tech)
	public static <E> void listSort(List<E> list, final boolean isAsc, final String... fields) {

		Collections.sort(list, new Comparator<Object>() {
			public int compare(Object a, Object b) {

				int ret = 0;
				for (String field : fields) {

					// 获取每一个排序字段对应的值
					Object aValue = null;
					Object bValue = null;
					try {
						aValue = CommUtil.getProperty(a, field);
						bValue = CommUtil.getProperty(b, field);
					} catch (Exception e) {
						throw Sys.E0001("根据属性名从对象中获取属性的值失败！");
					}
					// 比较
					ret = MyCompare(aValue, bValue);
					if (ret != 0) {
						break;
					}
				}
				if (isAsc)
					return ret;
				else
					return -1 * ret;

			}
		});
	}

	/**
	 * @Author T
	 *         <p>
	 *         <li>2014年6月19日-下午8:11:01</li>
	 *         <li>功能说明：对象比较，仅支持String, int，BigDecimal, DefaultEnum</li>
	 *         <li>注：该方法视空与null相等</li>
	 *         </p>
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static int MyCompare(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null)
			return 0;
		if (obj1 == null && obj2 != null) {
			if ("".equals(obj2)) {
				return 0;
			}
			return -1;
		}
		if (obj1 != null && obj2 == null) {
			if ("".equals(obj1)) {
				return 0;
			}
			return 1;
		}
		if (BigDecimal.class.isAssignableFrom(obj1.getClass()) && BigDecimal.class.isAssignableFrom(obj2.getClass())) {
			return ((BigDecimal) obj1).compareTo((BigDecimal) obj2);
		} else if (String.class.isAssignableFrom(obj1.getClass()) && String.class.isAssignableFrom(obj2.getClass())) {
			return ((String) obj1).compareTo((String) obj2);
		} else if (Integer.class.isAssignableFrom(obj1.getClass()) && Integer.class.isAssignableFrom(obj2.getClass())) {
			return (Integer) obj1 - (Integer) obj2;
		} else if (Long.class.isAssignableFrom(obj1.getClass()) && Long.class.isAssignableFrom(obj2.getClass())) {
			return (int) ((Long) obj1 - (Long) obj2);
		} else if (DefaultEnum.class.isAssignableFrom(obj1.getClass())
				&& DefaultEnum.class.isAssignableFrom(obj2.getClass())) {
			return MyCompare(((DefaultEnum) obj1).getValue(), ((DefaultEnum) obj2).getValue());
		}
		throw Sys.E0001("不支持的类型比较");
	}

	/**
	 * @Author T
	 *         <p>
	 *         <li>2014年6月19日-下午8:11:36</li>
	 *         <li>功能说明：合法串匹配检查。</li>
	 *         </p>
	 * @param sRpx
	 *            合法正则串
	 * @param sData
	 *            待核查串
	 * @return int 如果匹配合法返回0，否则返回1。
	 */
	public static int rpxMatch(String sRpx, String sData) {
		Pattern p = Pattern.compile(sRpx);
		Matcher m = p.matcher(sData);
		boolean b = m.matches();
		if (b) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * @Author ?
	 *         <p>
	 *         <li>2014年3月4日-下午5:18:15</li>
	 *         <li>功能说明：生成账号校验位，模10隔位乘2加"校验位算法。</li>
	 *         </p>
	 * @param acctno
	 *            账号
	 * @return int 账户校验位
	 */
	public static int genCheckNumber(String sAcctno) {

		sAcctno = CommUtil.trim(sAcctno);
		int parity;
		if (rpxMatch("^[0-9]*$", sAcctno) == 0 && !sAcctno.isEmpty()) {
			int sum = 0;
			int temp = 0;
			char[] acctnoArray = sAcctno.toCharArray();
			for (int i = 0; i < acctnoArray.length; i++) {
				if (i % 2 == 0) {
					temp = (Character.getNumericValue(acctnoArray[i]) * 2);
					if (temp > 9) {
						temp = temp - 9;
					}
				} else {
					temp = Character.getNumericValue(acctnoArray[i]);
				}
				sum += temp;
			}

			parity = 10 - sum % 10;
			if (parity == 10) {
				parity = 0;
			}
		} else {
			throw new AdpBusinessException("10", "输入字符串必须是数字");
		}
		return parity;
	}

	/**
	 * @Author ?
	 *         <p>
	 *         <li>2014年3月4日-下午5:18:15</li>
	 *         <li>功能说明：生成账号校验位，模10隔位乘2加"校验位算法,并返回账号。</li>
	 *         </p>
	 * @param acctno
	 *            账号
	 * @return int 账户校验位
	 */
	public static String genCheckNumberReturnAcctno(String sAcctno) {

		sAcctno = CommUtil.trim(sAcctno);
		int parity;
		if (rpxMatch("^[0-9]*$", sAcctno) == 0 && !sAcctno.isEmpty()) {
			int sum = 0;
			int temp = 0;
			char[] acctnoArray = sAcctno.toCharArray();
			for (int i = 0; i < acctnoArray.length; i++) {
				if (i % 2 == 0) {
					temp = (Character.getNumericValue(acctnoArray[i]) * 2);
					if (temp > 9) {
						temp = temp - 9;
					}
				} else {
					temp = Character.getNumericValue(acctnoArray[i]);
				}
				sum += temp;
			}

			parity = 10 - sum % 10;
			if (parity == 10) {
				parity = 0;
			}
		} else {
			throw new AdpBusinessException("10", "输入字符串必须是数字");
		}
		return sAcctno + parity;
	}

	/**
	 * @Author ?
	 *         <p>
	 *         <li>2014年3月19日-下午3:22:35</li>
	 *         <li>功能说明：字符串转成16进制字符</li>
	 *         </p>
	 * @param str
	 * @return
	 */
	public static String toHexString(String str) {

		StringBuilder stringBuilder = new StringBuilder();

		char[] chars = str.toCharArray();

		if (str == null || str.length() <= 0) {
			return null;
		}

		for (int i = 0; i < chars.length; i++) {

			int iChr = (int) chars[i];

			String chr = Integer.toHexString(iChr);

			stringBuilder.append(chr);

		}

		return stringBuilder.toString();

	}

	/**
	 * @Author cuijia
	 *         <p>
	 *         <li>2017年5月22日-下午11:23:05</li>
	 *         <li>功能说明：获取系统默认币种</li>
	 *         </p>
	 * 
	 * @return 币种代码
	 */
	public static String getDefineCurrency() {
		String currency = ApUtil.CURRENCY;

		if (BaseEnumType.E_YES___.YES == getApCurrency(currency).getBasefg()) {
			return currency;
		}
		throw ApError.Aplt.E0053(currency);

	}

	/**
	 * @Author qiuhan
	 *         <p>
	 *         <li>2016年12月6日-下午2:48:47</li>
	 *         <li>功能说明：按币种精度位检查金额是否合法</li>
	 *         </p>
	 * @param ccyCode
	 * @param amount
	 * @return
	 */
	public static void validAmount(String ccyCode, BigDecimal amount) {
		if (amount != null) {
			BigDecimal bigAmountNew = roundByCurrency(ccyCode, amount, null);
			if (CommUtil.compare(bigAmountNew, amount) != 0) {
				throw ApError.Aplt.E0039(ccyCode, amount, bigAmountNew);
			}
		}
	}

	/**
	 * @Author T
	 *         <p>
	 *         <li>2014年3月19日-下午3:23:05</li>
	 *         <li>功能说明：按币种最小记账单位进行四舍五入</li>
	 *         <li>进位规则如果为空按照默认为四舍五入处理</li>
	 *         </p>
	 * @param crcycd
	 *            币种
	 * @param bigAmount
	 *            金额
	 * @param bigAmount
	 *            金额
	 * @return
	 */
	public static BigDecimal roundByCurrency(String crcycd, BigDecimal bigAmount, BaseEnumType.E_CARRTP roundRule) {

		return roundAmount(bigAmount, roundRule, getApCurrency(crcycd).getCcymin().intValue());
	}

	/**
	 * @Author T
	 *         <p>
	 *         <li>2017年5月22日-下午11:23:05</li>
	 *         <li>功能说明：按币种计息单位进行四舍五入</li>
	 *         <li>进位规则如果为空按照默认为四舍五入处理</li>
	 *         </p>
	 * @param crcycd
	 *            币种
	 * @param bigAmount
	 *            金额
	 * @param bigAmount
	 *            金额
	 * @return
	 */
	public static BigDecimal roundIncinByCurrency(String crcycd, BigDecimal bigAmount, BaseEnumType.E_CARRTP roundRule) {
		return roundAmount(bigAmount, roundRule, getApCurrency(crcycd).getIamnun().intValue());
	}

	/**
	 * 
	 * <p>
	 * Title:exists
	 * </p>
	 * <p>
	 * Description: 检查币种是否存在
	 * </p>
	 * 
	 * @author cuijia
	 * @date 2017年6月20日
	 * @param crcycd
	 * @return
	 */
	public static boolean existsApCurrency(String crcycd) {
		AppCrcy appCrcy = AppCrcyDao.selectOne_odb1(crcycd, false);
		if (CommUtil.isNull(appCrcy)) {
			return false;
		}
		return true;
	}

	/**
	 * @Author Cuijia
	 *         <p>
	 *         <li>2017年5月20日-下午12:27:51</li>
	 *         <li>功能说明：获取货币参数信息</li>
	 *         </p>
	 * @param crcycd
	 * @return
	 */
	public static ApCurrency getApCurrency(String crcycd) {
		ApCurrency appCurency = SysUtil.getInstance(ApCurrency.class);

		AppCrcy appCrcy = AppCrcyDao.selectOne_odb1(crcycd, false);
		if (CommUtil.isNull(appCrcy)) {
			throw Aplt.E0013(crcycd);
		}

		CommUtil.copyProperties(appCurency, appCrcy);
		return appCurency;
	}

	/**
	 * 
	 * <p>
	 * Title:listApCurrency
	 * </p>
	 * <p>
	 * Description: 获取币种列表
	 * </p>
	 * 
	 * @author cuijia
	 * @date 2017年6月19日
	 * @return
	 */
	public static List<ApCurrency> listApCurrency() {
		return SysPublicDao.listAppCrcy(CommTools.prcRunEnvs().getCorpno(), false);
	}

	/**
	 * @Author T
	 *         <p>
	 *         <li>2017年5月22日-下午11:23:05</li>
	 *         <li>功能说明：按币种计息单位进行四舍五入</li>
	 *         <li>进位规则如果为空按照默认为四舍五入处理</li>
	 *         </p>
	 * @param crcycd
	 *            币种
	 * @param bigAmount
	 *            金额
	 * @param bigAmount
	 *            金额
	 * @return
	 */
	private static BigDecimal roundAmount(BigDecimal bigAmount, BaseEnumType.E_CARRTP roundRule, int scale) {
		BigDecimal amtOut = bigAmount;
		// 四舍五入
		if (CommUtil.isNull(roundRule) || roundRule == E_CARRTP.ROUND) {
			amtOut = amtOut.setScale(scale, BigDecimal.ROUND_HALF_UP);
		}
		// 无条件进位
		else if (roundRule == E_CARRTP.UP) {
			amtOut = amtOut.setScale(scale, BigDecimal.ROUND_UP);
		}
		// 无条件舍去
		else if (roundRule == E_CARRTP.DOWN) {
			amtOut = amtOut.setScale(scale, BigDecimal.ROUND_FLOOR);
		}
		return amtOut;
	}

	/**
	 * @Author T
	 *         <p>
	 *         <li>2014年3月19日-下午3:23:41</li>
	 *         <li>功能说明：使用千分符格式化</li>
	 *         </p>
	 * @param sAmount
	 * @param iPoint
	 * @return
	 */
	public static String formatAmount(String sAmount, int iPoint) {
		if (CommUtil.isNull(sAmount) || CommUtil.isNull(iPoint)) {
			return null;
		}

		NumberFormat formater = null;
		double dAmount = Double.parseDouble(sAmount);

		if (iPoint == 0) {
			formater = new DecimalFormat("###,###,###,###");
		} else {
			StringBuffer buff = new StringBuffer();
			buff.append("###,###,###,###.");
			for (int i = 0; i < iPoint; i++) {
				buff.append("#");
			}
			formater = new DecimalFormat(buff.toString());
		}

		return formater.format(dAmount);
	}

	/**
	 * <p>
	 * <li>功能说明：获取公共运行变量</li>
	 * <li>如果要取客户化的环境变量请使用客户化的,如：CommToolsAplt.prcRunEnvs()</li>
	 * </p>
	 * 
	 * @return trxRun 公共运行变量
	 */
	public static RunEnvsComm prcRunEnvs() {
		RunEnvsComm trxRun = SysUtil.getTrxRunEnvs();
		return trxRun;
	}


	@Deprecated
	public static String getFrdm(Class<?> intfClass) {
		return getTranCorpno();
	}

	@Deprecated
	public static String getFrdm() {
		return getTranCorpno();
	}

	/**
	 * 获取当前交易法人代码(若当前交易环境中存在则直接返回，否则取中心法人代码) 2017-4-24
	 * 
	 * @return
	 */
	public static String getTranCorpno() {
		DataArea transDataArea = EngineContext.getRequestData().getBody();
		String corpno = transDataArea.getSystem().getString(ApConstants.COPRNO_NAME_KEY);
		if (!StringUtil.isBlank(corpno)) {
			return corpno;
		} else {
			String coprno = CommTools.prcRunEnvs().getCorpno();// 当前交易法人代码
			if (CommUtil.isNull(coprno)) {
				coprno = getCenterCorpno();
			}
			return coprno;
		}
	}

	/**
	 * 获取专用法人 用于全法人共享模式的数据，如：共享式的客户信息和额度等
	 * 
	 * @return
	 */
	public static String getSpecCorpno() {
		String coprno = CommTools.prcRunEnvs().getSpcono();// 获取专用法人
		if (CommUtil.isNull(coprno)) {
			coprno = getCenterCorpno();
		}
		return coprno;
	}

	/**
	 * 获取省中心法人代号
	 * 
	 * @return
	 */
	public static String getCenterCorpno() {
		String corpno = CommTools.prcRunEnvs().getCtcono(); // getCorpno();
															// 2017-07-21 YangGX
		if (CommUtil.isNull(corpno)) {
			AppCorp appCorp = AppCorpDao.selectFirst_odb2(E_CORPLEVEL.FIRST, false);
			if (CommUtil.isNull(appCorp))
				throw ApError.Aplt.E0061();
			corpno = appCorp.getCorpno();
		}
		return corpno;
	}

	/**
	 * @Author T
	 *         <p>
	 *         <li>2014年3月19日-下午1:39:19</li>
	 *         <li>功能说明：字符串分割，按指定大小进行分割</li>
	 *         </p>
	 * @param str
	 *            原字符串
	 * @param averageSize
	 *            长度
	 * @return 分割后的字符串数组
	 */
	public static String[] split(String str, int averageSize) {
		if (str == null || str.length() == 0)
			return new String[0];
		if (averageSize >= str.length())
			return new String[] { str };
		int count = str.length() / averageSize + 1;
		String[] ret = new String[count];
		for (int i = 0; i < count; i++) {
			if ((i + 1) * averageSize < str.length())
				ret[i] = str.substring(i * averageSize, (i + 1) * averageSize);
			else
				ret[i] = str.substring(i * averageSize);
		}
		return ret;
	}

	/**
	 * @Author Qian
	 *         <p>
	 *         <li>2014年4月22日-下午7:42:10</li>
	 *         <li>功能说明：将外部交易码对应的内部处理码写入数据区</li>
	 *         </p>
	 * @param prcscd
	 * @param dataArea
	 */
	public static String prcTransCode(String prcscd, DataArea dataArea) {

		KnpTxns tblKnpTxns = SysUtil.getInstance(KnpTxns.class);
		try {
			tblKnpTxns = KnpTxnsDao.selectOne_odb1(prcscd, true);
		} catch (AdpDaoNoDataFoundException e) {
			throw Aplt.E0019(prcscd, e);
		} catch (Exception e) {
			throw Aplt.E0003(e);
		}
		bizlog.debug("交易码[%s]对应的内部处理码为[%s]", prcscd, tblKnpTxns.getInprcd());
		if (null == tblKnpTxns.getInprcd() || "".equals(tblKnpTxns.getInprcd())) {
			throw Aplt.E0019(prcscd);
		} else {
			prcRunEnvs().setPrcsna(tblKnpTxns.getTranna());
			prcRunEnvs().setAptrtp(E_APTRTP.get(tblKnpTxns.getTrantp().toString()));
			prcRunEnvs().setLttscd((tblKnpTxns.getInprcd()));
			bizlog.debug("内部处理码为[%s]-[%s]", dataArea.getCommReq().get(ApDict.Aplt.lttscd.getId()), prcRunEnvs()
					.getAptrtp());
		}
		return tblKnpTxns.getInprcd();
	}



	/**
	 * 是不是冲账交易
	 * 
	 * @return
	 */
	@Deprecated
	public static boolean isCZTrans() {
		return ApUtil.STRIKE_TXN_CODE_STR.indexOf(CommTools.prcRunEnvs().getPrcscd()) >= 0;
	}

	// 判断渠道类型是否柜面
	public static boolean isCounterChannel() {
		String servtp = null;
		if (CommUtil.isNotNull(CommTools.prcRunEnvs().getServtp())) {
			servtp = CommTools.prcRunEnvs().getServtp().getValue();
		} else {
			servtp = "";
		}

		return ApUtil.COUNTER_CHANNEL.equals(servtp);
	}


	/**
	 * 获取序列号，当位数不足时左补齐0
	 * 
	 * @param key
	 *            产生序列号key
	 * @param len
	 *            产生序列号长度
	 * @return 序列号
	 */
	public static String getSequence(String key, int len) {
		return getSequence(key, len, "0");
	}

	/**
	 * 获取序列号，当位数不足时左补齐
	 * 
	 * @param key
	 *            产生序列号key
	 * @param len
	 *            产生序列号长度
	 * @param padStr
	 *            补齐字符，默认为0
	 * @return 序列号
	 */
	public static String getSequence(String key, int len, String padStr) {
		if (CommUtil.isNull(key))
			throw Aplt.E0000("生成序列号key不能为空");
		if (CommUtil.isNull(len)) {
			throw Aplt.E0000("生成序列号长度不能为空");
		}
		if (len == 0) {
			throw Aplt.E0000("生成序列号长度不能为0");
		}
		String keyno = SequenceManager.nextval(null,key).getNextValue();

		return CommUtil.lpad(keyno, len, padStr);

	}

	/**
	 * 根据RunEnv生成新流水号
	 * 注意：仅返回生成的流水号，不会将新流水号设置到RunEnvs中
	 */
	public static String genTransq() {
		return SeqUtil.getTransqFromEnvs();
	}

	/**
	 * 根据RunEnv生成新流水号
	 * 注意：仅返回生成的流水号，不会将新流水号设置到RunEnvs中
	 */
	public static String genBizSeq() {
		return SeqUtil.getTransqFromEnvs();
	}

	/**
	 * 提供调用远程服务的方法 1、登记流水 2、不支持多实现
	 * 
	 * @param intfClass
	 * @return
	 */
	public static <T> T getRemoteInstance(Class<T> intfClass) {
		return SysUtil.getInstanceProxyByBind(intfClass, "*");
	}

	/**
	 * 提供自定义绑定调用远程服务的方法 1、登记流水 2、通过自定义绑定支持多服务实现的场景
	 * 
	 * @param intfClass
	 * @param bindId
	 * @return
	 */
	public static <T> T getRemoteInstance(Class<T> intfClass, String bindId) {
		return SysUtil.getInstanceProxyByBind(intfClass, bindId);
	}

	/**
	 * 提供创建实例的方法 1、不登记流水
	 * 
	 * @param intfClass
	 * @return
	 */
	public static <T> T getInstance(Class<T> intfClass) {
		return SysUtil.getInstance(intfClass);
	}

	/**
	 * 获取系统标识
	 * 
	 * @return
	 */
	public static String getSystemId() {
		return SysUtil.getSystemId();
	}

	public static String getMySysId() {
		return SysUtil.getSubSystemId();
	}

	/**
	 * 判断是否字符是否全是数字
	 * 
	 * @param str
	 * @return boolean
	 */
	public static boolean isNum(String str) {
		return str.matches("[0-9]+");
	}

	

	/**
	 * 根据数据库中存储的大字段，获取对应的Map(例如split1=|@|、split2=‘=’，则以key=value|@|key1=value1
	 * ...形式存储)
	 * 
	 * @param bigstr
	 *            数据库存储的大字段
	 * @return Map<key,value>
	 */
	public static Map<String, String> getValueMap(String bigstr, String split1, String split2) {

		if (CommUtil.isNull(bigstr)) {
			return null;
		}
		if (CommUtil.isNull(split1)) {
			throw Aplt.E0000("数据间隔符1不能为空");
		}
		if (CommUtil.isNull(split2)) {
			throw Aplt.E0000("数据间隔符2不能为空");
		}

		Map<String, String> valueMap = new HashMap<String, String>();
		String[] values = bigstr.split(split1);

		for (int i = 0; i < values.length; i++) {
			String[] str = values[i].split(split2);
			if (str.length != 2) {
				throw Aplt.E0000(values[i] + "数据键值异常");
			}
			valueMap.put(str[0], str[1]);
		}

		return valueMap;

	}

	/**
	 * 根据key获取map的value，不存在则返回空
	 * 
	 * @param Map
	 *            <String,String> keyMap
	 * @param String
	 *            key
	 * @return String value
	 */
	public static String getKeyValue(Map<String, String> keyMap, String key) {

		if (CommUtil.isNull(keyMap)) {
			throw Aplt.E0000("keyMap不能为空");
		}
		if (!keyMap.containsKey(key)) {
			return "";
		}
		return keyMap.get(key);

	}

	/**
	 * 
	 * @Title: josnMatter
	 * @Description: (map转化为json字符串)
	 * @param input
	 * @return
	 * @author xiongzhao
	 * @date 2016年9月20日 下午4:12:12
	 * @version V2.3.0
	 */
	public static String josnMatter(Map<String, String> input) {

		String josnMatter = JSON.toJSONString(input);
		return josnMatter;

	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年2月27日-下午4:42:19</li>
	 *         <li>功能说明：输入转为json字符串</li>
	 *         </p>
	 * @param request
	 * @return
	 */
	public static String toJson(Object request) {
		return JsonUtil.format(request);
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2017年1月10日-下午1:58:08</li>
	 *         <li>功能说明: 对象克隆</li>
	 *         </p>
	 * @param clazz
	 * @param obj
	 * @return
	 */
	public static <T> T clone(Class<T> clazz, T obj) {

		String s = SysUtil.serialize(obj);
		return (T) SysUtil.deserialize(s, clazz);
	}

	/**
	 * 
	 * @Title: messageid
	 * @Description: (生成MQ消息ID)
	 * @return
	 * @author xiongzhao
	 * @date 2016年9月23日 下午5:10:02
	 * @version V2.3.0
	 */
	public static String getMessageId() {
		int len = 9;
		StringBuffer mesgid = new StringBuffer();
		String trandt = CommTools.prcRunEnvs().getTrandt();
		String mesgsq = CommTools.getSequence("mesgid", len);
		mesgid.append("NAS").append(trandt).append(mesgsq);
		return mesgid.toString();

	}

	/**
	 * 
	 * @Title: getSysIp
	 * @Description: (获取当前应用机器IP)
	 * @return
	 * @author xiongzhao
	 * @date 2016年10月10日 下午3:23:16
	 * @version V2.3.0
	 */
	public static String getSysIp() {
		return SysUtil.getIp();
	}

	/**
	 * 字符串去双引号和空格
	 */
	public static String delDoubleQuotationMarks(String str) {
		return str.replaceAll("\"", "").replaceAll(" ", "");
	}

	/**
	 * 日期字符串去双引号转成YYYYMMDD格式 2016-01-27
	 */
	public static String delDoubleQuotationMarksDate(String str) {

		String strdate = str.replaceAll("\"", "").replaceAll(" ", "");

		return strdate.substring(0, 4) + strdate.substring(5, 7) + strdate.substring(8, 10);
	}

	/**
	 * 生成新交易流水(包括：mntrsq和transq)
	 * 注意：不仅生产新的流水号，还会把新流水号设置到RunEvns中
	 * @param trandt 交易日期
	 */
	public static void genNewSerail(String trandt) {
		/* Modify 2017-09-04
		prcRunEnvs().setInpucd(SysUtil.getSystemId());
		prcRunEnvs().setInpudt(trandt);
		prcRunEnvs().setInpusq(SeqUtil.getNextCallTransqFromEnvs());
		*/
		RunEnvsComm trxRun = SysUtil.getTrxRunEnvs();
		//trxRun.setTrandt(trandt); //防止在账务类交易中使用造成记账日期不是核心系统当前日期
		trxRun.setMntrsq(SeqUtil.getTransqFromEnvs());
		trxRun.setTransq(trxRun.getMntrsq());
		trxRun.setInpudt(trandt);
		trxRun.setInpusq(null);
	}

	/**
	 * 生成新交易流水(包括：mntrsq和transq)
	 * 注意：不仅生产新的流水号，还会把新流水号设置到RunEvns中
	 */
	public static void genNewSerail() {
		genNewSerail(prcRunEnvs().getTrandt());
	}

	/**
	 * 
	 * @Title: fieldNotNull
	 * @Description: 检查输入必填项
	 * @return
	 * @author cuijia
	 * @date 2017年5月22日
	 * 
	 */
	public static void fieldNotNull(Object data, String fieldName, String fieldDesc) {
		if (CommUtil.isNull(data))
			throw ApError.Aplt.E0054(fieldName, fieldDesc);
	}

	/**
	 * @Author zhoumy
	 *         <p>
	 *         <li>2017年1月19日-下午1:58:08</li>
	 *         <li>功能说明: 数据分组散列值(哈希值)计算</li>
	 *         </p>
	 * @param subKey
	 *            业务参数表中的sub_key字段值： main_key = "DATA_GROUP_HASH_VALUE"
	 * @param sourceSeq
	 *            来源流水号：如账号、冻结编号、流水号等
	 * @return 散列值(哈希值)
	 */
	public static long getGroupHashValue(String subKey, String sourceSeq) {

		// 数据拆分组数
		KnpPara knpPara = CommTools.KnpParaQryByCorpno("DATA_GROUP_HASH_VALUE", subKey, "%", "%", false);
		if (CommUtil.isNull(knpPara))
			throw ApError.Aplt.E0055("DATA_GROUP_HASH_VALUE", subKey);
		int groupNum = Integer.valueOf(knpPara.getPmval1()).intValue();
		int len = (String.valueOf(groupNum).length()) * 2;
		// 只保留传入源流水号的2倍被除数长度
		String tempSeq = sourceSeq;
		if (sourceSeq.length() > len) {
			tempSeq = sourceSeq.substring(sourceSeq.length() - len);
		}
		// 返回的长整形值
		Long longValue = null;
		// 类型转换
		try {
			longValue = new Long(tempSeq);
		} catch (NumberFormatException e) {
			// 含有字符，将字符替换成数字
			char[] seqArray = tempSeq.toCharArray();
			String resultStr = "";
			int seqLen = seqArray.length;
			for (int k = 0; k < seqLen; k++) {
				if (!Character.isDigit(seqArray[k])) {
					int num = seqArray[k] - 65; // 字母在ASCII码中的值
					if (num < 0) {
						num = 0;
					}
					num = num % 10; // 只保留一位
					resultStr = resultStr.concat(String.valueOf(num));
				} else {
					resultStr = resultStr.concat(String.valueOf(seqArray[k]));
				}
			}
			// 得到转换后的整形数字
			longValue = new Long(resultStr);
		}
		// 整除取余得到哈希值: 哈希值从1开始
		long hashValue = longValue % groupNum + 1;

		return hashValue;
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2016年12月13日-下午3:42:26</li>
	 *         <li>功能说明：根据key获取公共运行变量的value</li>
	 *         </p>
	 * @param key
	 * @return value key对应公共运行变量的值 公共运行变量不存在，返回null
	 */
	public static Object getTrxRunEnvsValue(String key) {
		if (CommUtil.isNull(key))
			return null;

		Object value = null;
		RunEnvsComm runEnvs = prcRunEnvs();
		try {
			Method method = RunEnvsComm.class.getMethod("get" + key.substring(0, 1).toUpperCase() + key.substring(1));
			value = method.invoke(runEnvs);
		} catch (Exception e) {
			// 取不到值则默认为空
		}
		return value;
	}

	/**
	 * @Author chensy
	 *         <p>
	 *         <li>2016年12月13日-上午9:57:21</li>
	 *         <li>功能说明：生成账\卡号校验位，模10隔位乘2加"校验位算法。</li>
	 *         </p>
	 * @param cardno
	 *            不含校验位的账号(卡号)
	 * @return 校验位
	 */
	public static int genCardnoCheckBit(String cardno) {

		fieldNotNull(cardno, "acct_no", "account no");

		cardno = CommUtil.trim(cardno);

		if (!cardno.matches("^[0-9]*$")) {
			throw ApError.Aplt.E0009(cardno);
		}

		int sum = 0;
		int temp = 0;
		char[] array = cardno.toCharArray();
		for (int i = 0; i < array.length; i++) {
			if (i % 2 == 0) {
				temp = (Character.getNumericValue(array[i]) * 2);
				if (temp > 9) {
					temp = temp - 9;
				}
			} else {
				temp = Character.getNumericValue(array[i]);
			}
			sum += temp;
		}

		int parity = 10 - sum % 10;
		if (parity == 10) {
			parity = 0;
		}

		return parity;
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2016年12月8日-下午4:47:40</li>
	 *         <li>功能说明：获取表的主键</li>
	 *         </p>
	 * @return
	 */
	public static String getTablePkValue(Object obj) {
		Table table = OdbFactory.getTable(obj.getClass());
		if (table == null)
			return null;

		List<DbIndex> indexList = table.getIndex();
		DbIndex pkIndex = null;
		for (DbIndex index : indexList) {
			if (index.getType() == Index.PRIMARY_KEY) { // 约定只取primarykey
				pkIndex = index;
			}
		}

		// 如果主键为空，检索唯一索引
		if (pkIndex == null) {
			for (DbIndex index : indexList) {
				if (index.getType() == Index.UNIQUE) {
					pkIndex = index;
				}
			}
		}

		if (pkIndex != null) {
			Map<String, Object> map = new HashMap<String, Object>();
			String[] fields = pkIndex.getFieldArray();
			for (String field : fields) {
				ModelPropertyDescriptor mpd = ModelPropertyDescriptor.get(obj.getClass(), field);
				Object value = mpd.getProperty(obj);
				map.put(field, value);
			}
			return SysUtil.serialize(map);
		}

		return null;
	}

	/**
	 * 在当前线程内，根据KEY值获得顺序递增的序号，交易结束清理
	 * <p>
	 * 注意：对于批量交易跟随EngineContext.getTxnTempObjMap对象清理
	 * 
	 * @param keyName
	 *            产生序号的KEY
	 * @return 默认从1开始
	 */
	public static long getCurrentThreadSeq(String keyName) {
		return getCurrentThreadSeq(DEFAULT_PREFIX, keyName);
	}

	public static long getCurrentThreadSeq(String prefix, String keyName) {
		if (prefix == null || keyName == null)
			throw ApError.Aplt.E0001("产生序号的前缀和键值不能为空!");
		return setSequenceNoByKeyName(prefix, keyName, 0L, true);
	}

	/**
	 * 在当前线程内，根据KEY值获得顺序递增的序号，交易结束清理
	 * 
	 * @param clazz
	 *            表或者类名
	 * @param keyName
	 *            键值
	 * @return
	 */
	public static long getCurrentThreadSeq(Class<?> clazz, String keyName) {
		if (clazz == null || keyName == null)
			throw ApError.Aplt.E0001("产生序号的类名和键值不能为空!");
		return setSequenceNoByKeyName(clazz.getSimpleName(), keyName, 0L, true);
	}

	private static final String KEY_SEQ_IN_TRAN = "key_seq_in_tran"; // 用于专门存储交易级序号KEY值，避免与其他交易级缓存对象重复
	private static final String DEFAULT_PREFIX = "_default";

	/**
	 * <p>
	 * <li>功能说明：设置指定Key的交易级缓存序号的值</li>
	 * </p>
	 * 
	 * @param prefix
	 *            关键字前缀
	 * @param keyName
	 *            关键字
	 * @param value
	 *            序号的值
	 * @param increase
	 *            是否需要递增
	 * @return 设置后的序号
	 */
	public static long setSequenceNoByKeyName(String prefix, String keyName, Long value, boolean increase) {
		String key = prefix.toString() + "." + keyName;
		@SuppressWarnings("unchecked")
		Map<String, ByRef<Long>> seqMap = (Map<String, ByRef<Long>>) ApltEngineContext.getTxnTempObjMap().get(
				KEY_SEQ_IN_TRAN);
		if (seqMap == null) {
			ApltEngineContext.getTxnTempObjMap()
					.put(KEY_SEQ_IN_TRAN, seqMap = new LinkedHashMap<String, ByRef<Long>>());
		}
		ByRef<Long> seq = new ByRef<Long>(value);
		if (!seqMap.containsKey(key)) {
			seqMap.put(key, seq);
		} else {
			seq = seqMap.get(key);
			if (!increase)
				seq.value = value;
		}
		if (increase)
			seq.value++;

		return seq.value;
	}

	// 判断是否分布式系统:“系统参数.是否分布式系统=”
	public static boolean isDistributedSystem() {
		return CoreUtil.isDistributedSystem();
	}

	// 判断是否分布式远程调用
	public static boolean isDistributedCallFromRemote() {

		// 非分布式系统不处理
		if (!isDistributedSystem())
			return false;

		RunEnvsComm runEnv = SysUtil.getTrxRunEnvs();
		// 非主调节点认为远程
		if (runEnv.getMntrfg() != E_YES___.YES) {
			return true;
		}

		return false;
	}

	/**
	 * 判断当前交易是否冲正
	 * 
	 * @return
	 */
	public static boolean isStrikeProcess() {
		Boolean b = (Boolean) ApltEngineContext.getTxnTempObjMap().get(KEY_IS_STRIKE);
		if (b == null)
			return false;
		else
			return b;
	}

	private static final String KEY_IS_STRIKE = "_key_is_strike";

	public static void setStrikeProcess() {
		ApltEngineContext.getTxnTempObjMap().put(KEY_IS_STRIKE, Boolean.TRUE);
	}

	/**
	 * 检查交易上传字段是否存在
	 * <p>
	 * 用法：一般用于维护交易,如：不上传字段说明不修改；上传字段存在，但值为null，则修改为null
	 * 
	 * @param fieldname
	 * @return true-存在,false-不存在
	 */
	public static boolean isExistsInReq(String fieldname) {
		if (CommUtil.isNull(fieldname))
			throw Aplt.E0000("检查是否上传的字段不能为空!");
		return EngineContext.getRequestData().getBody().getInput().containsKey(fieldname);
	}
	

	public static KnpPara KnpParaQryByCorpno(String parmcd, String pmkey1,
			String pmkey2,String pmkey3, boolean b) {
		
		String coprno = getCenterCorpno();
		KnpPara para;
		
		if (CommTools.prcRunEnvs().getCorpno()!=null&&!CommTools.prcRunEnvs().getCorpno().equals(coprno)){
			para =KnpParaDao.selectOne_odb1(parmcd, pmkey1,pmkey2,pmkey3,CommTools.prcRunEnvs().getCorpno(), false);
			if (CommUtil.isNull(para)){
				para =KnpParaDao.selectOne_odb1(parmcd, pmkey1,pmkey2,pmkey3,coprno, b);
			}
		}
		else{
			para =KnpParaDao.selectOne_odb1(parmcd, pmkey1,pmkey2,pmkey3,coprno, b);
		}

		 return para;
	}
	
	public static boolean isFlowTran() {
		boolean isFlowTran = !EngineContext.getEngineRuntimeContext().getInnerServiceCode().contains(".");
		return isFlowTran;
	}

	public static E_SMIDET ChangeIdtftp(E_IDTFTP idtftp) {
		// TODO Auto-generated method stub
		return null;
	}

	public static boolean isNeedRegistPckg() {
		return (prcRunEnvs().getPckgfg() != null) && (prcRunEnvs().getPckgfg() == E_YES___.YES) ? true : false;
	}
	
	public static boolean isRegistTransactionSeq() {
		return (prcRunEnvs().getTrsqfg() != null) && (prcRunEnvs().getTrsqfg() == E_YES___.YES) ? true : false;
	}
	
	
	public static String getBranchSeq() {
		if (ServiceEngine.getCurrServiceExecutorContext() == null) { // 远程被消费服务
    		return SysUtil.getRequestHead().getCallSeqNo();
		}
		else { // 本地服务
			return SysUtil.getServiceRequestHead().getCallSeqNo();
		}
	}

	
	/**
	 * 发送消息（默认交易后处理发送，不直接发送）
	 * @param topicId 消息主题ID
	 * @param messgae  消息内容
	 */
	public static void pushMessage(String topicId,String messgae) {
		pushMessage(topicId,messgae,false);
		
	}
	
	/**
	 * 直接发送消息
	 * @param topicId  消息主题ID
	 * @param messgae  消息内容
	 * @param isNow  是否直接发送
	 */
    public static void pushMessage(String topicId,String messgae,boolean isNow) {
    	String busiId = String.valueOf(getTrxRunEnvsValue("busisq"));
    	if(isNow) {
    		FmqUtil.send(topicId, messgae, busiId);
    	}else {
    		Message msg = new Message(topicId,messgae,busiId);
    		MessageEngineContext.addTxnTempObj(msg);
    	}
	}

}
