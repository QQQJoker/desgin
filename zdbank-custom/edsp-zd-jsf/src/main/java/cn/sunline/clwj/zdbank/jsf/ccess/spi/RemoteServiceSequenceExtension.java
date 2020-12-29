package cn.sunline.clwj.zdbank.jsf.ccess.spi;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.service.engine.spi.ServiceSequenceExtension;
import cn.sunline.adp.cedar.service.executor.ServiceExecutorContext;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.edsp.base.util.date.DateUtil;

import org.springframework.core.annotation.Order;

@SPIMeta(id = "default")
@Order(100)
public class RemoteServiceSequenceExtension implements ServiceSequenceExtension {

	@Override
	public String getServiceCallSequence(ServiceExecutorContext var1) {
		StringBuilder sb = new StringBuilder();
		sb.append(CoreUtil.getSubSystemId());
		sb.append(DateUtil.getNow("HHmmssSSS"));
		sb.append(CommUtil.lpad(CoreUtil.nextValue("service_call_seq"), 9, "0"));
		var1.getServiceRequest().getRequestHeader().setCallSeqNo(sb.toString());
		return sb.toString();
	}

}
