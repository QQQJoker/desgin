
package cn.sunline.edsp.busi.aptran.batchtran;

import cn.sunline.adp.cedar.server.batch.engine.split.BatchDataWalker;
import cn.sunline.adp.cedar.server.batch.engine.split.impl.CursorBatchDataWalker;
import cn.sunline.edsp.base.lang.Params;
import cn.sunline.ltts.amsg.namedsql.ApMsgNsqlDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessHdMQ;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessHdMQDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.type.SPEnumType.E_PROCSTATUS;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.server.batch.engine.split.AbstractBatchDataProcessor;

/**
 * 定时处理发送MQ
 * 
 * @author
 * @Date
 */

public class handmqDataProcessor extends
		AbstractBatchDataProcessor<cn.sunline.edsp.busi.aptran.batchtran.intf.Handmq.Input, cn.sunline.edsp.busi.aptran.batchtran.intf.Handmq.Property, cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessHdMQ> {
	private static final BizLog bizlog = BizLogUtil.getBizLog(handmqDataProcessor.class);

	/**
	 * 批次数据项处理逻辑。
	 * 
	 * @param job
	 *            批次作业ID
	 * @param index
	 *            批次作业第几笔数据(从1开始)
	 * @param dataItem
	 *            批次数据项
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 */
	@Override
	public void process(String jobId, int index, cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessHdMQ dataItem,
			cn.sunline.edsp.busi.aptran.batchtran.intf.Handmq.Input input,
			cn.sunline.edsp.busi.aptran.batchtran.intf.Handmq.Property property) {

		/**
		 * 1. 逐笔处理发送 2. 更新批次号和状态更改为待处理 3. 发送成功状态改为成功 4. 发送失败抛出异常 状态失败 重试次数+1
		 */

		// 更新批次号和状态
		ApsMessHdMQ tblMessHdMQ = SysUtil.getInstance(ApsMessHdMQ.class);
		CommUtil.copyProperties(tblMessHdMQ, dataItem);
		tblMessHdMQ.setTaskid(jobId);
		tblMessHdMQ.setHandst(E_PROCSTATUS.P);
		ApsMessHdMQDao.updateOne_odb1(tblMessHdMQ);

		try {

			ApsMessHdMQ sendMsgFMQ = ApsMessHdMQDao.selectOne_odb2(dataItem.getTransq(), jobId, false);
			// FMQ推送消息
			CommTools.pushMessage(sendMsgFMQ.getTopcid(), sendMsgFMQ.getMesstp(), true);
			sendMsgFMQ.setHandst(E_PROCSTATUS.S);
			ApsMessHdMQDao.updateOne_odb1(sendMsgFMQ);

		} catch (Exception e) {

			tblMessHdMQ.setTragti(tblMessHdMQ.getTragti() + 1);
			tblMessHdMQ.setTaskid(jobId);
			tblMessHdMQ.setHandst(E_PROCSTATUS.F);
			ApsMessHdMQDao.updateOne_odb1(tblMessHdMQ);
			bizlog.error("FMQ消息推送失败", e);

		}
	}

	/**
	 * 获取数据遍历器。
	 * 
	 * @param input
	 *            批量交易输入接口
	 * @param property
	 *            批量交易属性接口
	 * @return 数据遍历器
	 */
	@Override
	public BatchDataWalker<cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessHdMQ> getBatchDataWalker(
			cn.sunline.edsp.busi.aptran.batchtran.intf.Handmq.Input input,
			cn.sunline.edsp.busi.aptran.batchtran.intf.Handmq.Property property) {

		Params params = new Params();
		params.put("corpno", CommTools.prcRunEnvs().getCorpno());
		params.put("handst", E_PROCSTATUS.W);
		params.put("hands1", E_PROCSTATUS.F);

		return new CursorBatchDataWalker<ApsMessHdMQ>(ApMsgNsqlDao.namedsql_selApsMessHdMQToProcess, params);
	}

}
