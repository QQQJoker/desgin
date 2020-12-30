package cn.sunline.ltts.fa.util;

import java.math.BigDecimal;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.tools.DateTools2;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApBatchResultOut;
import cn.sunline.ltts.busi.aplt.type.ComFaApFile.ApFileIn;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.Apb_batch_requestDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.Apb_batch_sendDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.App_batchDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.App_batch_channelDao;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.apb_batch_request;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.apb_batch_send;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.app_batch;
import cn.sunline.ltts.busi.fa.tables.TabFaFile.app_batch_channel;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_FILEDEALSTATUS;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_YESORNO;
import cn.sunline.ltts.sys.dict.GlDict;

/**
 * <p>
 * 文件功能说明： 文件批量相关
 * </p>
 * 
 * @Author yangdl
 *         <p>
 *         <li>2017年2月28日-下午5:33:53</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20160228 yangdl：文件批量相关</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */

public class FaApBatch {

    /**
     * @Author yangdl
     *         <p>
     *         <li>2017年2月28日-下午8:58:06</li>
     *         <li>功能说明：文件批量申请</li>
     *         </p>
     * @param reqInfo
     */
    public static void fileBatchApply(final ApFileIn reqInfo) {

        // 非空字段检查
        CommTools.fieldNotNull(reqInfo.getBusi_batch_code(), GlDict.A.busi_batch_code.getId(), GlDict.A.busi_batch_code.getLongName());// 文件批量号
        CommTools.fieldNotNull(reqInfo.getBusi_batch_id(), GlDict.A.busi_batch_id.getId(), GlDict.A.busi_batch_id.getLongName());// 文件批量业务id
        CommTools.fieldNotNull(reqInfo.getFile_server_path(), GlDict.A.file_server_path.getId(), GlDict.A.file_server_path.getLongName());// 文件服务器路径
        CommTools.fieldNotNull(reqInfo.getFile_name(), GlDict.A.field_name.getId(), GlDict.A.field_name.getLongName()); // 文件名称
        CommTools.fieldNotNull(reqInfo.getTiming_process_ind(), GlDict.A.timing_process_ind.getId(), GlDict.A.timing_process_ind.getLongName()); // 定时处理标志

        String processTime = reqInfo.getTiming_process_time();

        if (reqInfo.getTiming_process_ind() == E_YESORNO.YES) {
            CommTools.fieldNotNull(processTime, GlDict.A.timing_process_time.getId(), GlDict.A.timing_process_time.getLongName()); // 定时处理时间
            // 时间格式校验
            DateTools2.isDateString(processTime, "yyyyMMdd HH:mm:ss");
        }
        else {
            processTime = null;
        }

        // 获取公共运行变量
        RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

        // 文件批量业务ID检查(不存在则报错)
        app_batch appBatch = App_batchDao.selectOne_odb1(reqInfo.getBusi_batch_id(), false);

        if (CommUtil.isNull(appBatch)) {
            throw ApPubErr.APPUB.E0005(OdbFactory.getTable(app_batch.class).getLongname(), GlDict.A.busi_batch_id.getLongName(), reqInfo.getBusi_batch_id());
        }

        // 文件批量号唯一性检查
        apb_batch_request batchReq = Apb_batch_requestDao.selectOne_odb1(reqInfo.getBusi_batch_code(), false);

        if (CommUtil.isNotNull(batchReq)) {
            throw ApPubErr.APPUB.E0005(OdbFactory.getTable(apb_batch_request.class).getLongname(), GlDict.A.busi_batch_code.getLongName(), reqInfo.getBusi_batch_code());
        }

        // 业务渠道许可检查
        app_batch_channel batchChannel = App_batch_channelDao.selectOne_odb1(reqInfo.getBusi_batch_id(), runEnvs.getServtp().getValue()
                , false);

        if (CommUtil.isNull(batchChannel)) {
            throw ApPubErr.APPUB.E0005(OdbFactory.getTable(app_batch_channel.class).getLongname(), GlDict.A.busi_batch_id.getLongName(), reqInfo.getBusi_batch_id());
        }

        // 登记文件收取登记簿
        String fileId = FaApFileRecv.register(reqInfo.getFile_name(), reqInfo.getFile_server_path(), appBatch.getLocal_dir_code());

        batchReq = CommTools.getInstance(apb_batch_request.class);

        batchReq.setBusi_batch_code(reqInfo.getBusi_batch_code());// 文件批量号
        batchReq.setBusi_batch_id(reqInfo.getBusi_batch_id()); // 文件批量业务ID
        batchReq.setBusi_batch_type(appBatch.getBusi_batch_type()); // 文件批量类型
        batchReq.setRequest_file_id(fileId); // 请求文件ID
        batchReq.setFile_name(reqInfo.getFile_name()); // 文件名
        batchReq.setTiming_process_ind(reqInfo.getTiming_process_ind()); // 定时处理标志
        batchReq.setTiming_process_time(processTime); // 定时处理时间
        batchReq.setHash_value(CommTools.getGroupHashValue("REQUEST_HASH_VALUE", fileId)); // 散列值
        batchReq.setFile_handling_status(E_FILEDEALSTATUS.UNCHECK); // 待校验
        batchReq.setHead_total_count(0L); // 文件头记录数
        batchReq.setHead_total_amt(BigDecimal.ZERO); // 文件头总金额
        batchReq.setFilebody_total_count(0L); // 文件体记录数
        batchReq.setFilebody_total_amt(BigDecimal.ZERO); // 文件体总金额
        batchReq.setSuccess_total_count(0L); // 成功总笔数
        batchReq.setSuccess_total_amt(BigDecimal.ZERO); // 成功总金额
        batchReq.setRecdver(1l);
        // 登记文件批量请求登记薄
        Apb_batch_requestDao.insert(batchReq);

    }

    /**
     * @Author yangdl
     *         <p>
     *         <li>2017年2月28日-下午8:58:06</li>
     *         <li>功能说明: 文件导入成功, 更新状态</li>
     *         </p>
     * @param batchCode
     * @param count
     * @param amount
     */
    public static void setSuccessByImport(String batchCode, long count, BigDecimal amount) {

        apb_batch_request batchReq = Apb_batch_requestDao.selectOneWithLock_odb1(batchCode, true);

        batchReq.setFile_handling_status(E_FILEDEALSTATUS.CHECKED);

        batchReq.setHead_total_count(count);
        batchReq.setHead_total_amt(amount);
        batchReq.setFilebody_total_count(count);
        batchReq.setFilebody_total_amt(amount);

        Apb_batch_requestDao.updateOne_odb1(batchReq);

    }

    /**
     * @Author yangdl
     *         <p>
     *         <li>2017年2月28日-下午8:58:06</li>
     *         <li>功能说明: 文件格式有误, 更新状态</li>
     *         </p>
     * @param batchCode
     */
    public static void setFormatErrorByImport(String batchCode) {

        apb_batch_request batchReq = Apb_batch_requestDao.selectOneWithLock_odb1(batchCode, true);

        batchReq.setFile_handling_status(E_FILEDEALSTATUS.FAILCHECK_FORMAT);

        Apb_batch_requestDao.updateOne_odb1(batchReq);

    }

    /**
     * 
     * @Author yangdl
     *         <p>
     *         <li>2017年3月16日-下午3:37:38</li>
     *         <li>功能说明：文件明细导入异常，更新状态</li>
     *         </p>
     * @param batchCode
     */
    public static void setInsertErrorByImport(String batchCode, String e) {

        apb_batch_request batchReq = Apb_batch_requestDao.selectOneWithLock_odb1(batchCode, true);

        batchReq.setFile_handling_status(E_FILEDEALSTATUS.FAILCHECK_INSERT);
        int len = e.length();
        if (len > 1000)
            len = 1000;
        batchReq.setError_text(e.substring(0, len));
        Apb_batch_requestDao.updateOne_odb1(batchReq);

    }

    /**
     * @Author yangdl
     *         <p>
     *         <li>2017年2月28日-下午8:58:06</li>
     *         <li>功能说明: 明细导入后, 更新状态</li>
     *         </p>
     * @param batchCode
     * @param headCount
     * @param headAmount
     * @param bodyCount
     * @param bodyAmount
     */
    public static void setStatusByImport(String batchCode, long headCount, BigDecimal headAmount, long bodyCount, BigDecimal bodyAmount) {

        apb_batch_request batchReq = Apb_batch_requestDao.selectOneWithLock_odb1(batchCode, true);

        if (headCount == bodyCount && CommUtil.equals(headAmount, bodyAmount))
            batchReq.setFile_handling_status(E_FILEDEALSTATUS.CHECKED);
        else
            batchReq.setFile_handling_status(E_FILEDEALSTATUS.FAILCHECK_UNEQUAL);

        batchReq.setHead_total_count(headCount);
        batchReq.setHead_total_amt(headAmount);
        batchReq.setFilebody_total_count(bodyCount);
        batchReq.setFilebody_total_amt(bodyAmount);

        Apb_batch_requestDao.updateOne_odb1(batchReq);

    }

    /**
     * @Author yangdl
     *         <p>
     *         <li>2017年2月28日-下午8:58:06</li>
     *         <li>功能说明: 文件执行结束, 更新状态</li>
     *         </p>
     * @param batchCode
     * @param successCount
     * @param successAmount
     */
    public static void setStatusByExecute(String batchCode, long successCount, BigDecimal successAmount, String rtFileID) {

        apb_batch_request batchReq = Apb_batch_requestDao.selectOneWithLock_odb1(batchCode, true);

        batchReq.setFile_handling_status(E_FILEDEALSTATUS.SUCCESS);

        batchReq.setSuccess_total_count(successCount); // 成功总笔数
        batchReq.setSuccess_total_amt(successAmount); // 成功总金额
        batchReq.setReturn_file_id(rtFileID); //返回文件ID

        Apb_batch_requestDao.updateOne_odb1(batchReq);

    }

    /**
     * @Author yangdl
     *         <p>
     *         <li>2017年3月6日-下午4:03:20</li>
     *         <li>功能说明：文件批量处理结果查询</li>
     *         </p>
     * @param batchCode
     * @return ApBatchResultOut
     */
    public static ApBatchResultOut queryBatchResult(String batchCode) {

        // 非空字段检查
        CommTools.fieldNotNull(batchCode, GlDict.A.busi_batch_code.getId(), GlDict.A.busi_batch_code.getLongName());

        // 获取文件批量请求登记薄
        apb_batch_request batchReq = Apb_batch_requestDao.selectOne_odb1(batchCode, false);

        if (batchReq == null) {
            throw ApPubErr.APPUB.E0005(OdbFactory.getTable(apb_batch_request.class).getLongname(), GlDict.A.busi_batch_code.getLongName(), batchCode);
        }

        ApBatchResultOut cplOut = CommTools.getInstance(ApBatchResultOut.class);

        cplOut.setFile_handling_status(batchReq.getFile_handling_status()); // 文件处理状态

        if (batchReq.getFile_handling_status() == E_FILEDEALSTATUS.SUCCESS) {

            // 获取文件发送簿
            apb_batch_send batchSend = Apb_batch_sendDao.selectOne_odb1(batchReq.getRequest_file_id(), false);

            if (batchSend == null) {
                throw ApPubErr.APPUB.E0005(OdbFactory.getTable(apb_batch_send.class).getLongname(), GlDict.A.file_id.getLongName(), batchReq.getRequest_file_id());
            }

            if (batchSend.getSend_ind() == E_YESORNO.YES) {
                cplOut.setSuccess_total_count(batchReq.getSuccess_total_count()); // 成功总笔数
                cplOut.setSuccess_total_amt(batchReq.getSuccess_total_amt()); // 成功总金额
                cplOut.setFile_name(batchSend.getFile_name()); // 返回文件名称
                cplOut.setFile_server_path(batchSend.getFile_server_path()); // 返回文件路径
            }
        }

        return cplOut;
    }

    /**
     * @Author wengxt
     *         <p>
     *         <li>2017年2月28日-下午8:58:06</li>
     *         <li>功能说明: 更新批量表状态</li>
     *         </p>
     * @param batchCode
     * @param headCount
     * @param headAmount
     * @param bodyCount
     * @param bodyAmount
     */
    public static void setbatchReqStatus(String batchCode, E_FILEDEALSTATUS fileDealStatus) {

        apb_batch_request batchReq = Apb_batch_requestDao.selectOneWithLock_odb1(batchCode, true);

        batchReq.setFile_handling_status(fileDealStatus);

        Apb_batch_requestDao.updateOne_odb1(batchReq);

        return;
    }

    /**
     * @Author yangdl
     *         <p>
     *         <li>2017年2月28日-下午8:58:06</li>
     *         <li>功能说明: 明细导入后, 更新状态</li>
     *         </p>
     * @param batchCode
     * @param headCount
     * @param headAmount
     * @param bodyCount
     * @param bodyAmount
     * @return 校验成功返回true，否则返回false
     */
    public static boolean returnStatusByImport(String batchCode, long headCount, BigDecimal headAmount, long bodyCount, BigDecimal bodyAmount) {

        boolean flag = false;
        apb_batch_request batchReq = Apb_batch_requestDao.selectOneWithLock_odb1(batchCode, true);

        if (headCount == bodyCount && CommUtil.equals(headAmount, bodyAmount)) {
            batchReq.setFile_handling_status(E_FILEDEALSTATUS.CHECKED);
            flag = true;
        }
        else
            batchReq.setFile_handling_status(E_FILEDEALSTATUS.FAILCHECK_UNEQUAL);

        batchReq.setHead_total_count(headCount);
        batchReq.setHead_total_amt(headAmount);
        batchReq.setFilebody_total_count(bodyCount);
        batchReq.setFilebody_total_amt(bodyAmount);

        Apb_batch_requestDao.updateOne_odb1(batchReq);

        return flag;
    }
}
