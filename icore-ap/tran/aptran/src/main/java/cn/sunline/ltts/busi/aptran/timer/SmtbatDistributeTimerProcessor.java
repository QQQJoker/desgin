package cn.sunline.ltts.busi.aptran.timer;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.ap.iobus.type.ap.fbat.IoApFileBatchType.BatchFileSubmit;
import cn.sunline.ltts.busi.aplt.para.ApBatchFileParams;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplsubDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Knp_bussDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplsub;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.knp_buss;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aptran.namedsql.SmtbatSqlsDao;
import cn.sunline.ltts.busi.aptran.serviceimpl.SmtbatSvc;
import cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.smtbat.InputSetter;
import cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.smtbat.Output;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_FILETP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * 功能：下发文件批量
 */
public class SmtbatDistributeTimerProcessor extends LttsTimerProcessor {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(SmtbatDistributeTimerProcessor.class);

	@Override
	/**
	 * timerName为定时调度配置的名称
	 * paramDataArea为数据区
	 */
	public void process(String timerName, DataArea paramDataArea) {
		
		bizlog.info("<< ============文件批量下发开始 【SmtbatDistributeTimerProcessor】===========>>");

		List<kapb_wjplsub> subList = SmtbatSqlsDao.selWjplsubByBtfest(
				E_BTFEST.WAIT_DISTRIBUTE, false);
		bizlog.info(">> 文件子表data={}", subList);
		// 若无数据等待N秒
		KnpPara para = CommTools.KnpParaQryByCorpno("Batch.File", "%", "%", "%",true);

		for (kapb_wjplsub sub : subList) {
			try {
//				knp_conf conf = Knp_confDao.selectOne_odb1(sub.getFiletp(),
//						true);
				kapb_wjplxxb wjxx = Kapb_wjplxxbDao.selectOne_odb1(sub.getBtchno(), true);
				knp_buss buss = Knp_bussDao.selectOne_odb1(sub.getBusseq(), true);
				// 远程调用服务SmtbatSvc.smtbat
				SmtbatSvc svc = SysUtil.getInstanceProxyByBind(SmtbatSvc.class, "LS");
				InputSetter input = SysUtil.getInstance(InputSetter.class);
				Output output = SysUtil.getInstance(Output.class);
                if(buss.getFiletp()== E_FILETP.DP021200){
                	input.setSource(buss.getSource());
                	input.setTarget(buss.getTarget()); // 目标系统标识
                }else{
                	input.setSource(buss.getCursys()); // 来源系统标识 分发：管理节点到零售节点 NAS-->NAS
                	input.setTarget(buss.getCursys()); // 目标系统标识
                }
				input.setAcctdt(sub.getAcctdt()); // 业务日期
				input.setEndtdt(sub.getEndtdt()); // 业务结束日期
	
				String filetx = wjxx.getFiletx();
				JSONObject obj = JSON.parseObject(filetx);
//				input.setLsleix(obj.getString(ApBatchFileParams.BATCH_PMS_LSLEIX));//TODO
				input.setBrchno(obj.getString(ApBatchFileParams.BATCH_PMS_BRCHNO));
				input.setCrcycn(CommTools.getDefineCurrency());			
				input.setDataid(sub.getFiletp()); // 数据类型
				input.setBusseq(sub.getBusseq()); // 业务流水
				input.setTdcnno(sub.getTdcnno()); // 分发到目标DCN
				input.setSubbno(sub.getSubbno()); // 设置子批次号
				CommToolsAplt.prcRunEnvs().setBusisq(sub.getBusseq()); // 设置业务流水
				// modify by luoxiaoyu on 20171127 判断是否分发文件，如果分发添加文件信息
				KnpPara knpPara = CommTools.KnpParaQryByCorpno("BatchParm.Filetp", sub.getFiletp().toString(), "%", "%", false);
				boolean flag = true;
				if (CommUtil.isNotNull(knpPara)) {
					if (CommUtil.compare(knpPara.getPmval1(), E_YES___.NO.getValue()) == 0) {
						flag = false;
					}
				}
				if (flag) {
					BatchFileSubmit submit = SysUtil
							.getInstance(BatchFileSubmit.class);
					submit.setFilenm(sub.getDownna()); // 文件名
					submit.setFilemd(sub.getDownm5()); // 文件md5
					submit.setFlpath(sub.getDownph().substring(para.getPmval1().length() - 1)); // 文件路径
					//submit.setFlpath(sub.getDownph()); // 文件路径
					submit.setParams(wjxx.getFiletx()); // 附加参数
					input.getFileList().add(submit);
				}

				bizlog.info(">> 调起子节点服务开始，input={}", input);

				svc.smtbat(input, output);
				bizlog.info(">> 调起子节点服务结束，output={}", output);
				if (output.getBusseq() == null) {
					throw ApError.Sys.E0001("远程调用smtbat服务返回的busseq为空！");
				}

				DaoUtil.beginTransaction();
				// 修改kapb_wjplsub 状态为“等待结果”
				sub.setBtfest(E_BTFEST.WAIT_SUB);
				Kapb_wjplsubDao.updateOne_odb1(sub);
				DaoUtil.commitTransaction();
			} catch (Exception e) {
				bizlog.error("定时任进行批量文件下发时出错！btchno[%s],DCNNO[%s].错误信息[%s]",
						sub.getBtchno(), sub.getTdcnno(), e.getMessage());
				bizlog.error("错误堆栈：", e);
				DaoUtil.rollbackTransaction();
			}
			
			bizlog.info("<< ============文件批量下发结束 【SmtbatDistributeTimerProcessor】===========>>");
		}

	}

}
