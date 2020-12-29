
package cn.sunline.clwj.transaction;

import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvntDao;
import cn.sunline.ltts.busi.aplt.transaction.ApStrikeEvntProcessor;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

/**
  * TXC分布式事务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="TXCServiceImpl", longname="TXC分布式事务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class TXCServiceImpl implements cn.sunline.adp.cedar.service.servicetype.TXCService{
 /**
  * 冲正服务
  *
  */
	public void rollback(final cn.sunline.adp.cedar.service.servicetype.TXCService.rollback.Input input){	
		
		Map<String, Object> inputHeader = JsonUtil.parse(input.getInput_head());
		String origInitiatorSeq = (String)inputHeader.get(HeaderDataConstants.CALL_SEQ_NO);  //原外调流水，分支事务流水
		String trandt = (String)inputHeader.get(HeaderDataConstants.TRAN_TIMESTAMP);  //原交易日期
		
		List<KnbEvnt> evnt = KnbEvntDao.selectAll_odb4(origInitiatorSeq, trandt, false);
		
		if(evnt == null) {
			return;  // 如果被调方也是分布式事务，出现循环嵌套事务
		}
		
		for (KnbEvnt knbEvnt : evnt) { // 循环处理事件
			try {
	            String impl = ApStrikeEvntProcessor.implClazzPrefix + knbEvnt.getTranev();
	            ApStrikeEvntProcessor processor = (ApStrikeEvntProcessor) Class.forName(impl).newInstance();
	            processor.process(E_STACPS.POSITIVE, E_COLOUR.RED, knbEvnt);

	        } catch (InstantiationException e) {
	            throw Aplt.E0000("事件[" + knbEvnt.getTranev() + "]冲正处理失败", e);
	        } catch (IllegalAccessException e) {
	            throw Aplt.E0000("事件[" + knbEvnt.getTranev() + "]冲正处理失败", e);
	        } catch (ClassNotFoundException e) {
	            throw Aplt.E0000("事件[" + knbEvnt.getTranev() + "]暂不支持冲正", e);
	        } catch (Exception e) {
	            throw e;
	        }
		}
		
		
		
	}
 /**
  * 二次提交服务
  *
  */
	public void commit2(final cn.sunline.adp.cedar.service.servicetype.TXCService.commit2.Input input){
		input.getAttachment(); // 附件信息
		input.getInput_head(); // 原服务输入头信息
		input.getInput_body(); // 原服务数据体
		input.getTarget_dcn(); // 目标DCN
		
	}
}

