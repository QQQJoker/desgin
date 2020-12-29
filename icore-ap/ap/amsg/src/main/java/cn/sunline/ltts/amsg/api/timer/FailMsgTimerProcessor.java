package cn.sunline.ltts.amsg.api.timer;

import java.util.List;

import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.server.batch.timer.LttsTimerProcessor;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessData;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessDataDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessFail;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessFailDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessSucc;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessSuccDao;
import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;

public class FailMsgTimerProcessor  extends LttsTimerProcessor{

	@Override
	public void process(String str, DataArea data) {
		//1.查询异常信息表
		List<ApsMessFail> listExMsg = null;//TODO 
		
		for(ApsMessFail exMsg:listExMsg){
			//2.获取消息内容
			ApsMessData  tbAps_mess_data = ApsMessDataDao.selectOne_odb1(exMsg.getBusisq(), exMsg.getMessid(), true);
			
			try{
				//3.发送消息 
//TODO			AsyncMessageUtil.add(tbAps_mess_data.getTopcid(),tbAps_mess_data.getMedata().getClass(), tbAps_mess_data.getMedata(), SUCCESS_FLAG.SUCCESS);
				AsyncMessageUtil.publishOrSave(E_MSGOPT.SUCESS);
				
				//4.登记处理消息成功表
			   ApsMessSucc tbaps_mess_succ = CommTools.getInstance(ApsMessSucc.class);
			   tbaps_mess_succ.setBusisq(exMsg.getBusisq());
			   tbaps_mess_succ.setDestdt(exMsg.getDestdt());
			   tbaps_mess_succ.setDestip(exMsg.getDestip());
			   tbaps_mess_succ.setDevmid(exMsg.getDevmid());
			   tbaps_mess_succ.setMesseq(exMsg.getMesseq());
			   tbaps_mess_succ.setMessid(exMsg.getMessid());
			   tbaps_mess_succ.setPrvmid(exMsg.getPrvmid());
			   tbaps_mess_succ.setSdcnid(exMsg.getSdcnid());
			   tbaps_mess_succ.setSosyco(exMsg.getSosyco());
			   tbaps_mess_succ.setSourip(exMsg.getSourip());
			   tbaps_mess_succ.setSovmid(exMsg.getSovmid());
			   tbaps_mess_succ.setTopcid(exMsg.getTopcid());
			   tbaps_mess_succ.setTragti(exMsg.getTragti()+1); //重试次数
		   	   tbaps_mess_succ.setTrandt(exMsg.getTrandt());
		   	   tbaps_mess_succ.setTransq(exMsg.getTransq());
		      ApsMessSuccDao.insert(tbaps_mess_succ);
				//5.移除异常信息表
				ApsMessFailDao.deleteOne_odb2(exMsg.getMessid());
				//.deleteOne_odb1(exMsg.getTrandt(), exMsg.getBusisq(), exMsg.getTransq(), exMsg.getMesseq(), exMsg.getSosyco(), exMsg.getTopcid());
				
			}catch(Exception e){
				//5.异常处理失败，重试次数加一
				ApsMessFail tbaps_mess_fail = CommTools.getInstance(ApsMessFail.class);
				   tbaps_mess_fail.setBusisq(exMsg.getBusisq());
				   tbaps_mess_fail.setDestdt(exMsg.getDestdt());
				   tbaps_mess_fail.setDestip(exMsg.getDestip());
				   tbaps_mess_fail.setDevmid(exMsg.getDevmid());
				   tbaps_mess_fail.setEstack(e.getStackTrace().toString());  //异常堆栈
				   tbaps_mess_fail.setExcode("");  //异常错误码 TODO
				   tbaps_mess_fail.setMesseq(exMsg.getMesseq());
				   tbaps_mess_fail.setMessid(exMsg.getMessid());
				   tbaps_mess_fail.setPrvmid(exMsg.getPrvmid());
				   tbaps_mess_fail.setSdcnid(exMsg.getSdcnid());
				   tbaps_mess_fail.setSosyco(exMsg.getSosyco());
				   tbaps_mess_fail.setSourip(exMsg.getSourip());
				   tbaps_mess_fail.setSovmid(exMsg.getSovmid());
				   tbaps_mess_fail.setTopcid(exMsg.getTopcid());
				   tbaps_mess_fail.setTragti(exMsg.getTragti()+1);  //重试次数
				   tbaps_mess_fail.setTrandt(exMsg.getTrandt());
				   tbaps_mess_fail.setTransq(exMsg.getTransq());
				   ApsMessFailDao.updateOne_odb2(tbaps_mess_fail); //更新记录
			}
		}
	}

}
