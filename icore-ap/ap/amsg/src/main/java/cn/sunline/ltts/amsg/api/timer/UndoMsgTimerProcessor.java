package cn.sunline.ltts.amsg.api.timer;

import java.util.List;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessData;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessDataDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessFail;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessFailDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessSucc;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessSuccDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessUndo;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessUndoDao;
import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;

/**
 * 定时任务用于处理批量消息
 * 
 * @author xieqq
 * 
 */
public class UndoMsgTimerProcessor extends LttsTimerProcessor {

	private static final SysLog log = SysLogUtil.getSysLog(UndoMsgTimerProcessor.class);

	@Override
	public void process(String str, DataArea data) {
		long start = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("AMSG process batch message start");
		}
		// 1.批量查询未处理消息
		ApsMessUndo tbaps_mess_undo = CommTools.getInstance(ApsMessUndo.class);
		List<ApsMessUndo> listUndo = null;// TODO 批量获取未处理数据

		for (ApsMessUndo undoMsg : listUndo) {
			// 2.获取消息内容
			ApsMessData tbAps_mess_data = ApsMessDataDao.selectOne_odb1(undoMsg.getBusisq(), undoMsg.getMessid(), true);

			try {
				// 3.调用消息发布接口
				// TODO AsyncMessageUtil.add(tbAps_mess_data.getTopcid(),
				// tbAps_mess_data.getMesobj(), tbAps_mess_data.getMedata(),
				// SUCCESS_FLAG.SUCCESS);
				AsyncMessageUtil.publishOrSave(E_MSGOPT.SUCESS);

				// 4.增加成功处理消息记录
				ApsMessSucc tbaps_mess_succ = CommTools.getInstance(ApsMessSucc.class);
				tbaps_mess_succ.setBusisq(undoMsg.getBusisq());
				tbaps_mess_succ.setDestdt(undoMsg.getDestdt());
				tbaps_mess_succ.setDestip(undoMsg.getDestip());
				tbaps_mess_succ.setDevmid(undoMsg.getDevmid());
				tbaps_mess_succ.setMesseq(undoMsg.getMesseq());
				tbaps_mess_succ.setMessid(undoMsg.getMessid());
				tbaps_mess_succ.setPrvmid(undoMsg.getPrvmid());
				tbaps_mess_succ.setSdcnid(undoMsg.getSdcnid());
				tbaps_mess_succ.setSosyco(undoMsg.getSosyco());
				tbaps_mess_succ.setSourip(undoMsg.getSourip());
				tbaps_mess_succ.setSovmid(undoMsg.getSovmid());
				tbaps_mess_succ.setTopcid(undoMsg.getTopcid());
				tbaps_mess_succ.setTragti(0); // 重试次数
				tbaps_mess_succ.setTrandt(undoMsg.getTrandt());
				tbaps_mess_succ.setTransq(undoMsg.getTransq());

				ApsMessSuccDao.insert(tbaps_mess_succ);// 插入成功消息表
			} catch (Exception e) {
				// 5.若异常，增加异常消息记录
				ApsMessFail tbaps_mess_fail = CommTools.getInstance(ApsMessFail.class);
				tbaps_mess_fail.setBusisq(undoMsg.getBusisq());
				tbaps_mess_fail.setDestdt(undoMsg.getDestdt());
				tbaps_mess_fail.setDestip(undoMsg.getDestip());
				tbaps_mess_fail.setDevmid(undoMsg.getDevmid());
				tbaps_mess_fail.setEstack(e.getStackTrace().toString()); // 异常堆栈
				tbaps_mess_fail.setExcode(""); // 异常错误码 TODO
				tbaps_mess_fail.setMesseq(undoMsg.getMesseq());
				tbaps_mess_fail.setMessid(undoMsg.getMessid());
				tbaps_mess_fail.setPrvmid(undoMsg.getPrvmid());
				tbaps_mess_fail.setSdcnid(undoMsg.getSdcnid());
				tbaps_mess_fail.setSosyco(undoMsg.getSosyco());
				tbaps_mess_fail.setSourip(undoMsg.getSourip());
				tbaps_mess_fail.setSovmid(undoMsg.getSovmid());
				tbaps_mess_fail.setTopcid(undoMsg.getTopcid());
				tbaps_mess_fail.setTragti(0); // 重试次数
				tbaps_mess_fail.setTrandt(undoMsg.getTrandt());
				tbaps_mess_fail.setTransq(undoMsg.getTransq());

				ApsMessFailDao.insert(tbaps_mess_fail); // 插入异常消息表
			} finally {
				// 6.移除未处理消息表记录
				// ApsMessUndoDao.deleteOne_odb2(undoMsg.getTrandt(),
				// undoMsg.getBusisq(), undoMsg.getTransq(),
				// undoMsg.getMesseq(), undoMsg.getSosyco(),
				// undoMsg.getTopcid());
				ApsMessUndoDao.deleteOne_odb2(undoMsg.getMessid());
			}
		}
	}

}
