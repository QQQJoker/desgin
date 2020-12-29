package cn.sunline.ltts.amsg.spi;

import java.util.List;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.engine.online.handler.OETAfterHandler;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerConstant;
import cn.sunline.adp.cedar.engine.online.handler.OETHandlerContext;
import cn.sunline.edsp.base.annotation.Groups;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.amsg.util.SMSUtil;
import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;

@SPIMeta(id="send_success_sms")
@Order(100)
@Groups({OETHandlerConstant.FLOW_ENGINE_TYPE,OETHandlerConstant.SERVICE_ENGINE_TYPE,OETHandlerConstant.BPL_ENGINE_TYPE})
public class OETSendSuccessSmsHandler implements OETAfterHandler {
	
	private static final SysLog log = SysLogUtil.getSysLog(OETSendSuccessSmsHandler.class);
	
    //短信相关专项日志
    public static final BizLog smsLog = BizLogUtil.getBizLog("sms", OETSendSuccessSmsHandler.class);

	@Override
	public void handler(OETHandlerContext var1) {
        try {
            for (ApMessageComplexType.SMSCType sms : getSMSMessageListFromRunEnvs()) {
                if (sms.getMsgopt() == E_MSGOPT.FAIL || sms.getMsgopt() == E_MSGOPT.BOTH)//只发送失败的
                    SMSUtil.sendSMSMessage(sms);
            }
        } catch (Exception e) {
            smsLog.error("dbMainTransactionRollbackAfter发送短信失败", e);
        }

        try {
            // 交易失败后发送
            AsyncMessageUtil.publishOrSave(E_MSGOPT.FAIL);
        } catch (Exception e) {
            log.error("主交易事务回滚，发送失败的异步消息失败！e:", e);
        }
		
	}
	
    private List<ApMessageComplexType.SMSCType> getSMSMessageListFromRunEnvs() {
        RunEnvsComm env = SysUtil.getTrxRunEnvs();
        return env.getSmsmsg().getValues();
    }

}
