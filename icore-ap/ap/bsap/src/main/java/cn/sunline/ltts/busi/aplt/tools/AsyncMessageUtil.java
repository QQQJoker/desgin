package cn.sunline.ltts.busi.aplt.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.LogConfigManager.SystemType;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.metadata.loader.util.ModelFactoryUtil;
import cn.sunline.adp.metadata.model.ElementType;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.ltts.amsg.service.IoApAsyncMessage;
import cn.sunline.ltts.amsg.servicetype.IoApTopicReceipt;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType.AppTopiDefiCType;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageRealInfo;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageRefeInfo;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageTotalInfo;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CONSCY;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGMOD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SVCOPT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

public class AsyncMessageUtil {

    public final static String _ASYNC_MESSAGE_TXN_KEY = "EngineContextHelper.msg.key";
    private final static String _KEY_MESSID_PREFIX = "_messid_prefix.";
    private final static int _MESSID_SEQ_LENGTH = 4;
    private static final BizLog log = BizLogUtil.getBizLog(AsyncMessageUtil.class);

    /**
     * @see
     *      cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil.add(MessageRealInfo,
     *      RunEnvsComm)
     * @param mri
     */
    public static void add(MessageRealInfo mri) {
        RunEnvsComm env = SysUtil.getTrxRunEnvs();
        add(mri, env);
    }

    /**
     * 将产生的异步消息放入待发送暂存
     * <p>
     * <li>主节点直接放入暂存区；
     * <li>跨节点放入RunEnvs.msgdcn列表；
     * 
     * @param mri
     *        消息对象
     */
    public static void add(MessageRealInfo mri, RunEnvsComm env) {
        // 检查
        checkMessage(mri);
        // 如果是跨节点的，则写到RunEvns中，服务调用完成返回给主调节点；否则为主调节点，直接放入暂存区，等待交易commit后发送
        
        //if (env.getXdcnfg() = E_YES___.YES) { xdcnfg变量与业务代码dcn判断存在耦合，改为mntrfg @jizhirong 20171121
        //TODO 修改变量后逻辑验证
        if(env.getMntrfg() != E_YES___.YES){
            env.getMsgdcn().add(mri);
            env.setMsgcnt(env.getMsgcnt() == null ? 1 : env.getMsgcnt() + 1); // 累计消息数
            if (log.isInfoEnabled())
                log.info("当前被调服务的RunEnvs中异步消息数[%d],本次放入的消息[%s].", env.getMsgcnt(), mri);
        } else {
            List<MessageTotalInfo> list = getMsgList();
            int cnt = list.size();
            list.add(getTotalInfoByReal(mri, env, cnt));
            if (log.isInfoEnabled())
                log.info("当前主调节点的缓存中异步消息数[%d],本次放入的消息[%s].", getMsgList().size(), mri);
        }
    }

    /** 获取消息缓存区栈 */
    public static List<MessageTotalInfo> getMsgList() {
        List<MessageTotalInfo> list = EngineContext.getTxnTempObj(_ASYNC_MESSAGE_TXN_KEY);
        // 采用LinkedHashMap为保证其顺序
        if (list == null)
            EngineContext.getTxnTempObjMap().put(_ASYNC_MESSAGE_TXN_KEY, list = new ArrayList<MessageTotalInfo>());

        return list;
    }

    //清理消息
    public static void clear() {
        getMsgList().clear();
    }

    /**
     * 在分布式跨节点异步消息补充辅助信息，有两个调用点： <li>1.主调节点：业务代码请求待发送暂存时； <li>
     * 2.主调节点：跨节点外调服务返回，带回消息时，根据服务返回的RunEnvs补充辅助信息
     * 
     * @param mri
     *        异步消息主题内容
     * @param env
     *        运行环境（调用点1时为当前RunEnv，2时为服务返回的RunEnv）
     * @return
     */
    public static MessageTotalInfo getTotalInfoByReal(MessageRealInfo mri, RunEnvsComm env, int cnt) {
        MessageTotalInfo mti = SysUtil.getInstance(MessageTotalInfo.class);

        MessageRefeInfo ref = SysUtil.getInstance(MessageRefeInfo.class);
        ref.setTrandt(env.getTrandt()); // 交易日期
        ref.setBusisq(env.getBusisq()); // 业务流水号
        ref.setMntrsq(env.getMntrsq()); // 主交易流水号
        ref.setTransq(env.getTransq()); // 交易流水号
        ref.setInpusq(env.getInpusq()); // 上送系统流水号
        ref.setInpudt(env.getInpudt()); // 上送交易日期
        ref.setCallsq(env.getCallsq()); // 当前调用流水号
        ref.setSystcd(env.getSystcd()); // 子系统编号
        ref.setCdcnno(env.getCdcnno()); // 当前DCN号
        ref.setMdcnno(env.getMdcnno()); // 主调DCN号
        ref.setCorpno(env.getCorpno()); // 交易法人代码
        ref.setHostip(env.getHostip()); // HOSTIP
        ref.setVmidxx(env.getVmidxx()); // VMID

        mri.setMsgseq(cnt + 1); // 追加1条记录

        mti.setRefeInfo(ref);
        mti.setRealInfo(mri);
        // 如果消息有指定DCN的则按指定的，否则默认000
        if (CommUtil.isNotNull(mri.getTdcnno()))
            mti.setTdcnno(mri.getTdcnno());
        else
            mti.setTdcnno("000");

        return mti;
    }

    private static void checkMessage(MessageRealInfo mri) {
        if (CommUtil.isNull(mri.getMtopic())) {
            throw ApError.Aplt.E0410("主题");
        }
        if (CommUtil.isNull(mri.getMsgtyp())) {
            throw ApError.Aplt.E0410("消息类型");
        }
        if (CommUtil.isNull(mri.getMsgobj())) {
            throw ApError.Aplt.E0410("消息内容");
        }
        if (mri.getMsgopt() == null) {
            mri.setMsgopt(E_MSGOPT.SUCESS); // 默认成功发送
        }

        ElementType et = ModelFactoryUtil.getModelFactory().getModel(ElementType.class, mri.getMsgtyp());
        if (et == null)
            throw ApError.Aplt.E0411(mri.getMsgtyp());

        // 判断msgtyp与msgobj是否匹配
        if (!et.getJavaClass().isAssignableFrom(mri.getMsgobj().getClass())) {
            throw ApError.Aplt.E0412(et.getJavaClass().getSimpleName(), mri.getMsgobj().getClass().getSimpleName());
        }
    }

    /**
     * 发布或批量保存消息
     * <p>
     * 有两种情况：
     * <li>联机时，直接异步发送消息
     * <li>批量时，自动批量写入未处理表
     * 
     * @param msgopt
     *        消息发布条件：S-成功,F-失败，B-均处理
     */
    public static void publishOrSave(E_MSGOPT msgopt) {

        boolean b = SystemType.batch == SysUtil.getCurrentSystemType();
        String str = b ? "批量入库" : "发送";

        if (log.isInfoEnabled())
            log.info("准备[%s][%s]异步消息！", str, msgopt);

        if (log.isInfoEnabled())
            log.info("总消息数为[%d].", getMsgList().size());

        // 按事务ID分组存放
        Map<String, List<MessageTotalInfo>> sMap = new HashMap<String, List<MessageTotalInfo>>();
        for (MessageTotalInfo msg : getMsgList()) {
            if (msgopt == msg.getRealInfo().getMsgopt() || msg.getRealInfo().getMsgopt() == E_MSGOPT.BOTH) {
                // 生成消息ID
                msg.getRealInfo().setMessid(getMessageId(msg.getRefeInfo().getTransq()));
                log.info("生成消息ID");
                // 生成事务ID
                E_CONSCY conscy = getDef(msg.getRealInfo().getMtopic()).getConscy();
                String affaid = getAffaId(conscy, msg.getRefeInfo().getMntrsq(), msg.getRefeInfo().getTransq(), msg
                        .getRealInfo().getMessid());
                msg.getRealInfo().setAffaid(affaid);
                log.info("生成事务ID");
                if (!sMap.containsKey(affaid)) {
                    sMap.put(affaid, new ArrayList<MessageTotalInfo>());
                }
                sMap.get(affaid).add(msg);
                log.info("add(msg)");
            }
        }

        if (sMap.isEmpty()) {
            if (log.isInfoEnabled())
                log.info("可发送消息总数为0！");
            return;
        }

        for (List<MessageTotalInfo> list : sMap.values()) {
            Collections.sort(list, new Comparator<MessageTotalInfo>() {
                // 重写排序规则
                public int compare(MessageTotalInfo o1, MessageTotalInfo o2) {
                    if (o1.getRealInfo().getMsgseq() > o2.getRealInfo().getMsgseq())
                        return 1;
                    else
                        return 0;
                }
            });
            int cursq = 1;
            int total = list.size();
            for (MessageTotalInfo mti : list) {
                mti.setAffseq(cursq);
                mti.setAffcnt(total);
                cursq++;
            }
            _publishOrSave(list, b, str);
        }

        //保存或发送后，清理消息
        clear();

    }

    private static void _publishOrSave(List<MessageTotalInfo> toSendList, boolean b, String str) {
        if (log.isInfoEnabled())
            log.info("符合[%s]条件的消息数为[%d].", str, toSendList.size());

        if (b) { // 批量直接入库
            saveBatch(toSendList);
        } else {
            final List<MessageTotalInfo> failList = new ArrayList<MessageTotalInfo>();
            for (MessageTotalInfo mti : toSendList) {
                boolean sucess = false;
                try {

                    // FIXME 测试部分
                    /*
                     * Options<MessageConsInfo> consInfos = new
                     * DefaultOptions<MessageConsInfo>(); MessageConsInfo
                     * consInfo = SysUtil.getInstance(MessageConsInfo.class);
                     * consInfo.setTacorp("001"); consInfo.setTardcn("AA0");
                     * consInfo.setTsyscd("010"); consInfos.add(consInfo);
                     * mti.setConsInfo(consInfos);
                     */
                    // ------------------end----------------
                    realPublish(mti);
                    sucess = true;
                } catch (Exception e) {
                    log.error("调用真正发布消息服务时失败，跳过并继续尝试发布下条消息，失败原因：", e);
                 //暂时不修改   if (getDef(mti.getRealInfo().getMtopic()).getMsgmod() == E_MSGMOD.QUEUE) {//20171030,add by xw 广播模式不登记
                        failList.add(mti);
                //    }
                }
                if (sucess) {
                    try {
                        registTopic(mti);
                        //保存成功消息
                        final MessageTotalInfo fmti = mti;
                        if (log.isDebugEnabled())
                            log.info("[联机]消息异步发送成功，开始独立事物保存消息到发送成功表!");
                        DaoUtil.executeInNewTransation(new RunnableWithReturn<Integer>() {
                            @Override
                            public Integer execute() {
                                saveMessSucc(fmti);
                                return null;
                            }
                        });
                    } catch (Exception e) {
                        log.error("消息发送已成功，但在登记订阅模式相关消费者时失败，不会中断发布消息，失败原因：", e);
                        log.error("记订阅模式相关消费者出错的消息为：" + mti);
                    }
                }
            }

            // 如果存在发送失败的，独立事务写到本地库中
            if (failList.size() > 0) {
                DBTools.exeInNewTransation(new RunnableWithReturn<Void>() {
                    @Override
                    public Void execute() {
                        saveBatch(failList);
                        return null;
                    }
                });

            }
        }
    }

    /**
     * 真正异步发布消息(批量发送的也可以调用此方法发送)
     * 
     * @param mti
     *        消息主题对象
     */
    public static void realPublish(final MessageTotalInfo mti) {
        if (log.isDebugEnabled())
            log.debug("准备处理消息发送的全量消息（MTI）为:[%s]", mti);

        String mtopic = mti.getRealInfo().getMtopic();
        IoApTopicReceipt rc = SysUtil.getInstance(IoApTopicReceipt.class);
        AppTopiDefiCType defi = rc.getTopicDefine(mtopic);

        IoApAsyncMessage svc = null;
        if (defi.getSvcopt() == E_SVCOPT.MTI) {
            // 以服务调用方式执行：服务绑定表ksys_ywfwbd表中必须以主题作为绑定（bindid）!!!
            // 可配置不同实现（异步外调交易或异步外调服务）
            try {
                svc = CommTools.getRemoteInstance(IoApAsyncMessage.class, mti.getRealInfo().getMtopic());
            } catch (Exception e) {
                svc = CommTools.getRemoteInstance(IoApAsyncMessage.class);
            }
            if (log.isDebugEnabled())
                log.debug("实际发送的消息为全量消息（MTI）。");
            svc.publish(mti);
        } else if (defi.getSvcopt() == E_SVCOPT.MRI) {
            try {
                svc = CommTools.getRemoteInstance(IoApAsyncMessage.class, mti.getRealInfo().getMtopic());
            } catch (Exception e) {
                svc = CommTools.getRemoteInstance(IoApAsyncMessage.class);
            }
            if (log.isDebugEnabled())
                log.debug("实际发送的消息为MRI:[%s]", mti.getRealInfo());
            svc.publishMri(mti.getRealInfo());
        } else if (defi.getSvcopt() == E_SVCOPT.USR) {
            if (log.isDebugEnabled())
                log.debug("实际发送的消息为用户复合类型消息：[%s]", mti.getRealInfo().getMsgobj());
            _callService(defi.getSvceid(), defi.getSvcebd(), mti.getRealInfo().getMsgobj());
        } else {
            log.error("此消息主题的[消息发送服务数据范围]配置不正确！将不会发送此消息！");
        }
    }

    private static void saveMessSucc(MessageTotalInfo data) {
        if (log.isDebugEnabled())
            log.debug("准备保存成功消息[%s]", data);
        SysUtil.getInstance(IoApAsyncMessage.class).saveMessSucc(data); // 此方法是本地调用
        if (log.isDebugEnabled())
            log.debug("保存成功消息完毕！");
    }

    // 获得消息的事务ID
    public static String getAffaId(E_CONSCY conscy, String mntrsq, String transq, String messid) {
        if (conscy == E_CONSCY.BY_MNTRSQ) {
            return mntrsq;
        } else if (conscy == E_CONSCY.BY_TRANSQ) {
            return transq;
        } else {
            return messid;
        }
    }

    // 消息ID=流水号+4位序号
    public static String getMessageId(String transq) {
        String seq = CommTools.getCurrentThreadSeq(_KEY_MESSID_PREFIX, transq) + "";
        return transq + CommUtil.lpad(seq, _MESSID_SEQ_LENGTH, "0");
    }

    // 调用登记订阅类型相关数据
    private static void registTopic(MessageTotalInfo mti) {
        if (getDef(mti.getRealInfo().getMtopic()).getMsgmod() != E_MSGMOD.TOPIC) {
            return;
        }

        IoApTopicReceipt atr = SysUtil.getInstance(IoApTopicReceipt.class);
        atr.registSubscribers(mti);

    }

    private static void saveBatch(List<MessageTotalInfo> mtiList) {
        for (MessageTotalInfo data : mtiList) {
            if (log.isDebugEnabled())
                log.debug("准备保存消息[%s]", data);
            SysUtil.getInstance(IoApAsyncMessage.class).saveMessage(data); // 此方法是本地调用
            if (log.isDebugEnabled())
                log.debug("保存消息完毕！");
        }
    }

    private static AppTopiDefiCType getDef(String topcid) {
        IoApTopicReceipt tc = SysUtil.getInstance(IoApTopicReceipt.class);
        AppTopiDefiCType topicDefine = tc.getTopicDefine(topcid);
        return topicDefine;
    }

    private static Map<String, Object> _callService(String serviceId, String callIdentity, Object data) {
        // 初始化运行上下文
        Map<String, Object> input = CommUtil.toMap(data);
        DataArea dataArea = DataArea.buildWithInput(input);
        if (log.isDebugEnabled()) {
            log.debug("_callService input:" + input);
            log.debug("_callService dataArea:" + dataArea);
        }

/*        // //////////////////// 执行服务 /////////////////////  20200429 TODO 补执行服务的方式
        // 获取服务绑定信息
        IServiceBind bind = ServiceUtil.getServiceBind(callIdentity, serviceId);
        // 获取服务执行器
        RemoteServiceExecutor serviceExecutor = ExtensionUtil.getExtensionPointImpl(ServiceExecutor.POINT,
                bind.getExecutorId());
        // 获取服务控制器
        IServiceController controller = ServiceUtil.getServiceController(serviceId);
        // 创建请求对象
        ServiceRequest request = new ServiceRequest(dataArea, bind, controller);
        // 需要路由的服务（1、未配置或配置了本地执行器则不需要路由，配置了其它执行器则需要路由）
        IServiceRouterManager routerManager = ExtensionUtil.getExtensionPointImpl(IServiceRouterManager.POINT);
        IServiceRouter initRouter = routerManager.initBizServiceRoute(request);
        IServiceRouter serviceRoute = routerManager.getBizServiceRoute(request, initRouter);
        request.setRouter(serviceRoute);
        // 在调用服务前，将调用标识压入栈中
        ServiceResponse response = new ServiceResponse(DataArea.buildWithEmpty());
        // 远程服务执行前处理 -- 2017.8.24 
        IRemoteServiceProcess process = ExtensionUtil.getExtensionPointImpl(IRemoteServiceProcess.POINT);
        process.callRemoteBefore(request);

        serviceExecutor.callRemote(request, response);*/


        return null;
    }

}
