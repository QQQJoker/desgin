package cn.sunline.ltts.busi.aplt.tools;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbSequ;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.ApbSequDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSequ;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSequBuid;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSequBuidDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSequDao;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;
import cn.sunline.adp.core.exception.AdpDaoDuplicateException;

/**
 * <p>
 * 文件功能说明：流水号
 * 
 * </p>
 * 
 * @Author chensy
 *         <p>
 *         <li>2016年12月5日-上午9:09:09</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20161205 chensy：创建</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class ApSeq {

	// 流水缓存
	private static Map<String, Sequence> seqCaches = new ConcurrentHashMap<String, Sequence>();

	// 流水缓存key的连接符
	private final static String SEPARATOR = "^^^^";

	// 默认识别段
	private final static String DEFALUT_RECOGINTION_SEGMENT = "****";

	// 默认补位值
	private final static String DEFAULT_PADDING_VALUE = "0";
	/**
	 * 
	 * @Author chensy
	 *         <p>
	 *         <li>2016年12月8日-下午2:08:43</li>
	 *         <li>功能说明：获取一个字符型流水号</li>
	 *         <li>当流水组建标志为Y时，调用此函数前需要调用addDataToBuffer方法，
	 *         往runEnvs中设置app_sequence_builder对于的rule_buffer</li>
	 *         </p>
	 * @param seqCode
	 * @return
	 */
	public static String genSeq(String seqCode) {
		// 获取流水定义
		AppSequ seqDef = AppSequDao.selectOne_odb1(seqCode,CommTools.prcRunEnvs().getCorpno(), false);
		String corpno = CommTools.prcRunEnvs().getCorpno();
		if (CommUtil.isNull(seqDef)){
			corpno = CommTools.prcRunEnvs().getCtcono();
			seqDef =  AppSequDao.selectOne_odb1(seqCode,corpno, false);
		}
		if (CommUtil.isNull(seqDef))
			throw ApError.Aplt.E0059(seqCode);

		// 如果序号组建标志为N,则不使用app_sequence_builder组装报文
		if (BaseEnumType.E_YES___.NO == seqDef.getBuidfg()) {
			return genSeq(seqCode, DEFALUT_RECOGINTION_SEGMENT);
		}

		StringBuilder recognition = new StringBuilder(); // 识别段
		String sequence = ""; // 序号段
		StringBuilder result = new StringBuilder();

		AppSequBuid seqBuilder = null; // 流水序号组建定义
		int seqStartIndex = 0; // 流水序号在流水中的起始位置

		AppSequBuid checkBuilder = null; // 校验位组建定义
		int checkStartIndex = 0; // 校验位在流水中的起始位置

		int curLength = 0; // 流水当前组装的长度，用于序号段和检验为占位

		List<AppSequBuid> seqBuilders = AppSequBuidDao.selectAll_odb1(seqCode,corpno,
				true);

		for (AppSequBuid builder : seqBuilders) {
			switch (builder.getBuidtp()) {
			case RECOGNITION:
				// 根据build构造字符串
				String recogintionTemp = buildRecogintion(builder);
				// 进行截取、补位，
				recogintionTemp = padding(recogintionTemp, builder);

				recognition.append(recogintionTemp);
				curLength += Math.abs(builder.getCutlen());
				break;
			case SEQUENCE:
				// 由于序号段需要根据最终的识别段生成，因此这里先标记序号段位置
				seqBuilder = builder;
				seqStartIndex = curLength;
				curLength += Math.abs(builder.getCutlen());
				break;
			case CHECKBIT:
				// 由于校验位需要根据最终的识别段和序号段生成，因此这里先标记校验位位置
				checkBuilder = builder;
				checkStartIndex = curLength;
				curLength += Math.abs(builder.getCutlen());
				recognition.append(padding("", builder));// 校验位先添加空格，防止下面截取报错
				break;
			case CONSTANTS:
				// 增加常量配置
			    curLength += Math.abs(builder.getCutlen());
				recognition.append(builder.getFildna());
				break;
			default:
				break;
			}
		}

		// 产生序号
		if (seqBuilder != null) {
			sequence = getSequence(seqDef, recognition.toString()).next() + "";
			sequence = padding(sequence, seqBuilder);
			result.append(recognition.substring(0, seqStartIndex))
					.append(sequence)
					.append(recognition.substring(seqStartIndex));
		}

		// 产生校验位
		if (checkBuilder != null) {
			// 根据 recognition 和 sequence 算出 check bit;
			String resultTemp = CommUtil.isNull(result.toString()) ? recognition
					.toString() : result.toString();
			String checkBit = CommTools.genCardnoCheckBit(resultTemp.trim()) + "";
			checkBit = padding(checkBit, checkBuilder);
			result.setLength(0);
			result.append(resultTemp.substring(0, checkStartIndex))
					.append(checkBit)
					.append(resultTemp.substring(checkStartIndex
							+ checkBit.length()));
		}
		// 总长度检查（由于均根据builder定义的长度对每段数据进行 了截取、补位，
		// 除非配置出错,app_sequence里面的seq_build_length和app_sequence_builder各段组成的长度不一致，才可能出现长度不一致）

		if (CommUtil.isNull(result.toString())) {// 全是A
			result.append(recognition.toString().trim());
		}
		return result.toString();
	}

	// 根据build构造字符串
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static String buildRecogintion(AppSequBuid builder) {
		String dataMart = builder.getDatamr();
		String fieldName = builder.getFildna();
		Object fieldValue = null;
		String result = ""; // 默认为空字符串

		// 数据集如果是RunEnvs则直接冲RunEnvs中获取
		if (ApConstants.RUN_ENVS.equals(dataMart)) {
			fieldValue = CommTools.getTrxRunEnvsValue(fieldName);
		} else {
			// 从runEnvs中获取Rule_buffer
			Map ruleBuffer = ApBuffer.getBuffer();
			// 如果没有调用addDataToBuffer方法，则获取的字段为空字符串
			if (ruleBuffer != null) {
				Map<String, Object> mDataMart = (Map<String, Object>) ruleBuffer
						.get(dataMart);
				if (mDataMart != null) {
					fieldValue = mDataMart.get(fieldName);
				}
			}
		}

		if (fieldValue != null) {
			// 日期格式特殊处理
			if (fieldValue instanceof Date) {
				result = DateTools.covDateToString((Date) fieldValue);
			} else {
				result = fieldValue.toString();
			}
		}

		// 先截取长度，后补位
		if (result.length() > (Math.abs(builder.getStposi()) - 1)) {
			if (builder.getStposi() > 0) {
				if (builder.getCutlen() > 0) {
					result = result
							.substring(builder.getStposi().intValue() - 1); // 参数里面配置起始位置从1开始算，因此程序里面必须减1；
				} else {
					result = result
							.substring(0, builder.getStposi().intValue());
				}
			} else {
				int start = result.length()
						- Math.abs(builder.getStposi().intValue());
				if (builder.getCutlen() > 0) {
					result = result.substring(start);
				} else {
					result = result.substring(0, start + 1);
				}
			}
		}
		return result;
	}

	// 根据编码配置规则，进行相应的截取、补位方式、补位值。
	private static String padding(String src, AppSequBuid builder) {

		String ret = (src == null ? "" : src);
		int length = builder.getCutlen().intValue();

		if (ret.length() > Math.abs(length)) {
			if (length > 0) {
				ret = ret.substring(0, length);
			} else {
				ret = ret.substring(ret.length() - Math.abs(length));
			}

		} else if (ret.length() < Math.abs(length)) {
			String padValue = builder.getPadval();
			if (CommUtil.isNull(padValue)) {
				padValue = DEFAULT_PADDING_VALUE;
			}
			if (ApBaseEnumType.E_PADDINGMODE.LEFT == builder.getPadmod()) {
				ret = CommUtil.lpad(ret, length, padValue);
			} else {
				ret = CommUtil.rpad(ret, length, padValue);
			}
		}

		return ret;
	}

	/**
	 * 
	 * @Author chensy
	 *         <p>
	 *         <li>2016年12月8日-下午3:39:40</li>
	 *         <li>功能说明：获取一个长整型流水号（适用于流水组建标志=N、无流水号识别段的情况）</li>
	 *         </p>
	 * @param seqCode
	 * @return
	 */
	public static long genSeqId(String seqCode) {
		// 获取流水定义		
		AppSequ seqDef = AppSequDao.selectOne_odb1(seqCode,CommTools.prcRunEnvs().getCorpno(), false);
		if (CommUtil.isNull(seqDef)){
			String corpno = CommTools.prcRunEnvs().getCtcono();
			seqDef =  AppSequDao.selectOne_odb1(seqCode,corpno, false);
		}

		// 判断流水号组件标志必须=N
		if (BaseEnumType.E_YES___.YES == seqDef.getBuidfg()) {
			throw ApError.Aplt.E0037();
		}
		return getSequence(seqDef, DEFALUT_RECOGINTION_SEGMENT).next();
	}

	/**
	 * 
	 * @Author chensy
	 *         <p>
	 *         <li>2016年12月8日-下午2:09:41</li>
	 *         <li>功能说明：获取一个字符型流水号（适用于流水组建标志=N、无流水号识别段的情况）</li>
	 *         </p>
	 * @param seqCode
	 * @param recognitionSegment
	 * @return
	 */
	public static String genSeq(String seqCode, String recognitionSegment) {

		// 获取流水定义
		AppSequ seqDef = AppSequDao.selectOne_odb1(seqCode,CommTools.prcRunEnvs().getCorpno(), false);
		if (CommUtil.isNull(seqDef)){
			String corpno = CommTools.prcRunEnvs().getCtcono();
			seqDef =  AppSequDao.selectOne_odb1(seqCode,corpno, false);
		}

		// 判断流水号组件标志必须=N
		if (BaseEnumType.E_YES___.YES == seqDef.getBuidfg()) {
			throw ApError.Aplt.E0037();
		}

		if (CommUtil.isNull(recognitionSegment)) {
			recognitionSegment = DEFALUT_RECOGINTION_SEGMENT;
		}

		String sequence = getSequence(seqDef, recognitionSegment).next() + "";

		// 返回 recognitionSegment + 默认左补0 的流水号
		int seqLength = seqDef.getSeqlen().intValue();
		// 默认识别段不拼接到流水中
		if (DEFALUT_RECOGINTION_SEGMENT.equals(recognitionSegment)) {
			return CommUtil.lpad(sequence, seqLength, DEFAULT_PADDING_VALUE);
		} else {
			return recognitionSegment
					+ CommUtil.lpad(sequence, seqLength, DEFAULT_PADDING_VALUE);
		}
	}

	private static Sequence getSequence(AppSequ seqDef,
			String recognitionSegment) {
		String sKey = seqDef.getSequcd() + SEPARATOR + recognitionSegment;

		Sequence seq = seqCaches.get(sKey);
		if (seq == null) {
			synchronized (seqCaches) {
				seq = seqCaches.get(sKey);
				if (seq == null) {
					seq = new Sequence(seqDef.getSequcd(), recognitionSegment,
							seqDef);
					seqCaches.put(sKey, seq);
				}
			}
		}
		return seq;
	}

	private static class Sequence {
		private String seqCode; // 流水生成代码
		private String recognitionSegment; // 序号识别段

		private long currentValue = 1; // 缓存中流水当前值
		private long maxValue = 0;// 缓存中流水限值
		private AppSequ seqDef; // 缓存定义

		public Sequence(String seqCode, String recognitionSegment,
				AppSequ seqDef) {
			this.seqCode = seqCode;
			this.recognitionSegment = recognitionSegment;
			this.seqDef = seqDef;
		}

		public synchronized long next() {
			if (this.maxValue > this.currentValue) {
				this.currentValue = currentValue + 1;
				return this.currentValue;
			}

			// 获取最大值
			ApbSequ apbSeq = getSequenceInfo();
			this.maxValue = apbSeq.getSequno();
			this.currentValue = this.maxValue - seqDef.getCachsz() + 1;

			return this.currentValue;
		}

		private ApbSequ getSequenceInfo() {
			return DaoUtil
					.executeInNewTransation(new RunnableWithReturn<ApbSequ>() {

						public ApbSequ execute() {
							String corpno = CommTools.prcRunEnvs().getCorpno();
							ApbSequ ret = ApbSequDao.selectOneWithLock_odb1(seqCode,
											recognitionSegment,corpno, false);
							
							ApbSequ rett = null;
							if (ret == null) {
								corpno = CommTools.prcRunEnvs().getCtcono();
								rett = ApbSequDao.selectOneWithLock_odb1(seqCode,
												recognitionSegment,corpno, false);
							}													
							
							if (ret == null && rett == null) {
								ret = SysUtil.getInstance(ApbSequ.class);
								ret.setSequcd(seqCode);
								ret.setIdsegm(recognitionSegment);
								ret.setSequds(seqDef.getSequds());
								ret.setSequno(seqDef.getIntval()
										+ seqDef.getCachsz() - 1);
								ret.setCorpno(corpno);
								try {
									ApbSequDao.insert(ret);
								} catch (AdpDaoDuplicateException e) {
									ret = ApbSequDao.selectOneWithLock_odb1(
													seqCode,recognitionSegment,corpno, true);
								}
							} else if (ret != null) {
								long newSeqNo = ret.getSequno()
										+ seqDef.getCachsz();
								// 越界复位处理
								ret.setCorpno(corpno);
								if (newSeqNo > seqDef.getMaxval()) {
									ret.setSequno(seqDef.getIntval()
											+ seqDef.getCachsz() - 1);
								} else {
									ret.setSequno(newSeqNo);
								}
								ApbSequDao.updateOne_odb1(ret);
							}else if (rett != null) {
								long newSeqNo = rett.getSequno()
										+ seqDef.getCachsz();
								// 越界复位处理
								rett.setCorpno(corpno);
								if (newSeqNo > seqDef.getMaxval()) {
									rett.setSequno(seqDef.getIntval()
											+ seqDef.getCachsz() - 1);
								} else {
									rett.setSequno(newSeqNo);
								}
								ApbSequDao.updateOne_odb1(rett);
								return rett;
							}
							return ret;
						}
					});
		}
	}
}
