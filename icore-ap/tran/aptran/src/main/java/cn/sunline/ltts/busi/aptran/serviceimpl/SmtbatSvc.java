package cn.sunline.ltts.busi.aptran.serviceimpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.sunline.adp.cedar.base.engine.BatchConfigConstant;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.type.KBaseEnumType.E_PILJYZHT;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.SystemParams;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.Tsp_taskDao;
import cn.sunline.adp.cedar.server.batch.tables.KSysBatchTable.tsp_task;
import cn.sunline.adp.cedar.server.batch.util.BatchUtil;
import cn.sunline.edsp.base.util.security.encrypt.MD5EncryptUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.aplt.namedsql.ApSysBatchDao;
import cn.sunline.ltts.busi.aplt.namedsql.ApltTabDao;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplsubDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_bussDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_confDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplsub;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_buss;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_conf;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.LogManager;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApBatchResultNumber;
import cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.smtbat.Input;
import cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.smtbat.Output;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_SYSCCD;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 提交批量文件服务实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "SmtbatSvc", longname = "提交批量文件服务实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class SmtbatSvc implements cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc {
    
	private static final BizLog BIZLOG = LogManager.getBizLog(SmtbatSvc.class);
	/**
     * 提交批量文件服务
     * 
     */
    @Override
    public void smtbat(Input input, Output output) {

        if (CommUtil.isNull(input.getSource())) {
            throw ApError.Sys.E0001("来源系统标识不能为空");
        }
        if (CommUtil.isNull(input.getBusseq())) {
            throw ApError.Sys.E0001("业务流水不能为空");
        }
        if (CommUtil.isNull(input.getAcctdt())) {
            throw ApError.Sys.E0001("业务日期不能为空");
        }
        if (CommUtil.isNull(input.getDataid())) {
            throw ApError.Sys.E0001("数据类型不能为空");
        }
        if (CommUtil.isNull(input.getTarget())) {
            throw ApError.Sys.E0001("目标系统标识不能为空");
        }

        String trandt = CommToolsAplt.prcRunEnvs().getTrandt();

        String busseq = input.getBusseq(); // 流水
        String acctdt = input.getAcctdt(); // 日期

        E_FILETP filetp = input.getDataid();
        knp_conf conf = Knp_confDao.selectOne_odb1(filetp, false);
        if (CommUtil.isNull(conf)) {
            throw ApError.Aplt.E0040();
        }
        KnpPara para = CommTools.KnpParaQryByCorpno("Batch.File", "%", "%", "%", true);
        // 2017-11-08 增加
        boolean issued = false;
        // 集中式
        if (SystemParams.get().isDistributedSystem()) {
            issued = conf.getIssued() == E_YES___.YES ? true : false; // 是否下发，即：做拆分的
        } else {
            issued = false;
        }
        boolean issubf = input.getIssubf() == E_YES___.YES ? true : false; // 是否子文件
        BIZLOG.info("文件状态:issued[%s],issubf[%s]",issued ,issubf);
//        issued = false;//临时
//        issubf = true;//临时
        // 重复提交检查
        if (!issued || issubf) { // 2017-11-08 增加：不是下发 或 为子文件
                                 // 可以进行重复检查，否则不可以（则在ADM上跑的拆分下发动作不可以做重复检查）
            if (!dubSmt(busseq)) {
                output.setBusseq(busseq);
                return;
            }
        }

        String source = input.getSource().getValue().toUpperCase();
        // 本地工作目录
        // String user_home =
        // CommTools.getInstance(NFSFileTransfer.class).workDirectory();
        // String user_home = System.getProperty("user.home");
        String user_home = para.getPmval2(); // 工作目录

        String sep = File.separator; // 路径分隔符

        String nfs_ph = para.getPmval1();
        String downna = ""; // 上游系统上送文件名
        String downph = ""; // 上游系统上送文件路径
        String upfena = ""; // 返回文件名称
        String filena = ""; // 文件名称

        String relaph = E_SYSCCD.NAS + sep + input.getSource() + sep + trandt; // 相对路径
        // String fileph = localDir + relaph + sep; //返回文件本地存放路径
        String upfeph = nfs_ph + relaph + sep; // 返回文件路径
        String locaph = user_home + relaph + sep;

        knp_buss buss = CommTools.getInstance(knp_buss.class);
        buss.setBusseq(busseq);
        buss.setFiletp(filetp);
        buss.setAcctdt(acctdt);
        buss.setCursys(E_SYSCCD.NAS);
        buss.setDescrp(filetp.getLongName());
        // buss.setFilels(JSON.parseObject(input.getFileList()));
        buss.setSendst(E_YES___.NO);
        buss.setSource(conf.getSource());
        buss.setTarget(conf.getTarget());
        buss.setTrandt(trandt);
        buss.setIssend(conf.getIssend());
        buss.setStatus("");
        buss.setSendnm(0);// 第一次发送默认为0
        Knp_bussDao.insert(buss);

        List<BatchFileSubmit> batch = input.getFileList();

        String filesq = null;
        if (CommUtil.isNull(batch) || batch.size() <= 0) { // 没有文件

            //modify by wuwei 2071201 增加dcn	为防止数字拆开，数字变9位，前三位加上dcn号，因为下面取12位		
            //String seqno = CommTools.getSequence("smtbatseq", 8);
            String seqno = CommTools.getSequence("smtbatseq", 9);
            seqno = CommTools.prcRunEnvs().getCdcnno() + seqno;


            if (CommUtil.isNotNull(input.getSubbno())) { // 子批次号由管理节点传过来的直接使用
                filesq = input.getSubbno();
            } else {
                filesq = trandt.concat(filetp.getValue()).concat(CommUtil.lpad(seqno, 12, "0"));
            }
            filena = source.concat("_").concat(filetp.getValue()).concat("_").concat(trandt).concat("_").concat(seqno)
                    .concat(".txt");

            upfena = filena;

            DataArea area = DataArea.buildWithEmpty();
            // 报文头参数
            area.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, trandt); // 批量交易日期
            area.getSystem().setString(BatchConfigConstant.TASK_ID, filesq); // 批量交易批次号
            area.getCommReq().setString("corpno", CommToolsAplt.prcRunEnvs().getCorpno());
            area.getSystem().setString("corpno", CommToolsAplt.prcRunEnvs().getCorpno());
            area.getCommReq().setString("tranbr", CommToolsAplt.prcRunEnvs().getTranbr());
            area.getSystem().setString("tranbr", CommToolsAplt.prcRunEnvs().getTranbr());
            area.getCommReq().setString("tranus", CommToolsAplt.prcRunEnvs().getTranus());
            area.getSystem().setString("tranus", CommToolsAplt.prcRunEnvs().getTranus());
            // 报文体参数
            area.getInput().setString(ApBatchFileParams.BATCH_PMS_FILESQ, filesq); // 文件批次号
            area.getInput().setString(ApBatchFileParams.BATCH_PMS_ACCTDT, acctdt); // 账务日期

            kapb_wjplxxb wjb = CommTools.getInstance(kapb_wjplxxb.class);
            wjb.setBusseq(busseq); // 业务流水
            wjb.setBtchno(filesq); // 文件批次号
            wjb.setAcctdt(acctdt); // 业务日期
            wjb.setTrandt(trandt); // 交易日期
            wjb.setDownna(downna); // 下载文件名
            wjb.setDownph(downph); // 下载文件路径
            wjb.setUpfena(upfena); // 返回文件名
            wjb.setUpfeph(upfeph); // 返回文件路径
            wjb.setFiletp(filetp); // 文件类型
            wjb.setLocaph(locaph); // 文件本地存放路径
            if (issued && !issubf) { // 2017-11-08
                                     // 如果是下发且不是子文件，则直接为“待下发”状态，没有文件处理是，由定时任务下发
                wjb.setBtfest(E_BTFEST.WAIT_DISTRIBUTE);
            } else {
                wjb.setBtfest(E_BTFEST.DING); // 文件处理状态
            }
            Kapb_wjplxxbDao.insert(wjb);

            // 提交批量组
            if (!issued || issubf) { // 2017-11-08 如果不是下发或子文件，按原来方式创建批量进行处理
                BatchUtil.submitAndRunBatchTranGroup(filesq, conf.getBachcd(), area, false);
            }

        } else {
            for (BatchFileSubmit bat : batch) {
                String params = bat.getParams();
                if (CommUtil.isNull(bat.getFilenm())) { // 没有文件名上送
                    String seqno = CommTools.getSequence("fileseq", 5);
                    if (!issued) {
                        filena = source.concat("_").concat(filetp.getValue()).concat("_").concat(trandt).concat("_")
                                .concat(seqno).concat(input.getTdcnno()).concat(".txt");
                    } else {
                        filena = source.concat("_").concat(filetp.getValue()).concat("_").concat(trandt).concat("_")
                                .concat(seqno).concat(".txt");
                    }
                    upfena = filena;
                } else {
                    filena = bat.getFilenm();
                    downna = filena;
                    upfena = filena.concat(".RET");
                }

                if (bat.getFlpath().startsWith(File.separator)) {
                    downph = nfs_ph + bat.getFlpath().substring(File.separator.length()); // 拼接下载路径
                } else {
                    downph = nfs_ph + bat.getFlpath();
                }

                DataArea area = DataArea.buildWithEmpty();
//                String filedt = "";
                try {
                    JSONObject obj = JSON.parseObject(params);
                    if (CommUtil.isNotNull(input.getSubbno())) { // 子批次号由管理节点传过来的直接使用
                        filesq = input.getSubbno();
                    } else {
                        filesq = obj.getString(ApBatchFileParams.BATCH_PMS_FILESQ);
                        if(CommUtil.isNull(filesq)) {
                        	String seqno = CommTools.getSequence("fileseq", 5);
                        	filesq = trandt.concat(filetp.getValue()).concat(CommUtil.lpad(seqno, 6, "0"));
                        }
                    }
//                    filedt = obj.getString(ApBatchFileParams.BATCH_PMS_TRANDT);

                    if (filetp == E_FILETP.CA010300) { // 电子账户当日开户信息查询
                        area.getInput().setString(ApBatchFileParams.BATCH_PMS_BRCHNO,
                                obj.getString(ApBatchFileParams.BATCH_PMS_BRCHNO));
                        area.getInput().setString(ApBatchFileParams.BATCH_PMS_MAXNUM,
                                obj.getString(ApBatchFileParams.BATCH_PMS_MAXNUM));
                        area.getInput().setString(ApBatchFileParams.BATCH_PMS_RELTID,
                                obj.getString(ApBatchFileParams.BATCH_PMS_RELTID));
                        area.getInput().setString(ApBatchFileParams.BATCH_PMS_OPENDT,
                                obj.getString(ApBatchFileParams.BATCH_PMS_OPENDT));

                    } else if (filetp == E_FILETP.FN050100) {// 成立清算
                        int length = downna.length();
                        String seqno = downna.substring(length - 10, length - 4);
                        // 返回文件名称
                        upfena = "NAS" + "_" + E_FILETP.FN050100 + "_" + acctdt + "_"
                                + CommTools.getSequence("fileseq", 5) + "_" + seqno + ".txt";

                        KnpPara tbl_knpPara = CommTools.KnpParaQryByCorpno("FNTRAN", "FILEPH", "%", "%", true);

                        if (CommUtil.isNull(tbl_knpPara)) {
                            throw ApError.Sys.E0001("理财清算回执文件路径未配置");
                        }

                        // 组装成立清算返回路径
                        upfeph = tbl_knpPara.getPmval1() + E_FILETP.FN050100 + File.separator + acctdt + File.separator;

                    } else if (filetp == E_FILETP.FN050200) {// 到期清算
                        int length = downna.length();
                        String seqno = downna.substring(length - 10, length - 4);
                        // 文件名称
                        upfena = "NAS" + "_" + E_FILETP.FN050200 + "_" + acctdt + "_"
                                + CommTools.getSequence("fileseq", 5) + "_" + seqno + ".txt";

                        KnpPara tbl_knpPara = CommTools.KnpParaQryByCorpno("FNTRAN", "FILEPH", "%", "%", true);

                        if (CommUtil.isNull(tbl_knpPara)) {
                            throw ApError.Sys.E0001("理财清算回执文件路径未配置");
                        }

                        // 组装到期清算返回路径
                        upfeph = tbl_knpPara.getPmval1() + E_FILETP.FN050200 + File.separator + acctdt + File.separator;

                    } else if (filetp == E_FILETP.DP959300) {// 支付平台通知核心
                        String seqno = CommTools.getSequence("fileseq", 5);
                        if (CommUtil.isNotNull(input.getTdcnno())) {
                            filena = source.concat("_").concat(filetp.getValue()).concat("_").concat(trandt).concat("_")
                                    .concat(seqno).concat("_").concat(input.getTdcnno()).concat(".txt");
                            upfena = filena;
                        }

                        String chekdt = obj.getString(ApBatchFileParams.BATCH_PMS_CHCKDT);
                        area.getSystem().setString("prcscd", "payckawt");
                        area.getInput().setString("chckdt", chekdt);
                    }

                } catch (Exception e) {
                    throw ApError.Aplt.E0041();
                }
                // 报文头参数
                area.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, trandt); // 批量交易日期
                area.getSystem().setString(BatchConfigConstant.TASK_ID, filesq); // 批量交易批次号
                area.getCommReq().setString(ApBatchFileParams.BATCH_PMS_CORPNO, CommToolsAplt.prcRunEnvs().getCorpno());
                area.getCommReq().setString(ApBatchFileParams.BATCH_PMS_TRANBR, CommToolsAplt.prcRunEnvs().getTranbr());
                area.getCommReq().setString(ApBatchFileParams.BATCH_PMS_TRANUS, CommToolsAplt.prcRunEnvs().getTranus());
                area.getCommReq().setString("farendma", CommToolsAplt.prcRunEnvs().getCorpno());
                // 报文体参数
                area.getInput().setString(ApBatchFileParams.BATCH_PMS_FILESQ, filesq); // 文件批次号
                area.getInput().setString(ApBatchFileParams.BATCH_PMS_ACCTDT, acctdt); // 账务日期
                // 文件多次重发校验 add by wuzx 20170301 -beg
                /*
                 * if(CommUtil.isNotNull(downna)&&CommUtil.isNotNull(downph)){
                 * kapb_wjplxxb wjpl = Kapb_wjplxxbDao.selectOne_odb3(downna,
                 * downph, false); if(CommUtil.isNotNull(wjpl)){ throw
                 * ApError.Sys.E0001("文件["+downna+"]已存在,无需再次通知！"); }
                 * //无法适应外围系统是定时任务不停调用的情况，暂时去掉这个校验，modified by xieqq }
                 */
                // 文件多次重发校验 add by wuzx 20170301 -end
                kapb_wjplxxb wjb = CommTools.getInstance(kapb_wjplxxb.class);
                wjb.setBusseq(busseq); // 业务流水
                wjb.setBtchno(filesq); // 文件批次号
                wjb.setAcctdt(acctdt); // 业务日期
                wjb.setTrandt(trandt); // 交易日期
                wjb.setDownna(downna); // 下载文件名
                wjb.setDownph(downph); // 下载文件路径
                wjb.setUpfena(upfena); // 返回文件名
                wjb.setUpfeph(downph+"upfeph"+File.separator); // 返回文件路径
                wjb.setFiletp(filetp); // 文件类型
                wjb.setLocaph(locaph); // 文件本地存放路径
                wjb.setFiletx(params);

                // 2017-11-08 如果是下发且不是子文件，则直接为“待拆分”状态，且创建拆分批量交易
                String bachcd = conf.getBachcd();
                String taskid = "";
                if (issued && !issubf) {
                    wjb.setBtfest(E_BTFEST.WAIT_SPLIT);
                    bachcd = conf.getSpltcd(); // 拆分批量交易码
                    taskid = filesq + "-split"; //拆分批量的taskid
                } else {
                    wjb.setBtfest(E_BTFEST.DING); // 文件处理状态
                    taskid = filesq;
                }
                Kapb_wjplxxbDao.insert(wjb);

                // 提交批量组
                BatchUtil.submitAndRunBatchTranGroup(taskid, bachcd, area, false);

                //2018-1-9，xieqq 与支付对账文件批量特殊处理，管理节点也产生对账文件
                if (issued && filetp == E_FILETP.DP959300) {
                    wjb.setBtfest(E_BTFEST.DING);
                    bachcd = conf.getBachcd(); //批量交易码
                    taskid = filesq + "-ADM"; //ADM批量的taskid
                    BatchUtil.submitAndRunBatchTranGroup(taskid, bachcd, area, false);
                }

            }
        }
        output.setBusseq(busseq);
        output.setFilesq(filesq);
    }

    /**
     * @Title: dubSmt
     * @Description: 重复提交检查并提交
     * @param busseq
     * @author zhangan
     * @date 2017年1月16日 上午11:24:17
     * @version V2.3.0
     */
    private static boolean dubSmt(String busseq) {
        int fail = 0;
        int succ = 0;
        int total = 0;

        knp_buss buss = Knp_bussDao.selectOne_odb1(busseq, false);
        if (CommUtil.isNull(buss)) {
            return true;
        }

        List<kapb_wjplxxb> fail_batch = new ArrayList<>();

        int cnt = ApltTabDao.selKapbWjplxxbCnt(busseq, true);

        List<kapb_wjplxxb> btwj = ApltTabDao.selKapbWjplxxbDetl(busseq, false);
        for (kapb_wjplxxb val : btwj) {
            if (val.getBtfest() == E_BTFEST.SUCC) {
                succ = succ + 1;
            } else if (val.getBtfest() == E_BTFEST.FAIL) {
                fail = fail + 1;
                fail_batch.add(val);
            }
        }

        total = fail + succ;

        if (total < cnt) {
            throw ApError.Aplt.E0051();
        }

        if (fail > 0) {

            for (kapb_wjplxxb val : fail_batch) {
                tsp_task plrenw = Tsp_taskDao.selectOne_odb_1(val.getBtchno(), true);

                plrenw.setTran_state(E_PILJYZHT.onprocess);
                Tsp_taskDao.updateOne_odb_1(plrenw);

                val.setBtfest(E_BTFEST.DING);
                Kapb_wjplxxbDao.updateOne_odb1(val);
            }

            buss.setSendst(E_YES___.NO);
            Knp_bussDao.updateOne_odb1(buss);
        } else {
            buss.setSendst(E_YES___.NO);
            Knp_bussDao.updateOne_odb1(buss);
        }

        return false;
    }

    @Override
    public void smtbatReturn(cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.smtbatReturn.Input input,
            cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.smtbatReturn.Output output) {
    	String subbno = input.getWjplsub().getBtchno();//零售节点的批次号，即主节点子批次号
    	kapb_wjplsub wjplsub = Kapb_wjplsubDao.selectOne_odb1(subbno, false);
    	if (CommUtil.isNull(wjplsub)) {
    		throw ApError.Sys.E0001("未找到批量文件子表记录，subbno=" + subbno);
    	}
    	String btchno = wjplsub.getBtchno();
    	kapb_wjplxxb wjplxxb = Kapb_wjplxxbDao.selectOneWithLock_odb1(btchno, true);
    	long total = wjplxxb.getTotanm().longValue();
        /*if (wjplsub.getBtfest() != E_BTFEST.WAIT_SUB) {
            throw ApError.Sys.E0001("文件批量子表状态不为[" + E_BTFEST.WAIT_SUB.getValue() + "]-["
                    + E_BTFEST.WAIT_SUB.getLongName() + "]，无法接受子文件结果！");
        }*/
//        System.out.println("1待合并数与已完成数:");
//        BIZLOG.info("2待合并数与已完成数:");
        wjplsub.setBusseq(input.getWjplsub().getBusseq()); // 业务流水号
        wjplsub.setTrandt(input.getWjplsub().getTrandt()); // 交易日期
        wjplsub.setAcctdt(input.getWjplsub().getAcctdt()); // 业务日期
        wjplsub.setFiletp(input.getWjplsub().getFiletp()); // 文件类型
        wjplsub.setTotanm(input.getWjplsub().getTotanm()); // 总笔数
        wjplsub.setDistnm(input.getWjplsub().getDistnm()); // 处理总笔数
        wjplsub.setSuccnm(input.getWjplsub().getSuccnm()); // 成功笔数
        wjplsub.setFailnm(input.getWjplsub().getFailnm()); // 失败笔数
        wjplsub.setBtfest(input.getWjplsub().getBtfest()); // 批量文件状态
        wjplsub.setFiletx(input.getWjplsub().getFiletx()); // 文件信息
        wjplsub.setErrotx(input.getWjplsub().getErrotx()); // 错误信息
        wjplsub.setDownph(input.getWjplsub().getDownph()); // 下载路径
        wjplsub.setDownna(input.getWjplsub().getDownna()); // 下载文件名
        wjplsub.setUpfeph(input.getWjplsub().getUpfeph()); // 返回文件路径
        wjplsub.setUpfena(input.getWjplsub().getUpfena()); // 返回文件名
        wjplsub.setLocaph(input.getWjplsub().getLocaph()); // 文件本地路径
        wjplsub.setBtfest(E_BTFEST.WAIT_MERGE);
        wjplsub.setErrotx(E_BTFEST.WAIT_MERGE.getLongName());
        Kapb_wjplsubDao.updateOne_odb1(wjplsub);

        
//        int total = SmtbatSqlsDao.selWjplsubCountByBtchno(btchno, null, false);
        ApBatchResultNumber apBatchResultNumber = ApSysBatchDao.countBatchResultByKapbWjplsub(E_BTFEST.WAIT_MERGE, btchno, false);
        long succ = 0;
        long fail = 0;
        if(CommUtil.isNotNull(apBatchResultNumber)){
        	succ = apBatchResultNumber.getSuccnm().longValue();
        	fail = apBatchResultNumber.getFailnm().longValue();
        }
//        		SmtbatSqlsDao.selWjplsubCountByBtchno(btchno, , false);
//        System.out.println("3待合并数与已完成数:"+total+":"+succ);
//        BIZLOG.info("4待合并数与已完成数:"+total+":"+succ);
        // 成功笔数+失败笔数 等于总数，说明全部OK
        if (total == succ+fail) {
        	wjplxxb.setBtfest(E_BTFEST.WAIT_MERGE); // 全部成功，将状态改为“待合并”
        	wjplxxb.setErrotx(E_BTFEST.WAIT_MERGE.getLongName());
            Kapb_wjplxxbDao.updateOne_odb1(wjplxxb);

            // 创建合并的批量
            String trandt = CommToolsAplt.prcRunEnvs().getTrandt();
            String filesq = btchno;
            knp_conf conf = Knp_confDao.selectOne_odb1(wjplxxb.getFiletp(), true);
            DataArea area = DataArea.buildWithEmpty();
            // 报文头参数
            area.getCommReq().setString(BatchConfigConstant.BATCH_TRAN_DATE, trandt); // 批量交易日期
            area.getSystem().setString(BatchConfigConstant.TASK_ID, filesq); // 批量交易批次号
            area.getCommReq().setString("corpno", CommToolsAplt.prcRunEnvs().getCorpno());
            area.getSystem().setString("corpno", CommToolsAplt.prcRunEnvs().getCorpno());
            area.getCommReq().setString("tranbr", CommToolsAplt.prcRunEnvs().getTranbr());
            area.getSystem().setString("tranbr", CommToolsAplt.prcRunEnvs().getTranbr());
            area.getCommReq().setString("tranus", CommToolsAplt.prcRunEnvs().getTranus());
            area.getSystem().setString("tranus", CommToolsAplt.prcRunEnvs().getTranus());
            area.getCommReq().setString("farendma", CommToolsAplt.prcRunEnvs().getCorpno());
            // 报文体参数
            area.getInput().setString(ApBatchFileParams.BATCH_PMS_FILESQ, filesq); // 文件批次号
            area.getInput().setString(ApBatchFileParams.BATCH_PMS_ACCTDT, wjplxxb.getAcctdt()); // 账务日期
            BatchUtil.submitAndRunBatchTranGroup(filesq + "-merge", conf.getMergcd(), area, false); //合并批量taskid按总批次号+"-merge"
        }
    }

    @Override
    // 具体的拆分批量交易中，每个DCN文件生成并上传NAS后，调用此拆分创建子表，注意事务
    // 注意：要提供下载路径、下载文件名等
    public void split(cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.split.Input input,
            cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.split.Output output) {

        kapb_wjplsub wjplsub = SysUtil.getInstance(kapb_wjplsub.class);
        CommUtil.copyProperties(wjplsub, input.getWjplxxb());
        wjplsub.setDownm5(MD5EncryptUtil.getMD5String(wjplsub.getDownna())); //生成文件名MD5 TODO 是否仅文件名？方法是否这个？
        wjplsub.setSubbno(input.getWjplxxb().getBtchno() + "-" + input.getTdcnno()); // 固定子批次号
        wjplsub.setBtfest(E_BTFEST.WAIT_SUB);
        wjplsub.setTdcnno(input.getTdcnno()); //目标DCN号
        Kapb_wjplsubDao.insert(wjplsub);

    }

    /**
     * 此方法用于管理节点直接下发到零售节点处理，零售节点处理完成直接通知外围系统。
     * 在成功通知外围系统后跟新管理节点子任务状态
     */
    public void returnWithoutMerge(final cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.returnWithoutMerge.Input input,
            final cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.returnWithoutMerge.Output output) {
        String subbno = input.getWjplsub().getBtchno();
        String btbno = input.getWjplsub().getBusseq();

        kapb_wjplsub wjplsub = Kapb_wjplsubDao.selectOne_odb1(subbno, false);
        kapb_wjplxxb wjplxxb = Kapb_wjplxxbDao.selectOne_odb1(btbno, false);

        if (wjplxxb == null) {
            throw ApError.Sys.E0001("未找到管理节点主文件任务记录，btbno=" + btbno);
        }

        if (wjplsub == null) {
            throw ApError.Sys.E0001("未找到管理节点下发文件子任务记录，subbno=" + subbno);
        }

        wjplsub.setSubbno(subbno);
        wjplsub.setBtchno(input.getWjplsub().getBusseq());
        wjplsub.setBusseq(input.getWjplsub().getBusseq()); // 业务流水号
        wjplsub.setTrandt(input.getWjplsub().getTrandt()); // 交易日期
        wjplsub.setAcctdt(input.getWjplsub().getAcctdt()); // 业务日期
        wjplsub.setFiletp(input.getWjplsub().getFiletp()); // 文件类型
        wjplsub.setTotanm(input.getWjplsub().getTotanm()); // 总笔数
        wjplsub.setDistnm(input.getWjplsub().getDistnm()); // 处理总笔数
        wjplsub.setSuccnm(input.getWjplsub().getSuccnm()); // 成功笔数
        wjplsub.setFailnm(input.getWjplsub().getFailnm()); // 失败笔数
        wjplsub.setBtfest(input.getWjplsub().getBtfest()); // 批量文件状态
        wjplsub.setFiletx(input.getWjplsub().getFiletx()); // 文件信息
        wjplsub.setErrotx(input.getWjplsub().getErrotx()); // 错误信息
        wjplsub.setDownph(input.getWjplsub().getDownph()); // 下载路径
        wjplsub.setDownna(input.getWjplsub().getDownna()); // 下载文件名
        wjplsub.setUpfeph(input.getWjplsub().getUpfeph()); // 返回文件路径
        wjplsub.setUpfena(input.getWjplsub().getUpfena()); // 返回文件名
        wjplsub.setLocaph(input.getWjplsub().getLocaph()); // 文件本地路径
        wjplsub.setBtfest(input.getWjplsub().getBtfest()); //文件状态

        Kapb_wjplsubDao.updateOne_odb1(wjplsub);

        wjplxxb.setBtfest(E_BTFEST.SUCC); // 不需要合并，由零售节点直接通知外围系统的主文件任务，下发成功即处理成功

        Kapb_wjplxxbDao.updateOne_odb1(wjplxxb);

    }

	@Override
	public void batTranSmt(cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.batTranSmt.Input input,
			cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.batTranSmt.Output output) {
		// TODO Auto-generated method stub
		
	}
    
}
