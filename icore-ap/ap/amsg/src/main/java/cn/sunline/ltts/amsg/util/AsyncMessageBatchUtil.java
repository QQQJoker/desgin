package cn.sunline.ltts.amsg.util;

import java.util.List;

import org.aspectj.util.LangUtil;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.amsg.namedsql.ApMsgNsqlDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTopiDefi;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.AppTopiDefiDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessData;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessDataDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessFail;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessFailDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessSucc;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessSuccDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessUndoDao;
import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageTotalInfo;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType;

/**
 * 定时任务查寻数据库中待发送消息
 * 可配置多个定时任务
 * 查寻的结果通过配置参数控制范围（如：1000个分组,可以配置5个定时任务[0,200),[200,400),[400,600),[600,800),[800,1000),
 * 
 * @author Administrator
 * 
 */
public class AsyncMessageBatchUtil extends LttsTimerProcessor {

    private static final BizLog log = BizLogUtil.getBizLog(AsyncMessageBatchUtil.class);

    public static final String MIN_GROUP_ID = "minGroupId";
    public static final String MAX_GROUP_ID = "maxGroupId";

    private Integer minGroupId;
    private Integer maxGroupId;

    // dataArea中形如 {"comm_req":{"maxGroupId":"y","minGroupId":"x"}}
    @Override
    public void process(String param, DataArea dataArea) {
        initIds(dataArea);
        List<ApAmsg.ApsMessUndo> list = ApMsgNsqlDao.selApsMessUndoByGroupId(minGroupId, maxGroupId, dataArea.getCommReq().getString("corpno"), false);
        if (list.size() == 0) {
            return;
        }
        for (final ApAmsg.ApsMessUndo apsMessUndo : list) {
            // 通过消息主题ID,查询出消息发送服务数据范围枚举类型
            AppTopiDefi appTopiDefi = AppTopiDefiDao.selectOne_odb1(apsMessUndo.getTopcid(), SysUtil.getSystemId(), true);
            ApsMessData apsMessData = ApsMessDataDao.selectOne_odb1(apsMessUndo.getMessid(), apsMessUndo.getTrandt(), true);
            MessageTotalInfo mti = null;
            // 成功消息表
            final ApsMessSucc apsMessSucc = SysUtil.getInstance(ApsMessSucc.class);
            final ApsMessFail apsMessFail = SysUtil.getInstance(ApsMessFail.class);
            try {
                if (appTopiDefi.getSvcopt() == BaseEnumType.E_SVCOPT.MTI) {
                    mti = SysUtil.deserialize(apsMessData.getMedata(), MessageTotalInfo.class);
                } else if (appTopiDefi.getSvcopt() == BaseEnumType.E_SVCOPT.MRI) {
                    mti = SysUtil.getInstance(MessageTotalInfo.class);
                    mti.setRealInfo(SysUtil.deserialize(apsMessData.getMedata(), TrxBaseEnvs.MessageRealInfo.class));
                } else if (appTopiDefi.getSvcopt() == BaseEnumType.E_SVCOPT.USR) {
                    mti = SysUtil.getInstance(MessageTotalInfo.class);
                    // 若为用户消息 的类型，则要将消息主题Id给设值进去
                    mti.getRealInfo().setMtopic(apsMessUndo.getTopcid());
                    mti.getRealInfo().setMsgobj(SysUtil.deserialize(apsMessData.getMedata(), Object.class));// 消息明文内容
                }
            } catch (Exception e) {
                throw ExceptionUtil.wrapThrow("反序列化出现异常请检查", e);
            }
            try {
                log.info("批量定时发送消息前，将未处理消息表中busisq字段的值赋值到公共运行变量中");
                RunEnvsComm runEnvsComm = SysUtil.getTrxRunEnvs();
                if (CommUtil.isNull(runEnvsComm.getBusisq())) {
                    if (CommUtil.isNull(apsMessUndo.getBusisq())) {
                        log.info("本次messid为：【" + apsMessUndo.getMessid() + "】的消息busisq为空，不进行发送，请检查！");
                        continue;
                    } else {
                        log.info("本次messid为：【" + apsMessUndo.getMessid() + "】的消息busisq的值为【" + apsMessUndo.getBusisq() + "】将设值到公共运行变量中");
                        runEnvsComm.setBusisq(apsMessUndo.getBusisq());
                    }
                }
                AsyncMessageUtil.realPublish(mti);
                log.info("批量消息messid为：【" + apsMessUndo.getMessid() + "】异步发送成功，开始独立事物保存消息到发送成功表!");
                DaoUtil.executeInNewTransation(new RunnableWithReturn<Integer>() {
                    @Override
                    public Integer execute() {
                        // 将未处理消息表中的属性复制到成功消息表中
                        CommUtil.copyProperties(apsMessSucc, apsMessUndo);
                        ApsMessSuccDao.insert(apsMessSucc);
                        return null;
                    }
                });
            } catch (Exception e) {
                log.error("批量消息messid为：【" + apsMessUndo.getMessid() + "】发送失败，消息保存至消息失败表", e);
                CommUtil.copyProperties(apsMessFail, apsMessUndo);
                ApsMessFailDao.insert(apsMessFail);
            } finally {
                    log.info("批量消息messid为：【" + apsMessUndo.getMessid() + "】开始删除消息待发表!");
                    // 将未处理消息表中的属性复制到成功消息表中
                    ApsMessUndoDao.deleteOne_odb2(apsMessUndo.getMessid());
               

            }
        }
    }

    //初始化当前定时
    private void initIds(DataArea dataArea) {
        if (minGroupId != null) {
            return;
        }
        try {
            minGroupId = Integer.valueOf((String) dataArea.getCommReq().get(MIN_GROUP_ID));
            maxGroupId = Integer.valueOf((String) dataArea.getCommReq().get(MAX_GROUP_ID));
        } catch (Exception e) {
        	ExceptionUtil.wrapThrow("加载未处理消息表时，定时任务初始化异常：参数[minGroupId,maxGroupId]配置错误，请检查", e);
        }
    }

    public Integer getMaxGroupId() {
        return maxGroupId;
    }

    public void setMaxGroupId(Integer maxGroupId) {
        this.maxGroupId = maxGroupId;
    }

}
