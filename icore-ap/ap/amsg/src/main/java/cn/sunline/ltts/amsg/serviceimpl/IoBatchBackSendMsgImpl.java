
package cn.sunline.ltts.amsg.serviceimpl;

import cn.sunline.ltts.amsg.util.SMSUtil;

 /**
  * 批量回调发送短信服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoBatchBackSendMsgImpl", longname="批量回调发送短信服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoBatchBackSendMsgImpl implements cn.sunline.ltts.busi.bsap.servicetype.IoBatchBackSendMsg{
 /**
  * 批量回调点异常短信处理
  *
  */
	
	public void batchExceptionSendMsg(final cn.sunline.ltts.busi.bsap.type.ApMessageComplexType.SMSCType smscType){
			SMSUtil.sendSMSMessage(smscType);
	}
}

