package cn.sunline.ltts.busi.aplt.transaction;

import java.text.SimpleDateFormat;
import java.util.List;

import cn.sunline.adp.cedar.service.router.drs.util.CustomDRSUtil.TargetInfo;
import cn.sunline.adp.core.exception.AdpBusinessException;
import cn.sunline.adp.cedar.base.engine.MapListDataContext;
import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.edsp.base.lang.RunnableWithReturn;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.engine.online.config.OnlineEngineConfigManager;
import cn.sunline.adp.cedar.server.online.tables.ServerOnlineTable.Tsp_service_in_logDao;
import cn.sunline.adp.cedar.server.online.tables.ServerOnlineTable.tsp_service_in_log;
import cn.sunline.ltts.busi.ap.iobus.type.IoApReverseType.IoApReverseIn;
import cn.sunline.ltts.busi.aplt.coderule.ApUtil;
import cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC;
import cn.sunline.ltts.busi.aplt.servicetype.IoApTransactionTCC.rollbackOSVC.InputSetter;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvntDao;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvntStrk;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvntStrkDao;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsRedu;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsReduDao;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTran;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTranDao;
import cn.sunline.ltts.busi.aplt.tools.BaseEnvUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.iobus.type.ap.IoApStrikeType.IoApRegBook;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_CORRFG;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_VOBKFG;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_CALLST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_EVNTLV;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_RVFXST;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TXNSTS;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.type.SPEnumType.E_PROCSTATUS;
//import cn.sunline.ltts.plugin.online.engine.EngineExtensionManager;

/**
 * 文件功能说明： 冲账、冲正相关业务逻辑
 */
public class ApStrike {

    private static final BizLog bizlog = BizLogUtil.getBizLog(ApStrike.class);

    /**
     * 冲正登记事件(子序号不需要提供，自动再次获取)
     * 分两类：本地事件和外调事件，本地事件的事件级别sevnlv一定为NONE，外调事件的事务级别根据被调的服务事务传播方式和远程返回的事务级别确定。
     * 注意：远程返回的事件级别也是在本方法中将RunEnv中的evntlv设置后返回，关键是要搞清楚当前是本地执行还是在远程执行
     */
    public static void regBook(IoApRegBook cplInput) {
        if (bizlog.isDebugEnabled())
            bizlog.debug("准备登记冲正事件：" + cplInput);

        // 日终批量不登记冲账事件
        if (ApUtil.DP_DAYEND_CHANNEL.equals(CommTools.prcRunEnvs().getServtp()))
            return;

        if (CommUtil.isNull(cplInput.getTranev())) {
            throw Aplt.E0001("交易事件");
        }
        String inpudt = CommTools.prcRunEnvs().getInpudt();
        String trandt = CommTools.prcRunEnvs().getTrandt();
        String transq = CommTools.prcRunEnvs().getTransq();
        String mntrsq = CommTools.prcRunEnvs().getMntrsq();
        KnbEvnt evnt = SysUtil.getInstance(KnbEvnt.class);
        evnt.setInpudt(inpudt);// 上送日期
        evnt.setTrandt(trandt);// 交易日期
        evnt.setTransq(transq);// 交易流水号
        evnt.setMntrsq(mntrsq); // 主交易流水号
        evnt.setLogsno(CommTools.getCurrentThreadSeq("KnbEvnt_seq"));

/*        evnt.setEvntlv(cplInput.getEvntlv() == null ? E_EVNTLV.NONE : cplInput.getEvntlv());// 外调事件级别
        if (evnt.getEvntlv() != E_EVNTLV.NONE) {
            //	evnt.setCallsq(cplInput.getCallsq());// 外调流水 TODO 查询是否需要外调流水
            evnt.setCallsq(CommTools.prcRunEnvs().getCallsq()); //20170807新增
        } else {
        	//evnt.setCallsq(null); 2018-03-21 防止重复,按：流水号+序号
            evnt.setCallsq(transq + evnt.getLogsno());
        }

        // 若未提供[外调服务状态]或为[不适用]状态时，需要校验
        if (cplInput.getCallst() == null || cplInput.getCallst() == E_CALLST.NONE) {
            evnt.setCallst(E_CALLST.NONE);
        } else {
            evnt.setCallst(cplInput.getCallst());
        }*/
        
        evnt.setCallsq(CommTools.getBranchSeq());

        RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
        evnt.setPrcscd(runEnvs.getPrcscd());// 交易码
        evnt.setTranev(cplInput.getTranev());// 交易事件
        evnt.setCustac(cplInput.getCustac());// 客户账号
        evnt.setTranac(cplInput.getTranac());// 交易账号
        evnt.setTranam(cplInput.getTranam());// 交易金额
        evnt.setAmntcd(cplInput.getAmntcd());// 借贷标志
        evnt.setCrcycd(cplInput.getCrcycd());// 货币代号
        evnt.setStayno(cplInput.getStayno());// 待销账序号
        evnt.setTranno(cplInput.getTranno());// 交易序号
        evnt.setFrozno(cplInput.getFrozno());// 冻结编号
        evnt.setBgindt(cplInput.getBgindt());// 起息日期
        evnt.setEvent1(cplInput.getEvent1());// 事件关键字1
        evnt.setEvent2(cplInput.getEvent2());// 事件关键字2
        evnt.setEvent3(cplInput.getEvent3());// 事件关键字3
        evnt.setEvent4(cplInput.getEvent4());// 事件关键字4
        evnt.setEvent5(cplInput.getEvent5());// 事件关键字5
        evnt.setEvent6(cplInput.getEvent6());// 事件关键字6
        evnt.setEvent7(cplInput.getEvent7());// 事件关键字7
        evnt.setEvent8(cplInput.getEvent8());// 事件关键字8
        evnt.setEvent9(cplInput.getEvent9());// 事件关键字9

        // 外调事件级别为本地，则当前在本地执行服务；此时可能在执行交易或远程调过来的服务，对当前交易事件级别进行设置
        if (evnt.getEvntlv() == E_EVNTLV.NONE) {
            // 事件确定为资金入账类的，设定为资金入账事件，即：需要二次提交
            if (ApUtil.needDo2Commit(cplInput.getTranev())) {
                runEnvs.setEvntlv(E_EVNTLV.CREDIT);
                evnt.setEvntlv(E_EVNTLV.CREDIT); // 交易时间设定为: 资金入账 
            } else if (runEnvs.getEvntlv() != E_EVNTLV.CREDIT) {
                // 否则只要有冲正事件，且当前env中的事件级别非资金入账，则认为交易事件级别为普通金融或维护类事件
                runEnvs.setEvntlv(E_EVNTLV.NORMAL);
                evnt.setEvntlv(E_EVNTLV.NORMAL); // 交易时间设定为: 普通金融或维护
            }
        }

        if (bizlog.isDebugEnabled())
            bizlog.debug("登记冲正事件内容：" + evnt);

        ApJournal.addKnbEvnt(evnt);
    }

    public static void updateBook(String trandt, String callsq, E_CALLST callst, E_EVNTLV evntlv) {

    	/* 2018-01-06 修改
    	KnbEvnt evnt = SysUtil.getInstance(KnbEvnt.class);
        evnt.setTrandt(trandt);
        evnt.setCallsq(callsq);
        evnt.setCallst(callst);
        evnt.setEvntlv(evntlv);
        ApJournal.updateKnbEvnt(evnt);
        */
/*    	KnbEvnt evnt = KnbEvntDao.selectOne_odb4(callsq, trandt, true);
        evnt.setCallst(callst);
        evnt.setEvntlv(evntlv);
        KnbEvntDao.updateOne_odb4(evnt);*/
        /*2018-01-06 修改结束*/
    }

    // 获得冲正交易的输入参数，并设置到RunEnv中，用于主流水登记。
    // 分布式架构下，主流水登记在beforeProcess中登记，但此时并没有开始执行交易的流程，所以只能通过平台方法获得输入
    public static void prepareEnvs() {
        RequestData data = EngineContext.getRequestData();
        MapListDataContext map = data.getInput();
        if (bizlog.isDebugEnabled())
            bizlog.debug("冲正交易，准备冲正RunEvns要素，冲正输入要内容为：" + map);

        RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
        runEnvs.getRvervo().setRviast(E_YES___.get(map.get("rviast"))); // 是否主动冲正
        runEnvs.getRvervo().setRvcdrs((String) map.get("rvcdrs")); // 当日冲正拒绝原因
        runEnvs.getRvervo().setRvodrs((String) map.get("rvodrs")); // 隔日冲正拒绝原因
        runEnvs.getRvervo().setRvfxst(E_RVFXST.get(map.get("rvfxst"))); // 冲补账状态
        runEnvs.getRvervo().setRverdt((String) map.get("rverdt")); // 冲正日期
        runEnvs.getRvervo().setRverus((String) map.get("rverus")); // 冲正柜员
        runEnvs.getRvervo().setRverbr((String) map.get("rverbr")); // 冲正机构
        runEnvs.getRvervo().setRversq((String) map.get("rversq")); // 冲正交易流水号
        runEnvs.getRvervo().setOrigdt((String) map.get("origdt")); // 原错误日期
        runEnvs.getRvervo().setOrigsq((String) map.get("origsq")); // 原错账流水号

        if (bizlog.isDebugEnabled())
            bizlog.debug("冲正交易，冲正RunEvns要素处理后内容：" + runEnvs.getRvervo());
    }

    /**
     * 功能说明：冲正服务
     * 
     * @param IoApReverseIn
     * @return String 成功00，不存在10，已经冲正20
     */
    @Deprecated
    public static String prcRollback8(IoApReverseIn cplRvIn) {
        return prcRollback8(cplRvIn, E_YES___.YES, null);
    }

    public static String prcRollback8(IoApReverseIn cplRvIn, E_YES___ ocalfg) {
        return prcRollback8_Zd(cplRvIn, ocalfg, null);
    	//return prcRollback8(cplRvIn, ocalfg, null);
    }
    
    
    /**
     * 功能说明：冲正服务
     * 
     * @param IoApReverseIn
     * @return String 成功00，不存在10，已经冲正20
     */
    public static String prcRollback8_Zd(IoApReverseIn cplRvIn, E_YES___ ocalfg, ResponseData responseData) {

        if (bizlog.isDebugEnabled())
            bizlog.debug("开始进行冲正处理：" + cplRvIn);

        // 冲正冲账分类:1-冲账 2-冲正
        if (cplRvIn.getStacps() == null)
            cplRvIn.setStacps(E_STACPS.POSITIVE);
        //查询服务接入日志表
        tsp_service_in_log  tspServiceInLog = null;
        
        //20201205 KnsRedu toStrikeRedu = null;
        // 检查是否可冲正，且获得原状态
        if (CommTools.isFlowTran()) {
        	tspServiceInLog = checkAndTspServiceIn(cplRvIn);
        }

        // 在当前环境变量中设置为冲正处理
        CommTools.setStrikeProcess();

        String otradt = null;
        String otrasq = null;
        String inpudt = null;

    	if(CommUtil.isNotNull(tspServiceInLog)) {
            otradt = new SimpleDateFormat("yyyyMMdd").format(tspServiceInLog.getEnd_time()) ;
            inpudt = cplRvIn.getOtradt(); //如果为上送系统流水，则该日期应为上送系统日期，否则该日期为原系统交易日期
            otrasq = tspServiceInLog.getBusi_seq_num();
    	} else {
            throw ApError.Sys.E9005(cplRvIn.getOtrasq());
    	}
       
        RunEnvsComm runEnv = CommTools.prcRunEnvs();
       
        // 冲正交易本身的流水由交易统一登记

        if (bizlog.isDebugEnabled())
            bizlog.debug("调用反向业务处理==========");

        // 反向处理业务数据
        try {
            prcEvent(cplRvIn, ocalfg, inpudt, otrasq);
        } catch (AdpBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }

        if (bizlog.isDebugEnabled())
            bizlog.debug("调用反向业务处理结束==========");

        // 冲账后检查:总笔数相等，所有扎差余额为零。
        /*
         * IoTaAccountBackIn cplBack =
         * SysUtil.getInstance(IoTaAccountBackIn.class);
         * 
         * cplBack.setYjiaoyls(sYjiaoyls); cplBack.setYjiaoyrq(sYjiaoyrq);
         * cplBack.setJiaoyils(sJiaoyils); cplBack.setJiaoyirq(sJiaoyirq);
         * 
         * SysUtil.getInstance(IoTaSrvStrike.class).prcTaStrikeCheck(cplBack);
         */

        String erorcd = null;
        if (responseData != null) {
            erorcd = responseData.getHeaderData().getResponseCode();
        }
        if (bizlog.isDebugEnabled())
            bizlog.debug("冲正处理结束！");

        return ApUtil.Strike_backcode_SUCCESS;// 成功返回
    }
    

    // 检查是否允许冲正或冲账
    private static tsp_service_in_log checkAndTspServiceIn(IoApReverseIn cplRvIn) {
    	tsp_service_in_log  tspServiceInLog = null;
    	tspServiceInLog = Tsp_service_in_logDao.selectOne_odb1(cplRvIn.getOtrasq(), false);
        if (tspServiceInLog == null) {
            throw ApError.Sys.E9005(cplRvIn.getOtrasq());
        }
        //交易成功
        if (tspServiceInLog.getProcess_status() == E_PROCSTATUS.S) {
        	  BaseEnvUtil.setTspServiceIn(true, tspServiceInLog); // 放入交易缓存区，以便更新时使用
              return tspServiceInLog;
        } else {
                throw ApError.Sys.E9005(cplRvIn.getOtrasq());
        }
    }
    
    
    
    
    
    
    

    /**
     * 功能说明：冲正服务
     * 
     * @param IoApReverseIn
     * @return String 成功00，不存在10，已经冲正20
     */
    public static String prcRollback8(IoApReverseIn cplRvIn, E_YES___ ocalfg, ResponseData responseData) {

        if (bizlog.isDebugEnabled())
            bizlog.debug("开始进行冲正处理：" + cplRvIn);

        // 冲正冲账分类:1-冲账 2-冲正
        if (cplRvIn.getStacps() == null)
            cplRvIn.setStacps(E_STACPS.POSITIVE);
        KnsRedu toStrikeRedu = null;
        // 检查是否可冲正，且获得原状态
        //20170808新增
        if (CommTools.isFlowTran()) {
            toStrikeRedu = checkAndGetKnsRedu(cplRvIn);
        }

        // 在当前环境变量中设置为冲正处理
        CommTools.setStrikeProcess();

        String otradt = null;
        String otrasq = null;
        String inpudt = null;
        if (cplRvIn.getOtsqtp() == E_YES___.YES) { //若按上送系统流水冲正，则获得被冲正交易的本系统流水
        	if(CommUtil.isNotNull(toStrikeRedu)) {
	            otradt = toStrikeRedu.getTrandt();
	            inpudt = cplRvIn.getOtradt(); //如果为上送系统流水，则该日期应为上送系统日期，否则该日期为原系统交易日期
	            otrasq = toStrikeRedu.getTransq();
        	} else {//不是联机交易（批量），但是通过上送系统流水冲正（API11bat）
        		KnsRedu redu = KnsReduDao.selectFirst_odb4(cplRvIn.getOtrasq(), cplRvIn.getOtradt(), false);
        		if (redu == null) {
                    throw ApError.Sys.E9005(cplRvIn.getOtrasq());
                }
        		otradt = redu.getTrandt();
	            inpudt = cplRvIn.getOtradt();
	            otrasq = redu.getTransq();
        	}
        } else {
        	if (CommTools.isFlowTran()) {
	        	KnsRedu redu = KnsReduDao.selectOne_odb3(cplRvIn.getOtrasq(), cplRvIn.getOtradt(), false);
	        	if (redu == null) {
	                throw ApError.Sys.E9005(cplRvIn.getOtrasq());
	            }
	        	inpudt = redu.getInpudt();// 上送系统日期
        	}else {
        		inpudt = cplRvIn.getOtradt();// 原系统交易日期
        	}
            otradt = cplRvIn.getOtradt();// 原交易日期
            otrasq = cplRvIn.getOtrasq();// 原交易流水
        }
        RunEnvsComm runEnv = CommTools.prcRunEnvs();
        if (CommTools.isFlowTran()) {
            // 查询原交易流水
            KnsTran tran = KnsTranDao.selectOne_odb1(otrasq, otradt, false);

            // 检查是否允许冲正
            String retcd = checkAllow(tran);
            if (retcd != null)
                return retcd;

            // 更改原流水冲正要素
            tran.setRvfxst(E_RVFXST.REVERSED);
            tran.setRverbr(runEnv.getTranbr());
            tran.setRverus(runEnv.getTranus());
            tran.setRverdt(runEnv.getTrandt());
            tran.setRversq(runEnv.getTransq());
            tran.setSpaco1(CommTools.prcRunEnvs().getRemark());
            // 修改原交易流水信息
            ApJournal.updateKnsTran(tran);
        }
        // 冲正交易本身的流水由交易统一登记
        setDefaltValue(cplRvIn, runEnv.getTrandt());

        if (bizlog.isDebugEnabled())
            bizlog.debug("调用反向业务处理==========");

        // 反向处理业务数据
        try {
            prcEvent(cplRvIn, ocalfg, inpudt, otrasq);
        } catch (AdpBusinessException e) {
            String erorcd = "";
            if (responseData != null) {
                erorcd = responseData.getHeaderData().getResponseCode();
            }
            erorcd = erorcd + "|" + e.getCode(); // 前面的为原始错误，后面的为冲正错误
            if (CommTools.isFlowTran()) {
                updateKnsRedu(false, cplRvIn.getStacps(), toStrikeRedu, erorcd);
            }
            throw e;
        } catch (Exception e) {
            String erorcd = "";
            if (responseData != null) {
                erorcd = responseData.getHeaderData().getResponseCode();
            }
            erorcd = erorcd + "|" + OnlineEngineConfigManager.get().getDefaultErrorCode();// 其他错误按默认错误码
            if (CommTools.isFlowTran()) {
                updateKnsRedu(false, cplRvIn.getStacps(), toStrikeRedu, erorcd);
            }
            throw e;
        }

        if (bizlog.isDebugEnabled())
            bizlog.debug("调用反向业务处理结束==========");

        // 冲账后检查:总笔数相等，所有扎差余额为零。
        /*
         * IoTaAccountBackIn cplBack =
         * SysUtil.getInstance(IoTaAccountBackIn.class);
         * 
         * cplBack.setYjiaoyls(sYjiaoyls); cplBack.setYjiaoyrq(sYjiaoyrq);
         * cplBack.setJiaoyils(sJiaoyils); cplBack.setJiaoyirq(sJiaoyirq);
         * 
         * SysUtil.getInstance(IoTaSrvStrike.class).prcTaStrikeCheck(cplBack);
         */

        String erorcd = null;
        if (responseData != null) {
            erorcd = responseData.getHeaderData().getResponseCode();
        }
        if (CommTools.isFlowTran()) {
            updateKnsRedu(true, cplRvIn.getStacps(), toStrikeRedu, erorcd);
        }
        if (bizlog.isDebugEnabled())
            bizlog.debug("冲正处理结束！");

        return ApUtil.Strike_backcode_SUCCESS;// 成功返回
    }

    private static void updateKnsRedu(boolean strikeSuccess, E_STACPS stacps, final KnsRedu redu, String erorcd) {
        if (stacps == E_STACPS.ACCOUT) {
            redu.setTxnsts(strikeSuccess ? E_TXNSTS.STRIKED : E_TXNSTS.UNSTRIK);
        } else {
            redu.setTxnsts(strikeSuccess ? E_TXNSTS.FAILURE : E_TXNSTS.UNKNOW);
            redu.setErrocd(erorcd);
        }
        if (strikeSuccess) { // 若冲正或冲账成功，则跟随当前事务；否则按独立事务
            KnsReduDao.updateOne_odb1(redu);
        } else {
            DaoUtil.executeInNewTransation(new RunnableWithReturn<Void>() {
                @Override
                public Void execute() {
                    KnsReduDao.updateOne_odb1(redu);
                    return null;
                }
            });
        }
    }
    
    

    // 检查是否允许冲正或冲账
    private static KnsRedu checkAndGetKnsRedu(IoApReverseIn cplRvIn) {
        KnsRedu redu = null;
        if (cplRvIn.getOtsqtp() == E_YES___.YES) { // 根据调用方上送流水冲正
            List<KnsRedu> list = KnsReduDao.selectAll_odb4(cplRvIn.getOtrasq(), cplRvIn.getOtradt(), false);
            for (KnsRedu r : list) {
                if (cplRvIn.getStacps() == E_STACPS.POSITIVE && E_TXNSTS.POSITIVE.in(r.getTxnsts())) {
                    redu = r;
                    break;
                } else if (cplRvIn.getStacps() == E_STACPS.ACCOUT && E_TXNSTS.ACCOUT.in(r.getTxnsts())) {
                    redu = r;
                    break;
                }
            }
            if (redu == null) {
                throw ApError.Sys.E9005(cplRvIn.getOtrasq());
            }
        } else { // 根据本系统流水号冲正，因为是唯一的，所以直接获得

            redu = KnsReduDao.selectOne_odb3(cplRvIn.getOtrasq(), cplRvIn.getOtradt(), false);

            if (redu == null) {
                throw ApError.Sys.E9005(cplRvIn.getOtrasq());
            }
            if (cplRvIn.getStacps() == E_STACPS.ACCOUT) {
                if (!E_TXNSTS.ACCOUT.in(redu.getTxnsts()))
                    throw ApError.Sys.E9005(cplRvIn.getOtrasq());
            } else {
                if (!E_TXNSTS.POSITIVE.in(redu.getTxnsts()))
                    throw ApError.Sys.E9005(cplRvIn.getOtrasq());
            }
        }
        BaseEnvUtil.setKnsRedu(true, redu); // 放入交易缓存区，以便更新时使用
        return redu;

    }

    private static String checkAllow(KnsTran tran) {

        // rviast 是否主动冲正 BaseEnumType.E_YES___
        // rvcdfg 当日抹账允许标志 BaseEnumType.E_YES___
        // rvodfg 隔日抹账允许标志 BaseEnumType.E_YES___
        // rvcdrs 当日冲正拒绝原因 BaseType.U_CHAR60
        // rvodrs 隔日冲正拒绝原因 BaseType.U_CHAR60
        // rvfxst 冲补账状态 BaseEnumType.E_RVFXST
        // rverdt 冲正日期 BaseType.U_DATE08
        // rverus 冲正柜员 BaseType.U_USERID
        // rverbr 冲正机构 BaseType.U_ORGNBR
        // rversq 冲正交易流水号 BaseType.U_SEQUNO
        // origdt 原错误日期 BaseType.U_DATE08
        // origsq 原错账流水号 BaseType.U_SEQUNO

        if (tran == null) {
            bizlog.error("原交易流水不存在，冲正失败！");
            return ApUtil.Strike_backcode_NOTEXIST;
        }
        switch (tran.getRvfxst()) {
        case REVERSED:
            bizlog.error("原交易流水已冲正,不能重复冲正！");
            return ApUtil.Strike_backcode_COMPLETED;
        case FIX:
            bizlog.error("原交易流水已补账,不能冲正！");
            throw ApError.Aplt.E0060("已补账");
        case REVERSE:
            bizlog.error("原交易流水为冲正交易,不能被冲正！");
            throw ApError.Aplt.E0060("原交易流水为冲正交易");
        case NONE:
        default:
            break;
        }
        return null;
    }

    // 默认值设置
    private static void setDefaltValue(IoApReverseIn cplRvIn, String trandt) {

        // 是否冲回凭证标志[冲正是，隔日否]:1-是(默认) 0-否
        if (cplRvIn.getVobkfg() == null)
            cplRvIn.setVobkfg(E_VOBKFG.HUIC);

        // 抹账标志，仅对当日冲正或者冲账有意义
        if (CommUtil.compare(cplRvIn.getOtradt(), trandt) == 0) {

            // 冲正只能为抹账（忽略掉外部上送），外部未提供则默认为抹账
            if (cplRvIn.getStacps() == E_STACPS.POSITIVE)
                cplRvIn.setCorrfg(E_CORRFG.MOZHANG);
            else if (cplRvIn.getCorrfg() == null)
                cplRvIn.setCorrfg(E_CORRFG.MOZHANG);
        } else {
            cplRvIn.setCorrfg(E_CORRFG.ZHENGCH);
        }

    }

    /**
     * 轮询业务事件，逐一进行冲正处理
     */
    private static void prcEvent(IoApReverseIn cplInput, E_YES___ ocalfg, String inpudt, String transq) {
        // 轮询业务事件，逐一进行冲正处理：
        List<KnbEvnt> lstEvnt = KnbEvntDao.selectAll_odb1(inpudt, transq, false);

        if (lstEvnt == null || lstEvnt.size() <= 0) {
            if (bizlog.isDebugEnabled())
                bizlog.debug("业务服务事件表无对应记录，交易流水：" + transq);
        }
        for (KnbEvnt evnt : lstEvnt) {
            if ((evnt.getCallst() == E_CALLST.F_TIMEOUT || evnt.getCallst() == E_CALLST.F_SUCCESS
                    || evnt.getCallst() == E_CALLST.S_TIMEOUT || evnt.getCallst() == E_CALLST.S_SUCCESS
                    || evnt.getCallst() == E_CALLST.STRIKE_TIMEOUT || evnt.getCallst() == E_CALLST.STRIKE_FAILED)
                    && (evnt.getEvntlv() == E_EVNTLV.NONE || evnt.getEvntlv() == E_EVNTLV.QUERY)) {
                if (bizlog.isInfoEnabled())
                    bizlog.info("事务类型不需要冲正:" + transq);
                continue;
            }
            if (bizlog.isDebugEnabled())
                bizlog.debug("业务服务事件表对应记录，交易流水：" + transq);
            prcSingleEvent(evnt, cplInput, ocalfg);
        }
    }

    /**
     * 单个事件反操作处理
     */
    private static void prcSingleEvent(KnbEvnt evnt, IoApReverseIn cplInput, E_YES___ ocalfg) {

        if (bizlog.isDebugEnabled())
            bizlog.debug("开始处理冲正业务，事件：" + evnt.getTranev());

        E_COLOUR colour = E_COLOUR.RED;

        // 取消红蓝字设定，全部用红字
        /*
         * if (CommUtil.compare(evnt.getTrandt().substring(0, 4),
         * sTrandt.substring(0, 4)) != 0) eHolzjzbz = E_REDBLU.LZ;
         */

        if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_OUTSVC) == 0) {
            doRemoteServiceStrike(evnt, "IoApTransactionTCC.rollback" + ApUtil.TRANS_EVENT_OUTSVC, ocalfg);
            return;
        }
        if (CommUtil.compare(evnt.getTranev(), ApUtil.TRANS_EVENT_OUTTXN) == 0) {
            doRemoteTransactionStrike(evnt, "IoApTransactionTCC.rollback" + ApUtil.TRANS_EVENT_OUTTXN, ocalfg);
            return;
        }

        try {//20170809 update
            String impl = ApStrikeEvntProcessor.implClazzPrefix + evnt.getTranev();
            ApStrikeEvntProcessor processor = (ApStrikeEvntProcessor) Class.forName(impl).newInstance();
            processor.process(cplInput.getStacps(), colour, evnt);
            evnt.setCallst(E_CALLST.STRIKE_SUCCESS);
            KnbEvntDao.updateOne_odb3(evnt);
        } catch (InstantiationException e) {
            throw Aplt.E0000("事件[" + evnt.getTranev() + "]冲正处理失败", e);
        } catch (IllegalAccessException e) {
            throw Aplt.E0000("事件[" + evnt.getTranev() + "]冲正处理失败", e);
        } catch (ClassNotFoundException e) {
            throw Aplt.E0000("事件[" + evnt.getTranev() + "]暂不支持冲正", e);
        } catch (Exception e) {
            throw e;
        }
    }

    private static void doRemoteServiceStrike(KnbEvnt evnt, String bindid, E_YES___ ocalfg) {
        TargetInfo info=new TargetInfo();    //20170810 新增
        if (bizlog.isDebugEnabled()) {
            bizlog.debug("外调冲正处理开始=====================,被冲正的外调流水号:" + evnt.getCallsq());
        }
        if (ocalfg == E_YES___.NO)
            throw Aplt.E0000("不允许再外调服务冲正！");

        // 事件关键字1=是否跨DCN 事件关键字2=服务ID 事件关键字3=子系统编号 事件关键字4=目标DCN 事件关键字5=目标法人代码
        // 跨节点调用冲正服务
        String rvercd = null;
        try {
            IoApTransactionTCC tcc = SysUtil.getInstanceProxyByBind(IoApTransactionTCC.class, bindid);
            InputSetter input = SysUtil.getInstance(InputSetter.class);
            input.setOrigdt(evnt.getTrandt());
            input.setOrigsq(evnt.getCallsq());//20170808 新增
            info.setDcnNo(evnt.getEvent4());//20170810 新增
            info.setCorpno(evnt.getEvent5());//20170810 新增
//            input.setTdcnno( CustomDRSUtil.mergeDcnAndCorpno(info));//20170810 新增
            input.setTdcnno(DcnUtil.getTargetRoute(info));//20170810 新增
            input.setOcalfg(E_YES___.NO); // 外调服务冲正时，不允许再外调冲正
            
            IoApTransactionTCC.rollbackOSVC.Output output = SysUtil
                    .getInstance(IoApTransactionTCC.rollbackOSVC.Output.class);
            tcc.rollbackOSVC(input, output);

            if (bizlog.isDebugEnabled())
                bizlog.debug("外调冲正处理结束=====================");

            rvercd = output.getRvrtcd();

        } catch (Exception e) {
            String erorcd = null;
            String erortx = null;
            if (e instanceof AdpBusinessException) {
                erorcd = ((AdpBusinessException) e).getCode();
                erortx = ((AdpBusinessException) e).getMessage();
            } else {
                erorcd = OnlineEngineConfigManager.get().getDefaultErrorCode();
                erortx = e.getMessage();
            }
            regKnbEvntStrk(evnt, ocalfg, erorcd, erortx);
            throw Aplt.E0000("再外调服务冲正失败,被冲正外调流水号[" + evnt.getCallsq() + "],e:", e);
        }

        if (!"00".equals(rvercd)) {
            regKnbEvntStrk(evnt, ocalfg, OnlineEngineConfigManager.get().getDefaultErrorCode(), "外调服务冲正失败，返回码：" + rvercd);
            throw Aplt.E0000("再外调服务冲正失败，被冲正外调流水号[" + evnt.getCallsq() + "]，返回：" + rvercd);
        } else {
            regKnbEvntStrk(evnt, ocalfg, OnlineEngineConfigManager.get().getSuccessCode(), "外调冲正服务成功");
            if (bizlog.isInfoEnabled())
                bizlog.info("外调服务冲正成功,被冲正外调流水号[" + evnt.getCallsq() + "]");
        }
    }

    private static void doRemoteTransactionStrike(KnbEvnt evnt, String bindid, E_YES___ ocalfg) {
        if (bizlog.isDebugEnabled()) {
            bizlog.debug("外调交易冲正处理开始=====================,被冲正的外调流水号:" + evnt.getCallsq());
        }

        if (ocalfg == E_YES___.NO)
            throw Aplt.E0000("不允许再外调交易冲正！");

        // 事件关键字1=是否跨DCN 事件关键字2=服务ID 事件关键字3=子系统编号 事件关键字4=目标DCN 事件关键字5=目标法人代码
        // 跨节点调用冲正交易
        String rvercd = null;
        try {
            IoApTransactionTCC tcc = SysUtil.getInstanceProxyByBind(IoApTransactionTCC.class, bindid);
            IoApTransactionTCC.rollbackOTXN.InputSetter input = SysUtil
                    .getInstance(IoApTransactionTCC.rollbackOTXN.InputSetter.class);
            input.setOrigdt(evnt.getTrandt());
            input.setOrigsq(evnt.getCallsq());
            //input.setTdcnno(evnt.getEvent4());
            input.setOcalfg(E_YES___.NO); // 外调交易冲正时，不允许再外调冲正

            IoApTransactionTCC.rollbackOTXN.Output output = SysUtil
                    .getInstance(IoApTransactionTCC.rollbackOTXN.Output.class);
            tcc.rollbackOTXN(input, output);

            if (bizlog.isDebugEnabled())
                bizlog.debug("外调交易冲正处理结束=====================");

            rvercd = output.getRvrtcd();
        } catch (Exception e) {
            String erorcd = null;
            String erortx = null;
            if (e instanceof AdpBusinessException) {
                erorcd = ((AdpBusinessException) e).getCode();
                erortx = ((AdpBusinessException) e).getMessage();
            } else {
                erorcd = OnlineEngineConfigManager.get().getDefaultErrorCode();
                erortx = e.getMessage();
            }
            regKnbEvntStrk(evnt, ocalfg, erorcd, erortx);
            throw Aplt.E0000("再外调交易冲正失败,被冲正外调流水号[" + evnt.getCallsq() + "],e:", e);
        }

        if (!"00".equals(rvercd)) {
            regKnbEvntStrk(evnt, ocalfg, OnlineEngineConfigManager.get().getDefaultErrorCode(), "外调交易冲正失败，返回码：" + rvercd);
            throw Aplt.E0000("再外调交易冲正失败，被冲正外调流水号[" + evnt.getCallsq() + "]，返回：" + rvercd);
        } else {
            regKnbEvntStrk(evnt, ocalfg, OnlineEngineConfigManager.get().getSuccessCode(), "外调冲正交易成功");
            if (bizlog.isInfoEnabled())
                bizlog.info("外调交易冲正成功,被冲正外调流水号[" + evnt.getCallsq() + "]");
        }
    }

    private static void regKnbEvntStrk(KnbEvnt evnt, E_YES___ ocalfg, String erorcd, String erortx) {
        RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
        KnbEvntStrk strk = SysUtil.getInstance(KnbEvntStrk.class);
        strk.setTrandt(runEnvs.getTrandt());
        strk.setTransq(runEnvs.getTransq());
        strk.setLogsno(CommTools.getCurrentThreadSeq("KnbEvntStrk_seq"));
        strk.setCallsq(evnt.getCallsq());
        strk.setEvntlv(evnt.getEvntlv());
        strk.setCorpno(evnt.getCorpno());
        strk.setOrigdt(evnt.getTrandt());
        strk.setOrigsq(evnt.getTransq());
        strk.setOrigno(evnt.getLogsno());
        strk.setTranev(evnt.getTranev());
        strk.setErorcd(erorcd);
        strk.setFailrs(erortx);
        KnbEvntStrkDao.insert(strk);
    }
}
