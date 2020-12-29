package cn.sunline.ltts.busi.aplt.strk.impl;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.iobus.servicetype.cd.IoCdStrikeSvcType;
import cn.sunline.ltts.busi.iobus.servicetype.cd.IoCdStrikeSvcType.CdDrawStrike;
import cn.sunline.ltts.busi.iobus.type.serv.IoStrikeType.ProcCdDrawStrikeInput;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.CdEnumType.E_DPACST;

/**
 * <p>Title:ApStrikeEvntProcessorCD06</p>
 * <p>Description:CD06支取冲正</p>
 * 
 * @author songlw
 * @date 2019年2月26日
 */
public class ApStrikeEvntProcessorCD06 implements ApStrikeEvntProcessor {

    @Override
    public void process(E_STACPS stacps, E_COLOUR colour, KnbEvnt evnt) {
        if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_CDDRAW) != 0)
            throw ApError.Aplt.E0000("交易事件不是[" + ApUtil.TRANS_EVENT_CDDRAW + "]，不能冲正！");

        CdDrawStrike.Input input = SysUtil.getInstance(CdDrawStrike.Input.class);
        ProcCdDrawStrikeInput cplIn = input.getStrikeInput();

        cplIn.setCustac(evnt.getCustac());// 电子账户
        cplIn.setAcctno(evnt.getTranac()); // 负债账号
        cplIn.setStacps(stacps);// 冲正冲账分类
        cplIn.setOrtrdt(evnt.getTrandt());// 原交易日期
        cplIn.setTranam(evnt.getTranam());// 交易金额
        cplIn.setAmntcd(evnt.getAmntcd());// 借贷标志
        cplIn.setColrfg(colour); // 红蓝字记账标识
        cplIn.setDetlsq(evnt.getTranno());// 原交易序号
        cplIn.setCrcycd(evnt.getCrcycd());
        if (CommUtil.isNotNull(evnt.getEvent1())) {
            cplIn.setInstam(new BigDecimal(evnt.getEvent1())); // 利息
        }
        if (CommUtil.isNotNull(evnt.getEvent2())) {
            cplIn.setAcctst(CommUtil.toEnum(E_DPACST.class, evnt.getEvent2())); // 账户状态
        }
        if (CommUtil.isNotNull(evnt.getEvent3())) {
            cplIn.setIntxam(new BigDecimal(evnt.getEvent3())); // 利息税
        }
        
        SysUtil.getInstance(IoCdStrikeSvcType.class).procCdDrawStrike(input);
    }

}
