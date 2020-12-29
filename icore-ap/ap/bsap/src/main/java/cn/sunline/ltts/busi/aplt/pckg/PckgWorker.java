package cn.sunline.ltts.busi.aplt.pckg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPckg;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;

/**
 * 系统报文登记异步处理线程 <br/>
 * 
 */
public class PckgWorker implements Runnable {
	private static final SysLog log = PckgUtil.getLog();

	/**
	 * 待处理队列 <br/>
	 */
	private BlockingQueue<KnsPckg> que;

	public PckgWorker(BlockingQueue<KnsPckg> que) {
		this.que = que;
	}

	@Override
	public void run() {
		try {
			KnsPckg packge = null;
			List<KnsPckg> pckgList = new ArrayList<KnsPckg>();
			int cnt = 0;
			while (true) {
				// Log4j2Util.set_type("yb_KapbPackge");
				try {
					if (PckgUtil.getConfig().isByInsertBatch()) { // 批量保存
						packge = que.poll(3L, TimeUnit.SECONDS); // 3秒无数据则退出
						if (packge != null) {
							pckgList.add(packge);
							cnt++;
							if (cnt >= PckgUtil.getConfig().getBatchSize()) {
								doWorkList(pckgList);
								cnt = 0;
								pckgList.clear();
							}
						} else if (pckgList.size() > 0) { // 防止停机时可能丢失：只要3秒等不到数据，若已有数据则要保存
							doWorkList(pckgList);
							cnt = 0;
							pckgList.clear();
						}
					} else {
						packge = que.take();// 如果队列是空的这里会阻塞等待
						doWork(packge);
					}
				} catch (InterruptedException ex) {
					log.error("停止系统报文登记服务", ex);
					return;
				} catch (Exception e) {
					if (PckgUtil.getConfig().isByInsertBatch()) {
						log.error("异步队列登记系统报文失败,多笔内容为：" + pckgList, e);
						cnt = 0;
						pckgList.clear(); // 防止异常堆积
					} else {
						log.error("异步队列登记系统报文失败,内容为：" + packge, e);
					}
				} finally {
					EdspCoreBeanUtil.getEngineResourceManager().clearThreadCache(false);
				}
			}
		} finally {
			PckgUtil.closeConnection();
		}
	}

	/**
	 * 处理报文登记 <br/>
	 * 
	 * @param packge
	 */
	public static void doWork(KnsPckg packge) {
		try {
			EdspCoreBeanUtil.getEngineResourceManager().clearThreadCache(true);
			EdspCoreBeanUtil.getDBConnectionManager().checkAndReconnect();

			// LogConfigManager.get().setCurrentLogType(LogType.sys);

			if (log.isDebugEnabled())
				log.debug("-----------------------------系统报文登记开始-----------------------------");

			EdspCoreBeanUtil.getDBConnectionManager().beginTransation();

			PckgUtil.saveImmediate(packge, false);

			EdspCoreBeanUtil.getDBConnectionManager().commit();

			if (log.isDebugEnabled())
				log.debug("-----------------------------系统报文登记结束-----------------------------");
		} catch (Exception e) {
			EdspCoreBeanUtil.getDBConnectionManager().rollback();
			throw e;
		} finally {
			EngineContext.clear();
		}
	}

	public static void doWorkList(List<KnsPckg> packgeList) {
		try {
			EdspCoreBeanUtil.getEngineResourceManager().clearThreadCache(true);
			EdspCoreBeanUtil.getDBConnectionManager().checkAndReconnect();

			if (log.isDebugEnabled())
				log.debug("-----------------------------系统报文登记开始-----------------------------");

			EdspCoreBeanUtil.getDBConnectionManager().beginTransation();

			DaoUtil.insertBatch(KnsPckg.class, packgeList);

			EdspCoreBeanUtil.getDBConnectionManager().commit();

			if (log.isDebugEnabled())
				log.debug("-----------------------------系统报文登记结束-----------------------------");
		} catch (Exception e) {
			EdspCoreBeanUtil.getDBConnectionManager().rollback();
			throw e;
		} finally {
			EngineContext.clear();
		}
	}

}