package cn.sunline.ltts.busi.aplt.strk.impl;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.cd.IoCdStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.cd.IoCdStrikeSvcType.CdSaveStrike;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcCdSaveStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

/**
 * <p>Title:ApStrikeEvntProcessorCD05</p>
 * <p>Description: CD05存入冲正</p>
 * 
 * @author songlw
 * @date 2019年2月26日
 */
public class ApStrikeEvntProcessorCD05 implements ApStrikeEvntProcessor {
    // 日志信息
    private static final BizLog bizLog = BizLogUtil.getBizLog(ApStrikeEvntProcessorCD05.class);

    @Override
    public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
        bizLog.method(">>>>>>>>>>单位账户存入冲正开始>>>>>>>>>>");
        if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CDSAVE) != 0)
            throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CDSAVE + "]，不能冲正！");

        CdSaveStrike.Input input = SysUtil.getInstance(CdSaveStrike.Input.class);
        ProcCdSaveStrikeInput cplIn = input.getStrikeInput();
        cplIn.setCustac(evnt.getCustac());// 电子账户
        cplIn.setAcctno(evnt.getTranac()); // 负债账号
        cplIn.setStacps(stacps);// 冲正冲账分类
        cplIn.setOrtrdt(evnt.getTrandt());// 原交易日期
        cplIn.setTranam(evnt.getTranam());// 交易金额
        cplIn.setAmntcd(evnt.getAmntcd());// 借贷标志
        cplIn.setColrfg(colour); // 红蓝字记账标识
        cplIn.setDetlsq(evnt.getTranno());// 原交易序号
        cplIn.setCrcycd(evnt.getCrcycd());
        SysUtil.getInstance(IoCdStrikeSvcType.class).procCdSaveStrike(input);

        bizLog.method(">>>>>>>>>>单位账户存入冲正结束>>>>>>>>>>");
    }

}
