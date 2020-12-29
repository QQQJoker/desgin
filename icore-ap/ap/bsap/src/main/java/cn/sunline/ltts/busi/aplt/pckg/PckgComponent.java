package cn.sunline.ltts.busi.aplt.pckg;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import cn.sunline.adp.cedar.base.srv.socket.common.CustomThreadFactory;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsPckg;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.adp.cedar.base.boot.plugin.PluginSupport;

/**
 * 系统报文登记服务组件 <br/>
 * 
 */
public class PckgComponent extends PluginSupport {
	
	private static final SysLog log = PckgUtil.getLog();
    /**
     * 组件配置 <br/>
     */
    private PckgConfig config;
    
    /**
     * 停止标记
     */
    public static boolean stoped = true;
    
    /**
     * 处理队列 <br/>
     */
    private static BlockingQueue<KnsPckg> pckgQueue;
    
    /**
     * 通知处理线程池 <br/>
     */
    private ThreadPoolExecutor worksPool;


    @Override
    public boolean initPlugin() {
    	
        config = PckgUtil.getConfig();
        if (config == null) {
            log.warn("末找到配置，系统报文登记组件不会启用");
            return false;
        }
        return true;
    }

    @Override
    public void startupPlugin() {
        pckgQueue = new ArrayBlockingQueue<KnsPckg>(config.getQuequeSize());
        worksPool = new ThreadPoolExecutor(config.getThreadNum(), config.getThreadNum(), 0, TimeUnit.MINUTES,
                new SynchronousQueue<Runnable>(), new CustomThreadFactory("jioybw"));
        for (int i = 0; i < config.getThreadNum(); i++)
            worksPool.submit(new PckgWorker(pckgQueue));
        
        stoped = false;
    }

    @Override
    public void shutdownPlugin() {
		try {
			stoped = true;
			
			if (worksPool != null)
				worksPool.shutdown();
		} finally {
			if (pckgQueue != null && !pckgQueue.isEmpty()) {
				
				if( log.isWarnEnabled() )
					log.warn("正在停机，系统报文队列还有["+pckgQueue.size()+"]条消息，明细如下：");
				while (pckgQueue.iterator().hasNext()) {
					if( log.isWarnEnabled() )
					    log.warn("" + pckgQueue.iterator().next());
				}
			}
		}
    }
    
    /**
     * 将报文加入到非持久性处理队列等待处理 <br/>
     * @param jioybw
     */
    public static void addMsg2Queque(KnsPckg pckg) {
        if (pckgQueue == null)
            throw ExceptionUtil.wrapThrow("系统报文登记组件未启动");
        
        boolean sucess = pckgQueue.offer(pckg);
		String logMsg = String.format("交易:报文流水号[%s][%s]报文放入队列,结果[%s]",pckg.getPckgdt(), pckg.getPckgsq(), sucess);
        if( log.isInfoEnabled() ) {
            log.info(logMsg);
        }
        if (!sucess) {//添加队列失败
            throw ExceptionUtil.wrapThrow(logMsg);
        }
    }
}
