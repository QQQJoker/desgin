package cn.sunline.ltts.busi.aplt.impl;

import java.util.Map;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.server.online.packdata.OnlineAccessPackDataAdapter;
import cn.sunline.edsp.base.factories.SPIMeta;

@SPIMeta(id = "bsapDataAdapter")
@Order(1000)
public class BsapOnlineAccessPackDataAdapter implements OnlineAccessPackDataAdapter {

	@Override
	public DataArea toRequestData(RequestData requestData) {
	
		return requestData.getBody();
	}

	@Override
	public DataArea toResponseData(RequestData requestData, ResponseData responseData) {
	        Map<String, Object> headMap =  responseData.getHeaderData();
	        
	        if (!headMap.isEmpty()) {
	        	responseData.getHeaderData().remove("profile");
	            responseData.getData().setSystem(responseData.getHeaderData());
	            responseData.getData().getSystem().remove("profile");
	        }
	        return responseData.getData();

	}

}
