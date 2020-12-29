package cn.sunline.ltts.amsg.util;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.base.boot.plugin.util.ExtensionUtil;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.ltts.amsg.api.SMSSender;
import cn.sunline.ltts.amsg.namedsql.ApMsgNsqlDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsShorUndoDao;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType.SMSCType;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 定时任务查寻数据库中待发送消息
 * 可配置多个定时任务
 * 查寻的结果通过配置参数控制范围（如：1000个分组,可以配置5个定时任务[0,200),[200,400),[400,600),[600,800),[800,1000),
 * 
 * @author Administrator
 * 
 */
public class SMSBatchNowSendUtil extends LttsTimerProcessor {

    private static final BizLog log = BizLogUtil.getBizLog(AsyncMessageBatchUtil.class);

    public static final String MIN_GROUP_ID = "minGroupId";
    public static final String MAX_GROUP_ID = "maxGroupId";
    public static final String BATH_TIMER_SEND_SMS_IMPL = "process.SMSSender.TCloud";

    private Integer minGroupId;
    private Integer maxGroupId;

    // dataArea中形如 {"comm_req":{"maxGroupId":"y","minGroupId":"x"}}
    @Override
    public void process(String param, DataArea dataArea) {
        initIds(dataArea);
        // 发送短信的信息数据集合
        List<ApMessageComplexType.SMSCType> smsMessageList = null;
        // 查询出有效的实时的未处理短息集合
        List<ApAmsg.ApsShorUndo> list = ApMsgNsqlDao.selNowApsShorundoByGroupId(minGroupId, maxGroupId, dataArea.getCommReq().getString("corpno"), false);
        // 采用批量定时短信发送接口实现发送短信
        SMSSender smsSender = ExtensionUtil.getExtensionPointImpl(SMSSender.POINT, BATH_TIMER_SEND_SMS_IMPL);
        if (list.size() == 0) {
            return;
        }

        for (final ApAmsg.ApsShorUndo apsShorundo : list) {

            if (apsShorundo.getMessty() == E_YES___.NO) {
                // todo
            } else {
                SMSCType sMSCType = SysUtil.getInstance(SMSCType.class);
                sMSCType.setMeteid(String.valueOf(apsShorundo.getTemplid()));
                sMSCType.setMessty(apsShorundo.getMessty());
                sMSCType.setMobile(apsShorundo.getMobile());
                try {
                    sMSCType.setMsgparm(JSON.parseObject(apsShorundo.getPara()));
                } catch (Exception e) {
                    throw new RuntimeException("String参数解析成Map类型异常", e);
                }
                sMSCType.setNacode(apsShorundo.getNacode());
                // 构造size=1的集合
                smsMessageList = new ArrayList<ApMessageComplexType.SMSCType>();
                smsMessageList.add(sMSCType);

                try {
                    smsSender.sendSMSes(smsMessageList);
                } catch (Exception e) {

                } finally {
                    DaoUtil.executeInNewTransation(new RunnableWithReturn<Integer>() {
                        @Override
                        public Integer execute() {
                            log.info("删除aps_shor_undo表中messid【" + apsShorundo.getMessid() + "】的数据");
                            return ApsShorUndoDao.deleteOne_odb1(apsShorundo.getMessid());
                        }
                    });
                }
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
        	ExceptionUtil.wrapThrow("加载未处理短信表时，定时任务初始化异常：参数[minGroupId,maxGroupId]配置错误，请检查", e);
        }
    }

    public Integer getMaxGroupId() {
        return maxGroupId;
    }

    public void setMaxGroupId(Integer maxGroupId) {
        this.maxGroupId = maxGroupId;
    }

}
