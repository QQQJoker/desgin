package cn.sunline.ltts.busi.aplt.pckg;

import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPck2;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPck2Dao;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPckg;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPckgDao;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.metadata.mybatis.util.JdbcUtil;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;

public class PckgUtil {

	private static final SysLog log = SysLogUtil.getSysLog("sys.onl.pckg");

	private PckgUtil() {
	}

	public static SysLog getLog() {
		return log;
	}

	public static PckgConfig getConfig() {
		return EdspCoreBeanUtil.getConfigManagerFactory().getDefaultConfigManager().getConfig(PckgConfig.class);
	}

	/**
	 * 将系统报文放入Queque或直接登记 若放入Queque不成功，则尝试直接登记 若直接登记，失败则会抛出异常
	 * byNewTransaction 用于直接登记时，若交易成功不使用独立事务，若失败使用独立事务
	 * @param pckg
	 */
	public static void put(KnsPckg pckg, boolean byNewTransaction) {

		// 若不全登记，则把报文去掉，仅登记流水号和耗时
		if (getConfig() != null && !getConfig().isAllFlag()) {
			pckg.setRequme(null);
			pckg.setRespme(null);
			pckg.setBiglog(E_YES___.NO); // 如果不登记报文内容，肯定不存副表
		}

		if (log.isDebugEnabled())
			log.debug("异步系统报文为：" + pckg);

		if (PckgComponent.stoped || getConfig() == null || !getConfig().isAsynFlag()) {
			saveImmediate(pckg, byNewTransaction);
		} else {
			try {
				PckgComponent.addMsg2Queque(pckg);
			} catch (Exception e) {
				if (log.isInfoEnabled()) {
					log.info("交易[%s]系统报文放入队列失败,采用直接写库!", pckg.getTransq());
				}
				saveImmediate(pckg, byNewTransaction);
			}
		}
	}

	public static void saveImmediate(final KnsPckg pckg, boolean byNewTransaction) {
		if (!byNewTransaction)
			_saveImmediate(pckg);
		else {
			DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
				@Override
				public Void execute() {
					_saveImmediate(pckg);
					return null;
				}
			});
		}
	}

	private static void _saveImmediate(final KnsPckg pckg) {
		try {
			String requme = pckg.getRequme();
			String respme = pckg.getRespme();
			// 启用大字段表，且长度大于指定长度
			if (getConfig().isUseClobTab() && requme != null && requme.getBytes(JdbcUtil.getDbEncoding()).length > ApConstants.PCKG_MAX_LEN
					|| respme != null && respme.getBytes(JdbcUtil.getDbEncoding()).length > ApConstants.PCKG_MAX_LEN) {
				// 报文大于指定长度，登记副表
				pckg.setBiglog(E_YES___.YES);
				KnsPck2 pckg2 = SysUtil.getInstance(KnsPck2.class);
				pckg2.setPckgdt(pckg.getPckgdt());
				pckg2.setPckgsq(pckg.getPckgsq());
				pckg2.setRequme(requme);
				pckg2.setRespme(respme);
				pckg.setRequme(null);
				pckg.setRespme(null);
				KnsPck2Dao.insert(pckg2);
			}
			KnsPckgDao.insert(pckg);
		} catch (RuntimeException e) {
			log.error("交易[%s]系统报文直接写库失败!", pckg.getPckgsq(),e);
			throw e;
		} catch (Exception e) {
			log.error("交易[%s]系统报文直接写库失败2!", pckg.getPckgsq(),e);
			throw new RuntimeException(e);
		}
	}

	protected static void closeConnection() {
		try {
			EdspCoreBeanUtil.getDBConnectionManager().closeResource();
		} catch (Throwable e) {
			log.error("关闭[PCKG]数据库连接相关资源出错！" + e.getMessage(), e);
		}
		try {
			EdspCoreBeanUtil.getDBConnectionManager().close();
		} catch (Throwable e) {
			log.error("关闭[PCKG]数据库连接出错！" + e.getMessage(), e);
		}
	}
}
