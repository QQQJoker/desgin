package cn.sunline.ltts.busi.bsap.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import cn.sunline.adp.core.expression.ExpressionEvaluator;
import cn.sunline.adp.core.expression.ExpressionEvaluatorFactory;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysGsn.ApbSnvl;
import cn.sunline.ltts.busi.aplt.tables.SysGsn.ApbSnvlDao;
import cn.sunline.ltts.busi.aplt.tables.SysGsn.ApbSnvm;
import cn.sunline.ltts.busi.aplt.tables.SysGsn.ApbSnvmDao;
import cn.sunline.ltts.busi.aplt.tables.SysGsn.AppSndf;
import cn.sunline.ltts.busi.aplt.tables.SysGsn.AppSndfDao;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.bsap.type.GsnType.GsnKey;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.core.exception.AdpDaoDuplicateException;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.ByRef;

/**
 * iCore通用序号生成 相关表：app_sndf,apb_snvl,apb_snvm
 */
public class GsnUtil {

	// 当前JVM序号缓存对象
	private static final Map<String, Sequence> caches = new ConcurrentHashMap<String, Sequence>();
	// 缓存对象KEY分隔符
	private static final String _SPLITOR_ = "_";
	private static final BizLog log = BizLogUtil.getBizLog(GsnUtil.class);
	private static final Object _LOCK_ = new Object();

	public static String genSerialNumber(GsnKey key) {
		if (CommUtil.isNull(key.getCorpno())) {
			key.setCorpno(SysUtil.getDefaultTenantId());
		}
		if (CommUtil.isNull(key.getDcnnoo())) {
			key.setDcnnoo(DcnUtil.getCurrDCN());
		}
		if (CommUtil.isNull(key.getSystcd())) {
			key.setCorpno(SysUtil.getSystemId());
		}

		// 获得序号定义
		final AppSndf sndf = getSndf(key.getSncode());
		// 考虑法人代码、DCNNO、日期等要素后，获得真正的序号区分对象
		final GsnKey myKey = newGsnKey(key, sndf);
		// 缓存对象KEY字符串
		final String keyStr = getKeyStr(myKey);

		if (log.isDebugEnabled()) {
			log.debug("sndf===" + sndf);
			log.debug("myKey==" + myKey);
			log.debug("keyStr=" + keyStr);
		}

		// 从缓存中获得序号对象
		Sequence sequence = caches.get(keyStr);
		if (sequence == null) { // 首次运行，需初始化
			synchronized (_LOCK_) {
				sequence = caches.get(keyStr); // 再获得一次，是防止在同步等待时，其他线程已经初始化成功
				if (sequence == null) {
					sequence = init(myKey, keyStr, sndf);
				}
			}
		}

		long sn = sequence.next();
		if (sn < 0) { // 若小于0，则说明用完，重新获得
			synchronized (_LOCK_) {
				sn = sequence.next(); // 防止已有线程初始化成功
				if (sn < 0) {
					init(myKey, keyStr, sndf);
					sn = sequence.next();
					if (sn < 0) {
						throw ApError.Aplt.E0000("序号[" + key.getSncode() + "]获得序号值失败！原因不详！");
					}
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("sn===" + sn);
		}
		// 把序号组成流水号
		return getSNString(sn, myKey, sndf);
	}

	// 初始化或获取新的号段，独立事务或非独立事务
	private static Sequence init(final GsnKey key, final String keyStr, final AppSndf sndf) {

		final ByRef<Sequence> seqRef = new ByRef<Sequence>();
		if (sndf.getIsolat() == E_YES___.YES) { // 独立事务
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {
					seqRef.value = _init(key, keyStr, sndf);
					// 将号段同步到号段表使用情况中
					syncSnvm(key, seqRef.value, E_YES___.YES);
					return null;
				}
			});
		} else {
			seqRef.value = _init(key, keyStr, sndf);
			syncSnvm(key, seqRef.value, E_YES___.YES);
		}
		return seqRef.value;
	}

	private static Sequence _init(GsnKey key, String keyStr, AppSndf sndf) {
		Sequence sequence = caches.get(keyStr);
		if (sequence == null) {
			sequence = new Sequence();
		}
		try {
			// 先锁定记录
			ApbSnvl snvl = ApbSnvlDao.selectOneWithLock_odb1(key.getSncode(), key.getTrandt(), key.getSystcd(),
					key.getDcnnoo(), key.getCorpno(), false);
			if (snvl == null) { // 无记录，需初始化
				sequence.incrementBy = sndf.getStepsz();
				sequence.currentValue = sndf.getMinval(); // next时，先加后返回
				snvl = getSnvl(key, sndf.getCachsz());
				try{
				   ApbSnvlDao.insert(snvl);
				}catch(AdpDaoDuplicateException e){
//					_init(key,keyStr,sndf);  // TODO 是否可以适用递归？
					snvl = ApbSnvlDao.selectOneWithLock_odb1(key.getSncode(), key.getTrandt(), key.getSystcd(),
							key.getDcnnoo(), key.getCorpno(), false);
					if(snvl == null){
						throw ApError.Sys.E9006();
					}else{
						sequence.currentValue = snvl.getCurval();
						snvl.setCurval(snvl.getCurval() + sndf.getCachsz());
						if (CommUtil.compare(snvl.getCurval(), sndf.getMaxval()) > 0) {
							snvl.setCurval(sndf.getMaxval()); // 不能超过最大序号
						}
						if (CommUtil.compare(sequence.currentValue, snvl.getCurval()) >= 0) {
							throw ApError.Aplt.E0000("序号[" + key.getSncode() + "]已经达到最大序号！");
						}
						if (log.isDebugEnabled()) {
							log.debug("snvl=" + snvl);
						}

						sequence.incrementBy = sndf.getStepsz();
						ApbSnvlDao.updateOne_odb1(snvl);
					}
				}
			} else { // 有记录，申请号段
				sequence.currentValue = snvl.getCurval();
				snvl.setCurval(snvl.getCurval() + sndf.getCachsz());
				if (CommUtil.compare(snvl.getCurval(), sndf.getMaxval()) > 0) {
					snvl.setCurval(sndf.getMaxval()); // 不能超过最大序号
				}
				if (CommUtil.compare(sequence.currentValue, snvl.getCurval()) >= 0) {
					throw ApError.Aplt.E0000("序号[" + key.getSncode() + "]已经达到最大序号！");
				}
				if (log.isDebugEnabled()) {
					log.debug("snvl=" + snvl);
				}

				sequence.incrementBy = sndf.getStepsz();
				ApbSnvlDao.updateOne_odb1(snvl);
			}
			sequence.maxValue = snvl.getCurval(); // 最大值为加上缓存值之后

		} catch (Exception e) {
			log.error("序号[" + key.getSncode() + "]初始化失败！", e);
			throw ApError.Aplt.E0000("序号[" + key.getSncode() + "]初始化失败！", e);
		}

		if (!caches.containsKey(keyStr))
			caches.put(keyStr, sequence);

		return sequence;
	}

	// 拼起来
	private static String getSNString(long sn, GsnKey key, AppSndf sndf) {
		Map<String, Object> data = CommUtil.toMap(key);
		if (sndf.getIsdigt() == E_YES___.NO) {
			data.put("curval", CommUtil.lpad(sn + "", sndf.getSnlenn(), "0")); // 默认左补0
		} else {
			data.put("curval", sn); // 数字型不处理，直接拼
		}
		Params ps = new Params().addAll(data);
		ExpressionEvaluator ee = ExpressionEvaluatorFactory.getInstance();
		try {
			// 求表达式,如：SQ${trandt}${dcnnoo}${corpno}${systcd}${curval}
			return (String) ee.eval(sndf.getSnrule(), ps, ps);
		} catch (Exception e) {
			throw ApError.Aplt.E0000("序号组成规则[" + sndf.getSnrule() + "]解析错误, cause by: " + e.getMessage(), e);
		}
	}

	// 若不按法人、日期、DCNNO的，则设置为默认值，确保缓存的KEY保持一致。
	private static GsnKey newGsnKey(GsnKey key, AppSndf sndf) {
		GsnKey newKey = SysUtil.getInstance(GsnKey.class);
		newKey.setSncode(key.getSncode());
		newKey.setSystcd(key.getSystcd());

		newKey.setCorpno(sndf.getBycorp() == E_YES___.NO ? sndf.getCorpno() : key.getCorpno());
		newKey.setTrandt(sndf.getBydate() == E_YES___.NO ? sndf.getTrandt() : key.getTrandt());
		newKey.setDcnnoo(sndf.getBydcnn() == E_YES___.NO ? sndf.getDcnnoo() : key.getDcnnoo());

		if (log.isDebugEnabled()) {
			log.debug("newKey=" + newKey);
		}
		return newKey;
	}

	private static ApbSnvl getSnvl(GsnKey key, Integer cachsz) {
		ApbSnvl snvl = SysUtil.getInstance(ApbSnvl.class);
		snvl.setSncode(key.getSncode());
		snvl.setSystcd(key.getSystcd());
		snvl.setCorpno(key.getCorpno());
		snvl.setTrandt(key.getTrandt());
		snvl.setDcnnoo(key.getDcnnoo());
		snvl.setCurval(cachsz + 0L);

		if (log.isDebugEnabled()) {
			log.debug("snvl=" + snvl);
		}
		return snvl;
	}

	private static AppSndf getSndf(String sncode) {
		AppSndf sndf = AppSndfDao.selectOne_odb1(sncode, false);
		if (sndf == null) {
			throw Aplt.E0000("未找到序号[" + sncode + "]定义!");
		}
		if (CommUtil.compare(sndf.getMaxval(), 0L) <= 0) {
			throw Aplt.E0000("序号[" + sncode + "]定义错误，最大序号不能小于或等于0!");
		}
		if (CommUtil.compare(sndf.getMinval(), 0L) < 0) {
			throw Aplt.E0000("序号[" + sncode + "]定义错误，最小序号不能小于0!");
		}
		if (CommUtil.compare(sndf.getMinval(), sndf.getMaxval()) >= 0) {
			throw Aplt.E0000("序号[" + sncode + "]定义错误，最小序号不能大于或等于最大序号!");
		}

		if (CommUtil.isNull(sndf.getCorpno())) {
			sndf.setCorpno("000");
		}
		if (CommUtil.isNull(sndf.getTrandt())) {
			sndf.setTrandt("19000101");
		}
		if (CommUtil.isNull(sndf.getDcnnoo())) {
			sndf.setDcnnoo("000");
		}
		if (sndf.getCachsz() == null) {
			sndf.setCachsz(1);
		}
		if (sndf.getStepsz() == null) {
			sndf.setStepsz(1);
		}
		return sndf;
	}

	private static String getKeyStr(GsnKey key) {
		StringBuffer sb = new StringBuffer();
		sb.append(key.getSncode()).append(_SPLITOR_);
		sb.append(key.getTrandt()).append(_SPLITOR_);
		sb.append(key.getCorpno()).append(_SPLITOR_);
		sb.append(key.getSystcd()).append(_SPLITOR_);
		sb.append(key.getDcnnoo());

		return sb.toString();
	}

	private static String getKeyStr(ApbSnvm snvm) {
		StringBuffer sb = new StringBuffer();
		sb.append(snvm.getSncode()).append(_SPLITOR_);
		sb.append(snvm.getTrandt()).append(_SPLITOR_);
		sb.append(snvm.getCorpno()).append(_SPLITOR_);
		sb.append(snvm.getSystcd()).append(_SPLITOR_);
		sb.append(snvm.getDcnnoo());

		return sb.toString();
	}

	private static GsnKey getKeyFromStr(String keyStr) {
		String keys[] = keyStr.split(_SPLITOR_);
		GsnKey gsnKey = SysUtil.getInstance(GsnKey.class);
		gsnKey.setSncode(keys[0]);
		gsnKey.setTrandt(keys[1]);
		gsnKey.setCorpno(keys[2]);
		gsnKey.setSystcd(keys[3]);
		gsnKey.setDcnnoo(keys[4]);

		if (log.isDebugEnabled()) {
			log.debug("gsnKey=" + gsnKey);
		}
		return gsnKey;
	}

	private static void syncSnvm(GsnKey key, Sequence sequence, E_YES___ usstat) {
		ApbSnvm snvm = ApbSnvmDao.selectOne_odb1(key.getSncode(), key.getTrandt(), key.getSystcd(), key.getDcnnoo(),
				key.getCorpno(), CoreUtil.getSvcId(), false);
		if (snvm == null) {
			snvm = SysUtil.getInstance(ApbSnvm.class);
			snvm.setSncode(key.getSncode());
			snvm.setSystcd(key.getSystcd());
			snvm.setCorpno(key.getCorpno());
			snvm.setTrandt(key.getTrandt());
			snvm.setDcnnoo(key.getDcnnoo());
			snvm.setVmidxx(CoreUtil.getSvcId());
			snvm.setCurval(sequence.currentValue);
			snvm.setMaxval(sequence.maxValue);
			snvm.setUsstat(usstat);
			if (log.isDebugEnabled()) {
				log.debug("insert snvm=" + snvm);
			}
			ApbSnvmDao.insert(snvm);
		} else {
			snvm.setCurval(sequence.currentValue);
			snvm.setMaxval(sequence.maxValue);
			snvm.setUsstat(usstat);
			if (log.isDebugEnabled()) {
				log.debug("update snvm=" + snvm);
			}
			ApbSnvmDao.updateOne_odb1(snvm);
		}
	}

	// JVM停止时使用:将内存中使用
	public static void stopVM() {
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				for (Entry<String, Sequence> item : caches.entrySet()) {
					Sequence sequence = item.getValue();
					if (sequence == null)
						continue;
					syncSnvm(getKeyFromStr(item.getKey()), sequence, E_YES___.NO); // 状态改为未使用
				}
				return null;
			}
		});
	}

	// JVM启动时使用:从APB_SNVM表中将当前JVM的已分配号段加载到内存，继续使用。
	public static void startVM() {
		// 只加载状态为“未使用”的，如果状态在“使用中”的，可能是当前JVM之前为非正常停止（kill
		// -9），此处不需处理，不好的影响是：浪费了一些序号而已。
		final List<ApbSnvm> vmList = ApbSnvmDao.selectAll_odb2(CoreUtil.getSvcId(), E_YES___.NO, false);
		DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
			@Override
			public Void execute() {
				for (ApbSnvm snvm : vmList) {
					String keyStr = getKeyStr(snvm);
					Sequence sequence = caches.get(keyStr);
					if (sequence == null) {
						sequence = new Sequence();
						caches.put(keyStr, sequence);
					}
					AppSndf sndf = getSndf(snvm.getSncode());
					sequence.currentValue = snvm.getCurval();
					sequence.maxValue = snvm.getMaxval();
					sequence.incrementBy = sndf.getStepsz();
					try {
						snvm.setUsstat(E_YES___.YES);
						ApbSnvmDao.updateOne_odb1(snvm);
					} catch (Exception e) {
						log.error("更新序号[" + sndf.getSncode() + "]的JVM[" + CoreUtil.getSvcId() + "]当前值失败！");
					}
				}
				return null;
			}
		});
	}

	private static class Sequence {
		private int incrementBy = 1;
		private long maxValue = 0L; // 当前线程最大值
		private long currentValue = 0L; // 当前线程当前值

		synchronized long next() {
			if (this.maxValue >= this.currentValue + this.incrementBy) {
				this.currentValue = this.currentValue + this.incrementBy;
				return this.currentValue;
			} else {
				return -1L;
			}
		}
	}

}
