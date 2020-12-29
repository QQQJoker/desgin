package cn.sunline.ltts.busi.aptran.timer;

import java.util.List;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.Kapb_wjplxxbDao;
import cn.sunline.ltts.busi.aplt.tables.online.SysFileTables.kapb_wjplxxb;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.aplt.type.ApDefineType.ApWjplxxb;
import cn.sunline.ltts.busi.aptran.namedsql.SmtbatSqlsDao;
import cn.sunline.ltts.busi.aptran.serviceimpl.SmtbatSvc;
import cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.smtbatReturn.InputSetter;
import cn.sunline.ltts.busi.aptran.servicetype.SmtbatSvc.smtbatReturn.Output;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_BTFEST;

/**
 * 功能：收集返回下发处理的结果
 */
public class SmtbatCollectTimerProcessor extends LttsTimerProcessor {

	private static final BizLog bizlog = BizLogUtil
			.getBizLog(SmtbatCollectTimerProcessor.class);

	@Override
	/**
	 * timerName为定时调度配置的名称
	 * paramDataArea为数据区
	 */
	public void process(String timerName, DataArea paramDataArea) {
		bizlog.info("<<==========子节点文件上报开始 【SmtbatCollectTimerProcessor】========>>");
		// 查询kapb_wjplxxb状态为“待上报” WAIT_REPORT
		List<kapb_wjplxxb> xxbList = SmtbatSqlsDao.selWjplxxbByBtfest(
				E_BTFEST.WAIT_REPORT, false);

		for (kapb_wjplxxb xxb : xxbList) {
			try {
				// 远程调用服务SmtbatSvc.smtbatReturn 一定是向管理节点上报
				SmtbatSvc svc = SysUtil.getRemoteInstance(SmtbatSvc.class);
				InputSetter input = SysUtil.getInstance(InputSetter.class);
				Output output = SysUtil.getInstance(Output.class);
				ApWjplxxb toSend = SysUtil.getInstance(ApWjplxxb.class);

				CommToolsAplt.prcRunEnvs().setBusisq(xxb.getBusseq());

				toSend.setBtchno(xxb.getBtchno()); // 批量交易批次号:此批量号后缀是'-DCNNO'
				toSend.setBusseq(xxb.getBusseq()); // 业务流水号
				toSend.setTrandt(xxb.getTrandt()); // 交易日期
				toSend.setAcctdt(xxb.getAcctdt()); // 业务日期
				toSend.setEndtdt(xxb.getEndtdt()); // 业务结束日期
				toSend.setFiletp(xxb.getFiletp()); // 文件类型
				toSend.setTotanm(xxb.getTotanm()); // 总笔数
				toSend.setDistnm(xxb.getDistnm()); // 处理总笔数
				toSend.setSuccnm(xxb.getSuccnm()); // 成功笔数
				toSend.setFailnm(xxb.getFailnm()); // 失败笔数
				toSend.setFiletx(xxb.getFiletx()); // 文件信息
				toSend.setErrotx(xxb.getErrotx()); // 错误信息
				toSend.setDownph(xxb.getDownph()); // 下载路径
				toSend.setDownna(xxb.getDownna()); // 下载文件名
				toSend.setUpfeph(xxb.getUpfeph()); // 返回文件路径
				toSend.setUpfena(xxb.getUpfena()); // 返回文件名
				toSend.setLocaph(xxb.getLocaph()); // 文件本地路径
				toSend.setBtfest(xxb.getBtfest()); // 批量文件状
				input.setWjplsub(toSend);
				bizlog.info(">> input={}", input);
				svc.smtbatReturn(input, output);
				bizlog.info(">> output={}", input);

				DaoUtil.beginTransaction();
				// 修改kapb_wjplxxb 状态为“交易处理成功”
				xxb.setBtfest(E_BTFEST.SUCC);
				Kapb_wjplxxbDao.updateOne_odb1(xxb);
				DaoUtil.commitTransaction();

			} catch (Exception e) {
				bizlog.error("定时任进行批量文件上报时出错！btchno[%s].错误信息[%s]",
						xxb.getBtchno(), e.getMessage());
				DaoUtil.rollbackTransaction();
			}
		}
		
		bizlog.info("<<==========子节点文件上报结束 【SmtbatCollectTimerProcessor】========>>");

	}

}
