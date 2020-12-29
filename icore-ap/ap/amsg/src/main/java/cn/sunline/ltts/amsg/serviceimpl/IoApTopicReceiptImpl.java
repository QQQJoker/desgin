package cn.sunline.ltts.amsg.serviceimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.ltts.amsg.namedsql.ApMsgNsqlDao;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTopiDefi;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTopiDefiDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTopiSubs;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTopiSubsDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessData;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessDataDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsTopiDetl;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsTopiDetlDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsTopiMess;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsTopiMessDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsTopiPush;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsTopiPushDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppCorp;
import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType.AppTopiDefiCType;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType.ReceiptCType;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageConsInfo;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageRealInfo;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageTotalInfo;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYNCST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * 配置同步消息实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoApTopicReceiptImpl", longname = "配置同步消息实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoApTopicReceiptImpl implements cn.sunline.ltts.amsg.servicetype.IoApTopicReceipt {

    private final static String _ALL_CORPNO_DCN = "%";// 全法人或DCN
    private final static String _CUR_CORPNO = "*";// 当前法人
    private static final BizLog log = BizLogUtil.getBizLog(IoApTopicReceiptImpl.class);

    /**
     * 登记订阅者
     * <p>
     * 作为发布方时，登记订阅者
     */
    @Override
    public void registSubscribers(MessageTotalInfo mtinfo) {

        // --------------- 登记簿登记 -------------------
        String affaid = mtinfo.getRealInfo().getAffaid();
        this.registMess(affaid, mtinfo.getRealInfo().getMessid(), mtinfo.getRealInfo().getMtopic());

        ApsTopiPush push = SysUtil.getInstance(ApsTopiPush.class);
        push.setAffaid(affaid);
        push.setCorpno(mtinfo.getRefeInfo().getCorpno());
        push.setSendcn(mtinfo.getRefeInfo().getCdcnno());
        push.setSsyscd(SysUtil.getSystemId());
        push.setConstm(null);
        push.setStatus(E_SYNCST.START);
        ApsTopiPushDao.insert(push);

        Map<String, ApsTopiDetl> detlMap = null;

        // 给定消费者列表的，不考虑参数定义
        if (mtinfo.getConsInfo() != null && mtinfo.getConsInfo().size() > 0) {
            int detlsq = 0;
            detlMap = new HashMap<String, ApsTopiDetl>();
            for (MessageConsInfo mci : mtinfo.getConsInfo()) {
                // 因为获得的ApsTopiDetl可能重复，所以需要并不一定是递增的，而是Map的长度+1,状态为已发送
                detlsq = this.putApsDetailMapPublisher(detlMap,
                        this.getApsTopiDetl(mtinfo.getRealInfo(), mci, detlsq + 1, E_YES___.NO, E_SYNCST.SENDED));
            }
        } else {
            detlMap = this.preparegetApsTopiDetlMap(mtinfo);
        }

        if (detlMap.size() > 0) {
            log.debug("detlMap大小[%d]", detlMap.size());
            List<ApsTopiDetl> detlList = new ArrayList<ApsTopiDetl>(detlMap.values());
            // 批量插入
            DaoUtil.insertBatch(ApsTopiDetl.class, detlList);
        }
    }

    private Map<String, ApsTopiDetl> preparegetApsTopiDetlMap(MessageTotalInfo mtinfo) {

        boolean multCorpno = false;
        boolean multDcnno = false;
        String topcid = mtinfo.getRealInfo().getMtopic();
        String secorp = mtinfo.getRefeInfo().getCorpno();// 发布法人号

        // 查询订阅定义
        List<AppTopiSubs> subsList = AppTopiSubsDao.selectAll_odb2(topcid, SysUtil.getSystemId(), true);
        // 获得全部DCN列表 TODO 目前DRS无法按系统区分DCN
        //	IDRSClient drsClient = DRSFactoryManager.getInstance().getFactory().getDRSClient();
        List<String> dcnlist = DcnUtil.findAllDcnNosWithAdmin();
        //	List<DCN> dcnlist = drsClient.getCDCN();
        // 获得全部法人列表
        List<AppCorp> corpList = DaoUtil.selectAll(AppCorp.class);
        ApsTopiDetl apsDetl = null;
        MessageConsInfo consInfo = null;
        String tadcnno = null;
        String tacorp = null;
        int detlsq = 0;
        Map<String, ApsTopiDetl> detlMap = new HashMap<String, ApsTopiDetl>();

        for (AppTopiSubs subs : subsList) {

            multCorpno = false;
            multDcnno = false;

            // 法人处理
            if (_ALL_CORPNO_DCN.equals(subs.getScopno())) { // 全法人
                multCorpno = true; // 如果是全法人，先标记，后面统一处理
            } else if (_CUR_CORPNO.equals(subs.getScopno())) { // 当前法人
                tacorp = secorp;
            } else {
                tacorp = subs.getScopno(); // 指定法人
            }

            // DCN处理
            if (_ALL_CORPNO_DCN.equals(subs.getSdcnno())) { // 全部DCN
                multDcnno = true; // 如果是全DCN，先标记，后面统一处理
            } else {// 指定DCN
                tadcnno = subs.getSdcnno();
            }

            // 统一处理
            if (multCorpno && multDcnno) { // 全法人和DCN
                for (AppCorp corp : corpList) {
                    for (String dcn : dcnlist) {
                        consInfo = this.getConsInfo(corp.getCorpno(), dcn, subs.getSsyscd());
                        apsDetl = this.getApsTopiDetl(mtinfo.getRealInfo(), consInfo, detlsq + 1, E_YES___.NO,
                                E_SYNCST.SENDED);
                        detlsq = putApsDetailMapPublisher(detlMap, apsDetl);
                    }
                }
            } else if (multCorpno) { // 仅全法人
                for (AppCorp corp : corpList) {
                    consInfo = this.getConsInfo(corp.getCorpno(), tadcnno, subs.getSsyscd());
                    apsDetl = this.getApsTopiDetl(mtinfo.getRealInfo(), consInfo, detlsq + 1, E_YES___.NO,
                            E_SYNCST.SENDED);
                    detlsq = putApsDetailMapPublisher(detlMap, apsDetl);
                }
            } else if (multDcnno) {// 仅全DCN
                for (String dcn : dcnlist) {
                    consInfo = this.getConsInfo(tacorp, dcn, subs.getSsyscd());
                    apsDetl = this.getApsTopiDetl(mtinfo.getRealInfo(), consInfo, detlsq + 1, E_YES___.NO,
                            E_SYNCST.SENDED);
                    detlsq = putApsDetailMapPublisher(detlMap, apsDetl);
                }
            } else {
                consInfo = this.getConsInfo(tacorp, tadcnno, subs.getSsyscd());
                apsDetl = this.getApsTopiDetl(mtinfo.getRealInfo(), consInfo, detlsq + 1, E_YES___.NO, E_SYNCST.SENDED);
                detlsq = putApsDetailMapPublisher(detlMap, apsDetl);
            }
        }

        return detlMap;
    }

    private MessageConsInfo getConsInfo(String corpno, String dcnno, String tsyscd) {
        MessageConsInfo consInfo = SysUtil.getInstance(MessageConsInfo.class);
        consInfo.setTacorp(corpno);
        consInfo.setTardcn(dcnno);
        consInfo.setTsyscd(tsyscd);
        return consInfo;
    }

    /**
     * 订阅者处理 作为消费者使用
     */
    @Override
    public void subscriberProcess(MessageTotalInfo mtinfo) {
        int detlsq = 0;
        List<ApsTopiDetl> list = new ArrayList<ApsTopiDetl>();

        //-----------------start by zhangli ---------------------
        //当异步运行参数中没有设置对应消费者就应该去数据库中查找。
        List<MessageConsInfo> consInfos = mtinfo.getConsInfo();
        if (consInfos == null || consInfos.size() <= 0) {
            Map<String, ApsTopiDetl> detlMap = preparegetApsTopiDetlMap(mtinfo);
            for (Map.Entry<String, ApsTopiDetl> entity : detlMap.entrySet()) {
                ApsTopiDetl apsTopiDetl = entity.getValue();
                MessageConsInfo cons = CommTools.getInstance(MessageConsInfo.class);
                cons.setTsyscd(apsTopiDetl.getTsyscd());
                cons.setTardcn(apsTopiDetl.getTardcn());
                cons.setTacorp(apsTopiDetl.getTacorp());
                consInfos.add(cons);
            }
        }
        //------------------end by zhangli --------------

        for (MessageConsInfo mci : consInfos) {
            // 只有属于当前系统和当前DCN的才能消费。
            if (CommUtil.equals(mci.getTardcn(), DcnUtil.getCurrDCN())
                    && CommUtil.equals(mci.getTsyscd(), SysUtil.getSystemId())) {
                detlsq++;
                list.add(this.getApsTopiDetl(mtinfo.getRealInfo(), mci, detlsq, E_YES___.YES, E_SYNCST.RECEIVED)); // 状态为已接收
            }
        }
        if (list.size() > 0) {
            //不是所有情况都需要插入 订阅类消息订阅者明细表 只有一个事务的第一条才插入
            int cnt = ApMsgNsqlDao.selTopiDetlCountByAffaid(mtinfo.getRealInfo().getAffaid(), CommTools.prcRunEnvs().getCorpno(), false);
            if (cnt <= 0) {
                DaoUtil.insertBatch(ApsTopiDetl.class, list);
            }
            /*本地登记消息内容 
             * @author zhangli
             */
            ApsMessData data = SysUtil.getInstance(ApsMessData.class);
            data.setTrandt(mtinfo.getRefeInfo().getTrandt()); // 交易日期（分区）
            data.setMessid(mtinfo.getRealInfo().getMessid()); // 消息ID
            data.setTopcid(mtinfo.getRealInfo().getMtopic()); // 消息主题ID
            AppTopiDefi topiDefi = AppTopiDefiDao.selectOne_odb1(mtinfo.getRealInfo().getMtopic(), SysUtil.getSystemId(), true);
            data.setMetype(topiDefi.getMetype()); // 消息类型
            // data.setMd5chk(); // MD5校验
            // data.setPremod(); // 压缩方式
            data.setMesobj(mtinfo.getRealInfo().getMsgtyp()); // 消息对象类
            // data.setAfpres(); // 消息压缩内容
            data.setMedata(SysUtil.serialize(mtinfo.getRealInfo().getMsgobj())); // 消息明文内容
            ApsMessDataDao.insert(data);
            //------------------ 结束 by zhangli-----------------

            ApsTopiMess mess = SysUtil.getInstance(ApsTopiMess.class);
            mess.setAffaid(mtinfo.getRealInfo().getAffaid());
            mess.setMessid(mtinfo.getRealInfo().getMessid());
            mess.setTopcid(mtinfo.getRealInfo().getMtopic());
            mess.setPumesq(mtinfo.getRealInfo().getMsgseq());
            ApsTopiMessDao.insert(mess);
        }

        /*ApsTopiMess mess = SysUtil.getInstance(ApsTopiMess.class);
        mess.setAffaid(mtinfo.getRealInfo().getAffaid());
        mess.setMessid(mtinfo.getRealInfo().getMessid());
        mess.setTopcid(mtinfo.getRealInfo().getMtopic());
        mess.setPumesq(mtinfo.getRealInfo().getMsgseq());
        ApsTopiMessDao.insert(mess);*/
    }

    /**
     * 回执处理服务 消费者端使用
     */
    @Override
    public void receiptPushlisher(String affaid, String tacorp, String tardcn, String tsyscd, E_SYNCST status) {

        // 查询出来才是准确的
        final List<ApsTopiDetl> detlList = ApsTopiDetlDao.selectAll_odb2(affaid, tacorp, tardcn, tsyscd, false);

        if (detlList == null || detlList.size() <= 0) {
            log.error("收到订阅模式的消息回执:affaid[%s]，tacorp[%s]，tardcn[%s]，tsyscd[%s],但未找到订阅者登记!", affaid, tacorp, tardcn,
                    tsyscd);
            throw ApError.Aplt.E0000("收到订阅模式的消息回执，但未找到订阅者登记！");
        }
        List<ApsTopiMess> messList = ApsTopiMessDao.selectAll_odb1(affaid, true);

        for (ApsTopiDetl detl : detlList) {
            detl.setStatus(status);
            detl.setConstm(DateUtil.getNow(null));
            for (ApsTopiMess mess : messList) {
                AsyncMessageUtil.add(this.getReceiptMessageRealInfo(detl, mess, status)); // 收集待发送的回执，由主交易事务提交后平台自动发送
            }
        }

        final List<Object> objs = new ArrayList<Object>(detlList);

        // 更改状态
        if (status == E_SYNCST.SUCC) {
            //DaoUtil.updateBatchByIndex(ApsTopiDetl.class, ApsTopiDetl.odb1.class, detlList);
            DaoUtil.updateBatch(ApMsgNsqlDao.namedsql_upTopiDetlByAffaidAndDetlsq, objs);
        } else {
            // 不成功使用独立事务
            DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
                public Void execute() {
                    //DaoUtil.updateBatchByIndex(ApsTopiDetl.class, ApsTopiDetl.odb1.class, detlList);
                    DaoUtil.updateBatch(ApMsgNsqlDao.namedsql_upTopiDetlByAffaidAndDetlsq, objs);
                    return null;
                }
            });
        }
    }

    @Override
    public AppTopiDefiCType getTopicDefine(String topcid) {
        AppTopiDefi src = AppTopiDefiDao.selectOne_odb1(topcid, SysUtil.getSystemId(), false);
        if(CommUtil.isNull(src)){
        	throw ApError.Aplt.E0000("查询消息主题定义表失败，无对应记录");
        }
        AppTopiDefiCType ret = SysUtil.getInstance(AppTopiDefiCType.class);
        CommUtil.copyProperties(ret, src);
        return ret;
    }

    /**
     * 登记发布事务消息明细
     * 
     * @param mesgid
     *        事务ID
     * @param messid
     *        消息ID
     * @return
     */
    private void registMess(String affaid, String messid, String topcid) {

        ApsTopiMess mess = SysUtil.getInstance(ApsTopiMess.class);
        mess.setAffaid(affaid);
        mess.setPumesq(1);
        mess.setMessid(messid);
        mess.setTopcid(topcid);
        ApsTopiMessDao.insert(mess);
    }

    // 返回添加后的数量
    private int putApsDetailMapPublisher(final Map<String, ApsTopiDetl> map, final ApsTopiDetl apsDetl) {
        if (apsDetl == null)
            return map.size();

        String key = apsDetl.getTsyscd() + "." + apsDetl.getTacorp() + "." + apsDetl.getTardcn();
        if (!map.containsKey(key)) {
            map.put(key, apsDetl);
        }

        return map.size();
    }

    private ApsTopiDetl getApsTopiDetl(MessageRealInfo realInfo, MessageConsInfo consInfo, int detlsq, E_YES___ iscons,
            E_SYNCST status) {
        // 发布端：排除本法人和本DCN   update 20171009
        if (iscons != E_YES___.YES && CommUtil.equals(consInfo.getTacorp(), CommTools.prcRunEnvs().getCorpno())
                && CommUtil.equals(consInfo.getTardcn(), CommTools.prcRunEnvs().getCdcnno()) && CommUtil.equals(consInfo.getTsyscd(), CommTools.getSystemId()))
            return null;

        ApsTopiDetl apsDetl = SysUtil.getInstance(ApsTopiDetl.class);
        apsDetl.setAffaid(realInfo.getAffaid()); // 事务ID
        apsDetl.setDetlsq(detlsq);
        apsDetl.setIscons(iscons); // 是否消费
        apsDetl.setTacorp(consInfo.getTacorp());
        apsDetl.setTardcn(consInfo.getTardcn());
        apsDetl.setTsyscd(consInfo.getTsyscd()); // 目标系统
        apsDetl.setConstm(iscons != E_YES___.YES ? null : DateUtil.getNow(null)); // 消费时间:发布端为null
        apsDetl.setStatus(status); // 状态
        return apsDetl;
    }

    // 构造回执对象
    private MessageRealInfo getReceiptMessageRealInfo(ApsTopiDetl detl, ApsTopiMess mess, E_SYNCST status) {
        MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);

        mri.setMtopic(mess.getTopcid()); // 消息主题
        mri.setAffaid(detl.getAffaid()); // 事务ID
        mri.setMessid(mess.getMessid()); // 消息ID
        mri.setMsgtyp(ReceiptCType.class.getName()); // 消息类型
        if (status == E_SYNCST.SUCC)
            mri.setMsgopt(E_MSGOPT.SUCESS); // 异步消息发送标志:成功时发送
        else
            mri.setMsgopt(E_MSGOPT.FAIL); // 异步消息发送标志：否则失败是发送

        ReceiptCType rct = SysUtil.getInstance(ReceiptCType.class);
        rct.setAffaid(detl.getAffaid()); // 事务ID
        rct.setTopcid(mess.getTopcid()); // 消息主题ID
        rct.setMessid(mess.getMessid()); // 消息ID
        rct.setCcopno(detl.getTacorp()); // 消费法人
        rct.setCdcnno(detl.getTardcn()); // 消费DCN
        rct.setCsyscd(detl.getTsyscd()); // 消费系统
        rct.setConstm(detl.getConstm()); // 消费时间
        rct.setStatus(status); //消费状态
        mri.setMsgobj(rct); // 消息复合对象
        return mri;
    }
}
