package cn.sunline.clwj.zdbank.jsf.ccess.spi;

import java.util.Map;

import cn.sunline.adp.cedar.service.executor.ServiceExecutorContext;
import cn.sunline.edsp.base.factories.SPI;

@SPI
public interface HttpRequestHeaderHandler {
	
 public Map<String,Object> processHeader(ServiceExecutorContext context);
	
}
